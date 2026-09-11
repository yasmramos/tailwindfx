package io.github.yasmramos.tailwindfx.benchmark;

import io.github.yasmramos.tailwindfx.style.StyleToken;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * JMH-based benchmarks for StyleToken.parse() method.
 *
 * <p>Measures parsing performance for different token kinds: SCALE, COLOR_SHADE, ARBITRARY, NAMED,
 * and UNKNOWN. Uses parametrized tokens to characterize the cost of each regex pattern.
 *
 * <h2>Usage:</h2>
 *
 * <pre>{@code
 * # Run all StyleToken parse benchmarks
 * java -jar tailwindfx-benchmarks/target/benchmarks.jar StyleTokenParseBenchmark
 *
 * # Run specific benchmark for ARBITRARY tokens
 * java -jar tailwindfx-benchmarks/target/benchmarks.jar StyleTokenParseBenchmark.*Arbitrary*
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
public class StyleTokenParseBenchmark {

  /** Test tokens for SCALE kind: p-4, m-2, w-12, etc. */
  @Param({"p-4", "m-2", "w-12", "gap-8", "h-auto"})
  private String scaleToken;

  /** Test tokens for COLOR_SHADE kind: bg-blue-500, text-gray-900, etc. */
  @Param({"bg-blue-500", "text-gray-900", "border-red-300", "shadow-blue-500"})
  private String colorShadeToken;

  /** Test tokens for COLOR_SHADE with alpha: bg-blue-500/80, text-gray-900/50, etc. */
  @Param({"bg-blue-500/80", "text-gray-900/50", "border-red-300/75"})
  private String colorShadeAlphaToken;

  /** Test tokens for ARBITRARY kind: w-[320px], bg-[#ff6600], etc. */
  @Param({"w-[320px]", "bg-[#ff6600]", "p-[13px]", "text-[16px]", "rotate-[45deg]"})
  private String arbitraryToken;

  /** Test tokens for NAMED kind: text-sm, rounded-lg, font-bold, etc. */
  @Param({"text-sm", "rounded-lg", "font-bold", "shadow-md", "flex"})
  private String namedToken;

  /** Test tokens for UNKNOWN kind: invalid or malformed tokens. */
  @Param({"invalid-token-", "not-a-class", "123invalid", ""})
  private String unknownToken;

  /**
   * Benchmarks parsing of SCALE tokens (e.g., p-4, m-2).
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkScaleParse(Blackhole blackhole) {
    StyleToken result = StyleToken.parse(scaleToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks parsing of COLOR_SHADE tokens without alpha (e.g., bg-blue-500).
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkColorShadeParse(Blackhole blackhole) {
    StyleToken result = StyleToken.parse(colorShadeToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks parsing of COLOR_SHADE tokens with alpha (e.g., bg-blue-500/80).
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkColorShadeAlphaParse(Blackhole blackhole) {
    StyleToken result = StyleToken.parse(colorShadeAlphaToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks parsing of ARBITRARY tokens (e.g., w-[320px], bg-[#ff6600]).
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkArbitraryParse(Blackhole blackhole) {
    StyleToken result = StyleToken.parse(arbitraryToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks parsing of NAMED tokens (e.g., text-sm, rounded-lg).
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkNamedParse(Blackhole blackhole) {
    StyleToken result = StyleToken.parse(namedToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks parsing of UNKNOWN/invalid tokens.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkUnknownParse(Blackhole blackhole) {
    StyleToken result = StyleToken.parse(unknownToken);
    blackhole.consume(result);
  }
}
