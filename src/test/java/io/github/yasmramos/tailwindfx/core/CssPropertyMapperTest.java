package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

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
    assertEquals("-fx-margin", mapper.mapToCssProperty("m"));
    assertEquals("-fx-margin", mapper.mapToCssProperty("mx"));
    assertEquals("-fx-margin", mapper.mapToCssProperty("mt"));
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
}
