
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
 */package eu.appsatori.pipes.impl;

import eu.appsatori.pipes.NodeDescriptor;
import eu.appsatori.pipes.NodeDatastore;

 /**
  * Base implementation of {@link NodeDatastore}.
  * Contains common exception handling logic.
  * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
  */
public abstract class BaseNodeDatastore implements NodeDatastore {

	/* (non-Javadoc)
	 * @see eu.appsatori.pipes.NodeDatastore#find(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public NodeDescriptor find(Class<? extends Throwable> from) {
		NodeDescriptor node = find(from.getName());
		if(node != null){
			return node;
		}
		if(Throwable.class.equals(from)){
			return null;
		}
		return find((Class<? extends Throwable>)from.getSuperclass());
	}

}