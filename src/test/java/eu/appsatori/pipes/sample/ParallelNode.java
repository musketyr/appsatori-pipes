package eu.appsatori.pipes.sample;

import java.util.logging.Logger;

import eu.appsatori.pipes.Node;
import eu.appsatori.pipes.NodeResult;
import eu.appsatori.pipes.ParallelPipe;


public class ParallelNode implements Node<ParallelPipe, Long> {

	private static final Logger log = Logger.getLogger(ParallelNode.class.getName());
	
	public NodeResult execute(ParallelPipe pipe, Long param){
		log.info("Running parallel task. Got " + param);
		
		return pipe.join(JoinNode.class, param);
	}
	
}
