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
 * Parallel pipe controls the flow of the node executed in parallel.
 * 
 * All the parallel tasks must return {@link NodeResult} with the same following node
 * and node type. This means that only one of the methods {@link #sprint(Class, Object)},
 * {@link #next(Class, Object)}, {@link #join(Class, Object)} must be called in particular
 * {@link Node}.Otherwise the pipe flow is unpredictable.
 * 
 * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
 *
 */
public interface ParallelPipe extends Pipe{

	/**
	 * Sends the results to the next node which will be executed in parallel but only
	 * the fastest task will proceed to its following node. The others waiting for executions
	 * are canceled and the result of already executed but not finished ones is ignored.
	 * The next node must use {@link SerialPipe} because it will obtain result from the fasted current node.
	 * @param next type of the next node. <strong>All nodes running in parallel must return the same next node!</strong>
	 * @param result argument for the next node
	 * @return {@link NodeResult} signaling that only first task will proceed to the following node
	 */
	<R, N extends Node<SerialPipe, ? super R>> NodeResult sprint(
			Class<N> next, R result);

	/**
	 * Sends the results to the next node which will be executed in parallel.
	 * @param next type of the next node. <strong>All nodes running in parallel must return the same next node!</strong>
	 * @param result argument for the next node
	 * @return {@link NodeResult} signaling that next node should be also executed in parallel
	 */
	<R, N extends Node<ParallelPipe, ? super R>> NodeResult next(
			Class<N> next, R result);

	/**
	 * Sends the results to the next node which will obtain the collection of the results of current parallel nodes execution.
	 * @param next type of the next node. <strong>All nodes running in parallel must return the same next node!</strong>
	 * @param result argument for the next node to be collected
	 * @return {@link NodeResult} signaling that next node should be executed in serial and obtain the collected results as the argument
	 */
	<E, R extends Collection<? super E>, N extends Node<SerialPipe, R>> NodeResult join(
			Class<N> next, E result);
	
	/**
	 * Sends the results to the next node which will be executed independently (in serial manner).
	 * The next node must use {@link SerialPipe} because it is not waiting for the results from its parallel companions.
	 * @param next type of the next node. <strong>All nodes running in parallel must return the same next node!</strong>
	 * @param result argument for the next node
	 * @return {@link NodeResult} signaling that nodes should run independently
	 */
	<R, N extends Node<SerialPipe, ? super R>> NodeResult spread(Class<N> next, R result);

}