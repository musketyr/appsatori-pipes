/*
 * Copyright 2012 the original author or authors.
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

import java.util.List;

/**
 * Internal datastore to store pipe states and results.
 * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
 */
interface PipeDatastore {
	
	/**
	 * Keeps argument for the task and returns its path.
	 * @param argument the argument to be kept
	 * @return path of the kept argument
	 */
	String stashArgument(Object argument);
	
	/**
	 * Retrieves the argument by given path and removes it from the datastore. 
	 * @param path path to the argument
	 * @return argument kept in given path
	 */
	Object retrieveArgument(String path);
	
	/**
	 * Whether the task is still active.
	 * @param taskId id of the task
	 * @return <code>true</code> if the task is still active
	 */
	boolean isActive(String taskId);
	
	/**
	 * Sets the active state of the task.
	 * @param taskId id of the task
	 * @param active new active state
	 * @return whether setting the state succeeded 
	 */
	boolean setActive(String taskId, boolean active);
	
	
	/**
	 * Logs the start of the task execution.
	 * @param taskId base id of the started task (e.g. without index suffix)
	 * @return zero based index of current task added
	 */
	int logTaskStarted(String taskId);
	
	
	/**
	 * Logs that all the tasks has started.
	 * @param taskId base id of the started task (e.g. without index suffix)
	 * @return total count of task added
	 */
	int logAllTasksStarted(String taskId);
	
	/**
	 * Logs the finish of the task execution with optional results.
	 * 
	 * The results should be {@link java.io.Serializable} but implementations could
	 * constraint the results types more.
	 * @param taskId base id of the finished task (e.g. without index suffix)
	 * @param index zero based index of the executed task
	 * @param result result for given task if any or <code>null</code>
	 * @return number of tasks which haven't finished yet
	 * @throws IllegalArgumentException if there is no such task with given id logged
	 * @throws IndexOutOfBoundsException if the index is same or higher than expected count of parallel tasks
	 */
	int logTaskFinished(String taskId, int index, Object results);
	
	/**
	 * The results of the task.
	 * @param taskId base id of the task (e.g. without index suffix)
	 * @return the results of the task(s) execution
	 * @throws IllegalArgumentException if there is no such task with given id logged
	 * @throws IllegalStateException if all tasks haven't finished yet
	 */
	List<Object> getTaskResults(String taskId);
	
	/**
	 * The number of tasks running in parallel for particular base id.
	 * @param taskId base id of the task (e.g. without index suffix)
	 * @return number of tasks running in parallel for given base id
	 * @throws IllegalArgumentException if there is no such task with given id logged
	 */
	int getParallelTaskCount(String taskId);
	
	
	/**
	 * Clears task execution log form the datastore.
	 * @param taskId base id of the task (e.g. without index suffix)
	 * @return <code>true</code> if there was task of given name and has been cleared successfully
	 */
	boolean clearTaskLog(String taskId);
	
	/**
	 * Clears task execution log form the datastore.
	 * @param taskId base id of the task (e.g. without index suffix)
	 * @param force wheather unfinished tasks should be ignored
	 * @return <code>true</code> if there was task of given name and has been cleared successfully
	 */
	boolean clearTaskLog(String taskId, boolean force);

	/**
	 * Checks whether all the tasks has already started.
	 * @param baseTaskId  base id of the task (e.g. without index suffix)
	 * @return <code>true</code> if {@link #logAllTasksStarted(String)} was already called for particular id
	 */
	boolean haveAllTasksStarted(String baseTaskId);

}
