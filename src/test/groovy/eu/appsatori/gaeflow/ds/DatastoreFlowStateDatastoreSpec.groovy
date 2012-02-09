package eu.appsatori.gaeflow.ds

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper

import eu.appsatori.gaeflow.FlowStateDatastore;
import eu.appsatori.gaeflow.FlowStateDatastoreSpec;
import eu.appsatori.gaeflow.ds.DatastoreFlowStateDatastore;

class DatastoreFlowStateDatastoreSpec extends FlowStateDatastoreSpec {
	
	LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())
	
	
	@Override
	protected FlowStateDatastore createFlowDatastore() {
		return new DatastoreFlowStateDatastore();
	}
	
	
	def setup(){
		helper.setUp()
	}
	
	def cleanup(){
		helper.tearDown()
	}
	

}
