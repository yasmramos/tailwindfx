package io.github.yasmramos.tailwindfx.components;

import javafx.scene.control.CheckBox;

/**
 * TwCheckbox — Custom checkbox component extending JavaFX CheckBox with TailwindCSS variants.
 *
 * <p>Extends CheckBox for full CSS support and native behavior.
 */
public class TwCheckbox extends CheckBox {

  /** Base stylesheet class applied to every checkbox. */
  private static final String BASE_CLASS = "checkbox";

  /** Prefix shared by every modifier class applied by this component. */
  private static final String MODIFIER_PREFIX = "checkbox-";

  /** State class applied by {@link #setError(boolean)}. */
  private static final String ERROR_CLASS = "checkbox-error";

  /** State class applied by {@link #disabled(String)}. */
  private static final String DISABLED_CLASS = "checkbox-disabled";

  private String color = "blue";
  private String size = "md";

  /** Creates a default checkbox. */
  public TwCheckbox() {
    super();
    initialize();
  }

  /**
   * Creates a checkbox with text.
   *
   * @param text the label text
   */
  public TwCheckbox(String text) {
    super(text);
    initialize();
  }

  private void initialize() {
    getStyleClass().add(BASE_CLASS);
    applyStyling();
  }

  private void applyStyling() {
    // Remove previously applied color/size classes. The error and disabled state classes are
    // owned by setError/disabled(...) and are preserved so changing the color or size does not
    // silently clear a validation or disabled state.
    getStyleClass()
        .removeIf(
            cls ->
                cls.startsWith(MODIFIER_PREFIX)
                    && !cls.equals(ERROR_CLASS)
                    && !cls.equals(DISABLED_CLASS));

    // Add color class
    if (color != null && !color.isEmpty()) {
      getStyleClass().add("checkbox-" + color);
    }

    // Add size class
    if (size != null && !size.isEmpty()) {
      getStyleClass().add("checkbox-" + size);
    }
  }

  /**
   * Sets the checkbox color.
   *
   * @param color Tailwind color name
   */
  public void setColor(String color) {
    this.color = color;
    applyStyling();
  }

  /**
   * Gets the checkbox color.
   *
   * @return the color
   */
  public String getColor() {
    return color;
  }

  /**
   * Sets the checkbox size.
   *
   * @param size size modifier (xs, sm, md, lg, xl)
   */
  /**
   * Sets the checkbox size.
   *
   * @param size size modifier (xs, sm, md, lg, xl)
   */
  public void setSize(String size) {
    this.size = size;
    applyStyling();
  }

  /**
   * Gets the checkbox size.
   *
   * @return the size
   */
  public String getSize() {
    return size;
  }

  /**
   * Sets the error state.
   *
   * @param error true to show error state
   */
  public void setError(boolean error) {
    if (error) {
      getStyleClass().add(ERROR_CLASS);
    } else {
      getStyleClass().remove(ERROR_CLASS);
    }
  }

  /**
   * Checks if input is in error state.
   *
   * @return true if error
   */
  public boolean isError() {
    return getStyleClass().contains(ERROR_CLASS);
  }

  /**
   * Creates a checkbox with text.
   *
   * @param text label text
   * @return TwCheckbox instance
   */
  public static TwCheckbox create(String text) {
    return new TwCheckbox(text);
  }

  /**
   * Creates a checked checkbox.
   *
   * @param text label text
   * @param checked initial checked state
   * @return TwCheckbox instance
   */
  public static TwCheckbox checked(String text, boolean checked) {
    TwCheckbox chk = new TwCheckbox(text);
    chk.setSelected(checked);
    return chk;
  }

  /**
   * Creates a disabled checkbox.
   *
   * @param text label text
   * @return TwCheckbox instance
   */
  public static TwCheckbox disabled(String text) {
    TwCheckbox chk = new TwCheckbox(text);
    chk.setDisable(true);
    chk.getStyleClass().add(DISABLED_CLASS);
    return chk;
  }

  /**
   * Creates a small checkbox.
   *
   * @param text label text
   * @return TwCheckbox instance
   */
  public static TwCheckbox small(String text) {
    TwCheckbox chk = new TwCheckbox(text);
    chk.setSize("sm");
    return chk;
  }

  /**
   * Creates a large checkbox.
   *
   * @param text label text
   * @return TwCheckbox instance
   */
  public static TwCheckbox large(String text) {
    TwCheckbox chk = new TwCheckbox(text);
    chk.setSize("lg");
    return chk;
  }
}
