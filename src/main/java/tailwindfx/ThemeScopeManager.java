package tailwindfx;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * ThemeScopeManager — Temas por subtree, con herencia y override parcial.
 *
 * Problema: ThemeManager es global (afecta toda la Scene).
 * Para apps enterprise con secciones de tema diferente en la misma pantalla:
 *   - Panel de admin: dark
 *   - Formulario embebido: light  
 *   - Sidebar: custom accent
 *
 * Solución: aplicar variables Modena como inline style en cualquier Pane,
 * que JavaFX propaga a todos sus hijos (cascada CSS local).
 *
 * Uso:
 *   // Toda la escena en dark
 *   TailwindFX.theme(scene).dark().apply();
 *
 *   // Solo el panel de alertas con acento rojo
 *   ThemeScopeManager.scope(alertPanel)
 *       .accent("#ef4444")
 *       .focus("#f87171")
 *       .apply();
 *
 *   // Override parcial — solo cambia el fondo de un drawer
 *   ThemeScopeManager.scope(drawer)
 *       .base("#1e293b")
 *       .apply();
 *
 *   // Limpiar scope de un nodo (hereda del padre)
 *   ThemeScopeManager.clearScope(drawer);
 *
 *   // Leer qué scope tiene un nodo
 *   Optional<String> base = ThemeScopeManager.getVar(myPane, "-fx-base");
 */
public final class ThemeScopeManager {

    private ThemeScopeManager() {}

    // =========================================================================
    // Builder
    // =========================================================================

    /**
     * Inicia la configuración de un scope para un Pane específico.
     *
     * TailwindFX también expone esto como:
     *   TailwindFX.scope(pane).dark().apply();
     */
    public static ScopeBuilder scope(Pane pane) {
        return new ScopeBuilder(pane);
    }

    /**
     * Elimina el scope de un Pane (hereda el tema del padre).
     */
    public static void clearScope(Pane pane) {
        // Eliminar solo las variables de tema del inline style, preservar el resto
        String current = pane.getStyle();
        if (current == null || current.isBlank()) return;
        Map<String, String> props = StyleMerger.parseStyle(current);
        props.keySet().removeIf(ThemeScopeManager::isThemeVar);
        pane.setStyle(StyleMerger.buildStyle(props));
        pane.getStyleClass().remove("dark");
        pane.getStyleClass().remove("theme-scoped");
    }

    /**
     * Lee el valor de una variable de tema en el scope de un Pane.
     * Devuelve vacío si el Pane no tiene scope propio para esa variable.
     */
    public static Optional<String> getVar(Pane pane, String variable) {
        if (pane.getStyle() == null) return Optional.empty();
        Map<String, String> props = StyleMerger.parseStyle(pane.getStyle());
        return Optional.ofNullable(props.get(variable));
    }

    /**
     * Comprueba si un Pane tiene scope de tema propio.
     */
    public static boolean hasScope(Pane pane) {
        return pane.getStyleClass().contains("theme-scoped");
    }

    /**
     * Copia el scope de un Pane a otro (útil para modales que deben heredar el tema de su origen).
     */
    public static void copyScope(Pane source, Pane target) {
        if (source.getStyle() == null || source.getStyle().isBlank()) return;
        Map<String, String> props = StyleMerger.parseStyle(source.getStyle());
        Map<String, String> themeProps = new LinkedHashMap<>();
        props.forEach((k, v) -> { if (isThemeVar(k)) themeProps.put(k, v); });
        if (!themeProps.isEmpty()) {
            String targetStyle = StyleMerger.merge(target.getStyle(), StyleMerger.buildStyle(themeProps));
            target.setStyle(targetStyle);
            if (source.getStyleClass().contains("dark")) {
                if (!target.getStyleClass().contains("dark")) target.getStyleClass().add("dark");
            }
            if (!target.getStyleClass().contains("theme-scoped")) {
                target.getStyleClass().add("theme-scoped");
            }
        }
    }

    // =========================================================================
    // Builder interno
    // =========================================================================

    public static final class ScopeBuilder {

        private final Pane pane;
        private final Map<String, String> vars = new LinkedHashMap<>();
        private Boolean forceDark = null;  // null = auto-detect, true = dark, false = light

        private ScopeBuilder(Pane pane) {
            this.pane = pane;
        }

        // --- Preset completo ---
        public ScopeBuilder dark() {
            forceDark = true;
            return base("#2b2b2b")
                .background("#1e1e1e")
                .innerBackground("#1e1e1e")
                .accent("#3b82f6")
                .focus("#60a5fa")
                .faintFocus("rgba(96,165,250,0.15)")
                .selectionBar("#1d4ed8")
                .defaultButton("#1e3a8a");
        }

        public ScopeBuilder light() {
            forceDark = false;
            return base("#ececec")
                .background("#f5f5f5")
                .innerBackground("#ffffff")
                .accent("#0096C9")
                .focus("#039ED3")
                .faintFocus("#039ED322")
                .defaultButton("#ABD8ED");
        }

        public ScopeBuilder preset(String name) {
            return switch (name.toLowerCase()) {
                case "dark"   -> dark();
                case "light"  -> light();
                case "blue"   -> base("#dbeafe").accent("#2563eb").focus("#3b82f6").faintFocus("rgba(59,130,246,0.2)");
                case "green"  -> base("#dcfce7").accent("#16a34a").focus("#22c55e").faintFocus("rgba(34,197,94,0.2)");
                case "purple" -> base("#ede9fe").accent("#7c3aed").focus("#8b5cf6").faintFocus("rgba(139,92,246,0.2)");
                case "rose"   -> base("#ffe4e6").accent("#e11d48").focus("#f43f5e").faintFocus("rgba(244,63,94,0.2)");
                case "slate"  -> base("#e2e8f0").accent("#475569").focus("#64748b").faintFocus("rgba(100,116,139,0.2)");
                default -> throw new IllegalArgumentException("Preset desconocido: " + name);
            };
        }

        // --- Variables individuales (override parcial) ---
        public ScopeBuilder base(String color)            { vars.put("-fx-base", color);                     return this; }
        public ScopeBuilder background(String color)      { vars.put("-fx-background", color);               return this; }
        public ScopeBuilder innerBackground(String color) { vars.put("-fx-control-inner-background", color); return this; }
        public ScopeBuilder accent(String color)          { vars.put("-fx-accent", color); vars.put("-fx-selection-bar", color); return this; }
        public ScopeBuilder focus(String color)           { vars.put("-fx-focus-color", color);              return this; }
        public ScopeBuilder faintFocus(String color)      { vars.put("-fx-faint-focus-color", color);        return this; }
        public ScopeBuilder defaultButton(String color)   { vars.put("-fx-default-button", color);           return this; }
        public ScopeBuilder selectionBar(String color)    { vars.put("-fx-selection-bar", color);            return this; }
        public ScopeBuilder cellHover(String color)       { vars.put("-fx-cell-hover-color", color);         return this; }
        public ScopeBuilder textFill(String color)        { vars.put("-fx-text-base-color", color);          return this; }

        /**
         * Aplica el scope al Pane.
         * Merge no destructivo: preserva inline styles previos no relacionados con theming.
         */
        public void apply() {
            if (vars.isEmpty()) return;

            // Merge con estilos existentes del Pane
            StringBuilder sb = new StringBuilder();
            vars.forEach((k, v) -> sb.append(k).append(": ").append(v).append("; "));
            String themeStyle = sb.toString().trim();
            String merged = StyleMerger.merge(pane.getStyle(), themeStyle);
            pane.setStyle(merged);

            // Gestionar clase .dark
            boolean dark;
            if (forceDark != null) {
                dark = forceDark;
            } else {
                // Auto-detect basado en luminancia del base
                String base = vars.getOrDefault("-fx-base", "#ececec");
                dark = isColorDark(base);
            }

            if (dark) {
                if (!pane.getStyleClass().contains("dark")) pane.getStyleClass().add("dark");
            } else {
                pane.getStyleClass().remove("dark");
            }

            // Marcar como con scope propio (para hasScope() y clearScope())
            if (!pane.getStyleClass().contains("theme-scoped")) {
                pane.getStyleClass().add("theme-scoped");
            }
        }

        /**
         * Solo actualiza variables específicas sin tocar el resto del scope.
         * Útil para cambios dinámicos (e.g., acento que cambia según estado).
         */
        public void update() {
            apply(); // mismo comportamiento — merge es no destructivo
        }
    }

    // =========================================================================
    // Nested scope support
    // =========================================================================

    /**
     * Finds the closest ancestor {@link Pane} that has a theme scope applied.
     *
     * <p>Useful when a node needs to know which scope it inherits. Returns
     * {@code null} if no scoped ancestor exists.
     *
     * @param node the node to start searching from
     * @return the nearest scoped ancestor Pane, or {@code null}
     */
    public static javafx.scene.layout.Pane findClosestScope(javafx.scene.Node node) {
        if (node == null) return null;
        javafx.scene.Parent parent = node.getParent();
        while (parent != null) {
            if (parent instanceof javafx.scene.layout.Pane pane
                    && pane.getStyleClass().contains("theme-scoped")) {
                return pane;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * Copies the effective theme scope that a node inherits to a target Pane.
     *
     * <p>Unlike {@link #copyScope(Pane, Pane)} which copies from a specific source,
     * this method walks up the scene graph from {@code node} to find the nearest
     * scoped ancestor and copies its variables to {@code target}. This is the
     * correct way to give a modal or popover the same theme as its trigger:
     *
     * <pre>
     * // When opening a modal from a button inside a dark-themed panel:
     * ThemeScopeManager.inheritScope(triggerButton, modalPane);
     * </pre>
     *
     * @param node   the node whose inherited scope to copy
     * @param target the Pane to apply the inherited scope to
     */
    public static void inheritScope(javafx.scene.Node node, javafx.scene.layout.Pane target) {
        if (node == null || target == null) return;
        javafx.scene.layout.Pane source = findClosestScope(node);
        if (source != null) {
            copyScope(source, target);
        } else {
            // No ancestor scope — clear any existing scope on target
            clearScope(target);
        }
    }

    /**
     * Reapplies a scope after a Pane is moved to a different parent or Scene.
     *
     * <p>JavaFX does not fire style-related events on reparenting, so scoped
     * inline styles are preserved, but this method forces a refresh so that
     * the new parent's CSS cascade takes effect alongside the scope variables.
     *
     * <pre>
     * // After moving a scoped pane to a different container:
     * newContainer.getChildren().add(scopedPane);
     * ThemeScopeManager.refreshScope(scopedPane);
     * </pre>
     *
     * @param pane the Pane to refresh
     */
    public static void refreshScope(javafx.scene.layout.Pane pane) {
        if (pane == null || !hasScope(pane)) return;
        // Force CSS re-evaluation by briefly toggling a dummy class
        String style = pane.getStyle();
        pane.applyCss();
        // Re-apply the existing style to flush the CSS engine
        pane.setStyle("");
        javafx.application.Platform.runLater(() -> pane.setStyle(style));
    }

    // =========================================================================
    // Internos
    // =========================================================================

    private static final Set<String> THEME_VARS = Set.of(
        "-fx-base", "-fx-background", "-fx-control-inner-background",
        "-fx-control-inner-background-alt", "-fx-accent", "-fx-default-button",
        "-fx-focus-color", "-fx-faint-focus-color", "-fx-color",
        "-fx-hover-base", "-fx-pressed-base", "-fx-selection-bar",
        "-fx-selection-bar-non-focused", "-fx-cell-hover-color",
        "-fx-text-base-color", "-fx-text-inner-color", "-fx-text-background-color"
    );

    private static boolean isThemeVar(String prop) {
        return THEME_VARS.contains(prop);
    }

    private static boolean isColorDark(String hexColor) {
        try {
            String hex = hexColor.trim().replaceAll("[^0-9a-fA-F]", "");
            if (hex.length() < 6) return false;
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0 < 0.4;
        } catch (Exception e) {
            return false;
        }
    }
}
