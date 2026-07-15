package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.control.ProgressIndicator;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for TwSpinner component.
 */
public class TwSpinnerTest {

  @Test
  public void testCreate_DefaultSpinner() {
    ProgressIndicator spinner = TwSpinner.create();
    
    assertNotNull(spinner);
    assertTrue(spinner.isIndeterminate());
    assertTrue(spinner.getStyleClass().contains("spinner"));
    assertTrue(spinner.getStyleClass().contains("spinner-md"));
  }

  @Test
  public void testXs_ExtraSmallSpinner() {
    ProgressIndicator spinner = TwSpinner.xs();
    
    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner-xs"));
  }

  @Test
  public void testSm_SmallSpinner() {
    ProgressIndicator spinner = TwSpinner.sm();
    
    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner-sm"));
  }

  @Test
  public void testMd_MediumSpinner() {
    ProgressIndicator spinner = TwSpinner.md();
    
    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner-md"));
  }

  @Test
  public void testLg_LargeSpinner() {
    ProgressIndicator spinner = TwSpinner.lg();
    
    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner-lg"));
  }

  @Test
  public void testXl_ExtraLargeSpinner() {
    ProgressIndicator spinner = TwSpinner.xl();
    
    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner-xl"));
  }

  @Test
  public void testColored_WithCustomColor() {
    ProgressIndicator spinner = TwSpinner.colored("blue");
    
    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner-blue"));
  }

  @Test
  public void testColored_RedColor() {
    ProgressIndicator spinner = TwSpinner.colored("red");
    
    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner-red"));
  }

  @Test
  public void testSize_CustomSize() {
    ProgressIndicator spinner = TwSpinner.size("2xl");
    
    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner-2xl"));
  }
}
