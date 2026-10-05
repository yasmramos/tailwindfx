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

  @Test
  public void testGeneratedStylesheet_OpacityIsDecimalNotPx() {
    // opacity-* utilities must resolve to unitless decimals (Tailwind scale is a percentage).
    String css = io.github.yasmramos.tailwindfx.TwCatalog.generateFullCss(themeConfig);
    assertTrue(css.contains(".opacity-0 {\n  -fx-opacity: 0.0;\n}"), "opacity-0 should be 0.0");
    assertTrue(css.contains(".opacity-50 {\n  -fx-opacity: 0.5;\n}"), "opacity-50 should be 0.5");
    assertTrue(
        css.contains(".opacity-100 {\n  -fx-opacity: 1.0;\n}"), "opacity-100 should be 1.0");
    assertFalse(css.contains("-fx-opacity: 200px"), "opacity must never emit px lengths");
  }

  @Test
  public void testGeneratedStylesheet_ScaleIsUnitlessOnBothAxes() {
    String css = io.github.yasmramos.tailwindfx.TwCatalog.generateFullCss(themeConfig);
    assertTrue(css.contains(".scale-50 {\n  -fx-scale-x: 0.5;\n  -fx-scale-y: 0.5;\n}"));
    assertTrue(css.contains(".scale-100 {\n  -fx-scale-x: 1.0;\n  -fx-scale-y: 1.0;\n}"));
    assertFalse(css.contains("-fx-scale-x: 200px"), "scale must never emit px lengths");
  }

  @Test
  public void testGeneratedStylesheet_AxisSubPrefixesAndGap() {
    String css = io.github.yasmramos.tailwindfx.TwCatalog.generateFullCss(themeConfig);
    // gap-y-* must emit -fx-vgap (previously it wrongly emitted -fx-hgap).
    assertTrue(css.contains(".gap-y-4 {\n  -fx-vgap: 16px;\n}"));
    assertTrue(css.contains(".gap-x-4 {\n  -fx-hgap: 16px;\n}"));
    // Bare gap-N sets both axes.
    assertTrue(css.contains(".gap-4 {\n  -fx-hgap: 16px;\n  -fx-vgap: 16px;\n}"));
    // scale-y-* must emit -fx-scale-y only.
    assertTrue(css.contains(".scale-y-50 {\n  -fx-scale-y: 0.5;\n}"));
  }

  @Test
  public void testGeneratedStylesheet_RoundedEmitsBorderRadiusToo() {
    String css = io.github.yasmramos.tailwindfx.TwCatalog.generateFullCss(themeConfig);
    assertTrue(
        css.contains(".rounded-lg {\n  -fx-background-radius: 8px;\n  -fx-border-radius: 8px;\n}"),
        "rounded-* must round both background and border so border-* + rounded-* works");
  }

  @Test
  public void testGeneratedStylesheet_ShadowUsesFullDropshadowSignature() {
    String css = io.github.yasmramos.tailwindfx.TwCatalog.generateFullCss(themeConfig);
    assertTrue(
        css.contains(
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 2, 0, 0, 1);"), "shadow-sm");
    assertTrue(
        css.contains("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"),
        "shadow-md");
    assertTrue(
        css.contains("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 10);"),
        "shadow-lg");
    assertTrue(
        css.contains("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 25, 0, 0, 20);"),
        "shadow-xl");
    assertTrue(
        css.contains("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 50, 0, 0, 25);"),
        "shadow-2xl");
    // The old invalid 4-argument form must be gone.
    assertFalse(css.contains("dropshadow(0,"), "invalid legacy dropshadow signature found");
  }

  @Test
  public void testGeneratedStylesheet_RuntimeOnlyUtilitiesAreOmitted() {
    // transition-*, duration-*, ease-* and animate-* have no JavaFX CSS equivalent; they are
    // handled at runtime by TransitionProcessor / TwAnimation and must not appear as inert
    // rules in the generated stylesheet.
    String css = io.github.yasmramos.tailwindfx.TwCatalog.generateFullCss(themeConfig);
    assertFalse(css.contains("-fx-transition"), "inert -fx-transition-* properties must not ship");
    assertFalse(css.contains(".transition-all {"), "transition-all should be runtime-only");
    assertFalse(css.contains(".animate-spin {"), "animate-spin should be runtime-only");
    assertFalse(css.contains(".duration-300 {"), "duration-300 should be runtime-only");
  }
}
