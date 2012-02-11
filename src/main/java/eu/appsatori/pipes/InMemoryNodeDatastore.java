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

import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("rawtypes")
public class InMemoryNodeDatastore extends BaseNodeDatastore implements NodeDatastore {
	
	private final Map<String, NodeDescriptor> transitions = new HashMap<String, NodeDescriptor>();
	
	public InMemoryNodeDatastore() {}
	
	public InMemoryNodeDatastore(NodeDescriptor<?>... tranistions){
		for(NodeDescriptor<?> t: tranistions){
			this.transitions.put(t.getName(), t);
		}
	}
	
	public final boolean add(NodeDescriptor node) {
		this.transitions.put(node.getName(), node);
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public <N extends Node<?>> NodeDescriptor<N> find(String from) {
		return transitions.get(from);
	}
	
	@SuppressWarnings("unchecked")
	public <N extends Node<?>> NodeDescriptor<N> find(Class<? extends Throwable> from) {
		NodeDescriptor node = find(from.getName());
		if(node != null){
			return node;
		}
		if(Throwable.class.equals(from)){
			return null;
		}
		return find((Class<? extends Throwable>)from.getSuperclass());
	}
	

}
