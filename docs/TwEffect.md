# TwEffect Documentation

The `TwEffect` class is a visual effects facade for JavaFX nodes. It provides access to visual effects including blur, brightness, contrast, grayscale, invert, sepia, and shadow utilities using JavaFX's `javafx.scene.effect` API.

## Overview

`TwEffect` applies visual effects programmatically via `Node.setEffect(Effect)` rather than through inline CSS styles, since JavaFX does not support CSS filter properties directly. All effects use JavaFX's built-in effect classes like `GaussianBlur`, `ColorAdjust`, and `DropShadow`.

## Key Features

- **Blur Effects**: Gaussian blur with configurable radius
- **Color Adjustments**: Brightness, contrast, saturation control
- **Filter Effects**: Grayscale, invert, sepia transformations
- **Shadow Effects**: Drop shadows with customizable parameters
- **Effect Chaining**: Combine multiple effects on a single node
- **Effect Removal**: Clean removal of applied effects

## Basic Usage

### Blur Effects

```java
// Apply Gaussian blur with radius 4
TwEffect.blur(node, 4);

// Apply blur with specific radius
TwEffect.blur(node, 8.0);

// Remove blur effect
TwEffect.blurNone(node);

// Backdrop blur (for containers)
TwEffect.backdropBlur(container, 6.0);
TwEffect.backdropBlurNone(container);
```

### Brightness Adjustment

```java
// Set brightness to 125%
TwEffect.brightness(node, 1.25);

// Set brightness to 75%
TwEffect.brightness(node, 0.75);

// Remove brightness adjustment
TwEffect.brightnessNone(node);

// Apply with percentage string
TwEffect.brightnessWithPercentage(node, "150%"); // 150% brightness
```

### Contrast Adjustment

```java
// Set contrast to 110%
TwEffect.contrast(node, 1.1);

// Set contrast to 80%
TwEffect.contrast(node, 0.8);

// Remove contrast adjustment
TwEffect.contrastNone(node);

// Apply with percentage string
TwEffect.contrastWithPercentage(node, "120%");
```

### Grayscale

```java
// Apply full grayscale
TwEffect.grayscale(node);

// Remove grayscale
TwEffect.grayscaleNone(node);
```

### Invert Colors

```java
// Invert colors
TwEffect.invert(node);

// Remove inversion
TwEffect.invertNone(node);
```

### Sepia Tone

```java
// Apply sepia tone
TwEffect.sepia(node);

// Remove sepia
TwEffect.sepiaNone(node);
```

## Advanced Usage

### Combining Effects

Chain multiple effects together:

```java
// Apply blur and brightness
TwEffect.blur(node, 4);
TwEffect.brightness(node, 1.2);

// Apply grayscale and contrast
TwEffect.grayscale(node);
TwEffect.contrast(node, 1.1);
```

### Custom Effect Chains

Create custom effect combinations:

```java
// Create vintage photo effect
public void applyVintageEffect(Node node) {
    TwEffect.sepia(node);
    TwEffect.contrast(node, 1.2);
    TwEffect.brightness(node, 0.9);
}

// Create dramatic effect
public void applyDramaticEffect(Node node) {
    TwEffect.grayscale(node);
    TwEffect.contrast(node, 1.5);
    TwEffect.brightness(node, 0.8);
}
```

### Effect State Management

Track and restore effect states:

```java
// Store original state
Effect originalEffect = node.getEffect();

// Apply new effect
TwEffect.blur(node, 8);

// Restore original effect
node.setEffect(originalEffect);
```

### Conditional Effects

Apply effects based on conditions:

```java
// Apply blur when loading
if (isLoading) {
    TwEffect.blur(contentNode, 4);
} else {
    TwEffect.blurNone(contentNode);
}

// Apply grayscale when disabled
node.disableProperty().addListener((obs, oldVal, newVal) -> {
    if (newVal) {
        TwEffect.grayscale(node);
    } else {
        TwEffect.grayscaleNone(node);
    }
});
```

## Effect Parameters

### Blur Radius Values

| Method | Radius | Use Case |
|--------|--------|----------|
| `blur(node, 2)` | 2px | Subtle softening |
| `blur(node, 4)` | 4px | Standard blur |
| `blur(node, 8)` | 8px | Strong blur |
| `blur(node, 12)` | 12px | Heavy blur |
| `blur(node, 16)` | 16px | Very heavy blur |

### Brightness/Contrast Values

| Value | Effect |
|-------|--------|
| `0.5` | 50% (darker/lower contrast) |
| `0.75` | 75% (somewhat darker/lower) |
| `1.0` | 100% (normal - no effect) |
| `1.25` | 125% (brighter/higher contrast) |
| `1.5` | 150% (much brighter/higher) |
| `2.0` | 200% (very bright/high) |

## Common Patterns

### Image Hover Effect

```java
ImageCard card = new ImageCard(image);

// Apply subtle effects on hover
card.setOnMouseEntered(e -> {
    TwEffect.brightness(card, 1.1);
    TwEffect.contrast(card, 1.05);
});

card.setOnMouseExited(e -> {
    TwEffect.brightnessNone(card);
    TwEffect.contrastNone(card);
});
```

### Loading State

```java
public void setLoadingState(Node content, boolean loading) {
    if (loading) {
        TwEffect.blur(content, 4);
        content.setOpacity(0.5);
    } else {
        TwEffect.blurNone(content);
        content.setOpacity(1.0);
    }
}
```

### Disabled State Enhancement

```java
public void enhanceDisabledState(Node node) {
    node.disableProperty().addListener((obs, oldVal, newVal) -> {
        if (newVal) {
            TwEffect.grayscale(node);
            node.setOpacity(0.5);
        } else {
            TwEffect.grayscaleNone(node);
            node.setOpacity(1.0);
        }
    });
}
```

### Focus Indicator

```java
// Add glow effect on focus
inputField.focusedProperty().addListener((obs, oldVal, newVal) -> {
    if (newVal) {
        // Could add custom drop shadow here
        DropShadow glow = new DropShadow(10, Color.BLUE);
        inputField.setEffect(glow);
    } else {
        inputField.setEffect(null);
    }
});
```

## Performance Considerations

1. **Effect Cost**: Visual effects can be GPU-intensive; use sparingly on many nodes
2. **Animation**: Avoid animating effect parameters frequently
3. **Removal**: Always remove effects when no longer needed to free resources
4. **Batching**: Apply multiple effects in a single operation when possible

## Best Practices

1. **Use Moderately**: Effects should enhance UX, not distract
2. **Consistent Application**: Apply similar effects consistently across similar elements
3. **Accessibility**: Ensure effects don't impair readability or usability
4. **Test Performance**: Monitor frame rates when using multiple effects
5. **Clean Up**: Remove effects when nodes are removed from scene graph

## Error Handling

```java
try {
    TwEffect.blur(node, 8.0);
} catch (NullPointerException e) {
    System.err.println("Node is null: " + e.getMessage());
}
```

## See Also

- `TwStyle` - Apply utility classes including some effect-related utilities
- `TwAnimation` - Animation effects and transitions
- JavaFX `GaussianBlur` - Underlying blur implementation
- JavaFX `ColorAdjust` - Underlying color adjustment implementation
- JavaFX `DropShadow` - Shadow effects
