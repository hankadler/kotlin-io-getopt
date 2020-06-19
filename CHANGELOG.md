# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
## [0.3.0] - 2020-06-19
### Changed
Significant Rewrite. Changes include:
 - Throwing GetOptError when invalid options are given or required option values are not given
### Fixed
 - opts and args are parsed correctly, regardless of their position

## [0.2.0] - 2020-06-18
### Changed
 - Switched order of output pair of getopt(), now options-arguments pair is obtained, like the original C-style parser

## [0.1.0] - 2020-06-17
### Added
 - .gitignore
 - .gradle/
 - CHANGELOG.md
 - LICENSE
 - README.md
 - build.gradle
 - gradle.properties
 - gradle/
 - gradlew
 - gradlew.bat
 - settings.gradle
 - src/

[Unreleased]: https://github.com/hankadler/kotlin-io-getopt/compare/v0.3.0...HEAD
[0.3.0]: https://github.com/hankadler/kotlin-io-getopt/compare/v0.2.0...0.3.0
[0.2.0]: https://github.com/hankadler/kotlin-io-getopt/compare/v0.1.0...0.2.0
[0.1.0]: https://github.com/hankadler/kotlin-io-getopt/releases/tag/v0.1.0
