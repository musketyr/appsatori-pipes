/*
 * Copyright 2012 AppSatori s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.appsatori.pipes;

import java.util.Collection;
import java.util.Random;
import java.util.regex.Pattern;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;


/**
 * Facade class to the AppSatori Pipes framework.
 * 
 * Offers methods to start execution of the nodes.
 * 
 * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
 *
 */
public class Pipes {
	
	private static final Random RANDOM = new Random();

	private static final Pattern TASK_NAME_PATTERN = Pattern.compile("[^0-9a-zA-Z\\-\\_]");  

	private static PipeDatastore pipeDatastore;
	
	static {
		resetDatastores();
	}
	
	private Pipes() { }
	
	/**
	 * Runs the selected node.
	 * 
	 * @param node class of the node to be run
	 */
	public static <R, N extends Node<SerialPipe, ? super R>> void run(Class<N> node){
		run(node, null);
	}
	
	
	/**
	 * Runs the selected node with given parameter
	 * @param node class of the node to be run
	 * @param parameter	parameter for the node
	 */
	public static <R, N extends Node<SerialPipe, ? super R>> void run(Class<N> node, R parameter){
		start(PipeType.SERIAL, node, parameter);
	}
	
	/**
	 * For each element of <code>parameters</code> argument runs one node in parallel.
	 * The pipe will be collecting results of the executions so it could be send to one of the following
	 * nodes using the {@link ParallelPipe#join(Class, Object)} method. 
	 * @param node class of the node to be run in parallel
	 * @param parameters the collection of parameters for the nodes
	 */
	public static <E, R extends Collection<E>, N extends Node<ParallelPipe, ? super E>> void fork(Class<N> node, R parameters){
		start(PipeType.PARALLEL, node, parameters);
	}
	
	/**
	 * For each element of <code>parameters</code> argument runs one independent node in parallel.
	 * Only the fastest node will proceed to the following node.
	 * @param node class of the node to be run in parallel
	 * @param parameters the collection of parameters for the nodes
	 */
	public static <E, R extends Collection<E>, N extends Node<SerialPipe, ? super E>> void sprint(Class<N> node, R parameters){
		start(PipeType.COMPETETIVE, node, parameters);
	}
	
	/**
	 * For each element of <code>parameters</code> argument runs one independent node in parallel.
	 * All the nodes will proceed to following node.
	 * This method is is used to start multiple independent serial nodes in parallel.
	 * @param node class of the node to be run in parallel
	 * @param parameters the collection of parameters for the nodes
	 */
	public static<E, R extends Collection<E>, N extends Node<SerialPipe, ? super E>> void spread(Class<N> next, R parameters){
		for(E e: parameters){
			run(next, e);
		}
	}
	
	
	static String start(PipeType type, Class<? extends Node<?,?>> node){
		return start(type, node, null);
	}
	
	static <N extends Node<?,?>> String start(PipeType type, Class<? extends Node<?,?>> node, Object arg){
		String taskId = getUniqueTaskId(node.getName());
		Queue q = getQueue(node);
		int total = type.getParallelTasksCount(arg);
		pipeDatastore.logTaskStarted(taskId, total);
		for (int i = 0; i < total; i++) {
			startTask(q, type, node, arg, taskId, i);
		}
		return taskId;
	}


	static PipeDatastore getPipeDatastore() {
		return pipeDatastore;
	}


	static void setPipeDatastore(PipeDatastore pipeDatastore) {
		if(pipeDatastore == null){
			throw new NullPointerException("Pipes datastore cannot be null");
		}
		Pipes.pipeDatastore = pipeDatastore;
	}


	
	static void resetDatastores(){
		pipeDatastore = new DatastorePipeDatastore();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void startTask(Queue q, PipeType type, Class node, Object arg, String taskId, int index) {
		NodeTask nodeTask = new NodeTask(type, node, taskId, index, arg);
		TaskOptions options = TaskOptions.Builder.withTaskName(index + "_" + taskId).payload(nodeTask).retryOptions(RetryOptions.Builder.withTaskRetryLimit(0));
		try {
			q.add(options);
		} catch (IllegalStateException e){
			if(!QueueFactory.getDefaultQueue().equals(q)){
				startTask(QueueFactory.getDefaultQueue(), type, node, arg, taskId, index);
			} else {
				throw e;
			}
		} catch (IllegalArgumentException e){
			if(e.getMessage().startsWith("Task size too large") && !(arg instanceof StashedArgument)){
				startTask(q, type, node, new StashedArgument(pipeDatastore.stashArgument(arg)), taskId, index);
			} else {
				throw e;
			}
		}
	}

	static <N extends Node<?,?>> Queue getQueue(Class<? extends Node<?,?>> node) {
		String name = getQueueName(node);
		
		Queue q = QueueFactory.getDefaultQueue();
		if(!"".equals(name)){
			q = QueueFactory.getQueue(name);
		}
		return q;
	}

	static String getQueueName(Class<? extends Node<?,?>> node) {
		String name = "";
		
		if(node.isAnnotationPresent(eu.appsatori.pipes.Queue.class)){
			name = node.getAnnotation(eu.appsatori.pipes.Queue.class).value();
		}
		return name;
	}
	
	
	private static String getUniqueTaskId(String from){
		return TASK_NAME_PATTERN.matcher(from + "_" + RANDOM.nextInt(1000) + "_" + System.currentTimeMillis()).replaceAll("_");
	}
	
	
	
	
}
