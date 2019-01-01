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
package org.leadpony.justify.internal.keyword.assertion.content;

import java.util.EnumSet;
import java.util.Set;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.keyword.assertion.AbstractAssertion;
import org.leadpony.justify.spi.ContentEncodingScheme;

/**
 * Content keyword representing "contentEncoding".
 *
 * @author leadpony
 */
public class ContentEncoding extends AbstractAssertion implements Evaluator {

    private final ContentEncodingScheme scheme;

    /**
     * Constructs this encoding.
     *
     * @param scheme the scheme of this encoding.
     */
    public ContentEncoding(ContentEncodingScheme scheme) {
        assert scheme != null;
        this.scheme = scheme;
    }

    @Override
    public String name() {
        return "contentEncoding";
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
    protected Evaluator doCreateEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return this;
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return this::evaluateNegated;
    }

    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        builder.add(name(), scheme.name());
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        String encoded = ((JsonString) parser.getValue()).getString();
        if (scheme.canDecode(encoded)) {
            return Result.TRUE;
        } else {
            dispatcher.dispatchProblem(buildProblem(parser, "instance.problem.contentEncoding"));
            return Result.FALSE;
        }
    }

    public Result evaluateNegated(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        String encoded = ((JsonString) parser.getValue()).getString();
        if (scheme.canDecode(encoded)) {
            dispatcher.dispatchProblem(buildProblem(parser, "instance.problem.not.contentEncoding"));
            return Result.FALSE;
        }
        return Result.TRUE;
    }

    /**
     * Returns the scheme of this content encoding.
     *
     * @return the scheme of this content encoding.
     */
    ContentEncodingScheme scheme() {
        return scheme;
    }

    private Problem buildProblem(JsonParser parser, String messageKey) {
        return createProblemBuilder(parser).withMessage(messageKey).withParameter("encoding", scheme.name()).build();
    }
}
