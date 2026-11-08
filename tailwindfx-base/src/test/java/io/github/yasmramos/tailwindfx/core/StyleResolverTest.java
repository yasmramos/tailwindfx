package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.style.StyleToken;
import io.github.yasmramos.tailwindfx.theme.ThemeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for StyleResolver. */
class StyleResolverTest {

  private StyleResolver resolver;
  private ThemeConfig themeConfig;

  @BeforeEach
  void setUp() {
    themeConfig = ThemeConfig.defaultConfig();
    resolver = new StyleResolver(themeConfig);
  }

  @Test
  void testResolveScaleToken() {
    StyleToken token = StyleToken.parse("p-4");
    String result = resolver.resolve(token);

    assertNotNull(result);
    assertEquals("16px", result);
  }

  @Test
  void testResolveScaleTokenWithMultiplier() {
    StyleToken token = StyleToken.parse("p-8");
    String result = resolver.resolve(token);

    assertNotNull(result);
    assertEquals("32px", result);
  }

  @Test
  void testResolveColorToken() {
    StyleToken token = StyleToken.parse("bg-blue-500");
    String result = resolver.resolve(token);

    assertNotNull(result);
    // It must return the direct color value (hex) so JavaFX can convert it to a
    // Paint in inline styles. References to CSS variables (-color-blue-500) do
    // not work in inline styles.
    assertEquals("#3b82f6", result);
  }

  @Test
  void testResolveColorWithOpacity() {
    StyleToken token = StyleToken.parse("bg-red-500/50");
    String result = resolver.resolve(token);

    assertNotNull(result);
    assertTrue(result.startsWith("rgba("));
    assertTrue(result.contains("0.50"));
  }

  @Test
  void testResolveArbitraryValue() {
    StyleToken token = StyleToken.parse("w-[320px]");
    String result = resolver.resolve(token);

    assertNotNull(result);
    assertEquals("320px", result);
  }

  @Test
  void testResolveNamedFontSize() {
    StyleToken token = StyleToken.parse("text-lg");
    String result = resolver.resolve(token);

    assertNotNull(result);
    assertTrue(result.endsWith("px"));
  }

  @Test
  void testResolveUnknownToken() {
    StyleToken token = StyleToken.parse("unknown-token");
    String result = resolver.resolve(token);

    assertNull(result);
  }

  @Test
  void testResolveNullToken() {
    String result = resolver.resolve(null);
    assertNull(result);
  }
}
