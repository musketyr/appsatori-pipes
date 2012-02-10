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

import eu.appsatori.pipes.util.ObjectsToIterables;


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
		PipeDatastore fds = Pipes.getPipeDatastore();
		NodeDescriptor node = nds.find(nodeName);
		NodeBase taskInstance = createTaskInstance(node);
		
		if(taskInstance == null){
			throw new IllegalStateException("Cannot initiate instance " + node.getTask().getName() + " does it have parameterless constructor?");
		}
		
		try {
			if(node.getTaskType().isSerial()){
				taskInstance.execute(this.arg);
			} else {
				taskInstance.execute(ObjectsToIterables.getAt(index, this.arg));
			}
			String nextNode = taskInstance.getNextNode();
			if(nextNode == null || "".equals(nextNode)){
				if(!node.getTaskType().isSerial()){
					if(0 == fds.logTaskFinished(baseTaskId, index, taskInstance.getResult())){
						fds.clearTaskLog(baseTaskId, true);
					}
				}
				return;
			}
			if(node.getTaskType().isSerial()){
				new NodeExecutor(fds).execute(findNextNode(nds, taskInstance.getNextNode()), taskInstance.getResult());
				fds.clearTaskLog(baseTaskId, true);
			} else {
				int remaining = fds.logTaskFinished(baseTaskId, index, taskInstance.getResult());
				if(remaining > 0){
					return;
				}
				new NodeExecutor(fds).execute(findNextNode(nds, taskInstance.getNextNode()), fds.getTaskResults(baseTaskId));
				fds.clearTaskLog(baseTaskId);
			}
		} catch (Exception e) {
			NodeDescriptor handler = nds.find(e.getClass());
			if(handler == null){
				throw new RuntimeException("Exception executing node. No exception handler defined.", e);
			}
			new NodeExecutor(fds).execute(handler, e);
			
		}
	}

	private NodeDescriptor findNextNode(NodeDatastore nds, String nextNodeName) {
		NodeDescriptor n = nds.find(nextNodeName);
		if(n == null){
			throw new IllegalArgumentException("Next node '" + nextNodeName + "' doesn't exits!");
		}
		return n;
	}

	private NodeBase createTaskInstance(NodeDescriptor node) {
		try {
			return (NodeBase) node.getTask().newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	public String getBaseTaskId() {
		return baseTaskId;
	}

	public Object getArg() {
		return arg;
	}
	
}
