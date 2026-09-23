package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * TokenParserTest - Unit tests for TokenParser classification logic.
 *
 * <p>Verifies that token parsing and classification produces identical results before and after
 * refactoring from TwStyle.applyInternal() to TokenParser.parse().
 *
 * @author yasmramos
 * @since 1.0
 */
class TokenParserTest {

  @Test
  void testParseStaticUtilityClasses() {
    TokenParser.ParseResult result = TokenParser.parse("rounded-lg", "btn-primary", "card-header");

    assertEquals(3, result.cssClasses().size());
    assertTrue(result.cssClasses().contains("rounded-lg"));
    assertTrue(result.cssClasses().contains("btn-primary"));
    assertTrue(result.cssClasses().contains("card-header"));

    assertTrue(result.jitTokens().isEmpty());
    assertTrue(result.layoutDependentTokens().isEmpty());
    assertTrue(result.layoutMigrationTokens().isEmpty());
    assertTrue(result.variantTokens().isEmpty());
    assertTrue(result.effectTokens().isEmpty());
    assertTrue(result.unknownTokens().isEmpty());
  }

  @Test
  void testParseJitTokens() {
    // Only arbitrary values and numeric values require JIT compilation
    // Named values like bg-blue-500, text-red-600 are known utilities and go as CSS classes
    TokenParser.ParseResult result = TokenParser.parse("bg-blue-500", "text-red-600", "w-[200px]");

    assertEquals(1, result.jitTokens().size());
    assertTrue(result.jitTokens().contains("w-[200px]"));

    // Named color values should be CSS classes
    assertEquals(2, result.cssClasses().size());
    assertTrue(result.cssClasses().contains("bg-blue-500"));
    assertTrue(result.cssClasses().contains("text-red-600"));

    assertTrue(result.layoutDependentTokens().isEmpty());
    assertTrue(result.layoutMigrationTokens().isEmpty());
    assertTrue(result.variantTokens().isEmpty());
    assertTrue(result.effectTokens().isEmpty());
    assertTrue(result.unknownTokens().isEmpty());
  }

  @Test
  void testParseArbitraryValueSyntax() {
    // Arbitrary values should be classified as JIT tokens
    TokenParser.ParseResult result = TokenParser.parse("m-[10px]", "p-[2rem]", "bg-[#ff0000]");

    assertEquals(3, result.jitTokens().size());
    assertTrue(result.jitTokens().contains("m-[10px]"));
    assertTrue(result.jitTokens().contains("p-[2rem]"));
    assertTrue(result.jitTokens().contains("bg-[#ff0000]"));

    assertTrue(result.cssClasses().isEmpty());
    assertTrue(result.unknownTokens().isEmpty());
  }

  @Test
  void testParseLayoutDependentTokens() {
    TokenParser.ParseResult result = TokenParser.parse("gap-4", "mx-auto");

    // gap-4 and mx-auto are layout-dependent with named values
    // They should be CSS classes AND layout-dependent tokens
    assertEquals(0, result.jitTokens().size());
    assertEquals(2, result.cssClasses().size());
    assertTrue(result.cssClasses().contains("gap-4"));
    assertTrue(result.cssClasses().contains("mx-auto"));

    // Layout-dependent tokens need programmatic application via LayoutApplier
    assertEquals(2, result.layoutDependentTokens().size());
    assertTrue(result.layoutDependentTokens().contains("gap-4"));
    assertTrue(result.layoutDependentTokens().contains("mx-auto"));

    assertTrue(result.layoutMigrationTokens().isEmpty());
  }

  @Test
  void testParseLayoutMigrationTokens() {
    TokenParser.ParseResult result = TokenParser.parse("flex", "grid", "inline-flex");

    // flex, grid, inline-flex son display properties -> CSS classes + layout migration
    // NO son JIT tokens (no tienen valores arbitrarios)
    assertEquals(0, result.jitTokens().size());
    assertEquals(3, result.cssClasses().size());
    assertEquals(3, result.layoutMigrationTokens().size());

    assertTrue(result.layoutMigrationTokens().contains("flex"));
    assertTrue(result.layoutMigrationTokens().contains("grid"));
    assertTrue(result.layoutMigrationTokens().contains("inline-flex"));
  }

  @Test
  void testParseVariantTokens() {
    TokenParser.ParseResult result =
        TokenParser.parse("hover:bg-blue-500", "focus:ring-2", "md:w-full", "dark:text-white");

    assertEquals(4, result.variantTokens().size());
    assertTrue(result.variantTokens().contains("hover:bg-blue-500"));
    assertTrue(result.variantTokens().contains("focus:ring-2"));
    assertTrue(result.variantTokens().contains("md:w-full"));
    assertTrue(result.variantTokens().contains("dark:text-white"));

    assertTrue(result.cssClasses().isEmpty());
    assertTrue(result.jitTokens().isEmpty());
  }

  @Test
  void testParseEffectTokens() {
    TokenParser.ParseResult result =
        TokenParser.parse("blur-sm", "brightness-125", "grayscale", "invert", "sepia-0");

    assertEquals(5, result.effectTokens().size());
    assertTrue(result.effectTokens().contains("blur-sm"));
    assertTrue(result.effectTokens().contains("brightness-125"));
    assertTrue(result.effectTokens().contains("grayscale"));
    assertTrue(result.effectTokens().contains("invert"));
    assertTrue(result.effectTokens().contains("sepia-0"));

    assertTrue(result.cssClasses().isEmpty());
    assertTrue(result.jitTokens().isEmpty());
  }

  @Test
  void testParseUnknownTokens() {
    TokenParser.ParseResult result = TokenParser.parse("nonexistent-class", "fake-token");

    assertEquals(2, result.cssClasses().size());
    assertEquals(2, result.unknownTokens().size());

    assertTrue(result.unknownTokens().contains("nonexistent-class"));
    assertTrue(result.unknownTokens().contains("fake-token"));
  }

  @Test
  void testCard2NotJit() {
    // Bug #16: "card-2" should NOT be classified as JIT (false positive)
    TokenParser.ParseResult result = TokenParser.parse("card-2");

    assertEquals(1, result.cssClasses().size());
    assertTrue(result.cssClasses().contains("card-2"));
    assertTrue(result.jitTokens().isEmpty());
    assertTrue(result.unknownTokens().isEmpty()); // card- is a known component prefix
  }

  @Test
  void testGrowXNoCollision() {
    // "grow-x" should not collide with "grow"
    TokenParser.ParseResult result = TokenParser.parse("grow-x");

    // grow-x starts with "grow" which is in JIT_PREFIXES, but it's not a known utility
    // Since it doesn't have arbitrary or numeric value, requiresJitCompilation returns false
    // So it goes to cssClasses, and since it's not a known utility, it's also unknown
    assertEquals(1, result.cssClasses().size());
    assertTrue(result.jitTokens().isEmpty());
    assertTrue(result.unknownTokens().contains("grow-x"));
    assertTrue(result.cssClasses().contains("grow-x"));
  }

  @Test
  void testInvertZeroVsInvert() {
    // Both invert-0 and invert should be classified as effect tokens
    TokenParser.ParseResult resultInvert = TokenParser.parse("invert");
    TokenParser.ParseResult resultInvert0 = TokenParser.parse("invert-0");

    assertEquals(1, resultInvert.effectTokens().size());
    assertTrue(resultInvert.effectTokens().contains("invert"));

    assertEquals(1, resultInvert0.effectTokens().size());
    assertTrue(resultInvert0.effectTokens().contains("invert-0"));
  }

  @Test
  void testGrayscaleZeroVsGrayscale() {
    // Both grayscale-0 and grayscale should be classified as effect tokens
    TokenParser.ParseResult resultGrayscale = TokenParser.parse("grayscale");
    TokenParser.ParseResult resultGrayscale0 = TokenParser.parse("grayscale-0");

    assertEquals(1, resultGrayscale.effectTokens().size());
    assertTrue(resultGrayscale.effectTokens().contains("grayscale"));

    assertEquals(1, resultGrayscale0.effectTokens().size());
    assertTrue(resultGrayscale0.effectTokens().contains("grayscale-0"));
  }

  @Test
  void testMixedTokenTypes() {
    TokenParser.ParseResult result =
        TokenParser.parse(
            "btn-primary", "bg-blue-500", "gap-4", "flex", "hover:text-white", "blur-sm");

    // btn-primary: css class (1)
    // bg-blue-500: css class (named value, not arbitrary/numeric) (1)
    // gap-4: css class (named value) + layout-dependent + unknown (not in known utilities registry)
    // (3)
    // flex: css class + layout-migration (no arbitrary values) (2)
    // hover:text-white: variant (1)
    // blur-sm: effect (1)
    assertEquals(4, result.cssClasses().size()); // btn-primary, bg-blue-500, gap-4, flex
    assertEquals(0, result.jitTokens().size()); // no arbitrary values
    assertEquals(1, result.layoutDependentTokens().size()); // gap-4
    assertTrue(result.layoutDependentTokens().contains("gap-4"));
    assertEquals(1, result.layoutMigrationTokens().size()); // flex
    assertEquals(1, result.variantTokens().size()); // hover:text-white
    assertEquals(1, result.effectTokens().size()); // blur-sm
    assertEquals(1, result.unknownTokens().size()); // gap-4 (not in known utilities registry)
    assertTrue(result.unknownTokens().contains("gap-4"));
  }

  @Test
  void testMultipleTokensInSingleString() {
    TokenParser.ParseResult result = TokenParser.parse("bg-blue-500 text-red-600", "gap-4 flex");

    // bg-blue-500: css class (named value)
    // text-red-600: css class (named value)
    // gap-4: css class (named value)
    // flex: css class + layout-migration (no arbitrary values, so not JIT)
    assertEquals(4, result.cssClasses().size()); // bg-blue-500, text-red-600, gap-4, flex
    assertEquals(0, result.jitTokens().size()); // no arbitrary values
    assertEquals(1, result.layoutMigrationTokens().size()); // flex only
  }

  @Test
  void testNullAndBlankTokens() {
    TokenParser.ParseResult result = TokenParser.parse(null, "", "   ", "bg-blue-500");

    // bg-blue-500 is a named value (not arbitrary/numeric), so it goes to cssClasses
    assertEquals(1, result.cssClasses().size());
    assertTrue(result.cssClasses().contains("bg-blue-500"));
    assertTrue(result.jitTokens().isEmpty());
    assertTrue(result.isEmpty() == false);
    assertEquals(1, result.totalTokenCount());
  }

  @Test
  void testEmptyParse() {
    TokenParser.ParseResult result = TokenParser.parse();

    assertTrue(result.isEmpty());
    assertEquals(0, result.totalTokenCount());
  }

  @Test
  void testChainedVariants() {
    TokenParser.ParseResult result = TokenParser.parse("dark:hover:bg-blue-500");

    assertEquals(1, result.variantTokens().size());
    assertTrue(result.variantTokens().contains("dark:hover:bg-blue-500"));
  }

  @Test
  void testOpacityModifierNotTreatedAsJitFalsePositive() {
    // icon/large should NOT be treated as opacity modifier
    TokenParser.ParseResult result = TokenParser.parse("icon/large");

    assertEquals(1, result.cssClasses().size());
    assertTrue(result.cssClasses().contains("icon/large"));
    assertTrue(result.jitTokens().isEmpty());
  }

  @Test
  void testValidOpacityModifier() {
    // bg-blue-500/80 should be treated as JIT with opacity modifier
    TokenParser.ParseResult result = TokenParser.parse("bg-blue-500/80");

    assertEquals(1, result.jitTokens().size());
    assertTrue(result.jitTokens().contains("bg-blue-500/80"));
  }

  @Test
  void testParseResultEmpty() {
    TokenParser.ParseResult empty = TokenParser.ParseResult.empty();

    assertTrue(empty.isEmpty());
    assertEquals(0, empty.totalTokenCount());
    assertTrue(empty.cssClasses().isEmpty());
    assertTrue(empty.jitTokens().isEmpty());
    assertTrue(empty.layoutDependentTokens().isEmpty());
    assertTrue(empty.layoutMigrationTokens().isEmpty());
    assertTrue(empty.variantTokens().isEmpty());
    assertTrue(empty.effectTokens().isEmpty());
    assertTrue(empty.unknownTokens().isEmpty());
  }

  @Test
  void testParseResultTotalTokenCount() {
    TokenParser.ParseResult result =
        TokenParser.parse(
            "bg-blue-500", "gap-4", "flex", "hover:text-white", "blur-sm", "unknown-token");

    // bg-blue-500: css class (named value) (1 token)
    // gap-4: css class + layout-dependent + unknown (not in known utilities registry) (3
    // classifications)
    // flex: css class + layout-migration (1 token, classified in multiple categories) (2)
    // hover:text-white: variant (1 token)
    // blur-sm: effect (1 token)
    // unknown-token: css class + unknown (1 token, classified in multiple categories) (2)
    // Total: 6 unique tokens
    // Count by classification: 0(jit) + 1(layout-dep) + 1(layout-mig) + 1(variant) + 1(effect) +
    // 4(css) + 2(unknown) = 10
    assertEquals(10, result.totalTokenCount());
  }
}
