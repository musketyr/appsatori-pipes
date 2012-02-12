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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DataTypeUtils;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;


class DatastorePipeDatastore implements PipeDatastore {
	
	private static final String FINISHED = "finished";
	private static final Long ZERO = Long.valueOf(0);
	private static final String RESULT = "result";
	private static final String ACTIVE = "active";
	private static final String COUNT = "count";
	private static final String TOTAL_COUNT = "total_count";
	private static final String SERIALIZED = "serialized";
	private static final String NUMERIC_TYPE = "numeric";
	private static final int FLOAT = 0;
	private static final int SHORT = 1;
	private static final int INTEGER = 2;
	private static final int BYTE = 3;
	
	
	DatastorePipeDatastore(){}
	
	public boolean isActive(final String taskId) {
		return DatastoreHelper.call(new DatastoreHelper.Operation<Boolean>(){
			public Boolean run(DatastoreService ds) {
				try {
					Boolean active = (Boolean) ds.get(DatastoreHelper.getKey(taskId)).getProperty(ACTIVE);
					if(active == null){
						return Boolean.TRUE;
					}
					return active;
				} catch (EntityNotFoundException e) {
					return Boolean.FALSE;
				}
			}
		}, Boolean.FALSE);
	}
	
	public boolean setActive(final String taskId, final boolean active) {
		return DatastoreHelper.call(new DatastoreHelper.Operation<Boolean>(){
			public Boolean run(DatastoreService ds) {
				try {
					Entity entity = ds.get(DatastoreHelper.getKey(taskId));
					entity.setUnindexedProperty(ACTIVE, active);
					ds.put(entity);
					return Boolean.TRUE;
				} catch (EntityNotFoundException e) {
					return Boolean.FALSE;
				}
			}
		}, Boolean.FALSE);
	}

	public boolean logTaskStarted(final String taskId, final int taskCount) {
		return DatastoreHelper.call(new DatastoreHelper.Operation<Boolean>(){
			public Boolean run(DatastoreService ds) {
				try {
					ds.get(DatastoreHelper.getKey(taskId));
					return Boolean.FALSE;
				} catch (EntityNotFoundException e){
					Entity task = new Entity(DatastoreHelper.TASK_KIND, taskId);
					task.setUnindexedProperty(TOTAL_COUNT, (long) taskCount);
					Key taskKey = ds.put(task);
					for(int i = 0; i < taskCount; i++){
						Entity subtask = new Entity(taskKey.getChild(DatastoreHelper.SUBTASK_KIND, i + 1));
						ds.put(subtask);
					}
					return Boolean.TRUE;
				}
			}
		}, Boolean.FALSE);
	}
	
	public int getParallelTaskCount(final String taskId) {
		return DatastoreHelper.call(new DatastoreHelper.Operation<Integer>(){
			public Integer run(DatastoreService ds) {
				try {
					Entity task = ds.get(DatastoreHelper.getKey(taskId));
					Long total = (Long) task.getProperty(TOTAL_COUNT);
					if(total == null){
						return 0;
					}
					return total.intValue();
				} catch (EntityNotFoundException e){
					throw new IllegalArgumentException("Node " + taskId + " hasn't been logged!", e);
				}
			}
		}, -1);
	}
	
	public int logTaskFinished(final String taskId, final int index, final Object result) {
		return DatastoreHelper.call(new DatastoreHelper.Operation<Integer>(){
			
			public Integer run(DatastoreService ds) {
				if(result instanceof Text){
					throw new IllegalArgumentException("Text is not supported result!");
				}
				try {
					Entity task = ds.get(DatastoreHelper.getKey(taskId));
					Long total = (Long) task.getProperty(TOTAL_COUNT);
					Long count = (Long) task.getProperty(COUNT);
					
					if(total == null) {
						total = Long.valueOf(ZERO);
					}
					if(count == null){
						count = total;
					}
					
					if(index >= total.intValue()){
						throw new IndexOutOfBoundsException("There are only " + total + " tasks expected ! You requested index " + index);
					}
					
					Entity subtask = ds.get(DatastoreHelper.getKey(taskId, index));
					
					
					if(Boolean.TRUE.equals(subtask.getProperty(FINISHED))){
						throw new IllegalStateException("Node with index " + index + " has already finished!");
					}
					subtask.setUnindexedProperty(FINISHED, Boolean.TRUE);
					if(result == null){
						subtask.setUnindexedProperty(RESULT, result);
					} else if(DataTypeUtils.isSupportedType(result.getClass())){
						if(Float.class.isAssignableFrom(result.getClass())){
							subtask.setUnindexedProperty(NUMERIC_TYPE, FLOAT);
						} else if(Integer.class.isAssignableFrom(result.getClass())){
							subtask.setUnindexedProperty(NUMERIC_TYPE, INTEGER);
						} else if(Byte.class.isAssignableFrom(result.getClass())){
							subtask.setUnindexedProperty(NUMERIC_TYPE, BYTE);
						} else if(Short.class.isAssignableFrom(result.getClass())){
							subtask.setUnindexedProperty(NUMERIC_TYPE, SHORT);
						}
						subtask.setUnindexedProperty(RESULT, result);
					} else if(Serializable.class.isAssignableFrom(result.getClass())){
						subtask.setUnindexedProperty(SERIALIZED, true);
						subtask.setUnindexedProperty(RESULT, serialize(result));
					}
					try {
						ds.put(subtask);
					} catch (IllegalArgumentException e){
						throw new IllegalArgumentException("Result type is not supported by this flow datastore!", e);
					}
					
					if(count == null || count.intValue() == 0){
						return 0;
					}
					
					count = count - 1;
					task.setUnindexedProperty(COUNT, count);
					
					ds.put(task);		
					return count.intValue();
				} catch (EntityNotFoundException e){
					throw new IllegalArgumentException("Node " + taskId + " hasn't been logged!", e);
				}
			}
			
		}, -1);
	}
	
	private Blob serialize(Object obj){
		try {
			ObjectOutputStream oos = null;
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				oos = new ObjectOutputStream(bos);
				oos.writeObject(obj);
				return new Blob(bos.toByteArray());
			} finally {
				if(oos != null){
					oos.close();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Exception reading object from datastore", e);
		}
	}

	public List<Object> getTaskResults(final String taskId) {
		return DatastoreHelper.call(new DatastoreHelper.Operation<List<Object>>(){
			
			public List<Object> run(DatastoreService ds) {
				try {
					Entity task = ds.get(DatastoreHelper.getKey(taskId));
					Long count = (Long) task.getProperty(COUNT);
					if(count == null || (count != null && count.intValue() != 0)){
						throw new IllegalStateException("All tasks haven't finished yet!");
					}
					
					Long total = (Long) task.getProperty(TOTAL_COUNT);
					
					if(total == null){
						throw new IllegalStateException("Total is null but should be greater than zero!");
					}
					List<Object> ret = new ArrayList<Object>(total.intValue());
					for (int i = 0; i < total.intValue(); i++) {
						Entity subtask = ds.get(DatastoreHelper.getKey(taskId, i));
						Object result = subtask.getProperty(RESULT);
						if(subtask.hasProperty(SERIALIZED) && result instanceof Blob){
							result = deserialize((Blob)result);
						} else if(Text.class.isAssignableFrom(result.getClass())){
							result = ((Text)result).getValue();
						}
						Object numTypeObject = (Long) subtask.getProperty(NUMERIC_TYPE);
						if(numTypeObject != null){
							Long numType = (Long) numTypeObject;
							switch (numType.intValue()) {
							case BYTE:
								result = Byte.valueOf(((Number)result).byteValue());
								break;
							case SHORT:
								result = Short.valueOf(((Number)result).shortValue());
								break;
							case INTEGER:
								result = Integer.valueOf(((Number)result).intValue());
								break;
							case FLOAT:
								result = Float.valueOf(((Number)result).floatValue());
								break;
							}
						}
						
						ret.add(result);
					}
					return Collections.unmodifiableList(ret);
				} catch (EntityNotFoundException e){
					throw new IllegalArgumentException("Node " + taskId + " hasn't been logged!", e);
				}
			}
			
		}, Collections.emptyList());
	}
	
	private Object deserialize(Blob blob){
		try {
			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(new ByteArrayInputStream(blob.getBytes()));
				return ois.readObject();
			} finally {
				if(ois != null){
					ois.close();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Exception reading object from datastore", e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Exception reading object from datastore", e);
		}
	}
	
	public boolean clearTaskLog(String taskId){
		return clearTaskLog(taskId, false);
	}

	public boolean clearTaskLog(final String taskId, final boolean force) {
		return DatastoreHelper.call(new DatastoreHelper.Operation<Boolean>(){
			public Boolean run(DatastoreService ds) {
				try {
					Entity task = ds.get(DatastoreHelper.getKey(taskId));
					Long count = (Long) task.getProperty(COUNT);
					if(!force && count != null && count.intValue() != 0){
						throw new IllegalStateException("All tasks haven't finished yet!");
					}
					
					Long total = (Long) task.getProperty(TOTAL_COUNT);
					
					if(total == null){
						throw new IllegalStateException("Total is null but should be greater than zero!");
					}
					
					for (int i = 0; i < total.intValue(); i++) {
						ds.delete(DatastoreHelper.getKey(taskId, i));
					}
					ds.delete(task.getKey());
					return true;
				} catch (EntityNotFoundException e){
					return false;
				}
			}
		}, Boolean.FALSE);
	}

}
