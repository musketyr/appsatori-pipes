package eu.appsatori.pipes;

import java.io.Serializable;

class StashedArgument implements Serializable {
	
	private static final long serialVersionUID = 1842254785944005410L;
	private final long key;

	public StashedArgument(long key) {
		this.key = key;
	}

	public long getKey() {
		return key;
	}
	
	

}
