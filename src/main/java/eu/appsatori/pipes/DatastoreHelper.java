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
