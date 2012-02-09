package eu.appsatori.gaeflow

import eu.appsatori.gaeflow.FlowStateDatastore;
import spock.lang.Specification;

abstract class FlowStateDatastoreSpec extends Specification{
	
		
	def "Handle parallel task"(){
		FlowStateDatastore fds = createFlowDatastore()
		String ptid = '__parallel_task__01__'
		int parallelTasksCount = 3
		
		expect:
		true == fds.logTaskStarted(ptid, parallelTasksCount)
		false == fds.logTaskStarted(ptid, parallelTasksCount)
		3 == fds.getParallelTaskCount(ptid)
		
		when:
		fds.getTaskResults(ptid)
		
		then:
		thrown(IllegalStateException)
		
		
		
		expect:
		2 == fds.logTaskFinished(ptid, 0, 'first')
		
		
		
		when:
		fds.getTaskResults(ptid)
		
		then:
		thrown(IllegalStateException)
		
		
		
		expect:
		1 == fds.logTaskFinished(ptid, 1, 'second')
		
		
		
		when:
		fds.getTaskResults(ptid)
		
		then:
		thrown(IllegalStateException)
		
		
		when:
		fds.logTaskFinished(ptid, 2, new Boolean[0])
		
		then:
		thrown(IllegalArgumentException)
		
		
		
		expect:
		0 == fds.logTaskFinished(ptid, 2, 'third')
		
		
		
		when:
		0 == fds.logTaskFinished(ptid, 0, 'one again')
		
		then:
		thrown(IllegalStateException)
		
		
		
		when:
		0 == fds.logTaskFinished(ptid, 3, 'fourth')
		then:
		thrown(IndexOutOfBoundsException)
		
		
		
		when:
		fds.logTaskFinished(ptid + 'xyz', 1, 'third')
		
		then:
		thrown(IllegalArgumentException)
		
		
		
		expect:
		['first','second','third'] == fds.getTaskResults(ptid)
		true == fds.clearTaskLog(ptid)
		false == fds.clearTaskLog(ptid)
		
		
		
		when:
		fds.getTaskResults(ptid)
		
		then:
		thrown(IllegalArgumentException)
	}

	protected abstract FlowStateDatastore createFlowDatastore();
	
}
