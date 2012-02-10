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

public final class NodeDescriptor {
	
	public static class AtBuilder {
		
		private final String name;
		
		AtBuilder(String name) {
			if(name == null){
				throw new NullPointerException();
			}
			this.name = name;
		}
		
		public NodeDescriptor run(Class<? extends NodeBase> nodeBase){
			if(nodeBase == null){
				throw new NullPointerException();
			}
			return new NodeDescriptor(name, nodeBase, NodeType.SERIAL);
		}
		
		public NodeDescriptor fork(Class<? extends NodeBase> nodeBase){
			if(nodeBase == null){
				throw new NullPointerException();
			}
			return new NodeDescriptor(name, nodeBase, NodeType.PARALLEL);
		}
	}
	
	
	private final String name;
	private final Class<? extends NodeBase> nodeBase;
	private final NodeType nodeType;
	
	private final String queue;
	
	NodeDescriptor(String name, Class<? extends NodeBase> nodeBase, NodeType type) {
		this(name, nodeBase, type, "");
	}
	
	NodeDescriptor(String name, Class<? extends NodeBase> nodeBase, NodeType type, String queue) {
		this.name = name;
		this.nodeBase = nodeBase;
		this.nodeType = type;
		this.queue = queue;
	}
	
	public static <A, R> AtBuilder at(String name){
		return new AtBuilder(name);
	}

	public static <A, R> AtBuilder on(Class<? extends Throwable> e){
		return new AtBuilder(e.getName());
	}

	public String getName() {
		return name;
	}

	public Class<? extends NodeBase> getTask() {
		return nodeBase;
	}
	
	public NodeType getTaskType() {
		return nodeType;
	}
	
	
	public NodeDescriptor inQueue(String queue){
		return new NodeDescriptor(name, nodeBase, nodeType, queue);
	}
	
	public String getQueue() {
		return queue;
	}
	
	
	

}
