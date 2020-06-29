/*
 * Copyright 2018, 2020 the Justify authors.
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
import java.util.List;
import java.util.Map;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.internal.base.json.DefaultPointerAwareJsonParser;
import org.leadpony.justify.internal.base.json.JsonService;
import org.leadpony.justify.internal.schema.SchemaSpec;
import org.leadpony.justify.internal.validator.JsonValidator;

/**
 * A basic implementation of {@link JsonSchemaReader}.
 *
 * @author leadpony
 */
public class JsonSchemaReaderImpl extends AbstractJsonSchemaReader implements ProblemHandler {

    private final JsonParser jsonParser;
    private final Map<String, KeywordType> keywordTypeMap;

    private URI initialBaseUri = DEFAULT_INITIAL_BASE_URI;

    public JsonSchemaReaderImpl(
            JsonParser parser,
            JsonService jsonService,
            SchemaSpec spec,
            Map<String, Object> config) {
        this(parser, jsonService, spec.getBareKeywordTypes(), config, null);
    }

    /**
     * Constructs this reader.
     *
     * @param parser
     * @param jsonService
     * @param keywordTypeMap
     * @param config
     * @param metaschema     the metaschema of the schema to read. This can be
     *                       {@code null}.
     */
    public JsonSchemaReaderImpl(
            JsonParser parser,
            JsonService jsonService,
            Map<String, KeywordType> keywordTypeMap,
            Map<String, Object> config,
            JsonSchema metaschema) {
        super(config);

        this.jsonParser = wrapJsonParser(parser, jsonService, metaschema);
        this.keywordTypeMap = keywordTypeMap;

        if (parser instanceof JsonValidator) {
            ((JsonValidator) parser).withHandler(this);
        }
    }

    /* As a AbstractSchemaReader */

    @Override
    protected JsonSchema readSchema() {
        JsonSchema schema = null;
        if (this.jsonParser.hasNext()) {
            this.jsonParser.next();
            JsonValue jsonValue = this.jsonParser.getValue();
            schema = parseRootSchema(jsonValue);
        }
        dispatchProblems();
        return schema;
    }

    @Override
    protected JsonLocation getLocation() {
        return jsonParser.getLocation();
    }

    @Override
    protected void closeParser() {
        jsonParser.close();
    }

    /* As a ProblemHandler */

    @Override
    public void handleProblems(List<Problem> problems) {
        addProblems(problems);
    }

    /* */

    private JsonSchema parseRootSchema(JsonValue jsonValue) {
        RootJsonSchemaParser schemaParser = new RootJsonSchemaParser(
                this.keywordTypeMap,
                getResolvers(),
                this::addProblem,
                getConfig()
                );

        return schemaParser.parseRoot(jsonValue, initialBaseUri);
    }

    private JsonParser wrapJsonParser(JsonParser realParser, JsonService jsonService,
            JsonSchema metaschema) {
        if (metaschema != null) {
            return new JsonValidator(realParser, metaschema, jsonService.getJsonProvider());
        } else {
            return new DefaultPointerAwareJsonParser(realParser, jsonService.getJsonProvider());
        }
    }
}
