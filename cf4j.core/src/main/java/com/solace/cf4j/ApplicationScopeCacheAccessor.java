package com.solace.cf4j;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.cf4j.config.Caches;

/**
 * Will guarantee that only a single instance of a cache configuration can exist
 * in the application. This class should be used for shared, in-memory caching.
 * 
 * @author dwilliams
 * 
 */
public class ApplicationScopeCacheAccessor extends CacheAccessor {
	static Logger LOGGER = LoggerFactory.getLogger(ApplicationScopeCacheAccessor.class);

	private static Map<String, Cache> m_caches = new HashMap<String, Cache>();

	protected ApplicationScopeCacheAccessor() {
		super();
	}

	/**
	 * Will look into m_caches and see if an instance exists, if not will
	 * attempt to create one
	 * 
	 * @param _name
	 *            name of the {@link Caches.CacheConfig}
	 * @return an instance of an {@link ICache}
	 * @throws CacheException
	 *             thrown by default
	 * @throws ArgumentException
	 *             will be thrown if the ReflectionUtil cannot create an
	 *             instance
	 * @throws ConfigurationException
	 *             thrown if there is no CacheConfig
	 */
	public static Cache getInstance(String _name) throws CacheException,
			ConfigurationException {
		Cache instance = null;

		synchronized (m_caches) {
			if ((instance = m_caches.get(_name)) == null) {
				ApplicationScopeCacheAccessor cache = new ApplicationScopeCacheAccessor();

				cache.setCache(loadImplementation(_name));

				cache.setRegionName(_name);
				
				instance = (Cache) cache;

				m_caches.put(_name, instance);
			}
		}

		return instance;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.solace.caching.ICache#getParameter()
	 */
	@Override
	public Map<String, String> getParameters() {
		return this.m_cache.getParameters();
	}
}

