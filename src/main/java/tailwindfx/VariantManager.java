package tailwindfx;

import javafx.scene.Node;
import javafx.scene.Scene;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Variant Manager for TailwindFX — Handles variant application to JavaFX nodes.
 * 
 * This manager applies dynamic styles based on:
 * - State variants (hover, focus, active, disabled)
 * - Theme variants (dark, light)
 * - Group variants (group-hover, group-focus)
 * - Arbitrary variants ([&:hover], [@media...])
 * 
 * IMPORTANT: Responsive breakpoints (sm:, md:, lg:) are NOT handled here.
 * They are managed centrally by BreakpointManager to avoid O(N) listeners.
 * This class subscribes ONCE per Scene to breakpoint changes, not per node.
 * 
 * Architecture: Publisher-Subscriber pattern
 * - BreakpointManager: Single publisher (one listener per Stage with throttling)
 * - VariantManager: Subscriber (one subscription per Scene, iterates affected nodes)
 * 
 * @see BreakpointManager
 */
public class VariantManager {
    
    /** Tracks nodes that need responsive variant updates per Scene */
    private static final Map<Scene, List<ResponsiveBinding>> responsiveNodes = new WeakHashMap<>();
    
    /** Cache of compiled styles for responsive utilities */
    private static final Map<String, JitCompiler.CompileResult> styleCache = new ConcurrentHashMap<>();
    
    /**
     * Applies a state variant (hover, focus, active, disabled) to a JavaFX node.
     * 
     * @param node The target node
     * @param variant The variant name (e.g., "hover", "focus")
     * @param utility The base utility to apply when variant is active
     * @param jitCompiler JIT compiler for generating styles
     */
    public static void applyStateVariant(Node node, String variant, String utility, JitCompiler jitCompiler) {
        if (node == null || variant == null || utility == null) {
            return;
        }
        
        // Compile the base utility
        JitCompiler.CompileResult result = jitCompiler.compile(utility);
        if (result == null || !result.hasInlineStyle()) {
            return;
        }
        String baseStyle = result.inlineStyle();
        
        switch (variant) {
            case "hover":
                node.setOnMouseEntered(e -> applyStyle(node, baseStyle));
                node.setOnMouseExited(e -> removeStyle(node, baseStyle));
                break;
                
            case "focus":
                if (node instanceof javafx.scene.control.Control) {
                    javafx.scene.control.Control control = (javafx.scene.control.Control) node;
                    control.focusedProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal) {
                            applyStyle(node, baseStyle);
                        } else {
                            removeStyle(node, baseStyle);
                        }
                    });
                }
                break;
                
            case "active":
                node.setOnMousePressed(e -> applyStyle(node, baseStyle));
                node.setOnMouseReleased(e -> removeStyle(node, baseStyle));
                break;
                
            case "disabled":
                if (node instanceof javafx.scene.control.Control) {
                    javafx.scene.control.Control control = (javafx.scene.control.Control) node;
                    control.disableProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal) {
                            applyStyle(node, baseStyle);
                        } else {
                            removeStyle(node, baseStyle);
                        }
                    });
                }
                break;
                
            case "checked":
                if (node instanceof javafx.scene.control.CheckBox) {
                    javafx.scene.control.CheckBox checkBox = (javafx.scene.control.CheckBox) node;
                    checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal) {
                            applyStyle(node, baseStyle);
                        } else {
                            removeStyle(node, baseStyle);
                        }
                    });
                }
                break;
        }
    }
    
    /**
     * Registers a node for responsive variant updates.
     * 
     * This method subscribes ONCE per Scene to breakpoint changes, not per node.
     * When the breakpoint changes, all registered nodes in that Scene are updated.
     * 
     * @param node The target node
     * @param breakpoint The breakpoint name (e.g., "md", "lg")
     * @param utility The base utility to apply when breakpoint is active
     * @param jitCompiler JIT compiler for generating styles
     */
    public static void bindResponsiveVariant(Node node, String breakpoint, String utility, JitCompiler jitCompiler) {
        if (node == null || breakpoint == null || utility == null) {
            return;
        }
        
        // Cache the compiled style
        String cacheKey = breakpoint + ":" + utility;
        JitCompiler.CompileResult result = styleCache.computeIfAbsent(cacheKey, k -> jitCompiler.compile(utility));
        if (result == null || !result.hasInlineStyle()) {
            return;
        }
        
        // Store the binding
        ResponsiveBinding binding = new ResponsiveBinding(node, breakpoint, result.inlineStyle());
        
        node.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                unregisterFromScene(oldScene, binding);
            }
            if (newScene != null) {
                registerToScene(newScene, binding);
            }
        });
        
        // Register immediately if already in a scene
        Scene currentScene = node.getScene();
        if (currentScene != null) {
            registerToScene(currentScene, binding);
        }
    }
    
    /**
     * Registers a responsive binding to a Scene.
     * Subscribes to BreakpointManager only once per Scene.
     */
    private static void registerToScene(Scene scene, ResponsiveBinding binding) {
        List<ResponsiveBinding> bindings = responsiveNodes.computeIfAbsent(scene, k -> {
            // First binding for this Scene — subscribe to breakpoint changes
            subscribeToBreakpointChanges(scene);
            return new ArrayList<>();
        });
        
        // Avoid duplicate bindings
        if (!bindings.contains(binding)) {
            bindings.add(binding);
        }
        
        // Apply initial state
        applyResponsiveStyle(binding);
    }
    
    /**
     * Unregisters a responsive binding from a Scene.
     */
    private static void unregisterFromScene(Scene scene, ResponsiveBinding binding) {
        List<ResponsiveBinding> bindings = responsiveNodes.get(scene);
        if (bindings != null) {
            bindings.remove(binding);
            if (bindings.isEmpty()) {
                responsiveNodes.remove(scene);
            }
        }
    }
    
    /**
     * Subscribes to breakpoint changes for a Scene.
     * Called only once per Scene to avoid O(N) listeners.
     */
    private static void subscribeToBreakpointChanges(Scene scene) {
        var window = scene.getWindow();
        if (window instanceof javafx.stage.Stage) {
            BreakpointManager bpm = BreakpointManager.from((javafx.stage.Stage) window);
            
            // Listen to breakpoint changes and update all nodes in this Scene
            bpm.activeBreakpointProperty().addListener((obs, oldBp, newBp) -> {
                List<ResponsiveBinding> bindings = responsiveNodes.get(scene);
                if (bindings != null) {
                    for (ResponsiveBinding binding : bindings) {
                        applyResponsiveStyle(binding);
                    }
                }
            });
        }
    }
    
    /**
     * Applies or removes responsive style based on current breakpoint.
     */
    private static void applyResponsiveStyle(ResponsiveBinding binding) {
        var scene = binding.node().getScene();
        if (scene == null) return;
        
        var window = scene.getWindow();
        if (!(window instanceof javafx.stage.Stage)) return;
        
        BreakpointManager.Breakpoint currentBp = BreakpointManager.from((javafx.stage.Stage) window).current();
        int currentMinWidth = (int) currentBp.minWidth;
        int targetMinWidth = getBreakpointMinWidth(binding.breakpoint());
        
        boolean isActive = currentMinWidth >= targetMinWidth;
        
        if (isActive) {
            applyStyle(binding.node(), binding.style());
        } else {
            removeStyle(binding.node(), binding.style());
        }
    }
    
    /**
     * Gets the minimum width for a breakpoint name.
     */
    private static int getBreakpointMinWidth(String breakpoint) {
        return switch (breakpoint) {
            case "sm" -> 640;
            case "md" -> 768;
            case "lg" -> 1024;
            case "xl" -> 1280;
            case "2xl" -> 1536;
            default -> 0;
        };
    }
    
    /**
     * Applies a theme variant (dark, light) to a node.
     */
    public static void applyThemeVariant(Node node, String variant, String utility, JitCompiler jitCompiler) {
        boolean isDark = "dark".equals(variant);
        
        node.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && newScene.getRoot() != null) {
                boolean sceneIsDark = newScene.getRoot().getStyleClass().contains("dark");
                
                if (sceneIsDark == isDark) {
                    JitCompiler.CompileResult result = jitCompiler.compile(utility);
                    if (result != null && result.hasInlineStyle()) {
                        applyStyle(node, result.inlineStyle());
                    }
                }
            }
        });
    }
    
    /**
     * Applies a group variant (group-hover, group-focus) to a node.
     */
    public static void applyGroupVariant(Node node, String variant, String utility, JitCompiler jitCompiler) {
        String groupVariant = variant.substring(6); // Remove "group-" prefix
        
        javafx.scene.Parent parent = node.getParent();
        while (parent != null) {
            if (parent.getStyleClass().contains("group")) {
                switch (groupVariant) {
                    case "hover":
                        parent.setOnMouseEntered(e -> {
                            JitCompiler.CompileResult result = jitCompiler.compile(utility);
                            if (result != null && result.hasInlineStyle()) {
                                applyStyle(node, result.inlineStyle());
                            }
                        });
                        parent.setOnMouseExited(e -> {
                            JitCompiler.CompileResult result = jitCompiler.compile(utility);
                            if (result != null && result.hasInlineStyle()) {
                                removeStyle(node, result.inlineStyle());
                            }
                        });
                        break;
                }
                break;
            }
            parent = parent.getParent();
        }
    }
    
    /**
     * Applies an arbitrary variant ([@media...], [&:hover]) to a node.
     */
    public static void applyArbitraryVariant(Node node, String variant, String utility, JitCompiler jitCompiler) {
        String content = variant.substring(1, variant.length() - 1);
        
        if (content.startsWith("@media")) {
            // Handle as responsive variant (simplified)
            JitCompiler.CompileResult result = jitCompiler.compile(utility);
            if (result != null && result.hasInlineStyle()) {
                applyStyle(node, result.inlineStyle());
            }
        } else {
            // Other arbitrary variants
            JitCompiler.CompileResult result = jitCompiler.compile(utility);
            if (result != null && result.hasInlineStyle()) {
                applyStyle(node, result.inlineStyle());
            }
        }
    }
    
    /**
     * Applies a style to a node, avoiding duplicates.
     */
    private static void applyStyle(Node node, String style) {
        if (style == null || style.isEmpty()) {
            return;
        }
        
        String currentStyle = node.getStyle();
        if (!currentStyle.contains(style)) {
            node.setStyle(currentStyle + style);
        }
    }
    
    /**
     * Removes a style from a node.
     */
    private static void removeStyle(Node node, String style) {
        if (style == null || style.isEmpty()) {
            return;
        }
        
        String currentStyle = node.getStyle();
        String newStyle = currentStyle.replace(style, "");
        node.setStyle(newStyle);
    }
    
    /**
     * Processes a token with variants and applies it to a node.
     * 
     * @param node The target node
     * @param token The full token (e.g., "hover:bg-blue-500", "md:w-full")
     * @param jitCompiler JIT compiler
     */
    public static void processToken(Node node, String token, JitCompiler jitCompiler) {
        VariantParser.VariantResult result = VariantParser.parse(token);
        
        if (!result.hasVariant()) {
            // No variants, apply directly
            JitCompiler.CompileResult compileResult = jitCompiler.compile(token);
            if (compileResult != null && compileResult.hasInlineStyle()) {
                applyStyle(node, compileResult.inlineStyle());
            }
            return;
        }
        
        List<String> variants = result.getVariants();
        String utility = result.getUtility();
        String lastVariant = variants.get(variants.size() - 1);
        
        // Route to appropriate handler based on variant type
        String variantType = VariantParser.getVariantType(lastVariant);
        
        switch (variantType) {
            case "state":
                applyStateVariant(node, lastVariant, utility, jitCompiler);
                break;
            case "screen":
                bindResponsiveVariant(node, lastVariant, utility, jitCompiler);
                break;
            case "theme":
                applyThemeVariant(node, lastVariant, utility, jitCompiler);
                break;
            case "group":
                applyGroupVariant(node, lastVariant, utility, jitCompiler);
                break;
            case "arbitrary":
                applyArbitraryVariant(node, lastVariant, utility, jitCompiler);
                break;
            default:
                // Unknown variant, apply directly
                JitCompiler.CompileResult compileResult = jitCompiler.compile(utility);
                if (compileResult != null && compileResult.hasInlineStyle()) {
                    applyStyle(node, compileResult.inlineStyle());
                }
        }
    }
    
    /**
     * Record representing a responsive binding between a node and a breakpoint.
     */
    private record ResponsiveBinding(Node node, String breakpoint, String style) {}
}
