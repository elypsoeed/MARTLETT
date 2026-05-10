# Development Guidelines

This document describes the baseline expectations for changes in this project.

## Service Design

Service classes must not depend on other service classes.

If a feature requires orchestration of multiple services or combines several steps into one use case, move that coordination into a separate `ComplexService` class.

Use regular service classes for focused, atomic responsibilities.

## Tests

Any new functionality must be covered by tests.

Tests in this project are intentionally uniform. When adding new tests, use any existing test as a formatting and structure reference.

Do not merge new behavior without test coverage.

## Naming

Classes and methods must be named by the task they solve, not by the way they are internally structured.

Prefer names that describe business intent or use-case meaning.

Avoid names based on implementation mechanics, technical shape, or internal decomposition when a domain-oriented name is possible.

## Environment And Secrets

Environment-specific secrets and sensitive runtime configuration must be managed through Vault.

Do not commit secrets to the repository.

Do not place real secret values directly into `application.yaml` or other tracked configuration files.

When adding new sensitive configuration, wire it through Vault in the same way as the existing secret-backed settings.
