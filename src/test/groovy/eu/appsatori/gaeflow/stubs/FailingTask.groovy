package eu.appsatori.gaeflow.stubs

import eu.appsatori.gaeflow.Task;

class FailingTask extends Task<String, Integer> {
	
	ExecutionResult execute(String text) {
		throw new IllegalArgumentException()
	}

}
