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

package eu.appsatori.pipes

import spock.lang.Unroll;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalFileServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

class DatastorePipeDatastoreSpec extends PipeDatastoreSpec {
	
	LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(), new LocalBlobstoreServiceTestConfig(), new LocalFileServiceTestConfig())
	PipeDatastore pds = createDatastore()
	DatastoreService ds = DatastoreServiceFactory.datastoreService
	
	@Unroll({ "${count} tasks logged" })
	def "Task logged"(){
		expect:
		0		== ds.getActiveTransactions().size()
		
		when:
		count.times { pds.logTaskStarted('taskid') }
		NamespaceManager.set(DatastoreHelper.FLOW_NAMESPACE)
		PreparedQuery taskQuery = ds.prepare(new Query(DatastoreHelper.TASK_KIND))
		PreparedQuery subtaskQuery = ds.prepare(new Query(DatastoreHelper.SUBTASK_KIND))
		
		then:
		0		== ds.getActiveTransactions().size()
		1 		== taskQuery.asList(FetchOptions.Builder.withDefaults()).size()
		count 	== subtaskQuery.asList(FetchOptions.Builder.withDefaults()).size()
		
		when:
		Entity taskEntity = taskQuery.asSingleEntity()
		Entity firstSubtask = subtaskQuery.asList(FetchOptions.Builder.withDefaults())[0]
		
		then:
		'taskid' 	== taskEntity.getKey().getName()
		count		== taskEntity.getProperty(DatastorePipeDatastore.TOTAL_COUNT)
		count		== taskEntity.getProperty(DatastorePipeDatastore.COUNT)
		1 			== firstSubtask.getKey().getId()
		taskEntity.getKey() == firstSubtask.getKey().getParent()
		null 		== firstSubtask.getProperty(DatastorePipeDatastore.FINISHED)
		null 		== firstSubtask.getProperty(DatastorePipeDatastore.RESULT)
		
		when:
		pds.logTaskFinished('taskid', 0, 'result')
		taskEntity = taskQuery.asSingleEntity()
		firstSubtask = subtaskQuery.asList(FetchOptions.Builder.withDefaults())[0]
		
		then:
		0			== ds.getActiveTransactions().size()
		count 		== taskEntity.getProperty(DatastorePipeDatastore.TOTAL_COUNT)
		count - 1 	== taskEntity.getProperty(DatastorePipeDatastore.COUNT)
		true 		== firstSubtask.getProperty(DatastorePipeDatastore.FINISHED)
		'result' 	== firstSubtask.getProperty(DatastorePipeDatastore.RESULT)
		
		when:
		pds.clearTaskLog('taskid', true)

		then:
		0 	== taskQuery.asList(FetchOptions.Builder.withDefaults()).size()
		0 	== subtaskQuery.asList(FetchOptions.Builder.withDefaults()).size()
		0	== ds.getActiveTransactions().size()
		
		
		where:
		count << [1,2,3,4,5,6,7,8,9,10]
	}
	
	
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
