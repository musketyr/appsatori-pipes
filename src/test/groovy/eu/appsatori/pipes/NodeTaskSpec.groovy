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

import spock.lang.Specification;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

import eu.appsatori.pipes.stubs.Stub1Node;



class NodeTaskSpec extends Specification {

	PipeDatastore fds = Mock()

	LocalTaskQueueTestConfig config = new LocalTaskQueueTestConfig()
	LocalServiceTestHelper helper = new LocalServiceTestHelper(config)
	
	def setup(){
		Pipes.pipeDatastore = fds;
		helper.setUp();
	}
	
	def cleanup(){
		helper.tearDown();
	}
	
	def 'Execute serial task'(){
		NodeTask executor = new NodeTask(NodeType.SERIAL, Stub1Node, 'taskid', 0, 'hello')
		
		expect:
		executor.node == Stub1Node
		executor.baseTaskId == 'taskid'
		executor.arg == 'hello'
		executor.index == 0
		
		when:
		executor.run()
		
		then:
		1 == config.localTaskQueue.getQueueStateInfo()[QueueFactory.defaultQueue.queueName].countTasks
	}
	
	def 'Execute unfisished parallel task'(){
		NodeTask executor = new NodeTask(NodeType.PARALLEL, Stub1Node, 'taskid', 0, 'hello')
		
		expect:
		executor.node == Stub1Node
		executor.baseTaskId == 'taskid'
		executor.arg == 'hello'
		executor.index == 0
		
		when:
		executor.run()
		
		then:
		1 * fds.logTaskFinished('taskid', 0, 5) >> 2
	}
	
	def 'Execute fisished parallel task'(){
		NodeTask executor = new NodeTask(NodeType.PARALLEL,Stub1Node, 'taskid', 0, 'hello')
		
		expect:
		executor.node == Stub1Node
		executor.baseTaskId == 'taskid'
		executor.arg == 'hello'
		executor.index == 0
		
		when:
		executor.run()
		
		then:
		1 * fds.logTaskFinished('taskid', 0, 5) >> 0
		1 * fds.getTaskResults('taskid')
		1 * fds.clearTaskLog('taskid')
		1 == config.localTaskQueue.getQueueStateInfo()[QueueFactory.defaultQueue.queueName].countTasks
	}
	
}
