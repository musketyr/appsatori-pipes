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
import java.util.ConcurrentModificationException;
import java.util.List;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DataTypeUtils;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.ImplicitTransactionManagementPolicy;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.ReadPolicy;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;


class DatastorePipeDatastore implements PipeDatastore {
	
	private static final String FINISHED = "finished";
	private static final Long ZERO = Long.valueOf(0);
	private static final String RESULT = "result";
	private static final String ACTIVE = "active";
	private static final String COUNT = "count";
	private static final String TOTAL_COUNT = "total_count";
	private static final String FLOW_NAMESPACE = "eu_appsatori_gaeflow";
	private static final String TASK_KIND = "task";
	private static final String SUBTASK_KIND = "subtask";
	private static final String SERIALIZED = "serialized";
	private static final String NUMERIC_TYPE = "numeric";
	private static final int FLOAT = 0;
	private static final int SHORT = 1;
	private static final int INTEGER = 2;
	private static final int BYTE = 3;
	
	private static final int RETRIES = 10;
	
	private final transient DatastoreService ds;
	
	public DatastorePipeDatastore(){
		DatastoreServiceConfig config = DatastoreServiceConfig.Builder.withImplicitTransactionManagementPolicy(ImplicitTransactionManagementPolicy.AUTO).readPolicy(new ReadPolicy(ReadPolicy.Consistency.STRONG));
		ds = DatastoreServiceFactory.getDatastoreService(config);
	}
	
	public boolean isActive(String taskId) {
		int attempt = 1;
		while(attempt <= RETRIES){
			try {
				return isActiveInternal(taskId);
			} catch (ConcurrentModificationException e){
				attempt++;
			}
		}
		return false;
	}
	
	private boolean isActiveInternal(String taskId){
		Transaction tx = ds.beginTransaction();
		try {
			try {
				Boolean active = (Boolean) get(getKey(taskId)).getProperty(ACTIVE);
				if(active == null){
					return false;
				}
				return active ;
			} catch (EntityNotFoundException e){
				return false;
			}
		} finally {
			tx.commit();
		}
	}
	
	public boolean setActive(String taskId, boolean active) {
		int attempt = 1;
		while(attempt <= RETRIES){
			try {
				return setActiveInternal(taskId, active);
			} catch (ConcurrentModificationException e){
				attempt++;
			}
		}
		return false;
	}
	
	private boolean setActiveInternal(String taskId, boolean active){
		Transaction tx = ds.beginTransaction();
		try {
			try {
				Entity entity = get(getKey(taskId));
				entity.setUnindexedProperty(ACTIVE, active);
				put(entity);
				return true;
			} catch (EntityNotFoundException e){
				return false;
			}
		} finally {
			tx.commit();
		}
	}

	public boolean logTaskStarted(String taskId, int taskCount) {
		int attempt = 1;
		while(attempt <= RETRIES){
			try {
				return logTaskStartedInternal(taskId, taskCount);
			} catch (ConcurrentModificationException e){
				attempt++;
			}
		}
		return false;
	}
	
	private boolean logTaskStartedInternal(String taskId, int taskCount) {
		Transaction tx = ds.beginTransaction();
		try {
			try {
				get(getKey(taskId));
				return false;
			} catch (EntityNotFoundException e){
				Entity task = new Entity(TASK_KIND, taskId);
				task.setUnindexedProperty(COUNT, (long) taskCount);
				task.setUnindexedProperty(TOTAL_COUNT, (long) taskCount);
				task.setUnindexedProperty(ACTIVE, true);
				Key taskKey = put(task);
				for(int i = 0; i < taskCount; i++){
					Entity subtask = new Entity(taskKey.getChild(SUBTASK_KIND, i + 1));
					subtask.setUnindexedProperty(FINISHED, false);
					subtask.setUnindexedProperty(RESULT, null);
					put(subtask);
				}
				return true;
			}
		} finally {
			tx.commit();
		}
	}
	
	public int getParallelTaskCount(String taskId) {
		int attempt = 1;
		while(attempt <= RETRIES){
			try {
				return getParallelTaskCountInternal(taskId);
			} catch (ConcurrentModificationException e){
				attempt++;
			}
		}
		throw new ConcurrentModificationException("Cannot get parallel task count even after " + RETRIES + " retries!");
	}

	private int getParallelTaskCountInternal(String taskId) {
		try {
			Transaction tx = ds.beginTransaction();
			try {
				Entity task = get(getKey(taskId));
				Long total = (Long) task.getProperty(TOTAL_COUNT);
				return total.intValue();
			} finally {
				tx.commit();
			}
		} catch (EntityNotFoundException e){
			throw new IllegalArgumentException("Node " + taskId + " hasn't been logged!", e);
		}
	}
	
	public int logTaskFinished(String taskId, int index, Object results) {
		int attempt = 1;
		while(attempt <= RETRIES){
			try {
				return logTaskFinishedInternal(taskId, index, results);
			} catch (ConcurrentModificationException e){
				attempt++;
			}
		}
		throw new ConcurrentModificationException("Cannot log task finished even after " + RETRIES + " retries!");
	}


	private int logTaskFinishedInternal(String taskId, int index, Object result) {
		try {
			Transaction tx = ds.beginTransaction();
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
			} finally {
				tx.commit();
			}
		} catch (EntityNotFoundException e){
			throw new IllegalArgumentException("Node " + taskId + " hasn't been logged!", e);
		}
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

	public List<Object> getTaskResults(String taskId) {
		int attempt = 1;
		while(attempt <= RETRIES){
			try {
				return getTaskResultsInternal(taskId);
			} catch (ConcurrentModificationException e){
				attempt++;
			}
		}
		throw new ConcurrentModificationException("Cannot get task results even after " + RETRIES + " retries!");
	}
	
	private List<Object> getTaskResultsInternal(String taskId) {
		try {
			Transaction tx = ds.beginTransaction();
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
			} finally {
				tx.commit();
			}
		} catch (EntityNotFoundException e){
			throw new IllegalArgumentException("Node " + taskId + " hasn't been logged!", e);
		}
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

	public boolean clearTaskLog(String taskId, boolean force) {
		int attempt = 1;
		while(attempt <= RETRIES){
			try {
				return clearTaskLogIntrenal(taskId, force);
			} catch (ConcurrentModificationException e){
				attempt++;
			}
		}
		return false;
	}
	
	private  boolean clearTaskLogIntrenal(String taskId, boolean force) {
		try {
			Entity task = get(getKey(taskId));
			Long count = (Long) task.getProperty(COUNT);
			if(!force && count != null && count.intValue() != 0){
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

	private static Key getKey(String taskId) {
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
