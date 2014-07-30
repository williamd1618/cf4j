package com.solace.cf4j.redis;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;
import com.solace.cf4j.ArgumentException;
import com.solace.cf4j.CacheBase;
import com.solace.cf4j.CacheException;
import com.solace.cf4j.Cacheable;
import com.solace.cf4j.ConfigurationException;
import com.solace.cf4j.DistributedCache;
import com.solace.cf4j.config.Caches;
import com.solace.cf4j.serialization.SerializationStrategy;
import com.solace.cf4j.support.ReflectionUtil;

import static com.solace.cf4j.redis.Keys.*;

/**
 * @see {@link Keys#SERVER_COUNT}
 * @see {@link Keys#SERVER_HOST}
 * @see {@link Keys#SERVER_WEIGHT}
 * @see {@link Keys#CONNECTION_TIMEOUT}
 * @see {@link Keys#CACHE_TIMESPAN}
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

	private static final String S_WAS_NOT_SUCCESSFULLY_DELETED = "{} was NOT successfully deleted.";

	private static final String S_WAS_SUCCESSFULLY_DELETED = "{} was successfully deleted.";

	private static final String S_WAS_NOT_SUCCESSFULLY_SET = "{} was NOT successfully set.";

	private static final String S_WAS_SUCCESSFULLY_SET = "{} was successfully set.";

	private static final String STORING_S_S_WITH_EXPIRY_S = "Storing {} = {} with expiry {}.";

	private static final String STORING_S_S = "Storing {} = {}.";

	private static final String S_MUST_BE_AN_INTEGER_1 = "{} must be an integer > 1";
	
	public static final Integer DEFAULT_SERVER_WEIGHT = new Integer(5);

	static final Logger LOGGER = LoggerFactory.getLogger(RedisCache.class);

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
	
	private SerializationStrategy serializer;	

	private int m_timeout = 3;
	private boolean m_hasTimespan = false;
	private int m_timespan = 0;

	protected Jedis jedis = null;

	/**
	 * Constructor fired by CacheConfig.loadImplementation
	 * 
	 * @param _config
	 * @throws CacheException
	 */
	public RedisCache(Caches.CacheConfig _config) throws ArgumentException,
			CacheException {
		super(_config);
		
		if (m_config == null)
			return;

		String tmp = null;
		
		if ((tmp = getParameters().get(SERIALIZATION_STRATEGY)) != null)
			try {
				serializer = ReflectionUtil.createInstance(tmp);
			} catch (Exception e) {
				throw new ConfigurationException(e.getMessage(), e);
			}
		else
			throw new ConfigurationException("serializationStrategy must be provided.");
		
		if (getParameters().containsKey("test"))
			return;
		
		int serverCount = 0;
		
		if ((tmp = getParameters().get(SERVER_COUNT)) != null) {
			serverCount = Integer.parseInt(tmp);
			if ( serverCount != 1)
				throw new ConfigurationException("currently redis implementation only supports a single server.");
		} else
			throw new ConfigurationException(
					"Server information must be defined for RedisCache.");
		
		HostAndPort hostAndPort = loadServer(0);

		if ((tmp = getParameters().get(CONNECTION_TIMEOUT)) != null
				&& tmp.trim() != "") {
			this.m_timeout = Integer.parseInt(tmp);

			if (m_timeout < 1)
				throw new ArgumentException(String.format(
						S_MUST_BE_AN_INTEGER_1, CONNECTION_TIMEOUT));
		}

		jedis = new Jedis(hostAndPort.getHost(), hostAndPort.getPort());

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

	public void shutdown() throws CacheException {

	}

	/**
	 * Loads the server configuration
	 * 
	 * @param i
	 * @return
	 * @throws ArgumentException
	 *             thrown if an argument is impropertly formed or does not exist
	 */
	private HostAndPort loadServer(int i) throws ArgumentException {
		String host = null;
		int port;
		
		String tmp = null;

		if ((host = getParameters().get(String.format(SERVER_HOST, i))) == null
				|| tmp == "")
			throw new ArgumentException(String.format(SERVER_HOST
					+ "property is required for RedisCache", i));		

		if ((tmp = getParameters().get(String.format(SERVER_PORT, i))) == null
				|| tmp == "") {
			port = Protocol.DEFAULT_PORT;
		} else {
			port = Integer.parseInt(tmp);
		}

		return new HostAndPort(host, port);
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
	public <T> boolean set(String _key, T _item)
			throws CacheException {
		boolean set = false;
		try {
			String val = ser(_item);

			if (!m_hasTimespan) {
				LOGGER.debug(STORING_S_S, _key, val);
				
				String hashedKey = keyAsString(_key);

				set = jedis.set(hashedKey, val).equals("OK");
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
	public <T> T get(String _key) throws CacheException {
		LOGGER.debug(GETTING_S, _key);

		String result = null;

		try {
			result = jedis.get(keyAsString(_key));
			LOGGER.info(result);
		} catch (Exception e) {
			throw new CacheException("Could not get %s", e, _key);
		}

		LOGGER.debug(KEY_S_S, _key, (result != null) ? FOUND : NOT_FOUND);

		return deser(result);
	}

	/**
	 * @see RedisCache#delete(String)
	 */
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
	public boolean delete(String _key) throws CacheException {
		LOGGER.debug(DELETING_S, _key);

		boolean retVal = true;
		try {
			String hashKey = keyAsString(_key);
			retVal = jedis.del(hashKey) != null;
		} catch (Exception e) {
			throw new CacheException(COULD_NOT_DELETE, e, _key);
		}

		if (retVal)
			LOGGER.debug(S_WAS_SUCCESSFULLY_DELETED, _key);
		else
			LOGGER.debug(S_WAS_NOT_SUCCESSFULLY_DELETED, _key);

		return retVal;
	}

	public void clear() throws CacheException {
		new UnsupportedOperationException("clear not supported by Redis.");
	}

	public void incr(String key) throws CacheException {
		incr(key, 1);
	}

	public void decr(String key) throws CacheException {
		decr(key, 1);
	}

	public void incr(String key, long delta) throws CacheException {
		jedis.incrBy(key, delta);
	}

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
		return String.format("[%s]", (key == null ? "" : key.toString()));
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

	public Future<Boolean> setAsync(final Cacheable _obj) throws CacheException {
		Callable<Boolean> c = new Callable<Boolean>() {

			public Boolean call() throws Exception {
				return RedisCache.this.set(_obj);
			}
		};

		return asyncExecutor.submit(c);
	}

	public <T> Future<Boolean> setAsync(final String _key,
			final T _obj) throws CacheException {
		Callable<Boolean> c = new Callable<Boolean>() {

			public Boolean call() throws Exception {
				return RedisCache.this.set(_key, _obj);
			}
		};

		return asyncExecutor.submit(c);
	}

	public <T> T get(Cacheable _key, Callable<T> ifNotFound)
			throws CacheException {
		T t = get(_key);

		if (null == t)
			t = invokeNotFound(ifNotFound);

		setAsync(_key.getCacheKey(), t);

		return t;
	}

	public <T> T get(String _key, Callable<T> ifNotFound)
			throws CacheException {

		T t = get(_key);

		if (null == t)
			t = invokeNotFound(ifNotFound);

		setAsync(_key, t);

		return t;
	}

	public Future<Boolean> deleteAsync(final String _key) throws CacheException {

		Callable<Boolean> c = new Callable<Boolean>() {

			public Boolean call() throws Exception {
				return RedisCache.this.delete(_key);
			}
		};

		return asyncExecutor.submit(c);
	}

//	@Override
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
		return (T)serializer.deserialize(_in);
	}
	
	
	/**
	 * Because redis is out of memory threadlocal access has no value
	 * @return
	 */
	@Override
	public boolean supportsThreadLocal() {
		return false;
	}
}
