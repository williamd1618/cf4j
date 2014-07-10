package com.solace.cf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.solace.cf4j.config.Caches;

/**
 * Rather self explanatory... documentation will come later.
 * 
 * @author williamd1618
 *
 */
public abstract class CacheBase implements Cache {

	protected Caches.CacheConfig m_config = null;

	protected Map<String, String> m_params;

	private String regionName;

	protected ExecutorService asyncExecutor = Executors.newCachedThreadPool();

	public CacheBase(Caches.CacheConfig _config) {
		m_config = _config;

		m_params = loadParams(_config);
	}

	public Map<String, String> getParameters() {
		return m_params;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	protected Map<String, String> loadParams(Caches.CacheConfig config) {

		Map<String, String> params = new HashMap<String, String>();

		// only iterate if the properties collection has elements
		if (m_config != null && m_config.getType().getProperty() != null
				&& m_config.getType().getProperty().size() > 0) {
			for (Caches.CacheConfig.Type.Property prop : m_config.getType()
					.getProperty()) {
				m_params.put(prop.getName(), prop.getValue());
			}
		}

		return params;
	}

	protected <T extends Serializable> T invokeNotFound(
			final Callable<T> notFound) throws CacheException {

		T result = null;

		try {
			result = notFound.call();
		} catch (Exception e) {
			throw new CacheException(e);
		}

		return result;
	}
}