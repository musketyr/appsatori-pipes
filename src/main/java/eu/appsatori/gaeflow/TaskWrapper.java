package eu.appsatori.gaeflow;

import java.util.regex.Pattern;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

public class TaskWrapper {
	
	private static final Pattern TASK_NAME_PATTERN = Pattern.compile("[^0-9a-zA-Z\\-\\_]");
	private final FlowStateDatastore fds;

	public TaskWrapper(FlowStateDatastore flowStateDatastore) {
		this.fds = flowStateDatastore;
	}
	
	public <A, R> String wrapAndExecute(Transition<A, R> transition, Object arg){
		String taskId = getUniqueTaskId(transition.getFrom(), transition.getTo());
		Queue q = getQueue(transition);
		
		for (int i = 0; i < transition.getTaskType().getParallelTasksCount(arg); i++) {
			startTask(q, transition, arg, taskId, i);
		}
		return taskId;
	}

	private <A, R> void startTask(Queue q, Transition<A, R> transition,
			Object arg, String taskId, int index) {
		fds.logTaskStarted(taskId, index);
		TaskOptions options = TaskOptions.Builder.withTaskName(index + "_" + taskId).payload(new TaskExecutor(transition.getTo(), taskId, arg, transition.getTask()));
		q.add(options);
	}

	private <A, R> Queue getQueue(Transition<A, R> transition) {
		Queue q = QueueFactory.getDefaultQueue();
		if(!"".equals(transition.getQueue())){
			q = QueueFactory.getQueue(transition.getQueue());
		}
		return q;
	}
	
	
	private <A, R> String getUniqueTaskId(String from, String to){
		return TASK_NAME_PATTERN.matcher(from + "_" + to + System.currentTimeMillis()).replaceAll("_");
	}

}
