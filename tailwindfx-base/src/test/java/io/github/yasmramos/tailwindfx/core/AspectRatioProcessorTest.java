package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Unit tests for AspectRatioProcessor. */
class AspectRatioProcessorTest {

  @Test
  void testIsAspectRatioTokenTrue() {
    assertTrue(AspectRatioProcessor.isAspectRatioToken("aspect-square"));
    assertTrue(AspectRatioProcessor.isAspectRatioToken("aspect-video"));
    assertTrue(AspectRatioProcessor.isAspectRatioToken("aspect-[4/3]"));
    assertTrue(AspectRatioProcessor.isAspectRatioToken("aspect-auto"));
  }

  @Test
  void testIsAspectRatioTokenFalse() {
    assertFalse(AspectRatioProcessor.isAspectRatioToken("w-full"));
    assertFalse(AspectRatioProcessor.isAspectRatioToken("bg-blue-500"));
    assertFalse(AspectRatioProcessor.isAspectRatioToken(null));
    assertFalse(AspectRatioProcessor.isAspectRatioToken(""));
    assertFalse(AspectRatioProcessor.isAspectRatioToken("   "));
  }

  @Test
  void testProcessAspectRatioSquare() {
    String css = AspectRatioProcessor.processAspectRatio("aspect-square");
    assertNotNull(css);
    assertTrue(css.contains("-fx-aspect-ratio"));
    assertTrue(css.contains("1.0"));
  }

  @Test
  void testProcessAspectRatioVideo() {
    String css = AspectRatioProcessor.processAspectRatio("aspect-video");
    assertNotNull(css);
    assertTrue(css.contains("-fx-aspect-ratio"));
    assertTrue(css.contains("1.777")); // 16/9 ≈ 1.7778
  }

  @Test
  void testProcessAspectRatioAuto() {
    String css = AspectRatioProcessor.processAspectRatio("aspect-auto");
    assertNotNull(css);
    assertTrue(css.contains("-fx-pref-width"));
    assertTrue(css.contains("-fx-pref-height"));
  }

  @Test
  void testProcessAspectRatioPortrait() {
    String css = AspectRatioProcessor.processAspectRatio("aspect-portrait");
    assertNotNull(css);
    assertTrue(css.contains("-fx-aspect-ratio"));
    assertTrue(css.contains("0.75")); // 3/4 = 0.75
  }

  @Test
  void testProcessAspectRatioLandscape() {
    String css = AspectRatioProcessor.processAspectRatio("aspect-landscape");
    assertNotNull(css);
    assertTrue(css.contains("-fx-aspect-ratio"));
    assertTrue(css.contains("1.333")); // 4/3 ≈ 1.3333
  }

  @Test
  void testProcessAspectRatioArbitrary() {
    String css = AspectRatioProcessor.processAspectRatio("aspect-[4/3]");
    assertNotNull(css);
    assertTrue(css.contains("-fx-aspect-ratio"));
    assertTrue(css.contains("1.333"));
  }

  @Test
  void testProcessAspectRatioArbitraryDecimal() {
    String css = AspectRatioProcessor.processAspectRatio("aspect-[1.5]");
    assertNotNull(css);
    assertTrue(css.contains("-fx-aspect-ratio"));
    assertTrue(css.contains("1.5"));
  }

  @Test
  void testProcessNull() {
    String css = AspectRatioProcessor.processAspectRatio(null);
    assertNull(css);
  }

  @Test
  void testProcessEmpty() {
    String css = AspectRatioProcessor.processAspectRatio("");
    assertNull(css);
  }

  @Test
  void testProcessInvalid() {
    String css = AspectRatioProcessor.processAspectRatio("invalid-token");
    assertNull(css);
  }

  @Test
  void testGetNumericRatio() {
    assertEquals(1.0, AspectRatioProcessor.getNumericRatio("square"));
    assertEquals(16.0 / 9.0, AspectRatioProcessor.getNumericRatio("video"));
    assertEquals(3.0 / 4.0, AspectRatioProcessor.getNumericRatio("portrait"));
    assertEquals(4.0 / 3.0, AspectRatioProcessor.getNumericRatio("landscape"));
    assertEquals(-1.0, AspectRatioProcessor.getNumericRatio("unknown"));
  }

  @Test
  void testCalculateHeight() {
    double height = AspectRatioProcessor.calculateHeight(1600, 16.0 / 9.0);
    assertEquals(900, height, 0.01);

    double height2 = AspectRatioProcessor.calculateHeight(1000, 1.0);
    assertEquals(1000, height2, 0.01);
  }

  @Test
  void testCalculateWidth() {
    double width = AspectRatioProcessor.calculateWidth(900, 16.0 / 9.0);
    assertEquals(1600, width, 0.01);

    double width2 = AspectRatioProcessor.calculateWidth(1000, 1.0);
    assertEquals(1000, width2, 0.01);
  }

  @Test
  void testCalculateWithInvalidRatio() {
    double height = AspectRatioProcessor.calculateHeight(100, -1);
    assertEquals(100, height); // Defaults to square

    double width = AspectRatioProcessor.calculateWidth(100, 0);
    assertEquals(100, width); // Defaults to square
  }
}
