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

package org.leadpony.justify.api;

/**
 * Base type of fixtures.
 * 
 * @author leadpony
 */
abstract class Fixture {

    private final String name;
    private final int index;
    
    /**
     * Constructs this fixture.
     * 
     * @param name the base nmae of this fixture.
     * @param index the index of this fixture.
     */
    protected Fixture(String name, int index) {
        this.name = name;
        this.index = index;    
    }
    
    String name() {
        StringBuilder builder = new StringBuilder();
        int beginIndex = name.lastIndexOf('/') + 1;
        int endIndex = name.lastIndexOf('.');
        builder.append(name.substring(beginIndex, endIndex))
               .append("[").append(index).append("]"); 
        return builder.toString();
    }
    
    int index() {
        return index;
    }
    
    String displayName() {
        StringBuilder builder = new StringBuilder();
        int beginIndex = name.lastIndexOf('/') + 1;
        int endIndex = name.lastIndexOf('.');
        builder.append(name.substring(beginIndex, endIndex))
               .append("[").append(index).append("]") 
               .append(" ").append(description()); 
        return builder.toString();
    }
    
    @Override
    public String toString() {
        return displayName();
    }
    
    abstract String description();
}
