package controller

import model.ModelModule
import model.game.{CaseFile, CaseKnowledgeGraph, Character, CustomEntity, Entity, Link}

trait CluesManagementController extends ControllerModule.Controller:
  def getEntities: Seq[Entity]
  def getRelationships: Seq[(Entity, Link, Entity)]
  def addAndSaveRelationship(from: Entity, link: Link, to: Entity): Unit
  def removeAndSaveRelationship(from: Entity, link: Link, to: Entity): Unit
  def findOrCreateEntity(name: String): Entity
  def modifyRelationship(oldRel: (Entity, Link, Entity), newRel: (Entity, Link, Entity)): Unit
  def getEntityDisplayName(entity: Entity): String

object CluesManagementController:
  def apply(model: ModelModule.Model): CluesManagementController = new CluesManagementControllerImpl(model)

  private final class CluesManagementControllerImpl(model: ModelModule.Model)
      extends ControllerModule.AbstractController(model) with CluesManagementController:

    private def currentGraph: CaseKnowledgeGraph =
      model.state.currentGraph.getOrElse(throw new IllegalStateException("Knowledge graph not initialized"))

    override def getEntities: Seq[Entity] =
      val graphEntities = currentGraph.nodes.toSeq
      val caseEntities = model.state.investigativeCase.fold(Seq.empty[Entity]): investigativeCase =>
        investigativeCase.characters.toSeq ++ investigativeCase.caseFiles.toSeq
      (graphEntities ++ caseEntities).distinct

    override def getRelationships: Seq[(Entity, Link, Entity)] = currentGraph.edges.toSeq

    override def addAndSaveRelationship(from: Entity, link: Link, to: Entity): Unit =
      val graph = currentGraph.deepCopy()
      addRelationship(graph, from, link, to)
      saveGraph(graph)

    override def removeAndSaveRelationship(from: Entity, link: Link, to: Entity): Unit =
      val graph = currentGraph.deepCopy()
      removeRelationship(graph, from, link, to)
      cleanOrphansOnGraph(graph)
      saveGraph(graph)

    override def findOrCreateEntity(name: String): Entity =
      getEntities.find(entity => getEntityDisplayName(entity) == name).getOrElse(CustomEntity(entityType = name))

    override def modifyRelationship(
        oldRelationship: (Entity, Link, Entity),
        newRelationship: (Entity, Link, Entity)
    ): Unit =
      val (oldFrom, oldLink, oldTo) = oldRelationship
      val (newFrom, newLink, newTo) = newRelationship
      val graph = currentGraph.deepCopy()
      removeRelationship(graph, oldFrom, oldLink, oldTo)
      addRelationship(graph, newFrom, newLink, newTo)
      cleanOrphansOnGraph(graph)
      saveGraph(graph)

    override def getEntityDisplayName(entity: Entity): String = entity match
      case Character(characterName, _)        => characterName
      case CaseFile(fileTitle, _, _, _, _, _) => fileTitle
      case CustomEntity(entityType)           => entityType

    private def addRelationship(graph: CaseKnowledgeGraph, from: Entity, link: Link, to: Entity): Unit =
      if !graph.nodes.contains(from) then graph.addNode(from)
      if !graph.nodes.contains(to) then graph.addNode(to)
      graph.addEdge(from, link, to)

    private def removeRelationship(graph: CaseKnowledgeGraph, from: Entity, link: Link, to: Entity): Unit =
      graph.removeEdge(from, link, to)

    private def cleanOrphansOnGraph(graph: CaseKnowledgeGraph): Unit =
      val entitiesUsedInRelationships = graph.edges.flatMap: (fromEntity, _, toEntity) =>
        Set(fromEntity, toEntity)

      val charactersDeclaredInCase = model.state.investigativeCase
        .fold(Set.empty[Character])(investigativeCase => investigativeCase.characters)

      val orphanNodes = graph.nodes.toSeq.filterNot: candidateNode =>
        val isUsedInRelationship = entitiesUsedInRelationships.contains(candidateNode)
        val isCaseCharacter = candidateNode match
          case character: Character => charactersDeclaredInCase.contains(character)
          case _                    => false
        isUsedInRelationship || isCaseCharacter

      orphanNodes.foreach(orphanNode => graph.removeNode(orphanNode))

    private def saveGraph(graph: CaseKnowledgeGraph): Unit =
      model.updateState(_.addGraphToHistory(graph.deepCopy()))
