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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.leadpony.justify.api.Problem;

/**
 * @author leadpony
 */
class BasicProblemRenderer implements ProblemRenderer {

    private final LineFormat lineFormat;

    /**
     * Constructs this renderer.
     *
     * @param lineFormat the format of each line.
     */
    BasicProblemRenderer(LineFormat lineFormat) {
        this.lineFormat = lineFormat;
    }

    @Override
    public void render(Problem problem, Locale locale, Consumer<String> consumer) {
        renderProblem(problem, locale, consumer, "");
    }

    @Override
    public String render(Problem problem, Locale locale) {
        if (!problem.hasBranches()) {
            // If there is no branch, returns the only line directly.
            return lineFormat.format(problem, locale);
        }
        List<String> lines = new ArrayList<>();
        renderProblem(problem, locale, lines::add, "");
        return lines.stream().collect(Collectors.joining("\n"));
    }

    private void renderProblem(Problem problem, Locale locale, Consumer<String> consumer, String prefix) {
        final int numOfBranches = problem.countBranches();
        if (numOfBranches > 1) {
            renderBranchingProblem(problem, locale, consumer, prefix);
        } else if (numOfBranches == 1) {
            renderFirstBranchOnly(problem, locale, consumer, prefix);
        } else {
            renderSimpleProblem(problem, locale, consumer, prefix);
        }
    }

    private void renderBranchingProblem(Problem problem, Locale locale, Consumer<String> consumer, String prefix) {
        final int numOfBranches = problem.countBranches();
        consumer.accept(prefix + problem.getMessage(locale));
        prefix = spaces(prefix.length());
        for (int i = 0; i < numOfBranches; i++) {
            String marker = (i + 1) + ") ";
            String newPrefix = prefix + marker;
            Iterator<Problem> it = problem.getBranch(i).iterator();
            renderProblem(it.next(), locale, consumer, newPrefix);
            newPrefix = prefix + spaces(marker.length());
            while (it.hasNext()) {
                renderProblem(it.next(), locale, consumer, newPrefix);
            }
        }
    }

    private void renderFirstBranchOnly(Problem problem, Locale locale, Consumer<String> consumer, String prefix) {
        Iterator<Problem> it = problem.getBranch(0).iterator();
        renderProblem(it.next(), locale, consumer, prefix);
        prefix = spaces(prefix.length());
        while (it.hasNext()) {
            renderProblem(it.next(), locale, consumer, prefix);
        }
    }

    private void renderSimpleProblem(Problem problem, Locale locale, Consumer<String> consumer, String prefix) {
        consumer.accept(prefix + lineFormat.format(problem, locale));
    }

    private static String spaces(int count) {
        StringBuilder b = new StringBuilder();
        while (count-- > 0) {
            b.append(' ');
        }
        return b.toString();
    }
}
