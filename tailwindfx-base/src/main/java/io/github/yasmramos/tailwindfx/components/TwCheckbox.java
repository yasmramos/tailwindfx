package io.github.yasmramos.tailwindfx.components;

import javafx.scene.control.CheckBox;

/**
 * TwCheckbox — Custom checkbox component extending JavaFX CheckBox with TailwindCSS variants.
 *
 * <p>Extends CheckBox for full CSS support and native behavior.
 */
public class TwCheckbox extends CheckBox {

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
    getStyleClass().add("checkbox");
    applyStyling();
  }

  private void applyStyling() {
    // Remove old color and size classes
    getStyleClass()
        .removeIf(
            cls ->
                (cls.startsWith("checkbox-") && !cls.equals("checkbox"))
                    || cls.equals("checkbox-error")
                    || cls.equals("checkbox-disabled"));

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
      getStyleClass().add("checkbox-error");
    } else {
      getStyleClass().remove("checkbox-error");
    }
  }

  /**
   * Checks if input is in error state.
   *
   * @return true if error
   */
  public boolean isError() {
    return getStyleClass().contains("checkbox-error");
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
    chk.getStyleClass().add("checkbox-disabled");
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
