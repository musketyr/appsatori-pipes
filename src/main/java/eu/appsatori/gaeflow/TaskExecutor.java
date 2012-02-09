package eu.appsatori.gaeflow;

import com.google.appengine.api.taskqueue.DeferredTask;

public class TaskExecutor<A,R> implements DeferredTask {

	private static final long serialVersionUID = -3569377001403545004L;
	private final String baseTaskId;
	private final A arg;
	private final Class<? extends Task<?,?>> task;
	private final String currentState;
	
	public TaskExecutor(String currentState, String baseTaskId, A arg, Class<? extends Task<A,R>> task) {
		this.currentState = currentState;
		this.baseTaskId = baseTaskId;
		this.arg = arg;
		this.task = task;
	}

	@SuppressWarnings("unchecked")
	public void run() {
		try {
			Task<A,R> taskInstance = (Task<A, R>) task.newInstance();
			taskInstance.execute(this.arg);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public String getBaseTaskId() {
		return baseTaskId;
	}

	public Object getArg() {
		return arg;
	}
	
	public Class<? extends Task<?, ?>> getTask() {
		return task;
	}

	public String getCurrentState() {
		return currentState;
	}
	
}
