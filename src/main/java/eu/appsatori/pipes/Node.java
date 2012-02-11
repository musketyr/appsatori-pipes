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

import java.io.Serializable;

/**
 * Node is basic building block of pipes. 
 * 
 * Use then to perform task in certain queue. 
 * 
 * If you want to perform task in different queue than default one,
 * use {@link Queue} annotation on the implementation class.
 * 
 * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
 *
 * @param <P> either {@link ParallelPipe} if the node is supposed to run in parallel or {@link SerialPipe} otherwise
 * @param <A> type of the argument passed to the node. Must be {@link Serializable}
 * 
 * @see Pipes#run(Class)
 * @see Pipes#run(Class, Serializable)
 * @see Pipes#fork(Class, Serializable)
 * @see Pipes#sprint(Class, Serializable)
 */
public interface Node<P extends Pipe, A> {
	
	/**
	 * The task executed by this node.
	 * 
	 * Use methods of {@link SerialPipe} and {@link ParallelPipe}
	 * to control the flow of the pipe. 
	 * The flow is influenced only if the methods are used in return statements such as
	 * 
	 * <code>return pipe.next(MyTask, params);</code>
	 * 
	 * To start another pipe as a side effect of the task use one of the {@link Pipes} methods.
	 * 
	 * @param pipe one of {@link ParallelPipe} or {@link SerialPipe} pipe controllers
	 * @param arg parameeter of the task
	 * @return {@link NodeResult} signaling following pipe flow
	 */
	NodeResult execute(P pipe, A arg);

}
