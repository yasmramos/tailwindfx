# TailwindFX - Main Entry Point Documentation

The `TailwindFX` class is the main entry point for the TailwindFX library. It provides a lightweight facade that delegates to specialized facades for each responsibility, offering unified access to all TailwindFX features.

## Overview

`TailwindFX` is a convenience class that consolidates access to all TailwindFX functionality through a single import. While you can use individual facades (`TwStyle`, `TwInstall`, `TwTheme`, etc.) directly, `TailwindFX` provides a unified API for common operations.

## Key Features

- **Unified API**: Single class for all TailwindFX operations
- **Delegation Pattern**: Delegates to specialized facades internally
- **Convenience Methods**: Shortcuts for common operations
- **Full Feature Access**: All TailwindFX capabilities available

## Basic Usage

### Styling Nodes

```java
// Apply utility classes
TailwindFX.apply(node, "btn-primary", "rounded-lg");

// Apply JIT-compiled styles
TailwindFX.jit(node, "bg-blue-500/80", "p-[13px]");

// Remove classes
TailwindFX.remove(node, "old-class");

// Toggle classes
TailwindFX.toggle(node, "active");
```

### Installation

```java
// Install TailwindCSS in scene
TailwindFX.install(scene);

// Install with stage reference
TailwindFX.install(scene, stage);

// Install base CSS only
TailwindFX.installBase(scene);

// Install dark mode support
TailwindFX.installDark(scene);
```

### Theme Management

```java
// Enable dark mode
TailwindFX.theme(scene).dark().apply();

// Enable light mode
TailwindFX.theme(scene).light().apply();

// Apply theme preset
TailwindFX.theme(scene).scope(pane).preset("blue").apply();
```

### Layout Operations

```java
// Create flex container
TwFlexPane flex = TailwindFX.flexRow();

// Create grid container
TwGridPane grid = TailwindFX.grid();

// Apply layout tokens
TailwindFX.layout(container, "flex", "justify-center");

// Use layout builder
TailwindFX.layout(container)
    .flex()
    .row()
    .gap(4)
    .apply();
```

### Responsive Design

```java
// Install responsive support
TailwindFX.responsive(stage);

// Monitor breakpoints
BreakpointManager bpm = BreakpointManager.forStage(stage);
bpm.currentBreakpointProperty().addListener((obs, oldVal, newVal) -> {
    System.out.println("Current: " + newVal);
});
```

### Batch Operations

```java
// Execute batch of style operations
TailwindFX.batch(() -> {
    TwStyle.apply(node1, "p-4", "bg-blue-500");
    TwStyle.apply(node2, "m-2", "text-white");
});
```

### Metrics and Monitoring

```java
// Get metrics instance
TailwindFXMetrics metrics = TailwindFX.metrics();

// Generate debug report
String report = TwMetrics.debugReport(node);

// Run health check
TwMetrics.healthCheck();
```

### Configuration

```java
// Set unit size
TailwindFX.unit(8.0);
double currentUnit = TailwindFX.unit();

// Enable debug mode
TailwindFX.debug(true);
boolean isDebug = TailwindFX.isDebug();
```

## Advanced Usage

### Complete Application Setup

```java
public class MyApp extends Application {
    
    @Override
    public void start(Stage stage) {
        // Configure
        TailwindFX.unit(8.0);
        TailwindFX.debug(true);
        
        // Create UI
        VBox root = new VBox(10);
        Label title = new Label("Welcome");
        Button button = new Button("Click Me");
        
        // Style with TailwindFX
        TailwindFX.apply(title, "text-2xl", "font-bold", "text-blue-500");
        TailwindFX.apply(button, "bg-blue-500", "text-white", "px-4", "py-2");
        
        // Install TailwindCSS
        Scene scene = new Scene(root, 400, 300);
        TailwindFX.install(scene);
        
        // Enable responsive design
        TailwindFX.responsive(stage);
        
        // Enable dark mode
        TailwindFX.theme(scene).dark().apply();
        
        stage.setScene(scene);
        stage.show();
    }
}
```

### Builder Pattern Integration

```java
public class UIBuilder {
    
    public static Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        TailwindFX.apply(btn, 
            "bg-blue-500", 
            "text-white",
            "px-4", 
            "py-2",
            "rounded",
            "hover:bg-blue-600"
        );
        return btn;
    }
    
    public static Card createCard(Node content) {
        Card card = new Card(content);
        TailwindFX.apply(card,
            "p-4",
            "rounded-lg",
            "shadow",
            "bg-white"
        );
        return card;
    }
}
```

### Dependency Injection Setup

```java
public class TailwindFXModule {
    
    @Provides
    public Scene provideScene() {
        Scene scene = new Scene(root);
        TailwindFX.install(scene);
        return scene;
    }
    
    @Provides
    public BreakpointManager provideBreakpointManager(Stage stage) {
        TailwindFX.responsive(stage);
        return BreakpointManager.forStage(stage);
    }
}
```

## Method Reference

### Style Methods

| Method | Description | Delegates To |
|--------|-------------|--------------|
| `apply(Node, String...)` | Apply utility classes | `TwStyle.apply()` |
| `jit(Node, String...)` | Apply JIT styles | `TwStyle.jit()` |
| `remove(Node, String...)` | Remove classes | `TwStyle.remove()` |
| `toggle(Node, String)` | Toggle class | `TwStyle.toggle()` |

### Installation Methods

| Method | Description | Delegates To |
|--------|-------------|--------------|
| `install(Scene)` | Install TailwindCSS | `TwInstall.install()` |
| `install(Scene, Stage)` | Install with stage | `TwInstall.install()` |
| `installBase(Scene)` | Install base CSS | `TwInstall.installBase()` |
| `installDark(Scene)` | Install dark mode | `TwInstall.installDark()` |

### Theme Methods

| Method | Description | Delegates To |
|--------|-------------|--------------|
| `theme(Scene)` | Get theme manager | `TwTheme.of()` |

### Layout Methods

| Method | Description | Delegates To |
|--------|-------------|--------------|
| `layout(Pane)` | Get layout builder | `TwLayout.of()` |
| `layout(Node, String...)` | Apply layout tokens | `TwLayout.apply()` |
| `flexRow()` | Create flex row | `TwLayout.flexRow()` |
| `grid()` | Create grid | `TwLayout.gridBuild()` |

### Responsive Methods

| Method | Description | Delegates To |
|--------|-------------|--------------|
| `responsive(Stage)` | Install responsive | `TwResponsive.on()` |

### Batch Methods

| Method | Description | Delegates To |
|--------|-------------|--------------|
| `batch(Runnable)` | Execute batch | `TwBatch.run()` |

### Metrics Methods

| Method | Description | Delegates To |
|--------|-------------|--------------|
| `metrics()` | Get metrics | `TailwindFXMetrics.getInstance()` |

### Configuration Methods

| Method | Description | Delegates To |
|--------|-------------|--------------|
| `unit()` | Get unit size | `TwConfig.unit()` |
| `unit(double)` | Set unit size | `TwConfig.unit()` |
| `debug(boolean)` | Set debug mode | `TwConfig.debug()` |
| `isDebug()` | Check debug mode | `TwConfig.isDebug()` |

## Best Practices

1. **Use Individual Facades for Clarity**: While `TailwindFX` provides unified access, using specific facades (`TwStyle`, `TwInstall`, etc.) can make code more readable and explicit.

2. **Import Once**: Import `TailwindFX` once and use it throughout your application for consistency.

3. **Combine with Specific Facades**: Use `TailwindFX` for common operations and specific facades for advanced features.

4. **Document Usage**: When using `TailwindFX`, document which underlying facade is being used for complex operations.

## Migration from Individual Facades

If you're currently using individual facades:

```java
// Before (individual facades)
TwStyle.apply(node, "bg-blue-500");
TwInstall.install(scene);
TwTheme.of(scene).dark().apply();

// After (unified TailwindFX)
TailwindFX.apply(node, "bg-blue-500");
TailwindFX.install(scene);
TailwindFX.theme(scene).dark().apply();
```

Both approaches are valid; choose based on your team's preference for explicit vs. unified APIs.

## See Also

- `TwStyle` - Style application facade
- `TwInstall` - Installation facade
- `TwTheme` - Theme management facade
- `TwLayout` - Layout utilities facade
- `TwResponsive` - Responsive design facade
- `TwBatch` - Batch operations facade
- `TwMetrics` - Metrics and monitoring facade
- `TwConfig` - Configuration facade
