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
import eu.appsatori.pipes.NodeDescriptor;
import eu.appsatori.pipes.NodeDatastore;
import eu.appsatori.pipes.NodeType;
import spock.lang.Specification;
import spock.lang.Unroll;

abstract class NodeDatastoreSpec extends Specification{
	
	NodeDatastore tds = createNodeDatastore(
		serial('one',Stub1Node),
		parallel('two',Stub2Node),
		handler(RuntimeException,Stub1Node),
		handler(IllegalArgumentException,Stub2Node)
	)
	
	def "Add node"(){
		expect:
		tds.add(serial('three',Stub1Node))
		tds.find('three')
	}
	
	def "Find transition"(){
		NodeDescriptor n = tds.find('one')
		
		expect:
		n.name == 'one'
		n.nodeType == NodeType.SERIAL
		n.node == Stub1Node
	}
	
	def "Return null if there is no such transition"(){
		NodeDescriptor n = tds.find('four')
		
		expect:
		!n
	}
	
	@Unroll({ "It should${found ? ' ' : ' not'} find node for ${ex}" })
	def "Find exception handler"(){
		when:
		NodeDescriptor r = tds.find(ex)
		
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

	protected abstract NodeDatastore createNodeDatastore(NodeDescriptor... transitions);
	
	
}
