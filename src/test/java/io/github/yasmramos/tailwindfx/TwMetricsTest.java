package io.github.yasmramos.tailwindfx;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for TwMetrics facade. */
class TwMetricsTest {

  @BeforeEach
  void setUp() {
    // Reset metrics state before each test
    TailwindFXMetrics.instance().reset();
    TailwindFXMetrics.instance().setEnabled(true);
  }

  @Test
  void testDebugReportReturnsString() {
    Pane node = new Pane();
    String report = TwMetrics.debugReport(node);

    assertNotNull(report);
    assertTrue(report.contains("TailwindFX Metrics"));
  }

  @Test
  void testHealthCheckRunsSuccessfully() {
    // Should not throw any exception
    assertDoesNotThrow(() -> TwMetrics.healthCheck());
  }

  @Test
  void testDebugReportWithNullNode() {
    // Even with null node, should return a report (delegates to metrics)
    String report = TwMetrics.debugReport(null);

    assertNotNull(report);
    assertTrue(report.contains("TailwindFX Metrics"));
  }

  @Test
  void testMultipleDebugReports() {
    Pane node1 = new Pane();
    Pane node2 = new Pane();

    String report1 = TwMetrics.debugReport(node1);
    String report2 = TwMetrics.debugReport(node2);

    assertNotNull(report1);
    assertNotNull(report2);
    // Both reports should contain the same metrics structure
    assertTrue(report1.contains("JIT cache hits"));
    assertTrue(report2.contains("JIT cache hits"));
  }

  @Test
  void testHealthCheckMultipleTimes() {
    // Should be idempotent
    assertDoesNotThrow(
        () -> {
          TwMetrics.healthCheck();
          TwMetrics.healthCheck();
          TwMetrics.healthCheck();
        });
  }

  @Test
  void testMetricsEnabledByDefault() {
    // Verify metrics are enabled
    assertTrue(TailwindFXMetrics.instance().isEnabled());
  }

  @Test
  void testMetricsCanBeDisabled() {
    TailwindFXMetrics.instance().setEnabled(false);
    assertFalse(TailwindFXMetrics.instance().isEnabled());

    // Re-enable for other tests
    TailwindFXMetrics.instance().setEnabled(true);
  }
}
