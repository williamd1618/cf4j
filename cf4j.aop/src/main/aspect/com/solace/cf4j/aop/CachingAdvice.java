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

		Object result, value = null;

		Logger logger = LoggerFactory.getLogger(pjp.getClass());

		Object[] args = pjp.getArgs();

		Cache accessor = null;

		String key = "";
		
		boolean hit = false;

		if (cached.tl())
			accessor = CacheAccessor.newThreadLocalScopeCache(cached
					.cacheName());
		else
			accessor = CacheAccessor.newApplicationScopeCache(cached
					.cacheName());

		if (cached.alwaysInvokeMethod()) {
			logger.debug("forcing execution of method ...");

			result = pjp.proceed();

			if (cached.cacheStackLocation() != Cached.NO_STACK_SPECIFIED) {
				logger.debug("pulling object off of stack at position {0}",
						cached.cacheStackLocation());
				value = pjp.getArgs()[cached.cacheStackLocation() - 1];
			}
		} else {
			logger.debug("execution of method was not forced.");
			try {
				key = getKey(cached, pjp);

				if (key != null) {
					logger.debug("Attempting key: {0}", key);
					value = accessor.get(key);

					// if (value != null && attrib.CacheReadStrategy != null)
					// value = HandleCacheReadStrategy(attrib, key, value,
					// cache);
				}
			} catch (Exception e) {
			}
			
			
            // if not found execute
            if (null == value)
            {
                logger.debug("Cache miss for key: {0}", key);

                value = invokeMethod(pjp, cached);

                key = getKey(cached, pjp);
            } else {
                hit = true;
                logger.debug("Cache hit for key: {0}", key);
            }
            
            key = getKey(cached, pjp);

            // certain operations we will want to delete if the operation was successful
            if (!hit && cached.operation() != CacheOperation.Delete)
            {
                if (value != null)
                {
                	accessor.set(key, value);
                } else {
                    value = invokeMethod(pjp, cached);
                }
            }
            else if (cached.operation() == CacheOperation.Delete) {
                logger.debug("Deleting key: {0}", key);
                accessor.delete(key);
            }
		}

		return value;
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
		context.set("o", cached.cacheStackLocation()-1);
		
		return e.evaluate(context).toString();
	}
	
	private Object invokeMethod(final ProceedingJoinPoint pjp, final Cached cached) throws Throwable {
		Object value = pjp.proceed();
		
		if (cached.cacheStackLocation() != Cached.NO_STACK_SPECIFIED) {
			value = pjp.getArgs()[cached.cacheStackLocation()-1];
		}
		
        return value;
	}
}
