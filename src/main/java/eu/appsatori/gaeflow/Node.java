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

public final class Node<A,R> {
	
	public static class AtBuilder<A,R> {
		
		private final String name;
		
		AtBuilder(String name) {
			if(name == null){
				throw new NullPointerException();
			}
			this.name = name;
		}
		
		public Node<A,R> run(Class<? extends Task<A,R>> task){
			if(task == null){
				throw new NullPointerException();
			}
			return new Node<A,R>(name, task, TaskType.SERIAL);
		}
		
		public Node<A,R> fork(Class<? extends Task<A,R>> task){
			if(task == null){
				throw new NullPointerException();
			}
			return new Node<A,R>(name, task, TaskType.PARALLEL);
		}
		
		public Node<A,R> challange(Class<? extends Task<A,R>> task){
			if(task == null){
				throw new NullPointerException();
			}
			return new Node<A,R>(name, task, TaskType.PARALLEL_COMPETETIVE);
		}
	}
	
	
	private final String name;
	private final Class<? extends Task<A,R>> task;
	private final TaskType taskType;
	
	private final String queue;
	
	Node(String name, Class<? extends Task<A, R>> task, TaskType type) {
		this(name, task, type, "");
	}
	
	Node(String name, Class<? extends Task<A, R>> task, TaskType type, String queue) {
		this.name = name;
		this.task = task;
		this.taskType = type;
		this.queue = queue;
	}
	
	public static <A, R> AtBuilder<A, R> at(String name){
		return new AtBuilder<A, R>(name);
	}

	public static <A, R> AtBuilder<A, R> on(Class<? extends Throwable> e){
		return new AtBuilder<A, R>(e.getName());
	}

	public String getName() {
		return name;
	}

	public Class<? extends Task<A, R>> getTask() {
		return task;
	}
	
	public TaskType getTaskType() {
		return taskType;
	}
	
	
	public Node<A,R> inQueue(String queue){
		return new Node<A,R>(name, task, taskType, queue);
	}
	
	
	
	public String getQueue() {
		return queue;
	}
	
	
	

}
