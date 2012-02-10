package eu.appsatori.pipes;

public final class NodeResult {
	public static final NodeResult END_RESULT = new NodeResult(null, null);
	
	private final Object result;
	private final String next;
	
	public static NodeResult create(String next){
		return create(next, null);
	}
	
	public static NodeResult create(String next, Object result){
		if(next == null){
			return END_RESULT;
		}
		return new NodeResult(next, result);
	}
	
	private NodeResult(String next, Object result) {
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

	public String getNext() {
		return next;
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
