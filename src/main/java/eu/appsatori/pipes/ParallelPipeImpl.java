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

enum ParallelPipeImpl implements ParallelPipe {
	INSTANCE;
	
	/* (non-Javadoc)
	 * @see eu.appsatori.pipes.ParalellPipe#sprint(java.lang.Class, R)
	 */
	public <R, N extends Node<SerialPipe, ? super R>> NodeResult sprint(Class<N> next, R result){
		return NodeResult.create(PipeType.COMPETETIVE, next, result);
	}
	
	/* (non-Javadoc)
	 * @see eu.appsatori.pipes.ParalellPipe#next(java.lang.Class, R)
	 */
	public <R, N extends Node<ParallelPipe, ? super R>> NodeResult next(Class<N> next, R result){
		return NodeResult.create(PipeType.PARALLEL, next, result);
	}
	
	/* (non-Javadoc)
	 * @see eu.appsatori.pipes.ParalellPipe#join(java.lang.Class, E)
	 */
	public <E, R extends Collection<? super E>, N extends Node<SerialPipe, R>> NodeResult join(Class<N> next, E result){
		return NodeResult.create(PipeType.SERIAL, next, result);
	}
	
	public final NodeResult finish(){
		return NodeResult.END_RESULT;
	}
	
}
