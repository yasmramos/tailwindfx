# TailwindFX Changelog

All notable changes to this project are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [1.0-SNAPSHOT] - Unreleased

### Added
- **Comprehensive Javadoc for `FxAnimation`** — Complete API documentation with examples, parameter descriptions, and usage guidelines (PR #69)
- **Tailwind v4.1 CSS utilities** — `overflow-wrap`, `word-break`, `whitespace` classes
- **SVG utilities** — `.fill-*`, `.stroke-*`, `.stroke-width-*`, `.stroke-dashed`, `.stroke-dotted`
- **Text-shadow** — `.text-shadow-sm/md/lg/xl/2xl` + colored variants (`.text-shadow-blue`, etc.)
- **Component presets** — `.card`, `.card-dark`, `.card-elevated`, `.badge-*`, `.modal-*`, `.drawer`, `.tooltip-*`, `.glass`, `.neumorph`
- **`FxDataTable<T>`** — declarative, sortable, filterable, paginated TableView wrapper
- **`Styles.textShadow*()`** — Java API for text-shadow (DropShadow on Text/Label)
- **`Styles.dropShadow()`** — colored drop-shadow with arbitrary color + alpha
- **`Styles.clipCircle/Rounded/Mask()`** — clip and mask utilities
- **`Styles.rotateX/Y()`**, **`Styles.translateZ()`**, **`Styles.perspective()`** — 3D transforms
- **`Styles.glass/glassDark/neumorph/neumorphInset()`** — effect presets
- **`Styles.setReducedMotion/shouldAnimate/playIfMotionOk()`** — motion-reduce support
- **`Styles.fill/stroke/strokeWidth()`** — SVG shape helpers
- **`TailwindFXMetrics` alert system** — `onAlert()`, `alertOnLowCacheHitRatio()`, `alertOnHighConflictRate()`, `alertOnSlowCompile()`
- **`JitCompiler`** — `drop-shadow-[#hex]`, `text-shadow-[rgba]`, `stroke-[n]`, `fill-[#hex]`, `aspect-ratio-[w/h]` arbitrary tokens
- **`FxDataTable` CSS** — `.table-striped`, `.table-hover`, `.table-compact`, `.table-bordered`, `.table-dark`, `.search-field`, `.row-even/odd`
- **`UtilityConflictResolver`** — v4.1 categories: `text-shadow`, `drop-shadow`, `fill`, `stroke`, `clip`, `break`, `skew-x/y`, `aspect`, `rotate-x/y`, `translate-z`, component categories
- **`FxFlexPane.computePrefHeight`** — correct wrap=true height computation
- **`FxFlexPane.flex-basis`** — `setBasis/getBasis()`
- **`FxFlexPane.setDirectionAnimated()`** — animated direction change
- **`TailwindFX.dataTable()`**, **`flexBasis()`**, **`flexDirection()`** — new entry-point shortcuts
- **Module descriptor** — `module-info.java` for modular projects
- **Package Javadoc** — `package-info.java` with class overview
- **Build file** — `pom.xml` (Maven, JavaFX 21, shade plugin)
- **Comprehensive example** — `TailwindFXExample` updated to demo all major features
- **`ResponsiveNode`** — per-node responsive utility rules driven by `Scene.widthProperty()`
- **`FxFlexPane`** — full Flexbox model: direction, wrap, justify-content (6), align-items (4), gap, flex-grow, flex-shrink, order, align-self
- **`StylePerf`** — StyleDiff cache (skip redundant applies), batch apply, auto-batch threshold
- **`TailwindFXMetrics`** — AtomicLong counters for cache hits/misses, compilations, conflicts, themes, animations, layout passes
- **`BreakpointManager`** — responsive-aware category detection (`md:w-4` scoped to `md:w` category)
- **`ThemeScopeManager`** — `findClosestScope()`, `inheritScope()`, `refreshScope()`
- **`AnimationUtil`** — `removeHoverEffects()`, auto-cancel on scene detach, `storeHoverHandlers()`
- **`JitCompiler`** — LRU eviction at `MAX_CACHE_SIZE=2000`, thread-safe `synchronized(CACHE)`
- **`UtilityConflictResolver`** — `cleanupNode()`, `autoCleanup()`, `invalidateCategoryCache()`
- **`TailwindFX.Config`** — `autoBatch(threshold)` configuration
- Specific imports replacing wildcards across all 13 files
- **`TailwindFX.aspectRatio/Square()`** — Java equivalent of CSS `aspect-ratio`
- **`TailwindFX.backdropBlur*()`** — BoxBlur glassmorphism helpers
- **`TailwindFX.transition()`** — CSS `transition` equivalent via Timeline
- **`TailwindFX.cleanupNode/autoCleanup()`** — cache cleanup + memory management
- **`TailwindFX.applyDiff()`** — StyleDiff entry-point shortcut
- **`TailwindFX.batch/batchAsync()`** — batch apply entry-point
- **`FxFlexPane.setBasis/getShrink/setOrder/setAlignSelf()`** — flex parity features
- **`FxFlexPane.ensureLayoutOnParent()`** — deferred `requestLayout()` for pre-parenting
- **`TailwindFXMetrics.recordLayoutPass()`** — layout timing for FxFlexPane
- **`TailwindFX.metrics()`** — entry-point access to `TailwindFXMetrics`
- **`FxGridPane`** — grid-template-areas, auto-flow, masonry layout
- **`ComponentFactory`** — high-level card, badge, modal, drawer, tooltip, datatable builders
- **`FxLayout` TilePane** — tile layout support in FxLayout builder
- **`Styles.java`** — skewX/Y, objectCover/Contain, imgSize, 40+ grid/margin/filter methods
- **`JitCompiler`** — gradient parsing, hex validation, negative translate, arbitrary values
- **`ThemeManager`** — save/load/delete theme persistence via `java.util.prefs.Preferences`
- **`ThemeScopeManager`** — scoped themes for any Pane subtree
- **`AnimationUtil`** — 14 animations, FxAnimation fluent API, AnimationRegistry, ResponsiveAnimationGuard
- **`TailwindFX.Config`** — unit, breakpoints, debug, warnOnParent
- **`TailwindFX.watch()`** — hot-reload CSS in development
- **`TailwindFX.debugReport()`** — runtime node inspection
- CSS: 11 900+ lines, 1 400+ utility classes
- CSS utility framework: layout, typography, colors, borders, effects, transforms
- `TailwindFX.install()`, `apply()`, `jit()`, `jitApply()`
- `ColorPalette` — 209 Tailwind colors
- `BreakpointManager` — SM/MD/LG/XL/XXL breakpoints
- `ThemeManager` — dark/light/blue/green/purple/rose/slate presets

### Changed
- **Java 17 Migration** — Project migrated from Java 21 to Java 17 for broader compatibility
- **`FxAnimation` as Public API** — Extracted `FxAnimation` as public class, deprecated `AnimationUtil`
- **Unified `apply()` method** — Enhanced to auto-detect CSS classes and JIT tokens
- **Examples refactored** — Moved to separate `tailwindfx-examples` Maven project
- **TestFX integration** — Comprehensive headless testing with Monocle and xvfb
- **Documentation overhaul** — Added CONTRIBUTING.md, CODE_OF_CONDUCT.md, updated README
- **MIT License** — Added LICENSE file with 2026 copyright

### Fixed
- `JitCompiler` regex patterns to prevent false positives in arbitrary value parsing
- `FxFlexPane.computePrefHeight` returned wrong height for `wrap=true` rows
- `UtilityConflictResolver` — skew, aspect, 3D transform categories were missing, causing classes to accumulate instead of replace
- Spanish strings remaining in log messages (complete sweep)
- Hover effects (`onHoverScale/Lift/Dim`) used `setOnMouseEntered` (overwrites) → replaced with `addEventHandler` (chains)
- `JitCompiler` alpha clamp: was warning-only, now actually clamps to `[0, 100]`
- `UtilityConflictResolver.invalidateCache(null)` — added null safety
- All log/exception/comment strings normalized to English
- Wildcard imports replaced with specific imports in all Java files
- TestFX tests fixed for JavaFX 17 compatibility (disabled module-info)
- Avatar component test updated for Java 17
- Documentation typos and grammatical errors corrected
- README updated to reflect Java 17 requirement

### Removed
- **`AnimationUtil.java`** — Removed dead code, functionality fully migrated to `FxAnimation`
- **Deprecated JIT methods** — Cleaned up legacy API surface
- **Old documentation files** — Removed outdated docs, keeping `docs/` folder empty for comprehensive rewrite
- **Preview features** — Removed Java 21 preview features for Java 17 compatibility
- **`TailwindFXExample.java`** — Moved to examples module
- **Obsolete test suites** — Removed deprecated ComponentFactory, TailwindFXMain, and ThemeManagerRefresh tests

### Documentation
- **`FxAnimation`** — comprehensive JavaDoc added to all methods with detailed descriptions, parameters, return values, and usage examples

### Tests added
- `FxDataTableTest` — 21 tests covering builder, filter, pagination, search, style
- `StylesTest` — 13 new tests for v4.1 APIs (textShadow, dropShadow, clip, 3D, glass, neumorph, SVG)
- `TailwindFXTest` — JIT v4.1 token tests + `TailwindFXMetrics` alert system tests
- `TestRunner` — `FxDataTableTest` wired in
- `FxAnimationTest` — Comprehensive animation API tests
- `ThemeManagerTest` — Theme persistence and propagation tests
- `AdvancedTestFXIntegrationTest` — Advanced UI integration tests with Monocle
- `TailwindFXUnifiedApplyTest` — Tests for unified apply() method with JIT detection
- `FxI18nTest` — Internationalization tests
- `FxFlexPaneTest` — Flexbox layout tests
- `EdgeCaseTests` — Edge case handling tests

### Breaking Changes
- **`AnimationUtil` deprecated** — Migrate to `FxAnimation` fluent API
- **Java 17 required** — Update your JDK from Java 21 to Java 17
- **`jit()` method deprecated** — Use `apply()` which now auto-detects JIT tokens
- **`applyDiff()` made private** — Internal optimization, use `apply()` instead
- **Examples moved** — Example applications now in separate `tailwindfx-examples` module
