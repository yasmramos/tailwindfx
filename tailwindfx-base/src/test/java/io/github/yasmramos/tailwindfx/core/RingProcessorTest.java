package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.style.StyleToken;
import org.junit.jupiter.api.Test;

/** Unit tests for RingProcessor. */
class RingProcessorTest {

  @Test
  void testIsRingTokenTrue() {
    assertTrue(RingProcessor.isRingToken("ring-1"));
    assertTrue(RingProcessor.isRingToken("ring-2"));
    assertTrue(RingProcessor.isRingToken("ring-blue-500"));
    assertTrue(RingProcessor.isRingToken("ring-offset-2"));
    assertTrue(RingProcessor.isRingToken("ring-opacity-50"));
  }

  @Test
  void testIsRingTokenFalse() {
    assertFalse(RingProcessor.isRingToken("border-2"));
    assertFalse(RingProcessor.isRingToken("bg-blue-500"));
    assertFalse(RingProcessor.isRingToken(null));
    assertFalse(RingProcessor.isRingToken(""));
    assertFalse(RingProcessor.isRingToken("   "));
  }

  @Test
  void testProcessRingWidthScale() {
    StyleToken token1 = StyleToken.parse("ring-1");
    String css1 = RingProcessor.processRingToken(token1);
    assertNotNull(css1);
    assertTrue(css1.contains("-fx-border-width"));

    StyleToken token2 = StyleToken.parse("ring-2");
    String css2 = RingProcessor.processRingToken(token2);
    assertNotNull(css2);
    assertTrue(css2.contains("-fx-border-width"));

    StyleToken token0 = StyleToken.parse("ring-0");
    String css0 = RingProcessor.processRingToken(token0);
    assertNotNull(css0);
    assertTrue(css0.contains("0px"));
  }

  @Test
  void testProcessRingColorShade() {
    StyleToken token = StyleToken.parse("ring-blue-500");
    String css = RingProcessor.processRingToken(token);
    assertNotNull(css);
    assertTrue(css.contains("-fx-border-color"));
  }

  @Test
  void testProcessRingColorWithAlpha() {
    StyleToken token = StyleToken.parse("ring-red-500/50");
    String css = RingProcessor.processRingToken(token);
    assertNotNull(css);
    assertTrue(css.contains("rgba") || css.contains("-fx-border-color"));
  }

  @Test
  void testProcessRingNamedColor() {
    StyleToken tokenWhite = StyleToken.parse("ring-white");
    String cssWhite = RingProcessor.processRingToken(tokenWhite);
    assertNotNull(cssWhite);
    assertTrue(cssWhite.contains("#ffffff"));

    StyleToken tokenBlack = StyleToken.parse("ring-black");
    String cssBlack = RingProcessor.processRingToken(tokenBlack);
    assertNotNull(cssBlack);
    assertTrue(cssBlack.contains("#000000"));

    StyleToken tokenTransparent = StyleToken.parse("ring-transparent");
    String cssTransparent = RingProcessor.processRingToken(tokenTransparent);
    assertNotNull(cssTransparent);
    assertTrue(cssTransparent.contains("transparent"));
  }

  @Test
  void testProcessRingArbitraryColor() {
    StyleToken token = StyleToken.parse("ring-[#ff0000]");
    String css = RingProcessor.processRingToken(token);
    assertNotNull(css);
    assertTrue(css.contains("#ff0000"));
  }

  @Test
  void testProcessRingArbitraryWidth() {
    StyleToken token = StyleToken.parse("ring-[3px]");
    String css = RingProcessor.processRingToken(token);
    assertNotNull(css);
    assertTrue(css.contains("3px"));
  }

  @Test
  void testProcessRingOffsetWidth() {
    StyleToken token = StyleToken.parse("ring-offset-2");
    String css = RingProcessor.processRingToken(token);
    assertNotNull(css);
    assertTrue(css.contains("-fx-padding"));
  }

  @Test
  void testProcessRingOffsetColor() {
    StyleToken token = StyleToken.parse("ring-offset-blue-500");
    String css = RingProcessor.processRingToken(token);
    assertNotNull(css);
    assertTrue(css.contains("-fx-background-color"));
  }

  @Test
  void testProcessRingOpacity() {
    StyleToken token = StyleToken.parse("ring-opacity-50");
    String css = RingProcessor.processRingToken(token);
    assertNotNull(css);
    assertTrue(css.contains("-fx-opacity"));
    assertTrue(css.contains("0.5"));
  }

  @Test
  void testProcessNonRingToken() {
    StyleToken token = StyleToken.parse("border-2");
    String css = RingProcessor.processRingToken(token);
    assertNull(css);

    StyleToken token2 = StyleToken.parse("bg-blue-500");
    String css2 = RingProcessor.processRingToken(token2);
    assertNull(css2);
  }

  @Test
  void testProcessNullToken() {
    String css = RingProcessor.processRingToken(null);
    assertNull(css);
  }

  @Test
  void testGetDefaultRingWidth() {
    assertEquals(0, RingProcessor.getDefaultRingWidth(0));
    assertEquals(1, RingProcessor.getDefaultRingWidth(1));
    assertEquals(2, RingProcessor.getDefaultRingWidth(2));
    assertEquals(4, RingProcessor.getDefaultRingWidth(4));
    assertEquals(8, RingProcessor.getDefaultRingWidth(8));
    assertEquals(16, RingProcessor.getDefaultRingWidth(16)); // Non-standard
  }

  @Test
  void testNegativeRingReturnsNull() {
    StyleToken token = StyleToken.parse("-ring-2");
    String css = RingProcessor.processRingToken(token);
    // Negative rings should return null or be handled gracefully
    // Depending on implementation, might return null or ignore negative
    assertTrue(css == null || css.isEmpty());
  }

  @Test
  void testRingCurrentColor() {
    StyleToken token = StyleToken.parse("ring-current");
    String css = RingProcessor.processRingToken(token);
    assertNotNull(css);
    assertTrue(css.contains("derive(-fx-text-fill"));
  }
}
