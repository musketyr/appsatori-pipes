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

package eu.appsatori.pipes;

import java.util.Collection;

/**
 * Serial pipe controls the flow of the node executed in serial (one by one).
 * 
 * All the parallel tasks must return {@link NodeResult} with the same following node
 * and node type. This means that only one of the methods {@link #sprint(Class, Object)},
 * {@link #next(Class, Object)}, {@link #join(Class, Object)} must be called in particular
 * {@link Node}.Otherwise the pipe flow is unpredictable.
 * 
 * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
 *
 */
public interface SerialPipe extends Pipe {

	/**
	 * Runs the next node in serial.
	 * @param next type of the next node to be run in serial
	 * @return {@link NodeResult} signaling that next node should be also executed in serial right after this task finish
	 */
	<R, N extends Node<SerialPipe, R>> NodeResult run(Class<N> next);

	/**
	 * Sends the results to the next node which will be executed independently (in serial manner).
	 * The next node must use {@link SerialPipe} because it will obtain the argument directly.
	 * @param next type of the next node
	 * @param result argument for the next node
	 * @return {@link NodeResult} signaling that following nodes should be run independently
	 */
	<E, R extends Collection<E>, N extends Node<SerialPipe, ? super E>> NodeResult spread(Class<N> next, R result);

	/**
	 * Sends the result directly to the next node which will be run in serial.
	 * The next node must use {@link SerialPipe} because it will obtain the argument directly.
	 * @param next the next node to be run in serial
	 * @param result argument for the next node
	 * @return {@link NodeResult} signaling that next node should be also executed in serial right after this task finish
	 */
	<R, N extends Node<SerialPipe, ? super R>> NodeResult run(Class<N> next,
			R result);

	/**
	 * Distribute the results to the next tasks. 
	 * Each element of the results collection will be handled by its own node. Only the fastest node will proceed
	 * to its next node which will be executed in serial.
	 * The next node must use {@link SerialPipe} because it will obtain the argument directly.
	 * @param next the type of next node to be run in parallel
	 * @param result collection of parameters for the next task
	 * @return {@link NodeResult} signaling that next node should be in parallel but only first task will proceed to the following node
	 */
	<E, R extends Collection<E>, N extends Node<SerialPipe, ? super E>> NodeResult sprint(Class<N> next, R result);

	/**
	 * Distribute the results to the next tasks. 
	 * Each element of the results collection will be handled by its own node. 
	 * All tasks are supposed to proceed to its next node or to finish. There is several options
	 * how to finish this parallel execution. You can find them in {@link ParallelPipe} interface.
	 * 
	 * @param next the type of next node to be run in parallel
	 * @param result collection of parameters for the next task
	 * @return {@link NodeResult} signaling that next node should be executed in parallel
	 */
	<E, R extends Collection<E>, N extends Node<ParallelPipe, ? super E>> NodeResult fork(Class<N> next, R result);

}