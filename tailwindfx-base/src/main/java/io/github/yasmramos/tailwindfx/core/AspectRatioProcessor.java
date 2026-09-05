package io.github.yasmramos.tailwindfx.core;

/**
 * AspectRatioProcessor — Handles Tailwind CSS v4 aspect-ratio utilities.
 *
 * <p>Aspect ratio utilities control the proportional relationship between width and height.
 * Commonly used for responsive media containers, cards, and layouts.
 *
 * <p>Supported utilities:
 *
 * <ul>
 *   <li>aspect-{ratio} — Set aspect ratio (aspect-square, aspect-video, etc.)
 *   <li>aspect-[value] — Arbitrary aspect ratio (aspect-[4/3], aspect-[16/9])
 * </ul>
 *
 * <p>JavaFX Implementation: Uses -fx-pref-width and -fx-pref-height bindings to maintain aspect
 * ratio, or sets explicit dimensions based on parent container.
 */
public final class AspectRatioProcessor {

  private static final String ASPECT_PREFIX = "aspect";

  private AspectRatioProcessor() {
    // Utility class
  }

  /**
   * Processes an aspect-ratio token and returns the corresponding CSS.
   *
   * @param tokenRaw The raw token string (e.g., "aspect-square", "aspect-video")
   * @return CSS string or null if not an aspect-ratio token
   */
  public static String processAspectRatio(String tokenRaw) {
    if (tokenRaw == null || tokenRaw.isBlank()) {
      return null;
    }

    if (!tokenRaw.startsWith(ASPECT_PREFIX + "-")) {
      return null;
    }

    String value = tokenRaw.substring(ASPECT_PREFIX.length() + 1);

    if (value.isBlank()) {
      return null;
    }

    // Handle arbitrary values: aspect-[4/3], aspect-[16/9]
    if (value.startsWith("[") && value.endsWith("]")) {
      String arbitraryValue = value.substring(1, value.length() - 1);
      return buildAspectRatio(arbitraryValue);
    }

    // Handle named ratios
    return switch (value) {
      case "square" -> buildAspectRatio("1 / 1");
      case "video" -> buildAspectRatio("16 / 9");
      case "auto" -> "-fx-pref-width: -1; -fx-pref-height: -1;"; // USE_COMPUTED_SIZE
      case "portrait" -> buildAspectRatio("3 / 4");
      case "landscape" -> buildAspectRatio("4 / 3");
      default -> {
        // Try to parse as a fraction like "4/3" or decimal like "1.5"
        String css = tryParseRatio(value);
        yield css != null ? css : null;
      }
    };
  }

  /**
   * Checks if a token is an aspect-ratio token.
   *
   * @param tokenRaw The raw token string
   * @return true if it's an aspect-ratio token
   */
  public static boolean isAspectRatioToken(String tokenRaw) {
    if (tokenRaw == null || tokenRaw.isBlank()) {
      return false;
    }
    return tokenRaw.startsWith("aspect-");
  }

  private static String buildAspectRatio(String ratio) {
    // JavaFX doesn't have native aspect-ratio support in inline styles
    // We use a marker property that can be interpreted by layout managers
    // Format: -fx-aspect-ratio: width / height;

    // Normalize the ratio
    String normalized = normalizeRatio(ratio);
    if (normalized == null) {
      return null;
    }

    return "-fx-aspect-ratio: " + normalized + ";";
  }

  private static String normalizeRatio(String ratio) {
    if (ratio == null || ratio.isBlank()) {
      return null;
    }

    ratio = ratio.trim();

    // Handle "width/height" format
    if (ratio.contains("/")) {
      String[] parts = ratio.split("/", 2);
      if (parts.length == 2) {
        try {
          double width = Double.parseDouble(parts[0].trim());
          double height = Double.parseDouble(parts[1].trim());
          if (height == 0) {
            return null;
          }
          return String.format("%.4f", width / height);
        } catch (NumberFormatException e) {
          // If parsing fails, return as-is for CSS
          return ratio.replace(" ", "");
        }
      }
    }

    // Handle decimal format directly
    try {
      double value = Double.parseDouble(ratio);
      if (value <= 0) {
        return null;
      }
      return String.valueOf(value);
    } catch (NumberFormatException e) {
      // Not a valid number
      return null;
    }
  }

  private static String tryParseRatio(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    // Try direct decimal
    try {
      double ratio = Double.parseDouble(value);
      if (ratio > 0) {
        return buildAspectRatio(String.valueOf(ratio));
      }
    } catch (NumberFormatException e) {
      // Ignore
    }

    // Try fraction format without spaces
    if (value.contains("/")) {
      return buildAspectRatio(value);
    }

    return null;
  }

  /**
   * Gets the numeric aspect ratio value for a named ratio.
   *
   * @param name The named ratio (e.g., "square", "video")
   * @return The numeric ratio (width/height), or -1 if not found
   */
  public static double getNumericRatio(String name) {
    return switch (name) {
      case "square" -> 1.0;
      case "video" -> 16.0 / 9.0;
      case "portrait" -> 3.0 / 4.0;
      case "landscape" -> 4.0 / 3.0;
      default -> -1.0;
    };
  }

  /**
   * Calculates height based on width and aspect ratio.
   *
   * @param width The width
   * @param aspectRatio The aspect ratio (width/height)
   * @return The calculated height
   */
  public static double calculateHeight(double width, double aspectRatio) {
    if (aspectRatio <= 0) {
      return width; // Default to square
    }
    return width / aspectRatio;
  }

  /**
   * Calculates width based on height and aspect ratio.
   *
   * @param height The height
   * @param aspectRatio The aspect ratio (width/height)
   * @return The calculated width
   */
  public static double calculateWidth(double height, double aspectRatio) {
    if (aspectRatio <= 0) {
      return height; // Default to square
    }
    return height * aspectRatio;
  }
}
