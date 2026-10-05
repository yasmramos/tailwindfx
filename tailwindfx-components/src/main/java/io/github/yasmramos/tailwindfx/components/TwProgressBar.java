package io.github.yasmramos.tailwindfx.components;

import java.util.Set;
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

  /** Base stylesheet class applied to every TwProgressBar. */
  private static final String BASE_CLASS = "progress-bar";

  /** Prefix shared by every modifier class applied by this component. */
  private static final String MODIFIER_PREFIX = "progress-";

  /**
   * Modifier classes that are owned by other concerns and must survive {@link #applyColor()}. The
   * table adds {@code progress-bar-success} style classes itself, and {@code ProgressBar} exposes
   * {@code progress-bar} as a base class, so neither may be treated as a color modifier.
   */
  private static final Set<String> MODIFIERS =
      Set.of(BASE_CLASS, "progress-bar-success", "progress-bar-warning", "progress-bar-danger");

  private String color = "blue";
  private boolean striped = false;

  public TwProgressBar(double progress) {
    super(progress);
    initialize();
  }

  private void initialize() {
    getStyleClass().add(BASE_CLASS);
    applyColor();
  }

  private void applyColor() {
    // Drop previously applied modifier classes (progress-blue, progress-striped, ...) while
    // keeping the base "progress-bar" class. Matches on the prefix so every modifier, including
    // ones added by later features, is cleaned up in one place.
    getStyleClass()
        .removeIf(
            cls ->
                cls.startsWith(MODIFIER_PREFIX)
                    && !cls.equals(BASE_CLASS)
                    && !MODIFIERS.contains(cls));

    if (color != null && !color.isEmpty()) {
      getStyleClass().add(MODIFIER_PREFIX + color);
    }
    if (striped) {
      getStyleClass().add(MODIFIER_PREFIX + "striped");
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
