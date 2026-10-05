package io.github.yasmramos.tailwindfx.metrics;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for TailwindFXMetrics. */
@DisplayName("TailwindFXMetrics Tests")
class TailwindFXMetricsTest {

  private TailwindFXMetrics metrics;

  @BeforeEach
  void setUp() {
    metrics = TailwindFXMetrics.instance();
    metrics.reset();
    metrics.setEnabled(true);
  }

  @Nested
  @DisplayName("Counters")
  class CounterTests {

    @Test
    @DisplayName("Should record cache hit")
    void testRecordCacheHit() {
      long before = metrics.cacheHits();
      metrics.recordCacheHit();
      long after = metrics.cacheHits();

      assertEquals(before + 1, after);
    }

    @Test
    @DisplayName("Should record cache miss")
    void testRecordCacheMiss() {
      long before = metrics.cacheMisses();
      metrics.recordCacheMiss();
      long after = metrics.cacheMisses();

      assertEquals(before + 1, after);
    }

    @Test
    @DisplayName("Should record compilation time")
    void testRecordCompilation() {
      metrics.recordCompilation(1000);

      assertTrue(metrics.compilations() > 0);
    }
  }

  @Nested
  @DisplayName("Cache Hit Ratio")
  class CacheHitRatioTests {

    @Test
    @DisplayName("Should calculate hit ratio correctly")
    void testHitRatio() {
      metrics.recordCacheHit();
      metrics.recordCacheHit();
      metrics.recordCacheMiss();

      double ratio = metrics.cacheHitRatio();
      assertEquals(0.666, ratio, 0.01);
    }

    @Test
    @DisplayName("Should return 1.0 when no misses")
    void testAllHits() {
      metrics.recordCacheHit();
      metrics.recordCacheHit();

      assertEquals(1.0, metrics.cacheHitRatio());
    }

    @Test
    @DisplayName("Should return 0.0 when no hits")
    void testAllMisses() {
      metrics.recordCacheMiss();
      metrics.recordCacheMiss();

      assertEquals(0.0, metrics.cacheHitRatio());
    }

    @Test
    @DisplayName("Should handle zero total operations")
    void testZeroOperations() {
      assertEquals(1.0, metrics.cacheHitRatio());
    }
  }

  @Nested
  @DisplayName("Average Compile Time")
  class CompileTimeTests {

    @Test
    @DisplayName("Should calculate average compile time")
    void testAverageCompileTime() {
      metrics.recordCompilation(100);
      metrics.recordCompilation(200);
      metrics.recordCompilation(300);

      long avg = metrics.avgCompileNs();
      assertEquals(200.0, avg, 50.0);
    }

    @Test
    @DisplayName("Should handle zero compilations")
    void testZeroCompilations() {
      assertEquals(0, metrics.avgCompileNs());
    }
  }

  @Nested
  @DisplayName("Alerts")
  class AlertTests {

    @Test
    @DisplayName("Should trigger alert on low cache hit ratio")
    void testLowCacheHitRatioAlert() {
      metrics.setEnabled(true);
      metrics.onAlert(
          (metric, current, threshold) -> {
            // Alert callback - just verify it doesn't throw
          });
      metrics.alertOnLowCacheHitRatio(0.01);
      metrics.recordCacheMiss();
      metrics.recordCacheMiss();
      metrics.recordCacheMiss();

      // Should not throw exception
    }

    @Test
    @DisplayName("Should enable/disable metrics")
    void testEnableDisable() {
      metrics.setEnabled(false);
      assertFalse(metrics.isEnabled());

      metrics.setEnabled(true);
      assertTrue(metrics.isEnabled());
    }

    @Test
    @DisplayName("Alert sampling fires exactly once per 50 cache misses")
    void testAlertSamplingIsExactlyOncePerInterval() {
      int[] alerts = {0};
      metrics.setEnabled(true);
      // Seed hits so that hits+misses is already above the 100-lookup warm-up gate; otherwise
      // checkAlerts() would bail out before ever evaluating a threshold.
      for (int i = 0; i < 200; i++) {
        metrics.recordCacheHit();
      }
      // A ratio of 1.0 can never be met with any miss recorded, so every evaluation alerts.
      metrics.alertOnLowCacheHitRatio(1.0);
      metrics.onAlert((metric, current, threshold) -> alerts[0]++);

      // 200 misses == 4 sampling intervals, so exactly 4 threshold evaluations must be delivered.
      for (int i = 0; i < 200; i++) {
        metrics.recordCacheMiss();
      }

      assertEquals(
          4,
          alerts[0],
          "alerts must fire once per completed 50-miss interval, not once per miss");
    }

    @Test
    @DisplayName("A partial sampling interval fires no alert")
    void testNoAlertForPartialInterval() {
      int[] alerts = {0};
      metrics.setEnabled(true);
      for (int i = 0; i < 200; i++) {
        metrics.recordCacheHit();
      }
      metrics.alertOnLowCacheHitRatio(1.0);
      metrics.onAlert((metric, current, threshold) -> alerts[0]++);

      for (int i = 0; i < 49; i++) {
        metrics.recordCacheMiss();
      }

      assertEquals(0, alerts[0], "49 misses must not complete a 50-miss interval");
    }

    @Test
    @DisplayName("Concurrent misses sample every interval exactly once")
    void testAlertSamplingIsNotLostUnderConcurrency() throws InterruptedException {
      // The regression this guards: recordCacheMiss() used to re-read the counter with get()
      // instead of using the value returned by its own incrementAndGet(). Under concurrency the
      // re-read observes another thread's increment, so whole 50-miss intervals could be skipped
      // and the alert count came out below the expected value.
      int[] alerts = {0};
      metrics.setEnabled(true);
      for (int i = 0; i < 200; i++) {
        metrics.recordCacheHit();
      }
      metrics.alertOnLowCacheHitRatio(1.0);
      metrics.onAlert((metric, current, threshold) -> alerts[0]++);

      Thread[] threads = new Thread[8];
      for (int t = 0; t < threads.length; t++) {
        threads[t] =
            new Thread(
                () -> {
                  for (int i = 0; i < 50; i++) {
                    metrics.recordCacheMiss();
                  }
                });
      }
      for (Thread thread : threads) {
        thread.start();
      }
      for (Thread thread : threads) {
        thread.join();
      }

      assertEquals(400, metrics.cacheMisses(), "all misses must be counted");
      assertEquals(
          8,
          alerts[0],
          "400 concurrent misses must yield exactly 8 sampling intervals, none lost or duplicated");
    }

    @Test
    @DisplayName("Disabled metrics are reported distinctly from 'no data collected'")
    void testHealthIssuesDistinguishDisabledFromEmpty() {
      // Enabled but nothing recorded -> "no data" issue, not "disabled".
      metrics.setEnabled(true);
      metrics.reset();
      List<String> enabledIssues =
          metrics.checkHealth().stream().map(TailwindFXMetrics.HealthIssue::message).toList();
      assertTrue(
          enabledIssues.stream().anyMatch(m -> m.contains("No metrics data collected")),
          "an enabled collector with no activity must report missing data: " + enabledIssues);
      assertFalse(
          enabledIssues.stream().anyMatch(m -> m.contains("disabled")),
          "an enabled collector must not be reported as disabled: " + enabledIssues);

      // Disabled -> its own dedicated issue.
      metrics.setEnabled(false);
      List<String> disabledIssues =
          metrics.checkHealth().stream().map(TailwindFXMetrics.HealthIssue::message).toList();
      assertTrue(
          disabledIssues.stream().anyMatch(m -> m.contains("disabled")),
          "a disabled collector must report that metrics are off: " + disabledIssues);
      assertFalse(
          disabledIssues.stream().anyMatch(m -> m.contains("No metrics data collected")),
          "a disabled collector must not be reported as silently missing data: " + disabledIssues);

      // Restore for the remaining tests in this class.
      metrics.setEnabled(true);
    }
  }

  @Nested
  @DisplayName("Reset")
  class ResetTests {

    @Test
    @DisplayName("Should reset all counters")
    void testReset() {
      metrics.recordCacheHit();
      metrics.recordCacheMiss();
      metrics.recordCompilation(100);

      metrics.reset();

      assertEquals(0, metrics.cacheHits());
      assertEquals(0, metrics.cacheMisses());
      assertEquals(0, metrics.compilations());
    }
  }

  @Nested
  @DisplayName("Report")
  class ReportTests {

    @Test
    @DisplayName("Should generate report without errors")
    void testReportGeneration() {
      metrics.recordCacheHit();
      metrics.recordCacheMiss();
      metrics.recordCompilation(100);

      String report = metrics.report();

      assertNotNull(report);
      assertFalse(report.isEmpty());
    }

    @Test
    @DisplayName("Should print report without errors")
    void testPrintReport() {
      // Should not throw exception
      assertDoesNotThrow(
          () -> {
            metrics.print();
          });
    }
  }
}
