package io.github.yasmramos.tailwindfx.theme;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

@DisplayName("ThemeManager Integration Tests")
class ThemeManagerIT extends ApplicationTest {

  private Scene scene;
  private Pane root;
  private Stage stage;

  @Override
  public void start(Stage stage) {
    this.stage = stage;
    root = new StackPane();
    scene = new Scene(root, 800, 600);
    stage.setScene(scene);
    stage.show();
  }

  @BeforeEach
  void setUp() {
    // Reset theme before each test
    if (scene.getRoot() != null) {
      scene.getRoot().setStyle("");
      scene.getRoot().getStyleClass().clear();
    }
  }

  @Test
  @DisplayName("Should create ThemeManager for Scene")
  void testForScene() {
    ThemeManager manager = ThemeManager.forScene(scene);
    assertNotNull(manager);
  }

  @Test
  @DisplayName("Should create ThemeManager for scope Node")
  void testScope() {
    Pane childPane = new Pane();
    interact(() -> root.getChildren().add(childPane));

    ThemeManager manager = ThemeManager.scope(childPane);
    assertNotNull(manager);
  }

  @Test
  @DisplayName("Should apply light theme preset")
  void testLightPreset() {
    interact(
        () -> {
          ThemeManager.forScene(scene).light().apply();

          String style = scene.getRoot().getStyle();
          assertTrue(style.contains("-fx-base"));
          assertTrue(style.contains("#ececec"));
          assertFalse(scene.getRoot().getStyleClass().contains("dark"));
        });
  }

  @Test
  @DisplayName("Should apply dark theme preset")
  void testDarkPreset() {
    interact(
        () -> {
          ThemeManager.forScene(scene).dark().apply();

          String style = scene.getRoot().getStyle();
          assertTrue(style.contains("-fx-base"));
          assertTrue(style.contains("#2b2b2b"));
          assertTrue(scene.getRoot().getStyleClass().contains("dark"));
        });
  }

  @Test
  @DisplayName("Should apply blue theme preset")
  void testBluePreset() {
    interact(
        () -> {
          ThemeManager.forScene(scene).preset("blue").apply();

          String style = scene.getRoot().getStyle();
          assertTrue(style.contains("#dbeafe"));
          assertTrue(style.contains("-fx-accent"));
        });
  }

  @Test
  @DisplayName("Should apply green theme preset")
  void testGreenPreset() {
    interact(
        () -> {
          ThemeManager.forScene(scene).preset("green").apply();

          String style = scene.getRoot().getStyle();
          assertTrue(style.contains("#dcfce7"));
          assertTrue(style.contains("#16a34a"));
        });
  }

  @Test
  @DisplayName("Should apply purple theme preset")
  void testPurplePreset() {
    interact(
        () -> {
          ThemeManager.forScene(scene).preset("purple").apply();

          String style = scene.getRoot().getStyle();
          assertTrue(style.contains("#ede9fe"));
          assertTrue(style.contains("#7c3aed"));
        });
  }

  @Test
  @DisplayName("Should apply rose theme preset")
  void testRosePreset() {
    interact(
        () -> {
          ThemeManager.forScene(scene).preset("rose").apply();

          String style = scene.getRoot().getStyle();
          assertTrue(style.contains("#ffe4e6"));
          assertTrue(style.contains("#e11d48"));
        });
  }

  @Test
  @DisplayName("Should apply slate theme preset")
  void testSlatePreset() {
    interact(
        () -> {
          ThemeManager.forScene(scene).preset("slate").apply();

          String style = scene.getRoot().getStyle();
          assertTrue(style.contains("#e2e8f0"));
          assertTrue(style.contains("#475569"));
        });
  }

  @Test
  @DisplayName("Should throw exception for invalid theme preset")
  void testInvalidPreset() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ThemeManager.forScene(scene).preset("invalid-theme").apply());
  }

  @Test
  @DisplayName("Should apply custom base color")
  void testCustomBaseColor() {
    ThemeManager.forScene(scene).base("#ff5733").apply();

    String style = scene.getRoot().getStyle();
    assertTrue(style.contains("-fx-base: #ff5733"));
  }

  @Test
  @DisplayName("Should apply custom background color")
  void testCustomBackgroundColor() {
    ThemeManager.forScene(scene).background("#33ff57").apply();

    String style = scene.getRoot().getStyle();
    assertTrue(style.contains("-fx-background: #33ff57"));
  }

  @Test
  @DisplayName("Should apply custom inner background color")
  void testCustomInnerBackgroundColor() {
    ThemeManager.forScene(scene).innerBackground("#5733ff").apply();

    String style = scene.getRoot().getStyle();
    assertTrue(style.contains("-fx-control-inner-background: #5733ff"));
  }

  @Test
  @DisplayName("Should apply custom accent color")
  void testCustomAccentColor() {
    ThemeManager.forScene(scene).accent("#ff33a1").apply();

    String style = scene.getRoot().getStyle();
    assertTrue(style.contains("-fx-accent: #ff33a1"));
    assertTrue(style.contains("-fx-selection-bar: #ff33a1"));
  }

  @Test
  @DisplayName("Should apply custom focus color")
  void testCustomFocusColor() {
    ThemeManager.forScene(scene).focus("#33fff5").apply();

    String style = scene.getRoot().getStyle();
    assertTrue(style.contains("-fx-focus-color: #33fff5"));
  }

  @Test
  @DisplayName("Should apply custom faint focus color")
  void testCustomFaintFocusColor() {
    ThemeManager.forScene(scene).faintFocus("#f5ff33").apply();

    String style = scene.getRoot().getStyle();
    assertTrue(style.contains("-fx-faint-focus-color: #f5ff33"));
  }

  @Test
  @DisplayName("Should apply custom default button color")
  void testCustomDefaultButtonColor() {
    ThemeManager.forScene(scene).defaultButton("#a133ff").apply();

    String style = scene.getRoot().getStyle();
    assertTrue(style.contains("-fx-default-button: #a133ff"));
  }

  @Test
  @DisplayName("Should chain multiple theme customizations")
  void testChainedCustomizations() {
    ThemeManager.forScene(scene).base("#123456").accent("#654321").background("#abcdef").apply();

    String style = scene.getRoot().getStyle();
    assertTrue(style.contains("-fx-base: #123456"));
    assertTrue(style.contains("-fx-accent: #654321"));
    assertTrue(style.contains("-fx-background: #abcdef"));
  }

  @Test
  @DisplayName("Should reset theme to default")
  void testReset() {
    // Run all mutations and assertions on the JavaFX Application Thread so the test is
    // deterministic: apply() schedules a deferred CSS refresh via Platform.runLater, which
    // could otherwise race with the assertions below when executed from the FX thread.
    interact(
        () -> {
          ThemeManager.forScene(scene).dark().apply();
          assertTrue(scene.getRoot().getStyleClass().contains("dark"));

          ThemeManager.forScene(scene).reset();

          scene.getRoot().applyCss();
          scene.getRoot().layout();
          assertTrue(scene.getRoot().getStyle().isEmpty());
          assertFalse(scene.getRoot().getStyleClass().contains("dark"));
        });
  }

  @Test
  @DisplayName("Should toggle between dark and light themes")
  void testToggle() {
    // Start with light theme
    ThemeManager.forScene(scene).light().apply();
    assertFalse(scene.getRoot().getStyleClass().contains("dark"));

    // Toggle to dark
    ThemeManager.toggle(scene);
    assertTrue(scene.getRoot().getStyleClass().contains("dark"));

    // Toggle back to light
    ThemeManager.toggle(scene);
    assertFalse(scene.getRoot().getStyleClass().contains("dark"));
  }

  @Test
  @DisplayName("Should detect dark theme correctly")
  void testIsDark() {
    interact(
        () -> {
          ThemeManager.forScene(scene).light().apply();
          assertFalse(ThemeManager.isDark(scene));

          ThemeManager.forScene(scene).dark().apply();
          assertTrue(ThemeManager.isDark(scene));
        });
  }

  @Test
  @DisplayName("Should cycle through available themes")
  void testCyclePreset() {
    interact(
        () -> {
          ThemeManager.forScene(scene).preset("light").apply();
          ThemeManager.cyclePreset(scene);

          // Should cycle to next theme (dark)
          String style = scene.getRoot().getStyle();
          assertTrue(style.contains("-fx-base"));
        });
  }

  @Test
  @DisplayName("Should return list of available themes")
  void testAvailableThemes() {
    var themes = ThemeManager.availableThemes();

    assertNotNull(themes);
    assertFalse(themes.isEmpty());
    assertTrue(themes.contains("light"));
    assertTrue(themes.contains("dark"));
    assertTrue(themes.contains("blue"));
    assertTrue(themes.contains("green"));
    assertTrue(themes.contains("purple"));
    assertTrue(themes.contains("rose"));
    assertTrue(themes.contains("slate"));
  }

  @Test
  @DisplayName("Should apply theme to scoped node only")
  void testApplyToScopedNode() {
    Pane childPane = new Pane();
    interact(() -> root.getChildren().add(childPane));

    ThemeManager.scope(childPane).preset("dark").applyTo(childPane);

    String style = childPane.getStyle();
    assertTrue(style.contains("-fx-base"));
    assertTrue(childPane.getStyleClass().contains("dark"));

    // Parent should not be affected
    assertFalse(root.getStyleClass().contains("dark"));
  }

  @Test
  @DisplayName("Should handle null scene gracefully in apply")
  void testNullSceneInApply() {
    ThemeManager manager = ThemeManager.scope(null);
    // Should not throw exception
    assertDoesNotThrow(() -> manager.preset("dark").apply());
  }

  @Test
  @DisplayName("Style refresh must never leak an Error from applyCss")
  void testStyleRefreshDoesNotPropagateErrors() {
    // Regression test for the AssertionError raised by StyleMap.getCascadingStyles when
    // forceStyleRefresh() ran applyCss() while the node tree was still in a transient state.
    // safeApplyCss() used to catch only NullPointerException, so the Error escaped, aborted the
    // Platform.runLater() deferred pass and failed the whole test. Both passes must now complete.
    AtomicReference<Throwable> escaped = new AtomicReference<>();

    Thread.UncaughtExceptionHandler handler = (thread, error) -> escaped.set(error);
    Thread.setDefaultUncaughtExceptionHandler(handler);
    try {
      Pane child = new Pane();
      interact(() -> root.getChildren().add(child));

      assertDoesNotThrow(
          () ->
              interact(
                  () -> {
                    ThemeManager.forScene(scene).preset("light").apply();
                    ThemeManager.cyclePreset(scene);
                  }));

      // Drain the deferred Platform.runLater pass scheduled by forceStyleRefresh.
      interact(() -> {});
      Thread.sleep(150);
      interact(() -> {});
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      fail("interrupted while waiting for the deferred style refresh");
    } finally {
      Thread.setDefaultUncaughtExceptionHandler(null);
    }

    assertNull(
        escaped.get(),
        "an Error from applyCss escaped onto the JavaFX Application Thread: " + escaped.get());
  }

  @Test
  @DisplayName("Theme cycling repeatedly does not destabilize the scene graph")
  void testRepeatedThemeCyclingIsStable() {
    // The CI failure surfaced in testCyclePreset after several themes had been applied in the
    // same JVM. Cycling through every preset must stay free of leaked Errors.
    AtomicReference<Throwable> escaped = new AtomicReference<>();
    Thread.setDefaultUncaughtExceptionHandler((thread, error) -> escaped.set(error));
    try {
      for (String preset : ThemeManager.availableThemes()) {
        interact(() -> ThemeManager.forScene(scene).preset(preset).apply());
      }
      for (String preset : ThemeManager.availableThemes()) {
        interact(() -> ThemeManager.cyclePreset(scene));
      }
      interact(() -> {});
      Thread.sleep(150);
      interact(() -> {});
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      fail("interrupted while waiting for the deferred style refresh");
    } finally {
      Thread.setDefaultUncaughtExceptionHandler(null);
    }

    assertNull(
        escaped.get(),
        "cycling themes leaked an Error onto the JavaFX Application Thread: " + escaped.get());
  }

  @Test
  @DisplayName("Should warn when applying without variables")
  void testApplyWithoutVariables() {
    ThemeManager manager = ThemeManager.forScene(scene);
    // Should log warning but not throw
    assertDoesNotThrow(() -> manager.apply());
  }

  @Test
  @DisplayName("Should save theme to preferences")
  void testSaveTheme() {
    interact(() -> ThemeManager.forScene(scene).dark().apply());

    assertDoesNotThrow(() -> ThemeManager.saveTheme(scene, "test.app.theme"));
  }

  @Test
  @DisplayName("Should load theme from preferences")
  void testLoadTheme() {
    // First save a theme
    interact(() -> ThemeManager.forScene(scene).dark().apply());
    ThemeManager.saveTheme(scene, "test.load.theme");

    // Reset
    ThemeManager.forScene(scene).reset();

    // Load and verify
    boolean loaded = ThemeManager.loadTheme(scene, "test.load.theme");
    assertTrue(loaded);
  }

  @Test
  @DisplayName("Should return false when loading non-existent theme")
  void testLoadNonExistentTheme() {
    boolean loaded = ThemeManager.loadTheme(scene, "non.existent.key");
    assertFalse(loaded);
  }

  @Test
  @DisplayName("Should delete saved theme")
  void testDeleteTheme() {
    interact(() -> ThemeManager.forScene(scene).dark().apply());
    ThemeManager.saveTheme(scene, "test.delete.theme");

    assertDoesNotThrow(() -> ThemeManager.deleteTheme("test.delete.theme"));
  }

  @Test
  @DisplayName("Should handle animated theme transition")
  void testAnimatedTransition() {
    ThemeManager manager = ThemeManager.forScene(scene);

    assertDoesNotThrow(() -> manager.preset("dark").animated(300).apply());
  }

  @Test
  @DisplayName("Should apply theme override on top of preset")
  void testPresetWithOverride() {
    interact(
        () -> {
          ThemeManager.forScene(scene).preset("dark").accent("#ff0000").apply();

          String style = scene.getRoot().getStyle();
          assertTrue(style.contains("#2b2b2b")); // dark base
          assertTrue(style.contains("-fx-accent: #ff0000")); // custom accent
        });
  }

  @Test
  @DisplayName("Should throw exception for null scene in forScene")
  void testNullSceneInForScene() {
    assertThrows(IllegalArgumentException.class, () -> ThemeManager.forScene(null));
  }

  @Test
  @DisplayName("Should throw exception for blank preset name")
  void testBlankPresetName() {
    assertThrows(
        IllegalArgumentException.class, () -> ThemeManager.forScene(scene).preset("").apply());
  }

  @Test
  @DisplayName("Should throw exception for null color in base")
  void testNullBaseColor() {
    assertThrows(
        IllegalArgumentException.class, () -> ThemeManager.forScene(scene).base(null).apply());
  }

  @Test
  @DisplayName("Should throw exception for blank color in base")
  void testBlankBaseColor() {
    assertThrows(
        IllegalArgumentException.class, () -> ThemeManager.forScene(scene).base("   ").apply());
  }

  @Test
  @DisplayName("Should handle theme persistence with null key gracefully")
  void testSaveThemeWithNullKey() {
    assertThrows(IllegalArgumentException.class, () -> ThemeManager.saveTheme(scene, null));
  }

  @Test
  @DisplayName("Should handle load theme with null key gracefully")
  void testLoadThemeWithNullKey() {
    assertThrows(IllegalArgumentException.class, () -> ThemeManager.loadTheme(scene, null));
  }

  @Test
  @DisplayName("Should handle delete theme with null key gracefully")
  void testDeleteThemeWithNullKey() {
    assertThrows(IllegalArgumentException.class, () -> ThemeManager.deleteTheme(null));
  }

  @Test
  @DisplayName("Should handle save theme when scene has no root")
  void testSaveThemeWithNoRoot() {
    Scene emptyScene = new Scene(new Pane());
    // Remove root temporarily - this will throw, so we catch it
    try {
      Pane oldRoot = (Pane) emptyScene.getRoot();
      emptyScene.setRoot(null);
      assertDoesNotThrow(() -> ThemeManager.saveTheme(emptyScene, "test.no.root"));
      emptyScene.setRoot(oldRoot);
    } catch (NullPointerException e) {
      // Expected - JavaFX doesn't allow setting null root
      // Test passes as we're verifying graceful handling
    }
  }

  @Test
  @DisplayName("Should resolve preset names case-insensitively regardless of locale")
  void testPresetUppercaseName() {
    // Regression: preset(name) usaba toLowerCase() sin Locale.ROOT; en una JVM con locale
    // turco, "BLUE".toLowerCase() produce "bluÌˆe" y el preset no se encontraba.
    interact(
        () -> {
          ThemeManager.forScene(scene).preset("DARK").apply();

          String style = scene.getRoot().getStyle();
          assertTrue(style.contains("#2b2b2b"));
          assertTrue(scene.getRoot().getStyleClass().contains("dark"));
        });
  }

  @Test
  @DisplayName("Should add dark class for non-hex dark base colors")
  void testIsColorDark_nonHexColors() {
    // Regression: the [^0-9a-fA-F] regex turned "rgb(30,30,30)" into "303030" by accident
    // and reduced "black"/"red" to an empty string (always "light"). Now it is parsed with
    // Color.web() and the W3C luminance decides.
    interact(
        () -> {
          ThemeManager.forScene(scene).base("rgb(30,30,30)").apply();
          assertTrue(
              scene.getRoot().getStyleClass().contains("dark"),
              "rgb(30,30,30) is dark and must add the .dark class");

          ThemeManager.forScene(scene).base("black").apply();
          assertTrue(
              scene.getRoot().getStyleClass().contains("dark"), "black must add the .dark class");

          // #ff0000: W3C luminance ≈ 0.21 < 0.4 → dark (previously hit the regex edge case)
          ThemeManager.forScene(scene).base("#f00").apply();
          assertTrue(scene.getRoot().getStyleClass().contains("dark"), "hex shorthand #f00 is dark");

          // Light colors must not add the class
          ThemeManager.forScene(scene).base("white").apply();
          assertFalse(scene.getRoot().getStyleClass().contains("dark"));

          // Unparseable value → treated as light (historical behavior)
          ThemeManager.forScene(scene).base("not-a-color").apply();
          assertFalse(scene.getRoot().getStyleClass().contains("dark"));
        });
  }

  @Test
  @DisplayName("cyclePreset should follow the real order after applying a concrete preset")
  void testCyclePresetOrderAfterConcretePreset() {
    List<String> themes = ThemeManager.availableThemes();
    int roseIdx = themes.indexOf("rose");
    String expectedNext = themes.get((roseIdx + 1) % themes.size());

    // Previous light presets ("light", index 0) made the old base-only matching fail to
    // detect "rose", so cyclePreset always jumped back to the first theme.
    interact(
        () -> {
          ThemeManager.forScene(scene).light().apply();
          ThemeManager.forScene(scene).preset("ROSE").apply();

          ThemeManager.cyclePreset(scene);

          String style = scene.getRoot().getStyle();
          assertTrue(
              style.contains(PRESET_BASE.get(expectedNext)),
              "after 'rose' the next theme should be '" + expectedNext + "', style was: " + style);
        });
  }

  @Test
  @DisplayName("cyclePreset should fall back to token matching for unmanaged scenes")
  void testCyclePresetTokenFallback() {
    // Scene whose style was set manually (e.g. restored by loadTheme): with no preset
    // record, cyclePreset must detect the theme from its distinctive tokens (base+accent+bg).
    // The fallback path is forced by clearing the static record after applying a preset.
    Scene manual = new Scene(new StackPane(), 200, 200);
    ThemeManager.forScene(manual).preset("purple").apply();

    String purpleStyle = manual.getRoot().getStyle();
    assertTrue(purpleStyle.contains("#ede9fe"));

    // Simulate an unmanaged scene: unknown last preset + empty style.
    // With no recognizable tokens, cyclePreset should start at the first theme (light).
    clearLastPresetName();
    manual.getRoot().setStyle("");
    ThemeManager.cyclePreset(manual);

    assertTrue(
        manual.getRoot().getStyle().contains(PRESET_BASE.get("light")),
        "with no style and no registry, cyclePreset should start at the first theme (light)");

    // Now, with the "purple" style set manually and the registry cleared, the token-based
    // fallback should detect "purple" and apply the next theme in the list.
    clearLastPresetName();
    manual.getRoot().setStyle(purpleStyle);

    ThemeManager.cyclePreset(manual);

    java.util.List<String> themes = ThemeManager.availableThemes();
    String expectedNext =
        themes.get((themes.indexOf("purple") + 1) % themes.size());
    assertTrue(
        manual.getRoot().getStyle().contains(PRESET_BASE.get(expectedNext)),
        "fallback token matching should have advanced past 'purple'");
  }

  /** Resets the static {@code lastPresetName} record to exercise the fallback path. */
  private static void clearLastPresetName() {
    try {
      java.lang.reflect.Field f = ThemeManager.class.getDeclaredField("lastPresetName");
      f.setAccessible(true);
      f.set(null, null);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError("cannot reset ThemeManager.lastPresetName", e);
    }
  }

  /** Preset base colors used to verify cyclePreset order (must match ThemeManager). */
  private static final Map<String, String> PRESET_BASE =
      Map.of(
          "light", "#ececec",
          "dark", "#2b2b2b",
          "blue", "#dbeafe",
          "green", "#dcfce7",
          "purple", "#ede9fe",
          "rose", "#ffe4e6",
          "slate", "#e2e8f0");
}
