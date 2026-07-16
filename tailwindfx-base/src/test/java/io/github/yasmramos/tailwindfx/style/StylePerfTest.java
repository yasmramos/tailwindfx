package io.github.yasmramos.tailwindfx.style;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.TailwindFX;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/** Comprehensive unit tests for StylePerf. */
@DisplayName("StylePerf Tests")
class StylePerfTest extends ApplicationTest {

  private Button button;
  private VBox container;

  @BeforeEach
  void setUp() {
    button = new Button("Test Button");
    container = new VBox();
    container.getChildren().add(button);
  }

  @Nested
  @DisplayName("StyleDiff - Cache Optimization")
  class StyleDiffTests {

    @Test
    @DisplayName("Should apply classes on first call")
    void testFirstApply() {
      boolean result = StylePerf.apply(button, "btn-primary", "rounded-lg");
      assertTrue(result, "First apply should return true");
    }

    @Test
    @DisplayName("Should skip redundant apply with same classes")
    void testRedundantApply() {
      StylePerf.apply(button, "btn-primary", "rounded-lg");
      boolean result = StylePerf.apply(button, "btn-primary", "rounded-lg");
      assertFalse(result, "Redundant apply should return false");
    }

    @Test
    @DisplayName("Should apply when classes change")
    void testDifferentClasses() {
      StylePerf.apply(button, "btn-primary");
      boolean result = StylePerf.apply(button, "btn-secondary");
      assertTrue(result, "Apply with different classes should return true");
    }

    @Test
    @DisplayName("Should handle order-independent class comparison")
    void testOrderIndependentHash() {
      StylePerf.apply(button, "p-4", "w-8");
      boolean result = StylePerf.apply(button, "w-8", "p-4");
      assertFalse(result, "Same classes in different order should be cached");
    }

    @Test
    @DisplayName("Should return false for null classes")
    void testNullClasses() {
      boolean result = StylePerf.apply(button, (String[]) null);
      assertFalse(result, "Null classes should return false");
    }

    @Test
    @DisplayName("Should return false for empty classes")
    void testEmptyClasses() {
      boolean result = StylePerf.apply(button);
      assertFalse(result, "Empty classes should return false");
    }

    @Test
    @DisplayName("Should invalidate cache correctly")
    void testInvalidate() {
      StylePerf.apply(button, "btn-primary");
      StylePerf.invalidate(button);
      boolean result = StylePerf.apply(button, "btn-primary");
      assertTrue(result, "After invalidation, same classes should apply again");
    }

    @Test
    @DisplayName("Should return current hash after apply")
    void testCurrentHash() {
      StylePerf.apply(button, "btn-primary", "rounded-lg");
      Integer hash = StylePerf.currentHash(button);
      assertNotNull(hash, "Current hash should not be null after apply");
    }

    @Test
    @DisplayName("Should return null hash before any apply")
    void testNullCurrentHash() {
      Integer hash = StylePerf.currentHash(button);
      assertNull(hash, "Current hash should be null before any apply");
    }

    @Test
    @DisplayName("Should throw exception for null node in apply")
    void testNullNodeInApply() {
      assertThrows(
          IllegalArgumentException.class,
          () -> StylePerf.apply(null, "btn-primary"),
          "Should throw for null node in apply");
    }

    @Test
    @DisplayName("Should throw exception for null node in invalidate")
    void testNullNodeInInvalidate() {
      assertThrows(
          IllegalArgumentException.class,
          () -> StylePerf.invalidate(null),
          "Should throw for null node in invalidate");
    }

    @Test
    @DisplayName("Should throw exception for null node in currentHash")
    void testNullNodeInCurrentHash() {
      assertThrows(
          IllegalArgumentException.class,
          () -> StylePerf.currentHash(null),
          "Should throw for null node in currentHash");
    }
  }

  @Nested
  @DisplayName("Batch Processing")
  class BatchProcessingTests {

    @Test
    @DisplayName("Should execute batch on FX thread")
    void testBatchExecution() {
      interact(
          () -> {
            assertDoesNotThrow(
                () -> {
                  StylePerf.batch(() -> {
                    TailwindFX.apply(button, "btn-primary");
                    TailwindFX.apply(container, "v-box", "p-4");
                  });
                },
                "Batch execution should not throw");
          });
    }

    @Test
    @DisplayName("Should throw exception for null batch runnable")
    void testNullBatchRunnable() {
      assertThrows(
          IllegalArgumentException.class,
          () -> StylePerf.batch(null),
          "Should throw for null runnable");
    }

    @Test
    @DisplayName("Should throw exception when batch called from non-FX thread")
    void testBatchFromNonFxThread() throws Exception {
      final boolean[] exceptionThrown = {false};
      Thread thread =
          new Thread(
              () -> {
                try {
                  StylePerf.batch(() -> TailwindFX.apply(button, "btn-primary"));
                } catch (IllegalStateException e) {
                  exceptionThrown[0] = true;
                }
              });
      thread.start();
      thread.join(5000);
      assertTrue(exceptionThrown[0], "Should throw IllegalStateException from non-FX thread");
    }

    @Test
    @DisplayName("Should handle nested batches")
    void testNestedBatch() {
      interact(
          () -> {
            assertDoesNotThrow(
                () -> {
                  StylePerf.batch(
                      () -> {
                        TailwindFX.apply(button, "btn-primary");
                        // Nested batch
                        StylePerf.batch(() -> TailwindFX.apply(container, "v-box"));
                      });
                },
                "Nested batches should not throw");
          });
    }

    @Test
    @DisplayName("Should track batch active state")
    void testBatchActiveState() {
      interact(
          () -> {
            assertFalse(StylePerf.isBatchActive(), "Batch should not be active initially");

            StylePerf.batch(
                () -> {
                  assertTrue(StylePerf.isBatchActive(), "Batch should be active during execution");
                });

            assertFalse(StylePerf.isBatchActive(), "Batch should not be active after completion");
          });
    }

    @Test
    @DisplayName("Should clear pending operations after batch")
    void testPendingOpsCleared() {
      interact(
          () -> {
            StylePerf.batch(() -> TailwindFX.apply(button, "btn-primary"));
            // After batch, pendingOps should be cleared internally
            // We verify by checking batch is not active
            assertFalse(StylePerf.isBatchActive());
          });
    }
  }

  @Nested
  @DisplayName("Async Batch Processing")
  class AsyncBatchTests {

    @Test
    @DisplayName("Should enqueue work to FX thread")
    void testBatchAsync() {
      assertDoesNotThrow(
          () -> {
            StylePerf.batchAsync(() -> TailwindFX.apply(button, "btn-primary"));
            // Give time for async execution
            Thread.sleep(100);
          },
          "batchAsync should not throw");
    }

    @Test
    @DisplayName("Should throw exception for null async runnable")
    void testNullAsyncRunnable() {
      assertThrows(
          IllegalArgumentException.class,
          () -> StylePerf.batchAsync(null),
          "Should throw for null runnable in batchAsync");
    }
  }

  @Nested
  @DisplayName("Auto-Batch Threshold")
  class AutoBatchThresholdTests {

    @Test
    @DisplayName("Should set and get auto-batch threshold")
    void testSetGetThreshold() {
      StylePerf.setAutoBatchThreshold(10);
      assertEquals(10, StylePerf.getAutoBatchThreshold());
    }

    @Test
    @DisplayName("Should normalize negative threshold to zero")
    void testNegativeThreshold() {
      StylePerf.setAutoBatchThreshold(-5);
      assertEquals(0, StylePerf.getAutoBatchThreshold());
    }

    @Test
    @DisplayName("Should allow zero threshold (disabled)")
    void testZeroThreshold() {
      StylePerf.setAutoBatchThreshold(0);
      assertEquals(0, StylePerf.getAutoBatchThreshold());
    }

    @Test
    @DisplayName("Should accept large threshold values")
    void testLargeThreshold() {
      StylePerf.setAutoBatchThreshold(Integer.MAX_VALUE);
      assertEquals(Integer.MAX_VALUE, StylePerf.getAutoBatchThreshold());
    }
  }

  @Nested
  @DisplayName("Benchmark Utility")
  class BenchmarkTests {

    @Test
    @DisplayName("Should measure execution time")
    void testBenchmark() {
      double timeMs =
          StylePerf.benchmark(
              100, i -> Platform.runLater(() -> StylePerf.apply(button, "btn-primary")));
      assertTrue(timeMs >= 0, "Benchmark time should be non-negative");
    }

    @Test
    @DisplayName("Should throw exception for null benchmark work")
    void testNullBenchmarkWork() {
      assertThrows(
          IllegalArgumentException.class,
          () -> StylePerf.benchmark(10, null),
          "Should throw for null work in benchmark");
    }

    @Test
    @DisplayName("Should throw exception for zero count")
    void testZeroCount() {
      assertThrows(
          IllegalArgumentException.class,
          () -> StylePerf.benchmark(0, i -> {}),
          "Should throw for zero count in benchmark");
    }

    @Test
    @DisplayName("Should throw exception for negative count")
    void testNegativeCount() {
      assertThrows(
          IllegalArgumentException.class,
          () -> StylePerf.benchmark(-5, i -> {}),
          "Should throw for negative count in benchmark");
    }

    @Test
    @DisplayName("Should measure multiple iterations")
    void testMultipleIterations() {
      double timeMs = StylePerf.benchmark(1000, i -> Math.sqrt(i));
      assertTrue(timeMs > 0, "Benchmark with 1000 iterations should take measurable time");
    }
  }

  @Nested
  @DisplayName("Deferred Apply Operations")
  class DeferredApplyTests {

    @Test
    @DisplayName("Should enqueue deferred apply operation")
    void testEnqueueDeferredApply() {
      assertDoesNotThrow(
          () -> {
            StylePerf.enqueueDeferredApply(button, new String[] {"btn-primary", "rounded-lg"});
          },
          "enqueueDeferredApply should not throw");
    }

    @Test
    @DisplayName("Should handle null classes in deferred apply")
    void testEnqueueDeferredApplyWithNullClasses() {
      assertDoesNotThrow(
          () -> {
            StylePerf.enqueueDeferredApply(button, null);
          },
          "enqueueDeferredApply should handle null classes");
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should work with TailwindFX.apply inside batch")
    void testTailwindFXApplyInBatch() {
      interact(
          () -> {
            assertDoesNotThrow(
                () -> {
                  StylePerf.batch(
                      () -> {
                        TailwindFX.apply(button, "btn-primary", "text-white");
                        TailwindFX.apply(container, "flex", "gap-2");
                      });
                },
                "TailwindFX.apply should work inside StylePerf.batch");
          });
    }

    @Test
    @DisplayName("Should maintain cache across multiple nodes")
    void testMultipleNodesCache() {
      Button button2 = new Button("Button 2");
      
      StylePerf.apply(button, "btn-primary");
      StylePerf.apply(button2, "btn-primary");
      
      // Both should have cache now
      assertNotNull(StylePerf.currentHash(button));
      assertNotNull(StylePerf.currentHash(button2));
      
      // Redundant applies should be skipped
      assertFalse(StylePerf.apply(button, "btn-primary"));
      assertFalse(StylePerf.apply(button2, "btn-primary"));
    }

    @Test
    @DisplayName("Should handle complex class combinations")
    void testComplexClassCombinations() {
      String[] classes = {
        "btn-primary", "rounded-lg", "shadow-md", "text-white", "font-bold", "p-4", "m-2"
      };
      
      assertTrue(StylePerf.apply(button, classes), "First apply should succeed");
      assertFalse(StylePerf.apply(button, classes), "Redundant apply should be skipped");
      
      // Change one class
      classes[3] = "text-black";
      assertTrue(StylePerf.apply(button, classes), "Apply with changed class should succeed");
    }
  }
}
