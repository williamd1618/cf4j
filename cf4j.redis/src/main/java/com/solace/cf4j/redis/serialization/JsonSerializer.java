package com.solace.cf4j.redis.serialization;

import java.io.Serializable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.cf4j.serialization.*;


public abstract class JsonSerializer<T> implements SerializationStrategy<T> {
	
	Class<T> clazz;
	
	protected static final ObjectMapper DEFAULT = new ObjectMapper();

	protected JsonSerializer(Class<T> c) {
		this.clazz = c;
	}
	
	protected ObjectMapper mapper() {
		return DEFAULT;
	}
	
	
//	@Override
	public String serialize(T obj) throws SerializationException {
		try {
			return mapper().writer().writeValueAsString(obj);
		} catch ( Exception e ) {
			throw new SerializationException(e);
		}
	}
	
//	@Override
	public T deserialize(String val) throws SerializationException {
		
		if (null == val)
			return null;
		
		T t = null;
		try {
			t = mapper().reader(clazz).readValue(val);
		} catch (Exception e) {
			throw new SerializationException(e);
		}
		
		return t;
	}
}
