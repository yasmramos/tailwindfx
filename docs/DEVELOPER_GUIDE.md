# TailwindFX Developer Guide

This guide is for people who contribute to TailwindFX itself: how the code is organised, how a
utility class becomes a JavaFX style, how to add new utilities, and how the test suite is wired.

For end-user documentation, start with the [documentation index](README.md).

## 1. Architecture overview

### Modules

TailwindFX is a Maven multi-module project. The root `pom.xml` builds three core modules; examples
and benchmarks live behind opt-in profiles.

| Module | Purpose |
|--------|---------|
| `tailwindfx-base` | Core engine: facades (`TwStyle`, `TwLayout`, `TwTheme`, ...), token parsing/classification, JIT compilation, theming, responsive/breakpoint support, metrics, i18n |
| `tailwindfx-components` | Pre-built JavaFX controls (`TwButton`, `TwCard`, `TwDataTable`, ...) built on top of `tailwindfx-base` |
| `tailwindfx-maven-plugin` | Build-time CSS generation (`tailwindfx:generate`), scans sources and emits an optimized stylesheet |
| `tailwindfx-benchmarks` | JMH benchmarks, built only with `-P benchmarks` |
| `examples/*` | Runnable example applications, built only with `-P examples` |

### Layers inside `tailwindfx-base`

The package layout reflects a single direction of dependency:

```
io.github.yasmramos.tailwindfx            ← public facades (Tw* entry points)
├── core/          ← parsing, classification, JIT compilation, caching
├── style/         ← value resolution, property mapping, style merging/appliers
├── theme/         ← ThemeConfig, ThemeManager, CSS generation
├── layout/        ← container-aware layout panes and appliers
├── animation/     ← TwAnimation timelines and applier
├── effect/        ← filter effects
├── responsive/    ← container queries and breakpoints
├── breakpoint/    ← breakpoint manager
├── color/         ← color palette
├── metrics/       ← performance metrics
└── i18n/          ← internationalisation
```

Public facades are thin: they validate input and delegate to the packages above. For example
`TwStyle.apply` delegates to `TokenParser` (classification), then to `EffectApplier`,
`UtilityConflictResolver`, `LayoutApplier`, `VariantManager`, `StyleMerger` and `JitCompiler`
depending on the token categories found.

### The `apply()` pipeline

When a node is styled, tokens flow through the following stages (the ordering is intentional and
documented on `TwStyle`):

1. **Parse & classify** — `TokenParser.parse(...)` splits the varargs on whitespace and sorts each
   token into buckets: effects, animations, layout-dependent, layout-migration, CSS classes,
   variants, JIT tokens, and unknown tokens.
2. **Effects** — `EffectApplier` handles filter tokens (`blur-*`, `grayscale`, ...).
3. **Animations** — `AnimationApplier` plays `animate-*` tokens as `TwAnimation` timelines, since
   JavaFX has no CSS animation engine.
4. **CSS classes** — `UtilityConflictResolver` resolves conflicts and adds the surviving classes;
   `ProgrammaticStyleApplier` applies the utilities that have no CSS equivalent (visibility,
   overflow, z-order, fill sizing) through the JavaFX API.
5. **Layout-dependent styles** — `LayoutApplier` handles margins, gaps and flex properties that need
   parent context.
6. **Variants** — `VariantManager` registers hover/focus/dark states (must run before JIT so that
   variant styles are not overwritten by inline styles).
7. **JIT compilation** — dynamic tokens are compiled to inline styles via `StyleMerger` +
   `JitCompiler`, or resolved from the AOT stylesheet when `TwConfig.preferStylesheet(true)`.

## 2. How the JIT compiler works

The JIT compiler turns a Tailwind token into JavaFX CSS. The work is split across small,
single-responsibility classes so each can be tested in isolation:

| Class | Responsibility |
|-------|----------------|
| `TokenParser` | Tokenize and classify tokens into buckets |
| `TokenRegistry` | Single source of truth for token classification (JIT prefixes, layout-dependent, effects, animations, known utilities) |
| `StyleToken` | Parsed representation of a token (prefix, scale, color name/shade, arbitrary value, sign) |
| `StyleResolver` | Convert a `StyleToken` into a concrete CSS **value** (`p-4` → `16px`) |
| `CssPropertyMapper` | Map a prefix to a `-fx-*` **property** (`p` → `-fx-padding`) |
| `JitCompiler` | Orchestrate resolve + map, plus caching, logging and metrics |

Example: `bg-blue-500/80`

```
StyleToken(prefix=bg, colorName=blue, shade=500, opacity=0.80)
        │
        ├─ CssPropertyMapper.mapToCssProperty("bg")  → "-fx-background-color"
        └─ StyleResolver.resolve(token)              → "rgba(59,130,246,0.80)"
                          ↓
              "-fx-background-color: rgba(59,130,246,0.80);"
```

### Caching

Compiled tokens are stored in a bounded, thread-safe `LruCache` (default 2000 entries; see
`JitCompiler.MAX_CACHE_SIZE`). Compiling `p-4` a thousand times costs the same as compiling it once.
The cache is keyed by token **and** by the active `ThemeConfig`, so switching themes does not return
stale values. `StyleCache` provides the same idea for per-node class resolution, with targeted
invalidation (`TwStyle.invalidateCache`, `TwStyle.invalidateCategoryCache`).

### Special modifiers

- **Important**: a trailing `!` (for example `p-4!`) marks the property as important.
- **Dark mode**: a `dark:` prefix (for example `dark:bg-gray-800`).
- **Responsive**: `md:`, `lg:` and similar breakpoint prefixes.
- **Arbitrary values**: square brackets, for example `w-[320px]` or `bg-[#38bdf8]`.

### Unknown tokens

`JitCompiler` uses a heuristic for tokens it cannot resolve:

- Tokens that look like a JIT utility (they contain digits, `/` or `[`) are logged as a warning.
- Tokens that look like an intentional CSS class (`btn-primary`, `card`) are added silently as a
  style class.

`JitCompiler.setDebug(true)` logs **all** tokens, which is useful when a class appears to be ignored.

### AOT vs JIT

At build time the Maven plugin (`tailwindfx:generate`) scans `TwStyle.apply(...)`,
`getStyleClass().add(...)`, `setStyleClass(...)` and FXML `styleClass="..."` attributes, and emits
an optimized stylesheet containing only the utilities that are actually used. At runtime,
`TwConfig.preferStylesheet(true)` makes the engine look up classes in that stylesheet first and fall
back to inline JIT compilation for dynamic or arbitrary values.

## 3. Adding a new utility

Adding a utility such as `gap-6` or `bg-<color>-<shade>` touches a small, predictable set of files.

### Step 1 — Classify the token (`TokenRegistry`)

Decide which bucket the token belongs to and register its prefix:

- **JIT prefix** (resolved at runtime from a value) → add to the JIT prefix set.
- **Layout-dependent** (needs parent context, e.g. `gap-*`) → register as layout-dependent.
- **Effect** (`blur-*`, `grayscale`, ...) → register as an effect token.
- **Animation** (`animate-*`) → register as an animation token.
- **Static known utility** (`rounded-lg`) → register as a known utility.
- **Layout migration** (`flex`, `grid` on a plain node) → register as requiring migration.

`TokenParser` consults `TokenRegistry`, so most new tokens need no change in the parser itself.

### Step 2 — Map the property (`CssPropertyMapper`)

Add a `case` for the prefix in `mapToCssProperty`, returning the `-fx-*` property. Return `null`
when JavaFX has no CSS equivalent (as is done today for margins and inset offsets, which are applied
programmatically instead).

### Step 3 — Resolve the value (`StyleResolver`)

Add or extend the relevant `resolve*` branch so the token produces a concrete value. Values are
looked up in `ThemeConfig` where possible (`spacing()`, `color()`, `borderRadius()`, ...) rather than
hard-coded, so custom themes keep working.

### Step 4 — Apply programmatically if needed

If the utility has no CSS property (for example visibility or overflow), handle it in
`ProgrammaticStyleApplier` or `LayoutApplier` instead of emitting an inline style.

### Step 5 — Catalog and documentation

- Add the family to `TwCatalog` so it appears in `TwCatalog.allUtilityClasses()` and in the
  runtime-generated full CSS.
- Update the user-facing docs (root `README.md` and/or the relevant `docs/Tw*.md` page).

### Step 6 — Tests

Add unit tests next to the existing ones (`TokenRegistryTest`, `StyleResolverTest`,
`CssPropertyMapperTest`, `JitCompilerTest`, `TokenParserTest`). See the next section for conventions.

## 4. Testing guidelines

### Test tiers and naming

Tests are split by **suffix**, and Maven runs them in different phases:

| Suffix | Runner | Phase | Use for |
|--------|--------|-------|---------|
| `*Test.java` | Surefire | `test` | Pure unit tests (no toolkit needed) and headless JavaFX tests |
| `*IT.java` | Failsafe | `integration-test` / `verify` | Integration and UI tests that exercise the full pipeline |

Run everything with:

```bash
mvn -q verify
```

Run a single module or class:

```bash
mvn -q -pl tailwindfx-base test
mvn -q -pl tailwindfx-base test -Dtest=JitCompilerTest
```

### Headless JavaFX

Tests run headless with Monocle; no display or Xvfb is required. The configuration lives in
`tailwindfx-base/pom.xml` and `tailwindfx-components/pom.xml` (surefire/failsafe `argLine` and
`systemPropertyVariables`):

- `glass.platform=Monocle`, `monocle.platform=Headless`, `prism.order=sw`.
- A `--patch-module javafx.graphics=<openjfx-monocle.jar>` argument makes the Monocle platform
  factory resolvable on JDKs that bundle JavaFX (for example Liberica Full). On plain JDKs the JVM
  logs `Unknown module: javafx.graphics` and ignores the patch.

### Instantiating JavaFX controls in tests

Plain unit tests can create JavaFX controls without extending a TestFX base class:

- `JavaFxToolkitExtension` (a JUnit 5 `BeforeAllCallback`) starts the toolkit before each test class.
  It is auto-registered through `junit-platform.properties` (`junit.jupiter.extensions.autodetection.enabled=true`).
- `ToolkitBootstrap.ensureStarted()` is the underlying helper; it starts the platform once per JVM
  and keeps the FX threads alive.

For UI-level tests, use TestFX (`ApplicationExtension`, `FxAssert.verifyThat`, ...) in an `*IT` class;
see `tailwindfx-components` for examples.

### Guidelines

- **Test real code paths.** Prefer asserting on the actual compiled styles, style classes and node
  state over mocks.
- **Isolate global state.** `TwConfig`, `ThemeConfig`, `JitCompiler` cache and the metrics singleton
  are global. Reset them in `@BeforeEach`/`@AfterEach` (see existing tests that call
  `ThemeConfig.reset()`, `TwConfig.reset()`, `JitCompiler.clearCache()`).
- **Cover both the happy path and edge cases**: arbitrary values, negative values, opacity modifiers,
  variants, and unknown tokens.
- **Keep unit tests free of the toolkit** when the code under test does not touch JavaFX; this keeps
  the fast `*Test` tier fast.

## 5. Extension points

- **Custom theme** — build a `ThemeConfig` with `ThemeConfig.Builder` and pass it where the API
  accepts one; the JIT cache is keyed by theme, so switching is safe.
- **Custom components** — extend `tailwindfx-components` or compose `Region`/`Pane` subclasses and
  style them with `TwStyle`.
- **FXML** — use `TwFXML` and the Maven plugin's FXML scanning to keep stylesheet generation in sync
  with `styleClass` attributes.
- **Internationalisation** — `TwI18n` provides `get`, `createBinding` and `bind` for localising
  labels; base names and locales are configurable at runtime.

## 6. Build and release notes

- The project targets **Java 17** and **Maven 3.8.7+**; the enforcer plugin fails the build
  otherwise. JavaFX is always the `org.openjfx` 17 artifacts — JavaFX from the JDK is banned.
- Versions are CI-friendly: `${revision}` plus `flatten-maven-plugin`.
- Examples and benchmarks are excluded from publication via profiles; keep new example modules in
  the `examples` profile.

## License

Apache License 2.0 — see the `LICENSE` file for details.
