package com.solace.cf4j;

public class CacheException extends RuntimeException {

	public CacheException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CacheException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public CacheException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public CacheException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public CacheException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public CacheException(String format, Exception e, Object... args) {
		this(String.format(format, args), e);
	}	
}