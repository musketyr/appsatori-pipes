package eu.appsatori.gaeflow.mem

import eu.appsatori.gaeflow.Node
import eu.appsatori.gaeflow.NodeDatastore;
import eu.appsatori.gaeflow.NodeDatastoreSpec;

class InMemoryNodeDatastoreSpec extends NodeDatastoreSpec {

	@Override
	protected NodeDatastore createNodeDatastore(Node... transitions) {
		return new InMemoryNodeDatastore(transitions.toList());
	}

}
