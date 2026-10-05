package io.github.yasmramos.tailwindfx.components;

import javafx.scene.control.PasswordField;

/**
 * TwPasswordField — Masked password input styled with the TailwindFX input classes.
 *
 * <p>JavaFX only masks characters in a {@link PasswordField}; a plain {@link TextField} cannot hide
 * its content. This component therefore extends {@code PasswordField} directly and reuses the same
 * {@code tw-input} styling, error state and input formatters as {@link TwInput}.
 *
 * <pre>
 * TwPasswordField password = TwPasswordField.create();
 * password.setErrorMessage("Password is required");
 * </pre>
 */
public class TwPasswordField extends PasswordField {

  private boolean error = false;

  /** Creates an empty password field. */
  public TwPasswordField() {
    super();
    initialize();
  }

  /**
   * Creates a password field with prompt text.
   *
   * @param placeholder the prompt text shown while the field is empty
   */
  public TwPasswordField(String placeholder) {
    super();
    setPromptText(placeholder);
    initialize();
  }

  private void initialize() {
    getStyleClass().add("tw-input");

    // Clear the error state as soon as the user starts correcting the value, so a stale
    // validation message does not stay visible next to valid input.
    textProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              if (error && newVal != null && !newVal.isEmpty()) {
                setError(false);
              }
            });
  }

  /**
   * Sets the error state.
   *
   * @param error true to render the error styling
   */
  public void setError(boolean error) {
    this.error = error;
    if (error) {
      getStyleClass().add("input-error");
    } else {
      getStyleClass().remove("input-error");
    }
  }

  /**
   * Reports whether this field is currently in the error state.
   *
   * @return true when the error styling is applied
   */
  public boolean isError() {
    return error;
  }

  /**
   * Creates an empty password field.
   *
   * @return a styled TwPasswordField
   */
  public static TwPasswordField create() {
    return new TwPasswordField();
  }

  /**
   * Creates a password field with prompt text.
   *
   * @param placeholder the prompt text shown while the field is empty
   * @return a styled TwPasswordField
   */
  public static TwPasswordField withPlaceholder(String placeholder) {
    return new TwPasswordField(placeholder);
  }
}