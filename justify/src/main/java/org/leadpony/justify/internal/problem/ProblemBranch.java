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

package org.leadpony.justify.internal.problem;

import java.util.ArrayList;
import org.leadpony.justify.api.Problem;

/**
 * A branch of problems.
 *
 * @author leadpony
 */
@SuppressWarnings("serial")
public class ProblemBranch extends ArrayList<Problem> {

    public static ProblemBranch of(Problem problem) {
        ProblemBranch branch = new ProblemBranch(1);
        branch.add(problem);
        return branch;
    }

    public ProblemBranch() {
    }

    public ProblemBranch(ProblemBranch other) {
        addAll(other);
    }

    public ProblemBranch(int initialCapacity) {
        super(initialCapacity);
    }

    public boolean isResolvable() {
        for (Problem problem : this) {
            if (!problem.isResolvable()) {
                return false;
            }
        }
        return true;
    }
}
