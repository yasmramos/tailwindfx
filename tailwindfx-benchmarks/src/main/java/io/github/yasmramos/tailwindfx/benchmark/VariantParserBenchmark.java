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
 * <p>Measures parsing performance for tokens with no variants, single variant, multiple variants,
 * and arbitrary variants. Characterizes the cost of variant extraction and utility separation.
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

  /** Token without any variant: bg-blue-500 */
  @Param({"bg-blue-500", "text-white", "rounded-lg", "flex"})
  private String noVariantToken;

  /** Token with a single variant: hover:bg-blue-500 */
  @Param({"hover:bg-blue-500", "focus:text-white", "md:flex", "dark:bg-gray-900"})
  private String singleVariantToken;

  /** Token with multiple variants: md:hover:focus:bg-blue-700 */
  @Param({
    "md:hover:bg-blue-700",
    "hover:focus:text-gray-900",
    "sm:dark:flex",
    "lg:hover:rounded-lg"
  })
  private String multipleVariantsToken;

  /** Token with complex multiple variants: md:hover:focus:bg-blue-700 */
  @Param({
    "md:hover:focus:bg-blue-700",
    "sm:dark:hover:text-white",
    "lg:focus:valid:border-green-500"
  })
  private String complexVariantsToken;

  /** Token with arbitrary variant: [@media(min-width:768px)]:w-full */
  @Param({
    "[@media(min-width:768px)]:w-full",
    "[&:hover]:bg-blue-500",
    "[@supports(display:grid)]:grid"
  })
  private String arbitraryVariantToken;

  /**
   * Benchmarks parsing of tokens without variants.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkNoVariant(Blackhole blackhole) {
    VariantParser.VariantResult result = VariantParser.parse(noVariantToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks parsing of tokens with a single variant.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkSingleVariant(Blackhole blackhole) {
    VariantParser.VariantResult result = VariantParser.parse(singleVariantToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks parsing of tokens with multiple variants.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkMultipleVariants(Blackhole blackhole) {
    VariantParser.VariantResult result = VariantParser.parse(multipleVariantsToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks parsing of tokens with complex multiple variants.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkComplexVariants(Blackhole blackhole) {
    VariantParser.VariantResult result = VariantParser.parse(complexVariantsToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks parsing of tokens with arbitrary variants.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkArbitraryVariant(Blackhole blackhole) {
    VariantParser.VariantResult result = VariantParser.parse(arbitraryVariantToken);
    blackhole.consume(result);
  }

  /**
   * Benchmarks extractVariants method performance.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkExtractVariants(Blackhole blackhole) {
    List<String> variants = VariantParser.extractVariants(multipleVariantsToken);
    blackhole.consume(variants);
  }

  /**
   * Benchmarks hasVariant method performance.
   *
   * @param blackhole JMH blackhole to consume results and prevent dead-code elimination
   */
  @Benchmark
  public void benchmarkHasVariant(Blackhole blackhole) {
    boolean hasHover = VariantParser.hasVariant(singleVariantToken, "hover");
    blackhole.consume(hasHover);
  }
}
