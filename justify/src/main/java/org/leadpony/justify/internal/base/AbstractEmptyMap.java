/*
 * Copyright 2018-2019 the Justify authors.
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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author leadpony
 *
 * @param <K> the type of the key.
 * @param <V> the type of the value.
 */
public abstract class AbstractEmptyMap<K, V> implements Map<K, V> {

    @Override
    public final int size() {
        return 0;
    }

    @Override
    public final boolean isEmpty() {
        return false;
    }

    @Override
    public final boolean containsValue(Object value) {
        return false;
    }

    @Override
    public final boolean containsKey(Object key) {
        return false;
    }

    @Override
    public final V get(Object key) {
        return null;
    }

    @Override
    public final Set<Entry<K, V>> entrySet() {
        return Collections.emptySet();
    }

    @Override
    public final V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Set<K> keySet() {
        return Collections.emptySet();
    }

    @Override
    public final Collection<V> values() {
        return Collections.emptySet();
    }
}
