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

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

import eu.appsatori.pipes.stubs.FailingTask;
import eu.appsatori.pipes.stubs.StubTask1;
import eu.appsatori.pipes.stubs.StubTask2;
import eu.appsatori.pipes.PipeDatastore;
import eu.appsatori.pipes.NodeDescriptor;
import eu.appsatori.pipes.NodeDatastore;
import eu.appsatori.pipes.NodeTask;
import spock.lang.Specification



class NodeTaskSpec extends Specification {

	PipeDatastore fds = Mock()
	NodeDatastore tds = Mock()

	LocalTaskQueueTestConfig config = new LocalTaskQueueTestConfig()
	LocalServiceTestHelper helper = new LocalServiceTestHelper(config)
	
	def setup(){
		Pipes.pipeDatastore = fds;
		Pipes.nodeDatastore = tds;
		helper.setUp();
	}
	
	def cleanup(){
		Pipes.pipeDatastore = null;
		Pipes.nodeDatastore = null;
		helper.tearDown();
	}
	
	def 'Execute serial task'(){
		NodeDescriptor node1 = NodeDescriptor.at('one').run(StubTask1)
		NodeDescriptor node2 = NodeDescriptor.at('two').run(StubTask2)
		NodeTask executor = new NodeTask('one', 'taskid', 0, 'hello')
		
		expect:
		executor.nodeName == 'one'
		executor.baseTaskId == 'taskid'
		executor.arg == 'hello'
		executor.index == 0
		
		when:
		executor.run()
		
		then:
		1 * tds.find('one') >> node1
		1 * tds.find('two') >> node2
		1 == config.localTaskQueue.getQueueStateInfo()[QueueFactory.defaultQueue.queueName].countTasks
	}
	
	def 'Execute unfisished parallel task'(){
		NodeDescriptor node1 = NodeDescriptor.at('one').fork(StubTask1)
		NodeDescriptor node2 = NodeDescriptor.at('two').run(StubTask2)
		NodeTask executor = new NodeTask('one', 'taskid', 0, 'hello')
		
		expect:
		executor.nodeName == 'one'
		executor.baseTaskId == 'taskid'
		executor.arg == 'hello'
		executor.index == 0
		
		when:
		executor.run()
		
		then:
		1 * fds.logTaskFinished('taskid', 0, 5) >> 2
		1 * tds.find('one') >> node1
	}
	
	def 'Execute fisished parallel task'(){
		NodeDescriptor node1 = NodeDescriptor.at('one').fork(StubTask1)
		NodeDescriptor node2 = NodeDescriptor.at('two').run(StubTask2)
		NodeTask executor = new NodeTask('one', 'taskid', 0, 'hello')
		
		expect:
		executor.nodeName == 'one'
		executor.baseTaskId == 'taskid'
		executor.arg == 'hello'
		executor.index == 0
		
		when:
		executor.run()
		
		then:
		1 * fds.logTaskFinished('taskid', 0, 5) >> 0
		1 * fds.getTaskResults('taskid')
		1 * fds.clearTaskLog('taskid')
		1 * tds.find('one') >> node1
		1 * tds.find('two') >> node2
		1 == config.localTaskQueue.getQueueStateInfo()[QueueFactory.defaultQueue.queueName].countTasks
	}
	
}
