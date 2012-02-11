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


class NodeTask<A,N extends Node<A>> implements DeferredTask {

	private static final long serialVersionUID = -3569377001403545004L;
	private final String baseTaskId;
	private final int index;
	private final Object arg;
	private final Class<N> node;
	private final NodeType type;
	
	public NodeTask(NodeType type, Class<N>node, String baseTaskId, int index, Object arg) {
		this.type = type;
		this.node = node;
		this.baseTaskId = baseTaskId;
		this.index = index;
		this.arg = arg;
	}

	public void run() {
		
		try {
			NodeResult result = execute(type, arg, index);
			if(!result.hasNext()){
				type.handlePipeEnd(baseTaskId, index, result);
				return;
			}
			type.handleNext(baseTaskId, index, result);
		} catch (Exception e) {
			Pipes.handleException(node, e);
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

	public NodeResult execute(NodeType type, Object arg, int index) throws Exception {
		return type.execute(createTaskInstance(), arg, index);
	}
	
}
