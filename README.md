# AppSatori Pipes

AppSatori Pipes is framework for easier concurrent background processing on 
[Google App Engine Java](http://code.google.com/appengine/docs/java/overview.html)
environment.

>  Looks so easy you can't beleive how much work it does for you!


**Key concepts**

  * easy concurrency
  * type safety
  * simple and intuitive usage
  * small with no external dependencies
  * hiding *the most of* [Google App Engine](http://code.google.com/appengine/) speciallities

## Introduction
### Fork-Join Example
[Hint: Check a few test nodes to find out more examples](https://github.com/musketyr/appsatori-pipes/tree/master/src/test/java/eu/appsatori/pipes/sample)

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

If you want to start a new pipe aside of existing use one of `run`, `spring`, `fork` methods of `Pipes` class. They behave
exactly the same as returning result of *serial pipe* from the *node* execution.

You can specify the name of the queue where is the node located by using `Queue` annotation. The queue must exist.


### Three ways of execution
There are always three ways how can the *node* handle its `execute` method. The concreate way depends if they are executed
throught the *serial* or the *parallel pipe*.


**Serial Pipes**

1. direct chaining results to the next node using `run` method
2. forking the results so each item will be handled separely using the `fork` method
3. forking the results so each item will be handled separelly but only the first success task will proceed to next node using the `sprint` method
  

**Parallel Pipes**

1. waiting unless all tasks have finished and continuing in parallel processing using the `next` method
2. waiting unless all tasks have finished and passing collected result for serial processing using the `join` method
3. waiting unless all tasks have finished and passing collected result for challange processing using the `sprint` method


The framework is using generics heavily to check the *nodes* are chained properly. For example you must pass collections
to the `fork` and `sprint` methods and the following *node's* second parameter must be of the same type as the elements of
supplied collection.


## App Engine Details

### Costs
Serial pipes runs directly using
[deffered tasks](http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/taskqueue/DeferredTask.html)
so the results are not stored into the datastore but count towards 
[tasks quota](http://code.google.com/appengine/docs/quotas.html#Task_Queue). This also applies on both
serial and parallel pipes' parameters. 

Results of parallel pipes are stored into
the datastore until the last task is finished. Datastore operations are designed to be as minimal as possible but 
will they still cost you some money according to
[billing policy](http://code.google.com/appengine/docs/billing.html). Use this framework wisely. It suites well 
for use cases when running the tasks serially takes very long time like tens of seconds or even minutes.

### Drawbacks
As the results of the parallel pipes are stored in the datastore some problems with conversions may occur. 

All objects of types which are not supported by the datastore according to
[DataTypeUtils](http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/DataTypeUtils.html#isSupportedType(java.lang.Class)
are serialized as [blobs](http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Blob.html).
This means that they are realtivelly safe to use but their representation mustn't be greater than 1MB.

Supported object are saved to the datastore directly. This means that 
[some coversion usually happens](http://code.google.com/appengine/docs/java/datastore/entities.html#Properties_and_Value_Types).
Strings are particulary vunerable to 500 characters limit so the framework always convert [app engine Text type](http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Text.html) 
to String before serving so do not use 
[app engine Text type](http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Text.html)
directly result of parallel execution. 

App Engine stores numbers as Longs and Doubles only. Use them directly for better performance.





