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

    // Logger público para uso en toda la librería
    public static final Logger LOG = Logger.getLogger(Preconditions.class.getName());
    
    // Límites razonables para validaciones numéricas
    private static final int MAX_SPAN = 1000;
    private static final int MAX_DURATION_MS = 600_000; // 10 minutos
    private static final double MAX_SCALE = 100.0;
    private static final double MAX_SPEED = 100.0;

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

    /** GridPane span: must be >= 1 and <= MAX_SPAN */
    public static int requireSpan(int span, String method) {
        if (span < 1) {
            throw new IllegalArgumentException(
                method + ": span must be >= 1, got: " + span
            );
        }
        if (span > MAX_SPAN) {
            throw new IllegalArgumentException(
                method + ": span exceeds maximum allowed (" + MAX_SPAN + "), got: " + span
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

    /** Animation duration: > 0 ms and <= MAX_DURATION_MS */
    public static int requirePositiveDuration(int ms, String method) {
        if (ms <= 0) {
            throw new IllegalArgumentException(
                method + ": duration must be > 0 ms, got: " + ms
            );
        }
        if (ms > MAX_DURATION_MS) {
            throw new IllegalArgumentException(
                method + ": duration exceeds maximum allowed (" + MAX_DURATION_MS + " ms), got: " + ms
            );
        }
        return ms;
    }

    /** Animation scale: > 0 and <= MAX_SCALE */
    public static double requirePositiveScale(double scale, String method) {
        if (scale <= 0) {
            throw new IllegalArgumentException(
                method + ": scale must be > 0, got: " + scale
            );
        }
        if (scale > MAX_SCALE) {
            throw new IllegalArgumentException(
                method + ": scale exceeds maximum allowed (" + MAX_SCALE + "), got: " + scale
            );
        }
        return scale;
    }

    /** Speed multiplier for animations: > 0 and <= MAX_SPEED */
    public static double requirePositiveSpeed(double speed, String method) {
        if (speed <= 0) {
            throw new IllegalArgumentException(
                method + ": speed must be > 0, got: " + speed
            );
        }
        if (speed > MAX_SPEED) {
            throw new IllegalArgumentException(
                method + ": speed exceeds maximum allowed (" + MAX_SPEED + "), got: " + speed
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

    /** Warns if brightness is out of the recommended range [0.0-2.0]. 
     *  Note: This is a warning only; values outside this range are still allowed. */
    public static void warnBrightnessRange(double value, String method) {
        if (value < 0.0 || value > 2.0) {
            LOG.warning(String.format(
                "%s: brightness %.2f is outside the recommended range [0.0-2.0] and may produce unexpected visual results",
                method, value
            ));
        }
    }

    /** Warns if ImageView has no image when a viewport is applied */
    public static void warnNoImage(javafx.scene.image.ImageView iv, String method) {
        if (iv.getImage() == null) {
            LOG.warning(method + ": ImageView has no image — viewport will have no effect");
        }
    }

    /** Warns if a JIT token looks like a known typo.
     *  Note: This is a heuristic warning; not all unrecognized tokens are typos. */
    public static void warnLikelyTypo(String token, String method) {
        LOG.warning(String.format(
            "%s: token '%s' was not recognized as a valid Tailwind utility or CSS class - verify spelling or check documentation",
            method, token
        ));
    }

    /** Warns if an animation is applied to a node that already has one in the same slot */
    public static void warnAnimationOverride(javafx.scene.Node node, String slot, String method) {
        if (FxAnimation.AnimationRegistry.isActive(node, slot)) {
            LOG.info(String.format(
                "%s: replacing active animation in slot '%s' on node %s - previous animation will be stopped",
                method, slot, node
            ));
        }
    }
}