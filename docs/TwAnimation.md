# TwAnimation Class Documentation

The `TwAnimation` class is a fluent wrapper around JavaFX `Animation` that provides a clean, chainable API for configuring and controlling animations in TailwindFX. It serves as the primary interface for animation operations, offering method chaining for common animation properties and integration with the `AnimationRegistry` for automatic lifecycle management.

## Overview

`TwAnimation` eliminates the need for direct JavaFX animation imports in user code by providing a declarative API over `Timeline`, `KeyFrame`, and `Interpolator`. All animations are ready-to-play upon creation and can be further configured using a fluent interface.

## Key Features

- **Zero JavaFX Animation Imports**: User code only imports `TwAnimation`
- **Fluent API**: Chain configuration methods like `.cycleCount()`, `.onFinished()`, `.easeOut()`
- **Pre-built Animations**: Ready-to-use entry, exit, and effect animations
- **Slot-based Registry**: Automatic lifecycle management via semantic slots
- **Hover Effects**: Built-in hover-based animations (scale, lift, dim)
- **Chaining Support**: Sequential (`chain`) and parallel (`parallel`) animation composition
- **Responsive Guards**: Prevents animations during layout changes

## Duration Constants

```java
TwAnimation.FAST    // 150ms
TwAnimation.NORMAL  // 250ms (default)
TwAnimation.SLOW    // 400ms
TwAnimation.SLOWER  // 600ms
```

## Basic Usage

### Simple Entry Animation

```java
// Fade in with default duration (250ms)
TwAnimation.fadeIn(button).play();

// Fade in with custom duration
TwAnimation.fadeIn(node, 300).play();

// Slide up from below
TwAnimation.slideUp(node).play();

// Scale in with pop effect
TwAnimation.scaleIn(node).play();
```

### Chained Configuration

```java
// Pulse animation repeated 3 times
TwAnimation.pulse(badge)
    .cycleCount(3)
    .play();

// Spin animation looping indefinitely
TwAnimation.spin(node)
    .loop()
    .play();

// Fade in with custom speed
TwAnimation.fadeIn(node)
    .speed(2.0)
    .play();
```

### Multiple Animations

```java
// Sequential chain: fade in then slide up
TwAnimation.chain(
    TwAnimation.fadeIn(node),
    TwAnimation.slideUp(node, 200)
).play();

// Parallel execution: fade and scale simultaneously
TwAnimation.parallel(
    TwAnimation.fadeIn(node),
    TwAnimation.scaleIn(node)
).play();
```

### Registry-Controlled Animations

`TwAnimation` uses a slot-based registry system to manage animations. Each slot governs a semantic category:

```java
// Register animation in specific slot
TwAnimation.fadeIn(node)
    .register(node, "enter")
    .play();

// Check if animation is active in slot
boolean isActive = TwAnimation.AnimationRegistry.isActive(node, "enter");

// Cancel animation in specific slot
TwAnimation.AnimationRegistry.cancel(node, "enter");

// Cancel all animations on node
TwAnimation.AnimationRegistry.cancelAll(node);
```

### Easing Functions

```java
// Ease out (smooth deceleration - default for entry animations)
TwAnimation.fadeIn(node).easeOut().play();

// Ease in (smooth acceleration)
TwAnimation.fadeIn(node).easeIn().play();

// Ease both (smooth acceleration and deceleration)
TwAnimation.fadeIn(node).easeBoth().play();

// Linear interpolation
TwAnimation.fadeIn(node).linear().play();
```

### Hover Effects

```java
// Scale on hover (default 1.05x)
TwAnimation.onHoverScale(button, 1.05);

// Lift effect on hover (default 4px)
TwAnimation.onHoverLift(card);

// Custom lift offset
TwAnimation.onHoverLift(card, 8.0);

// Dim effect on hover
TwAnimation.onHoverDim(button, 0.8);

// Remove all hover effects
TwAnimation.removeHoverEffects(node);
```

## Available Animations

### Entry Animations
- `fadeIn(Node)` / `fadeIn(Node, int)` / `fadeIn(Node, int, Interpolator)`
- `slideUp(Node)` / `slideUp(Node, int)`
- `slideDown(Node)` / `slideDown(Node, int)`
- `slideRight(Node)` / `slideRight(Node, int)`
- `slideLeft(Node)` / `slideLeft(Node, int)`
- `slideInLeft(Node, int)`
- `slideInRight(Node, int)`
- `slideInBottom(Node, int)`
- `slideInTop(Node, int)`
- `scaleIn(Node)` / `scaleIn(Node, int)` / `scaleIn(Node, int, Interpolator)`

### Exit Animations
- `fadeOut(Node)` / `fadeOut(Node, int)`
- `scaleOut(Node)` / `scaleOut(Node, int)`

### Effect Animations
- `pulse(Node)` - Quick scale pulse
- `shake(Node)` - Horizontal shake
- `bounce(Node)` - Vertical bounce
- `flash(Node)` - Opacity flash
- `spin(Node)` / `spin(Node, int)` - Rotation
- `breathe(Node)` - Subtle breathing effect

### Utility Methods
- `chain(TwAnimation...)` - Sequential execution
- `parallel(TwAnimation...)` - Simultaneous execution
- `pause(int)` - Pause for specified duration
- `onHoverScale(Node, double)` - Scale on hover
- `onHoverLift(Node)` / `onHoverLift(Node, double)` - Lift on hover
- `onHoverDim(Node, double)` - Opacity change on hover
- `removeHoverEffects(Node)` - Clean up hover effects

## Advanced Configuration

```java
// Auto-reverse animation (ping-pong effect)
TwAnimation.scaleIn(node)
    .autoReverse()
    .cycleCount(Animation.INDEFINITE)
    .play();

// Callback on animation finish
TwAnimation.slideUp(node)
    .onFinished(e -> System.out.println("Animation complete!"))
    .play();

// Custom interpolator
TwAnimation.fadeIn(node, 300, Interpolator.EASE_IN)
    .play();
```

## Responsive Animation Guard

Prevents animations from running during layout changes to avoid visual glitches:

```java
// Enable responsive guards for a scene
TwAnimation.ResponsiveAnimationGuard.onLayoutChangeStart(scene);

// Disable when layout changes complete
TwAnimation.ResponsiveAnimationGuard.onLayoutChangeEnd(scene);

// Reset node state after guard
TwAnimation.ResponsiveAnimationGuard.resetNode(node);
```

## Best Practices

1. **Use Semantic Slots**: Register animations with meaningful slot names ("enter", "exit", "hover") for better lifecycle management.

2. **Prefer Constants**: Use `TwAnimation.FAST`, `NORMAL`, `SLOW`, `SLOWER` instead of magic numbers.

3. **Clean Up Hover Effects**: Call `removeHoverEffects(node)` when nodes are removed to prevent memory leaks.

4. **Chain for Complexity**: Use `chain()` and `parallel()` for complex animation sequences instead of manual callbacks.

5. **Respect Layout Changes**: Use `ResponsiveAnimationGuard` during dynamic layout operations.

## See Also

- `TwStyle` - Apply utility classes to nodes
- `TwInstall` - Install TailwindCSS in JavaFX scenes
- `TailwindFXMetrics` - Track animation performance metrics
