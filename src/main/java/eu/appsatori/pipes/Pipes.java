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

import java.io.Serializable;
import java.util.regex.Pattern;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;


public class Pipes {

	private static final Pattern TASK_NAME_PATTERN = Pattern.compile("[^0-9a-zA-Z\\-\\_]");  

	private static PipeDatastore pipeDatastore;
	
	static {
		resetDatastores();
	}
	
	private Pipes() { }
	
	public static <P extends Pipe, R extends Serializable, N extends Node<P,R>> String run(Class<N> state){
		return run(state, null);
	}
	
	public static <P extends Pipe,R extends Serializable, N extends Node<P,R>> String run(Class<N> next, R result){
		return start(PipeType.SERIAL, next, result);
	}
	
	public static <P extends Pipe,R extends Serializable, N extends Node<P,R>> String fork(Class<N> next, R result){
		return start(PipeType.PARALLEL, next, result);
	}
	
	public static <P extends Pipe,R extends Serializable, N extends Node<P,R>> String sprint(Class<N> next, R result){
		return start(PipeType.COMPETETIVE, next, result);
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
			QueueFactory.getDefaultQueue().add(options);
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
		return TASK_NAME_PATTERN.matcher(from + "_" + System.currentTimeMillis()).replaceAll("_");
	}
	
	
	
	
}
