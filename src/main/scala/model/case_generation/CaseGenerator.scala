package model.case_generation

trait CaseGenerator:
  def generate(constraints: Constraint*): Either[Error, Case]
