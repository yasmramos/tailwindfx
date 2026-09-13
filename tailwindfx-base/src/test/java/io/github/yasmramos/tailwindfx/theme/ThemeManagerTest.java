package io.github.yasmramos.tailwindfx.theme;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ThemeManager} — theme registration, switching, and lifecycle.
 */
public class ThemeManagerTest {

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
  void testRegisterTheme() {
    ThemeManager.reset();
    ThemeManager.register("test", "{\"colors\": {}}");
    org.junit.jupiter.api.Assertions.assertTrue(ThemeManager.isRegistered("test"));
  }

  @Test
  void testUnregisterTheme() {
    ThemeManager.reset();
    ThemeManager.register("temp", "{}");
    org.junit.jupiter.api.Assertions.assertTrue(ThemeManager.isRegistered("temp"));
    ThemeManager.unregister("temp");
    org.junit.jupiter.api.Assertions.assertFalse(ThemeManager.isRegistered("temp"));
  }

  @Test
  void testSetCurrentTheme() {
    ThemeManager.reset();
    ThemeManager.register("dark", "{}");
    ThemeManager.setCurrent("dark");
    org.junit.jupiter.api.Assertions.assertEquals("dark", ThemeManager.getCurrent());
  }

  @Test
  void testReset() {
    ThemeManager.reset();
    ThemeManager.register("x", "{}");
    org.junit.jupiter.api.Assertions.assertTrue(ThemeManager.isRegistered("x"));
    ThemeManager.reset();
    org.junit.jupiter.api.Assertions.assertFalse(ThemeManager.isRegistered("x"));
  }

  @Test
  void testGuardClauses() {
    ThemeManager.reset();
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> ThemeManager.register(null, "{}"));
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> ThemeManager.register("", "{}"));
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> ThemeManager.register("  ", "{}"));
  }

  @Test
  void testIsRegistered() {
    ThemeManager.reset();
    org.junit.jupiter.api.Assertions.assertFalse(ThemeManager.isRegistered("nonexistent"));
    ThemeManager.register("exists", "{}");
    org.junit.jupiter.api.Assertions.assertTrue(ThemeManager.isRegistered("exists"));
  }
}
