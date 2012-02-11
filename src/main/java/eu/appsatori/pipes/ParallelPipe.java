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