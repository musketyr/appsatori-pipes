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

public enum Pipe {
	INSTANCE;
	
	public static Pipe getPipe(){
		return Pipe.INSTANCE;
	}
	
	public <R, N extends Node<R>> NodeResult<N> next(Class<N> state){
		return next(state, null);
	}
	
	public <R, N extends Node<R>> NodeResult<N> next(Class<N> next, R result){
		return NodeResult.create(NodeType.SERIAL, next, result);
	}
	
	public <E, R extends Collection<E>, N extends Node<R>> NodeResult<N> fork(Class<N> next, R result){
		return NodeResult.create(NodeType.PARALLEL, next, result);
	}
	
	public <E, R extends Collection<E>, N extends Node<R>> NodeResult<N> join(Class<N> next, E result){
		return NodeResult.create(NodeType.PARALLEL, next, result);
	}
	
	public <R, N extends Node<R>> String start(Class<N> state){
		return start(state, null);
	}
	
	public <R, N extends Node<R>> String start(Class<N> next, R result){
		return Pipes.start(NodeType.SERIAL, next, result);
	}
	
	@SuppressWarnings("unchecked")
	public final <R, N extends Node<R>> NodeResult<N> finish(){
		return NodeResult.END_RESULT;
	}
	
}
