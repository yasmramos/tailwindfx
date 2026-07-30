package io.github.yasmramos.tailwindfx.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TransitionProcessor — Handles Tailwind CSS transition and animation utilities.
 *
 * <p>Processes tokens like:
 * <ul>
 *   <li>{@code transition-none} — No transitions</li>
 *   <li>{@code transition-all} — Transition all properties</li>
 *   <li>{@code transition-colors} — Transition color properties</li>
 *   <li>{@code transition-opacity} — Transition opacity only</li>
 *   <li>{@code transition-transform} — Transition transform property</li>
 *   <li>{@code duration-75}, {@code duration-100}, ..., {@code duration-1000}</li>
 *   <li>{@code ease-linear}, {@code ease-in}, {@code ease-out}, {@code ease-in-out}</li>
 *   <li>{@code animate-spin}, {@code animate-pulse}, {@code animate-bounce}</li>
 * </ul>
 *
 * <p>Note: JavaFX does not support CSS animations directly. This processor generates
 * transition properties that can be used with JavaFX's Timeline animations or
 * CSS-like styling where supported.
 *
 * @author yasmramos
 * @since 1.0.0
 */
public final class TransitionProcessor {

    private static final String TRANSITION_NONE = "transition-none";
    private static final String TRANSITION_ALL = "transition-all";
    private static final String TRANSITION_COLORS = "transition-colors";
    private static final String TRANSITION_OPACITY = "transition-opacity";
    private static final String TRANSITION_TRANSFORM = "transition-transform";
    
    private static final String DURATION_PREFIX = "duration-";
    private static final String EASE_PREFIX = "ease-";
    private static final String ANIMATE_PREFIX = "animate-";

    // Standard Tailwind CSS v4 duration values (in ms)
    private static final Map<String, Integer> DURATIONS = new ConcurrentHashMap<>();
    static {
        DURATIONS.put("75", 75);
        DURATIONS.put("100", 100);
        DURATIONS.put("150", 150);
        DURATIONS.put("200", 200);
        DURATIONS.put("300", 300);
        DURATIONS.put("500", 500);
        DURATIONS.put("700", 700);
        DURATIONS.put("1000", 1000);
    }

    // Standard Tailwind CSS v4 easing functions
    private static final Map<String, String> EASINGS = new ConcurrentHashMap<>();
    static {
        EASINGS.put("linear", "cubic-bezier(0, 0, 1, 1)");
        EASINGS.put("in", "cubic-bezier(0.4, 0, 1, 1)");
        EASINGS.put("out", "cubic-bezier(0, 0, 0.2, 1)");
        EASINGS.put("in-out", "cubic-bezier(0.4, 0, 0.2, 1)");
    }

    // Animation types
    private static final Set<String> ANIMATIONS = new HashSet<>(Arrays.asList(
        "spin", "pulse", "bounce", "ping", "flash", "shake-x", "shake-y",
        "spin-slow", "pulse-slow", "bounce-slow"
    ));

    private TransitionProcessor() {
        // Utility class
    }

    /**
     * Checks if a token is a transition-related utility.
     *
     * @param token the token to check
     * @return true if the token is a transition, duration, ease, or animate utility
     */
    public static boolean isTransitionToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return token.startsWith("transition-") ||
               token.startsWith(DURATION_PREFIX) ||
               token.startsWith(EASE_PREFIX) ||
               token.startsWith(ANIMATE_PREFIX);
    }

    /**
     * Processes a transition token and returns the corresponding CSS-like properties.
     *
     * @param token the transition token to process
     * @return TransitionResult with inline style and metadata, or null if not a transition token
     */
    public static TransitionResult processTransition(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        // Remove modifiers for processing
        String cleanToken = removeModifiers(token);

        if (cleanToken.startsWith("transition-")) {
            return processTransitionProperty(cleanToken);
        } else if (cleanToken.startsWith(DURATION_PREFIX)) {
            return processDuration(cleanToken);
        } else if (cleanToken.startsWith(EASE_PREFIX)) {
            return processEasing(cleanToken);
        } else if (cleanToken.startsWith(ANIMATE_PREFIX)) {
            return processAnimation(cleanToken);
        }

        return null;
    }

    /**
     * Removes !important and other modifiers from a token.
     */
    private static String removeModifiers(String token) {
        String result = token;
        if (result.endsWith("!")) {
            result = result.substring(0, result.length() - 1);
        }
        // Remove responsive prefixes (sm:, md:, etc.) and state variants (hover:, focus:, etc.)
        int colonIdx = result.lastIndexOf(':');
        if (colonIdx != -1) {
            result = result.substring(colonIdx + 1);
        }
        return result;
    }

    private static TransitionResult processTransitionProperty(String token) {
        String cssProperty;
        
        switch (token) {
            case TRANSITION_NONE:
                cssProperty = "-fx-transition: none;";
                break;
            case TRANSITION_ALL:
                cssProperty = "-fx-transition: -fx-background-color, -fx-text-fill, -fx-opacity, -fx-scale-x, -fx-scale-y;";
                break;
            case TRANSITION_COLORS:
                cssProperty = "-fx-transition: -fx-background-color, -fx-text-fill, -fx-border-color;";
                break;
            case TRANSITION_OPACITY:
                cssProperty = "-fx-transition: -fx-opacity;";
                break;
            case TRANSITION_TRANSFORM:
                cssProperty = "-fx-transition: -fx-scale-x, -fx-scale-y, -fx-rotate, -fx-translate-x, -fx-translate-y;";
                break;
            default:
                return null;
        }

        return new TransitionResult(cssProperty, "transition", null, null);
    }

    private static TransitionResult processDuration(String token) {
        String durationValue = token.substring(DURATION_PREFIX.length());
        Integer durationMs = DURATIONS.get(durationValue);
        
        if (durationMs == null) {
            // Try parsing as arbitrary value
            if (durationValue.startsWith("[") && durationValue.endsWith("]")) {
                String arbitraryValue = durationValue.substring(1, durationValue.length() - 1);
                try {
                    durationMs = Integer.parseInt(arbitraryValue);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else {
                return null;
            }
        }

        String cssProperty = "-fx-transition-duration: " + durationMs + "ms;";
        return new TransitionResult(cssProperty, "duration", durationMs, null);
    }

    private static TransitionResult processEasing(String token) {
        String easingName = token.substring(EASE_PREFIX.length());
        String cubicBezier = EASINGS.get(easingName);
        
        if (cubicBezier == null) {
            return null;
        }

        String cssProperty = "-fx-transition-timing-function: " + cubicBezier + ";";
        return new TransitionResult(cssProperty, "ease", null, easingName);
    }

    private static TransitionResult processAnimation(String token) {
        String animationName = token.substring(ANIMATE_PREFIX.length());
        
        if (!ANIMATIONS.contains(animationName)) {
            return null;
        }

        // Note: JavaFX doesn't support CSS animations directly
        // This marks the token for programmatic animation handling
        String comment = "/* animate-" + animationName + " requires TwAnimation */";
        return new TransitionResult(comment, "animate", null, animationName);
    }

    /**
     * Result of processing a transition token.
     *
     * @param inlineStyle the generated CSS-like inline style
     * @param type the type of transition (transition, duration, ease, animate)
     * @param durationMs the duration in milliseconds (if applicable)
     * @param easingOrAnimation the easing function name or animation name (if applicable)
     */
    public record TransitionResult(
        String inlineStyle,
        String type,
        Integer durationMs,
        String easingOrAnimation
    ) {}
}
