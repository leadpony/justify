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

package org.leadpony.justify.internal.keyword.annotation;

import org.leadpony.justify.internal.keyword.AbstractKeyword;

/**
 * Keyword for annotation.
 * 
 * @param <T> the type of this annotation value.
 * 
 * @author leadpony
 */
public abstract class Annotation<T> extends AbstractKeyword {
    
    @Override
    public boolean canEvaluate() {
        return false;
    }
    
    @Override
    public Annotation<T> negate() {
        return this;
    }

    /**
     * Returns the value of this annotation.
     * 
     * @return the value of this annotation. 
     */
    abstract T value();
}
