package com.solace.cf4j.redis;

public class Keys {
	public static final String SERIALIZATION_STRATEGY = "redis.serializationStrategy";
	public static final String SERVER_COUNT = "redis.serverCount";
	/**
	 * host of server
	 */
	public static final String SERVER_HOST = "redis.server[%d].host";

	/**
	 * Set if needs to be overridden, defaults to 6379
	 */
	public static final String SERVER_PORT = "redis.server[%d].port";

	/**
	 * connection timeout in minutes
	 */
	public static final String CONNECTION_TIMEOUT = "redis.connectionTimeout";
	
	/**
	 * how long should the entity exist in seconds.
	 */
	public static final String CACHE_TIMESPAN = "redis.cacheTimespan";
}
