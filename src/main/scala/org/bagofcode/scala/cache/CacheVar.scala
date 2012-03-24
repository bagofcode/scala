package org.bagofcode.scala.cache

import scala.actors.Actor
import scala.actors.TIMEOUT

class CacheVar[T](func: => T, lifespan: Int) {
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
            CacheVarWatcher ! Register(this)
            value.get
          }
        }
      }
    }
  }

  def reset = this.synchronized { value = None; created = 0 }

  def check = if (expired) reset

  def expired = expiresOn <= System.currentTimeMillis

  def expiresOn = created + lifespan
}