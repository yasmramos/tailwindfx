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

  private TwMetrics() {}

  /**
   * Generates a debug report of the collected metrics.
   *
   * @param node the node the report relates to; currently informational only
   * @return the formatted metrics report
   */
  public static String debugReport(Node node) {
    // Delegate to existing report mechanism
    return TailwindFXMetrics.instance().report();
  }

  /** Runs a health check on the TailwindFX system and prints the result. */
  public static void healthCheck() {
    TailwindFXMetrics.instance().printHealth();
  }

  /**
   * Enables or disables metrics collection.
   *
   * @param enabled {@code true} to collect metrics, {@code false} to stop
   */
  public static void setEnabled(boolean enabled) {
    TailwindFXMetrics.instance().setEnabled(enabled);
  }

  /**
   * Returns whether metrics collection is enabled.
   *
   * @return {@code true} if metrics are being collected
   */
  public static boolean isEnabled() {
    return TailwindFXMetrics.instance().isEnabled();
  }
}
