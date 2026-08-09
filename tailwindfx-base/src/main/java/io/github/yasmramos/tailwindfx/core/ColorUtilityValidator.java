package io.github.yasmramos.tailwindfx.core;

/**
 * Utility class for validating Tailwind color utility tokens.
 * 
 * <p>This class provides shared validation logic for color utilities that support
 * opacity modifiers (e.g., bg-red-500/80).
 */
public final class ColorUtilityValidator {

  private ColorUtilityValidator() {
    // Utility class - prevent instantiation
  }

  /**
   * Validates if a base token (before /) is a valid color utility that can have opacity.
   * Supports the 8 color utility prefixes: bg, text, border, fill, stroke, shadow, ring, outline.
   * Validates that the shade is numeric (e.g., blue-500) or recognizes named colors without shade
   * (e.g., bg-transparent).
   *
   * @param base the token before the '/' modifier
   * @return true if this is a valid color utility base
   */
  public static boolean isValidColorUtilityBase(String base) {
    if (base == null || base.isEmpty()) return false;

    // Known color utility prefixes (8 supported prefixes)
    String[] colorPrefixes = {
      "bg", "text", "border", "fill", "stroke", "shadow", "ring", "outline"
    };

    for (String prefix : colorPrefixes) {
      if (base.startsWith(prefix + "-")) {
        // Extract the rest after prefix-
        String rest = base.substring(prefix.length() + 1);
        // Should have at least one more dash for color-shade (e.g., red-500)
        int lastDash = rest.lastIndexOf('-');
        if (lastDash > 0) {
          String shadeStr = rest.substring(lastDash + 1);
          // Validate that the last part is a number (shade)
          try {
            Integer.parseInt(shadeStr);
            return true;
          } catch (NumberFormatException e) {
            // Not a valid shade number
            return false;
          }
        }
        // Named colors without shade (e.g., bg-transparent, bg-white)
        if (!rest.contains("-")) {
          return true;
        }
      }
    }
    return false;
  }
}
