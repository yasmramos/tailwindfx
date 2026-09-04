# TailwindFX Changelog

All notable changes to this project are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [0.1.0] - 2026-09-04

> **Early Preview Release** — This is an initial preview version intended for testing and feedback. Some features may be incomplete or subject to change.

### Added

#### Core API — Static Facade Entry Point
- **`TailwindFX`** — Main static facade delegating to specialized facades: `TwStyle`, `TwInstall`, `TwTheme`, `TwLayout`, `TwResponsive`, `TwEffect`, `TwMetrics`, `TwBatch`, `TwConfig`, `TwAnimation`
- Key methods: `apply()`, `jit()`, `remove()`, `toggle()`, `install()`, `installBase()`, `installDark()`, `theme()`, `layout()`

#### Style System (`style` package)
- **`Styles`** — Comprehensive utility class for applying Tailwind-like styles programmatically
- **`StyleMerger`** — Merges multiple style maps with conflict resolution
- **`StylePerf`** — Performance optimization with style diff caching and batch apply
- **`StyleToken`** — Represents parsed style tokens
- **`TypeHint`** — Type hints for style resolution

#### Theme System (`theme` package)
- **`ThemeManager`** — Manages light/dark/custom themes with persistence via `java.util.prefs.Preferences`
- **`ThemeScopeManager`** — Scoped themes for any Pane subtree with `findClosestScope()`, `inheritScope()`, `refreshScope()`
- **`ThemeConfig`** — Theme configuration options
- **`ThemeCustomizationPanel`** — UI panel for runtime theme customization

#### Layout Components (`layout` package)
- **`TwFlexPane`** — Full Flexbox model: direction, wrap, justify-content (6 variants), align-items (4 variants), gap, flex-grow, flex-shrink, order, align-self, flex-basis
- **`TwGridPane`** — Grid layout with grid-template-areas, auto-flow, and masonry support

#### UI Components (`components` package)
- **`TwAlert`** — Alert/notification component
- **`TwAvatar`** — Avatar image component
- **`TwBadge`** — Badge/label component with variants
- **`TwButton`** — Styled button component
- **`TwCard`** — Card container component
- **`TwCheckbox`** — Checkbox component
- **`TwDataTable<T>`** — Declarative, sortable, filterable, paginated TableView wrapper
- **`TwInput`** — Text input component
- **`TwProgressBar`** — Progress indicator component
- **`TwSelect`** — Dropdown selection component
- **`TwSpinner`** — Loading spinner component
- **`TwVirtualFlow`** — Virtualized list component
- **`TWAccordion`** — Accordion/collapsible panel component
- **`TWTitledPane`** — Titled pane component

#### Animation System (`animation` package)
- **`TwAnimation`** — Fluent animation API with 14+ built-in animations, animation registry, and responsive animation guard

#### Core Engine (`core` package)
- **`JitCompiler`** — Just-in-time CSS compiler with LRU cache (2000 entries), thread-safe operations, supports arbitrary values like `drop-shadow-[#hex]`, `text-shadow-[rgba]`, `stroke-[n]`, `fill-[#hex]`, `aspect-ratio-[w/h]`
- **`StyleResolver`** — Resolves style tokens to CSS properties
- **`UtilityConflictResolver`** — Handles v4.1 categories including text-shadow, drop-shadow, fill, stroke, clip, break, skew-x/y, aspect, rotate-x/y, translate-z, and component categories; includes `cleanupNode()`, `autoCleanup()`, `invalidateCategoryCache()`
- **`VariantManager`** — Manages state variants (hover, focus, active, etc.)
- **`VariantParser`** — Parses variant prefixes from utility classes
- **`GradientProcessor`** — Processes gradient utilities
- **`RingProcessor`** — Processes ring/border utilities
- **`ScrollSnapProcessor`** — Processes scroll-snap utilities
- **`AspectRatioProcessor`** — Processes aspect-ratio utilities
- **`ContainerQueryProcessor`** — Processes container query utilities
- **`TransitionProcessor`** — Processes CSS transition utilities
- **`TypeHintProcessor`** — Processes type hints for style resolution
- **`CssPropertyMapper`** — Maps utility classes to CSS properties
- **`ThemeCssGenerator`** — Generates theme-specific CSS
- **`ComponentStyles`** — Predefined component style presets
- **`ColorUtilityValidator`** — Validates color utility classes
- **`ManualLruCache`** — Thread-safe LRU cache implementation
- **`Preconditions`** — Utility for argument validation

#### Responsive & Breakpoint System
- **`BreakpointManager`** (`breakpoint` package) — Responsive-aware category detection with SM/MD/LG/XL/XXL breakpoints
- **`ResponsiveNode`** (`responsive` package) — Per-node responsive utility rules driven by `Scene.widthProperty()`
- **`ContainerQuery`** (`responsive` package) — Container query support

#### Color System (`color` package)
- **`ColorPalette`** — 209 Tailwind colors with programmatic access

#### Internationalization (`i18n` package)
- **`TwI18n`** — Internationalization support for components

#### Metrics & Monitoring (`metrics` package)
- **`TailwindFXMetrics`** — AtomicLong counters for cache hits/misses, compilations, conflicts, themes, animations, layout passes; alert system with `onAlert()`, `alertOnLowCacheHitRatio()`, `alertOnHighConflictRate()`, `alertOnSlowCompile()`

#### Benchmarking (`benchmark` package)
- **`Benchmark`** — Performance benchmarking utilities

#### Configuration & Batch Operations
- **`TwConfig`** — Configuration options including unit, breakpoints, debug mode, warn-on-parent, auto-batch threshold
- **`TwBatch`** — Batch style application with `batch()` and `batchAsync()` methods

#### Maven Plugin
- **`tailwindfx-maven-plugin`** — Maven plugin for TailwindFX integration
- **`TailwindCssMojo`** — Mojo for processing Tailwind CSS during build

#### Module Support
- **`module-info.java`** — Java module descriptor for modular projects
- **`package-info.java`** — Package-level Javadoc documentation

### Tests

Comprehensive test suite with 59+ test classes covering:

#### Component Tests
- `TwAlertTest`, `TwAvatarTest`, `TwBadgeTest`, `TwButtonTest`, `TwCardTest`, `TwCheckboxTest`, `TwDataTableTest`, `TwInputTest`, `TwFlexPaneTest`

#### Layout Tests
- `TwFlexPaneTest`, `TwGridPaneTest`, `TwLayoutTest`

#### Core Engine Tests
- `JitCompilerTest`, `JitCompilerExtendedTest`, `VariantManagerTest`, `VariantParserTest`, `GradientProcessorTest`, `RingProcessorTest`, `AspectRatioProcessorTest`, `ContainerQueryProcessorTest`, `TransitionProcessorTest`, `TypeHintProcessorTest`, `UtilityConflictResolverTest`, `StyleResolverTest`, `CssPropertyMapperTest`, `ThemeCssGeneratorTest`, `LruCacheTest`, `PreconditionsTest`

#### Style System Tests
- `StylesTest`, `StyleTokenTest`, `StyleTokenExtendedTest`, `StyleMergerTest`, `StylePerfTest`, `StylePerfAdditionalTest`, `TypeHintTest`

#### Theme System Tests
- `ThemeManagerTest`, `ThemeScopeManagerTest`

#### Integration Tests
- `TailwindFXTest`, `TailwindFXIntegrationTest`, `AdvancedTestFXIntegrationTest`, `IntegrationTest`, `MetricsIntegrationTest`

#### Responsive & Breakpoint Tests
- `BreakpointManagerTest`, `ResponsiveNodeTest`, `ContainerQueryTest`

#### Color & i18n Tests
- `ColorPaletteTest`, `TwI18nTest`

#### Configuration & Batch Tests
- `TwConfigTest`, `TwBatchTest`, `TwMetricsTest`, `TwResponsiveTest`, `TwStyleTest`, `TwStyleLayoutTest`, `TwThemeTest`, `TwEffectTest`

#### Benchmark Tests
- `BenchmarkTest`

#### CSS Utilities Tests
- `CssUtilitiesTest`

### Documentation

- Complete Javadoc for all public APIs including `TwAnimation` with examples, parameter descriptions, and usage guidelines
- Package-level documentation via `package-info.java` files
- README.md with setup and usage instructions
- CONTRIBUTING.md with contribution guidelines
- CODE_OF_CONDUCT.md with community standards
- MIT License with 2026 copyright
