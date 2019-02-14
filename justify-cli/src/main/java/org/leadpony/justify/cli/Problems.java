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
package org.leadpony.justify.cli;

import java.util.List;

import org.leadpony.justify.api.Problem;

/**
 * A utility class for processing {@code Problem}s.
 *
 * @author leadpony
 */
final class Problems {

    /**
     * Counts problems least.
     *
     * @param problems the problems to count.
     * @return the number of problems.
     */
    static long countLeast(List<Problem> problems) {
        long total = 0;
        for (Problem problem : problems) {
            total += countLeast(problem);
        }
        return total;
    }

    private static long countLeast(Problem problem) {
        if (problem.hasBranches()) {
            long least = Long.MAX_VALUE;
            for (int i = 0; i < problem.countBranches(); i++) {
                long count = countLeast(problem.getBranch(i));
                if (least > count) {
                    least = count;
                }
            }
            return least;
        } else {
            return 1;
        }
    }

    private Problems() {
    }
}
