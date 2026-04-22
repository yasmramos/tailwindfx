# TailwindFX Changelog

All notable changes to this project are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [4.3.0] — 2026-03-19

### Added
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

### Fixed
- `FxFlexPane.computePrefHeight` returned wrong height for `wrap=true` rows
- `UtilityConflictResolver` — skew, aspect, 3D transform categories were missing, causing classes to accumulate instead of replace
- Spanish strings remaining in log messages (complete sweep)

### Tests added
- `FxDataTableTest` — 21 tests covering builder, filter, pagination, search, style
- `StylesTest` — 13 new tests for v4.1 APIs (textShadow, dropShadow, clip, 3D, glass, neumorph, SVG)
- `TailwindFXTest` — JIT v4.1 token tests + `TailwindFXMetrics` alert system tests
- `TestRunner` — `FxDataTableTest` wired in

---

## [4.2.0] — 2026-03-18

### Added
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

### Fixed
- Hover effects (`onHoverScale/Lift/Dim`) used `setOnMouseEntered` (overwrites) → replaced with `addEventHandler` (chains)
- `JitCompiler` alpha clamp: was warning-only, now actually clamps to `[0, 100]`
- `UtilityConflictResolver.invalidateCache(null)` — added null safety

---

## [4.1.0] — 2026-03-17

### Added
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
- Build files: `pom.xml`

### Fixed
- All log/exception/comment strings normalized to English
- Wildcard imports replaced with specific imports in all Java files

---

## [4.0.0] — 2026-03-16

### Added
- **`FxGridPane`** — grid-template-areas, auto-flow, masonry layout
- **`ComponentFactory`** — high-level card, badge, modal, drawer, tooltip, datatable builders
- **`FxLayout` TilePane** — tile layout support in FxLayout builder
- **`Styles.java`** — skewX/Y, objectCover/Contain, imgSize, 40+ grid/margin/filter methods
- **`JitCompiler`** — gradient parsing, hex validation, negative translate, arbitrary values
- **`ThemeManager`** — save/load/delete theme persistence via `java.util.prefs.Preferences`
- **`ThemeScopeManager`** — scoped themes for any Pane subtree
- **`AnimationUtil`** — 14 animations, FxAnimation fluent API, AnimationRegistry, ResponsiveAnimationGuard
- **`TailwindFX.Config`** — unit, breakpoints, debug, warnOnNoParent
- **`TailwindFX.watch()`** — hot-reload CSS in development
- **`TailwindFX.debugReport()`** — runtime node inspection
- CSS: 11 900+ lines, 1 400+ utility classes

---

## [1.0.0] — Initial release

- CSS utility framework: layout, typography, colors, borders, effects, transforms
- `TailwindFX.install()`, `apply()`, `jit()`, `jitApply()`
- `ColorPalette` — 209 Tailwind colors
- `BreakpointManager` — SM/MD/LG/XL/XXL breakpoints
- `ThemeManager` — dark/light/blue/green/purple/rose/slate presets
