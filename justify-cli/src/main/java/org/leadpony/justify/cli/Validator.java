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

import static org.leadpony.justify.cli.Message.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.json.JsonException;
import javax.json.JsonReader;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonSchemaReaderFactory;
import org.leadpony.justify.api.JsonValidatingException;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.ProblemHandler;

/**
 * A command-line validator for validating JSON instances and schemas.
 *
 * @author leadpony
 */
class Validator {

    private final JsonValidationService service = JsonValidationService.newInstance();

    private int exitCode;

    private final Printer out = new Printer(System.out);
    private final ProblemHandler problemPrinter;

    private final Set<Option> options = EnumSet.noneOf(Option.class);

    /**
     * Constructs this object.
     */
    public Validator() {
        this.problemPrinter = service.createProblemPrinter(out::print);
    }

    /**
     * Runs this validator with command-line arguments.
     *
     * @param args the arguments specified on command line.
     * @return exit code.
     */
    public int run(String... args) {
        List<Path> paths;
        try {
            paths = parseCommandLine(args);
        } catch (Exception e) {
            setExitCode(1);
            return exitCode;
        }

        if (options.contains(Option.HELP) || paths.isEmpty()) {
            out.printUsage();
            out.print();
        } else {
            validateAll(paths);
        }

        return exitCode;
    }

    private void validateAll(List<Path> paths) {
        Iterator<Path> it = paths.iterator();
        JsonSchema schema;
        try {
            schema = validateSchema(it.next());
        } catch (Exception e) {
            setExitCode(1);
            return;
        }
        while (it.hasNext()) {
            validateInstance(it.next(), schema);
        }
    }

    /**
     * Validates a JSON schema.
     *
     * @param path the path to the JSON schema.
     * @return the schema read.
     * @throws JsonValidatingException if the schema is invalid.
     * @throws JsonException           if the input is corrupt.
     * @throws IOException             if an I/O error has occurred.
     */
    private JsonSchema validateSchema(Path path) throws IOException {
        JsonSchemaReaderFactory factory = createSchemaReaderFactory();

        out.print(JSON_SCHEMA_START, path.getFileName());
        final long offset = out.linesPrinted();

        try (InputStream in = Files.newInputStream(path); JsonSchemaReader reader = factory.createSchemaReader(in)) {
            return reader.read();
        } catch (IOException e) {
            out.print(JSON_SCHEMA_NOT_FOUND, path);
            throw e;
        } catch (JsonValidatingException e) {
            problemPrinter.handleProblems(e.getProblems());
            throw e;
        } catch (JsonException e) {
            out.print(e);
            throw e;
        } finally {
            final long lines = out.linesPrinted() - offset;
            out.print(VALIDATION_DONE, lines);
            out.print();

            if (lines > 0) {
                setExitCode(1);
            }
        }
    }

    private JsonSchemaReaderFactory createSchemaReaderFactory() {
        final boolean strict = options.contains(Option.STRICT);
        return service.createSchemaReaderFactoryBuilder().withStrictWithKeywords(strict).withStrictWithFormats(strict)
                .build();
    }

    /**
     * Validates a JSON instance.
     *
     * @param path   the path to the JSON instance.
     * @param schema the JSON schema against which the instance to be validated.
     */
    private void validateInstance(Path path, JsonSchema schema) {
        out.print(JSON_INSTANCE_START, path.getFileName());
        final long offset = out.linesPrinted();

        try (InputStream in = Files.newInputStream(path);
                JsonReader reader = service.createReader(in, schema, problemPrinter)) {
            reader.readValue();
        } catch (IOException e) {
            out.print(JSON_INSTANCE_NOT_FOUND, path);
        } catch (JsonException e) {
            out.print(e);
        }

        final long lines = out.linesPrinted() - offset;
        out.print(VALIDATION_DONE, lines);
        out.print();

        if (lines > 0) {
            setExitCode(1);
        }
    }

    /**
     * Assigns the exit code of this program.
     *
     * @param exitCode the exit code of this program.
     */
    private void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    private List<Path> parseCommandLine(String[] args) {
        List<Path> paths = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                parseOptionArgument(arg);
            } else {
                paths.add(Paths.get(arg));
            }
        }
        return paths;
    }

    private void parseOptionArgument(String arg) {
        String name = arg.substring(1);
        try {
            Option option = Option.byName(name);
            options.add(option);
        } catch (NoSuchElementException e) {
            out.print(UNRECOGNIZED_OPTION, arg);
            throw e;
        }
    }
}
