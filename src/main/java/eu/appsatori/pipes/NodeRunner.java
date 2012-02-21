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


/**
 * Internal abstaction of type running particular nodes.
 * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
 */
interface NodeRunner {
	<N extends Node<?,?>> String run(PipeType type, Class<N> node, Object arg);
	<N extends Node<?,?>> int run(String taskId, PipeType type, Class<N> node, Object arg);
	PipeDatastore getPipeDatastore();
	void clearTasks(String queue, String baseTaskId, int tasksCount);
}
