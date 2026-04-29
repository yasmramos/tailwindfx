package io.github.yasmramos.tailwindfx.theme;

import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import io.github.yasmramos.tailwindfx.core.Preconditions;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ThemeManager v2 — Motor de temas de TailwindFX.
 *
 * Características: 1. VARIABLES MODENA: sobreescribe -fx-base, -fx-accent, etc.
 * en el root. Modena propaga automáticamente a TODOS los controles hijos.
 *
 * 2. TEMAS PREDEFINIDOS: light, dark, blue, green, purple, rose, slate.
 *
 * 3. HERENCIA DE TEMAS: tema base + override parcial.
 * ThemeManager.theme(scene).preset("dark").accent("#f97316").apply();
 *
 * 4. SCOPES: aplicar un tema solo a un subárbol de nodos.
 * ThemeManager.scope(myPanel).preset("dark").applyTo(myPanel);
 *
 * 5. TRANSICIÓN ANIMADA (opcional):
 * ThemeManager.theme(scene).dark().animated(300).apply();
 *
 * Uso básico: TailwindFX.theme(scene).dark().apply();
 * TailwindFX.theme(scene).preset("blue").apply();
 * TailwindFX.theme(scene).base("#1e293b").accent("#3b82f6").apply();
 * ThemeManager.toggle(scene);
 */
public final class ThemeManager {

    // =========================================================================
    // Temas predefinidos
    // =========================================================================
    private record ThemeVars(
            String base, String innerBg, String bg,
            String accent, String focus, String faintFocus, String defaultBtn) {

    }

    private static final Map<String, ThemeVars> PRESETS = new LinkedHashMap<>();

    static {
        PRESETS.put("light",
                new ThemeVars("#ececec", "#f5f5f5", "#f9f9f9", "#0096C9", "#039ED3", "#039ED322", "#ABD8ED"));
        PRESETS.put("dark", new ThemeVars("#2b2b2b", "#1e1e1e", "#161616", "#3b82f6", "#60a5fa",
                "rgba(96,165,250,0.15)", "#1e3a8a"));
        PRESETS.put("blue", new ThemeVars("#dbeafe", "#e0ecff", "#f0f7ff", "#2563eb", "#3b82f6", "rgba(59,130,246,0.2)",
                "#93c5fd"));
        PRESETS.put("green",
                new ThemeVars("#dcfce7", "#e0fbe9", "#f0fdf4", "#16a34a", "#22c55e", "rgba(34,197,94,0.2)", "#86efac"));
        PRESETS.put("purple", new ThemeVars("#ede9fe", "#f0ecff", "#f9f7ff", "#7c3aed", "#8b5cf6",
                "rgba(139,92,246,0.2)", "#c4b5fd"));
        PRESETS.put("rose",
                new ThemeVars("#ffe4e6", "#ffe8ea", "#fff5f6", "#e11d48", "#f43f5e", "rgba(244,63,94,0.2)", "#fda4af"));
        PRESETS.put("slate", new ThemeVars("#e2e8f0", "#eaf0f6", "#f1f5f9", "#475569", "#64748b",
                "rgba(100,116,139,0.2)", "#94a3b8"));
    }

    /**
     * Lista de temas disponibles
     */
    public static List<String> availableThemes() {
        return new ArrayList<>(PRESETS.keySet());
    }

    // =========================================================================
    // Estado del builder
    // =========================================================================
    private final Scene scene;
    private final Node scopeNode; // null = aplica al root de la scene
    private final Map<String, String> vars = new LinkedHashMap<>();
    private long animDurationMs = 0;

    private ThemeManager(Scene scene, Node scopeNode) {
        this.scene = Preconditions.requireNonNull(scene, "ThemeManager", "scene");
        this.scopeNode = scopeNode;
    }

    // =========================================================================
    // Factories
    // =========================================================================
    /**
     * Aplica al root completo de la Scene
     */
    public static ThemeManager forScene(Scene scene) {
        return new ThemeManager(scene, null);
    }

    /**
     * Aplica solo al nodo indicado y su subárbol
     */
    public static ThemeManager scope(Node node) {
        return new ThemeManager(null, node);
    }

    // =========================================================================
    // Builder — preset
    // =========================================================================
    /**
     * Aplica un tema predefinido
     */
    public ThemeManager preset(String name) {
        Preconditions.requireNonBlank(name, "ThemeManager.preset", "name");
        ThemeVars t = PRESETS.get(name.toLowerCase());
        if (t == null) {
            throw new IllegalArgumentException(
                    "ThemeManager.preset: tema '" + name + "' no existe. Disponibles: " + PRESETS.keySet());
        }
        return base(t.base()).innerBackground(t.innerBg()).background(t.bg())
                .accent(t.accent()).focus(t.focus())
                .faintFocus(t.faintFocus()).defaultButton(t.defaultBtn());
    }

    /**
     * Alias rápidos
     */
    public ThemeManager dark() {
        return preset("dark");
    }

    public ThemeManager light() {
        return preset("light");
    }

    // =========================================================================
    // Builder — variables individuales
    // =========================================================================
    public ThemeManager base(String color) {
        Preconditions.requireNonBlank(color, "ThemeManager.base", "color");
        vars.put("-fx-base", color);
        return this;
    }

    public ThemeManager background(String color) {
        Preconditions.requireNonBlank(color, "ThemeManager.background", "color");
        vars.put("-fx-background", color);
        return this;
    }

    public ThemeManager innerBackground(String color) {
        Preconditions.requireNonBlank(color, "ThemeManager.innerBackground", "color");
        vars.put("-fx-control-inner-background", color);
        return this;
    }

    public ThemeManager accent(String color) {
        Preconditions.requireNonBlank(color, "ThemeManager.accent", "color");
        vars.put("-fx-accent", color);
        vars.put("-fx-selection-bar", color);
        return this;
    }

    public ThemeManager focus(String color) {
        Preconditions.requireNonBlank(color, "ThemeManager.focus", "color");
        vars.put("-fx-focus-color", color);
        return this;
    }

    public ThemeManager faintFocus(String color) {
        Preconditions.requireNonBlank(color, "ThemeManager.faintFocus", "color");
        vars.put("-fx-faint-focus-color", color);
        return this;
    }

    public ThemeManager defaultButton(String color) {
        Preconditions.requireNonBlank(color, "ThemeManager.defaultButton", "color");
        vars.put("-fx-default-button", color);
        return this;
    }

    /**
     * Activa transición animada al aplicar el tema. durationMs = milisegundos
     */
    public ThemeManager animated(long durationMs) {
        animDurationMs = durationMs;
        return this;
    }

    // =========================================================================
    // apply() — inyecta el tema
    // =========================================================================
    /**
     * Aplica el tema al root de la Scene (o al scopeNode si se usó scope()).
     * 
     * <ul>
     * <li>Forces style refresh on ALL descendant nodes</li>
     * <li>Applies theme to Stage window chrome (if available)</li>
     * <li>Ensures Modena variables propagate correctly</li>
     * </ul>
     */
    public void apply() {
        if (vars.isEmpty()) {
            Preconditions.LOG.warning(
                    "ThemeManager.apply: ninguna variable definida — usa preset() o base()/accent() antes de apply()");
            return;
        }

        Node target = resolveTarget();
        if (target == null) {
            Preconditions.LOG.warning("ThemeManager.apply: target not found — Scene has no root or scopeNode is null");
            return;
        }

        String newStyle = buildStyleString();

        if (animDurationMs > 0) {
            applyAnimated(target, newStyle);
        } else {
            target.setStyle(newStyle);
        }

        // CRITICAL FIX 1: Force style refresh on all descendant nodes
        // JavaFX caches computed styles, so we need to invalidate the cache
        forceStyleRefresh(target);

        TailwindFXMetrics.instance().recordThemeSwitch();

        // Gestionar clase .dark
        boolean isDark = isColorDark(vars.getOrDefault("-fx-base", "#ececec"));
        target.getStyleClass().remove("dark");
        if (isDark) {
            target.getStyleClass().add("dark");
        }

        // CRITICAL FIX 2: Apply theme to Stage window chrome (title bar, borders)
        if (scene != null && scene.getWindow() instanceof javafx.stage.Stage stage) {
            applyToStage(stage, isDark);
        }
    }

    /**
     * Aplica el tema a un nodo específico (scope externo)
     * 
     * <p>
     * Forces style refresh on the scoped node tree.
     */
    public void applyTo(Node node) {
        if (vars.isEmpty()) {
            return;
        }
        String style = buildStyleString();
        node.setStyle(style);

        // CRITICAL FIX: Force style refresh on scoped subtree
        forceStyleRefresh(node);

        boolean isDark = isColorDark(vars.getOrDefault("-fx-base", "#ececec"));
        node.getStyleClass().remove("dark");
        if (isDark) {
            node.getStyleClass().add("dark");
        }
    }

    /**
     * Elimina el tema y vuelve a Modena por defecto.
     */
    public void reset() {
        Node target = resolveTarget();
        if (target != null) {
            target.setStyle("");
            target.getStyleClass().remove("dark");
        }
    }

    // =========================================================================
    // Estáticos de conveniencia
    // =========================================================================
    /**
     * Alterna dark ↔ light
     */
    public static void toggle(Scene scene) {
        if (isDark(scene)) {
            forScene(scene).light().apply();
        } else {
            forScene(scene).dark().apply();
        }
    }

    /**
     * ¿El tema actual es dark?
     */
    public static boolean isDark(Scene scene) {
        return scene.getRoot().getStyleClass().contains("dark");
    }

    /**
     * Cicla por los temas predefinidos en orden
     */
    public static void cyclePreset(Scene scene) {
        List<String> themes = availableThemes();
        String style = scene.getRoot().getStyle();
        // Buscar qué tema está activo por comparación de color base
        int next = 0;
        for (int i = 0; i < themes.size(); i++) {
            ThemeVars t = PRESETS.get(themes.get(i));
            if (style.contains(t.base())) {
                next = (i + 1) % themes.size();
                break;
            }
        }
        forScene(scene).preset(themes.get(next)).apply();
    }

    // =========================================================================
    // Internos
    // =========================================================================
    private Node resolveTarget() {
        if (scopeNode != null) {
            return scopeNode;
        }
        if (scene != null) {
            return scene.getRoot();
        }
        return null;
    }

    private String buildStyleString() {
        StringBuilder sb = new StringBuilder();
        for (var e : vars.entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append("; ");
        }
        return sb.toString().trim();
    }

    private void applyAnimated(Node target, String newStyle) {
        // Animación de opacidad para hacer la transición suave
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(target.opacityProperty(), 1.0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(animDurationMs / 2.0),
                        new KeyValue(target.opacityProperty(), 0.85, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(animDurationMs / 2.0 + 1), e -> target.setStyle(newStyle)),
                new KeyFrame(Duration.millis(animDurationMs),
                        new KeyValue(target.opacityProperty(), 1.0, Interpolator.EASE_BOTH)));
        tl.play();
    }

    private boolean isColorDark(String hex) {
        try {
            String h = hex.trim().replaceAll("[^0-9a-fA-F]", "");
            if (h.length() < 6) {
                return false;
            }
            int r = Integer.parseInt(h.substring(0, 2), 16);
            int g = Integer.parseInt(h.substring(2, 4), 16);
            int b = Integer.parseInt(h.substring(4, 6), 16);
            // Luminancia relativa W3C
            return (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0 < 0.4;
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================================
    // PERSISTENCIA DE TEMAS — java.util.prefs.Preferences
    // Los temas se guardan en las preferencias del SO y sobreviven entre sesiones.
    // =========================================================================
    /**
     * Guarda el tema actual de la escena en las preferencias del usuario.
     *
     * <pre>
     * ThemeManager.saveTheme(scene, "myapp.mainWindow");
     * </pre>
     *
     * @param scene Scene cuyo tema guardar
     * @param key   clave única (ej: "myapp.theme"). Se recomienda usar el nombre
     *              de la app.
     */
    public static void saveTheme(Scene scene, String key) {
        Preconditions.requireNonNull(scene, "ThemeManager.saveTheme", "scene");
        Preconditions.requireNonBlank(key, "ThemeManager.saveTheme", "key");
        if (scene.getRoot() == null) {
            Preconditions.LOG.warning("ThemeManager.saveTheme: Scene has no root — nothing to save");
            return;
        }
        try {
            Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
            String style = scene.getRoot().getStyle();
            prefs.put(key + ".style", style != null ? style : "");
            prefs.putBoolean(key + ".dark", isDark(scene));
            prefs.flush();
        } catch (Exception e) {
            Preconditions.LOG.warning("ThemeManager.saveTheme: error — " + e.getMessage());
        }
    }

    /**
     * Restaura un tema guardado previamente con {@link #saveTheme}.
     * 
     * <pre>
     * boolean loaded = ThemeManager.loadTheme(scene, "myapp.mainWindow");
     * if (!loaded)
     *     ThemeManager.of(scene).preset("dark").apply(); // fallback
     * </pre>
     *
     * @param scene Scene a la que aplicar el tema
     * @param key   clave usada en {@link #saveTheme}
     * @return {@code true} si se encontró y aplicó el tema guardado
     */
    public static boolean loadTheme(Scene scene, String key) {
        Preconditions.requireNonNull(scene, "ThemeManager.loadTheme", "scene");
        Preconditions.requireNonBlank(key, "ThemeManager.loadTheme", "key");
        try {
            Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
            String style = prefs.get(key + ".style", null);
            if (style == null) {
                return false;
            }
            boolean dark = prefs.getBoolean(key + ".dark", false);
            Platform.runLater(() -> {
                if (scene.getRoot() != null) {
                    scene.getRoot().setStyle(style);

                    // CRITICAL FIX: Force style refresh
                    forceStyleRefresh(scene.getRoot());

                    if (dark) {
                        if (!scene.getRoot().getStyleClass().contains("dark")) {
                            scene.getRoot().getStyleClass().add("dark");
                        }
                    } else {
                        scene.getRoot().getStyleClass().remove("dark");
                    }

                    // Apply to Stage if available
                    if (scene.getWindow() instanceof javafx.stage.Stage stage) {
                        applyToStage(stage, dark);
                    }
                }
            });
            return true;
        } catch (Exception e) {
            Preconditions.LOG.warning("ThemeManager.loadTheme: error — " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un tema guardado de las preferencias del sistema.
     *
     * @param key clave usada en {@link #saveTheme}
     */
    public static void deleteTheme(String key) {
        Preconditions.requireNonBlank(key, "ThemeManager.deleteTheme", "key");
        try {
            Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
            prefs.remove(key + ".style");
            prefs.remove(key + ".dark");
            prefs.flush();
        } catch (Exception e) {
            Preconditions.LOG.warning("ThemeManager.deleteTheme: error — " + e.getMessage());
        }
    }

    // =========================================================================
    // CRITICAL FIX: Force style refresh helpers
    // =========================================================================

    /**
     * Forces a complete style refresh on a node and all its descendants.
     * 
     * <p>
     * JavaFX caches computed styles for performance. When theme variables change,
     * we need to invalidate this cache to ensure all components pick up the new
     * values.
     * 
     * <p>
     * This method:
     * <ol>
     * <li>Triggers applyCss() on the node tree (forces style recalculation)</li>
     * <li>Requests layout on Parent nodes (ensures proper sizing with new
     * styles)</li>
     * <li>Schedules a second pass on next frame (catches lazy-loaded
     * components)</li>
     * </ol>
     */
    private static void forceStyleRefresh(Node root) {
        if (root == null)
            return;

        // Pass 1: Immediate refresh
        root.applyCss();
        if (root instanceof javafx.scene.Parent) {
            ((javafx.scene.Parent) root).requestLayout();
        }

        // Refresh all descendants
        refreshDescendants(root);

        // Pass 2: Deferred refresh (catches components that load lazily)
        Platform.runLater(() -> {
            root.applyCss();
            refreshDescendants(root);
        });
    }

    /**
     * Recursively applies CSS and requests layout on all descendant nodes.
     */
    private static void refreshDescendants(Node node) {
        if (node instanceof javafx.scene.Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                child.applyCss();
                if (child instanceof javafx.scene.Parent) {
                    ((javafx.scene.Parent) child).requestLayout();
                }
                refreshDescendants(child);
            }
        }
    }

    /**
     * Applies theme styling to the Stage window chrome (title bar, borders).
     * 
     * <p>
     * On macOS and Windows, JavaFX allows styling the native window decorations.
     * This method applies appropriate styling based on the theme mode.
     * 
     * <p>
     * Note: This only works when the Stage is showing and uses native decorations.
     * 
     * @param stage  the Stage to style
     * @param isDark whether the current theme is dark mode
     */
    private static void applyToStage(javafx.stage.Stage stage, boolean isDark) {
        if (stage == null || !stage.isShowing())
            return;

        try {
            // Apply user-agent stylesheet to the Scene
            // This ensures the Stage picks up the theme variables
            javafx.scene.Scene scene = stage.getScene();
            if (scene != null) {
                // Force a full style recalculation on the scene
                scene.getRoot().applyCss();

                // On some platforms, we can hint the OS about the theme preference
                // This affects the native window chrome (title bar, borders)
                if (isDark) {
                    // Dark mode hint - supported on macOS 10.14+ and Windows 10+
                    stage.getProperties().put("apple.awt.application.appearance", "NSAppearanceNameDarkAqua");
                    stage.getProperties().put("windows.theme", "dark");
                } else {
                    // Light mode hint
                    stage.getProperties().put("apple.awt.application.appearance", "NSAppearanceNameAqua");
                    stage.getProperties().put("windows.theme", "light");
                }
            }
        } catch (Exception e) {
            // Silently fail if platform doesn't support theme hints
            // This is not critical - the content will still be themed correctly
        }
    }

}
