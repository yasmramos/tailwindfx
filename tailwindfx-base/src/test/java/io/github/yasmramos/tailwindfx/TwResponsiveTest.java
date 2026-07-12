package io.github.yasmramos.tailwindfx;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.breakpoint.BreakpointManager;
import io.github.yasmramos.tailwindfx.responsive.ResponsiveNode;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

class TwResponsiveTest extends ApplicationTest {

  private Scene scene;
  private StackPane root;
  private Stage stage;

  @Override
  public void start(Stage stage) {
    this.stage = stage;
    root = new StackPane();
    scene = new Scene(root, 800, 600);
    stage.setScene(scene);
    stage.show();
  }

  @Test
  void testOnStageWithScene() {
    ResponsiveNode responsiveNode = TwResponsive.on(stage);
    assertNotNull(responsiveNode);
  }

  @Test
  void testOnStageWithoutSceneThrowsException() {
    // Create a stage without attaching it to the JavaFX application thread properly
    // We need to use interact to ensure we're on the FX thread
    interact(
        () -> {
          Stage emptyStage = new Stage();
          // Stage without scene should throw IllegalArgumentException
          assertThrows(
              IllegalArgumentException.class,
              () -> {
                TwResponsive.on(emptyStage);
              });
        });
  }

  @Test
  void testOnRegion() {
    ResponsiveNode responsiveNode = TwResponsive.on(root);
    assertNotNull(responsiveNode);
  }

  @Test
  void testForStage() {
    BreakpointManager breakpointManager = TwResponsive.forStage(stage);
    assertNotNull(breakpointManager);
  }

  @Test
  void testSingletonInstance() throws Exception {
    // Verify that the class has a singleton pattern
    var instanceField = TwResponsive.class.getDeclaredField("INSTANCE");
    instanceField.setAccessible(true);
    Object instance = instanceField.get(null);
    assertNotNull(instance);
    assertTrue(instance instanceof TwResponsive);
  }

  @Test
  void testPrivateConstructor() throws Exception {
    var constructor = TwResponsive.class.getDeclaredConstructor();
    assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    // Utility classes with private constructors should not throw when instantiated via reflection
    // The constructor is private to prevent instantiation, but reflection can bypass this
    Object instance = constructor.newInstance();
    assertNotNull(instance);
  }

  @Test
  void testResponsiveNodeInstallation() {
    ResponsiveNode responsiveNode = TwResponsive.on(stage);

    assertNotNull(responsiveNode);
    // Verify that the responsive node is properly installed
    assertEquals(-1, responsiveNode.activeBreakpoint());
  }

  @Test
  void testBreakpointManagerFromStage() {
    BreakpointManager manager = TwResponsive.forStage(stage);

    assertNotNull(manager);
    assertNotNull(manager.current());
  }
}
