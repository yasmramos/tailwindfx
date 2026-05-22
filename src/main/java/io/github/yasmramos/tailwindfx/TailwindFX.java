package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.layout.TwFlexPane;
import io.github.yasmramos.tailwindfx.layout.TwGridPane;
import io.github.yasmramos.tailwindfx.layout.TwLayoutHelper;
import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import io.github.yasmramos.tailwindfx.responsive.ResponsiveNode;
import io.github.yasmramos.tailwindfx.style.StylePerf;
import io.github.yasmramos.tailwindfx.theme.ThemeManager;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * TailwindFX — Main entry point (lightweight facade).
 *
 * <p>This class delegates to specialized facades for each responsibility.
 *
 * <p>All methods are static for direct access:
 *
 * <pre>
 * TailwindFX.apply(node, "btn-primary", "rounded-lg");
 * TailwindFX.install(scene);
 * TailwindFX.theme(scene).dark().apply();
 * </pre>
 *
 * @see TwStyle
 * @see TwInstall
 */
public final class TailwindFX {
  private TailwindFX() {}

  /** Apply utility classes and JIT tokens to a node. */
  public static void apply(Node node, String... tokens) {
    TwStyle.apply(node, tokens);
  }

  /** Apply JIT-compiled styles to a node. */
  public static void jit(Node node, String... tokens) {
    TwStyle.apply(node, tokens);
  }

  /** Remove CSS classes from a node. */
  public static void remove(Node node, String... classes) {
    TwStyle.remove(node, classes);
  }

  /** Toggle a CSS class on a node. */
  public static void toggle(Node node, String cssClass) {
    TwStyle.toggle(node, cssClass);
  }

  /** Install all CSS stylesheets. */
  public static void install(Scene scene) {
    TwInstall.install(scene);
  }

  /** Install all CSS stylesheets with Stage for responsive support. */
  public static void install(Scene scene, Stage stage) {
    TwInstall.install(scene, stage);
  }

  /** Install only the base module. */
  public static void installBase(Scene scene) {
    TwInstall.installBase(scene);
  }

  /** Install components module. */
  public static void installComponents(Scene scene) {
    TwInstall.installComponents(scene);
  }

  /** Install utilities module. */
  public static void installUtilities(Scene scene) {
    TwInstall.installUtilities(scene);
  }

  /** Install colors module. */
  public static void installColors(Scene scene) {
    TwInstall.installColors(scene);
  }

  /** Install effects module. */
  public static void installEffects(Scene scene) {
    TwInstall.installEffects(scene);
  }

  /** Install dark mode styles. */
  public static void installDark(Scene scene) {
    TwInstall.installDark(scene);
  }

  /** Install essential modules (base, components, presets). */
  public static void installEssentials(Scene scene) {
    TwInstall.installEssentials(scene);
  }

  /** Get theme manager for a scene. */
  public static ThemeManager theme(Scene scene) {
    return ThemeManager.forScene(scene);
  }

  /** Get layout builder for a container. */
  public static TwLayoutHelper layout(Pane container) {
    return TwLayoutHelper.of(container);
  }

  /** Apply layout classes (flex, grid, gap) with automatic container migration if needed. */
  public static void layout(Node node, String... tokens) {
    TwLayout.apply(node, tokens);
  }

  /** Create a horizontal flex pane. */
  public static TwFlexPane flexRow() {
    return TwFlexPane.row();
  }

  /** Create a grid pane. */
  public static TwGridPane grid() {
    return TwGridPane.create().build();
  }

  /** Install responsive support on a Stage. */
  public static ResponsiveNode responsive(Stage stage) {
    return ResponsiveNode.on(stage.getScene().getRoot()).install(stage.getScene());
  }

  /** Execute style operations in batch mode. */
  public static void batch(Runnable action) {
    StylePerf.batch(action);
  }

  /** Get metrics instance. */
  public static TailwindFXMetrics metrics() {
    return TailwindFXMetrics.instance();
  }

  /** Get current unit size. */
  public static double unit() {
    return TwConfig.unit();
  }

  /** Set unit size. */
  public static void unit(double value) {
    TwConfig.unit(value);
  }

  /** Enable/disable debug mode. */
  public static void debug(boolean enabled) {
    TwConfig.debug(enabled);
  }

  /** Check if debug mode is enabled. */
  public static boolean isDebug() {
    return TwConfig.isDebug();
  }
}
