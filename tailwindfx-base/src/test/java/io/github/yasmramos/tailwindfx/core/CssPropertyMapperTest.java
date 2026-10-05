package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.style.StyleToken;
import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests unitarios para CssPropertyMapper. */
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
}
