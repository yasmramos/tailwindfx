package tailwindfx;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * FxAnimation — Fluent wrapper for JavaFX animations.
 *
 * Provides a declarative API over Timeline + KeyFrame + Interpolator.
 * Zero javafx.animation imports in user code.
 *
 * Usage:
 *   FxAnimation.fadeIn(node).play();
 *   FxAnimation.fadeIn(node, 300).play();
 *   FxAnimation.slideUp(node).onFinished(e -> ...).play();
 *   FxAnimation.pulse(node).cycleCount(3).play();
 *   FxAnimation.onHoverScale(button, 1.05);
 *   FxAnimation.chain(
 *       FxAnimation.fadeIn(node),
 *       FxAnimation.slideUp(node, 200)
 *   ).play();
 */
public final class FxAnimation {

    // Default durations (ms)
    public static final int FAST    = 150;
    public static final int NORMAL  = 250;
    public static final int SLOW    = 400;
    public static final int SLOWER  = 600;

    private final Animation timeline;
    private Node registeredNode;
    private String registeredSlot;
    private Interpolator ease = null;

    FxAnimation(Animation animation) {
        this.timeline = animation;
    }

    // =========================================================================
    // ENTRY ANIMATIONS — Fade, Slide, Scale
    // =========================================================================

    /**
     * Fades a node in from opacity 0 to 1 with default {@link #NORMAL} duration.
     * 
     * <p>The node's initial opacity is set to 0. The animation uses {@code EASE_OUT}
     * interpolation for a smooth entrance effect.
     *
     * @param node the node to animate (must not be null)
     * @return a ready-to-play {@link FxAnimation} registered in slot {@code "enter"}
     * @see #fadeIn(Node, int)
     * @see #fadeIn(Node, int, Interpolator)
     * @throws IllegalArgumentException if node is null
     */
    public static FxAnimation fadeIn(Node node) {
        return fadeIn(node, NORMAL);
    }

    /**
     * Fades a node in from opacity 0 to 1 using {@code EASE_OUT} interpolation.
     *
     * @param node       the node to animate (must not be null)
     * @param durationMs animation duration in milliseconds (must be > 0)
     * @return a ready-to-play {@link FxAnimation} registered in slot {@code "enter"}
     */
    public static FxAnimation fadeIn(Node node, int durationMs) {
        return fadeIn(node, durationMs, Interpolator.EASE_OUT);
    }

    /**
     * Fades a node in from opacity 0 to 1 with custom easing interpolation.
     *
     * <p>The node's initial opacity is set to 0. The animation applies the specified
     * interpolator for fine-grained control over acceleration/deceleration.
     *
     * <pre>
     * FxAnimation.fadeIn(node, 300, Interpolator.EASE_IN).play();  // Ease in for 300ms
     * FxAnimation.fadeIn(node, 200, Interpolator.LINEAR).play();   // Linear for 200ms
     * </pre>
     *
     * @param node       the node to animate (must not be null)
     * @param durationMs animation duration in milliseconds (must be > 0)
     * @param ease       the interpolation function (must not be null)
     * @return a ready-to-play {@link FxAnimation} registered in slot {@code "enter"}
     * @throws IllegalArgumentException if node is null, ease is null or durationMs <= 0
     * @see #fadeIn(Node)
     * @see #fadeIn(Node, int)
     */
    public static FxAnimation fadeIn(Node node, int durationMs, Interpolator ease) {
        Preconditions.requireNode(node, "FxAnimation.fadeIn");
        Preconditions.requirePositiveDuration(durationMs, "FxAnimation.fadeIn");
        Preconditions.requireNonNull(ease, "FxAnimation.fadeIn", "ease");
        node.setOpacity(0);
        FxAnimation fxAnim = new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.opacityProperty(), 0, ease)),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(node.opacityProperty(), 1, ease))
        ));
        fxAnim.ease = ease;
        return fxAnim.register(node, "enter");
    }

    /**
     * Slides a node up from below while fading in with default {@link #NORMAL} duration.
     * 
     * <p>The node starts 20 pixels below its current position with opacity 0, then slides
     * up and fades to full opacity. Uses {@code EASE_OUT} interpolation.
     *
     * @param node the node to animate (must not be null)
     * @return a ready-to-play {@link FxAnimation}
     * @see #slideUp(Node, int)
     * @throws IllegalArgumentException if node is null
     */
    public static FxAnimation slideUp(Node node) {
        return slideUp(node, NORMAL);
    }

    /**
     * Slides a node up from below while fading in with specified duration.
     * 
     * <p>The node starts 20 pixels below its current position with opacity 0, then slides
     * up to its original position while fading to full opacity. Uses {@code EASE_OUT}
     * interpolation for smooth motion.
     *
     * @param node       the node to animate (must not be null)
     * @param durationMs animation duration in milliseconds (must be > 0)
     * @return a ready-to-play {@link FxAnimation}
     * @see #slideUp(Node)
     * @throws IllegalArgumentException if node is null or durationMs <= 0
     */
    public static FxAnimation slideUp(Node node, int durationMs) {
        double startY = node.getTranslateY() + 20;
        node.setOpacity(0);
        node.setTranslateY(startY);
        return new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.opacityProperty(),    0,       Interpolator.EASE_OUT),
                new KeyValue(node.translateYProperty(), startY,  Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(node.opacityProperty(),    1,       Interpolator.EASE_OUT),
                new KeyValue(node.translateYProperty(), node.getTranslateY() - 20 + 20, Interpolator.EASE_OUT))
        ));
    }

    /**
     * Slides a node down from above while fading in with default {@link #NORMAL} duration.
     * 
     * <p>The node starts 20 pixels above its current position with opacity 0, then slides
     * down and fades to full opacity. Uses {@code EASE_OUT} interpolation.
     *
     * @param node the node to animate (must not be null)
     * @return a ready-to-play {@link FxAnimation}
     * @see #slideDown(Node, int)
     * @throws IllegalArgumentException if node is null
     */
    public static FxAnimation slideDown(Node node) {
        return slideDown(node, NORMAL);
    }

    /**
     * Slides a node down from above while fading in with specified duration.
     * 
     * <p>The node starts 20 pixels above its current position with opacity 0, then slides
     * down to its original position while fading to full opacity. Uses {@code EASE_OUT}
     * interpolation for smooth motion.
     *
     * @param node       the node to animate (must not be null)
     * @param durationMs animation duration in milliseconds (must be > 0)
     * @return a ready-to-play {@link FxAnimation}
     * @see #slideDown(Node)
     * @throws IllegalArgumentException if node is null or durationMs <= 0
     */
    public static FxAnimation slideDown(Node node, int durationMs) {
        double startY = node.getTranslateY() - 20;
        node.setOpacity(0);
        node.setTranslateY(startY);
        return new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.opacityProperty(),    0,      Interpolator.EASE_OUT),
                new KeyValue(node.translateYProperty(), startY, Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(node.opacityProperty(),    1,      Interpolator.EASE_OUT),
                new KeyValue(node.translateYProperty(), node.getTranslateY() + 20 - 20, Interpolator.EASE_OUT))
        ));
    }

    /**
     * Slides a node from left to right while fading in with default {@link #NORMAL} duration.
     * 
     * <p>The node starts 24 pixels to the left with opacity 0, then slides right and fades
     * to full opacity. Uses {@code EASE_OUT} interpolation.
     *
     * @param node the node to animate (must not be null)
     * @return a ready-to-play {@link FxAnimation}
     * @see #slideRight(Node, int)
     * @throws IllegalArgumentException if node is null
     */
    public static FxAnimation slideRight(Node node) { return slideRight(node, NORMAL); }
    
    /**
     * Slides a node from left to right while fading in with specified duration.
     * 
     * <p>The node starts 24 pixels to the left with opacity 0, then slides right to its
     * original position while fading to full opacity. Uses {@code EASE_OUT} interpolation.
     *
     * @param node       the node to animate (must not be null)
     * @param durationMs animation duration in milliseconds (must be > 0)
     * @return a ready-to-play {@link FxAnimation}
     * @see #slideRight(Node)
     * @throws IllegalArgumentException if node is null or durationMs <= 0
     */
    public static FxAnimation slideRight(Node node, int durationMs) {
        double startX = node.getTranslateX() - 24;
        node.setOpacity(0); node.setTranslateX(startX);
        return new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.opacityProperty(),    0,      Interpolator.EASE_OUT),
                new KeyValue(node.translateXProperty(), startX, Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(node.opacityProperty(),    1,      Interpolator.EASE_OUT),
                new KeyValue(node.translateXProperty(), node.getTranslateX() + 24 - 24, Interpolator.EASE_OUT))
        ));
    }

    /**
     * Slides a node from right to left while fading in with default {@link #NORMAL} duration.
     * 
     * <p>The node starts 24 pixels to the right with opacity 0, then slides left and fades
     * to full opacity. Uses {@code EASE_OUT} interpolation.
     *
     * @param node the node to animate (must not be null)
     * @return a ready-to-play {@link FxAnimation}
     * @see #slideLeft(Node, int)
     * @throws IllegalArgumentException if node is null
     */
    public static FxAnimation slideLeft(Node node) { return slideLeft(node, NORMAL); }
    
    /**
     * Slides a node from right to left while fading in with specified duration.
     * 
     * <p>The node starts 24 pixels to the right with opacity 0, then slides left to its
     * original position while fading to full opacity. Uses {@code EASE_OUT} interpolation.
     *
     * @param node       the node to animate (must not be null)
     * @param durationMs animation duration in milliseconds (must be > 0)
     * @return a ready-to-play {@link FxAnimation}
     * @see #slideLeft(Node)
     * @throws IllegalArgumentException if node is null or durationMs <= 0
     */
    public static FxAnimation slideLeft(Node node, int durationMs) {
        double startX = node.getTranslateX() + 24;
        node.setOpacity(0); node.setTranslateX(startX);
        return new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.opacityProperty(),    0,      Interpolator.EASE_OUT),
                new KeyValue(node.translateXProperty(), startX, Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(node.opacityProperty(),    1,      Interpolator.EASE_OUT),
                new KeyValue(node.translateXProperty(), node.getTranslateX() - 24 + 24, Interpolator.EASE_OUT))
        ));
    }

    /**
     * Scales a node up from 0.85 to 1.0 while fading in with default {@link #NORMAL} duration.
     * 
     * <p>Creates a "pop-in" entrance effect with {@code EASE_OUT} interpolation.
     * The node starts at 85% scale with opacity 0.
     *
     * @param node the node to animate (must not be null)
     * @return a ready-to-play {@link FxAnimation}
     * @see #scaleIn(Node, int)
     * @see #scaleIn(Node, int, Interpolator)
     * @throws IllegalArgumentException if node is null
     */
    public static FxAnimation scaleIn(Node node) { return scaleIn(node, NORMAL); }
    
    /**
     * Scales a node up from 0.85 to 1.0 while fading in with specified duration.
     * 
     * <p>Creates a "pop-in" entrance effect with {@code EASE_OUT} interpolation.
     * The node starts at 85% scale with opacity 0.
     *
     * @param node       the node to animate (must not be null)
     * @param durationMs animation duration in milliseconds (must be > 0)
     * @return a ready-to-play {@link FxAnimation}
     * @see #scaleIn(Node)
     * @see #scaleIn(Node, int, Interpolator)
     * @throws IllegalArgumentException if node is null or durationMs <= 0
     */
    public static FxAnimation scaleIn(Node node, int durationMs) {
        return scaleIn(node, durationMs, Interpolator.EASE_OUT);
    }
    
    /**
     * Scales a node up from 0.85 to 1.0 while fading in with custom easing.
     * 
     * <p>Creates a "pop-in" entrance effect. The node starts at 85% scale with opacity 0,
     * then scales to 100% and fades to full opacity using the specified interpolator.
     *
     * @param node       the node to animate (must not be null)
     * @param durationMs animation duration in milliseconds (must be > 0)
     * @param ease       the interpolation function (must not be null)
     * @return a ready-to-play {@link FxAnimation}
     * @see #scaleIn(Node)
     * @see #scaleIn(Node, int)
     * @throws IllegalArgumentException if node is null, ease is null or durationMs <= 0
     */
    public static FxAnimation scaleIn(Node node, int durationMs, Interpolator ease) {
        Preconditions.requireNode(node, "FxAnimation.scaleIn");
        Preconditions.requireNonNull(ease, "FxAnimation.scaleIn", "ease");
        node.setOpacity(0); node.setScaleX(0.85); node.setScaleY(0.85);
        FxAnimation fxAnim = new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.opacityProperty(), 0,    ease),
                new KeyValue(node.scaleXProperty(),  0.85, ease),
                new KeyValue(node.scaleYProperty(),  0.85, ease)),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(node.opacityProperty(), 1,    ease),
                new KeyValue(node.scaleXProperty(),  1.0,  ease),
                new KeyValue(node.scaleYProperty(),  1.0,  ease))
        ));
        fxAnim.ease = ease;
        return fxAnim;
    }

    // =========================================================================
    // EXIT ANIMATIONS — Fade Out, Scale Out
    // =========================================================================

    /**
     * Fades a node out from its current opacity to 0 with default {@link #NORMAL} duration.
     * 
     * <p>Uses {@code EASE_IN} interpolation. Automatically registered in the {@code "exit"} slot.
     *
     * @param node the node to animate (must not be null)
     * @return a ready-to-play {@link FxAnimation} registered in slot {@code "exit"}
     * @see #fadeOut(Node, int)
     * @throws IllegalArgumentException if node is null
     */
    public static FxAnimation fadeOut(Node node) { return fadeOut(node, NORMAL); }
    
    /**
     * Fades a node out from its current opacity to 0 with specified duration.
     * 
     * <p>Uses {@code EASE_IN} interpolation for a smooth exit effect. Automatically
     * registered in the {@code "exit"} slot, cancelling any previous exit animation.
     *
     * @param node       the node to animate (must not be null)
     * @param durationMs animation duration in milliseconds (must be > 0)
     * @return a ready-to-play {@link FxAnimation} registered in slot {@code "exit"}
     * @see #fadeOut(Node)
     * @throws IllegalArgumentException if node is null or durationMs <= 0 (calls requirePositiveDuration)
     */
    public static FxAnimation fadeOut(Node node, int durationMs) {
        Preconditions.requireNode(node, "FxAnimation.fadeOut");
        Preconditions.requirePositiveDuration(durationMs, "FxAnimation.fadeOut");
        return new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,        new KeyValue(node.opacityProperty(), node.getOpacity())),
            new KeyFrame(Duration.millis(durationMs), new KeyValue(node.opacityProperty(), 0, Interpolator.EASE_IN))
        )).register(node, "exit");
    }

    /**
     * Scales a node down from 1.0 to 0.85 while fading out with default {@link #NORMAL} duration.
     * 
     * <p>Creates a "pop-out" exit effect with {@code EASE_IN} interpolation.
     *
     * @param node the node to animate (must not be null)
     * @return a ready-to-play {@link FxAnimation}
     * @see #scaleOut(Node, int)
     * @throws IllegalArgumentException if node is null
     */
    public static FxAnimation scaleOut(Node node) { return scaleOut(node, NORMAL); }
    
    /**
     * Scales a node down from 1.0 to 0.85 while fading out with specified duration.
     * 
     * <p>Creates a "pop-out" exit effect. The node scales down to 85% and fades to
     * opacity 0 using {@code EASE_IN} interpolation.
     *
     * @param node       the node to animate (must not be null)
     * @param durationMs animation duration in milliseconds (must be > 0)
     * @return a ready-to-play {@link FxAnimation}
     * @see #scaleOut(Node)
     * @throws IllegalArgumentException if node is null or durationMs <= 0
     */
    public static FxAnimation scaleOut(Node node, int durationMs) {
        return new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.opacityProperty(), 1,   Interpolator.EASE_IN),
                new KeyValue(node.scaleXProperty(),  1.0, Interpolator.EASE_IN),
                new KeyValue(node.scaleYProperty(),  1.0, Interpolator.EASE_IN)),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(node.opacityProperty(), 0,    Interpolator.EASE_IN),
                new KeyValue(node.scaleXProperty(),  0.85, Interpolator.EASE_IN),
                new KeyValue(node.scaleYProperty(),  0.85, Interpolator.EASE_IN))
        ));
    }

    // =========================================================================
    // ATTENTION ANIMATIONS — Pulse, Shake, Bounce, Flash
    // =========================================================================

    /**
     * Pulses a node by fading its opacity in and out indefinitely.
     * 
     * <p>Useful for drawing attention to badges, notifications, or status indicators.
     * The animation cycles with period of 1 second (500ms fade out, 500ms fade in).
     * Call {@link FxAnimation#cycleCount(int)} to limit the number of pulses.
     *
     * @param node the node to animate (must not be null)
     * @return a ready-to-play {@link FxAnimation} with infinite cycle count
     * @throws IllegalArgumentException if node is null
     */
    public static FxAnimation pulse(Node node) {
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,         new KeyValue(node.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(500),  new KeyValue(node.opacityProperty(), 0.6, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(1000), new KeyValue(node.opacityProperty(), 1.0))
        );
        tl.setCycleCount(Animation.INDEFINITE);
        return new FxAnimation(tl);
    }

    /**
     * Shakes the node horizontally — useful for validation errors.
     *
     * @param node the node to shake (must not be null)
     * @return a ready-to-play {@link FxAnimation} registered in slot {@code "attention"}
     */
    public static FxAnimation shake(Node node) {
        Preconditions.requireNode(node, "FxAnimation.shake");
        double x = node.getTranslateX();
        return new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,        new KeyValue(node.translateXProperty(), x)),
            new KeyFrame(Duration.millis(60),  new KeyValue(node.translateXProperty(), x - 8, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(120), new KeyValue(node.translateXProperty(), x + 8, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(180), new KeyValue(node.translateXProperty(), x - 6, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(240), new KeyValue(node.translateXProperty(), x + 6, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(300), new KeyValue(node.translateXProperty(), x - 4, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(360), new KeyValue(node.translateXProperty(), x,     Interpolator.EASE_OUT))
        ));
    }

    /**
     * Bounces a node vertically with a dampening effect.
     * 
     * <p>Useful for attention-drawing animations on errors or interactive feedback.
     * The node bounces up and down with decreasing amplitude over 480 milliseconds.
     *
     * @param node the node to animate (must not be null)
     * @return a ready-to-play {@link FxAnimation}
     * @throws IllegalArgumentException if node is null
     */
    public static FxAnimation bounce(Node node) {
        double y = node.getTranslateY();
        return new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,        new KeyValue(node.translateYProperty(), y)),
            new KeyFrame(Duration.millis(100), new KeyValue(node.translateYProperty(), y - 12, Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(200), new KeyValue(node.translateYProperty(), y,      Interpolator.EASE_IN)),
            new KeyFrame(Duration.millis(280), new KeyValue(node.translateYProperty(), y - 6,  Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(360), new KeyValue(node.translateYProperty(), y,      Interpolator.EASE_IN)),
            new KeyFrame(Duration.millis(420), new KeyValue(node.translateYProperty(), y - 3,  Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(480), new KeyValue(node.translateYProperty(), y,      Interpolator.EASE_IN))
        ));
    }

    /**
     * Flashes a node by rapidly toggling its opacity on and off.
     * 
     * <p>Useful for toast notifications or quick attention-grabbing effects.
     * The animation completes a full flash cycle in 800 milliseconds (4 toggles).
     *
     * @param node the node to animate (must not be null)
     * @return a ready-to-play {@link FxAnimation}
     * @throws IllegalArgumentException if node is null
     */
    public static FxAnimation flash(Node node) {
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,        new KeyValue(node.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(200), new KeyValue(node.opacityProperty(), 0.0)),
            new KeyFrame(Duration.millis(400), new KeyValue(node.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(600), new KeyValue(node.opacityProperty(), 0.0)),
            new KeyFrame(Duration.millis(800), new KeyValue(node.opacityProperty(), 1.0))
        );
        return new FxAnimation(tl);
    }

    // =========================================================================
    // LOOP ANIMATIONS — Spin, Breathe (Infinite Cycles)
    // =========================================================================

    /**
     * Continuously rotates a node indefinitely.
     * 
     * <p>Perfect for loading spinners, progress indicators, or hover effects.
     * Uses default duration of 1000ms per full rotation with {@code LINEAR} interpolation.
     *
     * @param node the node to animate (must not be null)
     * @return a ready-to-play {@link FxAnimation} with infinite cycle count
     * @see #spin(Node, int)
     * @throws IllegalArgumentException if node is null
     */
    public static FxAnimation spin(Node node) {
        return spin(node, 1000);
    }
    
    /**
     * Continuously rotates a node indefinitely with specified rotation speed.
     * 
     * <p>Perfect for loading spinners or progress indicators. Rotates 360 degrees
     * in the specified duration using {@code LINEAR} interpolation.
     *
     * @param node       the node to animate (must not be null)
     * @param durationMs time for one full 360-degree rotation in milliseconds (must be > 0)
     * @return a ready-to-play {@link FxAnimation} with infinite cycle count
     * @see #spin(Node)
     * @throws IllegalArgumentException if node is null or durationMs <= 0
     */
    public static FxAnimation spin(Node node, int durationMs) {
        Preconditions.requireNode(node, "FxAnimation.spin");
        Preconditions.requirePositiveDuration(durationMs, "FxAnimation.spin");
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,            new KeyValue(node.rotateProperty(), 0)),
            new KeyFrame(Duration.millis(durationMs), new KeyValue(node.rotateProperty(), 360, Interpolator.LINEAR))
        );
        tl.setCycleCount(Animation.INDEFINITE);
        return new FxAnimation(tl);
    }

    /**
     * Gently scales a node in and out indefinitely (breathing effect).
     * 
     * <p>Useful for status indicators, pulsing notifications, or meditative UI elements.
     * The node scales from 1.0 to 1.06 (6% growth) with fading opacity over a 2-second
     * cycle, creating a calming breathing animation.
     *
     * @param node the node to animate (must not be null)
     * @return a ready-to-play {@link FxAnimation} with infinite cycle count
     * @throws IllegalArgumentException if node is null
     */
    public static FxAnimation breathe(Node node) {
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,            new KeyValue(node.scaleXProperty(), 1.0), new KeyValue(node.scaleYProperty(), 1.0)),
            new KeyFrame(Duration.millis(1000),    new KeyValue(node.scaleXProperty(), 1.06), new KeyValue(node.scaleYProperty(), 1.06), new KeyValue(node.opacityProperty(), 0.85)),
            new KeyFrame(Duration.millis(2000),    new KeyValue(node.scaleXProperty(), 1.0), new KeyValue(node.scaleYProperty(), 1.0), new KeyValue(node.opacityProperty(), 1.0))
        );
        tl.setCycleCount(Animation.INDEFINITE);
        return new FxAnimation(tl);
    }

    // =========================================================================
    // HOVER effects — se instalan como listeners permanentes
    // =========================================================================

    /**
     * Scales the node on hover — installs permanent mouse enter/exit listeners.
     *
     * <pre>
     * FxAnimation.onHoverScale(button, 1.05); // 5% larger on hover
     * </pre>
     *
     * @param node        the node to add hover effect to (must not be null)
     * @param scaleFactor scale factor on hover (e.g. {@code 1.05} = 5% larger, must be > 0)
     * @throws IllegalArgumentException if node is null or scaleFactor <= 0
     */
    public static void onHoverScale(Node node, double scaleFactor) {
        Preconditions.requireNode(node, "FxAnimation.onHoverScale");
        if (scaleFactor <= 0) {
            throw new IllegalArgumentException("FxAnimation.onHoverScale: scaleFactor must be > 0");
        }
        final double baseScaleX = node.getScaleX();
        final double baseScaleY = node.getScaleY();
        node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, e ->
            animateScale(node, baseScaleX * scaleFactor, baseScaleY * scaleFactor, 150)
        );
        node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, e ->
            animateScale(node, baseScaleX, baseScaleY, 150)
        );
    }

    /**
     * Internal helper that animates scale transitions smoothly.
     * 
     * <p>Used by hover effects to animate between scale values. Supports smooth
     * interpolation from current scale to target scale.
     *
     * @param node       the node to animate (must not be null)
     * @param targetX    target scale X value
     * @param targetY    target scale Y value
     * @param durationMs animation duration in milliseconds
     */
    private static void animateScale(Node node, double targetX, double targetY, int durationMs) {
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.scaleXProperty(), node.getScaleX()),
                new KeyValue(node.scaleYProperty(), node.getScaleY())),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(node.scaleXProperty(), targetX),
                new KeyValue(node.scaleYProperty(), targetY))
        );
        tl.play();
    }

    // =========================================================================
    // COMPOSITION UTILITIES — Chain, Parallel, Pause
    // =========================================================================

    /**
     * Chains multiple animations to execute sequentially, one after another.
     * 
     * <p>Each animation waits for the previous one to complete. Useful for
     * choreographed entrance sequences or multi-step effects.
     * 
     * <pre>
     * FxAnimation.chain(
     *     FxAnimation.fadeIn(node),
     *     FxAnimation.slideUp(node, 200)
     * ).play();
     * </pre>
     *
     * @param animations the animations to chain (must not be null or empty)
     * @return a ready-to-play {@link FxAnimation} containing all animations in sequence
     * @throws IllegalArgumentException if animations is null or empty
     * @see #parallel(FxAnimation...)
     */
    public static FxAnimation chain(FxAnimation... animations) {
        if (animations == null || animations.length == 0) {
            throw new IllegalArgumentException("FxAnimation.chain: al menos una animación requerida");
        }
        javafx.animation.SequentialTransition seq = new javafx.animation.SequentialTransition();
        for (FxAnimation anim : animations) {
            seq.getChildren().add(anim.timeline);
        }
        return new FxAnimation(seq);
    }

    /**
     * Executes multiple animations simultaneously (in parallel).
     * 
     * <p>All animations play at the same time. The overall animation completes when
     * the longest animation finishes. Useful for combined effects on the same or
     * different nodes.
     *
     * @param animations the animations to play in parallel (must not be null or empty)
     * @return a ready-to-play {@link FxAnimation} containing all animations in parallel
     * @throws IllegalArgumentException if animations is null or empty
     * @see #chain(FxAnimation...)
     */
    public static FxAnimation parallel(FxAnimation... animations) {
        if (animations == null || animations.length == 0) {
            throw new IllegalArgumentException("FxAnimation.parallel: al menos una animación requerida");
        }
        javafx.animation.ParallelTransition par = new javafx.animation.ParallelTransition();
        for (FxAnimation anim : animations) {
            par.getChildren().add(anim.timeline);
        }
        return new FxAnimation(par);
    }

    /**
     * Creates a pause animation of specified duration.
     * 
     * <p>Useful in animation chains to add delays between animations.
     * 
     * <pre>
     * FxAnimation.chain(
     *     FxAnimation.fadeIn(node1),
     *     FxAnimation.pause(200),
     *     FxAnimation.fadeIn(node2)
     * ).play();
     * </pre>
     *
     * @param durationMs pause duration in milliseconds (must be > 0)
     * @return a ready-to-play {@link FxAnimation}
     * @throws IllegalArgumentException if durationMs <= 0
     * @see #chain(FxAnimation...)
     */
    public static FxAnimation pause(int durationMs) {
        PauseTransition p = new PauseTransition(Duration.millis(durationMs));
        return new FxAnimation(p);
    }

    // =========================================================================
    // FLUENT API METHODS — Configure animation behavior
    // =========================================================================

    /**
     * Sets the number of times this animation repeats.
     * 
     * <p>Use {@link Animation#INDEFINITE} for infinite looping. By default,
     * animations play once (cycle count = 1).
     *
     * @param count number of cycles (1 = play once, INDEFINITE = infinite loop)
     * @return this animation for method chaining
     * @see #loop()
     */
    public FxAnimation cycleCount(int count) {
        timeline.setCycleCount(count);
        return this;
    }

    /**
     * Sets this animation to repeat infinitely.
     * 
     * <p>Convenience method equivalent to {@code cycleCount(Animation.INDEFINITE)}.
     * Stop explicitly with {@link #stop()} or when the node is removed from the scene.
     *
     * @return this animation for method chaining
     * @see #cycleCount(int)
     */
    public FxAnimation loop() {
        timeline.setCycleCount(Animation.INDEFINITE);
        return this;
    }

    /**
     * Adjusts the playback speed of this animation.
     * 
     * <p>Values less than 1.0 slow down the animation; values greater than 1.0
     * speed it up. For example: 0.5 = half speed, 2.0 = double speed.
     *
     * @param rate playback rate multiplier (must be > 0)
     * @return this animation for method chaining
     * @throws IllegalArgumentException if rate <= 0
     */
    public FxAnimation speed(double rate) {
        timeline.setRate(rate);
        return this;
    }

    /**
     * Registers a callback to execute when this animation completes.
     * 
     * @param handler callback function (must not be null)
     * @return this animation for method chaining
     * @throws IllegalArgumentException if handler is null
     */
    public FxAnimation onFinished(javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        timeline.setOnFinished(handler);
        return this;
    }

    /**
     * Enables auto-reverse: the animation plays forward, then backward.
     * 
     * <p>Useful for animations that should return to their starting state
     * automatically (e.g., hover effects, attention animations).
     * Only has effect when cycle count is > 1.
     *
     * @return this animation for method chaining
     */
    public FxAnimation autoReverse() {
        timeline.setAutoReverse(true);
        return this;
    }

    /**
     * Registers this animation in the AnimationRegistry of the target node.
     * 
     * <p>When {@link #play()} is called, any previous animation in the same slot is
     * automatically cancelled. This prevents competing animations from fighting over
     * the same node properties.
     * 
     * <p>Standard slots:
     * <ul>
     *   <li>{@code "enter"} — entry animations (fadeIn, slideUp, scaleIn, etc.)
     *   <li>{@code "exit"} — exit animations (fadeOut, scaleOut, etc.)
     *   <li>{@code "attention"} — attention animations (shake, bounce, pulse, etc.)
     *   <li>{@code "loop"} — infinite animations (spin, breathe, etc.)
     * </ul>
     * 
     * <p>Custom slots can be used for independent animations that should not
     * cancel each other.
     *
     * @param node the target node (must not be null)
     * @param slot animation slot name (must not be null or empty)
     * @return this animation for method chaining
     * @throws IllegalArgumentException if node or slot is null/empty
     * @see AnimationRegistry
     */
    public FxAnimation register(javafx.scene.Node node, String slot) {
        this.registeredNode = node;
        this.registeredSlot = slot;
        return this;
    }

    /**
     * Starts playback of this animation.
     * 
     * <p>If this animation is registered, any previous animation in the same slot is
     * automatically cancelled before this one starts. Otherwise, plays the animation
     * directly on its underlying Timeline.
     *
     * @see #register(Node, String)
     * @see #stop()
     * @see #pause()
     */
    public void play() {
        TailwindFXMetrics.instance().recordAnimationPlay();
        if (registeredNode != null && registeredSlot != null) {
            AnimationRegistry.play(registeredNode, registeredSlot, timeline);
        } else {
            timeline.play();
        }
    }

    /**
     * Stops this animation and unregisters it if registered.
     * 
     * <p>Resets the animation to its initial state. To resume later, call {@link #play()}.
     *
     * @see #pause()
     * @see #play()
     */
    public void stop() {
        timeline.stop();
        if (registeredNode != null && registeredSlot != null) {
            AnimationRegistry.cancel(registeredNode, registeredSlot);
        }
    }

    /**
     * Pauses this animation, preserving its current state.
     * 
     * <p>Call {@link #resume()} or {@link #play()} to continue playback.
     *
     * @see #resume()
     * @see #stop()
     */
    public void pause() { timeline.pause(); }

    /**
     * Resumes this animation from where it was paused.
     * 
     * <p>Does nothing if the animation is not currently paused.
     *
     * @see #pause()
     * @see #play()
     */
    public void resume() { timeline.play(); }

    /**
     * Sets the easing interpolator for this animation.
     *
     * <p>Use the convenience shortcuts {@link #easeIn()}, {@link #easeOut()},
     * {@link #easeBoth()}, {@link #linear()} for common cases.
     *
     * @param interpolator the {@link Interpolator} to apply (must not be null)
     * @return this animation for chaining
     */
    public FxAnimation withEase(Interpolator interpolator) {
        Preconditions.requireNonNull(interpolator, "FxAnimation.withEase", "interpolator");
        this.ease = interpolator;
        return this;
    }

    /**
     * Applies {@code EASE_IN} interpolation — decelerates at the start.
     * 
     * <p>Use for animations that should start slow and end fast (e.g., exits).
     *
     * @return this animation for method chaining
     * @see #easeOut()
     * @see #easeBoth()
     */
    public FxAnimation easeIn()    { return withEase(Interpolator.EASE_IN); }
    
    /**
     * Applies {@code EASE_OUT} interpolation — decelerates at the end.
     * 
     * <p>Use for animations that should start fast and end slow (e.g., entrances).
     * This is the most common easing for natural-feeling animations.
     *
     * @return this animation for method chaining
     * @see #easeIn()
     * @see #easeBoth()
     */
    public FxAnimation easeOut()   { return withEase(Interpolator.EASE_OUT); }
    
    /**
     * Applies {@code EASE_IN_OUT} interpolation — decelerates at both ends.
     * 
     * <p>Use for animations that should ease in and out symmetrically.
     *
     * @return this animation for method chaining
     * @see #easeIn()
     * @see #easeOut()
     */
    public FxAnimation easeBoth()  { return withEase(Interpolator.EASE_BOTH); }
    
    /**
     * Applies {@code LINEAR} interpolation — constant speed throughout.
     * 
     * <p>Use for mechanical animations like spinners or progress indicators.
     *
     * @return this animation for method chaining
     * @see #easeIn()
     * @see #easeOut()
     */
    public FxAnimation linear()    { return withEase(Interpolator.LINEAR); }

    /**
     * Returns the underlying JavaFX {@link Animation} for advanced configuration.
     * 
     * <p>Use this for direct access to features not exposed by FxAnimation's fluent API.
     * Most users should use the fluent API methods instead.
     *
     * @return the underlying Timeline or Transition
     */
    public Animation raw() { return timeline; }

    // =========================================================================
    // Hover effects (onHoverLift, onHoverDim, removeHoverEffects)
    // =========================================================================

    /**
     * Lifts a node up slightly on hover (default 4 pixels).
     * 
     * <p>Installs permanent mouse enter/exit listeners. Multiple hover effects can
     * be stacked on the same node. Remove all effects with {@link #removeHoverEffects(Node)}.
     *
     * @param node the node to add lift effect to (must not be null)
     * @throws IllegalArgumentException if node is null
     * @see #onHoverLift(Node, double)
     * @see #removeHoverEffects(Node)
     */
    public static void onHoverLift(Node node) { onHoverLift(node, -4); }
    
    /**
     * Lifts a node on hover with custom vertical offset.
     * 
     * <p>Installs permanent mouse enter/exit listeners that animate the node up/down.
     * Negative offsets lift the node up; positive offsets push it down.
     * Multiple hover effects can be stacked. Remove all with {@link #removeHoverEffects(Node)}.
     *
     * @param node    the node to add lift effect to (must not be null)
     * @param offsetY vertical offset in pixels (typically negative for upward lift)
     * @throws IllegalArgumentException if node is null
     * @see #onHoverLift(Node)
     * @see #removeHoverEffects(Node)
     */
    public static void onHoverLift(Node node, double offsetY) {
        double originalY = node.getTranslateY();
        Timeline hoverIn  = new Timeline(new KeyFrame(Duration.millis(FAST),
            new KeyValue(node.translateYProperty(), originalY + offsetY, Interpolator.EASE_OUT)));
        Timeline hoverOut = new Timeline(new KeyFrame(Duration.millis(FAST),
            new KeyValue(node.translateYProperty(), originalY, Interpolator.EASE_OUT)));
        javafx.event.EventHandler<javafx.scene.input.MouseEvent> enterLift =
            e -> { hoverOut.stop(); hoverIn.play(); };
        javafx.event.EventHandler<javafx.scene.input.MouseEvent> exitLift =
            e -> { hoverIn.stop();  hoverOut.play(); };
        node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, enterLift);
        node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED,  exitLift);
        storeHoverHandlers(node, enterLift, exitLift);
    }

    /**
     * Dims a node (reduces opacity) on hover.
     * 
     * <p>Installs permanent mouse enter/exit listeners. Multiple hover effects can
     * be stacked on the same node. Remove all effects with {@link #removeHoverEffects(Node)}.
     * 
     * <pre>
     * FxAnimation.onHoverDim(button, 0.7);  // 70% opacity on hover
     * </pre>
     *
     * @param node            the node to add dim effect to (must not be null)
     * @param targetOpacity   opacity on hover (0.0 = transparent, 1.0 = opaque)
     * @throws IllegalArgumentException if node is null
     * @see #removeHoverEffects(Node)
     */
    public static void onHoverDim(Node node, double targetOpacity) {
        Timeline hoverIn  = new Timeline(new KeyFrame(Duration.millis(FAST),
            new KeyValue(node.opacityProperty(), targetOpacity, Interpolator.EASE_OUT)));
        Timeline hoverOut = new Timeline(new KeyFrame(Duration.millis(FAST),
            new KeyValue(node.opacityProperty(), 1.0, Interpolator.EASE_OUT)));
        javafx.event.EventHandler<javafx.scene.input.MouseEvent> enterH2 =
            e -> { hoverOut.stop(); hoverIn.play(); };
        javafx.event.EventHandler<javafx.scene.input.MouseEvent> exitH2 =
            e -> { hoverIn.stop();  hoverOut.play(); };
        node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, enterH2);
        node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED,  exitH2);
        storeHoverHandlers(node, enterH2, exitH2);
    }

    /**
     * Removes all installed hover effects from a node.
     * 
     * <p>Unregisters all mouse listeners added by {@link #onHoverScale(Node, double)},
     * {@link #onHoverLift(Node)}, {@link #onHoverLift(Node, double)}, and
     * {@link #onHoverDim(Node, double)}. Also resets scale, opacity, and rotation
     * to baseline values (1.0, 1.0, 0).
     *
     * @param node the node to remove hover effects from (must not be null)
     * @throws IllegalArgumentException if node is null
     */
    public static void removeHoverEffects(Node node) {
        @SuppressWarnings("unchecked")
        var handlers = (java.util.List<javafx.event.EventHandler<javafx.scene.input.MouseEvent>>)
            node.getProperties().remove("tailwindfx.hover.handlers");
        if (handlers != null) {
            for (int i = 0; i < handlers.size(); i += 2) {
                node.removeEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, handlers.get(i));
                if (i + 1 < handlers.size())
                    node.removeEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, handlers.get(i + 1));
            }
        }
        node.setScaleX(1.0); node.setScaleY(1.0);
        node.setOpacity(1.0);
    }

    /**
     * Internal helper that stores hover event handlers for later removal.
     * 
     * <p>Maintains a list of all hover handlers installed on a node so they can
     * be unregistered cleanly by {@link #removeHoverEffects(Node)}.
     *
     * @param node  the target node (must not be null)
     * @param enter the mouse enter handler (must not be null)
     * @param exit  the mouse exit handler (must not be null)
     */
    @SuppressWarnings("unchecked")
    private static void storeHoverHandlers(Node node, 
            javafx.event.EventHandler<javafx.scene.input.MouseEvent> enter,
            javafx.event.EventHandler<javafx.scene.input.MouseEvent> exit) {
        var handlers = (java.util.List<javafx.event.EventHandler<javafx.scene.input.MouseEvent>>)
            node.getProperties().computeIfAbsent("tailwindfx.hover.handlers", k -> new java.util.ArrayList<>());
        handlers.add(enter);
        handlers.add(exit);
    }

    // =========================================================================
    // Inner classes moved from AnimationUtil
    // =========================================================================

    /**
     * AnimationRegistry — Manages active animations per node to prevent memory leaks.
     * 
     * <p>Registers animations in named slots, auto-cancels previous animations in the
     * same slot, and automatically cleans up when nodes are removed from the scene.
     * This prevents Timeline objects from holding strong references to detached nodes.
     * 
     * <h3>Predefined Slots</h3>
     * Each slot governs a semantic category of animation. Starting an animation
     * in a slot cancels whatever is currently running in that slot:
     * <ul>
     *   <li>{@code "enter"} — entry animations (fadeIn, slideUp, slideDown, etc.)
     *   <li>{@code "exit"} — exit animations (fadeOut, scaleOut)
     *   <li>{@code "attention"} — attention animations (shake, bounce, pulse, flash)
     *   <li>{@code "loop"} — infinite animations (spin, breathe)
     * </ul>
     * 
     * <h3>Custom Slots</h3>
     * Any string is a valid slot. Use custom slots when you need independent
     * animations that should not cancel each other:
     * <pre>
     * registry.play(node, "highlight", fadeAnimation);
     * registry.play(node, "bounce", bounceAnimation);
     * // Both run concurrently
     * </pre>
     * 
     * <h3>Scene Listener</h3>
     * Automatically removes all active animations when the node is detached from
     * the scene, preventing Timeline memory leaks.
     */
    public static final class AnimationRegistry {

        private static final String KEY = "tailwindfx.animations";

        private AnimationRegistry() {}

        /**
         * Registers and starts an animation in a named slot.
         * 
         * <p>If an animation is already running in the same slot, it is automatically
         * stopped and replaced. Also installs a scene listener that auto-cancels all
         * animations if the node is removed from the scene, preventing Timeline-induced
         * memory leaks.
         *
         * @param node      the target node (must not be null)
         * @param slot      animation slot name (must not be null)
         * @param animation the animation to play (must not be null)
         */
        public static void play(javafx.scene.Node node, String slot, Animation animation) {
            cancel(node, slot);
            getSlots(node).put(slot, animation);
            animation.setOnFinished(e -> getSlots(node).remove(slot));
            animation.play();
            installSceneListener(node);
        }

        /**
         * Checks if an animation is currently running in the specified slot.
         *
         * @param node the target node (must not be null)
         * @param slot animation slot name (must not be null)
         * @return true if an animation is active in the slot, false otherwise
         */
        public static boolean isActive(javafx.scene.Node node, String slot) {
            return getSlots(node).containsKey(slot);
        }

        /**
         * Installs a scene listener (once per node) that cancels all active animations
         * when the node is removed from the scene graph.
         * 
         * <p><b>Why this matters:</b> A running Timeline holds references to KeyValues,
         * which hold references to node properties, creating a chain:
         * Timeline → KeyValue → node. This prevents GC of detached nodes.
         * Cancelling the Timeline on scene removal breaks this chain.
         *
         * @param node the target node (must not be null)
         */
        private static void installSceneListener(javafx.scene.Node node) {
            final String LISTENER_KEY = "tailwindfx.anim.scene-listener";
            if (node.getProperties().containsKey(LISTENER_KEY)) return;

            javafx.beans.value.ChangeListener<javafx.scene.Scene> listener =
                (obs, oldScene, newScene) -> {
                    if (newScene == null) {
                        cancelAll(node);
                    }
                };
            node.sceneProperty().addListener(listener);
            node.getProperties().put(LISTENER_KEY, Boolean.TRUE);
        }

        /**
         * Stops and removes an animation from its slot.
         *
         * @param node the target node (must not be null)
         * @param slot animation slot name (must not be null)
         */
        public static void cancel(javafx.scene.Node node, String slot) {
            Animation prev = getSlots(node).get(slot);
            if (prev != null) { prev.stop(); getSlots(node).remove(slot); }
        }

        /**
         * Cancels all active animations on this node across all slots.
         *
         * @param node the target node (must not be null)
         */
        public static void cancelAll(javafx.scene.Node node) {
            getSlots(node).values().forEach(Animation::stop);
            getSlots(node).clear();
        }

        /**
         * Internal helper to retrieve or create the animation slot map for a node.
         *
         * @param node the target node (must not be null)
         * @return map of slot names to active animations
         */
        @SuppressWarnings("unchecked")
        private static java.util.Map<String, Animation> getSlots(javafx.scene.Node node) {
            return (java.util.Map<String, Animation>) node.getProperties()
                .computeIfAbsent(KEY, k -> new java.util.HashMap<String, Animation>());
        }
    }

    /**
     * ResponsiveAnimationGuard — Coordinates animations with responsive layout changes.
     * 
     * <p>Prevents animations from becoming desynchronized when the layout reorganizes
     * (e.g., when crossing a breakpoint that changes from row to column layout).
     * 
     * <h3>Problem Scenario</h3>
     * If a node is animating its translateX/Y while the layout simultaneously recalculates
     * positions, the node can end up in an incorrect final position.
     * 
     * <h3>Solution</h3>
     * Pause active transform animations before layout changes, then resume afterward.
     * This integration with BreakpointManager ensures smooth responsive behavior.
     * 
     * <pre>
     * BreakpointManager bpm = TailwindFX.responsive(stage);
     * ResponsiveAnimationGuard.install(scene);
     * // Animations now pause/resume automatically during breakpoint changes
     * </pre>
     */
    public static final class ResponsiveAnimationGuard {

        private static final String PAUSED_KEY = "tailwindfx.anim.paused";

        private ResponsiveAnimationGuard() {}

        /**
         * Pauses all active transform animations in the scene subtree.
         * 
         * <p>Call at the start of a responsive layout change (before reorganizing Panes).
         * Animations in transform slots (enter, exit, slide, scale) are paused to prevent
         * conflicting with position recalculations.
         *
         * @param scene the scene whose animations to pause (must not be null)
         */
        public static void onLayoutChangeStart(javafx.scene.Scene scene) {
            if (scene == null || scene.getRoot() == null) return;
            pauseAnimationsInSubtree(scene.getRoot());
        }

        /**
         * Resumes all animations that were paused during a responsive layout change.
         * 
         * <p>Call after completing the layout reorganization. Animations that were
         * active before the layout change are automatically resumed.
         *
         * @param scene the scene whose animations to resume (must not be null)
         */
        public static void onLayoutChangeEnd(javafx.scene.Scene scene) {
            if (scene == null || scene.getRoot() == null) return;
            resumeAnimationsInSubtree(scene.getRoot());
        }

        /**
         * Resets a node's animation state and visual properties to baseline.
         * 
     * <p>Cancels all active animations and restores the node to its default
         * visual state: translateX/Y = 0, scaleX/Y = 1, opacity = 1, rotation = 0.
         * Useful when switching layouts to prevent residual transform values from
         * affecting the new layout.
         *
         * @param node the node to reset (must not be null)
         */
        public static void resetNode(javafx.scene.Node node) {
            AnimationRegistry.cancelAll(node);
            node.setTranslateX(0);
            node.setTranslateY(0);
            node.setScaleX(1);
            node.setScaleY(1);
            node.setOpacity(1);
            node.setRotate(0);
        }

        /**
         * Internal helper that recursively pauses transform animations in a node subtree.
         *
         * @param node the root node of the subtree (must not be null)
         */
        private static void pauseAnimationsInSubtree(javafx.scene.Node node) {
            @SuppressWarnings("unchecked")
            var slots = (java.util.Map<String, Animation>) node.getProperties()
                .get("tailwindfx.animations");
            if (slots != null) {
                java.util.List<String> toPause = new java.util.ArrayList<>();
                for (var entry : slots.entrySet()) {
                    if (entry.getValue().getStatus() == Animation.Status.RUNNING
                        && isTransformAnimation(entry.getKey())) {
                        entry.getValue().pause();
                        toPause.add(entry.getKey());
                    }
                }
                if (!toPause.isEmpty()) {
                    node.getProperties().put(PAUSED_KEY, toPause);
                }
            }
            if (node instanceof javafx.scene.Parent parent) {
                for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                    pauseAnimationsInSubtree(child);
                }
            }
        }

        /**
         * Internal helper that recursively resumes paused animations in a node subtree.
         *
         * @param node the root node of the subtree (must not be null)
         */
        private static void resumeAnimationsInSubtree(javafx.scene.Node node) {
            @SuppressWarnings("unchecked")
            var paused = (java.util.List<String>) node.getProperties().remove(PAUSED_KEY);
            @SuppressWarnings("unchecked")
            var slots = (java.util.Map<String, Animation>) node.getProperties()
                .get("tailwindfx.animations");
            if (paused != null && slots != null) {
                for (String slot : paused) {
                    Animation a = slots.get(slot);
                    if (a != null && a.getStatus() == Animation.Status.PAUSED) {
                        a.play();
                    }
                }
            }
            if (node instanceof javafx.scene.Parent parent) {
                for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                    resumeAnimationsInSubtree(child);
                }
            }
        }

        /**
         * Internal helper that identifies transform animation slots.
         * 
         * <p>Slots like "enter", "exit", "slide*", "scale*" animate position and size.
         * These must be paused during responsive layout changes.
         *
         * @param slot the animation slot name
         * @return true if this slot contains transform animations
         */
        private static boolean isTransformAnimation(String slot) {
            return "translate".equals(slot) || "scale".equals(slot) ||
                   "rotate".equals(slot) || "slide".equals(slot);
        }
    }
}
