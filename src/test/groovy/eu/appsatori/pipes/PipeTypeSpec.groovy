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
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

import spock.lang.Specification;

class PipeTypeSpec extends Specification {
	
	LocalTaskQueueTestConfig config = new LocalTaskQueueTestConfig()
	LocalServiceTestHelper helper = new LocalServiceTestHelper(config)
	
	def setup(){
		helper.setUp()
	}
	
	def cleanup(){
		helper.tearDown()
	}
	
	def "Clean queue"(){
		QueueFactory.defaultQueue.add(TaskOptions.Builder.withTaskName("0_taskid"))
		QueueFactory.defaultQueue.add(TaskOptions.Builder.withTaskName("2_taskid"))
		QueueFactory.defaultQueue.add(TaskOptions.Builder.withTaskName("4_taskid"))
		QueueFactory.defaultQueue.add(TaskOptions.Builder.withTaskName("5_taskid"))
		
		expect:
		4 == config.localTaskQueue.getQueueStateInfo()[QueueFactory.defaultQueue.queueName].countTasks
		
		when:
		PipeType.clean("", "taskid", 5)
		
		then:
		1 == config.localTaskQueue.getQueueStateInfo()[QueueFactory.defaultQueue.queueName].countTasks
		
	}
	
	def "Count size"(){
		expect:
		1 == PipeType.sizeOf(null)
		1 == PipeType.sizeOf(new Object())
		1 == PipeType.sizeOf("Hallo")
		3 == PipeType.sizeOf(new String[3])
		5 == PipeType.sizeOf(["H","a","l","l","o"]);
	}
	
}
