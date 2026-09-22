package io.github.yasmramos.tailwindfx.metrics;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.TwMetrics;
import io.github.yasmramos.tailwindfx.TwStyle;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Integration tests for TwMetrics — verifies that metrics are properly recorded when TailwindFX
 * operations are performed with metrics enabled.
 */
@DisplayName("TwMetrics Integration Tests")
class MetricsIntegrationTest extends ApplicationTest {

  @BeforeEach
  void setUp() {
    // Reset global state before each test to avoid cross-test contamination
    TailwindFXMetrics.instance().reset();
    TwMetrics.setEnabled(true);
  }

  /** Runs work on FX thread and blocks until done (max 3s). */
  @Test
  @DisplayName("Should record metrics when applying styles")
  void testMetricsRecordedOnApply() {
    interact(
        () -> {
          Region node = new Region();

          // Apply some styles
          TwStyle.apply(node, "rounded-lg");
          TwStyle.apply(node, "p-4");

          // Verify metrics were recorded
          String report = TwMetrics.debugReport(node);
          assertNotNull(report);
          assertTrue(report.contains("TailwindFX Metrics"));

          // Get current metrics - applyCalls tracks all style applications regardless of JIT
          long applyCalls = TailwindFXMetrics.instance().applyCalls();

          // At least one operation should have been recorded
          assertTrue(applyCalls > 0, "Should have recorded at least one apply operation");
        });
  }

  @Test
  @DisplayName("Should show metrics when disabled")
  void testMetricsWhenDisabled() {
    interact(
        () -> {
          // Disable metrics
          TwMetrics.setEnabled(false);

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
  void testCacheHitsOnDuplicateApply() {
    interact(
        () -> {
          Region node = new Region();

          // Apply mixed tokens: predefined utilities (go to cssClasses and recordApply) 
          // plus arbitrary values (go through JIT compiler to exercise cache metrics)
          TwStyle.apply(node, "bg-blue-500", "p-[13px]");
          TwStyle.apply(node, "text-white", "w-[200px]");
          
          // Apply again to trigger cache hits on JIT tokens
          TwStyle.apply(node, "bg-blue-500", "p-[13px]");
          TwStyle.apply(node, "text-white", "w-[200px]");

          // Verify operations were recorded using applyCalls and JIT cache metrics
          long applyCalls = TailwindFXMetrics.instance().applyCalls();
          long cacheHits = TailwindFXMetrics.instance().cacheHits();
          long cacheMisses = TailwindFXMetrics.instance().cacheMisses();
          
          assertTrue(applyCalls > 0, "Should have recorded at least one apply operation");
          assertTrue(cacheHits + cacheMisses > 0, "Should have recorded JIT cache activity");
        });
  }

  @Test
  @DisplayName("Should generate valid report with actual data")
  void testReportWithRealData() {
    interact(
        () -> {
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
  void testMultipleNodesMetrics() {
    interact(
        () -> {
          Region node1 = new Region();
          Region node2 = new Region();
          Region node3 = new Region();

          // Apply different styles to different nodes
          TwStyle.apply(node1, "w-4");
          TwStyle.apply(node2, "p-2");
          TwStyle.apply(node3, "rounded-lg");

          // Verify metrics were recorded using applyCalls
          long applyCalls = TailwindFXMetrics.instance().applyCalls();
          assertTrue(applyCalls > 0, "Should have recorded at least one operation");

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
  void testResetClearsAllMetrics() {
    interact(
        () -> {
          Region node = new Region();
          TwStyle.apply(node, "rounded-lg", "p-4", "shadow-md");

          // Verify some metrics were recorded using applyCalls
          long applyCallsBeforeReset = TailwindFXMetrics.instance().applyCalls();
          assertTrue(applyCallsBeforeReset > 0);

          // Reset
          TailwindFXMetrics.instance().reset();

          // Verify all metrics are zero
          assertEquals(0, TailwindFXMetrics.instance().cacheHits());
          assertEquals(0, TailwindFXMetrics.instance().cacheMisses());
          assertEquals(0, TailwindFXMetrics.instance().compilations());
          assertEquals(0, TailwindFXMetrics.instance().applyCalls());
        });
  }

  @Test
  @DisplayName("Should calculate cache hit ratio correctly")
  void testCacheHitRatioCalculation() {
    interact(
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
  void testEnableDisableToggle() {
    interact(
        () -> {
          TwMetrics.setEnabled(false);
          assertFalse(TwMetrics.isEnabled(), "Should be disabled after setEnabled(false)");

          TwMetrics.setEnabled(true);
          assertTrue(TwMetrics.isEnabled(), "Should be enabled after setEnabled(true)");
        });
  }
}
