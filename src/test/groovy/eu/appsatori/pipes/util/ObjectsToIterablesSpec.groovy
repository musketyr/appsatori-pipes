package eu.appsatori.pipes.util

import spock.lang.Specification

class ObjectsToIterablesSpec extends Specification {
	
	def "Count size"(){
		expect:
		1 == ObjectsToIterables.sizeOf(null)
		1 == ObjectsToIterables.sizeOf(new Object())
		1 == ObjectsToIterables.sizeOf("Hallo")
		3 == ObjectsToIterables.sizeOf(new String[3])
		5 == ObjectsToIterables.sizeOf(["H","a","l","l","o"]);
	}
	
}
