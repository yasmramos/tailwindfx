package tailwindfx;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * TailwindFX — Main entry point.
 *
 * Basic usage:
 *   TailwindFX.install(scene);
 *   TailwindFX.apply(node, "btn-primary", "rounded-lg");
 *   TailwindFX.jit(node, "bg-blue-500/80", "p-[13px]", "-translate-x-4");
 *   TailwindFX.jitApply(node, "btn-primary", "bg-blue-500/80", "p-[13px]");
 *   TailwindFX.layout(container).row().gap(8).center().build();
 *   TailwindFX.responsive(stage).withOrientation();
 *   TailwindFX.theme(scene).dark().apply();
 */
public final class TailwindFX {

    private static final String CSS_PATH = "/tailwindfx/tailwindfx.css";

    private TailwindFX() {}

    // =========================================================================
    // Installation
    // =========================================================================

    /**
     * Installs the combined CSS file (all modules).
     */
    public static void install(Scene scene) {
        String url = Objects.requireNonNull(
            TailwindFX.class.getResource(CSS_PATH),
            "tailwindfx.css not found in classpath"
        ).toExternalForm();
        if (!scene.getStylesheets().contains(url)) {
            scene.getStylesheets().add(url);
        }
    }

    /**
     * Installs only the base module (variables and reset).
     * Required by other modules.
     */
    public static void installBase(Scene scene){
        installCss(scene, "/tailwindfx/tailwindfx-base.css");
    }

    /**
     * Installs the JavaFX components module.
     */
    public static void installComponents(Scene scene){
        installCss(scene, "/tailwindfx/tailwindfx-components.css");
    }

    /**
     * Installs the utilities module (layout, spacing, sizing).
     */
    public static void installUtilities(Scene scene){
        installCss(scene, "/tailwindfx/tailwindfx-utilities.css");
    }

    /**
     * Installs the colors and typography module.
     */
    public static void installColors(Scene scene){
        installCss(scene, "/tailwindfx/tailwindfx-colors.css");
    }

    /**
     * Installs the effects module (shadows, transforms, filters).
     */
    public static void installEffects(Scene scene){
        installCss(scene, "/tailwindfx/tailwindfx-effects.css");
    }

    /**
     * Installs the components preset module (cards, badges, buttons, etc.).
     */
    public static void installComponentsPreset(Scene scene){
        installCss(scene, "/tailwindfx/tailwindfx-components-preset.css");
    }

    /**
     * Installs the dark mode module.
     */
    public static void installDark(Scene scene){
        installCss(scene, "/tailwindfx/tailwindfx-dark.css");
    }

    /**
     * Installs essential modules (base + components + components-preset).
     * This is the recommended setup for most applications.
     */
    public static void installEssentials(Scene scene){
        installBase(scene);
        installComponents(scene);
        installComponentsPreset(scene);
    }

    /**
     * Installs all individual modules.
     * Equivalent to install() but loading each file separately.
     */
    public static void installAll(Scene scene){
        installBase(scene);
        installComponents(scene);
        installUtilities(scene);
        installColors(scene);
        installEffects(scene);
        installComponentsPreset(scene);
        installDark(scene);
    }
    
    public static void installAll(Scene scene, Stage stage){
        installBase(scene);
        installComponents(scene);
        installUtilities(scene);
        installColors(scene);
        installEffects(scene);
        installComponentsPreset(scene);
        installDark(scene);
        responsive(stage);
    }

    private static void installCss(Scene scene, String css_path){
        String url = Objects.requireNonNull(
            TailwindFX.class.getResource(css_path),
            css_path +" not found in classpath"
        ).toExternalForm();
        if (!scene.getStylesheets().contains(url)) {
            scene.getStylesheets().add(url);
        }
    }

    public static void install(Scene scene, Stage stage) {
        install(scene);
        responsive(stage);
    }

    // =========================================================================
    // Unified Apply — Intelligent detection of CSS classes vs JIT tokens
    // =========================================================================

    /**
     * Applies utility classes and JIT tokens to a node with intelligent auto-detection.
     *
     * <p><b>UNIFIED API</b> — This method automatically detects:
     * <ul>
     * <li>CSS classes from stylesheet (e.g., {@code "btn-primary", "rounded-lg"})</li>
     * <li>JIT tokens with opacity (e.g., {@code "bg-blue-500/80"})</li>
     * <li>JIT tokens with negative values (e.g., {@code "-translate-x-4"})</li>
     * <li>JIT tokens with arbitrary values (e.g., {@code "w-[320px]", "p-[13px]"})</li>
     * </ul>
     *
     * <p>Conflict resolution is automatic for CSS classes. JIT tokens are applied
     * as inline styles and merged non-destructively with existing styles.
     *
     * <h3>Usage Examples</h3>
     * <pre>
     * // Pure CSS classes
     * TailwindFX.apply(button, "btn-primary", "rounded-lg");
     *
     * // Mixed CSS + JIT (auto-detected)
     * TailwindFX.apply(card, "card", "shadow-md", "bg-blue-500/80", "p-[13px]");
     *
     * // JIT with arbitrary values
     * TailwindFX.apply(node, "w-[320px]", "-translate-x-4", "rotate-[45deg]");
     *
     * // Space-separated strings work too
     * TailwindFX.apply(button, "btn-primary rounded-lg bg-blue-500/80");
     * </pre>
     *
     * <h3>Detection Logic</h3>
     * <p>A token is treated as JIT if it contains:
     * <ul>
     * <li>Opacity slash: {@code "bg-blue-500/80"}</li>
     * <li>Arbitrary value brackets: {@code "w-[320px]"}</li>
     * <li>Leading minus: {@code "-translate-x-4"}</li>
     * </ul>
     * Otherwise, it's treated as a CSS class.
     *
     * @param node    the JavaFX node to style
     * @param tokens  one or more utility classes or JIT tokens (space-separated or varargs)
     */
    public static void apply(Node node, String... tokens) {
        Preconditions.requireNode(node, "TailwindFX.apply");
        if (tokens == null || tokens.length == 0) return;
        
        if (StylePerf.isBatchActive()) {
            StylePerf.enqueueDeferredApply(node, tokens);
        } else {
            applyInternal(node, tokens);
        }
    }

    /**
     * Internal unified application logic.
     * Separates tokens into CSS classes and JIT tokens, then applies both.
     */
    private static void applyInternal(Node node, String... tokens) {
        java.util.List<String> cssClasses = new java.util.ArrayList<>();
        java.util.List<String> jitTokens = new java.util.ArrayList<>();
        
        // Split and categorize tokens
        for (String token : tokens) {
            if (token == null || token.isBlank()) continue;
            
            // Handle space-separated tokens
            for (String t : token.split("\\s+")) {
                if (t.isBlank()) continue;
                
                if (isJitToken(t)) {
                    jitTokens.add(t);
                } else {
                    cssClasses.add(t);
                }
            }
        }
        
        // Apply CSS classes with conflict resolution
        if (!cssClasses.isEmpty()) {
            UtilityConflictResolver.applyAll(node, cssClasses.toArray(new String[0]));
            TailwindFXMetrics.instance().recordApply(cssClasses.size());
        }
        
        // Apply JIT tokens as inline styles
        if (!jitTokens.isEmpty()) {
            StyleMerger.applyJit(node, jitTokens.toArray(new String[0]));
        }
    }

    /**
     * Detects if a token should be compiled as JIT.
     * Returns true for tokens with: opacity (/), arbitrary values ([]), or negatives (-)
     */
    private static boolean isJitToken(String token) {
        // Fast path checks
        if (token.contains("/")) return true;  // opacity: bg-blue-500/80
        if (token.contains("[")) return true;  // arbitrary: w-[320px]
        if (token.startsWith("-")) return true; // negative: -translate-x-4
        
        // Check for numeric patterns that indicate JIT (e.g., custom scale values)
        // This catches edge cases like "rotate-45" vs "rotate" (CSS class)
        return token.matches(".*\\d+.*") && 
               (token.contains("-") && Character.isDigit(token.charAt(token.lastIndexOf("-") + 1)));
    }

    /**
     * Applies utility classes only if they differ from the last applied state
     * (StyleDiff). Skips the resolver entirely on a cache hit.
     *
     * <p><b>Note:</b> This method works with both CSS classes and JIT tokens.
     * Auto-detection is applied just like {@link #apply(Node, String...)}.
     *
     * <pre>
     * TailwindFX.applyDiff(button, "btn-primary", "rounded-lg");
     * TailwindFX.applyDiff(button, "btn-primary", "rounded-lg"); // no-op (cache hit)
     * 
     * // Works with JIT too
     * TailwindFX.applyDiff(card, "shadow-md", "bg-blue-500/80"); // applied
     * TailwindFX.applyDiff(card, "shadow-md", "bg-blue-500/80"); // skipped
     * </pre>
     *
     * @param node    the node to style
     * @param tokens  utility classes or JIT tokens to apply
     * @return {@code true} if styles were applied, {@code false} if skipped (no change)
     */
    public static boolean applyDiff(Node node, String... tokens) {
        return StylePerf.apply(node, tokens);
    }

    /**
     * Applies utility classes WITHOUT conflict resolution.
     * Use when you want to accumulate classes deliberately.
     * 
     * <p><b>Warning:</b> This is a low-level method. Most users should use
     * {@link #apply(Node, String...)} which handles conflicts automatically.
     * 
     * <p>This method only works with CSS classes, not JIT tokens.
     * 
     * @param node    the node to style
     * @param classes CSS class names (JIT tokens will be ignored)
     */
    public static void applyRaw(Node node, String... classes) {
        for (String c : classes) {
            if (c == null || c.isBlank()) continue;
            for (String part : c.split("\\s+")) {
                if (!part.isBlank() && !node.getStyleClass().contains(part)) {
                    node.getStyleClass().add(part);
                }
            }
        }
    }

    public static void remove(Node node, String... classes) {
        node.getStyleClass().removeAll(Arrays.asList(classes));
    }

    public static void replace(Node node, String... classes) {
        node.getStyleClass().setAll(Arrays.asList(classes));
    }

    public static void toggle(Node node, String cssClass) {
        if (node.getStyleClass().contains(cssClass)) {
            node.getStyleClass().remove(cssClass);
        } else {
            node.getStyleClass().add(cssClass);
        }
    }

    // =========================================================================
    // Legacy JIT methods — Now deprecated, use apply() instead
    // =========================================================================

    /**
     * @deprecated Use {@link #apply(Node, String...)} instead.
     * The unified apply() method now auto-detects JIT tokens.
     * 
     * <p>This method is kept for backward compatibility but will be removed in v5.0.
     * 
     * <pre>
     * // Old way:
     * TailwindFX.jit(node, "bg-blue-500/80", "p-[13px]");
     * 
     * // New way (recommended):
     * TailwindFX.apply(node, "bg-blue-500/80", "p-[13px]");
     * </pre>
     */
    @Deprecated(since = "4.4.0", forRemoval = true)
    public static void jit(Node node, String... tokens) {
        apply(node, tokens);
    }

    /**
     * @deprecated Use {@link #apply(Node, String...)} instead.
     * The unified apply() method now auto-detects JIT tokens.
     * 
     * <p>This method is kept for backward compatibility but will be removed in v5.0.
     * 
     * <pre>
     * // Old way:
     * TailwindFX.jitApply(node, "btn-primary", "bg-blue-500/80", "rounded-lg");
     * 
     * // New way (recommended):
     * TailwindFX.apply(node, "btn-primary", "bg-blue-500/80", "rounded-lg");
     * </pre>
     */
    @Deprecated(since = "4.4.0", forRemoval = true)
    public static void jitApply(Node node, String... tokens) {
        apply(node, tokens);
    }

    /**
     * Elimina tokens JIT previamente aplicados como inline style.
     */
    public static void jitRemove(Node node, String... tokens) {
        StyleMerger.removeJit(node, tokens);
    }

    /**
     * Compila un token y devuelve su inline style string (sin aplicar a ningún nodo).
     * Útil para debugging.
     *
     *   TailwindFX.compile("bg-blue-500/80") → "-fx-background-color: rgba(59,130,246,0.80);"
     *   TailwindFX.compile("p-4")            → "-fx-padding: 16px;"
     *   TailwindFX.compile("w-[320px]")      → "-fx-pref-width: 320px;"
     */
    public static String compile(String token) {
        JitCompiler.CompileResult result = JitCompiler.compile(token);
        return result.hasInlineStyle() ? result.inlineStyle() : "";
    }

    /** Tamaño actual del cache JIT */
    public static int jitCacheSize() {
        return JitCompiler.cacheSize();
    }

    // =========================================================================
    // Theme Scopes — Theme by subtree
    // =========================================================================

    /**
     * Applies a theme to a subtree (Pane and its children), without affecting the rest of the Scene.
     *
     *   TailwindFX.scope(alertPanel).preset("rose").apply();
     *   TailwindFX.scope(adminPane).dark().apply();
     *   TailwindFX.scope(drawer).accent("#8b5cf6").focus("#a78bfa").apply();
     *   TailwindFX.clearScope(drawer);
     */
    public static ThemeScopeManager.ScopeBuilder scope(javafx.scene.layout.Pane pane) {
        return ThemeScopeManager.scope(pane);
    }

    public static void clearScope(javafx.scene.layout.Pane pane) {
        ThemeScopeManager.clearScope(pane);
    }

    /**
     * Copies the theme scope that {@code node} inherits to {@code target}.
     * Walks up the scene graph to find the nearest scoped ancestor.
     * Useful for modals/popovers that should match the theme of their trigger.
     *
     * @param node   the node whose inherited scope to copy
     * @param target the Pane to apply the scope to
     */
    public static void inheritScope(javafx.scene.Node node, javafx.scene.layout.Pane target) {
        ThemeScopeManager.inheritScope(node, target);
    }

    /**
     * Reapplies a scope after a Pane is moved to a different parent or Scene.
     * JavaFX does not fire CSS events on reparenting — call this to force a refresh.
     *
     * @param pane the Pane to refresh
     */
    public static void refreshScope(javafx.scene.layout.Pane pane) {
        ThemeScopeManager.refreshScope(pane);
    }

    /**
     * Finds the nearest scoped ancestor Pane in the scene graph.
     *
     * @param node the starting node
     * @return the nearest scoped ancestor, or {@code null} if none
     */
    public static javafx.scene.layout.Pane findClosestScope(javafx.scene.Node node) {
        return ThemeScopeManager.findClosestScope(node);
    }

    // =========================================================================
    // Animations
    // =========================================================================

    /**
     * Enter/exit/attention animations for nodes.
     * Delegates to AnimationUtil — can also be used directly.
     *
     *   TailwindFX.fadeIn(node).play();
     *   TailwindFX.fadeIn(node, 300).play();
     *   TailwindFX.shake(node).play();
     *   TailwindFX.onHoverScale(button, 1.05);
     *   AnimationUtil.chain(TailwindFX.fadeIn(node), TailwindFX.slideUp(node)).play();
     */
    public static AnimationUtil.FxAnimation fadeIn(javafx.scene.Node n)              { return AnimationUtil.fadeIn(n); }
    public static AnimationUtil.FxAnimation fadeIn(javafx.scene.Node n, int ms)      { return AnimationUtil.fadeIn(n, ms); }
    public static AnimationUtil.FxAnimation fadeOut(javafx.scene.Node n)             { return AnimationUtil.fadeOut(n); }
    public static AnimationUtil.FxAnimation fadeOut(javafx.scene.Node n, int ms)     { return AnimationUtil.fadeOut(n, ms); }
    public static AnimationUtil.FxAnimation slideUp(javafx.scene.Node n)             { return AnimationUtil.slideUp(n); }
    public static AnimationUtil.FxAnimation slideDown(javafx.scene.Node n)           { return AnimationUtil.slideDown(n); }
    public static AnimationUtil.FxAnimation slideLeft(javafx.scene.Node n)           { return AnimationUtil.slideLeft(n); }
    public static AnimationUtil.FxAnimation slideRight(javafx.scene.Node n)          { return AnimationUtil.slideRight(n); }
    public static AnimationUtil.FxAnimation scaleIn(javafx.scene.Node n)             { return AnimationUtil.scaleIn(n); }
    public static AnimationUtil.FxAnimation scaleOut(javafx.scene.Node n)            { return AnimationUtil.scaleOut(n); }
    public static AnimationUtil.FxAnimation pulse(javafx.scene.Node n)               { return AnimationUtil.pulse(n); }
    public static AnimationUtil.FxAnimation shake(javafx.scene.Node n)               { return AnimationUtil.shake(n); }
    public static AnimationUtil.FxAnimation bounce(javafx.scene.Node n)              { return AnimationUtil.bounce(n); }
    public static AnimationUtil.FxAnimation flash(javafx.scene.Node n)               { return AnimationUtil.flash(n); }
    public static AnimationUtil.FxAnimation spin(javafx.scene.Node n)                { return AnimationUtil.spin(n); }
    public static AnimationUtil.FxAnimation breathe(javafx.scene.Node n)             { return AnimationUtil.breathe(n); }
    public static void onHoverScale(javafx.scene.Node n, double f)    { AnimationUtil.onHoverScale(n, f); }
    public static void onHoverLift(javafx.scene.Node n)                { AnimationUtil.onHoverLift(n); }
    public static void onHoverDim(javafx.scene.Node n, double opacity) { AnimationUtil.onHoverDim(n, opacity); }
    /**
     * Removes all hover effects (scale, lift, dim) previously installed on a node.
     * Call before re-installing effects to avoid stacking.
     */
    public static void removeHoverEffects(javafx.scene.Node n) { AnimationUtil.removeHoverEffects(n); }

    /** Cancela todas las animaciones activas en un nodo */
    public static void cancelAnimations(javafx.scene.Node n) {
        AnimationUtil.AnimationRegistry.cancelAll(n);
    }

    /**
     * Resetea un nodo a su estado visual base (translate=0, scale=1, opacity=1, rotate=0).
     * Útil antes de aplicar un nuevo layout responsive.
     */
    public static void resetNode(javafx.scene.Node n) {
        AnimationUtil.ResponsiveAnimationGuard.resetNode(n);
    }

    /**
     * Integra el guard de animaciones con el responsive engine.
     * Llamar después de TailwindFX.responsive(stage) para proteger
     * animaciones de translate/scale durante cambios de breakpoint.
     *
     * Ejemplo:
     *   BreakpointManager bpm = TailwindFX.responsive(stage);
     *   TailwindFX.installAnimationGuard(bpm, scene);
     *   // Ahora los breakpoint changes pausan/reanudan animaciones automáticamente
     */
    public static void installAnimationGuard(BreakpointManager bpm, javafx.scene.Scene scene) {
        // Hook en cada breakpoint: pause → reconfigure → resume
        for (BreakpointManager.Breakpoint bp : new BreakpointManager.Breakpoint[]{
            BreakpointManager.BP.SM, BreakpointManager.BP.MD,
            BreakpointManager.BP.LG, BreakpointManager.BP.XL
        }) {
            bpm.onBreakpoint(bp, () -> {
                AnimationUtil.ResponsiveAnimationGuard.onLayoutChangeStart(scene);
                // El reconfigure del layout ocurre en el listener del usuario
                // Este guard solo maneja el ciclo pause/resume de animaciones
                javafx.application.Platform.runLater(() ->
                    AnimationUtil.ResponsiveAnimationGuard.onLayoutChangeEnd(scene));
            });
        }
    }

    /**
     * Activa el modo debug del JIT.
     * Con debug=true: loguea TODOS los tokens procesados (útil en desarrollo).
     * With debug=false (default): only warns on unrecognized JIT tokens.
     *
     *   TailwindFX.jitDebug(true);
     *   TailwindFX.jit(node, "p-4", "btn-primary", "bg-bleu-500");  // "bg-bleu-500" → warn
     *   TailwindFX.jitDebug(false);
     */
    public static void jitDebug(boolean enabled) {
        JitCompiler.setDebug(enabled);
    }

    /**
     * Compila múltiples tokens y devuelve el inline style combinado.
     * Equivale a llamar compile() en cada token y concatenar el resultado.
     *
     *   TailwindFX.compileAll("p-4", "bg-blue-500/80", "rounded-lg")
     *   // → "-fx-padding: 16px; -fx-background-color: rgba(59,130,246,0.80); ..."
     */
    public static String compileAll(String... tokens) {
        return JitCompiler.compileBatch(tokens).inlineStyle();
    }

    // =========================================================================
    // Layout Builder
    // =========================================================================

    public static FxLayout layout(Pane container) {
        return new FxLayout(container);
    }

    // =========================================================================
    // Responsive Engine
    // =========================================================================

    public static BreakpointManager responsive(Stage stage) {
        return BreakpointManager.attach(stage);
    }

    // =========================================================================
    // Theme Engine
    // =========================================================================

    public static ThemeManager theme(Scene scene) {
        return ThemeManager.forScene(scene);
    }

    // =========================================================================
    // Styles — Java API for what CSS cannot do in JavaFX
    // Delegates to Styles.java, also usable directly.
    // =========================================================================

    /**
     * GridPane: col-span, row-span, cell position.
     *   TailwindFX.colSpan(card, 3)
     *   TailwindFX.rowSpanFull(panel)
     *   TailwindFX.gridCell(node, col, row)
     */
    public static <T extends javafx.scene.Node> T colSpan(T n, int s)   { return Styles.colSpan(n, s); }
    public static <T extends javafx.scene.Node> T colSpanFull(T n)       { return Styles.colSpanFull(n); }
    public static <T extends javafx.scene.Node> T rowSpan(T n, int s)    { return Styles.rowSpan(n, s); }
    public static <T extends javafx.scene.Node> T rowSpanFull(T n)       { return Styles.rowSpanFull(n); }
    public static <T extends javafx.scene.Node> T gridCell(T n, int c, int r) { return Styles.gridCell(n, c, r); }

    /**
     * Grow/shrink: flex-1, grow, flex-none.
     *   TailwindFX.flex1(myNode)
     *   TailwindFX.hboxFillWidth(content)
     */
    public static <T extends javafx.scene.Node> T flex1(T n)          { return Styles.flex1(n); }
    public static <T extends javafx.scene.Node> T grow(T n)           { return Styles.grow(n); }
    public static <T extends javafx.scene.Node> T growNone(T n)       { return Styles.growNone(n); }
    public static <T extends javafx.scene.Node> T vgrow(T n)          { return Styles.vgrow(n); }
    public static <T extends javafx.scene.Node> T hboxFillWidth(T n)  { return Styles.hboxFillWidth(n); }
    public static <T extends javafx.scene.Node> T vboxFillHeight(T n) { return Styles.vboxFillHeight(n); }

    /**
     * Margin: m-*, mx-*, my-*, mt-*, mr-*, mb-*, ml-*  (n × 4px).
     *   TailwindFX.mx(node, 4)   → margin horizontal 16px
     *   TailwindFX.mt(node, 6)   → margin-top 24px
     */
    public static <T extends javafx.scene.Node> T m(T n, int v)                              { return Styles.m(n, v); }
    public static <T extends javafx.scene.Node> T mx(T n, int v)                             { return Styles.mx(n, v); }
    public static <T extends javafx.scene.Node> T my(T n, int v)                             { return Styles.my(n, v); }
    public static <T extends javafx.scene.Node> T mt(T n, int v)                             { return Styles.mt(n, v); }
    public static <T extends javafx.scene.Node> T mr(T n, int v)                             { return Styles.mr(n, v); }
    public static <T extends javafx.scene.Node> T mb(T n, int v)                             { return Styles.mb(n, v); }
    public static <T extends javafx.scene.Node> T ml(T n, int v)                             { return Styles.ml(n, v); }
    public static <T extends javafx.scene.Node> T mxAuto(T n)                                { return Styles.mxAuto(n); }
    public static <T extends javafx.scene.Node> T margin(T n, double t, double r, double b, double l) { return Styles.margin(n, t, r, b, l); }

    /**
     * Z-order: z-0 … z-50, orderFirst, orderLast.
     *   TailwindFX.z(tooltip, 50)
     *   TailwindFX.orderFirst(overlay)
     */
    public static <T extends javafx.scene.Node> T z(T n, int v)     { return Styles.z(n, v); }
    public static <T extends javafx.scene.Node> T z50(T n)          { return Styles.z50(n); }
    public static <T extends javafx.scene.Node> T orderFirst(T n)   { return Styles.orderFirst(n); }
    public static <T extends javafx.scene.Node> T orderLast(T n)    { return Styles.orderLast(n); }

    /**
     * Alignment constraints: self-*, justify-self-*.
     *   TailwindFX.selfCenter(node)
     *   TailwindFX.justifySelfEnd(node)
     */
    public static <T extends javafx.scene.Node> T selfStart(T n)        { return Styles.selfStart(n); }
    public static <T extends javafx.scene.Node> T selfCenter(T n)       { return Styles.selfCenter(n); }
    public static <T extends javafx.scene.Node> T selfEnd(T n)          { return Styles.selfEnd(n); }
    public static <T extends javafx.scene.Node> T justifySelfStart(T n) { return Styles.justifySelfStart(n); }
    public static <T extends javafx.scene.Node> T justifySelfCenter(T n){ return Styles.justifySelfCenter(n); }
    public static <T extends javafx.scene.Node> T justifySelfEnd(T n)   { return Styles.justifySelfEnd(n); }

    /**
     * Filtros CSS (ColorAdjust): grayscale, brightness, contrast, saturate, hueRotate, invert, sepia.
     *   TailwindFX.grayscale(node)
     *   TailwindFX.brightness(node, 0.75)
     *   TailwindFX.saturate(node, 1.5)
     */
    public static <T extends javafx.scene.Node> T grayscale(T n)                { return Styles.grayscale(n); }
    public static <T extends javafx.scene.Node> T brightness(T n, double v)     { return Styles.brightness(n, v); }
    public static <T extends javafx.scene.Node> T contrast(T n, double v)       { return Styles.contrast(n, v); }
    public static <T extends javafx.scene.Node> T saturate(T n, double v)       { return Styles.saturate(n, v); }
    public static <T extends javafx.scene.Node> T hueRotate(T n, double deg)    { return Styles.hueRotate(n, deg); }
    public static <T extends javafx.scene.Node> T invert(T n)                   { return Styles.invert(n); }
    public static <T extends javafx.scene.Node> T sepia(T n)                    { return Styles.sepia(n); }
    public static <T extends javafx.scene.Node> T filterNone(T n)               { return Styles.filterNone(n); }

    /**
     * Skew (Shear transform): skewX, skewY.
     *   TailwindFX.skewX(node, 6)
     *   TailwindFX.skewY(node, 3)
     */
    public static <T extends javafx.scene.Node> T skewX(T n, double deg) { return Styles.skewX(n, deg); }
    public static <T extends javafx.scene.Node> T skewY(T n, double deg) { return Styles.skewY(n, deg); }

    /**
     * ImageView: object-fit, object-position.
     *   TailwindFX.objectCover(imageView)
     *   TailwindFX.objectCenter(imageView)
     *   TailwindFX.imgSize(iv, 200, 150, true)
     */
    public static javafx.scene.image.ImageView objectCover(javafx.scene.image.ImageView iv)   { return Styles.objectCover(iv); }
    public static javafx.scene.image.ImageView objectContain(javafx.scene.image.ImageView iv) { return Styles.objectContain(iv); }
    public static javafx.scene.image.ImageView objectCenter(javafx.scene.image.ImageView iv)  { return Styles.objectCenter(iv); }
    public static javafx.scene.image.ImageView imgSize(javafx.scene.image.ImageView iv, double w, double h, boolean r) { return Styles.imgSize(iv, w, h, r); }

    /**
     * Visibilidad: invisible (oculta pero ocupa espacio), hiddenNode (oculta y elimina del layout).
     *   TailwindFX.hiddenNode(node)   // equivale a .hidden-node
     *   TailwindFX.show(node)         // restaura
     */
    public static <T extends javafx.scene.Node> T invisible(T n)  { return Styles.invisible(n); }
    public static <T extends javafx.scene.Node> T hiddenNode(T n) { return Styles.hiddenNode(n); }
    public static <T extends javafx.scene.Node> T show(T n)       { return Styles.show(n); }

    // =========================================================================
    // CONFIGURACIÓN GLOBAL
    // =========================================================================

    /**
     * Configuración global de TailwindFX. Fluent builder, persiste en la sesión.
     *
     * <pre>
     * TailwindFX.configure()
     *     .unit(4.0)
     *     .breakpoint("tablet", 700)
     *     .debug(true);
     * </pre>
     */
    public static final class Config {

        private double  unit         = 4.0;
        private boolean debugMode    = false;
        private boolean warnNoParent = true;
        private final Map<String, Integer> customBreakpoints = new LinkedHashMap<>();

        private Config() {}

        /** Cambia la unidad base de espaciado (default 4px). Invalida el cache JIT. */
        public Config unit(double px) {
            if (px <= 0) throw new IllegalArgumentException(
                "Config.unit: must be > 0, got: " + px);
            this.unit = px;
            JitCompiler.clearCache();
            return this;
        }

        /** Registra un breakpoint personalizado (nombre, ancho en px). */
        public Config breakpoint(String name, int px) {
            Preconditions.requireNonBlank(name, "Config.breakpoint", "name");
            if (px < 0) throw new IllegalArgumentException(
                "Config.breakpoint: px must be >= 0, got: " + px);
            customBreakpoints.put(name, px);
            return this;
        }

        /** Activa/desactiva el modo debug global (JIT verbose + logs de conflictos). */
        public Config debug(boolean enabled) {
            this.debugMode = enabled;
            JitCompiler.setDebug(enabled);
            return this;
        }

        /** If false, silences "node has no parent" warnings from Styles.margin methods. */
        public Config warnOnNoParent(boolean enabled) {
            this.warnNoParent = enabled;
            return this;
        }

        /**
         * Enables automatic batch mode when many nodes are updated in one frame.
         *
         * <pre>
         * TailwindFX.configure().autoBatch(20); // auto-batch > 20 apply calls/frame
         * TailwindFX.configure().autoBatch(0);  // disabled (default)
         * </pre>
         *
         * <p>Prefer explicit {@link TailwindFX#batch} for predictable performance.
         * Auto-batch is useful for integrating TailwindFX into existing codebases
         * where wrapping code in batch() is not practical.
         *
         * @param threshold minimum apply-calls per frame to trigger auto-batch (0 = off)
         */
        public Config autoBatch(int threshold) {
            StylePerf.setAutoBatchThreshold(threshold);
            return this;
        }

        /** Returns the current auto-batch threshold (0 = disabled). */
        public int getAutoBatchThreshold() { return StylePerf.getAutoBatchThreshold(); }

        public double unit()                            { return unit; }
        public boolean isDebug()                        { return debugMode; }
        public boolean isWarnNoParent()                 { return warnNoParent; }
        public Map<String, Integer> customBreakpoints() {
            return Collections.unmodifiableMap(customBreakpoints);
        }
    }

    private static final Config GLOBAL_CONFIG = new Config();

    /** Acceso a la configuración global. */
    public static Config configure() { return GLOBAL_CONFIG; }

    /** Unidad base actual en px (default 4.0). */
    public static double unit() { return GLOBAL_CONFIG.unit(); }

    /**
     * Activa o desactiva el modo debug global.
     * Equivale a {@code TailwindFX.configure().debug(enabled)}.
     */
    public static void setDebug(boolean enabled) { GLOBAL_CONFIG.debug(enabled); }

    /**
     * Access to runtime metrics (cache hit ratio, compilation counts, etc.).
     *
     * <pre>
     * TailwindFXMetrics m = TailwindFX.metrics();
     * m.setEnabled(true);
     * // ... run your app ...
     * m.print();
     * </pre>
     *
     * @return the singleton {@link TailwindFXMetrics} instance
     */
    public static TailwindFXMetrics metrics() { return TailwindFXMetrics.instance(); }

    // =========================================================================
    // HOT RELOAD — recarga CSS sin reiniciar (solo desarrollo)
    // =========================================================================

    /**
     * Observa un archivo CSS y lo recarga automáticamente al detectar cambios.
     * <b>Solo para desarrollo.</b>
     *
     * <pre>
     * TailwindFX.install(scene);
     * TailwindFX.watch("/ruta/absoluta/tailwindfx.css", scene);
     * </pre>
     *
     * <p>Crea un hilo daemon que usa {@code WatchService} del sistema de archivos.
     * La recarga ocurre en el JavaFX Application Thread via {@code Platform.runLater}.
     *
     * @param cssPath ruta absoluta al archivo CSS
     * @param scene   Scene a recargar
     */
    public static void watch(String cssPath, Scene scene) {
        Preconditions.requireNonBlank(cssPath, "TailwindFX.watch", "cssPath");
        Preconditions.requireNonNull(scene, "TailwindFX.watch", "scene");

        Thread wt = new Thread(() -> {
            try {
                Path path = Paths.get(cssPath).toAbsolutePath().normalize();
                if (!Files.exists(path)) {
                    Preconditions.LOG.warning("TailwindFX.watch: file not found: " + path);
                    return;
                }
                WatchService ws = FileSystems.getDefault().newWatchService();
                path.getParent().register(ws, StandardWatchEventKinds.ENTRY_MODIFY);
                Preconditions.LOG.info("TailwindFX: watching " + path.getFileName());

                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key;
                    try { key = ws.take(); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.context().toString().equals(path.getFileName().toString())) {
                            String url = path.toUri() + "?t=" + System.currentTimeMillis();
                            Platform.runLater(() -> {
                                scene.getStylesheets().removeIf(
                                    s -> s.contains(path.getFileName().toString()));
                                scene.getStylesheets().add(url);
                                JitCompiler.clearCache();
                                Preconditions.LOG.info("TailwindFX: CSS reloaded — "
                                    + path.getFileName());
                            });
                        }
                    }
                    if (!key.reset()) break;
                }
                ws.close();
            } catch (Exception e) {
                Preconditions.LOG.warning("TailwindFX.watch: error — " + e.getMessage());
            }
        }, "TailwindFX-FileWatcher");
        wt.setDaemon(true);
        wt.start();
    }

    // =========================================================================
    // DEBUG API — inspección de estado en tiempo de ejecución
    // =========================================================================

    /**
     * Genera un reporte de estado para un nodo.
     *
     * <pre>
     * System.out.println(TailwindFX.debugReport(myButton));
     *
     * // Salida ejemplo:
     * // Nodo:          Button
     * // Style classes: [btn-primary, rounded-lg]
     * // Inline style:  -fx-background-color: rgba(59,130,246,0.80);
     * // JIT cache:     42 entradas
     * // Categorias:    {w=w-12, p=p-4, shadow=shadow-md}
     * // Animaciones:   [enter: RUNNING]
     * </pre>
     */
    public static String debugReport(Node node) {
        Preconditions.requireNode(node, "TailwindFX.debugReport");

        @SuppressWarnings("unchecked")
        Map<String, String> cats = (Map<String, String>) node.getProperties()
            .getOrDefault("tailwindfx.category.cache", Map.of());

        @SuppressWarnings("unchecked")
        Map<String, javafx.animation.Animation> anims =
            (Map<String, javafx.animation.Animation>) node.getProperties()
                .getOrDefault("tailwindfx.animations", Map.of());

        String animStr = anims.isEmpty() ? "ninguna"
            : anims.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue().getStatus())
                .reduce((a, b) -> a + ", ").orElse("ninguna");

        return String.format(
            "Nodo:          %s%n" +
            "Style classes: %s%n" +
            "Inline style:  %s%n" +
            "JIT cache:     %d entradas%n" +
            "Categorias:    %s%n" +
            "Animaciones:   [%s]",
            node.getClass().getSimpleName(),
            node.getStyleClass(),
            node.getStyle() != null && !node.getStyle().isBlank()
                ? node.getStyle() : "(none)",
            JitCompiler.cacheSize(),
            cats.isEmpty() ? "(vacio)" : cats,
            animStr
        );
    }

    /** Imprime el reporte de estado a stdout. Atajo de {@code System.out.println(debugReport(node))}. */
    public static void debugPrint(Node node) { System.out.println(debugReport(node)); }

    // =========================================================================
    // PERSISTENCIA DE TEMAS — atajos a ThemeManager
    // =========================================================================

    /**
     * Guarda el tema actual en las preferencias del sistema operativo.
     *
     * <pre>
     * TailwindFX.saveTheme(scene, "myapp.theme");
     * // Siguiente arranque:
     * TailwindFX.loadTheme(scene, "myapp.theme");
     * </pre>
     */
    public static void saveTheme(Scene scene, String key)    { ThemeManager.saveTheme(scene, key); }

    /**
     * Restaura un tema guardado. Devuelve {@code true} si se encontró la clave.
     * Si no existe, la Scene no se modifica.
     */
    public static boolean loadTheme(Scene scene, String key) { return ThemeManager.loadTheme(scene, key); }

    /** Elimina un tema guardado de las preferencias del sistema. */
    public static void deleteTheme(String key) { ThemeManager.deleteTheme(key); }

    // =========================================================================
    // RESPONSIVE NODE — per-node responsive utility rules
    // =========================================================================

    /**
     * Creates a responsive rule builder for a node. Rules are applied as the
     * scene width changes, using standard Tailwind breakpoints.
     *
     * <pre>
     * TailwindFX.responsive(sidebar)
     *     .base("flex-col", "w-64")
     *     .sm("w-full")
     *     .md("w-48")
     *     .install(scene);
     * </pre>
     *
     * @param node the node to attach responsive rules to
     * @return a {@link ResponsiveNode.Builder} for configuring breakpoint rules
     */
    public static ResponsiveNode.Builder responsive(Node node) {
        return ResponsiveNode.on(node);
    }

    // =========================================================================
    // FXFLEXPANE — Flexbox layout container
    // =========================================================================

    /**
     * Creates a row-direction {@link FxFlexPane} — JavaFX's missing flex container.
     *
     * <pre>
     * FxFlexPane row = TailwindFX.flexRow()
     *     .wrap(true)
     *     .justify(FxFlexPane.Justify.BETWEEN)
     *     .align(FxFlexPane.Align.CENTER)
     *     .gap(16);
     * TailwindFX.apply(row, "p-4", "bg-white", "rounded-lg");
     * </pre>
     *
     * @return a new row-direction FxFlexPane
     */
    public static FxFlexPane flexRow() { return FxFlexPane.row(); }

    /**
     * Creates a column-direction {@link FxFlexPane}.
     *
     * @return a new column-direction FxFlexPane
     */
    public static FxFlexPane flexCol() { return FxFlexPane.col(); }

    // =========================================================================
    // FXGRIDPANE — declarative grid with auto-flow, areas, masonry
    // =========================================================================

    /**
     * Returns a builder for {@link FxGridPane} — JavaFX's missing declarative grid.
     *
     * <pre>
     * // 3-column auto-flow:
     * FxGridPane grid = TailwindFX.grid().cols(3).gap(16).build();
     *
     * // Template areas:
     * FxGridPane page = TailwindFX.grid()
     *     .areas("header header", "sidebar main", "footer footer")
     *     .gap(8).build();
     * page.placeIn(header, "header");
     *
     * // Masonry:
     * FxGridPane pins = TailwindFX.grid().masonry(3).gap(12).build();
     * </pre>
     *
     * @return a new {@link FxGridPane.Builder}
     */
    public static FxGridPane.Builder grid() { return FxGridPane.create(); }

    // =========================================================================
    // COMPONENT FACTORY — high-level UI components
    // =========================================================================

    /**
     * Entry point for {@link ComponentFactory} — cards, badges, modals, drawers, tables.
     *
     * <pre>
     * VBox card    = TailwindFX.component().card().title("Revenue").build();
     * Label badge  = TailwindFX.component().badge("NEW", "blue");
     * StackPane modal = TailwindFX.component().modal(content).show(root);
     * </pre>
     *
     * @return the {@link ComponentFactory} singleton accessor
     */
    public static ComponentFactory component() { return COMPONENT_FACTORY; }
    private static final ComponentFactory COMPONENT_FACTORY = new ComponentFactory();

    // Convenience direct-access shortcuts (avoid needing to call component() first)

    /** Creates a styled card builder. @see ComponentFactory.CardBuilder */
    public static ComponentFactory.CardBuilder card()             { return ComponentFactory.card(); }
    /** Creates a badge label. @see ComponentFactory#badge */
    public static Label  badge(String text, String color)         { return ComponentFactory.badge(text, color); }
    /** Creates a pill/chip label. @see ComponentFactory#pill */
    public static Label  pill(String text, String color)          { return ComponentFactory.pill(text, color); }
    /** Installs a dark tooltip. @see ComponentFactory#tooltip */
    public static Tooltip tooltip(Node node, String text)         { return ComponentFactory.tooltip(node, text); }
    /** Creates a glass-morphism builder. @see ComponentFactory.GlassBuilder */
    public static ComponentFactory.GlassBuilder glass()           { return ComponentFactory.glass(); }
    /** Applies neumorphic styling. @see ComponentFactory.NeumorphicBuilder */
    public static ComponentFactory.NeumorphicBuilder neumorphic(javafx.scene.layout.Region n) { return ComponentFactory.neumorphic(n); }
    /** Creates a modal builder. @see ComponentFactory.ModalBuilder */
    public static ComponentFactory.ModalBuilder modal(Node content){ return ComponentFactory.modal(content); }
    /** Creates a drawer builder. @see ComponentFactory.DrawerBuilder */
    public static ComponentFactory.DrawerBuilder drawer(ComponentFactory.DrawerSide side, double size) { return ComponentFactory.drawer(side, size); }
    /** Creates a data table builder. @see ComponentFactory.DataTableBuilder */
    public static <T> ComponentFactory.DataTableBuilder<T> dataTable(Class<T> type) { return ComponentFactory.dataTable(type); }
    /** Creates a circular avatar with initials. @see ComponentFactory#avatar */
    public static StackPane avatar(String initials, String color, double size) { return ComponentFactory.avatar(initials, color, size); }
    /** Creates a styled alert bar. @see ComponentFactory#alert */
    public static HBox alert(String message, String type)         { return ComponentFactory.alert(message, type); }

    /**
     * Sets the flex-basis (initial main-axis size) for a child of a {@link FxFlexPane}.
     *
     * @param node  the child node
     * @param basis base size in px, or {@code -1} for auto (natural pref size)
     * @see FxFlexPane#setBasis(Node, double)
     */
    public static void flexBasis(javafx.scene.Node node, double basis) {
        FxFlexPane.setBasis(node, basis);
    }

    /**
     * Changes a {@link FxFlexPane}'s direction with an animated fade transition.
     *
     * <pre>
     * // Animate direction change on breakpoint:
     * bpm.onBreakpoint(BP.MD, () ->
     *     TailwindFX.flexDirection(container, FxFlexPane.Direction.COL, 150));
     * </pre>
     *
     * @param pane       the FxFlexPane
     * @param d          the new direction
     * @param durationMs fade duration in milliseconds (0 = instant)
     */
    public static void flexDirection(FxFlexPane pane, FxFlexPane.Direction d, int durationMs) {
        Preconditions.requireNonNull(pane, "TailwindFX.flexDirection", "pane");
        pane.setDirectionAnimated(d, durationMs);
    }

    // =========================================================================
    // PERFORMANCE — batch + style diffing
    // =========================================================================

    /**
     * Executes a block of apply operations in batch mode.
     *
     * <p>All {@link #apply} calls inside {@code work} are deferred and flushed
     * in a single pass, triggering one CSS engine re-evaluation instead of one
     * per node. Ideal for dashboard updates with many nodes.
     *
     * <pre>
     * TailwindFX.batch(() -> {
     *     cards.forEach(c -> TailwindFX.apply(c, "card shadow-md rounded-lg"));
     * });
     * </pre>
     *
     * <p><b>Must be called on the JavaFX Application Thread.</b>
     * For background threads, use {@link #batchAsync(Runnable)}.
     *
     * @param work the block of apply operations to batch (must not be null)
     * @throws IllegalArgumentException if {@code work} is null
     * @throws IllegalStateException    if called from a non-JavaFX Application Thread
     */
    public static void batch(Runnable work) {
        Preconditions.requireNonNull(work, "TailwindFX.batch", "work");
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                "TailwindFX.batch: must be called on the JavaFX Application Thread. "
                + "Use TailwindFX.batchAsync() for background threads.");
        }
        StylePerf.batch(work);
    }

    /**
     * Thread-safe variant: enqueues work on the FX thread and returns immediately.
     *
     * @param work the work to run on the FX thread inside a batch
     */
    public static void batchAsync(Runnable work)  { StylePerf.batchAsync(work); }

    /**
     * Invalidates the StyleDiff cache for a node.
     * Call after externally modifying a node's style classes.
     *
     * @param node the node whose diff cache to invalidate
     */
    public static void invalidateDiff(Node node)  { StylePerf.invalidate(node); }

    // =========================================================================
    // CACHE CLEANUP
    // =========================================================================

    /**
     * Removes all TailwindFX metadata from a node and stops any active animations.
     *
     * <p>Call when permanently removing a node to release all framework resources.
     * Not needed if the node has no other strong references — its properties
     * map is collected automatically by GC.
     *
     * <pre>
     * parent.getChildren().remove(card);
     * TailwindFX.cleanupNode(card);  // break Timeline → node reference chain
     * </pre>
     *
     * @param node the node to clean up (null-safe)
     */
    public static void cleanupNode(Node node) { UtilityConflictResolver.cleanupNode(node); }

    /**
     * Installs an automatic cleanup listener that calls {@link #cleanupNode}
     * when the node is removed from the scene graph.
     *
     * <p>Recommended for frequently created/discarded nodes such as virtual list cells,
     * tab content panes, or carousel items:
     *
     * <pre>
     * TailwindFX.apply(cell, "table-row", "p-3");
     * TailwindFX.autoCleanup(cell); // fires cleanupNode when cell leaves scene
     * </pre>
     *
     * @param node the node to auto-cleanup on scene removal
     */
    public static void autoCleanup(Node node) { UtilityConflictResolver.autoCleanup(node); }

    /**
     * Removes only the category cache from a node. Lighter than {@link #cleanupNode};
     * preserves animations and other metadata.
     *
     * @param node the node whose category cache to clear
     */
    public static void invalidateCache(Node node) { UtilityConflictResolver.invalidateCache(node); }

    /**
     * Removes a single category entry from a node's category cache.
     *
     * <p>More surgical than {@link #invalidateCache} — use this after externally
     * modifying one specific utility class without discarding all cached categories:
     *
     * <pre>
     * node.getStyleClass().remove("w-4");                 // external change
     * TailwindFX.invalidateCategoryCache(node, "w");      // sync cache for "w" only
     * TailwindFX.apply(node, "w-8");                      // resolver works correctly
     * </pre>
     *
     * @param node     the node whose cache to partially invalidate (null-safe)
     * @param category the conflict category to remove (e.g. {@code "w"}, {@code "p"}, {@code "shadow"})
     */
    public static void invalidateCategoryCache(Node node, String category) {
        UtilityConflictResolver.invalidateCategoryCache(node, category);
    }

    // =========================================================================
    // TAILWIND v4.1 — TEXT-SHADOW
    // =========================================================================

    /** Applies a text/element shadow. @see Styles#textShadow */
    public static void textShadowSm(Node n)   { Styles.textShadowSm(n); }
    public static void textShadowMd(Node n)   { Styles.textShadowMd(n); }
    public static void textShadowLg(Node n)   { Styles.textShadowLg(n); }
    public static void textShadowXl(Node n)   { Styles.textShadowXl(n); }
    public static void textShadowNone(Node n) { Styles.textShadowNone(n); }

    /**
     * Colored text shadow (analogous to Tailwind v4.1 {@code text-shadow-[color]}).
     * @param node the Label or Text node
     * @param color hex or rgba color string
     * @param radius blur radius in px
     * @param offsetX horizontal offset
     * @param offsetY vertical offset
     */
    public static void textShadow(Node node, String color,
                                   double radius, double offsetX, double offsetY) {
        Styles.textShadow(node, color, radius, offsetX, offsetY);
    }

    // =========================================================================
    // TAILWIND v4.1 — COLORED DROP-SHADOW
    // =========================================================================

    /**
     * Colored drop shadow (analogous to Tailwind v4.1 {@code drop-shadow-[color]}).
     * @param node    the node to shadow
     * @param hexColor hex color (e.g. {@code "#3b82f6"})
     * @param alpha   alpha multiplier [0.0, 1.0]
     * @param radius  blur radius
     * @param offsetX horizontal offset
     * @param offsetY vertical offset
     */
    public static void dropShadow(Node node, String hexColor, double alpha,
                                   double radius, double offsetX, double offsetY) {
        Styles.dropShadow(node, hexColor, alpha, radius, offsetX, offsetY);
    }

    /** {@code drop-shadow-blue} preset. */
    public static void dropShadowBlue(Node n)   { Styles.dropShadowBlue(n); }
    /** {@code drop-shadow-green} preset. */
    public static void dropShadowGreen(Node n)  { Styles.dropShadowGreen(n); }
    /** {@code drop-shadow-red} preset. */
    public static void dropShadowRed(Node n)    { Styles.dropShadowRed(n); }
    /** {@code drop-shadow-purple} preset. */
    public static void dropShadowPurple(Node n) { Styles.dropShadowPurple(n); }

    // =========================================================================
    // MASK / CLIP
    // =========================================================================

    /**
     * Clips a node to a circle (analogous to CSS {@code border-radius: 50%} + {@code overflow: hidden}).
     * The clip updates automatically when the node is resized.
     * @param node the node to clip
     */
    public static void clipCircle(Node node) { Styles.clipCircle(node); }

    /**
     * Clips a node to a rounded rectangle.
     * @param node   the node to clip
     * @param radius corner radius in px
     */
    public static void clipRounded(Node node, double radius) { Styles.clipRounded(node, radius); }

    /**
     * Clips a node to an arbitrary shape.
     * @param node  the node to clip
     * @param shape the clipping shape
     */
    public static void clipMask(Node node, javafx.scene.shape.Shape shape) {
        Styles.clipMask(node, shape);
    }

    /** Removes any clip/mask from the node. */
    public static void clipNone(Node node) { Styles.clipNone(node); }

    // =========================================================================
    // SVG
    // =========================================================================

    /**
     * Sets an SVG shape's fill color.
     * @param shape the shape
     * @param hex   hex color string
     */
    public static void fill(javafx.scene.shape.Shape shape, String hex) {
        Styles.fill(shape, hex);
    }

    /**
     * Sets an SVG shape's stroke color.
     * @param shape the shape
     * @param hex   hex color string
     */
    public static void stroke(javafx.scene.shape.Shape shape, String hex) {
        Styles.stroke(shape, hex);
    }

    /** Sets an SVG shape's stroke width. */
    public static void strokeWidth(javafx.scene.shape.Shape shape, double width) {
        Styles.strokeWidth(shape, width);
    }

    // =========================================================================
    // 3D TRANSFORMS
    // =========================================================================

    /** Rotates a node around the X axis (analogous to CSS {@code rotateX(deg)}). */
    public static void rotateX(Node n, double deg)  { Styles.rotateX(n, deg); }

    /** Rotates a node around the Y axis (analogous to CSS {@code rotateY(deg)}). */
    public static void rotateY(Node n, double deg)  { Styles.rotateY(n, deg); }

    /** Translates a node along the Z axis (analogous to CSS {@code translateZ(px)}). */
    public static void translateZ(Node n, double px) { Styles.translateZ(n, px); }

    /** Resets all 3D transforms on a node. */
    public static void reset3D(Node n) { Styles.reset3D(n); }

    // =========================================================================
    // MOTION-REDUCE
    // =========================================================================

    /**
     * Sets the reduced-motion preference globally.
     * When {@code true}, use {@link #shouldAnimate()} to skip non-essential animations.
     * @param reduced {@code true} to indicate reduced motion is preferred
     */
    public static void setReducedMotion(boolean reduced) { Styles.setReducedMotion(reduced); }

    /**
     * Returns {@code true} if animations should run (motion is not reduced).
     * @return {@code false} when reduced motion is enabled
     */
    public static boolean shouldAnimate() { return Styles.shouldAnimate(); }

    /**
     * Plays an animation only if motion is not reduced; otherwise applies end state instantly.
     * @param animation the animation to conditionally play
     */
    public static void playIfMotionOk(AnimationUtil.FxAnimation animation) {
        Styles.playIfMotionOk(animation);
    }

    // =========================================================================
    // GLASSMORPHISM / NEUMORPHISM
    // =========================================================================

    /**
     * Applies glassmorphism effect (semi-transparent + blur + border).
     * Combine with {@link #backdropBlur} for full effect.
     * @param node the Region to style
     */
    public static void glass(javafx.scene.layout.Region node) { Styles.glass(node); }

    /** Dark-tinted glass variant for dark backgrounds. */
    public static void glassDark(javafx.scene.layout.Region node) { Styles.glassDark(node); }

    /**
     * Applies neumorphism (soft-UI dual-shadow) effect.
     * Node background should be {@code #e0e5ec}.
     * @param node the Region to style
     */
    public static void neumorph(javafx.scene.layout.Region node) { Styles.neumorph(node); }

    /** Inset neumorphism (pressed state). */
    public static void neumorphInset(javafx.scene.layout.Region node) { Styles.neumorphInset(node); }

    // =========================================================================
    // FXDATATABLE
    // =========================================================================

    // =========================================================================
    // UNSUPPORTED CSS FEATURES — Java API equivalents
    // =========================================================================

    /**
     * Simulates {@code aspect-ratio} by binding prefHeight to prefWidth × ratio.
     *
     * <p>JavaFX CSS has no aspect-ratio property. This method installs a width
     * listener that keeps the height proportional at all times.
     *
     * <pre>
     * TailwindFX.aspectRatio(videoPane, 16, 9);  // 16:9
     * TailwindFX.aspectRatio(avatar, 1, 1);       // square
     * </pre>
     *
     * @param node   the Region to constrain
     * @param width  ratio width  (e.g. {@code 16} for 16:9)
     * @param height ratio height (e.g. {@code 9}  for 16:9)
     */
    public static void aspectRatio(javafx.scene.layout.Region node, double width, double height) {
        Preconditions.requireNonNull(node, "TailwindFX.aspectRatio", "node");
        if (width  <= 0) throw new IllegalArgumentException("TailwindFX.aspectRatio: width must be > 0");
        if (height <= 0) throw new IllegalArgumentException("TailwindFX.aspectRatio: height must be > 0");
        double ratio = height / width;
        node.widthProperty().addListener((obs, ov, nv) ->
            node.setPrefHeight(nv.doubleValue() * ratio));
        if (node.getWidth() > 0) node.setPrefHeight(node.getWidth() * ratio);
    }

    /** Shorthand for {@code aspectRatio(node, 1, 1)} — square container. */
    public static void aspectSquare(javafx.scene.layout.Region node) { aspectRatio(node, 1, 1); }

    /**
     * Simulates {@code backdrop-blur} using {@link javafx.scene.effect.BoxBlur}.
     *
     * <p>Note: JavaFX blurs the node itself, not the content behind it.
     * For a glass-morphism effect, apply this to a semi-transparent overlay pane.
     *
     * <pre>
     * TailwindFX.backdropBlur(glassPanel, 8);   // backdrop-blur
     * TailwindFX.backdropBlurLg(modal);          // backdrop-blur-lg
     * TailwindFX.backdropBlurNone(panel);        // remove
     * </pre>
     *
     * @param node   the node to blur
     * @param radius blur radius in px (0 removes the effect)
     */
    public static void backdropBlur(Node node, double radius) {
        Preconditions.requireNode(node, "TailwindFX.backdropBlur");
        if (radius <= 0) {
            if (node.getEffect() instanceof javafx.scene.effect.BoxBlur)
                node.setEffect(null);
        } else {
            node.setEffect(new javafx.scene.effect.BoxBlur(radius, radius, 3));
        }
    }

    /** {@code backdrop-blur-sm} — 4px blur. */
    public static void backdropBlurSm(Node node)   { backdropBlur(node, 4);  }
    /** {@code backdrop-blur} — 8px blur. */
    public static void backdropBlurMd(Node node)   { backdropBlur(node, 8);  }
    /** {@code backdrop-blur-lg} — 16px blur. */
    public static void backdropBlurLg(Node node)   { backdropBlur(node, 16); }
    /** {@code backdrop-blur-xl} — 24px blur. */
    public static void backdropBlurXl(Node node)   { backdropBlur(node, 24); }
    /** Removes blur effect from node. */
    public static void backdropBlurNone(Node node) { backdropBlur(node, 0);  }

    /**
     * Simulates CSS {@code transition} — animates a property change over time.
     *
     * <p>JavaFX CSS does not support {@code transition}. This method wraps
     * property changes in a {@link javafx.animation.Timeline}:
     *
     * <pre>
     * // transition: opacity 300ms ease
     * TailwindFX.transition(node, 300,
     *     new KeyValue(node.opacityProperty(), 0.5, Interpolator.EASE_BOTH))
     *     .play();
     *
     * // transition: all 200ms — multiple properties
     * TailwindFX.transition(node, 200,
     *     new KeyValue(node.opacityProperty(), 0.8),
     *     new KeyValue(node.scaleXProperty(), 1.05))
     *     .play();
     * </pre>
     *
     * @param node       target node (used for slot registration in AnimationRegistry)
     * @param durationMs transition duration in milliseconds
     * @param values     target {@link javafx.animation.KeyValue}s to animate toward
     * @return a ready-to-play {@link AnimationUtil.FxAnimation}
     */
    public static AnimationUtil.FxAnimation transition(
            Node node, int durationMs, javafx.animation.KeyValue... values) {
        Preconditions.requireNode(node, "TailwindFX.transition");
        Preconditions.requirePositiveDuration(durationMs, "TailwindFX.transition");
        javafx.animation.Timeline tl = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(durationMs), values));
        return new AnimationUtil.FxAnimation(tl).register(node, "transition");
    }

}