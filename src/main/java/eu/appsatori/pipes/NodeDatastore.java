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
  * Datastore to store flow nodes.
  * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
  */
public interface NodeDatastore {
	
	/**
	 * Finds {@link NodeDescriptor} for a given name.
	 * @param from node name
	 * @return {@link NodeDescriptor} for given name or <code>null</code> if there is no such {@link NodeDescriptor}
	 */
	NodeDescriptor find(String from);
	
	/**
	 * Finds {@link NodeDescriptor} handling given exception.
	 * @param from class of exception to be handled
	 * @return {@link NodeDescriptor} for given name or <code>null</code> if there is no such {@link NodeDescriptor}
	 */
	NodeDescriptor find(Class<? extends Throwable> from);
	
}
