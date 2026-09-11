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

## Benchmark Types

### Cache Miss Benchmark
Measures compilation time when the token is not in the cache (cold compilation).

### Cache Hit Benchmark  
Measures compilation time when the token is already cached (warm compilation).

### Mixed Workload Benchmark
Alternates between cache hits and misses (50/50) to simulate real-world usage.

### Throughput Benchmark
Measures compilations per second to assess overall throughput.

## Interpreting Results

Results are reported in two modes:
- **Average Time** (`avgt`): Lower is better (time per operation in milliseconds)
- **Throughput** (`thrpt`): Higher is better (operations per second)

Example output:
```
Benchmark                    Mode  Cnt     Score    Error   Units
JitCompilerBenchmark.benchmarkCacheHit     avgt    5  0.001234 ± 0.000123  ms/op
JitCompilerBenchmark.benchmarkCacheMiss    avgt    5  0.098765 ± 0.005432  ms/op
JitCompilerBenchmark.benchmarkThroughput  thrpt    5 85000.123 ± 5000.456  ops/sec
```

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
