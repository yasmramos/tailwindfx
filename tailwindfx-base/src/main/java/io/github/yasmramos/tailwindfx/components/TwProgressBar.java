package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.TailwindFX;
import javafx.scene.control.ProgressBar;

/**
 * TwProgressBar — Progress bar component with TailwindCSS variants.
 *
 * <p>Extends JavaFX ProgressBar with TailwindCSS styling.
 *
 * <pre>
 * TwProgressBar bar = new TwProgressBar(0.5);
 * TwProgressBar bar = TwProgressBar.success(0.75);
 * TwProgressBar bar = TwProgressBar.warning(0.3);
 * TwProgressBar bar = TwProgressBar.error(0.1);
 * TwProgressBar bar = TwProgressBar.striped(0.6);
 * </pre>
 */
public class TwProgressBar extends ProgressBar {

  private String color = "blue";
  private boolean striped = false;

  public TwProgressBar(double progress) {
    super(progress);
    initialize();
  }

  private void initialize() {
    getStyleClass().add("progress-bar");
    applyColor();
  }

  private void applyColor() {
    getStyleClass().removeIf(cls -> cls.startsWith("progress-") && !cls.equals("progress-bar"));
    if (color != null && !color.isEmpty()) {
      getStyleClass().add("progress-" + color);
    }
    if (striped) {
      getStyleClass().add("progress-striped");
    }
  }

  public void setColor(String color) {
    this.color = color;
    applyColor();
  }

  public String getColor() {
    return color;
  }

  public void setStriped(boolean striped) {
    this.striped = striped;
    applyColor();
  }

  public boolean isStriped() {
    return striped;
  }

  /**
   * Creates a success progress bar with given progress.
   *
   * @param progress value between 0.0 and 1.0
   * @return styled TwProgressBar
   */
  public static TwProgressBar success(double progress) {
    TwProgressBar bar = new TwProgressBar(progress);
    bar.setColor("green");
    return bar;
  }

  /**
   * Creates a warning progress bar with given progress.
   *
   * @param progress value between 0.0 and 1.0
   * @return styled TwProgressBar
   */
  public static TwProgressBar warning(double progress) {
    TwProgressBar bar = new TwProgressBar(progress);
    bar.setColor("yellow");
    return bar;
  }

  /**
   * Creates an error progress bar with given progress.
   *
   * @param progress value between 0.0 and 1.0
   * @return styled TwProgressBar
   */
  public static TwProgressBar error(double progress) {
    TwProgressBar bar = new TwProgressBar(progress);
    bar.setColor("red");
    return bar;
  }

  /**
   * Creates a striped progress bar with given progress.
   *
   * @param progress value between 0.0 and 1.0
   * @return styled TwProgressBar
   */
  public static TwProgressBar striped(double progress) {
    TwProgressBar bar = new TwProgressBar(progress);
    bar.setStriped(true);
    return bar;
  }
}
