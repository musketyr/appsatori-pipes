package eu.appsatori.gaeflow;

public abstract class Task<A,R> {
	
	private String nextState = null;
	private R result = null;
	
	public abstract void execute(A arg);
	
	protected void next(String state){
		next(state, null);
	}
	
	protected void next(String state, R result){
		this.nextState = state;
		this.result = result;
	}
	
	public R getResult() {
		return result;
	}
	
	public String getNextState() {
		return nextState;
	}
	
}
