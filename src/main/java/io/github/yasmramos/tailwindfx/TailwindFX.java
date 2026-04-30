package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.breakpoint.BreakpointManager;
import io.github.yasmramos.tailwindfx.responsive.ResponsiveNode;
import io.github.yasmramos.tailwindfx.components.FxFlexPane;
import io.github.yasmramos.tailwindfx.components.FxGridPane;
import io.github.yasmramos.tailwindfx.style.StylePerf;
import io.github.yasmramos.tailwindfx.style.Styles;
import io.github.yasmramos.tailwindfx.style.StyleMerger;
import io.github.yasmramos.tailwindfx.animation.FxAnimation;
import io.github.yasmramos.tailwindfx.theme.ThemeManager;
import io.github.yasmramos.tailwindfx.theme.ThemeScopeManager;
import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import io.github.yasmramos.tailwindfx.components.ComponentFactory;
import io.github.yasmramos.tailwindfx.core.UtilityConflictResolver;
import io.github.yasmramos.tailwindfx.core.Preconditions;
import io.github.yasmramos.tailwindfx.core.JitCompiler;
import io.github.yasmramos.tailwindfx.layout.FxLayout;

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
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * TailwindFX — Main entry point.
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
    private TailwindFX() {}

    // Thread-safety for config mutations and JIT cache invalidation
    private static final ReadWriteLock CONFIG_LOCK = new ReentrantReadWriteLock();

    private static final Set<String> JIT_PREFIXES = new HashSet<>(Arrays.asList(
        "bg", "text", "border", "ring", "shadow", "w", "h", "min-w", "min-h", "max-w", "max-h",
        "p", "px", "py", "pt", "pr", "pb", "pl", "m", "mx", "my", "mt", "mr", "mb", "ml", "space",
        "translate", "rotate", "scale", "skew", "opacity", "z", "order", "col", "row", "gap",
        "inset", "top", "right", "bottom", "left", "blur", "brightness", "contrast", "grayscale",
        "hue-rotate", "invert", "saturate", "sepia", "drop-shadow", "backdrop"
    ));

    /**
     * Installs the combined CSS file (all modules).
     */
    public static void install(Scene scene) {
        installAll(scene);
    }

    public static void install(Scene scene, Stage stage) {
        installAll(scene, stage);
    }

    /**
     * Installs only the base module (variables and reset).
     * Required by other modules.
     */
    public static void installBase(Scene scene) {
        installCss(scene, "/tailwindfx/tailwindfx-base.css", 0);
    }

    public static void installComponents(Scene scene) {
        installCss(scene, "/tailwindfx/tailwindfx-components.css", 1);
    }

    public static void installUtilities(Scene scene) {
        installCss(scene, "/tailwindfx/tailwindfx-utilities.css", 2);
    }

    public static void installColors(Scene scene) {
        installCss(scene, "/tailwindfx/tailwindfx-colors.css", 3);
    }

    public static void installEffects(Scene scene) {
        installCss(scene, "/tailwindfx/tailwindfx-effects.css", 4);
    }

    public static void installComponentsPreset(Scene scene) {
        installCss(scene, "/tailwindfx/tailwindfx-components-preset.css", 5);
    }

    public static void installDark(Scene scene) {
        installCss(scene, "/tailwindfx/tailwindfx-dark.css", 10); // High priority to override
    }

    public static void installEssentials(Scene scene) {
        installBase(scene);
        installComponents(scene);
        installComponentsPreset(scene);
    }

    private static void installAll(Scene scene) {
        installBase(scene);
        installComponents(scene);
        installUtilities(scene);
        installColors(scene);
        installEffects(scene);
        installComponentsPreset(scene);
        installDark(scene);
    }

    private static void installAll(Scene scene, Stage stage) {
        installAll(scene);
        responsive(stage);
    }

    private static void installCss(Scene scene, String cssPath, int priority) {
        // ClassLoader.getResource() no acepta "/" inicial; Class.getResource() sí.
        String normalizedPath = cssPath.startsWith("/") ? cssPath.substring(1) : cssPath;
        java.net.URL url = null;

        // 1. Thread Context ClassLoader: resolve recursos en OSGi bundles, Java Modules o classloaders delegados
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if (tccl != null) url = tccl.getResource(normalizedPath);

        // 2. ClassLoader de TailwindFX: fallback para entornos donde el TCCL es el del host/app
        if (url == null) url = TailwindFX.class.getClassLoader().getResource(normalizedPath);

        // 3. Resolución relativa a la clase: captura recursos empaquetados junto al framework
        if (url == null) url = TailwindFX.class.getResource(cssPath);

        String urlStr = Objects.requireNonNull(url,
            cssPath + " not found via TCCL, Framework CL, or class-relative path").toExternalForm();

        var sheets = scene.getStylesheets();
        if (sheets.contains(urlStr)) sheets.remove(urlStr);
    
        // Inserción determinista por prioridad (mantiene cascada CSS estable)
        sheets.add(Math.min(priority, sheets.size()), urlStr);
    }

    /**
     * Applies utility classes and JIT tokens to a node with intelligent auto-detection.
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

    private static void applyInternal(Node node, String... tokens) {
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
     * Detects if a token should be compiled as JIT.
     * Uses strict prefix matching + numeric/arbitrary/negative pattern validation.
     * Eliminates false positives like "card-2" or "panel-v2".
     */
    private static boolean isJitToken(String token) {
        if (token.contains("/")) return true;  // opacity: bg-blue-500/80
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
     * Applies utility classes only if they differ from the last applied state.
     */
    private static boolean applyDiff(Node node, String... tokens) {
        return StylePerf.apply(node, tokens);
    }

    /**
     * Applies utility classes WITHOUT conflict resolution.
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
    // Theme Scopes — Theme by subtree
    // =========================================================================
    public static ThemeScopeManager.ScopeBuilder scope(javafx.scene.layout.Pane pane) {
        return ThemeScopeManager.scope(pane);
    }
 
    public static void clearScope(javafx.scene.layout.Pane pane) {
        ThemeScopeManager.clearScope(pane);
    }

    public static void inheritScope(javafx.scene.Node node, javafx.scene.layout.Pane target) {
        ThemeScopeManager.inheritScope(node, target);
    }

    public static void refreshScope(javafx.scene.layout.Pane pane) {
        ThemeScopeManager.refreshScope(pane);
    }

    public static javafx.scene.layout.Pane findClosestScope(javafx.scene.Node node) {
        return ThemeScopeManager.findClosestScope(node);
    }

    // =========================================================================
    // Animations
    // =========================================================================
    public static FxAnimation fadeIn(javafx.scene.Node n)              { return FxAnimation.fadeIn(n); }
    public static FxAnimation fadeIn(javafx.scene.Node n, int ms)      { return FxAnimation.fadeIn(n, ms); }
    public static FxAnimation fadeOut(javafx.scene.Node n)             { return FxAnimation.fadeOut(n); }
    public static FxAnimation fadeOut(javafx.scene.Node n, int ms)     { return FxAnimation.fadeOut(n, ms); }
    public static FxAnimation slideUp(javafx.scene.Node n)             { return FxAnimation.slideUp(n); }
    public static FxAnimation slideDown(javafx.scene.Node n)           { return FxAnimation.slideDown(n); }
    public static FxAnimation slideLeft(javafx.scene.Node n)           { return FxAnimation.slideLeft(n); }
    public static FxAnimation slideRight(javafx.scene.Node n)          { return FxAnimation.slideRight(n); }
    public static FxAnimation scaleIn(javafx.scene.Node n)             { return FxAnimation.scaleIn(n); }
    public static FxAnimation scaleOut(javafx.scene.Node n)            { return FxAnimation.scaleOut(n); }
    public static FxAnimation pulse(javafx.scene.Node n)               { return FxAnimation.pulse(n); }
    public static FxAnimation shake(javafx.scene.Node n)               { return FxAnimation.shake(n); }
    public static FxAnimation bounce(javafx.scene.Node n)              { return FxAnimation.bounce(n); }
    public static FxAnimation flash(javafx.scene.Node n)               { return FxAnimation.flash(n); }
    public static FxAnimation spin(javafx.scene.Node n)                { return FxAnimation.spin(n); }
    public static FxAnimation breathe(javafx.scene.Node n)             { return FxAnimation.breathe(n); }
    public static void onHoverScale(javafx.scene.Node n, double f)    { FxAnimation.onHoverScale(n, f); }
    public static void onHoverLift(javafx.scene.Node n)                { FxAnimation.onHoverLift(n); }
    public static void onHoverDim(javafx.scene.Node n, double opacity) { FxAnimation.onHoverDim(n, opacity); }
    public static void removeHoverEffects(javafx.scene.Node n) { FxAnimation.removeHoverEffects(n); }
    public static void cancelAnimations(javafx.scene.Node n) {
        FxAnimation.AnimationRegistry.cancelAll(n);
    }
    public static void resetNode(javafx.scene.Node n) {
        FxAnimation.ResponsiveAnimationGuard.resetNode(n);
    }

    /**
     * Instala el guard de animaciones dinámicamente.
     * Ya no hardcodea SM/MD/LG/XL; itera sobre los breakpoints registrados + personalizados.
     */
    public static void installAnimationGuard(BreakpointManager bpm, javafx.scene.Scene scene) {
        CONFIG_LOCK.readLock().lock();
        try {
            var breakpoints = new java.util.ArrayList<>(Arrays.asList(
                BreakpointManager.BP.SM, BreakpointManager.BP.MD,
                BreakpointManager.BP.LG, BreakpointManager.BP.XL
            ));
            // Agregar custom breakpoints al guard
            for (String name : GLOBAL_CONFIG.customBreakpoints().keySet()) {
                try {
                    // Asumimos que BreakpointManager expone un método fromString o similar
                    // Si no, se ignora silenciosamente o se requiere API pública
                } catch (Exception ignored) {}
            }
            CONFIG_LOCK.readLock().unlock();

            for (BreakpointManager.Breakpoint bp : breakpoints) {
                bpm.onBreakpoint(bp, () -> {
                    FxAnimation.ResponsiveAnimationGuard.onLayoutChangeStart(scene);
                    Platform.runLater(() ->
                        FxAnimation.ResponsiveAnimationGuard.onLayoutChangeEnd(scene));
                });
            }
        } catch (Exception e) {
            CONFIG_LOCK.readLock().unlock();
            throw e;
        }
    }

    /**
     * Activa el modo debug del JIT.
     * @deprecated Use {@link #compileDebug(boolean)} instead.
     */
    @Deprecated(since = "1.0-SNAPSHOT", forRemoval = true)
    public static void jitDebug(boolean enabled) {
        CONFIG_LOCK.writeLock().lock();
        try { JitCompiler.setDebug(enabled); }
        finally { CONFIG_LOCK.writeLock().unlock(); }
    }

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
    // =========================================================================
    public static  <T extends javafx.scene.Node > T colSpan(T n, int s)   { return Styles.colSpan(n, s); }
    public static  <T extends javafx.scene.Node > T colSpanFull(T n)       { return Styles.colSpanFull(n); }
    public static  <T extends javafx.scene.Node > T rowSpan(T n, int s)    { return Styles.rowSpan(n, s); }
    public static  <T extends javafx.scene.Node > T rowSpanFull(T n)       { return Styles.rowSpanFull(n); }
    public static  <T extends javafx.scene.Node > T gridCell(T n, int c, int r) { return Styles.gridCell(n, c, r); }
    public static  <T extends javafx.scene.Node > T flex1(T n)          { return Styles.flex1(n); }
    public static  <T extends javafx.scene.Node > T grow(T n)           { return Styles.grow(n); }
    public static  <T extends javafx.scene.Node > T growNone(T n)       { return Styles.growNone(n); }
    public static  <T extends javafx.scene.Node > T vgrow(T n)          { return Styles.vgrow(n); }
    public static  <T extends javafx.scene.Node > T hboxFillWidth(T n)  { return Styles.hboxFillWidth(n); }
    public static  <T extends javafx.scene.Node > T vboxFillHeight(T n) { return Styles.vboxFillHeight(n); }
    public static  <T extends javafx.scene.Node > T m(T n, int v)                              { return Styles.m(n, v); }
    public static  <T extends javafx.scene.Node > T mx(T n, int v)                             { return Styles.mx(n, v); }
    public static  <T extends javafx.scene.Node > T my(T n, int v)                             { return Styles.my(n, v); }
    public static  <T extends javafx.scene.Node > T mt(T n, int v)                             { return Styles.mt(n, v); }
    public static  <T extends javafx.scene.Node > T mr(T n, int v)                             { return Styles.mr(n, v); }
    public static  <T extends javafx.scene.Node > T mb(T n, int v)                             { return Styles.mb(n, v); }
    public static  <T extends javafx.scene.Node > T ml(T n, int v)                             { return Styles.ml(n, v); }
    public static  <T extends javafx.scene.Node > T mxAuto(T n)                                { return Styles.mxAuto(n); }
    public static  <T extends javafx.scene.Node > T margin(T n, double t, double r, double b, double l) { return Styles.margin(n, t, r, b, l); }
    public static  <T extends javafx.scene.Node > T z(T n, int v)     { return Styles.z(n, v); }
    public static  <T extends javafx.scene.Node > T z50(T n)          { return Styles.z50(n); }
    public static  <T extends javafx.scene.Node > T orderFirst(T n)   { return Styles.orderFirst(n); }
    public static  <T extends javafx.scene.Node > T orderLast(T n)    { return Styles.orderLast(n); }
    public static  <T extends javafx.scene.Node > T selfStart(T n)        { return Styles.selfStart(n); }
    public static  <T extends javafx.scene.Node > T selfCenter(T n)       { return Styles.selfCenter(n); }
    public static  <T extends javafx.scene.Node > T selfEnd(T n)          { return Styles.selfEnd(n); }
    public static  <T extends javafx.scene.Node > T justifySelfStart(T n) { return Styles.justifySelfStart(n); }
    public static  <T extends javafx.scene.Node > T justifySelfCenter(T n){ return Styles.justifySelfCenter(n); }
    public static  <T extends javafx.scene.Node > T justifySelfEnd(T n)   { return Styles.justifySelfEnd(n); }
    public static  <T extends javafx.scene.Node > T grayscale(T n)                { return Styles.grayscale(n); }
    public static  <T extends javafx.scene.Node > T brightness(T n, double v)     { return Styles.brightness(n, v); }
    public static  <T extends javafx.scene.Node > T contrast(T n, double v)       { return Styles.contrast(n, v); }
    public static  <T extends javafx.scene.Node > T saturate(T n, double v)       { return Styles.saturate(n, v); }
    public static  <T extends javafx.scene.Node > T hueRotate(T n, double deg)    { return Styles.hueRotate(n, deg); }
    public static  <T extends javafx.scene.Node > T invert(T n)                   { return Styles.invert(n); }
    public static  <T extends javafx.scene.Node > T sepia(T n)                    { return Styles.sepia(n); }
    public static  <T extends javafx.scene.Node > T filterNone(T n)               { return Styles.filterNone(n); }
    public static  <T extends javafx.scene.Node > T skewX(T n, double deg) { return Styles.skewX(n, deg); }
    public static  <T extends javafx.scene.Node > T skewY(T n, double deg) { return Styles.skewY(n, deg); }
    public static javafx.scene.image.ImageView objectCover(javafx.scene.image.ImageView iv)   { return Styles.objectCover(iv); }
    public static javafx.scene.image.ImageView objectContain(javafx.scene.image.ImageView iv) { return Styles.objectContain(iv); }
    public static javafx.scene.image.ImageView objectCenter(javafx.scene.image.ImageView iv)  { return Styles.objectCenter(iv); }
    public static javafx.scene.image.ImageView imgSize(javafx.scene.image.ImageView iv, double w, double h, boolean r) { return Styles.imgSize(iv, w, h, r); }
    public static  <T extends javafx.scene.Node > T invisible(T n)  { return Styles.invisible(n); }
    public static  <T extends javafx.scene.Node > T hiddenNode(T n) { return Styles.hiddenNode(n); }
    public static  <T extends javafx.scene.Node > T show(T n)       { return Styles.show(n); }

    // =========================================================================
    // CONFIGURACIÓN GLOBAL (Thread-safe)
    // =========================================================================
    public static final class Config {
        private double unit = 4.0;
        private boolean debugMode = false;
        private boolean warnNoParent = true;
        private final Map<String, Integer> customBreakpoints = new LinkedHashMap<>();

        private Config() {}

        public Config unit(double px) {
            if (px <= 0) throw new IllegalArgumentException("Config.unit: must be > 0, got: " + px);
            CONFIG_LOCK.writeLock().lock();
            try {
                this.unit = px;
                JitCompiler.clearCache();
            } finally { CONFIG_LOCK.writeLock().unlock(); }
            return this;
        }

        public Config breakpoint(String name, int px) {
            Preconditions.requireNonBlank(name, "Config.breakpoint", "name");
            if (px < 0) throw new IllegalArgumentException("Config.breakpoint: px must be >= 0, got: " + px);
            CONFIG_LOCK.writeLock().lock();
            try { customBreakpoints.put(name, px); }
            finally { CONFIG_LOCK.writeLock().unlock(); }
            return this;
        }

        public Config debug(boolean enabled) {
            CONFIG_LOCK.writeLock().lock();
            try {
                this.debugMode = enabled;
                JitCompiler.setDebug(enabled);
            } finally { CONFIG_LOCK.writeLock().unlock(); }
            return this;
        }

        public Config warnOnNoParent(boolean enabled) {
            CONFIG_LOCK.writeLock().lock();
            try { this.warnNoParent = enabled; }
            finally { CONFIG_LOCK.writeLock().unlock(); }
            return this;
        }

        public Config autoBatch(int threshold) {
            StylePerf.setAutoBatchThreshold(threshold);
            return this;
        }

        public int getAutoBatchThreshold() { return StylePerf.getAutoBatchThreshold(); }
        public double unit() { return unit; }
        public boolean isDebug() { return debugMode; }
        public boolean isWarnNoParent() { return warnNoParent; }
        public Map<String, Integer> customBreakpoints() {
            CONFIG_LOCK.readLock().lock();
            try { return Collections.unmodifiableMap(customBreakpoints); }
            finally { CONFIG_LOCK.readLock().unlock(); }
        }
    }

    private static final Config GLOBAL_CONFIG = new Config();
    public static Config configure() { return GLOBAL_CONFIG; }
    public static double unit() { return GLOBAL_CONFIG.unit(); }
    public static void setDebug(boolean enabled) { GLOBAL_CONFIG.debug(enabled); }
    public static TailwindFXMetrics metrics() { return TailwindFXMetrics.instance(); }

    // =========================================================================
    // HOT RELOAD
    // =========================================================================
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
                                scene.getStylesheets().removeIf(s -> s.contains(path.getFileName().toString()));
                                scene.getStylesheets().add(url);
                                CONFIG_LOCK.writeLock().lock();
                                try { JitCompiler.clearCache(); }
                                finally { CONFIG_LOCK.writeLock().unlock(); }
                                Preconditions.LOG.info("TailwindFX: CSS reloaded — " + path.getFileName());
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
    // DEBUG API
    // =========================================================================
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
            node.getStyle() != null && !node.getStyle().isBlank() ? node.getStyle() : "(none)",
            JitCompiler.cacheSize(),
            cats.isEmpty() ? "(vacio)" : cats,
            animStr
        );
    }

    public static void debugPrint(Node node) { System.out.println(debugReport(node)); }

    // =========================================================================
    // PERSISTENCIA DE TEMAS
    // =========================================================================
    public static void saveTheme(Scene scene, String key)    { ThemeManager.saveTheme(scene, key); }
    public static boolean loadTheme(Scene scene, String key) { return ThemeManager.loadTheme(scene, key); }
    public static void deleteTheme(String key) { ThemeManager.deleteTheme(key); }

    // =========================================================================
    // RESPONSIVE NODE
    // =========================================================================
    public static ResponsiveNode.Builder responsive(Node node) {
        return ResponsiveNode.on(node);
    }

    // =========================================================================
    // FXFLEXPANE & FXGRIDPANE
    // =========================================================================
    public static FxFlexPane flexRow() { return FxFlexPane.row(); }
    public static FxFlexPane flexCol() { return FxFlexPane.col(); }
    public static FxGridPane.Builder grid() { return FxGridPane.create(); }

    // =========================================================================
    // COMPONENT FACTORY
    // =========================================================================
    public static ComponentFactory component() { return COMPONENT_FACTORY; }
    private static final ComponentFactory COMPONENT_FACTORY = new ComponentFactory();

    public static ComponentFactory.CardBuilder card()             { return ComponentFactory.card(); }
    public static Label  badge(String text, String color)         { return ComponentFactory.badge(text, color); }
    public static Label  pill(String text, String color)          { return ComponentFactory.pill(text, color); }
    public static Tooltip tooltip(Node node, String text)         { return ComponentFactory.tooltip(node, text); }
    public static ComponentFactory.GlassBuilder glass()           { return ComponentFactory.glass(); }
    public static ComponentFactory.NeumorphicBuilder neumorphic(javafx.scene.layout.Region n) { return ComponentFactory.neumorphic(n); }
    public static ComponentFactory.ModalBuilder modal(Node content){ return ComponentFactory.modal(content); }
    public static ComponentFactory.DrawerBuilder drawer(ComponentFactory.DrawerSide side, double size) { return ComponentFactory.drawer(side, size); }
    public static <T> ComponentFactory.DataTableBuilder<T> dataTable(Class<T> type) { return ComponentFactory.dataTable(type); }
    public static StackPane avatar(String initials, String color, double size) { return ComponentFactory.avatar(initials, color, size); }
    public static HBox alert(String message, String type)         { return ComponentFactory.alert(message, type); }
    public static void flexBasis(javafx.scene.Node node, double basis) {
        FxFlexPane.setBasis(node, basis);
    }
    public static void flexDirection(FxFlexPane pane, FxFlexPane.Direction d, int durationMs) {
        Preconditions.requireNonNull(pane, "TailwindFX.flexDirection", "pane");
        pane.setDirectionAnimated(d, durationMs);
    }

    // =========================================================================
    // PERFORMANCE
    // =========================================================================
    public static void batch(Runnable work) {
        Preconditions.requireNonNull(work, "TailwindFX.batch", "work");
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("TailwindFX.batch: must be called on the JavaFX Application Thread. Use TailwindFX.batchAsync() for background threads.");
        }
        StylePerf.batch(work);
    }
    public static void batchAsync(Runnable work)  { StylePerf.batchAsync(work); }
    public static void invalidateDiff(Node node)  { StylePerf.invalidate(node); }

    // =========================================================================
    // CACHE CLEANUP
    // =========================================================================
    public static void cleanupNode(Node node) { UtilityConflictResolver.cleanupNode(node); }
    public static void autoCleanup(Node node) { UtilityConflictResolver.autoCleanup(node); }
    public static void invalidateCache(Node node) { UtilityConflictResolver.invalidateCache(node); }
    public static void invalidateCategoryCache(Node node, String category) {
        UtilityConflictResolver.invalidateCategoryCache(node, category);
    }

    // =========================================================================
    // TAILWIND v4.1 — TEXT-SHADOW & DROP-SHADOW
    // =========================================================================
    public static void textShadowSm(Node n)   { Styles.textShadowSm(n); }
    public static void textShadowMd(Node n)   { Styles.textShadowMd(n); }
    public static void textShadowLg(Node n)   { Styles.textShadowLg(n); }
    public static void textShadowXl(Node n)   { Styles.textShadowXl(n); }
    public static void textShadowNone(Node n) { Styles.textShadowNone(n); }
    public static void textShadow(Node node, String color, double radius, double offsetX, double offsetY) {
        Styles.textShadow(node, color, radius, offsetX, offsetY);
    }
    public static void dropShadow(Node node, String hexColor, double alpha, double radius, double offsetX, double offsetY) {
        Styles.dropShadow(node, hexColor, alpha, radius, offsetX, offsetY);
    }
    public static void dropShadowBlue(Node n)   { Styles.dropShadowBlue(n); }
    public static void dropShadowGreen(Node n)  { Styles.dropShadowGreen(n); }
    public static void dropShadowRed(Node n)    { Styles.dropShadowRed(n);  }
    public static void dropShadowPurple(Node n) { Styles.dropShadowPurple(n); }

    // =========================================================================
    // MASK / CLIP
    // =========================================================================
    public static void clipCircle(Node node) { Styles.clipCircle(node); }
    public static void clipRounded(Node node, double radius) { Styles.clipRounded(node, radius); }
    public static void clipMask(Node node, javafx.scene.shape.Shape shape) { Styles.clipMask(node, shape); }
    public static void clipNone(Node node) { Styles.clipNone(node); }

    // =========================================================================
    // SVG
    // =========================================================================
    public static void fill(javafx.scene.shape.Shape shape, String hex) { Styles.fill(shape, hex); }
    public static void stroke(javafx.scene.shape.Shape shape, String hex) { Styles.stroke(shape, hex); }
    public static void strokeWidth(javafx.scene.shape.Shape shape, double width) { Styles.strokeWidth(shape, width); }

    // =========================================================================
    // 3D TRANSFORMS
    // =========================================================================
    public static void rotateX(Node n, double deg)  { Styles.rotateX(n, deg); }
    public static void rotateY(Node n, double deg)  { Styles.rotateY(n, deg); }
    public static void translateZ(Node n, double px) { Styles.translateZ(n, px); }
    public static void reset3D(Node n) { Styles.reset3D(n); }

    // =========================================================================
    // MOTION-REDUCE
    // =========================================================================
    public static void setReducedMotion(boolean reduced) { Styles.setReducedMotion(reduced); }
    public static boolean shouldAnimate() { return Styles.shouldAnimate(); }
    public static void playIfMotionOk(FxAnimation animation) { Styles.playIfMotionOk(animation); }

    // =========================================================================
    // GLASSMORPHISM / NEUMORPHISM
    // =========================================================================
    public static void glass(javafx.scene.layout.Region node) { Styles.glass(node); }
    public static void glassDark(javafx.scene.layout.Region node) { Styles.glassDark(node); }
    public static void neumorph(javafx.scene.layout.Region node) { Styles.neumorph(node); }
    public static void neumorphInset(javafx.scene.layout.Region node) { Styles.neumorphInset(node); }

    // =========================================================================
    // UNSUPPORTED CSS FEATURES — Java API equivalents
    // =========================================================================
    public static void aspectRatio(javafx.scene.layout.Region node, double width, double height) {
        Preconditions.requireNonNull(node, "TailwindFX.aspectRatio", "node");
        if (width  <= 0) throw new IllegalArgumentException("TailwindFX.aspectRatio: width must be > 0");
        if (height <= 0) throw new IllegalArgumentException("TailwindFX.aspectRatio: height must be > 0");
        double ratio = height / width;
        node.widthProperty().addListener((obs, ov, nv) -> node.setPrefHeight(nv.doubleValue() * ratio));
        if (node.getWidth() > 0) node.setPrefHeight(node.getWidth() * ratio);
    }
    public static void aspectSquare(javafx.scene.layout.Region node) { aspectRatio(node, 1, 1); }

    /**
     * Aplica blur al nodo mismo. JavaFX no soporta backdrop-blur real.
     * Renombrado para eliminar ambigüedad. {@code backdropBlur*} permanece como alias deprecated.
     */
    public static void blurSelf(Node node, double radius) {
        Preconditions.requireNode(node, "TailwindFX.blurSelf");
        if (radius <= 0) {
            if (node.getEffect() instanceof javafx.scene.effect.BoxBlur)
                node.setEffect(null);
        } else {
            node.setEffect(new javafx.scene.effect.BoxBlur(radius, radius, 3));
        }
    }

    /** @deprecated Use {@link #blurSelf(Node, double)} to reflect JavaFX capabilities accurately. */
    @Deprecated
    public static void backdropBlur(Node node, double radius) { blurSelf(node, radius); }
    /** @deprecated Use {@link #blurSelfSm(Node)} */
    @Deprecated public static void backdropBlurSm(Node node)   { blurSelf(node, 4);  }
    /** @deprecated Use {@link #blurSelfMd(Node)} */
    @Deprecated public static void backdropBlurMd(Node node)   { blurSelf(node, 8);  }
    /** @deprecated Use {@link #blurSelfLg(Node)} */
    @Deprecated public static void backdropBlurLg(Node node)   { blurSelf(node, 16); }
    /** @deprecated Use {@link #blurSelfXl(Node)} */
    @Deprecated public static void backdropBlurXl(Node node)   { blurSelf(node, 24); }
    /** @deprecated Use {@link #blurSelfNone(Node)} */
    @Deprecated public static void backdropBlurNone(Node node) { blurSelf(node, 0);  }

    public static void blurSelfSm(Node node)   { blurSelf(node, 4);  }
    public static void blurSelfMd(Node node)   { blurSelf(node, 8);  }
    public static void blurSelfLg(Node node)   { blurSelf(node, 16); }
    public static void blurSelfXl(Node node)   { blurSelf(node, 24); }
    public static void blurSelfNone(Node node) { blurSelf(node, 0);  }

    public static FxAnimation transition(Node node, int durationMs, javafx.animation.KeyValue... values) {
        Preconditions.requireNode(node, "TailwindFX.transition");
        Preconditions.requirePositiveDuration(durationMs, "TailwindFX.transition");
        javafx.animation.Timeline tl = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(durationMs), values));
        return new FxAnimation(tl).register(node, "transition");
    }
}