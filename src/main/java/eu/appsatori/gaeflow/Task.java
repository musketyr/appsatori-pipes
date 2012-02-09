package eu.appsatori.gaeflow;

public abstract class Task<A,R> {
	
	protected static enum ExecutionResult {
		INSTANCE;
	}
	
	private String nextState = null;
	private R result = null;
	
	public abstract ExecutionResult execute(A arg) throws Exception;
	
	protected ExecutionResult next(String state){
		return next(state, null);
	}
	
	protected ExecutionResult next(String state, R result){
		this.nextState = state;
		this.result = result;
		return ExecutionResult.INSTANCE;
	}
	
	protected ExecutionResult finish(){
		return ExecutionResult.INSTANCE;
	}
	
	public R getResult() {
		return result;
	}
	
	public String getNextNode() {
		return nextState;
	}
	
}
