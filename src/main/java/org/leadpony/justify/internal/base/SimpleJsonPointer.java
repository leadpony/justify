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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.json.JsonPointer;
import javax.json.JsonStructure;
import javax.json.JsonValue;

/**
 * Extended JSON pointer implementation.
 * 
 * @author leadpony
 */
public class SimpleJsonPointer implements JsonPointer, Iterable<String> {

    public static final SimpleJsonPointer EMPTY = new SimpleJsonPointer();
    
    private final List<String> tokens;
    
    public static SimpleJsonPointer of(String... tokens) {
        if (tokens.length == 0) {
            return EMPTY;
        } else {
            return new SimpleJsonPointer(Arrays.asList(tokens));
        }
    }
    
    public static String concat(String... tokens) {
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            sb.append("/").append(escape(token));
        }
        return sb.toString();
    }

    private SimpleJsonPointer() {
        this(Collections.emptyList());
    }
    
    private SimpleJsonPointer(List<String> tokens) {
        this.tokens = Collections.unmodifiableList(tokens);
    }
    
    @Override
    public <T extends JsonStructure> T add(T target, JsonValue value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends JsonStructure> T remove(T target) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends JsonStructure> T replace(T target, JsonValue value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsValue(JsonStructure target) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonValue getValue(JsonStructure target) {
        throw new UnsupportedOperationException();
    }

    public boolean isEmpty() {
        return tokens.isEmpty();
    }
    
    public List<String> tokens() {
        return tokens;
    }
    
    public SimpleJsonPointer concat(String propertyName) {
        List<String> list = new ArrayList<>(this.tokens.size() + 1);
        list.addAll(this.tokens);
        list.add(escape(propertyName));
        return new SimpleJsonPointer(list);
    }
    
    public SimpleJsonPointer concat(int itemIndex) {
        List<String> list = new ArrayList<>(this.tokens.size() + 1);
        list.addAll(this.tokens);
        list.add(Integer.toString(itemIndex));
        return new SimpleJsonPointer(list);
    }

    @Override
    public Iterator<String> iterator() {
        return tokens.iterator();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String token : this.tokens) {
            sb.append("/").append(escape(token));
        }
        return sb.toString();
    }
    
    private static String escape(String original) {
        return original.replaceAll("~", "~0").replaceAll("/", "~1");
    }
}
