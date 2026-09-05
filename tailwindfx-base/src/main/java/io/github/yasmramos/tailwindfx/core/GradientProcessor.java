package io.github.yasmramos.tailwindfx.core;

import io.github.yasmramos.tailwindfx.color.ColorPalette;
import javafx.scene.paint.Color;

/**
 * GradientProcessor — Dedicated processor for Tailwind gradient utilities.
 *
 * <p>Handles parsing and building of linear gradients from tokens like:
 *
 * <ul>
 *   <li>{@code bg-gradient-to-r} → direction
 *   <li>{@code from-blue-500} → start color
 *   <li>{@code via-purple-500} → middle color (optional)
 *   <li>{@code to-pink-500} → end color
 *   <li>{@code from-blue-500/80} → color with opacity
 * </ul>
 *
 * <p>Output: CSS-ready {@code linear-gradient(...)} for JavaFX {@code -fx-background-color}.
 *
 * @author yasmramos
 * @since 1.0.0
 */
public final class GradientProcessor {

  // Gradient token prefixes
  private static final String GRADIENT_PREFIX = "bg-gradient-to-";
  private static final String FROM_PREFIX = "from-";
  private static final String VIA_PREFIX = "via-";
  private static final String TO_PREFIX = "to-";

  /**
   * Result of gradient processing.
   *
   * @param inlineStyle CSS inline style string (e.g., "-fx-background-color:
   *     linear-gradient(...);")
   * @param isGradient true if gradient tokens were found and processed
   */
  public record GradientResult(String inlineStyle, boolean isGradient) {}

  /**
   * Processes an array of tokens and extracts gradient-related ones.
   *
   * @param tokens array of Tailwind tokens to process
   * @return GradientResult with CSS string and flag indicating if gradient was found
   */
  public static GradientResult processGradientTokens(String[] tokens) {
    if (tokens == null || tokens.length == 0) {
      return new GradientResult(null, false);
    }

    String direction = null;
    String fromColor = null;
    String viaColor = null;
    String toColor = null;
    boolean hasGradient = false;

    for (String token : tokens) {
      if (token == null || token.isBlank()) {
        continue;
      }

      // Check for gradient direction
      if (token.startsWith(GRADIENT_PREFIX)) {
        direction = mapDirection(token.substring(GRADIENT_PREFIX.length()));
        hasGradient = true;

      } else if (token.startsWith(FROM_PREFIX)) {
        String colorResolved = resolveGradientColor(token.substring(FROM_PREFIX.length()));
        if (colorResolved != null) {
          fromColor = colorResolved;
          hasGradient = true;
        }

      } else if (token.startsWith(VIA_PREFIX)) {
        String colorResolved = resolveGradientColor(token.substring(VIA_PREFIX.length()));
        if (colorResolved != null) {
          viaColor = colorResolved;
          hasGradient = true;
        }

      } else if (token.startsWith(TO_PREFIX)) {
        String colorResolved = resolveGradientColor(token.substring(TO_PREFIX.length()));
        if (colorResolved != null) {
          toColor = colorResolved;
          hasGradient = true;
        }
      }
    }

    if (!hasGradient) {
      return new GradientResult(null, false);
    }

    String css = buildGradient(direction, fromColor, viaColor, toColor);
    return new GradientResult(css, true);
  }

  /**
   * Maps Tailwind direction shorthand to CSS gradient direction.
   *
   * @param dir Tailwind direction code (e.g., "r", "tr", "b")
   * @return CSS direction string (e.g., "to right", "to top right")
   */
  private static String mapDirection(String dir) {
    return switch (dir) {
      case "t" -> "to top";
      case "tr" -> "to top right";
      case "r" -> "to right";
      case "br" -> "to bottom right";
      case "b" -> "to bottom"; // Default
      case "bl" -> "to bottom left";
      case "l" -> "to left";
      case "tl" -> "to top left";
      default -> "to bottom";
    };
  }

  /**
   * Resolves a color token to its hex/rgba value.
   *
   * <p>Supports:
   *
   * <ul>
   *   <li>Standard colors: {@code blue-500}, {@code gray-800}
   *   <li>Colors with opacity: {@code blue-500/80} → rgba(59,130,246,0.80)
   *   <li>Named colors: {@code transparent}, {@code white}, {@code black}
   *   <li>Arbitrary values: {@code [#fff]}, {@code [rgb(255,0,0)]}
   * </ul>
   *
   * @param colorToken the color token to resolve
   * @return CSS color value (hex or rgba), or null if unresolvable
   */
  private static String resolveGradientColor(String colorToken) {
    if (colorToken == null || colorToken.isBlank()) {
      return null;
    }

    // Handle opacity modifier (e.g., blue-500/80)
    if (colorToken.contains("/")) {
      int slashIdx = colorToken.indexOf('/');
      String baseColor = colorToken.substring(0, slashIdx);
      String opacityStr = colorToken.substring(slashIdx + 1);

      // Resolve base color first
      String baseHex = resolveGradientColor(baseColor);
      if (baseHex != null && baseHex.startsWith("#")) {
        try {
          Color color = Color.web(baseHex);
          double opacityValue = parseOpacity(opacityStr);
          return formatRgba(color, opacityValue);
        } catch (IllegalArgumentException e) {
          // Invalid color format, return base color without opacity
          return baseHex;
        }
      }
      return baseHex;
    }

    // Handle arbitrary values [...] or (...)
    if (isArbitraryValue(colorToken)) {
      String content = colorToken.substring(1, colorToken.length() - 1);
      // If it's already a valid CSS color, return as-is
      if (content.startsWith("#") || content.startsWith("rgb") || content.startsWith("hsl")) {
        return content;
      }
      // Try to resolve as color name
      return resolveNamedColor(content);
    }

    // Handle named colors without shade (transparent, white, black, etc.)
    if (!colorToken.contains("-")) {
      return resolveNamedColor(colorToken);
    }

    // Parse color-shade pattern (e.g., blue-500, gray-800)
    int lastDash = colorToken.lastIndexOf('-');
    if (lastDash == -1) {
      return resolveNamedColor(colorToken);
    }

    String colorName = colorToken.substring(0, lastDash);
    String shadeStr = colorToken.substring(lastDash + 1);

    try {
      int shade = Integer.parseInt(shadeStr);
      String hex = ColorPalette.hex(colorName, shade);
      return hex != null ? hex : resolveNamedColor(colorToken);
    } catch (NumberFormatException e) {
      // Not a valid shade number, try as named color
      return resolveNamedColor(colorToken);
    }
  }

  /**
   * Parses opacity value from string.
   *
   * @param opacityStr opacity as percentage (0-100) or decimal (0-1)
   * @return opacity as double between 0.0 and 1.0
   */
  private static double parseOpacity(String opacityStr) {
    if (opacityStr == null || opacityStr.isBlank()) {
      return 1.0;
    }

    try {
      double value = Double.parseDouble(opacityStr);
      // If value > 1, assume it's a percentage (0-100)
      if (value > 1.0) {
        return value / 100.0;
      }
      return value;
    } catch (NumberFormatException e) {
      return 1.0;
    }
  }

  /**
   * Formats a JavaFX Color as RGBA string.
   *
   * @param color the color to format
   * @param alpha opacity value (0.0-1.0)
   * @return RGBA string (e.g., "rgba(59,130,246,0.80)")
   */
  private static String formatRgba(Color color, double alpha) {
    return String.format(
        "rgba(%.0f,%.0f,%.0f,%.2f)",
        color.getRed() * 255,
        color.getGreen() * 255,
        color.getBlue() * 255,
        Math.max(0.0, Math.min(1.0, alpha)));
  }

  /**
   * Resolves a named color to its hex value.
   *
   * @param name color name (e.g., "white", "black", "transparent")
   * @return hex value or null if not found
   */
  private static String resolveNamedColor(String name) {
    if (name == null || name.isBlank()) {
      return null;
    }

    // Check common named colors
    return switch (name.toLowerCase()) {
      case "transparent" -> "transparent";
      case "white" -> "#ffffff";
      case "black" -> "#000000";
      case "red" -> "#ef4444";
      case "green" -> "#22c55e";
      case "blue" -> "#3b82f6";
      case "yellow" -> "#eab308";
      case "purple" -> "#a855f7";
      case "pink" -> "#ec4899";
      case "gray", "grey" -> "#6b7280";
      case "orange" -> "#f97316";
      case "teal" -> "#14b8a6";
      case "cyan" -> "#06b6d4";
      case "indigo" -> "#6366f1";
      case "lime" -> "#84cc16";
      case "emerald" -> "#10b981";
      case "rose" -> "#f43f5e";
      case "amber" -> "#f59e0b";
      case "sky" -> "#0ea5e9";
      case "violet" -> "#8b5cf6";
      case "fuchsia" -> "#d946ef";
      case "slate" -> "#475569";
      case "zinc" -> "#525252";
      case "neutral" -> "#737373";
      case "stone" -> "#78716c";
      default -> {
        // Try ColorPalette for extended names
        String hex = ColorPalette.hex(name, 500);
        yield hex != null ? hex : null;
      }
    };
  }

  /**
   * Checks if a string is an arbitrary value in [...] or (...) syntax.
   *
   * @param value the string to check
   * @return true if it's an arbitrary value
   */
  private static boolean isArbitraryValue(String value) {
    if (value == null || value.length() < 2) {
      return false;
    }
    return (value.startsWith("[") && value.endsWith("]"))
        || (value.startsWith("(") && value.endsWith(")"));
  }

  /**
   * Builds a CSS linear-gradient string from components.
   *
   * @param direction gradient direction (e.g., "to right")
   * @param from starting color
   * @param via middle color (optional)
   * @param to ending color
   * @return CSS string for -fx-background-color, or null if invalid
   */
  private static String buildGradient(String direction, String from, String via, String to) {
    // Validate we have at least one color
    if (from == null && to == null) {
      return null;
    }

    String dir = direction != null ? direction : "to bottom";
    String fromColor = from != null ? from : "transparent";
    String toColor = to != null ? to : fromColor;

    StringBuilder sb = new StringBuilder("linear-gradient(");
    sb.append(dir);

    if (via != null) {
      sb.append(", ").append(fromColor).append(", ").append(via).append(", ").append(toColor);
    } else {
      sb.append(", ").append(fromColor).append(", ").append(toColor);
    }

    sb.append(")");
    return "-fx-background-color: " + sb.toString() + ";";
  }

  /**
   * Checks if a token is gradient-related.
   *
   * @param token the token to check
   * @return true if it's a gradient token
   */
  public static boolean isGradientToken(String token) {
    if (token == null || token.isBlank()) {
      return false;
    }
    return token.startsWith(GRADIENT_PREFIX)
        || token.startsWith(FROM_PREFIX)
        || token.startsWith(VIA_PREFIX)
        || token.startsWith(TO_PREFIX);
  }

  // Private constructor to prevent instantiation
  private GradientProcessor() {
    throw new UnsupportedOperationException(
        "GradientProcessor is a utility class and cannot be instantiated");
  }
}
