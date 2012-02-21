package eu.appsatori.pipes

import spock.lang.Specification

class DevPipeDatastoreSpec extends PipeDatastoreSpec {

	@Override
	protected PipeDatastore createDatastore() {
		return new DevPipeDatastore()
	}
	
}
