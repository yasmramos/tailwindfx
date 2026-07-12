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
}
