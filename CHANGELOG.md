# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased
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
