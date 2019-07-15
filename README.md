# Justify
[![Apache 2.0 License](https://img.shields.io/:license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.leadpony.justify/justify.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.leadpony.justify%22%20AND%20a:%22justify%22)
[![Javadocs](https://www.javadoc.io/badge/org.leadpony.justify/justify.svg?color=green)](https://www.javadoc.io/doc/org.leadpony.justify/justify)
[![Build Status](https://travis-ci.org/leadpony/justify.svg?branch=master)](https://travis-ci.org/leadpony/justify)

Justify is a JSON validator based on [JSON Schema Specification] and [Java API for JSON Processing (JSR 374)].

## Key Features

* Compliant with [JSON Schema Specification] Draft-07, -06, and -04.
* Reinforces [Java API for JSON Processing (JSR 374)] transparently with the validation functionality.
* Can be used with [Java API for JSON Binding (JSR 367)] via a custom JsonProvider.
* Reports problems with the source locations including line and column numbers.
* Passes all test cases provided by [JSON Schema Test Suite] including both mandatory and optional tests.
* Validates the input in streaming way, which claims small memory footprint even when the input is a large JSON.
* Accepts custom formats for string and other instance types.
* Supports Java 8, 9, 10, 11, and 12.
* Can be used as a modular jar in Java 9 and higher.
* Internationalized problem messages, including Japanese language support.

## Getting Started

### Minimum Setup

This software is available in the [Maven Central Repository] and the following `dependency` element should be added to your `pom.xml`.
```xml
<dependency>
    <groupId>org.leadpony.justify</groupId>
    <artifactId>justify</artifactId>
    <version>1.0.0</version>
</dependency>
```

Besides the library itself, one of [Java API for JSON Processing (JSR 374)] implementations is needed in runtime.
This library supports the following implementations and you can select whichever you prefer.
1. [Reference Implementation]
2. [Apache Johnzon]

Please add exactly one dependency to the `pom.xml` as shown below.

#### Reference Implementation
```xml
<dependency>
    <groupId>org.glassfish</groupId>
    <artifactId>jakarta.json</artifactId>
    <classifier>module</classifier>
    <version>1.1.5</version>
</dependency>
```

#### Apache Johnzon
```xml
<dependency>
    <groupId>org.apache.johnzon</groupId>
    <artifactId>johnzon-core</artifactId>
    <version>1.1.12</version>
</dependency>
```

### Using with the Streaming API of JSON Processing

```java
JsonValidationService service = JsonValidationService.newInstance();

// Reads the JSON schema
JsonSchema schema = service.readSchema(Paths.get("news.schema.json"));

// Problem handler which will print problems found.
ProblemHandler handler = service.createProblemPrinter(System.out::println);

Path path = Paths.get("fake-news.json");
// Parses the JSON instance by javax.json.stream.JsonParser
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
// Reads the JSON instance by javax.json.JsonReader
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

For more information, please see [the code sample](https://github.com/leadpony/justify-examples/tree/master/justify-examples-defaultvalue).

## Similar Solutions

There exist several JSON validator implementations conformant to the JSON Schema Specification, including those for other programming languages. [The list of implementations] is available on the JSON Schema web site.

## Copyright Notice
Copyright &copy; 2018-2019 the Justify authors. This software is licensed under [Apache License, Versions 2.0][Apache 2.0 License].

[JSON Schema Specification]: https://json-schema.org/
[Java API for JSON Processing (JSR 374)]: https://eclipse-ee4j.github.io/jsonp/
[Java API for JSON Binding (JSR 367)]: http://json-b.net/
[JSON Schema Test Suite]: https://github.com/json-schema-org/JSON-Schema-Test-Suite
[Apache 2.0 License]: https://www.apache.org/licenses/LICENSE-2.0
[Justify Examples]: https://github.com/leadpony/justify-examples
[Justify CLI]: https://github.com/leadpony/justify-cli
[API Reference in Javadoc]: https://www.javadoc.io/doc/org.leadpony.justify/justify
[Maven Central Repository]: https://mvnrepository.com/repos/central
[Reference Implementation]: https://github.com/eclipse-ee4j/jsonp
[Apache Johnzon]: https://johnzon.apache.org/
[The list of implementations]: https://json-schema.org/implementations.html  
[Releases]: https://github.com/leadpony/justify/releases/latest
[Changelog]: CHANGELOG.md

[everit-org/json-schema]: https://github.com/everit-org/json-schema
[java-json-tools/json-schema-validator]: https://github.com/java-json-tools/json-schema-validator
[networknt/json-schema-validator]: https://github.com/networknt/json-schema-validator

[JSON Schema Conformance Test]: https://github.com/leadpony/json-schema-conformance-test
