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

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;

/**
 * Monitor of events for the current instance.
 * 
 * @author leadpony
 */
interface InstanceMonitor {
    
    /**
     * Checks whether the current instance is completed or not.
     * @param event the event dispatched by the parser.
     * @param depth the depth where the event occurred.
     * @return {@code true} if the current instance is completed.
     *         {@code false} if the current instance is not completed. 
     */
    boolean isCompleted(Event event, int depth);

    static final InstanceMonitor ARRAY_MONITOR = 
            (event, depth)->(depth == 0 && event == Event.END_ARRAY);

    static final InstanceMonitor OBJECT_MONITOR = 
            (event, depth)->(depth == 0 && event == Event.END_OBJECT);

    static final InstanceMonitor DEFAULT_MONITOR = 
            (event, depth)->(depth == 0);

    /**
     * Returns the instance of this type for the specified instance type.
     * @param type the type of the current instance.
     * @return the instance of this type.
     */
    static InstanceMonitor of(InstanceType type) {
        requireNonNull(type, "type");
        switch (type) {
        case ARRAY:
            return ARRAY_MONITOR;
        case OBJECT:
            return OBJECT_MONITOR;
        default:
            return DEFAULT_MONITOR;
        }
    }
}
