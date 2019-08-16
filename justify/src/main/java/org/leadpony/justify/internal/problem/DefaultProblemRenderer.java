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

/**
 * The default implementation of {@link ProblemRenderer}.
 *
 * @author leadpony
 */
class DefaultProblemRenderer implements ProblemRenderer {

    private final LineFormat lineFormat;

    DefaultProblemRenderer(LineFormat lineFormat) {
        this.lineFormat = lineFormat;
    }

    @Override
    public void render(Problem problem, Locale locale, Consumer<String> consumer) {
        if (problem.hasBranches()) {
            consumer.accept(problem.getMessage(locale));
            renderBranches(problem, locale, consumer);
        } else {
            consumer.accept(lineFormat.format(problem, locale));
        }
    }

    @Override
    public String render(Problem problem, Locale locale) {
        if (problem.hasBranches()) {
            StringBuilder builder = new StringBuilder();
            builder.append(problem.getMessage(locale));
            renderBranches(problem, locale, line -> {
                builder.append('\n').append(line);
            });
            return builder.toString();
        } else {
            return lineFormat.format(problem, locale);
        }
    }

    private void renderBranches(Problem problem, Locale locale, Consumer<String> consumer) {
        renderBranches(problem, locale, consumer, "");
    }

    private void renderBranches(Problem problem, Locale locale, Consumer<String> consumer, String prefix) {
        final int count = problem.countBranches();
        for (int i = 0; i < count; i++) {
            renderBranch(i, problem.getBranch(i), locale, consumer, prefix);
        }
    }

    private void renderBranch(int index, List<Problem> branch, Locale locale, Consumer<String> consumer,
            String prefix) {
        final String firstPrefix = prefix + (index + 1) + ") ";
        final String laterPrefix = repeat(' ', firstPrefix.length());
        boolean isFirst = true;
        for (Problem problem : branch) {
            String currentPrefix = isFirst ? firstPrefix : laterPrefix;
            if (problem.hasBranches()) {
                consumer.accept(currentPrefix + problem.getMessage(locale));
                renderBranches(problem, locale, consumer, laterPrefix);
            } else {
                consumer.accept(currentPrefix + lineFormat.format(problem, locale));
            }
            isFirst = false;
        }
    }

    private static String repeat(char c, int count) {
        StringBuilder b = new StringBuilder();
        while (count-- > 0) {
            b.append(c);
        }
        return b.toString();
    }
}
