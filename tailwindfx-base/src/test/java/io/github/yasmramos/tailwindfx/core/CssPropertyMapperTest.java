package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.style.StyleToken;
import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for CssPropertyMapper. */
class CssPropertyMapperTest {

  private CssPropertyMapper mapper;
  private ThemeConfig themeConfig;

  @BeforeEach
  void setUp() {
    themeConfig = ThemeConfig.defaultConfig();
    mapper = new CssPropertyMapper(themeConfig);
  }

  @Test
  void testMapPaddingProperty() {
    assertEquals("-fx-padding", mapper.mapToCssProperty("p"));
    assertEquals("-fx-padding", mapper.mapToCssProperty("px"));
    assertEquals("-fx-padding", mapper.mapToCssProperty("py"));
  }

  @Test
  void testMapMarginProperty() {
    // Margin properties return null because JavaFX doesn't support -fx-margin CSS property.
    // They are handled by Styles.java via HBox.setMargin(), VBox.setMargin(), GridPane.setMargin()
    assertNull(mapper.mapToCssProperty("m"));
    assertNull(mapper.mapToCssProperty("mx"));
    assertNull(mapper.mapToCssProperty("mt"));
    assertNull(mapper.mapToCssProperty("mr"));
    assertNull(mapper.mapToCssProperty("mb"));
    assertNull(mapper.mapToCssProperty("ml"));
  }

  @Test
  void testMapBackgroundColorProperty() {
    assertEquals("-fx-background-color", mapper.mapToCssProperty("bg"));
  }

  @Test
  void testMapTextColorProperty() {
    assertEquals("-fx-text-fill", mapper.mapToCssProperty("text"));
  }

  @Test
  void testMapWidthProperty() {
    assertEquals("-fx-pref-width", mapper.mapToCssProperty("w"));
  }

  @Test
  void testMapHeightProperty() {
    assertEquals("-fx-pref-height", mapper.mapToCssProperty("h"));
  }

  @Test
  void testMapOpacityProperty() {
    assertEquals("-fx-opacity", mapper.mapToCssProperty("opacity"));
  }

  @Test
  void testMapBorderRadiusProperty() {
    assertEquals("-fx-background-radius", mapper.mapToCssProperty("rounded"));
  }

  @Test
  void testMapUnknownProperty() {
    assertNull(mapper.mapToCssProperty("unknown"));
  }

  @Test
  void testResolveFontSize() {
    String result = mapper.resolveNamedValue("text", "lg");
    assertNotNull(result);
    assertTrue(result.endsWith("px"));
  }

  @Test
  void testResolveFontWeight() {
    assertEquals("700", mapper.resolveNamedValue("font", "bold"));
    assertEquals("400", mapper.resolveNamedValue("font", "normal"));
  }

  @Test
  void testResolveBorderRadius() {
    String result = mapper.resolveNamedValue("rounded", "lg");
    assertNotNull(result);
    assertTrue(result.endsWith("px"));
  }

  @Test
  void testResolveShadow() {
    String result = mapper.resolveNamedValue("shadow", "md");
    assertNotNull(result);
    assertTrue(result.contains("rgba"));
  }

  @Test
  void testResolveUnknownNamedValue() {
    assertNull(mapper.resolveNamedValue("unknown", "value"));
  }

  // ---------------------------------------------------------------------------
  // Generated-CSS bug fixes: opacity/scale must be unitless decimals, sub-prefix
  // axis handling for scale/gap, rounded border radius, full dropshadow signature.
  // ---------------------------------------------------------------------------

  private String resolveAndMap(String token) {
    StyleToken parsed = StyleToken.parse(token);
    String resolved = new StyleResolver(themeConfig).resolve(parsed);
    return mapper.map(parsed, resolved);
  }

  @Test
  void testOpacityEmitsDecimalNotPx() {
    assertEquals("-fx-opacity: 0.0;", resolveAndMap("opacity-0"));
    assertEquals("-fx-opacity: 0.1;", resolveAndMap("opacity-10"));
    assertEquals("-fx-opacity: 0.5;", resolveAndMap("opacity-50"));
    assertEquals("-fx-opacity: 1.0;", resolveAndMap("opacity-100"));
  }

  @Test
  void testScaleEmitsUnitlessFactorOnBothAxes() {
    String s50 = resolveAndMap("scale-50");
    assertNotNull(s50);
    assertTrue(s50.contains("-fx-scale-x: 0.5;"), s50);
    assertTrue(s50.contains("-fx-scale-y: 0.5;"), s50);
    assertFalse(s50.contains("px"), "scale values must never carry px: " + s50);

    String s100 = resolveAndMap("scale-100");
    assertTrue(s100.contains("-fx-scale-x: 1.0;"), s100);
    assertTrue(s100.contains("-fx-scale-y: 1.0;"), s100);
  }

  @Test
  void testScaleAxisSubPrefixes() {
    String sx = resolveAndMap("scale-x-50");
    assertEquals("-fx-scale-x: 0.5;", sx);

    String sy = resolveAndMap("scale-y-50");
    assertEquals("-fx-scale-y: 0.5;", sy);
  }

  @Test
  void testGapAxisSubPrefixes() {
    String gx = resolveAndMap("gap-x-4");
    assertEquals("-fx-hgap: 16px;", gx);

    String gy = resolveAndMap("gap-y-4");
    assertEquals("-fx-vgap: 16px;", gy);

    String g = resolveAndMap("gap-4");
    assertNotNull(g);
    assertTrue(g.contains("-fx-hgap: 16px;"), g);
    assertTrue(g.contains("-fx-vgap: 16px;"), g);
  }

  @Test
  void testRoundedEmitsBackgroundAndBorderRadius() {
    String result = resolveAndMap("rounded-lg");
    assertNotNull(result);
    assertTrue(result.contains("-fx-background-radius:"), result);
    assertTrue(result.contains("-fx-border-radius:"), result);
  }

  @Test
  void testRoundedScaleMatchesTailwind() {
    assertTrue(resolveAndMap("rounded-none").contains("0px"), resolveAndMap("rounded-none"));
    assertTrue(resolveAndMap("rounded-sm").contains("2px"), resolveAndMap("rounded-sm"));
    assertTrue(resolveAndMap("rounded").contains("4px"), resolveAndMap("rounded"));
    assertTrue(resolveAndMap("rounded-md").contains("6px"), resolveAndMap("rounded-md"));
    assertTrue(resolveAndMap("rounded-lg").contains("8px"), resolveAndMap("rounded-lg"));
    assertTrue(resolveAndMap("rounded-xl").contains("12px"), resolveAndMap("rounded-xl"));
    assertTrue(resolveAndMap("rounded-2xl").contains("16px"), resolveAndMap("rounded-2xl"));
    assertTrue(resolveAndMap("rounded-3xl").contains("24px"), resolveAndMap("rounded-3xl"));
  }

  @Test
  void testRoundedFullIsPillRadius() {
    String result = resolveAndMap("rounded-full");
    assertNotNull(result);
    assertTrue(result.contains("-fx-background-radius: 9999px"), result);
  }

  @Test
  void testShadowUsesFullJavaFxDropshadowSignature() {
    assertEquals(
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 2, 0, 0, 1);",
        resolveAndMap("shadow-sm"));
    assertEquals(
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);",
        resolveAndMap("shadow-md"));
    assertEquals(
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 10);",
        resolveAndMap("shadow-lg"));
    assertEquals(
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 25, 0, 0, 20);",
        resolveAndMap("shadow-xl"));
    assertEquals(
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 50, 0, 0, 25);",
        resolveAndMap("shadow-2xl"));
  }

  @Test
  void testRuntimeOnlyUtilitiesEmitNoCss() {
    // aspect-*, transition-*, duration-*, ease-* and animate-* have no real JavaFX CSS
    // equivalent; they are applied at runtime by AspectRatioProcessor / TransitionProcessor
    // and must not pollute the generated stylesheet with inert properties.
    assertNull(resolveAndMap("transition-all"));
    assertNull(resolveAndMap("duration-300"));
    assertNull(resolveAndMap("ease-in-out"));
    assertNull(resolveAndMap("animate-spin"));
    assertNull(resolveAndMap("aspect-square"));
  }

  // ---------------------------------------------------------------------------
  // Extended coverage: prefix -> property mapping and named-value resolution.
  // ---------------------------------------------------------------------------

  @Test
  void testMapRemainingPrefixes() {
    assertEquals("-fx-font-family", mapper.mapToCssProperty("font"));
    assertEquals("-fx-min-width", mapper.mapToCssProperty("min-w"));
    assertEquals("-fx-min-height", mapper.mapToCssProperty("min-h"));
    assertEquals("-fx-max-width", mapper.mapToCssProperty("max-w"));
    assertEquals("-fx-max-height", mapper.mapToCssProperty("max-h"));
    assertEquals("-fx-opacity", mapper.mapToCssProperty("opacity"));
    assertEquals("-fx-rotate", mapper.mapToCssProperty("rotate"));
    assertEquals("-fx-scale-x", mapper.mapToCssProperty("scale"));
    assertEquals("-fx-scale-y", mapper.mapToCssProperty("scale-y"));
    assertEquals("-fx-translate-x", mapper.mapToCssProperty("translate-x"));
    assertEquals("-fx-translate-y", mapper.mapToCssProperty("translate-y"));
    assertEquals("-fx-background-radius", mapper.mapToCssProperty("rounded"));
    assertEquals("-fx-effect", mapper.mapToCssProperty("shadow"));
    assertEquals("-fx-hgap", mapper.mapToCssProperty("gap"));
    assertEquals("-fx-vgap", mapper.mapToCssProperty("gap-y"));
    assertEquals("-fx-cursor", mapper.mapToCssProperty("cursor"));
    assertEquals("-fx-border-color", mapper.mapToCssProperty("border"));
  }

  @Test
  void testUnmappedPrefixesReturnNull() {
    // Utilities with no JavaFX CSS equivalent must return null to avoid invalid CSS.
    for (String p :
        new String[] {
          "pt", "top", "pr", "pb", "pl", "left",
          "visible", "hidden", "invisible", "overflow", "z", "resize",
          "skew-x", "skew-y", "blur", "brightness", "contrast", "grayscale", "invert", "sepia",
          "unknown-prefix"
        }) {
      assertNull(mapper.mapToCssProperty(p), "prefix should not map: " + p);
    }
  }

  @Test
  void testResolveNamedColors() {
    assertEquals("#ffffff", mapper.resolveNamedValue("bg", "white"));
    assertEquals("#000000", mapper.resolveNamedValue("bg", "black"));
    assertEquals("transparent", mapper.resolveNamedValue("bg", "transparent"));
    assertEquals("#ffffff", mapper.resolveNamedValue("text", "white"));
    assertEquals("#000000", mapper.resolveNamedValue("border", "black"));
    assertNull(mapper.resolveNamedValue("bg", "chartreuse"));
  }

  @Test
  void testResolveBorderStyles() {
    assertEquals("solid", mapper.resolveNamedValue("border", "solid"));
    assertEquals("dashed", mapper.resolveNamedValue("border", "dashed"));
    assertEquals("dotted", mapper.resolveNamedValue("border", "dotted"));
    assertEquals("none", mapper.resolveNamedValue("border", "none"));
    assertNull(mapper.resolveNamedValue("border", "wavy"));
  }

  @Test
  void testResolveCursors() {
    assertEquals("default", mapper.resolveNamedValue("cursor", "default"));
    assertEquals("hand", mapper.resolveNamedValue("cursor", "pointer"));
    assertEquals("text", mapper.resolveNamedValue("cursor", "text"));
    assertEquals("move", mapper.resolveNamedValue("cursor", "move"));
    assertEquals("wait", mapper.resolveNamedValue("cursor", "wait"));
    assertEquals("crosshair", mapper.resolveNamedValue("cursor", "crosshair"));
    assertEquals("open-hand", mapper.resolveNamedValue("cursor", "grab"));
    assertEquals("closed-hand", mapper.resolveNamedValue("cursor", "grabbing"));
    assertEquals("h-resize", mapper.resolveNamedValue("cursor", "col-resize"));
    assertEquals("v-resize", mapper.resolveNamedValue("cursor", "row-resize"));
    assertEquals("ne-resize", mapper.resolveNamedValue("cursor", "nesw-resize"));
    assertEquals("nw-resize", mapper.resolveNamedValue("cursor", "nwse-resize"));
    assertEquals("none", mapper.resolveNamedValue("cursor", "none"));
    assertNull(mapper.resolveNamedValue("cursor", "bogus"));
  }

  @Test
  void testResolveOverflowVisibilityResize() {
    assertEquals("hidden", mapper.resolveNamedValue("overflow", "hidden"));
    assertEquals("scroll", mapper.resolveNamedValue("overflow", "scroll"));
    assertEquals("auto", mapper.resolveNamedValue("overflow", "auto"));
    assertNull(mapper.resolveNamedValue("overflow", "clip"));

    assertEquals("visible", mapper.resolveNamedValue("visible", "visible"));
    assertEquals("hidden", mapper.resolveNamedValue("hidden", "hidden"));
    assertEquals("hidden", mapper.resolveNamedValue("invisible", "invisible"));

    assertEquals("none", mapper.resolveNamedValue("resize", "none"));
    assertEquals("horizontal", mapper.resolveNamedValue("resize", "x"));
    assertEquals("vertical", mapper.resolveNamedValue("resize", "y"));
    assertEquals("both", mapper.resolveNamedValue("resize", "both"));
    assertNull(mapper.resolveNamedValue("resize", "weird"));
  }

  @Test
  void testResolveDimensions() {
    assertEquals("-1", mapper.resolveNamedValue("w", "auto"));
    assertEquals("-1", mapper.resolveNamedValue("w", "min"));
    assertEquals("-1", mapper.resolveNamedValue("h", "max"));
    assertNull(mapper.resolveNamedValue("w", "huge"));
  }

  @Test
  void testResolveMaxWidth() {
    assertEquals("320px", mapper.resolveNamedValue("max-w", "xs"));
    assertEquals("384px", mapper.resolveNamedValue("max-w", "sm"));
    assertEquals("448px", mapper.resolveNamedValue("max-w", "md"));
    assertEquals("512px", mapper.resolveNamedValue("max-w", "lg"));
    assertEquals("576px", mapper.resolveNamedValue("max-w", "xl"));
    assertEquals("672px", mapper.resolveNamedValue("max-w", "2xl"));
    assertEquals("768px", mapper.resolveNamedValue("max-w", "3xl"));
    // "full" is not expressible as a JavaFX CSS length.
    assertNull(mapper.resolveNamedValue("max-w", "full"));
  }

  @Test
  void testResolveEffects() {
    assertEquals("0px", mapper.resolveNamedValue("blur", "none"));
    assertEquals("4px", mapper.resolveNamedValue("blur", "default"));
    assertEquals("32px", mapper.resolveNamedValue("blur", "3xl"));
    assertEquals("10px", mapper.resolveNamedValue("blur", "10"));
    assertNull(mapper.resolveNamedValue("blur", "nope"));

    assertEquals("0%", mapper.resolveNamedValue("brightness", "0"));
    assertEquals("150%", mapper.resolveNamedValue("brightness", "150"));
    assertEquals("123%", mapper.resolveNamedValue("brightness", "123"));

    assertEquals("200%", mapper.resolveNamedValue("contrast", "200"));
    assertEquals("75%", mapper.resolveNamedValue("grayscale", "75"));
    assertNull(mapper.resolveNamedValue("grayscale", "x"));
    assertEquals("40%", mapper.resolveNamedValue("invert", "40"));
    assertNull(mapper.resolveNamedValue("invert", "x"));
    assertEquals("60%", mapper.resolveNamedValue("sepia", "60"));
    assertNull(mapper.resolveNamedValue("sepia", "x"));
  }

  @Test
  void testResolveSkew() {
    assertEquals("0", mapper.resolveNamedValue("skew-x", "0"));
    assertEquals("0.0175", mapper.resolveNamedValue("skew-x", "1"));
    assertEquals("0.2126", mapper.resolveNamedValue("skew-y", "12"));
    assertEquals("0.1051", mapper.resolveNamedValue("skew-y", "6"));
    // Numeric degrees go through tan(radians).
    assertTrue(mapper.resolveNamedValue("skew-x", "45").startsWith("0.99"));
    assertNull(mapper.resolveNamedValue("skew-x", "abc"));
  }

  @Test
  void testResolveFontSizeAndWeight() {
    assertEquals("14px", mapper.resolveNamedValue("text", "sm"));
    assertEquals("36px", mapper.resolveNamedValue("text", "4xl"));
    assertNull(mapper.resolveNamedValue("text", "chartreuse"));

    assertEquals("100", mapper.resolveNamedValue("font", "thin"));
    assertEquals("900", mapper.resolveNamedValue("font", "black"));
    assertNull(mapper.resolveNamedValue("font", "bogus"));
  }

  @Test
  void testMapBorderWidthsAndStyles() {
    assertEquals("-fx-border-width: 1px;", resolveAndMap("border"));
    assertEquals("-fx-border-width: 2px;", resolveAndMap("border-2"));
    assertTrue(resolveAndMap("border-t-4").startsWith("-fx-border-width:"));
    assertTrue(resolveAndMap("border-x-2").contains("2px"));
    assertEquals("-fx-border-style: solid;", resolveAndMap("border-solid"));
    assertEquals("-fx-border-color: #000000;", resolveAndMap("border-black"));
  }

  @Test
  void testMapFontSizeAndWeightTokens() {
    assertEquals("-fx-font-size: 16px;", resolveAndMap("text-base"));
    assertEquals("-fx-font-weight: 700;", resolveAndMap("font-bold"));
    assertEquals("-fx-text-fill: #ffffff;", resolveAndMap("text-white"));
  }

  @Test
  void testMapRotateAndTranslate() {
    assertEquals("-fx-rotate: 45;", resolveAndMap("rotate-45"));
    assertEquals("-fx-translate-x: 16px;", resolveAndMap("translate-x-4"));
    assertEquals("-fx-translate-y: 16px;", resolveAndMap("translate-y-4"));
  }

  @Test
  void testMapPaddingAxisForms() {
    assertEquals("-fx-padding: 0px 16px 0px 16px;", resolveAndMap("px-4"));
    assertEquals("-fx-padding: 16px 0px 16px 0px;", resolveAndMap("py-4"));
    assertEquals("-fx-padding: 16px 0px 0px 0px;", resolveAndMap("pt-4"));
    assertEquals("-fx-padding: 0px 16px 0px 0px;", resolveAndMap("pr-4"));
    assertEquals("-fx-padding: 0px 0px 16px 0px;", resolveAndMap("pb-4"));
    assertEquals("-fx-padding: 0px 0px 0px 16px;", resolveAndMap("pl-4"));
  }

  @Test
  void testMapDimensionAndMaxWidthTokens() {
    assertEquals("-fx-pref-width: -1;", resolveAndMap("w-auto"));
    assertEquals("-fx-pref-height: -1;", resolveAndMap("h-auto"));
    assertEquals("-fx-max-width: 448px;", resolveAndMap("max-w-md"));
  }

  @Test
  void testMapNullInputs() {
    assertNull(mapper.map(null, "10px"));
    assertNull(mapper.map(StyleToken.parse("p-4"), null));
  }
}
