package io.github.yasmramos.tailwindfx.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * LruCache — A thread-safe, generic LRU (Least Recently Used) cache implementation.
 *
 * <p>This cache provides bounded storage with automatic eviction of the least recently used entries
 * when the capacity is exceeded. It is designed for high-concurrency scenarios with minimal lock
 * contention.
 *
 * <h3>Key Features:</h3>
 *
 * <ul>
 *   <li><b>Thread-safe:</b> Uses read-write locks for concurrent access
 *   <li><b>LRU eviction:</b> Automatically removes least recently used entries
 *   <li><b>Bounded capacity:</b> Prevents unbounded memory growth
 *   <li><b>Generic:</b> Works with any key/value types
 *   <li><b>Metrics:</b> Tracks hits, misses, and evictions
 * </ul>
 *
 * <h3>Performance Characteristics:</h3>
 *
 * <ul>
 *   <li>Get: O(1) average case
 *   <li>Put: O(1) average case
 *   <li>Remove: O(1) average case
 * </ul>
 *
 * <h3>Usage Example:</h3>
 *
 * <pre>{@code
 * // Create a cache with capacity of 1000 entries
 * LruCache<String, CompiledStyle> cache = new LruCache<>(1000);
 *
 * // Put values
 * cache.put("p-4", new CompiledStyle("-fx-padding: 16px"));
 *
 * // Get values (returns null if not present)
 * CompiledStyle style = cache.get("p-4");
 *
 * // Compute if absent (atomic operation)
 * CompiledStyle result = cache.computeIfAbsent("w-[320px]",
 *     key -> compileStyle(key));
 *
 * // Get metrics
 * System.out.println("Hit ratio: " + cache.getHitRatio());
 * }</pre>
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 * @author TailwindFX Team
 * @version 3.0
 */
public class LruCache<K, V> {

  /** The underlying LinkedHashMap that maintains insertion/access order */
  private final LinkedHashMap<K, V> map;

  /** Maximum number of entries the cache can hold */
  private final int maxCapacity;

  /** Read-write lock for thread-safe operations */
  private final ReentrantReadWriteLock lock;

  /** Read lock view for concurrent reads */
  private final ReentrantReadWriteLock.ReadLock readLock;

  /** Write lock view for exclusive writes */
  private final ReentrantReadWriteLock.WriteLock writeLock;

  /** Cache statistics */
  private volatile long hitCount;

  private volatile long missCount;
  private volatile long evictionCount;

  /**
   * Creates a new LRU cache with the specified capacity.
   *
   * @param maxCapacity the maximum number of entries this cache can hold
   * @throws IllegalArgumentException if maxCapacity is less than or equal to zero
   */
  public LruCache(int maxCapacity) {
    if (maxCapacity <= 0) {
      throw new IllegalArgumentException("LruCache: maxCapacity must be > 0, got: " + maxCapacity);
    }
    this.maxCapacity = maxCapacity;
    this.map =
        new LinkedHashMap<K, V>(maxCapacity, 0.75f, true) {
          @Override
          protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            boolean shouldRemove = size() > maxCapacity;
            if (shouldRemove) {
              evictionCount++;
            }
            return shouldRemove;
          }
        };
    this.lock = new ReentrantReadWriteLock();
    this.readLock = lock.readLock();
    this.writeLock = lock.writeLock();
    this.hitCount = 0;
    this.missCount = 0;
    this.evictionCount = 0;
  }

  /**
   * Retrieves the value associated with the given key from the cache.
   *
   * <p>If the key exists in the cache, it becomes the most recently used entry. This method updates
   * cache statistics (hit/miss counts).
   *
   * @param key the key whose associated value is to be returned
   * @return the value associated with the key, or {@code null} if the key is not present
   * @throws IllegalArgumentException if key is null
   */
  public V get(K key) {
    if (key == null) {
      throw new IllegalArgumentException("LruCache.get: key cannot be null");
    }

    readLock.lock();
    try {
      V value = map.get(key);
      if (value != null) {
        hitCount++;
      } else {
        missCount++;
      }
      return value;
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Associates the specified value with the specified key in the cache.
   *
   * <p>If the cache already contains a mapping for the key, the old value is replaced. If adding
   * this entry would exceed the capacity, the least recently used entry is evicted.
   *
   * @param key the key with which the specified value is to be associated
   * @param value the value to be associated with the key
   * @return the previous value associated with the key, or {@code null} if there was no mapping
   * @throws IllegalArgumentException if key or value is null
   */
  public V put(K key, V value) {
    if (key == null) {
      throw new IllegalArgumentException("LruCache.put: key cannot be null");
    }
    if (value == null) {
      throw new IllegalArgumentException("LruCache.put: value cannot be null");
    }

    writeLock.lock();
    try {
      return map.put(key, value);
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Computes the value for a key if it is not already present in the cache.
   *
   * <p>This method atomically checks if the key exists, and if not, computes the value using the
   * provided mapping function and stores it in the cache. This is useful for lazy-loading expensive
   * computations.
   *
   * <p>The computation is performed while holding the write lock, ensuring thread-safety.
   *
   * @param key the key to compute a value for
   * @param mappingFunction the function to compute a value if the key is absent
   * @return the existing value if present, otherwise the computed value
   * @throws IllegalArgumentException if key or mappingFunction is null
   * @throws RuntimeException if the mappingFunction throws an exception
   */
  public V computeIfAbsent(
      K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
    if (key == null) {
      throw new IllegalArgumentException("LruCache.computeIfAbsent: key cannot be null");
    }
    if (mappingFunction == null) {
      throw new IllegalArgumentException(
          "LruCache.computeIfAbsent: mappingFunction cannot be null");
    }

    // Fast path: try read lock first
    readLock.lock();
    try {
      V value = map.get(key);
      if (value != null) {
        hitCount++;
        return value;
      }
      missCount++;
    } finally {
      readLock.unlock();
    }

    // Slow path: acquire write lock and compute
    writeLock.lock();
    try {
      // Double-check after acquiring write lock
      V value = map.get(key);
      if (value != null) {
        hitCount++;
        return value;
      }

      value = mappingFunction.apply(key);
      if (value != null) {
        map.put(key, value);
      }
      return value;
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Removes the mapping for a key from the cache.
   *
   * @param key the key whose mapping is to be removed
   * @return the previous value associated with the key, or {@code null} if there was no mapping
   * @throws IllegalArgumentException if key is null
   */
  public V remove(K key) {
    if (key == null) {
      throw new IllegalArgumentException("LruCache.remove: key cannot be null");
    }

    writeLock.lock();
    try {
      return map.remove(key);
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Removes all entries from the cache.
   *
   * <p>After this method returns, the cache will be empty and ready for reuse.
   */
  public void clear() {
    writeLock.lock();
    try {
      map.clear();
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Returns the number of entries currently in the cache.
   *
   * @return the current size of the cache
   */
  public int size() {
    readLock.lock();
    try {
      return map.size();
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Returns the maximum capacity of this cache.
   *
   * @return the maximum number of entries this cache can hold
   */
  public int getMaxCapacity() {
    return maxCapacity;
  }

  /**
   * Returns the total number of cache hits since creation or last reset.
   *
   * @return the hit count
   */
  public long getHitCount() {
    return hitCount;
  }

  /**
   * Returns the total number of cache misses since creation or last reset.
   *
   * @return the miss count
   */
  public long getMissCount() {
    return missCount;
  }

  /**
   * Returns the total number of cache evictions since creation or last reset.
   *
   * @return the eviction count
   */
  public long getEvictionCount() {
    return evictionCount;
  }

  /**
   * Returns the cache hit ratio as a percentage.
   *
   * <p>The hit ratio is calculated as: (hits / (hits + misses)) * 100
   *
   * @return the hit ratio as a percentage (0.0 to 100.0), or 0.0 if no accesses have occurred
   */
  public double getHitRatio() {
    long total = hitCount + missCount;
    if (total == 0) {
      return 0.0;
    }
    return (hitCount * 100.0) / total;
  }

  /**
   * Resets all cache statistics (hit count, miss count, eviction count).
   *
   * <p>This does not clear the cache contents, only the statistics.
   */
  public void resetStats() {
    writeLock.lock();
    try {
      hitCount = 0;
      missCount = 0;
      evictionCount = 0;
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Returns a snapshot of the current cache statistics.
   *
   * @return a CacheStats object containing current statistics
   */
  public CacheStats getStats() {
    readLock.lock();
    try {
      return new CacheStats(hitCount, missCount, evictionCount, size(), maxCapacity);
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Checks if the cache contains a mapping for the specified key.
   *
   * @param key the key to check for
   * @return {@code true} if the cache contains a mapping for the key
   * @throws IllegalArgumentException if key is null
   */
  public boolean containsKey(K key) {
    if (key == null) {
      throw new IllegalArgumentException("LruCache.containsKey: key cannot be null");
    }

    readLock.lock();
    try {
      return map.containsKey(key);
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Immutable snapshot of cache statistics at a point in time.
   *
   * @param hitCount total number of cache hits
   * @param missCount total number of cache misses
   * @param evictionCount total number of cache evictions
   * @param size current number of entries in the cache
   * @param maxCapacity maximum cache capacity
   */
  public record CacheStats(
      long hitCount, long missCount, long evictionCount, int size, int maxCapacity) {
    /**
     * Returns the hit ratio as a percentage.
     *
     * @return hit ratio (0.0 to 100.0)
     */
    public double hitRatio() {
      long total = hitCount + missCount;
      return total == 0 ? 0.0 : (hitCount * 100.0) / total;
    }

    /**
     * Returns the cache utilization as a percentage.
     *
     * @return utilization (0.0 to 100.0)
     */
    public double utilization() {
      return maxCapacity == 0 ? 0.0 : (size * 100.0) / maxCapacity;
    }

    @Override
    public String toString() {
      return String.format(
          "CacheStats[size=%d/%d, hits=%d, misses=%d, evictions=%d, hitRatio=%.2f%%]",
          size, maxCapacity, hitCount, missCount, evictionCount, hitRatio());
    }
  }
}
