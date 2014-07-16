package com.solace.cf4j.config;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.solace.cf4j.Cache;
import com.solace.cf4j.CacheException;
import com.solace.cf4j.Cacheable;

public class ConfigurationTests {
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	private static class MyCache implements Cache {

		public boolean set(Cacheable _obj) throws CacheException {
			// TODO Auto-generated method stub
			return false;
		}

		public <T> boolean set(String _key, T _obj)
				throws CacheException {
			// TODO Auto-generated method stub
			return false;
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

		public <T> T get(Cacheable _key) throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public <T> T get(String _key) throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public <T> Future<T> getAsync(Cacheable _key)
				throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public <T> Future<T> getAsync(String _key)
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

		public <T> T get(Cacheable _key,
				Callable<T> ifNotFound) throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public <T> T get(String _key,
				Callable<T> ifNotFound) throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	
	@Test
	public void verify_json() {
		
		Caches c = (new ConfigurationLoader()).load("testConfigLoad.json");
		
		assertTrue(c.getCache().size() == 1);
		assertTrue(Iterables.get(c.getCache(), 0, null).type.property.size() == 1);
		assertTrue(Iterables.get(c.getCache(), 0, null).type.value.equals(MyCache.class));		
	}
	
}
