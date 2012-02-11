package eu.appsatori.pipes;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

class ObjectsToIterables {
	
	private ObjectsToIterables(){}
	
	public static int sizeOf(Object arg){
		if(arg == null){
			return 1;
		}
		if(arg.getClass().isArray()){
			return Array.getLength(arg);
		}
		if (!(arg instanceof Collection<?>)) {
			return 1;
		}
		Collection<?> col = (Collection<?>) arg;
		return col.size();
	}
	
	public static Object getAt(int index, Object arg) {
		if(arg == null){
			return null;
		}
		if(arg.getClass().isArray()){
			return Array.get(arg, index);
		}
		if(List.class.isAssignableFrom(arg.getClass())){
			return List.class.cast(arg).get(index);
		}
		if(Iterable.class.isAssignableFrom(arg.getClass())){
			int i = 0;
			@SuppressWarnings("rawtypes")
			Iterator it = ((Iterable)arg).iterator();
			while (it.hasNext()) {
				Object next = (Object) it.next();
				if(i == index){
					return next;
				}
			}
		}
		return arg;
		
	}

}
