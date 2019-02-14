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
package org.leadpony.justify.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A test fixture.
 *
 * @author leadpony
 */
class Fixture {

    private static final Path BASE_DIR = Paths.get("target", "test-classes");

    private final Status status;
    private final String[] args;

    static Fixture of(Status status, String... args) {
        String[] copied = Arrays.copyOf(args, args.length);
        for (int i = 0; i < args.length; i++) {
            String arg = copied[i];
            if (!arg.startsWith("-")) {
                copied[i] = BASE_DIR.resolve(arg).toString();
            }
        }
        return new Fixture(status, copied);
    }

    private Fixture(Status status, String[] args) {
        this.status = status;
        this.args = args;
    }

    Status getExpectedStatus() {
        return status;
    }

    String[] args() {
        return args;
    }

    @Override
    public String toString() {
        return Stream.of(args).collect(Collectors.joining(" "));
    }
}
