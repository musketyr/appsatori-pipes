package eu.appsatori.pipes.sample;

import eu.appsatori.pipes.Node;
import eu.appsatori.pipes.NodeResult;
import eu.appsatori.pipes.SerialPipe;

public class TestNode implements Node<SerialPipe, Integer> {
	
	public NodeResult execute(SerialPipe pipe, Integer arg) {
		return null;
	}

}
