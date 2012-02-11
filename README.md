# AppSatori Pipes

AppSatori Pipes is framework for easier concurrent background processing on 
[Google App Engine Java](http://code.google.com/appengine/docs/java/overview.html)
environment.

The key concepts of the engine are

  * easy concurrency
  * type safety
  * simple and intuitive usage
  * hiding *the most of* [Google App Engine](http://code.google.com/appengine/) speciallities

## Introduction
### Fork-Join Example
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

### App Engine Details

## Costs
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

## Drawbacks
As the results of the parallel pipes are stored in the datastore some problems with conversions may occur. 

All objects of types which are not supported by the datastore according to
[DataTypeUtils](http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/DataTypeUtils.html#isSupportedType(java.lang.Class)
are serialized as [blobs](http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Blob.html).
This means that they are realtivelly safe to use but their representation mustn't be greater than 1MB.

Supported object are saved to the datastore directly. This means that 
[some coversion usually happens](http://code.google.com/appengine/docs/java/datastore/entities.html#Properties_and_Value_Types).
Strings are particulary vunerable to 500 characters limit so the framework always convertapp engine Text 
to String before serving as a result.





