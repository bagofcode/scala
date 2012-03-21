package org.bagofcode.scala.cache


import scala.actors.Actor
import scala.actors.TIMEOUT

class CacheVar[T](func: => T, timeout: Int) {
  private var value: Option[T] = None
  private var created: Long = 0

  def get = this.synchronized {
    check
    value match {
      case Some(v) => v
      case None => this.synchronized {
        value match {
          case Some(v) => v
          case None => set 
        }
      }
    }
  }

  private def set = {
    value = Some(func)
    created = System.currentTimeMillis
    watcher.run
    value.get
  }

  def reset = this.synchronized{ value = None;created = 0; watcher ! Stop }

  def check = if (expired) reset

  def expired = expiresOn <= System.currentTimeMillis

  def expiresOn = created + timeout

  private lazy val watcher = new CacheVarWatcher(this)
}