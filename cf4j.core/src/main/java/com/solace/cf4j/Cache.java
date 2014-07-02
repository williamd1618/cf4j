package com.solace.cf4j;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * The interface enforces a hardened type of T that extends Serializable b/c there is no way
 * to create an overloaded covariant type in Java if the base def is Object for the return type.
 * This will allow other line implementations to be leveraged.
 * @author williamd1618
 *
 */
public interface Cache {

	public boolean set(Cacheable _obj) throws CacheException;
	public <T extends Serializable> boolean set(String _key, T _obj) throws CacheException;
	
    public Future<Boolean> setAsync(Cacheable _obj) throws CacheException;
    public <T extends Serializable> Future<Boolean> setAsync(String _key, T _obj) throws CacheException;
		
	public <T extends Serializable> T get(Cacheable _key) throws CacheException;
	public <T extends Serializable> T get(Cacheable _key, Callable<T> ifNotFound) throws CacheException;
	public <T extends Serializable> T get(String _key) throws CacheException;
	public <T extends Serializable> T get(String _key, Callable<T> ifNotFound) throws CacheException;
	
	public boolean delete(Cacheable _key) throws CacheException;
	public boolean delete(String _key) throws CacheException;
	
    public Future<Boolean> deleteAsync(Cacheable _key) throws CacheException;
    public Future<Boolean> deleteAsync(String _key) throws CacheException;
	
	public String getRegionName() throws CacheException;
	public void setRegionName(String _name) throws CacheException;
	
	public Map<String, String> getParameters();
	
	public void clear() throws CacheException;
	
	public void shutdown() throws CacheException;
}
