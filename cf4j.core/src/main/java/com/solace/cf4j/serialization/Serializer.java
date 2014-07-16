package com.solace.cf4j.serialization;

public interface Serializer<T> {
	String serialize(T _item) throws SerializationException;
}
