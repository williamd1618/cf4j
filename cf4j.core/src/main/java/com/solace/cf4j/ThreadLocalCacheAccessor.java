package com.solace.cf4j;

import java.util.HashMap;

import com.solace.cf4j.config.Caches.CacheConfig;


/**
 * Meant to serve as a single, thread specific access point for getting a
 * {@link CacheAccessor} associated with a {@link CacheConfig}. {@code {@code
 * ICache cache = ThreadLocalAccessor.getInstance("Some.Cache.Name");
 * 
 * cache.put("foo","bar"); * }
 * 
 * @author dwilliams
 * 
 */
public class ThreadLocalCacheAccessor extends CacheAccessor {

	static String m_key = "ThreadLocalAccessor";

	static ThreadLocalCacheAccessor m_instance = null;

	/*
	 * A static, anonymous inner class for maintaining the cache implementation
	 * per thread
	 */
	static ThreadLocal<HashMap<String, ThreadLocalCacheAccessor>> m_tlCaches = new ThreadLocal<HashMap<String, ThreadLocalCacheAccessor>>() {
		protected HashMap<String, ThreadLocalCacheAccessor> initialValue() {
			return new HashMap<String, ThreadLocalCacheAccessor>();
		}
	};

	/**
	 * static accessor to enforce a singleton and make sure that all
	 * ThreadLocalAccessors are stored for the thread.
	 * 
	 * @param _name
	 *            the nam eof the cache to load
	 * @return a ThreadLocalAccessor
	 * @throws Exception
	 *             if there is an issue in constructing the ThreadLocalAccessor
	 * @see com.solace.caching.Caches
	 * @see com.solace.caching.Caches.CacheConfig
	 */
	public synchronized static Cache getInstance(String _name)
			throws ConfigurationException, CacheException {

		String key = m_key + _name;

		HashMap<String, ThreadLocalCacheAccessor> caches = m_tlCaches.get();

		if (null == (m_instance = caches.get(key))) {
			m_instance = new ThreadLocalCacheAccessor();

			m_instance.setCache(loadImplementation(_name));

			caches.put(key, m_instance);
		}

		return m_instance;
	}

	protected ThreadLocalCacheAccessor() {
		super();
	}

	@Override
	public String getRegionName() throws CacheException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void  setRegionName(String name) throws CacheException {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}
}

