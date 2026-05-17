package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.TailwindFX;
import io.github.yasmramos.tailwindfx.animation.FxAnimation;
import javafx.scene.control.Button;

/**
 * TwButton — Pre-styled button component with TailwindCSS variants.
 * 
 * Uses base .btn class from tailwindfx-components.css with utility modifiers.
 * 
 * <pre>
 * Button btn = TwButton.primary("Save");
 * Button btn = TwButton.secondary("Cancel");
 * Button btn = TwButton.outline("Delete", "red");
 * Button btn = TwButton.ghost("More info");
 * Button btn = TwButton.icon("🔍", "Search");
 * </pre>
 */
public final class TwButton {

    private TwButton() {}

    /**
     * Creates a primary button (solid background, prominent).
     * @param text button text
     * @return styled Button
     */
    public static Button primary(String text) {
        return primary(text, "blue");
    }

    /**
     * Creates a primary button with custom color.
     * @param text button text
     * @param color Tailwind color name (e.g. "blue", "green", "red")
     * @return styled Button
     */
    public static Button primary(String text, String color) {
        Button btn = new Button(text);
        TailwindFX.apply(btn, 
            "btn",
            "btn-primary",
            "btn-" + color,
            "btn-md"
        );
        FxAnimation.onHoverLift(btn, -2);
        return btn;
    }

    /**
     * Creates a secondary button (subtle background).
     * @param text button text
     * @return styled Button
     */
    public static Button secondary(String text) {
        return secondary(text, "gray");
    }

    /**
     * Creates a secondary button with custom color.
     * @param text button text
     * @param color Tailwind color name
     * @return styled Button
     */
    public static Button secondary(String text, String color) {
        Button btn = new Button(text);
        TailwindFX.apply(btn,
            "btn",
            "btn-secondary",
            "btn-" + color,
            "btn-md"
        );
        FxAnimation.onHoverLift(btn, -1);
        return btn;
    }

    /**
     * Creates an outline button (border only).
     * @param text button text
     * @return styled Button
     */
    public static Button outline(String text) {
        return outline(text, "gray");
    }

    /**
     * Creates an outline button with custom color.
     * @param text button text
     * @param color Tailwind color name
     * @return styled Button
     */
    public static Button outline(String text, String color) {
        Button btn = new Button(text);
        TailwindFX.apply(btn,
            "btn",
            "btn-outline",
            "btn-" + color,
            "btn-md"
        );
        FxAnimation.onHoverLift(btn, -1);
        return btn;
    }

    /**
     * Creates a ghost button (no border, subtle hover).
     * @param text button text
     * @return styled Button
     */
    public static Button ghost(String text) {
        return ghost(text, "gray");
    }

    /**
     * Creates a ghost button with custom color.
     * @param text button text
     * @param color Tailwind color name
     * @return styled Button
     */
    public static Button ghost(String text, String color) {
        Button btn = new Button(text);
        TailwindFX.apply(btn,
            "btn",
            "btn-ghost",
            "btn-" + color,
            "btn-md"
        );
        return btn;
    }

    /**
     * Creates an icon button with text label (for accessibility).
     * @param icon icon character or emoji
     * @param label accessible label
     * @return styled Button
     */
    public static Button icon(String icon, String label) {
        return icon(icon, label, "gray");
    }

    /**
     * Creates an icon button with custom color.
     * @param icon icon character or emoji
     * @param label accessible label
     * @param color Tailwind color name
     * @return styled Button
     */
    public static Button icon(String icon, String label, String color) {
        Button btn = new Button(icon);
        btn.setAccessibleText(label);
        TailwindFX.apply(btn,
            "btn",
            "btn-icon",
            "btn-" + color,
            "btn-circle"
        );
        return btn;
    }

    /**
     * Creates a danger button (red, destructive action).
     * @param text button text
     * @return styled Button
     */
    public static Button danger(String text) {
        return primary(text, "red");
    }

    /**
     * Creates a success button (green, confirmatory action).
     * @param text button text
     * @return styled Button
     */
    public static Button success(String text) {
        return primary(text, "green");
    }

    /**
     * Creates a disabled-looking button (non-interactive style).
     * Note: You should also call setDisable(true) on the returned button.
     * @param text button text
     * @return styled Button
     */
    public static Button disabled(String text) {
        Button btn = new Button(text);
        TailwindFX.apply(btn,
            "btn",
            "btn-disabled"
        );
        btn.setDisable(true);
        return btn;
    }
}
