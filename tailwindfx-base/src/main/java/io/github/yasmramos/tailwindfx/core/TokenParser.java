package io.github.yasmramos.tailwindfx.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TokenParser - Dedicated parser for TailwindFX tokens.
 *
 * <p>This class extracts tokenization and classification logic from TwStyle.applyInternal(),
 * providing a single responsibility component for parsing space-separated token strings and
 * categorizing them into:
 *
 * <ul>
 *   <li>CSS classes (static utilities)
 *   <li>JIT tokens (dynamic compilation needed)
 *   <li>Layout-dependent tokens (require parent context)
 *   <li>Layout migration tokens (need container conversion)
 *   <li>Variant tokens (hover:, focus:, responsive, etc.)
 *   <li>Effect tokens (blur, brightness, etc. via TwEffect)
 *   <li>Unknown tokens (not recognized by the system)
 * </ul>
 *
 * <p>The parser handles:
 *
 * <ul>
 *   <li>Variant prefix stripping (hover:, md:, dark:hover:, etc.)
 *   <li>Arbitrary property syntax detection ([prop:value] vs [value])
 *   <li>JIT validation with numeric/arbitrary pattern matching
 *   <li>Opacity modifier validation using ColorUtilityValidator
 *   <li>Intelligent token classification based on TokenRegistry
 * </ul>
 *
 * <p>Usage:
 *
 * <pre>
 * TokenParser.ParseResult result = TokenParser.parse("bg-blue-500 hover:p-2 gap-4 flex");
 * result.cssClasses();      // []
 * result.jitTokens();       // ["bg-blue-500"]
 * result.layoutDependentTokens(); // ["gap-4"]
 * result.layoutMigrationTokens(); // ["flex"]
 * result.variantTokens();   // ["hover:p-2"]
 * result.effectTokens();    // []
 * result.unknownTokens();   // []
 * </pre>
 *
 * @author yasmramos
 * @since 1.0
 */
public final class TokenParser {

  private TokenParser() {
    // Prevent instantiation
  }

  /**
   * Parses and classifies an array of token strings.
   *
   * <p>This method tokenizes each input string by whitespace, then classifies each token into
   * appropriate categories based on:
   *
   * <ul>
   *   <li>Presence of variant prefixes (:)
   *   <li>Arbitrary property syntax ([...])
   *   <li>JIT prefix matching via TokenRegistry
   *   <li>Layout dependency via TokenRegistry
   *   <li>Effect token matching via TokenRegistry
   *   <li>Migration requirements via TokenRegistry
   *   <li>Known utility validation via TokenRegistry
   * </ul>
   *
   * @param tokens array of token strings (may contain multiple tokens separated by whitespace)
   * @return ParseResult containing categorized tokens
   */
  public static ParseResult parse(String... tokens) {
    List<String> cssClasses = new ArrayList<>();
    List<String> jitTokens = new ArrayList<>();
    List<String> layoutDependentTokens = new ArrayList<>();
    List<String> layoutMigrationTokens = new ArrayList<>();
    List<String> variantTokens = new ArrayList<>();
    List<String> effectTokens = new ArrayList<>();
    Set<String> unknownTokens = new HashSet<>();

    for (String token : tokens) {
      if (token == null || token.isBlank()) continue;

      // Split by whitespace to handle multiple tokens in a single string
      for (String t : token.split("\\s+")) {
        if (t.isBlank()) continue;

        classifyToken(
            t,
            cssClasses,
            jitTokens,
            layoutDependentTokens,
            layoutMigrationTokens,
            variantTokens,
            effectTokens,
            unknownTokens);
      }
    }

    return new ParseResult(
        cssClasses,
        jitTokens,
        layoutDependentTokens,
        layoutMigrationTokens,
        variantTokens,
        effectTokens,
        unknownTokens);
  }

  /**
   * Classifies a single token into appropriate categories.
   *
   * <p>This is the core classification logic that was previously embedded in
   * TwStyle.applyInternal(). It handles:
   *
   * <ol>
   *   <li>Variant detection (distinguishing variant syntax from arbitrary property syntax)
   *   <li>Base utility extraction for variant tokens
   *   <li>Effect token detection and delegation
   *   <li>Variant token handling
   *   <li>JIT token detection with proper validation
   *   <li>Static CSS class identification
   *   <li>Unknown token tracking
   * </ol>
   *
   * @param token the token to classify
   * @param cssClasses list to add static CSS classes to
   * @param jitTokens list to add JIT tokens to
   * @param layoutDependentTokens list to add layout-dependent tokens to
   * @param layoutMigrationTokens list to add migration-required tokens to
   * @param variantTokens list to add variant tokens to
   * @param effectTokens list to add effect tokens to
   * @param unknownTokens set to add unknown tokens to
   */
  private static void classifyToken(
      String token,
      List<String> cssClasses,
      List<String> jitTokens,
      List<String> layoutDependentTokens,
      List<String> layoutMigrationTokens,
      List<String> variantTokens,
      List<String> effectTokens,
      Set<String> unknownTokens) {

    // Check if token has variants (hover:, focus:, dark:, sm:, etc.)
    // Must distinguish between:
    // 1. Variant syntax (prefix:) → hasVariant = true
    // 2. Arbitrary property syntax ([prop:value]) → hasVariant = false, goes to JIT
    // 3. Arbitrary variant syntax ([&:hover]:utility or [@media...]:utility) → hasVariant = true
    boolean isArbitraryProperty = token.startsWith("[") && !token.contains("]:");
    boolean hasVariant = token.contains(":") && !isArbitraryProperty;

    // Extract base utility for variant tokens to enable proper validation
    String baseUtility = hasVariant ? stripVariantPrefix(token) : token;

    // Check for unsupported variants (responsive/state) on layout-dependent properties
    // Delegate to VariantManager for automatic handling instead of throwing exception
    if (hasVariant && TokenRegistry.isLayoutDependent(baseUtility)) {
      variantTokens.add(token);
      return;
    }

    // Handle filter/effect tokens via TwEffect (blur, brightness, contrast, etc.)
    if (TokenRegistry.isEffectToken(baseUtility)) {
      effectTokens.add(hasVariant ? token : baseUtility);
      return;
    }

    if (hasVariant) {
      // Tokens with variants need special handling via VariantManager
      variantTokens.add(token);
    } else if (TokenRegistry.requiresMigration(token)) {
      // Migration tokens (flex, grid, inline-flex) are also JIT tokens
      jitTokens.add(token);
      layoutMigrationTokens.add(token);
    } else if (isJitToken(token)) {
      // JIT tokens (dynamic/arbitrary values or utilities with numeric suffixes) get compiled at
      // runtime
      // However, named values like text-white, bg-blue-500, text-lg are known utilities
      // that should be applied as CSS classes, not JIT compiled.
      if (requiresJitCompilation(token)) {
        jitTokens.add(token);
        if (TokenRegistry.isLayoutDependent(token)) {
          layoutDependentTokens.add(token);
        }
      } else {
        // Named value with JIT prefix (e.g., text-white, bg-blue-500) -> CSS class
        cssClasses.add(token);
        // Track unknown tokens for warning (single pass)
        if (!TokenRegistry.isKnownUtility(token)) {
          unknownTokens.add(token);
        }
      }
    } else {
      cssClasses.add(token);
      // Track unknown tokens for warning (single pass)
      if (!TokenRegistry.isKnownUtility(token)) {
        unknownTokens.add(token);
      }
    }
  }

  /**
   * Checks if a token contains arbitrary value syntax.
   *
   * @param token the token to check
   * @return true if token contains [...] syntax
   */
  private static boolean hasArbitraryValue(String token) {
    return token.contains("[") && token.contains("]");
  }

  /**
   * Detects if a token should be compiled as JIT.
   *
   * <p>Uses strict prefix matching + numeric/arbitrary/negative pattern validation. Eliminates
   * false positives like "card-2" or "panel-v2".
   *
   * <p>This method strips variant prefixes (hover:, focus:, dark:, sm:, etc.) before checking, so
   * that "hover:bg-blue-500" is correctly identified as a JIT token.
   *
   * @param token the token to check
   * @return true if this token should be compiled as JIT
   */
  private static boolean isJitToken(String token) {
    // Delegate to TokenRegistry for centralized JIT detection
    return TokenRegistry.isJitPrefix(token);
  }

  /**
   * Checks if a JIT token has an arbitrary or numeric value that requires runtime compilation.
   *
   * <p>Tokens like "text-white", "text-lg", "bg-blue-500" are known utilities that should be
   * applied as CSS classes. Only tokens with arbitrary values ([...]) or numeric suffixes require
   * JIT compilation.
   *
   * <p>Tokens with opacity modifiers (e.g., "bg-blue-500/80", "text-red-500/50") also require JIT
   * compilation because the opacity value needs to be applied dynamically.
   *
   * <p>Numeric values like "gap-4", "p-2" are part of Tailwind's spacing scale and are known
   * utilities that should be applied as CSS classes, not JIT compiled.
   *
   * @param token the token to check (already confirmed as JIT prefix)
   * @return true if this token requires JIT compilation
   */
  private static boolean requiresJitCompilation(String token) {
    // Arbitrary values always need JIT
    if (hasArbitraryValue(token)) {
      return true;
    }

    // Strip variant prefixes to get the base token
    String baseToken = stripVariantPrefix(token);

    // Extract the value part (everything after the first hyphen)
    int hyphenIndex = baseToken.indexOf('-');
    if (hyphenIndex < 0 || hyphenIndex >= baseToken.length() - 1) {
      // No value part (e.g., "text", "bg") - shouldn't happen for valid tokens
      return false;
    }

    String valuePart = baseToken.substring(hyphenIndex + 1);

    // Check for opacity modifier (e.g., blue-500/80, red-600/50)
    // The format is: value/opacity where opacity is a number
    if (valuePart.contains("/")) {
      String[] parts = valuePart.split("/", 2);
      if (parts.length == 2) {
        String opacityPart = parts[1];
        // Opacity can be a number (50, 80) or decimal (0.5, .75)
        if (opacityPart.matches("^\\.?\\d+(\\.\\d+)?$")) {
          return true;
        }
      }
    }

    // Named values (white, blue-500, lg, 2xl, etc.) and spacing scale values (0, 1, 2, 3, 4, etc.)
    // are known utilities and should be applied as CSS classes, not JIT
    return false;
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
    // Find the last colon to handle chained variants like "dark:hover:bg-blue-500"
    int lastColon = token.lastIndexOf(':');
    if (lastColon >= 0 && lastColon < token.length() - 1) {
      return token.substring(lastColon + 1);
    }
    return token;
  }

  /**
   * Result object containing categorized tokens from parsing.
   *
   * <p>This record provides immutable access to the classified tokens, enabling
   * TwStyle.applyInternal() to act as an orchestrator that directs each category to its appropriate
   * applier.
   *
   * @param cssClasses static utility classes to apply via CSS
   * @param jitTokens dynamic tokens requiring JIT compilation
   * @param layoutDependentTokens tokens requiring parent container context
   * @param layoutMigrationTokens tokens requiring container migration (flex, grid)
   * @param variantTokens tokens with state/responsive variants
   * @param effectTokens filter/effect tokens for TwEffect
   * @param unknownTokens unrecognized tokens for warning/debugging
   */
  public record ParseResult(
      List<String> cssClasses,
      List<String> jitTokens,
      List<String> layoutDependentTokens,
      List<String> layoutMigrationTokens,
      List<String> variantTokens,
      List<String> effectTokens,
      Set<String> unknownTokens) {

    /**
     * Creates an empty ParseResult with all categories initialized to empty collections.
     *
     * @return empty ParseResult
     */
    public static ParseResult empty() {
      return new ParseResult(
          new ArrayList<>(),
          new ArrayList<>(),
          new ArrayList<>(),
          new ArrayList<>(),
          new ArrayList<>(),
          new ArrayList<>(),
          new HashSet<>());
    }

    /**
     * Checks if any tokens were parsed.
     *
     * @return true if all categories are empty
     */
    public boolean isEmpty() {
      return cssClasses.isEmpty()
          && jitTokens.isEmpty()
          && layoutDependentTokens.isEmpty()
          && layoutMigrationTokens.isEmpty()
          && variantTokens.isEmpty()
          && effectTokens.isEmpty()
          && unknownTokens.isEmpty();
    }

    /**
     * Returns the total number of tokens across all categories.
     *
     * @return total token count
     */
    public int totalTokenCount() {
      return cssClasses.size()
          + jitTokens.size()
          + layoutDependentTokens.size()
          + layoutMigrationTokens.size()
          + variantTokens.size()
          + effectTokens.size()
          + unknownTokens.size();
    }
  }
}
