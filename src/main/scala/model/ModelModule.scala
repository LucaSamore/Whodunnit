package model

import model.generation.*
import cats.effect.IO
import model.game.{Case, GameState, Timer}
import scala.concurrent.duration.DurationInt

object ModelModule:

  trait Model[S]:
    val producer: Producer[Case]
    var gameState: GameState

    def generateNewCase(
        theme: Option[String],
        difficulty: Constraint.Difficulty,
        customConstraints: Seq[Constraint] = Seq.empty
    ): IO[Either[ProductionError, Case]]

    def initializeGame(generateCase: Case): GameState
    def startTimer(): Unit

  trait Provider[S]:
    val model: Model[S]

  trait Component[S]:

    class ModelImpl extends Model[S]:
      import Producers.given
      val producer: Producer[Case] = summon[Producer[Case]]

      var gameState: GameState = GameState.empty()

      private def generateCase(constraints: Seq[Constraint])
          : IO[Either[ProductionError, Case]] =
        IO(producer.produce(constraints*))

      def generateNewCase(
          theme: Option[String],
          difficulty: Constraint.Difficulty,
          customConstraints: Seq[Constraint] = Seq.empty
      ): IO[Either[ProductionError, Case]] =
        val baseConstraints = Seq(difficulty) ++ customConstraints
        val allConstraints = theme match
          case Some(t) => baseConstraints :+ Constraint.Theme(t)
          case None    => baseConstraints
        generateCase(allConstraints)

      override def initializeGame(generatedCase: Case): GameState =
        val timer = Timer(30.seconds)
        val state = GameState.initialize(generatedCase, timer)
        gameState = state
        gameState

      override def startTimer(): Unit =
        gameState.timer.foreach { timer =>
          timer.start()
        }

  trait Interface[S] extends Provider[S] with Component[S]:
    def Model(): Model[S] = new ModelImpl()
