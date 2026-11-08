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
 * <p>Measures resolution performance for the different token kinds: SCALE, COLOR_SHADE (with and
 * without alpha), ARBITRARY and NAMED. Tokens are pre-parsed in setup to isolate resolution cost
 * from parsing cost.
 *
 * <p>Each token kind lives in its own {@link State}, so the per-kind {@code @Param} values are
 * measured in isolation. Keeping the parameters in the benchmark class itself would form the
 * Cartesian product of every parameter group and blow up the run time.
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

  /** StyleResolver shared by every per-kind state. */
  private StyleResolver resolver;

  /** Setup runs once per trial to create the resolver used by all token-kind states. */
  @Setup(Level.Trial)
  public void setup() {
    resolver = new StyleResolver(ThemeConfig.defaultConfig());
  }

  /** Holds the pre-parsed SCALE tokens measured by {@link #benchmarkScaleResolve}. */
  @State(Scope.Thread)
  public static class ScaleState {
    @Param({"p-4", "m-2", "w-12", "gap-8"})
    private String scaleTokenStr;

    private StyleToken scaleToken;

    /** Pre-parses the SCALE token to exclude parsing time from resolution measurement. */
    @Setup(Level.Trial)
    public void setup() {
      scaleToken = StyleToken.parse(scaleTokenStr);
    }
  }

  /** Holds the pre-parsed COLOR_SHADE tokens measured by {@link #benchmarkColorShadeResolve}. */
  @State(Scope.Thread)
  public static class ColorShadeState {
    @Param({"bg-blue-500", "text-gray-900", "border-red-300"})
    private String colorShadeTokenStr;

    private StyleToken colorShadeToken;

    /** Pre-parses the COLOR_SHADE token to exclude parsing time from resolution measurement. */
    @Setup(Level.Trial)
    public void setup() {
      colorShadeToken = StyleToken.parse(colorShadeTokenStr);
    }
  }

  /**
   * Holds the pre-parsed COLOR_SHADE tokens with alpha measured by {@link
   * #benchmarkColorShadeAlphaResolve}.
   */
  @State(Scope.Thread)
  public static class ColorShadeAlphaState {
    @Param({"bg-blue-500/80", "text-gray-900/50", "border-red-300/75"})
    private String colorShadeAlphaTokenStr;

    private StyleToken colorShadeAlphaToken;

    /** Pre-parses the COLOR_SHADE+alpha token to exclude parsing time from the measurement. */
    @Setup(Level.Trial)
    public void setup() {
      colorShadeAlphaToken = StyleToken.parse(colorShadeAlphaTokenStr);
    }
  }

  /** Holds the pre-parsed ARBITRARY tokens measured by {@link #benchmarkArbitraryResolve}. */
  @State(Scope.Thread)
  public static class ArbitraryState {
    @Param({"w-[320px]", "bg-[#ff6600]", "p-[13px]"})
    private String arbitraryTokenStr;

    private StyleToken arbitraryToken;

    /** Pre-parses the ARBITRARY token to exclude parsing time from resolution measurement. */
    @Setup(Level.Trial)
    public void setup() {
      arbitraryToken = StyleToken.parse(arbitraryTokenStr);
    }
  }

  /** Holds the pre-parsed NAMED tokens measured by {@link #benchmarkNamedResolve}. */
  @State(Scope.Thread)
  public static class NamedState {
    @Param({"text-sm", "rounded-lg", "font-bold", "shadow-md"})
    private String namedTokenStr;

    private StyleToken namedToken;

    /** Pre-parses the NAMED token to exclude parsing time from resolution measurement. */
    @Setup(Level.Trial)
    public void setup() {
      namedToken = StyleToken.parse(namedTokenStr);
    }
  }

  /**
   * Benchmarks resolution of SCALE tokens (e.g., p-4 → "16px").
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @param state the pre-parsed SCALE token
   */
  @Benchmark
  public void benchmarkScaleResolve(Blackhole blackhole, ScaleState state) {
    blackhole.consume(resolver.resolve(state.scaleToken));
  }

  /**
   * Benchmarks resolution of COLOR_SHADE tokens without alpha (e.g., bg-blue-500 →
   * "rgb(59,130,246)").
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @param state the pre-parsed COLOR_SHADE token
   */
  @Benchmark
  public void benchmarkColorShadeResolve(Blackhole blackhole, ColorShadeState state) {
    blackhole.consume(resolver.resolve(state.colorShadeToken));
  }

  /**
   * Benchmarks resolution of COLOR_SHADE tokens with alpha (e.g., bg-blue-500/80 →
   * "rgba(59,130,246,0.8)").
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @param state the pre-parsed COLOR_SHADE+alpha token
   */
  @Benchmark
  public void benchmarkColorShadeAlphaResolve(Blackhole blackhole, ColorShadeAlphaState state) {
    blackhole.consume(resolver.resolve(state.colorShadeAlphaToken));
  }

  /**
   * Benchmarks resolution of ARBITRARY tokens (e.g., w-[320px] → "320px").
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @param state the pre-parsed ARBITRARY token
   */
  @Benchmark
  public void benchmarkArbitraryResolve(Blackhole blackhole, ArbitraryState state) {
    blackhole.consume(resolver.resolve(state.arbitraryToken));
  }

  /**
   * Benchmarks resolution of NAMED tokens (e.g., text-sm → "0.875rem").
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @param state the pre-parsed NAMED token
   */
  @Benchmark
  public void benchmarkNamedResolve(Blackhole blackhole, NamedState state) {
    blackhole.consume(resolver.resolve(state.namedToken));
  }
}
