# TwInstall Documentation

The `TwInstall` class is the installation facade for CSS stylesheets in JavaFX applications using TailwindFX. It handles installing minimal TailwindFX CSS files into JavaFX scenes, with base variables generated dynamically from `ThemeConfig`.

## Overview

`TwInstall` provides a simple API for installing TailwindCSS stylesheets in JavaFX scenes. Most utilities are JIT-compiled at runtime via `JitCompiler`, while base variables are generated dynamically from `ThemeConfig`. The class ensures proper cascade order when multiple stylesheets are installed.

## Key Features

- **Base CSS Installation**: Installs core TailwindFX variables and reset styles
- **Generated Stylesheet Support**: Installs build-time generated utility classes from Maven plugin
- **Minimal Installation**: Lightweight installation for JIT-only workflows
- **Dark Mode Support**: Optional dark mode stylesheet installation
- **Cascade Order Management**: Ensures proper stylesheet ordering for CSS specificity
- **Stage Integration**: Optional stage parameter for advanced configurations

## Installation Methods

### Basic Installation

```java
// Install base CSS with default settings
TwInstall.install(scene);

// Install with stage reference
TwInstall.install(scene, stage);
```

### Minimal Installation

For applications that rely primarily on JIT compilation:

```java
// Install only essential base variables
TwInstall.installMinimal(scene);
```

### Generated Stylesheet Installation

Installs the build-time generated stylesheet produced by the Maven plugin:

```java
// Install with default path
TwInstall.installGenerated(scene);

// Install with custom path
TwInstall.installGenerated(scene, "css/tailwindfx-generated.css");
TwInstall.installGenerated(scene, "/styles/custom-tailwind.css");
```

### Base CSS Installation

Installs only the base CSS containing variables and reset:

```java
TwInstall.installBase(scene);
```

### Dark Mode Installation

Installs dark mode support:

```java
TwInstall.installDark(scene);
```

## Installation Order

When using multiple installation methods, the cascade order is critical:

1. **Base CSS** (`installBase`) - Installed first, contains variables and reset
2. **Generated Stylesheet** (`installGenerated`) - Installed second, can override base utilities
3. **Dark Mode** (`installDark`) - Installed last for proper dark mode overrides

Example of correct installation order:

```java
// Correct order for full installation
TwInstall.installBase(scene);
TwInstall.installGenerated(scene, "css/tailwindfx-generated.css");
TwInstall.installDark(scene);
```

Or simply use the convenience method:

```java
// This handles the order automatically
TwInstall.install(scene);
```

## Usage Examples

### Standard Application Setup

```java
@Override
public void start(Stage stage) {
    Scene scene = new Scene(root, 800, 600);
    
    // Install TailwindCSS
    TwInstall.install(scene);
    
    // Apply utility classes
    TwStyle.apply(button, "bg-blue-500", "text-white", "px-4", "py-2");
    
    stage.setScene(scene);
    stage.show();
}
```

### JIT-Only Workflow

For applications that prefer runtime JIT compilation over pre-generated utilities:

```java
@Override
public void start(Stage stage) {
    Scene scene = new Scene(root, 800, 600);
    
    // Install minimal base variables
    TwInstall.installMinimal(scene);
    
    // Use JIT for all styling
    TwStyle.jit(node, "bg-[#38bdf8]", "p-[13px]");
    
    stage.setScene(scene);
    stage.show();
}
```

### Custom Generated Stylesheet

When using a custom path for the generated stylesheet:

```java
@Override
public void start(Stage stage) {
    Scene scene = new Scene(root, 800, 600);
    
    // Install with custom generated stylesheet location
    TwInstall.installGenerated(scene, "/assets/css/tailwind-custom.css");
    
    stage.setScene(scene);
    stage.show();
}
```

### Dark Mode Support

```java
@Override
public void start(Stage stage) {
    Scene scene = new Scene(root, 800, 600);
    
    // Install base and dark mode support
    TwInstall.install(scene);
    TwInstall.installDark(scene);
    
    // Use dark mode variants
    TwStyle.apply(card, "bg-white", "dark:bg-gray-800");
    
    stage.setScene(scene);
    stage.show();
}
```

## Path Resolution

The `installGenerated` method resolves CSS paths in the following order:

1. **Explicit Path**: Uses the provided path directly
2. **Default Path**: Falls back to `/css/tailwindfx-generated.css` if null or empty
3. **Classpath Resource**: Attempts to load via class loader
4. **TCCL Fallback**: Tries Thread Context Class Loader if primary lookup fails

Path normalization is automatic:
- Paths starting with `/` are used as-is
- Relative paths are prefixed with `/`

## Error Handling

- **Missing Stylesheet**: Logs a warning if the generated stylesheet is not found
- **Null Scene**: Throws `NullPointerException` (validated internally)
- **Invalid Path**: Falls back to default path or logs warning

## Best Practices

1. **Install Early**: Call installation methods before applying any styles
2. **Use Convenience Methods**: Prefer `TwInstall.install(scene)` for standard setups
3. **Respect Cascade Order**: When installing multiple stylesheets, ensure proper order
4. **Minimal for JIT**: Use `installMinimal()` if relying heavily on JIT compilation
5. **Check Resource Existence**: Verify generated CSS exists before deployment

## Integration with Build Tools

### Maven Plugin

When using the TailwindFX Maven plugin, the generated stylesheet is automatically created:

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

Then install it in your application:

```java
TwInstall.installGenerated(scene);
```

## See Also

- `TwStyle` - Apply utility classes to nodes
- `TwCatalog` - Generate utility class catalogs
- `ThemeConfig` - Theme configuration and customization
- `ThemeCssGenerator` - Generate CSS from theme configuration
- `JitCompiler` - Just-In-Time style compilation
- TailwindFX Maven Plugin - Build-time CSS generation
