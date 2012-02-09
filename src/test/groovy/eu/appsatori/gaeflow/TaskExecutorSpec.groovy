package eu.appsatori.gaeflow

import eu.appsatori.gaeflow.stubs.StubTask1;
import eu.appsatori.gaeflow.stubs.StubTask2;
import spock.lang.Specification



class TaskExecutorSpec extends Specification {

	FlowStateDatastore fds = Mock()
	TransitionDatastore tds = Mock()
	
	def setup(){
		FlowStateDatastoreHolder.flowStateDatastore = fds;
		TransitionDatastoreHolder.transitionDatastore = tds;
	}
	
	def cleanup(){
		FlowStateDatastoreHolder.flowStateDatastore = null;
		TransitionDatastoreHolder.transitionDatastore = null;
	}
	
	def 'Execute task'(){
		
		TaskExecutor executor = new TaskExecutor('one', 'taskid', 'hello', StubTask1)
		
		expect:
		executor.arg == 'hello'
		executor.task == StubTask1
		executor.baseTaskId == 'taskid'
		
		when:
		executor.run()
		
		then:
		1 * tds.find('one', 'two') << Transition.from('one').to('two').run(StubTask2)
	}
	
}
