package eu.appsatori.gaeflow;

public class FlowStateDatastoreHolder {
	
	private static FlowStateDatastore flowStateDatastore;
	
	private FlowStateDatastoreHolder() {}

	public static FlowStateDatastore getFlowStateDatastore() {
		return flowStateDatastore;
	}

	public static void setFlowStateDatastore(FlowStateDatastore flowStateDatastore) {
		FlowStateDatastoreHolder.flowStateDatastore = flowStateDatastore;
	}
	
}
