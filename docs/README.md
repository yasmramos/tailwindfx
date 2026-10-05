# TailwindFX Documentation

Welcome to the TailwindFX documentation. This guide covers all public APIs and features of the TailwindFX library for JavaFX applications.

## Getting Started

### Installation

Add TailwindFX to your project dependencies, then install it in your JavaFX application:

```java
@Override
public void start(Stage stage) {
    Scene scene = new Scene(root, 800, 600);
    
    // Install TailwindCSS
    TwInstall.install(scene);
    
    // Apply utility classes
    TwStyle.apply(button, "bg-blue-500", "text-white", "px-4", "py-2", "rounded");
    
    stage.setScene(scene);
    stage.show();
}
```

### Maven Dependency

```xml
<dependency>
    <groupId>io.github.yasmramos</groupId>
    <artifactId>tailwindfx-base</artifactId>
    <version>${tailwindfx.version}</version>
</dependency>
```

### Basic Setup

1. **Install CSS**: Call `TwInstall.install(scene)` before applying any styles
2. **Apply Utilities**: Use `TwStyle.apply(node, "utility-class")` to style nodes
3. **Configure Theme**: Optionally customize with `TwConfig` and `TwTheme`

## Core API Reference

### Main Entry Point

- **[TailwindFX](TailwindFX.md)** - Main entry point facade providing unified access to all TailwindFX features

### Styling & Layout

- **[TwStyle](TwStyle.md)** - Apply utility classes and JIT-compiled styles to nodes
- **[TwInstall](TwInstall.md)** - Install TailwindCSS stylesheets in JavaFX scenes
- **[TwLayout](TwLayout.md)** - Layout utilities and container management
- **[TwCatalog](TwCatalog.md)** - Generate utility class catalogs and CSS

### Configuration & Theme

- **[TwConfig](TwConfig.md)** - Global configuration (unit size, debug mode, performance)
- **[TwTheme](TwTheme.md)** - Theme management (dark/light mode, presets, scoping)

### Effects & Animation

- **[TwEffect](TwEffect.md)** - Visual effects (blur, brightness, contrast, grayscale, etc.)
- **[TwAnimation](TwAnimation.md)** - Fluent animation API with pre-built effects

### Advanced Features

- **[TwMetrics](TwMetrics.md)** - Performance monitoring and health checks
- **[TwResponsive](TwResponsive.md)** - Responsive design and breakpoint management
- **[TwBatch](TwBatch.md)** - Batch operations for performance optimization
- **[TwFXML](TwFXML.md)** - FXML integration and scene graph processing

## Quick Examples

### Applying Styles

```java
// Standard utility classes
TwStyle.apply(button, "bg-blue-500", "text-white", "p-4", "rounded-lg");

// JIT compilation for arbitrary values
TwStyle.jit(element, "bg-[#38bdf8]", "p-[13px]");

// Remove or toggle classes
TwStyle.remove(node, "old-class");
TwStyle.toggle(node, "active");
```

### Theme Management

```java
// Enable dark mode
TwTheme.of(scene).dark().apply();

// Apply theme preset
TwTheme.scope(pane).preset("blue").apply();

// Save/load themes
TwTheme.saveTheme(scene, "my-theme");
TwTheme.loadTheme(scene, "my-theme");
```

### Animations

```java
// Simple entry animation
TwAnimation.fadeIn(node).play();

// Chained animations
TwAnimation.chain(
    TwAnimation.fadeIn(node),
    TwAnimation.slideUp(node)
).play();

// Hover effects
TwAnimation.onHoverScale(button, 1.05);
```

### Layout

```java
// Flexbox layout
TwFlexPane flex = TwLayout.flexRow();
flex.applyClass("gap-4");
flex.getChildren().addAll(node1, node2);

// Grid layout
TwGridPane grid = TwLayout.grid()
    .cols(3)
    .gap("4")
    .build();

// Apply layout tokens
TwLayout.apply(container, "flex", "justify-center", "items-center");
```

### Configuration

```java
// Set custom unit size
TwConfig.unit(8.0);

// Enable debug mode
TwConfig.debug(true);

// Prefer stylesheet for performance
TwConfig.preferStylesheet(true);
```

### Responsive Design

```java
// Install responsive support
TwResponsive.on(stage);

// Monitor breakpoints
BreakpointManager bpm = BreakpointManager.forStage(stage);
bpm.currentBreakpointProperty().addListener((obs, oldVal, newVal) -> {
    System.out.println("Current breakpoint: " + newVal);
});
```

### Performance

```java
// Batch style operations
TwBatch.run(() -> {
    TwStyle.apply(node1, "p-4", "bg-blue-500");
    TwStyle.apply(node2, "m-2", "text-white");
});

// Monitor metrics
String report = TwMetrics.debugReport(node);
TwMetrics.healthCheck();
```

## Component Documentation

See [Components](components/) for documentation on UI components like `TwButton`, `TwCard`, `TwAlert`, and `TwInput`.

## Integration Guides

### Maven Plugin

Use the TailwindFX Maven Plugin to generate optimized CSS at build time:

```xml
<plugin>
    <groupId>io.github.yasmramos</groupId>
    <artifactId>tailwindfx-maven-plugin</artifactId>
    <version>${tailwindfx.version}</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### FXML Support

Process FXML files with TailwindFX utilities:

```java
Parent root = FXMLLoader.load(getClass().getResource("view.fxml"));
TwFXML.process(root);
TwFXML.enableAutoJit(root);
```

## Best Practices

1. **Install Early**: Call `TwInstall.install()` before applying any styles
2. **Use Standard Utilities**: Prefer predefined classes over JIT for better performance
3. **Batch Operations**: Use `TwBatch.run()` for multiple style applications
4. **Enable Debug in Development**: Use `TwConfig.debug(true)` during development
5. **Clean Up**: Remove unused classes with `TwStyle.remove()`
6. **Leverage Variants**: Use hover, focus, and dark mode variants for interactive elements

## Troubleshooting

### Common Issues

**Styles not applying:**
- Ensure `TwInstall.install(scene)` is called before `TwStyle.apply()`
- Check that the node is in the scene graph when styles are applied

**JIT compilation not working:**
- Verify the token syntax is correct (e.g., `bg-[#38bdf8]`)
- Check debug logs for compilation errors

**Dark mode not working:**
- Call `TwInstall.installDark(scene)` to enable dark mode support
- Use `TwTheme.of(scene).dark().apply()` to activate

### Getting Help

- Check the [GitHub Issues](https://github.com/yasmramos/tailwindfx/issues)
- Review existing documentation pages for detailed API information
- Enable debug mode with `TwConfig.debug(true)` for detailed logs

## Contributing

Contributions are welcome! See the main repository for contribution guidelines.

## License

Apache License 2.0 - See LICENSE file for details.
