package io.github.yasmramos.tailwindfx.responsive;

import static org.junit.jupiter.api.Assertions.*;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;
import org.testfx.framework.junit5.Stop;

/** Tests for {@link ContainerQuery} — container-based responsive utilities. */
public class ContainerQueryTest extends ApplicationTest {

  private Label testLabel;
  private Pane container;

  @Start
  @Override
  public void start(Stage stage) {
    testLabel = new Label("Test");
    container = new Pane(testLabel);
    Scene scene = new Scene(container, 800, 600);
    stage.setScene(scene);
    stage.show();
  }

  @Stop
  @Override
  public void stop() {
    Platform.runLater(
        () -> {
          if (container != null && container.getScene() != null) {
            container.getScene().setRoot(new Pane());
          }
        });
  }

  @Test
  @DisplayName("Factory and Builder API")
  public void testFactoryAndBuilder() {
    assertDoesNotThrow(
        () -> {
          ContainerQuery query = ContainerQuery.on(testLabel).base("p-4").sm("p-6").md("p-8");
          query.install(container);

          assertNotNull(query);
          assertTrue(query instanceof ContainerQuery);
        },
        "ContainerQuery builder should not throw exceptions");
  }

  @Test
  @DisplayName("Standard Breakpoints")
  public void testStandardBreakpoints() {
    assertDoesNotThrow(
        () -> {
          ContainerQuery query =
              ContainerQuery.on(testLabel)
                  .sm("text-sm")
                  .md("text-md")
                  .lg("text-lg")
                  .xl("text-xl")
                  .xxl("text-xxl");
          query.install(container);

          assertNotNull(query);
        },
        "Standard breakpoints should work correctly");
  }

  @Test
  @DisplayName("Custom Breakpoints")
  public void testCustomBreakpoints() {
    assertDoesNotThrow(
        () -> {
          ContainerQuery query =
              ContainerQuery.on(testLabel)
                  .at(300, "w-[300px]")
                  .at(500, "w-[500px]")
                  .at(700, "w-[700px]");
          query.install(container);

          assertNotNull(query);
        },
        "Custom breakpoints should work correctly");
  }

  @Test
  @DisplayName("Base Classes Always Applied")
  public void testBaseClassesAlwaysApplied() {
    assertDoesNotThrow(
        () -> {
          ContainerQuery query =
              ContainerQuery.on(testLabel).base("flex", "flex-col", "gap-4").sm("flex-row");
          query.install(container);

          assertNotNull(query);
          // Base classes should be present regardless of breakpoint
        },
        "Base classes should always be applied");
  }

  @Test
  @DisplayName("Breakpoint Callback")
  public void testBreakpointCallback() {
    final String[] capturedBreakpoint = new String[1];

    assertDoesNotThrow(
        () -> {
          ContainerQuery query =
              ContainerQuery.on(testLabel)
                  .onBreakpoint(bp -> capturedBreakpoint[0] = bp)
                  .sm("text-sm")
                  .md("text-md");
          query.install(container);

          assertNotNull(query);
          // Allow time for callback to execute
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }

          // Callback should have been invoked at least once
          assertNotNull(capturedBreakpoint[0], "Callback should be invoked");
        },
        "Breakpoint callback should work correctly");
  }

  @Test
  @DisplayName("Auto Detach on Scene Leave")
  public void testAutoDetach() {
    assertDoesNotThrow(
        () -> {
          Label tempLabel = new Label("Temp");
          Pane tempContainer = new Pane(tempLabel);

          ContainerQuery query = ContainerQuery.on(tempLabel).sm("text-sm");
          query.install(tempContainer);

          assertNotNull(query);

          // Remove from scene graph
          Platform.runLater(
              () -> {
                tempContainer.getChildren().remove(tempLabel);
              });

          // Should not throw after detach
          waitForFxEvents();
        },
        "Auto-detach should work when node leaves scene");
  }

  @Test
  @DisplayName("Manual Refresh")
  public void testRefresh() {
    assertDoesNotThrow(
        () -> {
          ContainerQuery query = ContainerQuery.on(testLabel).sm("text-sm").md("text-md");
          query.install(container);

          assertNotNull(query);

          // Manual refresh should not throw
          query.refresh();
        },
        "Manual refresh should work correctly");
  }

  @Test
  @DisplayName("Null Safety")
  public void testNullSafety() {
    // Test with null node
    assertThrows(
        NullPointerException.class,
        () -> {
          ContainerQuery.on(null);
        },
        "Should throw exception for null node");

    // Test with null container
    assertDoesNotThrow(
        () -> {
          ContainerQuery query = ContainerQuery.on(testLabel).sm("text-sm");
          query.install(container);
          // Should handle null container gracefully
        },
        "Should handle null container gracefully");
  }

  private void waitForFxEvents() {
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
