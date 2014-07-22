package com.solace.cf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.solace.cf4j.config.Caches.CacheConfig;
import com.solace.cf4j.support.ReflectionUtil;

public class L1L2Cache extends CacheBase {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(L1L2Cache.class);

	private List<Cache> m_caches = new ArrayList<Cache>();

	public L1L2Cache(CacheConfig _config) {
		super(_config);

		try {
			int cacheCount = 0;
			String tmp;

			if ((tmp = getParameters().get("cache.count")) != null)
				cacheCount = Integer.parseInt(tmp);
			else
				throw new ConfigurationException(
						"cache.count must be provided.");

			for (int i = 0; i < cacheCount; i++) {
				String key = "cache[" + (i + 1) + "].type";

				LOGGER.info("Looking up {0}", key);

				if ((tmp = getParameters().get(key)) == null)
					throw new ConfigurationException(key
							+ " does not exist as a property.");

				m_caches.add(ReflectionUtil
						.<Cache> createInstance(tmp, _config));
			}
		} catch (Exception e) {
			throw new ConfigurationException(e);
		}

		if (Iterables.isEmpty(m_caches))
			throw new ConfigurationException(
					"At least 1 cache must be configured.");
	}

	private boolean eval(List<Boolean> b) {
		Optional<Boolean> result = Optional.absent();

		for (Boolean v : b) {
			if (result.isPresent())
				result = Optional.fromNullable(result.get() && v);
			else
				result = Optional.fromNullable(v);
		}

		return (result.isPresent()) ? result.get() : false;
	}
	
	private Future<Boolean> factoryFromList(final List<Future<Boolean>> b) {
		Callable<Boolean> c = new Callable<Boolean>() {
			public Boolean call() throws Exception {
				List<Boolean> vals = new ArrayList<Boolean>();
				for(Future<Boolean> v : b) 
					vals.add(v.get());
				
				return eval(vals);
			}
		};
		
		return asyncExecutor.submit(c);
	}

	public boolean set(Cacheable _obj) throws CacheException {

		List<Boolean> b = new ArrayList<Boolean>();

		for (Cache c : m_caches)
			b.add(c.set(_obj));

		return eval(b);
	}

	public <T> boolean set(String _key, T _obj) throws CacheException {

		List<Boolean> b = new ArrayList<Boolean>();

		for (Cache c : m_caches)
			c.set(_key, _obj);

		return eval(b);
	}

	public Future<Boolean> setAsync(Cacheable _obj) throws CacheException {
		List<Future<Boolean>> b = new ArrayList<Future<Boolean>>();
		for(Cache c : m_caches) 
			b.add(c.setAsync(_obj));
		
		return factoryFromList(b);
	}

	public <T> Future<Boolean> setAsync(String _key, T _obj)
			throws CacheException {
		
		List<Future<Boolean>> b = new ArrayList<Future<Boolean>>();
		for(Cache c : m_caches) {
			b.add(c.setAsync(_key, _obj));
		}
		
		return factoryFromList(b);
	}

	public <T> T get(Cacheable _key) throws CacheException {
		T t = null;

		for (Cache c : m_caches) {
			t = c.get(_key);

			if (t != null)
				break;
		}

		return t;
	}

	public <T> T get(Cacheable _key, Callable<T> ifNotFound)
			throws CacheException {
		T t = null;

		for (Cache c : m_caches) {
			t = c.get(_key);

			if (t != null)
				break;
		}

		if (t == null) {
			t = invokeNotFound(ifNotFound);
			setAsync(_key.getCacheKey(), t);
		}

		return t;
	}

	public <T> T get(String _key) throws CacheException {
		T t = null;

		for (Cache c : m_caches) {
			t = c.get(_key);

			if (t != null)
				break;
		}

		return t;
	}

	public <T> T get(String _key, Callable<T> ifNotFound) throws CacheException {
		T t = null;

		for (Cache c : m_caches) {
			t = c.get(_key);

			if (t != null)
				break;
		}

		if (t == null) {
			t = invokeNotFound(ifNotFound);
			setAsync(_key, t);
		}

		return t;
	}

	public boolean delete(Cacheable _key) throws CacheException {

		List<Boolean> b = new ArrayList<Boolean>();

		for (Cache c : m_caches) {
			b.add(c.delete(_key));
		}

		return eval(b);
	}

	public boolean delete(String _key) throws CacheException {
		List<Boolean> b = new ArrayList<Boolean>();

		for (Cache c : m_caches) {
			b.add(c.delete(_key));
		}

		return eval(b);
	}

	public Future<Boolean> deleteAsync(Cacheable _key) throws CacheException {
		List<Future<Boolean>> b = Lists.newArrayList();
		for (Cache c : m_caches) {
			b.add(c.deleteAsync(_key));
		}

		return factoryFromList(b);
	}

	public Future<Boolean> deleteAsync(String _key) throws CacheException {
		List<Future<Boolean>> b = Lists.newArrayList();

		for (Cache c : m_caches) {
			b.add(c.deleteAsync(_key));
		}

		return factoryFromList(b);
	}

	public void clear() throws CacheException {
		for (Cache c : m_caches) {
			c.clear();
		}
	}

	public void shutdown() throws CacheException {
		for (Cache c : m_caches) {
			c.shutdown();
		}
	}
}
