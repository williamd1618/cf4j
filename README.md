_cf4j_ is a simple caching facade written in Java 7 to allow for it to be 
as consumable as possible.  The intention of the codebase is to allow a configurable and injectable caching implementation and be directly accessed via _Managers_.  I _Manager_ is simply a decorated _Cache_ implementation that provides access via a synchronized application scope or thread-local scope (useful for webservices and passing context other than on the stack).

In addition, cf4j provides a advise that can be applied via AspectJ to your JVM
project.  This includes all variations of JVM architectected languages including Scala and Clojure.  The advice application allow for an expression language written in JEXL to define what the key'ing structure is for the cache entry instead of having to be programmatically defined.

The _Cache_ implementations support asynchronous and synchronous access.  
Asynchronous access is handled via Java Futures.  Perhaps a future implementation will support RxJava whose Futures are more compatibile with Scala Futures.
