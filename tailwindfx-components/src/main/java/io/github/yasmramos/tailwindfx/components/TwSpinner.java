package io.github.yasmramos.tailwindfx.components;

import javafx.scene.control.ProgressIndicator;

/**
 * TwSpinner — Loading spinner component using ProgressIndicator.
 *
 * <p>Utility class for creating styled ProgressIndicator spinners.
 *
 * <pre>
 * ProgressIndicator spinner = TwSpinner.create();
 * ProgressIndicator spinner = TwSpinner.small();
 * ProgressIndicator spinner = TwSpinner.large();
 * ProgressIndicator spinner = TwSpinner.colored("red");
 * </pre>
 */
public final class TwSpinner {

  /** Base stylesheet class applied to every spinner. */
  private static final String BASE_CLASS = "spinner";

  /** Prefix shared by the size and color modifier classes. */
  private static final String MODIFIER_PREFIX = "spinner-";

  private TwSpinner() {}

  /**
   * Creates a default sized spinner.
   *
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator create() {
    return size("md");
  }

  /**
   * Creates a small spinner.
   *
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator small() {
    return size("sm");
  }

  /**
   * Creates a large spinner.
   *
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator large() {
    return size("lg");
  }

  /**
   * Creates a spinner with custom size.
   *
   * @param size size variant (sm, md, lg)
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator size(String size) {
    ProgressIndicator spinner = new ProgressIndicator();
    spinner.getStyleClass().add(BASE_CLASS);
    spinner.getStyleClass().add(MODIFIER_PREFIX + size);
    return spinner;
  }

  /**
   * Creates a spinner with custom color.
   *
   * @param color Tailwind color name
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator colored(String color) {
    return sizeAndColor("md", color);
  }

  /**
   * Creates a small spinner with custom color.
   *
   * @param color Tailwind color name
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator smallColored(String color) {
    return sizeAndColor("sm", color);
  }

  /**
   * Creates a large spinner with custom color.
   *
   * @param color Tailwind color name
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator largeColored(String color) {
    return sizeAndColor("lg", color);
  }

  /**
   * Creates a spinner with both a size and a color modifier.
   *
   * @param size size variant (sm, md, lg)
   * @param color Tailwind color name
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator sizeAndColor(String size, String color) {
    ProgressIndicator spinner = new ProgressIndicator();
    spinner.getStyleClass().add(BASE_CLASS);
    spinner.getStyleClass().add(MODIFIER_PREFIX + size);
    spinner.getStyleClass().add(MODIFIER_PREFIX + color);
    return spinner;
  }
}
