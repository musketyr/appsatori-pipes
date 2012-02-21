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

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;

/**
 * Internal implementation of type running particular nodes on Google App Engine.
 * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
 */
class AppEngineNodeRunner implements NodeRunner {
	
	private PipeDatastore pipeDatastore;
	
	AppEngineNodeRunner(PipeDatastore datastore){
		if(datastore == null){
			throw new NullPointerException("Pipes datastore cannot be null");
		}
		this.pipeDatastore = datastore;
	}
	
	AppEngineNodeRunner() {
		this(new DatastorePipeDatastore());
	}
	
	public PipeDatastore getPipeDatastore() {
		return pipeDatastore;
	}

	public <N extends Node<?, ?>> String run(PipeType type, Class<? extends Node<?, ?>> node, Object arg) {
		String taskId = Pipes.getUniqueTaskId(node.getName());
		Queue q = getQueue(node);
		int total = type.getParallelTasksCount(arg);
		pipeDatastore.logTaskStarted(taskId, total);
		for (int i = 0; i < total; i++) {
			startTask(q, type, node, arg, taskId, i);
		}
		return taskId;
	}


	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void startTask(Queue q, PipeType type, Class node, Object arg, String taskId, int index) {
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
		String name = Pipes.getQueueName(node);
		
		Queue q = QueueFactory.getDefaultQueue();
		if(!"".equals(name)){
			q = QueueFactory.getQueue(name);
		}
		return q;
	}

}
