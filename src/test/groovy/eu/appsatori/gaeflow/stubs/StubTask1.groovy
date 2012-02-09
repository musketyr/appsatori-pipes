package eu.appsatori.gaeflow.stubs

import eu.appsatori.gaeflow.Task;

class StubTask1 extends Task<String, Integer> {
	
	void execute(String text) {
		next('two', text.length())
	}

}
