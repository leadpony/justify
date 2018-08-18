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

package org.leadpony.justify.internal.evaluator;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.util.stream.Stream;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Evaluator.Result;
import org.leadpony.justify.core.InstanceType;

/**
 * Provides various kinds of evaluators.
 * 
 * @author leadpony
 */
public final class Evaluators {
     
    /**
     * The evaluator which evaluates any JSON schema as true ("valid").
     */
    public static final Evaluator ALWAYS_TRUE = (event, parser, depth, reporter)->Result.TRUE;

    /**
     * The evaluator whose result should be ignored.
     */
    public static final Evaluator ALWAYS_IGNORED = (event, parser, depth, reporter)->Result.IGNORED;
    
    private Evaluators() {
    }
    
    public static Evaluator always(boolean result, JsonSchema schema) {
        return result ? ALWAYS_TRUE : alwaysFalse(schema);
    }

    public static Evaluator alwaysFalse(JsonSchema schema) {
        requireNonNull(schema, "schema");
        return new AlwaysFalseEvaluator(schema);
    }
    
    public static AppendableLogicalEvaluator conjunctive(InstanceType type) {
        if (type.isContainer()) {
            return new LongConjunctiveEvaluator(type);
        } else {
            return new ConjunctiveEvaluator();
        }
    }
    
    public static LogicalEvaluator conjunctive(Stream<JsonSchema> children, InstanceType type, boolean affirmative) {
        if (type.isContainer()) {
            return new LongConjunctiveEvaluator(children, type, affirmative);
        } else {
            return new ConjunctiveEvaluator(children, type, affirmative);
        }
    }

    public static AppendableLogicalEvaluator disjunctive(InstanceType type) {
        if (type.isContainer()) {
            return new LongDisjunctiveEvaluator(type);
        } else {
            return new DisjunctiveEvaluator();
        }
    }

    public static LogicalEvaluator disjunctive(Stream<JsonSchema> children, InstanceType type, boolean affirmative) {
        if (type.isContainer()) {
            return new LongDisjunctiveEvaluator(children, type, affirmative);
        } else {
            return new DisjunctiveEvaluator(children, type, affirmative);
        }
    }

    public static LogicalEvaluator exclusive(Stream<JsonSchema> children, InstanceType type) {
        return new ExclusiveEvaluator(children, type);
    }

    public static LogicalEvaluator notExclusive(Stream<JsonSchema> children, InstanceType type) {
        if (type.isContainer()) {
            return new LongNotExclusiveEvaluator(children, type);
        } else {
            return new NotExclusiveEvaluator(children, type);
        }
    }

    private static final DefaultFactory DEFAULT_FACTORY = new DefaultFactory();

    /**
     * Returns the implementation of {@link JsonSchema.EvaluatorFactory}.
     * 
     * @return the evaluator factory.
     */
    public static JsonSchema.EvaluatorFactory asFactory() {
        return DEFAULT_FACTORY;
    }

    /**
     * The implementation of {@link JsonSchema.EvaluatorFactory}.
     * 
     * @author leadpony
     */
    private static class DefaultFactory implements JsonSchema.EvaluatorFactory {

        @Override
        public Evaluator alwaysTrue() {
            return ALWAYS_TRUE;
        }

        @Override
        public Evaluator alwaysFalse(JsonSchema schema) {
            return new AlwaysFalseEvaluator(schema);
        }
        
    }
}
