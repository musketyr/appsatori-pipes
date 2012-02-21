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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DataTypeUtils;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.FinalizationException;
import com.google.appengine.api.files.LockException;
import com.google.apphosting.api.ApiProxy.RequestTooLargeException;


/**
 * Internal implementation of {@link PipeDatastore} based on App Engine datastore.
 * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
 *
 */
class DatastorePipeDatastore implements PipeDatastore {
	
	private static final String STASHED_ARG_MIME_TYPE = "application/x-appsatori-stashed-argument";
	private static final String FINISHED = "finished";
	private static final String RESULT = "result";
	private static final String ACTIVE = "active";
	private static final String COUNT = "count";
	private static final String TOTAL_COUNT = "total_count";
	private static final String SERIALIZED = "serialized";
	private static final String ALL_TASKS_STARTED = "all_started";
	private static final String NUMERIC_TYPE = "numeric";
	private static final int FLOAT = 0;
	private static final int SHORT = 1;
	private static final int INTEGER = 2;
	private static final int BYTE = 3;
	
	private static final String STASH_KIND = "stash";
	private static final String ARG = "arg";
	
	
	DatastorePipeDatastore(){}
	
	public Object retrieveArgument(final String path) {
		if(path.startsWith("stash:")){
			return DatastoreHelper.call(new DatastoreHelper.Operation<Object>() {
				public Object run(DatastoreService ds) {
					try {
						Entity stash = ds.get(KeyFactory.createKey(STASH_KIND, Long.valueOf(path.replace("stash:", ""))));
						Blob blob = (Blob) stash.getProperty(ARG);
						Object ret = deserialize(blob);
						ds.delete(stash.getKey());
						return ret;
					} catch (NumberFormatException e) {
						return null;
					} catch (EntityNotFoundException e) {
						return null;
					}
				}
			}, null);
		}
		final AppEngineFile[] helper = new AppEngineFile[1];
		Object ret =  DatastoreHelper.call(new DatastoreHelper.Operation<Object>() {
			public Object run(DatastoreService ds) {
				try {
					FileService fileService = FileServiceFactory.getFileService();
					AppEngineFile file = new AppEngineFile(path);
					
					FileReadChannel readChannel = fileService.openReadChannel(file, true);
					ObjectInputStream ois = new ObjectInputStream(Channels.newInputStream(readChannel));
					helper[0] = file;
					
					try {
						return ois.readObject();
					} finally {
						ois.close();
						readChannel.close();
					}
				} catch (FileNotFoundException e) {
					return null;
				} catch (LockException e) {
					return null;
				} catch (IOException e) {
					return null;
				} catch (ClassNotFoundException e) {
					return null;
				}
			}
		}, null);

		FileService fileService = FileServiceFactory.getFileService();
		BlobKey blobKey =  fileService.getBlobKey(helper[0]);
		BlobstoreService blobStoreService = BlobstoreServiceFactory.getBlobstoreService();
		blobStoreService.delete(blobKey);
		
		return ret;
	}
	
	public String stashArgument(final Object argument) {
		try {
			return "stash:" + DatastoreHelper.call(new DatastoreHelper.Operation<Long>() {
				public Long run(DatastoreService ds) {
					Entity stash = new Entity(STASH_KIND);
					stash.setUnindexedProperty(ARG, serialize(argument));
					return ds.put(stash).getId();
				}
			}, 0L);
		} catch(RequestTooLargeException e){
			return DatastoreHelper.call(new DatastoreHelper.Operation<String>() {
				public String run(DatastoreService ds) {
					try {
						FileService service = FileServiceFactory.getFileService();
						AppEngineFile file = service.createNewBlobFile(STASHED_ARG_MIME_TYPE);
						FileWriteChannel fwch = service.openWriteChannel(file, true);
						Channels.newOutputStream(fwch);
						ObjectOutputStream oos = new ObjectOutputStream(Channels.newOutputStream(fwch));
						try {
							oos.writeObject(argument);
							return file.getFullPath();
						} finally {
							oos.close();
							fwch.closeFinally();
						}
					} catch (FileNotFoundException e) {
						return "";
					} catch (FinalizationException e) {
						return "";
					} catch (LockException e) {
						return "";
					} catch (IllegalStateException e) {
						return "";
					} catch (IOException e) {
						return "";
					}
				}
			}, "");
		}
	}
	
	
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

	public int logTaskStarted(final String taskId) {
		return DatastoreHelper.call(new DatastoreHelper.Operation<Integer>(){
			public Integer run(DatastoreService ds) {
				try {
					Key taskKey = DatastoreHelper.getKey(taskId);
					Entity task = ds.get(taskKey);
					if(Boolean.TRUE.equals(task.getProperty(ALL_TASKS_STARTED))){
						throw new IllegalStateException("No more tasks expected!");
					}
					
					long total = getLong(task, TOTAL_COUNT) + 1;
					task.setUnindexedProperty(TOTAL_COUNT, total);
					task.setUnindexedProperty(COUNT, getLong(task, COUNT) + 1);
					ds.put(task);
					Entity subtask = new Entity(taskKey.getChild(DatastoreHelper.SUBTASK_KIND, total));
					ds.put(subtask);
					return (int) (total - 1);
				} catch (EntityNotFoundException e){
					int total = 1;
					Entity task = new Entity(DatastoreHelper.TASK_KIND, taskId);
					task.setUnindexedProperty(TOTAL_COUNT, (long) total);
					task.setUnindexedProperty(COUNT, (long) total);
					Key taskKey = ds.put(task);
					Entity subtask = new Entity(taskKey.getChild(DatastoreHelper.SUBTASK_KIND, total));
					ds.put(subtask);
					return 0;
				}
			}
		}, 0);
	}
	
	public int logAllTasksStarted(final String taskId) {
		return DatastoreHelper.call(new DatastoreHelper.Operation<Integer>(){
			public Integer run(DatastoreService ds) {
				try {
					Key taskKey = DatastoreHelper.getKey(taskId);
					Entity task = ds.get(taskKey);
					task.setUnindexedProperty(ALL_TASKS_STARTED, Boolean.TRUE);
					ds.put(task);
					return (int) getLong(task, TOTAL_COUNT);
				} catch (EntityNotFoundException e){
					throw new IllegalArgumentException("Given task doesn't exist");
				}
			}
		}, 0);
	}
	
	public boolean haveAllTasksStarted(final String taskId) {
		return DatastoreHelper.call(new DatastoreHelper.Operation<Boolean>(){
			public Boolean run(DatastoreService ds) {
				try {
					Key taskKey = DatastoreHelper.getKey(taskId);
					Entity task = ds.get(taskKey);
					Boolean allStarted = (Boolean) task.getProperty(ALL_TASKS_STARTED);
					if(allStarted == null){
						return Boolean.FALSE;
					}
					return allStarted;
				} catch (EntityNotFoundException e){
					throw new IllegalArgumentException("Given task doesn't exist");
				}
			}
		}, Boolean.FALSE);
	}
	
	public int getParallelTaskCount(final String taskId) {
		return DatastoreHelper.call(new DatastoreHelper.Operation<Integer>(){
			public Integer run(DatastoreService ds) {
				try {
					Entity task = ds.get(DatastoreHelper.getKey(taskId));
					Long total = getLong(task, TOTAL_COUNT);
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
					if((count == null || count.intValue() == 0) && (Boolean.TRUE.equals(task.getProperty(ALL_TASKS_STARTED)))){
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
			ByteArrayOutputStream bos = null;
			ObjectOutputStream oos = null;
			try {
				
				bos = new ByteArrayOutputStream();
				oos = new ObjectOutputStream(bos);
				oos.writeObject(obj);
				oos.flush();
				return new Blob(compressBytes(bos.toByteArray()));
			} finally {
				if(oos != null){
					oos.close();
				}
				if(bos != null){
					bos.close();
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
					Boolean allTaskStarted = (Boolean) task.getProperty(ALL_TASKS_STARTED);
					if(allTaskStarted == null || !allTaskStarted){
						throw new IllegalStateException("All tasks haven't started yet!");
					}
					
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
						if(result == null) {
							// continue
						} if(subtask.hasProperty(SERIALIZED) && result instanceof Blob){
							result = deserialize((Blob)result);
						} else if(Text.class.isAssignableFrom(result.getClass())){
							result = ((Text)result).getValue();
						} else {
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
			ByteArrayInputStream bais = null;
			try {
				bais = new ByteArrayInputStream(extractBytes(blob.getBytes()));
				ois = new ObjectInputStream(bais);
				return ois.readObject();
			} finally {
				if(ois != null){
					ois.close();
				}
				if(bais != null){
					bais.close();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Exception reading object from datastore", e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Exception reading object from datastore", e);
		} catch (DataFormatException e) {
			throw new RuntimeException("Exception reading object from datastore", e);
		} 
	}
	
	private static byte[] compressBytes(byte[] input) throws IOException {

        Deflater df = new Deflater(); 
        df.setInput(input);
 
        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
        df.finish();
        byte[] buff = new byte[1024];
        while(!df.finished())
        {
            int count = df.deflate(buff); 
            baos.write(buff, 0, count); 
        }
        baos.close();
        byte[] output = baos.toByteArray();
 
        return output;
    }
 
    private static byte[] extractBytes(byte[] input) throws IOException, DataFormatException
    {
        Inflater ifl = new Inflater();
        ifl.setInput(input);
 
        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
        byte[] buff = new byte[1024];
        while(!ifl.finished())
        {
            int count = ifl.inflate(buff);
            baos.write(buff, 0, count);
        }
        baos.close();
        byte[] output = baos.toByteArray();
        return output;
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

	private long getLong(Entity task, String propName) {
		Long total = (Long) task.getProperty(propName);
		if(total == null){
			return 0;
		}
		return total;
	}

}
