package model

import model.casegeneration.*
import model.knowledgegraph.CaseKnowledgeGraph

object ModelModule:

  trait Model[S]:
    def createNothing(): Unit

  trait Provider[S]:
    val model: Model[S]

  trait Component[S]:
    class ModelImpl extends Model[S]:
      override def createNothing(): Unit = println("Creating nothing...")

  trait Interface[S] extends Provider[S] with Component[S]:
    def Model(): Model[S] = new ModelImpl()
