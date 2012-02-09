package eu.appsatori.gaeflow;

public class TransitionDatastoreHolder {
	
	private static TransitionDatastore transitionDatastore;
	
	private TransitionDatastoreHolder() {}

	public static TransitionDatastore getTransitionDatastore() {
		return transitionDatastore;
	}

	public static void setTransitionDatastore(TransitionDatastore transitionDatastore) {
		TransitionDatastoreHolder.transitionDatastore = transitionDatastore;
	}

}
