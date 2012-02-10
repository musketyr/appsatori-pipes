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

package eu.appsatori.gaeflow;

public abstract class Task<A,R> {
	
	protected static enum ExecutionResult {
		INSTANCE;
	}
	
	private String nextState = null;
	private R result = null;
	
	public abstract ExecutionResult execute(A arg) throws Exception;
	
	protected final ExecutionResult next(String state){
		return next(state, null);
	}
	
	protected final ExecutionResult next(String state, R result){
		this.nextState = state;
		this.result = result;
		return ExecutionResult.INSTANCE;
	}
	
	protected final ExecutionResult start(String state){
		return start(state, null);
	}
	
	protected final ExecutionResult start(String state, R result){
		Flow.getFlow().start(state, result);
		return ExecutionResult.INSTANCE;
	}
	
	protected final ExecutionResult finish(){
		return ExecutionResult.INSTANCE;
	}
	
	
	public R getResult() {
		return result;
	}
	
	public String getNextNode() {
		return nextState;
	}
	
}
