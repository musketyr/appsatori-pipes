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

import java.util.logging.Logger;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.DeferredTaskContext;


/**
 * Internal implementation of {@link DeferredTask} used to run nodes in queues.
 * 
 * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
 *
 * @param <P> either {@link SerialPipe} or {@link ParallelPipe} to control the flow
 * @param <A> type of argument passed to the node
 * @param <N> type of the passed node
 */
class NodeTask<P extends Pipe, A,N extends Node<P,A>> implements DeferredTask {
	
	private static final Logger log = Logger.getLogger(NodeTask.class.getName());

	private static final long serialVersionUID = -3569377001403545004L;
	private final String baseTaskId;
	private final int index;
	private final Object arg;
	private final Class<N> node;
	private final PipeType type;
	private transient boolean executed;
	
	public NodeTask(PipeType type, Class<N>node, String baseTaskId, int index, Object arg) {
		this.type = type;
		this.node = node;
		this.baseTaskId = baseTaskId;
		this.index = index;
		this.arg = arg;
	}

	public void run() {
		NodeResult result = NodeResult.END_RESULT;
		Throwable t = null;
		try {
			Object a = arg;
			if(arg instanceof StashedArgument){
				a = Pipes.getRunner().getPipeDatastore().retrieveArgument(((StashedArgument)arg).getKey());
			}
			if(Pipes.getRunner().getPipeDatastore().isActive(baseTaskId)){
				log.info("Executing: id=" + baseTaskId + ", index=" + index);
				result = type.execute(createTaskInstance(), a, index);
				executed = true;
			} else {
				log.info("Skipping due inactivity: id=" + baseTaskId + ", index=" + index);
				return;
			}
		} catch(Throwable th){
			DeferredTaskContext.setDoNotRetry(true);
			t = th;
		}
		
		if(result == null || !result.hasNext()){
			type.handlePipeEnd(Pipes.getRunner(), Pipes.getQueueName(node), baseTaskId, index, result);
			return;
		}
		log.info("Handle Next: id=" + baseTaskId + ", index=" + index + ", result= " + result);
		type.handleNext(Pipes.getRunner(), Pipes.getQueueName(node), baseTaskId, index, result);
		
		if(t != null){
			throw new RuntimeException("Exception during running task.", t);
		}
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

	public String getBaseTaskId() {
		return baseTaskId;
	}

	public boolean isExecuted() {
		return executed;
	}
	
	@Override
	public String toString() {
		return  (executed ? "Executed " : "") + (index + 1) + " of " + PipeType.sizeOf(arg) + " " 
				+ node.getSimpleName() + " (" + type + ") with parameter '" 
				+ PipeType.getAt(index, arg) + "' and base name '" + baseTaskId + "'";
	}
	
}
