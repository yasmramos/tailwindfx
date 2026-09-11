package io.github.yasmramos.tailwindfx.benchmark;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * BenchmarkTest — Unit tests for Benchmark class formatting and logic.
 *
 * <p>Tests that validate the formatting API and non-timing-dependent logic of the Benchmark class.
 * Performance-related assertions have been migrated to JMH benchmarks in tailwindfx-benchmarks.
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
  @DisplayName("Cache hit rate should be high for repeated tokens")
  void testCacheHitRate() {
    Benchmark.ComparisonResult comparison = Benchmark.compareCacheStrategies();

    // With repeated tokens, hit rate should be reasonable (at least 50%)
    // Note: This is a simplified check since we're measuring successful compilations
    assertTrue(
        comparison.cacheHitRatePercent() > 50.0,
        "Cache hit rate should be above 50% for repeated tokens");

    assertEquals(
        1000,
        comparison.totalOperations(),
        "Total operations should match benchmark configuration");
  }

  @Test
  @DisplayName("Benchmark metric formatting should work correctly")
  void testMetricFormatting() {
    Benchmark.BenchmarkMetric metric = new Benchmark.BenchmarkMetric("Test", 0.1234, 100);

    String formatted = metric.toFormattedString();
    assertTrue(formatted.contains("Test"), "Formatted string should contain metric name");
    assertTrue(formatted.contains("ms"), "Formatted string should contain unit");
    assertTrue(formatted.contains("100"), "Formatted string should contain iteration count");
  }

  @Test
  @DisplayName("Throughput metric formatting should use correct unit")
  void testThroughputFormatting() {
    Benchmark.BenchmarkMetric throughput =
        new Benchmark.BenchmarkMetric("Throughput", 50000, 1000, "compilations/sec");

    String formatted = throughput.toFormattedString();
    assertTrue(
        formatted.contains("compilations/sec"), "Throughput should use compilations/sec unit");
  }

  @Test
  @DisplayName("Benchmark results markdown should be well-formatted")
  void testMarkdownOutput() {
    Benchmark.BenchmarkResults results = Benchmark.runAll();

    String markdown = results.toMarkdown();

    assertTrue(markdown.contains("Benchmark Results"), "Markdown should contain header");
    assertTrue(markdown.contains("|"), "Markdown should contain table formatting");
    assertTrue(markdown.contains("Cache Speedup"), "Markdown should contain speedup summary");
  }

  @Test
  @DisplayName("Comparison result formatting should be accurate")
  void testComparisonFormatting() {
    Benchmark.ComparisonResult comparison = new Benchmark.ComparisonResult(95.5, 955, 1000);

    String formatted = comparison.toFormattedString();
    assertTrue(formatted.contains("95.50%"), "Should format percentage with 2 decimals");
    assertTrue(formatted.contains("955/1000"), "Should show hit/total operations");
  }
}
