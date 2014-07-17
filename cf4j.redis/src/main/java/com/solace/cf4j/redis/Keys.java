package com.solace.cf4j.redis;

public class Keys {
	public static final String SERIALIZATION_STRATEGY = "serializationStrategy";
	public static final String SERVER_COUNT = "serverCount";
	/**
	 * host of server
	 */
	public static final String SERVER_HOST = "server[%d].host";

	/**
	 * weight in relation to other servers, default is 5
	 */
	public static final String SERVER_WEIGHT = "server[%d].weight";
	
	/**
	 * Set if needs to be overridden, defaults to 6379
	 */
	public static final String SERVER_PORT = "server[%d].port";

	/**
	 * connection timeout in minutes
	 */
	public static final String CONNECTION_TIMEOUT = "connectionTimeout";
	
	/**
	 * how long should the entity exist in seconds.
	 */
	public static final String CACHE_TIMESPAN = "cacheTimespan";
}
