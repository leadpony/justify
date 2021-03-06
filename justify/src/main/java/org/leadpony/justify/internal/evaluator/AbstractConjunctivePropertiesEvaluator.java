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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.keyword.Keyword;

/**
 * A skeletal implementation of {@link ChildrenEvaluator} specifialized for JSON
 * objects.
 *
 * @author leadpony
 */
public abstract class AbstractConjunctivePropertiesEvaluator extends AbstractLogicalEvaluator
        implements ChildrenEvaluator {

    private Result finalResult = Result.TRUE;
    private Evaluator firstChildEvaluator;
    private List<Evaluator> additionalChildEvaluators;

    protected AbstractConjunctivePropertiesEvaluator(Evaluator parent, Keyword keyword) {
        super(parent, keyword);
    }

    @Override
    public Result evaluate(Event event, int depth) {
        if (depth == 0 && event == Event.END_OBJECT) {
            return finalResult;
        }

        if (depth == 1) {
            updateChildren(event, getParser());
        }

        if (firstChildEvaluator != null) {
            final int childDepth = depth - 1;

            if (!invokeChildEvaluator(firstChildEvaluator, event, childDepth)) {
                firstChildEvaluator = null;
            }

            if (additionalChildEvaluators != null) {
                Iterator<Evaluator> it = additionalChildEvaluators.iterator();
                while (it.hasNext()) {
                    if (!invokeChildEvaluator(it.next(), event, childDepth)) {
                        it.remove();
                    }
                }
                if (firstChildEvaluator == null && !additionalChildEvaluators.isEmpty()) {
                    firstChildEvaluator = additionalChildEvaluators.remove(0);
                }
            }
        }

        return Result.PENDING;
    }

    @Override
    public void append(Function<Evaluator, Evaluator> mapper) {
        Evaluator evaluator = mapper.apply(this);
        if (evaluator == Evaluator.ALWAYS_TRUE) {
            return;
        }
        if (firstChildEvaluator == null) {
            firstChildEvaluator = evaluator;
        } else {
            getAdditionalChildEvaluators().add(evaluator);
        }
    }


    private boolean invokeChildEvaluator(Evaluator evalutor, Event event, int depth) {
        Result result = evalutor.evaluate(event, depth);
        if (result == Result.PENDING) {
            return true;
        } else {
            if (result == Result.FALSE) {
                finalResult = Result.FALSE;
            }
            return false;
        }
    }

    private List<Evaluator> getAdditionalChildEvaluators() {
        if (additionalChildEvaluators == null) {
            additionalChildEvaluators = new ArrayList<>();
        }
        return additionalChildEvaluators;
    }
}
