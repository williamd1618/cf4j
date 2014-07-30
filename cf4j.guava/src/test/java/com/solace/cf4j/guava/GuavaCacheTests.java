package com.solace.cf4j.guava;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.*;

import com.google.common.collect.Lists;
import com.solace.cf4j.Cache;
import com.solace.cf4j.CacheAccessor;
import com.solace.cf4j.config.Caches;
import com.solace.cf4j.config.Caches.CacheConfig;
import com.solace.cf4j.config.Caches.CacheConfig.Type;

import static org.junit.Assert.*;

public class GuavaCacheTests {

	Caches c;
	CacheConfig config;
	
	
	@Before
	public void setup() {
		c = new Caches();

		config = new CacheConfig();
		config.setName("test");

		Type type = new Type();
		type.setValue(GuavaCache.class);

		config.setType(type);

		c.setCaches(Lists.newArrayList(config));
		
		CacheAccessor.addConfiguration(config);
	}
	
	
	@Test
	public void test_app_scope_get_hit() {
		MyData o = new MyData();

		CacheAccessor.addConfiguration(config);

		Cache cache = CacheAccessor.newApplicationScopeCache("test");

		assertTrue(cache.set("test", o));
	}

	/**
	 * Will create a mock cache with an in-memory map.
	 * Will create two Callables, an executor service to put and get with same key
	 * and assert different values.
	 */
	@Test
	public void test_load_of_tl_scope_accessor() {
		CacheAccessor.addConfiguration(config);
		
		Callable<MyData> c1 = new Callable<MyData>() {
			public MyData call() throws Exception {
				Cache c = CacheAccessor.newThreadLocalScopeCache("test");
				c.set("test", new MyData(Long.toString(System.nanoTime())));
				return c.get("test");
			}
		};
		
		ExecutorService e = Executors.newCachedThreadPool();
		
		Future<MyData> first = e.submit(c1);
		Future<MyData> second = e.submit(c1);
		
		try {
			MyData res1 = first.get();
			MyData res2 = second.get();
			
			System.out.println(res1.value());
			System.out.println(res2.value());
			
			assertNotSame(res1.value(), res2.value());
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}

	public static class MyData implements Serializable {

		String val;

		public MyData() {

		}

		public MyData(String v) {
			this.val = v;
		}

		public String value() {
			return this.val;
		}
	}
}
