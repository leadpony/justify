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
package org.leadpony.justify.internal.keyword.content;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.MediaType;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.AbstractKeywordBasedEvaluator;
import org.leadpony.justify.internal.keyword.AbstractAssertionKeyword;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.spi.ContentEncodingScheme;
import org.leadpony.justify.spi.ContentMimeType;

/**
 * A content keyword representing "contentMediaType".
 *
 * @author leadpony
 */
@KeywordClass("contentMediaType")
@Spec(SpecVersion.DRAFT_07)
public class ContentMediaType extends AbstractAssertionKeyword {

    static class ContentMediaTypeType implements KeywordType {

        private final Map<String, ContentMimeType> mimeTypes;

        ContentMediaTypeType(ContentMimeType... mimeTypes) {
            this(toMap(new HashMap<>(), Arrays.asList(mimeTypes)));
        }

        private ContentMediaTypeType(Map<String, ContentMimeType> mimeTypes) {
            this.mimeTypes = mimeTypes;
        }

        ContentMediaTypeType withMimeTypes(Collection<ContentMimeType> mimeTypes) {
            if (mimeTypes.isEmpty()) {
                return this;
            } else {
                Map<String, ContentMimeType> newMap = toMap(new HashMap<>(this.mimeTypes), mimeTypes);
                return new ContentMediaTypeType(newMap);
            }
        }

        @Override
        public String name() {
            return "contentMediaType";
        }

        @Override
        public Keyword parse(JsonValue jsonValue) {
            if (jsonValue.getValueType() == ValueType.STRING) {
                final String name = ((JsonString) jsonValue).getString();
                return createKeyword(jsonValue, name);
            } else {
                throw new IllegalArgumentException();
            }
        }

        private Keyword createKeyword(JsonValue jsonValue, String name) {
            try {
                MediaType mediaType = MediaType.valueOf(name);
                ContentMimeType mimeType = mimeTypes.get(mediaType.mimeType());
                if (mimeType != null) {
                    return new ContentMediaType(jsonValue, mimeType, mediaType.parameters());
                } else {
                    return new UnknownContentMediaType(jsonValue, name);
                }
            } catch (IllegalArgumentException e) {
                return new UnknownContentMediaType(jsonValue, name);
            }
        }

        private static Map<String, ContentMimeType> toMap(Map<String, ContentMimeType> map,
                Collection<ContentMimeType> mimeTypes) {
            for (ContentMimeType mimeType : mimeTypes) {
                map.put(mimeType.toString().toLowerCase(), mimeType);
            }
            return map;
        }
    }

    static final ContentMediaTypeType TYPE = new ContentMediaTypeType(JsonMimeType.INSTANCE);

    private final ContentMimeType mimeType;
    private final Map<String, String> parameters;
    private ContentEncodingScheme encodingScheme;

    /**
     * Constructs this media type.
     *
     * @param json the original JSON value.
     * @param mimeType the type/subtype part of this media type.
     * @param parameters additional parameters of this media type.
     */
    public ContentMediaType(JsonValue json, ContentMimeType mimeType, Map<String, String> parameters) {
        this(json, mimeType, parameters, null);
    }

    public ContentMediaType(JsonValue json, ContentMimeType mimeType, Map<String, String> parameters,
            ContentEncodingScheme encodingScheme) {
        super(json);
        this.mimeType = mimeType;
        this.parameters = parameters;
        this.encodingScheme = encodingScheme;
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public Keyword withKeywords(Map<String, Keyword> siblings) {
        if (siblings.containsKey("contentEncoding")) {
            Keyword keyword = siblings.get("contentEncoding");
            if (keyword instanceof ContentEncoding) {
                ContentEncodingScheme encodingScheme = ((ContentEncoding) keyword).scheme();
                return new ContentMediaType(
                        getValueAsJson(),
                        this.mimeType,
                        this.parameters,
                        encodingScheme);
            }
        }
        return this;
    }

    @Override
    public boolean canEvaluate() {
        return true;
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
    public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        EvaluatorContext context = parent.getContext();
        String value = context.getParser().getString();
        if (testValue(value, context, true)) {
            return Evaluator.ALWAYS_TRUE;
        }
        return new FalseContentEvaluator(parent, this) {
            @Override
            public Result evaluate(Event event, int depth) {
                Problem p = newProblemBuilder().withMessage(Message.INSTANCE_PROBLEM_CONTENTMEDIATYPE).build();
                getDispatcher().dispatchProblem(p);
                return Result.FALSE;
            }
        };
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        EvaluatorContext context = parent.getContext();
        String value = context.getParser().getString();
        if (!testValue(value, context, false)) {
            return Evaluator.ALWAYS_TRUE;
        }
        return new FalseContentEvaluator(parent, this) {
            @Override
            public Result evaluate(Event event, int depth) {
                Problem p = newProblemBuilder().withMessage(Message.INSTANCE_PROBLEM_NOT_CONTENTMEDIATYPE).build();
                getDispatcher().dispatchProblem(p);
                return Result.FALSE;
            }
        };
    }

    private boolean testValue(String value, EvaluatorContext context, boolean defaultResult) {
        if (encodingScheme != null) {
            if (encodingScheme.canDecode(value)) {
                byte[] decoded = encodingScheme.decode(value);
                return mimeType.test(decoded, parameters, context);
            } else {
                return defaultResult;
            }
        } else {
            return mimeType.test(value, context);
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

    abstract class FalseContentEvaluator extends AbstractKeywordBasedEvaluator {

        FalseContentEvaluator(Evaluator parent, Keyword keyword) {
            super(parent, keyword);
        }

        @Override
        protected ProblemBuilder newProblemBuilder() {
            return super.newProblemBuilder().withParameter("type", value());
        }
    }
}
