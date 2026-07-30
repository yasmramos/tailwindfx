package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for ManualLruCache. */
@DisplayName("ManualLruCache Tests")
class LruCacheTest {

  @Nested
  @DisplayName("Basic Operations")
  class BasicOperationsTests {

    @Test
    @DisplayName("Should create cache with specified max size")
    void testCreateCache() {
      ManualLruCache<String, Integer> cache = new ManualLruCache<>(100);

      assertEquals(100, cache.getMaxSize());
      assertEquals(0, cache.size());
      assertTrue(cache.isEmpty());
    }

    @Test
    @DisplayName("Should throw exception for invalid max size")
    void testInvalidMaxSize() {
      assertThrows(IllegalArgumentException.class, () -> new ManualLruCache<>(0));
      assertThrows(IllegalArgumentException.class, () -> new ManualLruCache<>(-1));
    }

    @Test
    @DisplayName("Should put and get values")
    void testPutAndGet() {
      ManualLruCache<String, Integer> cache = new ManualLruCache<>(10);

      assertNull(cache.put("key1", 100));
      assertEquals(100, cache.get("key1"));
      assertEquals(1, cache.size());
    }

    @Test
    @DisplayName("Should update existing values")
    void testUpdateValue() {
      ManualLruCache<String, Integer> cache = new ManualLruCache<>(10);

      cache.put("key1", 100);
      assertEquals(100, cache.put("key1", 200)); // Returns old value
      assertEquals(200, cache.get("key1"));
      assertEquals(1, cache.size()); // Size should not increase
    }

    @Test
    @DisplayName("Should return null for missing keys")
    void testMissingKey() {
      ManualLruCache<String, Integer> cache = new ManualLruCache<>(10);

      assertNull(cache.get("nonexistent"));
      assertFalse(cache.containsKey("nonexistent"));
    }

    @Test
    @DisplayName("Should check containsKey correctly")
    void testContainsKey() {
      ManualLruCache<String, Integer> cache = new ManualLruCache<>(10);

      cache.put("key1", 100);
      assertTrue(cache.containsKey("key1"));
      assertFalse(cache.containsKey("key2"));
    }

    @Test
    @DisplayName("Should remove entries")
    void testRemove() {
      ManualLruCache<String, Integer> cache = new ManualLruCache<>(10);

      cache.put("key1", 100);
      assertEquals(100, cache.remove("key1"));
      assertNull(cache.get("key1"));
      assertFalse(cache.containsKey("key1"));
      assertEquals(0, cache.size());
    }

    @Test
    @DisplayName("Should clear all entries")
    void testClear() {
      ManualLruCache<String, Integer> cache = new ManualLruCache<>(10);

      cache.put("key1", 100);
      cache.put("key2", 200);
      cache.clear();

      assertEquals(0, cache.size());
      assertTrue(cache.isEmpty());
      assertNull(cache.get("key1"));
    }

    @Test
    @DisplayName("Should use putIfAbsent correctly")
    void testPutIfAbsent() {
      ManualLruCache<String, Integer> cache = new ManualLruCache<>(10);

      assertNull(cache.putIfAbsent("key1", 100)); // First insert
      assertEquals(100, cache.get("key1"));

      assertEquals(100, cache.putIfAbsent("key1", 200)); // Should not update
      assertEquals(100, cache.get("key1")); // Still original value
    }
  }

  @Nested
  @DisplayName("LRU Eviction")
  class LruEvictionTests {

    @Test
    @DisplayName("Should evict oldest entries when exceeding max size")
    void testEvictionOnOverflow() {
      ManualLruCache<Integer, String> cache = new ManualLruCache<>(5);

      // Insert 7 entries
      for (int i = 0; i < 7; i++) {
        cache.put(i, "value" + i);
      }

      // Cache should be at or below max size
      assertTrue(cache.size() <= 5, "Cache size should not exceed max: " + cache.size());

      // Oldest entries (0, 1) should be evicted
      // Note: exact eviction depends on timing, but size constraint must hold
      assertNotNull(cache.get(6)); // Most recent should exist
      assertNotNull(cache.get(5)); // Second most recent should exist
    }

    @Test
    @DisplayName("Should keep recently accessed entries during eviction")
    void testKeepRecentlyAccessed() throws InterruptedException {
      ManualLruCache<Integer, String> cache = new ManualLruCache<>(3);

      // Insert 3 entries
      cache.put(1, "one");
      cache.put(2, "two");
      cache.put(3, "three");

      // Access entry 1 to make it recently used
      Thread.sleep(10); // Ensure time difference
      cache.get(1);

      // Add a 4th entry, should evict the least recently used (2)
      cache.put(4, "four");

      // Entry 1 should still be present (recently accessed)
      assertNotNull(cache.get(1));
      // Entry 2 might be evicted (oldest not accessed)
      // Entry 3 and 4 should be present
      assertNotNull(cache.get(3));
      assertNotNull(cache.get(4));
    }

    @Test
    @DisplayName("Should handle eviction with overhead factor")
    void testOverheadFactor() {
      ManualLruCache<Integer, String> cache = new ManualLruCache<>(10);

      // Insert up to overhead limit (10 * 1.2 = 12)
      for (int i = 0; i < 15; i++) {
        cache.put(i, "value" + i);
      }

      // After cleanup triggered by put(), cache should be at or below max size
      // Note: cleanup is async via maybeCleanup() on gets, so we trigger it manually
      cache.get(0); // Trigger potential cleanup
      
      assertTrue(cache.size() <= 10, "Cache should be cleaned up to max size: " + cache.size());
    }

    @Test
    @DisplayName("Should not evict if under max size")
    void testNoEvictionWhenUnderLimit() {
      ManualLruCache<Integer, String> cache = new ManualLruCache<>(100);

      // Insert 50 entries (under limit)
      for (int i = 0; i < 50; i++) {
        cache.put(i, "value" + i);
      }

      assertEquals(50, cache.size());
      // All entries should still be present
      for (int i = 0; i < 50; i++) {
        assertNotNull(cache.get(i));
      }
    }
  }

  @Nested
  @DisplayName("Thread Safety")
  class ThreadSafetyTests {

    @Test
    @DisplayName("Should handle concurrent puts from multiple threads")
    void testConcurrentPuts() throws InterruptedException {
      ManualLruCache<Integer, String> cache = new ManualLruCache<>(1000);
      int threadCount = 10;
      int operationsPerThread = 100;

      ExecutorService executor = Executors.newFixedThreadPool(threadCount);
      CountDownLatch latch = new CountDownLatch(threadCount);

      for (int t = 0; t < threadCount; t++) {
        final int threadId = t;
        executor.submit(
            () -> {
              try {
                for (int i = 0; i < operationsPerThread; i++) {
                  int key = threadId * operationsPerThread + i;
                  cache.put(key, "value" + key);
                }
              } finally {
                latch.countDown();
              }
            });
      }

      latch.await(10, TimeUnit.SECONDS);
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);

      // Verify no corruption - cache should have some entries
      assertTrue(cache.size() > 0);
      // Size should be bounded by max
      assertTrue(cache.size() <= 1000);
    }

    @Test
    @DisplayName("Should handle concurrent gets and puts")
    void testConcurrentGetAndPut() throws InterruptedException {
      ManualLruCache<Integer, String> cache = new ManualLruCache<>(500);
      int threadCount = 8;

      // Pre-populate cache
      for (int i = 0; i < 100; i++) {
        cache.put(i, "initial" + i);
      }

      ExecutorService executor = Executors.newFixedThreadPool(threadCount);
      CountDownLatch latch = new CountDownLatch(threadCount);
      AtomicInteger successCount = new AtomicInteger(0);

      for (int t = 0; t < threadCount; t++) {
        executor.submit(
            () -> {
              try {
                for (int i = 0; i < 200; i++) {
                  int key = i % 100;
                  String value = cache.get(key);
                  if (value != null) {
                    successCount.incrementAndGet();
                  }
                  cache.put(100 + i, "new" + i);
                }
              } finally {
                latch.countDown();
              }
            });
      }

      latch.await(10, TimeUnit.SECONDS);
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);

      // Verify operations completed without errors
      assertTrue(successCount.get() > 0);
      assertTrue(cache.size() <= 500);
    }

    @Test
    @DisplayName("Should maintain consistency under high concurrency")
    void testHighConcurrencyConsistency() throws InterruptedException {
      ManualLruCache<String, Integer> cache = new ManualLruCache<>(100);
      int threadCount = 20;

      ExecutorService executor = Executors.newFixedThreadPool(threadCount);
      CountDownLatch latch = new CountDownLatch(threadCount);
      List<String> errors = new ArrayList<>();

      for (int t = 0; t < threadCount; t++) {
        final String key = "key-" + t;
        executor.submit(
            () -> {
              try {
                for (int i = 0; i < 50; i++) {
                  cache.put(key, i);
                  Integer value = cache.get(key);
                  if (value == null) {
                    synchronized (errors) {
                      errors.add("Null value for key: " + key);
                    }
                  }
                }
              } finally {
                latch.countDown();
              }
            });
      }

      latch.await(10, TimeUnit.SECONDS);
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);

      // No errors should occur
      assertTrue(errors.isEmpty(), "Errors occurred: " + errors);
    }
  }

  @Nested
  @DisplayName("Cache Statistics")
  class CacheStatsTests {

    @Test
    @DisplayName("Should provide accurate stats")
    void testCacheStats() {
      ManualLruCache<String, Integer> cache = new ManualLruCache<>(100);

      cache.put("key1", 1);
      cache.put("key2", 2);
      cache.get("key1"); // Access to increment counter

      ManualLruCache.CacheStats stats = cache.getStats();

      assertEquals(2, stats.currentSize());
      assertEquals(100, stats.maxSize());
      assertTrue(stats.totalAccesses() >= 1); // At least one access recorded
    }

    @Test
    @DisplayName("Should update stats after operations")
    void testStatsUpdates() {
      ManualLruCache<String, Integer> cache = new ManualLruCache<>(100);

      ManualLruCache.CacheStats initial = cache.getStats();
      assertEquals(0, initial.currentSize());

      cache.put("key1", 1);
      cache.put("key2", 2);

      ManualLruCache.CacheStats afterPut = cache.getStats();
      assertEquals(2, afterPut.currentSize());

      cache.remove("key1");

      ManualLruCache.CacheStats afterRemove = cache.getStats();
      assertEquals(1, afterRemove.currentSize());
    }
  }

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
      ManualLruCache<String, String> cache = new ManualLruCache<>(10);

      cache.put("key1", null);
      assertNull(cache.get("key1"));
      assertTrue(cache.containsKey("key1"));
    }

    @Test
    @DisplayName("Should handle large cache sizes")
    void testLargeCache() {
      ManualLruCache<Integer, String> cache = new ManualLruCache<>(10000);

      for (int i = 0; i < 10000; i++) {
        cache.put(i, "value" + i);
      }

      assertEquals(10000, cache.size());
      assertNotNull(cache.get(9999));
    }

    @Test
    @DisplayName("Should handle rapid put-get cycles")
    void testRapidPutGetCycles() {
      ManualLruCache<Integer, Integer> cache = new ManualLruCache<>(100);

      for (int cycle = 0; cycle < 100; cycle++) {
        for (int i = 0; i < 200; i++) {
          cache.put(i, i * 2);
          assertEquals(i * 2, cache.get(i));
        }
      }

      // Final state should be valid
      assertTrue(cache.size() <= 100);
    }

    @Test
    @DisplayName("Should handle repeated same-key operations")
    void testRepeatedSameKey() {
      ManualLruCache<String, Integer> cache = new ManualLruCache<>(10);

      for (int i = 0; i < 1000; i++) {
        cache.put("sameKey", i);
      }

      assertEquals(999, cache.get("sameKey"));
      assertEquals(1, cache.size());
    }
  }
}
