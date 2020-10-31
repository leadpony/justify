# Justify
[![Apache 2.0 License](https://img.shields.io/:license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.leadpony.justify/justify.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.leadpony.justify%22%20AND%20a:%22justify%22)
[![Javadocs](https://www.javadoc.io/badge/org.leadpony.justify/justify.svg?color=green)](https://www.javadoc.io/doc/org.leadpony.justify/justify)
[![Build Status](https://travis-ci.org/leadpony/justify.svg?branch=master)](https://travis-ci.org/leadpony/justify)

Justify is a JSON validator based on [JSON Schema Specification] and [Jakarta JSON Processing API] (JSON-P).

## Key Features

* Compliant with [JSON Schema Specification] Draft-07, -06, and -04.
* Reinforces [Jakarta JSON Processing API] (JSON-P) transparently with the validation functionality.
* Can be used with [Jakarta JSON Binding API] (JSON-B) via a custom JsonProvider.
* Reports problems with the source locations including line and column numbers.
* Passes all test cases provided by [JSON Schema Test Suite] including both mandatory and optional tests.
* Validates the input in streaming way, which claims small memory footprint even when the input is a large JSON.
* Accepts custom formats for string and other simple types.
* Supports YAML validation with [Joy].
* Runs under Java 8 and higher.
* Can be used as a modular jar in Java 9 and higher.
* Internationalized problem messages, including Japanese language support.

## Getting Started

_For Justify version 2.x users:_
Please refer to [old README](./README-v2.md) instead of this one for more appropriate instructions.

The difference between Justify version 3.x and 2.x is:
* Justify 3.x uses JSON-P API version 2.x, which is defined in the new package `jakarta.json`.
* Justify 2.x uses JSON-P API version 1.x, which is defined in the old package `javax.json`.

### Minimum Setup

This software is available in the [Maven Central Repository] and the following dependency should be added to your build.

*Maven*
```xml
<dependency>
    <groupId>org.leadpony.justify</groupId>
    <artifactId>justify</artifactId>
    <version>3.0.0</version>
</dependency>
```

*Gradle*
```
implementation 'org.leadpony.justify:justify:3.0.0'
```

Note that the addition of this dependency brings the following artifacts as transitive dependencies.

* `jakarta.json:jakarta.json-api`
* `com.ibm.icu:icu4j`

Besides the library itself, an implementation of [Jakarta JSON Processing API] is needed during runtime.
This library supports the following implementations and you can select whichever you prefer.
1. [Jakarta JSON Processing] (Reference Implementation)
2. [Joy]

Please add exactly one dependency to your build as shown below.

#### Jakarta JSON Processing
*Maven*
```xml
<dependency>
    <groupId>org.glassfish</groupId>
    <artifactId>jakarta.json</artifactId>
    <classifier>module</classifier>
    <version>2.0.0</version>
    <scope>runtime</scope>
</dependency>
```

*Gradle*
```
runtimeOnly 'org.glassfish:jakarta.json:2.0.0:module'
```

Please note that the classifier `module` is required when using this implementation.

#### Joy
*Maven*
```xml
<dependency>
    <groupId>org.leadpony.joy</groupId>
    <artifactId>joy-classic</artifactId>
    <version>2.0.0</version>
    <scope>runtime</scope>
</dependency>
```

*Gradle*
```
runtimeOnly 'org.leadpony.joy:joy-classic:2.0.0'
```

### Using with the Streaming API of JSON Processing

```java
JsonValidationService service = JsonValidationService.newInstance();

// Reads the JSON schema
JsonSchema schema = service.readSchema(Paths.get("news.schema.json"));

// Problem handler which will print problems found.
ProblemHandler handler = service.createProblemPrinter(System.out::println);

Path path = Paths.get("fake-news.json");
// Parses the JSON instance by JsonParser
try (JsonParser parser = service.createParser(path, schema, handler)) {
    while (parser.hasNext()) {
        JsonParser.Event event = parser.next();
        // Do something useful here
    }
}
```

### Using with the Object Model API of JSON Processing

```java
JsonValidationService service = JsonValidationService.newInstance();

// Reads the JSON schema
JsonSchema schema = service.readSchema(Paths.get("news.schema.json"));

// Problem handler which will print problems found.
ProblemHandler handler = service.createProblemPrinter(System.out::println);

Path path = Paths.get("fake-news.json");
// Reads the JSON instance by JsonReader
try (JsonReader reader = service.createReader(path, schema, handler)) {
    JsonValue value = reader.readValue();
    // Do something useful here
}
```

## Command-Line Interface

Justify CLI is a command-line wrapper of Justify library.
This utility can be used to validate JSON documents against JSON schemas without writing any code.

### Downloads

Check the [Releases] page to get the latest distribution in `tar.gz` or `zip` format,
whichever you prefer. The software requires Java 8 or higher to run.

### Usage

After unpacking the downloaded file, just typing the following command validates a JSON instance against a JSON schema.

```bash
$ ./justify -s <path/to/schema> -i <path/to/instance>
```

The following command validates a JSON schema against its metaschema.

```bash
$ ./justify -s <path/to/schema>
```

#### Options

##### -s _<path/to/schema>_

Required option to specify a path to a JSON schema against which one or more JSON instances will be validated.

##### -i _<path/to/instance>_ ...

Optional option to specify a path to a JSON instance to be validated.
Multiple instances can be specified using whitespace as a delimiter.

##### -r _<path/to/schema>_ ...

Optional option to specify a path to a JSON schema to be referenced by other JSON schemas.
Multiple schemas can be specified using whitespace as a delimiter.

##### -h

Displays all available options including those shown above.

## Additional Resources

* [Justify Examples] which show how to use this library.
* [API Reference in Javadoc]
* [Changelog]

## Conformance to Specification

This software is one of the most correct implementation of the JSON Schema Specification. Please refer to the result of [JSON Schema Conformance Test].

## Completion by `default` Keyword

The missing properties and/or items in the instance can be filled with default values provided by `default` keyword while it is being validated.

For example, the input JSON instance shown below
```json
{
    "red": 64,
    "green": 128,
    "blue": 192
}
```

will be filled with the default value and modified to:
```json
{
    "red": 64,
    "green": 128,
    "blue": 192,
    "alpha": 255
}
```

Both `JsonParser` and `JsonReader` support the feature. `JsonParser` produces additional events caused by the default values and `JsonReader` expands objects and arrays with the additional values.

By default, this feature is disabled and the instance never be modified. The following code shows how to explicitly enable the feature for the parsers and readers.

```java
ValidationConfig config = service.createValidationConfig();
config.withSchema(schema)
      .withProblemHandler(handler)
      .withDefaultValues(true);  // This enables the feature.
// For retrieving parser factory
JsonParserFactory parserFactory = service.createParserFactory(config.getAsMap());
// Or for retrieving reader factory
JsonReaderFactory readerFactory = service.createReaderFactory(config.getAsMap());
```

For more information, please see [Default Value example](https://github.com/leadpony/justify-examples/tree/master/justify-examples-defaultvalue).

## YAML Validation

Just replacing the JSON-P implementation with `joy-yaml` provided by [Joy] project as shown below enables the validation of YAML documents.

```xml
<dependency>
    <groupId>org.leadpony.joy</groupId>
    <artifactId>joy-yaml</artifactId>
    <version>2.0.0</version>
    <scope>runtime</scope>
</dependency>
```

For more information, please see [YAML Validator example](https://github.com/leadpony/justify-examples/tree/master/justify-examples-yamlvalidator).

## Building from Source

The following tools are required to build this software.
* [JDK] 14
* [Apache Maven] 3.6.2 or higher

The commands below build this software and install it into your local Maven repository.

```bash
$ git clone --recursive https://github.com/leadpony/justify.git
$ cd justify
$ mvn clean install -P release
```

## Similar Solutions

There exist several JSON validator implementations conformant to the JSON Schema Specification, including those for other programming languages. [The list of implementations] is available on the JSON Schema web site.

## Copyright Notice
Copyright &copy; 2018-2020 the Justify authors. This software is licensed under [Apache License, Versions 2.0][Apache 2.0 License].

[Apache 2.0 License]: https://www.apache.org/licenses/LICENSE-2.0
[Apache Maven]: https://maven.apache.org/
[API Reference in Javadoc]: https://www.javadoc.io/doc/org.leadpony.justify/justify
[Changelog]: CHANGELOG.md
[Jakarta JSON Processing]: https://github.com/eclipse-ee4j/jsonp
[Jakarta JSON Processing API]: https://eclipse-ee4j.github.io/jsonp/
[Jakarta JSON Binding API]: http://json-b.net/
[JDK]: https://jdk.java.net/
[Joy]: https://github.com/leadpony/joy
[JSON Schema Conformance Test]: https://github.com/leadpony/json-schema-conformance-test
[JSON Schema Specification]: https://json-schema.org/
[JSON Schema Test Suite]: https://github.com/json-schema-org/JSON-Schema-Test-Suite
[Justify CLI]: https://github.com/leadpony/justify-cli
[Justify Examples]: https://github.com/leadpony/justify-examples
[Maven Central Repository]: https://mvnrepository.com/repos/central
[Releases]: https://github.com/leadpony/justify/releases/latest
[The list of implementations]: https://json-schema.org/implementations.html  
