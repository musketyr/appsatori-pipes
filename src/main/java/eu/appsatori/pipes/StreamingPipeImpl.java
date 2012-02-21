package eu.appsatori.pipes;

public class StreamingPipeImpl implements StreamingPipe {

	private final String taskId;
	
	public StreamingPipeImpl(String taskId) {
		this.taskId = taskId;
	}

	public NodeResult finish() {
		Pipes.getRunner().getPipeDatastore().logAllTasksStarted(taskId);
		return NodeResult.RESULT_STREAMING;
	}

	public NodeResult fail() {
		return SerialPipeImpl.INSTANCE.fail();
	}

	public <R, N extends Node<SerialPipe, ? super R>> NodeResult fail(
			Class<N> next, R result) {
		return SerialPipeImpl.INSTANCE.fail(next, result);
	}

	public <A, N extends Node<ParallelPipe, ? super A>> void send(Class<N> part, A param) {
		Pipes.getRunner().run(taskId, PipeType.PARALLEL, part, param);
	}

}
