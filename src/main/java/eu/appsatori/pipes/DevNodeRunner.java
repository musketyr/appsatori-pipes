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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.apphosting.api.ApiProxy;

/**
 * Internal implementation of {@link NodeRunner} for running nodes in
 * development mode. Can be used only for development purposes because Google
 * App Engine doesn't allow creating executor service.
 * 
 * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
 * 
 */
class DevNodeRunner implements NodeRunner {

	public interface ExecutionListener {
		public void taskExecuted(NodeTask<?, ?, ?> task);
	}

	private final class DevRunnerRunnable implements Runnable {
		
		private final ApiProxy.Environment env = ApiProxy
				.getCurrentEnvironment();
		private final NodeTask<?, ?, ?> delegate;

		public DevRunnerRunnable(NodeTask<?, ?, ?> delegate) {
			if (delegate == null) {
				throw new NullPointerException("Delegate cannot be null");
			}
			this.delegate = delegate;
		}

		public void run() {
			synchronized (clearedTasks) {
				if (clearedTasks.contains(delegate.getBaseTaskId())) {
					return;
				}
			}
			ApiProxy.setEnvironmentForCurrentThread(env);
			delegate.run();
			if(delegate.isExecuted()){
				for (ExecutionListener el : executionListeners) {
					try {
						el.taskExecuted(delegate);
					} catch (Exception e) {
						// do nothing
					}
				}
			}
		}

	}

	private final PipeDatastore pipeDatastore;
	private final ExecutorService executorService;
	private final Set<String> clearedTasks = new HashSet<String>();
	private final Set<DevNodeRunner.ExecutionListener> executionListeners = new HashSet<DevNodeRunner.ExecutionListener>();

	private DevNodeRunner(PipeDatastore datastore, ExecutorService service) {
		if (datastore == null) {
			throw new NullPointerException("Pipes datastore cannot be null");
		}
		if (service == null) {
			throw new NullPointerException("Executor service cannot be null");
		}
		this.pipeDatastore = datastore;
		this.executorService = service;
	}

	DevNodeRunner() {
		this(new DevPipeDatastore(), Executors.newFixedThreadPool(1));
	}

	public <N extends Node<?, ?>> String run(PipeType type,
			Class<? extends Node<?, ?>> node, Object arg) {
		String taskId = Pipes.getUniqueTaskId(node.getName());
		int total = type.getParallelTasksCount(arg);
		if(pipeDatastore.logTaskStarted(taskId, total)){
			for (int i = 0; i < total; i++) {
				startTask(type, node, arg, taskId, i);
			}
		}
		return taskId;
	}

	@SuppressWarnings("rawtypes")
	private void startTask(PipeType type, Class node, Object arg,
			String taskId, int index) {
		@SuppressWarnings("unchecked")
		NodeTask nodeTask = new NodeTask(type, node, taskId, index, arg);
		executorService.execute(new DevRunnerRunnable(nodeTask));
	}

	public PipeDatastore getPipeDatastore() {
		return pipeDatastore;
	}

	public void clearTasks(String queue, String baseTaskId, int tasksCount) {
		synchronized (clearedTasks) {
			clearedTasks.add(baseTaskId);
		}
	}
	
	public void addExecutionListener(ExecutionListener el){
		executionListeners.add(el);
	}
	
	public void removeExecutionListener(ExecutionListener el){
		executionListeners.remove(el);
	}

}
