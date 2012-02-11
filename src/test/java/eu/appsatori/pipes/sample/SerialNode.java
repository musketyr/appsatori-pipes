package eu.appsatori.pipes.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import eu.appsatori.pipes.Node;
import eu.appsatori.pipes.NodeResult;
import eu.appsatori.pipes.SerialPipe;

public class SerialNode implements Node<SerialPipe, Integer> {

	private static final Logger log = Logger.getLogger(SerialNode.class.getName());
	
	public NodeResult execute(SerialPipe pipe, Integer arg) {
		log.info("Running serial task, I've got: " + arg);
		List<Long> list = new ArrayList<Long>(1000);
		for (long i = 0; i < 100; i++) {
			list.add(i);
		}
		return pipe.fork(ParallelNode.class, list);
	}
	
}
