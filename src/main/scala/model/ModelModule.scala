package model

import cats.effect.IO
import model.casegeneration.*
import model.casegeneration.Constraint.{Difficulty, Theme}

object ModelModule:

  trait Model[S]:
    def generateCase(constraints: Seq[Constraint]): IO[Either[ProductionError, Case]]

  trait Provider[S]:
    val model: Model[S]

  trait Component[S]:

    class ModelImpl extends Model[S]:
      import Producers.given 
      override def generateCase(constraints: Seq[Constraint]): IO[Either[ProductionError, Case]] =
        CaseGenerationModel.apply(summon[Producer[Case]]).generateCase(constraints)

  trait Interface[S] extends Provider[S] with Component[S]:
    def Model(): Model[S] = new ModelImpl()
