package com.solace.cf4j.serialization;

public interface SerializationStrategy<T> {
	T deserialize(String val) throws SerializationException;
	
	String serialize(T _item) throws SerializationException;
}
