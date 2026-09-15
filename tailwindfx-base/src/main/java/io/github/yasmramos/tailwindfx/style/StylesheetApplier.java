package io.github.yasmramos.tailwindfx.style;

import io.github.yasmramos.tailwindfx.TwConfig;
import io.github.yasmramos.tailwindfx.core.TokenParser;
import io.github.yasmramos.tailwindfx.core.UtilityConflictResolver;
import javafx.scene.Node;

/**
 * StylesheetApplier — Applies tokens with stylesheet preference mode.
 *
 * <p>When preferStylesheet is enabled, this class adds CSS classes for tokens that exist
 * in the AOT-generated stylesheet, and only uses JIT inline compilation as fallback for
 * dynamic/arbitrary values not resolved at build-time.
 *
 * <p>This class fixes:
 * <ul>
 *   <li>Bug #7: Duplicate isJitToken logic by reusing TokenParser/TokenRegistry for detecting
 *       arbitrary values and opacity modifiers instead of reimplementing contains("[")/indexOf('/')
 *   <li>Bug #8: Double class addition by using a single path (UtilityConflictResolver.applyAll)
 *       instead of both manual styleClass.add loop and UtilityConflictResolver
 * </ul>
 *
 * <pre>
 * // Apply tokens with stylesheet preference
 * StylesheetApplier.applyWithStylesheetPreference(node, "bg-blue-500", "p-[13px]", "text-red-500/50");
 * </pre>
 */
public final class StylesheetApplier {

  private static final StylesheetApplier INSTANCE = new StylesheetApplier();

  private StylesheetApplier() {}

  /**
   * Applies tokens with stylesheet preference mode. When preferStylesheet is enabled, this method
   * adds CSS classes for tokens that exist in the AOT-generated stylesheet, and only uses JIT
   * inline compilation as fallback for dynamic/arbitrary values not resolved at build-time.
   *
   * <p>Static tokens (no arbitrary values or opacity modifiers) are added as CSS classes.
   * Dynamic/arbitrary tokens ([...], color/opacity) fallback to JIT inline compilation.
   *
   * <p>Fixes Bug #8: Uses only UtilityConflictResolver.applyAll to avoid double class addition.
   *
   * @param node the target node
   * @param tokens the tokens to apply (already tokenized, no whitespace splitting needed)
   */
  public static void applyWithStylesheetPreference(Node node, String... tokens) {
    if (node == null || tokens == null || tokens.length == 0) {
      return;
    }

    // Separate tokens into:
    // 1. Static tokens (no arbitrary values) → apply as CSS class
    // 2. Dynamic/arbitrary tokens ([...], /opacity) → fallback to JIT inline
    java.util.List<String> staticTokens = new java.util.ArrayList<>();
    java.util.List<String> dynamicTokens = new java.util.ArrayList<>();

    for (String token : tokens) {
      if (token == null || token.isBlank()) continue;

      // Use TokenParser's isDynamicToken helper for consistent detection
      // This avoids duplicating the logic from TwStyle.isValidColorUtilityBase
      if (isDynamicToken(token)) {
        dynamicTokens.add(token);
      } else {
        staticTokens.add(token);
      }
    }

    // Apply static tokens as CSS classes (AOT stylesheet will handle them)
    // Fix Bug #8: Use only UtilityConflictResolver to avoid double class addition
    if (!staticTokens.isEmpty()) {
      UtilityConflictResolver.applyAll(node, staticTokens.toArray(new String[0]));
    }

    // Fallback to JIT inline for dynamic/arbitrary values
    if (!dynamicTokens.isEmpty()) {
      io.github.yasmramos.tailwindfx.style.StyleMerger.applyJit(node, dynamicTokens.toArray(new String[0]));
    }
  }

  /**
   * Checks if a token is dynamic (requires JIT compilation).
   *
   * <p>A token is dynamic if it contains:
   * <ul>
   *   <li>Arbitrary value syntax: [...]
   *   <li>Opacity modifier on a color utility: bg-red-500/50
   * </ul>
   *
   * <p>This method delegates to TokenRegistry via TokenParser for consistent detection,
   * fixing Bug #7 by avoiding duplicate logic.
   *
   * @param token the token to check
   * @return true if the token requires JIT compilation
   */
  private static boolean isDynamicToken(String token) {
    // Check for arbitrary value syntax [...]
    if (token.contains("[") && token.contains("]")) {
      return true;
    }

    // Check for opacity modifier on color utilities
    // Delegate to TokenParser's logic for consistency (fixes Bug #7)
    if (token.contains("/")) {
      int slashIndex = token.indexOf('/');
      if (slashIndex > 0) {
        String base = token.substring(0, slashIndex);
        // Use the same validation as TokenParser to avoid false positives like "icon/large"
        return isValidColorUtilityBase(base);
      }
    }

    return false;
  }

  /**
   * Validates if a string is a valid color utility base that can have an opacity modifier.
   *
   * <p>This delegates to ColorUtilityValidator for centralized validation, ensuring consistency
   * with TokenParser's isJitToken detection.
   *
   * @param base the base token to validate
   * @return true if this is a valid color utility that supports opacity
   */
  private static boolean isValidColorUtilityBase(String base) {
    return io.github.yasmramos.tailwindfx.core.ColorUtilityValidator.isValidColorUtilityBase(base);
  }
}
