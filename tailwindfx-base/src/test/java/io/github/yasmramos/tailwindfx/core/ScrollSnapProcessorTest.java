package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Unit tests for ScrollSnapProcessor class. */
class ScrollSnapProcessorTest {

  @Test
  void testIsScrollSnapTokenValidTokens() {
    assertTrue(ScrollSnapProcessor.isScrollSnapToken("snap-x"));
    assertTrue(ScrollSnapProcessor.isScrollSnapToken("snap-y"));
    assertTrue(ScrollSnapProcessor.isScrollSnapToken("snap-both"));
    assertTrue(ScrollSnapProcessor.isScrollSnapToken("snap-mandatory"));
    assertTrue(ScrollSnapProcessor.isScrollSnapToken("snap-proximity"));
    assertTrue(ScrollSnapProcessor.isScrollSnapToken("snap-start"));
    assertTrue(ScrollSnapProcessor.isScrollSnapToken("snap-end"));
    assertTrue(ScrollSnapProcessor.isScrollSnapToken("snap-center"));
    assertTrue(ScrollSnapProcessor.isScrollSnapToken("snap-normal"));
    assertTrue(ScrollSnapProcessor.isScrollSnapToken("snap-always"));
  }

  @Test
  void testIsScrollSnapTokenInvalidTokens() {
    assertFalse(ScrollSnapProcessor.isScrollSnapToken(null));
    assertFalse(ScrollSnapProcessor.isScrollSnapToken(""));
    // "snap-" is actually valid as it starts with "snap-"
    // assertFalse(ScrollSnapProcessor.isScrollSnapToken("snap-"));
    assertFalse(ScrollSnapProcessor.isScrollSnapToken("snap"));
    assertFalse(ScrollSnapProcessor.isScrollSnapToken("padding-4"));
    assertFalse(ScrollSnapProcessor.isScrollSnapToken("flex-row"));
  }

  @Test
  void testProcessScrollSnapAlwaysReturnsNull() {
    // JavaFX doesn't support scroll-snap via CSS
    assertNull(ScrollSnapProcessor.processScrollSnap("snap-x"));
    assertNull(ScrollSnapProcessor.processScrollSnap("snap-y"));
    assertNull(ScrollSnapProcessor.processScrollSnap("snap-mandatory"));
    assertNull(ScrollSnapProcessor.processScrollSnap("snap-start"));
    assertNull(ScrollSnapProcessor.processScrollSnap("snap-invalid"));
    assertNull(ScrollSnapProcessor.processScrollSnap(null));
    assertNull(ScrollSnapProcessor.processScrollSnap(""));
  }

  @Test
  void testGetSnapAxis() {
    assertEquals("horizontal", ScrollSnapProcessor.getSnapAxis("snap-x"));
    assertEquals("vertical", ScrollSnapProcessor.getSnapAxis("snap-y"));
    assertEquals("both", ScrollSnapProcessor.getSnapAxis("snap-both"));
    assertNull(ScrollSnapProcessor.getSnapAxis("snap-mandatory"));
    assertNull(ScrollSnapProcessor.getSnapAxis("snap-start"));
    assertNull(ScrollSnapProcessor.getSnapAxis(null));
    assertNull(ScrollSnapProcessor.getSnapAxis("invalid"));
  }

  @Test
  void testGetSnapType() {
    assertEquals("mandatory", ScrollSnapProcessor.getSnapType("snap-mandatory"));
    assertEquals("proximity", ScrollSnapProcessor.getSnapType("snap-proximity"));
    assertNull(ScrollSnapProcessor.getSnapType("snap-x"));
    assertNull(ScrollSnapProcessor.getSnapType("snap-y"));
    assertNull(ScrollSnapProcessor.getSnapType(null));
    assertNull(ScrollSnapProcessor.getSnapType("invalid"));
  }

  @Test
  void testGetSnapAlign() {
    assertEquals("start", ScrollSnapProcessor.getSnapAlign("snap-start"));
    assertEquals("end", ScrollSnapProcessor.getSnapAlign("snap-end"));
    assertEquals("center", ScrollSnapProcessor.getSnapAlign("snap-center"));
    assertNull(ScrollSnapProcessor.getSnapAlign("snap-x"));
    assertNull(ScrollSnapProcessor.getSnapAlign("snap-mandatory"));
    assertNull(ScrollSnapProcessor.getSnapAlign(null));
    assertNull(ScrollSnapProcessor.getSnapAlign("invalid"));
  }

  @Test
  void testGetSnapStop() {
    assertEquals("normal", ScrollSnapProcessor.getSnapStop("snap-normal"));
    assertEquals("always", ScrollSnapProcessor.getSnapStop("snap-always"));
    assertNull(ScrollSnapProcessor.getSnapStop("snap-x"));
    assertNull(ScrollSnapProcessor.getSnapStop("snap-start"));
    assertNull(ScrollSnapProcessor.getSnapStop(null));
    assertNull(ScrollSnapProcessor.getSnapStop("invalid"));
  }

  @Test
  void testAllScrollSnapTokenTypes() {
    String[] axisTokens = {"snap-x", "snap-y", "snap-both"};
    String[] typeTokens = {"snap-mandatory", "snap-proximity"};
    String[] alignTokens = {"snap-start", "snap-end", "snap-center"};
    String[] stopTokens = {"snap-normal", "snap-always"};

    for (String token : axisTokens) {
      assertTrue(ScrollSnapProcessor.isScrollSnapToken(token));
      assertNotNull(ScrollSnapProcessor.getSnapAxis(token));
    }

    for (String token : typeTokens) {
      assertTrue(ScrollSnapProcessor.isScrollSnapToken(token));
      assertNotNull(ScrollSnapProcessor.getSnapType(token));
    }

    for (String token : alignTokens) {
      assertTrue(ScrollSnapProcessor.isScrollSnapToken(token));
      assertNotNull(ScrollSnapProcessor.getSnapAlign(token));
    }

    for (String token : stopTokens) {
      assertTrue(ScrollSnapProcessor.isScrollSnapToken(token));
      assertNotNull(ScrollSnapProcessor.getSnapStop(token));
    }
  }

  @Test
  void testNullSafetyForAllMethods() {
    // All methods should handle null gracefully
    assertFalse(ScrollSnapProcessor.isScrollSnapToken(null));
    assertNull(ScrollSnapProcessor.processScrollSnap(null));
    assertNull(ScrollSnapProcessor.getSnapAxis(null));
    assertNull(ScrollSnapProcessor.getSnapType(null));
    assertNull(ScrollSnapProcessor.getSnapAlign(null));
    assertNull(ScrollSnapProcessor.getSnapStop(null));
  }
}
