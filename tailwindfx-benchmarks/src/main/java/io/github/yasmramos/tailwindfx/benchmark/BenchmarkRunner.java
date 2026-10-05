package io.github.yasmramos.tailwindfx.benchmark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * Programmatic JMH benchmark runner for TailwindFX.
 *
 * <p>This class provides a convenient way to execute all benchmarks in the project with a single
 * invocation, using reproducible default configuration. It leverages the JMH {@link
 * org.openjdk.jmh.runner.Runner} API to configure and run benchmarks programmatically.
 *
 * <h2>Default Configuration:</h2>
 *
 * <ul>
 *   <li><strong>Warmup iterations:</strong> 5 (1 second each)
 *   <li><strong>Measurement iterations:</strong> 5 (1 second each)
 *   <li><strong>Forks:</strong> 2 (separate JVM processes for reliability)
 *   <li><strong>Benchmark pattern:</strong> All classes in {@code
 *       io.github.yasmramos.tailwindfx.benchmark} package
 *   <li><strong>Output format:</strong> JSON file at {@code target/jmh-results.json} plus console
 *       output
 *   <li><strong>Fail on error:</strong> true (stops execution if benchmarks fail)
 * </ul>
 *
 * <p>These defaults override any annotations present on individual benchmark classes when running
 * through this runner. For fine-grained control, use the JMH command-line interface directly.
 *
 * <h2>Usage:</h2>
 *
 * <pre>{@code
 * # Build the benchmarks module
 * mvn -P benchmarks package
 *
 * # Run all benchmarks with default configuration (recommended)
 * java -jar tailwindfx-benchmarks/target/benchmarks.jar
 *
 * # Run with custom JMH arguments (delegates to org.openjdk.jmh.Main)
 * java -jar tailwindfx-benchmarks/target/benchmarks.jar -i 10 -wi 5 -f 3
 *
 * # Run specific benchmark pattern
 * java -jar tailwindfx-benchmarks/target/benchmarks.jar "JitCompiler.*"
 *
 * # Alternative: run via classpath without changing Main-Class
 * java -cp tailwindfx-benchmarks/target/benchmarks.jar io.github.yasmramos.tailwindfx.benchmark.BenchmarkRunner
 * }</pre>
 *
 * <h2>Output:</h2>
 *
 * <p>Results are written to:
 *
 * <ul>
 *   <li>Console output (standard JMH format)
 *   <li>JSON file: {@code target/jmh-results.json} (for further analysis or CI integration)
 * </ul>
 *
 * <p>The JSON results file can be used with tools like {@code
 * benchmark-action/github-action-benchmark} for regression detection.
 *
 * <h2>Adding New Benchmarks:</h2>
 *
 * <p>New benchmark classes added to the {@code io.github.yasmramos.tailwindfx.benchmark} package
 * will automatically be included when running this runner, as it uses a regex pattern that matches
 * all classes in the package.
 *
 * @author yasmramos
 * @since 0.1.0
 * @see org.openjdk.jmh.runner.Runner
 * @see org.openjdk.jmh.runner.options.OptionsBuilder
 */
public class BenchmarkRunner {

  /**
   * Default regex pattern to include all benchmarks in the package.
   *
   * <p>This pattern matches any class in the {@code io.github.yasmramos.tailwindfx.benchmark}
   * package, ensuring that new benchmarks are automatically discovered and executed.
   */
  private static final String BENCHMARK_PATTERN =
      "io\\.github\\.yasmramos\\.tailwindfx\\.benchmark\\..*";

  /**
   * Default number of warmup iterations.
   *
   * <p>Each iteration runs for 1 second as configured by {@code warmupTime}.
   */
  private static final int DEFAULT_WARMUP_ITERATIONS = 5;

  /**
   * Default number of measurement iterations.
   *
   * <p>Each iteration runs for 1 second as configured by {@code measurementTime}.
   */
  private static final int DEFAULT_MEASUREMENT_ITERATIONS = 5;

  /**
   * Default number of forks (separate JVM processes).
   *
   * <p>Multiple forks help ensure result reliability by running benchmarks in isolated
   * environments.
   */
  private static final int DEFAULT_FORKS = 2;

  /** Default warmup time per iteration in seconds. */
  private static final int WARMUP_TIME_SECONDS = 1;

  /** Default measurement time per iteration in seconds. */
  private static final int MEASUREMENT_TIME_SECONDS = 1;

  /**
   * Main entry point for running benchmarks programmatically.
   *
   * <p>If command-line arguments are provided, delegates to {@link org.openjdk.jmh.Main#main} to
   * preserve CLI flexibility. If no arguments are provided, uses the default configuration defined
   * by this class.
   *
   * @param args command-line arguments; if empty, uses default configuration; otherwise delegates
   *     to JMH Main
   * @throws RunnerException if benchmark execution fails
   */
  public static void main(String[] args) throws RunnerException {
    if (args.length > 0) {
      // Delegate to JMH Main when arguments are provided for CLI flexibility
      try {
        org.openjdk.jmh.Main.main(args);
      } catch (IOException e) {
        throw new RunnerException("Failed to run benchmarks", e);
      }
    } else {
      // Use default programmatic configuration
      runWithDefaults();
    }
  }

  /**
   * Runs all benchmarks with the default configuration.
   *
   * <p>This method configures and executes all benchmarks matching the package pattern with
   * reproducible defaults for warmup, measurement, and forking. Results are output to both console
   * and a JSON file.
   *
   * @throws RunnerException if benchmark execution fails
   */
  public static void runWithDefaults() throws RunnerException {
    Options options = buildDefaultOptions();
    new Runner(options).run();
  }

  /**
   * Builds the default JMH options for running benchmarks.
   *
   * <p>Configuration includes:
   *
   * <ul>
   *   <li>Benchmark pattern matching all classes in the benchmark package
   *   <li>5 warmup iterations (1 second each)
   *   <li>5 measurement iterations (1 second each)
   *   <li>2 forks for reliability
   *   <li>JSON output to {@code target/jmh-results.json}
   *   <li>Fails on error to catch benchmark failures
   * </ul>
   *
   * @return configured JMH {@link Options}
   */
  private static Options buildDefaultOptions() {
    // Ensure the target directory exists for results output
    ensureTargetDirectoryExists();

    return new OptionsBuilder()
        .include(BENCHMARK_PATTERN)
        .warmupIterations(DEFAULT_WARMUP_ITERATIONS)
        .measurementIterations(DEFAULT_MEASUREMENT_ITERATIONS)
        .forks(DEFAULT_FORKS)
        .warmupTime(TimeValue.seconds(WARMUP_TIME_SECONDS))
        .measurementTime(TimeValue.seconds(MEASUREMENT_TIME_SECONDS))
        .resultFormat(ResultFormatType.JSON)
        .result("target/jmh-results.json")
        .shouldFailOnError(true)
        .build();
  }

  /**
   * Ensures the target directory exists for writing benchmark results.
   *
   * <p>Creates the directory if it doesn't exist. This is necessary because JMH may not
   * automatically create parent directories for the results file path.
   */
  private static void ensureTargetDirectoryExists() {
    try {
      Path targetDir = Path.of("target");
      if (!Files.exists(targetDir)) {
        Files.createDirectories(targetDir);
      }
    } catch (Exception e) {
      // Silently ignore - JMH will handle errors when writing results
      System.err.println("Warning: Could not ensure target directory exists: " + e.getMessage());
    }
  }
}
