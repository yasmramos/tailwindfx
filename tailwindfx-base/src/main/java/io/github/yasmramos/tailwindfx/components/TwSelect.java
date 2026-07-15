package io.github.yasmramos.tailwindfx.components;

import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;

/**
 * TwSelect — Pre-styled select/combobox components with TailwindCSS variants.
 *
 * <p>Utility class for creating styled ChoiceBox and ComboBox controls.
 *
 * <pre>
 * ChoiceBox&lt;String&gt; cb = TwSelect.choiceBox("Option 1", "Option 2");
 * ComboBox&lt;String&gt; combo = TwSelect.comboBox("A", "B", "C");
 * ComboBox&lt;String&gt; editable = TwSelect.editable("Type or select...");
 * </pre>
 */
public final class TwSelect {

  private TwSelect() {}

  /**
   * Creates a styled ChoiceBox with the given options.
   *
   * @param options option values
   * @return styled ChoiceBox
   */
  @SafeVarargs
  public static <T> ChoiceBox<T> choiceBox(T... options) {
    ChoiceBox<T> cb = new ChoiceBox<>(FXCollections.observableArrayList(options));
    cb.getStyleClass().addAll("select", "select-base");
    if (options.length > 0) {
      cb.setValue(options[0]);
    }
    return cb;
  }

  /**
   * Creates a styled ComboBox with the given options.
   *
   * @param options option values
   * @return styled ComboBox
   */
  @SafeVarargs
  public static <T> ComboBox<T> comboBox(T... options) {
    ComboBox<T> combo = new ComboBox<>(FXCollections.observableArrayList(options));
    combo.getStyleClass().addAll("select", "select-base");
    return combo;
  }

  /**
   * Creates a styled editable ComboBox.
   *
   * @param placeholder placeholder text
   * @return styled editable ComboBox
   */
  public static ComboBox<String> editable(String placeholder) {
    ComboBox<String> combo = new ComboBox<>();
    combo.setEditable(true);
    combo.setPromptText(placeholder);
    combo.getStyleClass().addAll("select", "select-base");
    return combo;
  }

  /**
   * Creates a disabled ChoiceBox.
   *
   * @param options option values
   * @return styled ChoiceBox (disabled)
   */
  @SafeVarargs
  public static <T> ChoiceBox<T> disabledChoiceBox(T... options) {
    ChoiceBox<T> cb = new ChoiceBox<>();
    cb.setDisable(true);
    cb.setItems(FXCollections.observableArrayList(options));
    if (options.length > 0) {
      cb.setValue(options[0]);
    }
    cb.getStyleClass().addAll("select", "select-disabled");
    return cb;
  }

  /**
   * Creates a disabled ComboBox.
   *
   * @param options option values
   * @return styled ComboBox (disabled)
   */
  @SafeVarargs
  public static <T> ComboBox<T> disabledComboBox(T... options) {
    ComboBox<T> combo = new ComboBox<>();
    combo.setDisable(true);
    combo.setItems(FXCollections.observableArrayList(options));
    if (options.length > 0) {
      combo.setValue(options[0]);
    }
    combo.getStyleClass().addAll("select", "select-disabled");
    return combo;
  }

  /**
   * Creates a ComboBox with error styling.
   *
   * @param promptText prompt text
   * @param options option values
   * @return styled ComboBox (error state)
   */
  @SafeVarargs
  public static <T> ComboBox<T> error(String promptText, T... options) {
    ComboBox<T> combo = new ComboBox<>();
    combo.setPromptText(promptText);
    combo.setItems(FXCollections.observableArrayList(options));
    combo.getStyleClass().addAll("select", "select-error");
    return combo;
  }
}
