package io.github.yasmramos.tailwindfx.cache;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for LruCache implementation. */
class LruCacheTest {

  private LruCache<String, String> cache;

  @BeforeEach
  void setUp() {
    cache = new LruCache<>(3);
  }

  @Test
  void testConstructor_InvalidCapacity() {
    assertThrows(IllegalArgumentException.class, () -> new LruCache<>(0));
    assertThrows(IllegalArgumentException.class, () -> new LruCache<>(-1));
  }

  @Test
  void testPutAndGet() {
    cache.put("key1", "value1");
    assertEquals("value1", cache.get("key1"));
  }

  @Test
  void testGet_NonExistentKey() {
    assertNull(cache.get("nonexistent"));
  }

  @Test
  void testGet_NullKey() {
    assertThrows(IllegalArgumentException.class, () -> cache.get(null));
  }

  @Test
  void testPut_NullKey() {
    assertThrows(IllegalArgumentException.class, () -> cache.put(null, "value"));
  }

  @Test
  void testPut_NullValue() {
    assertThrows(IllegalArgumentException.class, () -> cache.put("key", null));
  }

  @Test
  void testLruEviction() {
    // Fill cache to capacity
    cache.put("key1", "value1");
    cache.put("key2", "value2");
    cache.put("key3", "value3");

    // Access key1 to make it most recently used
    cache.get("key1");

    // Add new key - should evict key2 (least recently used)
    cache.put("key4", "value4");

    // Verify key2 was evicted
    assertNull(cache.get("key2"));

    // Verify other keys still exist
    assertNotNull(cache.get("key1"));
    assertNotNull(cache.get("key3"));
    assertNotNull(cache.get("key4"));
  }

  @Test
  void testRemove() {
    cache.put("key1", "value1");
    assertEquals("value1", cache.remove("key1"));
    assertNull(cache.get("key1"));
    assertNull(cache.remove("key1")); // Remove again returns null
  }

  @Test
  void testRemove_NullKey() {
    assertThrows(IllegalArgumentException.class, () -> cache.remove(null));
  }

  @Test
  void testClear() {
    cache.put("key1", "value1");
    cache.put("key2", "value2");
    cache.clear();
    assertEquals(0, cache.size());
    assertNull(cache.get("key1"));
    assertNull(cache.get("key2"));
  }

  @Test
  void testSize() {
    assertEquals(0, cache.size());
    cache.put("key1", "value1");
    assertEquals(1, cache.size());
    cache.put("key2", "value2");
    assertEquals(2, cache.size());
  }

  @Test
  void testContainsKey() {
    assertFalse(cache.containsKey("key1"));
    cache.put("key1", "value1");
    assertTrue(cache.containsKey("key1"));
    assertFalse(cache.containsKey("key2"));
  }

  @Test
  void testContainsKey_NullKey() {
    assertThrows(IllegalArgumentException.class, () -> cache.containsKey(null));
  }

  @Test
  void testComputeIfAbsent_KeyExists() {
    cache.put("key1", "value1");
    String result = cache.computeIfAbsent("key1", k -> "computed");
    assertEquals("value1", result);
    assertEquals("value1", cache.get("key1"));
  }

  @Test
  void testComputeIfAbsent_KeyNotExists() {
    String result = cache.computeIfAbsent("key1", k -> "computed");
    assertEquals("computed", result);
    assertEquals("computed", cache.get("key1"));
  }

  @Test
  void testComputeIfAbsent_NullKey() {
    assertThrows(
        IllegalArgumentException.class, () -> cache.computeIfAbsent(null, k -> "computed"));
  }

  @Test
  void testComputeIfAbsent_NullFunction() {
    assertThrows(IllegalArgumentException.class, () -> cache.computeIfAbsent("key1", null));
  }

  @Test
  void testStatistics_HitAndMiss() {
    cache.put("key1", "value1");

    // First access - miss
    cache.get("key1");
    assertEquals(1, cache.getHitCount());
    assertEquals(0, cache.getMissCount());

    // Second access - hit
    cache.get("key1");
    assertEquals(2, cache.getHitCount());
    assertEquals(0, cache.getMissCount());

    // Access non-existent - miss
    cache.get("key2");
    assertEquals(2, cache.getHitCount());
    assertEquals(1, cache.getMissCount());
  }

  @Test
  void testStatistics_Eviction() {
    cache.put("key1", "value1");
    cache.put("key2", "value2");
    cache.put("key3", "value3");
    cache.put("key4", "value4"); // Should trigger eviction

    assertEquals(1, cache.getEvictionCount());
  }

  @Test
  void testHitRatio() {
    cache.put("key1", "value1");

    cache.get("key1"); // hit
    cache.get("key1"); // hit
    cache.get("key2"); // miss

    double expectedRatio = (2.0 / 3.0) * 100.0;
    assertEquals(expectedRatio, cache.getHitRatio(), 0.01);
  }

  @Test
  void testHitRatio_NoAccesses() {
    assertEquals(0.0, cache.getHitRatio(), 0.01);
  }

  @Test
  void testResetStats() {
    cache.put("key1", "value1");
    cache.get("key1");
    cache.get("key2");

    cache.resetStats();

    assertEquals(0, cache.getHitCount());
    assertEquals(0, cache.getMissCount());
    assertEquals(0, cache.getEvictionCount());
  }

  @Test
  void testGetStats() {
    cache.put("key1", "value1");
    cache.get("key1");

    LruCache.CacheStats stats = cache.getStats();

    assertEquals(1, stats.hitCount());
    assertEquals(0, stats.missCount());
    assertEquals(0, stats.evictionCount());
    assertEquals(1, stats.size());
    assertEquals(3, stats.maxCapacity());
  }

  @Test
  void testCacheStatsToString() {
    LruCache.CacheStats stats = new LruCache.CacheStats(10, 5, 2, 50, 100);
    String str = stats.toString();

    assertTrue(str.contains("size=50/100"));
    assertTrue(str.contains("hits=10"));
    assertTrue(str.contains("misses=5"));
    assertTrue(str.contains("evictions=2"));
    assertTrue(str.contains("hitRatio="));
  }

  @Test
  void testCacheStatsHitRatio() {
    LruCache.CacheStats stats = new LruCache.CacheStats(80, 20, 0, 0, 100);
    assertEquals(80.0, stats.hitRatio(), 0.01);
  }

  @Test
  void testCacheStatsUtilization() {
    LruCache.CacheStats stats = new LruCache.CacheStats(0, 0, 0, 50, 100);
    assertEquals(50.0, stats.utilization(), 0.01);
  }

  @Test
  void testUpdateOnExistingKey() {
    cache.put("key1", "value1");
    assertEquals("value1", cache.put("key1", "updated"));
    assertEquals("updated", cache.get("key1"));
    assertEquals(1, cache.size());
  }

  @Test
  void testConcurrentAccess_SmokeTest() throws InterruptedException {
    LruCache<Integer, Integer> concurrentCache = new LruCache<>(100);
    Thread[] threads = new Thread[10];

    for (int i = 0; i < threads.length; i++) {
      final int threadId = i;
      threads[i] =
          new Thread(
              () -> {
                for (int j = 0; j < 100; j++) {
                  int key = threadId * 100 + j;
                  concurrentCache.put(key, key * 2);
                  concurrentCache.get(key);
                  concurrentCache.computeIfAbsent(key, k -> k * 3);
                }
              });
      threads[i].start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    // Just verify no exceptions were thrown
    assertTrue(concurrentCache.size() > 0);
  }
}
