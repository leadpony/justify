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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.json.JsonException;
import javax.json.JsonReader;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonValidatingException;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.ProblemHandler;

import com.ibm.icu.text.MessageFormat;

/**
 * Validator of JSON instances and schemas.
 *
 * @author leadpony
 */
public class Validator {

    private static final String BUNDLE_NAME = Validator.class.getPackage().getName() + ".messages";

    private final JsonValidationService service = JsonValidationService.newInstance();
    private final ProblemHandler handler;
    private final ResourceBundle bundle;
    private int errors;

    /**
     * Constructs this object.
     */
    public Validator() {
        handler = service.createProblemPrinter(this::printProblem);
        bundle = ResourceBundle.getBundle(BUNDLE_NAME);
    }

    /**
     * Validates a JSON schema and a JSON instance.
     * @param args the command line arguments.
     * @return {@code true} if all of them are valid.
     *         {@code false} if one of them is invalid.
     */
    public boolean validate(String... args) {
        switch (args.length) {
        case 1:
            return validateSchema(Paths.get(args[0]));
        case 2:
            return validateInstance(Paths.get(args[0]), Paths.get(args[1]));
        default:
            printUsage();
            return true;
        }
    }

    /**
     * Return the number of lines in the standard error.
     * @return the number of lines in the standard error.
     */
    public int getNumberOfErrors() {
        return errors;
    }

    private boolean validateSchema(Path path) {
        try {
            readSchema(path);
            printMessage("json.schema.valid", path.getFileName());
            return true;
        } catch (IOException | JsonException e) {
            return false;
        }
    }

    private JsonSchema readSchema(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path);
             JsonSchemaReader reader = service.createSchemaReader(in)){
            return reader.read();
        } catch (IOException e) {
            printErrorMessage("json.schema.not.found", path);
            throw e;
        } catch (JsonValidatingException e) {
            handler.handleProblems(e.getProblems());
            throw e;
        } catch (JsonException e) {
            printError(e);
            throw e;
        }
    }

    private boolean validateInstance(Path schemaPath, Path instancePath) {
        try {
            JsonSchema schema = readSchema(schemaPath);
            boolean result = validateInstanceAgainstSchema(instancePath, schema);
            if (result) {
                printMessage("json.instance.valid", instancePath.getFileName());
            }
            return result;
        } catch (IOException | JsonException e) {
            return false;
        }
    }

    private boolean validateInstanceAgainstSchema(Path path, JsonSchema schema) {
        try (InputStream in = Files.newInputStream(path);
             JsonReader reader = service.createReader(in, schema, handler)) {
            reader.readValue();
        } catch (IOException e) {
            printErrorMessage("json.instance.not.found", path);
        } catch (JsonException e) {
            printError(e);
        }
        return (errors == 0);
    }

    private void printMessage(String key, Object... arguments) {
        System.out.println(formatMessage(key, arguments));
    }

    private void printErrorMessage(String key, Object... arguments) {
        System.err.println(formatMessage(key, arguments));
        ++errors;
    }

    private void printError(Exception e) {
        System.err.println(e.getLocalizedMessage());
        ++errors;
    }

    private void printProblem(String line) {
        System.err.println(line);
        ++errors;
    }

    private String formatMessage(String key, Object... arguments) {
        String pattern = bundle.getString(key);
        return MessageFormat.format(pattern, arguments);
    }

    private void printUsage() {
        InputStream in = findUsageResourceAsStream();
        if (in != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                reader.lines().forEach(System.out::println);
            } catch (IOException e) {
            }
        }
    }

    private InputStream findUsageResourceAsStream() {
        Locale locale = Locale.getDefault();
        String name = "usage_" + locale.getLanguage() + ".txt";
        InputStream stream = getClass().getResourceAsStream(name);
        if (stream != null) {
            return stream;
        }
        return getClass().getResourceAsStream("usage.txt");
    }

    /**
     * The entry point of this program.
     * @param args the arguments given to this program.
     */
    public static void main(String[] args) {
        boolean result = new Validator().validate(args);
        System.exit(result ? 0 : 1);
    }
}
