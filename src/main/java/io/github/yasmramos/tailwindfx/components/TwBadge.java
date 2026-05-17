package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.TwStyle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

/**
 * TwBadge — Pre-styled badge and pill components.
 * 
 * <pre>
 * TwBadge badge = TwBadge.create("NEW", "blue");
 * TwBadge pill = TwBadge.pill("Active", "green");
 * TwBadge dot = TwBadge.dot("Online", "green");
 * </pre>
 */
public class TwBadge extends Label {

    /**
     * Creates a small badge label (e.g. NEW, BETA, PRO).
     * @param text badge text
     * @return styled TwBadge with default blue color
     */
    public static TwBadge create(String text) {
        return create(text, "blue");
    }

    /**
     * Creates a small badge label with custom color.
     * @param text badge text
     * @param color Tailwind color name (e.g. "blue", "green", "red")
     * @return styled TwBadge
     */
    public static TwBadge create(String text, String color) {
        TwBadge lbl = new TwBadge();
        lbl.setText(text.toUpperCase());
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
     * @return styled TwBadge with default blue color
     */
    public static TwBadge pill(String text) {
        return pill(text, "blue");
    }

    /**
     * Creates a rounded pill / chip label with custom color.
     * @param text pill text
     * @param color Tailwind color name
     * @return styled TwBadge
     */
    public static TwBadge pill(String text, String color) {
        TwBadge lbl = create(text, color);
        lbl.setStyle(lbl.getStyle().replace("-fx-background-radius: 4px", "")
            + " -fx-background-radius: 999px;");
        return lbl;
    }

    /**
     * Creates a badge with a status dot indicator.
     * @param text badge text
     * @param color Tailwind color name for the dot
     * @return TwBadgeDot container with dot and text
     */
    public static TwBadgeDot dot(String text, String color) {
        return dot(text, color, "gray");
    }

    /**
     * Creates a badge with a status dot indicator.
     * @param text badge text
     * @param dotColor Tailwind color name for the dot
     * @param textColor Tailwind color name for the text
     * @return TwBadgeDot container with dot and text
     */
    public static TwBadgeDot dot(String text, String dotColor, String textColor) {
        return new TwBadgeDot(text, dotColor, textColor);
    }

    /**
     * Creates an outline badge (border only).
     * @param text badge text
     * @return styled TwBadge with default gray color
     */
    public static TwBadge outline(String text) {
        return outline(text, "gray");
    }

    /**
     * Creates an outline badge with custom color.
     * @param text badge text
     * @param color Tailwind color name
     * @return styled TwBadge
     */
    public static TwBadge outline(String text, String color) {
        TwBadge lbl = new TwBadge();
        lbl.setText(text.toUpperCase());
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
     * Protected constructor for internal usage.
     */
    protected TwBadge() {
        super();
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

    /**
     * Container for badge with dot indicator.
     */
    public static class TwBadgeDot extends HBox {
        
        private final Label dot;
        private final TwBadge label;
        
        /**
         * Creates a badge with dot indicator.
         * @param text badge text
         * @param dotColor Tailwind color name for the dot
         * @param textColor Tailwind color name for the text
         */
        public TwBadgeDot(String text, String dotColor, String textColor) {
            super();
            
            dot = new Label();
            String dotBg = getColor(dotColor, 500);
            dot.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 999px;"
                + " -fx-min-width: 8px; -fx-min-height: 8px;"
                + " -fx-max-width: 8px; -fx-max-height: 8px;", dotBg));

            label = new TwBadge();
            label.setText(text);
            String fg = getColor(textColor, 700);
            label.setStyle(String.format(
                "-fx-text-fill: %s; -fx-font-size: 12px;"
                + " -fx-padding: 0 0 0 6;", fg));
            label.setPadding(new Insets(0));

            getChildren().addAll(dot, label);
            setSpacing(4);
            setAlignment(Pos.CENTER_LEFT);
        }
        
        /**
         * Gets the dot node.
         * @return the dot Label
         */
        public Label getDot() {
            return dot;
        }
        
        /**
         * Gets the text label.
         * @return the TwBadge label
         */
        public TwBadge getLabel() {
            return label;
        }
    }
}
