package com.solace.cf4j.aop;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.solace.cf4j.annotations.Cached;
import com.solace.cf4j.*;

/**
 * The CachingAdvice is weaved into code based upon the pointcut defined.
 * <p>
 * If using an expression language, {@link Cached#el()}=true, then the object
 * reference must be of the form $1...$n representing the parameter passed in on
 * the stack
 * 
 * @author <a href="mailto:daniel.williams@gmail.com">Daniel Williams</a>
 *
 */
@Aspect
public class CachingAdvice {

	private static final JexlEngine jexl = new JexlEngine();
	static {
		jexl.setCache(512);
		jexl.setLenient(false);
		jexl.setSilent(false);
	}

	public CachingAdvice() {
		// TODO Auto-generated constructor stub
	}

	@Around(value = "call(* *(..)) && @annotation(cached)", argNames = "pjp,cached")
	public Object invoke(final ProceedingJoinPoint pjp, final Cached cached)
			throws Throwable {

		if (cached.op() == CacheOperation.Get)
			return handleGet(cached, pjp);
		else if (cached.op() == CacheOperation.Put)
			return handleSet(cached, pjp);
		else
			return handleDelete(cached, pjp);

	}

	private Cache accessor(final Cached cached) {
		Cache accessor = null;

		if (cached.tl())
			accessor = CacheAccessor.newThreadLocalScopeCache(cached
					.cacheName());
		else
			accessor = CacheAccessor.newApplicationScopeCache(cached
					.cacheName());

		return accessor;
	}

	private Logger logger(ProceedingJoinPoint pjp) {
		return LoggerFactory.getLogger(pjp.getThis().getClass());
	}

	/**
	 * Attempt to set an object in the designated cache asynchronously
	 * @param logger
	 * @param c
	 * @param key
	 * @param v
	 */
	private void set(Logger logger, Cache c, String key, Object v) {
		boolean couldSet = true;
		if (!Strings.isNullOrEmpty(key))
			c.setAsync(key, v);
		else
			couldSet = false;
		
		if (!couldSet)			
			logger.error("could not set key: {}", key);
	}

	/**
	 * Handles {@link CacheOperation#Get} operations.
	 * Will first attempt to query resolved cache, if hit then will return; if not hit, will invoke and set asynchronously
	 * @param cached
	 * @param pjp
	 * @return
	 * @throws Throwable
	 */
	private Object handleGet(final Cached cached, final ProceedingJoinPoint pjp)
			throws Throwable {
		Logger logger = logger(pjp);

		Cache cache = accessor(cached);

		Object value = null;
		String key = null;
		try {
			key = getKey(cached, pjp);

			if (key != null) {
				logger.debug("Attempting key: {}", key);
				value = cache.get(key);
			}
		} catch (Exception e) {
		}

		if (null == value) {
			value = pjp.proceed();

			set(logger, cache, key, value);				
		}

		return value;
	}

	
	/**
	 * Will handle {@link CacheOperation#Put} put operations.
	 * If value to be put is passed in on stack, {@link Cached#cacheStackLocation()}, will attempt to set on resolved {@link Cache} asynchronously and then invoke method
	 * assuming additional business logic.
	 * 
	 * If {@link Cached#cacheStackLocation()} not set, or set to {@value Cached#NO_STACK_SPECIFIED}, will invoke method and 
	 * asynchronously set.
	 * @param cached
	 * @param pjp
	 * @return
	 * @throws Throwable
	 */
	private Object handleSet(final Cached cached, final ProceedingJoinPoint pjp)
			throws Throwable {
		Cache c = accessor(cached);
		Logger logger = logger(pjp);
		Object result = null;
		String key = getKey(cached, pjp);
		if (cached.cacheStackLocation() != Cached.NO_STACK_SPECIFIED) {
			logger.info("Setting pre-defined object passed on stack.");

			set(logger, c, key, pjp.getArgs()[cached.cacheStackLocation() - 1]);			
			
			result = pjp.proceed();
		} else {
			result = pjp.proceed();
			
			if (null == result)
				logger.error(
						"method did not return a result to replace for key {}",
						key);
			else
				set(logger, c, key, result);
		}

		return result;
	}

	/**
	 * Handle {@link CacheOperation#Delete} operations
	 * Will invoke an asynchronous delete on the resolved {@link Cache} first thing.
	 * Will then invoke {@link #handleSet(Cached, ProceedingJoinPoint)} which will invoke properly
	 * @param cached
	 * @param pjp
	 * @return
	 * @throws Throwable
	 */
	private Object handleDelete(final Cached cached,
			final ProceedingJoinPoint pjp) throws Throwable {
		Cache c = accessor(cached);
		String key = getKey(cached, pjp);
		c.deleteAsync(key);
		return handleSet(cached, pjp);
	}

	private String getKey(final Cached cached, final ProceedingJoinPoint pjp) {
		return !Strings.isNullOrEmpty(cached.key()) ? (cached.el() ? buildElKey(
				cached, pjp) : cached.key())
				: cached.key();
	}

	private String buildElKey(final Cached cached, final ProceedingJoinPoint pjp) {
		Expression e = jexl.createExpression(cached.key());

		// populate the context
		JexlContext context = new MapContext();
		for (int i = 1; i <= pjp.getArgs().length; i++)
			context.set("$" + i, pjp.getArgs()[i - 1]);

		return e.evaluate(context).toString();
	}

	private Object invokeMethod(final ProceedingJoinPoint pjp,
			final Cached cached) throws Throwable {
		Object value = pjp.proceed();

		if (cached.cacheStackLocation() != Cached.NO_STACK_SPECIFIED) {
			value = pjp.getArgs()[cached.cacheStackLocation() - 1];
		}

		return value;
	}
}
