package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.TwStyle;
import io.github.yasmramos.tailwindfx.animation.FxAnimation;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * TwAlert — Pre-styled alert/notification component.
 * 
 * <pre>
 * HBox info = TwAlert.info("Operation completed successfully");
 * HBox warning = TwAlert.warning("Please review before continuing");
 * HBox error = TwAlert.error("Something went wrong");
 * HBox success = TwAlert.success("Data saved!");
 * </pre>
 */
public final class TwAlert {

    private TwAlert() {}

    /**
     * Creates an info alert (blue).
     * @param message alert message
     * @return styled HBox
     */
    public static HBox info(String message) {
        return create(message, "info", "blue");
    }

    /**
     * Creates a success alert (green).
     * @param message alert message
     * @return styled HBox
     */
    public static HBox success(String message) {
        return create(message, "success", "green");
    }

    /**
     * Creates a warning alert (yellow/amber).
     * @param message alert message
     * @return styled HBox
     */
    public static HBox warning(String message) {
        return create(message, "warning", "amber");
    }

    /**
     * Creates an error alert (red).
     * @param message alert message
     * @return styled HBox
     */
    public static HBox error(String message) {
        return create(message, "error", "red");
    }

    /**
     * Creates a custom alert.
     * @param message alert message
     * @param type type label (INFO, SUCCESS, WARNING, ERROR)
     * @param color Tailwind color name
     * @return styled HBox
     */
    public static HBox create(String message, String type, String color) {
        HBox alert = new HBox();
        alert.setSpacing(12);
        alert.setPadding(new Insets(12, 16, 12, 16));
        
        String bg = getLightColor(color);
        String border = getColor(color);
        String fg = getDarkColor(color);
        
        alert.setStyle(String.format(
            "-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 1px;"
            + " -fx-border-radius: 8px; -fx-background-radius: 8px;", bg, border));

        // Icon/Type label
        Label typeLabel = new Label(type.toUpperCase());
        typeLabel.setStyle(String.format(
            "-fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: 12px;", fg));
        
        // Message
        Label msgLabel = new Label(message);
        msgLabel.setStyle(String.format(
            "-fx-text-fill: %s; -fx-font-size: 13px;", getDarkColor("gray")));
        
        alert.getChildren().addAll(typeLabel, msgLabel);
        alert.setAlignment(Pos.CENTER_LEFT);
        
        return alert;
    }

    /**
     * Creates an alert with a dismiss button.
     * @param message alert message
     * @param type alert type
     * @param color Tailwind color name
     * @param onDismiss callback when dismissed
     * @return styled HBox with dismiss button
     */
    public static HBox dismissible(String message, String type, String color, Runnable onDismiss) {
        HBox alert = create(message, type, color);
        
        Label closeBtn = new Label("✕");
        String fg = getDarkColor(color);
        closeBtn.setStyle(String.format(
            "-fx-text-fill: %s; -fx-font-weight: bold; -fx-cursor: hand;"
            + " -fx-padding: 4 8 4 8; -fx-background-radius: 4px;", fg));
        
        closeBtn.setOnMouseClicked(e -> {
            if (onDismiss != null) onDismiss.run();
            if (alert.getParent() instanceof Pane) {
                ((Pane) alert.getParent()).getChildren().remove(alert);
            }
        });
        
        HBox.setHgrow(closeBtn, Priority.NEVER);
        alert.getChildren().add(closeBtn);
        alert.setAlignment(Pos.CENTER_LEFT);
        
        return alert;
    }

    private static String getLightColor(String color) {
        try {
            Class<?> palette = Class.forName("io.github.yasmramos.tailwindfx.color.ColorPalette");
            java.lang.reflect.Method method = palette.getMethod("hex", String.class, int.class);
            return (String) method.invoke(null, color, 50);
        } catch (Exception e) {
            return "#eff6ff";
        }
    }

    private static String getColor(String color) {
        try {
            Class<?> palette = Class.forName("io.github.yasmramos.tailwindfx.color.ColorPalette");
            java.lang.reflect.Method method = palette.getMethod("hex", String.class, int.class);
            return (String) method.invoke(null, color, 200);
        } catch (Exception e) {
            return "#bfdbfe";
        }
    }

    private static String getDarkColor(String color) {
        try {
            Class<?> palette = Class.forName("io.github.yasmramos.tailwindfx.color.ColorPalette");
            java.lang.reflect.Method method = palette.getMethod("hex", String.class, int.class);
            return (String) method.invoke(null, color, 800);
        } catch (Exception e) {
            return "#1e40af";
        }
    }
}
