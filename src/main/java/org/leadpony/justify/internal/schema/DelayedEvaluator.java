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

package org.leadpony.justify.internal.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Problem;

/**
 * @author leadpony
 */
public class DelayedEvaluator implements Evaluator, Consumer<Problem> {
    
    private final Evaluator real;
    private List<Problem> problems;

    public DelayedEvaluator(Evaluator real) {
        this.real = real;
    }
    
    public List<Problem> problems() {
        if (problems == null) {
            return Collections.emptyList();
        } else {
            return problems;
        }
    }
    
    public int countProblems() {
        return problems().size();
    }
    
    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
        return real.evaluate(event, parser, depth, this);
    }

    @Override
    public void accept(Problem problem) {
        if (problems == null) {
            problems = new ArrayList<>();
        }
        problems.add(problem);
    }
}
