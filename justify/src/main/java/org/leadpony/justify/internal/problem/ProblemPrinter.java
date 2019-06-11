/*
 * Copyright 2018-2019 the Justify authors.
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

package org.leadpony.justify.internal.problem;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;

/**
 * An object to print validation problems.
 *
 * @author leadpony
 */
class ProblemPrinter implements ProblemHandler {

    private final Consumer<String> lineConsumer;
    private final ProblemFormatter formatter;
    private final Locale locale;

    /**
     * Constructs this object.
     *
     * @param lineConsumer the object which will output the line to somewhere.
     * @param locale       the locale for which the problem messages will be
     *                     localized.
     * @param formatter    the formatter for formatting each message for a problem.
     */
    ProblemPrinter(Consumer<String> lineConsumer, Locale locale, ProblemFormatter formatter) {
        this.lineConsumer = lineConsumer;
        this.locale = locale;
        this.formatter = formatter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleProblems(List<Problem> problems) {
        for (Problem problem : problems) {
            printProblem(problem);
        }
    }

    private void printProblem(Problem problem) {
        if (problem.hasBranches()) {
            lineConsumer.accept(formatter.formatBranchingProblem(problem, locale));
            printAllProblemGroups(problem, "");
        } else {
            lineConsumer.accept(formatter.formatProblem(problem, locale));
        }
    }

    private void printAllProblemGroups(Problem problem, String prefix) {
        for (int i = 0; i < problem.countBranches(); i++) {
            List<Problem> branch = problem.getBranch(i);
            printProblemGroup(i, branch, prefix);
        }
    }

    private void printProblemGroup(int groupIndex, List<Problem> branch, String prefix) {
        final String firstPrefix = prefix + (groupIndex + 1) + ") ";
        final String laterPrefix = repeat(' ', firstPrefix.length());
        boolean isFirst = true;
        for (Problem problem : branch) {
            String currentPrefix = isFirst ? firstPrefix : laterPrefix;
            if (problem.hasBranches()) {
                putLine(currentPrefix + formatter.formatBranchingProblem(problem, locale));
                printAllProblemGroups(problem, laterPrefix);
            } else {
                putLine(currentPrefix + formatter.formatProblem(problem, locale));
            }
            isFirst = false;
        }
    }

    private void putLine(String line) {
        this.lineConsumer.accept(line);
    }

    private static String repeat(char c, int count) {
        StringBuilder b = new StringBuilder();
        while (count-- > 0) {
            b.append(c);
        }
        return b.toString();
    }
}
