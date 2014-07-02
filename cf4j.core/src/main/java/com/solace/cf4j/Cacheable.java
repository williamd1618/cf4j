package com.solace.cf4j;

public interface Cacheable {

	/**
	 * the method used to get the key that an object should be cached as
	 * @return the key
	 */
	public String getCacheKey();
}
