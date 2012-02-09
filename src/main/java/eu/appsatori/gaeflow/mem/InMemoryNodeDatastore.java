package eu.appsatori.gaeflow.mem;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import eu.appsatori.gaeflow.Node;
import eu.appsatori.gaeflow.NodeDatastore;

public class InMemoryNodeDatastore implements NodeDatastore {
	
	private final Map<String, Node<?,?>> transitions;
	
	public InMemoryNodeDatastore(Collection<Node<?,?>> tranistions){
		Map<String, Node<?,?>> builder = new HashMap<String, Node<?,?>>(tranistions.size());
		
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
