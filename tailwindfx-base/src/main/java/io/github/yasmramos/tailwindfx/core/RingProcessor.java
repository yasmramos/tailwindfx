package io.github.yasmramos.tailwindfx.core;

import io.github.yasmramos.tailwindfx.color.ColorPalette;
import io.github.yasmramos.tailwindfx.style.StyleToken;

/**
 * RingProcessor — Handles Tailwind CSS v4 ring utilities.
 *
 * <p>Ring utilities create outline rings around elements, commonly used for focus states. Unlike
 * borders, rings don't affect layout and are drawn outside the element's border.
 *
 * <p>Supported utilities:
 *
 * <ul>
 *   <li>ring-{width} — Set ring width (ring-0, ring-1, ring-2, ring-4, ring-8)
 *   <li>ring-{color} — Set ring color (ring-blue-500, ring-red-300/50)
 *   <li>ring-opacity-{amount} — Set ring opacity (ring-opacity-50)
 *   <li>ring-offset-{width} — Set offset width (ring-offset-0, ring-offset-1, etc.)
 *   <li>ring-offset-{color} — Set offset color (ring-offset-blue-500)
 * </ul>
 *
 * <p>JavaFX Implementation: Uses -fx-effect with dropshadow to simulate rings, combined with
 * -fx-border-color for the actual ring appearance.
 */
public final class RingProcessor {

  private static final String RING_PREFIX = "ring";
  private static final String RING_OPACITY_PREFIX = "ring-opacity";
  private static final String RING_OFFSET_PREFIX = "ring-offset";

  private RingProcessor() {
    // Utility class
  }

  /**
   * Processes a ring-related token and returns the corresponding CSS.
   *
   * @param token The parsed style token
   * @return CSS string or null if not a ring token
   */
  public static String processRingToken(StyleToken token) {
    if (token == null || token.prefix == null) {
      return null;
    }

    if (RING_OFFSET_PREFIX.equals(token.prefix)) {
      return processRingOffset(token);
    }

    if (RING_OPACITY_PREFIX.equals(token.prefix)) {
      return processRingOpacity(token);
    }

    if (RING_PREFIX.equals(token.prefix)) {
      return processRing(token);
    }

    return null;
  }

  /**
   * Checks if a token is ring-related.
   *
   * @param tokenRaw The raw token string
   * @return true if it's a ring token
   */
  public static boolean isRingToken(String tokenRaw) {
    if (tokenRaw == null || tokenRaw.isBlank()) {
      return false;
    }
    return tokenRaw.startsWith("ring-") || tokenRaw.startsWith("ring-offset-");
  }

  private static String processRing(StyleToken token) {
    // ring-0, ring-1, ring-2, ring-4, ring-8
    if (token.kind == StyleToken.Kind.SCALE && token.scale != null) {
      int width = token.signedScale();
      if (width < 0) {
        return null; // Negative rings not supported
      }
      return buildRingWidth(width);
    }

    // ring-{color}-{shade} or ring-{color}-{shade}/{alpha}
    if (token.kind == StyleToken.Kind.COLOR_SHADE && token.colorName != null) {
      String color = resolveRingColor(token);
      if (color != null) {
        return "-fx-border-color: " + color + "; -fx-border-width: 3px;";
      }
    }

    // ring-{named-color} like ring-white, ring-black, ring-transparent
    if (token.kind == StyleToken.Kind.NAMED && token.namedValue != null) {
      String color = resolveNamedRingColor(token.namedValue);
      if (color != null) {
        return "-fx-border-color: " + color + "; -fx-border-width: 3px;";
      }
    }

    // Arbitrary value: ring-[3px] or ring-[#ff0000]
    if (token.kind == StyleToken.Kind.ARBITRARY && token.arbitraryVal != null) {
      String arbitraryVal = token.arbitraryVal;

      // Check if it's a color
      if (arbitraryVal.startsWith("#")
          || arbitraryVal.startsWith("rgb")
          || arbitraryVal.startsWith("rgba")) {
        return "-fx-border-color: " + arbitraryVal + "; -fx-border-width: 3px;";
      }

      // Otherwise assume it's a width
      return buildRingWidthCss(arbitraryVal);
    }

    return null;
  }

  private static String processRingOffset(StyleToken token) {
    // ring-offset-{width}
    if (token.kind == StyleToken.Kind.SCALE && token.scale != null) {
      int width = token.signedScale();
      if (width < 0) {
        return null;
      }
      // JavaFX doesn't have direct ring-offset support
      // Simulate with padding adjustment
      return "-fx-padding: " + width + "px;";
    }

    // ring-offset-{color}
    if (token.kind == StyleToken.Kind.COLOR_SHADE && token.colorName != null) {
      String color = ColorPalette.hex(token.colorName, token.shade);
      if (color != null) {
        // Simulate offset with background color
        return "-fx-background-color: " + color + ";";
      }
    }

    // ring-offset-{named-color}
    if (token.kind == StyleToken.Kind.NAMED && token.namedValue != null) {
      String color = resolveNamedRingColor(token.namedValue);
      if (color != null) {
        return "-fx-background-color: " + color + ";";
      }
    }

    return null;
  }

  private static String processRingOpacity(StyleToken token) {
    if (token.kind == StyleToken.Kind.SCALE && token.scale != null) {
      int opacity = token.scale;
      if (opacity < 0 || opacity > 100) {
        return null;
      }
      double alpha = opacity / 100.0;
      return "-fx-opacity: " + alpha + ";";
    }
    return null;
  }

  private static String buildRingWidth(int widthInPx) {
    if (widthInPx == 0) {
      // For ring-0, only set border-width to 0, do not emit -fx-effect: null;
      // as "null" is not a valid CSS value for -fx-effect.
      return "-fx-border-width: 0px;";
    }
    // Standard ring widths from Tailwind: 1→1px, 2→2px, 4→4px, 8→8px
    return buildRingWidthCss(widthInPx + "px");
  }

  private static String buildRingWidthCss(String width) {
    // JavaFX uses -fx-border-width for ring simulation
    // Also add a subtle effect for better visibility
    return "-fx-border-width: " + width + "; -fx-border-style: solid;";
  }

  private static String resolveRingColor(StyleToken token) {
    String baseColor = ColorPalette.hex(token.colorName, token.shade);
    if (baseColor == null) {
      return null;
    }

    if (token.alpha != null) {
      // Apply alpha to color
      return applyAlpha(baseColor, token.alpha);
    }

    return baseColor;
  }

  private static String resolveNamedRingColor(String colorName) {
    return switch (colorName) {
      case "white" -> "#ffffff";
      case "black" -> "#000000";
      case "transparent" -> "transparent";
      case "current" -> "derive(-fx-text-fill, 0%)"; // Use current text color
      default -> ColorPalette.hex(colorName, 500); // Default to shade 500
    };
  }

  private static String applyAlpha(String hexColor, int alphaPercent) {
    if (hexColor == null || !hexColor.startsWith("#")) {
      return hexColor;
    }

    try {
      javafx.scene.paint.Color color = javafx.scene.paint.Color.web(hexColor);
      double alpha = alphaPercent / 100.0;
      return String.format(
          "rgba(%d,%d,%d,%.2f)",
          (int) (color.getRed() * 255),
          (int) (color.getGreen() * 255),
          (int) (color.getBlue() * 255),
          alpha);
    } catch (Exception e) {
      return hexColor;
    }
  }

  /**
   * Gets the default ring width in pixels for a given scale value.
   *
   * @param scale The Tailwind scale value (0, 1, 2, 4, 8)
   * @return Width in pixels
   */
  public static int getDefaultRingWidth(int scale) {
    return switch (scale) {
      case 0 -> 0;
      case 1 -> 1;
      case 2 -> 2;
      case 4 -> 4;
      case 8 -> 8;
      default -> scale;
    };
  }
}
