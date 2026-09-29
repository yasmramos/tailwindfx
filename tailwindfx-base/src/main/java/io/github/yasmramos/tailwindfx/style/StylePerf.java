/*
 * Copyright 2026 Yasmany Ramos García (yasmramos).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.yasmramos.tailwindfx.style;

import io.github.yasmramos.tailwindfx.core.Preconditions;
import io.github.yasmramos.tailwindfx.core.UtilityConflictResolver;
import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Node;

/**
 * StylePerf — performance utilities for TailwindFX.
 *
 * <h3>StyleDiff — skip redundant applies</h3>
 *
 * <p>Applying the same classes to a node twice in a row is a no-op after the first call. StyleDiff
 * hashes the incoming class set and compares it to the last applied state stored in {@code
 * node.getProperties()}:
 *
 * <pre>
 * // Without StyleDiff: both calls run the full resolver
 * TwStyle.apply(button, "btn-primary rounded-lg");
 * TwStyle.apply(button, "btn-primary rounded-lg"); // redundant work
 *
 * // With StyleDiff: second call is a no-op
 * StylePerf.apply(button, "btn-primary rounded-lg");
 * StylePerf.apply(button, "btn-primary rounded-lg"); // skipped
 * </pre>
 *
 * <h3>BatchApply — consolidate writes into one pass</h3>
 *
 * <p>JavaFX's CSS engine re-evaluates styles every frame it detects a change. Applying utilities to
 * many nodes individually fires one re-evaluation per node. {@link #batch} collects the deferred
 * applies and executes them <b>synchronously in a single consolidated pass at the end of the call
 * block</b>, so the CSS engine sees one batched change on the next frame. The variant that defers
 * the whole block to the next frame via {@code Platform.runLater} is {@link #batchAsync}:
 *
 * <pre>
 * // Without batch: 3 CSS engine passes
 * TwStyle.apply(card1, "w-full p-4");
 * TwStyle.apply(card2, "w-full p-4");
 * TwStyle.apply(card3, "w-full p-4");
 *
 * // With batch: 1 CSS engine pass
 * StylePerf.batch(() -> {
 *     TwStyle.apply(card1, "w-full p-4");
 *     TwStyle.apply(card2, "w-full p-4");
 *     TwStyle.apply(card3, "w-full p-4");
 * });
 * </pre>
 *
 * <h3>Integration with TailwindFX</h3>
 *
 * <p>Both features are available directly via the entry point:
 *
 * <pre>
 * TwStyle.apply(node, "btn-primary", "rounded-lg");
 * TailwindFX.batch(() -> { ... });
 * TailwindFX.batchJit(() -> { ... });
 * </pre>
 */
public final class StylePerf {

  private StylePerf() {}

  // StyleDiff

  private static final String DIFF_KEY = "tailwindfx.style.hash";

  /**
   * Applies utility classes to a node only if the incoming class set differs from the last applied
   * state.
   *
   * <p>On a cache hit (identical classes), the call is a no-op. On a cache miss, delegates to
   * {@link UtilityConflictResolver#applyAll} and stores the new hash.
   *
   * <p><b>Note:</b> the diff cache tracks only classes passed through this method. If you modify
   * {@code node.getStyleClass()} directly (outside {@link #apply}), the cached hash becomes stale
   * and a subsequent identical {@code apply} call may be skipped incorrectly. Call {@link
   * #invalidate(Node)} after any external mutation of the style-class list.
   *
   * @param node the node to apply classes to
   * @param classes utility classes to apply
   * @return {@code true} if classes were actually applied, {@code false} if skipped
   */
  public static boolean apply(Node node, String... classes) {
    Preconditions.requireNode(node, "StylePerf.apply");
    if (classes == null || classes.length == 0) return false;

    int hash = computeHash(classes);
    Object prev = node.getProperties().get(DIFF_KEY);
    if (prev instanceof Integer prevHash && prevHash == hash) {
      TailwindFXMetrics.instance().recordCacheHit();
      return false; // no change
    }

    node.getProperties().put(DIFF_KEY, hash);
    UtilityConflictResolver.applyAll(node, classes);
    // Record the number of individual class tokens (applyAll splits whitespace-separated
    // entries), not the varargs length, so metrics match actual applied work.
    int tokenCount = 0;
    for (String c : classes) {
      if (c == null || c.isBlank()) continue;
      for (String part : c.split("\\s+")) {
        if (!part.isBlank()) tokenCount++;
      }
    }
    if (tokenCount > 0) TailwindFXMetrics.instance().recordApply(tokenCount);
    return true;
  }

  /**
   * Invalidates the StyleDiff cache for a node. Call this after externally modifying the node's
   * style classes.
   *
   * @param node the node whose cache to invalidate
   */
  public static void invalidate(Node node) {
    Preconditions.requireNode(node, "StylePerf.invalidate");
    node.getProperties().remove(DIFF_KEY);
  }

  /**
   * Returns the current StyleDiff hash for a node, or {@code null} if no classes have been applied
   * via {@link #apply} yet.
   *
   * @param node the node to inspect
   * @return the hash of the last applied class set, or {@code null}
   */
  public static Integer currentHash(Node node) {
    Preconditions.requireNode(node, "StylePerf.currentHash");
    Object v = node.getProperties().get(DIFF_KEY);
    return v instanceof Integer i ? i : null;
  }

  // BatchApply

  private static final Logger LOGGER = Logger.getLogger(StylePerf.class.getName());

  /**
   * Whether a batch is currently accumulating. Volatile because {@link #isBatchActive()} is public
   * and may be queried from other threads; enqueueing itself is still FX-thread only.
   */
  private static volatile boolean batchActive = false;

  /**
   * Pending batch operations. Built up during {@code batch()}, flushed synchronously at the end.
   */
  private static final List<PendingOp> pendingOps = new ArrayList<>();

  /**
   * Re-entrancy guard: true while {@link #flushBatch()} is running, so nested enqueues (triggered
   * by apply hooks invoked from within the flush) execute immediately instead of being lost —
   * {@code batch()} clears the queue in its finally block.
   */
  private static boolean flushing = false;

  /** A single deferred apply operation queued while a batch is active. */
  private record PendingOp(Node node, String[] classes) {}

  /**
   * Executes {@code work} in batch mode: all deferred apply calls collected inside {@code work}
   * (queued by {@link io.github.yasmramos.tailwindfx.TwStyle#apply}) are flushed <b>synchronously
   * in a single consolidated pass at the end of this method</b>, triggering one CSS engine
   * re-evaluation instead of one per node. If you need the flush to happen on the next frame
   * instead, use {@link #batchAsync}.
   *
   * <p>Batch mode is transparent to callers of {@link io.github.yasmramos.tailwindfx.TwStyle#apply}
   * and {@link StylePerf#apply} — they do not need modification.
   *
   * <p>Must be called on the JavaFX Application Thread.
   *
   * <pre>
   * // Applying utilities to a dashboard of 200 cards:
   * StylePerf.batch(() ->
   *     cards.forEach(c -> TwStyle.apply(c, "card", "shadow-md", "rounded-lg"))
   * );
   * </pre>
   *
   * @param work the block of apply operations to batch
   * @throws IllegalArgumentException if work is null
   * @throws IllegalStateException if called from a non-FX thread
   */
  public static void batch(Runnable work) {
    Preconditions.requireNonNull(work, "StylePerf.batch", "work");
    if (!Platform.isFxApplicationThread()) {
      throw new IllegalStateException(
          "StylePerf.batch: must be called on the JavaFX Application Thread");
    }
    if (batchActive) {
      // Nested batch — just run inline, outer batch handles flushing
      work.run();
      return;
    }
    batchActive = true;
    pendingOps.clear();
    try {
      work.run();
    } finally {
      flushBatch();
      batchActive = false;
    }
  }

  /**
   * Returns whether a batch is currently accumulating. Used by {@link
   * io.github.yasmramos.tailwindfx.TwStyle#apply} to decide whether to defer.
   */
  public static boolean isBatchActive() {
    return batchActive;
  }

  /**
   * Enqueues a deferred apply operation. Called by {@link io.github.yasmramos.tailwindfx.TwStyle}
   * while a {@link #batch} block is active; outside a batch the operation is applied immediately
   * (there is no frame boundary at which a later flush would be safe, so nothing is queued).
   *
   * <p><b>Threading contract:</b> must be called on the JavaFX Application Thread, same as {@link
   * #batch}.
   *
   * @param node the node to apply to
   * @param classes the classes to apply (defensively copied)
   * @throws IllegalStateException if called from a non-FX thread
   */
  public static void enqueueDeferredApply(Node node, String[] classes) {
    Preconditions.requireNode(node, "StylePerf.enqueueDeferredApply");
    if (!Platform.isFxApplicationThread()) {
      throw new IllegalStateException(
          "StylePerf.enqueueDeferredApply: must be called on the JavaFX Application Thread");
    }
    if (classes == null || classes.length == 0) return;

    if (!batchActive) {
      // No batch accumulating — apply right away instead of queueing work that nobody would
      // flush deterministically. (An earlier "auto-batch threshold" variant was removed: with
      // only one enqueue site, TwStyle.apply inside batch(), implicit mid-loop flushes added
      // complexity without a real use case.)
      applyNow(node, classes);
      return;
    }

    // Nested enqueue during a flush: apply immediately instead of queueing, because the outer
    // flush already swapped the queue and batch() clears leftovers in its finally block.
    if (flushing) {
      applyNow(node, classes);
      return;
    }

    pendingOps.add(new PendingOp(node, classes.clone()));
  }

  /** Applies classes directly, logging (instead of propagating) failures, for enqueue callers. */
  private static void applyNow(Node node, String[] classes) {
    try {
      UtilityConflictResolver.applyAll(node, classes);
    } catch (RuntimeException ex) {
      LOGGER.log(Level.WARNING, "StylePerf.enqueueDeferredApply: immediate apply failed.", ex);
    }
  }

  private static void flushBatch() {
    if (pendingOps.isEmpty()) return;
    // Copy first so partial state survives an exception mid-flush; then clear the shared queue
    // in a finally block so nothing is lost no matter how the loop exits.
    List<PendingOp> ops = new ArrayList<>(pendingOps);
    pendingOps.clear();
    // Re-entrancy guard: nested enqueueDeferredApply calls (triggered by apply hooks running
    // inside this flush) execute immediately instead of piling up in a queue that batch()'s
    // finally block would later discard.
    boolean outerFlushing = flushing;
    flushing = true;
    int appliedClasses = 0;
    try {
      for (PendingOp op : ops) {
        try {
          UtilityConflictResolver.applyAll(op.node(), op.classes());
          appliedClasses += op.classes().length;
        } catch (RuntimeException ex) {
          // A single bad token must not discard the rest of the batch.
          LOGGER.log(
              Level.WARNING,
              "StylePerf.flushBatch: failed to apply classes "
                  + java.util.Arrays.toString(op.classes())
                  + " — skipping this operation.",
              ex);
        }
      }
    } finally {
      flushing = outerFlushing;
    }
    if (appliedClasses > 0) {
      TailwindFXMetrics.instance().recordApply(appliedClasses);
    }
  }

  // Async batch — for non-FX-thread callers

  /**
   * Thread-safe variant of {@link #batch}: enqueues work on the FX thread and returns immediately.
   * The batch is flushed in the next frame.
   *
   * <p>May be called from any thread. The callback receives no return value — if you need the
   * result, use {@code Platform.runLater} directly.
   *
   * <pre>
   * // From a background data-loading thread:
   * StylePerf.batchAsync(() -> {
   *     results.forEach(row -> TwStyle.apply(row.cell(), "table-cell"));
   * });
   * </pre>
   *
   * @param work the work to run on the FX thread inside a batch
   */
  public static void batchAsync(Runnable work) {
    Preconditions.requireNonNull(work, "StylePerf.batchAsync", "work");
    Platform.runLater(() -> batch(work));
  }

  // Benchmark helper

  /**
   * Measures the wall-clock time (in milliseconds) to apply utilities to {@code count} nodes using
   * the given work function.
   *
   * <p>Useful for comparing batched vs non-batched apply performance:
   *
   * <pre>
   * var nodes = buildNodes(500);
   *
   * double noBatch = StylePerf.benchmark(500,
   *     i -> TwStyle.apply(nodes.get(i), "card shadow-md rounded-lg"));
   *
   * double withBatch = StylePerf.benchmark(1, i ->
   *     StylePerf.batch(() ->
   *         nodes.forEach(n -> TwStyle.apply(n, "card shadow-md rounded-lg"))));
   *
   * System.out.printf("No batch: %.2f ms, Batch: %.2f ms%n", noBatch, withBatch);
   * </pre>
   *
   * <p><b>This helper is indicative only:</b> it measures raw wall-clock time of the loop without
   * JIT warm-up, GC pauses control or variance measurement. Do not use it for rigorous benchmarking
   * (use JMH for that). Note that work scheduled via {@code Platform.runLater} is <em>not</em>
   * waited on — only the enqueueing cost is measured.
   *
   * @param count number of iterations
   * @param work function receiving the iteration index
   * @return elapsed wall-clock time in milliseconds
   */
  public static double benchmark(int count, java.util.function.IntConsumer work) {
    Preconditions.requireNonNull(work, "StylePerf.benchmark", "work");
    if (count <= 0)
      throw new IllegalArgumentException("StylePerf.benchmark: count must be > 0, got: " + count);
    long t0 = System.nanoTime();
    for (int i = 0; i < count; i++) work.accept(i);
    return (System.nanoTime() - t0) / 1_000_000.0;
  }

  // Helpers

  private static int computeHash(String[] classes) {
    // Order-dependent hash: UtilityConflictResolver.applyAll resolves conflicts with "last wins"
    // per category, so "p-4 p-8" and "p-8 p-4" are semantically different and must not collide.
    // LinkedHashSet keeps first-insertion order; removing before re-adding collapses duplicates
    // conservatively keeping the LAST occurrence position of each class (["a","b","a"] ->
    // ["b","a"]).
    LinkedHashSet<String> dedup = new LinkedHashSet<>();
    for (String s : classes) {
      if (s == null) continue;
      dedup.remove(s);
      dedup.add(s);
    }
    int h = 1;
    for (String s : dedup) {
      h = 31 * h + s.hashCode();
    }
    return h;
  }
}
