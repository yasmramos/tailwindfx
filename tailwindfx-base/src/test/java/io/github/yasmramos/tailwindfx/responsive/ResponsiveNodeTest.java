package io.github.yasmramos.tailwindfx.responsive;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ResponsiveNode} functionality.
 */
public class ResponsiveNodeTest {

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
          Region node = new Region();
          ResponsiveNode rn = new ResponsiveNode(node);
          org.junit.jupiter.api.Assertions.assertNotNull(rn);
          latch.countDown();
        });
    org.junit.jupiter.api.Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
  }

  @Test
  void testWithBreakpoint() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          Region node = new Region();
          ResponsiveNode rn = new ResponsiveNode(node);
          rn.withBreakpoint("md", "p-4");
          latch.countDown();
        });
    org.junit.jupiter.api.Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
  }

  @Test
  void testApplyStyles() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          Region node = new Region();
          ResponsiveNode rn = new ResponsiveNode(node);
          rn.applyStyles("m-2");
          latch.countDown();
        });
    org.junit.jupiter.api.Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
  }

  @Test
  void testGetNode() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          Region node = new Region();
          ResponsiveNode rn = new ResponsiveNode(node);
          org.junit.jupiter.api.Assertions.assertEquals(node, rn.getNode());
          latch.countDown();
        });
    org.junit.jupiter.api.Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
  }

  @Test
  void testNullNodeGuard() {
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> new ResponsiveNode(null));
  }

  @Test
  void testMultipleBreakpoints() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          Region node = new Region();
          ResponsiveNode rn = new ResponsiveNode(node);
          rn.withBreakpoint("sm", "p-1")
            .withBreakpoint("md", "p-2")
            .withBreakpoint("lg", "p-3");
          latch.countDown();
        });
    org.junit.jupiter.api.Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
  }

  @Test
  void testChainMethods() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          Region node = new Region();
          ResponsiveNode rn = new ResponsiveNode(node);
          ResponsiveNode result = rn.withBreakpoint("md", "p-4");
          org.junit.jupiter.api.Assertions.assertNotNull(result);
          latch.countDown();
        });
    org.junit.jupiter.api.Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
  }
}
