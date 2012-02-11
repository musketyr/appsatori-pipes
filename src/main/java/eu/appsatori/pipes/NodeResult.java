package eu.appsatori.pipes;

public class NodeResult {
	
	static final NodeResult END_RESULT = new NodeResult(PipeType.SERIAL, null, null);
	
	private final Object result;
	private final Class<? extends Node<?,?>> next;
	private final PipeType type;
	
	static NodeResult create(PipeType type, Class<? extends Node<?,?>> next){
		return create(type, next, null);
	}
	
	static NodeResult create(PipeType type, Class<? extends Node<?,?>> next, Object result){
		if(next == null){
			return END_RESULT;
		}
		return new NodeResult(type, next, result);
	}
	
	private NodeResult(PipeType type, Class<? extends Node<?,?>> next, Object result) {
		this.type = type;
		this.result = result;
		this.next = next;
	}
	
	public boolean hasResult(){
		return result != null;
	}
	
	public Object getResult() {
		return result;
	}
	
	public boolean hasNext(){
		return next != null;
	}

	public Class<? extends Node<?,?>> getNext() {
		return next;
	}
	
	public PipeType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((next == null) ? 0 : next.hashCode());
		result = prime * result
				+ ((this.result == null) ? 0 : this.result.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeResult other = (NodeResult) obj;
		if (next == null) {
			if (other.next != null)
				return false;
		} else if (!next.equals(other.next))
			return false;
		if (result == null) {
			if (other.result != null)
				return false;
		} else if (!result.equals(other.result))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "NodeResult [result=" + result + ", next=" + next + "]";
	}

}
