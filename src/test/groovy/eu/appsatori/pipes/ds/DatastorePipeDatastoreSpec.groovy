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

package eu.appsatori.pipes.ds

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper

import eu.appsatori.pipes.PipeDatastore;
import eu.appsatori.pipes.PipeDatastoreSpec;
import eu.appsatori.pipes.ds.DatastorePipeDatastore;

class DatastorePipeDatastoreSpec extends PipeDatastoreSpec {
	
	LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())
	
	
	@Override
	protected PipeDatastore createDatastore() {
		return new DatastorePipeDatastore();
	}
	
	
	def setup(){
		helper.setUp()
	}
	
	def cleanup(){
		helper.tearDown()
	}
	

}
