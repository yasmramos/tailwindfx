package io.github.yasmramos.tailwindfx.benchmark;

import io.github.yasmramos.tailwindfx.core.StyleResolver;
import io.github.yasmramos.tailwindfx.style.StyleToken;
import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * JMH-based benchmarks for StyleResolver.resolve() method.
 *
 * <p>Measures resolution performance for different token kinds: SCALE, COLOR_SHADE (with and
 * without alpha), ARBITRARY, and NAMED. Pre-parses tokens in setup to isolate resolution cost from
 * parsing cost.
 *
 * <h2>Usage:</h2>
 *
 * <pre>{@code
 * # Run all StyleResolver benchmarks
 * java -jar tailwindfx-benchmarks/target/benchmarks.jar StyleResolverBenchmark
 *
 * # Run specific benchmark for color resolution
 * java -jar tailwindfx-benchmarks/target/benchmarks.jar StyleResolverBenchmark.*Color*
 * }</pre>
 *
 * @author yasmramos
 * @since 0.1.0
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
public class StyleResolverBenchmark {

  /** Pre-parsed SCALE tokens: p-4, m-2, etc. */
  @Param({"p-4", "m-2", "w-12", "gap-8"})
  private String scaleTokenStr;

  /** Pre-parsed COLOR_SHADE tokens without alpha: bg-blue-500, text-gray-900, etc. */
  @Param({"bg-blue-500", "text-gray-900", "border-red-300"})
  private String colorShadeTokenStr;

  /** Pre-parsed COLOR_SHADE tokens with alpha: bg-blue-500/80, text-gray-900/50, etc. */
  @Param({"bg-blue-500/80", "text-gray-900/50", "border-red-300/75"})
  private String colorShadeAlphaTokenStr;

  /** Pre-parsed ARBITRARY tokens: w-[320px], bg-[#ff6600], etc. */
  @Param({"w-[320px]", "bg-[#ff6600]", "p-[13px]"})
  private String arbitraryTokenStr;

  /** Pre-parsed NAMED tokens: text-sm, rounded-lg, font-bold, etc. */
  @Param({"text-sm", "rounded-lg", "font-bold", "shadow-md"})
  private String namedTokenStr;

  /** StyleResolver instance with default theme config. */
  private StyleResolver resolver;

  /** Pre-parsed tokens for each kind. */
  private StyleToken scaleToken;

  private StyleToken colorShadeToken;
  private StyleToken colorShadeAlphaToken;
  private StyleToken arbitraryToken;
  private StyleToken namedToken;

  /**
   * Setup runs once per trial to create the resolver and pre-parse all tokens. This isolates
   * resolution cost from parsing cost.
   */
  @Setup(Level.Trial)
  public void setup() {
    ThemeConfig themeConfig = ThemeConfig.defaultConfig();
    resolver = new StyleResolver(themeConfig);

    // Pre-parse tokens to exclude parsing time from resolution measurement
    scaleToken = StyleToken.parse(scaleTokenStr);
    colorShadeToken = StyleToken.parse(colorShadeTokenStr);
    colorShadeAlphaToken = StyleToken.parse(colorShadeAlphaTokenStr);
    arbitraryToken = StyleToken.parse(arbitraryTokenStr);
    namedToken = StyleToken.parse(namedTokenStr);
  }

  /**
   * Benchmarks resolution of SCALE tokens (e.g., p-4 → "16px").
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkScaleResolve(Blackhole blackhole) {
    String result = resolver.resolve(scaleToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks resolution of COLOR_SHADE tokens without alpha (e.g., bg-blue-500 →
   * "rgb(59,130,246)").
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkColorShadeResolve(Blackhole blackhole) {
    String result = resolver.resolve(colorShadeToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks resolution of COLOR_SHADE tokens with alpha (e.g., bg-blue-500/80 →
   * "rgba(59,130,246,0.8)").
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkColorShadeAlphaResolve(Blackhole blackhole) {
    String result = resolver.resolve(colorShadeAlphaToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks resolution of ARBITRARY tokens (e.g., w-[320px] → "320px").
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkArbitraryResolve(Blackhole blackhole) {
    String result = resolver.resolve(arbitraryToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks resolution of NAMED tokens (e.g., text-sm → "0.875rem").
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkNamedResolve(Blackhole blackhole) {
    String result = resolver.resolve(namedToken);
    blackhole.consume(result);
  }
}
