package model

import model.generation.*
import model.game.{Case, GameState}

object ModelModule:

  trait Model[S]:
    val producer: Producer[Case]
    var gameState: GameState

  trait Provider[S]:
    val model: Model[S]

  trait Component[S]:

    class ModelImpl extends Model[S]:
      import Producers.given
      val producer: Producer[Case] = summon[Producer[Case]]
      var gameState: GameState = GameState.empty()

  trait Interface[S] extends Provider[S] with Component[S]:
    def Model(): Model[S] = new ModelImpl()
