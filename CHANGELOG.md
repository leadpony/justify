# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased
### Added
- getPointer() method in Problem interface which provides the problem location as a JSON pointer.

### Changed
- Problem printer outputs the messages including JSON pointers in addition to line and column numbers by default.
- The evaluation of "uniqueItems" keyword is now deferred until the end of the array.
- The evaluation of false boolean schema for "properties", "patternProperties", "additionalProperties" keywords is now deferred until the value of the property.

## 0.13.0 - 2019-02-27
### Added
- JsonSchemaReaderFactoryBuilder#withCustomFormatAttributes() method for deactivating custom format attributes.
- [CLI] Options to resolve remote schema references. (Issue #4 proposed by @kerrykimbrough)

### Changed
- [CLI] Revised completely with new syntax.

## 0.12.0 - 2019-02-08
### Added
- JsonSchema#containsKeyword() which tests whether a schema contains the specified keyword or not.
- JsonSchema#defaultValue() which returns the value of the "default" keyword.
- JsonSchema#hasAbsoluteId() which tests whether a schema has an "$id" whose  value is an absolute URI.
- JsonSchema#getInPlaceSubschemas() which returns the subschemas which will be applied to the same instance location as the owning schema.
- Detection of infinite recursive looping while reading schemas.

### Changed
- JsonSchema#subschemas() and subschemaAt() were renamed to getSubschemas() and getSubschemaAt(), respectively.

### Fixed
- A bug which was causing JsonSchemaReader to throw a ClassCastException when the schema has both "$id" and "$ref" keywords. (Issue #2 reported by @avstp)
- A bug which was causing the evaluation result of "if"/"then"/"else" keywords to be ignored when the instance is a JSON object or a JSON array.

## 0.11.0 - 2019-01-26
### Added
- comment(), title() and description() methods to JsonSchema interface
  to obtain the keyword value respectively.
- JsonSchemaReaderFactory and JsonSchemaReaderFactoryBuilder as API interfaces.
- JsonSchemaReaderFactoryBuilder#withStrictWithKeywords().
  With this option enabled, the schema reader reports a problem when it
  encounters an unrecognized schema keyword.
- JsonSchemaReaderFactoryBuilder#withStrictWithFormats().
  With this option enabled, the schema reader reports a problem when it
  encounters an unrecognized format attribute.
- [CLI] -strict option which forces the schema validator to report
  a problem when it finds an unrecognized keyword or format attribute.
  This is useful for detecting misspelled keywords and format attributes.

### Changed
- Move withSchemaResolver() method in JsonSchemaReader to JsonSchemaReaderFactoryBuilder interface.
- BranchProblem interface was merged with Problem interface and removed.

### Fixed
- A bug which was causing JSON schemas to be valid even if they are unclosed JSON objects.

## 0.10.0 - 2019-01-02
### Added
- Support of contentEncoding and contentMediaType keywords.

### Changed
- Rename schemaId() to schema() in JsonSchema.
- Rename withItem() to withItems() in JsonSchemaBuilder.

### Fixed
- A bug which was causing definitions/dependencies/patternProperties/properties to disappear from the read schema when they are empty.

## 0.9.1 - 2018-12-22
### Fixed
- A problem in Java 9/10.

## 0.9.0 - 2018-12-10
### Added
- First release to the Maven Central Repository.
