# Project Guidelines

## Language Policy (Strict)

- **All source code must be written in English**: identifiers, class/method names, variables.
- **All code comments and Javadoc must be in English.**
- **All documentation** (README, CONTRIBUTING, CHANGELOG, docs/, commit messages, PR titles/descriptions) **must be in English.**
- Never introduce Spanish (or any other language) into code, comments, or documentation.

## Git Configuration

- Author: `yasmramos` <yasmramos95@gmail.com> (already set via `git config user.name` / `user.email`).
- **Never add AI attribution to commits.** Do not add `Co-authored-by: openhands`,
  `Generated with ...`, or any similar trailer. Commits must look like they were
  authored solely by `yasmramos`.
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
  Never add an AI co-author or "generated with" trailer.
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

## Architecture Notes

TailwindFX compiles Tailwind-style utility tokens to JavaFX at runtime. Two distinct
paths exist and they are easy to confuse:

1. **CSS classes** — `TwCatalog` advertises every utility as a CSS class, and
   `TwCatalog.generateFullCss()` writes a stylesheet. But the generated CSS only emits
   the `.root` theme block (via `ThemeCssGenerator`); it does **not** emit a rule per
   utility. So a token that only maps to a `cssClass` and has no inline style is a
   **silent no-op** unless something else handles it.
2. **Inline styles / programmatic application** — `JitCompiler.compileBatch` runs the
   generic compiler plus specialized processors (`RingProcessor`, `GradientProcessor`,
   `AspectRatioProcessor`, `ScrollSnapProcessor`, ...), and `Styles` /
   `ProgrammaticStyleApplier` map tokens onto the JavaFX API.

When adding or fixing a utility, verify which path actually applies it:

- Specialized processors only run when the generic compiler produced no inline CSS;
  a token that the generic compiler reports as "known" (falling back to a `cssClass`)
  will otherwise never reach the specialized processor.
- Utilities with no JavaFX `-fx-*` equivalent (visibility, `overflow-*`, `z-*`,
  `aspect-*`, fill sizing, `snap-*`) must be applied programmatically via `Styles` and
  `ProgrammaticStyleApplier`; they cannot work through the stylesheet alone.
- `TwStyle.apply` is the runtime entry point that wires the programmatic path.

### Locale and number formatting

Any CSS produced with `String.format`/`NumberFormat` must pin `Locale.ROOT`. Under a
comma-decimal locale (`de_DE`, `fr_FR`, ...) the default locale emits invalid CSS such
as `rgba(59,130,246,0,80)`. Existing helpers already do this in `StyleResolver`; follow
that convention in `ColorPalette`, `GradientProcessor`, `RingProcessor`, etc.

### Testing

- Run `mvn verify` before committing (all modules). `mvn -o` may fail offline when a
  build plugin is not yet in the local repository; drop `-o` to allow downloads.
- Tests run on JUnit 5 + TestFX + Mockito; the JavaFX toolkit is initialised by the
  test harness, so tests may construct `Node`/`Region`/`Scene` directly.
- Regression tests for utility behaviour live next to the processor or in
  `style/ProgrammaticStyleApplierTest`. Prefer asserting real code paths, not mocks.
