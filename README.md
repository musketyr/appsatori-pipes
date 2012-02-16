# AppSatori Pipes

[AppSatori](http://www.appsatori.eu) Pipes is framework for easier concurrent background processing on 
[Google App Engine Java](http://code.google.com/appengine/docs/java/overview.html)
environment.

> Hint: AppSatori Pipes are available in the Maven Central. You can simply
> use group: "eu.appsatori", name:"pipes", version: "0.6.0" as a dependency


**Key concepts**

  * easy concurrency
  * type safety
  * simple and intuitive usage
  * small with no external dependencies
  * hiding *the most of* [Google App Engine](http://code.google.com/appengine/) speciallities

## Introduction
### Fork-Join Example
> Hint: Check [ a few test nodes](https://github.com/musketyr/appsatori-pipes/tree/master/src/test/java/eu/appsatori/pipes/sample)
> to find out more examples

Let's see a little fork-join example first. To run some operation in parallel you need to create a `Node` 
and then run `fork` method of the facade class `Pipes`.

```java
Pipes.fork(ParallelNode.class, Arrays.asList("Hallo", "AppSatori", "Pipes"));
```

This tells the framework to proceed to the `ParallelNode` *node* and run it in parallel. Let's say
it is executed throught the *parallel pipe*. It up to the *node*
to decide what to do when all the tasks are finished. You usually want to *join* the results 
and proceed to next *node*.


```java
public class ParallelNode implements Node<ParallelPipe, String> {

 public NodeResult execute(ParallelPipe pipe, String param) {
   return pipe.join(JoinNode.class, param.length());
 }
	
}
```

The `JoinNode` will obtain the collection with the results of `ParallelNode` invocations

```java
@Queue("join_queue")
public class JoinNode implements Node<SerialPipe, Collection<Integer>> {

 public NodeResult execute(SerialPipe pipe, Collection<Integer> results) {
   // do some magic with results = [5,9,5]
   return pipe.finish();
 }
  
}
```

Calling `pipe.finish()` terminates the pipe.

> Note: Returning `null` has the same effect as `pipe.finish()` but good guys don't return null, do they?

If you want to start a new pipe aside of existing use one of `run`, `spring`, `fork` methods of `Pipes` class. They behave
exactly the same as returning result of *serial pipe* from the *node* execution.

You can specify the name of the queue where is the node located by using `Queue` annotation. The queue must exist.


## Four ways of execution
> Hint: The framework is using generics extensively to check the *nodes* are chained properly. Use them properly to avoid
> class cast exceptions.


There are always four ways how can the *node* handle its `execute` method. There is always `finish` method
to end the pipe execution too.


##Run or Next

**Run** (for serial nodes) or **next** (for parallel nodes) calls just runs the nodes serially one by one. 

![Serial](http://klient.appsatori.eu/pipes/haystack-serial-one.png)

It's just like one farmer is searching the single needle in the first than in the second and finally in the third haystack.


##Spread

**Spread** is just shortcut for running more independent serial processes.

![Serial](http://klient.appsatori.eu/pipes/haystack-spread.png)

It's just like many farmers are searching many needles in many haystacks side by side but they don't care about
each other. As soon as the farmer finds his needle he just go home and let his companions to continue searching.


##Fork and Join

**Fork** runs node in multiple parallel pipes. As soon as parallel processing is over you can 
**join** them to collects the results for the next node.

![Parallel](http://klient.appsatori.eu/pipes/haystack-parallel.png)

It's just like you many farmers are searching many needles in many haystacks side by side. As soon as thay finish they
come together and count the needles.


##Sprint

**Sprint** runs node in multiple parallel pipes but only the result of the fastest one is send to next node.

![Sprint](http://klient.appsatori.eu/pipes/haystack-sprint.png)

It's just like you many farmers are searching the single needle in many haystacks side by side. As soon as the first one
finds it he tells his companions to stop the work.

## App Engine Details

### Costs

> Hint: Don't forget you have 9 hours backend hours free a day. Set up new backend in [backends.xml](http://code.google.com/appengine/docs/java/config/backends.html)
> then a queue in [queue.xml](http://code.google.com/appengine/docs/java/config/queue.html) 
> with proper [target](http://code.google.com/appengine/docs/java/config/queue.html#target)
> set to the backend and use `@Queue` annotation to execute node in the queue.

Serial pipes runs directly using
[deffered tasks](http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/taskqueue/DeferredTask.html)
so the results are not stored into the datastore but count towards 
[tasks quota](http://code.google.com/appengine/docs/quotas.html#Task_Queue). This applies on both
serial and parallel pipes' parameters. There is one exception: if the arguments are bigger than allowed limit 
they are stored in the datastore.


Results of parallel pipes are stored into
the datastore until the last task is finished. Datastore operations are designed to be as minimal as possible but 
will they still cost you some money according to
[billing policy](http://code.google.com/appengine/docs/billing.html). Use this framework wisely. It suites well 
for use cases when running the tasks serially takes very long time like tens of seconds or even minutes.

### Drawbacks
> Hint: App Engine stores numbers as Longs and Doubles only. Use them directly for better performance. 

As the results of the parallel pipes are stored in the datastore some problems with conversions may occur. 

All objects of types which are not supported by the datastore according to
[DataTypeUtils](http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/DataTypeUtils.html#isSupportedType(java.lang.Class)
are serialized as [blobs](http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Blob.html).
This means that they are realtivelly safe to use but their representation mustn't be greater than 1MB.

Supported object are saved to the datastore directly. This means that 
[some coversion usually happens](http://code.google.com/appengine/docs/java/datastore/entities.html#Properties_and_Value_Types).
Strings are particulary vunerable to 500 characters limit so the framework always convert [app engine Text type](http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Text.html) 
to String before serving. Illegal argument exception is thrown if you try to use 
[app engine Text type](http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Text.html)
as a result of parallel execution.

Bytes, Shorts, Integers and Floats are handled properly for you by the framework. You don't need to be afraid that they are accidentaly
converted to Longs and Doubles by the datastore.





