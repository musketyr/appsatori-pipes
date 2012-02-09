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

package eu.appsatori.gaeflow.ds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import eu.appsatori.gaeflow.FlowStateDatastore;

public class DatastoreFlowStateDatastore implements FlowStateDatastore {
	
	private static final String FINISHED = "finished";
	private static final Long ZERO = Long.valueOf(0);
	private static final String RESULT = "result";
	private static final String COUNT = "count";
	private static final String TOTAL_COUNT = "total_count";
	private static final String FLOW_NAMESPACE = "eu_appsatori_gaeflow";
	private static final String TASK_KIND = "task";
	private static final String SUBTASK_KIND = "subtask";
	
	private final transient DatastoreService ds;
	
	public DatastoreFlowStateDatastore(){
		ds = DatastoreServiceFactory.getDatastoreService();
	}
	

	public boolean logTaskStarted(String taskId, int taskCount) {
		try {
			get(getKey(taskId));
			return false;
		} catch (EntityNotFoundException e){
			Entity task = new Entity(TASK_KIND, taskId);
			task.setUnindexedProperty(COUNT, (long) taskCount);
			task.setUnindexedProperty(TOTAL_COUNT, (long) taskCount);
			Key taskKey = put(task);
			for(int i = 0; i < taskCount; i++){
				Entity subtask = new Entity(taskKey.getChild(SUBTASK_KIND, i + 1));
				subtask.setUnindexedProperty(FINISHED, false);
				subtask.setUnindexedProperty(RESULT, null);
				put(subtask);
			}
			return true;
		}
	}

	public int getParallelTaskCount(String taskId) {
			try {
				Entity task = get(getKey(taskId));
				Long total = (Long) task.getProperty(TOTAL_COUNT);
				return total.intValue();
			} catch (EntityNotFoundException e){
				throw new IllegalArgumentException("Task " + taskId + " hasn't been logged!", e);
			}
	}


	public int logTaskFinished(String taskId, int index, Object result) {
		try {
			Entity task = get(getKey(taskId));
			Long count = (Long) task.getProperty(COUNT);
			Long total = (Long) task.getProperty(TOTAL_COUNT);
			
			if(total == null) {
				total = Long.valueOf(ZERO);
			}
			
			if(index >= total.intValue()){
				throw new IndexOutOfBoundsException("There are only " + total + " tasks expected ! You requested index " + index);
			}
			
			Entity subtask = get(getKey(taskId, index));
			
			
			if(Boolean.TRUE.equals(subtask.getProperty(FINISHED))){
				throw new IllegalStateException("Task with index " + index + " has already finished!");
			}
			subtask.setUnindexedProperty(FINISHED, Boolean.TRUE);
			subtask.setUnindexedProperty(RESULT, result);
			try {
				put(subtask);
			} catch (IllegalArgumentException e){
				throw new IllegalArgumentException("Result type is not supported by this flow datastore!", e);
			}
			
			if(count == null || count.intValue() == 0){
				return 0;
			}
			
			count = count - 1;
			task.setUnindexedProperty(COUNT, count);
			
			put(task);
			return count.intValue();
		} catch (EntityNotFoundException e){
			throw new IllegalArgumentException("Task " + taskId + " hasn't been logged!", e);
		}
	}

	public List<Object> getTaskResults(String taskId) {
		try {
			Entity task = get(getKey(taskId));
			Long count = (Long) task.getProperty(COUNT);
			if(count != null && count.intValue() != 0){
				throw new IllegalStateException("All tasks haven't finished yet!");
			}
			
			Long total = (Long) task.getProperty(TOTAL_COUNT);
			
			if(total == null){
				throw new IllegalStateException("Total is null but should be greater than zero!");
			}
			List<Object> ret = new ArrayList<Object>(total.intValue());
			for (int i = 0; i < total.intValue(); i++) {
				Entity subtask = get(getKey(taskId, i));
				ret.add(subtask.getProperty(RESULT));
			}
			return Collections.unmodifiableList(ret);
		} catch (EntityNotFoundException e){
			throw new IllegalArgumentException("Task " + taskId + " hasn't been logged!", e);
		}
	}

	public boolean clearTaskLog(String taskId) {
		try {
			Entity task = get(getKey(taskId));
			Long count = (Long) task.getProperty(COUNT);
			if(count != null && count.intValue() != 0){
				throw new IllegalStateException("All tasks haven't finished yet!");
			}
			
			Long total = (Long) task.getProperty(TOTAL_COUNT);
			
			if(total == null){
				throw new IllegalStateException("Total is null but should be greater than zero!");
			}
			
			for (int i = 0; i < total.intValue(); i++) {
				delete(getKey(taskId, i));
			}
			delete(task.getKey());
			return true;
		} catch (EntityNotFoundException e){
			return false;
		}
	}

	private Key getKey(String taskId) {
		String old = NamespaceManager.get();
		NamespaceManager.set(FLOW_NAMESPACE);
		Key key = KeyFactory.createKey(TASK_KIND, taskId);
		NamespaceManager.set(old);
		return key;
	}
	
	private Key getKey(String taskId, int index) {
		String old = NamespaceManager.get();
		NamespaceManager.set(FLOW_NAMESPACE);
		Key key = KeyFactory.createKey(getKey(taskId), SUBTASK_KIND, index + 1);
		NamespaceManager.set(old);
		return key;
	}
	
	private Entity get(Key key) throws EntityNotFoundException{
		String old = NamespaceManager.get();
		NamespaceManager.set(FLOW_NAMESPACE);
		Entity en = ds.get(key);
		NamespaceManager.set(old);
		return en;
	}
	
	private Key put(Entity en){
		String old = NamespaceManager.get();
		NamespaceManager.set(FLOW_NAMESPACE);
		Key key = ds.put(en);
		NamespaceManager.set(old);
		return key;
	}
	
	private void delete(Key key){
		String old = NamespaceManager.get();
		NamespaceManager.set(FLOW_NAMESPACE);
		ds.delete(key);
		NamespaceManager.set(old);
	}
}
