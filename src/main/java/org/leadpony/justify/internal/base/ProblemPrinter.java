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

import java.util.List;
import java.util.function.Consumer;

import org.leadpony.justify.core.Problem;

/**
 * Utility class for printing validation problems.
 * 
 * @author leadpony
 */
class ProblemPrinter {

    private static final String INDENT = "  ";
    
    static void printProblem(Problem problem, Consumer<String> lineConsumer) {
        lineConsumer.accept(problem.toString());
        if (problem.hasSubproblem()) {
            printSubproblemsOf(problem, INDENT, lineConsumer);
        }
    }
    
    private static void printSubproblemsOf(Problem problem, String indent, Consumer<String> lineConsumer) {
        final String firstPrefix = indent + "- ";
        final String laterPrefix = indent + "  ";
        for (List<Problem> list : problem.getSubproblems()) {
            boolean isFirst = true;
            for (Problem subproblem : list) {
                String prefix = isFirst ? firstPrefix : laterPrefix;
                lineConsumer.accept(prefix + subproblem.toString());
                isFirst = false;
                if (subproblem.hasSubproblem()) {
                    printSubproblemsOf(subproblem, indent + INDENT, lineConsumer);
                }
            }
        }
    }
}