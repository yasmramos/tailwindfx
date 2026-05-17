package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.TwStyle;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * TwBadge — Pre-styled badge and pill components.
 * 
 * <pre>
 * Label badge = TwBadge.create("NEW", "blue");
 * Label pill = TwBadge.pill("Active", "green");
 * Label dot = TwBadge.dot("Online", "green");
 * </pre>
 */
public final class TwBadge {

    private TwBadge() {}

    /**
     * Creates a small badge label (e.g. NEW, BETA, PRO).
     * @param text badge text
     * @return styled Label with default blue color
     */
    public static Label create(String text) {
        return create(text, "blue");
    }

    /**
     * Creates a small badge label with custom color.
     * @param text badge text
     * @param color Tailwind color name (e.g. "blue", "green", "red")
     * @return styled Label
     */
    public static Label create(String text, String color) {
        Label lbl = new Label(text.toUpperCase());
        String bg = getColor(color, 100);
        String fg = getColor(color, 700);
        lbl.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: %s; -fx-font-size: 10px;"
            + " -fx-font-weight: bold; -fx-padding: 2 8 2 8;"
            + " -fx-background-radius: 4px;", bg, fg));
        return lbl;
    }

    /**
     * Creates a rounded pill / chip label.
     * @param text pill text
     * @return styled Label with default blue color
     */
    public static Label pill(String text) {
        return pill(text, "blue");
    }

    /**
     * Creates a rounded pill / chip label with custom color.
     * @param text pill text
     * @param color Tailwind color name
     * @return styled Label
     */
    public static Label pill(String text, String color) {
        Label lbl = create(text, color);
        lbl.setStyle(lbl.getStyle().replace("-fx-background-radius: 4px", "")
            + " -fx-background-radius: 999px;");
        return lbl;
    }

    /**
     * Creates a badge with a status dot indicator.
     * @param text badge text
     * @param color Tailwind color name for the dot
     * @return HBox containing dot and text
     */
    public static HBox dot(String text, String color) {
        return dot(text, color, "gray");
    }

    /**
     * Creates a badge with a status dot indicator.
     * @param text badge text
     * @param dotColor Tailwind color name for the dot
     * @param textColor Tailwind color name for the text
     * @return HBox containing dot and text
     */
    public static HBox dot(String text, String dotColor, String textColor) {
        Label dot = new Label();
        String dotBg = getColor(dotColor, 500);
        dot.setStyle(String.format(
            "-fx-background-color: %s; -fx-background-radius: 999px;"
            + " -fx-min-width: 8px; -fx-min-height: 8px;"
            + " -fx-max-width: 8px; -fx-max-height: 8px;", dotBg));

        Label lbl = new Label(text);
        String fg = getColor(textColor, 700);
        lbl.setStyle(String.format(
            "-fx-text-fill: %s; -fx-font-size: 12px;"
            + " -fx-padding: 0 0 0 6;", fg));

        HBox container = new HBox(4, dot, lbl);
        container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return container;
    }

    /**
     * Creates an outline badge (border only).
     * @param text badge text
     * @return styled Label with default gray color
     */
    public static Label outline(String text) {
        return outline(text, "gray");
    }

    /**
     * Creates an outline badge with custom color.
     * @param text badge text
     * @param color Tailwind color name
     * @return styled Label
     */
    public static Label outline(String text, String color) {
        Label lbl = new Label(text.toUpperCase());
        String border = getColor(color, 600);
        String fg = getColor(color, 700);
        lbl.setStyle(String.format(
            "-fx-border-color: %s; -fx-border-width: 1px;"
            + " -fx-text-fill: %s; -fx-font-size: 10px;"
            + " -fx-font-weight: bold; -fx-padding: 2 8 2 8;"
            + " -fx-background-radius: 4px; -fx-border-radius: 4px;", border, fg));
        return lbl;
    }

    /**
     * Helper to get hex color from Tailwind color name and shade.
     */
    private static String getColor(String color, int shade) {
        try {
            Class<?> palette = Class.forName("io.github.yasmramos.tailwindfx.color.ColorPalette");
            java.lang.reflect.Method method = palette.getMethod("hex", String.class, int.class);
            return (String) method.invoke(null, color, shade);
        } catch (Exception e) {
            // Fallback colors if ColorPalette is not available
            return "#3b82f6";
        }
    }
}
