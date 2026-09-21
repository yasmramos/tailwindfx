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
    spinner.getStyleClass().addAll("spinner", "spinner-" + size);
    return spinner;
  }

  /**
   * Creates a spinner with custom color.
   *
   * @param color Tailwind color name
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator colored(String color) {
    ProgressIndicator spinner = new ProgressIndicator();
    spinner.getStyleClass().addAll("spinner", "spinner-md", "spinner-" + color);
    return spinner;
  }

  /**
   * Creates a small spinner with custom color.
   *
   * @param color Tailwind color name
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator smallColored(String color) {
    ProgressIndicator spinner = new ProgressIndicator();
    spinner.getStyleClass().addAll("spinner", "spinner-sm", "spinner-" + color);
    return spinner;
  }

  /**
   * Creates a large spinner with custom color.
   *
   * @param color Tailwind color name
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator largeColored(String color) {
    ProgressIndicator spinner = new ProgressIndicator();
    spinner.getStyleClass().addAll("spinner", "spinner-lg", "spinner-" + color);
    return spinner;
  }
}
