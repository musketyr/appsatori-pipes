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

public final class NodeDescriptor<N extends Node<?>> {
	
	
	private final String name;
	private final Class<N> node;
	private final NodeType nodeType;
	
	private final String queue;
	
	public static <N extends Node<?>> NodeDescriptor<N> handler(Class<? extends Throwable> exception, Class<N> nodeBase, String queue){
		return new NodeDescriptor<N>(exception.getName(), nodeBase, NodeType.SERIAL, queue);
	}
	
	public static <N extends Node<?>> NodeDescriptor<N> handler(Class<? extends Throwable> exception, Class<N> nodeBase){
		return new NodeDescriptor<N>(exception.getName(), nodeBase, NodeType.SERIAL);
	}
	
	public static <N extends Node<?>> NodeDescriptor<N> parallel(String name, Class<N> nodeBase, String queue){
		return new NodeDescriptor<N>(name, nodeBase, NodeType.PARALLEL, queue);
	}
	
	public static <N extends Node<?>> NodeDescriptor<N> parallel(String name, Class<N> nodeBase){
		return new NodeDescriptor<N>(name, nodeBase, NodeType.PARALLEL);
	}
	
	public static <N extends Node<?>> NodeDescriptor<N> serial(String name, Class<N> nodeBase, String queue){
		return new NodeDescriptor<N>(name, nodeBase, NodeType.SERIAL, queue);
	}
	
	public static <N extends Node<?>> NodeDescriptor<N> serial(String name, Class<N> nodeBase){
		return new NodeDescriptor<N>(name, nodeBase, NodeType.SERIAL);
	}
	
	NodeDescriptor(String name, Class<N> nodeBase, NodeType type) {
		this(name, nodeBase, type, "");
	}
	
	NodeDescriptor(String name, Class<N> nodeBase, NodeType type, String queue) {
		this.name = name;
		this.node = nodeBase;
		this.nodeType = type;
		this.queue = queue;
	}
	
	public String getName() {
		return name;
	}

	public Class<N> getNode() {
		return node;
	}
	
	public NodeType getNodeType() {
		return nodeType;
	}
	
	
	public String getQueue() {
		return queue;
	}
	
	private N createTaskInstance() {
		try {
			return (N) getNode().newInstance();
		} catch (InstantiationException e) {
			throw new IllegalStateException("Cannot initiate instance " + getNode().getName() + " does it have parameterless constructor?");
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Cannot initiate instance " + getNode().getName() + " does it have parameterless constructor?");
		}
	}

	@SuppressWarnings("unchecked")
	public <A> NodeResult execute(Object arg, int index) throws Exception {
		return getNodeType().execute((Node<A>) createTaskInstance(), arg, index);
	}
	

}
