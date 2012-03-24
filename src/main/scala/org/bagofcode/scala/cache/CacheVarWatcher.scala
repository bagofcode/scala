package org.bagofcode.scala.cache

import scala.actors.Actor
import scala.actors.Actor.State.Terminated
import scala.actors.TIMEOUT
import scala.ref.WeakReference
import java.lang.System.currentTimeMillis

class CacheVarWatcher private (toWatch: WeakReference[CacheVar[_]]) extends Actor {
  def this(watch: CacheVar[_]) = this(new WeakReference(watch))

  private def timeout = toWatch.get match {
    case Some(v) => if(v.expired) 5000 else v.expiresOn - currentTimeMillis
    case None => exit
  }

  override def act = loop {
    receiveWithin(timeout) {
      case TIMEOUT => toWatch.get match {
        case Some(v) => v.check
        case None => exit
      }
    }
  }
  
  start
}