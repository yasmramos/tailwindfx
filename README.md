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

### Architecture: 100% JIT Compilation ✅

TailwindFX uses a **pure Just-In-Time (JIT) compilation** architecture. All styles are generated dynamically at runtime, with zero dependency on static CSS files. This approach provides maximum flexibility and minimal footprint.

### Current: Pure JIT Architecture (v3.0) ✅

TailwindFX features a **Just-In-Time compiler** that translates Tailwind tokens directly into JavaFX properties and API calls. No static CSS files are loaded at runtime.

**Architecture breakdown:**

| Layer | Responsibility | Implementation | Size |
|-------|---------------|----------------|------|
| **JIT Compiler** | Dynamic compilation of all utilities | `JitCompiler` + `CssPropertyMapper` + `Styles` | Dynamic |
| **Theme Generator** | Dynamic generation of CSS variables | `ThemeCssGenerator` (injected at runtime) | ~5KB |
| **Component Styles** | Programmatic component styling | `ComponentStyles.java` | Dynamic |
| **Color Parser** | Dynamic color resolution | `ColorParser.java` | Dynamic |
| **LRU Cache** | High-performance caching | Configurable size (2000 entries max) | Minimal |

**Key features:**
- **100% Dynamic**: Zero static CSS files loaded at runtime
- **JIT for everything**: Compile any token like `p-4`, `bg-blue-500`, `w-[320px]`, `blur-md` on-demand
- **Direct API application**: Properties not supported by CSS (transforms, filters, effects) are applied directly via JavaFX API
- **Smart caching**: LRU cache for compiled styles to maximize performance
- **Theme support**: Dynamic variable injection via `ThemeCssGenerator`
- **Dark mode**: Handled programmatically via `ThemeManager.theme(scene).dark().apply()`

**Why 100% JIT?**
JavaFX CSS has limitations compared to web CSS. By compiling everything JIT and applying unsupported properties directly via Java API, TailwindFX provides full Tailwind CSS feature parity without workarounds. This eliminates the need for large static CSS files and allows dynamic customization at runtime.

**Architecture flow:**
```
TwStyle.apply(node, "p-4", "bg-blue-500", "w-[320px]", "blur-md")
    ↓
┌─────────────────────────────────────┐
│ JIT Compiler (single path)          │
│ JitCompiler.compile(token)          │
│ ↓                                   │
│ StyleToken.parse()                  │
│ ↓                                   │
│ CssPropertyMapper.map()             │
│ ↓                                   │
│ If CSS-supported:                   │
│   Generated: -fx-padding: 16px      │
│   Applied to node.style             │
│ ↓                                   │
│ If Java-only (blur, skew, etc.):    │
│   Styles.setBlur(node, value)       │
│   Applied via JavaFX API            │
│ ↓                                   │
│ Cached for reuse                    │
└─────────────────────────────────────┘
    ↓
Applied to Node
```

**Installation:**
```java
// Standard installation (recommended)
// Injects theme variables dynamically, enables JIT for all tokens
TwInstall.install(scene); 
// No static CSS files loaded
// Everything compiled JIT on-demand
TwStyle.apply(btn, "btn-primary", "rounded-lg", "px-4", "py-2"); // JIT compiled
TwStyle.apply(node, "bg-blue-500/80", "w-[320px]", "blur-md"); // JIT compiled

// Dark mode support (programmatic)
ThemeManager.theme(scene).dark().apply(); // Switch to dark mode
ThemeManager.theme(scene).light().apply(); // Switch to light mode
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
| **ComponentFactory** | Cards, badges, modals, drawers, tooltips |
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
| `TwComponent` | Pre-built components (cards, badges) | `TwComponent.card().title("Hi").build()` |
| `TwResponsive` | Breakpoint-aware nodes | `TwResponsive.on(region).sm("w-full").install(scene)` |
| `TwEffect` | Glassmorphism, neumorphism, shadows | `TwEffect.glass(panel)` |
| `TwMetrics` | Performance monitoring, alerts | `TwMetrics.print()` |
| `TwConfig` | Global configuration | `TwConfig.unit(Unit.PX)` |
| `TwBatch` | Batch operations for performance | `TwBatch.run(() -> applyStyles())` |

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

// Component
TwComponent.card()
    .title("Welcome")
    .content("Hello world")
    .build();

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
