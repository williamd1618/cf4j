package com.solace.cf4j.guava;

import static com.solace.cf4j.guava.Keys.*;
 
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.solace.cf4j.ArgumentException;
import com.solace.cf4j.Cache;
import com.solace.cf4j.CacheBase;
import com.solace.cf4j.CacheException;
import com.solace.cf4j.Cacheable;
import com.solace.cf4j.ConfigurationException;
import com.solace.cf4j.config.Caches.CacheConfig;
import com.solace.cf4j.support.ReflectionUtil;

public class GuavaCache extends CacheBase implements Cache {

	private Optional<Long> maxSize = Optional.absent();
	private Optional<Long> expiresAfterWrite = Optional.absent();
	private Optional<TimeUnit> expiresAfterWriteInterval = Optional.absent();
	private Optional<RemovalListener> removalListener = Optional.absent();

	private com.google.common.cache.Cache cache;

	public GuavaCache(CacheConfig _config) {
		super(_config);
		String tmp = null;

		int serverCount = 0;

		if ((tmp = getParameters().get(MAXIMUM_SIZE)) != null)
			maxSize = Optional.fromNullable(Long.parseLong(tmp));

		if ((tmp = getParameters().get(EXPIRES_AFTER_WRITE)) != null)
			expiresAfterWrite = Optional.fromNullable(Long.parseLong(tmp));

		if ((tmp = getParameters().get(EXPIRES_AFTER_WRITE_INTERVAL)) != null)
			expiresAfterWriteInterval = Optional.fromNullable(TimeUnit
					.valueOf(tmp));

		if (expiresAfterWrite.isPresent() != expiresAfterWriteInterval
				.isPresent())
			throw new ConfigurationException(
					"expiresAfterWrite and expiresAfterWriteInterval must both be set, or neither set.");

		if ((tmp = getParameters().get(REMOVAL_LISTENER)) != null) {
			RemovalListener l;
			try {
				l = ReflectionUtil.<RemovalListener> createInstance(tmp);
				if (l != null)
					removalListener = Optional.fromNullable(l);
			} catch (Exception e) {
				throw new ConfigurationException(e);
			}
		}

		CacheBuilder builder = CacheBuilder.newBuilder();

		if (maxSize.isPresent())
			builder.maximumSize(maxSize.get());

		if (expiresAfterWrite.isPresent())
			builder.expireAfterWrite(expiresAfterWrite.get(),
					expiresAfterWriteInterval.get());

		if (removalListener.isPresent())
			builder.removalListener(removalListener.get());

		cache = builder.build();
	}

	@Override
	public boolean set(Cacheable _obj) throws CacheException {
		cache.put(_obj.getCacheKey(), _obj);

		return true;
	}

	@Override
	public <T> boolean set(String _key, T _obj)
			throws CacheException {

		cache.put(_key, _obj);

		return true;
	}

	@Override
	public Future<Boolean> setAsync(final Cacheable _obj) throws CacheException {

		Callable<Boolean> c = new Callable<Boolean>() {
			public Boolean call() throws Exception {
				return GuavaCache.this.set(_obj);
			};
		};

		return asyncExecutor.submit(c);
	}

	@Override
	public <T> Future<Boolean> setAsync(final String _key,
			final T _obj) throws CacheException {
		Callable<Boolean> c = new Callable<Boolean>() {
			public Boolean call() throws Exception {
				return GuavaCache.this.set(_key, _obj);
			};
		};

		return asyncExecutor.submit(c);
	}

	@Override
	public <T> T get(Cacheable _key) throws CacheException {
		return (T) cache.getIfPresent(_key.getCacheKey());
	}

	@Override
	public <T> T get(Cacheable _key, Callable<T> ifNotFound)
			throws CacheException {
		
		T t = null;
		if (ifNotFound != null)
			t = (T) cache.getIfPresent(_key);
		else {
			try {
				t = (T) cache.get(_key.getCacheKey(), ifNotFound);
			} catch (Exception e) {
				throw new CacheException(e);
			}
		}
		
		return t;
	}

	@Override
	public <T> T get(String _key) throws CacheException {
		return (T) cache.getIfPresent(_key);
	}

	@Override
	public <T> T get(String _key, Callable<T> ifNotFound)
			throws CacheException {
		T t = null;
		if (ifNotFound != null)
			t = (T) cache.getIfPresent(_key);
		else {
			try {
				t = (T) cache.get(_key, ifNotFound);
			} catch (Exception e) {
				throw new CacheException(e);
			}
		}
		
		return t;
	}

	@Override
	public boolean delete(Cacheable _key) throws CacheException {
		cache.invalidate(_key.getCacheKey());
		
		return true;
	}

	@Override
	public boolean delete(String _key) throws CacheException {
		cache.invalidate(_key);
		
		return true;
	}

	@Override
	public Future<Boolean> deleteAsync(final Cacheable _key) throws CacheException {
		Callable<Boolean> c = new Callable<Boolean>() {
			public Boolean call() throws Exception {
				return delete(_key);
			}
		};
		
		return asyncExecutor.submit(c);
	}

	@Override
	public Future<Boolean> deleteAsync(final String _key) throws CacheException {
		Callable<Boolean> c = new Callable<Boolean>() {
			public Boolean call() throws Exception {
				return delete(_key);
			}
		};
		
		return asyncExecutor.submit(c);
	}

	@Override
	public void clear() throws CacheException {
		cache.invalidateAll();
	}

	@Override
	public void shutdown() throws CacheException {
		clear();
	}
}
