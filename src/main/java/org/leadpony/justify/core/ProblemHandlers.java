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

package org.leadpony.justify.core;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.leadpony.justify.core.spi.JsonValidationServiceProvider;

/**
 * Utility class for providing various kinds of common problem handlers.
 * 
 * @author leadpony
 */
public final class ProblemHandlers {

    /**
     * Returns a problem handler which will store problems to the specified collection.
     * 
     * @param collection the collection to which problems will be stored.
     * @return newly created instance of problem handler.
     * @throws NullPointerException if specified {@code collection} was {@code null}.
     */
    public static Consumer<List<Problem>> collectingTo(Collection<Problem> collection) {
        Objects.requireNonNull(collection, "collection must not be null.");
        return problems->collection.addAll(problems);
    }
    
    /**
     * Returns a problem handler which will print problems 
     * with the aid of the specified line consumer.
     * 
     * @param lineConsumer the object which will consume the line to print.
     * @return newly created instance of problem handler.
     * @throws NullPointerException if specified {@code lineConsumer} was {@code null}.
     */
    public static Consumer<List<Problem>> printingWith(Consumer<String> lineConsumer) {
        return JsonValidationServiceProvider.provider().createProblemPrinter(lineConsumer);
    }

    /**
     * Constructor to prevent instantiation of this class.
     */
    private ProblemHandlers() {
    }
}
