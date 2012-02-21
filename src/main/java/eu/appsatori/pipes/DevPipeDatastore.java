package eu.appsatori.pipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

class DevPipeDatastore implements PipeDatastore {
	
	private static class TaskMetadata {
		AtomicBoolean active;
		AtomicInteger totalCount;
		AtomicReferenceArray<Object> results;
		AtomicInteger count;
		AtomicIntegerArray finished;
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
		System.out.println("Is active " + taskId);
		TaskMetadata task = tasks.get(taskId);
		if(task == null){
			return false;
		}
		return task.active.get();
	}

	public boolean setActive(String taskId, boolean active) {
		System.out.println("Set active ("+ active + ")  " + taskId);
		TaskMetadata task = null;
		task = tasks.get(taskId);
		if(task == null){
			return false;
		}
		task.active.set(active);
		return true;
	}

	public boolean logTaskStarted(String taskId, int parallelTaskCount) {
		TaskMetadata task = tasks.get(taskId);
		if(task != null){
			return false;
		}
		task = new TaskMetadata();
		task.active = new AtomicBoolean(true);
		task.totalCount = new AtomicInteger(parallelTaskCount);
		task.count = new AtomicInteger(parallelTaskCount);
		task.results = new AtomicReferenceArray<Object>(parallelTaskCount);
		task.finished = new AtomicIntegerArray(parallelTaskCount);
		return tasks.putIfAbsent(taskId, task) == null;
	}

	public int logTaskFinished(String taskId, int index, Object results) {
		TaskMetadata task  = tasks.get(taskId);
		if(task == null){
			throw new IllegalArgumentException("Node " + taskId + " hasn't been logged!");
		}
		if(task.finished.get(index) != 0){
			throw new IllegalStateException("Node with index " + index + " has already finished!");
		}
		int count = task.count.decrementAndGet();
		task.results.set(index, results);
		task.finished.set(index, 1);
		if(count == 0){
			task.active.set(false);
		}
		return count;
	}

	public List<Object> getTaskResults(String taskId) {
		TaskMetadata task = tasks.get(taskId);
			if(task == null){
				throw new IllegalArgumentException("Node " + taskId + " hasn't been logged!");
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
