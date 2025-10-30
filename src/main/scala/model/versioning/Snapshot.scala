package model.versioning

import java.time.LocalDateTime

class Snapshot[A](val subject: A, val timestamp: LocalDateTime)

object Snapshot:
  def snap[A](subject: A): Snapshot[A] = {
    new Snapshot(subject, LocalDateTime.now())
  }

  def restore[A](snapshot: Snapshot[A]): A = {
    snapshot.subject
  }