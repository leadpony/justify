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
import java.util.Collections;
import java.util.List;

import javax.json.stream.JsonParser;

import org.leadpony.justify.core.Evaluator;

/**
 * @author leadpony
 */
class DisjunctionEvaluator extends AbstractLogicalEvaluator {

    protected int numberOfTrues;
    protected List<StoringEvaluator> failed;
    
    @Override
    public void append(Evaluator evaluator) {
        super.append(new StoringEvaluator(evaluator));
    }
    
    @Override
    protected boolean accumulateResult(Evaluator evaluator, Result result) {
        if (result == Result.TRUE) {
            ++numberOfTrues;
        } else {
            if (failed == null) {
                failed = new ArrayList<>();
            }
            failed.add((StoringEvaluator)evaluator);
        }
        return (numberOfTrues == 0);
    }
    
    @Override
    protected Result conclude(JsonParser parser, Reporter reporter) {
        if (numberOfTrues > 0 || failed == null || failed.isEmpty()) {
            return Result.TRUE;
        }
        Collections.sort(failed);
        StoringEvaluator first = failed.get(0);
        first.problems().forEach(problem->reporter.reportProblem(problem));
        return Result.FALSE;
    }
}
