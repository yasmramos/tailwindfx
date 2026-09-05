package io.github.yasmramos.tailwindfx.core;

/**
 * ScrollSnapProcessor — Handles Tailwind CSS v4 scroll-snap utilities.
 *
 * <p>Scroll snap utilities create scroll containers that "snap" to specific points, commonly used
 * for carousels, image galleries, and section-based layouts.
 *
 * <p>Supported utilities:
 *
 * <ul>
 *   <li>snap-{axis} — Set snap axis (snap-x, snap-y, snap-both)
 *   <li>snap-{type} — Set snap type (snap-mandatory, snap-proximity)
 *   <li>snap-{align} — Set snap alignment (snap-start, snap-end, snap-center, snap-always)
 *   <li>snap-{stop} — Set snap stop behavior (snap-normal, snap-always)
 * </ul>
 *
 * <p>JavaFX Implementation: Uses custom properties that can be interpreted by ScrollPane or custom
 * scroll handlers to implement snapping behavior.
 */
public final class ScrollSnapProcessor {

  private static final String SNAP_PREFIX = "snap";

  private ScrollSnapProcessor() {
    // Utility class
  }

  /**
   * Processes a scroll-snap token and returns the corresponding CSS.
   *
   * @param tokenRaw The raw token string
   * @return CSS string or null if not a scroll-snap token (JavaFX doesn't support scroll-snap via CSS)
   */
  public static String processScrollSnap(String tokenRaw) {
    // JavaFX does not support scroll-snap via CSS properties.
    // Scroll snap should be handled programmatically via ScrollPane.
    // Return null to prevent invalid -fx-snap-* properties from being injected.
    return null;
  }

  /**
   * Checks if a token is a scroll-snap token.
   *
   * @param tokenRaw The raw token string
   * @return true if it's a scroll-snap token
   */
  public static boolean isScrollSnapToken(String tokenRaw) {
    if (tokenRaw == null || tokenRaw.isBlank()) {
      return false;
    }
    return tokenRaw.startsWith("snap-");
  }

  /**
   * Gets the snap axis from a token.
   *
   * @param tokenRaw The raw token string
   * @return The axis (horizontal, vertical, both), or null if not applicable
   */
  public static String getSnapAxis(String tokenRaw) {
    if (tokenRaw == null) {
      return null;
    }
    return switch (tokenRaw) {
      case "snap-x" -> "horizontal";
      case "snap-y" -> "vertical";
      case "snap-both" -> "both";
      default -> null;
    };
  }

  /**
   * Gets the snap type from a token.
   *
   * @param tokenRaw The raw token string
   * @return The type (mandatory, proximity), or null if not applicable
   */
  public static String getSnapType(String tokenRaw) {
    if (tokenRaw == null) {
      return null;
    }
    return switch (tokenRaw) {
      case "snap-mandatory" -> "mandatory";
      case "snap-proximity" -> "proximity";
      default -> null;
    };
  }

  /**
   * Gets the snap alignment from a token.
   *
   * @param tokenRaw The raw token string
   * @return The alignment (start, end, center), or null if not applicable
   */
  public static String getSnapAlign(String tokenRaw) {
    if (tokenRaw == null) {
      return null;
    }
    return switch (tokenRaw) {
      case "snap-start" -> "start";
      case "snap-end" -> "end";
      case "snap-center" -> "center";
      default -> null;
    };
  }

  /**
   * Gets the snap stop behavior from a token.
   *
   * @param tokenRaw The raw token string
   * @return The stop behavior (normal, always), or null if not applicable
   */
  public static String getSnapStop(String tokenRaw) {
    if (tokenRaw == null) {
      return null;
    }
    return switch (tokenRaw) {
      case "snap-normal" -> "normal";
      case "snap-always" -> "always";
      default -> null;
    };
  }
}
