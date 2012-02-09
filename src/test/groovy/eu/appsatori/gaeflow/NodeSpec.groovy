package eu.appsatori.gaeflow

import static eu.appsatori.gaeflow.Node.*;
import eu.appsatori.gaeflow.stubs.StubTask1;

import spock.lang.Specification;



class NodeSpec extends Specification {
	
	def "Simple serial node"(){
		when:
		def n = at 'one' run StubTask1
		
		then:
		n.name == 'one'
		n.task == StubTask1
		n.taskType == TaskType.SERIAL
	}
	
	def "Simple exception handler"(){
		when:
		def n = on IllegalArgumentException run StubTask1
		
		then:
		n.name == IllegalArgumentException.name
		n.task == StubTask1
		n.taskType == TaskType.SERIAL
	}
	
	def "Simple serial node with target"(){
		when:
		def n = at 'two' run StubTask1 inQueue 'sync'
		
		then:
		n.name == 'two'
		n.task == StubTask1
		n.taskType == TaskType.SERIAL
		n.queue == 'sync'
	}
	
	def "Simple parallel node"(){
		when:
		def n = at'three' fork StubTask1
		
		then:
		n.name == 'three'
		n.task == StubTask1
		n.taskType == TaskType.PARALLEL
	}
	
	def "Simple competetive node"(){
		when:
		def n = at 'three' challange StubTask1
		
		then:
		n.name == 'three'
		n.task == StubTask1
		n.taskType == TaskType.PARALLEL_COMPETETIVE
	}
	
}
