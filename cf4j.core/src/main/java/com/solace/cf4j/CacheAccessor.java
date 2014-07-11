package com.solace.cf4j;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.cf4j.config.Caches;
import com.solace.cf4j.config.ConfigurationLoader;
import com.solace.cf4j.support.ReflectionUtil;

public abstract class CacheAccessor implements Cache {

	private static Logger LOGGER = LoggerFactory.getLogger(CacheAccessor.class);

	protected static Map<String, Caches.CacheConfig> m_configs = new ConcurrentHashMap<String, Caches.CacheConfig>();

	/**
	 * Static loading space for configuration options from the configuration
	 * file
	 */
	static {
		synchronized (m_configs) {
			try {
				Caches c = (new ConfigurationLoader()).load();

				if (c != null) {
					if (c.getCache() != null && c.getCache().size() > 0) {
						for (Caches.CacheConfig config : c.getCache()) {
							addConfiguration(config);
						}
					}
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	public static Cache newApplicationScopeCache(String name) {
		return ApplicationScopeCacheAccessor.getInstance(name);
	}

	public static Cache newThreadLocalScopeCache(String name) {
		return ThreadLocalCacheAccessor.getInstance(name);
	}

	/**
	 * Will add a configuration to the static map of configs.
	 * 
	 * @param _config
	 *            configuration to be added
	 */
	public static void addConfiguration(Caches.CacheConfig _config) {
		synchronized (m_configs) {
			if (m_configs.containsKey(_config.getName()))
				LOGGER.info(
						"A previous CacheConfig exists with Name: [{}] ... replacing",
						_config.getName());
			else
				LOGGER.info("Adding a CacheConfig with Name: [{}]",
						_config.getName());

			m_configs.put(_config.getName(), _config);
		}
	}

	public static void clearConfigurations() {
		synchronized (m_configs) {
			m_configs.clear();
		}
	}

	/**
	 * Will dynamically load the cache implementation into memory using the
	 * loosely coupled {@link Caches.CaceConfig} class.
	 * 
	 * @param _name
	 *            the name of the {@link Caches.CachConfig} to load
	 * @return
	 * @throws ConfigurationException
	 *             thrown if the config name is not found
	 * @throws ArgumentException
	 *             thrown if the ReflectionUtil cannot initialize the
	 *             configuration {@link Caches.CacheConfig.Type}
	 * @throws CacheException
	 */
	protected static Cache loadImplementation(String _name)
			throws ConfigurationException, CacheException {

		Cache cache = null;

		synchronized (m_configs) {

			Caches.CacheConfig config = null;

			if ((config = m_configs.get(_name)) == null) {
				throw new ConfigurationException(String.format(
						"CacheConfig with the name {} does not exist", _name));
			}

			try {
				cache = ReflectionUtil.<Cache> createInstance(config.getType()
						.getValue(), config);
			} catch (Exception e) {
				throw new ConfigurationException(String.format(
						"Trouble loading {}", config.getType().getValue()), e);
			}

			cache.setRegionName(config.getName());
		}

		return cache;
	}

	public CacheAccessor() {
	}

	protected Cache m_cache;

	public void setCache(Cache _cache) {
		m_cache = _cache;
	}

	public Cache getCache() {
		return m_cache;
	}

	public <T extends Serializable> T get(Cacheable _key) throws CacheException {
		return m_cache.get(_key);
	}

	public <T extends Serializable> T get(Cacheable _key, Callable<T> ifNotFound)
			throws CacheException {
		return m_cache.get(_key, ifNotFound);
	}

	public <T extends Serializable> T get(String _key) throws CacheException {
		return m_cache.get(_key);
	}

	public <T extends Serializable> T get(String _key, Callable<T> ifNotFound)
			throws CacheException {
		return m_cache.get(_key, ifNotFound);
	}

	public boolean set(Cacheable _obj) throws CacheException {
		return m_cache.set(_obj);
	}

	public <T extends Serializable> boolean set(String _key, T _obj)
			throws CacheException {
		return m_cache.set(_key, _obj);
	}

	public Future<Boolean> setAsync(Cacheable _obj) throws CacheException {
		return m_cache.setAsync(_obj);
	}

	public <T extends Serializable> Future<Boolean> setAsync(String _key, T _obj)
			throws CacheException {
		return m_cache.setAsync(_key, _obj);
	}

	public boolean delete(Cacheable _key) throws CacheException {
		return m_cache.delete(_key);
	}

	public boolean delete(String _key) throws CacheException {
		return m_cache.delete(_key);
	}

	public Future<Boolean> deleteAsync(Cacheable _key) throws CacheException {
		return m_cache.deleteAsync(_key);
	}

	public Future<Boolean> deleteAsync(String _key) throws CacheException {
		return m_cache.deleteAsync(_key);
	}

	public String getRegionName() throws CacheException {
		return m_cache.getRegionName();
	}

	public void setRegionName(String _name) throws CacheException {
		
	}

	public Map<String, String> getParameters() {
		return m_cache.getParameters();
	}

	public void clear() throws CacheException {
		m_cache.clear();
	}

	public void shutdown() throws CacheException {
		m_cache.shutdown();
	}
}
