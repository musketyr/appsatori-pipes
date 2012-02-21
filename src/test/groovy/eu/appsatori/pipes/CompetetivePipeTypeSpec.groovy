package eu.appsatori.pipes

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeType;

import spock.lang.Specification
import spock.lang.Unroll;

class CompetetivePipeTypeSpec extends Specification {

	static final String BASE_ID = "base_id"

	static final int MOCK_INDEX = 2

	static final String QUEUE = ""
	
	PipeDatastore fds = Mock()
	NodeRunner runner = Mock()

	
	def setup(){
		runner.pipeDatastore >> fds
	}
	
	def "Handle end 1"(){
		when:
		fds.isActive(BASE_ID) >> true
		boolean result = PipeType.COMPETETIVE.handlePipeEnd(runner, QUEUE, BASE_ID, MOCK_INDEX, null);
		
		then:
		result 
		1 * fds.setActive(BASE_ID, false) >> true
		1 * fds.getParallelTaskCount(BASE_ID) >> MOCK_INDEX
		1 * runner.clearTasks(QUEUE, BASE_ID, MOCK_INDEX)
		1 * fds.clearTaskLog(BASE_ID, true) >> true
	}
	
	def "Handle end 2"(){
		when:
		fds.isActive(BASE_ID) >> false
		boolean result = PipeType.COMPETETIVE.handlePipeEnd(runner, QUEUE, BASE_ID, MOCK_INDEX, null);
		
		then:
		!result
		0 * fds.setActive(BASE_ID, false) >> true
		0 * fds.getParallelTaskCount(BASE_ID) >> MOCK_INDEX
		0 * runner.clearTasks(QUEUE, BASE_ID, MOCK_INDEX)
		0 * fds.clearTaskLog(BASE_ID, true) >> true
	}
	
	def "Handle end 3"(){
		when:
		fds.isActive(BASE_ID) >> true
		boolean result = PipeType.COMPETETIVE.handlePipeEnd(runner, QUEUE, BASE_ID, MOCK_INDEX, null);
		
		then:
		!result
		1 * fds.setActive(BASE_ID, false) >> false
		0 * fds.getParallelTaskCount(BASE_ID) >> MOCK_INDEX
		0 * runner.clearTasks(QUEUE, BASE_ID, MOCK_INDEX)
		0 * fds.clearTaskLog(BASE_ID, true) >> true
	}

}
