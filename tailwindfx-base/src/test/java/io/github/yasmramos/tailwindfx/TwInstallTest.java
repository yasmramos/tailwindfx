package io.github.yasmramos.tailwindfx;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Unit tests for TwInstall class.
 */
public class TwInstallTest extends ApplicationTest {

  @Override
  public void start(Stage stage) {
    Pane root = new Pane();
    Scene scene = new Scene(root, 800, 600);
    stage.setScene(scene);
    stage.show();
  }

  @Test
  public void testInstall_AddsStylesheetToScene() {
    Scene scene = getScene();
    int initialSize = scene.getStylesheets().size();
    
    TwInstall.install(scene);
    
    assertTrue(scene.getStylesheets().size() > initialSize);
  }

  @Test
  public void testInstallMinimal_AddsBaseCss() {
    Scene scene = getScene();
    int initialSize = scene.getStylesheets().size();
    
    TwInstall.installMinimal(scene);
    
    assertTrue(scene.getStylesheets().size() > initialSize);
  }

  @Test
  public void testInstallBase_GeneratesAndInstallsBaseCss() {
    Scene scene = getScene();
    int initialSize = scene.getStylesheets().size();
    
    TwInstall.installBase(scene);
    
    assertTrue(scene.getStylesheets().size() > initialSize);
  }

  @Test
  public void testInstallWithStage_InstallsCorrectly() {
    Stage stage = getStage();
    Scene scene = stage.getScene();
    int initialSize = scene.getStylesheets().size();
    
    TwInstall.install(scene, stage);
    
    assertTrue(scene.getStylesheets().size() > initialSize);
  }

  @Test
  public void testInstallMultipleTimes_NoDuplicates() {
    Scene scene = getScene();
    
    TwInstall.install(scene);
    int firstSize = scene.getStylesheets().size();
    
    TwInstall.install(scene);
    int secondSize = scene.getStylesheets().size();
    
    assertEquals(firstSize, secondSize);
  }
}
