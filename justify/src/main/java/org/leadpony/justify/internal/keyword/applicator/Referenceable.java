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

package org.leadpony.justify.internal.keyword.applicator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.keyword.Evaluatable;
import org.leadpony.justify.internal.keyword.SchemaKeyword;

/**
 * A keyword containing referenceable subschema.
 *
 * @author leadpony
 */
public class Referenceable extends Applicator {

    private final JsonSchema subschema;

    /**
     * Constructs this keyword.
     *
     * @param name the name of this keyword.
     * @param subschema the subschema contained by this keyword.
     */
    public Referenceable(String name, JsonSchema subschema) {
        super(name, subschema.toJson());
        this.subschema = subschema;
    }

    @Override
    public void addToEvaluatables(List<Evaluatable> evaluatables, Map<String, SchemaKeyword> keywords) {
    }

    @Override
    public boolean hasSubschemas() {
        return true;
    }

    @Override
    public Stream<JsonSchema> getSubschemas() {
        return Stream.of(subschema);
    }

    @Override
    public JsonSchema getSubschema(Iterator<String> jsonPointer) {
        return subschema;
    }
}
