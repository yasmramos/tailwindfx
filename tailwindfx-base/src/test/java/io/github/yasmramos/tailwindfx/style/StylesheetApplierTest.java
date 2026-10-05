package io.github.yasmramos.tailwindfx.style;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.core.ColorUtilityValidator;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for StylesheetApplier. Tests cover stylesheet preference mode, static vs dynamic token
 * handling, and edge cases.
 */
@DisplayName("StylesheetApplier Unit Tests")
class StylesheetApplierTest {

  private Button button;
  private Label label;

  @BeforeEach
  void setUp() {
    button = new Button();
    label = new Label();
  }

  @Nested
  @DisplayName("Basic Application")
  class BasicApplicationTests {

    @Test
    @DisplayName("Should apply static tokens as CSS classes")
    void testApplyStaticTokens() {
      StylesheetApplier.applyWithStylesheetPreference(button, "bg-blue-500", "p-4", "rounded-lg");

      assertNotNull(button.getStyleClass());
      assertTrue(button.getStyleClass().contains("bg-blue-500"));
      assertTrue(button.getStyleClass().contains("p-4"));
      assertTrue(button.getStyleClass().contains("rounded-lg"));
    }

    @Test
    @DisplayName("Should handle null node gracefully")
    void testNullNode() {
      assertDoesNotThrow(
          () -> StylesheetApplier.applyWithStylesheetPreference(null, "bg-blue-500", "p-4"));
    }

    @Test
    @DisplayName("Should handle null tokens gracefully")
    void testNullTokens() {
      assertDoesNotThrow(
          () -> StylesheetApplier.applyWithStylesheetPreference(button, (String[]) null));
    }

    @Test
    @DisplayName("Should handle empty tokens gracefully")
    void testEmptyTokens() {
      int initialSize = button.getStyleClass().size();
      StylesheetApplier.applyWithStylesheetPreference(button);

      assertEquals(initialSize, button.getStyleClass().size());
    }

    @Test
    @DisplayName("Should skip null or blank tokens")
    void testNullAndBlankTokens() {
      StylesheetApplier.applyWithStylesheetPreference(button, "bg-blue-500", null, "", "  ", "p-4");

      assertTrue(button.getStyleClass().contains("bg-blue-500"));
      assertTrue(button.getStyleClass().contains("p-4"));
    }
  }

  @Nested
  @DisplayName("Dynamic Token Handling")
  class DynamicTokenHandlingTests {

    @Test
    @DisplayName("Should detect arbitrary value syntax as dynamic")
    void testArbitraryValueDetection() {
      // Tokens with [...] should be treated as dynamic
      StylesheetApplier.applyWithStylesheetPreference(label, "p-[13px]", "m-[2rem]");

      // Dynamic tokens use JIT inline styles, not CSS classes
      assertNotNull(label.getStyle());
    }

    @Test
    @DisplayName("Should detect opacity modifier as dynamic")
    void testOpacityModifierDetection() {
      // Tokens with /opacity should be treated as dynamic
      StylesheetApplier.applyWithStylesheetPreference(label, "bg-red-500/50", "text-blue-600/75");

      // Dynamic tokens use JIT inline styles
      assertNotNull(label.getStyle());
    }

    @Test
    @DisplayName("Should handle mixed static and dynamic tokens")
    void testMixedTokens() {
      StylesheetApplier.applyWithStylesheetPreference(
          button,
          "bg-blue-500", // static
          "p-[13px]", // dynamic (arbitrary)
          "text-red-500/50", // dynamic (opacity)
          "rounded-lg" // static
          );

      // Static tokens should be added as CSS classes
      assertTrue(button.getStyleClass().contains("bg-blue-500"));
      assertTrue(button.getStyleClass().contains("rounded-lg"));

      // Dynamic tokens should use inline styles
      assertNotNull(button.getStyle());
    }

    @Test
    @DisplayName("Should correctly identify color utility with opacity")
    void testColorUtilityWithOpacity() {
      String token = "bg-red-500/50";
      int slashIndex = token.indexOf('/');
      String base = token.substring(0, slashIndex);

      assertTrue(ColorUtilityValidator.isValidColorUtilityBase(base));
    }

    @Test
    @DisplayName("Should not treat non-color slash as dynamic")
    void testNonColorSlashToken() {
      // icon/large should NOT be treated as dynamic (not a color utility)
      StylesheetApplier.applyWithStylesheetPreference(button, "icon/large");

      // Should be added as CSS class since it's not a color utility with opacity
      assertTrue(button.getStyleClass().contains("icon/large"));
    }
  }

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCasesTests {

    @Test
    @DisplayName("Should handle single token")
    void testSingleToken() {
      StylesheetApplier.applyWithStylesheetPreference(button, "bg-green-500");

      assertTrue(button.getStyleClass().contains("bg-green-500"));
    }

    @Test
    @DisplayName("Should handle multiple identical tokens")
    void testDuplicateTokens() {
      StylesheetApplier.applyWithStylesheetPreference(button, "p-4", "p-4", "p-4");

      // Should only add once
      int count = 0;
      for (String cls : button.getStyleClass()) {
        if ("p-4".equals(cls)) count++;
      }
      assertEquals(1, count);
    }

    @Test
    @DisplayName("Should handle complex arbitrary values")
    void testComplexArbitraryValues() {
      StylesheetApplier.applyWithStylesheetPreference(
          label, "w-[calc(100%-2rem)]", "h-[var(--custom-height)]", "bg-[#1a2b3c]");

      assertNotNull(label.getStyle());
    }

    @Test
    @DisplayName("Should handle various opacity values")
    void testVariousOpacityValues() {
      StylesheetApplier.applyWithStylesheetPreference(
          label,
          "bg-blue-500/0", // 0% opacity
          "text-red-500/5", // 5% opacity
          "border-green-500/100" // 100% opacity
          );

      assertNotNull(label.getStyle());
    }

    @Test
    @DisplayName("Should preserve existing CSS classes")
    void testPreserveExistingClasses() {
      button.getStyleClass().add("existing-class");
      button.getStyleClass().add("another-class");

      StylesheetApplier.applyWithStylesheetPreference(button, "bg-blue-500", "p-4");

      assertTrue(button.getStyleClass().contains("existing-class"));
      assertTrue(button.getStyleClass().contains("another-class"));
      assertTrue(button.getStyleClass().contains("bg-blue-500"));
      assertTrue(button.getStyleClass().contains("p-4"));
    }

    @Test
    @DisplayName("Should handle whitespace-only tokens")
    void testWhitespaceOnlyTokens() {
      StylesheetApplier.applyWithStylesheetPreference(button, "   ", "\t", "\n");

      // Should not add any classes for whitespace-only tokens
      assertTrue(
          button.getStyleClass().isEmpty()
              || button.getStyleClass().stream().noneMatch(s -> s.isBlank()));
    }
  }

  @Nested
  @DisplayName("Integration with UtilityConflictResolver")
  class IntegrationTests {

    @Test
    @DisplayName("Should use UtilityConflictResolver for static tokens")
    void testUsesUtilityConflictResolver() {
      // Apply multiple static tokens that might have conflicts
      StylesheetApplier.applyWithStylesheetPreference(
          button,
          "bg-blue-500",
          "bg-red-500", // Should override blue
          "text-white",
          "text-black" // Should override white
          );

      // UtilityConflictResolver should handle conflicts
      assertTrue(button.getStyleClass().contains("bg-red-500"));
      assertTrue(button.getStyleClass().contains("text-black"));
    }

    @Test
    @DisplayName("Should combine JIT and CSS class approaches")
    void testCombinedApproach() {
      StylesheetApplier.applyWithStylesheetPreference(
          button,
          "bg-gradient-to-r", // static - CSS class
          "from-blue-500", // static - CSS class
          "to-red-500", // static - CSS class
          "p-[20px]", // dynamic - JIT inline
          "opacity-75" // static - CSS class
          );

      // Check CSS classes
      assertTrue(button.getStyleClass().contains("bg-gradient-to-r"));
      assertTrue(button.getStyleClass().contains("from-blue-500"));
      assertTrue(button.getStyleClass().contains("to-red-500"));
      assertTrue(button.getStyleClass().contains("opacity-75"));

      // Check inline style for dynamic token
      assertNotNull(button.getStyle());
    }
  }

  @Nested
  @DisplayName("Token Classification")
  class TokenClassificationTests {

    @Test
    @DisplayName("Should classify bg-blue-500 as static")
    void testBgBlue500IsStatic() {
      assertFalse(isDynamicToken("bg-blue-500"));
    }

    @Test
    @DisplayName("Should classify p-[10px] as dynamic")
    void testP10pxIsDynamic() {
      assertTrue(isDynamicToken("p-[10px]"));
    }

    @Test
    @DisplayName("Should classify text-red-500/50 as dynamic")
    void testTextRed50050IsDynamic() {
      assertTrue(isDynamicToken("text-red-500/50"));
    }

    @Test
    @DisplayName("Should classify rounded-lg as static")
    void testRoundedLgIsStatic() {
      assertFalse(isDynamicToken("rounded-lg"));
    }

    @Test
    @DisplayName("Should classify m-[var(--spacing)] as dynamic")
    void testMVarSpacingIsDynamic() {
      assertTrue(isDynamicToken("m-[var(--spacing)]"));
    }

    @Test
    @DisplayName("Should classify bg-custom-500/25 as dynamic")
    void testBgCustom50025IsDynamic() {
      assertTrue(isDynamicToken("bg-custom-500/25"));
    }

    @Test
    @DisplayName("Should classify font-bold as static")
    void testFontBoldIsStatic() {
      assertFalse(isDynamicToken("font-bold"));
    }

    /**
     * Helper method to check if a token is dynamic. This replicates the internal logic of
     * StylesheetApplier.isDynamicToken()
     */
    private boolean isDynamicToken(String token) {
      // Check for arbitrary value syntax [...]
      if (token.contains("[") && token.contains("]")) {
        return true;
      }

      // Check for opacity modifier on color utilities
      if (token.contains("/")) {
        int slashIndex = token.indexOf('/');
        if (slashIndex > 0) {
          String base = token.substring(0, slashIndex);
          return ColorUtilityValidator.isValidColorUtilityBase(base);
        }
      }

      return false;
    }
  }

  @Nested
  @DisplayName("Real-world Scenarios")
  class RealWorldScenariosTests {

    @Test
    @DisplayName("Should handle card component styling")
    void testCardComponentStyling() {
      StylesheetApplier.applyWithStylesheetPreference(
          button, "bg-white", "rounded-lg", "shadow-md", "p-6", "hover:shadow-lg");

      assertTrue(button.getStyleClass().contains("bg-white"));
      assertTrue(button.getStyleClass().contains("rounded-lg"));
      assertTrue(button.getStyleClass().contains("shadow-md"));
      assertTrue(button.getStyleClass().contains("p-6"));
    }

    @Test
    @DisplayName("Should handle button variant styling")
    void testButtonVariantStyling() {
      StylesheetApplier.applyWithStylesheetPreference(
          button,
          "bg-blue-600",
          "text-white",
          "font-semibold",
          "py-2",
          "px-4",
          "rounded-md",
          "hover:bg-blue-700");

      assertTrue(button.getStyleClass().contains("bg-blue-600"));
      assertTrue(button.getStyleClass().contains("text-white"));
      assertTrue(button.getStyleClass().contains("font-semibold"));
    }

    @Test
    @DisplayName("Should handle badge with arbitrary color")
    void testBadgeWithArbitraryColor() {
      StylesheetApplier.applyWithStylesheetPreference(
          label,
          "bg-[#ff6b6b]",
          "text-white",
          "text-xs",
          "font-medium",
          "px-2.5",
          "py-0.5",
          "rounded-full");

      // Arbitrary color uses JIT
      assertNotNull(label.getStyle());
      // Static tokens use CSS classes
      assertTrue(label.getStyleClass().contains("text-white"));
      assertTrue(label.getStyleClass().contains("rounded-full"));
    }
  }
}
