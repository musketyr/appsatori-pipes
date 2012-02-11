package eu.appsatori.pipes.sample;

import java.util.Collection;
import java.util.logging.Logger;

import eu.appsatori.pipes.Node;
import eu.appsatori.pipes.NodeResult;
import eu.appsatori.pipes.SerialPipe;

public class JoinNode implements Node<SerialPipe,Collection<Long>> {

	private static final Logger log = Logger.getLogger(JoinNode.class.getName());
	
	public NodeResult execute(SerialPipe pipe, Collection<Long> param){
		log.info("Running join task. Got " + param);
		return pipe.sprint(WinNode.class, param);
	}
	
}
