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

package eu.appsatori.gaeflow.mem;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import eu.appsatori.gaeflow.BaseNodeDatastore;
import eu.appsatori.gaeflow.Node;
import eu.appsatori.gaeflow.NodeDatastore;

public class InMemoryNodeDatastore extends BaseNodeDatastore implements NodeDatastore {
	
	private final Map<String, Node<?,?>> transitions;
	
	public InMemoryNodeDatastore(Node<?,?>... tranistions){
		Map<String, Node<?,?>> builder = new HashMap<String, Node<?,?>>(tranistions.length);
		
		for(Node<?,?> t: tranistions){
			builder.put(t.getName(), t);
		}
		this.transitions = Collections.unmodifiableMap(builder);
	}
	
	@SuppressWarnings("unchecked")
	public <A, R> Node<A, R> find(String name) {
		return (Node<A, R>) transitions.get(name);
	}
	
	@SuppressWarnings("unchecked")
	public <E extends Throwable, R> Node<E, R> find(
			Class<? extends Throwable> from) {
		Node<E,R> node = find(from.getName());
		if(node != null){
			return node;
		}
		if(Throwable.class.equals(from)){
			return null;
		}
		return find((Class<? extends Throwable>)from.getSuperclass());
	}
	

}
