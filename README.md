__cf4j__

_cf4j_ is an a caching facade written in Java for the JVM.
Codebase is currently authored in Java 7, exposes its own runtime exceptions,
as well as a tightly defined interfaced for a _Cache_. 

__Configuration__
Currently done in json, there will be support yaml support in the near future.

```
{
	"cache":[
		{
			"type": {
				"properties": {
					"key":"value"
				},
				"value":"com.solace.cf4j.guava.GuavaCache"
			},
			"name":"test"
		}
	]
}
```

Caches are registered into the system by name and are subsequently accessed by 
name via a decorated interface, _CacheAccessor_, that will allow for application or thread
local scope access, thread local only if the cache supports it.  Currently implementations
are in-proc guava, out-of-proc caches (i.e. Redis) will not support construction via 
thread local.

Caches can be factoried programmatically in the following manner:

```
Cache c = CacheAccessor.newApplicationScopeCache("test");

c.set("foo", 1);
```

In addition, the https://github.com/williamd1618/cf4j/blob/master/cf4j.core/src/main/java/com/solace/cf4j/Cache.java 
interface supports the ability to provide cache loading when keys are not resolved as well 
as asynchronous operations for _set_ and _delete_

__CURRENT IMPLEMENTATIONS__

_Guava_

artifact: cf4j.guava

_properties_

| property  | description  | type |
| :------------ | :---------------| :-----|
| maximumSize | max number of cached elements | long |
| expireAfterWrite | expiration time |   long |
| expireAfterWriteInterval | expiration interval | http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/TimeUnit.html |
| removalListener | class to invoke when elements removed | string |


_Redis_

artifact: cf4j.redis

_properties_

| property  | description  | type |
| :------------ |:---------------| :-----|
| serializationStrategy | serialization strategy to use | string |
| serverCount | number of servers in cluster (defines key'ing) | string |
| server[n].host | host of nth server | string |
| server[n].port | port of nth server | int (defaults to 6379) |
| server[n].weight | weight of nth server | int |	
| connectionTimeout | timeout | long |	
| cacheTimespan | timespan to set by default in secs | int (defaults to forever) |	


__AOP__

artifact: cf4j.aop

Provides an AOP declarative strategy for cache operations against standard methods.  The pointcut definition,
as can be seen in the code, supports calls to all methods decorations with @Cached.

_examples_

The AOP strategy requires metadata to be runtime retained.  This is because depending on your chosen weaving strategy 
(e.g. runtime weaving) this may be needed.  In the case of compile-time weaving this isn't needed and creates loadable
Java classes.

All `key` values passed in can be evaluated as an expression language if the `el` attribute is set to true.

_GET_

Will invoke a GET operation against the configuration `test` cache using an expression language that concatenates the first and second stack parameters.
```
@Cached(cacheName = "test", key = "$1 + $2", el = true, op = CacheOperation.Get)
public Person getPersonWithEl(String firstName, String lastName) {
	LOGGER.info("cache miss");
	return new Person(firstName, lastName);
}
```

_PUT_

Will invoke a PUT/SET operation against the configured cache.  This operation will be performed asynchronously and will disregard the Future state.

In the following example the `Person` instance passed onto the stack will be immediately, and asynchronously, cached using the concatenated
values of the person's first and last names.  If the resultant of the method is desired to be cached then simply do not specific a `cacheStackLocation`.

```
@Cached(cacheName = "test", key = "$1.firstName + $1.lastName", el = true, op = CacheOperation.Put, cacheStackLocation = 1)
public void setPerson(Person p) {
	LOGGER.debug("now we're doing something else.");
}
```

_DELETE_

Will immediately, asynchronously issue a DELETE operation against the resolved cache and then proceed with the same fundamental flow as PUT.
