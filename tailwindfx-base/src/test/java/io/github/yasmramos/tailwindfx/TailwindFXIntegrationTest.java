package io.github.yasmramos.tailwindfx;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for TailwindFX core functionality.
 */
public class TailwindFXIntegrationTest {

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
  void testInitialization() {
    org.junit.jupiter.api.Assertions.assertNotNull(TailwindFX.getInstance());
  }

  @Test
  void testApplyClasses() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          Region node = new Region();
          TailwindFX.apply(node, "p-4", "m-2");
          latch.countDown();
        });
    org.junit.jupiter.api.Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
  }

  @Test
  void testReset() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          TailwindFX.reset();
          latch.countDown();
        });
    org.junit.jupiter.api.Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
  }

  @Test
  void testThemeRegistration() {
    org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> TailwindFX.registerTheme("test", "{}"));
  }
}
