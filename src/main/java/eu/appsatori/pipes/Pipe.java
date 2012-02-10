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

public enum Pipe {
	INSTANCE;
	
	public static Pipe getPipe(){
		return Pipe.INSTANCE;
	}
	
	public final NodeResult next(String state){
		return next(state, null);
	}
	
	public final NodeResult next(String next, Object result){
		return NodeResult.create(next, result);
	}
	
	public final NodeResult start(String state){
		return start(state, null);
	}
	
	public final NodeResult start(String next, Object result){
		Pipes.start(next, result);
		return NodeResult.create(next, result);
	}
	
	public final NodeResult finish(){
		return NodeResult.END_RESULT;
	}
	
}
