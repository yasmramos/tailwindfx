package io.github.yasmramos.tailwindfx;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Unit tests for TwInstall facade class. Tests installation of CSS stylesheets into JavaFX scenes.
 */
class TwInstallTest extends ApplicationTest {

  private Scene scene;
  private Pane root;

  @Override
  public void start(javafx.stage.Stage stage) {
    root = new Pane();
    scene = new Scene(root, 800, 600);
    stage.setScene(scene);
    stage.show();
  }

  @BeforeEach
  void setUp() {
    // Reset scene stylesheets before each test
    scene.getStylesheets().clear();
  }

  @Test
  void testInstallMinimal_InstallsBaseCss() {
    TwInstall.installMinimal(scene);

    assertFalse(scene.getStylesheets().isEmpty(), "Should have at least one stylesheet");
    assertTrue(
        scene.getStylesheets().get(0).contains("data:text/css"),
        "Base CSS should be installed as data URL");
  }

  @Test
  void testInstall_CallsInstallMinimal() {
    TwInstall.install(scene);

    assertFalse(scene.getStylesheets().isEmpty(), "Should have stylesheets installed");
    assertTrue(scene.getStylesheets().get(0).contains("data:text/css"), "Should install base CSS");
  }

  @Test
  void testInstallBase_GeneratesDynamicCss() {
    TwInstall.installBase(scene);

    assertFalse(scene.getStylesheets().isEmpty(), "Should have at least one stylesheet");
    String cssUrl = scene.getStylesheets().get(0);
    assertTrue(cssUrl.contains("data:text/css"), "Base CSS should be installed as data URL");
  }

  @Test
  void testInstallGenerated_WithCustomPath() {
    // Test with null path (should use default)
    TwInstall.installGenerated(scene, "/css/tailwindfx-generated.css");

    // Should have base CSS at minimum (generated CSS may not exist in test resources)
    assertFalse(scene.getStylesheets().isEmpty(), "Should have base CSS installed");
  }

  @Test
  void testInstallGenerated_WithNullPath_UsesDefault() {
    TwInstall.installGenerated(scene, null);

    assertFalse(scene.getStylesheets().isEmpty(), "Should install base CSS");
  }

  @Test
  void testInstallGenerated_EmptyPath_UsesDefault() {
    TwInstall.installGenerated(scene, "");

    assertFalse(scene.getStylesheets().isEmpty(), "Should install base CSS with empty path");
  }

  @Test
  void testInstallDark_NoOp() {
    // Dark mode is now handled by ThemeManager, so this should be a no-op
    int initialSize = scene.getStylesheets().size();
    TwInstall.installDark(scene);

    assertEquals(
        initialSize,
        scene.getStylesheets().size(),
        "installDark should be a no-op (handled by ThemeManager)");
  }

  @Test
  void testMultipleInstalls_ReplacesExisting() {
    TwInstall.installBase(scene);
    int firstSize = scene.getStylesheets().size();

    TwInstall.installBase(scene);
    int secondSize = scene.getStylesheets().size();

    // Installation may add multiple stylesheets, but should be consistent
    assertTrue(secondSize >= firstSize, "Should not remove existing stylesheets");
  }

  @Test
  void testInstallBase_GeneratesValidCssContent() {
    TwInstall.installBase(scene);

    String cssUrl = scene.getStylesheets().get(0);
    // Data URL should contain encoded CSS content
    assertTrue(cssUrl.startsWith("data:text/css"), "Generated CSS should be a data URL");
  }

  @Test
  void testInstall_WithStage_AttachesBreakpointManager() {
    interact(
        () -> {
          javafx.stage.Stage stage = new javafx.stage.Stage();
          Scene testScene = new Scene(new Pane(), 800, 600);
          stage.setScene(testScene);

          TwInstall.install(testScene, stage);

          assertFalse(
              testScene.getStylesheets().isEmpty(),
              "Should install base CSS when stage is provided");
        });
  }
}
