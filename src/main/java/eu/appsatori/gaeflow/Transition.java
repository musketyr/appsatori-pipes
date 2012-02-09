package eu.appsatori.gaeflow;

import com.google.appengine.api.taskqueue.TaskOptions;


public final class Transition<A,R> {
	
	public static class ExceptionBuilder<E extends Throwable, R> {
		private final Class<E> exception;

		ExceptionBuilder(Class<E> errorName) {
			this.exception = errorName;
		}
		
		public DuringBuilder<E,R> during(String state){
			return new DuringBuilder<E,R>(exception, state);
		}
		
		public Transition<E, R> handleBy(Class<? extends ExceptionHandler<E,R>> handler){
			return new Transition<E, R>("", exception.getName(), handler, TaskType.EXCEPTION_HANDLER);
		}
		
	}
	
	public static class DuringBuilder<E extends Throwable, R>{
		private final Class<E> exception;
		private final String state;
		
		DuringBuilder(Class<E> exception, String state) {
			this.exception = exception;
			this.state = state;
		}
		
		public Transition<E, R> handleBy(Class<? extends ExceptionHandler<E,R>> handler){
			return new Transition<E, R>(state, exception.getName(), handler, TaskType.EXCEPTION_HANDLER);
		}
		
	}
	
	public static class FromBuilder<A,R> {
		
		private final String from;
		
		FromBuilder(String from) {
			this.from = from;
		}

		public ToBuilder<A,R> to(String nextState){
			return new ToBuilder<A,R>(from, nextState);
		}
	}
	
	public static class ToBuilder<A,R> {
		
		private final String from;
		private final String to;
		
		ToBuilder(String from, String to) {
			this.from = from;
			this.to = to;
		}
		
		public Transition<A,R> run(Class<? extends Task<A,R>> task){
			return new Transition<A,R>(from, to, task, TaskType.SERIAL);
		}
		
		public Transition<A,R> fork(Class<? extends Task<A,R>> task){
			return new Transition<A,R>(from, to, task, TaskType.PARALLEL);
		}
		
		public Transition<A,R> win(Class<? extends Task<A,R>> task){
			return new Transition<A,R>(from, to, task, TaskType.PARALLEL_COMPETETIVE);
		}
	}
	
	
	private final String from;
	private final String to;
	private final Class<? extends Task<A,R>> task;
	private final TaskType taskType;
	
	private final String queue;
	private final TaskOptions options;
	
	Transition(String from, String to, Class<? extends Task<A, R>> task, TaskType type) {
		this(from, to, task, type, TaskOptions.Builder.withDefaults(), "");
	}
	
	Transition(String from, String to, Class<? extends Task<A, R>> task, TaskType type, TaskOptions options, String queue) {
		this.from = from;
		this.to = to;
		this.task = task;
		this.taskType = type;
		this.options = options;
		this.queue = queue;
	}
	
	public static <A, R> FromBuilder<A, R> from(String initialState){
		return new FromBuilder<A, R>(initialState);
	}
	
	public static <E extends Throwable,R> ExceptionBuilder<E,R> exception(Class<E> exception){
		return new ExceptionBuilder<E, R>(exception);
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public Class<? extends Task<A, R>> getTask() {
		return task;
	}
	
	public TaskType getTaskType() {
		return taskType;
	}
	
	public Transition<A,R> withOptions(TaskOptions options){
		return new Transition<A,R>(from, to, task, taskType, options, this.queue);
	}
	
	public Transition<A,R> inQueue(String queue){
		return new Transition<A,R>(from, to, task, taskType, this.options, queue);
	}
	
	public TaskOptions getOptions() {
		return options;
	}
	
	public String getQueue() {
		return queue;
	}
	
	
	

}
