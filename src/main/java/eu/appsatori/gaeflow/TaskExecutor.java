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

package eu.appsatori.gaeflow;

import java.util.regex.Pattern;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

public class TaskExecutor {
	
	private static final Pattern TASK_NAME_PATTERN = Pattern.compile("[^0-9a-zA-Z\\-\\_]");
	private final FlowStateDatastore fds;

	public TaskExecutor(FlowStateDatastore flowStateDatastore) {
		this.fds = flowStateDatastore;
	}
	
	public <A, R> String execute(Node<A, R> node, Object arg){
		if(node == null){
			throw new IllegalArgumentException("Trying to run nonexisting node!");
		}
		String taskId = getUniqueTaskId(node.getName());
		Queue q = getQueue(node);
		int total = node.getTaskType().getParallelTasksCount(arg);
		fds.logTaskStarted(taskId, total);
		for (int i = 0; i < total; i++) {
			startTask(q, node, arg, taskId, i);
		}
		return taskId;
	}

	private <A, R> void startTask(Queue q, Node<A, R> node,
			Object arg, String taskId, int index) {
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		NodeExecutor nodeExecutor = new NodeExecutor(node.getName(), taskId, index, arg);
		TaskOptions options = TaskOptions.Builder.withTaskName(index + "_" + taskId).payload(nodeExecutor);
		q.add(options);
	}

	private <A, R> Queue getQueue(Node<A, R> transition) {
		Queue q = QueueFactory.getDefaultQueue();
		if(!"".equals(transition.getQueue())){
			q = QueueFactory.getQueue(transition.getQueue());
		}
		return q;
	}
	
	
	private <A, R> String getUniqueTaskId(String from){
		return TASK_NAME_PATTERN.matcher(from + "_" + System.currentTimeMillis()).replaceAll("_");
	}

}
