package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.TailwindFX;
import javafx.scene.control.ProgressIndicator;

/**
 * TwSpinner — Loading spinner component using ProgressIndicator.
 *
 * <p>Uses base ProgressIndicator styles from tailwindfx-components.css.
 *
 * <pre>
 * Node spinner = TwSpinner.create();
 * Node spinner = TwSpinner.small();
 * Node spinner = TwSpinner.large();
 * Node spinner = TwSpinner.colored("red");
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
   * Creates an extra small spinner.
   *
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator xs() {
    return size("xs");
  }

  /**
   * Creates a small spinner.
   *
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator sm() {
    return size("sm");
  }

  /**
   * Creates a medium spinner (default).
   *
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator md() {
    return size("md");
  }

  /**
   * Creates a large spinner.
   *
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator lg() {
    return size("lg");
  }

  /**
   * Creates an extra large spinner.
   *
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator xl() {
    return size("xl");
  }

  /**
   * Creates a spinner with custom size.
   *
   * @param size Tailwind size modifier (xs, sm, md, lg, xl)
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator size(String size) {
    ProgressIndicator spinner = new ProgressIndicator();
    TailwindFX.apply(spinner, "spinner", "spinner-" + size);
    return spinner;
  }

  /**
   * Creates a spinner with custom color.
   *
   * @param color Tailwind color name (e.g. "blue", "green", "red")
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator colored(String color) {
    ProgressIndicator spinner = new ProgressIndicator();
    TailwindFX.apply(spinner, "spinner", "spinner-md", "spinner-" + color);
    return spinner;
  }

  /**
   * Creates a colored spinner with custom size.
   *
   * @param color Tailwind color name
   * @param size Tailwind size modifier
   * @return styled ProgressIndicator
   */
  public static ProgressIndicator colored(String color, String size) {
    ProgressIndicator spinner = new ProgressIndicator();
    TailwindFX.apply(spinner, "spinner", "spinner-" + size, "spinner-" + color);
    return spinner;
  }
}
