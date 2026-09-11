# TailwindFX JMH Benchmarks

This module contains [JMH (Java Microbenchmark Harness)](http://openjdk.java.net/projects/code-tools/jmh/) benchmarks for measuring the performance of the TailwindFX JIT Compiler.

## Why JMH?

The previous benchmark implementation used `System.nanoTime()` with fragile timing assertions that failed intermittently in CI environments due to:
- JVM warmup effects
- CPU throttling
- Resource contention
- Dead-code elimination by the JIT compiler

JMH addresses these issues by:
- Proper JVM warmup iterations
- Statistical measurement over multiple iterations
- Forking separate JVM processes
- Using `Blackhole` to prevent dead-code elimination
- Accounting for GC effects

## Building

Build the benchmarks module (requires `-P benchmarks` profile):

```bash
mvn -P benchmarks package
```

This generates an executable JAR at `tailwindfx-benchmarks/target/benchmarks.jar`.

## Running Benchmarks

### Run all benchmarks

```bash
java -jar tailwindfx-benchmarks/target/benchmarks.jar
```

### Run specific benchmark

```bash
java -jar tailwindfx-benchmarks/target/benchmarks.jar JitCompilerBenchmark.benchmarkCacheHit
```

### Run with custom options

```bash
java -jar tailwindfx-benchmarks/target/benchmarks.jar -f 2 -wi 5 -i 5
```

Common JMH options:
- `-f <forks>`: Number of forks (default: 1)
- `-wi <iterations>`: Warmup iterations
- `-i <iterations>`: Measurement iterations
- `-t <threads>`: Number of threads
- `-r <time>`: Time per iteration (e.g., `1s`, `500ms`)
- `-v EXTRA`: Verbose output

### List available benchmarks

```bash
java -jar tailwindfx-benchmarks/target/benchmarks.jar -lp
```

## Benchmark Classes

### JitCompilerBenchmark
Measures the performance of `JitCompiler.compile()` with different cache scenarios:
- **benchmarkCacheHit**: Compilation with warm cache (tokens pre-populated)
- **benchmarkCacheMiss**: Compilation with cold cache (cache cleared before each invocation)
- **benchmarkMixedWorkload**: Alternating hits and misses (50/50)
- **benchmarkCacheHitThroughput**: Throughput measurement for cache hits (ops/s)
- **benchmarkThroughput**: Overall compilation throughput (ops/s)

### StyleTokenParseBenchmark
Measures the performance of `StyleToken.parse()` for different token kinds:
- **benchmarkScaleParse**: SCALE tokens (e.g., p-4, m-2, w-12)
- **benchmarkColorShadeParse**: COLOR_SHADE tokens without alpha (e.g., bg-blue-500)
- **benchmarkColorShadeAlphaParse**: COLOR_SHADE tokens with alpha (e.g., bg-blue-500/80)
- **benchmarkArbitraryParse**: ARBITRARY tokens (e.g., w-[320px], bg-[#ff6600])
- **benchmarkNamedParse**: NAMED tokens (e.g., text-sm, rounded-lg)
- **benchmarkUnknownParse**: UNKNOWN/invalid tokens

### VariantParserBenchmark
Measures the performance of `VariantParser.parse()` for different variant scenarios:
- **benchmarkNoVariant**: Tokens without variants (e.g., bg-blue-500)
- **benchmarkSingleVariant**: Tokens with one variant (e.g., hover:bg-blue-500)
- **benchmarkMultipleVariants**: Tokens with multiple variants (e.g., md:hover:bg-blue-700)
- **benchmarkComplexVariants**: Tokens with complex variant chains
- **benchmarkArbitraryVariant**: Tokens with arbitrary variants (e.g., [@media(min-width:768px)]:w-full)
- **benchmarkExtractVariants**: Performance of extractVariants() method
- **benchmarkHasVariant**: Performance of hasVariant() method

### StyleResolverBenchmark
Measures the performance of `StyleResolver.resolve()` for different token kinds:
- **benchmarkScaleResolve**: Resolving SCALE tokens to pixel values
- **benchmarkColorShadeResolve**: Resolving COLOR_SHADE tokens to RGB values
- **benchmarkColorShadeAlphaResolve**: Resolving COLOR_SHADE tokens with alpha to RGBA values
- **benchmarkArbitraryResolve**: Resolving ARBITRARY tokens
- **benchmarkNamedResolve**: Resolving NAMED tokens

Note: Tokens are pre-parsed in setup to isolate resolution cost from parsing cost.

### ThemeCssGeneratorBenchmark
Measures the performance of `ThemeCssGenerator.generateBaseCss()`:
- **benchmarkGenerateBaseCss**: Time to generate full CSS with all variables
- **benchmarkGenerateBaseCssThroughput**: CSS generations per second

This is a heavier operation that generates all color, spacing, font-size, border-radius, opacity, and shadow variables. Useful for characterizing startup cost.

## Interpreting Results

Results are reported in two modes:
- **Average Time** (`avgt`): Lower is better (time per operation in milliseconds)
- **Throughput** (`thrpt`): Higher is better (operations per second)

Example output (actual results from hardened configuration with `@Fork(3)`, JDK 17):
```
Benchmark                                          Mode  Cnt         Score        Error   Units
JitCompilerBenchmark.benchmarkCacheHitThroughput  thrpt   15  22084163.924 ± 222820.028   ops/s
JitCompilerBenchmark.benchmarkThroughput          thrpt   15   1889995.571 ± 152869.049   ops/s
JitCompilerBenchmark.benchmarkCacheHit             avgt   15        ≈ 10⁻⁴                 ms/op
JitCompilerBenchmark.benchmarkCacheMiss            avgt   15         0.001 ±      0.001   ms/op
JitCompilerBenchmark.benchmarkMixedWorkload        avgt   15         0.002 ±      0.001   ms/op
```

Key findings:
- Cache hit throughput: ~22M operations per second
- Cache miss throughput: ~1.9M operations per second
- Cache hits are approximately 10x faster than cache misses

## CI Integration

**Note:** Benchmarks are NOT executed in CI by default because:
1. They are slow (multiple seconds per benchmark)
2. Results vary based on CI runner load
3. They are meant for manual performance verification, not correctness testing

To run benchmarks in CI (e.g., for performance regression testing), activate the profile:
```bash
mvn -P benchmarks test
```

## Adding New Benchmarks

1. Create a new class in `src/main/java/io/github/yasmramos/tailwindfx/benchmark/`
2. Annotate with JMH annotations (`@Benchmark`, `@State`, `@Setup`, etc.)
3. Use `Blackhole` to consume results
4. Rebuild with `mvn -P benchmarks package`

See `JitCompilerBenchmark.java` for examples.

## References

- [JMH Official Documentation](http://openjdk.java.net/projects/code-tools/jmh/)
- [JMH Samples](https://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/)
- [How to Write a Good Java Benchmark](https://shipilev.net/blog/2014/nanotrusting-nanotime/)
