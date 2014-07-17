package com.solace.cf4j.redis.serialization;

import java.io.Serializable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.cf4j.serialization.*;


public abstract class JsonSerializer<T> implements SerializationStrategy<T> {
	
	Class<T> clazz;
	
	protected static final ObjectMapper MAPPER = new ObjectMapper();

	protected JsonSerializer(Class<T> c) {
		this.clazz = c;
		
		
	}
	
	
	@Override
	public String serialize(T obj) throws SerializationException {
		try {
			return MAPPER.writer().writeValueAsString(obj);
		} catch ( Exception e ) {
			throw new SerializationException(e);
		}
	}
	
	@Override
	public T deserialize(String val) throws SerializationException {
		
		T t = null;
		try {
			t = MAPPER.reader(clazz).readValue(val);
		} catch (Exception e) {
			throw new SerializationException(e);
		}
		
		return t;
	}
}
