package io.github.yasmramos.tailwindfx.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ManualLruCache — A thread-safe, lock-free LRU cache implementation with bounded size.
 *
 * <p>This cache uses a combination of ConcurrentHashMap for storage and a secondary access-order
 * tracking map to implement LRU eviction without external dependencies.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Thread-safe concurrent reads and writes
 *   <li>Automatic LRU eviction when size exceeds maximum
 *   <li>Lock-free reads for optimal performance
 *   <li>Bounded memory usage for long-running applications
 *   <li>No external dependencies (pure Java)
 * </ul>
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * ManualLruCache<String, CompileResult> cache = new ManualLruCache<>(2000);
 * cache.put("p-4", result);
 * CompileResult cached = cache.get("p-4");
 * }</pre>
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 * @author yasmramos
 * @since 1.0.0
 */
public class ManualLruCache<K, V> {

  /** Maximum number of entries to keep in the cache. */
  private final int maxSize;

  /** Thread-safe storage for cache entries. */
  private final ConcurrentHashMap<K, CacheEntry<V>> storage;

  /** Access counter for triggering periodic cleanup. */
  private final AtomicInteger accessCounter;

  /** Interval at which to check for cleanup (number of accesses). */
  private static final int CLEANUP_INTERVAL = 100;

  /** Overhead factor for cleanup trigger (cleanup when size > maxSize * 1.2). */
  private static final double OVERHEAD_FACTOR = 1.2;

  /**
   * Creates a new LRU cache with the specified maximum size.
   *
   * @param maxSize the maximum number of entries to keep in the cache
   * @throws IllegalArgumentException if maxSize is less than or equal to zero
   */
  public ManualLruCache(int maxSize) {
    if (maxSize <= 0) {
      throw new IllegalArgumentException("Max size must be positive: " + maxSize);
    }
    this.maxSize = maxSize;
    this.storage = new ConcurrentHashMap<>(Math.min(256, maxSize));
    this.accessCounter = new AtomicInteger(0);
  }

  /**
   * Gets a value from the cache by key.
   *
   * <p>This method updates the access order for LRU tracking and may trigger a cleanup if the cache
   * has grown too large.
   *
   * @param key the key whose associated value is to be returned
   * @return the value associated with the key, or null if not present
   */
  public V get(K key) {
    CacheEntry<V> entry = storage.get(key);
    if (entry != null) {
      // Update access timestamp for LRU tracking
      entry.touch();
      maybeCleanup();
      return entry.value;
    }
    return null;
  }

  /**
   * Puts a key-value pair into the cache.
   *
   * <p>If the key already exists, the value is updated and access order is refreshed. If the cache
   * exceeds its maximum size after this operation, cleanup is triggered.
   *
   * @param key the key to associate with the value
   * @param value the value to store
   * @return the previous value associated with the key, or null if none
   */
  public V put(K key, V value) {
    CacheEntry<V> newEntry = new CacheEntry<>(value);

    CacheEntry<V> oldEntry = storage.put(key, newEntry);

    // Check if cleanup is needed - trigger immediately when over threshold
    if (storage.size() > (int) (maxSize * OVERHEAD_FACTOR)) {
      cleanup();
    } else if (storage.size() > maxSize) {
      // Also cleanup if we're over max but under overhead threshold
      cleanup();
    }

    return oldEntry != null ? oldEntry.value : null;
  }

  /**
   * Puts a key-value pair into the cache only if the key is not already present.
   *
   * @param key the key to associate with the value
   * @param value the value to store
   * @return the existing value if present, or null if the new value was stored
   */
  public V putIfAbsent(K key, V value) {
    CacheEntry<V> newEntry = new CacheEntry<>(value);
    CacheEntry<V> oldEntry = storage.putIfAbsent(key, newEntry);

    if (oldEntry == null) {
      // New entry was added, check if cleanup is needed
      if (storage.size() > (int) (maxSize * OVERHEAD_FACTOR)) {
        cleanup();
      }
      return null;
    }

    return oldEntry.value;
  }

  /**
   * Removes a key from the cache.
   *
   * @param key the key to remove
   * @return the previous value associated with the key, or null if none
   */
  public V remove(K key) {
    CacheEntry<V> entry = storage.remove(key);
    return entry != null ? entry.value : null;
  }

  /**
   * Checks if the cache contains a value for the given key.
   *
   * @param key the key to check
   * @return true if the key is present, false otherwise
   */
  public boolean containsKey(K key) {
    return storage.containsKey(key);
  }

  /**
   * Returns the current number of entries in the cache.
   *
   * @return the number of entries
   */
  public int size() {
    return storage.size();
  }

  /**
   * Returns the maximum size of the cache.
   *
   * @return the maximum number of entries
   */
  public int getMaxSize() {
    return maxSize;
  }

  /** Clears all entries from the cache. */
  public void clear() {
    storage.clear();
    accessCounter.set(0);
  }

  /**
   * Checks if the cache is empty.
   *
   * @return true if the cache contains no entries, false otherwise
   */
  public boolean isEmpty() {
    return storage.isEmpty();
  }

  /**
   * Triggers cleanup if enough accesses have occurred since the last check. This is a lightweight
   * check that avoids unnecessary synchronization.
   */
  private void maybeCleanup() {
    int count = accessCounter.incrementAndGet();
    if (count % CLEANUP_INTERVAL == 0 && storage.size() > maxSize) {
      cleanup();
    }
  }

  /**
   * Performs LRU eviction to bring the cache size back under the maximum.
   *
   * <p>This method removes the least recently accessed entries until the cache size is within the
   * limit. It uses a snapshot-based approach to avoid holding locks during iteration.
   */
  private void cleanup() {
    if (storage.size() <= maxSize) {
      return;
    }

    // Find and remove the oldest entries based on access time
    // We need to remove enough entries to get under the limit
    int toRemove = storage.size() - maxSize;

    // Get a snapshot of entries sorted by access time
    storage.entrySet().stream()
        .sorted(
            (e1, e2) -> Long.compare(e1.getValue().lastAccessTime, e2.getValue().lastAccessTime))
        .limit(toRemove)
        .forEach(entry -> storage.remove(entry.getKey()));
  }

  /**
   * Returns statistics about the cache state.
   *
   * @return a CacheStats record with current metrics
   */
  public CacheStats getStats() {
    return new CacheStats(size(), maxSize, accessCounter.get());
  }

  /**
   * Internal cache entry that tracks access time for LRU eviction.
   *
   * @param <V> the type of the cached value
   */
  private static class CacheEntry<V> {
    final V value;
    volatile long lastAccessTime;

    CacheEntry(V value) {
      this.value = value;
      this.lastAccessTime = System.nanoTime();
    }

    /** Updates the access timestamp to the current time. */
    void touch() {
      this.lastAccessTime = System.nanoTime();
    }
  }

  /**
   * Cache statistics snapshot.
   *
   * @param currentSize current number of entries in the cache
   * @param maxSize maximum allowed size
   * @param totalAccesses total number of cache accesses since creation
   */
  public record CacheStats(int currentSize, int maxSize, int totalAccesses) {}
}
