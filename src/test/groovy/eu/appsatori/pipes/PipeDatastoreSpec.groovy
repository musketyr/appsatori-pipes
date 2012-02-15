/*
 * Copyright 2012 AppSatori s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.appsatori.pipes

import groovy.transform.Canonical;
import spock.lang.Specification;
import spock.lang.Unroll;

abstract class PipeDatastoreSpec extends Specification{
	
	PipeDatastore pds = createDatastore()
	
	def "Handle serialization"(){
		String te
		String ptid = '__parallel_task__01__'
		int parallelTasksCount = 1
		MySerializable myser = new MySerializable("Hello")
		
		expect:
		pds.logTaskStarted(ptid, parallelTasksCount)
		!pds.logTaskFinished(ptid, 0, myser)
		myser.text == pds.getTaskResults(ptid)[0].text
	}
	
	@Unroll({ "$literal must be $cls"})
	def "Handle primitives"(){
		String te
		String ptid = '__parallel_task__01__'
		int parallelTasksCount = 1
		
		expect:
		pds.logTaskStarted(ptid, parallelTasksCount)
		!pds.logTaskFinished(ptid, 0, literal)
		cls == pds.getTaskResults(ptid)[0].getClass()
		where:
		literal 					| cls
		Byte.valueOf((byte)100)		| Byte
		Short.valueOf((short)25)	| Short
		Integer.valueOf(124578)		| Integer
		Character.valueOf((char)'c')| Character
		Long.valueOf((long)1246)	| Long
		Float.valueOf((float)12.22)	| Float
		Double.valueOf((double)12.1)| Double 
		new byte[10]				| byte[].class
		
	}
	
		
	def "Handle parallel task"(){
		String ptid = '__parallel_task__01__'
		int parallelTasksCount = 3
		
		expect:
		true == pds.logTaskStarted(ptid, parallelTasksCount)
		false == pds.logTaskStarted(ptid, parallelTasksCount)
		3 == pds.getParallelTaskCount(ptid)
		
		when:
		pds.getTaskResults(ptid)
		
		then:
		thrown(IllegalStateException)
		
		
		
		expect:
		2 == pds.logTaskFinished(ptid, 0, 'first')
		
		
		
		when:
		pds.getTaskResults(ptid)
		
		then:
		thrown(IllegalStateException)
		
		
		
		expect:
		1 == pds.logTaskFinished(ptid, 1, 'second')
		
		
		
		when:
		pds.getTaskResults(ptid)
		
		then:
		thrown(IllegalStateException)
		
		then:
		0 == pds.logTaskFinished(ptid, 2, 'third')
		
		
		
		when:
		0 == pds.logTaskFinished(ptid, 0, 'one again')
		
		then:
		thrown(IllegalStateException)
		
		
		
		when:
		0 == pds.logTaskFinished(ptid, 3, 'fourth')
		then:
		thrown(IndexOutOfBoundsException)
		
		
		
		when:
		pds.logTaskFinished(ptid + 'xyz', 1, 'third')
		
		then:
		thrown(IllegalArgumentException)
		
		
		
		expect:
		['first','second','third'] == pds.getTaskResults(ptid)
		true == pds.clearTaskLog(ptid)
		false == pds.clearTaskLog(ptid)
		
		
		
		when:
		pds.getTaskResults(ptid)
		
		then:
		thrown(IllegalArgumentException)
	}
	
	def "Stash argument and retrieve"(){
		when:
		def testObject = "test object"
		String key = pds.stashArgument(testObject)
		
		then:
		testObject == pds.retrieveArgument(key)
	}

	protected abstract PipeDatastore createDatastore();
	
}

@Canonical class MySerializable implements Serializable {
	String text
}
