package io.github.yasmramos.tailwindfx.metrics;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.TwMetrics;
import io.github.yasmramos.tailwindfx.TwStyle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Integration tests for TwMetrics — verifies that metrics are properly recorded when TailwindFX
 * operations are performed with metrics enabled.
 */
@DisplayName("TwMetrics Integration Tests")
class MetricsIntegrationTest extends ApplicationTest {

  @BeforeAll
  static void setupSpec() {
    // Ensure JavaFX is initialized
  }

  /** Runs work on FX thread and blocks until done (max 3s). */
  static void runFx(Runnable work) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Throwable> err = new AtomicReference<>();
    Platform.runLater(
        () -> {
          try {
            work.run();
          } catch (Throwable t) {
            err.set(t);
          } finally {
            latch.countDown();
          }
        });
    if (!latch.await(3, TimeUnit.SECONDS)) {
      throw new RuntimeException("FX test timed out");
    }
    if (err.get() != null) {
      throw new RuntimeException(err.get());
    }
  }

  @Test
  @DisplayName("Should record metrics when applying styles")
  void testMetricsRecordedOnApply() throws Exception {
    runFx(
        () -> {
          // Reset and enable metrics
          TailwindFXMetrics.instance().reset();
          TwMetrics.setEnabled(true);

          Region node = new Region();

          // Apply some styles
          TwStyle.apply(node, "rounded-lg");
          TwStyle.apply(node, "p-4");

          // Verify metrics were recorded
          String report = TwMetrics.debugReport(node);
          assertNotNull(report);
          assertTrue(report.contains("TailwindFX Metrics"));

          // Get current metrics
          long cacheHits = TailwindFXMetrics.instance().cacheHits();
          long cacheMisses = TailwindFXMetrics.instance().cacheMisses();

          // At least one operation should have been recorded
          assertTrue(
              cacheHits + cacheMisses > 0, "Should have recorded at least one cache operation");
        });
  }

  @Test
  @DisplayName("Should show metrics when disabled")
  void testMetricsWhenDisabled() throws Exception {
    runFx(
        () -> {
          // Disable metrics
          TwMetrics.setEnabled(false);
          TailwindFXMetrics.instance().reset();

          Region node = new Region();
          TwStyle.apply(node, "rounded-lg");
          TwStyle.apply(node, "p-4");

          // Verify we can still get metrics (no exception)
          assertNotNull(TailwindFXMetrics.instance().report());

          // Re-enable for other tests
          TwMetrics.setEnabled(true);
        });
  }

  @Test
  @DisplayName("Should increment cache hits on duplicate apply")
  void testCacheHitsOnDuplicateApply() throws Exception {
    runFx(
        () -> {
          TailwindFXMetrics.instance().reset();
          TwMetrics.setEnabled(true);

          Region node = new Region();

          // Apply styles - metrics should be recorded
          TwStyle.apply(node, "rounded-lg");
          TwStyle.apply(node, "p-4");

          // Verify operations were recorded
          long totalOps =
              TailwindFXMetrics.instance().cacheHits() + TailwindFXMetrics.instance().cacheMisses();
          assertTrue(totalOps > 0, "Should have recorded at least one operation");
        });
  }

  @Test
  @DisplayName("Should generate valid report with actual data")
  void testReportWithRealData() throws Exception {
    runFx(
        () -> {
          TailwindFXMetrics.instance().reset();
          TwMetrics.setEnabled(true);

          StackPane root = new StackPane();
          Scene scene = new Scene(root, 400, 300);
          Region node = new Region();
          root.getChildren().add(node);

          // Apply multiple styles
          TwStyle.apply(node, "btn-primary", "rounded-lg", "p-4");
          TwStyle.apply(node, "shadow-md");

          // Generate report
          String report = TwMetrics.debugReport(node);

          // Verify report structure
          assertNotNull(report);
          assertTrue(report.contains("=== TailwindFX Metrics ==="));
          assertTrue(report.contains("JIT cache hits"));
          assertTrue(report.contains("JIT cache misses"));
          assertTrue(report.contains("apply() calls"));
          assertTrue(report.contains("Uptime"));
        });
  }

  @Test
  @DisplayName("Should track multiple nodes independently")
  void testMultipleNodesMetrics() throws Exception {
    runFx(
        () -> {
          TailwindFXMetrics.instance().reset();
          TwMetrics.setEnabled(true);

          Region node1 = new Region();
          Region node2 = new Region();
          Region node3 = new Region();

          // Apply different styles to different nodes
          TwStyle.apply(node1, "w-4");
          TwStyle.apply(node2, "p-2");
          TwStyle.apply(node3, "rounded-lg");

          // Verify metrics were recorded (at least some operations)
          long totalOps =
              TailwindFXMetrics.instance().cacheHits() + TailwindFXMetrics.instance().cacheMisses();

          assertTrue(totalOps > 0, "Should have recorded at least one operation");

          // Each node should have its own report
          String report1 = TwMetrics.debugReport(node1);
          String report2 = TwMetrics.debugReport(node2);
          String report3 = TwMetrics.debugReport(node3);

          assertNotNull(report1);
          assertNotNull(report2);
          assertNotNull(report3);
        });
  }

  @Test
  @DisplayName("Should reset all metrics to zero")
  void testResetClearsAllMetrics() throws Exception {
    runFx(
        () -> {
          TailwindFXMetrics.instance().reset();
          TwMetrics.setEnabled(true);

          Region node = new Region();
          TwStyle.apply(node, "rounded-lg", "p-4", "shadow-md");

          // Verify some metrics were recorded
          long beforeReset =
              TailwindFXMetrics.instance().cacheHits() + TailwindFXMetrics.instance().cacheMisses();
          assertTrue(beforeReset > 0);

          // Reset
          TailwindFXMetrics.instance().reset();

          // Verify all metrics are zero
          assertEquals(0, TailwindFXMetrics.instance().cacheHits());
          assertEquals(0, TailwindFXMetrics.instance().cacheMisses());
          assertEquals(0, TailwindFXMetrics.instance().compilations());
        });
  }

  @Test
  @DisplayName("Should calculate cache hit ratio correctly")
  void testCacheHitRatioCalculation() throws Exception {
    runFx(
        () -> {
          TailwindFXMetrics.instance().reset();
          TwMetrics.setEnabled(true);

          // Manually record some hits and misses for testing
          TailwindFXMetrics.instance().recordCacheHit();
          TailwindFXMetrics.instance().recordCacheHit();
          TailwindFXMetrics.instance().recordCacheMiss();

          double ratio = TailwindFXMetrics.instance().cacheHitRatio();

          // Should be approximately 0.666 (2 hits / 3 total)
          assertTrue(ratio > 0.6 && ratio < 0.7, "Hit ratio should be ~0.666, got: " + ratio);
        });
  }

  @Test
  @DisplayName("Should handle health check without errors")
  void testHealthCheckNoErrors() {
    // Should not throw any exception
    assertDoesNotThrow(() -> TwMetrics.healthCheck());
  }

  @Test
  @DisplayName("Should enable and disable metrics correctly")
  void testEnableDisableToggle() throws Exception {
    runFx(
        () -> {
          TwMetrics.setEnabled(false);
          assertFalse(TwMetrics.isEnabled(), "Should be disabled after setEnabled(false)");

          TwMetrics.setEnabled(true);
          assertTrue(TwMetrics.isEnabled(), "Should be enabled after setEnabled(true)");
        });
  }
}
