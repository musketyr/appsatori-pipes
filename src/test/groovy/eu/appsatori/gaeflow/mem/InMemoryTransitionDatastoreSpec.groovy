package eu.appsatori.gaeflow.mem

import eu.appsatori.gaeflow.Transition
import eu.appsatori.gaeflow.TransitionDatastore;
import eu.appsatori.gaeflow.TransitionDatastoreSpec;

class InMemoryTransitionDatastoreSpec extends TransitionDatastoreSpec {

	@Override
	protected TransitionDatastore createTransitionDatastore(Transition... transitions) {
		return new InMemoryTransitionDatastore(transitions.toList());
	}

}
