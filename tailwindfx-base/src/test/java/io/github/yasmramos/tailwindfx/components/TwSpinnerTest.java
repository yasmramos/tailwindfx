package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.control.ProgressIndicator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

/** Unit tests for TwSpinner component. */
@ExtendWith(ApplicationExtension.class)
public class TwSpinnerTest {

  @Test
  public void testCreateDefaultSpinner() {
    ProgressIndicator spinner = TwSpinner.create();

    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner"));
    assertTrue(spinner.getStyleClass().contains("spinner-md"));
  }

  @Test
  public void testSmallSpinner() {
    ProgressIndicator spinner = TwSpinner.small();

    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner"));
    assertTrue(spinner.getStyleClass().contains("spinner-sm"));
  }

  @Test
  public void testLargeSpinner() {
    ProgressIndicator spinner = TwSpinner.large();

    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner"));
    assertTrue(spinner.getStyleClass().contains("spinner-lg"));
  }

  @Test
  public void testSpinnerWithCustomSize() {
    ProgressIndicator spinner = TwSpinner.size("sm");

    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner"));
    assertTrue(spinner.getStyleClass().contains("spinner-sm"));
  }

  @Test
  public void testSpinnerWithCustomSizeMedium() {
    ProgressIndicator spinner = TwSpinner.size("md");

    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner-md"));
  }

  @Test
  public void testSpinnerWithCustomSizeLarge() {
    ProgressIndicator spinner = TwSpinner.size("lg");

    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner-lg"));
  }

  @Test
  public void testColoredSpinner() {
    ProgressIndicator spinner = TwSpinner.colored("red");

    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner"));
    assertTrue(spinner.getStyleClass().contains("spinner-md"));
    assertTrue(spinner.getStyleClass().contains("spinner-red"));
  }

  @Test
  public void testColoredSpinnerWithBlue() {
    ProgressIndicator spinner = TwSpinner.colored("blue");

    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner-blue"));
  }

  @Test
  public void testColoredSpinnerWithGreen() {
    ProgressIndicator spinner = TwSpinner.colored("green");

    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner-green"));
  }

  @Test
  public void testSmallColoredSpinner() {
    ProgressIndicator spinner = TwSpinner.smallColored("yellow");

    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner"));
    assertTrue(spinner.getStyleClass().contains("spinner-sm"));
    assertTrue(spinner.getStyleClass().contains("spinner-yellow"));
  }

  @Test
  public void testLargeColoredSpinner() {
    ProgressIndicator spinner = TwSpinner.largeColored("purple");

    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner"));
    assertTrue(spinner.getStyleClass().contains("spinner-lg"));
    assertTrue(spinner.getStyleClass().contains("spinner-purple"));
  }

  @Test
  public void testSmallColoredSpinnerWithOrange() {
    ProgressIndicator spinner = TwSpinner.smallColored("orange");

    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner-orange"));
  }

  @Test
  public void testLargeColoredSpinnerWithPink() {
    ProgressIndicator spinner = TwSpinner.largeColored("pink");

    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner-pink"));
  }

  @Test
  public void testAllSpinnersHaveBaseSpinnerClass() {
    ProgressIndicator[] spinners = {
      TwSpinner.create(),
      TwSpinner.small(),
      TwSpinner.large(),
      TwSpinner.size("md"),
      TwSpinner.colored("red"),
      TwSpinner.smallColored("blue"),
      TwSpinner.largeColored("green")
    };

    for (ProgressIndicator spinner : spinners) {
      assertTrue(
          spinner.getStyleClass().contains("spinner"), "Spinner should have 'spinner' base class");
    }
  }

  @Test
  public void testSpinnerWithNullColor() {
    ProgressIndicator spinner = TwSpinner.colored(null);

    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner-null"));
  }

  @Test
  public void testSpinnerWithEmptyColor() {
    ProgressIndicator spinner = TwSpinner.colored("");

    assertNotNull(spinner);
    assertTrue(spinner.getStyleClass().contains("spinner-"));
  }
}
