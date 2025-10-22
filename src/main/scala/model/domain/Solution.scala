package model.domain

sealed trait Solution:
  def culprit: Character
  def motive: String
