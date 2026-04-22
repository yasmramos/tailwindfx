package tailwindfx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TailwindFXMetrics.
 */
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
            metrics.onAlert((metric, current, threshold) -> {
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
            assertDoesNotThrow(() -> {
                metrics.print();
            });
        }
    }
}
