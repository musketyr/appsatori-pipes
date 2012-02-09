
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
 */package eu.appsatori.gaeflow;

 /**
  * Datastore to store transitions between two states of the flow.
  * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
  */
public interface TransitionDatastore {
	
	/**
	 * Finds {@link Transition} from the given state to the given state
	 * @param from original state
	 * @param to next state
	 * @return {@link Transition} for given combination or <code>null</code> if there is no such {@link Transition}
	 */
	<A,R> Transition<A,R> find(String from, String to);
	
}
