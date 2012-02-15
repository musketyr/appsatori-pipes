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

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;



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
		
		public void handlePipeEnd(String queue, String baseTaskId, int index, Object result){
			Pipes.getPipeDatastore().clearTaskLog(baseTaskId, true);
		}

		public <N extends Node<?,?>> void handleNext(String queue, String baseTaskId, int index, NodeResult result) {
			Pipes.start(result.getType(), result.getNext(), result.getResult());
			Pipes.getPipeDatastore().clearTaskLog(baseTaskId, true);
		}
	}, 
	PARALLEL,
	COMPETETIVE {
		@SuppressWarnings("unchecked")
		public <P extends Pipe, A, N extends Node<P,A>> NodeResult execute(N taskInstance, Object arg, int index){
			return taskInstance.execute((P)SerialPipeImpl.INSTANCE, (A) getAt(index, arg));
		}
		
		public void handlePipeEnd(String queue, String baseTaskId, int index, Object result) {
			try {
				Pipes.getPipeDatastore().setActive(baseTaskId, false);
				clean(queue, baseTaskId, Pipes.getPipeDatastore().getParallelTaskCount(baseTaskId));
				Pipes.getPipeDatastore().clearTaskLog(baseTaskId, true);
			} catch(IllegalArgumentException e){
				// already deleted
			}
			
		}
		
		public <N extends Node<?,?>> void handleNext(String queue, String baseTaskId, int index, NodeResult result) {
			handlePipeEnd(queue, baseTaskId, index, result);
			Pipes.start(result.getType(), result.getNext(), result.getResult());
		}

	};
	
	public int getParallelTasksCount(Object arg) {
		return sizeOf(arg);
	}
	
	@SuppressWarnings("unchecked")
	public <P extends Pipe, A, N extends Node<P,A>> NodeResult execute(N taskInstance, Object arg, int index){
		return taskInstance.execute((P)ParallelPipeImpl.INSTANCE, (A) getAt(index, arg));
	}
	
	public void handlePipeEnd(String queue, String baseTaskId, int index, Object result) {
		PipeDatastore pds = Pipes.getPipeDatastore();
		if(0 == pds.logTaskFinished(baseTaskId, index, result)){
			pds.clearTaskLog(baseTaskId, true);
		}
	}
	
	public <N extends Node<?,?>> void handleNext(String queue, String baseTaskId, int index, NodeResult result) {
		PipeDatastore fds = Pipes.getPipeDatastore();
		int remaining = fds.logTaskFinished(baseTaskId, index, result.getResult());
		if(remaining > 0){
			return;
		}
		Pipes.start(result.getType(), result.getNext(), fds.getTaskResults(baseTaskId));
		fds.clearTaskLog(baseTaskId);
	}
	
	protected static int sizeOf(Object arg){
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
	
	protected static Object getAt(int index, Object arg) {
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
	
	protected static <A, R> void clean(String queue, String baseTaskId, int tasksCount){
		Queue q;
		if("".equals(queue) || queue == null){
			q = QueueFactory.getDefaultQueue();
		} else {
			q = QueueFactory.getQueue(queue);
		}
		for (int i = 0; i < tasksCount; i++) {
			String taskName = "" + i + "_" + baseTaskId;
			try {
				q.deleteTask(taskName);
			} catch (IllegalStateException e){
				QueueFactory.getDefaultQueue().deleteTask(taskName);
			}
		}
	}

}
