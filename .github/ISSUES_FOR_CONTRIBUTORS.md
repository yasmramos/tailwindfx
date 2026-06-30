# TailwindFX - Issues for Contributors

This document contains curated issues for contributors at all levels.

## ✅ Resolved Issues (v0.1.0)

The following issues have been completed and merged into the main branch:

- ~~**1. Add Missing Cursor Utilities** - Implemented in v0.1.0~~
- ~~**2. Add User-Select Utilities** - Implemented in Styles.java~~
- ~~**3. Add Resize Utilities** - Implemented in v0.1.0~~
- ~~**4. Document FxAnimation Class** - docs/FxAnimation.md created~~
- ~~**5. Add Unit Tests for ColorPalette** - ColorPaletteTest.java added with 80%+ coverage~~
- ~~**6. Create Example Project** - basic-app and demo-app examples created~~
- ~~**7. Add Touch-Action Utilities** - Implemented in Styles.java~~
- ~~**8. Fix Typos in Documentation** - README.md and CONTRIBUTING.md reviewed~~
- ~~**9. Optimize JitCompiler Cache Performance** - LRU cache with ReadWriteLock implemented~~
- ~~**10. Add Container Queries Support** - ContainerQuery class implemented~~
- ~~**12. Implement Scroll-Snap Utilities** - Implemented in Styles.java~~
- ~~**14. Implement Columns Utilities** - Implemented in Styles.java~~

---

## 🟢 Good First Issues (Perfect for Newcomers)

### None Currently Available

All good first issues have been resolved! Check back soon for new opportunities, or consider tackling a medium-level issue with guidance from maintainers.

---

## 🟡 Medium Issues (Some Experience Required)

### 11. Create CLI Tool for CSS Scanning
**Difficulty:** Intermediate  
**Labels:** `enhancement`, `tooling`, `cli`  
**Description:** Build a CLI tool that scans Java/FXML files and reports unused CSS utilities.  
**Expected:** `java -jar tailwindfx-cli.jar scan --input src/ --output report.json`

### 13. Add E-commerce Example
**Difficulty:** Intermediate  
**Labels:** `example`, `documentation`  
**Description:** Create a complete e-commerce product page example with cart, product grid, and filters.  
**Files:** `examples/ecommerce/`  
**Expected:** Full working demo with multiple screens.

### 15. Add Theme Customization UI
**Difficulty:** Intermediate  
**Labels:** `enhancement`, `ui`, `theme`  
**Description:** Create a runtime theme customization panel (color picker, font selector, preview).  
**Files:** `src/main/java/io/github/yasmramos/tailwindfx/theme/ThemeCustomizer.java`  
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
**Files:** `src/main/java/io/github/yasmramos/tailwindfx/TailwindFX.java` (watch method enhancement)  
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
**Files:** `src/test/java/io/github/yasmramos/tailwindfx/perf/`  
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
