package eu.appsatori.pipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class DevPipeDatastore implements PipeDatastore {
	
	private static class TaskMetadata {
		AtomicBoolean allTaskStarted;
		AtomicBoolean active;
		AtomicInteger totalCount;
		Map<Integer, Object> results;
		AtomicInteger count;
		Map<Integer, Boolean> finished;
	}
	
	private static final Random RANDOM = new Random();
	
	ConcurrentMap<String, Object> stash = new ConcurrentHashMap<String, Object>();
	ConcurrentMap<String, TaskMetadata> tasks = new ConcurrentHashMap<String, TaskMetadata>();

	public String stashArgument(Object argument) {
		if(argument == null){
			return "";
		}
		String key = argument.toString() + RANDOM.nextLong();
		stash.put(key, argument);
		return key;
	}

	public Object retrieveArgument(String path) {
		return stash.get(path);
	}

	public boolean isActive(String taskId) {
		TaskMetadata task = tasks.get(taskId);
		if(task == null){
			return false;
		}
		return task.active.get();
	}

	public boolean setActive(String taskId, boolean active) {
		TaskMetadata task = null;
		task = tasks.get(taskId);
		if(task == null){
			return false;
		}
		task.active.set(active);
		return true;
	}

	public int logTaskStarted(String taskId) {
		TaskMetadata task = tasks.get(taskId);
		if(task != null){
			if(task.allTaskStarted.get()){
				throw new IllegalStateException("No more tasks expected!");
			}
			int current = task.totalCount.incrementAndGet();
			task.count.incrementAndGet();
			return current -1;
		}
		task = new TaskMetadata();
		task.active = new AtomicBoolean(true);
		task.totalCount = new AtomicInteger(1);
		task.count = new AtomicInteger(1);
		task.results = new TreeMap<Integer, Object>();
		task.finished = new TreeMap<Integer, Boolean>();
		task.allTaskStarted = new AtomicBoolean();
		tasks.putIfAbsent(taskId, task);
		return 0;
	}
	
	
	
	public int logAllTasksStarted(String taskId) {
		TaskMetadata task = tasks.get(taskId);
		if(task == null){
			throw new IllegalArgumentException("Task " + taskId + " doesn't exist!");
		}
		task.allTaskStarted.set(true);
		return task.totalCount.get();
	}
	
	public boolean haveAllTasksStarted(String taskId) {
		TaskMetadata task = tasks.get(taskId);
		if(task == null){
			throw new IllegalArgumentException("Task " + taskId + " doesn't exist!");
		}
		return task.allTaskStarted.get();
	}

	public int logTaskFinished(String taskId, int index, Object results) {
		TaskMetadata task  = tasks.get(taskId);
		if(task == null){
			throw new IllegalArgumentException("Node " + taskId + " hasn't been logged!");
		}
		if(index >= task.totalCount.get()){
			throw new ArrayIndexOutOfBoundsException();
		}
		if(Boolean.TRUE.equals(task.finished.get(index))){
			throw new IllegalStateException("Node with index " + index + " has already finished!");
		}
		int count = task.count.decrementAndGet();
		task.results.put(index, results);
		task.finished.put(index, Boolean.TRUE);
		if(count == 0 && task.allTaskStarted.get()){
			task.active.set(false);
		}
		return count;
	}

	public List<Object> getTaskResults(String taskId) {
		TaskMetadata task = tasks.get(taskId);
		if(task == null){
			throw new IllegalArgumentException("Node " + taskId + " hasn't been logged!");
		}
		if(!task.allTaskStarted.get()){
			throw new IllegalStateException("All tasks haven't started yet!");
		}
		if(task.count.get() != 0){
			throw new IllegalStateException("All tasks haven't finished yet!");
		}
		List<Object> ret = new ArrayList<Object>();
		for (int i = 0; i < task.totalCount.get(); i++) {
			ret.add(task.results.get(i));
		}
		return Collections.unmodifiableList(ret);
	}

	public int getParallelTaskCount(String taskId) {
		TaskMetadata task = tasks.get(taskId);
		if (task == null) {
			throw new IllegalArgumentException("Node " + taskId
					+ " hasn't been logged!");
		}
		return task.totalCount.intValue();
	}

	public boolean clearTaskLog(String taskId) {
		return clearTaskLog(taskId, false);
	}

	public boolean clearTaskLog(String taskId, boolean force) {
		if(force){
			TaskMetadata task = tasks.remove(taskId);
			return task != null;
		}
		TaskMetadata task = tasks.get(taskId);
		if(task == null){
			return false;
		}
		if(task.count.intValue() != 0){
			throw new IllegalStateException("All tasks haven't finished yet!");
		}
		task = tasks.remove(taskId);
		return task != null;
		
	}

}
