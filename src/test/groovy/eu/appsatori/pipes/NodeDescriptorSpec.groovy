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

import static eu.appsatori.pipes.NodeDescriptor.*;
import eu.appsatori.pipes.stubs.StubTask1;
import eu.appsatori.pipes.NodeType;

import spock.lang.Specification;



class NodeDescriptorSpec extends Specification {
	
	def "Simple serial node"(){
		when:
		def n = serial 'one', StubTask1
		
		then:
		n.name == 'one'
		n.node == StubTask1
		n.nodeType == NodeType.SERIAL
	}
	
	def "Simple exception handler"(){
		when:
		def n = handler IllegalArgumentException,  StubTask1
		
		then:
		n.name == IllegalArgumentException.name
		n.node == StubTask1
		n.nodeType == NodeType.EXCEPTION_HANDLER
	}
	
	def "Simple serial node with target"(){
		when:
		def n = serial 'two', StubTask1, 'sync'
		
		then:
		n.name == 'two'
		n.node == StubTask1
		n.nodeType == NodeType.SERIAL
		n.queue == 'sync'
	}
	
	def "Simple parallel node"(){
		when:
		def n = parallel 'three', StubTask1
		
		then:
		n.name == 'three'
		n.node == StubTask1
		n.nodeType == NodeType.PARALLEL
	}
	
}
