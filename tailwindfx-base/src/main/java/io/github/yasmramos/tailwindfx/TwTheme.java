/*
 * Copyright 2026 Yasmany Ramos García (yasmramos).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.theme.ThemeManager;
import io.github.yasmramos.tailwindfx.theme.ThemeScopeManager;
import javafx.scene.Scene;

/**
 * TwTheme — Theme management facade.
 *
 * <p>Provides access to theme operations including dark/light mode, theme scoping, and preset
 * management.
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
   *
   * @param scene the scene
   * @return ThemeManager instance
   */
  public static ThemeManager of(Scene scene) {
    return ThemeManager.forScene(scene);
  }

  /**
   * Get theme manager for a scene (alias for of).
   *
   * @param scene the scene
   * @return ThemeManager instance
   */
  public static ThemeManager forScene(Scene scene) {
    return ThemeManager.forScene(scene);
  }

  /**
   * Get theme scope manager for a node.
   *
   * @param pane the pane node
   * @return ScopeBuilder to configure and apply theme
   */
  public static ThemeScopeManager.ScopeBuilder scope(javafx.scene.layout.Pane pane) {
    return ThemeScopeManager.scope(pane);
  }

  /**
   * Save current theme to a file.
   *
   * @param scene the scene
   * @param themeName the theme name
   */
  public static void saveTheme(Scene scene, String themeName) {
    ThemeManager.saveTheme(scene, themeName);
  }

  /**
   * Load theme from a file.
   *
   * @param scene the scene
   * @param themeName the theme name
   * @return true if loaded successfully
   */
  public static boolean loadTheme(Scene scene, String themeName) {
    return ThemeManager.loadTheme(scene, themeName);
  }

  /**
   * Delete a saved theme.
   *
   * @param themeName the theme name
   */
  public static void deleteTheme(String themeName) {
    ThemeManager.deleteTheme(themeName);
  }
}
