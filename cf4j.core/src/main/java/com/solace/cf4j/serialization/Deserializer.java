package com.solace.cf4j.serialization;

public interface Deserializer<T> {
	T deserialize(String val) throws SerializationException;
}
