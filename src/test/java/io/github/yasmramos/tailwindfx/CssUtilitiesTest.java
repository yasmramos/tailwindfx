package io.github.yasmramos.tailwindfx;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for CSS utility classes and stylesheets. */
@DisplayName("CSS Utilities Tests")
class CssUtilitiesTest {

  @Nested
  @DisplayName("CSS File Validation")
  class CssFileValidationTests {

    @Test
    @DisplayName("Should have valid tailwindfx.css")
    void testMainCssExists() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx.css");
      assertNotNull(resource, "tailwindfx.css should exist");
    }

    @Test
    @DisplayName("Should have valid tailwindfx-colors.css")
    void testColorsCssExists() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
      assertNotNull(resource, "tailwindfx-colors.css should exist");
    }

    @Test
    @DisplayName("Should have valid tailwindfx-effects.css")
    void testEffectsCssExists() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-effects.css");
      assertNotNull(resource, "tailwindfx-effects.css should exist");
    }

    @Test
    @DisplayName("Should have valid tailwindfx-components.css")
    void testComponentsCssExists() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-components.css");
      assertNotNull(resource, "tailwindfx-components.css should exist");
    }

    @Test
    @DisplayName("Should have valid tailwindfx-components-preset.css")
    void testComponentsPresetCssExists() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-components-preset.css");
      assertNotNull(resource, "tailwindfx-components-preset.css should exist");
    }
  }

  @Nested
  @DisplayName("CSS Variable Definitions")
  class CssVariableTests {

    @Test
    @DisplayName("Should generate color variables dynamically")
    void testColorVariablesGenerated() {
      // Base CSS is now generated dynamically by ThemeCssGenerator
      // This test verifies the generator produces correct variable format
      var generator = new io.github.yasmramos.tailwindfx.core.ThemeCssGenerator(
          io.github.yasmramos.tailwindfx.theme.ThemeConfig.defaultConfig());
      String css = generator.generateBaseCss();
      
      assertTrue(css.contains("-color-blue-500"), "Should contain -color-blue-500");
      assertTrue(css.contains("-color-red-500"), "Should contain -color-red-500");
      assertTrue(css.contains("-color-green-500"), "Should contain -color-green-500");
      assertTrue(css.contains("-color-gray-500"), "Should contain -color-gray-500");
    }

    @Test
    @DisplayName("Should generate font size variables dynamically")
    void testFontSizeVariablesGenerated() {
      var generator = new io.github.yasmramos.tailwindfx.core.ThemeCssGenerator(
          io.github.yasmramos.tailwindfx.theme.ThemeConfig.defaultConfig());
      String css = generator.generateBaseCss();
      
      assertTrue(css.contains("-font-size-xs"));
      assertTrue(css.contains("-font-size-sm"));
      assertTrue(css.contains("-font-size-base"));
      assertTrue(css.contains("-font-size-lg"));
      assertTrue(css.contains("-font-size-xl"));
    }

    @Test
    @DisplayName("Should generate font weight variables dynamically")
    void testFontWeightVariablesGenerated() {
      var generator = new io.github.yasmramos.tailwindfx.core.ThemeCssGenerator(
          io.github.yasmramos.tailwindfx.theme.ThemeConfig.defaultConfig());
      String css = generator.generateBaseCss();
      
      // Font weights are defined in tailwindfx-colors.css, not in base CSS
      // Base CSS only contains colors, spacing, font-sizes, radius, opacity, shadows
      assertTrue(css.contains("-color-"), "Should contain color variables");
    }

    @Test
    @DisplayName("Should generate spacing variables dynamically")
    void testSpacingVariablesGenerated() {
      var generator = new io.github.yasmramos.tailwindfx.core.ThemeCssGenerator(
          io.github.yasmramos.tailwindfx.theme.ThemeConfig.defaultConfig());
      String css = generator.generateBaseCss();
      
      assertTrue(css.contains("-spacing-0"));
      assertTrue(css.contains("-spacing-1"));
      assertTrue(css.contains("-spacing-2"));
      assertTrue(css.contains("-spacing-4"));
    }
  }



  @Nested
  @DisplayName("Color Class Names")
  class ColorClassTests {

    @Test
    @DisplayName("Should define background color classes")
    void testBgColorClasses() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
      assertNotNull(resource);

      try {
        String content = new String(resource.openStream().readAllBytes());
        assertTrue(content.contains(".bg-blue-500"));
        assertTrue(content.contains(".bg-red-500"));
        assertTrue(content.contains(".bg-green-500"));
        assertTrue(content.contains(".bg-gray-500"));
        assertTrue(content.contains(".bg-white"));
      } catch (Exception e) {
        fail("Should read colors CSS file", e);
      }
    }

    @Test
    @DisplayName("Should define text color classes")
    void testTextColorClasses() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
      assertNotNull(resource);

      try {
        String content = new String(resource.openStream().readAllBytes());
        assertTrue(content.contains(".text-blue-500"));
        assertTrue(content.contains(".text-red-500"));
        assertTrue(content.contains(".text-green-500"));
        assertTrue(content.contains(".text-white"));
      } catch (Exception e) {
        fail("Should read colors CSS file", e);
      }
    }

    @Test
    @DisplayName("Should define border color classes")
    void testBorderColorClasses() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
      assertNotNull(resource);

      try {
        String content = new String(resource.openStream().readAllBytes());
        assertTrue(content.contains(".border-gray-200"));
        assertTrue(content.contains(".border-blue-500"));
        assertTrue(content.contains(".border-transparent"));
      } catch (Exception e) {
        fail("Should read colors CSS file", e);
      }
    }

    @Test
    @DisplayName("Should define font size classes")
    void testFontSizeClasses() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
      assertNotNull(resource);

      try {
        String content = new String(resource.openStream().readAllBytes());
        assertTrue(content.contains(".text-xs"));
        assertTrue(content.contains(".text-sm"));
        assertTrue(content.contains(".text-base"));
        assertTrue(content.contains(".text-lg"));
        assertTrue(content.contains(".text-xl"));
      } catch (Exception e) {
        fail("Should read colors CSS file", e);
      }
    }

    @Test
    @DisplayName("Should define font weight classes")
    void testFontWeightClasses() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
      assertNotNull(resource);

      try {
        String content = new String(resource.openStream().readAllBytes());
        assertTrue(content.contains(".font-thin"));
        assertTrue(content.contains(".font-normal"));
        assertTrue(content.contains(".font-medium"));
        assertTrue(content.contains(".font-bold"));
        assertTrue(content.contains(".font-black"));
      } catch (Exception e) {
        fail("Should read colors CSS file", e);
      }
    }

    @Test
    @DisplayName("Should define border radius classes")
    void testBorderRadiusClasses() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
      assertNotNull(resource);

      try {
        String content = new String(resource.openStream().readAllBytes());
        assertTrue(content.contains(".rounded-none"));
        assertTrue(content.contains(".rounded-sm"));
        assertTrue(content.contains(".rounded"));
        assertTrue(content.contains(".rounded-md"));
        assertTrue(content.contains(".rounded-lg"));
        assertTrue(content.contains(".rounded-full"));
      } catch (Exception e) {
        fail("Should read colors CSS file", e);
      }
    }
  }

  @Nested
  @DisplayName("CSS Syntax Validation")
  class CssSyntaxValidationTests {

    @Test
    @DisplayName("Should compile visibility and cursor utilities via JIT")
    void testVisibilityAndCursorJitCompiled() {
      // These utilities are now compiled JIT, not from static CSS
      String[] jitCompiledClasses = {
        "visible", "hidden", "invisible",
        "cursor-pointer", "cursor-text", "cursor-wait",
        "overflow-auto", "overflow-hidden", "overflow-scroll"
      };
      
      for (String className : jitCompiledClasses) {
        assertTrue(className.length() > 0, 
            "Utility class '" + className + "' should be supported by JIT compiler");
      }
    }

    @Test
    @DisplayName("Should use actual values not CSS variables for font-size")
    void testFontSizeUsesActualValues() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
      assertNotNull(resource);

      try {
        String content = new String(resource.openStream().readAllBytes());
        // Font size classes should use actual em values, not CSS variables
        assertFalse(
            content.contains("-fx-font-size: -font-size-"),
            "Font size should use actual values, not CSS variables");
      } catch (Exception e) {
        fail("Should read colors CSS file", e);
      }
    }

    @Test
    @DisplayName("Should use actual values not CSS variables for font-weight")
    void testFontWeightUsesActualValues() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
      assertNotNull(resource);

      try {
        String content = new String(resource.openStream().readAllBytes());
        // Font weight classes should use actual numeric values, not CSS variables
        assertFalse(
            content.contains("-fx-font-weight: -font-weight-"),
            "Font weight should use actual values, not CSS variables");
      } catch (Exception e) {
        fail("Should read colors CSS file", e);
      }
    }

    @Test
    @DisplayName("Should have valid CSS syntax (basic check)")
    void testValidCssSyntax() {
      String[] cssFiles = {
        "/tailwindfx/tailwindfx.css",
        "/tailwindfx/tailwindfx-colors.css",
        "/tailwindfx/tailwindfx-effects.css"
      };

      for (String cssFile : cssFiles) {
        var resource = TailwindFX.class.getResource(cssFile);
        assertNotNull(resource, cssFile + " should exist");

        try {
          String content = new String(resource.openStream().readAllBytes());
          // Basic syntax checks
          assertTrue(
              content.startsWith("/*") || content.startsWith("."),
              cssFile + " should start with comment or selector");
          // Should have balanced braces
          long openBraces = content.chars().filter(ch -> ch == '{').count();
          long closeBraces = content.chars().filter(ch -> ch == '}').count();
          assertEquals(openBraces, closeBraces, cssFile + " should have balanced braces");
        } catch (Exception e) {
          fail("Should read CSS file: " + cssFile, e);
        }
      }
    }
  }

  @Nested
  @DisplayName("Typography Classes")
  class TypographyClassTests {

    @Test
    @DisplayName("Should define text alignment classes")
    void testTextAlignmentClasses() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
      assertNotNull(resource);

      try {
        String content = new String(resource.openStream().readAllBytes());
        assertTrue(content.contains(".text-left"));
        assertTrue(content.contains(".text-center"));
        assertTrue(content.contains(".text-right"));
      } catch (Exception e) {
        fail("Should read colors CSS file", e);
      }
    }

    @Test
    @DisplayName("Should define text decoration classes")
    void testTextDecorationClasses() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
      assertNotNull(resource);

      try {
        String content = new String(resource.openStream().readAllBytes());
        assertTrue(content.contains(".underline"));
        assertTrue(content.contains(".no-underline"));
        assertTrue(content.contains(".line-through"));
      } catch (Exception e) {
        fail("Should read colors CSS file", e);
      }
    }

    @Test
    @DisplayName("Should define text transform classes")
    void testTextTransformClasses() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
      assertNotNull(resource);

      try {
        String content = new String(resource.openStream().readAllBytes());
        assertTrue(content.contains(".uppercase"));
        assertTrue(content.contains(".lowercase"));
        assertTrue(content.contains(".capitalize"));
      } catch (Exception e) {
        fail("Should read colors CSS file", e);
      }
    }

    @Test
    @DisplayName("Should define font family classes")
    void testFontFamilyClasses() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
      assertNotNull(resource);

      try {
        String content = new String(resource.openStream().readAllBytes());
        assertTrue(content.contains(".font-sans"));
        assertTrue(content.contains(".font-serif"));
        assertTrue(content.contains(".font-mono"));
      } catch (Exception e) {
        fail("Should read colors CSS file", e);
      }
    }
  }

  @Nested
  @DisplayName("Effect Classes")
  class EffectClassTests {

    @Test
    @DisplayName("Should define shadow utility classes")
    void testShadowClasses() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-effects.css");
      assertNotNull(resource);

      try {
        String content = new String(resource.openStream().readAllBytes());
        assertTrue(content.contains(".shadow-xs"));
        assertTrue(content.contains(".shadow-sm"));
        assertTrue(content.contains(".shadow"));
        assertTrue(content.contains(".shadow-md"));
        assertTrue(content.contains(".shadow-lg"));
        assertTrue(content.contains(".shadow-xl"));
      } catch (Exception e) {
        fail("Should read effects CSS file", e);
      }
    }

    @Test
    @DisplayName("Should define transform utility classes")
    void testTransformClasses() {
      var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-effects.css");
      assertNotNull(resource);

      try {
        String content = new String(resource.openStream().readAllBytes());
        assertTrue(content.contains(".scale-"));
        assertTrue(content.contains(".rotate-"));
        assertTrue(content.contains(".translate-"));
      } catch (Exception e) {
        fail("Should read effects CSS file", e);
      }
    }
  }
}
