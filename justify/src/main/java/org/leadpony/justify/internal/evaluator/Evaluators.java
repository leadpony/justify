/*
 * Copyright 2018-2020 the Justify authors.
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

import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.keyword.Keyword;

/**
 * Provides various kinds of evaluators.
 *
 * @author leadpony
 */
public final class Evaluators {

    private Evaluators() {
    }

    public static LogicalEvaluator conjunctive(Evaluator parent, InstanceType type) {
        switch (type) {
        case ARRAY:
            return new ConjunctiveEvaluator(parent, Event.END_ARRAY);
        case OBJECT:
            return new ConjunctiveEvaluator(parent, Event.END_OBJECT);
        default:
            return new SimpleConjunctiveEvaluator(parent);
        }
    }

    public static LogicalEvaluator disjunctive(Evaluator parent, Keyword keyword,
            InstanceType type) {
        switch (type) {
        case ARRAY:
            return new DisjunctiveEvaluator(parent, keyword, Event.END_ARRAY);
        case OBJECT:
            return new DisjunctiveEvaluator(parent, keyword, Event.END_OBJECT);
        default:
            return new SimpleDisjunctiveEvaluator(parent, keyword);
        }
    }

    public static Evaluator exclusive(Evaluator parent, Keyword keyword,
            InstanceType type,
            Iterable<JsonSchema> schemas) {
        switch (type) {
        case ARRAY:
            return new ExclusiveEvaluator(parent, keyword, Event.END_ARRAY, schemas, type);
        case OBJECT:
            return new ExclusiveEvaluator(parent, keyword, Event.END_OBJECT, schemas, type);
        default:
            return new SimpleExclusiveEvaluator(parent, keyword, schemas, type);
        }
    }

    public static Evaluator notExclusive(Evaluator parent, Keyword keyword,
            InstanceType type,
            Iterable<JsonSchema> schemas) {
        switch (type) {
        case ARRAY:
            return new NotExclusiveEvaluator(parent, keyword, Event.END_ARRAY, schemas, type);
        case OBJECT:
            return new NotExclusiveEvaluator(parent, keyword, Event.END_OBJECT, schemas, type);
        default:
            return new SimpleNotExclusiveEvaluator(parent, keyword, schemas, type);
        }
    }
}
