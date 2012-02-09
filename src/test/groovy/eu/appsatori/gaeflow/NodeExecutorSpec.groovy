package eu.appsatori.gaeflow

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

import eu.appsatori.gaeflow.stubs.StubTask1;
import eu.appsatori.gaeflow.stubs.StubTask2;
import spock.lang.Specification



class NodeExecutorSpec extends Specification {

	FlowStateDatastore fds = Mock()
	NodeDatastore tds = Mock()

	LocalTaskQueueTestConfig config = new LocalTaskQueueTestConfig()
	LocalServiceTestHelper helper = new LocalServiceTestHelper(config)
	
	def setup(){
		FlowStateDatastoreHolder.flowStateDatastore = fds;
		NodeDatastoreHolder.nodeDatastore = tds;
		helper.setUp();
	}
	
	def cleanup(){
		FlowStateDatastoreHolder.flowStateDatastore = null;
		NodeDatastoreHolder.nodeDatastore = null;
		helper.tearDown();
	}
	
	def 'Execute serial task'(){
		Node node1 = Node.at('one').run(StubTask1)
		Node node2 = Node.at('two').run(StubTask2)
		NodeExecutor executor = new NodeExecutor('one', 'taskid', 0, 'hello')
		
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
		Node node1 = Node.at('one').fork(StubTask1)
		Node node2 = Node.at('two').run(StubTask2)
		NodeExecutor executor = new NodeExecutor('one', 'taskid', 0, 'hello')
		
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
		Node node1 = Node.at('one').fork(StubTask1)
		Node node2 = Node.at('two').run(StubTask2)
		NodeExecutor executor = new NodeExecutor('one', 'taskid', 0, 'hello')
		
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
	
	def 'Execute parallel competetive task'(){
		Node node1 = Node.at('one').challange(StubTask1)
		Node node2 = Node.at('two').run(StubTask2)
		NodeExecutor executor = new NodeExecutor('one', 'taskid', 0, 'hello')
		
		expect:
		executor.nodeName == 'one'
		executor.baseTaskId == 'taskid'
		executor.arg == 'hello'
		executor.index == 0
		
		when:
		executor.run()
		
		then:
		1 * fds.clearTaskLog('taskid')
		1 * tds.find('one') >> node1
		1 * tds.find('two') >> node2
		1 == config.localTaskQueue.getQueueStateInfo()[QueueFactory.defaultQueue.queueName].countTasks
		
		where:
		remaining << [0, 1]
	}
	
}