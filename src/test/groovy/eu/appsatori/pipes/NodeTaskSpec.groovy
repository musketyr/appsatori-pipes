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

import eu.appsatori.pipes.sample.JoinNode;
import eu.appsatori.pipes.sample.ParallelNode;
import eu.appsatori.pipes.sample.SerialNode;



class NodeTaskSpec extends Specification {

	PipeDatastore fds = Mock()

	LocalTaskQueueTestConfig config = new LocalTaskQueueTestConfig()
	LocalServiceTestHelper helper = new LocalServiceTestHelper(config)
	
	def setup(){
		Pipes.setRunner(new AppEngineNodeRunner(fds))
		helper.setUp();
	}
	
	def cleanup(){
		helper.tearDown();
	}
	
	def 'Execute serial task'(){
		NodeTask executor = new NodeTask(PipeType.SERIAL, SerialNode, 'taskid', 0, 10)
		
		expect:
		executor.node == SerialNode
		executor.baseTaskId == 'taskid'
		executor.arg == 10
		executor.index == 0
		
		when:
		executor.run()
		
		then:
		1 * fds.isActive('taskid') >> true
		10 == config.localTaskQueue.getQueueStateInfo()[QueueFactory.defaultQueue.queueName].countTasks
	}
	
	def 'Execute cometetive task'(){
		NodeTask executor = new NodeTask(PipeType.COMPETETIVE, SerialNode, 'taskid', 0, [10, 20, 30])
		
		expect:
		executor.node == SerialNode
		executor.baseTaskId == 'taskid'
		executor.arg == [10, 20, 30]
		executor.index == 0
		
		when:
		executor.run()
		
		then:
		1 * fds.isActive('taskid') >> true
		10 == config.localTaskQueue.getQueueStateInfo()[QueueFactory.defaultQueue.queueName].countTasks
	}
	
	def 'Execute unfisished parallel task'(){
		NodeTask executor = new NodeTask(PipeType.PARALLEL, ParallelNode, 'taskid', 0, 10L)
		
		expect:
		executor.node == ParallelNode
		executor.baseTaskId == 'taskid'
		executor.arg == 10L
		executor.index == 0
		
		when:
		executor.run()
		
		then:
		1 * fds.isActive('taskid') >> true
		1 * fds.logTaskFinished('taskid', 0, _) >> 2
	}
	
	def 'Execute fisished parallel task'(){
		NodeTask executor = new NodeTask(PipeType.PARALLEL,ParallelNode, 'taskid', 0, 10L)
		
		expect:
		executor.node == ParallelNode
		executor.baseTaskId == 'taskid'
		executor.arg == 10L
		executor.index == 0
		
		when:
		executor.run()
		
		then:
		1 * fds.isActive('taskid') >> true
		1 * fds.logTaskFinished('taskid', 0, _) >> 0
		1 * fds.getTaskResults('taskid')
		1 * fds.clearTaskLog('taskid')
		1 == config.localTaskQueue.getQueueStateInfo()[QueueFactory.defaultQueue.queueName].countTasks
	}
	
}
