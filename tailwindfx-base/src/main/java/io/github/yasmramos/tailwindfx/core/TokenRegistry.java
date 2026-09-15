package io.github.yasmramos.tailwindfx.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * TokenRegistry - Centralized source of truth for TailwindFX token classification.
 *
 * <p>This class eliminates duplicate token lists by providing a single authoritative registry for:
 *
 * <ul>
 *   <li>JIT (Just-In-Time) prefixes for dynamic style compilation
 *   <li>Layout-dependent prefixes requiring parent container context
 *   <li>Effect/filter tokens handled via TwEffect
 *   <li>Static utility classes known to the system
 *   <li>Tokens requiring layout migration (flex, grid containers)
 * </ul>
 *
 * <p>Usage:
 *
 * <pre>
 * TokenRegistry.isJitPrefix("bg-blue-500");      // true
 * TokenRegistry.isLayoutDependent("gap-4");       // true
 * TokenRegistry.isEffectToken("blur-sm");         // true
 * TokenRegistry.requiresMigration("flex");        // true
 * TokenRegistry.isKnownUtility("rounded-lg");     // true
 * </pre>
 *
 * @author yasmramos
 * @since 1.0
 */
public final class TokenRegistry {

  /** JIT prefixes for dynamic style compilation (colors, spacing, sizing, etc.). */
  private static final Set<String> JIT_PREFIXES =
      new HashSet<>(
          Arrays.asList(
              "bg",
              "text",
              "border",
              "ring",
              "shadow",
              "w",
              "h",
              "min-w",
              "min-h",
              "max-w",
              "max-h",
              "p",
              "px",
              "py",
              "pt",
              "pr",
              "pb",
              "pl",
              "m",
              "mx",
              "my",
              "mt",
              "mr",
              "mb",
              "ml",
              "space",
              "translate",
              "rotate",
              "scale",
              "skew",
              "opacity",
              "z",
              "order",
              "col",
              "row",
              "gap",
              "inset",
              "top",
              "right",
              "bottom",
              "left",
              "blur",
              "brightness",
              "contrast",
              "grayscale",
              "hue-rotate",
              "invert",
              "saturate",
              "sepia",
              "drop-shadow",
              "backdrop",
              "grow",
              "shrink"));

  /** Layout-dependent prefixes requiring parent container context for application. */
  private static final Set<String> LAYOUT_DEPENDENT_PREFIXES =
      new HashSet<>(
          Arrays.asList(
              "m-",
              "mx-",
              "my-",
              "mt-",
              "mr-",
              "mb-",
              "ml-",
              "gap-",
              "gap-x-",
              "gap-y-",
              "flex-",
              "grow",
              "shrink",
              "justify-",
              "items-",
              "content-",
              "grid-cols-",
              "grid-rows-",
              "grid-flow-",
              "col-span-",
              "row-span-"));

  /** Effect/filter prefixes handled via TwEffect instead of CSS. */
  private static final Set<String> EFFECT_PREFIXES =
      new HashSet<>(
          Arrays.asList(
              "blur",
              "brightness",
              "contrast",
              "grayscale",
              "invert",
              "sepia",
              "hue-rotate",
              "saturate",
              "drop-shadow",
              "backdrop-blur",
              "opacity"));

  /** Component class prefixes for static utility validation. */
  private static final Set<String> COMPONENT_PREFIXES =
      new HashSet<>(
          Arrays.asList(
              "btn",
              "input",
              "card",
              "badge",
              "avatar",
              "alert",
              "spinner",
              "tooltip",
              "modal",
              "group"));

  /** Theme variant tokens. */
  private static final Set<String> THEME_VARIANTS =
      new HashSet<>(Arrays.asList("dark", "light"));

  /** Valid utility patterns for typo detection. */
  private static final Pattern[] UTILITY_PATTERNS = {
    Pattern.compile("^rounded(-[a-zA-Z0-9]+)?$"),
    Pattern.compile("^font(-[a-zA-Z0-9]+)?$"),
    Pattern.compile("^shadow(-[a-zA-Z0-9]+)?$"),
    Pattern.compile("^(flex|hidden|italic)$"),
    Pattern.compile("^text(-[a-zA-Z0-9-/]+)?$"),
    Pattern.compile("^bg(-[a-zA-Z0-9-/\\[\\]#]+)?$"),
    Pattern.compile("^border(-[a-zA-Z0-9-/\\[\\]#]+)?$"),
    Pattern.compile("^[pm](t|r|b|l|x|y)?(-[a-zA-Z0-9\\[\\]]+)?$"),
    Pattern.compile("^(w|h|min|max)(-[a-zA-Z0-9]+)?$"),
    Pattern.compile("^opacity(-[0-9]+)?$"),
    Pattern.compile("^rotate(-[0-9]+)?$"),
    Pattern.compile("^scale(-[0-9]+)?$"),
    Pattern.compile("^translate(x|y)?(-[a-zA-Z0-9]+)?$"),
    Pattern.compile("^skew(x|y)?(-[0-9]+)?$"),
    Pattern.compile("^cursor(-[a-zA-Z]+)?$"),
    Pattern.compile("^overflow(-[a-zA-Z]+)?$"),
    Pattern.compile("^resize$"),
    Pattern.compile("^visible$"),
    Pattern.compile("^z(-[a-zA-Z0-9]+)?$"),
    Pattern.compile("^blur(-[a-zA-Z0-9]+)?$"),
    Pattern.compile("^brightness(-[0-9]+)?$"),
    Pattern.compile("^contrast(-[0-9]+)?$"),
    Pattern.compile("^grayscale(-[0-9]+)?$"),
    Pattern.compile("^invert(-[0-9]+)?$"),
    Pattern.compile("^sepia(-[0-9]+)?$")
  };

  private TokenRegistry() {
    // Prevent instantiation
  }

  /**
   * Checks if a token starts with a JIT prefix.
   *
   * <p>This method uses robust prefix matching by extracting the prefix before the first hyphen
   * (or the entire token if no hyphen) and comparing against known JIT prefixes.
   *
   * @param token the token to check (may include variant prefixes like "hover:")
   * @return true if the token's base starts with a known JIT prefix
   */
  public static boolean isJitPrefix(String token) {
    if (token == null || token.isEmpty()) {
      return false;
    }

    // Strip variant prefixes (hover:, focus:, md:, etc.)
    String baseToken = stripVariantPrefix(token);

    // Handle opacity modifier: bg-blue-500/80
    if (baseToken.contains("/")) {
      baseToken = baseToken.substring(0, baseToken.indexOf('/'));
    }

    // Extract prefix: everything before first hyphen, or entire token
    String prefix = extractPrefix(baseToken);

    return JIT_PREFIXES.contains(prefix);
  }

  /**
   * Checks if a token requires layout context (parent container) to be applied.
   *
   * <p>This method uses exact matching for tokens without values (grow, shrink) and prefix
   * matching for tokens with values (gap-4, flex-1, etc.).
   *
   * @param token the token to check (without variant prefix)
   * @return true if this token requires parent container context
   */
  public static boolean isLayoutDependent(String token) {
    if (token == null || token.isEmpty()) {
      return false;
    }

    // Check for exact matches first (grow, shrink)
    if (token.equals("grow") || token.equals("shrink")) {
      return true;
    }

    // Check for prefix matches (gap-, flex-, justify-, etc.)
    return LAYOUT_DEPENDENT_PREFIXES.stream().anyMatch(token::startsWith);
  }

  /**
   * Checks if a token is a filter/effect token that should be handled via TwEffect.
   *
   * <p>This method distinguishes between effect tokens with values (blur-4, brightness-125) and
   * bare effect tokens (grayscale, invert) using robust matching.
   *
   * @param token the base token (without variant prefix)
   * @return true if this token should be applied via TwEffect
   */
  public static boolean isEffectToken(String token) {
    if (token == null || token.isEmpty()) {
      return false;
    }

    // Extract prefix for matching
    String prefix = extractPrefix(token);

    // Check if prefix matches any effect prefix
    return EFFECT_PREFIXES.stream()
        .anyMatch(
            eff -> {
              // Exact match for bare tokens (grayscale, invert)
              if (token.equals(eff)) {
                return true;
              }
              // Prefix match for tokens with values (blur-4, brightness-125)
              if (token.startsWith(eff + "-")) {
                return true;
              }
              return false;
            });
  }

  /**
   * Checks if a token requires container migration (flex, grid display properties).
   *
   * <p>Migration is needed when the token changes the node's display type to a layout container.
   *
   * @param token the token to check (may include variant prefixes)
   * @return true if applying this token requires migrating to TwFlexPane or TwGridPane
   */
  public static boolean requiresMigration(String token) {
    if (token == null || token.isEmpty()) {
      return false;
    }

    // Remove variants like hover:, md:, etc.
    String baseToken = stripVariantPrefix(token);

    // Migration is needed for display classes that convert the node into a container
    return baseToken.equals("flex")
        || baseToken.equals("inline-flex")
        || baseToken.equals("grid");
  }

  /**
   * Checks if a token is a known utility class that can be applied via CSS.
   *
   * <p>This method delegates from Styles.isKnownUtilityClass to centralize token knowledge while
   * maintaining backward compatibility.
   *
   * @param token the token to validate
   * @return true if this is a recognized utility class
   */
  public static boolean isKnownUtility(String token) {
    if (token == null || token.isEmpty()) {
      return false;
    }

    // Component classes - exact prefix match
    for (String prefix : COMPONENT_PREFIXES) {
      if (token.equals(prefix) || token.startsWith(prefix + "-")) {
        return true;
      }
    }

    // Theme variants
    if (THEME_VARIANTS.contains(token)
        || token.startsWith("dark:")
        || token.startsWith("light:")) {
      return true;
    }

    // Tailwind utility classes with regex validation to catch typos
    for (Pattern pattern : UTILITY_PATTERNS) {
      if (pattern.matcher(token).matches()) {
        return true;
      }
    }

    return false;
  }

  /**
   * Gets all JIT prefixes for iteration or debugging.
   *
   * @return unmodifiable set of JIT prefixes
   */
  public static Set<String> getJitPrefixes() {
    return new HashSet<>(JIT_PREFIXES);
  }

  /**
   * Gets all layout-dependent prefixes for iteration or debugging.
   *
   * @return unmodifiable set of layout-dependent prefixes
   */
  public static Set<String> getLayoutDependentPrefixes() {
    return new HashSet<>(LAYOUT_DEPENDENT_PREFIXES);
  }

  /**
   * Gets all effect prefixes for iteration or debugging.
   *
   * @return unmodifiable set of effect prefixes
   */
  public static Set<String> getEffectPrefixes() {
    return new HashSet<>(EFFECT_PREFIXES);
  }

  /**
   * Strips variant prefixes from a token.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>"hover:bg-blue-500" → "bg-blue-500"
   *   <li>"dark:hover:text-white" → "text-white"
   *   <li>"md:w-full" → "w-full"
   * </ul>
   *
   * @param token the token possibly containing variant prefixes
   * @return the base utility token without variant prefixes
   */
  private static String stripVariantPrefix(String token) {
    if (token == null || !token.contains(":")) {
      return token;
    }
    // Find the last colon to handle chained variants
    int lastColon = token.lastIndexOf(':');
    if (lastColon >= 0 && lastColon < token.length() - 1) {
      return token.substring(lastColon + 1);
    }
    return token;
  }

  /**
   * Extracts the prefix from a token (everything before the first hyphen).
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>"bg-blue-500" → "bg"
   *   <li>"flex-1" → "flex"
   *   <li>"grow" → "grow"
   *   <li>"grid-cols-3" → "grid"
   * </ul>
   *
   * @param token the token to extract prefix from
   * @return the prefix before the first hyphen, or the entire token if no hyphen
   */
  private static String extractPrefix(String token) {
    if (token == null || token.isEmpty()) {
      return token;
    }
    int firstHyphen = token.indexOf('-');
    if (firstHyphen > 0) {
      return token.substring(0, firstHyphen);
    }
    return token;
  }
}
