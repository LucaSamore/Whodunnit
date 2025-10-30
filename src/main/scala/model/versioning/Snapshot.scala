package model.versioning

class Snapshot[A](val subject: A)

object Snapshot:
  def snap[A](subject: A): Snapshot[A] = new Snapshot(subject)
