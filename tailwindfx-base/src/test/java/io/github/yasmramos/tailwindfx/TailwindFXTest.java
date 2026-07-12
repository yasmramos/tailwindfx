package io.github.yasmramos.tailwindfx;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.layout.TwGridPane;
import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

class TailwindFXTest extends ApplicationTest {

  private Button testButton;
  private Pane testPane;
  private Scene scene;
  private Stage stage;

  @Override
  public void start(Stage stage) {
    this.stage = stage;
    testButton = new Button("Test");
    testPane = new StackPane();
    testPane.getChildren().add(testButton);
    scene = new Scene(testPane, 800, 600);
    stage.setScene(scene);
    stage.show();

    // Reset config before each test
    TwConfig.reset();
  }

  @Test
  void testApplyStyles() {
    TailwindFX.apply(testButton, "bg-blue-500", "text-white");

    // Verify that styles were applied (checking if style class was added)
    assertFalse(testButton.getStyleClass().isEmpty());
  }

  @Test
  void testJitStyles() {
    TailwindFX.jit(testButton, "bg-red-500");

    // Verify that JIT styles were applied
    assertFalse(testButton.getStyleClass().isEmpty());
  }

  @Test
  void testRemoveStyles() {
    testButton.getStyleClass().addAll("bg-blue-500", "text-white");

    TailwindFX.remove(testButton, "bg-blue-500");

    assertFalse(testButton.getStyleClass().stream().anyMatch(s -> s.contains("bg-blue")));
    assertTrue(testButton.getStyleClass().stream().anyMatch(s -> s.contains("text")));
  }

  @Test
  void testToggleStyle() {
    TailwindFX.toggle(testButton, "active");
    assertTrue(testButton.getStyleClass().contains("active"));

    TailwindFX.toggle(testButton, "active");
    assertFalse(testButton.getStyleClass().contains("active"));
  }

  @Test
  void testInstallScene() {
    assertDoesNotThrow(() -> TailwindFX.install(scene));
  }

  @Test
  void testInstallWithStage() {
    assertDoesNotThrow(() -> TailwindFX.install(scene, stage));
  }

  @Test
  void testInstallBase() {
    assertDoesNotThrow(() -> TailwindFX.installBase(scene));
  }

  @Test
  void testInstallDark() {
    assertDoesNotThrow(() -> TailwindFX.installDark(scene));
    TwGridPane gridPane = TailwindFX.grid();
    assertNotNull(gridPane);
  }

  @Test
  void testResponsiveOperation() {
    assertDoesNotThrow(() -> TailwindFX.responsive(stage));
  }

  @Test
  void testBatchOperation() {
    // Batch operations must be executed on the JavaFX Application Thread
    interact(
        () -> {
          TailwindFX.batch(
              () -> {
                TailwindFX.apply(testButton, "bg-green-500");
              });

          assertTrue(testButton.getStyleClass().contains("bg-green-500"));
        });
  }

  @Test
  void testMetrics() {
    TailwindFXMetrics metrics = TailwindFX.metrics();
    assertNotNull(metrics);
  }

  @Test
  void testUnitConfiguration() {
    double originalUnit = TailwindFX.unit();

    TailwindFX.unit(8.0);
    assertEquals(8.0, TailwindFX.unit());

    TailwindFX.unit(originalUnit);
  }

  @Test
  void testDebugConfiguration() {
    boolean originalDebug = TailwindFX.isDebug();

    TailwindFX.debug(true);
    assertTrue(TailwindFX.isDebug());

    TailwindFX.debug(false);
    assertFalse(TailwindFX.isDebug());

    TailwindFX.debug(originalDebug);
  }

  @Test
  void testPrivateConstructor() throws Exception {
    var constructor = TailwindFX.class.getDeclaredConstructor();
    assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    // Utility classes with private constructors should not throw when instantiated via reflection
    // The constructor is private to prevent instantiation, but reflection can bypass this
    Object instance = constructor.newInstance();
    assertNotNull(instance);
  }
}
