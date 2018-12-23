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
import java.util.Locale;
import java.util.function.Consumer;

import org.leadpony.justify.api.BranchProblem;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;

/**
 * An object to print validation problems.
 * 
 * @author leadpony
 */
public class ProblemPrinter implements ProblemHandler {

    private final Consumer<String> lineConsumer;
    private final Locale locale;
    
    /**
     * Constructs this object.
     * @param lineConsumer the object which will output the line to somewhere.
     * @param locale the locale for which the problem messages will be localized. 
     */
    public ProblemPrinter(Consumer<String> lineConsumer, Locale locale) {
        this.lineConsumer = lineConsumer;
        this.locale = locale;
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
        if (problem instanceof BranchProblem) {
            lineConsumer.accept(problem.getMessage(locale));
            printAllProblemGroups((BranchProblem)problem, "");
        } else {
            lineConsumer.accept(problem.getContextualMessage(locale));
        }
    }
    
    private void printAllProblemGroups(BranchProblem problem, String prefix) {
        for (int i = 0; i < problem.countBranches(); i++) {
            List<Problem> branch = problem.getBranch(i); 
            printProblemGroup(i, branch, prefix);
        }
    }
    
    private void printProblemGroup(int groupIndex, List<Problem> group, String prefix) {
        final String firstPrefix = prefix + (groupIndex + 1) + ") ";
        final String laterPrefix = repeat(' ', firstPrefix.length());
        boolean isFirst = true;
        for (Problem problem : group) {
            String currentPrefix = isFirst ? firstPrefix : laterPrefix;
            if (problem instanceof BranchProblem) {
                putLine(currentPrefix + problem.getMessage(locale));
                printAllProblemGroups((BranchProblem)problem, laterPrefix);
            } else {
                putLine(currentPrefix + problem.getContextualMessage(locale));
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

