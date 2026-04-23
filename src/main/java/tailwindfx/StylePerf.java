package tailwindfx;

import javafx.application.Platform;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * StylePerf — performance utilities for TailwindFX.
 *
 * <h3>StyleDiff — skip redundant applies</h3>
 * <p>Applying the same classes to a node twice in a row is a no-op after
 * the first call. StyleDiff hashes the incoming class set and compares it
 * to the last applied state stored in {@code node.getProperties()}:
 *
 * <pre>
 * // Without StyleDiff: both calls run the full resolver
 * TailwindFX.apply(button, "btn-primary rounded-lg");
 * TailwindFX.apply(button, "btn-primary rounded-lg"); // redundant work
 *
 * // With StyleDiff: second call is a no-op
 * StylePerf.apply(button, "btn-primary rounded-lg");
 * StylePerf.apply(button, "btn-primary rounded-lg"); // skipped
 * </pre>
 *
 * <h3>BatchApply — defer writes to one frame</h3>
 * <p>JavaFX's CSS engine re-evaluates styles every frame it detects a change.
 * Applying utilities to many nodes individually fires one re-evaluation per
 * node. Batching defers all writes to a single {@code Platform.runLater} call
 * so the CSS engine sees one consolidated change:
 *
 * <pre>
 * // Without batch: 3 CSS engine passes
 * TailwindFX.apply(card1, "w-full p-4");
 * TailwindFX.apply(card2, "w-full p-4");
 * TailwindFX.apply(card3, "w-full p-4");
 *
 * // With batch: 1 CSS engine pass
 * StylePerf.batch(() -> {
 *     TailwindFX.apply(card1, "w-full p-4");
 *     TailwindFX.apply(card2, "w-full p-4");
 *     TailwindFX.apply(card3, "w-full p-4");
 * });
 * </pre>
 *
 * <h3>Integration with TailwindFX</h3>
 * <p>Both features are available directly via the entry point:
 * <pre>
 * TailwindFX.apply(node, "btn-primary", "rounded-lg");
 * TailwindFX.batch(() -> { ... });
 * TailwindFX.batchJit(() -> { ... });
 * </pre>
 */
public final class StylePerf {

    private StylePerf() {}

    // =========================================================================
    // StyleDiff
    // =========================================================================

    private static final String DIFF_KEY = "tailwindfx.style.hash";

    /**
     * Applies utility classes to a node only if the incoming class set differs
     * from the last applied state.
     *
     * <p>On a cache hit (identical classes), the call is a no-op.
     * On a cache miss, delegates to {@link UtilityConflictResolver#applyAll}
     * and stores the new hash.
     *
     * @param node    the node to apply classes to
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
        TailwindFXMetrics.instance().recordApply(classes.length);
        return true;
    }

    /**
     * Invalidates the StyleDiff cache for a node.
     * Call this after externally modifying the node's style classes.
     *
     * @param node the node whose cache to invalidate
     */
    public static void invalidate(Node node) {
        Preconditions.requireNode(node, "StylePerf.invalidate");
        node.getProperties().remove(DIFF_KEY);
    }

    /**
     * Returns the current StyleDiff hash for a node, or {@code null} if
     * no classes have been applied via {@link #apply} yet.
     *
     * @param node the node to inspect
     * @return the hash of the last applied class set, or {@code null}
     */
    public static Integer currentHash(Node node) {
        Preconditions.requireNode(node, "StylePerf.currentHash");
        Object v = node.getProperties().get(DIFF_KEY);
        return v instanceof Integer i ? i : null;
    }

    // =========================================================================
    // BatchApply
    // =========================================================================

    /**
     * Whether a batch is currently accumulating (used on FX thread only).
     * Not volatile — batch is always called on the FX thread.
     */
    private static boolean batchActive = false;

    /**
     * Pending batch operations (pairs of [node, classes[]]).
     * Built up during batch(), flushed at the end.
     */
    private static final java.util.List<Object[]> pendingOps = new ArrayList<>();

    /**
     * Executes {@code work} in batch mode: all {@code UtilityConflictResolver.applyAll}
     * calls inside {@code work} are collected and flushed in a single pass at the
     * end, triggering one CSS engine re-evaluation instead of one per node.
     *
     * <p>Batch mode is transparent to callers of {@link TailwindFX#apply} and
     * {@link StylePerf#apply} — they do not need modification.
     *
     * <p>Must be called on the JavaFX Application Thread.
     *
     * <pre>
     * // Applying utilities to a dashboard of 200 cards:
     * StylePerf.batch(() ->
     *     cards.forEach(c -> TailwindFX.apply(c, "card", "shadow-md", "rounded-lg"))
     * );
     * </pre>
     *
     * @param work the block of apply operations to batch
     * @throws IllegalArgumentException if work is null
     * @throws IllegalStateException    if called from a non-FX thread
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
     * Returns whether a batch is currently accumulating.
     * Used internally by {@link TailwindFX#apply} to decide whether to defer.
     */
    static boolean isBatchActive() { return batchActive; }

    // =========================================================================
    // Auto-batch threshold
    // =========================================================================

    /**
     * Minimum number of nodes that triggers automatic batch mode when
     * {@link TailwindFX#configure()}.{@code autoBatch(threshold)} is set.
     * 0 = disabled (default).
     */
    static volatile int autoBatchThreshold = 0;

    /** Enables automatic batching when at least {@code threshold} nodes are queued. */
    static void setAutoBatchThreshold(int threshold) {
        autoBatchThreshold = Math.max(0, threshold);
    }

    /** Returns the current auto-batch threshold (0 = disabled). */
    static int getAutoBatchThreshold() { return autoBatchThreshold; }

    /**
     * Enqueues a deferred apply operation. Called by TailwindFX.apply() when
     * a batch is active. Do not call directly.
     *
     * @param node    the node to apply to
     * @param classes the classes to apply
     */
    static void enqueueDeferredApply(Node node, String[] classes) {
        pendingOps.add(new Object[]{ node, classes });
    }

    private static void flushBatch() {
        if (pendingOps.isEmpty()) return;
        int count = 0;
        for (Object[] op : pendingOps) {
            Node node     = (Node)     op[0];
            String[] cls  = (String[]) op[1];
            UtilityConflictResolver.applyAll(node, cls);
            count += cls.length;
        }
        TailwindFXMetrics.instance().recordApply(count);
        pendingOps.clear();
    }

    // =========================================================================
    // Async batch — for non-FX-thread callers
    // =========================================================================

    /**
     * Thread-safe variant of {@link #batch}: enqueues work on the FX thread
     * and returns immediately. The batch is flushed in the next frame.
     *
     * <p>May be called from any thread. The callback receives no return value —
     * if you need the result, use {@code Platform.runLater} directly.
     *
     * <pre>
     * // From a background data-loading thread:
     * StylePerf.batchAsync(() -> {
     *     results.forEach(row -> TailwindFX.apply(row.cell(), "table-cell"));
     * });
     * </pre>
     *
     * @param work the work to run on the FX thread inside a batch
     */
    public static void batchAsync(Runnable work) {
        Preconditions.requireNonNull(work, "StylePerf.batchAsync", "work");
        Platform.runLater(() -> batch(work));
    }

    // =========================================================================
    // Benchmark helper
    // =========================================================================

    /**
     * Measures the wall-clock time (in milliseconds) to apply utilities to
     * {@code count} nodes using the given work function.
     *
     * <p>Useful for comparing batched vs non-batched apply performance:
     * <pre>
     * var nodes = buildNodes(500);
     *
     * double noBatch = StylePerf.benchmark(500,
     *     i -> TailwindFX.apply(nodes.get(i), "card shadow-md rounded-lg"));
     *
     * double withBatch = StylePerf.benchmark(1, i ->
     *     StylePerf.batch(() ->
     *         nodes.forEach(n -> TailwindFX.apply(n, "card shadow-md rounded-lg"))));
     *
     * System.out.printf("No batch: %.2f ms, Batch: %.2f ms%n", noBatch, withBatch);
     * </pre>
     *
     * @param count    number of iterations
     * @param work     function receiving the iteration index
     * @return elapsed wall-clock time in milliseconds
     */
    public static double benchmark(int count, java.util.function.IntConsumer work) {
        Preconditions.requireNonNull(work, "StylePerf.benchmark", "work");
        if (count <= 0) throw new IllegalArgumentException(
            "StylePerf.benchmark: count must be > 0, got: " + count);
        long t0 = System.nanoTime();
        for (int i = 0; i < count; i++) work.accept(i);
        return (System.nanoTime() - t0) / 1_000_000.0;
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static int computeHash(String[] classes) {
        // Order-independent hash: sort to normalize "p-4 w-8" == "w-8 p-4"
        String[] sorted = classes.clone();
        // Inline sort to avoid Arrays.sort allocation in hot path
        if (sorted.length > 1) {
            Arrays.sort(sorted);
        }
        int h = 1;
        for (String s : sorted) {
            if (s != null) h = 31 * h + s.hashCode();
        }
        return h;
    }
}
