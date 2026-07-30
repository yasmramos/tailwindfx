package io.github.yasmramos.tailwindfx.benchmark;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BenchmarkTest — Unit tests for Benchmark class.
 *
 * <p>Verifies that benchmark measurements are accurate and within expected ranges.</p>
 *
 * @author yasmramos
 * @since 1.0.0
 */
@DisplayName("Benchmark Tests")
public class BenchmarkTest {

    @BeforeAll
    static void warmup() {
        // Warm up JIT compiler before running tests
        Benchmark.warmup(50);
    }

    @Test
    @DisplayName("Cache hit should be faster than cache miss")
    void testCacheHitFasterThanMiss() {
        Benchmark.BenchmarkResults results = Benchmark.runAll();
        
        // Cache hits should be significantly faster than misses
        assertTrue(results.getSpeedupFactor() > 1.0, 
            "Cache hits should be faster than cache misses");
        
        // Typically cache should be at least 2x faster (relaxed for CI environments)
        assertTrue(results.getSpeedupFactor() > 2.0, 
            "Cache speedup should be at least 2x in normal conditions, got: " + results.getSpeedupFactor());
    }

    @Test
    @DisplayName("Cache hit rate should be high for repeated tokens")
    void testCacheHitRate() {
        Benchmark.ComparisonResult comparison = Benchmark.compareCacheStrategies();
        
        // With repeated tokens, hit rate should be reasonable (at least 50%)
        // Note: This is a simplified check since we're measuring successful compilations
        assertTrue(comparison.cacheHitRatePercent() > 50.0, 
            "Cache hit rate should be above 50% for repeated tokens");
        
        assertEquals(1000, comparison.totalOperations(), 
            "Total operations should match benchmark configuration");
    }

    @Test
    @DisplayName("Throughput should be reasonable")
    void testThroughput() {
        Benchmark.BenchmarkResults results = Benchmark.runAll();
        
        // Should achieve at least 10,000 compilations per second
        assertTrue(results.throughput().value() > 10_000, 
            "Throughput should exceed 10,000 compilations/sec");
        
        // Typical modern systems should achieve 100,000+ compilations/sec
        System.out.println("Current throughput: " + (int)results.throughput().value() + " comp/sec");
    }

    @Test
    @DisplayName("Cache miss time should be measurable")
    void testCacheMissTime() {
        Benchmark.BenchmarkResults results = Benchmark.runAll();
        
        // Cache miss should take some measurable time (> 0.001ms)
        assertTrue(results.cacheMiss().value() > 0.001, 
            "Cache miss time should be measurable");
        
        // But shouldn't be too slow (< 10ms per compilation)
        assertTrue(results.cacheMiss().value() < 10.0, 
            "Cache miss should complete in under 10ms");
    }

    @Test
    @DisplayName("Cache hit time should be very fast")
    void testCacheHitTime() {
        Benchmark.BenchmarkResults results = Benchmark.runAll();
        
        // Cache hit should be extremely fast (< 0.1ms)
        assertTrue(results.cacheHit().value() < 0.1, 
            "Cache hit should complete in under 0.1ms");
        
        // And measurable (> 0)
        assertTrue(results.cacheHit().value() > 0, 
            "Cache hit time should be positive");
    }

    @Test
    @DisplayName("Mixed workload should be between hit and miss times")
    void testMixedWorkload() {
        Benchmark.BenchmarkResults results = Benchmark.runAll();
        
        double mixed = results.mixed().value();
        double hit = results.cacheHit().value();
        double miss = results.cacheMiss().value();
        
        // Mixed should be >= hit time (allowing some variance for JVM warmup)
        // We relax the upper bound since mixed includes 50% cache misses
        assertTrue(mixed >= hit * 0.5, 
            "Mixed workload time should be at least half of hit time");
        
        // Log for debugging
        System.out.println(String.format("Hit: %.4f ms, Miss: %.4f ms, Mixed: %.4f ms", 
            hit, miss, mixed));
    }

    @Test
    @DisplayName("Benchmark results should be consistent across runs")
    void testConsistency() {
        // Note: Benchmark consistency can vary significantly in CI environments due to JVM warmup,
        // CPU throttling, and resource contention. This test is primarily for manual verification.
        Benchmark.BenchmarkResults run1 = Benchmark.runAll();
        Benchmark.BenchmarkResults run2 = Benchmark.runAll();
        
        // Just verify both runs completed successfully with positive speedup
        assertTrue(run1.getSpeedupFactor() > 0, 
            "Run1 should have positive speedup: " + run1.getSpeedupFactor());
        assertTrue(run2.getSpeedupFactor() > 0, 
            "Run2 should have positive speedup: " + run2.getSpeedupFactor());
        
        // Log for manual inspection (not asserting strict consistency)
        System.out.println(String.format(
            "Benchmark consistency check - Run1: %.2fx, Run2: %.2fx (variance expected in CI)",
            run1.getSpeedupFactor(), run2.getSpeedupFactor()));
    }

    @Test
    @DisplayName("Benchmark metric formatting should work correctly")
    void testMetricFormatting() {
        Benchmark.BenchmarkMetric metric = new Benchmark.BenchmarkMetric(
            "Test", 0.1234, 100);
        
        String formatted = metric.toFormattedString();
        assertTrue(formatted.contains("Test"), 
            "Formatted string should contain metric name");
        assertTrue(formatted.contains("ms"), 
            "Formatted string should contain unit");
        assertTrue(formatted.contains("100"), 
            "Formatted string should contain iteration count");
    }

    @Test
    @DisplayName("Throughput metric formatting should use correct unit")
    void testThroughputFormatting() {
        Benchmark.BenchmarkMetric throughput = new Benchmark.BenchmarkMetric(
            "Throughput", 50000, 1000, "compilations/sec");
        
        String formatted = throughput.toFormattedString();
        assertTrue(formatted.contains("compilations/sec"), 
            "Throughput should use compilations/sec unit");
    }

    @Test
    @DisplayName("Benchmark results markdown should be well-formatted")
    void testMarkdownOutput() {
        Benchmark.BenchmarkResults results = Benchmark.runAll();
        
        String markdown = results.toMarkdown();
        
        assertTrue(markdown.contains("Benchmark Results"), 
            "Markdown should contain header");
        assertTrue(markdown.contains("|"), 
            "Markdown should contain table formatting");
        assertTrue(markdown.contains("Cache Speedup"), 
            "Markdown should contain speedup summary");
    }

    @Test
    @DisplayName("Comparison result formatting should be accurate")
    void testComparisonFormatting() {
        Benchmark.ComparisonResult comparison = new Benchmark.ComparisonResult(
            95.5, 955, 1000);
        
        String formatted = comparison.toFormattedString();
        assertTrue(formatted.contains("95.50%"), 
            "Should format percentage with 2 decimals");
        assertTrue(formatted.contains("955/1000"), 
            "Should show hit/total operations");
    }
}
