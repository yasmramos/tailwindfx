package io.github.yasmramos.tailwindfx.benchmark;

import io.github.yasmramos.tailwindfx.core.ThemeCssGenerator;
import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
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
 * JMH-based benchmarks for ThemeCssGenerator.generateBaseCss() method.
 *
 * <p>Measures the performance of generating base CSS variables from ThemeConfig. This is a heavier
 * operation that generates all color, spacing, font-size, border-radius, opacity, and shadow
 * variables. Useful for characterizing startup cost of theme generation.
 *
 * <h2>Usage:</h2>
 *
 * <pre>{@code
 * # Run all ThemeCssGenerator benchmarks
 * java -jar tailwindfx-benchmarks/target/benchmarks.jar ThemeCssGeneratorBenchmark
 * }</pre>
 *
 * @author yasmramos
 * @since 0.1.0
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
public class ThemeCssGeneratorBenchmark {

  /** ThemeCssGenerator instance with default theme config. */
  private ThemeCssGenerator generator;

  /** Setup runs once per trial to create the generator with default theme config. */
  @Setup(Level.Trial)
  public void setup() {
    ThemeConfig themeConfig = ThemeConfig.defaultConfig();
    generator = new ThemeCssGenerator(themeConfig);
  }

  /**
   * Benchmarks generation of full base CSS including all variables (colors, spacing, font-sizes,
   * border-radius, opacity, shadows).
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkGenerateBaseCss(Blackhole blackhole) {
    String css = generator.generateBaseCss();
    blackhole.consume(css);
  }

  /**
   * Benchmarks throughput of CSS generation (generations per second).
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @return 1 to count each generation
   */
  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.SECONDS)
  public int benchmarkGenerateBaseCssThroughput(Blackhole blackhole) {
    String css = generator.generateBaseCss();
    blackhole.consume(css);
    return 1;
  }
}
