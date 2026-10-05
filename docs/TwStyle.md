# TwStyle Documentation

The `TwStyle` class is the primary facade for applying TailwindCSS utility classes and JIT-compiled styles to JavaFX nodes. It provides a comprehensive API for styling nodes with both predefined CSS classes and dynamic JIT (Just-In-Time) compiled styles.

## Overview

`TwStyle` handles applying, removing, and toggling CSS classes and JIT-compiled styles on JavaFX nodes. It integrates with multiple core components including `JitCompiler`, `TokenParser`, `UtilityConflictResolver`, `VariantManager`, and `EffectApplier` to provide a complete styling solution.

## Key Features

- **Utility Class Application**: Apply standard TailwindCSS utility classes
- **JIT Compilation**: Support for arbitrary values like `bg-[#38bdf8]` or `p-[13px]`
- **Variant Support**: Hover, focus, dark mode variants via `VariantManager`
- **Effect Integration**: Filter effects (blur, grayscale, invert) via `EffectApplier`
- **Layout-aware Styling**: Handles layout-dependent properties requiring parent context
- **Conflict Resolution**: Automatic resolution of conflicting utility classes
- **Performance Caching**: Built-in style caching via `StyleCache`
- **Metrics Tracking**: Performance metrics via `TailwindFXMetrics`

## Basic Usage

### Applying Styles

```java
// Apply standard utility classes
TwStyle.apply(node, "btn-primary", "rounded-lg");

// Apply multiple classes at once
TwStyle.apply(button, "bg-blue-500", "text-white", "px-4", "py-2", "rounded");

// Apply JIT-compiled styles with arbitrary values
TwStyle.jit(node, "bg-blue-500/80", "p-[13px]");

// Apply arbitrary color values
TwStyle.jit(element, "bg-[#38bdf8]", "text-[#1e3a8a]");
```

### Removing Styles

```java
// Remove specific classes
TwStyle.remove(node, "old-class");

// Remove multiple classes
TwStyle.remove(button, "bg-red-500", "text-bold");
```

### Toggling Styles

```java
// Toggle a single class
TwStyle.toggle(node, "active");

// Toggle based on condition
TwStyle.toggle(node, "disabled", isDisabled);
```

### JIT Compilation

```java
// Arbitrary background color with opacity
TwStyle.jit(node, "bg-blue-500/80");

// Arbitrary padding value
TwStyle.jit(node, "p-[13px]");

// Arbitrary border radius
TwStyle.jit(node, "rounded-[7px]");

// Combined JIT tokens
TwStyle.jit(element, "bg-[#38bdf8]", "text-[#1e3a8a]", "m-[10px]");
```

## Application Order

When `apply()` is called, styles are applied in the following sequence:

1. **Effects**: Filter effects like blur, grayscale, invert via `EffectApplier`
2. **CSS Classes**: Standard utility classes via `UtilityConflictResolver`
3. **Layout Migration Warning**: Detects legacy layout tokens requiring container migration
4. **Layout-dependent Styles**: Margins, gaps, flex properties requiring parent context
5. **Variants**: Hover, focus, dark mode variants via `VariantManager`
6. **Inline JIT Styles**: Dynamically compiled styles applied directly

## Advanced Features

### Layout Properties

```java
// HBox with spacing and growth
HBox hbox = new HBox();
TwStyle.apply(hbox, "flex", "gap-4");
TwStyle.apply(childNode, "flex-grow");

// VBox with priority
VBox vbox = new VBox();
TwStyle.apply(vbox, "flex", "flex-col");
TwStyle.apply(childNode, "vbox-grow", Priority.ALWAYS);

// GridPane constraints
GridPane grid = new GridPane();
TwStyle.apply(cellNode, "grid-col-span-2");
```

### Variant Support

```java
// Hover variant
TwStyle.apply(button, "hover:bg-blue-600");

// Focus variant
TwStyle.apply(input, "focus:ring-2", "focus:ring-blue-500");

// Dark mode variant
TwStyle.apply(card, "dark:bg-gray-800", "dark:text-white");
```

### Effect Integration

```java
// Apply blur effect
TwStyle.apply(node, "blur", "blur-sm", "blur-md", "blur-lg");

// Apply grayscale
TwStyle.apply(image, "grayscale", "grayscale-50");

// Apply invert
TwStyle.apply(element, "invert", "invert-90");
```

## Best Practices

1. **Use Standard Classes When Possible**: Prefer predefined utility classes over JIT compilation for better performance and cache utilization.

2. **Group Related Classes**: Apply related classes together to reduce parsing overhead:
   ```java
   TwStyle.apply(button, "bg-blue-500", "text-white", "px-4", "py-2");
   ```

3. **Clean Up Unused Styles**: Remove classes that are no longer needed to prevent style bloat:
   ```java
   TwStyle.remove(node, "loading-state");
   ```

4. **Use Variants Appropriately**: Leverage hover, focus, and dark mode variants for interactive elements:
   ```java
   TwStyle.apply(button, "bg-blue-500", "hover:bg-blue-600", "focus:ring-2");
   ```

5. **Cache Frequently Used Combinations**: The internal `StyleCache` automatically caches parsed tokens, but grouping commonly used combinations improves lookup performance.

## Performance Considerations

- **Caching**: `TwStyle` uses `StyleCache` to cache parsed tokens for repeated use
- **Batch Operations**: Apply multiple classes in a single call to reduce overhead
- **JIT Compilation**: Arbitrary values require compilation; prefer standard utilities when possible
- **Metrics**: Track performance with `TailwindFXMetrics` for optimization insights

## Error Handling

`TwStyle` includes robust validation via `Preconditions`:
- Null node checks
- Invalid token detection
- Layout constraint violations

Errors are logged via Java's `Logger` with appropriate severity levels.

## See Also

- `TwInstall` - Install TailwindCSS stylesheets in scenes
- `TwCatalog` - Generate utility class catalogs
- `TwTheme` - Theme configuration and customization
- `TwEffect` - Visual effects and filters
- `TwLayout` - Layout utilities and containers
- `JitCompiler` - Just-In-Time style compilation
- `VariantManager` - State-based variant handling
