# Issue: Restructure Source Code Packages for Better Modularity

## 🎯 Objective

Restructure the source code packages to improve modularity, maintainability, and scalability. Currently, all ~50 Java classes are in the root `tailwindfx` package, making navigation and maintenance difficult.

## 📊 Current State Analysis

**Problem:** All classes are in the root package `tailwindfx`:
- Core library classes (TailwindFX, FxLayout, JIT compiler)
- UI components (FxButton, FxCard, FxDataTable, etc.)
- Utilities (ColorUtil, CssParser)
- Animations (TwAnimation)
- Theme management (ThemeManager, ThemeScopeManager)
- Internationalization (FxI18n)
- Data components (FxVirtualFlow)

**Issues:**
1. ❌ Mixed responsibilities in single package
2. ❌ Difficult navigation with 50+ classes
3. ❌ Limited scalability
4. ❌ Violates package-level SOLID principles

## 🏗️ Proposed Structure

```
tailwindfx/
├── core/                    # Core library functionality
│   ├── TailwindFX.java
│   ├── FxLayout.java
│   ├── TailwindConfig.java
│   └── CssPropertyRegistry.java
├── components/              # Reusable UI components
│   ├── buttons/
│   │   ├── FxButton.java
│   │   └── FxToggleButton.java
│   ├── cards/
│   │   └── FxCard.java
│   ├── tables/
│   │   ├── FxDataTable.java
│   │   └── FxDataTableColumn.java
│   ├── forms/
│   │   ├── FxInput.java
│   │   ├── FxSelect.java
│   │   └── FxCheckbox.java
│   ├── layout/
│   │   ├── FxFlexPane.java
│   │   └── FxGridPane.java
│   └── misc/
│       ├── FxAvatar.java
│       ├── FxBadge.java
│       └── FxSpinner.java
├── utils/                   # Utility classes
│   ├── ColorUtil.java
│   ├── CssParser.java
│   ├── ComponentFactory.java
│   └── JitCompiler.java
├── animation/               # Animation utilities
│   └── TwAnimation.java
├── theme/                   # Theme management
│   ├── ThemeManager.java
│   ├── ThemeScopeManager.java
│   └── Theme.java
├── i18n/                    # Internationalization
│   └── FxI18n.java
└── data/                    # Data handling components
    └── FxVirtualFlow.java
```

## ✅ Tasks

### Phase 1: Create Package Structure
- [ ] Create directories: `core`, `components`, `utils`, `theme`, `i18n`, `data`
- [ ] Create subdirectories in `components`: `buttons`, `cards`, `tables`, `forms`, `layout`, `misc`

### Phase 2: Move Classes
- [ ] Move core classes to `tailwindfx.core`
- [ ] Move UI components to `tailwindfx.components.*`
- [ ] Move utilities to `tailwindfx.utils`
- [ ] Move theme classes to `tailwindfx.theme`
- [ ] Move FxI18n to `tailwindfx.i18n`
- [ ] Move FxVirtualFlow to `tailwindfx.data`

### Phase 3: Update References
- [ ] Update all imports in moved classes
- [ ] Update imports in test files
- [ ] Update `module-info.java` with new package exports
- [ ] Verify no broken references

### Phase 4: Testing & Validation
- [ ] Run full test suite (339 tests)
- [ ] Verify example applications still work
- [ ] Check documentation references
- [ ] Update any external documentation

## 🎯 Benefits

1. ✅ **Better organization**: Clear separation of concerns
2. ✅ **Easier navigation**: Find classes by category
3. ✅ **Scalability**: Easy to add new components
4. ✅ **Maintainability**: Isolated changes per module
5. ✅ **Discoverability**: Clear API structure for users

## 📝 Notes for Contributors

- This is a **refactoring-only** task: no functional changes
- All tests must pass after refactoring (339 tests)
- Preserve all public APIs (no breaking changes)
- Update `module-info.java` carefully
- Consider doing this in multiple small PRs by package

## 🏷️ Suggested Labels

`refactoring`, `enhancement`, `architecture`, `good first issue` (partial)

---

**How to create this issue on GitHub:**
1. Go to https://github.com/yasmramos/TailwindFX/issues/new
2. Copy this content
3. Add labels: `refactoring`, `enhancement`, `architecture`
4. Submit
