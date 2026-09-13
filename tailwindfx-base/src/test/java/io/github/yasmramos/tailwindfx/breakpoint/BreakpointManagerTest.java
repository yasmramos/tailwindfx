package io.github.yasmramos.tailwindfx.breakpoint;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link BreakpointManager} — breakpoint registration, matching, and lifecycle.
 */
public class BreakpointManagerTest {

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
  void testRegisterAndMatch() {
    BreakpointManager.reset();
    BreakpointManager.register("sm", 640);
    BreakpointManager.register("md", 768);
    BreakpointManager.register("lg", 1024);

    org.junit.jupiter.api.Assertions.assertTrue(BreakpointManager.matches("sm"));
    org.junit.jupiter.api.Assertions.assertTrue(BreakpointManager.matches("md"));
    org.junit.jupiter.api.Assertions.assertTrue(BreakpointManager.matches("lg"));
  }

  @Test
  void testUnregister() {
    BreakpointManager.reset();
    BreakpointManager.register("test", 500);
    org.junit.jupiter.api.Assertions.assertTrue(BreakpointManager.matches("test"));
    BreakpointManager.unregister("test");
    org.junit.jupiter.api.Assertions.assertFalse(BreakpointManager.matches("test"));
  }

  @Test
  void testReset() {
    BreakpointManager.reset();
    BreakpointManager.register("x", 100);
    org.junit.jupiter.api.Assertions.assertTrue(BreakpointManager.matches("x"));
    BreakpointManager.reset();
    org.junit.jupiter.api.Assertions.assertFalse(BreakpointManager.matches("x"));
  }

  @Test
  void testGuardClauses() {
    BreakpointManager.reset();
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> BreakpointManager.register(null, 100));
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> BreakpointManager.register("", 100));
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> BreakpointManager.register("  ", 100));
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> BreakpointManager.register("ok", -1));
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> BreakpointManager.register("ok", 0));
  }

  @Test
  void testCurrentBreakpoint() {
    BreakpointManager.reset();
    BreakpointManager.register("base", 100);
    String current = BreakpointManager.current();
    org.junit.jupiter.api.Assertions.assertNotNull(current);
  }

  @Test
  void testIsActive() {
    BreakpointManager.reset();
    BreakpointManager.register("active", 200);
    org.junit.jupiter.api.Assertions.assertTrue(BreakpointManager.isActive("active"));
    org.junit.jupiter.api.Assertions.assertFalse(BreakpointManager.isActive("nonexistent"));
  }
}
