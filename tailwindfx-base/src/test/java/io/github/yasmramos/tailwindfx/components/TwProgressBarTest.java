package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.control.ProgressBar;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for TwProgressBar component.
 */
public class TwProgressBarTest {

  @Test
  public void testCreate_DefaultProgressBar() {
    ProgressBar bar = TwProgressBar.create(0.5);
    
    assertNotNull(bar);
    assertEquals(0.5, bar.getProgress(), 0.01);
    assertTrue(bar.getStyleClass().contains("progress-bar"));
    assertTrue(bar.getStyleClass().contains("progress-blue"));
  }

  @Test
  public void testCreate_ZeroProgress() {
    ProgressBar bar = TwProgressBar.create(0.0);
    
    assertNotNull(bar);
    assertEquals(0.0, bar.getProgress(), 0.01);
  }

  @Test
  public void testCreate_FullProgress() {
    ProgressBar bar = TwProgressBar.create(1.0);
    
    assertNotNull(bar);
    assertEquals(1.0, bar.getProgress(), 0.01);
  }

  @Test
  public void testColor_CustomColor() {
    ProgressBar bar = TwProgressBar.color(0.75, "red");
    
    assertNotNull(bar);
    assertEquals(0.75, bar.getProgress(), 0.01);
    assertTrue(bar.getStyleClass().contains("progress-red"));
  }

  @Test
  public void testSuccess_GreenColor() {
    ProgressBar bar = TwProgressBar.success(0.6);
    
    assertNotNull(bar);
    assertEquals(0.6, bar.getProgress(), 0.01);
    assertTrue(bar.getStyleClass().contains("progress-green"));
  }

  @Test
  public void testWarning_YellowColor() {
    ProgressBar bar = TwProgressBar.warning(0.4);
    
    assertNotNull(bar);
    assertEquals(0.4, bar.getProgress(), 0.01);
    assertTrue(bar.getStyleClass().contains("progress-yellow"));
  }

  @Test
  public void testError_RedColor() {
    ProgressBar bar = TwProgressBar.error(0.2);
    
    assertNotNull(bar);
    assertEquals(0.2, bar.getProgress(), 0.01);
    assertTrue(bar.getStyleClass().contains("progress-red"));
  }

  @Test
  public void testStriped_WithStripedStyle() {
    ProgressBar bar = TwProgressBar.striped(0.8);
    
    assertNotNull(bar);
    assertEquals(0.8, bar.getProgress(), 0.01);
    assertTrue(bar.getStyleClass().contains("striped"));
  }
}
