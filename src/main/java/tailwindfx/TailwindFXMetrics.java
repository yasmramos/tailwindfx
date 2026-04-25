package tailwindfx;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;

/**
 * TailwindFXMetrics — Runtime metrics for TailwindFX.
 *
 * <p>Tracks cache performance, method call counts, and timing data.
 * All counters are thread-safe ({@link AtomicLong}).
 *
 * <p>Access via {@link TailwindFX#metrics()} or directly:
 * <pre>
 * TailwindFXMetrics m = TailwindFXMetrics.instance();
 * System.out.println(m.report());
 *
 * // Output example:
 * // === TailwindFX Metrics ===
 * // JIT cache hits:    1 842  (91.3%)
 * // JIT cache misses:    175
 * // JIT compilations:    175
 * // apply() calls:     2 310
 * // conflict resolutions: 88
 * // theme switches:        3
 * // Uptime:            00:04:32
 * </pre>
 *
 * <p>Reset counters at any point with {@link #reset()}.
 * Metrics are disabled by default — enable with {@link #setEnabled(boolean)}.
 */
public final class TailwindFXMetrics {

    // Singleton
    private static final TailwindFXMetrics INSTANCE = new TailwindFXMetrics();

    public static TailwindFXMetrics instance() { return INSTANCE; }

    private TailwindFXMetrics() {}

    // =========================================================================
    // State
    // =========================================================================

    private volatile boolean enabled = true;
    private final long startTime = System.currentTimeMillis();

    // JIT cache
    private final AtomicLong jitCacheHits   = new AtomicLong();
    private final AtomicLong jitCacheMisses = new AtomicLong();
    private final AtomicLong jitCompilations = new AtomicLong();

    // apply() / applyAll()
    private final AtomicLong applyCalls     = new AtomicLong();

    // UtilityConflictResolver
    private final AtomicLong conflictResolutions = new AtomicLong();

    // Theme switches
    private final AtomicLong themeSwitches  = new AtomicLong();

    // Animation plays
    private final AtomicLong animationPlays = new AtomicLong();

    // Per-token compilation time (ns), stored as running average
    private final AtomicLong totalCompileNs     = new AtomicLong();
    private final AtomicLong totalCompileCount   = new AtomicLong();

    // Per-category conflict resolution counts
    private final ConcurrentHashMap<String, AtomicLong> conflictsByCategory =
        new ConcurrentHashMap<>();

    // =========================================================================
    // Record methods — called internally by TailwindFX components
    // =========================================================================

    /** Records a JIT cache hit. */
    public void recordCacheHit()   { if (enabled) jitCacheHits.incrementAndGet(); }

    /** Records a JIT cache miss. */
    public void recordCacheMiss()  {
        if (!enabled) return;
        jitCacheMisses.incrementAndGet();
        // Sample alerts every 50 misses to avoid overhead
        if (jitCacheMisses.get() % 50 == 0) checkAlerts();
    }

    /** Records a JIT compilation. */
    public void recordCompilation() { if (enabled) jitCompilations.incrementAndGet(); }

    /**
     * Records a JIT compilation with timing.
     *
     * @param durationNs duration of the compilation in nanoseconds
     */
    public void recordCompilation(long durationNs) {
        if (!enabled) return;
        jitCompilations.incrementAndGet();
        totalCompileNs.addAndGet(durationNs);
        totalCompileCount.incrementAndGet();
    }

    /**
     * Records a call to {@code apply()} or {@code applyAll()}.
     *
     * @param count number of classes applied in this call
     */
    public void recordApply(int count) {
        if (enabled) applyCalls.addAndGet(count);
    }

    /**
     * Records a conflict resolution for a given utility category.
     *
     * @param category the utility category (e.g. "w", "p", "shadow")
     */
    public void recordConflictResolution(String category) {
        if (!enabled) return;
        conflictResolutions.incrementAndGet();
        conflictsByCategory
            .computeIfAbsent(category, k -> new AtomicLong())
            .incrementAndGet();
    }

    /** Records a theme switch (preset change or dark/light toggle). */
    public void recordThemeSwitch() { if (enabled) themeSwitches.incrementAndGet(); }

    /** Records an animation start. */
    public void recordAnimationPlay() { if (enabled) animationPlays.incrementAndGet(); }

    // Layout pass timing (FxFlexPane)
    private final AtomicLong layoutPasses   = new AtomicLong();
    private final AtomicLong totalLayoutNs  = new AtomicLong();

    /**
     * Records a layout pass from {@link FxFlexPane#layoutChildren}.
     *
     * @param durationNs duration of the layout pass in nanoseconds
     */
    public void recordLayoutPass(long durationNs) {
        if (!enabled) return;
        layoutPasses.incrementAndGet();
        totalLayoutNs.addAndGet(durationNs);
    }

    /** Total number of FxFlexPane layout passes since last reset. */
    public long layoutPasses() { return layoutPasses.get(); }

    /**
     * Average FxFlexPane layout time in nanoseconds.
     * Returns {@code 0} if no layout passes have been recorded.
     */
    public long avgLayoutNs() {
        long count = layoutPasses.get();
        return count == 0 ? 0 : totalLayoutNs.get() / count;
    }

    // =========================================================================
    // Read methods
    // =========================================================================

    /** Total JIT cache hits since last reset. */
    public long cacheHits()   { return jitCacheHits.get(); }

    /** Total JIT cache misses since last reset. */
    public long cacheMisses() { return jitCacheMisses.get(); }

    /** Total JIT compilations since last reset. */
    public long compilations() { return jitCompilations.get(); }

    /** Total {@code apply()} calls since last reset. */
    public long applyCalls()  { return applyCalls.get(); }

    /** Total conflict resolutions since last reset. */
    public long conflictResolutions() { return conflictResolutions.get(); }

    /** Total theme switches since last reset. */
    public long themeSwitches() { return themeSwitches.get(); }

    /** Total animation plays since last reset. */
    public long animationPlays() { return animationPlays.get(); }

    /**
     * JIT cache hit ratio as a fraction [0.0, 1.0].
     * Returns {@code 1.0} if no lookups have been made yet.
     */
    public double cacheHitRatio() {
        long total = jitCacheHits.get() + jitCacheMisses.get();
        return total == 0 ? 1.0 : (double) jitCacheHits.get() / total;
    }

    /**
     * Average compilation time per token in nanoseconds.
     * Returns {@code 0} if no timed compilations have been recorded.
     */
    public long avgCompileNs() {
        long count = totalCompileCount.get();
        return count == 0 ? 0 : totalCompileNs.get() / count;
    }

    /**
     * Returns conflict counts grouped by utility category, sorted by frequency descending.
     *
     * @return unmodifiable map of category name → conflict count
     */
    public Map<String, Long> conflictsByCategory() {
        Map<String, Long> result = new LinkedHashMap<>();
        conflictsByCategory.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
            .forEach(e -> result.put(e.getKey(), e.getValue().get()));
        return Collections.unmodifiableMap(result);
    }

    /** Uptime in milliseconds since metrics were created. */
    public long uptimeMs() { return System.currentTimeMillis() - startTime; }

    // =========================================================================
    // Control
    // =========================================================================

    /**
     * Enables or disables metric collection. Disabled by default.
     * When disabled, all {@code record*()} calls are no-ops.
     *
     * @param enabled {@code true} to enable, {@code false} to disable
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /** Returns whether metric collection is currently enabled. */
    public boolean isEnabled() { return enabled; }

    // =========================================================================
    // Metric alerts
    // =========================================================================

    /** Alert callback registered via {@link #onAlert}. */
    @FunctionalInterface
    public interface MetricAlert {
        /**
         * Called when a metric threshold is breached.
         *
         * @param metric  name of the metric (e.g. {@code "cacheHitRatio"})
         * @param current current value
         * @param threshold the configured threshold
         */
        void onAlert(String metric, double current, double threshold);
    }

    private MetricAlert alertHandler = null;

    // Thresholds — 0 = disabled
    private volatile double minCacheHitRatio    = 0;  // alert below this
    private volatile double maxConflictRate     = 0;  // alert above this (conflicts/apply)
    private volatile double maxAvgCompileMs     = 0;  // alert above this

    /**
     * Registers an alert handler for metric threshold violations.
     *
     * <pre>
     * TailwindFX.metrics()
     *     .onAlert((metric, value, threshold) ->
     *         System.err.printf("[TailwindFX] Alert: %s=%.2f (threshold=%.2f)%n",
     *             metric, value, threshold))
     *     .alertOnLowCacheHitRatio(0.70)
     *     .alertOnHighConflictRate(0.30)
     *     .alertOnSlowCompile(0.5);
     * </pre>
     *
     * @param handler the alert callback (null to clear)
     * @return this for chaining
     */
    public TailwindFXMetrics onAlert(MetricAlert handler) {
        this.alertHandler = handler;
        return this;
    }

    /**
     * Fires an alert when the JIT cache hit ratio drops below {@code minRatio}.
     *
     * @param minRatio minimum acceptable ratio [0.0, 1.0] (e.g. {@code 0.70} = 70%)
     * @return this for chaining
     */
    public TailwindFXMetrics alertOnLowCacheHitRatio(double minRatio) {
        this.minCacheHitRatio = Math.max(0, Math.min(1, minRatio));
        return this;
    }

    /**
     * Fires an alert when conflict resolutions per apply call exceed {@code maxRate}.
     *
     * @param maxRate maximum acceptable conflict rate (e.g. {@code 0.30} = 30%)
     * @return this for chaining
     */
    public TailwindFXMetrics alertOnHighConflictRate(double maxRate) {
        this.maxConflictRate = Math.max(0, maxRate);
        return this;
    }

    /**
     * Fires an alert when the average JIT compilation time exceeds {@code maxMs}.
     *
     * @param maxMs maximum acceptable average compile time in milliseconds
     * @return this for chaining
     */
    public TailwindFXMetrics alertOnSlowCompile(double maxMs) {
        this.maxAvgCompileMs = Math.max(0, maxMs);
        return this;
    }

    /** Checks all configured thresholds and fires alerts if breached. */
    private void checkAlerts() {
        if (alertHandler == null || !enabled) return;
        long total = jitCacheHits.get() + jitCacheMisses.get();
        if (minCacheHitRatio > 0 && total > 100) {
            double ratio = cacheHitRatio();
            if (ratio < minCacheHitRatio)
                alertHandler.onAlert("cacheHitRatio", ratio, minCacheHitRatio);
        }
        if (maxConflictRate > 0 && applyCalls.get() > 0) {
            double rate = (double) conflictResolutions.get() / applyCalls.get();
            if (rate > maxConflictRate)
                alertHandler.onAlert("conflictRate", rate, maxConflictRate);
        }
        if (maxAvgCompileMs > 0 && totalCompileCount.get() > 0) {
            double avgMs = avgCompileNs() / 1_000_000.0;
            if (avgMs > maxAvgCompileMs)
                alertHandler.onAlert("avgCompileMs", avgMs, maxAvgCompileMs);
        }
    }

    /**
     * Resets all counters to zero. Does not reset uptime or enabled state.
     */
    public void reset() {
        jitCacheHits.set(0);
        jitCacheMisses.set(0);
        jitCompilations.set(0);
        applyCalls.set(0);
        conflictResolutions.set(0);
        themeSwitches.set(0);
        animationPlays.set(0);
        totalCompileNs.set(0);
        totalCompileCount.set(0);
        conflictsByCategory.clear();
        layoutPasses.set(0);
        totalLayoutNs.set(0);
    }

    // =========================================================================
    // Report
    // =========================================================================

    /**
     * Returns a formatted human-readable report of all metrics.
     *
     * <pre>
     * === TailwindFX Metrics ===
     * JIT cache hits:       1 842  (91.3%)
     * JIT cache misses:       175
     * JIT compilations:       175  (avg 0.012 ms)
     * apply() calls:        2 310
     * Conflict resolutions:    88  {w=34, p=22, shadow=12, ...}
     * Theme switches:           3
     * Animation plays:         47
     * Uptime:              00:04:32
     * </pre>
     *
     * @return formatted metrics report
     */
    public String report() {
        long hits      = jitCacheHits.get();
        long misses    = jitCacheMisses.get();
        long compiles  = jitCompilations.get();
        long applies   = applyCalls.get();
        long conflicts = conflictResolutions.get();
        long themes    = themeSwitches.get();
        long anims     = animationPlays.get();
        long upMs      = uptimeMs();

        String hitPct = (hits + misses) == 0 ? "N/A"
            : String.format("%.1f%%", cacheHitRatio() * 100);
        String avgMs = totalCompileCount.get() == 0 ? "N/A"
            : String.format("%.3f ms", avgCompileNs() / 1_000_000.0);

        long s = upMs / 1000, m = s / 60, h = m / 60;
        String uptime = String.format("%02d:%02d:%02d", h, m % 60, s % 60);

        StringBuilder sb = new StringBuilder();
        sb.append("=== TailwindFX Metrics ===\n");
        sb.append(String.format("  JIT cache hits:       %,8d  (%s)%n",  hits,      hitPct));
        sb.append(String.format("  JIT cache misses:     %,8d%n",         misses));
        sb.append(String.format("  JIT compilations:     %,8d  (avg %s)%n", compiles, avgMs));
        sb.append(String.format("  apply() calls:        %,8d%n",         applies));
        sb.append(String.format("  Conflict resolutions: %,8d%n",         conflicts));

        if (!conflictsByCategory.isEmpty()) {
            Map<String, Long> top = conflictsByCategory();
            sb.append("  Top conflicts:        ");
            top.entrySet().stream().limit(5)
               .forEach(e -> sb.append(e.getKey()).append("=").append(e.getValue()).append(" "));
            sb.append("\n");
        }

        sb.append(String.format("  Theme switches:       %,8d%n", themes));
        sb.append(String.format("  Animation plays:      %,8d%n", anims));
        sb.append(String.format("  Uptime:                  %s%n", uptime));
        if (layoutPasses.get() > 0) {
            String avgLayout = String.format("%.3f ms", avgLayoutNs() / 1_000_000.0);
            sb.append(String.format("  Layout passes:        %,8d  (avg %s)%n",
                layoutPasses.get(), avgLayout));
        }
        return sb.toString();
    }

    /** Prints the metrics report to stdout. */
    public void print() { System.out.print(report()); }

    @Override
    public String toString() { return report(); }

    // =========================================================================
    // Health alerts
    // =========================================================================

    /**
     * A health issue detected from current metrics.
     *
     * @param level   severity: {@code "WARN"} or {@code "ERROR"}
     * @param message human-readable description
     * @param advice  recommended fix
     */
    public record HealthIssue(String level, String message, String advice) {
        @Override public String toString() {
            return "[" + level + "] " + message + " → " + advice;
        }
    }

    /**
     * Analyzes current metrics and returns a list of health issues.
     *
     * <p>Checks performed:
     * <ul>
     *   <li><b>Low JIT cache hit ratio</b> (&lt;50% after 100+ lookups) — possible token explosion</li>
     *   <li><b>High conflict resolution rate</b> (&gt;30% of apply calls) — excessive class churn</li>
     *   <li><b>Slow layout passes</b> (&gt;2ms average) — FxFlexPane/FxGridPane overload</li>
     *   <li><b>Slow JIT compilation</b> (&gt;1ms average) — complex token patterns</li>
     *   <li><b>Metrics not enabled</b> — no data collected yet</li>
     * </ul>
     *
     * <pre>
     * TailwindFX.metrics().checkHealth()
     *     .forEach(System.out::println);
     * </pre>
     *
     * @return unmodifiable list of issues (empty = healthy)
     */
    public java.util.List<HealthIssue> checkHealth() {
        java.util.List<HealthIssue> issues = new java.util.ArrayList<>();

        long total = cacheHits() + cacheMisses();
        if (total >= 100 && cacheHitRatio() < 0.50) {
            issues.add(new HealthIssue("WARN",
                String.format("Low JIT cache hit ratio: %.1f%% (%d lookups)", cacheHitRatio() * 100, total),
                "Use TailwindFX.apply() for stable classes; reserve jit() for dynamic values"));
        }

        if (applyCalls() > 0 && conflictResolutions() > applyCalls() * 0.30) {
            issues.add(new HealthIssue("WARN",
                String.format("High conflict resolution rate: %d conflicts / %d applies (%.0f%%)",
                    conflictResolutions(), applyCalls(),
                    (double) conflictResolutions() / applyCalls() * 100),
                "Use applyRaw() for intentional stacking, or apply all classes in one call"));
        }

        if (layoutPasses() >= 10 && avgLayoutNs() > 2_000_000) {
            issues.add(new HealthIssue("WARN",
                String.format("Slow FxFlexPane/FxGridPane layout: avg %.2f ms over %d passes",
                    avgLayoutNs() / 1_000_000.0, layoutPasses()),
                "Reduce children count or avoid frequent direction/flow changes"));
        }

        if (compilations() >= 10 && avgCompileNs() > 1_000_000) {
            issues.add(new HealthIssue("WARN",
                String.format("Slow JIT compilation: avg %.2f ms over %d compilations",
                    avgCompileNs() / 1_000_000.0, compilations()),
                "Pre-compile common tokens at startup using apply() which auto-detects JIT tokens"));
        }

        if (!enabled || (cacheHits() == 0 && cacheMisses() == 0 && compilations() == 0)) {
            issues.add(new HealthIssue("WARN",
                "No metrics data collected",
                "Call TailwindFXMetrics.instance().setEnabled(true) before running your app"));
        }

        return java.util.Collections.unmodifiableList(issues);
    }

    /**
     * Prints a health report to stdout.
     *
     * <pre>
     * TailwindFX.metrics().printHealth();
     * // ✅ All healthy   — or —
     * // [WARN] Low JIT cache hit ratio: 34.2% (250 lookups)
     * //   → Use TailwindFX.apply() for stable classes...
     * </pre>
     */
    public void printHealth() {
        java.util.List<HealthIssue> issues = checkHealth();
        if (issues.isEmpty()) {
            System.out.println("TailwindFX metrics: ✅ All healthy");
        } else {
            System.out.println("TailwindFX metrics health report:");
            for (HealthIssue issue : issues) {
                System.out.println("  " + issue.level() + ": " + issue.message());
                System.out.println("        → " + issue.advice());
            }
        }
    }
}
