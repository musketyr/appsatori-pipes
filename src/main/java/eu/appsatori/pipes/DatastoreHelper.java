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

import java.util.ConcurrentModificationException;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.ImplicitTransactionManagementPolicy;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.ReadPolicy;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;


/**
 * Internal helper class to run operations in transactions and the right namespace.
 * 
 * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
 *
 */
class DatastoreHelper {
	
	static interface Operation<V>{
		V run(DatastoreService ds);
	}
	
	private static final int RETRIES = 10;
	private static final String FLOW_NAMESPACE = "eu_appsatori_pipes";
	static final String TASK_KIND = "task";
	static final String SUBTASK_KIND = "subtask";
	
	private DatastoreHelper() {}
	
	static <V> V call(Operation<V> op, V defaultValue){
		return call(op, defaultValue, TransactionOptions.Builder.withDefaults());
	}
	
	static <V> V call(Operation<V> op, V defaultValue, TransactionOptions txops){
		int attempt = 1;
		while(attempt <= RETRIES){
			String oldNs = NamespaceManager.get();
			NamespaceManager.set(FLOW_NAMESPACE);
			DatastoreServiceConfig config = DatastoreServiceConfig.Builder.withImplicitTransactionManagementPolicy(ImplicitTransactionManagementPolicy.AUTO).readPolicy(new ReadPolicy(ReadPolicy.Consistency.STRONG));
			DatastoreService ds = DatastoreServiceFactory.getDatastoreService(config);
			Transaction tx = ds.beginTransaction();
			try {
				V result = op.run(ds);
				tx.commit();
				return result;
			} catch (ConcurrentModificationException e){
				attempt++;
			} finally {
				if(tx.isActive()){
					tx.rollback();
				}
				NamespaceManager.set(oldNs);
			}
		}
		return defaultValue;
	}
	

	static Key getKey(String taskId) {
		return KeyFactory.createKey(TASK_KIND, taskId);
	}
	
	static Key getKey(String taskId, int index) {
		return KeyFactory.createKey(getKey(taskId), SUBTASK_KIND, index + 1);
	}

}
