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

package org.leadpony.justify.core;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.json.stream.JsonLocation;

/**
 * Problem detected by validation.
 * 
 * @author leadpony
 */
public interface Problem {
    
    /**
     * Return the message describing this problem.
     * 
     * @return the message of this problem.
     */
    default String getMessage() {
        return getMessage(Locale.getDefault());
    }
    
    /**
     * Return the message describing this problem.
     * 
     * @param locale the locale of the message.
     * @return the message of this problem.
     * @throws NullPointerException if given parameter was {@code null}.
     */
    String getMessage(Locale locale);

    default String getContextualMessage() {
        return getContextualMessage(Locale.getDefault());
    }
    
    String getContextualMessage(Locale locale);
    
    /**
     * Returns the location where this problem was detected.
     * 
     * @return the location of this problem, can be {@code null} if location is unknown.
     */
    JsonLocation getLocation();
    
    /**
     * Assigns the location to this problem.
     * 
     * @param location the location to assign.
     * @throws NullPointerException if given parameter was {@code null}.
     */
    void setLocation(JsonLocation location);
    
    default Map<String, ?> parametersAsMap() {
        return Collections.emptyMap();
    }

    @Override
    String toString();
}
