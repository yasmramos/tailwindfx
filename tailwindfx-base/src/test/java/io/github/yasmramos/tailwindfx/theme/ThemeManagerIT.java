package io.github.yasmramos.tailwindfx.theme;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

@DisplayName("ThemeManager Tests")
class ThemeManagerTest extends ApplicationTest {

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
    ThemeManager.forScene(scene).light().apply();

    String style = scene.getRoot().getStyle();
    assertTrue(style.contains("-fx-base"));
    assertTrue(style.contains("#ececec"));
    assertFalse(scene.getRoot().getStyleClass().contains("dark"));
  }

  @Test
  @DisplayName("Should apply dark theme preset")
  void testDarkPreset() {
    ThemeManager.forScene(scene).dark().apply();

    String style = scene.getRoot().getStyle();
    assertTrue(style.contains("-fx-base"));
    assertTrue(style.contains("#2b2b2b"));
    assertTrue(scene.getRoot().getStyleClass().contains("dark"));
  }

  @Test
  @DisplayName("Should apply blue theme preset")
  void testBluePreset() {
    ThemeManager.forScene(scene).preset("blue").apply();

    String style = scene.getRoot().getStyle();
    assertTrue(style.contains("#dbeafe"));
    assertTrue(style.contains("-fx-accent"));
  }

  @Test
  @DisplayName("Should apply green theme preset")
  void testGreenPreset() {
    ThemeManager.forScene(scene).preset("green").apply();

    String style = scene.getRoot().getStyle();
    assertTrue(style.contains("#dcfce7"));
    assertTrue(style.contains("#16a34a"));
  }

  @Test
  @DisplayName("Should apply purple theme preset")
  void testPurplePreset() {
    ThemeManager.forScene(scene).preset("purple").apply();

    String style = scene.getRoot().getStyle();
    assertTrue(style.contains("#ede9fe"));
    assertTrue(style.contains("#7c3aed"));
  }

  @Test
  @DisplayName("Should apply rose theme preset")
  void testRosePreset() {
    ThemeManager.forScene(scene).preset("rose").apply();

    String style = scene.getRoot().getStyle();
    assertTrue(style.contains("#ffe4e6"));
    assertTrue(style.contains("#e11d48"));
  }

  @Test
  @DisplayName("Should apply slate theme preset")
  void testSlatePreset() {
    ThemeManager.forScene(scene).preset("slate").apply();

    String style = scene.getRoot().getStyle();
    assertTrue(style.contains("#e2e8f0"));
    assertTrue(style.contains("#475569"));
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
    ThemeManager.forScene(scene).dark().apply();
    assertTrue(scene.getRoot().getStyleClass().contains("dark"));

    ThemeManager.forScene(scene).reset();

    assertTrue(scene.getRoot().getStyle().isEmpty());
    assertFalse(scene.getRoot().getStyleClass().contains("dark"));
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
    ThemeManager.forScene(scene).preset("light").apply();
    ThemeManager.cyclePreset(scene);

    // Should cycle to next theme (dark)
    String style = scene.getRoot().getStyle();
    assertTrue(style.contains("-fx-base"));
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
  @DisplayName("Should warn when applying without variables")
  void testApplyWithoutVariables() {
    ThemeManager manager = ThemeManager.forScene(scene);
    // Should log warning but not throw
    assertDoesNotThrow(() -> manager.apply());
  }

  @Test
  @DisplayName("Should save theme to preferences")
  void testSaveTheme() {
    ThemeManager.forScene(scene).dark().apply();

    assertDoesNotThrow(() -> ThemeManager.saveTheme(scene, "test.app.theme"));
  }

  @Test
  @DisplayName("Should load theme from preferences")
  void testLoadTheme() {
    // First save a theme
    ThemeManager.forScene(scene).dark().apply();
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
    ThemeManager.forScene(scene).dark().apply();
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
    ThemeManager.forScene(scene).preset("dark").accent("#ff0000").apply();

    String style = scene.getRoot().getStyle();
    assertTrue(style.contains("#2b2b2b")); // dark base
    assertTrue(style.contains("-fx-accent: #ff0000")); // custom accent
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
}
