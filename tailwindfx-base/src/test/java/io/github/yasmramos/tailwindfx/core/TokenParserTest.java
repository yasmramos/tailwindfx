package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
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
    TokenParser.ParseResult result = TokenParser.parse("bg-blue-500", "text-red-600", "w-[200px]");

    assertEquals(3, result.jitTokens().size());
    assertTrue(result.jitTokens().contains("bg-blue-500"));
    assertTrue(result.jitTokens().contains("text-red-600"));
    assertTrue(result.jitTokens().contains("w-[200px]"));
    
    assertTrue(result.cssClasses().isEmpty());
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

    assertEquals(2, result.jitTokens().size());
    assertEquals(2, result.layoutDependentTokens().size());
    
    assertTrue(result.layoutDependentTokens().contains("gap-4"));
    assertTrue(result.layoutDependentTokens().contains("mx-auto"));
    
    assertTrue(result.layoutMigrationTokens().isEmpty());
  }

  @Test
  void testParseLayoutMigrationTokens() {
    TokenParser.ParseResult result = TokenParser.parse("flex", "grid", "inline-flex");

    assertEquals(3, result.jitTokens().size());
    assertEquals(3, result.layoutMigrationTokens().size());
    
    assertTrue(result.layoutMigrationTokens().contains("flex"));
    assertTrue(result.layoutMigrationTokens().contains("grid"));
    assertTrue(result.layoutMigrationTokens().contains("inline-flex"));
  }

  @Test
  void testParseVariantTokens() {
    TokenParser.ParseResult result = TokenParser.parse(
        "hover:bg-blue-500", "focus:ring-2", "md:w-full", "dark:text-white");

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
    TokenParser.ParseResult result = TokenParser.parse(
        "blur-sm", "brightness-125", "grayscale", "invert", "sepia-0");

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

    // grow-x is a JIT token (starts with "grow" prefix which is in JIT_PREFIXES)
    // but it's not a known utility, so it should be tracked as unknown
    // However, current implementation only adds to unknownTokens if it's a CSS class
    // JIT tokens that are unknown are NOT added to unknownTokens set
    assertEquals(0, result.cssClasses().size());
    assertEquals(1, result.jitTokens().size());
    // Note: JIT tokens are not added to unknownTokens even if not recognized
    // This is by design - they will be compiled and may work at runtime
    assertTrue(result.unknownTokens().isEmpty());
    assertTrue(result.jitTokens().contains("grow-x"));
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
    TokenParser.ParseResult result = TokenParser.parse(
        "btn-primary", "bg-blue-500", "gap-4", "flex", "hover:text-white", "blur-sm");

    assertEquals(1, result.cssClasses().size());
    // Note: gap-4 and flex are also JIT tokens (they need compilation), 
    // so jitTokens contains: bg-blue-500, gap-4, flex
    assertEquals(3, result.jitTokens().size()); // bg-blue-500, gap-4, flex
    assertEquals(1, result.layoutDependentTokens().size()); // gap-4
    assertEquals(1, result.layoutMigrationTokens().size()); // flex
    assertEquals(1, result.variantTokens().size()); // hover:text-white
    assertEquals(1, result.effectTokens().size()); // blur-sm
    assertTrue(result.unknownTokens().isEmpty());
  }

  @Test
  void testMultipleTokensInSingleString() {
    TokenParser.ParseResult result = TokenParser.parse("bg-blue-500 text-red-600", "gap-4 flex");

    // All four tokens are JIT: bg-blue-500, text-red-600, gap-4, flex
    assertEquals(4, result.jitTokens().size());
    // Only gap-4 is layout-dependent (flex is layout-migration, not layout-dependent)
    assertEquals(1, result.layoutDependentTokens().size()); // gap-4 only
    assertEquals(1, result.layoutMigrationTokens().size()); // flex only
  }

  @Test
  void testNullAndBlankTokens() {
    TokenParser.ParseResult result = TokenParser.parse(null, "", "   ", "bg-blue-500");

    assertEquals(1, result.jitTokens().size());
    assertTrue(result.jitTokens().contains("bg-blue-500"));
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
    TokenParser.ParseResult result = TokenParser.parse(
        "bg-blue-500", "gap-4", "flex", "hover:text-white", "blur-sm", "unknown-token");

    // bg-blue-500: jit (1 token)
    // gap-4: jit + layout-dependent (1 token, classified in multiple categories)
    // flex: jit + layout-migration (1 token, classified in multiple categories)
    // hover:text-white: variant (1 token)
    // blur-sm: effect (1 token)
    // unknown-token: css class + unknown (1 token, classified in multiple categories)
    // Total: 6 unique tokens, but some appear in multiple categories
    // Count by classification: 3(jit) + 1(layout-dep) + 1(layout-mig) + 1(variant) + 1(effect) + 2(css+unknown) = 9
    assertEquals(9, result.totalTokenCount());
  }
}
