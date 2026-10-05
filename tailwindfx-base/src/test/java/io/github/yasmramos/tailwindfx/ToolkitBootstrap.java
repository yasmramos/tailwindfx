package io.github.yasmramos.tailwindfx;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;

/**
 * JUnit 5 bootstrap that initializes the JavaFX toolkit once per JVM, so plain unit tests can
 * instantiate JavaFX controls without a running Platform.
 */
public final class ToolkitBootstrap {

  private static volatile boolean initialized = false;

  private ToolkitBootstrap() {}

  /** Starts the JavaFX toolkit if it is not already running. */
  public static synchronized void ensureStarted() {
    if (initialized) {
      return;
    }
    try {
      Platform.startup(() -> {});
    } catch (IllegalStateException e) {
      // Toolkit already initialized (e.g. by TestFX in another test class).
    }
    // Keep the FX threads alive for the whole JVM lifetime.
    Platform.setImplicitExit(false);
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(latch::countDown);
    try {
      if (!latch.await(10, TimeUnit.SECONDS)) {
        throw new IllegalStateException("JavaFX Application Thread did not start");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while waiting for JavaFX toolkit", e);
    }
    initialized = true;
  }
}
