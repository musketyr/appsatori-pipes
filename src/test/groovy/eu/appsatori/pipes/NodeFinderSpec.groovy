package eu.appsatori.pipes

import eu.appsatori.pipes.stubs.Stub1Node;
import spock.lang.Specification

class NodeFinderSpec extends Specification {
	
	def "Find nodes"(){
		String packageName = Stub1Node.class.getPackage().getName()
		NodeFinder nf = new NodeFinder(packageName)
		def nodes = nf.find()
		
		expect:
		5 == nodes.size()
		nodes.find { it.name == 'Stub1' }
	}

}
