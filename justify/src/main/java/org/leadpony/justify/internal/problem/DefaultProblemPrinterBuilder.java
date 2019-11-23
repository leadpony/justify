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
package org.leadpony.justify.internal.problem;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.util.Locale;
import java.util.function.Consumer;

import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.api.ProblemPrinterBuilder;

/**
 * The default implementation of {@link ProblemPrinterBuilder}.
 *
 * @author leadpony
 */
public class DefaultProblemPrinterBuilder implements ProblemPrinterBuilder {

    private final Consumer<String> lineConsumer;
    private Locale locale = Locale.getDefault();
    private boolean location = true;
    private boolean pointer = true;

    public DefaultProblemPrinterBuilder(Consumer<String> lineConsumer) {
        this.lineConsumer = lineConsumer;
    }

    @Override
    public ProblemHandler build() {
        LineFormat format = LineFormat.get(location, pointer);
        ProblemRenderer renderer = new BasicProblemRenderer(format);
        return new ProblemPrinter(renderer, lineConsumer, locale);
    }

    @Override
    public ProblemPrinterBuilder withLocale(Locale locale) {
        requireNonNull(locale, "locale");
        this.locale = locale;
        return this;
    }

    @Override
    public ProblemPrinterBuilder withLocation(boolean present) {
        this.location = present;
        return this;
    }

    @Override
    public ProblemPrinterBuilder withPointer(boolean present) {
        this.pointer = present;
        return this;
    }
}
