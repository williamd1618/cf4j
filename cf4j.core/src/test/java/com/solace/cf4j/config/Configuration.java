package com.solace.cf4j.config;

import static com.solace.cf4j.config.Caches.*;
import static com.solace.cf4j.config.Caches.CacheConfig.*;
import static com.solace.cf4j.config.Caches.CacheConfig.Type.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Future;

import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.solace.cf4j.Cache;
import com.solace.cf4j.CacheException;
import com.solace.cf4j.Cacheable;

import static com.google.common.base.Preconditions.*;

public class Configuration {
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	private static class MyCache implements Cache {

		public boolean set(Cacheable _obj) throws CacheException {
			// TODO Auto-generated method stub
			return false;
		}

		public <T extends Serializable> boolean set(String _key, T _obj)
				throws CacheException {
			// TODO Auto-generated method stub
			return false;
		}

		public Future<Boolean> setAsync(Cacheable _obj) throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public <T extends Serializable> Future<Boolean> setAsync(String _key,
				T _obj) throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public Object get(Cacheable _key) throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public Object get(String _key) throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public <T extends Serializable> Future<T> getAsync(Cacheable _key)
				throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		public <T extends Serializable> Future<T> getAsync(String _key)
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
		
	}
	
	public static void main(String... args) {
		
		
		
		Caches c = new Caches();
		
		CacheConfig config = new CacheConfig();
		config.setName("test");
		
		Type t = new Type();
		t.setValue(MyCache.class);
		t.setProperties(Lists.<Property>newArrayList(new Property() {
			{
				setName("name");
				setValue("value");
			}
		}));
		
		checkNotNull(t.getProperty());
		
		config.setType(t);
		
		c.getCache().add(config);
		
		try {
			String json = MAPPER.writer().writeValueAsString(c);
			
			Caches newCaches = MAPPER.reader(Caches.class).readValue(json);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
