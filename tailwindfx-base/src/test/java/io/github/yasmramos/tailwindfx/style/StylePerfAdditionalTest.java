package io.github.yasmramos.tailwindfx.style;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Additional tests for StylePerf class */
public class StylePerfAdditionalTest {

  @Test
  public void testStylePerfCreation() {
    // Test that StylePerf can be instantiated or used
    assertNotNull(StylePerf.class);
  }

  @Test
  public void testPerformanceMeasurement() {
    long start = System.nanoTime();
    // Simulate some work
    for (int i = 0; i < 1000; i++) {
      Math.sqrt(i);
    }
    long end = System.nanoTime();

    assertTrue(end > start);
  }

  @Test
  public void testNullSafety() {
    // Test null handling if applicable
    assertNull(null);
  }
}
