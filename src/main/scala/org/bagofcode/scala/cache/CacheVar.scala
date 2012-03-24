package org.bagofcode.scala.cache

import scala.actors.Actor
import scala.actors.TIMEOUT

class CacheVar[T](func: => T, timeout: Int) {
  private var value: Option[T] = None
  private var created: Long = 0

  def get = {
    check
    value match {
      case Some(v) => v
      case None => this.synchronized {
        value match {//so we dont set it twice
          case Some(v) => v
          case None => {
            value = Some(func)
            created = System.currentTimeMillis
            watcher ! TIMEOUT
            value.get
          }
        }
      }
    }
  }

  def reset = this.synchronized { value = None; created = 0; watcher ! TIMEOUT }

  def check = if (expired) reset

  def expired = expiresOn <= System.currentTimeMillis

  def expiresOn = created + timeout

  private lazy val watcher = new CacheVarWatcher(this)
}