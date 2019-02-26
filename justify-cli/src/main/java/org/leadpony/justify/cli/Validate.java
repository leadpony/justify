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
 * A command implementation executing "validate" command.
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
        Location location = (Location) getOptionValue(ValidateOption.SCHEMA);
        @SuppressWarnings("unchecked")
        List<Location> instances = (List<Location>) getOptionValues(ValidateOption.INSTANCE);
        JsonSchema schema = readSchemaAt(location);
        if (schema != null) {
            for (Location instance : instances) {
                validateInstanceAt(instance, schema);
            }
        } else if (!instances.isEmpty()) {
            throw new CommandException(SCHEMA_FAILED);
        }
    }

    private JsonSchema readSchemaAt(Location location) {
        console.print(VALIDATE_SCHEMA, location);
        return validateSchemaAt(location);
    }

    /**
     * Reads and validates a JSON schema.
     *
     * @param location the location of the JSON schema to be read and validated.
     * @return the schema read if successful, or {@code null} if errors occcured.
     */
    private JsonSchema validateSchemaAt(Location location) {
        JsonSchemaReaderFactory factory = createSchemaReaderFactory();
        try (JsonSchemaReader reader = factory.createSchemaReader(openSchema(location))) {
            JsonSchema schema = reader.read();
            console.withColor(Color.SUCCESS).print(SCHEMA_VALID, location);
            return schema;
        } catch (JsonValidatingException e) {
            List<Problem> problems = e.getProblems();
            problemPrinter.handleProblems(problems);
            console.withColor(Color.DANGER).print(SCHEMA_INVALID, location, Problems.countLeast(problems));
            setStatus(Status.INVALID);
            return null;
        } catch (JsonParsingException e) {
            console.withColor(Color.DANGER).print(SCHEMA_MALFORMED, e);
            setStatus(Status.INVALID);
            return null;
        } catch (JsonException e) {
            throw new CommandException(e);
        }
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
     * @param location the location of the JSON instance to be validated.
     * @param schema   the JSON schema against which the instance to be validated.
     */
    private void validateInstanceAt(Location location, JsonSchema schema) {
        console.print(VALIDATE_INSTANCE, location);

        List<Problem> problems = new ArrayList<>();
        ProblemHandler handler = createProblemHandler(problems);

        try (JsonReader reader = service.createReader(openInstance(location), schema, handler)) {
            reader.readValue();
        } catch (JsonParsingException e) {
            console.withColor(Color.DANGER).print(INSTANCE_MALFORMED, e);
            setStatus(Status.INVALID);
            return;
        } catch (JsonException e) {
            throw new CommandException(e);
        }

        if (problems.isEmpty()) {
            console.withColor(Color.SUCCESS).print(INSTANCE_VALID, location);
        } else {
            console.withColor(Color.DANGER).print(INSTANCE_INVALID, location, Problems.countLeast(problems));
            setStatus(Status.INVALID);
        }
    }

    private void populateCatalog() {
        Location catalog = (Location) getOptionValue(ValidateOption.CATALOG);
        if (catalog != null) {
            readSchemaCatalogAt(catalog);
        }
        @SuppressWarnings("unchecked")
        List<Location> refs = (List<Location>) getOptionValues(ValidateOption.REFERENCE);
        if (refs != null) {
            for (Location ref : refs) {
                addReferencedSchema(ref);
            }
        }
    }

    private void readSchemaCatalogAt(Location location) {
        console.print(READ_CATALOG, location);
        JsonSchema schema = readSchemaFromResource("catalog.schema.json");
        List<Problem> problems = new ArrayList<>();
        ProblemHandler handler = createProblemHandler(problems);
        try (JsonParser parser = service.createParser(openCatalog(location), schema, handler)) {
            parseCatalog(parser, location);
            if (!problems.isEmpty()) {
                console.withColor(Color.DANGER).print(CATALOG_INVALID, location, Problems.countLeast(problems));
                throw new CommandException(CATALOG_FAILED);
            }
        } catch (JsonParsingException e) {
            console.withColor(Color.DANGER).print(CATALOG_MALFORMED, e);
            throw new CommandException(CATALOG_FAILED);
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

    private void parseCatalog(JsonParser parser, Location location) {
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
                    Location value = Location.at(parser.getString());
                    if (id != null) {
                        catalog.put(id, location.resolve(value));
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

    private void addReferencedSchema(Location location) {
        console.print(INSPECT_SCHEMA, location);
        URI id = identifySchema(location);
        if (id != null) {
            console.print(SCHEMA_ID_FOUND, id);
            catalog.put(id, location);
        }
    }

    private URI identifySchema(Location location) {
        try (JsonParser parser = parserFactory.createParser(openSchema(location))) {
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
            return null;
        } catch (JsonParsingException e) {
            console.withColor(Color.DANGER).print(SCHEMA_MALFORMED, e);
            return null;
        } catch (JsonException e) {
            throw new CommandException(e);
        }
    }

    private URI extractSchemaId(JsonParser parser) {
        if (parser.next() == Event.VALUE_STRING) {
            try {
                return new URI(parser.getString());
            } catch (URISyntaxException e) {
            }
        }
        console.withColor(Color.WARNING).print(SCHEMA_ID_INVALID);
        return null;
    }

    private static InputStream openSchema(Location location) {
        try {
            return location.openStream();
        } catch (NoSuchFileException e) {
            throw new CommandException(SCHEMA_NOT_FOUND, location);
        } catch (IOException e) {
            throw new CommandException(ACCESS_FAILED, location);
        }
    }

    private static InputStream openInstance(Location location) {
        try {
            return location.openStream();
        } catch (NoSuchFileException e) {
            throw new CommandException(INSTANCE_NOT_FOUND, location);
        } catch (IOException e) {
            throw new CommandException(ACCESS_FAILED, location);
        }
    }

    private static InputStream openCatalog(Location location) {
        try {
            return location.openStream();
        } catch (NoSuchFileException e) {
            throw new CommandException(CATALOG_NOT_FOUND, location);
        } catch (IOException e) {
            throw new CommandException(ACCESS_FAILED, location);
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
        protected JsonSchema readReferencedSchema(Location location) {
            console.print(VALIDATE_REFERENCED_SCHEMA, location);
            return validateSchemaAt(location);
        }
    }
}
