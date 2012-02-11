package eu.appsatori.pipes;

import java.util.Collection;

public interface ParallelPipe extends Pipe{

	public abstract <R, N extends Node<SerialPipe, R>> NodeResult sprint(
			Class<N> next, R result);

	public abstract <R, N extends Node<ParallelPipe, R>> NodeResult next(
			Class<N> next, R result);

	public abstract <E, R extends Collection<E>, N extends Node<SerialPipe, R>> NodeResult join(
			Class<N> next, E result);

}