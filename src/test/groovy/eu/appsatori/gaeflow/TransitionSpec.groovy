package eu.appsatori.gaeflow

import static eu.appsatori.gaeflow.Transition.*
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;

import com.google.appengine.api.taskqueue.TaskOptions;


import eu.appsatori.gaeflow.stubs.StubExceptionHandler;
import eu.appsatori.gaeflow.stubs.StubTask1;
import eu.appsatori.gaeflow.stubs.StubTask2;
import spock.lang.Specification



class TransitionSpec extends Specification {
	
	def "Simple serial transtion"(){
		when:
		def t = from 'one' to 'two' run StubTask1
		
		then:
		t.from == 'one'
		t.to == 'two'
		t.task == StubTask1
		t.taskType == TaskType.SERIAL
	}
	
	def "Simple serial transtion with target"(){
		when:
		def t = from 'one' to 'two' run StubTask1 inQueue 'sync'
		
		then:
		t.from == 'one'
		t.to == 'two'
		t.task == StubTask1
		t.taskType == TaskType.SERIAL
		t.queue == 'sync'
	}
	
	def "Simple serial transtion with options"(){
		when:
		def t = from 'one' to 'two' run StubTask1 withOptions withMethod(TaskOptions.Method.POST)
		
		then:
		t.from == 'one'
		t.to == 'two'
		t.task == StubTask1
		t.taskType == TaskType.SERIAL
		t.options.method == TaskOptions.Method.POST
	}
	
	def "Simple parallel transtion"(){
		when:
		def t = from 'two' to 'three' fork StubTask1
		
		then:
		t.from == 'two'
		t.to == 'three'
		t.task == StubTask1
		t.taskType == TaskType.PARALLEL
	}
	
	def "Simple competetive transtion"(){
		when:
		def t = from 'two' to 'three' win StubTask1
		
		then:
		t.from == 'two'
		t.to == 'three'
		t.task == StubTask1
		t.taskType == TaskType.PARALLEL_COMPETETIVE
	}
	
	

	def "Simple error handler"(){
		when:
		def t = exception IllegalArgumentException during 'one' handleBy StubExceptionHandler
		
		then:
		t.from == 'one'
		t.to == IllegalArgumentException.class.name
		t.task == StubExceptionHandler
		t.taskType == TaskType.EXCEPTION_HANDLER
	}
	
	def "Default error handler"(){
		when:
		def t = exception IllegalArgumentException handleBy StubExceptionHandler
		
		then:
		t.from == ''
		t.to == IllegalArgumentException.class.name
		t.task == StubExceptionHandler
		t.taskType == TaskType.EXCEPTION_HANDLER
	}
	
}
