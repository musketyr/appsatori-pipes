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

import eu.appsatori.pipes.sample.ParallelNode;
import eu.appsatori.pipes.sample.SerialNode;

class PipesSpec extends Specification {
	
	LocalTaskQueueTestConfig config = new LocalTaskQueueTestConfig()
	LocalServiceTestHelper helper = new LocalServiceTestHelper(config)
	
	PipeDatastore fds = Mock()
	
	
	def "Executes serial flow"(){
		when:
		Pipes.start(PipeType.SERIAL, SerialNode, 'hello')
		
		then:
		1 * fds.logTaskStarted(_, 1)
		1 == config.localTaskQueue.getQueueStateInfo()[QueueFactory.defaultQueue.queueName].countTasks
	}
	
	def "Executes parallel flow"(){
		when:
		Pipes.start(PipeType.PARALLEL, ParallelNode, ['hello', 'world', '!'])
		
		then:
		1 * fds.logTaskStarted(_, 3)
		3 == config.localTaskQueue.getQueueStateInfo()[QueueFactory.defaultQueue.queueName].countTasks
	}
	
	def setup(){
		Pipes.setRunner(new AppEngineNodeRunner(fds))
		helper.setUp()
	}
	
	def cleanup(){
		helper.tearDown()
	}

}
