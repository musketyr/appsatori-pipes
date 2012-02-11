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

import java.util.regex.Pattern;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;


class Pipes {

	private static final Pattern TASK_NAME_PATTERN = Pattern.compile("[^0-9a-zA-Z\\-\\_]");  

	private static PipeDatastore pipeDatastore;
	
	static {
		resetDatastores();
	}
	
	private Pipes() { }
	
	public static <R, N extends Node<R>> String run(Class<N> state){
		return run(state, null);
	}
	
	public static <R, N extends Node<R>> String run(Class<N> next, R result){
		return start(NodeType.SERIAL, next, result);
	}
	
	public static <R, N extends Node<R>> String fork(Class<N> state){
		return fork(state, null);
	}
	
	public static <R, N extends Node<R>> String fork(Class<N> next, R result){
		return start(NodeType.PARALLEL, next, result);
	}
	
	
	static String start(NodeType type, Class<? extends Node<?>> node){
		return start(type, node, null);
	}
	
	static <N extends Node<?>> String start(NodeType type, Class<? extends Node<?>> node, Object arg){
		String taskId = getUniqueTaskId(node.getName());
		Queue q = getQueue(node);
		int total = type.getParallelTasksCount(arg);
		pipeDatastore.logTaskStarted(taskId, total);
		for (int i = 0; i < total; i++) {
			startTask(q, type, node, arg, taskId, i);
		}
		return taskId;
	}
	
	static <N extends Node<?>> String handleException(Class<? extends Node<?>> node, Throwable e){
		if(!node.isAnnotationPresent(ExceptionHandler.class)){
			throw new RuntimeException("Exception executing node. No exception handler defined.", e);
		}
		
		for(Class<Node<? extends Throwable>> handler : node.getAnnotation(ExceptionHandler.class).value()){
			Class<?> cls = (Class<?>) handler.getTypeParameters()[0].getBounds()[0];
			if(cls.isAssignableFrom(e.getClass())){
				return start(NodeType.SERIAL, handler, e);
			}
		}
		throw new RuntimeException("Exception executing node. No exception handler defined.", e);
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
	private static void startTask(Queue q, NodeType type, Class node, Object arg, String taskId, int index) {
		NodeTask nodeTask = new NodeTask(type, node, taskId, index, arg);
		TaskOptions options = TaskOptions.Builder.withTaskName(index + "_" + taskId).payload(nodeTask).retryOptions(RetryOptions.Builder.withTaskRetryLimit(0));
		q.add(options);
	}

	private static <N extends Node<?>> Queue getQueue(Class<? extends Node<?>> node) {
		String name = "";
		
		if(node.isAnnotationPresent(eu.appsatori.pipes.Queue.class)){
			name = node.getAnnotation(eu.appsatori.pipes.Queue.class).value();
		}
		
		Queue q = QueueFactory.getDefaultQueue();
		if(!"".equals(name)){
			q = QueueFactory.getQueue(name);
		}
		return q;
	}
	
	
	private static String getUniqueTaskId(String from){
		return TASK_NAME_PATTERN.matcher(from + "_" + System.currentTimeMillis()).replaceAll("_");
	}
	
	
	
	
}
