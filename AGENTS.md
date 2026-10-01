# Project Guidelines

## Language Policy (Strict)

- **All source code must be written in English**: identifiers, class/method names, variables.
- **All code comments and Javadoc must be in English.**
- **All documentation** (README, CONTRIBUTING, CHANGELOG, docs/, commit messages, PR titles/descriptions) **must be in English.**
- Never introduce Spanish (or any other language) into code, comments, or documentation.

## Git Configuration

- Author: `yasmramos` <yasmramos95@gmail.com> (already set via `git config user.name` / `user.email`).
- Remote: `origin` -> `https://github.com/yasmramos/tailwindfx` (authenticated with a GitHub personal access token).
- Working branch: **`develop`** (tracks `origin/develop`). Feature work should branch off `develop` and be merged back into it.

## Commit Convention

Every commit **must** follow the Conventional Commits format, written in English:

```
<type>(<scope>): <subject>

<body>

<footer>
```

- Types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `chore`, `revert`.
- Subject: imperative mood ("add" not "added"), lowercase first letter, no trailing period, max 72 chars.
- Body: explain WHAT and WHY (not HOW); wrap at 72 chars.
- Footer: issue references (`Closes #123`), breaking changes (`BREAKING CHANGE: ...`).
- A template is registered locally via `git config commit.template .gitmessage`.

Example:

```
feat(core): add responsive breakpoint utilities

Introduce helpers to query Tailwind-style breakpoints at runtime so
components can adapt their layout dynamically.

Closes #42
```

## Build & Test

- This is a Maven multi-module Java project (`pom.xml` at the root).
- Run `mvn -q verify` (or `mvn -q -pl <module> test`) before committing; do not commit failing builds.

## Code Style

- Follow the existing style of each module (Java, modular codebase under `tailwindfx-*`).
- Public API requires Javadoc in English.
- Keep diffs focused: one logical change per commit.
