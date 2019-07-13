# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased
### Changed
- The parameter of `Keyword.getValueAsJson()` was removed.
- Now every JSON schema and schema keyword retains its original JSON representation internally.

## 0.17.0 - 2019-06-09
### Added
- `Keyword` interface which represents a keyword contained in a JSON schema.
- `ObjectJsonSchema` interface which is a JSON schema represented by a JSON object. This type can be viewed as an immutable map of `Keyword` instances.
- `JsonSchema.asObjectJsonSchema()`, which can cast an instance of `JsonSchema` to `ObjectJsonSchema`.
- `JsonSchema.getJsonValueType()`, which returns the value type of JSON value returned by `toJson()`.
- `JsonSchema.getKeywordValue(String)` and `JsonSchema.getKeywordValue(String, JsonValue)`, both return the value of the specified schema keyword as a `JsonValue`.
- `JsonParser` created by `JsonParserFactory.createParser(JsonArray)` or `JsonParserFactory.createParser(JsonObject)` now can validate the value even when using this library with the JSON-P Reference Implementation. (Issue #14 reported by @atomictag)
- `JsonValidationService.getJsonProvider()` which returns the underlying `JsonProvider` instance used by the service.

### Changed
- The Maven coordinates of the dependency which provides the JSON Processing API was now migrated to `jakarta.json:jakarta.json-api`. When using this library with the Reference Implementation, the Maven coordinates of the implementation now should be `org.glassfish:jakarta.json` with its classifier as `module`.
- The constant `JsonSchema.EMPTY` now has a type of `ObjectJsonSchema`.

### Fixed
- A bug which was causing unrecognized keywords to disappear from the effective schema. Note that ill-formed keywords reserved in the JSON Schema specification are not retained even in this release. This restriction should be fixed in the future release.  (Issue #16 reported by @atomictag)
- A bug which was causing `JsonParser.getString()` to throw  `NullPointerException` instead of `IllegalStateException` when the method was called before the initial invocation of `next()`.
- A bug which was causing the order of values given by `type` keyword to be changed while reading schemas.

## 0.16.0 - 2019-04-21
### Added
- Full support of JSON Schema specification Draft-06 and Draft-04.
- `SpecVersion` enum type to define the supported versions of JSON Schema specification.
- `JsonSchemaReaderFactoryBuilder.withDefaultSpecVersion()` to specify the default version of JSON Schema specification.
- Automatic detection of the JSON Schema specification version based on the `$schema` keyword values. This feature is enabled by default and can be disabled with `JsonSchemaReaderFactoryBuilder.withSpecVersionDetection()`.
- `JsonSchemaReaderFactoryBuilder.withSchemaValidation()` to enable or disable the validation of schemas against the metaschemas. By default this option is enabled as before.
- `ValidationConfig` interface to build configuration properties, which can be passed to `JsonParserFactory` or `JsonReaderFactory` type.

### Changed
- `type` parameters in `JsonSchema.createEvaluator()` and `JsonSchema.createNegatedEvaluator()` now receive `InstanceType.NUMBER` instead of `InstanceType.INTEGER` for integer type.
- `FormatAttribute.valueType()` now must return `InstanceType.NUMBER` instead of `InstanceType.INTEGER` for integer type.

### Removed
- `JsonValidatorFactoryBuilder` type introduced in the previous release. This type is superseded by new `ValidationConfig`.

### Fixed
- A bug of `oneOf` which was causing the validation to produce false result in case that two or more subschemas are evaluated eventually as valid  by different parser events. (Issue #13)   

## 0.15.0 - 2019-03-31
### Added
- New `JsonValidatorFactoryBuilder` interface for building configured instances of `JsonParserFactory` or `JsonReaderFactory`.
- Support of `default` keyword in the validations. The missing properties and/or items in the source instances are filled with the default values provided by the keyword. Both `JsonParser` and `JsonReader` support this feature. `JsonParser` produces additional events caused by the default values and `JsonReader` expands objects and arrays with the additional values. This feature is disabled by default and can be enabled explicitly by `withDefaultValues()` in `JsonValidatorFactoryBuilder`.

### Fixed
- The bug which was causing `getArrayStream()`, `getObjectStream()`, and `getValueStream()` in `JsonParser` to throw wrongly `UnsupportedOperationException` in the case that they should throw `IllegalStateException`. (Issue #10)

## 0.14.0 - 2019-03-18
### Added
- `getPointer()` method in `Problem` interface which provides the location of the problem in the JSON instance as a JSON pointer. (Proposed originally by @mshaposhnik and the implementation is supported by @erdi)
- `ProblemPrinterBuilder` interface to build configured problem printers, such as printers with problem locations omitted.
- `JsonValidationService.createProblemPrinterBuilder()` to create instances of `ProblemPrinterBuilder`.

### Changed
- New problem printer outputs the messages including JSON pointers in addition to line and column numbers by default.
- The evaluation of `uniqueItems` keyword is now deferred until the end of the array.
- The evaluation of false boolean schema for `properties`, `patternProperties`, and `additionalProperties` keywords is now deferred until the value of the property.

### Fixed
- Fix broken Javadoc links to the JDK.

## 0.13.0 - 2019-02-27
### Added
- `JsonSchemaReaderFactoryBuilder.withCustomFormatAttributes()` method for deactivating custom format attributes.
- [CLI] Options to resolve remote schema references. (Issue #4 proposed by @kerrykimbrough)

### Changed
- [CLI] Revised completely with new syntax.

## 0.12.0 - 2019-02-08
### Added
- `JsonSchema.containsKeyword()` which tests whether a schema contains the specified keyword or not.
- `JsonSchema.defaultValue()` which returns the value of the `default` keyword.
- `JsonSchema.hasAbsoluteId()` which tests whether a schema has an `$id` whose  value is an absolute URI.
- `JsonSchema.getInPlaceSubschemas()` which returns the subschemas which will be applied to the same instance location as the owning schema.
- Detection of infinite recursive looping while reading schemas.

### Changed
- `JsonSchema.subschemas()` and `subschemaAt()` were renamed to `getSubschemas()` and `getSubschemaAt()`, respectively.

### Fixed
- A bug which was causing `JsonSchemaReader` to throw a `ClassCastException` when the schema has both `$id` and `$ref` keywords. (Issue #2 reported by @avstp)
- A bug which was causing the evaluation result of `if`/`then`/`else` keywords to be ignored when the instance is a JSON object or a JSON array.

## 0.11.0 - 2019-01-26
### Added
- `comment()`, `title()` and `description()` methods to `JsonSchema` interface
  to obtain the keyword value respectively.
- `JsonSchemaReaderFactory` and `JsonSchemaReaderFactoryBuilder` as API interfaces.
- `JsonSchemaReaderFactoryBuilder.withStrictWithKeywords()`.
  With this option enabled, the schema reader reports a problem when it
  encounters an unrecognized schema keyword.
- `JsonSchemaReaderFactoryBuilder.withStrictWithFormats()`.
  With this option enabled, the schema reader reports a problem when it
  encounters an unrecognized format attribute.
- [CLI] `-strict` option which forces the schema validator to report
  a problem when it finds an unrecognized keyword or format attribute.
  This is useful for detecting misspelled keywords and format attributes.

### Changed
- Move `withSchemaResolver()` method in `JsonSchemaReader` to `JsonSchemaReaderFactoryBuilder` interface.
- `BranchProblem` interface was merged with `Problem` interface and removed.

### Fixed
- A bug which was causing JSON schemas to be valid even if they are unclosed JSON objects.

## 0.10.0 - 2019-01-02
### Added
- Support of `contentEncoding` and `contentMediaType` keywords.

### Changed
- Rename `schemaId()` to `schema()` in `JsonSchema`.
- Correct `withItem()` to `withItems()` in `JsonSchemaBuilder`.

### Fixed
- A bug which was causing `definitions`/`dependencies`/`patternProperties`/`properties` to disappear from the read schema when they are empty.

## 0.9.1 - 2018-12-22
### Fixed
- A problem in Java 9/10.

## 0.9.0 - 2018-12-10
### Added
- First release to the Maven Central Repository.
