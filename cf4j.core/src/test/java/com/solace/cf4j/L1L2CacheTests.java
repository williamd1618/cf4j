package com.solace.cf4j;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.*;

import static org.junit.Assert.*;

import com.google.common.collect.Lists;
import com.solace.cf4j.config.Caches.CacheConfig;
import com.solace.cf4j.config.Caches.CacheConfig.Type;

import static org.mockito.Mockito.*;

public class L1L2CacheTests {

	CacheConfig config;
	L1L2Cache cache;

	@Before
	public void setup() {
		config = new CacheConfig();
		Type type = new Type();
		config.setType(type);
		type.setValue(L1L2Cache.class);
		type.setProperties(new HashMap<String, String>() {
			{
				put("test", Boolean.TRUE.toString());
			}
		});

		cache = new L1L2Cache(config);
	}

	@Test
	public void test_set() {
		Cache l1 = mock(Cache.class);
		Cache l2 = mock(Cache.class);

		cache.m_caches = Lists.newArrayList(l1, l2);

		when(l1.set(eq("1"), any(Data.class))).thenReturn(true);
		when(l2.set(eq("1"), any(Data.class))).thenReturn(true);

		cache.set("1", new Data());

		verify(l1).set(eq("1"), any(Data.class));
		verify(l2).set(eq("1"), any(Data.class));
	}

	/**
	 * Will test that
	 * <ul>
	 * <li>in the presence of many caches the all will be hit until a result is
	 * found
	 * <li>when a result is found it is set asynchronously on the other caches.
	 * assumes that the caches are configured in execution priority
	 * <ul>
	 */
	@Test
	public void test_deferred_get() {
		Data res = new Data("foo");

		Cache l1 = mock(Cache.class);
		Cache l2 = mock(Cache.class);

		cache.m_caches = Lists.newArrayList(l1, l2);

		when(l1.get("1")).thenReturn(null);
		when(l2.get("1")).thenReturn(res);
		when(l1.setAsync(eq("1"), any(Data.class))).thenReturn(
				new Future<Boolean>() {
					public Boolean get() throws InterruptedException,
							ExecutionException {
						return Boolean.TRUE;
					}

					public boolean cancel(boolean mayInterruptIfRunning) {
						// TODO Auto-generated method stub
						return false;
					}

					public boolean isCancelled() {
						// TODO Auto-generated method stub
						return false;
					}

					public boolean isDone() {
						// TODO Auto-generated method stub
						return false;
					}

					public Boolean get(long timeout, TimeUnit unit)
							throws InterruptedException, ExecutionException,
							TimeoutException {
						// TODO Auto-generated method stub
						return null;
					}
				});

		Data data = cache.get("1");
		
		assertNotNull(data);
		assertTrue("foo".equals(data.value()));

		verify(l1).get(eq("1"));
		verify(l2).get(eq("1"));
		verify(l1).setAsync(eq("1"), any(Data.class));
	}
	
	@Test
	public void test_deletion() {
		Cache l1 = mock(Cache.class);
		Cache l2 = mock(Cache.class);

		cache.m_caches = Lists.newArrayList(l1, l2);

		when(l1.delete(eq("1"))).thenReturn(true);
		when(l2.delete(eq("1"))).thenReturn(false);

		boolean result = cache.delete("1");
					
		assertFalse(result);
	}
	
	@Test
	public void test_deletion_async_fail() throws CacheException, InterruptedException, ExecutionException {
		Cache l1 = mock(Cache.class);
		Cache l2 = mock(Cache.class);

		cache.m_caches = Lists.newArrayList(l1, l2);

		when(l1.deleteAsync(eq("1"))).thenReturn(new Future<Boolean>() {

			public boolean cancel(boolean mayInterruptIfRunning) {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean isCancelled() {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean isDone() {
				// TODO Auto-generated method stub
				return false;
			}

			public Boolean get() throws InterruptedException,
					ExecutionException {
				// TODO Auto-generated method stub
				return null;
			}

			public Boolean get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException,
					TimeoutException {
				return Boolean.TRUE;
			}
			
		});
		when(l2.deleteAsync(eq("1"))).thenReturn(new Future<Boolean>() {

			public boolean cancel(boolean mayInterruptIfRunning) {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean isCancelled() {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean isDone() {
				// TODO Auto-generated method stub
				return false;
			}

			public Boolean get() throws InterruptedException,
					ExecutionException {
				// TODO Auto-generated method stub
				return null;
			}

			public Boolean get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException,
					TimeoutException {
				return Boolean.FALSE;
			}
			
		});

		boolean result = cache.deleteAsync("1").get().booleanValue();
					
		assertFalse(result);
	}
	
	@Test
	public void test_deletion_async_success() throws CacheException, InterruptedException, ExecutionException {
		Cache l1 = mock(Cache.class);
		Cache l2 = mock(Cache.class);

		cache.m_caches = Lists.newArrayList(l1, l2);

		when(l1.deleteAsync(eq("1"))).thenReturn(new Future<Boolean>() {

			public boolean cancel(boolean mayInterruptIfRunning) {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean isCancelled() {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean isDone() {
				// TODO Auto-generated method stub
				return false;
			}

			public Boolean get() throws InterruptedException,
					ExecutionException {
				// TODO Auto-generated method stub
				return null;
			}

			public Boolean get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException,
					TimeoutException {
				return Boolean.TRUE;
			}
			
		});
		when(l2.deleteAsync(eq("1"))).thenReturn(new Future<Boolean>() {

			public boolean cancel(boolean mayInterruptIfRunning) {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean isCancelled() {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean isDone() {
				// TODO Auto-generated method stub
				return false;
			}

			public Boolean get() throws InterruptedException,
					ExecutionException {
				// TODO Auto-generated method stub
				return null;
			}

			public Boolean get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException,
					TimeoutException {
				return Boolean.TRUE;
			}
			
		});

		boolean result = cache.deleteAsync("1").get().booleanValue();
					
		assertFalse(result);
	}


	public static class Data {

		String val;

		public Data() {

		}

		public Data(String v) {
			this.val = v;
		}

		public String value() {
			return this.val;
		}
	}
}
