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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonReader;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParserFactory;
import javax.json.stream.JsonParsingException;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonSchemaReaderFactory;
import org.leadpony.justify.api.JsonValidatingException;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.cli.Console.Color;

/**
 * A command implementation for "validate" command.
 *
 * @author leadpony
 */
class Validate extends AbstractCommand {

    private final JsonValidationService service = JsonValidationService.newInstance();
    private final JsonParserFactory parserFactory = Json.createParserFactory(null);

    private final ProblemHandler problemPrinter;
    private final Catalog catalog;

    /**
     * Constructs this command.
     *
     * @param console the console to which messages will be outputted.
     */
    Validate(Console console) {
        super(console);
        this.problemPrinter = service.createProblemPrinter(console::print);
        this.catalog = new Catalog();
    }

    @Override
    public Status execute(List<String> args) {
        parseCommandArguments(args);
        populateCatalog();
        validateAll();
        return getStatus();
    }

    private void validateAll() {
        Resource location = (Resource) getOptionValue(ValidateOption.SCHEMA);
        readSchema(location).ifPresent(schema -> {
            @SuppressWarnings("unchecked")
            List<Resource> instances = (List<Resource>) getOptionValues(ValidateOption.INSTANCE);
            if (instances != null) {
                for (Resource instance : instances) {
                    validateInstance(instance, schema);
                }
            }
        });
    }

    private Optional<JsonSchema> readSchema(Resource resource) {
        console.print(VALIDATE_SCHEMA, resource);
        return validateSchema(resource);
    }

    /**
     * Validates a JSON schema.
     *
     * @param resource the resource from which the JSON schema will be read.
     * @return the schema read if it is valid.
     */
    private Optional<JsonSchema> validateSchema(Resource resource) {
        JsonSchemaReaderFactory factory = createSchemaReaderFactory();
        try (JsonSchemaReader reader = factory.createSchemaReader(openSchema(resource))) {
            JsonSchema schema = reader.read();
            console.withColor(Color.SUCCESS).print(SCHEMA_VALID, resource);
            return Optional.of(schema);
        } catch (JsonValidatingException e) {
            List<Problem> problems = e.getProblems();
            problemPrinter.handleProblems(problems);
            console.withColor(Color.DANGER).print(SCHEMA_INVALID, resource, Problems.countLeast(problems));
            setStatus(Status.INVALID);
        } catch (JsonParsingException e) {
            throw new CommandException(SCHEMA_MALFORMED, e);
        } catch (JsonException e) {
            throw new CommandException(e);
        }
        return Optional.empty();
    }

    private JsonSchemaReaderFactory createSchemaReaderFactory() {
        final boolean strict = containsOption(ValidateOption.STRICT);
        return service.createSchemaReaderFactoryBuilder()
                .withCustomFormatAttributes(false)
                .withStrictWithKeywords(strict)
                .withStrictWithFormats(strict)
                .withSchemaResolver(this.catalog).build();
    }

    /**
     * Validates a JSON instance.
     *
     * @param resource the resource representing the JSON instance.
     * @param schema   the JSON schema against which the instance to be validated.
     */
    private void validateInstance(Resource resource, JsonSchema schema) {
        console.print(VALIDATE_INSTANCE, resource);

        List<Problem> problems = new ArrayList<>();
        ProblemHandler handler = createProblemHandler(problems);

        try (JsonReader reader = service.createReader(openInstance(resource), schema, handler)) {
            reader.readValue();
        } catch (JsonParsingException e) {
            throw new CommandException(INSTANCE_MALFORMED, e);
        } catch (JsonException e) {
            throw new CommandException(e);
        }

        if (problems.isEmpty()) {
            console.withColor(Color.SUCCESS).print(INSTANCE_VALID, resource);
        } else {
            console.withColor(Color.DANGER).print(INSTANCE_INVALID, resource, Problems.countLeast(problems));
            setStatus(Status.INVALID);
        }
    }

    private void populateCatalog() {
        Resource catalog = (Resource) getOptionValue(ValidateOption.CATALOG);
        if (catalog != null) {
            loadCatalog(catalog);
        }
        @SuppressWarnings("unchecked")
        List<Resource> refs = (List<Resource>) getOptionValues(ValidateOption.REFERENCE);
        if (refs != null) {
            for (Resource ref : refs) {
                addReferencedSchema(ref);
            }
        }
    }

    private void loadCatalog(Resource resource) {
        console.print(PARSE_CATALOG, resource);
        JsonSchema schema = readSchemaFromResource("catalog.schema.json");
        List<Problem> problems = new ArrayList<>();
        ProblemHandler handler = createProblemHandler(problems);
        try (JsonParser parser = service.createParser(openCatalog(resource), schema, handler)) {
            parseCatalog(parser);
            if (!problems.isEmpty()) {
                throw new CommandException(CATALOG_INVALID, resource, Problems.countLeast(problems));
            }
        } catch (JsonValidatingException e) {
            throw new CommandException(e);
        } catch (JsonParsingException e) {
            throw new CommandException(CATALOG_MALFORMED, e);
        } catch (JsonException e) {
            throw new CommandException(e);
        }
    }

    private JsonSchema readSchemaFromResource(String name) {
        try (InputStream in = getClass().getResourceAsStream(name)) {
            return service.readSchema(in);
        } catch (IOException e) {
            throw new CommandException(e);
        }
    }

    private void parseCatalog(JsonParser parser) {
        if (!parser.hasNext() || parser.next() != Event.START_OBJECT) {
            return;
        }
        URI id = null;
        while (parser.hasNext()) {
            switch (parser.next()) {
            case KEY_NAME:
                try {
                    id = new URI(parser.getString());
                } catch (URISyntaxException e) {
                    id = null;
                }
                break;
            case VALUE_STRING:
                try {
                    Resource value = Resource.at(parser.getString());
                    if (id != null) {
                        catalog.put(id, value);
                    }
                } catch (IllegalArgumentException e) {
                }
                break;
            case START_ARRAY:
                parser.skipArray();
            case START_OBJECT:
                parser.skipObject();
            default:
                break;
            }
        }
    }

    private void addReferencedSchema(Resource resource) {
        console.print(INSPECT_SCHEMA, resource);
        identifySchema(resource).ifPresent(id -> catalog.put(id, resource));
    }

    private Optional<URI> identifySchema(Resource resource) {
        try (JsonParser parser = parserFactory.createParser(openSchema(resource))) {
            if (parser.hasNext() && parser.next() == Event.START_OBJECT) {
                while (parser.hasNext()) {
                    switch (parser.next()) {
                    case KEY_NAME:
                        if (parser.getString().equals("$id")) {
                            return extractSchemaId(parser);
                        }
                        break;
                    case START_ARRAY:
                        parser.skipArray();
                    case START_OBJECT:
                        parser.skipObject();
                    default:
                        break;
                    }
                }
            }
            console.withColor(Color.WARNING).print(SCHEMA_ID_MISSING);
            return Optional.empty();
        } catch (JsonParsingException e) {
            throw new CommandException(SCHEMA_MALFORMED, e);
        } catch (JsonException e) {
            throw new CommandException(e);
        }
    }

    private Optional<URI> extractSchemaId(JsonParser parser) {
        if (parser.next() == Event.VALUE_STRING) {
            try {
                return Optional.of(new URI(parser.getString()));
            } catch (URISyntaxException e) {
            }
        }
        console.withColor(Color.WARNING).print(SCHEMA_ID_INVALID);
        return Optional.empty();
    }

    private static InputStream openSchema(Resource resource) {
        try {
            return resource.openStream();
        } catch (NoSuchFileException e) {
            throw new CommandException(SCHEMA_NOT_FOUND, resource);
        } catch (IOException e) {
            throw new CommandException(ACCESS_FAILED, resource);
        }
    }

    private static InputStream openInstance(Resource resource) {
        try {
            return resource.openStream();
        } catch (NoSuchFileException e) {
            throw new CommandException(INSTANCE_NOT_FOUND, resource);
        } catch (IOException e) {
            throw new CommandException(ACCESS_FAILED, resource);
        }
    }

    private static InputStream openCatalog(Resource resource) {
        try {
            return resource.openStream();
        } catch (NoSuchFileException e) {
            throw new CommandException(CATALOG_NOT_FOUND, resource);
        } catch (IOException e) {
            throw new CommandException(ACCESS_FAILED, resource);
        }
    }

    private ProblemHandler createProblemHandler(List<Problem> problems) {
        return found -> {
            problems.addAll(found);
            this.problemPrinter.handleProblems(found);
        };
    }

    @Override
    protected Option findOptionByName(String arg) {
        try {
            return ValidateOption.byName(arg);
        } catch (NoSuchElementException e) {
            return super.findOptionByName(arg);
        }
    }

    @Override
    protected void processNonOptionArguments(List<String> args) {
        List<Object> tyepd = args.stream().map(arg -> {
            try {
                return ValidateOption.INSTANCE.getTypedArgument(arg);
            } catch (IllegalArgumentException e) {
                throw new CommandException(OPTION_ARGUMENT_INVALID, ValidateOption.INSTANCE, arg);
            }
        }).collect(Collectors.toList());
        addOption(ValidateOption.INSTANCE, tyepd);
    }

    @Override
    protected Set<? extends Option> getRequiredOptions() {
        return EnumSet.of(ValidateOption.SCHEMA);
    }

    /**
     * A schema catalog.
     *
     * @author leadpony
     */
    private class Catalog extends SchemaCatalog {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        protected Optional<JsonSchema> readReferencedSchema(Resource resource) {
            console.print(VALIDATE_REFERENCED_SCHEMA, resource);
            return validateSchema(resource);
        }
    }
}
