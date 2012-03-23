package org.bagofcode.scala.test.cache

import org.bagofcode.scala.cache._
import org.scalatest.FunSuite

class CacheVarSpec extends FunSuite {
	test("returns the functions value") {
	  var cache = new CacheVar("test",5000)
	  assert(cache.get == "test")
	}
	
	test("is expired properly") {
	  var cache = new CacheVar("test", 1000)
	  assert(cache.expired)
	  assert(cache.get == "test")
	  assert(!cache.expired)
	  Thread.sleep(2000)
	  assert(cache.expired)
	  assert(cache.get == "test")
	}
	
	test("reset works as intended") {
	  var cache = new CacheVar("test", 5000)
	  (cache.get == "test")
	  cache.reset
	  (cache.expired)
	}
}