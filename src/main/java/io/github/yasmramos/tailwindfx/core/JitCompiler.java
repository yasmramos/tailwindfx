package io.github.yasmramos.tailwindfx.core;

import io.github.yasmramos.tailwindfx.cache.LruCache;
import io.github.yasmramos.tailwindfx.color.ColorPalette;
import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import io.github.yasmramos.tailwindfx.style.StyleToken;
import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * JitCompiler — Orchestrator that converts Tailwind tokens into inline -fx-* properties.
 *
 * <p>Delegates resolution and mapping to StyleResolver and CssPropertyMapper. Only handles cache,
 * logging and metrics.
 *
 * <p>Input: "p-4" → "-fx-padding: 16px;" Input: "bg-blue-500/80" → "-fx-background-color:
 * rgba(59,130,246,0.80);" Input: "w-[320px]" → "-fx-pref-width: 320px;" Input: "-translate-x-4" →
 * "-fx-translate-x: -16px;"
 *
 * <p>Cache: compiled tokens are stored in a thread-safe LRU cache with bounded size. Compiling
 * "p-4" 1000 times costs the same as compiling it once.
 *
 * <p>Unknown tokens — smart heuristic: If the token looks like a JIT utility (has numbers, /, [) →
 * WARN in console If it looks like an intentional CSS class (btn-primary, card) → silent, added as
 * class Debug mode: JitCompiler.setDebug(true) → log ALL tokens
 */
public final class JitCompiler {

  private static final Logger LOG = Logger.getLogger("TailwindFX.JIT");

  private final StyleResolver resolver;
  private final CssPropertyMapper propertyMapper;

  // Thread-safe LRU cache with bounded size — prevents unbounded growth in long-running apps
  /** Maximum number of compiled tokens to keep in the cache. */
  static final int MAX_CACHE_SIZE = 2_000;

  /**
   * Thread-safe LRU cache using ReentrantReadWriteLock for high-concurrency scenarios. Provides
   * O(1) get/put operations with automatic LRU eviction.
   *
   * <p>Why 2000? A typical large app uses ~300-500 unique utility tokens. 2000 gives 4× headroom
   * for JIT-compiled arbitrary values while keeping the cache under ~400KB in the worst case.
   */
  private static final LruCache<String, CompileResult> CACHE = new LruCache<>(MAX_CACHE_SIZE);

  // Modo debug: loguea todos los tokens procesados
  private static volatile boolean DEBUG = false;

  // Singleton instance for static compile() method
  private static final JitCompiler INSTANCE = new JitCompiler();

  /**
   * Returns the singleton JitCompiler instance.
   *
   * @return the singleton instance
   */
  public static JitCompiler getInstance() {
    return INSTANCE;
  }

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

    // Split modifier (after /) - Tailwind uses segment() with top-level parsing
    int slashIdx = token.lastIndexOf('/');
    String base = (slashIdx > 0) ? token.substring(0, slashIdx) : token;
    String modifier = (slashIdx > 0) ? token.substring(slashIdx + 1) : null;

    // Check if modifier is arbitrary (triggers JIT for opacity, etc.)
    if (modifier != null && isArbitraryValue(modifier)) {
      return true;
    }

    // Check if base contains arbitrary value [...]
    if (containsArbitraryValue(base)) {
      return true;
    }

    // CRITICAL: Only treat '/' as JIT trigger if base is a known color utility.
    // This prevents false positives like 'icon/large' being treated as JIT.
    // Valid: bg-red-500/80, text-gray-900/50
    // Invalid: icon/large, btn-primary/custom
    if (modifier != null && !isArbitraryValue(modifier)) {
      // Check if base matches color utility pattern (e.g., bg-red-500, text-blue-600)
      // Color utilities have format: prefix-colorname-shade
      if (!isValidColorUtilityBase(base)) {
        // Not a valid color utility with opacity modifier, so not JIT
        return false;
      }
    }

    return modifier != null;
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
      boolean isKnown // false if it was an unknown token
      ) {

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
   * Compiles a single token. Uses thread-safe LRU cache with automatic eviction. The cache provides
   * O(1) get/put operations with ReadWriteLock for high concurrency.
   */
  public static CompileResult compile(String token) {
    if (token == null) {
      throw new IllegalArgumentException("JitCompiler.compile: token cannot be null");
    }
    if (token.isBlank()) {
      return CompileResult.unknown(token);
    }
    String key = token.trim();

    // Use computeIfAbsent for atomic check-and-compute operation
    long t0 = System.nanoTime();
    CompileResult result =
        CACHE.computeIfAbsent(
            key,
            k -> {
              TailwindFXMetrics.instance().recordCacheMiss();
              return INSTANCE.doCompile(k);
            });

    // Record compilation time only on cache miss (when computation actually happened)
    // Note: computeIfAbsent doesn't distinguish hit/miss, so we track via metrics in the lambda
    TailwindFXMetrics.instance().recordCompilation(System.nanoTime() - t0);

    return result;
  }

  /**
   * Compiles multiple tokens and returns the combined inline style and the list de CSS classes a
   * agregar.
   */
  public static BatchResult compileBatch(String... tokens) {
    StringBuilder inlineStyle = new StringBuilder();
    List<String> cssClasses = new ArrayList<>();

    // Gradient state tracking
    String gradientDirection = null;
    String fromColor = null;
    String viaColor = null;
    String toColor = null;
    boolean hasGradient = false;

    // First pass: collect gradient-related tokens
    List<String> nonGradientTokens = new ArrayList<>();
    for (String token : tokens) {
      if (token == null || token.isBlank()) {
        continue;
      }
      for (String t : token.split("\\s+")) {
        if (t == null || t.isBlank()) {
          continue;
        }

        // Check for gradient direction
        if (t.startsWith("bg-gradient-to-")) {
          hasGradient = true;
          String dir = t.substring("bg-gradient-to-".length());
          gradientDirection =
              switch (dir) {
                case "t" -> "to top";
                case "tr" -> "to top right";
                case "r" -> "to right";
                case "br" -> "to bottom right";
                case "b" -> "to bottom";
                case "bl" -> "to bottom left";
                case "l" -> "to left";
                case "tl" -> "to top left";
                default -> "to bottom";
              };
        }
        // Check for from-* color (e.g., "from-blue-500", "from-gray-800")
        else if (t.startsWith("from-")) {
          String colorResolved = resolveGradientColor(t.substring(5));
          if (colorResolved != null) {
            hasGradient = true;
            fromColor = colorResolved;
          } else {
            nonGradientTokens.add(t);
          }
        }
        // Check for via-* color
        else if (t.startsWith("via-")) {
          String colorResolved = resolveGradientColor(t.substring(4));
          if (colorResolved != null) {
            hasGradient = true;
            viaColor = colorResolved;
          } else {
            nonGradientTokens.add(t);
          }
        }
        // Check for to-* color
        else if (t.startsWith("to-")) {
          String colorResolved = resolveGradientColor(t.substring(3));
          if (colorResolved != null) {
            hasGradient = true;
            toColor = colorResolved;
          } else {
            nonGradientTokens.add(t);
          }
        }
        // Non-gradient token
        else {
          nonGradientTokens.add(t);
        }
      }
    }

    // If we have gradient components, build the gradient
    if (hasGradient) {
      String gradient = buildGradient(gradientDirection, fromColor, viaColor, toColor);
      if (gradient != null) {
        inlineStyle.append(gradient).append(" ");
      }
    }

    // Process non-gradient tokens normally
    for (String t : nonGradientTokens) {
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
        boolean isGradientRelated =
            t.startsWith("from-")
                || t.startsWith("via-")
                || t.startsWith("to-")
                || t.startsWith("bg-gradient-");
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

    return new BatchResult(inlineStyle.toString().trim(), cssClasses);
  }

  /** Resuelve un token de color para gradientes (e.g., "blue-500", "gray-800") */
  private static String resolveGradientColor(String colorToken) {
    if (colorToken == null || colorToken.isBlank()) {
      return null;
    }

    // Parse color-shade pattern like "blue-500", "gray-800"
    int lastDash = colorToken.lastIndexOf('-');
    if (lastDash == -1) {
      // Single color name, try shade 500
      String hex = ColorPalette.hex(colorToken, 500);
      return hex != null ? hex : null;
    }

    String colorName = colorToken.substring(0, lastDash);
    String shadeStr = colorToken.substring(lastDash + 1);

    try {
      int shade = Integer.parseInt(shadeStr);
      String hex = ColorPalette.hex(colorName, shade);
      return hex != null ? hex : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /** Construye un gradiente linear a partir de sus componentes */
  private static String buildGradient(String direction, String from, String via, String to) {
    if (direction == null && from == null && to == null) {
      return null;
    }

    String dir = direction != null ? direction : "to bottom";

    // Validate that we have at least one valid color - fail explicitly if not
    if (from == null && to == null) {
      // Silent fail for incomplete gradient tokens (e.g., only from-* without to-*)
      return null;
    }

    String fromColor = from != null ? from : to; // Use to color as fallback if from is missing
    String toColor = to != null ? to : from; // Use from color as fallback if to is missing

    StringBuilder gradient = new StringBuilder("linear-gradient(");
    gradient.append(dir);

    if (via != null) {
      gradient.append(", ").append(fromColor).append(", ").append(via).append(", ").append(toColor);
    } else {
      gradient.append(", ").append(fromColor).append(", ").append(toColor);
    }

    gradient.append(")");
    // Para gradientes, retornamos directamente el valor CSS ya construido
    return "-fx-background-color: " + gradient.toString() + ";";
  }

  public record BatchResult(String inlineStyle, List<String> cssClasses) {

    public boolean hasInlineStyle() {
      return !inlineStyle.isBlank();
    }
  }

  /** Clears the cache (useful in tests or when changing theme) */
  /**
   * Clears the JIT compilation cache. Call when the application's utility class set changes
   * significantly (e.g., after a major theme reconfiguration). The cache is automatically bounded
   * by LRU eviction, so explicit clearing is rarely needed.
   */
  public static void clearCache() {
    CACHE.clear();
  }

  /** Current cache size */
  public static int cacheSize() {
    return CACHE.size();
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

  // Métodos estáticos legacy REMOVIDOS - ahora toda la lógica está en StyleResolver y
  // CssPropertyMapper
  // compileScale, compileColor, compileArbitrary, compileNamed han sido eliminados
}
