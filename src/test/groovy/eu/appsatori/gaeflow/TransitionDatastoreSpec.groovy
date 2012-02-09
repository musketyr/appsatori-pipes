package eu.appsatori.gaeflow

import static eu.appsatori.gaeflow.Transition.*;

import eu.appsatori.gaeflow.TransitionDatastore;
import eu.appsatori.gaeflow.stubs.StubExceptionHandler;
import eu.appsatori.gaeflow.stubs.StubTask1;
import eu.appsatori.gaeflow.stubs.StubTask2;
import eu.appsatori.gaeflow.stubs.StubTask3;
import spock.lang.Specification;

abstract class TransitionDatastoreSpec extends Specification{
	
	TransitionDatastore tds = createTransitionDatastore(
		from('one').to('two').run(StubTask1),
		from('two').to('three').fork(StubTask2),
		from('one').to('three').win(StubTask3),
		exception(IllegalArgumentException).handleBy(StubExceptionHandler),
		exception(IndexOutOfBoundsException).during('two').handleBy(StubExceptionHandler)
	)
		
	def "Find transition"(){
		Transition t = tds.find('one', 'three')
		
		expect:
		t.from == 'one'
		t.to == 'three'
		t.taskType == TaskType.PARALLEL_COMPETETIVE
		t.task == StubTask3
	}
	
	def "Return null if there is no such transition"(){
		when:
		Transition t = tds.find('one', 'four')
		
		then:
		!t
	}

	protected abstract TransitionDatastore createTransitionDatastore(Transition... transitions);
	
	
}
