package eu.appsatori.pipes;

public @interface ExceptionHandler {
	Class<Node<? extends Throwable>>[] value();
}
