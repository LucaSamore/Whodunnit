package model

import model.domain.*
import model.domain.types.*
import ujson.*

import scala.util.Try

class Parser:
  def parse(input: String): Try[Case] =
    for
      json <- Try(ujson.read(input))
      plot = json("plot").str
    yield Case(
      Plot(plot),
      Set.empty,
      Set.empty,
      CaseSolution(Set.empty, Character("", CaseRole.Suspect), "")
    )