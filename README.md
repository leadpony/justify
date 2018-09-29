# Justify
[![Apache 2.0 License](https://img.shields.io/:license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0
)

Justify is a JSON validator based on [JSON Schema Specification] and [JSON Processing API].

## Features

* Compliant with [JSON Schema Specification] Draft 7.
* Reinforces [JSON Processing API] (JSR 374) transparently with the validation functionality.
* Can be used with [JSON Binding API] (JSR 367) via custom JsonProvider.
* Reports problems with exact locations including line and column number.
* Passes 1000+ test cases including official ones provided by [JSON Schema Test Suite].
* Can be used as a Java module in Java 9 and 10, with additional support of legacy Java 8.

## Getting Started

### Using with the JSON-P Streaming API

```java
Jsonv jsonv = Jsonv.newInstance();

// Reads the JSON schema
Path pathToSchema = Paths.get("news.schema.json");
JsonSchema schema = jsonv.readSchema(pathToSchema);

// Problem handler
ProblemHandler handler = ProblemHandler.printingWith(System.out::println);

Path pathToInstance = Paths.get("fake-news.json");
try (JsonParser parser = jsonv.createParser(pathToInstance, schema, handler)) {
    while (parser.hasNext()) {
        JsonParser.Event event = parser.next();
        // Do something useful here
    }
}
```

### Using with the JSON-P Object Model API

```java
Jsonv jsonv = Jsonv.newInstance();

// Reads the JSON schema
Path pathToSchema = Paths.get("news.schema.json");
JsonSchema schema = jsonv.readSchema(pathToSchema);

// Problem handler
ProblemHandler handler = ProblemHandler.printingWith(System.out::println);

Path pathToInstance = Paths.get("fake-news.json");
try (JsonReader reader = jsonv.createReader(pathToInstance, schema, handler)) {
    JsonValue value = reader.readValue();
    // Do something useful here
}
```

## Current Development Status

### Schema keywords implemented

* type
* enum
* const
* multipleOf
* maximum/exclusiveMaximum
* minimum/exclusiveMinimum
* maxLength
* minLength
* pattern
* items
* additionalItems
* maxItems
* minItems
* uniqueItems
* contains
* maxProperties
* minProperties
* required
* properties
* patternProperties
* additionalProperties
* dependencies
* propertyNames
* if/then/else
* allOf
* anyOf
* oneOf
* not
* definitions
* title
* description
* format
  * date-time/date/time
  * email/idn-email
  * hostname/idn-hostname
  * ipv4/ipv6
  * json-pointer/relative-json-pointer
  * regex (ECMA 262 compliant)

### Schema keywords not implemented yet

* format
  * uri/uri-reference/iri/iri-reference
  * uri-template
* default
* contentMediaType
* contentEncoding

## Building from Source

The following tools are required to build this library.
* [OpenJDK 11]
* [Apache Maven] 3.5.4 or higher

The commands below build the library and install it into your local Maven repository.

```bash
$ git clone https://github.com/leadpony/justify.git
$ cd justify
$ mvn clean install
```

## Copyright Notice
Copyright &copy; 2018 the Justify authors. This software is licensed under [Apache License, Versions 2.0][Apache 2.0 License].

[JSON Schema Specification]: https://json-schema.org/
[JSON Processing API]: https://javaee.github.io/jsonp/
[JSON Binding API]: http://json-b.net/
[OpenJDK 11]: https://jdk.java.net/11/
[Apache Maven]: https://maven.apache.org/
[JSON Schema Test Suite]: https://github.com/json-schema-org/JSON-Schema-Test-Suite
[Apache 2.0 License]: https://www.apache.org/licenses/LICENSE-2.0
