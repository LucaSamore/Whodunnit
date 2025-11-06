package model

import model.casegeneration.*
import cats.effect.IO

object ModelModule:

  trait Model[S]:
    val producer: Producer[Case]

    def generateNewCase(
        theme: Option[String],
        difficulty: Constraint.Difficulty,
        customConstraints: Seq[Constraint] = Seq.empty
    ): IO[Either[ProductionError, Case]]

  trait Provider[S]:
    val model: Model[S]

  trait Component[S]:

    class ModelImpl extends Model[S]:
      import Producers.given
      val producer: Producer[Case] = summon[Producer[Case]]

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

  trait Interface[S] extends Provider[S] with Component[S]:
    def Model(): Model[S] = new ModelImpl()
