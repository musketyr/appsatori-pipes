package eu.appsatori.gaeflow;

import eu.appsatori.gaeflow.ds.DatastoreFlowStateDatastore;

public class FlowStateDatastoreHolder {
	
	private static FlowStateDatastore flowStateDatastore = new DatastoreFlowStateDatastore();
	
	private FlowStateDatastoreHolder() {}

	public static FlowStateDatastore getFlowStateDatastore() {
		return flowStateDatastore;
	}

	public static void setFlowStateDatastore(FlowStateDatastore flowStateDatastore) {
		FlowStateDatastoreHolder.flowStateDatastore = flowStateDatastore;
	}
	
}
