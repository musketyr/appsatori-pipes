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
	
	public static <N extends Node<?>> NodeDescriptor<N> from(Class<N> nodeBase){
		String name = nodeBase.getSimpleName().replaceAll("Node$", "");
		NodeType type = NodeType.SERIAL;
		String queue = "";
		
		if(nodeBase.isAnnotationPresent(Serial.class) && nodeBase.isAnnotationPresent(Parallel.class)){
			throw new IllegalArgumentException("Only one of Serial and Parallel annotation must be used!");
		}
		
		if(nodeBase.isAnnotationPresent(Serial.class)){
			String nameFromAnno = nodeBase.getAnnotation(Serial.class).value();
			if(!"".equals(nameFromAnno)){
				name = nameFromAnno;
			}
		} if(nodeBase.isAnnotationPresent(Parallel.class)){
			String nameFromAnno = nodeBase.getAnnotation(Parallel.class).value();
			if(!"".equals(nameFromAnno)){
				name = nameFromAnno;
			}
			type = NodeType.PARALLEL;
		}
		
		if(nodeBase.isAnnotationPresent(Queue.class)){
			queue = nodeBase.getAnnotation(Queue.class).value();
		}
		
		return new NodeDescriptor<N>(name, nodeBase, type, queue);
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
	
	public boolean isInDefaultQueue(){
		return "".equals(queue);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		result = prime * result
				+ ((nodeType == null) ? 0 : nodeType.hashCode());
		result = prime * result + ((queue == null) ? 0 : queue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeDescriptor<?> other = (NodeDescriptor<?>) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		if (nodeType != other.nodeType)
			return false;
		if (queue == null) {
			if (other.queue != null)
				return false;
		} else if (!queue.equals(other.queue))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Node '").append(name).append("' (").append(node.getName()).append(")");
		if(NodeType.PARALLEL == nodeType){
			sb.append(" running parallel");
		}
		if("".equals(queue)){
			sb.append(" in default queue");
		} else {
			sb.append(" in queue ").append(queue);
		}
		return sb.toString();
	}
	
	
	

}
