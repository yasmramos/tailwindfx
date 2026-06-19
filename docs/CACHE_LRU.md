# LRU Cache Implementation

## Overview

TailwindFX includes a high-performance, thread-safe LRU (Least Recently Used) cache implementation designed for concurrent access patterns in JavaFX applications.

## Architecture

### Thread Safety
- **Read-Write Locks**: Uses `ReentrantReadWriteLock` for minimal lock contention
- **Fast Path**: Read operations use shared read locks allowing concurrent access
- **Slow Path**: Write operations use exclusive write locks only when necessary

### LRU Eviction
- **LinkedHashMap**: Maintains access order automatically
- **Bounded Capacity**: Prevents unbounded memory growth
- **Automatic Eviction**: Least recently used entries are removed when capacity is exceeded

## API Reference

### Core Operations

```java
// Create cache with 1000 entry capacity
LruCache<String, CompiledStyle> cache = new LruCache<>(1000);

// Put a value
cache.put("p-4", new CompiledStyle("-fx-padding: 16px"));

// Get a value (returns null if not present)
CompiledStyle style = cache.get("p-4");

// Compute if absent (atomic lazy-loading)
CompiledStyle result = cache.computeIfAbsent("w-[320px]",
    key -> compileStyle(key));

// Remove a value
cache.remove("p-4");

// Clear all entries
cache.clear();
```

### Statistics & Metrics

```java
// Get hit/miss counts
long hits = cache.getHitCount();
long misses = cache.getMissCount();
long evictions = cache.getEvictionCount();

// Calculate hit ratio (percentage)
double hitRatio = cache.getHitRatio(); // 0.0 to 100.0

// Get comprehensive stats snapshot
LruCache.CacheStats stats = cache.getStats();
System.out.println(stats); 
// Output: CacheStats[size=500/1000, hits=1000, misses=50, evictions=10, hitRatio=95.24%]

// Reset statistics (keeps cache contents)
cache.resetStats();
```

### Performance Characteristics

| Operation | Time Complexity | Lock Type |
|-----------|----------------|-----------|
| `get()` | O(1) | Read (shared) |
| `put()` | O(1) | Write (exclusive) |
| `remove()` | O(1) | Write (exclusive) |
| `computeIfAbsent()` | O(1) avg | Read → Write |
| `clear()` | O(n) | Write (exclusive) |
| `size()` | O(1) | Read (shared) |

## Integration with JitCompiler

The JIT compiler uses the LRU cache to store compiled tokens:

```java
// In JitCompiler.java
private static final int MAX_CACHE_SIZE = 2_000;
private static final LruCache<String, CompileResult> CACHE = 
    new LruCache<>(MAX_CACHE_SIZE);

public static CompileResult compile(String token) {
    return CACHE.computeIfAbsent(token, JitCompiler::doCompile);
}
```

### Benefits
- **Thread-safe compilation**: Multiple threads can compile tokens concurrently
- **Bounded memory**: Cache never exceeds 2000 entries
- **Metrics tracking**: Monitor cache effectiveness via `TailwindFXMetrics`

## Best Practices

### 1. Choose Appropriate Capacity
```java
// Small app with limited utilities
LruCache<String, Style> smallCache = new LruCache<>(500);

// Large dashboard with many dynamic values
LruCache<String, Style> largeCache = new LruCache<>(5000);
```

### 2. Monitor Hit Ratio
```java
// Log cache performance periodically
Timer timer = new Timer();
timer.scheduleAtFixedRate(new TimerTask() {
    @Override
    public void run() {
        LruCache.CacheStats stats = cache.getStats();
        if (stats.hitRatio() < 80.0) {
            LOG.warning("Low cache hit ratio: " + stats.hitRatio() + "%");
        }
    }
}, 0, 60000); // Check every minute
```

### 3. Use computeIfAbsent for Lazy Loading
```java
// Instead of:
V value = cache.get(key);
if (value == null) {
    value = expensiveComputation(key);
    cache.put(key, value);
}

// Use:
V value = cache.computeIfAbsent(key, k -> expensiveComputation(k));
```

### 4. Clear Cache on Theme Changes
```java
// When switching themes, clear cached styles
ThemeManager.theme(scene).dark().apply();
JitCompiler.clearCache(); // Forces recompilation with new theme vars
```

## Example: Custom Cache with Expiration

```java
public class ExpiringCache<K, V> extends LruCache<K, V> {
    private final long ttlMillis;
    private final Map<K, Long> timestamps;
    
    public ExpiringCache(int capacity, long ttlMillis) {
        super(capacity);
        this.ttlMillis = ttlMillis;
        this.timestamps = new ConcurrentHashMap<>();
    }
    
    @Override
    public V get(K key) {
        Long timestamp = timestamps.get(key);
        if (timestamp != null && 
            System.currentTimeMillis() - timestamp > ttlMillis) {
            remove(key); // Expired
            return null;
        }
        return super.get(key);
    }
    
    @Override
    public V put(K key, V value) {
        timestamps.put(key, System.currentTimeMillis());
        return super.put(key, value);
    }
}
```

## See Also

- [JitCompiler](../src/main/java/io/github/yasmramos/tailwindfx/core/JitCompiler.java) - Uses LRU cache for token compilation
- [TailwindFXMetrics](../src/main/java/io/github/yasmramos/tailwindfx/metrics/TailwindFXMetrics.java) - Tracks cache hits/misses
- [StylePerf](../src/main/java/io/github/yasmramos/tailwindfx/style/StylePerf.java) - Additional performance optimizations
