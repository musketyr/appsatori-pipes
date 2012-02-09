package eu.appsatori.gaeflow;

import com.google.appengine.api.taskqueue.DeferredTask;

public class NodeExecutor<A,R> implements DeferredTask {

	private static final long serialVersionUID = -3569377001403545004L;
	private final String baseTaskId;
	private final int index;
	private final A arg;
	private final String nodeName;
	
	public NodeExecutor(String nodeName, String baseTaskId, int index, A arg) {
		this.nodeName = nodeName;
		this.baseTaskId = baseTaskId;
		this.index = index;
		this.arg = arg;
	}

	public void run() {
		NodeDatastore nds = NodeDatastoreHolder.getNodeDatastore();
		FlowStateDatastore fds = FlowStateDatastoreHolder.getFlowStateDatastore();
		Node<A,R> node = nds.find(nodeName);
		Task<A, R> taskInstance = createTaskInstance(node);
		
		if(taskInstance == null){
			throw new IllegalStateException("Cannot initiate instance " + node.getTask().getName() + " does it have parameterless constructor?");
		}
		
		try {
			taskInstance.execute(this.arg);
			String nextNode = taskInstance.getNextNode();
			if(nextNode == null || "".equals(nextNode)){
				if(!node.getTaskType().isSerial()){
					if(0 == fds.logTaskFinished(baseTaskId, index, taskInstance.getResult())){
						fds.clearTaskLog(baseTaskId);
					}
				}
				return;
			}
			if(node.getTaskType().isSerial() || TaskType.PARALLEL_COMPETETIVE.equals(node.getTaskType())){
				new TaskExecutor(fds).execute(nds.find(taskInstance.getNextNode()), taskInstance.getResult());
				QueueCleaner.clean(node.getQueue(), baseTaskId, fds.getParallelTaskCount(baseTaskId));
				fds.clearTaskLog(baseTaskId);
			} else {
				int remaining = fds.logTaskFinished(baseTaskId, index, taskInstance.getResult());
				if(remaining > 0){
					return;
				}
				new TaskExecutor(fds).execute(nds.find(taskInstance.getNextNode()), fds.getTaskResults(baseTaskId));
				fds.clearTaskLog(baseTaskId);
			}
		} catch (Exception e) {
			new TaskExecutor(fds).execute(nds.find(e.getClass()), e);
			
		}
	}

	private Task<A, R> createTaskInstance(Node<A, R> node) {
		try {
			return (Task<A, R>) node.getTask().newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	public String getBaseTaskId() {
		return baseTaskId;
	}

	public Object getArg() {
		return arg;
	}
	
}
