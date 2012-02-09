/*
 * Copyright 2012 AppSatori s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
