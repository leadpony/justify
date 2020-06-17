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
package org.leadpony.justify.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.leadpony.justify.api.keyword.Keyword;

import jakarta.json.JsonValue;

/**
 * An empty JSON schema.
 *
 * @author leadpony
 */
class EmptyJsonSchema implements ObjectJsonSchema {

    @Override
    public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        return Evaluator.ALWAYS_TRUE;
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        return Evaluator.alwaysFalse(parent, this);
    }

    @Override
    public JsonValue toJson() {
        return JsonValue.EMPTY_JSON_OBJECT;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Keyword get(Object key) {
        return null;
    }

    @Override
    public Keyword put(String key, Keyword value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Keyword remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends Keyword> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        return Collections.emptySet();
    }

    @Override
    public Collection<Keyword> values() {
        return Collections.emptySet();
    }

    @Override
    public Set<Entry<String, Keyword>> entrySet() {
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return "{}";
    }
}
