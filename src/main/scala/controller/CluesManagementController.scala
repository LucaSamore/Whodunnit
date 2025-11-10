package controller

import model.ModelModule
import model.game.{CaseFile, CaseKnowledgeGraph, Character, CustomEntity, Entity, Link}

trait CluesManagementController extends ControllerModule.Controller:
  def getEntities: Seq[Entity]
  def getRelationships: Seq[(Entity, Link, Entity)]
  def addRelationship(from: Entity, link: Link, to: Entity): Unit
  def removeRelationship(from: Entity, link: Link, to: Entity): Unit
  def createEntity(entity: Entity): Unit
  def findOrCreateEntity(name: String): Entity
  def modifyRelationship(
      oldRel: (Entity, Link, Entity),
      newRel: (Entity, Link, Entity)
  ): Unit
  def cleanOrphanEntities(): Unit
  def getEntityDisplayName(entity: Entity): String

object CluesManagementController:
  def apply(model: ModelModule.Model): CluesManagementController =
    new CluesManagementControllerImpl(model)

  private class CluesManagementControllerImpl(model: ModelModule.Model)
      extends ControllerModule.AbstractController(model)
      with CluesManagementController:

    private def knowledgeGraph: CaseKnowledgeGraph =
      model.state.currentGraph.getOrElse(
        throw new IllegalStateException("Knowledge graph not initialized")
      )

    override def getEntities: Seq[Entity] =
      val graphEntities = knowledgeGraph.nodes.toSeq
      val caseEntities = model.state.investigativeCase.fold(Seq.empty[Entity]): c =>
        c.characters ++ c.caseFiles
      (graphEntities ++ caseEntities).distinct

    override def getRelationships: Seq[(Entity, Link, Entity)] =
      knowledgeGraph.edges.toSeq

    override def addRelationship(from: Entity, link: Link, to: Entity): Unit =
      val graph = knowledgeGraph
      if !graph.nodes.contains(from) then createEntity(from)
      if !graph.nodes.contains(to) then createEntity(to)
      graph.addEdge(from, link, to)
      saveGraph(graph)

    override def removeRelationship(
        from: Entity,
        link: Link,
        to: Entity
    ): Unit =
      val graph = knowledgeGraph
      graph.removeEdge(from, link, to)
      cleanOrphanEntities()

    override def createEntity(entity: Entity): Unit =
      val graph = knowledgeGraph
      graph.addNode(entity)

    override def findOrCreateEntity(name: String): Entity =
      getEntities.find(e => getEntityDisplayName(e) == name) match
        case Some(existingEntity) => existingEntity
        case None                 =>
          val newEntity = CustomEntity(entityType = name, content = None)
          createEntity(newEntity)
          newEntity

    override def modifyRelationship(
        oldRel: (Entity, Link, Entity),
        newRel: (Entity, Link, Entity)
    ): Unit =
      removeRelationship(oldRel._1, oldRel._2, oldRel._3)
      addRelationship(newRel._1, newRel._2, newRel._3)

    override def cleanOrphanEntities(): Unit =
      val graph = knowledgeGraph
      val usedEntities = graph.edges.flatMap((from, _, to) => Set(from, to))
      val caseEntities = model.state.investigativeCase.fold(Set.empty[Entity])(c => c.characters ++ c.caseFiles)
      graph.nodes
        .collect:
          case ce: CustomEntity => ce
        .filterNot(e => usedEntities(e) || caseEntities(e))
        .foreach(graph.removeNode)
      saveGraph(graph)

    override def getEntityDisplayName(entity: Entity): String = entity match
      case Character(name, _)             => name
      case CaseFile(title, _, _, _, _, _) => title
      case CustomEntity(entityType, _)    => entityType

    private def saveGraph(graph: CaseKnowledgeGraph): Unit =
      model.updateState(_.addGraphToHistory(graph.deepCopy()))
