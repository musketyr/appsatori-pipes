package eu.appsatori.pipes

import java.util.concurrent.atomic.AtomicInteger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalFileServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import eu.appsatori.pipes.DevNodeRunner.ExecutionListener;
import eu.appsatori.pipes.sample.StartNode;
import eu.appsatori.pipes.sample.WinNode;

import spock.lang.Specification


class DevNodeRunnerSpec extends Specification {
	
	
	LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(),new LocalBlobstoreServiceTestConfig(), new LocalFileServiceTestConfig())

	
	def "Execute start node"(){
		DevNodeRunner runner = new DevNodeRunner()
		AtomicInteger winTaskCount = new AtomicInteger(0)
		runner.addExecutionListener(new ExecutionListener(){
			void taskExecuted(NodeTask task) {
				println task
				if(task.node == WinNode){
					winTaskCount.incrementAndGet()
				}
			}
		});
		Pipes.runner = runner
		String id = runner.run(PipeType.SERIAL, StartNode, null)
		Thread.currentThread().sleep(3000)
		
		expect:
		winTaskCount.intValue() == 1
	}
	
	def setup(){
		helper.setUp()
	}
	
	def cleanup(){
		helper.tearDown()
	}
	
}
