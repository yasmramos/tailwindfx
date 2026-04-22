# TailwindFX Unit Tests Documentation

This document describes the comprehensive unit test suite for the TailwindFX library.

## Test Files Overview

### Core Tests (Existing)
- **StylesTest.java** - Tests for style utilities (padding, margin, transforms, filters, etc.)
- **ColorPaletteTest.java** - Tests for color palette functionality
- **ComponentFactoryTest.java** - Tests for component factory methods
- **JitCompilerTest.java** - Tests for JIT compiler
- **StyleTokenTest.java** - Tests for style token parsing
- **ThemeManagerTest.java** - Tests for theme management
- And more...

### New Tests (Added)

#### 1. TailwindFXMainTest.java
Tests for the main `TailwindFX` entry point class.

**Test Coverage:**
- CSS installation (main, base, utilities, colors, effects, components)
- Style application (single, multiple, null handling)
- JIT compilation and application
- Layout builder creation
- Responsive manager creation
- Theme manager creation
- Convenience methods (padding, margin, bg, text, fontSize, rounded)
- Edge cases (null handling, multiple installations)

**Key Tests:**
- `testInstallMainCss()` - Verifies main CSS installation
- `testApplyMultipleStyles()` - Verifies multiple style application
- `testJitArbitraryValues()` - Verifies JIT with arbitrary values
- `testConveniencePadding()` - Verifies convenience methods

#### 2. StyleTokenExtendedTest.java
Extended tests for `StyleToken` parsing functionality.

**Test Coverage:**
- Arbitrary value parsing (pixels, colors, widths, font sizes)
- Color with alpha parsing (50%, 100%, 0%, text, border)
- Negative values (margin, translate, rotate, skew)
- Named values (text sizes, rounded corners, font weights, shadows)
- Scale values (padding, gap, width, height)
- Edge cases (null, blank, whitespace, unknown tokens)
- Complex color families (all 20 Tailwind color families)
- Sub-prefix parsing (px, py, pt, pr, pb, pl, mx, my, mt, mr, mb, ml)
- Transform values (rotate, scale, skew-x, skew-y)

**Key Tests:**
- `testArbitraryPixels()` - Tests `p-[16px]` parsing
- `testColorAlpha50()` - Tests `bg-blue-500/50` parsing
- `testNegativeMarginTop()` - Tests `-mt-4` parsing
- `testAllColorFamiliesBg()` - Tests all 20 color families

#### 3. FxGridPaneTest.java
Tests for `FxGridPane` layout functionality.

**Test Coverage:**
- Basic grid creation (empty, with gap, with different gaps)
- Adding nodes to grid (position, column span, row span, multiple nodes)
- Grid constraints (column constraints, row constraints, fill width)
- Grid pane styling (TailwindFX styles, padding)
- Grid alignment (node alignment, fill cell)
- Grid utility methods (colSpan, rowSpan, colSpanFull, rowSpanFull, gridCell)
- Edge cases (null node, negative indices, zero span)

**Key Tests:**
- `testCreateGridWithGap()` - Tests grid with uniform gap
- `testAddNodeWithColSpan()` - Tests node with column span
- `testColSpanUtility()` - Tests column span utility method
- `testGridCellPosition()` - Tests grid cell positioning

#### 4. ThemeScopeManagerTest.java
Tests for `ThemeScopeManager` functionality.

**Test Coverage:**
- Theme application (light, dark, custom)
- Theme toggling (toggle, get current, is dark)
- Theme CSS application (add CSS, replace themes)
- Theme scope management (scoped theme, multiple scopes, reset)
- Theme persistence (save preference, load preference)
- Theme customization (color overrides, multiple overrides, custom CSS)
- Theme validation (validate, is installed)
- Edge cases (null scene, rapid switching, concurrent applications)

**Key Tests:**
- `testApplyDarkTheme()` - Tests dark theme application
- `testToggleTheme()` - Tests theme toggling
- `testCustomColorOverrides()` - Tests custom color overrides
- `testRapidThemeSwitching()` - Tests rapid theme switching

#### 5. JitCompilerExtendedTest.java
Extended tests for `JitCompiler` functionality.

**Test Coverage:**
- Cache management (caching, size limit, clearing, size reporting)
- Debug mode (enable, disable, logging)
- JIT pattern matching (detection, CSS class distinction, scale-based, arbitrary)
- Compile result types (inline, CSS class, unknown, null handling)
- Padding and margin compilation (scale values, directional)
- Color compilation (background, text, alpha, border)
- Arbitrary value compilation (pixels, colors, width, font size)
- Transform compilation (translate-x, translate-y, rotate, negative)
- Unknown token handling (marking, CSS classes, empty, null)
- Performance tests (cached performance, many tokens)

**Key Tests:**
- `testTokenCaching()` - Tests token caching behavior
- `testJitDetection()` - Tests JIT pattern detection
- `testArbitraryPixels()` - Tests arbitrary pixel compilation
- `testCachedPerformance()` - Tests cached performance improvement

#### 6. CssUtilitiesTest.java
Tests for CSS utility classes and stylesheets.

**Test Coverage:**
- CSS file validation (all 7 CSS files exist)
- CSS variable definitions (colors, font sizes, font weights, spacing)
- Utility class names (padding, margin, gap, width, height, visibility, opacity, z-index)
- Color class names (background, text, border, font size, font weight, border radius)
- CSS syntax validation (no viewport units, actual values for font-size/weight, valid syntax)
- Typography classes (text alignment, text decoration, text transform, font family)
- Effect classes (shadows, transforms)

**Key Tests:**
- `testNoViewportUnits()` - Verifies no `vh` units (JavaFX incompatibility)
- `testFontSizeUsesActualValues()` - Verifies font-size uses actual values
- `testFontWeightUsesActualValues()` - Verifies font-weight uses actual values
- `testValidCssSyntax()` - Verifies CSS syntax validity

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=TailwindFXMainTest
mvn test -Dtest=StyleTokenExtendedTest
mvn test -Dtest=FxGridPaneTest
mvn test -Dtest=ThemeScopeManagerTest
mvn test -Dtest=JitCompilerExtendedTest
mvn test -Dtest=CssUtilitiesTest
```

### Run Test Suite
```bash
mvn test -Dtest=AllTests
```

## Test Statistics

- **Total Test Files**: 24 test files
- **New Test Files**: 6 test files
- **Total Test Cases**: 300+ test cases
- **Coverage Areas**:
  - Core functionality: ✅
  - CSS utilities: ✅
  - JIT compilation: ✅
  - Style tokens: ✅
  - Theme management: ✅
  - Layout systems: ✅
  - Components: ✅
  - Edge cases: ✅
  - Performance: ✅
  - CSS syntax validation: ✅

## Recent CSS Fixes Validated

The test suite validates the following CSS fixes:

1. **Fixed `max-h-screen` class** - Replaced `100vh` with `100%`
   - Validated by: `CssUtilitiesTest.testNoViewportUnits()`

2. **Fixed font size classes** - Replaced CSS variables with actual `em` values
   - Validated by: `CssUtilitiesTest.testFontSizeUsesActualValues()`

3. **Fixed font weight classes** - Replaced CSS variables with actual numeric values
   - Validated by: `CssUtilitiesTest.testFontWeightUsesActualValues()`

## Test Conventions

- All tests use JUnit 5 (`org.junit.jupiter`)
- Tests use nested classes for organization (`@Nested`)
- Tests use descriptive display names (`@DisplayName`)
- Parameterized tests use `@ParameterizedTest` with providers
- Tests follow the pattern: `test[Feature][Scenario]()`
- Edge cases are tested in dedicated nested classes
- All assertions use JUnit 5 assertion methods

## Future Test Ideas

- Integration tests with full JavaFX applications
- Visual regression tests for CSS styling
- Performance benchmarks for large applications
- Memory usage tests for cache management
- Cross-platform compatibility tests
- Accessibility tests for components
