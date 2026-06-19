package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.TailwindFX;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * TwInput — Pre-styled input components with TailwindCSS variants.
 *
 * <p>Uses base TextField, PasswordField, and TextArea controls styled via tailwindfx-components.css
 * utility classes.
 *
 * <pre>
 * TextField input = TwInput.text("Enter your name");
 * PasswordField pwd = TwInput.password("Enter password");
 * TextArea area = TwInput.area("Enter comments");
 * TextField email = TwInput.email("email@example.com");
 * </pre>
 */
public final class TwInput {

  private TwInput() {}

  /**
   * Creates a standard text input field.
   *
   * @param placeholder placeholder text
   * @return styled TextField
   */
  public static TextField text(String placeholder) {
    TextField field = new TextField();
    field.setPromptText(placeholder);
    TailwindFX.apply(field, "input", "input-md");
    return field;
  }

  /**
   * Creates a text input field with initial value.
   *
   * @param value initial value
   * @return styled TextField
   */
  public static TextField textWithValue(String value) {
    TextField field = new TextField(value);
    TailwindFX.apply(field, "input", "input-md");
    return field;
  }

  /**
   * Creates a large text input field.
   *
   * @param placeholder placeholder text
   * @return styled TextField (large)
   */
  public static TextField textLarge(String placeholder) {
    TextField field = new TextField();
    field.setPromptText(placeholder);
    TailwindFX.apply(field, "input", "input-lg");
    return field;
  }

  /**
   * Creates a small text input field.
   *
   * @param placeholder placeholder text
   * @return styled TextField (small)
   */
  public static TextField textSmall(String placeholder) {
    TextField field = new TextField();
    field.setPromptText(placeholder);
    TailwindFX.apply(field, "input", "input-sm");
    return field;
  }

  /**
   * Creates a password input field.
   *
   * @param placeholder placeholder text
   * @return styled PasswordField
   */
  public static PasswordField password(String placeholder) {
    PasswordField field = new PasswordField();
    field.setPromptText(placeholder);
    TailwindFX.apply(field, "input", "input-md");
    return field;
  }

  /**
   * Creates a large password input field.
   *
   * @param placeholder placeholder text
   * @return styled PasswordField (large)
   */
  public static PasswordField passwordLarge(String placeholder) {
    PasswordField field = new PasswordField();
    field.setPromptText(placeholder);
    TailwindFX.apply(field, "input", "input-lg");
    return field;
  }

  /**
   * Creates a text area for multi-line input.
   *
   * @param placeholder placeholder text
   * @return styled TextArea
   */
  public static TextArea area(String placeholder) {
    TextArea area = new TextArea();
    area.setPromptText(placeholder);
    area.setPrefRowCount(4);
    TailwindFX.apply(area, "input", "input-md");
    return area;
  }

  /**
   * Creates a large text area for multi-line input.
   *
   * @param placeholder placeholder text
   * @return styled TextArea (large)
   */
  public static TextArea areaLarge(String placeholder) {
    TextArea area = new TextArea();
    area.setPromptText(placeholder);
    area.setPrefRowCount(8);
    TailwindFX.apply(area, "input", "input-lg");
    return area;
  }

  /**
   * Creates a disabled input field.
   *
   * @param placeholder placeholder text
   * @return styled TextField (disabled)
   */
  public static TextField disabled(String placeholder) {
    TextField field = new TextField();
    field.setPromptText(placeholder);
    field.setDisable(true);
    TailwindFX.apply(field, "input", "input-disabled");
    return field;
  }

  /**
   * Creates an input field with error styling.
   *
   * @param placeholder placeholder text
   * @return styled TextField (error state)
   */
  public static TextField error(String placeholder) {
    TextField field = new TextField();
    field.setPromptText(placeholder);
    TailwindFX.apply(field, "input", "input-error");
    return field;
  }

  /**
   * Creates an email input field (text field with email placeholder).
   *
   * @param placeholder placeholder text
   * @return styled TextField
   */
  public static TextField email(String placeholder) {
    TextField field = new TextField();
    field.setPromptText(placeholder);
    TailwindFX.apply(field, "input", "input-md");
    return field;
  }

  /**
   * Creates a number input field.
   *
   * @param placeholder placeholder text
   * @return styled TextField
   */
  public static TextField number(String placeholder) {
    TextField field = new TextField();
    field.setPromptText(placeholder);
    TailwindFX.apply(field, "input", "input-md");
    return field;
  }

  /**
   * Creates a search input field.
   *
   * @param placeholder placeholder text
   * @return styled TextField
   */
  public static TextField search(String placeholder) {
    TextField field = new TextField();
    field.setPromptText(placeholder);
    TailwindFX.apply(field, "input", "input-md");
    return field;
  }
}
