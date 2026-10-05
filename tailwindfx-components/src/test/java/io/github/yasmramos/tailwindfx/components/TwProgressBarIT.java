package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

/** Unit tests for TwProgressBar component. */
@ExtendWith(ApplicationExtension.class)
public class TwProgressBarIT {

  @Test
  public void testProgressBarCreation() {
    TwProgressBar bar = new TwProgressBar(0.5);

    assertNotNull(bar);
    assertEquals(0.5, bar.getProgress(), 0.01);
    assertTrue(bar.getStyleClass().contains("progress-bar"));
  }

  @Test
  public void testProgressBarZeroProgress() {
    TwProgressBar bar = new TwProgressBar(0.0);

    assertNotNull(bar);
    assertEquals(0.0, bar.getProgress(), 0.01);
    assertTrue(bar.getStyleClass().contains("progress-bar"));
  }

  @Test
  public void testProgressBarFullProgress() {
    TwProgressBar bar = new TwProgressBar(1.0);

    assertNotNull(bar);
    assertEquals(1.0, bar.getProgress(), 0.01);
  }

  @Test
  public void testProgressBarSetColor() {
    TwProgressBar bar = new TwProgressBar(0.5);
    bar.setColor("green");

    assertTrue(bar.getStyleClass().contains("progress-green"));
    assertEquals("green", bar.getColor());
  }

  @Test
  public void testProgressBarSetColorMultipleTimes() {
    TwProgressBar bar = new TwProgressBar(0.5);
    bar.setColor("blue");
    bar.setColor("red");

    assertFalse(bar.getStyleClass().contains("progress-blue"));
    assertTrue(bar.getStyleClass().contains("progress-red"));
    assertEquals("red", bar.getColor());
  }

  @Test
  public void testProgressBarSetStriped() {
    TwProgressBar bar = new TwProgressBar(0.5);
    bar.setStriped(true);

    assertTrue(bar.isStriped());
    assertTrue(bar.getStyleClass().contains("progress-striped"));
  }

  @Test
  public void testProgressBarUnsetStriped() {
    TwProgressBar bar = new TwProgressBar(0.5);
    bar.setStriped(true);
    bar.setStriped(false);

    assertFalse(bar.isStriped());
    assertFalse(bar.getStyleClass().contains("progress-striped"));
  }

  @Test
  public void testSuccessProgressBar() {
    TwProgressBar bar = TwProgressBar.success(0.75);

    assertNotNull(bar);
    assertEquals(0.75, bar.getProgress(), 0.01);
    assertTrue(bar.getStyleClass().contains("progress-green"));
    assertEquals("green", bar.getColor());
  }

  @Test
  public void testWarningProgressBar() {
    TwProgressBar bar = TwProgressBar.warning(0.3);

    assertNotNull(bar);
    assertEquals(0.3, bar.getProgress(), 0.01);
    assertTrue(bar.getStyleClass().contains("progress-yellow"));
    assertEquals("yellow", bar.getColor());
  }

  @Test
  public void testErrorProgressBar() {
    TwProgressBar bar = TwProgressBar.error(0.1);

    assertNotNull(bar);
    assertEquals(0.1, bar.getProgress(), 0.01);
    assertTrue(bar.getStyleClass().contains("progress-red"));
    assertEquals("red", bar.getColor());
  }

  @Test
  public void testStripedProgressBar() {
    TwProgressBar bar = TwProgressBar.striped(0.6);

    assertNotNull(bar);
    assertEquals(0.6, bar.getProgress(), 0.01);
    assertTrue(bar.isStriped());
    assertTrue(bar.getStyleClass().contains("progress-striped"));
  }

  @Test
  public void testProgressBarColorRemovesOldColorClasses() {
    TwProgressBar bar = new TwProgressBar(0.5);
    bar.setColor("blue");
    bar.setColor("green");

    // Should only have progress-green, not progress-blue
    assertTrue(bar.getStyleClass().contains("progress-green"));
    // Verify old color class is removed
    for (String styleClass : bar.getStyleClass()) {
      assertFalse(
          styleClass.startsWith("progress-")
              && !styleClass.equals("progress-bar")
              && !styleClass.equals("progress-green"),
          "Should not have other progress color classes");
    }
  }

  @Test
  public void testProgressBarWithNullColor() {
    TwProgressBar bar = new TwProgressBar(0.5);
    bar.setColor(null);

    assertEquals(null, bar.getColor());
    // Should not add progress-null class
    assertFalse(bar.getStyleClass().contains("progress-null"));
  }

  @Test
  public void testProgressBarWithEmptyColor() {
    TwProgressBar bar = new TwProgressBar(0.5);
    bar.setColor("");

    assertEquals("", bar.getColor());
    assertFalse(bar.getStyleClass().contains("progress-"));
  }

  @Test
  public void testProgressBarStripedAndColored() {
    TwProgressBar bar = new TwProgressBar(0.8);
    bar.setColor("purple");
    bar.setStriped(true);

    assertTrue(bar.getStyleClass().contains("progress-purple"));
    assertTrue(bar.getStyleClass().contains("progress-striped"));
    assertTrue(bar.isStriped());
    assertEquals("purple", bar.getColor());
  }

  @Test
  public void testSuccessProgressBarIsNotStriped() {
    TwProgressBar bar = TwProgressBar.success(0.5);

    assertFalse(bar.isStriped());
    assertFalse(bar.getStyleClass().contains("progress-striped"));
  }

  @Test
  public void testWarningProgressBarHasCorrectProgress() {
    TwProgressBar bar = TwProgressBar.warning(0.45);

    assertEquals(0.45, bar.getProgress(), 0.01);
  }

  @Test
  public void testErrorProgressBarHasCorrectProgress() {
    TwProgressBar bar = TwProgressBar.error(0.9);

    assertEquals(0.9, bar.getProgress(), 0.01);
  }

  @Test
  public void testStripedProgressBarDefaultColor() {
    TwProgressBar bar = TwProgressBar.striped(0.5);

    // Default color should be blue (from constructor)
    assertEquals("blue", bar.getColor());
  }
}
