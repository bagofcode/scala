package org.bagofcode.scala.cache

import scala.actors.Actor
import scala.actors.TIMEOUT
import scala.ref.WeakReference
import java.lang.System.{currentTimeMillis => millis}
import scala.collection.mutable.PriorityQueue

object CacheVarWatcher extends Actor {
  type T = (Long, WeakReference[CacheVar[_]])

  implicit object VarsOrdering extends Ordering[T] {
    override def compare(x: T, y: T) = Ordering.Long.compare(x._1,y._1)
  }

  private val vars = new PriorityQueue[T]

  private def timeout: Long = if (vars.size == 0) 60000
  else {
    var top = vars.dequeue
    if (top._1 < millis)
      top._2.get match {
        case Some(v) => {
          v.synchronized {
            v.check
            if (!v.expired) vars.enqueue((v.expiresOn, top._2))
          }
          timeout
        }
        case None => timeout
      }
    else {
      var ret = top._1 - millis
      if (ret <= 0) ret = 1
      ret
    }
  }

  override def act = loop {
    receiveWithin(timeout) {
      case TIMEOUT =>
      case Register(v) => vars.enqueue((v.expiresOn, new WeakReference(v)))
    }
  }

  def size = vars.size

  start
}

case class Register(toRegister: CacheVar[_])