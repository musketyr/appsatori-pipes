package eu.appsatori.gaeflow

import static eu.appsatori.gaeflow.Node.*;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

import eu.appsatori.gaeflow.stubs.StubTask1;

import spock.lang.Specification

class TaskExecutorSpec extends Specification {
	
	LocalTaskQueueTestConfig config = new LocalTaskQueueTestConfig()
	LocalServiceTestHelper helper = new LocalServiceTestHelper(config)
	
	FlowStateDatastore fds = Mock()
	TaskExecutor executor = new TaskExecutor(fds)
	
	def "Executes serial flow"(){
		when:
		Node<?,?> node = at 'start' run StubTask1
		executor.execute(node, 'hello')
		
		then:
		1 * fds.logTaskStarted(_, 0)
		1 == config.localTaskQueue.getQueueStateInfo()[QueueFactory.defaultQueue.queueName].countTasks
	}
	
	def "Executes parallel flow"(){
		when:
		Node<?,?> node = at 'start' fork StubTask1
		executor.execute(node, ['hello', 'world', '!'])
		
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
