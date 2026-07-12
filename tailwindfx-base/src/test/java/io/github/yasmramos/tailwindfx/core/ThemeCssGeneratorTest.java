package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for ThemeCssGenerator. */
public class ThemeCssGeneratorTest {

  private ThemeCssGenerator generator;
  private ThemeConfig themeConfig;

  @BeforeEach
  public void setUp() {
    themeConfig = ThemeConfig.defaultConfig();
    generator = new ThemeCssGenerator(themeConfig);
  }

  @Test
  public void testGenerateBaseCss_NotEmpty() {
    String css = generator.generateBaseCss();
    assertNotNull(css);
    assertFalse(css.isEmpty());
    assertTrue(css.length() > 100); // Should have substantial content
  }

  @Test
  public void testGenerateBaseCss_HasRootSelector() {
    String css = generator.generateBaseCss();
    assertTrue(css.contains(".root {"));
    assertTrue(css.contains("}"));
  }

  @Test
  public void testGenerateBaseCss_HasColorVariables() {
    String css = generator.generateBaseCss();
    // Check for some standard color variables
    assertTrue(css.contains("-color-red-500"));
    assertTrue(css.contains("-color-blue-500"));
    assertTrue(css.contains("-color-green-500"));
  }

  @Test
  public void testGenerateBaseCss_HasSpacingVariables() {
    String css = generator.generateBaseCss();
    assertTrue(css.contains("-spacing-0"));
    assertTrue(css.contains("-spacing-1"));
    assertTrue(css.contains("-spacing-4"));
  }

  @Test
  public void testGenerateBaseCss_HasFontSizeVariables() {
    String css = generator.generateBaseCss();
    assertTrue(css.contains("-font-size-xs"));
    assertTrue(css.contains("-font-size-sm"));
    assertTrue(css.contains("-font-size-base"));
    assertTrue(css.contains("-font-size-lg"));
    assertTrue(css.contains("-font-size-xl"));
  }

  @Test
  public void testGenerateBaseCss_HasBorderRadiusVariables() {
    String css = generator.generateBaseCss();
    assertTrue(css.contains("-radius-none"));
    assertTrue(css.contains("-radius-sm"));
    assertTrue(css.contains("-radius-md"));
    assertTrue(css.contains("-radius-lg"));
    assertTrue(css.contains("-radius-full"));
  }

  @Test
  public void testGenerateBaseCss_HasOpacityVariables() {
    String css = generator.generateBaseCss();
    assertTrue(css.contains("-opacity-0"));
    assertTrue(css.contains("-opacity-25"));
    assertTrue(css.contains("-opacity-50"));
    assertTrue(css.contains("-opacity-75"));
    assertTrue(css.contains("-opacity-100"));
  }

  @Test
  public void testGenerateBaseCss_HasShadowVariables() {
    String css = generator.generateBaseCss();
    assertTrue(css.contains("-shadow-sm"));
    assertTrue(css.contains("-shadow-default"));
    assertTrue(css.contains("-shadow-md"));
    assertTrue(css.contains("-shadow-lg"));
    assertTrue(css.contains("-shadow-xl"));
  }

  @Test
  public void testGenerateBaseCss_ValidCssSyntax() {
    String css = generator.generateBaseCss();

    // Check basic CSS structure
    assertTrue(css.contains(".root {"));
    assertTrue(css.endsWith("}\n"));

    // Check that all properties end with semicolon
    String[] lines = css.split("\n");
    for (String line : lines) {
      line = line.trim();
      if (!line.isEmpty()
          && !line.startsWith("/*")
          && !line.equals(".root {")
          && !line.equals("}")) {
        assertTrue(line.endsWith(";"), "Line should end with semicolon: " + line);
      }
    }
  }

  @Test
  public void testGenerateBaseCss_ColorShades() {
    String css = generator.generateBaseCss();

    // Check that multiple shades are generated for a color
    assertTrue(css.contains("-color-red-50"));
    assertTrue(css.contains("-color-red-100"));
    assertTrue(css.contains("-color-red-200"));
    assertTrue(css.contains("-color-red-300"));
    assertTrue(css.contains("-color-red-400"));
    assertTrue(css.contains("-color-red-500"));
    assertTrue(css.contains("-color-red-600"));
    assertTrue(css.contains("-color-red-700"));
    assertTrue(css.contains("-color-red-800"));
    assertTrue(css.contains("-color-red-900"));
    assertTrue(css.contains("-color-red-950"));
  }
}
