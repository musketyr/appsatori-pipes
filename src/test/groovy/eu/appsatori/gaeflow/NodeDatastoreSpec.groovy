package eu.appsatori.gaeflow

import static eu.appsatori.gaeflow.Node.*;

import eu.appsatori.gaeflow.NodeDatastore;
import eu.appsatori.gaeflow.stubs.StubTask1;
import eu.appsatori.gaeflow.stubs.StubTask2;
import eu.appsatori.gaeflow.stubs.StubTask3;
import eu.appsatori.gaeflow.stubs.StubTask4;
import spock.lang.Specification;
import spock.lang.Unroll;

abstract class NodeDatastoreSpec extends Specification{
	
	NodeDatastore tds = createNodeDatastore(
		at('one').run(StubTask1),
		at('two').fork(StubTask2),
		at('one').challange(StubTask3),
		on(RuntimeException).run(StubTask1),
		on(IllegalArgumentException).run(StubTask2)
	)
		
	def "Find transition"(){
		Node n = tds.find('one')
		
		expect:
		n.name == 'one'
		n.taskType == TaskType.PARALLEL_COMPETETIVE
		n.task == StubTask3
	}
	
	def "Return null if there is no such transition"(){
		Node n = tds.find('four')
		
		expect:
		!n
	}
	
	@Unroll({ "It should${found ? ' ' : ' not'} find node for ${ex}" })
	def "Find exception handler"(){
		when:
		Node r = tds.find(ex)
		
		then:
		!!r == found
		
		where:
		found   | ex
		true	| RuntimeException
		false	| Throwable
		false	| Exception
		true	| IllegalStateException
		true	| IllegalArgumentException
	}

	protected abstract NodeDatastore createNodeDatastore(Node... transitions);
	
	
}
