# TailwindFX

> Utility-first UI framework for JavaFX, inspired by Tailwind CSS.

[![Java](https://img.shields.io/badge/Java-21%2B-blue)](https://openjdk.org/projects/jdk/21/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21%2B-green)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

---

## What is TailwindFX?

TailwindFX brings Tailwind CSS's utility-first approach to JavaFX. Instead of writing boilerplate style code, you compose styles from a comprehensive set of pre-built utility classes — and where CSS falls short, TailwindFX provides equivalent Java APIs.

```java
// Before TailwindFX
Button btn = new Button("Submit");
btn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; "
    + "-fx-background-radius: 8; -fx-padding: 8 16 8 16;");
btn.setOnMouseEntered(e -> btn.setStyle(...));

// With TailwindFX
Button btn = new Button("Submit");
TailwindFX.apply(btn, "btn-primary", "rounded-lg", "px-4", "py-2");
AnimationUtil.onHoverScale(btn, 1.05);
```

---

## Features

| Feature | Description |
|---|---|
| **1 400+ CSS utilities** | Layout, typography, colors, shadows, effects, transforms |
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
    <groupId>io.github.tailwindfx</groupId>
    <artifactId>tailwindfx</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Manual

1. Copy all `.java` files from `src/` to your project's `tailwindfx` package.
2. Copy `tailwindfx.css` to your resources root.
3. Ensure JavaFX 21+ is on the classpath.

---

## Quick Start

```java
public class MyApp extends Application {
    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 900, 600);

        // 1. Install (loads CSS + wires breakpoints)
        TailwindFX.install(scene, stage);

        // 2. Build UI with utilities
        VBox card = new VBox(12);
        TailwindFX.apply(card, "card", "w-80");

        Label title = new Label("Hello TailwindFX");
        TailwindFX.apply(title, "text-2xl", "font-bold", "text-blue-600");

        Button btn = new Button("Get Started");
        TailwindFX.apply(btn, "btn-primary", "rounded-lg");
        AnimationUtil.onHoverScale(btn, 1.05);

        card.getChildren().addAll(title, btn);
        root.getChildren().add(card);

        stage.setScene(scene);
        stage.show();
    }
}
```

---

## Core APIs

### Applying utilities

```java
// Single or multiple classes:
TailwindFX.apply(node, "p-4", "bg-white", "rounded-lg", "shadow-md");

// JIT — arbitrary values compiled at runtime:
TailwindFX.apply(node, "bg-blue-500/80", "p-[13px]", "drop-shadow-[#3b82f6]");

// No conflict-resolution (accumulate intentionally):
TailwindFX.applyRaw(node, "w-4", "w-8");  // both stay

// StyleDiff — skip if classes unchanged:
TailwindFX.applyDiff(node, "btn-primary", "rounded-lg");  // no-op on 2nd call
```

### Responsive

```java
// Per-node rules (uses Scene.widthProperty):
TailwindFX.responsive(sidebar)
    .base("w-64", "flex-col")
    .sm("w-full")
    .md("w-48")
    .onBreakpoint(bp -> System.out.println("Breakpoint: " + bp))
    .install(scene);

// Scene-level breakpoints:
BreakpointManager bpm = TailwindFX.responsive(stage);
bpm.onBreakpoint(BreakpointManager.Breakpoint.MD,
    () -> container.setDirection(FxFlexPane.Direction.ROW));
```

### FxFlexPane

```java
FxFlexPane flex = TailwindFX.flexRow()
    .wrap(true)
    .justify(FxFlexPane.Justify.BETWEEN)
    .align(FxFlexPane.Align.CENTER)
    .gap(16);

FxFlexPane.setGrow(mainContent, 1);     // flex-grow: 1
FxFlexPane.setShrink(sidebar, 0);       // flex-shrink: 0
FxFlexPane.setBasis(child, 0);          // flex-basis: 0
FxFlexPane.setAlignSelf(btn, FxFlexPane.Align.END); // align-self: end
FxFlexPane.setOrder(header, -1);        // order: -1 (first)

// Animated direction change:
flex.setDirectionAnimated(FxFlexPane.Direction.COL, 200);
```

### FxGridPane

```java
FxGridPane page = FxGridPane.create()
    .areas(
        "header header",
        "sidebar main",
        "footer footer"
    )
    .gap(12).build();

page.placeIn(headerRegion,  "header");
page.placeIn(sidebarRegion, "sidebar");
page.placeIn(mainContent,   "main");
page.placeIn(footerRegion,  "footer");

// Masonry layout:
FxGridPane pins = FxGridPane.create().masonry(3).gap(12).build();
```

### FxDataTable

```java
FxDataTable<User> table = TailwindFX.dataTable(User.class)
    .column("Name",  User::name)
    .column("Email", User::email)
    .column("Age",   u -> String.valueOf(u.age()))
    .searchable(true)
    .pageSize(25)
    .style("table-striped", "table-hover")
    .build();

table.setItems(userList);
table.setFilter(u -> u.dept().equals("Engineering"));
root.getChildren().add(table.container());
```

### Themes

```java
// Global theme:
TailwindFX.theme(scene).dark().apply();
TailwindFX.theme(scene).preset("blue").apply();
TailwindFX.saveTheme(scene, "myapp.theme");
TailwindFX.loadTheme(scene, "myapp.theme");

// Scoped theme (subtree only):
TailwindFX.scope(panel).preset("rose").apply();
TailwindFX.inheritScope(triggerButton, modal); // modal inherits trigger's scope
TailwindFX.refreshScope(panel);                // after reparenting
```

### Animations

```java
AnimationUtil.fadeIn(node, 300).play();
AnimationUtil.slideUp(node).play();
AnimationUtil.shake(button).play();        // validation error
AnimationUtil.spin(loadingIcon).loop().play();

AnimationUtil.onHoverScale(btn, 1.05);    // permanent hover scale
AnimationUtil.onHoverLift(btn);           // hover lift (-4px)
AnimationUtil.onHoverDim(btn, 0.8);       // hover dim
AnimationUtil.removeHoverEffects(btn);    // clean up all hover effects

// Chain / parallel:
AnimationUtil.chain(fadeIn, slideUp).play();
AnimationUtil.parallel(pulse, bounce).play();

// Motion reduce:
TailwindFX.setReducedMotion(true);
TailwindFX.playIfMotionOk(animation);  // plays or jumps to end state
```

### Tailwind v4.1

```java
// Text shadow:
TailwindFX.textShadowMd(heading);
TailwindFX.textShadow(label, "#3b82f6", 6, 0, 2);  // colored

// Colored drop shadow:
TailwindFX.dropShadowBlue(card);
TailwindFX.dropShadow(card, "#22c55e", 0.4, 12, 0, 4);

// Clip/mask:
TailwindFX.clipCircle(avatar);
TailwindFX.clipRounded(imageView, 12);

// 3D transforms:
TailwindFX.rotateX(panel, 15);
TailwindFX.rotateY(card, 30);
TailwindFX.translateZ(tooltip, 50);

// Glass / neumorph:
TailwindFX.glass(overlayPane);
TailwindFX.backdropBlurMd(overlayPane);
TailwindFX.neumorph(button);

// SVG:
TailwindFX.fill(svgPath, "#3b82f6");
TailwindFX.stroke(svgPath, "#000000");
```

### Metrics & alerts

```java
TailwindFX.metrics()
    .setEnabled(true)
    .onAlert((metric, value, threshold) ->
        System.err.printf("Alert: %s=%.2f%n", metric, value))
    .alertOnLowCacheHitRatio(0.70)
    .alertOnHighConflictRate(0.30)
    .alertOnSlowCompile(0.5);   // ms

TailwindFX.metrics().print(); // formatted report
```

### Performance

```java
// Batch apply (1 CSS pass for many nodes):
TailwindFX.batch(() ->
    cards.forEach(c -> TailwindFX.apply(c, "card", "shadow-md")));

// Configure auto-batch threshold:
TailwindFX.configure().autoBatch(20);

// Cleanup on node removal:
TailwindFX.cleanupNode(removedNode);   // explicit
TailwindFX.autoCleanup(cellNode);      // auto on scene removal
```

---

## Running tests

```bash
# Pure-Java suites (no display needed):
java -cp . tailwindfx.TailwindFXTest

# FxFlexPane layout math:
java -cp . tailwindfx.FxFlexPaneTest

# All suites including FX-thread tests:
java -cp . tailwindfx.TestRunner
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
