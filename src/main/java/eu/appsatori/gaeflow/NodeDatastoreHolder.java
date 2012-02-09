package eu.appsatori.gaeflow;

public class NodeDatastoreHolder {
	
	private static NodeDatastore nodeDatastore;
	
	private NodeDatastoreHolder() {}

	public static NodeDatastore getNodeDatastore() {
		return nodeDatastore;
	}

	public static void setNodeDatastore(NodeDatastore nodeDatastore) {
		NodeDatastoreHolder.nodeDatastore = nodeDatastore;
	}

}
