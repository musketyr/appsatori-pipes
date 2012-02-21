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

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalFileServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import eu.appsatori.pipes.DevNodeRunner.ExecutionListener;
import eu.appsatori.pipes.sample.FinishStreamingNode;
import eu.appsatori.pipes.sample.StartNode;
import eu.appsatori.pipes.sample.StartStreamingNode;
import eu.appsatori.pipes.sample.WinNode;

import spock.lang.Ignore;
import spock.lang.Specification


class DevNodeRunnerStreamingSpec extends Specification {
	
	
	LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(),new LocalBlobstoreServiceTestConfig(), new LocalFileServiceTestConfig())
	//DevNodeRunner runner = new DevNodeRunner(new DevPipeDatastore(), Executors.newFixedThreadPool(10))
	DevNodeRunner runner = new DevNodeRunner()
	
	def "Try streaming node"(){
		Object finishStreamingNodeArg = null;
		runner.addExecutionListener(new ExecutionListener(){
			void taskExecuted(NodeTask task) {
				println task
				if(task.node == FinishStreamingNode){
					finishStreamingNodeArg = task.arg
				}
			}
		});
		Pipes.runner = runner
		String id = runner.run(PipeType.STREAMING, StartStreamingNode, null)
		Thread.currentThread().sleep(5000)
		
		expect:
		finishStreamingNodeArg
		finishStreamingNodeArg == [5,5,2,9,5]
		
	}
	
	def setup(){
		helper.setUp()
	}
	
	def cleanup(){
		helper.tearDown()
	}
	
}
