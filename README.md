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

### Architecture: TailwindCSS-Style Build-Time Generation + Runtime Class-Based Application ✅

TailwindFX now follows the **TailwindCSS approach**: scan source files at build-time, generate a stylesheet with utility classes, and apply them by name at runtime. JIT inline compilation remains as a fallback for dynamic/arbitrary values not resolved at build-time.

**Architecture breakdown:**

| Layer | Responsibility | Implementation | Size |
|-------|---------------|----------------|------|
| **Base CSS** | CSS variables, reset, base styles | `tailwindfx-base.css` (generated from ThemeConfig) | ~2-5KB |
| **Generated Stylesheet (AOT)** | Build-time scanned utility classes | `tailwindfx-generated.css` (Maven plugin) | Variable |
| **JIT Compiler (Fallback)** | Runtime compilation for dynamic/arbitrary values | `JitCompiler` + `StyleToken` + `StyleResolver` | Dynamic |
| **LRU Cache** | High-performance caching for JIT compiled styles | Configurable size (2000 entries max) | In-memory |

**Key features:**
- **Build-time scanning**: Maven plugin scans `.java` and `.fxml` files for Tailwind classes
- **AOT stylesheet generation**: Generates `tailwindfx-generated.css` with utility classes and translated variants
- **Class-based runtime**: When `preferStylesheet` is enabled, applies CSS classes instead of inline styles
- **JIT fallback**: Dynamic/arbitrary values (`w-[<calculated>]`, `bg-[#rgb]/opacity`) still compile inline
- **Variant translation**: `hover:X` → `.X:hover`, `focus:X` → `.X:focused`, `md:X` → `.bp-md .X`, `dark:X` → `.dark .X`
- **!important filtering**: JavaFX doesn't support `!important`, automatically filtered during generation
- **Specialized processors**: `RingProcessor`, `GradientProcessor`, etc. captured via `compileBatch()`
- **Unsupported properties filtered**: `aspect-ratio`, `scroll-snap`, `transition`, `container-query` return `null` (handled programmatically)

**Why TailwindCSS-style?**
- **Better performance**: CSS classes are faster than inline style manipulation
- **Smaller runtime footprint**: Styles loaded once via stylesheet, not compiled per-node
- **Easier debugging**: Inspect `node.getStyleClass()` instead of inline `node.getStyle()`
- **Familiar workflow**: Same developer experience as TailwindCSS on the web
- **Optimized bundles**: Only generate CSS for classes actually used in your codebase

**Architecture flow:**
```
┌─────────────────────────────────────────────────────────────┐
│ BUILD-TIME (Maven Plugin: tailwindfx:generate)              │
├─────────────────────────────────────────────────────────────┤
│ 1. Scan source files (.java, .fxml) for Tailwind classes    │
│ 2. For each class:                                           │
│    - JitCompiler.compileBatch(className)                    │
│    - Capture inlineStyle output                             │
│    - Translate variants (hover:, md:, dark:, etc.)          │
│    - Filter !important                                      │
│    - Skip unsupported properties (aspect-ratio, etc.)       │
│ 3. Generate tailwindfx-generated.css                        │
│    Example output:                                          │
│    .btn-primary { -fx-background-color: #3b82f6; }          │
│    .btn-primary:hover { -fx-background-color: #2563eb; }    │
│    .bp-md .p-4 { -fx-padding: 16px; }                       │
│    .dark .text-white { -fx-text-fill: #ffffff; }            │
└─────────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────────┐
│ RUNTIME (Application Startup)                               │
├─────────────────────────────────────────────────────────────┤
│ 1. TwInstall.install(scene)                                 │
│    - Load tailwindfx-base.css (~2-5KB)                      │
│ 2. TwInstall.installGenerated(scene, "css/tailwindfx-generated.css") │
│    - Load generated stylesheet                              │
│ 3. TwConfig.preferStylesheet(true)                          │
│    - Enable class-based application mode                    │
└─────────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────────┐
│ RUNTIME (Applying Styles)                                   │
├─────────────────────────────────────────────────────────────┤
│ TwStyle.apply(node, "p-4", "bg-blue-500", "w-[320px]")      │
│    ↓                                                        │
│ ┌───────────────────────────────────────────────────────┐   │
│ │ preferStylesheet = true                               │   │
│ │                                                       │   │
│ │ Static tokens (p-4, bg-blue-500):                     │   │
│ │   → node.getStyleClass().add("p-4")                   │   │
│ │   → node.getStyleClass().add("bg-blue-500")           │   │
│ │   → CSS stylesheet handles the rest                   │   │
│ │                                                       │   │
│ │ Dynamic/arbitrary tokens (w-[320px]):                 │   │
│ │   → Fallback to JIT inline compilation                │   │
│ │   → JitCompiler.compile("w-[320px]")                  │   │
│ │   → node.setStyle("-fx-pref-width: 320px;")           │   │
│ └───────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

**Installation:**
```java
// Standard installation (recommended)
// Loads minimal base CSS + enables JIT compiler
TwInstall.install(scene); 

// Load generated AOT stylesheet (produced by tailwindfx:generate Maven goal)
// Place tailwindfx-generated.css in src/main/resources/css/
TwInstall.installGenerated(scene, "css/tailwindfx-generated.css");

// Enable class-based styling (applies CSS classes instead of inline JIT)
TwConfig.preferStylesheet(true);

// Apply styles
TwStyle.apply(btn, "btn-primary", "rounded-lg", "px-4", "py-2"); 
// → Adds CSS classes: btn.getStyleClass() = ["btn-primary", "rounded-lg", "px-4", "py-2"]
// → Styles applied via stylesheet, NOT inline

TwStyle.apply(node, "bg-blue-500/80", "w-[320px]"); 
// → bg-blue-500/80: static token → CSS class
// → w-[320px]: arbitrary value → JIT inline fallback

// Dark mode support (optional)
TwInstall.installWithDarkMode(scene); // Also loads dark theme overrides
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
    <artifactId>tailwindfx-base</artifactId>
    <version>0.1.0</version>
    <note>This is an early preview release. The artifact is published to OSSRH/Sonatype snapshot repository.</note>
</dependency>
```

**Note:** This is an **early preview** version (`0.1.0`). To use snapshot versions, add the following repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>ossrh-snapshots</id>
        <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
        <releases><enabled>false</enabled></releases>
        <snapshots><enabled>true</enabled></snapshots>
    </repository>
</repositories>
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

        // 2. Build UI with components and utilities
        TwCard card = new TwCard()
            .withTitle("Hello TailwindFX")
            .withContent("Welcome to utility-first JavaFX")
            .apply("w-80");

        TwButton btn = TwButton.primary("Get Started")
            .apply("rounded-lg");
        TwAnimation.onHoverScale(btn, 1.05);

        card.getChildren().add(btn);
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
TwLayout.flexRow().wrap(true).justify(TwFlexPane.Justify.BETWEEN).build();

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

## Known Limitations

TailwindFX is designed to bring Tailwind CSS concepts to JavaFX, but JavaFX has inherent limitations compared to web browsers. Please be aware of the following:

### Partial Implementation ⚠️
The following features detect tokens but require manual handling in your application:

- **Responsive Design**: Tokens like `sm:`, `md:`, `lg:` are detected but require manual implementation using `ResponsiveNode` or scene width bindings
- **Dark Mode**: The `dark:` prefix is recognized but requires manual theme switching via `TwTheme.of(scene).dark().apply()`
- **Transitions**: CSS transition properties are generated but require JavaFX `Timeline` for actual animation effects
- **Animations**: `animate-*` classes are detected but should use `TwAnimation` API for proper JavaFX animations

### Not Supported (JavaFX Limitations) ❌
The following CSS features are **not supported** due to JavaFX platform limitations:

- **`!important` in inline styles**: JavaFX does not support the `!important` modifier in inline styles. Tokens with `!` suffix will compile but the importance flag is ignored
- **CSS custom properties**: Variables like `--my-color` are not supported in JavaFX CSS
- **CSS keyframe animations**: Use `TwAnimation` API instead (`fadeIn()`, `slideUp()`, etc.)
- **Advanced selectors**: Pseudo-selectors like `:has()`, `:where()`, `:is()` are not supported
- **Pseudo-elements**: `::before` and `::after` are not available in JavaFX

These are **platform limitations**, not bugs. Report only issues related to TailwindFX's own logic and implementation.

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
