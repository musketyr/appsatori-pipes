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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;



/**
 * Internal representation of type of pipe.
 * 
 * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
 *
 */
enum PipeType {
	SERIAL {
		public int getParallelTasksCount(Object arg) {
			return 1;
		}

		@SuppressWarnings("unchecked")
		public <P extends Pipe, A, N extends Node<P,A>> NodeResult execute(N taskInstance, Object arg, int index) {
			return taskInstance.execute((P)SerialPipeImpl.INSTANCE, (A) arg);
		}
		
		public boolean handlePipeEnd(NodeRunner runner, String queue, String baseTaskId, int index, Object result){
			return runner.getPipeDatastore().clearTaskLog(baseTaskId, true);
		}

		public <N extends Node<?,?>> void handleNext(NodeRunner runner, String queue, String baseTaskId, int index, NodeResult result) {
			runner.run(result.getType(), result.getNext(), result.getResult());
			runner.getPipeDatastore().clearTaskLog(baseTaskId, true);
		}
	}, 
	PARALLEL,
	COMPETETIVE {
		@SuppressWarnings("unchecked")
		public <P extends Pipe, A, N extends Node<P,A>> NodeResult execute(N taskInstance, Object arg, int index){
			return taskInstance.execute((P)SerialPipeImpl.INSTANCE, (A) getAt(index, arg));
		}
		
		public boolean handlePipeEnd(NodeRunner runner, String queue, String baseTaskId, int index, Object result) {
			try {
				if(!runner.getPipeDatastore().isActive(baseTaskId)){
					return false;
				}
				if(!runner.getPipeDatastore().setActive(baseTaskId, false)){
					return false;
				}
				runner.clearTasks(queue, baseTaskId, runner.getPipeDatastore().getParallelTaskCount(baseTaskId));
				return runner.getPipeDatastore().clearTaskLog(baseTaskId, true);
			} catch(IllegalArgumentException e){
				return false;
			}
			
		}
		
		public <N extends Node<?,?>> void handleNext(NodeRunner runner, String queue, String baseTaskId, int index, NodeResult result) {
			if(handlePipeEnd(runner, queue, baseTaskId, index, result)){
				runner.run(result.getType(), result.getNext(), result.getResult());
			}
		}

	},
	FAIL_HANDLER {
		@Override
		public boolean handlePipeEnd(NodeRunner runner, String queue, String baseTaskId, int index, Object result) {
			return COMPETETIVE.handlePipeEnd(runner, queue, baseTaskId, index, result);
		}
		
		public <P extends Pipe, A, N extends Node<P,A>> NodeResult execute(N taskInstance, Object arg, int index) {
			return SERIAL.execute(taskInstance, arg, index);
		};
		
		public <N extends Node<?,?>> void handleNext(NodeRunner runner, String queue, String baseTaskId, int index, NodeResult result) {
			COMPETETIVE.handleNext(runner, queue, baseTaskId, index, result);
		}
	}, STREAMING{
		@Override
		public int getParallelTasksCount(Object arg) {
			return SERIAL.getParallelTasksCount(arg);
		}
		
		@SuppressWarnings("unchecked")
		public <P extends Pipe, A, N extends Node<P,A>> NodeResult execute(N taskInstance, Object arg, int index) {
			return taskInstance.execute((P)new StreamingPipeImpl(Pipes.getUniqueTaskId(taskInstance.getClass().getName())), (A)arg);
		}
	};
	
	public int getParallelTasksCount(Object arg) {
		return sizeOf(arg);
	}
	
	@SuppressWarnings("unchecked")
	public <P extends Pipe, A, N extends Node<P,A>> NodeResult execute(N taskInstance, Object arg, int index){
		return taskInstance.execute((P)ParallelPipeImpl.INSTANCE, (A) getAt(index, arg));
	}
	
	public boolean handlePipeEnd(NodeRunner runner, String queue, String baseTaskId, int index, Object result) {
		PipeDatastore pds = runner.getPipeDatastore();
		if(0 == pds.logTaskFinished(baseTaskId, index, result)){
			return pds.clearTaskLog(baseTaskId, true);
		}
		return true;
	}
	
	public <N extends Node<?,?>> void handleNext(NodeRunner runner, String queue, String baseTaskId, int index, NodeResult result) {
		PipeDatastore fds = runner.getPipeDatastore();
		int remaining = fds.logTaskFinished(baseTaskId, index, result.getResult());
		if(remaining > 0){
			return;
		}
		runner.run(result.getType(), result.getNext(), fds.getTaskResults(baseTaskId));
		fds.clearTaskLog(baseTaskId);
	}
	
	static int sizeOf(Object arg){
		if(arg == null){
			return 1;
		}
		if(arg.getClass().isArray()){
			return Array.getLength(arg);
		}
		if (!(arg instanceof Collection<?>)) {
			return 1;
		}
		Collection<?> col = (Collection<?>) arg;
		return col.size();
	}
	
	static Object getAt(int index, Object arg) {
		if(arg == null){
			return null;
		}
		if(arg.getClass().isArray()){
			return Array.get(arg, index);
		}
		if(List.class.isAssignableFrom(arg.getClass())){
			return List.class.cast(arg).get(index);
		}
		if(Iterable.class.isAssignableFrom(arg.getClass())){
			int i = 0;
			@SuppressWarnings("rawtypes")
			Iterator it = ((Iterable)arg).iterator();
			while (it.hasNext()) {
				Object next = (Object) it.next();
				if(i == index){
					return next;
				}
			}
		}
		return arg;
		
	}

}
