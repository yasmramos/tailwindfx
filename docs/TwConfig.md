# TwConfig Documentation

The `TwConfig` class is the global configuration facade for TailwindFX. It provides access to global settings including unit size, debug mode, and performance options.

## Overview

`TwConfig` is a singleton configuration class that manages global TailwindFX settings. All configuration is static and application-wide, affecting all nodes and scenes using TailwindFX.

## Key Features

- **Unit Size Configuration**: Base multiplier for spacing utilities
- **Debug Mode**: Enable/disable debug logging and diagnostics
- **Stylesheet Preference**: Toggle between JIT compilation and stylesheet-based styling
- **Singleton Access**: Global configuration via static methods
- **Reset Support**: Reset all settings to defaults

## Configuration Options

### Unit Size

The unit size is the base multiplier for spacing utilities. By default, it's set to `4.0` pixels, which means:
- `p-1` = 4px (1 × 4)
- `p-2` = 8px (2 × 4)
- `p-4` = 16px (4 × 4)

```java
// Get current unit size
double currentUnit = TwConfig.unit(); // Returns 4.0 by default

// Set custom unit size
TwConfig.unit(8.0); // Now p-1 = 8px, p-2 = 16px, etc.

// Validation: throws IllegalArgumentException if value <= 0
TwConfig.unit(-1.0); // Throws exception
```

### Debug Mode

Enable or disable debug logging and diagnostics:

```java
// Check if debug mode is enabled
boolean isDebug = TwConfig.isDebug(); // Returns false by default

// Enable debug mode
TwConfig.debug(true);

// Disable debug mode
TwConfig.debug(false);
```

When debug mode is enabled:
- Detailed logging of style applications
- Performance metrics output
- Warning messages for potential issues
- Additional validation checks

### Stylesheet Preference

Control whether to prefer stylesheet-based styling over JIT compilation:

```java
// Check current preference
boolean preferStylesheet = TwConfig.isPreferStylesheet(); // Returns false by default

// Prefer stylesheet-based styling
TwConfig.preferStylesheet(true);

// Prefer JIT compilation (default)
TwConfig.preferStylesheet(false);
```

When `preferStylesheet(true)`:
- Uses pre-generated CSS classes when available
- Falls back to JIT for arbitrary values
- Better performance for standard utilities

When `preferStylesheet(false)` (default):
- Uses JIT compilation for dynamic values
- More flexible for runtime customization

## Basic Usage

### Standard Configuration

```java
@Override
public void start(Stage stage) {
    // Set custom unit size before any styling
    TwConfig.unit(8.0);
    
    // Enable debug mode during development
    TwConfig.debug(true);
    
    Scene scene = new Scene(root, 800, 600);
    TwInstall.install(scene);
    
    stage.setScene(scene);
    stage.show();
}
```

### Production Configuration

```java
public class ProductionApp extends Application {
    
    @Override
    public void init() {
        // Use default unit size
        TwConfig.unit(4.0);
        
        // Disable debug mode for production
        TwConfig.debug(false);
        
        // Prefer stylesheet for better performance
        TwConfig.preferStylesheet(true);
    }
    
    @Override
    public void start(Stage stage) {
        // Application setup...
    }
}
```

### Dynamic Configuration

```java
// Adjust unit size based on screen DPI
double dpiScale = GraphicsEnvironment.getLocalGraphicsEnvironment()
    .getDefaultScreenDevice()
    .getDefaultConfiguration()
    .getNormalizingTransform()
    .getScaleX();

TwConfig.unit(4.0 * dpiScale);

// Enable debug mode conditionally
if (isDevelopmentMode()) {
    TwConfig.debug(true);
}
```

## Advanced Usage

### Reset to Defaults

Reset all configuration options to their default values:

```java
TwConfig.reset();
// After reset:
// - unit() = 4.0
// - isDebug() = false
// - isPreferStylesheet() = false
```

### Configuration Builder Pattern

Create a configuration helper for your application:

```java
public class AppConfig {
    
    public static void configureForDevelopment() {
        TwConfig.unit(8.0);
        TwConfig.debug(true);
        TwConfig.preferStylesheet(false);
    }
    
    public static void configureForProduction() {
        TwConfig.unit(4.0);
        TwConfig.debug(false);
        TwConfig.preferStylesheet(true);
    }
    
    public static void configureForHighDPI(double scaleFactor) {
        TwConfig.unit(4.0 * scaleFactor);
        TwConfig.debug(false);
    }
}
```

### Testing with Different Configurations

```java
@Test
public void testWithDifferentUnitSizes() {
    // Test with small unit
    TwConfig.unit(2.0);
    verifyLayout(50, 100); // Expected dimensions with 2px unit
    
    // Test with large unit
    TwConfig.unit(8.0);
    verifyLayout(200, 400); // Expected dimensions with 8px unit
    
    // Reset after test
    TwConfig.reset();
}
```

## Configuration Values

| Option | Default | Valid Range | Description |
|--------|---------|-------------|-------------|
| `unit()` | `4.0` | `> 0.0` | Base unit size in pixels |
| `isDebug()` | `false` | `true/false` | Enable debug logging |
| `isPreferStylesheet()` | `false` | `true/false` | Prefer stylesheet over JIT |

## Best Practices

1. **Configure Early**: Set configuration options before any styling operations
2. **Unit Size Consistency**: Choose a unit size and stick with it throughout the application
3. **Debug in Development**: Enable debug mode during development, disable in production
4. **Performance Tuning**: Use `preferStylesheet(true)` in production for better performance
5. **Thread Safety**: Configuration is thread-safe but should be set during application initialization

## Common Use Cases

### Responsive Design

```java
// Adjust unit size based on window size
stage.widthProperty().addListener((obs, oldVal, newVal) -> {
    if (newVal.doubleValue() < 600) {
        TwConfig.unit(3.0); // Smaller units for mobile
    } else {
        TwConfig.unit(4.0); // Standard units for desktop
    }
});
```

### Accessibility Scaling

```java
// Allow users to adjust UI scale
Slider scaleSlider = new Slider(0.5, 2.0, 1.0);
scaleSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
    TwConfig.unit(4.0 * newVal.doubleValue());
});
```

### Multi-Monitor Support

```java
// Detect primary monitor DPI
GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment()
    .getDefaultScreenDevice();
GraphicsConfiguration config = device.getDefaultConfiguration();
AffineTransform transform = config.getNormalizingTransform();
double scaleFactor = transform.getScaleX();

TwConfig.unit(4.0 * scaleFactor);
```

## Error Handling

```java
try {
    TwConfig.unit(-1.0); // Invalid value
} catch (IllegalArgumentException e) {
    System.err.println("Invalid unit size: " + e.getMessage());
    TwConfig.unit(4.0); // Fallback to default
}
```

## See Also

- `TwInstall` - Install TailwindCSS stylesheets
- `TwStyle` - Apply utility classes
- `ThemeConfig` - Theme configuration
- `JitCompiler` - Just-In-Time compilation
- `TailwindFXMetrics` - Performance metrics
