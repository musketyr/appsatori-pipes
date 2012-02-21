package eu.appsatori.pipes

import eu.appsatori.pipes.sample.StreamedNode;
import spock.lang.Specification

class StreamingPipeImplSpec extends Specification {
	
	def "Steaming pipe cummulates count"(){
		NodeRunner runner = Mock()
		PipeDatastore pds = Mock()
		runner.pipeDatastore >> pds
		
		Pipes.runner = runner
		
		StreamingPipe pipe = new StreamingPipeImpl("taskid")
		
		when:
		pipe.send(StreamedNode, "Hello")
		
		then:
		1 * runner.run("taskid", PipeType.PARALLEL, StreamedNode, "Hello")
		
		when:
		pipe.send(StreamedNode, "World")
		
		then:
		1 * runner.run("taskid", PipeType.PARALLEL, StreamedNode, "World")
		
		when:
		NodeResult result = pipe.finish()
		
		then:
		result.type == PipeType.STREAMING
		result.next == null
		result.result == null
		
		
	}

}
