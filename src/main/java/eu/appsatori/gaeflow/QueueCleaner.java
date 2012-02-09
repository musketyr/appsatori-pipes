package eu.appsatori.gaeflow;

import com.google.appengine.api.taskqueue.QueueFactory;

public class QueueCleaner {
	
	private QueueCleaner() {}
	
	static <A, R> void clean(String queue, String baseTaskId, int tasksCount){
		for (int i = 0; i < tasksCount; i++) {
			QueueFactory.getQueue(queue).deleteTask("" + i + "_" + baseTaskId);
		}
	}
	

}
