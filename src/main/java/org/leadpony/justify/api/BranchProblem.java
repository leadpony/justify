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

package org.leadpony.justify.api;

import java.util.List;

/**
 * A specialized problem which represents alternative groups of problems.
 * 
 * @author leadpony
 */
public interface BranchProblem extends Problem {

    /**
     * Returns the number of the alternative groups in this problem.
     * @return the number of the alternative groups. 
     *         This must be more than one.
     */
    int countBranches();
    
    /**
     * Returns the list of the problems contained in the group at the specified index.
     * @param index the index of the problem group.
     * @return the list of the problems in the specified index, never be {@code null}. 
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    List<Problem> getBranch(int index);
}
