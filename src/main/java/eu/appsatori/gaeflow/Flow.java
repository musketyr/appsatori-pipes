package eu.appsatori.gaeflow;


public class Flow {

	private final FlowStateDatastore flowStateDatastore;
	private final NodeDatastore nodeDatastore;
	
	private Flow(FlowStateDatastore flowStateDatastore, NodeDatastore nodeDatastore) {
		this.flowStateDatastore = flowStateDatastore;
		this.nodeDatastore = nodeDatastore;
	}
	
	
	public static Flow getFlow(FlowStateDatastore fds, NodeDatastore nds){
		if(fds == null){
			throw new NullPointerException("Flow datastore cannot be null!");
		}
		FlowStateDatastoreHolder.setFlowStateDatastore(fds);
		if(nds == null){
			throw new NullPointerException("Node datastore cannot be null!");
		}
		NodeDatastoreHolder.setNodeDatastore(nds);
		return new Flow(fds, nds);
	}
	
	public void start(String node){
		start(node, null);
	}
	
	public void start(String node, Object argument){
		new TaskExecutor(flowStateDatastore).execute(nodeDatastore.find(node), argument);
	}
	
	
	
	
}
