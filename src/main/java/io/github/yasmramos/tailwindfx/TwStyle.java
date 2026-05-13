package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.core.Preconditions;
import io.github.yasmramos.tailwindfx.core.UtilityConflictResolver;
import io.github.yasmramos.tailwindfx.style.StyleMerger;
import io.github.yasmramos.tailwindfx.style.StylePerf;
import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;

import javafx.scene.Node;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * TwStyle — Style facade for utility classes and JIT tokens.
 * 
 * <p>This class handles applying, removing, and toggling CSS classes
 * and JIT-compiled styles on JavaFX nodes.</p>
 * 
 * <p>Usage:</p>
 * <pre>
 * TwStyle.apply(node, "btn-primary", "rounded-lg");
 * TwStyle.jit(node, "bg-blue-500/80", "p-[13px]");
 * TwStyle.remove(node, "old-class");
 * TwStyle.toggle(node, "active");
 * </pre>
 */
public final class TwStyle {
    
    public static final TwStyle INSTANCE = new TwStyle();
    
    private static final Set<String> JIT_PREFIXES = new HashSet<>(Arrays.asList(
        "bg", "text", "border", "ring", "shadow", "w", "h", "min-w", "min-h", "max-w", "max-h",
        "p", "px", "py", "pt", "pr", "pb", "pl", "m", "mx", "my", "mt", "mr", "mb", "ml", "space",
        "translate", "rotate", "scale", "skew", "opacity", "z", "order", "col", "row", "gap",
        "inset", "top", "right", "bottom", "left", "blur", "brightness", "contrast", "grayscale",
        "hue-rotate", "invert", "saturate", "sepia", "drop-shadow", "backdrop"
    ));
    
    private TwStyle() {}
    
    /**
     * Applies utility classes and JIT tokens to a node with intelligent auto-detection.
     */
    public void apply(Node node, String... tokens) {
        Preconditions.requireNode(node, "TwStyle.apply");
        if (tokens == null || tokens.length == 0) return;
        
        if (StylePerf.isBatchActive()) {
            StylePerf.enqueueDeferredApply(node, tokens);
        } else {
            applyInternal(node, tokens);
        }
    }
    
    private void applyInternal(Node node, String... tokens) {
        java.util.List<String> cssClasses = new java.util.ArrayList<>();
        java.util.List<String> jitTokens = new java.util.ArrayList<>();
        
        for (String token : tokens) {
            if (token == null || token.isBlank()) continue;
            for (String t : token.split("\\s+")) {
                if (t.isBlank()) continue;
                if (isJitToken(t)) {
                    jitTokens.add(t);
                } else {
                    cssClasses.add(t); 
                }
            }
        }
        
        if (!cssClasses.isEmpty()) {
            UtilityConflictResolver.applyAll(node, cssClasses.toArray(new String[0]));
            TailwindFXMetrics.instance().recordApply(cssClasses.size());
        }
        
        if (!jitTokens.isEmpty()) {
            StyleMerger.applyJit(node, jitTokens.toArray(new String[0]));
        }
    }
    
    /**
     * Applies utility classes WITHOUT conflict resolution.
     */
    public void applyRaw(Node node, String... classes) {
        for (String c : classes) {
            if (c == null || c.isBlank()) continue;
            for (String part : c.split("\\s+")) {
                if (!part.isBlank() && !node.getStyleClass().contains(part)) {
                    node.getStyleClass().add(part);
                }
            }
        }
    }
    
    /**
     * Removes CSS classes from a node.
     */
    public void remove(Node node, String... classes) { 
        node.getStyleClass().removeAll(Arrays.asList(classes));
    }
    
    /**
     * Replaces all CSS classes on a node.
     */
    public void replace(Node node, String... classes) {
        node.getStyleClass().setAll(Arrays.asList(classes));
    }
    
    /**
     * Toggles a CSS class on a node.
     */
    public void toggle(Node node, String cssClass) {
        if (node.getStyleClass().contains(cssClass)) {
            node.getStyleClass().remove(cssClass);
        } else {
            node.getStyleClass().add(cssClass);
        }
    }
    
    /**
     * Detects if a token should be compiled as JIT.
     * Uses strict prefix matching + numeric/arbitrary/negative pattern validation.
     * Eliminates false positives like "card-2" or "panel-v2".
     */
    private boolean isJitToken(String token) {
        // Opacity modifier: bg-blue-500/80 - but only for valid color utilities
        if (token.contains("/")) {
            String base = token.substring(0, token.indexOf('/'));
            return isValidColorUtilityBase(base);
        }
        if (token.contains("[")) return true;  // arbitrary: w-[320px]
        
        // Strict negative prefix: only JIT if followed by a known property prefix
        if (token.startsWith("-") && token.length() > 1) {
            String withoutNeg = token.substring(1);
            return JIT_PREFIXES.stream().anyMatch(withoutNeg::startsWith);
        }
        
        // Must start with a known Tailwind prefix AND contain a numeric modifier
        boolean hasPrefix = JIT_PREFIXES.stream().anyMatch(p -> 
            token.startsWith(p) && (token.length() == p.length() || token.charAt(p.length()) == '-'));
            
        if (hasPrefix) {
            return token.matches(".*\\d+.*");
        }
        
        return false;
    }
    
    /**
     * Validates if a base token (before /) is a valid color utility that can have opacity.
     * Prevents false positives like "icon/large" being treated as JIT.
     */
    private boolean isValidColorUtilityBase(String base) {
        // Color utilities that support opacity: bg-*, text-*, border-*, ring-*, shadow-*
        String[] colorPrefixes = {"bg-", "text-", "border-", "ring-", "shadow-"};
        
        for (String prefix : colorPrefixes) {
            if (base.startsWith(prefix) && base.length() > prefix.length()) {
                String colorPart = base.substring(prefix.length());
                // Valid color patterns: blue-500, red-900, gray-50, custom-color
                // Must contain at least one hyphen or be a simple color name
                return colorPart.contains("-") || colorPart.matches("[a-zA-Z]+");
            }
        }
        return false;
    }
}
