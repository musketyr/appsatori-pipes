package eu.appsatori.pipes.sample;

import java.util.logging.Logger;

import eu.appsatori.pipes.Node;
import eu.appsatori.pipes.NodeResult;
import eu.appsatori.pipes.SerialPipe;

public class WinNode implements Node<SerialPipe, Long> {

	private static final Logger log = Logger.getLogger(WinNode.class.getName());
	
	public NodeResult execute(SerialPipe pipe, Long param) {
		log.info("Running win task. Got " + param);
		return pipe.finish();
	}
	
}
