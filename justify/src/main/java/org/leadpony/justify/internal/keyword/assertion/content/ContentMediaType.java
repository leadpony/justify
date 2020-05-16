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
package org.leadpony.justify.internal.keyword.assertion.content;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.MediaType;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.AbstractKeywordAwareEvaluator;
import org.leadpony.justify.internal.keyword.AbstractAssertionKeyword;
import org.leadpony.justify.internal.keyword.KeywordMapper;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.spi.ContentEncodingScheme;
import org.leadpony.justify.spi.ContentMimeType;

/**
 * A content keyword representing "contentMediaType".
 *
 * @author leadpony
 */
@KeywordType("contentMediaType")
@Spec(SpecVersion.DRAFT_07)
public class ContentMediaType extends AbstractAssertionKeyword {

    private final ContentMimeType mimeType;
    private final Map<String, String> parameters;
    private ContentEncodingScheme encodingScheme;
    private boolean unknownEncodingScheme;

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        return (value, context) -> {
            if (value.getValueType() == ValueType.STRING) {
                final String name = ((JsonString) value).getString();
                try {
                    MediaType mediaType = MediaType.valueOf(name);
                    ContentMimeType mimeType = context.getMimeType(mediaType.mimeType());
                    if (mimeType != null) {
                        return new ContentMediaType(value, mimeType, mediaType.parameters());
                    } else {
                        return new UnknownContentMediaType(value, name);
                    }
                } catch (IllegalArgumentException e) {
                    return new UnknownContentMediaType(value, name);
                }
            } else {
                throw new IllegalArgumentException();
            }
        };
    }

    /**
     * Constructs this media type.
     *
     * @param json the original JSON value.
     * @param mimeType the type/subtype part of this media type.
     * @param parameters additional parameters of this media type.
     */
    public ContentMediaType(JsonValue json, ContentMimeType mimeType, Map<String, String> parameters) {
        super(json);
        this.mimeType = mimeType;
        this.parameters = parameters;
    }

    @Override
    public Keyword link(Map<String, Keyword> siblings) {
        if (siblings.containsKey("contentEncoding")) {
            Keyword keyword = siblings.get("contentEncoding");
            if (keyword instanceof ContentEncoding) {
                this.encodingScheme = ((ContentEncoding) keyword).scheme();
            } else {
                // Unknown encoding scheme
                unknownEncodingScheme = true;
            }
        }
        return this;
    }

    @Override
    public boolean canEvaluate() {
        return !unknownEncodingScheme;
    }

    @Override
    public boolean supportsType(InstanceType type) {
        return type == InstanceType.STRING;
    }

    @Override
    public Set<InstanceType> getSupportedTypes() {
        return EnumSet.of(InstanceType.STRING);
    }

    @Override
    public Evaluator doCreateEvaluator(EvaluatorContext context, JsonSchema schema, InstanceType type) {
        String value = context.getParser().getString();
        if (testValue(value, true)) {
            return Evaluator.ALWAYS_TRUE;
        }
        return new ContentMediaTypeEvaluator(context, schema, this) {
            @Override
            public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                Problem p = newProblemBuilder().withMessage(Message.INSTANCE_PROBLEM_CONTENTMEDIATYPE).build();
                dispatcher.dispatchProblem(p);
                return Result.FALSE;
            }
        };
    }

    @Override
    public Evaluator doCreateNegatedEvaluator(EvaluatorContext context, JsonSchema schema, InstanceType type) {
        String value = context.getParser().getString();
        if (!testValue(value, false)) {
            return Evaluator.ALWAYS_TRUE;
        }
        return new ContentMediaTypeEvaluator(context, schema, this) {
            @Override
            public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                Problem p = newProblemBuilder().withMessage(Message.INSTANCE_PROBLEM_NOT_CONTENTMEDIATYPE).build();
                dispatcher.dispatchProblem(p);
                return Result.FALSE;
            }
        };
    }

    private boolean testValue(String value, boolean defaultResult) {
        if (encodingScheme != null) {
            if (encodingScheme.canDecode(value)) {
                byte[] decoded = encodingScheme.decode(value);
                return mimeType.test(decoded, parameters);
            } else {
                return defaultResult;
            }
        } else {
            return mimeType.test(value);
        }
    }

    /**
     * Returns the value of this media type.
     *
     * @return the value of this media type.
     */
    String value() {
        StringBuilder builder =  new StringBuilder(mimeType.toString());
        parameters.forEach((key, value) -> {
            builder.append("; ").append(key).append('=').append(value);
        });
        return builder.toString();
    }

    abstract class ContentMediaTypeEvaluator extends AbstractKeywordAwareEvaluator {

        ContentMediaTypeEvaluator(EvaluatorContext context, JsonSchema schema, Keyword keyword) {
            super(context, schema, keyword);
        }

        @Override
        protected ProblemBuilder newProblemBuilder() {
            return super.newProblemBuilder().withParameter("type", value());
        }
    }
}
