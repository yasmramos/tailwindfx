package io.github.yasmramos.tailwindfx.core;

import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import io.github.yasmramos.tailwindfx.style.StyleToken;
import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * JitCompiler — Orchestrator that converts Tailwind tokens into -fx-* inline properties.
 *
 * <p>Delegates resolution and mapping to StyleResolver and CssPropertyMapper. Only handles cache,
 * logging, and metrics.
 *
 * <p>Input: "p-4" → "-fx-padding: 16px;" Input: "bg-blue-500/80" → "-fx-background-color:
 * rgba(59,130,246,0.80);" Input: "w-[320px]" → "-fx-pref-width: 320px;" Input: "-translate-x-4" →
 * "-fx-translate-x: -16px;"
 *
 * <p>Cache: compiled tokens are stored in a thread-safe ManualLruCache with automatic eviction.
 * Compiling "p-4" 1000 times costs the same as compiling it once.
 *
 * <p>Unknown tokens — smart heuristic: If the token looks like a JIT utility (has numbers, /, [) →
 * WARN in console If it looks like an intentional CSS class (btn-primary, card) → silent, added as
 * class Debug mode: JitCompiler.setDebug(true) → log ALL tokens
 *
 * <p>Special modifiers:
 * <ul>
 *   <li><code>!</code> suffix: Marks property as important (e.g., "p-4!" → padding with !important flag)
 *   <li><code>md:</code>, <code>lg:</code> prefixes: Responsive breakpoints (requires manual handling)
 *   <li><code>dark:</code> prefix: Dark mode variant (requires dark mode enabled)
 * </ul>
 */
public final class JitCompiler {

  private static final Logger LOG = Logger.getLogger("TailwindFX.JIT");

  // Gradient token constants
  private static final String GRADIENT_PREFIX = "bg-gradient-to-";
  private static final String FROM_PREFIX = "from-";
  private static final String VIA_PREFIX = "via-";
  private static final String TO_PREFIX = "to-";

  // Special modifier suffixes and prefixes
  /** Suffix for !important modifier (e.g., "p-4!" → important padding). */
  private static final String IMPORTANT_SUFFIX = "!";

  /** Prefix for dark mode variant (e.g., "dark:bg-gray-800"). */
  private static final String DARK_PREFIX = "dark:";

  private final StyleResolver resolver;
  private final CssPropertyMapper propertyMapper;

  // Global cache: raw token → compiled result
  // Thread-safe LRU cache with bounded size — prevents unbounded growth in long-running apps
  /** Maximum number of compiled tokens to keep in the cache. */
  static final int MAX_CACHE_SIZE = 2_000;

  /**
   * Thread-safe LRU cache implementation with automatic eviction.
   *
   * <p>Why 2000? A typical large app uses ~300-500 unique utility tokens. 2000 gives 4× headroom
   * for JIT-compiled arbitrary values while keeping the cache under ~400KB in the worst case.
   */
  private static final ManualLruCache<String, CompileResult> CACHE =
      new ManualLruCache<>(MAX_CACHE_SIZE);

  // Modo debug: loguea todos los tokens procesados
  private static volatile boolean DEBUG = false;

  // Singleton instance for static compile() method
  private static final JitCompiler INSTANCE = new JitCompiler();

  public JitCompiler() {
    this(ThemeConfig.defaultConfig());
  }

  public JitCompiler(ThemeConfig themeConfig) {
    this.resolver = new StyleResolver(themeConfig);
    this.propertyMapper = new CssPropertyMapper(themeConfig);
  }

  /**
   * Detects if a token requires JIT compilation (arbitrary values only). Matches Tailwind CSS v4
   * candidate parsing logic.
   *
   * <p>JIT triggers: - Arbitrary values: w-[320px], bg-[#fff], text-[length:var(--x)] - Arbitrary
   * modifiers: bg-red-500/[0.3], hover:bg-[#fff]/(0.5) - Arbitrary properties: [color:red],
   * [mask-type:luminance]
   *
   * <p>NOT JIT (predefined utilities): - w-32, bg-red-500, -mt-4, col-1, z-10
   *
   * <p>IMPORTANT: This method validates that '/' is used for opacity/modifiers on known color
   * utilities, not arbitrary class names like 'icon/large'.
   */
  private static boolean requiresJitCompilation(String token) {
    if (token == null || token.isEmpty()) return false;

    // Fast path: arbitrary property [...]
    if (token.startsWith("[") && token.endsWith("]")) {
      // Must contain : for property:value syntax
      return token.indexOf(':', 1) > 1; // [color:red] ✓, [] ✗
    }

    int lastSlashIndex = token.lastIndexOf('/');

    if (lastSlashIndex == -1) {
      // No slash, check for arbitrary values in base
      return containsArbitraryValue(token);
    }

    String base = token.substring(0, lastSlashIndex);
    String modifier = token.substring(lastSlashIndex + 1);

    // If modifier is numeric (opacity), validate base normally
    if (isNumeric(modifier)) {
      return containsArbitraryValue(base);
    }

    // If modifier is arbitrary [...] or (...) → JIT
    if (isArbitraryValue(modifier)) {
      return true;
    }

    // If base contains arbitrary values → JIT
    if (containsArbitraryValue(base)) {
      return true;
    }

    // CRITICAL: Only treat '/' as JIT trigger if base is a known color utility.
    // This prevents false positives like 'icon/large' being treated as JIT.
    // Valid: bg-red-500/80, text-gray-900/50
    // Invalid: icon/large, btn-primary/custom
    if (!isValidColorUtilityBase(base)) {
      // Not a valid color utility with opacity modifier, so not JIT
      return false;
    }

    return true;
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
   * Validates if a base token (before /) is a valid color utility that can have opacity. Examples:
   * bg-red-500, text-gray-900, border-blue-300
   *
   * @param base the token before the '/' modifier
   * @return true if this is a valid color utility base
   */
  private static boolean isValidColorUtilityBase(String base) {
    if (base == null || base.isEmpty()) return false;

    // Known color utility prefixes
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
        // Named colors without shade (e.g., bg-transparent)
        if (!rest.contains("-")) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks if a string contains an arbitrary value in [...] syntax. Handles nested parens/brackets
   * like calc(100px-4rem) or var(--x).
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
      // Skip escaped chars and strings for robustness (simplified)
    }
    return false;
  }

  /** Checks if a modifier/value is arbitrary: [...] or (...) for CSS vars. */
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

  public static void setDebug(boolean enabled) {
    DEBUG = enabled;
  }

  public static boolean isDebug() {
    return DEBUG;
  }

  // Compilation result
  public record CompileResult(
      String inlineStyle, // -fx-* properties ready for setStyle()
      String cssClass, // CSS class to add via getStyleClass() (may be null)
      boolean isKnown, // false if it was an unknown token
      boolean isDarkMode // true if this is a dark mode variant (e.g., "dark:bg-gray-800")
      ) {

    /** Creates a CompileResult without dark mode flag (defaults to false). */
    public CompileResult(String inlineStyle, String cssClass, boolean isKnown) {
      this(inlineStyle, cssClass, isKnown, false);
    }

    public static CompileResult inline(String style) {
      return new CompileResult(style, null, true);
    }

    public static CompileResult cssClass(String cls) {
      return new CompileResult(null, cls, true);
    }

    public static CompileResult unknown(String token) {
      return new CompileResult(null, token, false);
    }

    public boolean hasInlineStyle() {
      return inlineStyle != null && !inlineStyle.isBlank();
    }

    public boolean hasCssClass() {
      return cssClass != null && !cssClass.isBlank();
    }
  }

  // Public API
  /**
   * Compiles a single token. Handles special modifiers like !important and dark mode.
   * Uses thread-safe LRU cache: compiling the same token N times costs the same as 1.
   *
   * <p>Special modifiers:
   * <ul>
   *   <li><code>!</code> suffix: Marks property as important (e.g., "p-4!" → adds !important flag)
   *   <li><code>dark:</code> prefix: Dark mode variant (requires manual handling in application)
   * </ul>
   *
   * @param token the token to compile
   * @return the compiled result with inline style and/or CSS class
   * @throws IllegalArgumentException if token is null
   */
  public static CompileResult compile(String token) {
    if (token == null) {
      throw new IllegalArgumentException("JitCompiler.compile: token cannot be null");
    }
    if (token.isBlank()) {
      return CompileResult.unknown(token);
    }

    // Extract modifiers before caching
    boolean isImportant = false;
    boolean isDarkMode = false;
    String baseToken = token.trim();

    // Check for !important suffix
    if (baseToken.endsWith(IMPORTANT_SUFFIX)) {
      isImportant = true;
      baseToken = baseToken.substring(0, baseToken.length() - 1);
    }

    // Check for dark: prefix
    if (baseToken.startsWith(DARK_PREFIX)) {
      isDarkMode = true;
      baseToken = baseToken.substring(DARK_PREFIX.length());
    }

    // Create cache key including modifiers
    String cacheKey = token.trim();

    // Thread-safe cache read
    CompileResult result = CACHE.get(cacheKey);
    if (result != null) {
      TailwindFXMetrics.instance().recordCacheHit();
      return result;
    }

    // Cache miss - compile the token
    long t0 = System.nanoTime();
    result = INSTANCE.doCompile(baseToken);
    TailwindFXMetrics.instance().recordCompilation(System.nanoTime() - t0);

    // Apply modifiers if needed
    if (isImportant && result.hasInlineStyle()) {
      // JavaFX doesn't support !important in inline styles
      // We mark it by adding a comment or keeping as-is for future stylesheet processing
      // For now, log a warning that !important is not supported in JavaFX inline styles
      LOG.warning(
          "TailwindFX: !important modifier is not supported in JavaFX inline styles. "
              + "Token '"
              + token
              + "' will be compiled without !important.");
      // Note: We keep the token as-is since JavaFX doesn't have !important support
      // This could be used later if exporting to external CSS
    }

    if (isDarkMode) {
      // Mark as dark mode variant - this requires manual handling
      // The result should be applied only when dark mode is active
      // For now, we just compile normally but could add metadata
      result = new CompileResult(
          result.inlineStyle(), result.cssClass(), result.isKnown(), true /* isDarkMode */);
    }

    // Thread-safe put with atomic operation
    CompileResult existing = CACHE.putIfAbsent(cacheKey, result);
    if (existing != null) {
      // Another thread compiled it first, use their result
      TailwindFXMetrics.instance().recordCacheHit();
      return existing;
    }

    TailwindFXMetrics.instance().recordCacheMiss();
    return result;
  }

  /**
   * Compiles multiple tokens and returns the combined inline style and the list de CSS classes a
   * agregar.
   */
  public static BatchResult compileBatch(String... tokens) {
    StringBuilder inlineStyle = new StringBuilder();
    List<String> cssClasses = new ArrayList<>();

    // Delegate gradient processing to GradientProcessor
    GradientProcessor.GradientResult gradientResult =
        GradientProcessor.processGradientTokens(tokens);

    if (gradientResult.isGradient() && gradientResult.inlineStyle() != null) {
      inlineStyle.append(gradientResult.inlineStyle()).append(" ");
    }

    // Process non-gradient tokens normally
    for (String token : tokens) {
      if (token == null || token.isBlank()) {
        continue;
      }
      for (String t : token.split("\\s+")) {
        if (t == null || t.isBlank()) {
          continue;
        }

        // Skip gradient tokens as they were already processed
        if (GradientProcessor.isGradientToken(t)) {
          continue;
        }

        CompileResult result = compile(t);
        if (result.hasInlineStyle()) {
          inlineStyle.append(result.inlineStyle()).append(" ");
        }
        if (result.hasCssClass()) {
          cssClasses.add(result.cssClass());
        }
        if (!result.isKnown()) {
          // Heuristic: if it looks like a JIT token (arbitrary values) → warn
          // If it looks like an intentional CSS class (btn-primary) → silent
          // Exceptions: unrecognized gradient tokens → silent
          boolean isGradientRelated = GradientProcessor.isGradientToken(t);
          if (requiresJitCompilation(t) && !isGradientRelated) {
            LOG.warning(
                "TailwindFX JIT: unknown token '"
                    + t
                    + "' (looks like a JIT utility but was not recognized)");
          } else if (DEBUG) {
            LOG.info("TailwindFX JIT: '" + t + "' → CSS class (fallback to stylesheet)");
          }
        } else if (DEBUG) {
          String what =
              result.hasInlineStyle()
                  ? "inline: " + result.inlineStyle().trim()
                  : "class: " + result.cssClass();
          LOG.info("TailwindFX JIT: '" + t + "' → " + what);
        }
      }
    }

    return new BatchResult(inlineStyle.toString().trim(), cssClasses);
  }

  public record BatchResult(String inlineStyle, List<String> cssClasses) {

    public boolean hasInlineStyle() {
      return !inlineStyle.isBlank();
    }
  }

  /**
   * Clears the JIT compilation cache. Call when the application's utility class set changes
   * significantly (e.g., after a major theme reconfiguration). The cache is automatically bounded
   * by LRU eviction, so explicit clearing is rarely needed.
   */
  public static void clearCache() {
    CACHE.clear();
  }

  /**
   * Returns the current number of entries in the cache.
   *
   * @return the current cache size
   */
  public static int cacheSize() {
    return CACHE.size();
  }

  /**
   * Returns statistics about the cache state.
   *
   * @return a CacheStats record with current metrics
   */
  public static ManualLruCache.CacheStats getCacheStats() {
    return CACHE.getStats();
  }

  // Main compilation - delega a StyleResolver y CssPropertyMapper
  private CompileResult doCompile(String raw) {
    StyleToken t = StyleToken.parse(raw);

    // Delegar resolución al StyleResolver
    String resolvedValue = resolver.resolve(t);
    if (resolvedValue == null) {
      return CompileResult.unknown(raw);
    }

    // Delegar mapeo de propiedades al CssPropertyMapper
    String style = propertyMapper.map(t, resolvedValue);

    if (style == null || style.isBlank()) {
      return CompileResult.cssClass(t.raw);
    }
    return CompileResult.inline(style);
  }

  // Legacy static methods REMOVED - all logic is now in StyleResolver and CssPropertyMapper
  // compileScale, compileColor, compileArbitrary, compileNamed have been removed

  // Deprecated gradient methods - use GradientProcessor instead
  /**
   * @deprecated Use {@link GradientProcessor#processGradientTokens(String[])} instead.
   */
  @Deprecated(since = "1.0.0", forRemoval = true)
  private static String resolveGradientColor(String colorToken) {
    // Delegate to GradientProcessor for consistency
    return null; // This method is no longer used internally
  }

  /**
   * @deprecated Use {@link GradientProcessor#processGradientTokens(String[])} instead.
   */
  @Deprecated(since = "1.0.0", forRemoval = true)
  private static String buildGradient(String direction, String from, String via, String to) {
    // Delegate to GradientProcessor for consistency
    return null; // This method is no longer used internally
  }
}
