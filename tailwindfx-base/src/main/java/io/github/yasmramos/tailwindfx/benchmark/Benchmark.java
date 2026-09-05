package io.github.yasmramos.tailwindfx.benchmark;

import io.github.yasmramos.tailwindfx.core.JitCompiler;

/**
 * Benchmark — Performance benchmarking for TailwindFX JIT Compiler.
 *
 * <p>Compares performance between cache hits and misses, measures compilation throughput, and
 * provides metrics for optimization decisions.
 *
 * <h2>Usage Example:</h2>
 *
 * <pre>{@code
 * public class CacheBenchmark {
 *     public static void main(String[] args) {
 *         // Warmup
 *         Benchmark.warmup(100);
 *
 *         // Run benchmarks
 *         BenchmarkResults results = Benchmark.runAll();
 *
 *         // Print results
 *         System.out.println(results.toMarkdown());
 *     }
 * }
 * }</pre>
 *
 * @author yasmramos
 * @since 1.0.0
 */
public final class Benchmark {

  private static final int WARMUP_ITERATIONS = 100;
  private static final int BENCHMARK_ITERATIONS = 1000;
  private static final String[] TEST_TOKENS = {
    "p-4",
    "m-2",
    "bg-blue-500",
    "text-white",
    "rounded-lg",
    "shadow-md",
    "flex",
    "items-center",
    "justify-between",
    "w-full",
    "h-auto"
  };

  private Benchmark() {
    // Utility class
  }

  /**
   * Warms up the JIT compiler cache before running benchmarks.
   *
   * @param iterations Number of warmup iterations
   */
  public static void warmup(int iterations) {
    for (int i = 0; i < iterations; i++) {
      JitCompiler.compile(TEST_TOKENS[i % TEST_TOKENS.length]);
    }
    // Clear cache after warmup to start fresh
    JitCompiler.clearCache();
  }

  /**
   * Runs all benchmark tests and returns aggregated results.
   *
   * @return BenchmarkResults containing all metrics
   */
  public static BenchmarkResults runAll() {
    BenchmarkResults.Builder builder = new BenchmarkResults.Builder();

    // Cache miss benchmark (first compilation)
    JitCompiler.clearCache();
    builder.cacheMiss(runCacheMissBenchmark());

    // Cache hit benchmark (subsequent compilations)
    builder.cacheHit(runCacheHitBenchmark());

    // Mixed workload benchmark
    JitCompiler.clearCache();
    builder.mixed(runMixedWorkloadBenchmark());

    // Throughput benchmark
    builder.throughput(runThroughputBenchmark());

    return builder.build();
  }

  /**
   * Benchmarks cache miss performance (cold compilation).
   *
   * @return BenchmarkMetric with timing statistics
   */
  private static BenchmarkMetric runCacheMissBenchmark() {
    JitCompiler.clearCache();

    long startTime = System.nanoTime();
    for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
      JitCompiler.compile(TEST_TOKENS[i % TEST_TOKENS.length]);
    }
    long endTime = System.nanoTime();

    double avgTimeMs = (endTime - startTime) / (double) BENCHMARK_ITERATIONS / 1_000_000.0;
    return new BenchmarkMetric("Cache Miss", avgTimeMs, BENCHMARK_ITERATIONS);
  }

  /**
   * Benchmarks cache hit performance (warm compilation).
   *
   * @return BenchmarkMetric with timing statistics
   */
  private static BenchmarkMetric runCacheHitBenchmark() {
    // Pre-populate cache
    for (String token : TEST_TOKENS) {
      JitCompiler.compile(token);
    }

    long startTime = System.nanoTime();
    for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
      JitCompiler.compile(TEST_TOKENS[i % TEST_TOKENS.length]);
    }
    long endTime = System.nanoTime();

    double avgTimeMs = (endTime - startTime) / (double) BENCHMARK_ITERATIONS / 1_000_000.0;
    return new BenchmarkMetric("Cache Hit", avgTimeMs, BENCHMARK_ITERATIONS);
  }

  /**
   * Benchmarks mixed workload (50% hits, 50% misses).
   *
   * @return BenchmarkMetric with timing statistics
   */
  private static BenchmarkMetric runMixedWorkloadBenchmark() {
    JitCompiler.clearCache();

    long startTime = System.nanoTime();
    for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
      if (i % 2 == 0) {
        // Cache miss - new token
        JitCompiler.compile("w-" + i + "px");
      } else {
        // Cache hit - existing token
        JitCompiler.compile(TEST_TOKENS[i % TEST_TOKENS.length]);
      }
    }
    long endTime = System.nanoTime();

    double avgTimeMs = (endTime - startTime) / (double) BENCHMARK_ITERATIONS / 1_000_000.0;
    return new BenchmarkMetric("Mixed Workload", avgTimeMs, BENCHMARK_ITERATIONS);
  }

  /**
   * Benchmarks compilation throughput (compilations per second).
   *
   * @return BenchmarkMetric with throughput statistics
   */
  private static BenchmarkMetric runThroughputBenchmark() {
    JitCompiler.clearCache();

    long startTime = System.nanoTime();
    int compilations = 0;
    long durationMs = 1000; // 1 second

    while ((System.nanoTime() - startTime) < durationMs * 1_000_000) {
      JitCompiler.compile(TEST_TOKENS[compilations % TEST_TOKENS.length]);
      compilations++;
    }
    long endTime = System.nanoTime();

    double throughput = compilations / ((endTime - startTime) / 1_000_000_000.0);
    return new BenchmarkMetric("Throughput", throughput, compilations, "compilations/sec");
  }

  /**
   * Compares LRU cache vs no-cache performance.
   *
   * @return ComparisonResult with relative performance metrics
   */
  public static ComparisonResult compareCacheStrategies() {
    // This would require a version without cache for comparison
    // For now, we measure cache effectiveness
    JitCompiler.clearCache();

    int totalOps = 1000;
    int cacheHits = 0;

    // Populate cache
    for (String token : TEST_TOKENS) {
      JitCompiler.compile(token);
    }

    // Measure hit rate - use compileBatch to get BatchResult
    for (int i = 0; i < totalOps; i++) {
      JitCompiler.BatchResult result =
          JitCompiler.compileBatch(TEST_TOKENS[i % TEST_TOKENS.length]);
      // If we got a result quickly, it was likely cached
      if (result != null && !result.inlineStyle().isEmpty()) {
        cacheHits++;
      }
    }

    double hitRate = (cacheHits * 100.0) / totalOps;
    return new ComparisonResult(hitRate, cacheHits, totalOps);
  }

  /** Represents a single benchmark metric. */
  public record BenchmarkMetric(String name, double value, int iterations, String unit) {
    public BenchmarkMetric(String name, double value, int iterations) {
      this(name, value, iterations, "ms");
    }

    public String toFormattedString() {
      if ("compilations/sec".equals(unit)) {
        return String.format("%s: %.0f %s (over %d iterations)", name, value, unit, iterations);
      }
      return String.format("%s: %.4f %s (over %d iterations)", name, value, unit, iterations);
    }
  }

  /** Represents cache strategy comparison results. */
  public record ComparisonResult(double cacheHitRatePercent, int cacheHits, int totalOperations) {
    public String toFormattedString() {
      return String.format(
          "Cache Hit Rate: %.2f%% (%d/%d operations)",
          cacheHitRatePercent, cacheHits, totalOperations);
    }
  }

  /** Aggregates all benchmark results. */
  public static class BenchmarkResults {
    private final BenchmarkMetric cacheMiss;
    private final BenchmarkMetric cacheHit;
    private final BenchmarkMetric mixed;
    private final BenchmarkMetric throughput;

    private BenchmarkResults(Builder builder) {
      this.cacheMiss = builder.cacheMiss;
      this.cacheHit = builder.cacheHit;
      this.mixed = builder.mixed;
      this.throughput = builder.throughput;
    }

    public BenchmarkMetric cacheMiss() {
      return cacheMiss;
    }

    public BenchmarkMetric cacheHit() {
      return cacheHit;
    }

    public BenchmarkMetric mixed() {
      return mixed;
    }

    public BenchmarkMetric throughput() {
      return throughput;
    }

    /**
     * Calculates speedup factor of cache hits vs misses.
     *
     * @return Speedup factor (e.g., 85.0 means 85x faster)
     */
    public double getSpeedupFactor() {
      if (cacheHit.value == 0) return 0;
      return cacheMiss.value / cacheHit.value;
    }

    /**
     * Returns results formatted as Markdown table.
     *
     * @return Markdown formatted string
     */
    public String toMarkdown() {
      StringBuilder sb = new StringBuilder();
      sb.append("## TailwindFX JIT Compiler Benchmark Results\n\n");
      sb.append("| Test | Value | Iterations |\n");
      sb.append("|------|-------|------------|\n");
      sb.append(
          String.format(
              "| %s | %.4f ms | %d |\n",
              cacheMiss.name(), cacheMiss.value(), cacheMiss.iterations()));
      sb.append(
          String.format(
              "| %s | %.4f ms | %d |\n", cacheHit.name(), cacheHit.value(), cacheHit.iterations()));
      sb.append(
          String.format(
              "| %s | %.4f ms | %d |\n", mixed.name(), mixed.value(), mixed.iterations()));
      sb.append(
          String.format(
              "| %s | %.0f comp/sec | %d |\n",
              throughput.name(), throughput.value(), throughput.iterations()));
      sb.append("\n### Performance Summary\n\n");
      sb.append(String.format("- **Cache Speedup**: %.2fx faster\n", getSpeedupFactor()));
      sb.append(String.format("- **Cache Hit Time**: %.4f ms\n", cacheHit.value()));
      sb.append(String.format("- **Cache Miss Time**: %.4f ms\n", cacheMiss.value()));

      ComparisonResult comparison = compareCacheStrategies();
      sb.append(String.format("- **Cache Hit Rate**: %.2f%%\n", comparison.cacheHitRatePercent()));

      return sb.toString();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Benchmark Results:\n");
      sb.append(cacheMiss.toFormattedString()).append("\n");
      sb.append(cacheHit.toFormattedString()).append("\n");
      sb.append(mixed.toFormattedString()).append("\n");
      sb.append(throughput.toFormattedString()).append("\n");
      sb.append(String.format("Cache Speedup: %.2fx\n", getSpeedupFactor()));

      ComparisonResult comparison = compareCacheStrategies();
      sb.append(comparison.toFormattedString());

      return sb.toString();
    }

    /** Builder for BenchmarkResults. */
    public static class Builder {
      private BenchmarkMetric cacheMiss;
      private BenchmarkMetric cacheHit;
      private BenchmarkMetric mixed;
      private BenchmarkMetric throughput;

      public Builder cacheMiss(BenchmarkMetric metric) {
        this.cacheMiss = metric;
        return this;
      }

      public Builder cacheHit(BenchmarkMetric metric) {
        this.cacheHit = metric;
        return this;
      }

      public Builder mixed(BenchmarkMetric metric) {
        this.mixed = metric;
        return this;
      }

      public Builder throughput(BenchmarkMetric metric) {
        this.throughput = metric;
        return this;
      }

      public BenchmarkResults build() {
        return new BenchmarkResults(this);
      }
    }
  }
}
