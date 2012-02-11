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

import static eu.appsatori.pipes.NodeDescriptor.*;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

import eu.appsatori.pipes.stubs.Stub1Node;
import eu.appsatori.pipes.PipeDatastore;
import eu.appsatori.pipes.NodeDescriptor;

import spock.lang.Specification

class PipesExecutorSpec extends Specification {
	
	LocalTaskQueueTestConfig config = new LocalTaskQueueTestConfig()
	LocalServiceTestHelper helper = new LocalServiceTestHelper(config)
	
	PipeDatastore fds = Mock()
	NodeDatastore nds = Mock()
	
	
	def "Executes serial flow"(){
		when:
		Pipes.start('start', 'hello')
		
		then:
		1 * fds.logTaskStarted(_, 1)
		1 * nds.find('start') >> serial('start', Stub1Node)
		1 == config.localTaskQueue.getQueueStateInfo()[QueueFactory.defaultQueue.queueName].countTasks
	}
	
	def "Executes parallel flow"(){
		when:
		Pipes.start('start', ['hello', 'world', '!'])
		
		then:
		1 * fds.logTaskStarted(_, 3)
		1 * nds.find('start') >> parallel('start', Stub1Node)
		3 == config.localTaskQueue.getQueueStateInfo()[QueueFactory.defaultQueue.queueName].countTasks
	}
	
	def setup(){
		Pipes.setPipeDatastore(fds)
		Pipes.setNodeDatastore(nds)
		helper.setUp()
	}
	
	def cleanup(){
		helper.tearDown()
	}

}
