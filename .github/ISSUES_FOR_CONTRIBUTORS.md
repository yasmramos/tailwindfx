# TailwindFX - Issues for Contributors

This document contains curated issues for contributors at all levels.

## 🟢 Good First Issues (Perfect for Newcomers)

### 1. Add Missing Cursor Utilities
**Difficulty:** Beginner  
**Labels:** `good first issue`, `css`, `utilities`  
**Description:** Add cursor utility classes (`cursor-pointer`, `cursor-text`, `cursor-grab`, etc.) to match TailwindCSS v4.  
**Files:** `src/main/resources/tailwindfx.css`  
**Expected:** ~15 new CSS classes following existing patterns.

### 2. Add User-Select Utilities
**Difficulty:** Beginner  
**Labels:** `good first issue`, `css`, `utilities`  
**Description:** Implement user-select utilities (`select-none`, `select-text`, `select-all`, `select-auto`).  
**Files:** `src/main/resources/tailwindfx.css`  
**Reference:** https://tailwindcss.com/docs/user-select

### 3. Add Resize Utilities
**Difficulty:** Beginner  
**Labels:** `good first issue`, `css`, `utilities`  
**Description:** Add resize property utilities (`resize-none`, `resize-y`, `resize-x`, `resize`).  
**Files:** `src/main/resources/tailwindfx.css`

### 4. Document FxAnimation Class
**Difficulty:** Beginner  
**Labels:** `good first issue`, `documentation`, `java`  
**Description:** Add comprehensive Javadoc to all public methods in FxAnimation.java with examples.  
**Files:** `src/main/java/io/github/yasmramos/tailwindfx/FxAnimation.java`

### 5. Add Unit Tests for ColorPalette
**Difficulty:** Beginner  
**Labels:** `good first issue`, `tests`, `java`  
**Description:** Write unit tests for ColorPalette class methods that lack coverage.  
**Files:** `src/test/java/io/github/tailwindfx/ColorPaletteTest.java`  
**Expected:** 80%+ code coverage for ColorPalette.

### 6. Create Example Project
**Difficulty:** Beginner  
**Labels:** `good first issue`, `example`, `documentation`  
**Description:** Create a complete example application demonstrating basic TailwindFX usage (login form, dashboard).  
**Files:** `examples/basic-app/`  
**Expected:** README with screenshots and setup instructions.

### 7. Add Touch-Action Utilities
**Difficulty:** Beginner  
**Labels:** `good first issue`, `css`, `utilities`  
**Description:** Implement touch-action utilities for mobile gesture control.  
**Files:** `src/main/resources/tailwindfx.css`  
**Reference:** https://tailwindcss.com/docs/touch-action

### 8. Fix Typos in Documentation
**Difficulty:** Beginner  
**Labels:** `good first issue`, `documentation`  
**Description:** Review and fix any typos or grammatical errors in README.md and CONTRIBUTING.md.  
**Files:** `README.md`, `CONTRIBUTING.md`

---

## 🟡 Medium Issues (Some Experience Required)

### 9. Optimize JitCompiler Cache Performance
**Difficulty:** Intermediate  
**Labels:** `enhancement`, `performance`, `java`  
**Description:** Profile and optimize the JIT compiler cache. Consider implementing better eviction strategies or cache warming.  
**Files:** `src/main/java/io/github/tailwindfx/compiler/JitCompiler.java`  
**Metrics:** Improve cache hit ratio by 10%+.

### 10. Add Container Queries Support
**Difficulty:** Intermediate  
**Labels:** `enhancement`, `css`, `responsive`  
**Description:** Implement container query utilities (@container) for component-level responsive design.  
**Files:** `src/main/resources/tailwindfx.css`, `src/main/java/io/github/tailwindfx/BreakpointManager.java`  
**Reference:** https://tailwindcss.com/docs/container-queries

### 11. Create CLI Tool for CSS Scanning
**Difficulty:** Intermediate  
**Labels:** `enhancement`, `tooling`, `cli`  
**Description:** Build a CLI tool that scans Java/FXML files and reports unused CSS utilities.  
**Expected:** `java -jar tailwindfx-cli.jar scan --input src/ --output report.json`

### 12. Implement Scroll-Snap Utilities
**Difficulty:** Intermediate  
**Labels:** `enhancement`, `css`, `scrolling`  
**Description:** Add scroll-snap CSS utilities for creating snap-scrolling containers.  
**Files:** `src/main/resources/tailwindfx.css`  
**Reference:** https://tailwindcss.com/docs/scroll-snap-type

### 13. Add E-commerce Example
**Difficulty:** Intermediate  
**Labels:** `example`, `documentation`  
**Description:** Create a complete e-commerce product page example with cart, product grid, and filters.  
**Files:** `examples/ecommerce/`  
**Expected:** Full working demo with multiple screens.

### 14. Implement Columns Utilities
**Difficulty:** Intermediate  
**Labels:** `enhancement`, `css`, `layout`  
**Description:** Add multi-column layout utilities (columns-2, columns-3, break-inside, etc.).  
**Files:** `src/main/resources/tailwindfx.css`  
**Reference:** https://tailwindcss.com/docs/columns

### 15. Add Theme Customization UI
**Difficulty:** Intermediate  
**Labels:** `enhancement`, `ui`, `theme`  
**Description:** Create a runtime theme customization panel (color picker, font selector, preview).  
**Files:** `src/main/java/io/github/tailwindfx/theme/ThemeCustomizer.java`  
**Expected:** Modal dialog with live preview.

---

## 🔴 Advanced Issues (Experienced Contributors)

### 16. IntelliJ IDEA Plugin
**Difficulty:** Advanced  
**Labels:** `enhancement`, `plugin`, `ide`  
**Description:** Develop an IntelliJ plugin providing autocomplete, linting, and preview for TailwindFX utilities.  
**Skills:** IntelliJ Platform SDK, Java, CSS  
**Expected:** Autocomplete suggestions, real-time validation, color preview.

### 17. Hot-Reload Development Server
**Difficulty:** Advanced  
**Labels:** `enhancement`, `devtools`, `hot-reload`  
**Description:** Implement a file watcher that automatically recompiles and applies CSS changes during development.  
**Files:** `src/main/java/io/github/tailwindfx/TailwindFX.java` (watch method enhancement)  
**Expected:** Sub-second reload on CSS file changes.

### 18. Visual Builder Tool
**Difficulty:** Advanced  
**Labels:** `enhancement`, `tooling`, `gui`  
**Description:** Create a drag-and-drop visual builder for designing JavaFX layouts with TailwindFX utilities.  
**Skills:** JavaFX, CSS, UI/UX  
**Expected:** Standalone application that exports Java/FXML code.

### 19. Accessibility Improvements
**Difficulty:** Advanced  
**Labels:** `accessibility`, `enhancement`, `a11y`  
**Description:** Audit and improve accessibility support (ARIA labels, focus states, reduced motion).  
**Files:** Multiple - framework-wide  
**Expected:** WCAG 2.1 AA compliance checklist completed.

### 20. Performance Benchmarking Suite
**Difficulty:** Advanced  
**Labels:** `performance`, `benchmark`, `tests`  
**Description:** Create automated performance benchmarks comparing TailwindFX vs manual CSS styling.  
**Files:** `src/test/java/io/github/tailwindfx/perf/`  
**Expected:** CI integration with performance regression detection.

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
