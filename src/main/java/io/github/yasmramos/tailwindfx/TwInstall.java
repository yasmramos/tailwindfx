package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.breakpoint.BreakpointManager;
import io.github.yasmramos.tailwindfx.core.ThemeCssGenerator;
import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
import java.util.Objects;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * TwInstall — Installation facade for CSS stylesheets.
 *
 * <p>This class handles installing minimal TailwindFX CSS files into JavaFX scenes. Most utilities
 * are now JIT-compiled at runtime via JitCompiler. Base variables are generated dynamically from
 * ThemeConfig.
 *
 * <p>Usage:
 *
 * <pre>
 * TwInstall.install(scene);
 * TwInstall.installMinimal(scene);
 * </pre>
 */
public final class TwInstall {

  private static final String GENERATED_BASE_CSS_ID = "tailwindfx-base-generated";

  private TwInstall() {}

  /** Installs minimal CSS (base variables only). All utilities are JIT-compiled. */
  public static void install(Scene scene) {
    installMinimal(scene);
  }

  public static void install(Scene scene, Stage stage) {
    installMinimal(scene, stage);
  }

  /** Installs only the base module (variables and reset). Required for JIT compilation. */
  public static void installBase(Scene scene) {
    installGeneratedBaseCss(scene, 0);
  }

  /** Installs dark mode overrides. Optional. */
  public static void installDark(Scene scene) {
    installCss(scene, "/tailwindfx/tailwindfx-dark.css", 10);
  }

  /**
   * Minimal installation: base CSS generated dynamically. All utilities JIT-compiled at runtime.
   */
  public static void installMinimal(Scene scene) {
    installBase(scene);
    // Optional: uncomment if you need dark mode support
    // installDark(scene);
  }

  private static void installMinimal(Scene scene, Stage stage) {
    installMinimal(scene);
    BreakpointManager.attach(stage);
  }

  /**
   * Installs dynamically generated base CSS from ThemeConfig. Removes any previously installed
   * static base CSS.
   */
  private static void installGeneratedBaseCss(Scene scene, int priority) {
    // Generate CSS from ThemeConfig
    ThemeConfig themeConfig = ThemeConfig.defaultConfig();
    ThemeCssGenerator generator = new ThemeCssGenerator(themeConfig);
    String generatedCss = generator.generateBaseCss();

    // Create a data URL for the generated CSS
    String dataUrl = "data:text/css," + generatedCss.replace("#", "%23").replace("\n", "%0A");

    var sheets = scene.getStylesheets();

    // Remove any existing generated base CSS
    sheets.removeIf(url -> url.contains(GENERATED_BASE_CSS_ID));

    // Also remove static base CSS if it exists
    sheets.removeIf(url -> url.contains("tailwindfx-base.css"));

    // Insert at specified priority
    sheets.add(Math.min(priority, sheets.size()), dataUrl);
  }

  private static void installCss(Scene scene, String cssPath, int priority) {
    // ClassLoader.getResource() no acepta "/" inicial; Class.getResource() sí.
    String normalizedPath = cssPath.startsWith("/") ? cssPath.substring(1) : cssPath;
    java.net.URL url = null;

    // 1. Thread Context ClassLoader: resolve recursos en OSGi bundles, Java Modules o classloaders
    // delegados
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    if (tccl != null) url = tccl.getResource(normalizedPath);

    // 2. ClassLoader de TailwindFX: fallback para entornos donde el TCCL es el del host/app
    if (url == null) url = TwInstall.class.getClassLoader().getResource(normalizedPath);

    // 3. Resolución relativa a la clase: captura recursos empaquetados junto al framework
    if (url == null) url = TwInstall.class.getResource(cssPath);

    String urlStr =
        Objects.requireNonNull(
                url, cssPath + " not found via TCCL, Framework CL, or class-relative path")
            .toExternalForm();

    var sheets = scene.getStylesheets();
    if (sheets.contains(urlStr)) sheets.remove(urlStr);

    // Inserción determinista por prioridad (mantiene cascada CSS estable)
    sheets.add(Math.min(priority, sheets.size()), urlStr);
  }
}
