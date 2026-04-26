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
AnimationUtil.fadeIn(button).play();
```

### Chained Configuration
```java
// Chained configuration with multiple options
AnimationUtil.slideUp(node, 300)
    .easeOut()
    .cycleCount(3)
    .onFinished(e -> System.out.println("Animation complete!"))
    .play();
```

### Registry-Controlled Animation
```java
// Registry-controlled animation with automatic lifecycle management
AnimationUtil.pulse(badge)
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
AnimationUtil.pulse(node).cycleCount(3).play();
```

### loop()
Configures the animation to repeat indefinitely.

```java
// Infinite spinning animation
AnimationUtil.spin(node).loop().play();
```

### speed(double rate)
Sets the playback speed multiplier.

```java
// Double speed animation
AnimationUtil.fadeIn(node).speed(2.0).play();
```

### onFinished(EventHandler handler)
Sets a callback handler invoked when the animation completes.

```java
AnimationUtil.slideUp(node)
    .onFinished(e -> System.out.println("Animation complete!"))
    .play();
```

### autoReverse()
Configures the animation to automatically reverse direction after each cycle.

```java
// Ping-pong effect
AnimationUtil.scaleIn(node).autoReverse().cycleCount(Animation.INDEFINITE).play();
```

### register(Node node, String slot)
Registers the animation with the AnimationRegistry for automatic lifecycle management.

```java
AnimationUtil.fadeIn(node).register(node, "enter").play();
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
AnimationUtil.fadeIn(node).easeIn().play();
AnimationUtil.fadeIn(node).easeOut().play();
AnimationUtil.fadeIn(node).easeBoth().play();
AnimationUtil.fadeIn(node).linear().play();
```

### Animation Composition
Chain and parallelize animations:

```java
// Sequential animations
AnimationUtil.chain(
    AnimationUtil.fadeIn(node),
    AnimationUtil.slideUp(node)
).play();

// Parallel animations
AnimationUtil.parallel(
    AnimationUtil.scaleIn(node),
    AnimationUtil.fadeIn(node)
).play();
```

## Best Practices

1. **Always use standard slots** when possible to leverage automatic lifecycle management
2. **Register animations** with the registry to prevent memory leaks
3. **Use fluent chaining** for clean, readable code
4. **Consider performance** - infinite animations should be explicitly stopped when no longer needed
5. **Test on scene removal** - ensure animations properly clean up when nodes are removed

## See Also

- [AnimationUtil](AnimationUtil.md) - Factory methods for creating animations
- [AnimationRegistry](AnimationRegistry.md) - Registry system for animation lifecycle management
