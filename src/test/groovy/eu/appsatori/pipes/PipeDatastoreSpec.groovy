/*
 * Copyright 2012 AppSatori s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.appsatori.pipes

import eu.appsatori.pipes.PipeDatastore;
import spock.lang.Specification;

abstract class PipeDatastoreSpec extends Specification{
		
	def "Handle parallel task"(){
		PipeDatastore fds = createDatastore()
		String ptid = '__parallel_task__01__'
		int parallelTasksCount = 3
		
		expect:
		true == fds.logTaskStarted(ptid, parallelTasksCount)
		false == fds.logTaskStarted(ptid, parallelTasksCount)
		3 == fds.getParallelTaskCount(ptid)
		
		when:
		fds.getTaskResults(ptid)
		
		then:
		thrown(IllegalStateException)
		
		
		
		expect:
		2 == fds.logTaskFinished(ptid, 0, 'first')
		
		
		
		when:
		fds.getTaskResults(ptid)
		
		then:
		thrown(IllegalStateException)
		
		
		
		expect:
		1 == fds.logTaskFinished(ptid, 1, 'second')
		
		
		
		when:
		fds.getTaskResults(ptid)
		
		then:
		thrown(IllegalStateException)
		
		
		when:
		fds.logTaskFinished(ptid, 2, new Boolean[0])
		
		then:
		thrown(IllegalArgumentException)
		
		
		
		expect:
		0 == fds.logTaskFinished(ptid, 2, 'third')
		
		
		
		when:
		0 == fds.logTaskFinished(ptid, 0, 'one again')
		
		then:
		thrown(IllegalStateException)
		
		
		
		when:
		0 == fds.logTaskFinished(ptid, 3, 'fourth')
		then:
		thrown(IndexOutOfBoundsException)
		
		
		
		when:
		fds.logTaskFinished(ptid + 'xyz', 1, 'third')
		
		then:
		thrown(IllegalArgumentException)
		
		
		
		expect:
		['first','second','third'] == fds.getTaskResults(ptid)
		true == fds.clearTaskLog(ptid)
		false == fds.clearTaskLog(ptid)
		
		
		
		when:
		fds.getTaskResults(ptid)
		
		then:
		thrown(IllegalArgumentException)
	}

	protected abstract PipeDatastore createDatastore();
	
}
