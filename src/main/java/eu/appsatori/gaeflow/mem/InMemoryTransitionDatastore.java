package eu.appsatori.gaeflow.mem;

import java.util.Collection;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

import eu.appsatori.gaeflow.Transition;
import eu.appsatori.gaeflow.TransitionDatastore;

public class InMemoryTransitionDatastore implements TransitionDatastore {
	
	final Table<String, String, Transition<?, ?>> transitions;
	
	public InMemoryTransitionDatastore(Collection<Transition<?,?>> tranistions){
		ImmutableTable.Builder<String, String, Transition<?,?>> builder = ImmutableTable.builder();
		
		for(Transition<?,?> t: tranistions){
			builder.put(t.getFrom(), t.getTo(), t);
		}
		this.transitions = builder.build();
	}
	
	@SuppressWarnings("unchecked")
	public <A, R> Transition<A, R> find(String from, String to) {
		return (Transition<A, R>) transitions.get(from, to);
	}
	

}
