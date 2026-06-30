# TailwindFX - Issues for Contributors

This document contains curated issues for contributors at all levels.

## ✅ Resolved Issues (v0.1.0)

The following issues have been completed in version 0.1.0:

- ~~**Add Missing Cursor Utilities**~~ - ✅ Completed
- ~~**Add Resize Utilities**~~ - ✅ Completed  
- ~~**Document FxAnimation Class**~~ - ✅ Completed (see `docs/FxAnimation.md`)
- ~~**Add Unit Tests for ColorPalette**~~ - ✅ Completed (80%+ coverage achieved)
- ~~**Create Example Project**~~ - ✅ Completed (see `examples/basic-app/`, `examples/demo-app/`)
- ~~**Fix Typos in Documentation**~~ - ✅ Completed

---

## 🟢 Good First Issues (Perfect for Newcomers)

### 1. Add User-Select Utilities
**Difficulty:** Beginner  
**Labels:** `good first issue`, `css`, `utilities`  
**Status:** 🔴 **IN PROGRESS**  
**Description:** Implement user-select utilities (`select-none`, `select-text`, `select-all`, `select-auto`).  
**Files:** `src/main/java/io/github/yasmramos/tailwindfx/style/Styles.java`  
**Reference:** https://tailwindcss.com/docs/user-select

### 2. Add Touch-Action Utilities
**Difficulty:** Beginner  
**Labels:** `good first issue`, `css`, `utilities`  
**Status:** 🔴 **IN PROGRESS**  
**Description:** Implement touch-action utilities for mobile gesture control.  
**Files:** `src/main/java/io/github/yasmramos/tailwindfx/style/Styles.java`  
**Reference:** https://tailwindcss.com/docs/touch-action

### 3. Document Maven Plugin Usage
**Difficulty:** Beginner  
**Labels:** `good first issue`, `documentation`, `maven`  
**Description:** Add comprehensive documentation for the Maven plugin with usage examples.  
**Files:** `tailwindfx-maven-plugin/README.md`  
**Expected:** Step-by-step guide with configuration examples.

### 4. Add Accessibility (ARIA) Support
**Difficulty:** Beginner  
**Labels:** `good first issue`, `accessibility`, `a11y`  
**Description:** Add ARIA label support to UI components for better accessibility.  
**Files:** Component classes in `src/main/java/io/github/yasmramos/tailwindfx/components/`  
**Reference:** WCAG 2.1 Guidelines

---

## 🟡 Medium Issues (Some Experience Required)

### 5. Optimize JitCompiler Cache Performance
**Difficulty:** Intermediate  
**Labels:** `enhancement`, `performance`, `java`  
**Description:** Profile and optimize the JIT compiler cache. Consider implementing better eviction strategies or cache warming.  
**Files:** `src/main/java/io/github/yasmramos/tailwindfx/compiler/JitCompiler.java`  
**Metrics:** Improve cache hit ratio by 10%+.

### 6. Add Container Queries Support
**Difficulty:** Intermediate  
**Labels:** `enhancement`, `css`, `responsive`  
**Description:** Implement container query utilities (@container) for component-level responsive design.  
**Files:** `src/main/java/io/github/yasmramos/tailwindfx/style/Styles.java`, `src/main/java/io/github/yasmramos/tailwindfx/BreakpointManager.java`  
**Reference:** https://tailwindcss.com/docs/container-queries

### 7. Implement Scroll-Snap Utilities
**Difficulty:** Intermediate  
**Labels:** `enhancement`, `css`, `scrolling`  
**Status:** 🔴 **IN PROGRESS**  
**Description:** Add scroll-snap CSS utilities for creating snap-scrolling containers.  
**Files:** `src/main/java/io/github/yasmramos/tailwindfx/style/Styles.java`  
**Reference:** https://tailwindcss.com/docs/scroll-snap-type

### 8. Implement Columns Utilities
**Difficulty:** Intermediate  
**Labels:** `enhancement`, `css`, `layout`  
**Status:** 🔴 **IN PROGRESS**  
**Description:** Add multi-column layout utilities (columns-2, columns-3, break-inside, etc.).  
**Files:** `src/main/java/io/github/yasmramos/tailwindfx/style/Styles.java`  
**Reference:** https://tailwindcss.com/docs/columns

### 9. Add Theme Customization UI
**Difficulty:** Intermediate  
**Labels:** `enhancement`, `ui`, `theme`  
**Description:** Create a runtime theme customization panel (color picker, font selector, preview).  
**Files:** `src/main/java/io/github/yasmramos/tailwindfx/theme/ThemeCustomizer.java`  
**Expected:** Modal dialog with live preview.

### 10. Add E-commerce Example
**Difficulty:** Intermediate  
**Labels:** `example`, `documentation`  
**Description:** Create a complete e-commerce product page example with cart, product grid, and filters.  
**Files:** `examples/ecommerce/`  
**Expected:** Full working demo with multiple screens.

---

## 🔴 Advanced Issues (Experienced Contributors)

### 11. IntelliJ IDEA Plugin
**Difficulty:** Advanced  
**Labels:** `enhancement`, `plugin`, `ide`  
**Description:** Develop an IntelliJ plugin providing autocomplete, linting, and preview for TailwindFX utilities.  
**Skills:** IntelliJ Platform SDK, Java, CSS  
**Expected:** Autocomplete suggestions, real-time validation, color preview.

### 12. Hot-Reload Development Server
**Difficulty:** Advanced  
**Labels:** `enhancement`, `devtools`, `hot-reload`  
**Description:** Implement a file watcher that automatically recompiles and applies CSS changes during development.  
**Files:** `src/main/java/io/github/yasmramos/tailwindfx/TailwindFX.java` (watch method enhancement)  
**Expected:** Sub-second reload on CSS file changes.

### 13. Visual Builder Tool
**Difficulty:** Advanced  
**Labels:** `enhancement`, `tooling`, `gui`  
**Description:** Create a drag-and-drop visual builder for designing JavaFX layouts with TailwindFX utilities.  
**Skills:** JavaFX, CSS, UI/UX  
**Expected:** Standalone application that exports Java/FXML code.

### 14. Accessibility Improvements
**Difficulty:** Advanced  
**Labels:** `accessibility`, `enhancement`, `a11y`  
**Description:** Audit and improve accessibility support (ARIA labels, focus states, reduced motion).  
**Files:** Multiple - framework-wide  
**Expected:** WCAG 2.1 AA compliance checklist completed.

### 15. Performance Benchmarking Suite
**Difficulty:** Advanced  
**Labels:** `performance`, `benchmark`, `tests`  
**Description:** Create automated performance benchmarks comparing TailwindFX vs manual CSS styling.  
**Files:** `src/test/java/io/github/yasmramos/tailwindfx/perf/`  
**Expected:** CI integration with performance regression detection.

---

## 📊 Project Statistics (v0.1.0)

- **Java Files (src/main):** 92
- **Java Files (src/test):** 42
- **CSS Utilities:** 1,400+
- **Colors Available:** 209
- **Animations:** 14+
- **UI Components:** 13
- **Breakpoints:** 5 (SM/MD/LG/XL/XXL)
- **Theme Presets:** 7 (dark/light/blue/green/purple/rose/slate)

---

## 📋 How to Claim an Issue

1. **Comment** on the issue expressing interest
2. **Wait** for maintainer to assign it to you
3. **Fork** the repository
4. **Create a branch** named `issue-<number>-<short-description>`
5. **Work** on the issue following our Contributing Guide
6. **Submit a Pull Request** referencing the issue number

## 🆘 Need Help?

- Check our [Contributing Guide](CONTRIBUTING.md)
- Read the [Code of Conduct](CODE_OF_CONDUCT.md)
- Ask questions in the issue comments
- Join our community discussions

## 🏷️ Label Legend

- `good first issue` - Perfect for newcomers
- `help wanted` - We need assistance with this
- `enhancement` - New feature or improvement
- `bug` - Something isn't working correctly
- `documentation` - Improvements to docs
- `performance` - Performance improvements
- `tests` - Test-related tasks
- `css` - CSS-related work
- `java` - Java code changes
- `example` - Example applications
