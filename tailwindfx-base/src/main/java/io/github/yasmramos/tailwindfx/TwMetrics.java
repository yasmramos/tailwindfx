package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import javafx.scene.Node;

/**
 * TwMetrics — Metrics facade for performance monitoring.
 *
 * <p>Provides access to TailwindFX metrics, debug reports, and health checks.
 *
 * <pre>
 * String report = TwMetrics.debugReport(node);
 * TwMetrics.healthCheck();
 * TwMetrics.setEnabled(true);
 * </pre>
 */
public final class TwMetrics {

  private static final TwMetrics INSTANCE = new TwMetrics();

  private TwMetrics() {}

  /** Generates a debug report for a node. */
  public static String debugReport(Node node) {
    // Delegate to existing report mechanism
    return TailwindFXMetrics.instance().report();
  }

  /** Runs a health check on the TailwindFX system. */
  public static void healthCheck() {
    TailwindFXMetrics.instance().printHealth();
  }

  /** Enables or disables metrics collection. */
  public static void setEnabled(boolean enabled) {
    TailwindFXMetrics.instance().setEnabled(enabled);
  }

  /** Returns whether metrics collection is enabled. */
  public static boolean isEnabled() {
    return TailwindFXMetrics.instance().isEnabled();
  }
}
