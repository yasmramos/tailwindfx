package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.TailwindFX;
import javafx.scene.control.ProgressBar;

/**
 * TwProgressBar — Progress bar component with TailwindCSS variants.
 *
 * <p>Uses base ProgressBar styles from tailwindfx-components.css with utility modifiers.
 *
 * <pre>
 * ProgressBar bar = TwProgressBar.create(0.5);
 * ProgressBar bar = TwProgressBar.success(0.75);
 * ProgressBar bar = TwProgressBar.warning(0.3);
 * ProgressBar bar = TwProgressBar.error(0.1);
 * ProgressBar bar = TwProgressBar.striped(0.6);
 * </pre>
 */
public final class TwProgressBar {

  private TwProgressBar() {}

  /**
   * Creates a default progress bar with given progress.
   *
   * @param progress value between 0.0 and 1.0
   * @return styled ProgressBar
   */
  public static ProgressBar create(double progress) {
    return color(progress, "blue");
  }

  /**
   * Creates a progress bar with custom color.
   *
   * @param progress value between 0.0 and 1.0
   * @param color Tailwind color name (e.g. "blue", "green", "red")
   * @return styled ProgressBar
   */
  public static ProgressBar color(double progress, String color) {
    ProgressBar bar = new ProgressBar(progress);
    TailwindFX.apply(bar, "progress-bar", "progress-" + color);
    return bar;
  }

  /**
   * Creates a success progress bar (green).
   *
   * @param progress value between 0.0 and 1.0
   * @return styled ProgressBar
   */
  public static ProgressBar success(double progress) {
    return color(progress, "green");
  }

  /**
   * Creates a warning progress bar (yellow).
   *
   * @param progress value between 0.0 and 1.0
   * @return styled ProgressBar
   */
  public static ProgressBar warning(double progress) {
    return color(progress, "yellow");
  }

  /**
   * Creates an error/danger progress bar (red).
   *
   * @param progress value between 0.0 and 1.0
   * @return styled ProgressBar
   */
  public static ProgressBar error(double progress) {
    return color(progress, "red");
  }

  /**
   * Creates a striped progress bar.
   *
   * @param progress value between 0.0 and 1.0
   * @return styled ProgressBar
   */
  public static ProgressBar striped(double progress) {
    return striped(progress, "blue");
  }

  /**
   * Creates a striped progress bar with custom color.
   *
   * @param progress value between 0.0 and 1.0
   * @param color Tailwind color name
   * @return styled ProgressBar
   */
  public static ProgressBar striped(double progress, String color) {
    ProgressBar bar = new ProgressBar(progress);
    TailwindFX.apply(bar, "progress-bar", "progress-" + color, "progress-striped");
    return bar;
  }

  /**
   * Creates an animated progress bar.
   *
   * @param progress value between 0.0 and 1.0
   * @return styled ProgressBar
   */
  public static ProgressBar animated(double progress) {
    return animated(progress, "blue");
  }

  /**
   * Creates an animated progress bar with custom color.
   *
   * @param progress value between 0.0 and 1.0
   * @param color Tailwind color name
   * @return styled ProgressBar
   */
  public static ProgressBar animated(double progress, String color) {
    ProgressBar bar = new ProgressBar(progress);
    TailwindFX.apply(bar, "progress-bar", "progress-" + color, "progress-animated");
    return bar;
  }

  /**
   * Creates a small progress bar.
   *
   * @param progress value between 0.0 and 1.0
   * @return styled ProgressBar
   */
  public static ProgressBar sm(double progress) {
    ProgressBar bar = new ProgressBar(progress);
    TailwindFX.apply(bar, "progress-bar", "progress-sm", "progress-blue");
    return bar;
  }

  /**
   * Creates a large progress bar.
   *
   * @param progress value between 0.0 and 1.0
   * @return styled ProgressBar
   */
  public static ProgressBar lg(double progress) {
    ProgressBar bar = new ProgressBar(progress);
    TailwindFX.apply(bar, "progress-bar", "progress-lg", "progress-blue");
    return bar;
  }

  /**
   * Creates an indeterminate progress bar.
   *
   * @return styled ProgressBar
   */
  public static ProgressBar indeterminate() {
    return indeterminate("blue");
  }

  /**
   * Creates an indeterminate progress bar with custom color.
   *
   * @param color Tailwind color name
   * @return styled ProgressBar
   */
  public static ProgressBar indeterminate(String color) {
    ProgressBar bar = new ProgressBar(-1);
    TailwindFX.apply(bar, "progress-bar", "progress-" + color);
    return bar;
  }
}
