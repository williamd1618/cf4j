package com.solace.cf4j.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

import com.solace.cf4j.CacheOperation;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Cached {
	
	/**
	 * key format
	 * @return
	 */
	String key() default "";
	
	/**
	 * key is an expression language and will reference a stack element.
	 * 
	 * @return
	 */
	boolean el() default false;
	
	boolean alwaysInvokeMethod() default false;
	
	/**
	 * should the cache be accessed in a thread local fashion.
	 * note: this may throw an exception if the underlying cache
	 * does not support thread local operations (e.g. distributed cache or a tuple cache with a distributed l1 or l2)
	 * 
	 * @return
	 */
	boolean tl() default false;
	
	/**
	 * name of the configured cache.
	 * @return
	 */
	String cacheName() default "";
	
	/**
	 * duration + timeUnit represent what the expiration policy is to be on the cached
	 * element and will consequently override that configured by cacheName()
	 * @return
	 */
	long duration() default Long.MAX_VALUE;
	
	/**
	 * represents the TimeUnit to be cached.  If durations is defaulted
	 * this value is ignored
	 * @return
	 */
	TimeUnit timeUnit() default TimeUnit.SECONDS;
	
	public static final int NO_STACK_SPECIFIED = 0;
	
	/**
	 * if the value is not returned by the method, which cache element should reference it (i.e. get operations).
	 * if the value is Integer.MAX_VALUE it is ignored
	 * @return
	 */
	int cacheStackLocation() default NO_STACK_SPECIFIED;
	
	CacheOperation op() default CacheOperation.Get;
}
