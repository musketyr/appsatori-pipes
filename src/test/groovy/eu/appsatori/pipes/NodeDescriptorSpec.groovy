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
import eu.appsatori.pipes.stubs.Stub1Node;
import eu.appsatori.pipes.stubs.Stub2Node;
import eu.appsatori.pipes.stubs.Stub3Node;
import eu.appsatori.pipes.stubs.Stub4Node;
import eu.appsatori.pipes.NodeType;

import spock.lang.Specification;



class NodeDescriptorSpec extends Specification {
	
	def "Simple serial node"(){
		when:
		def n = serial 'one', Stub1Node
		
		then:
		n.name == 'one'
		n.node == Stub1Node
		n.nodeType == NodeType.SERIAL
	}
	
	def "Simple exception handler"(){
		when:
		def n = handler IllegalArgumentException,  Stub1Node
		
		then:
		n.name == IllegalArgumentException.name
		n.node == Stub1Node
		n.nodeType == NodeType.SERIAL
	}
	
	def "Simple serial node with target"(){
		when:
		def n = serial 'two', Stub1Node, 'sync'
		
		then:
		n.name == 'two'
		n.node == Stub1Node
		n.nodeType == NodeType.SERIAL
		n.queue == 'sync'
	}
	
	def "Simple parallel node"(){
		when:
		def n = parallel 'three', Stub1Node
		
		then:
		n.name == 'three'
		n.node == Stub1Node
		n.nodeType == NodeType.PARALLEL
	}
	
	def "From type"(){
		when:
		NodeDescriptor n = from node.node
		
		then:
		n == node
		
		where:
		node << [
			new NodeDescriptor('Stub1', Stub1Node, NodeType.SERIAL, ''),
			new NodeDescriptor('two', Stub2Node, NodeType.SERIAL, ''),
			new NodeDescriptor('Stub3', Stub3Node, NodeType.PARALLEL, ''),
			new NodeDescriptor('four', Stub4Node, NodeType.PARALLEL, 'queue')
		]
	}
	
}
