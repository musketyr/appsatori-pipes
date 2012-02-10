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

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;

public class QueueCleaner {
	
	private QueueCleaner() {}
	
	static <A, R> void clean(String queue, String baseTaskId, int tasksCount){
		Queue q;
		if("".equals(queue) || queue == null){
			q = QueueFactory.getDefaultQueue();
		} else {
			q = QueueFactory.getQueue(queue);
		}
		for (int i = 0; i < tasksCount; i++) {
			q.deleteTask("" + i + "_" + baseTaskId);
		}
	}
	

}
