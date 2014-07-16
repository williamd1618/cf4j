package com.solace.cf4j.redis;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.ShardedJedis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.cf4j.ArgumentException;
import com.solace.cf4j.Cache;
import com.solace.cf4j.CacheBase;
import com.solace.cf4j.CacheException;
import com.solace.cf4j.Cacheable;
import com.solace.cf4j.ConfigurationException;
import com.solace.cf4j.DistributedCache;
import com.solace.cf4j.config.Caches;
import com.solace.cf4j.config.Caches.CacheConfig;
import com.solace.cf4j.serialization.Deserializer;
import com.solace.cf4j.serialization.SerializationException;
import com.solace.cf4j.serialization.Serializer;
import com.solace.cf4j.support.ReflectionUtil;

import static com.solace.cf4j.redis.Keys.*;

/**
 * @see {@link RedisCache#SERVER_COUNT}
 * @see {@link RedisCache#SERVER_HOST}
 * @see {@link RedisCache#SERVER_PORT}
 * @see {@link RedisCache#SERVER_WEIGHT}
 * @see {@link RedisCache#DEFAULT_SERVER_WEIGHT}
 * @see {@link RedisCache#SOCKET_POOL_MINSIZE}
 * @see {@link RedisCache#SOCKET_POOL_MAXSIZE}
 * @see {@link RedisCache#CONNECTION_TIMEOUT}
 * @see {@link RedisCache#CACHE_TIMESPAN}
 * @see {@link RedisCache#SOCKET_POOL_INIT_CONNS}
 * @see {@link RedisCache#SOCKET_POOL_MAINT_THREAD_SLEEP}
 * @see {@link RedisCache#SOCKET_POOL_SOCKET_CONNECT_TO}
 * @see {@link RedisCache#SOCKET_POOL_SOCKET_TO}
 * @see {@link RedisCache#COMPRESS}
 * @see {@link RedisCache#COMPRESSION_THRESHOLD}
 * 
 * 
 * @author dwilliams
 * 
 */
public class RedisCache extends CacheBase implements DistributedCache {

	private static final String COULD_NOT_SET = "Could not set";

	private static final String NOT_FOUND = "not found";

	private static final String FOUND = "found";

	private static final String KEY_S_S = "Key: {}: {}";

	private static final String GETTING_S = "Getting {}.";

	private static final String DELETING_S = "Deleting {}.";

	private static final String COULD_NOT_DELETE = "Could not delete";

	private static final String S_WAS_NOT_SUCCESSFULLY_DELETED = "[%s] was NOT successfully deleted.";

	private static final String S_WAS_SUCCESSFULLY_DELETED = "[%d] was successfully deleted.";

	private static final String S_WAS_NOT_SUCCESSFULLY_SET = "[%d] was NOT successfully set.";

	private static final String S_WAS_SUCCESSFULLY_SET = "[%d] was successfully set.";

	private static final String STORING_S_S_WITH_EXPIRY_S = "Storing {} = {} with expiry {}.";

	private static final String STORING_S_S = "Storing {} = {}.";

	private static final String S_MUST_BE_AN_INTEGER_1 = "{} must be an integer > 1";

	static final Logger LOGGER = LoggerFactory.getLogger(RedisCache.class);

	static final ExecutorService executor = Executors.newCachedThreadPool();

	static final ObjectMapper MAPPER = new ObjectMapper();

	// avoid recurring construction
	private static ThreadLocal<MessageDigest> MD5 = new ThreadLocal<MessageDigest>() {
		@Override
		protected MessageDigest initialValue() {
			try {
				return MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				LOGGER.error("No MD5 algorithm found");
				throw new IllegalStateException("No MD5 algorithm found");
			}
		}
	};
	
	private Serializer serializer;
	private Deserializer deserializer;

	private int m_minPoolSize = 5;
	private int m_maxPoolSize = 10;
	private int m_timeout = 3;
	private boolean m_hasTimespan = false;
	private int m_timespan = 0;

	/**
	 * how many servers in config
	 */
	public static final String SERVER_COUNT = "serer.count";

	/**
	 * host of server
	 */
	public static final String SERVER_HOST = "server[%d].host";

	/**
	 * port of server
	 */
	public static final String SERVER_PORT = "server[%d].port";

	/**
	 * weight in relation to other servers, default is 5
	 */
	public static final String SERVER_WEIGHT = "server[%d].weight";

	/**
	 * default weight
	 */
	public static final Integer DEFAULT_SERVER_WEIGHT = new Integer(5);

	/**
	 * min size of socket pool
	 */
	public static final String SOCKET_POOL_MINSIZE = "socketPool.minSize";

	/**
	 * max size of socket pool
	 */
	public static final String SOCKET_POOL_MAXSIZE = "socketPool.maxSize";

	/**
	 * the number of initial connections to factory
	 */
	public static final String SOCKET_POOL_INIT_CONNS = "socketPool.initialConnections";

	/**
	 * in seconds (defaults to 3s)
	 */
	public static final String SOCKET_POOL_SOCKET_CONNECT_TO = "socketPool.socketConnectTimeout";

	/**
	 * In milliseconds the amount of time for the client to block on reads
	 * (defaults to 3s)
	 */
	public static final String SOCKET_POOL_SOCKET_TO = "socketPool.socketTimeout";

	/**
	 * connection timeout in minutes
	 */
	public static final String CONNECTION_TIMEOUT = "connectionTimeout";

	/**
	 * how long should the entity exist in seconds.
	 */
	public static final String CACHE_TIMESPAN = "cacheTimespan";

	/**
	 * In bytes at what point the cache client should start compression the data
	 * streams (defaults to 4kb).
	 */
	public static final String COMPRESSION_THRESHOLD = "compressionThreshold";

	/**
	 * true or false to enable compressions (default is true)
	 */
	public static final String COMPRESS = "compress";

	/**
	 * The maintenace thread in ms. By default it is 5 seconds
	 */
	public static final String SOCKET_POOL_MAINT_THREAD_SLEEP = "socketPool.maintenanceThreadSleep";

	int initialConnections = 10;
	long maxIdleTime = 1000 * 60 * 30; // 30 minutes
	long maxBusyTime = 1000 * 60 * 5; // 5 minutes
	long maintThreadSleep = 1000 * 5; // 5 seconds
	int socketTimeOut = 1000 * 3; // 3 seconds to block on reads
	int socketConnectTO = 1000 * 3; // 3 seconds to block on initial
	boolean nagleAlg = false; // turn off Nagle's algorithm on all
	boolean aliveCheck = false; // disable health check of socket on
	int compressionThreshold = 1024 * 4;
	boolean compress = true;

	ShardedJedis jedis = null;

	/**
	 * Constructor fired by CacheConfig.loadImplementation
	 * 
	 * @param _config
	 * @throws CacheException
	 */
	public RedisCache(Caches.CacheConfig _config) throws ArgumentException,
			CacheException {
		super(_config);

		List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();

		String tmp = null;
		
		if ((tmp = getParameters().get(SERIALIZER)) != null)
			try {
				serializer = ReflectionUtil.createInstance(tmp);
			} catch (Exception e) {
				throw new ConfigurationException(e);
			}
		else
			throw new ConfigurationException("serializer must be provided.");
		
		if ((tmp = getParameters().get(DESERIALIZER)) != null)
			try {
				deserializer = ReflectionUtil.createInstance(tmp);
			} catch (Exception e) {
				throw new ConfigurationException(e);
			}
		else
			throw new ConfigurationException("serializer must be provided.");

		int serverCount = 0;

		if ((tmp = getParameters().get(SERVER_COUNT)) != null)
			serverCount = Integer.parseInt(tmp);
		else
			throw new ArgumentException(
					"Server inforemation must be defined for DistributedCache");

		String[] servers = new String[serverCount];
		int[] weights = new int[serverCount];

		for (int i = 0; i < serverCount; i++) {
			servers[i] = loadServer(i);
			weights[i] = loadWeight(i);
		}

		if ((tmp = getParameters().get(CONNECTION_TIMEOUT)) != null
				&& tmp.trim() != "") {
			this.m_timeout = Integer.parseInt(tmp);

			if (m_timeout < 1)
				throw new ArgumentException(String.format(
						S_MUST_BE_AN_INTEGER_1, CONNECTION_TIMEOUT));
		}

		StringBuffer sb = new StringBuffer();
		for (String str : servers)
			sb.append(str).append(";");

		LOGGER.info(String
				.format("Initializing a Redis client with servers: {}\n, min socket pool: {}\n, max socket pool: {}\n, timeout: {} mins\n, cache timespan: {} mins.",
						sb.toString(), m_minPoolSize, m_maxPoolSize, m_timeout,
						this.m_timespan));

		for (String s : servers)
			shards.add(new JedisShardInfo(s));

		jedis = new ShardedJedis(shards);

		if ((tmp = getParameters().get(CACHE_TIMESPAN)) != null
				&& tmp.trim() != "") {
			this.m_timespan = Integer.parseInt(tmp);

			if (m_timespan <= 0) {
				throw new ArgumentException(String.format(
						"{} must be a positive integer", CACHE_TIMESPAN));
			} else {
				m_hasTimespan = true;
			}
		}
	}

	/**
	 * Force a finalize for cleanup
	 */
	@Override
	protected void finalize() throws Throwable {
		try {
			shutdown();
		} finally {
			super.finalize();
		}
	}

	@Override
	public void shutdown() throws CacheException {
		try {
			jedis.disconnect();
		} catch (Exception e) {
			throw new CacheException(e.getMessage(), e);
		}
	}

	/**
	 * Loads the server configuration
	 * 
	 * @param i
	 * @return
	 * @throws ArgumentException
	 *             thrown if an argument is impropertly formed or does not exist
	 */
	private String loadServer(int i) throws ArgumentException {
		String tmp = null;

		StringBuffer sb = new StringBuffer("redis://");

		int port;

		if ((tmp = getParameters().get(String.format(SERVER_HOST, i))) == null
				|| tmp == "")
			throw new ArgumentException(String.format(SERVER_HOST
					+ "property is required for RedisCache", i));

		sb.append(tmp);

		if ((tmp = getParameters().get(String.format(SERVER_PORT, i))) == null
				|| tmp == "") {
			port = Protocol.DEFAULT_PORT;
		} else {
			port = Integer.parseInt(tmp);
		}

		sb.append(":").append(port);

		return sb.toString();
	}

	private int loadWeight(int i) {
		String tmp = null;

		// building up our server weights if provided
		// defaults to 5
		if ((tmp = getParameters().get(String.format(SERVER_WEIGHT, i))) != null
				&& tmp != "")
			return Integer.parseInt(tmp);
		else
			return DEFAULT_SERVER_WEIGHT.intValue();
	}

	/**
	 * Will add a number of seconds to the current datetime to signal the
	 * memcached server when to expire content
	 * 
	 * @return
	 */
	private Date getExpiry() {
		Calendar c = Calendar.getInstance();

		c.add(Calendar.SECOND, m_timespan);

		return c.getTime();
	}

	public int getTimeout() {
		return m_timespan;
	}

	/**
	 * @see RedisCache#set(String, Object)
	 */
	@Override
	public boolean set(Cacheable _item) throws CacheException {
		return set(_item.getCacheKey(), _item);
	}

	/**
	 * @see RedisCache#set(String, Object)
	 */
	public boolean set(Object key, Object value) throws CacheException {
		return set(keyAsString(key), value);
	}

	/**
	 * Will attempt to set the _item into the memcached instanced that it hashes
	 * to Will throw a CacheExcedption of the set operation throws up an
	 * exception, typically a SocketTimeoutException
	 * 
	 * @see com.solace.RedisCache.Cache#set(java.lang.String, java.lang.Object)
	 */
	@Override
	public <T> boolean set(String _key, T _item)
			throws CacheException {
		boolean set = false;
		try {
			String val = ser(_item);

			if (!m_hasTimespan) {
				LOGGER.debug(STORING_S_S, _key, val);

				set = jedis.set(keyAsString(_key), val) == _key;
			} else {
				Date d = getExpiry();

				LOGGER.debug(String.format(STORING_S_S_WITH_EXPIRY_S, _key,
						_item.toString(), d));

				// assume this has to be a differene against now
				// otherwise why would it be an int.
				set = jedis.setex(keyAsString(_key),
						(int) (d.getTime() - Calendar.getInstance()
								.getTimeInMillis()), val) == _key;
			}
		} catch (Exception e) {
			throw new CacheException(COULD_NOT_SET, e, _key, _item);
		}

		if (set)
			LOGGER.debug(S_WAS_SUCCESSFULLY_SET, _key);
		else
			LOGGER.debug(S_WAS_NOT_SUCCESSFULLY_SET, _key);

		return set;
	}

	/**
	 * @see RedisCache#get(String)
	 */
	@Override
	public <T> T get(Cacheable _item)
			throws CacheException {
		return get(_item.getCacheKey());
	}

	/**
	 * gets an object defined by _key
	 * 
	 * @param _key
	 *            the defining token of the item to be returned
	 * @throws CacheException
	 *             if the caching client throws an exception we will wrap it and
	 *             throw it up
	 * 
	 */
	@Override
	public <T> T get(String _key) throws CacheException {
		LOGGER.debug(GETTING_S, _key);

		String result = null;

		try {
			result = jedis.get(_key);
		} catch (Exception e) {
			throw new CacheException("Could not get %s", e, _key);
		}

		LOGGER.debug(KEY_S_S, _key, (result != null) ? FOUND : NOT_FOUND);

		return deser(result);
	}

	/**
	 * @see RedisCache#delete(String)
	 */
	@Override
	public boolean delete(Cacheable _item) throws CacheException {
		return delete(_item.getCacheKey());
	}

	/**
	 * Will delete an instance from the cache
	 * 
	 * @param _key
	 *            identifies the entity
	 * @throws CacheException
	 *             if the client throws up an exception
	 */
	@Override
	public boolean delete(String _key) throws CacheException {
		LOGGER.debug(DELETING_S, _key);

		boolean retVal = true;
		try {
			retVal = jedis.del(_key) != null;
		} catch (Exception e) {
			throw new CacheException(COULD_NOT_DELETE, e, _key);
		}

		if (retVal)
			LOGGER.debug(S_WAS_SUCCESSFULLY_DELETED, _key);
		else
			LOGGER.debug(S_WAS_NOT_SUCCESSFULLY_DELETED, _key);

		return retVal;
	}

	@Override
	public void clear() throws CacheException {
		new UnsupportedOperationException("clear not supported by Redis.");
	}

	@Override
	public void incr(String key) throws CacheException {
		incr(key, 1);
	}

	@Override
	public void decr(String key) throws CacheException {
		decr(key, 1);
	}

	@Override
	public void incr(String key, long delta) throws CacheException {
		jedis.incrBy(key, delta);
	}

	@Override
	public void decr(String key, long delta) throws CacheException {
		jedis.decrBy(key, delta);
	}

	protected String keyAsString(Object key) throws CacheException {
		String fullKey = fullKeyAsString(key);

		if (fullKey.length() >= 250)
			return computeHash(fullKey);
		else
			return fullKey.replace(' ', '-');
	}

	protected String fullKeyAsString(Object key) {
		return String.format("{}", (key == null ? "" : key.toString()));
	}

	/**
	 * Want to convert this to Hex so that we don't end up with anyone odd
	 * characters that could cause issues in memcached
	 * 
	 * @param data
	 *            bytes to be converted
	 * @return
	 */
	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	/**
	 * Hash the key using the SHA-1 hash algorithm
	 * 
	 * @param _key
	 *            key value
	 * @return hashed key
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	private static String computeHash(String _key) throws CacheException {
		MessageDigest md5 = MD5.get();
		md5.reset();
		md5.update(_key.getBytes());
		byte[] data = md5.digest();

		return convertToHex(data);
	}

	@Override
	public Future<Boolean> setAsync(final Cacheable _obj) throws CacheException {
		Callable<Boolean> c = new Callable<Boolean>() {

			public Boolean call() throws Exception {
				return RedisCache.this.set(_obj);
			}
		};

		return asyncExecutor.submit(c);
	}

	@Override
	public <T> Future<Boolean> setAsync(final String _key,
			final T _obj) throws CacheException {
		Callable<Boolean> c = new Callable<Boolean>() {

			public Boolean call() throws Exception {
				return RedisCache.this.set(_key, _obj);
			}
		};

		return asyncExecutor.submit(c);
	}

	@Override
	public <T> T get(Cacheable _key, Callable<T> ifNotFound)
			throws CacheException {
		T t = get(_key);

		if (null == t)
			t = invokeNotFound(ifNotFound);

		setAsync(_key.getCacheKey(), t);

		return t;
	}

	@Override
	public <T> T get(String _key, Callable<T> ifNotFound)
			throws CacheException {

		T t = get(_key);

		if (null == t)
			t = invokeNotFound(ifNotFound);

		setAsync(_key, t);

		return t;
	}

	@Override
	public Future<Boolean> deleteAsync(final String _key) throws CacheException {

		Callable<Boolean> c = new Callable<Boolean>() {

			public Boolean call() throws Exception {
				return RedisCache.this.delete(_key);
			}
		};

		return asyncExecutor.submit(c);
	}

	@Override
	public Future<Boolean> deleteAsync(final Cacheable _key)
			throws CacheException {
		Callable<Boolean> c = new Callable<Boolean>() {
			public Boolean call() throws Exception {
				return RedisCache.this.delete(_key);
			}
		};

		return asyncExecutor.submit(c);
	}
	
	
	private <T> String ser(T _in) {
		return serializer.serialize(_in);
	}
	
	private <T> T deser(String _in) {
		return (T)deserializer.deserialize(_in);
	}
}
