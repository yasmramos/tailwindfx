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
 *   <li>Animation tokens handled via TwAnimation
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
 * TokenRegistry.isAnimationToken("animate-spin"); // true
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
              "shrink",
              "flex"));

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

  /** Prefix for animation tokens handled via TwAnimation instead of CSS. */
  private static final String ANIMATION_PREFIX = "animate-";

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

  /** Animation tokens handled via TwAnimation instead of CSS. */
  private static final Set<String> ANIMATION_NAMES =
      new HashSet<>(
          Arrays.asList(
              "spin",
              "pulse",
              "bounce",
              "ping",
              "flash",
              "shake",
              "shake-x",
              "shake-y",
              "spin-slow",
              "pulse-slow",
              "bounce-slow"));

  /** Component class prefixes for static utility validation. */
  private static final Set<String> COMPONENT_PREFIXES =
      new HashSet<>(
          Arrays.asList(
              "btn", "input", "card", "badge", "avatar", "alert", "spinner", "tooltip", "modal",
              "group",
              // Component classes declared in tailwindfx-components.css. They are applied by the
              // components module (e.g. TwBadge applies dot / dot-sm / dot-<color> to the status
              // dot) and must not be reported as unknown tokens.
              "checkbox",
              "collapse",
              "data",
              "dot",
              "progress",
              "select",
              "table",
              "titled",
              "tw",
              "virtual"));

  /** Theme variant tokens. */
  private static final Set<String> THEME_VARIANTS = new HashSet<>(Arrays.asList("dark", "light"));

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
    // Flexbox / grid families. These are layout-dependent tokens handled programmatically by
    // LayoutApplier, so they are valid utilities even though they produce no CSS declaration.
    Pattern.compile("^gap(-(x|y))?(-[a-zA-Z0-9-]+)?$"),
    Pattern.compile("^space(-(x|y))?(-[a-zA-Z0-9-]+)?$"),
    Pattern.compile("^(justify|items|content|self|place-items|place-content)(-[a-zA-Z0-9-]+)?$"),
    Pattern.compile("^(flex|grid)(-[a-zA-Z0-9-]+)?$"),
    Pattern.compile("^(order|col|row)(-[a-zA-Z0-9-]+)?$"),
    // grow/shrink take no arbitrary value in Tailwind: only "grow", "grow-0", "shrink" and
    // "shrink-0" exist. Matching any suffix here would silently accept typos like "grow-x",
    // defeating the typo detection these patterns exist for.
    Pattern.compile("^grow(-0)?$"),
    Pattern.compile("^shrink(-0)?$"),
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
   * <p>This method uses robust prefix matching by extracting the prefix before the first hyphen (or
   * the entire token if no hyphen) and comparing against known JIT prefixes.
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
   * <p>This method uses exact matching for tokens without values (grow, shrink) and prefix matching
   * for tokens with values (gap-4, flex-1, etc.).
   *
   * @param token the token to check (without variant prefix)
   * @return true if this token requires parent container context
   */
  public static boolean isLayoutDependent(String token) {
    if (token == null || token.isEmpty()) {
      return false;
    }

    // Strip Tailwind's negative marker so "-mt-2" / "-m-4" match like "mt-2" / "m-4".
    String bare = stripNegativeMarker(token);

    // Check for exact matches first (grow, shrink). Bare "flex" is also layout-dependent:
    // when it lands on a node that is already inside a flex-capable container (TwFlexPane,
    // HBox, VBox), LayoutApplier treats it as the Tailwind `flex: 1 1 0%` shorthand
    // (grow=1, shrink=1) instead of a display-migration request. Tokens that genuinely need
    // container migration are still routed through requiresMigration() first in TokenParser.
    if (bare.equals("grow") || bare.equals("shrink") || bare.equals("flex")) {
      return true;
    }

    // Check for prefix matches (gap-, flex-, justify-, etc.)
    return LAYOUT_DEPENDENT_PREFIXES.stream().anyMatch(bare::startsWith);
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
   * Checks if a token is an animation token that should be played via TwAnimation.
   *
   * <p>JavaFX has no CSS animation engine, so {@code animate-*} utilities cannot be expressed as
   * inline styles. They are recognized here and routed to {@code AnimationApplier}, which plays the
   * equivalent {@code TwAnimation} timeline on the node.
   *
   * @param token the base token (without variant prefix)
   * @return true if this token should be applied via TwAnimation
   */
  public static boolean isAnimationToken(String token) {
    if (token == null || token.isEmpty()) {
      return false;
    }
    String baseToken = stripVariantPrefix(token);
    if (!baseToken.startsWith(ANIMATION_PREFIX)) {
      return false;
    }
    return ANIMATION_NAMES.contains(baseToken.substring(ANIMATION_PREFIX.length()));
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
    return baseToken.equals("flex") || baseToken.equals("inline-flex") || baseToken.equals("grid");
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
    if (THEME_VARIANTS.contains(token) || token.startsWith("dark:") || token.startsWith("light:")) {
      return true;
    }

    // Tailwind utility classes with regex validation to catch typos. The negative marker is
    // stripped first so that "-mt-2" / "-rotate-45" match the same patterns as their positive
    // counterparts instead of being reported as unknown tokens.
    String bare = stripNegativeMarker(token);
    for (Pattern pattern : UTILITY_PATTERNS) {
      if (pattern.matcher(bare).matches()) {
        return true;
      }
    }

    return false;
  }

  /**
   * Checks if a token requires JIT compilation (arbitrary values, arbitrary properties, or opacity
   * modifiers on colors).
   *
   * <p>This is the single source of truth for JIT compilation decisions, matching Tailwind CSS v4
   * behavior:
   *
   * <ul>
   *   <li>Arbitrary values: w-[320px], bg-[#fff], text-[length:var(--x)] → JIT
   *   <li>Arbitrary properties: [color:red], [mask-type:luminance] → JIT
   *   <li>Arbitrary modifiers: bg-red-500/[0.3], hover:bg-[#fff]/(0.5) → JIT
   *   <li>Opacity on color utilities: bg-blue-500/50, text-red-500/80 → JIT
   *   <li>Predefined utilities: w-32, bg-red-500, p-4, -mt-4 → NOT JIT (CSS class)
   * </ul>
   *
   * @param token the token to check (may include variant prefixes)
   * @return true if this token requires JIT compilation
   */
  public static boolean requiresJitCompilation(String token) {
    if (token == null || token.isEmpty()) return false;

    // Strip variant prefixes for validation
    String baseToken = stripVariantPrefix(token);

    // Fast path: arbitrary property [...:...]
    if (baseToken.startsWith("[") && baseToken.endsWith("]")) {
      // Must contain : for property:value syntax
      return baseToken.indexOf(':', 1) > 1; // [color:red] ✓, [] ✗
    }

    int lastSlashIndex = baseToken.lastIndexOf('/');

    if (lastSlashIndex == -1) {
      // No slash, check for arbitrary values in base
      return containsArbitraryValue(baseToken);
    }

    String base = baseToken.substring(0, lastSlashIndex);
    String modifier = baseToken.substring(lastSlashIndex + 1);

    // If modifier is numeric (opacity), validate base is a color utility
    if (isNumeric(modifier)) {
      // Opacity on color utility → JIT
      return ColorUtilityValidator.isValidColorUtilityBase(base);
    }

    // If modifier is arbitrary [...] or (...) → JIT
    if (isArbitraryValue(modifier)) {
      return true;
    }

    // If base contains arbitrary values → JIT
    if (containsArbitraryValue(base)) {
      return true;
    }

    // Not a valid JIT token (e.g., icon/large is not a color utility)
    return false;
  }

  /**
   * Checks if a string contains an arbitrary value in [...] syntax. Handles nested parens/brackets
   * like calc(100px-4rem) or var(--x).
   *
   * @param input the string to check
   * @return true if it contains arbitrary value syntax
   */
  private static boolean containsArbitraryValue(String input) {
    int bracketDepth = 0;
    int start = -1;

    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);

      if (c == '[' && bracketDepth == 0) {
        start = i;
        bracketDepth++;
      } else if (c == '[') {
        bracketDepth++;
      } else if (c == ']') {
        bracketDepth--;
        if (bracketDepth == 0 && start >= 0) {
          // Found complete [...] - validate it's not empty
          String arbitrary = input.substring(start + 1, i);
          return !arbitrary.isEmpty() && !arbitrary.trim().isEmpty();
        }
      }
    }
    return false;
  }

  /**
   * Checks if a modifier/value is arbitrary: [...] or (...) for CSS vars.
   *
   * @param value the value to check
   * @return true if it's an arbitrary value
   */
  private static boolean isArbitraryValue(String value) {
    if (value == null || value.length() < 2) return false;

    // Arbitrary: [value] or (var(--x))
    if ((value.startsWith("[") && value.endsWith("]"))
        || (value.startsWith("(") && value.endsWith(")"))) {
      String content = value.substring(1, value.length() - 1);
      return !content.isEmpty() && !content.trim().isEmpty();
    }
    return false;
  }

  /**
   * Checks if a string is numeric (integer or decimal).
   *
   * @param str the string to check
   * @return true if it's a valid number
   */
  private static boolean isNumeric(String str) {
    if (str == null || str.isEmpty()) return false;
    return str.matches("\\d+(\\.\\d+)?");
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
   * Gets all supported animation names (without the {@code animate-} prefix).
   *
   * @return unmodifiable set of animation names
   */
  public static Set<String> getAnimationNames() {
    return new HashSet<>(ANIMATION_NAMES);
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
    // Don't strip from tokens containing arbitrary values [...] - colons inside [] are not variants
    if (token.contains("[")) {
      return token;
    }
    // Don't strip from arbitrary properties [...] - they are not variants
    if (token.startsWith("[")) {
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
    // Skip Tailwind's negative marker so "-mt-2" extracts "mt" (same as "mt-2"). Without this,
    // indexOf('-') returns 0 for the leading marker and the whole token is treated as the prefix.
    int start = (token.length() > 1 && token.charAt(0) == '-') ? 1 : 0;
    int firstHyphen = token.indexOf('-', start);
    if (firstHyphen > start) {
      return token.substring(start, firstHyphen);
    }
    return token.substring(start);
  }

  /**
   * Removes Tailwind's leading negative marker from a token.
   *
   * <p>Examples: {@code "-mt-2"} → {@code "mt-2"}, {@code "-m-4"} → {@code "m-4"}. Tokens without
   * a marker are returned unchanged.
   *
   * @param token the token to normalize
   * @return the token without its leading minus sign
   */
  private static String stripNegativeMarker(String token) {
    return (token.length() > 1 && token.charAt(0) == '-') ? token.substring(1) : token;
  }
}
