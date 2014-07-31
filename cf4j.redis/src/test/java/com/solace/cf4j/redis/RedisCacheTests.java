package com.solace.cf4j.redis;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fiftyonred.mock_jedis.MockJedis;
import com.google.common.base.Joiner;
import com.solace.cf4j.config.Caches.CacheConfig;
import com.solace.cf4j.config.Caches.CacheConfig.Type;
import com.solace.cf4j.redis.serialization.JsonSerializer;

public class RedisCacheTests {

	CacheConfig config;
	RedisCache cache;

	/**
	 * Overload the concrete implementation to guarantee that we can inject a
	 * mock client
	 */
	@Before
	public void setup() {
		config = new CacheConfig();
		Type t = new Type();
		t.setValue(RedisCache.class);
		t.setProperties(new HashMap<String, String>() {
			{
				put("test", Boolean.TRUE.toString());
				put(Keys.SERIALIZATION_STRATEGY,
						Data.DataSerializationStrategy.class.getName());
			}
		});

		config.setType(t);

		cache = new RedisCache(config);

		cache.jedis = new MockJedis("test");
	}

	@After
	public void tearDown() {
		if (cache != null)
			cache.shutdown();
	}

	@Test
	public void test_set() {
		cache.set("1", new Data("foo"));
		Data d = cache.<Data> get("1");
		assertNotNull(d);
		assertTrue("foo".equals(d.getValue()));
	}

	@Test
	public void test_long_key() {
		Joiner j = Joiner.on(",");
		int cnt = 1000;
		List<Integer> vals = new ArrayList<Integer>();
		for (int i = 0; i < cnt; i++)
			vals.add(i);

		String key = j.join(vals);

		cache.set(key, new Data("foo"));
		Data d = cache.<Data> get(key);
		assertNotNull(d);
		assertTrue("foo".equals(d.getValue()));
	}

	@Test
	public void test_async_set() throws InterruptedException,
			ExecutionException {
		Future<Boolean> f = cache.setAsync("1", new Data("foo"));
		boolean r = f.get().booleanValue();

		if (r) {
			Data d = cache.<Data> get("1");
			assertNotNull(d);
			assertTrue("foo".equals(d.getValue()));
		} else {
			fail("did not successfully set async.");
		}
	}

	@Test
	public void test_delete() {
		cache.set("1", new Data("foo"));
		cache.delete("1");
		Data d = cache.<Data> get("1");
		assertNull(d);
	}

	@Test
	public void test_async_delete() throws InterruptedException,
			ExecutionException {
		cache.set("1", new Data("foo"));
		Future<Boolean> b = cache.deleteAsync("1");
		boolean r = b.get().booleanValue();
		if (r) {
			Data d = cache.<Data> get("1");
			assertNull(d);
		} else {
			fail("did not delete async.");
		}
	}

	@Test
	public void test_failed_get_and_load() {
		Data d = cache.get("1", new Callable<Data>() {
			public Data call() throws Exception {
				return new Data("foo");
			}
		});
				
		assertNotNull(d);
		assertTrue("foo".equals(d.getValue()));
	}

	public static class Data {
		String val;

		public Data() {

		}

		public Data(String val) {
			this.val = val;
		}

		public String getValue() {
			return val;
		}

		public void setValue(String v) {
			this.val = v;
		}

		public static class DataSerializationStrategy extends
				JsonSerializer<Data> {

			private ObjectMapper mapper = new ObjectMapper();

			public DataSerializationStrategy() {
				super(Data.class);
				mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,
						false);
			}

			@Override
			protected ObjectMapper mapper() {
				return mapper;
			}
		}
	}

}
