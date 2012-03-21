package org.bagofcode.scala.test.cache

import org.junit.Test
import org.bagofcode.scala.cache._

class CacheVarTest {
	@Test def value = {
	  var cache = new CacheVar("test",5000)
	  assert(cache.get == "test")
	}
	
	@Test def expired = {
	  var cache = new CacheVar("test",1000)
	  assert(cache.expired)
	  assert(cache.get == "test")
	  assert(!cache.expired)
	  Thread.sleep(2000)
	  assert(cache.expired)
	  assert(cache.get == "test")
	}
	
	@Test def reset = {
	  var cache = new CacheVar("test",5000)
	  assert(cache.get == "test")
	  cache.reset
	  assert(cache.expired)
	}
}