package io.github.yasmramos.tailwindfx.benchmark;

import io.github.yasmramos.tailwindfx.core.JitCompiler;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * JMH-based performance benchmarks for TailwindFX JIT Compiler.
 *
 * <p>Measures cache hit/miss performance and compilation throughput using proper microbenchmarking
 * techniques to avoid common pitfalls like dead-code elimination and JVM warmup effects.
 *
 * <h2>Usage:</h2>
 *
 * <pre>{@code
 * # Build the benchmarks module
 * mvn -P benchmarks package
 *
 * # Run all benchmarks
 * java -jar tailwindfx-benchmarks/target/benchmarks.jar
 *
 * # Run specific benchmark
 * java -jar tailwindfx-benchmarks/target/benchmarks.jar CacheHitBenchmark
 * }</pre>
 *
 * @author yasmramos
 * @since 0.1.0
 */
@State(Scope.Thread)
@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class JitCompilerBenchmark {

  /** Test tokens matching those used in the original Benchmark class. */
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

  /**
   * State holder for cache miss scenarios. Clears cache before each iteration to simulate cold
   * compilation.
   */
  @State(Scope.Thread)
  public static class CacheMissState {
    @Setup(Level.Invocation)
    public void clearCache() {
      JitCompiler.clearCache();
    }
  }

  /** State holder for cache hit scenarios. Pre-populates cache once per benchmark run. */
  @State(Scope.Thread)
  public static class CacheHitState {
    private int tokenIndex = 0;

    @Setup(Level.Iteration)
    public void populateCache() {
      // Pre-populate cache with all test tokens
      for (String token : TEST_TOKENS) {
        JitCompiler.compile(token);
      }
      tokenIndex = 0;
    }
  }

  /**
   * Benchmarks cache miss performance (cold compilation). Each invocation clears the cache to
   * ensure no hits.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @param state cache miss state with cleared cache
   */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void benchmarkCacheMiss(Blackhole blackhole, CacheMissState state) {
    String token = TEST_TOKENS[0];
    var result = JitCompiler.compile(token);
    blackhole.consume(result);
  }

  /**
   * Benchmarks cache miss throughput (compilations per second on cold cache). Each invocation
   * clears the cache to ensure no hits. This provides a stable ops/s metric for cache misses.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @param state cache miss state with cleared cache
   */
  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.SECONDS)
  public void benchmarkCacheMissThroughput(Blackhole blackhole, CacheMissState state) {
    String token = TEST_TOKENS[0];
    var result = JitCompiler.compile(token);
    blackhole.consume(result);
  }

  /**
   * Benchmarks cache hit performance (warm compilation). Cache is pre-populated, so all
   * compilations should be hits.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @param state cache hit state with populated cache
   */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void benchmarkCacheHit(Blackhole blackhole, CacheHitState state) {
    String token = TEST_TOKENS[state.tokenIndex % TEST_TOKENS.length];
    state.tokenIndex++;
    var result = JitCompiler.compile(token);
    blackhole.consume(result);
  }

  /**
   * Benchmarks cache hit throughput (compilations per second on warm cache). Cache is
   * pre-populated, so all compilations should be hits. This provides a stable ops/s metric for
   * cache hits.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @param state cache hit state with populated cache
   */
  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.SECONDS)
  public void benchmarkCacheHitThroughput(Blackhole blackhole, CacheHitState state) {
    String token = TEST_TOKENS[state.tokenIndex % TEST_TOKENS.length];
    state.tokenIndex++;
    var result = JitCompiler.compile(token);
    blackhole.consume(result);
  }

  /**
   * Benchmarks mixed workload (50% hits, 50% misses). Alternates between new tokens (misses) and
   * existing tokens (hits).
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @param state shared state for mixed workload
   */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void benchmarkMixedWorkload(Blackhole blackhole, MixedState state) {
    String token;
    if (state.opCount % 2 == 0) {
      // Cache miss - new unique token
      token = "w-" + state.opCount + "px";
    } else {
      // Cache hit - existing token
      token = TEST_TOKENS[state.opCount % TEST_TOKENS.length];
    }
    state.opCount++;
    var result = JitCompiler.compile(token);
    blackhole.consume(result);
  }

  /**
   * Benchmarks mixed workload throughput (compilations per second with 50% hits, 50% misses).
   * Alternates between new tokens (misses) and existing tokens (hits). This provides a stable ops/s
   * metric for mixed workloads.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @param state shared state for mixed workload
   */
  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.SECONDS)
  public void benchmarkMixedWorkloadThroughput(Blackhole blackhole, MixedState state) {
    String token;
    if (state.opCount % 2 == 0) {
      // Cache miss - new unique token
      token = "w-" + state.opCount + "px";
    } else {
      // Cache hit - existing token
      token = TEST_TOKENS[state.opCount % TEST_TOKENS.length];
    }
    state.opCount++;
    var result = JitCompiler.compile(token);
    blackhole.consume(result);
  }

  /** State holder for mixed workload benchmark. */
  @State(Scope.Thread)
  public static class MixedState {
    public int opCount = 0;

    @Setup(Level.Iteration)
    public void reset() {
      JitCompiler.clearCache();
      opCount = 0;
    }
  }
}
