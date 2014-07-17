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