package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.breakpoint.BreakpointManager;
import io.github.yasmramos.tailwindfx.responsive.ResponsiveNode;
import io.github.yasmramos.tailwindfx.components.FxFlexPane;
import io.github.yasmramos.tailwindfx.components.FxGridPane;
import io.github.yasmramos.tailwindfx.style.StylePerf;
import io.github.yasmramos.tailwindfx.animation.FxAnimation;
import io.github.yasmramos.tailwindfx.theme.ThemeManager;
import io.github.yasmramos.tailwindfx.theme.ThemeScopeManager;
import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import io.github.yasmramos.tailwindfx.components.ComponentFactory;
import io.github.yasmramos.tailwindfx.layout.FxLayout;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * TailwindFX — Main entry point (lightweight facade).
 * 
 * <p>This class delegates to specialized facades for each responsibility.</p>
 * 
 * <p>For new code, prefer using the specialized facades directly:</p>
 * <pre>
 * TwStyle.apply(node, "btn-primary", "rounded-lg");
 * TwInstall.install(scene);
 * TwTheme.of(scene).dark().apply();
 * </pre>
 * 
 * @see TwStyle
 * @see TwInstall
 */
public final class TailwindFX {
    private TailwindFX() {}

    // =========================================================================
    // Specialized Facades (preferred API)
    // =========================================================================

    /** Access the Style facade for utility classes and JIT tokens. */
    public static TwStyle style() { return TwStyle.INSTANCE; }

    /** Access the Install facade for CSS stylesheets. */
    public static void install(Scene scene) { TwInstall.install(scene); }

    /** Access the Install facade with Stage for responsive support. */
    public static void install(Scene scene, Stage stage) { TwInstall.install(scene, stage); }

    // =========================================================================
    // Convenience Delegates (backward compatible)
    // =========================================================================

    /** @see TwStyle#apply */
    public static void apply(Node node, String... tokens) {
        TwStyle.INSTANCE.apply(node, tokens);
    }

    /** @see TwStyle#jit */
    public static void jit(Node node, String... tokens) {
        TwStyle.INSTANCE.apply(node, tokens);
    }

    /** @see TwStyle#remove */
    public static void remove(Node node, String... classes) {
        TwStyle.INSTANCE.remove(node, classes);
    }

    /** @see TwStyle#toggle */
    public static void toggle(Node node, String cssClass) {
        TwStyle.INSTANCE.toggle(node, cssClass);
    }

    /** @see TwInstall#installBase */
    public static void installBase(Scene scene) {
        TwInstall.installBase(scene);
    }

    /** @see TwInstall#installComponents */
    public static void installComponents(Scene scene) {
        TwInstall.installComponents(scene);
    }

    /** @see TwInstall#installUtilities */
    public static void installUtilities(Scene scene) {
        TwInstall.installUtilities(scene);
    }

    /** @see TwInstall#installColors */
    public static void installColors(Scene scene) {
        TwInstall.installColors(scene);
    }

    /** @see TwInstall#installEffects */
    public static void installEffects(Scene scene) {
        TwInstall.installEffects(scene);
    }

    /** @see TwInstall#installDark */
    public static void installDark(Scene scene) {
        TwInstall.installDark(scene);
    }

    /** @see TwInstall#installEssentials */
    public static void installEssentials(Scene scene) {
        TwInstall.installEssentials(scene);
    }

    /** @see ThemeManager#forScene */
    public static ThemeManager theme(Scene scene) {
        return ThemeManager.forScene(scene);
    }

    /** @see FxLayout#of */
    public static FxLayout layout(Pane container) {
        return FxLayout.of(container);
    }

    public static FxFlexPane flexRow() {
        return FxFlexPane.row();
    }

    public static FxGridPane grid() {
        return FxGridPane.create().build();
    }

    /** @see ResponsiveNode#on */
    public static ResponsiveNode responsive(Stage stage) {
        return ResponsiveNode.on(stage.getScene().getRoot()).install(stage.getScene());
    }

    public static void batch(Runnable action) {
        StylePerf.batch(action);
    }

    public static TailwindFXMetrics metrics() {
        return TailwindFXMetrics.instance();
    }

    /** @see TwConfig#unit */
    public static double unit() {
        return TwConfig.INSTANCE.unit();
    }

    /** @see TwConfig#unit(double) */
    public static void unit(double value) {
        TwConfig.INSTANCE.unit(value);
    }

    /** @see TwConfig#debug */
    public static void debug(boolean enabled) {
        TwConfig.INSTANCE.debug(enabled);
    }

    /** @see TwConfig#isDebug */
    public static boolean isDebug() {
        return TwConfig.INSTANCE.isDebug();
    }
}
