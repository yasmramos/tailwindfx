package io.github.yasmramos.tailwindfx.components;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TwFlexPane} component.
 */
public class TwFlexPaneTest {

  @BeforeAll
  static void initToolkit() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(() -> latch.countDown());
    if (!latch.await(3, TimeUnit.SECONDS)) {
      throw new RuntimeException("Failed to initialize JavaFX toolkit");
    }
  }

  @AfterAll
  static void shutdownToolkit() {
    Platform.exit();
  }

  @Test
  void testConstructor() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          TwFlexPane pane = new TwFlexPane();
          org.junit.jupiter.api.Assertions.assertNotNull(pane);
          latch.countDown();
        });
    org.junit.jupiter.api.Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
  }

  @Test
  void testAddChild() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          TwFlexPane pane = new TwFlexPane();
          Region child = new Region();
          pane.getChildren().add(child);
          org.junit.jupiter.api.Assertions.assertTrue(pane.getChildren().contains(child));
          latch.countDown();
        });
    org.junit.jupiter.api.Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
  }

  @Test
  void testFlexDirection() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          TwFlexPane pane = new TwFlexPane();
          pane.setFlexDirection("row");
          latch.countDown();
        });
    org.junit.jupiter.api.Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
  }

  @Test
  void testJustifyContent() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          TwFlexPane pane = new TwFlexPane();
          pane.setJustifyContent("center");
          latch.countDown();
        });
    org.junit.jupiter.api.Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
  }

  @Test
  void testAlignItems() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          TwFlexPane pane = new TwFlexPane();
          pane.setAlignItems("center");
          latch.countDown();
        });
    org.junit.jupiter.api.Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
  }

  @Test
  void testFlexWrap() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          TwFlexPane pane = new TwFlexPane();
          pane.setFlexWrap("wrap");
          latch.countDown();
        });
    org.junit.jupiter.api.Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
  }

  @Test
  void testGap() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          TwFlexPane pane = new TwFlexPane();
          pane.setGap(8);
          latch.countDown();
        });
    org.junit.jupiter.api.Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
  }

  @Test
  void testClearChildren() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          TwFlexPane pane = new TwFlexPane();
          pane.getChildren().add(new Region());
          pane.getChildren().clear();
          org.junit.jupiter.api.Assertions.assertTrue(pane.getChildren().isEmpty());
          latch.countDown();
        });
    org.junit.jupiter.api.Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
  }

  @Test
  void testStyleClass() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          TwFlexPane pane = new TwFlexPane();
          pane.getStyleClass().add("flex");
          org.junit.jupiter.api.Assertions.assertTrue(pane.getStyleClass().contains("flex"));
          latch.countDown();
        });
    org.junit.jupiter.api.Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
  }
}
