package com.solace.cf4j;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.google.common.collect.Lists;
import com.solace.cf4j.config.Caches;
import com.solace.cf4j.config.Caches.CacheConfig;
import com.solace.cf4j.config.Caches.CacheConfig.Type;

public class CacheAccessorTests {

	Caches c;
	CacheConfig config;

	@Before
	public void setup() {
		c = new Caches();

		config = new CacheConfig();
		config.setName("test");

		Type type = new Type();
		type.setValue(MyCache.class);

		config.setType(type);

		c.setCaches(Lists.newArrayList(config));
	}

	@Test
	public void test_load_of_app_scope_accessor() {
		CacheAccessor.addConfiguration(config);

		Cache c = CacheAccessor.newApplicationScopeCache("test");

		CacheAccessor a = (CacheAccessor) c;

		assertNotNull(c);
		assertNotNull(a.m_cache);
		assertSame(MyCache.class, a.m_cache.getClass());
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

	public static class MyCache implements Cache {
		
		Map<String, MyData> data = new ConcurrentHashMap<String, CacheAccessorTests.MyData>();

		public MyCache(Caches.CacheConfig config) {

		}

		public boolean set(Cacheable _obj) throws CacheException {
			// TODO Auto-generated method stub
			return true;
		}

		public <T> boolean set(String _key, T _obj)
				throws CacheException {
			data.put(_key, (MyData)_obj);
			
			return true;
		}

		public Future<Boolean> setAsync(Cacheable _obj) throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public <T> Future<Boolean> setAsync(String _key,
				T _obj) throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public <T> T get(Cacheable _key)
				throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public <T> T get(Cacheable _key,
				Callable<T> ifNotFound) throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public <T> T get(String _key)
				throws CacheException {
			return (T)data.get(_key);
		}

		public <T> T get(String _key,
				Callable<T> ifNotFound) throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean delete(Cacheable _key) throws CacheException {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean delete(String _key) throws CacheException {
			// TODO Auto-generated method stub
			return false;
		}

		public Future<Boolean> deleteAsync(Cacheable _key)
				throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public Future<Boolean> deleteAsync(String _key) throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public String getRegionName() throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public void setRegionName(String _name) throws CacheException {
			// TODO Auto-generated method stub

		}

		public Map<String, String> getParameters() {
			// TODO Auto-generated method stub
			return null;
		}

		public void clear() throws CacheException {
			// TODO Auto-generated method stub

		}

		public void shutdown() throws CacheException {
			// TODO Auto-generated method stub

		}

	}

}
