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
import eu.appsatori.pipes.stubs.StubTask2;
import eu.appsatori.pipes.stubs.StubTask3;
import eu.appsatori.pipes.stubs.StubTask4;
import eu.appsatori.pipes.NodeDescriptor;
import eu.appsatori.pipes.NodeDatastore;
import eu.appsatori.pipes.NodeType;
import spock.lang.Specification;
import spock.lang.Unroll;

abstract class NodeDatastoreSpec extends Specification{
	
	NodeDatastore tds = createNodeDatastore(
		serial('one',StubTask1),
		parallel('two',StubTask2),
		handler(RuntimeException,StubTask1),
		handler(IllegalArgumentException,StubTask2)
	)
	
	def "Add node"(){
		expect:
		tds.add(serial('three',StubTask1))
		tds.find('three')
	}
	
	def "Find transition"(){
		NodeDescriptor n = tds.find('one')
		
		expect:
		n.name == 'one'
		n.nodeType == NodeType.SERIAL
		n.node == StubTask1
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
