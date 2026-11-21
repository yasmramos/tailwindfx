# Entry Points

TailwindFX exposes one small facade per responsibility instead of a single "god" entry point.
Each facade is a class with static methods that delegates to the specialised packages.

| Facade | Responsibility |
|--------|----------------|
| `TwInstall` | Install stylesheets into a `Scene` |
| `TwStyle` | Apply, remove and toggle utility classes |
| `TwTheme` | Theme management (dark/light, presets, scopes) |
| `TwLayout` | Layout utilities and container builders |
| `TwConfig` | Global configuration (unit size, debug, stylesheet preference) |
| `TwEffect` | Visual effects that have no CSS equivalent |
| `TwAnimation` | Fluent animation API |
| `TwBatch` | Batch style operations |
| `TwResponsive` | Responsive helpers and breakpoints |
| `TwCatalog` | Utility-class catalog and full CSS generation |
| `TwMetrics` | Performance metrics and health checks |
| `TwFXML` | FXML integration |

## Basic usage

### Installation

```java
// Install the base stylesheet
TwInstall.install(scene);

// Install with a stage reference (also attaches breakpoint tracking)
TwInstall.install(scene, stage);

// Install only the base variables
TwInstall.installBase(scene);

// Install a build-time generated stylesheet
TwInstall.installGenerated(scene, "/tailwindfx-generated.css");
```

### Styling nodes

```java
// Apply utility classes; arbitrary values are JIT-compiled automatically
TwStyle.apply(node, "btn-primary", "rounded-lg");
TwStyle.apply(node, "bg-blue-500/80", "p-[13px]");

// Remove or toggle classes
TwStyle.remove(node, "old-class");
TwStyle.toggle(node, "active");
```

### Themes

```java
TwTheme.of(scene).dark().apply();
TwTheme.of(scene).light().apply();
TwTheme.of(scene).preset("blue").apply();

// Scope a theme to a subtree
TwTheme.scope(panel).accent("#ef4444").apply();
```

### Layout

```java
// Create a flex container
TwFlexPane flex = TwLayout.flexRow();

// Create a grid container
TwGridPane grid = TwLayout.gridBuild();

// Apply layout tokens
TwLayout.apply(container, "flex", "justify-center");

// Use the layout builder
TwLayout.of(container).row().gap(4).build();
```

### Responsive design

```java
TwResponsive.on(stage);

BreakpointManager bpm = TwResponsive.forStage(stage);
bpm.activeBreakpointProperty().addListener((obs, oldVal, newVal) ->
    System.out.println("Current: " + newVal));
```

### Batch operations

```java
TwBatch.run(() -> {
    TwStyle.apply(node1, "p-4", "bg-blue-500");
    TwStyle.apply(node2, "m-2", "text-white");
});
```

### Metrics and monitoring

```java
String report = TwMetrics.debugReport(node);
TwMetrics.healthCheck();
TwMetrics.setEnabled(true);
```

### Configuration

```java
TwConfig.unit(8.0);
TwConfig.debug(true);
TwConfig.preferStylesheet(true);
```

## Complete application setup

```java
public class MyApp extends Application {

    @Override
    public void start(Stage stage) {
        TwConfig.debug(true);

        VBox root = new VBox(10);
        Label title = new Label("Welcome");
        Button button = new Button("Click Me");

        TwStyle.apply(title, "text-2xl", "font-bold", "text-blue-500");
        TwStyle.apply(button, "bg-blue-500", "text-white", "px-4", "py-2");

        Scene scene = new Scene(root, 400, 300);
        TwInstall.install(scene);

        TwResponsive.on(stage);
        TwTheme.of(scene).dark().apply();

        stage.setScene(scene);
        stage.show();
    }
}
```

## Notes

- `TwStyle.apply` is the single styling entry point. There is no separate `jit` method: tokens such
  as `bg-[#38bdf8]` are detected and compiled automatically.
- `TwInstall.installDark` is a deprecated no-op kept for source compatibility. Switch dark mode with
  `TwTheme.of(scene).dark().apply()`.

## See also

- [TwInstall](TwInstall.md) - installation facade
- [TwStyle](TwStyle.md) - style application facade
- [TwTheme](TwTheme.md) - theme facade
- [TwLayout](TwLayout.md) - layout facade
- [TwResponsive](TwResponsive.md) - responsive facade
- [TwBatch](TwBatch.md) - batch facade
- [TwMetrics](TwMetrics.md) - metrics facade
- [TwConfig](TwConfig.md) - configuration facade
- [Components](components/README.md) - pre-built UI components
