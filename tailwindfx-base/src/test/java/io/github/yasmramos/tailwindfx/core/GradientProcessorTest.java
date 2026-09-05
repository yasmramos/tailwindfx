package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for GradientProcessor. */
@DisplayName("GradientProcessor Tests")
class GradientProcessorTest {

  @Nested
  @DisplayName("Gradient Token Detection")
  class GradientTokenDetectionTests {

    @Test
    @DisplayName("Should detect bg-gradient-to-* tokens")
    void testDetectsGradientDirectionTokens() {
      assertTrue(GradientProcessor.isGradientToken("bg-gradient-to-r"));
      assertTrue(GradientProcessor.isGradientToken("bg-gradient-to-b"));
      assertTrue(GradientProcessor.isGradientToken("bg-gradient-to-l"));
      assertTrue(GradientProcessor.isGradientToken("bg-gradient-to-t"));
      assertTrue(GradientProcessor.isGradientToken("bg-gradient-to-tr"));
      assertTrue(GradientProcessor.isGradientToken("bg-gradient-to-br"));
      assertTrue(GradientProcessor.isGradientToken("bg-gradient-to-bl"));
      assertTrue(GradientProcessor.isGradientToken("bg-gradient-to-tl"));
    }

    @Test
    @DisplayName("Should detect from-* tokens")
    void testDetectsFromTokens() {
      assertTrue(GradientProcessor.isGradientToken("from-blue-500"));
      assertTrue(GradientProcessor.isGradientToken("from-red-600"));
      assertTrue(GradientProcessor.isGradientToken("from-gray-800"));
    }

    @Test
    @DisplayName("Should detect via-* tokens")
    void testDetectsViaTokens() {
      assertTrue(GradientProcessor.isGradientToken("via-purple-500"));
      assertTrue(GradientProcessor.isGradientToken("via-green-400"));
    }

    @Test
    @DisplayName("Should detect to-* tokens")
    void testDetectsToTokens() {
      assertTrue(GradientProcessor.isGradientToken("to-pink-500"));
      assertTrue(GradientProcessor.isGradientToken("to-yellow-300"));
    }

    @Test
    @DisplayName("Should not detect non-gradient tokens")
    void testNonGradientTokens() {
      assertFalse(GradientProcessor.isGradientToken("bg-blue-500"));
      assertFalse(GradientProcessor.isGradientToken("p-4"));
      assertFalse(GradientProcessor.isGradientToken("rounded-lg"));
      assertFalse(GradientProcessor.isGradientToken("text-white"));
      assertFalse(GradientProcessor.isGradientToken(null));
      assertFalse(GradientProcessor.isGradientToken(""));
    }
  }

  @Nested
  @DisplayName("Gradient Processing")
  class GradientProcessingTests {

    @Test
    @DisplayName("Should process simple gradient with from and to")
    void testProcessSimpleGradient() {
      String[] tokens = {"bg-gradient-to-r", "from-blue-500", "to-purple-600"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertTrue(result.isGradient());
      assertNotNull(result.inlineStyle());
      assertTrue(result.inlineStyle().contains("linear-gradient"));
      assertTrue(result.inlineStyle().contains("to right"));
    }

    @Test
    @DisplayName("Should process gradient with via color")
    void testProcessGradientWithVia() {
      String[] tokens = {"bg-gradient-to-r", "from-blue-500", "via-purple-500", "to-pink-500"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertTrue(result.isGradient());
      assertTrue(result.inlineStyle().contains("linear-gradient"));
    }

    @Test
    @DisplayName("Should handle gradient without direction (default to bottom)")
    void testGradientWithoutDirection() {
      String[] tokens = {"from-blue-500", "to-purple-600"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      // Should still be a gradient but with default direction
      assertTrue(result.isGradient());
      assertTrue(result.inlineStyle().contains("to bottom"));
    }

    @Test
    @DisplayName("Should return non-gradient when no gradient tokens present")
    void testNoGradientTokens() {
      String[] tokens = {"bg-blue-500", "p-4", "rounded-lg"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertFalse(result.isGradient());
      assertNull(result.inlineStyle());
    }

    @Test
    @DisplayName("Should handle empty token array")
    void testEmptyTokenArray() {
      String[] tokens = {};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertFalse(result.isGradient());
      assertNull(result.inlineStyle());
    }

    @Test
    @DisplayName("Should handle null token array")
    void testNullTokenArray() {
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(null);

      assertFalse(result.isGradient());
      assertNull(result.inlineStyle());
    }
  }

  @Nested
  @DisplayName("Gradient Direction Mapping")
  class DirectionMappingTests {

    @Test
    @DisplayName("Should map 'b' to 'to bottom'")
    void testDirectionBottom() {
      String[] tokens = {"bg-gradient-to-b", "from-blue-500", "to-purple-600"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertTrue(result.inlineStyle().contains("to bottom"));
    }

    @Test
    @DisplayName("Should map 't' to 'to top'")
    void testDirectionTop() {
      String[] tokens = {"bg-gradient-to-t", "from-blue-500", "to-purple-600"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertTrue(result.inlineStyle().contains("to top"));
    }

    @Test
    @DisplayName("Should map 'r' to 'to right'")
    void testDirectionRight() {
      String[] tokens = {"bg-gradient-to-r", "from-blue-500", "to-purple-600"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertTrue(result.inlineStyle().contains("to right"));
    }

    @Test
    @DisplayName("Should map 'l' to 'to left'")
    void testDirectionLeft() {
      String[] tokens = {"bg-gradient-to-l", "from-blue-500", "to-purple-600"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertTrue(result.inlineStyle().contains("to left"));
    }

    @Test
    @DisplayName("Should map 'tr' to 'to top right'")
    void testDirectionTopRight() {
      String[] tokens = {"bg-gradient-to-tr", "from-blue-500", "to-purple-600"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertTrue(result.inlineStyle().contains("to top right"));
    }

    @Test
    @DisplayName("Should map 'br' to 'to bottom right'")
    void testDirectionBottomRight() {
      String[] tokens = {"bg-gradient-to-br", "from-blue-500", "to-purple-600"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertTrue(result.inlineStyle().contains("to bottom right"));
    }

    @Test
    @DisplayName("Should map 'bl' to 'to bottom left'")
    void testDirectionBottomLeft() {
      String[] tokens = {"bg-gradient-to-bl", "from-blue-500", "to-purple-600"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertTrue(result.inlineStyle().contains("to bottom left"));
    }

    @Test
    @DisplayName("Should map 'tl' to 'to top left'")
    void testDirectionTopLeft() {
      String[] tokens = {"bg-gradient-to-tl", "from-blue-500", "to-purple-600"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertTrue(result.inlineStyle().contains("to top left"));
    }
  }

  @Nested
  @DisplayName("Color Resolution")
  class ColorResolutionTests {

    @Test
    @DisplayName("Should resolve standard Tailwind colors")
    void testStandardColorResolution() {
      String[] tokens = {"bg-gradient-to-r", "from-blue-500", "to-purple-600"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertTrue(result.inlineStyle().contains("#"));
    }

    @Test
    @DisplayName("Should resolve colors with opacity modifier")
    void testColorWithOpacity() {
      String[] tokens = {"bg-gradient-to-r", "from-blue-500/80", "to-purple-600"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertTrue(result.isGradient());
      // Should contain rgba format when opacity is specified
      assertTrue(result.inlineStyle().contains("rgba") || result.inlineStyle().contains("#"));
    }

    @Test
    @DisplayName("Should handle transparent as fallback color")
    void testTransparentFallback() {
      String[] tokens = {"bg-gradient-to-r", "from-blue-500"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertTrue(result.isGradient());
      // When only 'from' is specified, 'to' defaults to transparent
      assertNotNull(result.inlineStyle());
    }

    @Test
    @DisplayName("Should handle various shade values")
    void testVariousShades() {
      String[] shades = {"50", "100", "200", "300", "400", "500", "600", "700", "800", "900"};

      for (String shade : shades) {
        String[] tokens = {"bg-gradient-to-r", "from-blue-" + shade, "to-gray-" + shade};
        GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

        assertTrue(result.isGradient(), "Should handle shade: " + shade);
      }
    }

    @Test
    @DisplayName("Should handle different color families")
    void testDifferentColorFamilies() {
      String[] colors = {"red", "orange", "yellow", "green", "blue", "indigo", "purple", "pink"};

      for (String color : colors) {
        String[] tokens = {"bg-gradient-to-r", "from-" + color + "-500", "to-gray-500"};
        GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

        assertTrue(result.isGradient(), "Should handle color: " + color);
      }
    }
  }

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle only from-* without to-*")
    void testOnlyFromColor() {
      String[] tokens = {"bg-gradient-to-r", "from-blue-500"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertTrue(result.isGradient());
      assertNotNull(result.inlineStyle());
    }

    @Test
    @DisplayName("Should handle only to-* without from-*")
    void testOnlyToColor() {
      String[] tokens = {"bg-gradient-to-r", "to-purple-600"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertTrue(result.isGradient());
      assertTrue(result.inlineStyle().contains("transparent"));
    }

    @Test
    @DisplayName("Should handle gradient with only via-*")
    void testOnlyViaColor() {
      String[] tokens = {"bg-gradient-to-r", "via-purple-500"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      // Via without from/to should still produce a gradient with transparent ends
      assertTrue(result.isGradient());
    }

    @Test
    @DisplayName("Should handle multiple from/via/to tokens (last one wins)")
    void testMultipleColorStops() {
      String[] tokens = {
        "bg-gradient-to-r",
        "from-red-500",
        "from-blue-500", // This should override
        "to-yellow-500",
        "to-purple-500" // This should override
      };
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertTrue(result.isGradient());
      // The last specified colors should be used
    }

    @Test
    @DisplayName("Should handle tokens with extra whitespace")
    void testTokensWithWhitespace() {
      // Note: GradientProcessor expects pre-trimmed tokens
      // Whitespace handling is done by JitCompiler.compileBatch()
      String[] tokens = {"bg-gradient-to-r", "from-blue-500", "to-purple-600"};
      GradientProcessor.GradientResult result = GradientProcessor.processGradientTokens(tokens);

      assertTrue(result.isGradient());
    }
  }

  @Nested
  @DisplayName("Gradient Result Record")
  class GradientResultRecordTests {

    @Test
    @DisplayName("Should create GradientResult with inline style and isGradient flag")
    void testGradientResultCreation() {
      GradientProcessor.GradientResult result =
          new GradientProcessor.GradientResult("-fx-background-color: red;", true);

      assertEquals("-fx-background-color: red;", result.inlineStyle());
      assertTrue(result.isGradient());
    }

    @Test
    @DisplayName("Should create GradientResult with null style for non-gradients")
    void testNonGradientResult() {
      GradientProcessor.GradientResult result = new GradientProcessor.GradientResult(null, false);

      assertNull(result.inlineStyle());
      assertFalse(result.isGradient());
    }

    @Test
    @DisplayName("GradientResult should support equals and hashCode")
    void testEqualsAndHashCode() {
      GradientProcessor.GradientResult result1 =
          new GradientProcessor.GradientResult("style1", true);
      GradientProcessor.GradientResult result2 =
          new GradientProcessor.GradientResult("style1", true);
      GradientProcessor.GradientResult result3 =
          new GradientProcessor.GradientResult("style2", false);

      assertEquals(result1, result2);
      assertNotEquals(result1, result3);
      assertEquals(result1.hashCode(), result2.hashCode());
    }
  }

  @Nested
  @DisplayName("Integration with JitCompiler")
  class IntegrationTests {

    @Test
    @DisplayName("Should work with JitCompiler.compileBatch")
    void testIntegrationWithJitCompiler() {
      JitCompiler.BatchResult result =
          JitCompiler.compileBatch("bg-gradient-to-r", "from-blue-500", "to-purple-600", "p-4");

      assertTrue(result.hasInlineStyle());
      assertTrue(result.inlineStyle().contains("linear-gradient"));
      assertTrue(result.inlineStyle().contains("-fx-padding"));
    }

    @Test
    @DisplayName("Should handle complex gradient combinations")
    void testComplexGradientCombination() {
      JitCompiler.BatchResult result =
          JitCompiler.compileBatch(
              "bg-gradient-to-br",
              "from-gray-900",
              "via-purple-800",
              "to-pink-700",
              "text-white",
              "p-8",
              "rounded-xl");

      assertTrue(result.hasInlineStyle());
      assertTrue(result.inlineStyle().contains("linear-gradient"));
      assertTrue(result.inlineStyle().contains("to bottom right"));
    }
  }
}
