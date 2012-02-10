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
  * 
  * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
  */
public class Pipes {

	private static PipeDatastore pipeDatastore;
	private static NodeDatastore nodeDatastore;
	
	private Pipes() {}
	
	
	public static void start(String node){
		start(node, null);
	}
	
	public static void start(String node, Object argument){
		new NodeExecutor(pipeDatastore).execute(nodeDatastore.find(node), argument);
	}


	public static PipeDatastore getPipeDatastore() {
		return pipeDatastore;
	}


	public static void setPipeDatastore(PipeDatastore pipeDatastore) {
		Pipes.pipeDatastore = pipeDatastore;
	}


	public static NodeDatastore getNodeDatastore() {
		return nodeDatastore;
	}


	public static void setNodeDatastore(NodeDatastore nodeDatastore) {
		Pipes.nodeDatastore = nodeDatastore;
	}
	
	
	
	
}
