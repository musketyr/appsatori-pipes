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

import com.google.appengine.api.taskqueue.DeferredTask;

/**
 * Parallel pipe controls the flow of the task executed in parallel.
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
	 * the fastest task will proceed to its following node.
	 * The pipe will pause until all tasks 
	 * @param next Class of the next node. All parallel tasks must return the same one.
	 * @param result Argument for the next node. It is not saved to the datastore but serialized as a part of the {@link DeferredTask}
	 * @return {@link NodeResult} signaling that only first task will proceed to the following node
	 */
	public abstract <R, N extends Node<SerialPipe, ? super R>> NodeResult sprint(
			Class<N> next, R result);

	public abstract <R, N extends Node<ParallelPipe, ? super R>> NodeResult next(
			Class<N> next, R result);

	public abstract <E, R extends Collection<? super E>, N extends Node<SerialPipe, R>> NodeResult join(
			Class<N> next, E result);

}