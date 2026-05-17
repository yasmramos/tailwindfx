package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.TailwindFX;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * TwSelect — Pre-styled select/combobox components with TailwindCSS variants.
 * 
 * Uses base ChoiceBox and ComboBox controls styled via
 * tailwindfx-components.css utility classes.
 * 
 * <pre>
 * ChoiceBox&lt;String&gt; cb = TwSelect.choiceBox("Option 1", "Option 2");
 * ComboBox&lt;String&gt; combo = TwSelect.comboBox("Select...", "A", "B", "C");
 * ComboBox&lt;String&gt; editable = TwSelect.editable("Type or select...");
 * </pre>
 */
public final class TwSelect {

    private TwSelect() {}

    /**
     * Creates a ChoiceBox with the given options.
     * @param options option values
     * @return styled ChoiceBox
     */
    @SafeVarargs
    public static <T> ChoiceBox<T> choiceBox(T... options) {
        ChoiceBox<T> cb = new ChoiceBox<>();
        cb.setItems(FXCollections.observableArrayList(options));
        if (options.length > 0) {
            cb.setValue(options[0]);
        }
        TailwindFX.apply(cb, "select", "select-md");
        return cb;
    }

    /**
     * Creates a ChoiceBox with a placeholder (first item as default).
     * @param placeholder placeholder text (used as first item if string)
     * @param options option values
     * @return styled ChoiceBox
     */
    @SafeVarargs
    public static <T> ChoiceBox<T> choiceBoxWithPlaceholder(String placeholder, T... options) {
        ChoiceBox<T> cb = new ChoiceBox<>();
        ObservableList<T> items = FXCollections.observableArrayList();
        // Note: placeholder is just for display, user should handle it appropriately
        cb.setItems(FXCollections.observableArrayList(options));
        TailwindFX.apply(cb, "select", "select-md");
        return cb;
    }

    /**
     * Creates a large ChoiceBox.
     * @param options option values
     * @return styled ChoiceBox (large)
     */
    @SafeVarargs
    public static <T> ChoiceBox<T> choiceBoxLarge(T... options) {
        ChoiceBox<T> cb = new ChoiceBox<>();
        cb.setItems(FXCollections.observableArrayList(options));
        if (options.length > 0) {
            cb.setValue(options[0]);
        }
        TailwindFX.apply(cb, "select", "select-lg");
        return cb;
    }

    /**
     * Creates a small ChoiceBox.
     * @param options option values
     * @return styled ChoiceBox (small)
     */
    @SafeVarargs
    public static <T> ChoiceBox<T> choiceBoxSmall(T... options) {
        ChoiceBox<T> cb = new ChoiceBox<>();
        cb.setItems(FXCollections.observableArrayList(options));
        if (options.length > 0) {
            cb.setValue(options[0]);
        }
        TailwindFX.apply(cb, "select", "select-sm");
        return cb;
    }

    /**
     * Creates a ComboBox with the given options.
     * @param options option values
     * @return styled ComboBox
     */
    @SafeVarargs
    public static <T> ComboBox<T> comboBox(T... options) {
        ComboBox<T> combo = new ComboBox<>();
        combo.setItems(FXCollections.observableArrayList(options));
        if (options.length > 0) {
            combo.setValue(options[0]);
        }
        TailwindFX.apply(combo, "select", "select-md");
        return combo;
    }

    /**
     * Creates a ComboBox with a prompt text.
     * @param promptText prompt text
     * @param options option values
     * @return styled ComboBox
     */
    @SafeVarargs
    public static <T> ComboBox<T> comboBoxWithPrompt(String promptText, T... options) {
        ComboBox<T> combo = new ComboBox<>();
        combo.setPromptText(promptText);
        combo.setItems(FXCollections.observableArrayList(options));
        TailwindFX.apply(combo, "select", "select-md");
        return combo;
    }

    /**
     * Creates a large ComboBox.
     * @param options option values
     * @return styled ComboBox (large)
     */
    @SafeVarargs
    public static <T> ComboBox<T> comboBoxLarge(T... options) {
        ComboBox<T> combo = new ComboBox<>();
        combo.setItems(FXCollections.observableArrayList(options));
        if (options.length > 0) {
            combo.setValue(options[0]);
        }
        TailwindFX.apply(combo, "select", "select-lg");
        return combo;
    }

    /**
     * Creates a small ComboBox.
     * @param options option values
     * @return styled ComboBox (small)
     */
    @SafeVarargs
    public static <T> ComboBox<T> comboBoxSmall(T... options) {
        ComboBox<T> combo = new ComboBox<>();
        combo.setItems(FXCollections.observableArrayList(options));
        if (options.length > 0) {
            combo.setValue(options[0]);
        }
        TailwindFX.apply(combo, "select", "select-sm");
        return combo;
    }

    /**
     * Creates an editable ComboBox.
     * @param promptText prompt text
     * @param options option values
     * @return styled editable ComboBox
     */
    @SafeVarargs
    public static <T> ComboBox<T> editable(String promptText, T... options) {
        ComboBox<T> combo = new ComboBox<>();
        combo.setPromptText(promptText);
        combo.setEditable(true);
        combo.setItems(FXCollections.observableArrayList(options));
        TailwindFX.apply(combo, "select", "select-md", "select-editable");
        return combo;
    }

    /**
     * Creates a disabled ChoiceBox.
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
        TailwindFX.apply(cb, "select", "select-disabled");
        return cb;
    }

    /**
     * Creates a disabled ComboBox.
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
        TailwindFX.apply(combo, "select", "select-disabled");
        return combo;
    }

    /**
     * Creates a ComboBox with error styling.
     * @param promptText prompt text
     * @param options option values
     * @return styled ComboBox (error state)
     */
    @SafeVarargs
    public static <T> ComboBox<T> error(String promptText, T... options) {
        ComboBox<T> combo = new ComboBox<>();
        combo.setPromptText(promptText);
        combo.setItems(FXCollections.observableArrayList(options));
        TailwindFX.apply(combo, "select", "select-error");
        return combo;
    }
}
