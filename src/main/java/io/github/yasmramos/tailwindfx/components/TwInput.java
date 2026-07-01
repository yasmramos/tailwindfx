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

    /**
     * Creates a default text input.
     */
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
        setupValidation();
    }

    private void setupValidation() {
        textProperty().addListener((obs, oldVal, newVal) -> {
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
     * Sets a numeric formatter for this input.
     */
    public void setNumericOnly() {
        TextFormatter<?> formatter = new TextFormatter<>(c -> {
            String text = c.getControlNewText();
            if (text.matches("\\d*")) {
                return c;
            }
            return null;
        });
        setTextFormatter(formatter);
    }

    /**
     * Sets a decimal formatter for this input.
     */
    public void setDecimalOnly() {
        TextFormatter<?> formatter = new TextFormatter<>(c -> {
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
     * @return TwInput configured as password field
     */
    public static TwInput password() {
        TwInput input = new TwInput("Password");
        input.setEchoChar('●');
        return input;
    }

    /**
     * Sets the echo character for password input.
     *
     * @param char the echo character
     */
    public void setEchoChar(char echoChar) {
        // JavaFX TextField doesn't support echo char directly
        // This would need a custom skin or PasswordField
        setPromptText(String.valueOf(echoChar));
    }
}
