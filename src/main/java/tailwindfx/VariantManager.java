package tailwindfx;

import javafx.scene.Node;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor de variantes que integra las variantes de Tailwind CSS con JavaFX.
 * 
 * Maneja la aplicación dinámica de estilos basados en:
 * - Estado (hover, focus, active, disabled)
 * - Breakpoints responsivos (sm, md, lg, xl, 2xl)
 * - Tema (dark, light)
 * - Variantes arbitrarias
 */
public class VariantManager {
    
    private static final Map<Node, List<VariantListener>> nodeListeners = new ConcurrentHashMap<>();
    private static final Map<String, Integer> breakpointWidths = new HashMap<>();
    
    static {
        // Definir breakpoints estándar de Tailwind
        breakpointWidths.put("sm", 640);
        breakpointWidths.put("md", 768);
        breakpointWidths.put("lg", 1024);
        breakpointWidths.put("xl", 1280);
        breakpointWidths.put("2xl", 1536);
    }
    
    /**
     * Listener para manejar cambios de variante en un nodo.
     */
    @FunctionalInterface
    public interface VariantListener {
        void onVariantChange(boolean isActive);
    }
    
    /**
     * Aplica una variante a un nodo JavaFX.
     * 
     * @param node El nodo al que aplicar la variante
     * @param variant El nombre de la variante (hover, focus, md, etc.)
     * @param utility La utilidad base a aplicar cuando la variante está activa
     * @param jitCompiler El compilador JIT para generar los estilos
     */
    public static void applyVariant(Node node, String variant, String utility, JitCompiler jitCompiler) {
        if (node == null || variant == null || utility == null) {
            return;
        }
        
        // Determinar el tipo de variante
        String variantType = VariantParser.getVariantType(variant);
        
        switch (variantType) {
            case "state":
                applyStateVariant(node, variant, utility, jitCompiler);
                break;
            case "screen":
                applyScreenVariant(node, variant, utility, jitCompiler);
                break;
            case "theme":
                applyThemeVariant(node, variant, utility, jitCompiler);
                break;
            case "group":
                applyGroupVariant(node, variant, utility, jitCompiler);
                break;
            case "arbitrary":
                applyArbitraryVariant(node, variant, utility, jitCompiler);
                break;
            default:
                // Variante no soportada, aplicar directamente
                JitCompiler.CompileResult result = jitCompiler.compile(utility);
                if (result != null && result.hasInlineStyle()) {
                    node.setStyle(node.getStyle() + result.inlineStyle());
                }
        }
    }
    
    /**
     * Aplica una variante de estado (hover, focus, active, etc.).
     */
    private static void applyStateVariant(Node node, String variant, String utility, JitCompiler jitCompiler) {
        // Compilar la utilidad base
        JitCompiler.CompileResult result = jitCompiler.compile(utility);
        if (result == null || !result.hasInlineStyle()) {
            return;
        }
        String baseStyle = result.inlineStyle();
        
        // Limpiar listeners previos para este nodo
        cleanupListeners(node);
        
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
     * Aplica una variante de pantalla responsiva (sm, md, lg, etc.).
     */
    private static void applyScreenVariant(Node node, String variant, String utility, JitCompiler jitCompiler) {
        // Para variantes responsivas, necesitamos escuchar cambios en el tamaño de la ventana
        // Esto requiere acceso a la escena, lo cual manejaremos de forma diferida
        
        Integer minWidth = breakpointWidths.get(variant);
        if (minWidth == null) {
            // Intentar con prefijos min- y max-
            if (variant.startsWith("min-")) {
                String baseBreakpoint = variant.substring(4);
                minWidth = breakpointWidths.get(baseBreakpoint);
            } else if (variant.startsWith("max-")) {
                String baseBreakpoint = variant.substring(4);
                Integer maxWidth = breakpointWidths.get(baseBreakpoint);
                if (maxWidth != null) {
                    // Para max-, usamos maxWidth - 1
                    minWidth = maxWidth - 1;
                }
            }
        }
        
        if (minWidth != null) {
            // Registrar listener para cambios de tamaño
            registerBreakpointListener(node, variant, utility, jitCompiler, minWidth);
        }
    }
    
    /**
     * Aplica una variante de tema (dark, light).
     */
    private static void applyThemeVariant(Node node, String variant, String utility, JitCompiler jitCompiler) {
        boolean isDark = "dark".equals(variant);
        
        // Escuchar cambios en el tema - usar ThemeManager directamente
        // Nota: ThemeManager no tiene getInstance(), usamos métodos estáticos
        
        // Aplicar inmediatamente si el tema coincide (verificar preferencia del usuario)
        // Como no podemos acceder fácilmente al estado del tema, aplicamos diferido
        node.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Verificar si la escena tiene modo oscuro activado
                // Usamos una propiedad simple basada en el estilo de la escena
                boolean sceneIsDark = newScene.getRoot() != null && 
                    newScene.getRoot().getStyleClass().contains("dark");
                
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
     * Aplica una variante de grupo (group-hover, group-focus, etc.).
     */
    private static void applyGroupVariant(Node node, String variant, String utility, JitCompiler jitCompiler) {
        // Las variantes de grupo requieren encontrar el nodo padre con clase "group"
        // Esto es complejo en JavaFX, lo implementaremos de forma simplificada
        
        String groupVariant = variant.substring(6); // Remover "group-"
        
        // Buscar padre con clase "group"
        javafx.scene.Parent parent = node.getParent();
        while (parent != null) {
            if (parent.getStyleClass().contains("group")) {
                // Aplicar listener al padre
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
     * Aplica una variante arbitraria ([@media...], [&:hover], etc.).
     */
    private static void applyArbitraryVariant(Node node, String variant, String utility, JitCompiler jitCompiler) {
        // Extraer contenido de la variante arbitraria
        String content = variant.substring(1, variant.length() - 1);
        
        // Si es una media query, manejar como variante responsiva
        if (content.startsWith("@media")) {
            // Parsear la media query y aplicar listener apropiado
            // Implementación simplificada
            JitCompiler.CompileResult result = jitCompiler.compile(utility);
            if (result != null && result.hasInlineStyle()) {
                applyStyle(node, result.inlineStyle());
            }
        } else {
            // Otras variantes arbitrarias
            JitCompiler.CompileResult result = jitCompiler.compile(utility);
            if (result != null && result.hasInlineStyle()) {
                applyStyle(node, result.inlineStyle());
            }
        }
    }
    
    /**
     * Registra un listener para cambios de breakpoint.
     */
    private static void registerBreakpointListener(Node node, String variant, String utility, 
                                                    JitCompiler jitCompiler, int minWidth) {
        // Esta implementación requiere acceso a la escena
        // Se implementará cuando el nodo esté en una escena
        
        node.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((widthObs, oldWidth, newWidth) -> {
                    boolean isActive = newWidth.doubleValue() >= minWidth;
                    JitCompiler.CompileResult result = jitCompiler.compile(utility);
                    
                    if (isActive && result != null && result.hasInlineStyle()) {
                        applyStyle(node, result.inlineStyle());
                    } else {
                        removeStyle(node, result != null ? result.inlineStyle() : null);
                    }
                });
                
                // Evaluar inmediatamente
                boolean isActive = newScene.getWidth() >= minWidth;
                JitCompiler.CompileResult result = jitCompiler.compile(utility);
                if (isActive && result != null && result.hasInlineStyle()) {
                    applyStyle(node, result.inlineStyle());
                }
            }
        });
    }
    
    /**
     * Aplica un estilo a un nodo.
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
     * Remueve un estilo de un nodo.
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
     * Limpia los listeners de un nodo.
     */
    private static void cleanupListeners(Node node) {
        List<VariantListener> listeners = nodeListeners.remove(node);
        if (listeners != null) {
            listeners.clear();
        }
    }
    
    /**
     * Procesa un token con variantes y lo aplica a un nodo.
     * 
     * @param node El nodo al que aplicar el estilo
     * @param token El token completo (ej: "hover:bg-blue-500")
     * @param jitCompiler El compilador JIT
     */
    public static void processToken(Node node, String token, JitCompiler jitCompiler) {
        VariantParser.VariantResult result = VariantParser.parse(token);
        
        if (!result.hasVariant()) {
            // Sin variantes, aplicar directamente
            JitCompiler.CompileResult compileResult = jitCompiler.compile(token);
            if (compileResult != null && compileResult.hasInlineStyle()) {
                applyStyle(node, compileResult.inlineStyle());
            }
            return;
        }
        
        // Tiene variantes, procesar cada una
        List<String> variants = result.getVariants();
        String utility = result.getUtility();
        
        // Aplicar la última variante (la más específica)
        String lastVariant = variants.get(variants.size() - 1);
        applyVariant(node, lastVariant, utility, jitCompiler);
    }
}
