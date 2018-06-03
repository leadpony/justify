/*
 * Copyright 2018 the Justify authors.
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

package org.leadpony.justify.internal.base;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author leadpony
 */
public class CompositeIterator<E> implements Iterator<E> {

    private final LinkedList<Iterable<E>> iterables = new LinkedList<>();
    private Iterator<E> currentIterator;
    
    public CompositeIterator<E> add(Iterable<E> iterable) {
        Objects.requireNonNull(iterable, "iterable must not be null.");
        iterables.add(iterable);
        return this;
    }
    
    public CompositeIterator<E> add(Stream<Iterable<E>> stream) {
        stream.forEach(this::add);
        return this;
    }

    public CompositeIterator<E> add(E element) {
        add(Collections.singleton(element));
        return this;
    }

    public CompositeIterator<E> add(Optional<E> element) {
        element.ifPresent(this::add);
        return this;
    }
    
    @Override
    public boolean hasNext() {
        if (currentIterator == null) {
            currentIterator = nextIterator();
            if (currentIterator == null) {
                return false;
            }
        }
        while (!currentIterator.hasNext()) {
            currentIterator = nextIterator();
            if (currentIterator == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return currentIterator.next();
    }
    
    private Iterator<E> nextIterator() {
        if (iterables.isEmpty()) {
            return null;
        }
        return iterables.removeFirst().iterator();
    }
}
