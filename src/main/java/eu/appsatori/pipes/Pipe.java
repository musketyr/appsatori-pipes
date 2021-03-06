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
 * Abstract representation of pipe controller.
 * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
 */
public interface Pipe {
	
	/**
	 * Finish the execution of current node without proceeding to any other node.
	 * @return {@link NodeResult} representing the end of this pipe
	 */
	NodeResult finish();
	
	/**
	 * Fails the execution of current node without proceeding to any other node.
	 * @return {@link NodeResult} representing the end of this pipe due the failure
	 */
	NodeResult fail();
	
	/**
	 * Fails the execution of current node without proceeding to any other node.
	 * @param next the failure handler
	 * @param result the additional information about the failure
	 * @return {@link NodeResult} representing the end of this pipe due the failure
	 */
	<R, N extends Node<SerialPipe, ? super R>> NodeResult fail(Class<N> next, R result);
	
}
