package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.breakpoint.BreakpointManager;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Objects;

/**
 * TwInstall — Installation facade for CSS stylesheets.
 * 
 * <p>This class handles installing TailwindFX CSS files into JavaFX scenes.</p>
 * 
 * <p>Usage:</p>
 * <pre>
 * TwInstall.install(scene);
 * TwInstall.installBase(scene);
 * TwInstall.installDark(scene);
 * </pre>
 */
public final class TwInstall {
    
    private TwInstall() {}
    
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
        BreakpointManager.attach(stage);
    }
    
    private static void installCss(Scene scene, String cssPath, int priority) {
        // ClassLoader.getResource() no acepta "/" inicial; Class.getResource() sí.
        String normalizedPath = cssPath.startsWith("/") ? cssPath.substring(1) : cssPath;
        java.net.URL url = null;

        // 1. Thread Context ClassLoader: resolve recursos en OSGi bundles, Java Modules o classloaders delegados
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if (tccl != null) url = tccl.getResource(normalizedPath);

        // 2. ClassLoader de TailwindFX: fallback para entornos donde el TCCL es el del host/app
        if (url == null) url = TwInstall.class.getClassLoader().getResource(normalizedPath);

        // 3. Resolución relativa a la clase: captura recursos empaquetados junto al framework
        if (url == null) url = TwInstall.class.getResource(cssPath);

        String urlStr = Objects.requireNonNull(url,
            cssPath + " not found via TCCL, Framework CL, or class-relative path").toExternalForm();

        var sheets = scene.getStylesheets();
        if (sheets.contains(urlStr)) sheets.remove(urlStr);
    
        // Inserción determinista por prioridad (mantiene cascada CSS estable)
        sheets.add(Math.min(priority, sheets.size()), urlStr);
    }
}
