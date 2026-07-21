# TailwindFX

> Utility-first UI framework for JavaFX, inspired by Tailwind CSS.

[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://openjdk.org/projects/jdk/17/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17%2B-green)](https://openjfx.io/)
[![Build](https://img.shields.io/github/actions/workflow/status/yasmramos/TailwindFX/maven.yml?branch=main&label=build)](https://github.com/yasmramos/TailwindFX/actions/workflows/maven.yml)
[![Tests](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/yasmramos/85eea898808574addebcd08db98ccb69/raw/test-results.json)](https://github.com/yasmramos/TailwindFX/actions/workflows/maven.yml)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

---

## What is TailwindFX?

TailwindFX brings Tailwind CSS's utility-first approach to JavaFX. Instead of writing boilerplate style code, you compose styles from a comprehensive set of pre-built utility classes — and where CSS falls short, TailwindFX provides equivalent Java APIs.

### Architecture: Hybrid Approach (CSS Static + JIT Compilation) ✅

TailwindFX uses a **hybrid architecture** that combines the best of both worlds: static CSS for base styles and components, plus JIT compilation for utilities and arbitrary values. This approach mirrors Tailwind CSS v3+ strategy.

### Current: Hybrid Architecture (v2.0) ✅

TailwindFX features a **Just-In-Time compiler** for dynamic style generation, complemented by essential static CSS files for base styles and component presets.

**Architecture breakdown:**

| Layer | Responsibility | Implementation | Size |
|-------|---------------|----------------|------|
| **Base CSS** | CSS variables, reset, base styles | `tailwindfx-base.css` (generated from ThemeConfig) | ~17KB |
| **Color Palette** | Pre-defined color utilities | `tailwindfx-colors.css` | ~31KB |
| **Effects** | Shadows, filters, effects | `tailwindfx-effects.css` | ~11KB |
| **Components** | JavaFX component base styles | `tailwindfx-components.css` | ~297KB |
| **Utilities** | Common utility classes | `tailwindfx-utilities.css` | ~17KB |
| **Component Presets** | Pre-styled component templates | `tailwindfx-components-preset.css` | ~13KB |
| **JIT Compiler** | On-demand compilation for custom/arbitrary values | `JitCompiler` + delegates | Dynamic |
| **Dark Mode** | Optional dark theme overrides | `tailwindfx-dark.css` | ~20KB |

**Key features:**
- **Static CSS for common cases**: ~386KB of pre-generated CSS for instant loading of common utilities
- **JIT for flexibility**: Compile arbitrary values like `w-[320px]`, `bg-blue-500/80`, `drop-shadow-[#3b82f6]` on-demand
- **Dynamic theme generation**: `ThemeCssGenerator` creates `tailwindfx-base.css` from `ThemeConfig` at install time
- **Refactored architecture**: Separated parsing (`StyleToken`), resolution (`StyleResolver`), and property mapping (`CssPropertyMapper`)
- **LRU cache**: High-performance caching with configurable size (2000 entries max)
- **Smart fallback**: Unknown tokens are handled gracefully with warnings in debug mode

**Why hybrid?**
Unlike web browsers, JavaFX doesn't support CSS custom properties (variables) efficiently. Pre-generating common utilities ensures instant styling, while JIT handles edge cases and customization. This is similar to how Tailwind CSS generates a static CSS file during build-time rather than purely runtime JIT.

**Architecture flow:**
```
TwStyle.apply(node, "p-4", "bg-blue-500", "w-[320px]")
    ↓
┌─────────────────────────────────────┐
│ 1. Check static CSS (fast path)     │ ← p-4, bg-blue-500 found in tailwindfx-utilities.css
│    - tailwindfx-utilities.css       │
│    - tailwindfx-colors.css          │
│    - tailwindfx-effects.css         │
└─────────────────────────────────────┘
    ↓ (if not found in static CSS)
┌─────────────────────────────────────┐
│ 2. JIT Compiler (flexible path)     │ ← w-[320px] compiled on-demand
│    JitCompiler.compile(token)       │
│    ↓                                │
│    StyleToken.parse()               │
│    ↓                                │
│    StyleResolver.resolve()          │
│    ↓                                │
│    CssPropertyMapper.map()          │
│    ↓                                │
│    Generated: -fx-pref-width: 320px │
│    Cached for reuse                 │
└─────────────────────────────────────┘
    ↓
Applied to Node.style or added as style class
```

**Installation comparison:**
```java
// Standard installation (recommended)
// Loads all static CSS + enables JIT for arbitrary values
TwInstall.install(scene); 
// Files loaded:
// - tailwindfx-base.css (generated dynamically from ThemeConfig)
// - tailwindfx-colors.css
// - tailwindfx-effects.css
// - tailwindfx-utilities.css
// - tailwindfx-components.css
// - tailwindfx-components-preset.css
TwStyle.apply(btn, "btn-primary", "rounded-lg", "px-4", "py-2"); // Found in static CSS
TwStyle.jit(node, "bg-blue-500/80", "w-[320px]"); // JIT compiled

// Minimal installation (advanced users)
// Only loads base CSS, everything else is JIT compiled
TwInstall.installMinimal(scene); 
// Files loaded:
// - tailwindfx-base.css (generated dynamically)
// Everything else compiled JIT on-demand
TwStyle.apply(btn, "p-4", "bg-blue-500"); // JIT compiled
TwStyle.jit(node, "bg-blue-500/80", "w-[320px]"); // JIT compiled

// Dark mode support (optional)
TwInstall.installWithDarkMode(scene); // Also loads tailwindfx-dark.css
```

```java
// Before — JavaFX vanilla
btn.setStyle(
    "-fx-background-color: #3b82f6; " +
    "-fx-text-fill: white; " +
    "-fx-background-radius: 8px; " +
    "-fx-padding: 8px 16px;"
);

// Hover animation with vanilla JavaFX
btn.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
    Timeline tl = new Timeline(
        new KeyFrame(Duration.ZERO,
            new KeyValue(btn.scaleXProperty(), btn.getScaleX()),
            new KeyValue(btn.scaleYProperty(), btn.getScaleY())),
        new KeyFrame(Duration.millis(150),
            new KeyValue(btn.scaleXProperty(),1.05),
            new KeyValue(btn.scaleYProperty(),1.05))
    );
    tl.play();
});

// With TailwindFX (New API)
TwStyle.apply(btn, "btn-primary", "rounded-lg", "px-4", "py-2");
TwAnimation.onHoverScale(btn, 1.05);
```

---

## Features

| Feature | Description |
|---|---|
| **1,400+ CSS utilities** | Layout, typography, colors, shadows, effects, transforms |
| **JIT compiler** | `bg-blue-500/80`, `p-[13px]`, `drop-shadow-[#3b82f6]` arbitrary values |
| **FxFlexPane** | Real flexbox: direction, wrap, justify-content (6), align-items (4), gap, flex-grow/shrink/basis |
| **FxGridPane** | Grid-template-areas, masonry, auto-flow |
| **FxDataTable** | Sortable, filterable, paginated TableView wrapper |
| **ResponsiveNode** | Per-node breakpoint rules driven by `Scene.widthProperty()` |
| **Themes** | Dark/light/blue/green/purple/rose/slate + scoped subtree themes |
| **Animations** | fadeIn/Out, slideUp/Down/Left/Right, shake, bounce, pulse, spin + hover effects |
| **Tailwind v4.1** | text-shadow, drop-shadow-[color], SVG fill/stroke, 3D transforms, clip/mask |
| **Glassmorphism** | `TailwindFX.glass()`, `backdropBlur()`, `.glass` CSS class |
| **Neumorphism** | `TailwindFX.neumorph()`, `.neumorph` CSS class |
| **Pre-built Components** | TwButton, TwCard, TwBadge, TwAlert, TwInput, TwCheckbox, TwSelect, TwDataTable, TwProgressBar, TwSpinner, TwAvatar, TwVirtualFlow, TWAccordion |
| **Metrics + alerts** | Cache hit ratio, conflict rate, compile time alerts |
| **Performance** | StyleDiff (skip redundant applies), batch apply, LRU cache |

---

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.yasmramos</groupId>
    <artifactId>tailwindfx</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
---

## Quick Start

```java
public class MyApp extends Application {
    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 900, 600);

        // 1. Install (loads CSS + wires breakpoints)
        TwInstall.install(scene);

        // 2. Build UI with utilities
        VBox card = new VBox(12);
        TwStyle.apply(card, "card", "w-80");

        Label title = new Label("Hello TailwindFX");
        TwStyle.apply(title, "text-2xl", "font-bold", "text-blue-600");

        Button btn = new Button("Get Started");
        TwStyle.apply(btn, "btn-primary", "rounded-lg");
        TwAnimation.onHoverScale(btn, 1.05);

        card.getChildren().addAll(title, btn);
        root.getChildren().add(card);

        stage.setScene(scene);
        stage.show();
    }
}
```

---

## Specialized Facades

| Facade | Responsibility | Example |
|--------|----------------|---------|
| `TwStyle` | Apply utility classes, JIT tokens | `TwStyle.apply(node, "p-4", "bg-blue-500")` |
| `TwInstall` | Install CSS, watch changes | `TwInstall.install(scene)` |
| `TwTheme` | Dark/light themes, presets, scoped themes | `TwTheme.of(scene).dark().apply()` |
| `TwLayout` | Flexbox, Grid, layout builders | `TwLayout.of(container).row().gap(16).build()` |
| `TwAnimation` | Animations, hover effects | `TwAnimation.fadeIn(node).play()` |
| `TwResponsive` | Breakpoint-aware nodes | `TwResponsive.on(region).sm("w-full").install(scene)` |
| `TwEffect` | Glassmorphism, neumorphism, shadows | `TwEffect.glass(panel)` |
| `TwMetrics` | Performance monitoring, alerts | `TwMetrics.print()` |
| `TwConfig` | Global configuration | `TwConfig.unit(Unit.PX)` |
| `TwBatch` | Batch operations for performance | `TwBatch.run(() -> applyStyles())` |

### Components

TailwindFX provides pre-built components in the `io.github.yasmramos.tailwindfx.components` package. Use them directly by instantiating the classes:

| Component | Description | Usage Example |
|-----------|-------------|---------------|
| `TwButton` | Styled button with variants | `new TwButton("Click")` or `TwButton.primary("Submit")` |
| `TwCard` | Card container | `new TwCard("Title", "Content")` |
| `TwBadge` | Status badges | `TwBadge.create("NEW", "blue")` or `TwBadge.pill("Active", "green")` |
| `TwAlert` | Alert dialogs | `TwAlert.info("Message").show()` |
| `TwInput` | Text input field | `new TwInput("Placeholder")` |
| `TwCheckbox` | Checkbox with label | `new TwCheckbox("Accept terms")` |
| `TwSelect` | Dropdown selector | `new TwSelect<>(items)` |
| `TwDataTable` | Sortable table | `new TwDataTable<>(data)` |
| `TwProgressBar` | Progress indicator | `new TwProgressBar(0.75)` |
| `TwSpinner` | Loading spinner | `new TwSpinner()` |
| `TwAvatar` | User avatar | `new TwAvatar(imageUrl)` |
| `TwVirtualFlow` | Virtualized list | `new TwVirtualFlow<>(items)` |
| `TWAccordion` | Collapsible sections | `new TWAccordion()` |

### Usage Examples

```java
// Style
TwStyle.apply(btn, "btn-primary", "rounded-lg", "px-4", "py-2");
TwStyle.jit(node, "bg-blue-500/80", "p-[13px]");
TwStyle.remove(node, "text-red-500");
TwStyle.toggle(node, "dark-mode");

// Theme
TwTheme.of(scene).dark().apply();
TwTheme.of(scene).preset("blue").apply();
TwTheme.scope(panel).preset("rose").apply();

// Layout
TwLayout.of(container).row().gap(16).build();
TwLayout.flexRow().wrap(true).justify(Justify.BETWEEN).build();

// Animation
TwAnimation.fadeIn(node, 300).play();
TwAnimation.onHoverScale(btn, 1.05);
TwAnimation.shake(button).play();

// Components
TwCard card = new TwCard("Welcome", "Hello world");
TwButton btn = new TwButton("Click", ButtonVariant.PRIMARY);
TwBadge badge = new TwBadge("New", BadgeVariant.SUCCESS);
TwAlert.info("Operation completed").show();

// Responsive
TwResponsive.on(sidebar)
    .base("w-64")
    .sm("w-full")
    .md("w-48")
    .install(scene);

// Effect
TwEffect.glass(overlayPane);
TwEffect.neumorph(button);
TwEffect.textShadowMd(heading);

// Metrics
TwMetrics.setEnabled(true);
TwMetrics.print();

// Config
TwConfig.autoBatch(20);
TwConfig.debug(true);

// Batch
TwBatch.run(() -> {
    nodes.forEach(n -> TwStyle.apply(n, "p-4", "bg-white"));
});
```

### Backward Compatibility

The old `TailwindFX` facade still works for backward compatibility, delegating to the specialized facades:

```java
// Still works (delegates to TwStyle)
TailwindFX.apply(node, "p-4", "bg-white");

// Still works (delegates to TwInstall)
TailwindFX.install(scene);

// Still works (delegates to TwTheme)
TailwindFX.theme(scene).dark().apply();

// Recommended: use specialized facades directly
TwStyle.apply(node, "p-4", "bg-white");
TwInstall.install(scene);
TwTheme.of(scene).dark().apply();
```
---

## License

MIT — see [LICENSE](LICENSE) for details.

---

## Contributing

We welcome contributions from the community! Here's how you can help:

1. **Check existing issues** - Look for [Good First Issues](https://github.com/yasmramos/TailwindFX/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22) to get started
2. **Read our guides** - See [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)
3. **Fork and submit PRs** - Create a branch from `develop`, make your changes, and submit a pull request
4. **Report bugs** - Use our [bug report template](https://github.com/yasmramos/TailwindFX/issues/new?template=bug_report.md)
5. **Suggest features** - Use our [feature request template](https://github.com/yasmramos/TailwindFX/issues/new?template=feature_request.md)

### Quick Links
- 📚 [Issues for Contributors](.github/ISSUES_FOR_CONTRIBUTORS.md)
- 🐛 [Report a Bug](https://github.com/yasmramos/TailwindFX/issues/new?template=bug_report.md)
- 💡 [Request a Feature](https://github.com/yasmramos/TailwindFX/issues/new?template=feature_request.md)
- 🎯 [Good First Issues](https://github.com/yasmramos/TailwindFX/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22)

---
