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
 * Internal implementation of {@link SerialPipe}.
 * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
 *
 */
enum SerialPipeImpl implements Pipe, SerialPipe{
	INSTANCE;
	
	/* (non-Javadoc)
	 * @see eu.appsatori.pipes.SerialPipe#spread(java.lang.Class, java.util.Collection)
	 */
	public <E, R extends Collection<E>, N extends Node<SerialPipe,? super E>> NodeResult spread(Class<N> next, R result) {
		Pipes.spread(next, result);
		return NodeResult.END_RESULT;
	};
	
	/* (non-Javadoc)
	 * @see eu.appsatori.pipes.SerialPipe#run(java.lang.Class)
	 */
	public <R, N extends Node<SerialPipe, R>> NodeResult run(Class<N> state){
		return run(state, null);
	}
	
	/* (non-Javadoc)
	 * @see eu.appsatori.pipes.SerialPipe#run(java.lang.Class, java.lang.Object)
	 */
	public <R, N extends Node<SerialPipe, ? super R>> NodeResult run(Class<N> next, R result){
		return NodeResult.create(PipeType.SERIAL, next, result);
	}
	
	/* (non-Javadoc)
	 * @see eu.appsatori.pipes.SerialPipe#sprint(java.lang.Class, java.util.Collection)
	 */
	public <E, R extends Collection<E>, N extends Node<SerialPipe, ? super E>> NodeResult sprint(Class<N> next, R result){
		return NodeResult.create(PipeType.COMPETETIVE, next, result);
	}
	
	/* (non-Javadoc)
	 * @see eu.appsatori.pipes.SerialPipe#fork(java.lang.Class, java.util.Collection)
	 */
	public <E, R extends Collection<E>, N extends Node<ParallelPipe, ? super E>> NodeResult fork(Class<N> next, R result){
		return NodeResult.create(PipeType.PARALLEL, next, result);
	}
	
	/* (non-Javadoc)
	 * @see eu.appsatori.pipes.Pipe#finish()
	 */
	public final NodeResult finish(){
		return NodeResult.END_RESULT;
	}
	
}
