package eu.appsatori.pipes;

import java.util.Collection;

public interface SerialPipe extends Pipe {

	public abstract <R, N extends Node<SerialPipe, R>> NodeResult run(
			Class<N> state);

	public abstract <R, N extends Node<SerialPipe, R>> NodeResult run(
			Class<N> next, R result);

	public abstract <E, R extends Collection<E>, N extends Node<SerialPipe, E>> NodeResult sprint(
			Class<N> next, R result);

	public abstract <E, R extends Collection<E>, N extends Node<ParallelPipe, E>> NodeResult fork(
			Class<N> next, R result);

}