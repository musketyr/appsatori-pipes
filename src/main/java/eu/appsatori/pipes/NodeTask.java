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

import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.DeferredTaskContext;


class NodeTask<P extends Pipe, A,N extends Node<P,A>> implements DeferredTask {

	private static final long serialVersionUID = -3569377001403545004L;
	private final String baseTaskId;
	private final int index;
	private final Object arg;
	private final Class<N> node;
	private final PipeType type;
	
	public NodeTask(PipeType type, Class<N>node, String baseTaskId, int index, Object arg) {
		this.type = type;
		this.node = node;
		this.baseTaskId = baseTaskId;
		this.index = index;
		this.arg = arg;
	}

	public void run() {
		if(!Pipes.getPipeDatastore().isActive(baseTaskId)){
			return;
		}
		NodeResult result = NodeResult.END_RESULT;
		try {
			result = execute();
		} catch(Throwable th){
			DeferredTaskContext.setDoNotRetry(true);
			throw new RuntimeException("Exception during running task.", th);
		}
		
		if(result == null || !result.hasNext()){
			type.handlePipeEnd(Pipes.getQueueName(node), baseTaskId, index, result);
			return;
		}
		type.handleNext(Pipes.getQueueName(node), baseTaskId, index, result);
	}
	
	private N createTaskInstance() {
		try {
			return node.newInstance();
		} catch (InstantiationException e) {
			throw new IllegalStateException("Cannot initiate instance " + node.getName() + " does it have parameterless constructor?");
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Cannot initiate instance " + node.getName() + " does it have parameterless constructor?");
		}
	}

	public NodeResult execute(){
		return type.execute(createTaskInstance(), arg, index);
	}
	
}
