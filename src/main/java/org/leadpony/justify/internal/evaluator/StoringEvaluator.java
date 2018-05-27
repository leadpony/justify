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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Problem;

/**
 * @author leadpony
 */
class StoringEvaluator implements Evaluator, Consumer<Problem>, Comparable<StoringEvaluator> {

    private final Evaluator evaluator;
    private List<Problem> problems;
    
    StoringEvaluator(Evaluator evaluator) {
        this.evaluator = evaluator;
    }
    
    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
        return evaluator.evaluate(event, parser, depth, this);
    }
    
    @Override
    public void accept(Problem problem) {
        if (problems == null) {
            problems = new ArrayList<>();
        }
        problems.add(problem);
    }

    @Override
    public int compareTo(StoringEvaluator other) {
        return countProblems() - other.countProblems();
    }
    
    List<Problem> problems() {
        return this.problems;
    }
    
    int countProblems() {
        return (problems == null) ? 0 : problems.size();
    }
}
