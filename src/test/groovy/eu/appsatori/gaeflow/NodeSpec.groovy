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
