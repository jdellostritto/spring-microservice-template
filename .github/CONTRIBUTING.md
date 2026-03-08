# Contributing

Thank you for contributing to the Spring Microservice Template!

## Getting Started

1. Fork the repository
2. Clone your fork
3. Create a feature branch: `git checkout -b feat/your-feature`
4. See the [Quick Start](../README.md#quick-start) section in the README for development setup

## Development Workflow

1. Run tests before submitting: `make test`
2. Check code quality: `make sonar`
3. Ensure coverage is maintained
4. Push to your fork and create a Pull Request

## Commit Guidelines

Use conventional commits with co-author support:

```
feat(kafka): add DepartingEvent to event bus

Extend the kafka-producer module with a DepartingEvent proto
message and publish it from the departing request pipeline.

Co-authored-by: Jane Smith <jane@example.com>
```

Set up the commit template:
```bash
git config commit.template .github/commit-msg-template
```

Type conventions:
- `feat` — New feature
- `fix` — Bug fix
- `docs` — Documentation changes
- `test` — Test additions/changes
- `refactor` — Code refactoring
- `perf` — Performance improvements
- `chore` — Build/dependency updates

Scope examples: `kafka`, `api`, `metrics`, `probes`, `infra`, `docs`

## Pull Request Process

1. Update `README.md` and relevant `docs/` files if needed
2. Ensure CI passes (build + Sonar quality gate)
3. Request review from maintainers
4. Address feedback and rebase if needed

## Code Quality Standards

- All tests must pass: `make test`
- SonarCloud quality gate must pass
- New modules require unit tests
- Event bus changes require updating `docs/EVENT-BUS.md`

## Project Conventions

Each convention has a dedicated doc — read the relevant one before making changes:

| Area | Doc |
|---|---|
| API versioning | [docs/API-VERSIONING.md](../docs/API-VERSIONING.md) |
| URI design | [docs/URI-CONVENTIONS.md](../docs/URI-CONVENTIONS.md) |
| Package structure | [docs/PACKAGE-STRUCTURE.md](../docs/PACKAGE-STRUCTURE.md) |
| Logging | [docs/LOGGING.md](../docs/LOGGING.md) |
| Metrics | [docs/CUSTOM_METRICS.md](../docs/CUSTOM_METRICS.md) |
| Deprecation | [docs/DEPRECATION.md](../docs/DEPRECATION.md) |
| Kubernetes probes | [docs/KUBERNETES-PROBES.md](../docs/KUBERNETES-PROBES.md) |
| Event bus | [docs/EVENT-BUS.md](../docs/EVENT-BUS.md) |
| CI/CD workflows | [docs/GITHUB_ACTIONS_SETUP.md](../docs/GITHUB_ACTIONS_SETUP.md) |

## Questions?

Open an issue or start a discussion in the repository.
