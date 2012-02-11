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


enum NodeType {
	SERIAL, 
	PARALLEL {
		@Override
		public int getParallelTasksCount(Object arg) {
			return ObjectsToIterables.sizeOf(arg);
		}
		
		@Override
		public boolean isSerial() {
			return false;
		}
		
		@SuppressWarnings("unchecked")
		public <A, N extends Node<A>> NodeResult execute(N taskInstance, Object arg, int index) throws Exception {
			return taskInstance.execute(Pipe.INSTANCE, (A)ObjectsToIterables.getAt(index, arg));
		}
		
		@Override
		public void handlePipeEnd(String baseTaskId, int index, Object result) {
			PipeDatastore pds = Pipes.getPipeDatastore();
			if(0 == pds.logTaskFinished(baseTaskId, index, result)){
				pds.clearTaskLog(baseTaskId, true);
			}
		}
		
		@Override
		public <N extends Node<?>> void handleNext(String baseTaskId, int index, NodeResult result) {
			PipeDatastore fds = Pipes.getPipeDatastore();
			int remaining = fds.logTaskFinished(baseTaskId, index, result.getResult());
			if(remaining > 0){
				return;
			}
			Pipes.start(result.getType(), result.getNext(), fds.getTaskResults(baseTaskId));
			fds.clearTaskLog(baseTaskId);
		}
	};

	public int getParallelTasksCount(Object arg) {
		return 1;
	}
	
	public boolean isSerial(){
		return true;
	}

	@SuppressWarnings("unchecked")
	public <A, N extends Node<A>> NodeResult execute(N taskInstance, Object arg, int index) throws Exception {
		return taskInstance.execute(Pipe.INSTANCE, (A) arg);
	}
	
	public void handlePipeEnd(String baseTaskId, int index, Object result){}

	public <N extends Node<?>> void handleNext(String baseTaskId, int index, NodeResult result) {
		Pipes.start(result.getType(), result.getNext(), result.getResult());
		Pipes.getPipeDatastore().clearTaskLog(baseTaskId, true);
	}

}
