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

package eu.appsatori.pipes.sample;

import java.util.Collection;
import java.util.logging.Logger;

import eu.appsatori.pipes.Node;
import eu.appsatori.pipes.NodeResult;
import eu.appsatori.pipes.SerialPipe;

public class JoinNode implements Node<SerialPipe,Collection<Long>> {

	private static final Logger log = Logger.getLogger(JoinNode.class.getName());
	
	public NodeResult execute(SerialPipe pipe, Collection<Long> param){
		log.info("Running join task. Got " + param);
		return pipe.sprint(WinNode.class, param);
	}
	
}
