package eu.appsatori.gaeflow;

import java.util.regex.Pattern;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

public class TaskExecutor {
	
	private static final Pattern TASK_NAME_PATTERN = Pattern.compile("[^0-9a-zA-Z\\-\\_]");
	private final FlowStateDatastore fds;

	public TaskExecutor(FlowStateDatastore flowStateDatastore) {
		this.fds = flowStateDatastore;
	}
	
	public <A, R> String execute(Node<A, R> node, Object arg){
		if(node == null){
			throw new IllegalArgumentException("Trying to run nonexisting node!");
		}
		String taskId = getUniqueTaskId(node.getName());
		Queue q = getQueue(node);
		
		for (int i = 0; i < node.getTaskType().getParallelTasksCount(arg); i++) {
			startTask(q, node, arg, taskId, i);
		}
		return taskId;
	}

	private <A, R> void startTask(Queue q, Node<A, R> node,
			Object arg, String taskId, int index) {
		fds.logTaskStarted(taskId, index);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		NodeExecutor nodeExecutor = new NodeExecutor(node.getName(), taskId, index, arg);
		TaskOptions options = TaskOptions.Builder.withTaskName(index + "_" + taskId).payload(nodeExecutor);
		q.add(options);
	}

	private <A, R> Queue getQueue(Node<A, R> transition) {
		Queue q = QueueFactory.getDefaultQueue();
		if(!"".equals(transition.getQueue())){
			q = QueueFactory.getQueue(transition.getQueue());
		}
		return q;
	}
	
	
	private <A, R> String getUniqueTaskId(String from){
		return TASK_NAME_PATTERN.matcher(from + "_" + System.currentTimeMillis()).replaceAll("_");
	}

}
