# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased
### Fixed
- Fix the error handling of unclosed schemas.

## 0.10.0 - 2019-01-02
### Added
- Add support of contentEncoding and contentMediaType.

### Changed
- JsonSchema#schemaId() is renamed to schema().
- JsonSchemaBuilder#withItem() is renamed to withItems().

### Fixed
- Fix a bug that empty definitions/dependencies/patternProperties/properties was missing from read schema.

## 0.9.1 - 2018-12-22
### Fixed
- Fix problem in Java 9/10.

## 0.9.0 - 2018-12-10
### Added
- First release to the Maven Central Repository.
