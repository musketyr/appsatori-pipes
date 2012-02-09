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
