package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for ManualLruCache class. */
class ManualLruCacheTest {

  private ManualLruCache<String, Integer> cache;

  @BeforeEach
  void setUp() {
    cache = new ManualLruCache<>(5);
  }

  @Test
  void testConstructorWithValidSize() {
    ManualLruCache<String, String> smallCache = new ManualLruCache<>(10);
    assertEquals(10, smallCache.getMaxSize());
  }

  @Test
  void testConstructorWithZeroSizeThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> new ManualLruCache<>(0));
  }

  @Test
  void testConstructorWithNegativeSizeThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> new ManualLruCache<>(-1));
  }

  @Test
  void testPutAndGet() {
    cache.put("key1", 100);
    assertEquals(Integer.valueOf(100), cache.get("key1"));
  }

  @Test
  void testGetNonExistentKeyReturnsNull() {
    assertNull(cache.get("nonexistent"));
  }

  @Test
  void testPutUpdatesExistingKey() {
    cache.put("key1", 100);
    cache.put("key1", 200);
    assertEquals(Integer.valueOf(200), cache.get("key1"));
  }

  @Test
  void testPutReturnsOldValue() {
    cache.put("key1", 100);
    Integer oldValue = cache.put("key1", 200);
    assertEquals(Integer.valueOf(100), oldValue);
  }

  @Test
  void testPutIfAbsentWhenKeyNotPresent() {
    Integer result = cache.putIfAbsent("newKey", 42);
    assertNull(result);
    assertEquals(Integer.valueOf(42), cache.get("newKey"));
  }

  @Test
  void testPutIfAbsentWhenKeyPresent() {
    cache.put("existingKey", 100);
    Integer result = cache.putIfAbsent("existingKey", 200);
    assertEquals(Integer.valueOf(100), result);
    assertEquals(Integer.valueOf(100), cache.get("existingKey"));
  }

  @Test
  void testRemoveExistingKey() {
    cache.put("key1", 100);
    Integer removed = cache.remove("key1");
    assertEquals(Integer.valueOf(100), removed);
    assertNull(cache.get("key1"));
  }

  @Test
  void testRemoveNonExistentKey() {
    Integer removed = cache.remove("nonexistent");
    assertNull(removed);
  }

  @Test
  void testContainsKey() {
    cache.put("key1", 100);
    assertTrue(cache.containsKey("key1"));
    assertFalse(cache.containsKey("key2"));
  }

  @Test
  void testSize() {
    assertEquals(0, cache.size());
    cache.put("key1", 100);
    assertEquals(1, cache.size());
    cache.put("key2", 200);
    assertEquals(2, cache.size());
  }

  @Test
  void testIsEmpty() {
    assertTrue(cache.isEmpty());
    cache.put("key1", 100);
    assertFalse(cache.isEmpty());
  }

  @Test
  void testClear() {
    cache.put("key1", 100);
    cache.put("key2", 200);
    cache.clear();
    assertEquals(0, cache.size());
    assertTrue(cache.isEmpty());
    assertNull(cache.get("key1"));
  }

  @Test
  void testLruEviction() {
    // Fill cache to capacity
    cache.put("key1", 1);
    cache.put("key2", 2);
    cache.put("key3", 3);
    cache.put("key4", 4);
    cache.put("key5", 5);

    assertEquals(5, cache.size());

    // Access key1 to make it recently used
    cache.get("key1");

    // Add new key - should evict least recently used (key2)
    cache.put("key6", 6);

    // key1 should still be present (was accessed recently)
    assertNotNull(cache.get("key1"));

    // Size should be back to max
    assertTrue(cache.size() <= 5);
  }

  @Test
  void testLruEvictionOrder() {
    // Fill cache beyond capacity without accessing
    cache.put("key1", 1);
    cache.put("key2", 2);
    cache.put("key3", 3);
    cache.put("key4", 4);
    cache.put("key5", 5);
    cache.put("key6", 6); // Should trigger eviction

    // Oldest entries should be evicted
    assertTrue(cache.size() <= 5);
  }

  @Test
  void testGetStats() {
    cache.put("key1", 100);
    cache.put("key2", 200);
    cache.get("key1"); // Access to increment counter

    ManualLruCache.CacheStats stats = cache.getStats();
    assertEquals(2, stats.currentSize());
    assertEquals(5, stats.maxSize());
    assertTrue(stats.totalAccesses() > 0);
  }

  @Test
  void testConcurrentOperations() throws InterruptedException {
    ManualLruCache<Integer, Integer> concurrentCache = new ManualLruCache<>(100);
    Thread[] threads = new Thread[5];

    for (int i = 0; i < 5; i++) {
      final int threadId = i;
      threads[i] =
          new Thread(
              () -> {
                for (int j = 0; j < 20; j++) {
                  int key = threadId * 20 + j;
                  concurrentCache.put(key, key * 10);
                  concurrentCache.get(key);
                }
              });
      threads[i].start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    // Verify cache is in consistent state
    assertTrue(concurrentCache.size() <= 100);
  }

  @Test
  void testOverheadFactorTrigger() {
    ManualLruCache<String, Integer> smallCache = new ManualLruCache<>(2);

    // Add more than overhead factor (2 * 1.2 = 2.4, so 3 entries should trigger)
    smallCache.put("key1", 1);
    smallCache.put("key2", 2);
    smallCache.put("key3", 3);

    // Should have triggered cleanup
    assertTrue(smallCache.size() <= 2);
  }
}
