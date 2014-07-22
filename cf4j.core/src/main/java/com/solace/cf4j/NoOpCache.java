package com.solace.cf4j;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class NoOpCache implements Cache {

	public NoOpCache() {
		// TODO Auto-generated constructor stub
	}

	public boolean set(Cacheable _obj) throws CacheException {
		// TODO Auto-generated method stub
		return false;
	}

	public <T> boolean set(String _key, T _obj) throws CacheException {
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T get(String _key, Callable<T> ifNotFound) throws CacheException {
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

	public Future<Boolean> deleteAsync(Cacheable _key) throws CacheException {
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
