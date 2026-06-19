# FxAnimation Class Documentation

## Overview

The `FxAnimation` class is a fluent wrapper around JavaFX `Animation` that provides a clean, chainable API for configuring and controlling animations in TailwindFX. It serves as the primary interface for animation operations, offering method chaining for common animation properties and integration with the `AnimationRegistry` for automatic lifecycle management.

## Key Features

- **Fluent API**: Chain configuration methods for readable, expressive code
- **Registry Integration**: Automatic slot-based animation management prevents conflicts
- **Memory Safety**: Prevents zombie timelines and memory leaks
- **Scene Awareness**: Auto-cancels animations when nodes are removed from the scene graph

## Basic Usage

### Simple Animation
```java
// Simple fade-in animation
FxAnimation.fadeIn(button).play();
```

### Chained Configuration
```java
// Chained configuration with multiple options
FxAnimation.slideUp(node, 300)
    .easeOut()
    .cycleCount(3)
    .onFinished(e -> System.out.println("Animation complete!"))
    .play();
```

### Registry-Controlled Animation
```java
// Registry-controlled animation with automatic lifecycle management
FxAnimation.pulse(badge)
    .register(badge, "attention")
    .loop()
    .play();
```

## Standard Animation Slots

FxAnimation uses a slot-based registry system to manage animations. Each slot governs a semantic category:

| Slot | Purpose | Example Methods |
|------|---------|----------------|
| `"enter"` | Entry animations | `fadeIn`, `slideUp`, `scaleIn` |
| `"exit"` | Exit animations | `fadeOut`, `scaleOut` |
| `"attention"` | Attention-grabbing effects | `shake`, `pulse`, `flash` |
| `"loop"` | Infinite animations | `spin`, `breathe` |
| `"transition"` | Property transitions | `TailwindFX.transition()` |

## Core Methods

### cycleCount(int count)
Sets the number of times the animation will repeat.

```java
// Play animation 3 times
FxAnimation.pulse(node).cycleCount(3).play();
```

### loop()
Configures the animation to repeat indefinitely.

```java
// Infinite spinning animation
FxAnimation.spin(node).loop().play();
```

### speed(double rate)
Sets the playback speed multiplier.

```java
// Double speed animation
FxAnimation.fadeIn(node).speed(2.0).play();
```

### onFinished(EventHandler handler)
Sets a callback handler invoked when the animation completes.

```java
FxAnimation.slideUp(node)
    .onFinished(e -> System.out.println("Animation complete!"))
    .play();
```

### autoReverse()
Configures the animation to automatically reverse direction after each cycle.

```java
// Ping-pong effect
FxAnimation.scaleIn(node).autoReverse().cycleCount(Animation.INDEFINITE).play();
```

### register(Node node, String slot)
Registers the animation with the AnimationRegistry for automatic lifecycle management.

```java
FxAnimation.fadeIn(node).register(node, "enter").play();
```

### play()
Starts the animation. If registered with the registry, automatically cancels any existing animation in the same slot.

### stop()
Stops the animation immediately and removes it from the registry if registered.

### pause() / resume()
Pauses and resumes the animation.

## Advanced Usage

### Custom Slots
Use custom slots for independent animations that should not cancel each other:

```java
AnimationRegistry.play(node, "highlight", myHighlightAnim);
AnimationRegistry.play(node, "bounce", myBounceAnim);
// Both run concurrently - different slots
```

### Easing Interpolators
Apply different easing functions for custom timing:

```java
// Use built-in interpolators
FxAnimation.fadeIn(node).easeIn().play();
FxAnimation.fadeIn(node).easeOut().play();
FxAnimation.fadeIn(node).easeBoth().play();
FxAnimation.fadeIn(node).linear().play();
```

### Animation Composition
Chain and parallelize animations:

```java
// Sequential animations
FxAnimation.chain(
    FxAnimation.fadeIn(node),
    FxAnimation.slideUp(node)
).play();

// Parallel animations
FxAnimation.parallel(
    FxAnimation.scaleIn(node),
    FxAnimation.fadeIn(node)
).play();
```

## Best Practices

1. **Always use standard slots** when possible to leverage automatic lifecycle management
2. **Register animations** with the registry to prevent memory leaks
3. **Use fluent chaining** for clean, readable code
4. **Consider performance** - infinite animations should be explicitly stopped when no longer needed
5. **Test on scene removal** - ensure animations properly clean up when nodes are removed

## See Also

- [AnimationRegistry](AnimationRegistry.md) - Registry system for animation lifecycle management
