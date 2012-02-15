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
 * Internal {@link Serializable} object to store arguments between invocation and task run.
 * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
 */
class StashedArgument implements Serializable {
	
	private static final long serialVersionUID = 1842254785944005410L;
	private final String key;

	public StashedArgument(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
	
	

}
