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
package io.github.yasmramos.tailwindfx.theme;

import io.github.yasmramos.tailwindfx.core.Preconditions;
import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.util.Duration;

/**
 * ThemeManager v2 — TailwindFX theme engine.
 *
 * <p>Features: 1. MODENA VARIABLES: overrides -fx-base, -fx-accent, etc. in the root. Modena
 * automatically propagates to ALL child controls.
 *
 * <p>2. PREDEFINED THEMES: light, dark, blue, green, purple, rose, slate.
 *
 * <p>3. THEME INHERITANCE: base theme + partial override.
 * ThemeManager.theme(scene).preset("dark").accent("#f97316").apply();
 *
 * <p>4. SCOPES: apply a theme only to a subtree of nodes.
 * ThemeManager.scope(myPanel).preset("dark").applyTo(myPanel);
 *
 * <p>5. ANIMATED TRANSITION (optional): ThemeManager.theme(scene).dark().animated(300).apply();
 *
 * <p>Basic usage: TailwindFX.theme(scene).dark().apply();
 * TailwindFX.theme(scene).preset("blue").apply();
 * TailwindFX.theme(scene).base("#1e293b").accent("#3b82f6").apply(); ThemeManager.toggle(scene);
 */
public final class ThemeManager {

  // Temas predefinidos
  private record ThemeVars(
      String base,
      String innerBg,
      String bg,
      String accent,
      String focus,
      String faintFocus,
      String defaultBtn) {}

  private static final Map<String, ThemeVars> PRESETS = new LinkedHashMap<>();

  static {
    PRESETS.put(
        "light",
        new ThemeVars(
            "#ececec", "#f5f5f5", "#f9f9f9", "#0096C9", "#039ED3", "#039ED322", "#ABD8ED"));
    PRESETS.put(
        "dark",
        new ThemeVars(
            "#2b2b2b",
            "#1e1e1e",
            "#161616",
            "#3b82f6",
            "#60a5fa",
            "rgba(96,165,250,0.15)",
            "#1e3a8a"));
    PRESETS.put(
        "blue",
        new ThemeVars(
            "#dbeafe",
            "#e0ecff",
            "#f0f7ff",
            "#2563eb",
            "#3b82f6",
            "rgba(59,130,246,0.2)",
            "#93c5fd"));
    PRESETS.put(
        "green",
        new ThemeVars(
            "#dcfce7",
            "#e0fbe9",
            "#f0fdf4",
            "#16a34a",
            "#22c55e",
            "rgba(34,197,94,0.2)",
            "#86efac"));
    PRESETS.put(
        "purple",
        new ThemeVars(
            "#ede9fe",
            "#f0ecff",
            "#f9f7ff",
            "#7c3aed",
            "#8b5cf6",
            "rgba(139,92,246,0.2)",
            "#c4b5fd"));
    PRESETS.put(
        "rose",
        new ThemeVars(
            "#ffe4e6",
            "#ffe8ea",
            "#fff5f6",
            "#e11d48",
            "#f43f5e",
            "rgba(244,63,94,0.2)",
            "#fda4af"));
    PRESETS.put(
        "slate",
        new ThemeVars(
            "#e2e8f0",
            "#eaf0f6",
            "#f1f5f9",
            "#475569",
            "#64748b",
            "rgba(100,116,139,0.2)",
            "#94a3b8"));
  }

  /** Lista de temas disponibles */
  public static List<String> availableThemes() {
    return new ArrayList<>(PRESETS.keySet());
  }

  /**
   * Last preset applied through {@link #preset(String)} (lowercased). Used as the primary source
   * to disambiguate the active theme in {@link #cyclePreset(Scene)}; matching style tokens acts
   * as a fallback for scenes not managed by this ThemeManager.
   */
  private static volatile String lastPresetName;

  // Builder state
  private final Scene scene;
  private final Node scopeNode; // null = applies to scene root
  private final Map<String, String> vars = new LinkedHashMap<>();
  private long animDurationMs = 0;

  private ThemeManager(Scene scene, Node scopeNode) {
    this.scene = scene;
    this.scopeNode = scopeNode;
  }

  // Factories
  /** Applies to the entire Scene root */
  public static ThemeManager forScene(Scene scene) {
    Preconditions.requireNonNull(scene, "ThemeManager.forScene", "scene");
    return new ThemeManager(scene, null);
  }

  /** Applies only to the specified node and its subtree */
  public static ThemeManager scope(Node node) {
    return new ThemeManager(null, node);
  }

  // Builder — preset
  /** Applies a predefined theme */
  public ThemeManager preset(String name) {
    Preconditions.requireNonBlank(name, "ThemeManager.preset", "name");
    // Locale.ROOT avoids surprises with locales such as Turkish (e.g. preset("BLUE")).
    String key = name.toLowerCase(Locale.ROOT);
    ThemeVars t = PRESETS.get(key);
    if (t == null) {
      throw new IllegalArgumentException(
          "ThemeManager.preset: theme '"
              + name
              + "' does not exist. Available: "
              + PRESETS.keySet());
    }
    lastPresetName = key;
    return base(t.base())
        .innerBackground(t.innerBg())
        .background(t.bg())
        .accent(t.accent())
        .focus(t.focus())
        .faintFocus(t.faintFocus())
        .defaultButton(t.defaultBtn());
  }

  /** Quick aliases */
  public ThemeManager dark() {
    return preset("dark");
  }

  public ThemeManager light() {
    return preset("light");
  }

  // Builder — variables individuales
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

  /** Enables animated transition when applying the theme. durationMs = milliseconds */
  public ThemeManager animated(long durationMs) {
    animDurationMs = durationMs;
    return this;
  }

  // apply() — injects the theme
  /**
   * Applies the theme to the Scene root (or to scopeNode if scope() was used).
   *
   * <ul>
   *   <li>Forces style refresh on ALL descendant nodes
   *   <li>Applies theme to Stage window chrome (if available)
   *   <li>Ensures Modena variables propagate correctly
   * </ul>
   */
  public void apply() {
    if (vars.isEmpty()) {
      Preconditions.LOG.warning(
          "ThemeManager.apply: no variables defined — use preset() or base()/accent() before apply()");
      return;
    }

    Node target = resolveTarget();
    if (target == null) {
      Preconditions.LOG.warning(
          "ThemeManager.apply: target not found — Scene has no root or scopeNode is null");
      return;
    }

    boolean isDark = isColorDark(vars.getOrDefault("-fx-base", "#ececec"));
    String newStyle = buildStyleString(isDark);

    // Manage the .dark class
    String baseColor = vars.getOrDefault("-fx-base", "#ececec");
    boolean darkBase = isColorDark(baseColor);
    if (isPresetName(baseColor)) {
      lastPresetName = baseColor.toLowerCase(Locale.ROOT);
    } else if (lastPresetName != null && !baseMatchesPreset(baseColor)) {
      // The theme is no longer a pure preset (e.g. base()/accent() was overridden manually):
      // drop the record so cyclePreset falls back to token matching.
      lastPresetName = null;
    }

    if (animDurationMs > 0) {
      // When animating, the .dark class is applied together with the tokens inside the
      // corresponding KeyFrame, so both change at the same instant.
      applyAnimated(target, newStyle, darkBase);
    } else {
      target.setStyle(newStyle);
      target.getStyleClass().remove("dark");
      if (darkBase) {
        target.getStyleClass().add("dark");
      }
    }

    // Toggle the .dark class before refreshing so that `.dark ...` selectors are already
    // matched when the style cache is invalidated.
    target.getStyleClass().remove("dark");
    if (isDark) {
      target.getStyleClass().add("dark");
    }

    // CRITICAL FIX 1: Force style refresh on all descendant nodes
    // JavaFX caches computed styles, so we need to invalidate the cache
    forceStyleRefresh(target);

    TailwindFXMetrics.instance().recordThemeSwitch();

    // CRITICAL FIX 2: Apply theme to Stage window chrome (title bar, borders)
    if (scene != null && scene.getWindow() instanceof javafx.stage.Stage stage) {
      applyToStage(stage, darkBase);
    }
  }

  /**
   * Applies the same variable style and the {@code dark} class to the root of every open
   * {@link Scene} across all application windows ({@link javafx.stage.Window#getWindows()}).
   *
   * <p>This lets Dialogs, Popups and other windows with their own scene inherit the active theme
   * tokens, since {@link #apply()} only affects the scene associated with this ThemeManager.
   *
   * <p><b>Note:</b> scenes created after invoking this method do not benefit from it; they must
   * be registered manually by applying the theme via {@code ThemeManager.forScene(newScene)...
   * .apply()} (or by calling {@code applyToAllWindows()} again).
   */
  public void applyToAllWindows() {
    if (vars.isEmpty()) {
      Preconditions.LOG.warning(
          "ThemeManager.applyToAllWindows: no variables defined — use preset() or base()/accent() before applyToAllWindows()");
      return;
    }

    String newStyle = buildStyleString();
    boolean isDark = isColorDark(vars.getOrDefault("-fx-base", "#ececec"));

    for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
      if (window instanceof javafx.stage.Stage stage) {
        Scene sc = stage.getScene();
        if (sc == null || sc.getRoot() == null) {
          continue;
        }
        Node root = sc.getRoot();
        root.setStyle(newStyle);
        root.getStyleClass().remove("dark");
        if (isDark) {
          root.getStyleClass().add("dark");
        }
        forceStyleRefresh(root);
        applyToStage(stage, isDark);
      }
    }
  }

  /**
   * Applies the theme to a specific node (external scope)
   *
   * <p>Forces style refresh on the scoped node tree.
   */
  public void applyTo(Node node) {
    if (vars.isEmpty()) {
      return;
    }
    boolean isDark = isColorDark(vars.getOrDefault("-fx-base", "#ececec"));
    node.setStyle(buildStyleString(isDark));

    node.getStyleClass().remove("dark");
    if (isDark) {
      node.getStyleClass().add("dark");
    }

    // CRITICAL FIX: Force style refresh on scoped subtree
    forceStyleRefresh(node);
  }

  /** Removes the theme and restores the Modena defaults. */
  public void reset() {
    Node target = resolveTarget();
    if (target != null) {
      target.setStyle("");
      target.getStyleClass().remove("dark");
    }
  }

  // Convenience static methods
  /** Toggles dark ↔ light */
  public static void toggle(Scene scene) {
    if (isDark(scene)) {
      forScene(scene).light().apply();
    } else {
      forScene(scene).dark().apply();
    }
  }

  /** Is the current theme dark? */
  public static boolean isDark(Scene scene) {
    return scene.getRoot().getStyleClass().contains("dark");
  }

  /** Cycles through predefined themes in order */
  public static void cyclePreset(Scene scene) {
    List<String> themes = availableThemes();
    String style = scene.getRoot().getStyle();

    // Primary source: the last preset applied by this ThemeManager. It is exact even if two
    // presets shared the same base color (token matching would be ambiguous in that case).
    int active = -1;
    if (lastPresetName != null) {
      active = themes.indexOf(lastPresetName);
    }

    // Fallback: scenes not managed by this ThemeManager (e.g. styled manually or restored with
    // loadTheme). Pick the preset with the most distinctive tokens present in the style
    // (base + accent + background); ties keep the first entry in the list.
    if (active < 0 && style != null && !style.isEmpty()) {
      int bestMatches = 0;
      for (int i = 0; i < themes.size(); i++) {
        ThemeVars t = PRESETS.get(themes.get(i));
        int matches = 0;
        if (style.contains(t.base())) matches++;
        if (style.contains(t.accent())) matches++;
        if (style.contains(t.bg())) matches++;
        if (matches > bestMatches) {
          bestMatches = matches;
          active = i;
        }
      }
    }

    int next = (active >= 0) ? (active + 1) % themes.size() : 0;
    lastPresetName = themes.get(next);
    forScene(scene).preset(themes.get(next)).apply();
  }

  // Internal methods
  private Node resolveTarget() {
    if (scopeNode != null) {
      return scopeNode;
    }
    if (scene != null) {
      return scene.getRoot();
    }
    return null;
  }

  /**
   * Builds the inline style for the root (or scoped node): the Modena variables collected by the
   * builder plus the {@link ThemeTokens} of the resolved mode, so component stylesheets that use
   * {@code -tw-*} lookups follow the theme.
   */
  private String buildStyleString(boolean dark) {
    StringBuilder sb = new StringBuilder();
    for (var e : vars.entrySet()) {
      sb.append(e.getKey()).append(": ").append(e.getValue()).append("; ");
    }
    sb.append(ThemeTokens.toStyle(dark));
    return sb.toString().trim();
  }

  private void applyAnimated(Node target, String newStyle, boolean isDark) {
    // Opacity animation for smooth transition
    Timeline tl =
        new Timeline(
            new KeyFrame(
                Duration.ZERO, new KeyValue(target.opacityProperty(), 1.0, Interpolator.EASE_BOTH)),
            new KeyFrame(
                Duration.millis(animDurationMs / 2.0),
                new KeyValue(target.opacityProperty(), 0.85, Interpolator.EASE_BOTH)),
            new KeyFrame(
                Duration.millis(animDurationMs / 2.0 + 1),
                e -> {
                  // Tokens and the .dark class change together in the same KeyFrame
                  target.setStyle(newStyle);
                  target.getStyleClass().remove("dark");
                  if (isDark) {
                    target.getStyleClass().add("dark");
                  }
                }),
            new KeyFrame(
                Duration.millis(animDurationMs),
                new KeyValue(target.opacityProperty(), 1.0, Interpolator.EASE_BOTH)));
    tl.play();
  }

  /**
   * Returns whether the given value is exactly the base color of one of the registered presets.
   * Lets {@link #apply()} recognize when the builder corresponds to a pure preset.
   */
  private static boolean isPresetName(String value) {
    return value != null && PRESETS.containsKey(value.toLowerCase(Locale.ROOT));
  }

  /** Checks whether a base color matches the one of any registered preset. */
  private static boolean baseMatchesPreset(String baseColor) {
    if (baseColor == null) {
      return false;
    }
    for (ThemeVars t : PRESETS.values()) {
      if (t.base().equalsIgnoreCase(baseColor.trim())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determines whether a color is considered dark by applying the W3C relative luminance
   * (threshold 0.4) over its normalized RGB components.
   *
   * <p>Uses {@link javafx.scene.paint.Color#web(String)}, which supports CSS color names
   * ({@code "red"}), 3/4/6/8-digit hex values and {@code rgb()/rgba()} functions. The previous
   * regex ({@code [^0-9a-fA-F]}) turned {@code "rgb(30,30,30)"} into {@code "303030"} by accident
   * and reduced {@code "red"} to an empty string, misclassifying it as light.
   * Any unparseable value returns {@code false} (light), same as before.
   */
  private boolean isColorDark(String value) {
    try {
      javafx.scene.paint.Color color = javafx.scene.paint.Color.web(value.trim());
      double luminance =
          0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
      return luminance < 0.4;
    } catch (Exception e) {
      return false;
    }
  }

  // THEME PERSISTENCE — java.util.prefs.Preferences
  // Themes are saved in OS preferences and survive across sessions.
  /**
   * Saves the current theme of the scene to user preferences.
   *
   * <pre>
   * ThemeManager.saveTheme(scene, "myapp.mainWindow");
   * </pre>
   *
   * @param scene Scene whose theme to save
   * @param key unique key (e.g., "myapp.theme"). It is recommended to use the app name.
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
   * Restores a previously saved theme with {@link #saveTheme}.
   *
   * <pre>
   * boolean loaded = ThemeManager.loadTheme(scene, "myapp.mainWindow");
   * if (!loaded)
   *     ThemeManager.of(scene).preset("dark").apply(); // fallback
   * </pre>
   *
   * @param scene Scene to apply the theme to
   * @param key key used in {@link #saveTheme}
   * @return {@code true} if the saved theme was found and applied
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
      // Themes saved before the semantic tokens existed do not contain them: add them so
      // component stylesheets follow the restored theme.
      final String restoredStyle =
          style.contains(ThemeTokens.SURFACE + ":") ? style : style + " " + ThemeTokens.toStyle(dark);
      Platform.runLater(
          () -> {
            if (scene.getRoot() != null) {
              scene.getRoot().setStyle(restoredStyle.trim());

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

  // CRITICAL FIX: Force style refresh helpers

  /**
   * Forces a complete style refresh on a node and all its descendants.
   *
   * <p>JavaFX caches computed styles for performance. When theme variables change, we need to
   * invalidate this cache to ensure all components pick up the new values.
   *
   * <p>This method:
   *
   * <ol>
   *   <li>Triggers applyCss() on the node tree (forces style recalculation)
   *   <li>Requests layout on Parent nodes (ensures proper sizing with new styles)
   *   <li>Schedules a second pass on next frame (catches lazy-loaded components)
   * </ol>
   */
  private static void forceStyleRefresh(Node root) {
    if (root == null) return;

    // Pass 1: Immediate refresh
    safeApplyCss(root);
    if (root instanceof javafx.scene.Parent) {
      ((javafx.scene.Parent) root).requestLayout();
    }

    // Refresh all descendants
    refreshDescendants(root);

    // Pass 2: Deferred refresh (catches components that load lazily)
    Platform.runLater(
        () -> {
          safeApplyCss(root);
          refreshDescendants(root);
        });
  }

  /**
   * Safely applies CSS to a node, absorbing the failures JavaFX can raise when {@code applyCss()}
   * runs before the node is fully attached to the scene graph.
   *
   * <p>JavaFX's internal CSS processing does not only throw {@link NullPointerException}. When the
   * node tree is in a transient state, {@code StyleMap.getCascadingStyles} can fail with an {@link
   * AssertionError}. {@link AssertionError} is an {@link Error}, not an {@link Exception}, so a
   * {@code catch (NullPointerException)} block does not contain it: the error escaped {@code
   * safeApplyCss}, aborted the deferred refresh scheduled by {@link #forceStyleRefresh} and
   * surfaced on the JavaFX Application Thread. Containing both {@link RuntimeException} and {@link
   * Error} keeps the refresh best effort as documented.
   *
   * @param node the node whose CSS should be refreshed; ignored when {@code null}
   */
  private static void safeApplyCss(Node node) {
    if (node == null) return;
    try {
      node.applyCss();
    } catch (RuntimeException | Error e) {
      // The node may not be fully initialized yet. This is expected during scene graph
      // transitions, so the failure is logged at FINE level and must never propagate to the
      // caller or to the JavaFX Application Thread.
      Preconditions.LOG.log(java.util.logging.Level.FINE, "ThemeManager.safeApplyCss: skipped.", e);
    }
  }

  /** Recursively applies CSS and requests layout on all descendant nodes. */
  private static void refreshDescendants(Node node) {
    if (node instanceof javafx.scene.Parent parent) {
      for (Node child : parent.getChildrenUnmodifiable()) {
        safeApplyCss(child);
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
   * <p>On macOS and Windows, JavaFX allows styling the native window decorations. This method
   * applies appropriate styling based on the theme mode.
   *
   * <p>Note: This only works when the Stage is showing and uses native decorations.
   *
   * @param stage the Stage to style
   * @param isDark whether the current theme is dark mode
   */
  private static void applyToStage(javafx.stage.Stage stage, boolean isDark) {
    if (stage == null || !stage.isShowing()) return;

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
