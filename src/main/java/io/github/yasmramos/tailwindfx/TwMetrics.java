package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import javafx.scene.Node;

/**
 * TwMetrics — Metrics facade for performance monitoring.
 * 
 * <p>Provides access to TailwindFX metrics, debug reports, and health checks.</p>
 * 
 * <pre>
 * String report = TwMetrics.INSTANCE.debugReport(node);
 * TwMetrics.INSTANCE.healthCheck();
 * </pre>
 */
public final class TwMetrics {
    
    public static final TwMetrics INSTANCE = new TwMetrics();
    
    private TwMetrics() {}
    
    /**
     * Generates a debug report for a node.
     */
    public String debugReport(Node node) {
        // Delegate to existing report mechanism
        return TailwindFXMetrics.instance().report();
    }
    
    /**
     * Runs a health check on the TailwindFX system.
     */
    public void healthCheck() {
        TailwindFXMetrics.instance().printHealth();
    }
}
