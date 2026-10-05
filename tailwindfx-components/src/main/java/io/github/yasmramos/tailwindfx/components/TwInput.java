package io.github.yasmramos.tailwindfx.components;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * TwInput — Custom text input component extending JavaFX TextField with TailwindCSS styling.
 *
 * <p>Extends TextField for full CSS support, validation, and native behavior.
 */
public class TwInput extends TextField {

  private boolean error = false;
  private String placeholderText = "";

  /** Creates a default text input. */
  public TwInput() {
    super();
    initialize();
  }

  /**
   * Creates a text input with prompt text.
   *
   * @param placeholder the placeholder text
   */
  public TwInput(String placeholder) {
    super();
    setPromptText(placeholder);
    this.placeholderText = placeholder;
    initialize();
  }

  private void initialize() {
    getStyleClass().add("tw-input");

    // Clear the error state as soon as the user starts correcting the value, so a stale
    // validation message does not stay visible next to valid input.
    setupValidation();
  }

  private void setupValidation() {
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
   * @param error true to show error state
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
   * Checks if input is in error state.
   *
   * @return true if error
   */
  public boolean isError() {
    return error;
  }

  /**
   * Restricts input to digits only, rejecting any other character.
   *
   * <p>The formatter allows intermediate empty values so the field can be cleared while typing.
   */
  public void setNumericOnly() {
    TextFormatter<?> formatter =
        new TextFormatter<>(
            c -> {
              String text = c.getControlNewText();
              if (text.matches("\\d*")) {
                return c;
              }
              return null;
            });
    setTextFormatter(formatter);
  }

  /** Sets a decimal formatter for this input. */
  public void setDecimalOnly() {
    TextFormatter<?> formatter =
        new TextFormatter<>(
            c -> {
              String text = c.getControlNewText();
              if (text.matches("\\d*(\\.\\d*)?")) {
                return c;
              }
              return null;
            });
    setTextFormatter(formatter);
  }

  /**
   * Creates a text input with placeholder.
   *
   * @param placeholder the placeholder text
   * @return TwInput instance
   */
  public static TwInput withPlaceholder(String placeholder) {
    return new TwInput(placeholder);
  }

  /**
   * Creates a numeric input.
   *
   * @return TwInput with numeric formatter
   */
  public static TwInput numeric() {
    TwInput input = new TwInput();
    input.setNumericOnly();
    return input;
  }

  /**
   * Creates a decimal input.
   *
   * @return TwInput with decimal formatter
   */
  public static TwInput decimal() {
    TwInput input = new TwInput();
    input.setDecimalOnly();
    return input;
  }

  /**
   * Creates a password input.
   *
   * @deprecated a {@link javafx.scene.control.TextField} cannot mask its content, so the returned
   *     field would display the password in plain text. Use {@link TwPasswordField} instead.
   */
  @Deprecated
  public static TwInput password() {
    return new TwInput("Password");
  }
}
