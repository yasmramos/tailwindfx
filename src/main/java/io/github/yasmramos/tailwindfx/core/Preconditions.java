package io.github.yasmramos.tailwindfx.core;

import io.github.yasmramos.tailwindfx.animation.FxAnimation;
import java.util.logging.Logger;

/**
 * Preconditions — Validación centralizada para TailwindFX.
 * Tres niveles:
 * 1. requireNonNull()  → IllegalArgumentException (fallo inmediato)
 * 2. requireValid()    → IllegalArgumentException (valor fuera de rango)
 * 3. warnIf()          → LOG WARNING (válido pero sospechoso)
 */
public final class Preconditions {

    public static final Logger LOG = Logger.getLogger("TailwindFX");

    private Preconditions() {}

    public static <T> T requireNonNull(T value, String method, String paramName) {
        if (value == null) {
            throw new IllegalArgumentException(
                method + ": '" + paramName + "' cannot be null"
            );
        }
        return value;
    }

    public static <T extends javafx.scene.Node> T requireNode(T node, String method) {
        return requireNonNull(node, method, "node");
    }

    public static javafx.scene.layout.Pane requirePane(javafx.scene.layout.Pane pane, String method) {
        return requireNonNull(pane, method, "pane");
    }

    public static String requireNonBlank(String value, String method, String paramName) {
        requireNonNull(value, method, paramName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(
                method + ": '" + paramName + "' cannot be blank"
            );
        }
        return value;
    }

    /** GridPane span: must be >= 1 */
    public static int requireSpan(int span, String method) {
        if (span < 1) {
            throw new IllegalArgumentException(
                method + ": span must be >= 1, got: " + span
            );
        }
        return span;
    }

    /** Opacity: 0.0 – 1.0 */
    public static double requireOpacity(double value, String method) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(
                method + ": opacity must be between 0.0 and 1.0, got: " + value
            );
        }
        return value;
    }

    /** Animation duration: > 0 ms */
    public static int requirePositiveDuration(int ms, String method) {
        if (ms <= 0) {
            throw new IllegalArgumentException(
                method + ": duration must be > 0 ms, got: " + ms
            );
        }
        return ms;
    }

    /** Animation scale: > 0 */
    public static double requirePositiveScale(double scale, String method) {
        if (scale <= 0) {
            throw new IllegalArgumentException(
                method + ": scale must be > 0, got: " + scale
            );
        }
        return scale;
    }

    /** Speed multiplier for animations: > 0 */
    public static double requirePositiveSpeed(double speed, String method) {
        if (speed <= 0) {
            throw new IllegalArgumentException(
                method + ": speed must be > 0, got: " + speed
            );
        }
        return speed;
    }

    /** Alpha/N in JIT: 0–100 */
    public static int requireAlpha(int alpha, String method) {
        if (alpha < 0 || alpha > 100) {
            throw new IllegalArgumentException(
                method + ": alpha must be between 0 and 100, got: " + alpha
            );
        }
        return alpha;
    }

    /** Warns if the node has no parent (margin/grow will have no effect) */
    public static void warnNoParent(javafx.scene.Node node, String method) {
        if (node.getParent() == null) {
            LOG.warning(method + ": node has no parent — constraint will have no effect until added to the scene graph");
        }
    }

    /** Warns if brightness is out of the recommended range [0.0-2.0] */
    public static void warnBrightnessRange(double value, String method) {
        if (value < 0.0 || value > 2.0) {
            LOG.warning(method + ": brightness " + value + " out of useful range [0.0-2.0]");
        }
    }

    /** Warns if ImageView has no image when a viewport is applied */
    public static void warnNoImage(javafx.scene.image.ImageView iv, String method) {
        if (iv.getImage() == null) {
            LOG.warning(method + ": ImageView has no image — viewport will have no effect");
        }
    }

    /** Warns if a JIT token looks like a known typo */
    public static void warnLikelyTypo(String token, String method) {
        LOG.warning(method + ": token '" + token + "' not recognized as a JIT utility or CSS class — possible typo?");
    }

    /** Warns if an animation is applied to a node that already has one in the same slot */
    public static void warnAnimationOverride(javafx.scene.Node node, String slot, String method) {
        if (FxAnimation.AnimationRegistry.isActive(node, slot)) {
            LOG.fine(method + ": replacing active animation in slot '" + slot + "'");
        }
    }
}