package eu.appsatori.gaeflow

import static eu.appsatori.gaeflow.Transition.*;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

import eu.appsatori.gaeflow.stubs.StubTask1;

import spock.lang.Specification

class TaskWrapperSpec extends Specification {
	
	LocalTaskQueueTestConfig config = new LocalTaskQueueTestConfig()
	LocalServiceTestHelper helper = new LocalServiceTestHelper(config)
	
	FlowStateDatastore fds = Mock()
	TaskWrapper executor = new TaskWrapper(fds)
	
	def "Executes serial flow"(){
		when:
		Transition<?,?> transition = from 'start' to 'end' run StubTask1
		executor.wrapAndExecute(transition, 'hello')
		
		then:
		1 * fds.logTaskStarted(_, 0)
		1 == config.localTaskQueue.getQueueStateInfo()[QueueFactory.defaultQueue.queueName].countTasks
	}
	
	def "Executes parallel flow"(){
		when:
		Transition<?,?> transition = from 'start' to 'end' fork StubTask1
		executor.wrapAndExecute(transition, ['hello', 'world', '!'])
		
		then:
		1 * fds.logTaskStarted(_, 0)
		1 * fds.logTaskStarted(_, 1)
		1 * fds.logTaskStarted(_, 2)
		3 == config.localTaskQueue.getQueueStateInfo()[QueueFactory.defaultQueue.queueName].countTasks
	}
	
	def setup(){
		helper.setUp()
	}
	
	def cleanup(){
		helper.tearDown()
	}

}
