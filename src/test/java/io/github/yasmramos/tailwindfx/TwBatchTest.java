package io.github.yasmramos.tailwindfx;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/** Unit tests for TwBatch batch operations. */
@DisplayName("TwBatch Tests")
class TwBatchTest extends ApplicationTest {

  @Override
  public void start(javafx.stage.Stage stage) {
    javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane();
    javafx.scene.Scene scene = new javafx.scene.Scene(root, 800, 600);
    TailwindFX.install(scene);
    stage.setScene(scene);
    stage.show();
  }

  @Test
  @DisplayName("Should execute async batch operation")
  void shouldExecuteAsyncBatchOperation() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicBoolean executed = new AtomicBoolean(false);

    TwBatch.runAsync(
        () -> {
          executed.set(true);
          latch.countDown();
        });

    assertTrue(latch.await(5, TimeUnit.SECONDS), "Async batch should complete within timeout");
    assertTrue(executed.get(), "Async batch operation should be executed");
  }

  @Test
  @DisplayName("Should handle async batch with exception")
  void shouldHandleAsyncBatchWithException() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicBoolean errorOccurred = new AtomicBoolean(false);

    TwBatch.runAsync(
        () -> {
          try {
            throw new RuntimeException("Test exception");
          } catch (RuntimeException e) {
            errorOccurred.set(true);
          } finally {
            latch.countDown();
          }
        });

    assertTrue(latch.await(5, TimeUnit.SECONDS), "Async batch should complete within timeout");
    assertTrue(errorOccurred.get(), "Exception should be caught in async batch");
  }

  @Test
  @DisplayName("Should handle concurrent async batches")
  void shouldHandleConcurrentAsyncBatches() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(3);
    AtomicBoolean batch1 = new AtomicBoolean(false);
    AtomicBoolean batch2 = new AtomicBoolean(false);
    AtomicBoolean batch3 = new AtomicBoolean(false);

    TwBatch.runAsync(
        () -> {
          batch1.set(true);
          latch.countDown();
        });

    TwBatch.runAsync(
        () -> {
          batch2.set(true);
          latch.countDown();
        });

    TwBatch.runAsync(
        () -> {
          batch3.set(true);
          latch.countDown();
        });

    assertTrue(latch.await(5, TimeUnit.SECONDS), "All async batches should complete");
    assertTrue(batch1.get(), "First batch should execute");
    assertTrue(batch2.get(), "Second batch should execute");
    assertTrue(batch3.get(), "Third batch should execute");
  }

  @Test
  @DisplayName("Should execute batch operation on FX thread via interact")
  void shouldExecuteBatchOperationOnFXThread() {
    AtomicBoolean executed = new AtomicBoolean(false);

    interact(
        () -> {
          TwBatch.run(
              () -> {
                executed.set(true);
              });
        });

    assertTrue(executed.get(), "Batch operation should be executed on FX thread");
  }

  @Test
  @DisplayName("Should execute multiple operations in batch on FX thread")
  void shouldExecuteMultipleOperationsInBatchOnFXThread() {
    AtomicBoolean op1 = new AtomicBoolean(false);
    AtomicBoolean op2 = new AtomicBoolean(false);
    AtomicBoolean op3 = new AtomicBoolean(false);

    interact(
        () -> {
          TwBatch.run(
              () -> {
                op1.set(true);
                op2.set(true);
                op3.set(true);
              });
        });

    assertTrue(op1.get(), "First operation should be executed");
    assertTrue(op2.get(), "Second operation should be executed");
    assertTrue(op3.get(), "Third operation should be executed");
  }

  @Test
  @DisplayName("Should handle nested batch operations on FX thread")
  void shouldHandleNestedBatchOperationsOnFXThread() {
    AtomicBoolean outer = new AtomicBoolean(false);
    AtomicBoolean inner = new AtomicBoolean(false);

    interact(
        () -> {
          TwBatch.run(
              () -> {
                outer.set(true);
                TwBatch.run(
                    () -> {
                      inner.set(true);
                    });
              });
        });

    assertTrue(outer.get(), "Outer batch should be executed");
    assertTrue(inner.get(), "Inner batch should be executed");
  }

  @Test
  @DisplayName("Should propagate exceptions from batch operations on FX thread")
  void shouldPropagateExceptionsFromBatchOperationsOnFXThread() {
    assertThrows(
        RuntimeException.class,
        () -> {
          interact(
              () -> {
                TwBatch.run(
                    () -> {
                      throw new RuntimeException("Test exception");
                    });
              });
        },
        "Batch should propagate exceptions");
  }

  @Test
  @DisplayName("Should maintain execution order in batch on FX thread")
  void shouldMaintainExecutionOrderInBatchOnFXThread() {
    List<Integer> executionOrder = new ArrayList<>();

    interact(
        () -> {
          TwBatch.run(
              () -> {
                executionOrder.add(1);
                executionOrder.add(2);
                executionOrder.add(3);
              });
        });

    assertEquals(3, executionOrder.size(), "Should have 3 executions");
    assertEquals(1, executionOrder.get(0), "First execution should be 1");
    assertEquals(2, executionOrder.get(1), "Second execution should be 2");
    assertEquals(3, executionOrder.get(2), "Third execution should be 3");
  }
}
