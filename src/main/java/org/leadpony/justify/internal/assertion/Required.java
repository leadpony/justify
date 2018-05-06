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

package org.leadpony.justify.internal.assertion;

import java.util.HashSet;
import java.util.Set;

import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Evaluator;

/**
 * @author leadpony
 */
public class Required implements Assertion {
    
    private final Set<String> names;
    
    public Required(Iterable<String> names) {
        this.names = new HashSet<>();
        names.forEach(this.names::add);
    }

    @Override
    public boolean isApplicableTo(InstanceType type) {
        return type == InstanceType.OBJECT;
    }

    @Override
    public Evaluator createEvaluator() {
        return null;
    }

    @Override
    public void toJson(JsonGenerator generator) {
        generator.writeStartArray("required");
        names.forEach(generator::write);
        generator.writeEnd();
    }
}
