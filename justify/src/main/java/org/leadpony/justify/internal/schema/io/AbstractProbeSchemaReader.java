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
package org.leadpony.justify.internal.schema.io;

import java.net.URI;
import java.util.Collections;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.base.Message;

/**
 * A shcema reader which detects the version of JSON Schema specfication
 * automatically.
 *
 * @author leadpony
 */
abstract class AbstractProbeSchemaReader extends AbstractSchemaReader {

    private final JsonParser parser;
    private final SpecVersion defaultSpecVersion;

    protected AbstractProbeSchemaReader(JsonParser parser, SpecVersion defaultSpecVersion) {
        super(Collections.emptyMap());
        this.parser = parser;
        this.defaultSpecVersion = defaultSpecVersion;
    }

    /* As a AbstractSchemaReader */

    @Override
    protected JsonSchema readSchema() {
        SpecVersion version = probe(parser);
        return createSchemaReader(version).read();
    }

    @Override
    protected void closeParser() {
        parser.close();
    }

    protected abstract JsonSchemaReader createSchemaReader(SpecVersion version);

    private SpecVersion probe(JsonParser parser) {
        SpecVersion version = doProbe(parser);
        return version != null ? version : defaultSpecVersion;
    }

    private SpecVersion doProbe(JsonParser parser) {
        if (parser.hasNext()) {
            if (parser.next() == Event.START_OBJECT) {
                while (parser.hasNext()) {
                    switch (parser.next()) {
                    case KEY_NAME:
                        if (parser.getString().equals("$schema")) {
                            switch (parser.next()) {
                            case VALUE_STRING:
                                return getSpecVersion(parser.getString());
                            case START_ARRAY:
                                parser.skipArray();
                                break;
                            case START_OBJECT:
                                parser.skipObject();
                                break;
                            default:
                                break;
                            }
                        }
                        break;
                    case START_ARRAY:
                        parser.skipArray();
                        break;
                    case START_OBJECT:
                        parser.skipObject();
                        break;
                    case END_OBJECT:
                        return null;
                    default:
                        break;
                    }
                }
            }
        }
        return null;
    }

    private SpecVersion getSpecVersion(String value) {
        try {
            URI id = URI.create(value);
            if (id.getFragment() == null) {
                id = id.resolve("#");
            }
            return SpecVersion.getById(id);
        } catch (IllegalArgumentException e) {
            Problem p = createProblemBuilder(parser.getLocation(), "/$schema")
                    .withMessage(Message.SCHEMA_PROBLEM_VERSION_UNSUPPORTED)
                    .withParameter("schema", value)
                    .build();
            addProblem(p);
            dispatchProblems();
            return null;
        }
    }
}
