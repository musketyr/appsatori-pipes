package eu.appsatori.pipes.sample;

import java.util.logging.Logger;

import eu.appsatori.pipes.Node;
import eu.appsatori.pipes.NodeResult;
import eu.appsatori.pipes.SerialPipe;

public class StartNode implements Node<SerialPipe, String> {

	private static final Logger log = Logger.getLogger(StartNode.class.getName());
	
	public NodeResult execute(SerialPipe pipe, String arg){
		log.info("Running serial task");
		return pipe.run(SerialNode.class, 5);
	}
	
}
