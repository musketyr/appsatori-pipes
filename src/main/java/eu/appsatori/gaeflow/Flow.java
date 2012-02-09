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

package eu.appsatori.gaeflow;

 /**
  * 
  * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
  */
public class Flow {

	private final FlowStateDatastore flowStateDatastore;
	private final NodeDatastore nodeDatastore;
	
	private Flow(FlowStateDatastore flowStateDatastore, NodeDatastore nodeDatastore) {
		this.flowStateDatastore = flowStateDatastore;
		this.nodeDatastore = nodeDatastore;
	}
	
	
	public static Flow getFlow(FlowStateDatastore fds, NodeDatastore nds){
		if(fds == null){
			throw new NullPointerException("Flow datastore cannot be null!");
		}
		FlowStateDatastoreHolder.setFlowStateDatastore(fds);
		if(nds == null){
			throw new NullPointerException("Node datastore cannot be null!");
		}
		NodeDatastoreHolder.setNodeDatastore(nds);
		return new Flow(fds, nds);
	}
	
	public void start(String node){
		start(node, null);
	}
	
	public void start(String node, Object argument){
		new TaskExecutor(flowStateDatastore).execute(nodeDatastore.find(node), argument);
	}
	
	
	
	
}
