package eu.appsatori.gaeflow;


public abstract class BaseNodeDatastore implements NodeDatastore {

	@SuppressWarnings("unchecked")
	public <E extends Throwable, R> Node<E, R> find(Class<? extends Throwable> from) {
		Node<E,R> node = find(from.getName());
		if(node != null){
			return node;
		}
		if(Throwable.class.equals(from)){
			return null;
		}
		return find((Class<? extends Throwable>)from.getSuperclass());
	}

}