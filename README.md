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
			"type":
				{"property":
					[
						{"name":"name","value":"value"}
					],
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

__AOP__