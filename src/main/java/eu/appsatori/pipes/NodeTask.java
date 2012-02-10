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


public class NodeTask implements DeferredTask {

	private static final long serialVersionUID = -3569377001403545004L;
	private final String baseTaskId;
	private final int index;
	private final Object arg;
	private final String nodeName;
	
	public NodeTask(String nodeName, String baseTaskId, int index, Object arg) {
		this.nodeName = nodeName;
		this.baseTaskId = baseTaskId;
		this.index = index;
		this.arg = arg;
	}

	public void run() {
		NodeDatastore nds = Pipes.getNodeDatastore();
		NodeDescriptor<?> node = nds.find(nodeName);
		
		if(node == null){
			throw new IllegalStateException("Node " + nodeName + " has disappeared!");
		}
		
		try {
			NodeResult result = node.execute(arg, index);
			if(!result.hasNext()){
				node.getNodeType().handlePipeEnd(baseTaskId, index, result);
				return;
			}
			node.getNodeType().handleNext(baseTaskId, index, result);
		} catch (Exception e) {
			Pipes.handleException(e);
		}
	}

	public String getBaseTaskId() {
		return baseTaskId;
	}

	public Object getArg() {
		return arg;
	}
	
}
