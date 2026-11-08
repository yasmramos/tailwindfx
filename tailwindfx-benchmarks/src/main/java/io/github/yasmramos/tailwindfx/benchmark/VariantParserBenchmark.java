package io.github.yasmramos.tailwindfx.benchmark;

import io.github.yasmramos.tailwindfx.core.VariantParser;
import java.util.List;
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
 * JMH-based benchmarks for VariantParser.parse() method.
 *
 * <p>Measures parsing performance for tokens with no variants, a single variant, multiple variants
 * and arbitrary variants. Characterizes the cost of variant extraction and utility separation.
 *
 * <p>Each token category lives in its own {@link State}, so the per-category {@code @Param} values
 * are measured in isolation. Keeping the parameters in the benchmark class itself would form the
 * Cartesian product of every parameter group and blow up the run time.
 *
 * <h2>Usage:</h2>
 *
 * <pre>{@code
 * # Run all VariantParser benchmarks
 * java -jar tailwindfx-benchmarks/target/benchmarks.jar VariantParserBenchmark
 *
 * # Run specific benchmark for multiple variants
 * java -jar tailwindfx-benchmarks/target/benchmarks.jar VariantParserBenchmark.*Multiple*
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
public class VariantParserBenchmark {

  /** Holds tokens without any variant: bg-blue-500, text-white, ... */
  @State(Scope.Thread)
  public static class NoVariantState {
    @Param({"bg-blue-500", "text-white", "rounded-lg", "flex"})
    private String noVariantToken;
  }

  /** Holds tokens with a single variant: hover:bg-blue-500, focus:text-white, ... */
  @State(Scope.Thread)
  public static class SingleVariantState {
    @Param({"hover:bg-blue-500", "focus:text-white", "md:flex", "dark:bg-gray-900"})
    private String singleVariantToken;
  }

  /** Holds tokens with multiple variants: md:hover:bg-blue-700, sm:dark:flex, ... */
  @State(Scope.Thread)
  public static class MultipleVariantsState {
    @Param({
      "md:hover:bg-blue-700",
      "hover:focus:text-gray-900",
      "sm:dark:flex",
      "lg:hover:rounded-lg"
    })
    private String multipleVariantsToken;
  }

  /** Holds tokens with complex multiple variants: md:hover:focus:bg-blue-700, ... */
  @State(Scope.Thread)
  public static class ComplexVariantsState {
    @Param({
      "md:hover:focus:bg-blue-700",
      "sm:dark:hover:text-white",
      "lg:focus:valid:border-green-500"
    })
    private String complexVariantsToken;
  }

  /** Holds tokens with an arbitrary variant: [@media(min-width:768px)]:w-full, ... */
  @State(Scope.Thread)
  public static class ArbitraryVariantState {
    @Param({
      "[@media(min-width:768px)]:w-full",
      "[&:hover]:bg-blue-500",
      "[@supports(display:grid)]:grid"
    })
    private String arbitraryVariantToken;
  }

  /**
   * Benchmarks parsing of tokens without variants.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @param state the token without variants
   */
  @Benchmark
  public void benchmarkNoVariant(Blackhole blackhole, NoVariantState state) {
    VariantParser.VariantResult result = VariantParser.parse(state.noVariantToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks parsing of tokens with a single variant.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @param state the token with a single variant
   */
  @Benchmark
  public void benchmarkSingleVariant(Blackhole blackhole, SingleVariantState state) {
    VariantParser.VariantResult result = VariantParser.parse(state.singleVariantToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks parsing of tokens with multiple variants.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @param state the token with multiple variants
   */
  @Benchmark
  public void benchmarkMultipleVariants(Blackhole blackhole, MultipleVariantsState state) {
    VariantParser.VariantResult result = VariantParser.parse(state.multipleVariantsToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks parsing of tokens with complex multiple variants.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @param state the token with complex multiple variants
   */
  @Benchmark
  public void benchmarkComplexVariants(Blackhole blackhole, ComplexVariantsState state) {
    VariantParser.VariantResult result = VariantParser.parse(state.complexVariantsToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks parsing of tokens with arbitrary variants.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @param state the token with an arbitrary variant
   */
  @Benchmark
  public void benchmarkArbitraryVariant(Blackhole blackhole, ArbitraryVariantState state) {
    VariantParser.VariantResult result = VariantParser.parse(state.arbitraryVariantToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks extractVariants method performance.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @param state the token with multiple variants
   */
  @Benchmark
  public void benchmarkExtractVariants(Blackhole blackhole, MultipleVariantsState state) {
    List<String> variants = VariantParser.extractVariants(state.multipleVariantsToken);
    blackhole.consume(variants);
  }

  /**
   * Benchmarks hasVariant method performance.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   * @param state the token with a single variant
   */
  @Benchmark
  public void benchmarkHasVariant(Blackhole blackhole, SingleVariantState state) {
    boolean hasHover = VariantParser.hasVariant(state.singleVariantToken, "hover");
    blackhole.consume(hasHover);
  }
}
