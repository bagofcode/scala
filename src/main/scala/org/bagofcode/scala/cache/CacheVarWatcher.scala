package org.bagofcode.scala.cache

import scala.actors.Actor
import scala.actors.Actor.State.Terminated
import scala.actors.TIMEOUT
import scala.ref.WeakReference
import java.lang.System.currentTimeMillis

object CacheVarWatcher extends Actor {

  private val vars = new java.util.TreeMap[Long, WeakReference[CacheVar[_]]]

  private def timeout: Long = if (vars.isEmpty()) 60000
  else {
    var firtstEntry = vars.firstEntry
    firtstEntry.getValue.get match {
      case Some(v) => { var ret:Long = 0
        v.synchronized {
          if (v.expired) vars.remove(firtstEntry.getKey)
          else ret = v.expiresOn - currentTimeMillis
        }
      	if(ret != 0) ret else timeout
      }
      case None => { vars.remove(firtstEntry.getKey); timeout }
    }
  }

  override def act = loop {
    receiveWithin(timeout) {
      case TIMEOUT =>
      case Register(v) => vars.put(v.expiresOn, new WeakReference(v))
    }
  }

  def size = vars.size()

  start
}

case class Register(toRegister: CacheVar[_])