package com.solace.cf4j;

/**
 * Introduces additional operations value to a distributed cache implementation
 * @author <a href="mailto:daniel.williams@gmail.com">Daniel Williams</a>
 *
 */
public interface DistributedCache extends Cache {
	
	public void incr(String key) throws CacheException;

	public void decr(String key) throws CacheException;

	public void incr(String key, long delta) throws CacheException;
	
	public void decr(String key, long delta) throws CacheException;
}
