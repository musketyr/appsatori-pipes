package eu.appsatori.pipes

import spock.lang.Specification;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

class QueueCleanerSpec extends Specification{
	
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
		QueueCleaner.clean("", "taskid", 5)
		
		then:
		1 == config.localTaskQueue.getQueueStateInfo()[QueueFactory.defaultQueue.queueName].countTasks
		
	}

}
