package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.style.StylePerf;

/**
 * TwBatch — Batch operations facade for performance optimization.
 *
 * <pre>
 * TwBatch.run(() -> {
 *     TwStyle.apply(node1, "p-4", "bg-blue-500");
 *     TwStyle.apply(node2, "m-2", "text-white");
 * });
 * </pre>
 */
public final class TwBatch {

  private static final TwBatch INSTANCE = new TwBatch();

  private TwBatch() {}

  /** Executes a batch of style operations efficiently. */
  public static void run(Runnable action) {
    StylePerf.batch(action);
  }

  /** Executes a batch of style operations asynchronously. */
  public static void runAsync(Runnable action) {
    StylePerf.batchAsync(action);
  }
}
