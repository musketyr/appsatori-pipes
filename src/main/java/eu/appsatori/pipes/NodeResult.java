package eu.appsatori.pipes;

public final class NodeResult<N extends Node<?>> {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final NodeResult END_RESULT = new NodeResult(NodeType.SERIAL, null, null);
	
	private final Object result;
	private final Class<N> next;
	private final NodeType type;
	
	public static <N extends Node<?>> NodeResult<N> create(NodeType type, Class<N> next){
		return create(type, next, null);
	}
	
	@SuppressWarnings("unchecked")
	public static <N extends Node<?>> NodeResult<N> create(NodeType type, Class<N> next, Object result){
		if(next == null){
			return END_RESULT;
		}
		return new NodeResult<N>(type, next, result);
	}
	
	private NodeResult(NodeType type, Class<N> next, Object result) {
		this.type = NodeType.SERIAL;
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

	public Class<N> getNext() {
		return next;
	}
	
	public NodeType getType() {
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
		NodeResult<?> other = (NodeResult<?>) obj;
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
