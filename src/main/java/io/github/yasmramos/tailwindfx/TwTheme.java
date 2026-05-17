package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.theme.ThemeManager;
import io.github.yasmramos.tailwindfx.theme.ThemeScopeManager;
import javafx.scene.Scene;
import javafx.scene.Node;

/**
 * TwTheme — Theme management facade.
 * 
 * <p>Provides access to theme operations including dark/light mode,
 * theme scoping, and preset management.</p>
 * 
 * <pre>
 * TwTheme.of(scene).dark().apply();
 * TwTheme.scope(node).preset("blue").apply();
 * TwTheme.saveTheme(scene, "my-theme");
 * </pre>
 */
public final class TwTheme {
    
    private static final TwTheme INSTANCE = new TwTheme();
    
    private TwTheme() {}
    
    /**
     * Get theme manager for a scene.
     * @param scene the scene
     * @return ThemeManager instance
     */
    public static ThemeManager of(Scene scene) {
        return ThemeManager.forScene(scene);
    }
    
    /**
     * Get theme manager for a scene (alias for of).
     * @param scene the scene
     * @return ThemeManager instance
     */
    public static ThemeManager forScene(Scene scene) {
        return ThemeManager.forScene(scene);
    }
    
    /**
     * Get theme scope manager for a node.
     * @param pane the pane node
     * @return ScopeBuilder to configure and apply theme
     */
    public static ThemeScopeManager.ScopeBuilder scope(javafx.scene.layout.Pane pane) {
        return ThemeScopeManager.scope(pane);
    }
    
    /**
     * Save current theme to a file.
     * @param scene the scene
     * @param themeName the theme name
     */
    public static void saveTheme(Scene scene, String themeName) {
        ThemeManager.saveTheme(scene, themeName);
    }
    
    /**
     * Load theme from a file.
     * @param scene the scene
     * @param themeName the theme name
     * @return true if loaded successfully
     */
    public static boolean loadTheme(Scene scene, String themeName) {
        return ThemeManager.loadTheme(scene, themeName);
    }
    
    /**
     * Delete a saved theme.
     * @param themeName the theme name
     */
    public static void deleteTheme(String themeName) {
        ThemeManager.deleteTheme(themeName);
    }
}
