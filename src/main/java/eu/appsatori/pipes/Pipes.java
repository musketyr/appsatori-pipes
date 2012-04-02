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

import java.util.Collection;
import java.util.Random;
import java.util.regex.Pattern;


/**
 * Facade class to the AppSatori Pipes framework.
 * 
 * Offers methods to start execution of the nodes.
 * 
 * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
 *
 */
public class Pipes {
	
	private static final Random RANDOM = new Random();
	private static final Pattern TASK_NAME_PATTERN = Pattern.compile("[^0-9a-zA-Z\\-\\_]");
	
	private static NodeRunner runner = new AppEngineNodeRunner();

	private Pipes() { }
	
	/**
	 * Runs the selected node.
	 * 
	 * @param node class of the node to be run
	 */
	public static <R, N extends Node<SerialPipe, ? super R>> void run(Class<N> node){
		run(node, null);
	}
	
	
	/**
	 * Runs the selected node with given parameter
	 * @param node class of the node to be run
	 * @param parameter	parameter for the node
	 */
	public static <R, N extends Node<SerialPipe, ? super R>> void run(Class<N> node, R parameter){
		start(PipeType.SERIAL, node, parameter);
	}
	
	/**
	 * Runs the selected node in streaming mode.
	 * 
	 * @param node class of the node to be run in streaming mode
	 */
	public static <R, N extends Node<StreamingPipe, ? super R>> void stream(Class<N> node){
		stream(node, null);
	}
	
	
	/**
	 * Runs the selected node with given parameter in streaming mode.
	 * @param node class of the node to be run in streaming mode
	 * @param parameter	parameter for the node
	 */
	public static <R, N extends Node<StreamingPipe, ? super R>> void stream(Class<N> node, R parameter){
		start(PipeType.STREAMING, node, parameter);
	}
	
	/**
	 * For each element of <code>parameters</code> argument runs one node in parallel.
	 * The pipe will be collecting results of the executions so it could be send to one of the following
	 * nodes using the {@link ParallelPipe#join(Class, Object)} method. 
	 * @param node class of the node to be run in parallel
	 * @param parameters the collection of parameters for the nodes
	 */
	public static <E, R extends Collection<E>, N extends Node<ParallelPipe, ? super E>> void fork(Class<N> node, R parameters){
		start(PipeType.PARALLEL, node, parameters);
	}
	
	/**
	 * For each element of <code>parameters</code> argument runs one independent node in parallel.
	 * Only the fastest node will proceed to the following node.
	 * @param node class of the node to be run in parallel
	 * @param parameters the collection of parameters for the nodes
	 */
	public static <E, R extends Collection<E>, N extends Node<SerialPipe, ? super E>> void sprint(Class<N> node, R parameters){
		start(PipeType.COMPETETIVE, node, parameters);
	}
	
	/**
	 * For each element of <code>parameters</code> argument runs one independent node in parallel.
	 * All the nodes will proceed to following node.
	 * This method is is used to start multiple independent serial nodes in parallel.
	 * @param node class of the node to be run in parallel
	 * @param parameters the collection of parameters for the nodes
	 */
	public static<E, R extends Collection<E>, N extends Node<SerialPipe, ? super E>> void spread(Class<N> next, R parameters){
		for(E e: parameters){
			run(next, e);
		}
	}
	
	private static <N extends Node<?,?>> String start(PipeType type, Class<? extends Node<?,?>> node, Object arg){
		return runner.run(type, node, arg);
	}
	
	static NodeRunner getRunner() {
		return runner;
	}
	
	static void setRunner(NodeRunner runner) {
		if(runner == null){
			throw new IllegalArgumentException("Runner cannot be null!");
		}
		Pipes.runner = runner;
	}
	
	static String getQueueName(Class<? extends Node<?,?>> node) {
		String name = "";
		
		if(node.isAnnotationPresent(eu.appsatori.pipes.Queue.class)){
			name = node.getAnnotation(eu.appsatori.pipes.Queue.class).value();
		}
		return name;
	}
	
	
	public static String getUniqueTaskId(String from){
		return TASK_NAME_PATTERN.matcher(from + "_" + RANDOM.nextLong() + "_" + System.nanoTime()).replaceAll("_");
	}
	
}
