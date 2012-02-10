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
import com.google.appengine.api.taskqueue.TaskOptions;

import eu.appsatori.pipes.ds.DatastorePipeDatastore;
import eu.appsatori.pipes.mem.InMemoryNodeDatastore;

public class Pipes {

	private static final Pattern TASK_NAME_PATTERN = Pattern.compile("[^0-9a-zA-Z\\-\\_]");  

	private static PipeDatastore pipeDatastore;
	private static NodeDatastore nodeDatastore;
	
	static {
		resetDatastores();
	}
	
	private Pipes() { }
	
	
	static String start(String node){
		return start(node, null);
	}
	
	static <N extends Node<?>> String start(String nodeName, Object arg){
		NodeDescriptor<N> node = nodeDatastore.find(nodeName);
		if(node == null){
			throw new IllegalArgumentException("Trying to run nonexisting node "  + nodeName + "!");
		}
		String taskId = getUniqueTaskId(node.getName());
		Queue q = getQueue(node);
		int total = node.getNodeType().getParallelTasksCount(arg);
		pipeDatastore.logTaskStarted(taskId, total);
		for (int i = 0; i < total; i++) {
			startTask(q, node, arg, taskId, i);
		}
		return taskId;
	}
	
	static <N extends Node<?>> String handleException(Throwable e){
		NodeDescriptor<N> handler = nodeDatastore.find(e.getClass());
		if(handler == null){
			throw new RuntimeException("Exception executing node. No exception handler defined.", e);
		}
		return start(handler.getName(), e);
	}


	public static PipeDatastore getPipeDatastore() {
		return pipeDatastore;
	}


	public static void setPipeDatastore(PipeDatastore pipeDatastore) {
		if(pipeDatastore == null){
			throw new NullPointerException("Pipes datastore cannot be null");
		}
		Pipes.pipeDatastore = pipeDatastore;
	}


	public static NodeDatastore getNodeDatastore() {
		return nodeDatastore;
	}


	public static void setNodeDatastore(NodeDatastore nodeDatastore) {
		if(nodeDatastore == null){
			throw new NullPointerException("Node datastore cannot be null");
		}
		Pipes.nodeDatastore = nodeDatastore;
	}
	
	public static void resetDatastores(){
		pipeDatastore = new DatastorePipeDatastore();
		nodeDatastore = new InMemoryNodeDatastore();
	}
	
	private static <N extends Node<?>> void startTask(Queue q, NodeDescriptor<N> node,
			Object arg, String taskId, int index) {
		NodeTask nodeTask = new NodeTask(node.getName(), taskId, index, arg);
		TaskOptions options = TaskOptions.Builder.withTaskName(index + "_" + taskId).payload(nodeTask);
		q.add(options);
	}

	private static <N extends Node<?>> Queue getQueue(NodeDescriptor<N> node) {
		Queue q = QueueFactory.getDefaultQueue();
		if(!"".equals(node.getQueue())){
			q = QueueFactory.getQueue(node.getQueue());
		}
		return q;
	}
	
	
	private static String getUniqueTaskId(String from){
		return TASK_NAME_PATTERN.matcher(from + "_" + System.currentTimeMillis()).replaceAll("_");
	}
	
	
	
	
}
