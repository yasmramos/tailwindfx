# TailwindFX Animation Examples

Practical examples for the `TwAnimation` API. JavaFX has no CSS animation engine, so
`animate-*` utilities and `TwAnimation` are played as JavaFX timelines rather than compiled to
inline styles.

## Running

The examples are part of the `examples` Maven profile:

```bash
# Build the example module (and its dependencies)
mvn -P examples -pl examples/animations-demo -am package

# Run the main showcase
mvn -P examples -pl examples/animations-demo javafx:run
```

Each focused example can also be run directly by overriding the main class:

```bash
mvn -P examples -pl examples/animations-demo javafx:run \
    -Dexec.mainClass=io.github.yasmramos.tailwindfx.examples.animations.FadeExamples
```

## Examples

| Class | Demonstrates |
|-------|--------------|
| `AnimationsDemoApp` | Main showcase: a tile per animation with a Play button, plus launchers for the focused examples |
| `FadeExamples` | `fadeIn`, `fadeOut` |
| `SlideExamples` | `slideUp`, `slideDown`, `slideLeft`, `slideRight`, `slideInLeft`, `slideInRight` |
| `ScaleExamples` | `scaleIn`, `scaleOut` |
| `RotateExamples` | `spin`, `pulse`, `breathe`, `shake`, `bounce`, `flash`, and `stop()` |
| `ChainedExamples` | `chain`, `parallel`, `pause`, custom duration and `withEase`/`speed`/`autoReverse` |

## Quick reference

```java
// Entrance
TwAnimation.fadeIn(node, TwAnimation.SLOW).play();
TwAnimation.slideUp(node, TwAnimation.NORMAL).play();
TwAnimation.scaleIn(node, 300).play();

// Exit (restore the node afterwards if you want to replay it)
TwAnimation.fadeOut(node, TwAnimation.SLOW)
    .onFinished(e -> node.setOpacity(1))
    .play();

// Loops
TwAnimation.spin(node, 1000).play();            // infinite
TwAnimation.pulse(node).cycleCount(3).play();   // exactly three pulses

// Composition
TwAnimation.chain(
        TwAnimation.scaleIn(node, TwAnimation.NORMAL),
        TwAnimation.pause(400),
        TwAnimation.fadeOut(node, TwAnimation.SLOW))
    .play();

// Custom easing / speed
TwAnimation.fadeIn(node, 700, Interpolator.EASE_IN).play();
TwAnimation.fadeIn(node, 600).speed(0.5).play();
```

## Troubleshooting

- **The animation does not run.** Animations must be created and played on the JavaFX Application
  Thread. Build them inside `Application.start()` or wrap calls in `Platform.runLater(...)`.
- **Nothing happens on a second click.** Entrance animations such as `fadeIn` reset the node's
  starting values, so they replay. Exit animations (`fadeOut`, `scaleOut`) end at opacity 0; restore
  the node in `onFinished` (as the examples do) before replaying.
- **A loop never stops.** `spin`, `pulse` and `breathe` use an indefinite cycle count. Keep a
  reference and call `stop()`, or use `cycleCount(n)` for a bounded run.
- **Two animations fight over the same property.** A timeline animates one value per property;
  applying a second animation to the same property replaces the first. Use `chain` to sequence them
  or animate different nodes with `parallel`.
- **Repeated calls stack animations.** Each `play()` starts a new timeline. Prefer reusing a single
  `TwAnimation` instance, or `stop()` the previous one before playing again (see
  `RotateExamples.playLoop`).
- **Easing seems ignored.** The three-argument factories (`fadeIn(node, ms, interpolator)`) and the
  fluent `withEase(...)` both set the interpolator; pick one. The two-argument factory defaults to
  `EASE_OUT`.
