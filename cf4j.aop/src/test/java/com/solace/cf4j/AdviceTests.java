package com.solace.cf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

import com.solace.cf4j.*;
import com.solace.cf4j.annotations.Cached;
import com.solace.cf4j.config.Caches;
import com.solace.cf4j.config.Caches.CacheConfig;
import com.solace.cf4j.config.Caches.CacheConfig.Type;

public class AdviceTests {

	CacheConfig config;

	@Before
	public void setup() {
		config = new CacheConfig();
		config.setName("test");
		Type t = new Type();
		t.setValue(MyCache.class);
		t.setProperties(new HashMap<String, String>() {
			{
				put("test", Boolean.TRUE.toString());
			}
		});

		config.setType(t);

		CacheAccessor.addConfiguration(config);
	}

	@Test
	public void test_el() {
		BusinessDelegate del = new BusinessDelegate();

		Person p = del.getPersonWithEl("Daniel", "Williams");

		Person p2 = del.getPersonWithEl("Daniel", "Williams");

		assertTrue(p.hashCode() != p2.hashCode());
	}

	@Test
	public void test_delete() {
		BusinessDelegate del = new BusinessDelegate();

		Person p = del.getPersonWithEl("Daniel", "Williams");

		Person p2 = del.refresh("Daniel", "Williams");

		assertTrue(p.hashCode() != p2.hashCode());
	}

	@Test
	public void test_set_cache_location() {
		BusinessDelegate del = new BusinessDelegate();

		del.setPerson(new Person("Daniel", "Williams"));
	}

	public static class Person {
		private String firstName, lastName;

		public Person(String firstName, String lastName) {
			super();
			this.firstName = firstName;
			this.lastName = lastName;
		}

		public String getFirstName() {
			return firstName;
		}

		public String getLastName() {
			return lastName;
		}
	}

	public static class BusinessDelegate {

		private static final Logger LOGGER = LoggerFactory
				.getLogger(BusinessDelegate.class);

		public BusinessDelegate() {

		}

		@Cached(cacheName = "test", key = "$1 + $2", el = true, op = CacheOperation.Get)
		public Person getPersonWithEl(String firstName, String lastName) {
			LOGGER.info("cache miss");
			return new Person(firstName, lastName);
		}

		@Cached(cacheName = "test", key = "$1 + $2", el = true, op = CacheOperation.Delete)
		public Person refresh(String firstName, String lastName) {
			return getPersonWithEl(firstName, lastName);
		}

		@Cached(cacheName = "test", key = "$1.firstName + $1.lastName", el = true, op = CacheOperation.Put, cacheStackLocation = 1)
		public void setPerson(Person p) {
			LOGGER.debug("now we're doing something else.");
		}
	}

	public static class MyCache implements Cache {

		Map<String, Person> data = new ConcurrentHashMap<String, Person>();

		public MyCache(Caches.CacheConfig config) {

		}

		public boolean set(Cacheable _obj) throws CacheException {
			// TODO Auto-generated method stub
			return true;
		}

		public <T> boolean set(String _key, T _obj) throws CacheException {
			data.put(_key, (Person) _obj);

			return true;
		}

		public Future<Boolean> setAsync(Cacheable _obj) throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public <T> Future<Boolean> setAsync(String _key, T _obj)
				throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public <T> T get(Cacheable _key) throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public <T> T get(Cacheable _key, Callable<T> ifNotFound)
				throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public <T> T get(String _key) throws CacheException {
			return (T) data.get(_key);
		}

		public <T> T get(String _key, Callable<T> ifNotFound)
				throws CacheException {
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

		public boolean supportsThreadLocal() {
			// TODO Auto-generated method stub
			return false;
		}
	}
}
