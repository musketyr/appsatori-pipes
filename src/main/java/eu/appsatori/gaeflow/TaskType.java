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

package eu.appsatori.gaeflow;

import java.lang.reflect.Array;
import java.util.Collection;

public enum TaskType {
	SERIAL, 
	PARALLEL {
		@Override
		public int getParallelTasksCount(Object arg) {
			if(arg == null){
				return 1;
			}
			if(arg.getClass().isArray()){
				return Array.getLength(arg);
			}
			if (arg instanceof Collection<?>) {
				Collection<?> col = (Collection<?>) arg;
				return col.size();
			}
			return super.getParallelTasksCount(arg);
		}
		
		@Override
		public boolean isSerial() {
			return false;
		}
	}, 
	PARALLEL_COMPETETIVE {
		@Override
		public int getParallelTasksCount(Object arg) {
			return PARALLEL.getParallelTasksCount(arg);
		}
		
		@Override
		public boolean isSerial() {
			return PARALLEL.isSerial();
		}
	},
	EXCEPTION_HANDLER;

	public int getParallelTasksCount(Object arg) {
		return 1;
	}
	
	public boolean isSerial(){
		return true;
	}

}
