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

public abstract class NodeBase {
	
	protected static enum ExecutionResult {
		OK;
	}
	
	private String nextState = null;
	private Object result = null;
	
	public abstract ExecutionResult execute(Object arg) throws Exception;
	
	protected final ExecutionResult next(String state){
		return next(state, null);
	}
	
	protected final ExecutionResult next(String state, Object result){
		this.nextState = state;
		this.result = result;
		return ExecutionResult.OK;
	}
	
	protected final ExecutionResult start(String state){
		return start(state, null);
	}
	
	protected final ExecutionResult start(String state, Object result){
		Pipes.getPipes().start(state, result);
		return ExecutionResult.OK;
	}
	
	protected final ExecutionResult finish(){
		return ExecutionResult.OK;
	}
	
	
	public Object getResult() {
		return result;
	}
	
	public String getNextNode() {
		return nextState;
	}
	
}
