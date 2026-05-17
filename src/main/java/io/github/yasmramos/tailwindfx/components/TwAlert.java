package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.TailwindFX;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 * TwAlert — Pre-styled alert/notification component.
 * 
 * Uses base .alert class from tailwindfx-components.css with utility modifiers.
 * 
 * <pre>
 * TwAlert info = TwAlert.info("Operation completed successfully");
 * TwAlert warning = TwAlert.warning("Please review before continuing");
 * TwAlert error = TwAlert.error("Something went wrong");
 * TwAlert success = TwAlert.success("Data saved!");
 * </pre>
 */
public class TwAlert extends HBox {

    /**
     * Creates an info alert (blue).
     * @param message alert message
     * @return styled TwAlert
     */
    public static TwAlert info(String message) {
        return create(message, "info", "blue");
    }

    /**
     * Creates a success alert (green).
     * @param message alert message
     * @return styled TwAlert
     */
    public static TwAlert success(String message) {
        return create(message, "success", "green");
    }

    /**
     * Creates a warning alert (yellow/amber).
     * @param message alert message
     * @return styled TwAlert
     */
    public static TwAlert warning(String message) {
        return create(message, "warning", "yellow");
    }

    /**
     * Creates an error alert (red).
     * @param message alert message
     * @return styled TwAlert
     */
    public static TwAlert error(String message) {
        return create(message, "error", "red");
    }

    /**
     * Creates a custom alert.
     * @param message alert message
     * @param type type label (INFO, SUCCESS, WARNING, ERROR)
     * @param color Tailwind color name
     * @return styled TwAlert
     */
    public static TwAlert create(String message, String type, String color) {
        TwAlert alert = new TwAlert();
        alert.setSpacing(12);
        alert.setPadding(new Insets(12, 16, 12, 16));
        
        // Apply base alert class and variant
        TailwindFX.apply(alert, "alert", "alert-" + color);

        // Icon/Type label
        Label typeLabel = new Label(type.toUpperCase());
        TailwindFX.apply(typeLabel, "font-bold", "text-sm", "text-" + color + "-800");
        
        // Message
        Label msgLabel = new Label(message);
        TailwindFX.apply(msgLabel, "text-gray-700");
        
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
     * @return styled TwAlert with dismiss button
     */
    public static TwAlert dismissible(String message, String type, String color, Runnable onDismiss) {
        TwAlert alert = create(message, type, color);
        
        Label closeBtn = new Label("✕");
        TailwindFX.apply(closeBtn, 
            "font-bold", 
            "cursor-hand",
            "px-2", "py-1",
            "rounded",
            "text-" + color + "-600",
            "hover:bg-" + color + "-100"
        );
        
        closeBtn.setOnMouseClicked(e -> {
            if (onDismiss != null) onDismiss.run();
            if (alert.getParent() instanceof Pane) {
                ((Pane) alert.getParent()).getChildren().remove(alert);
            }
        });
        
        HBox.setHgrow(closeBtn, Priority.NEVER);
        alert.getChildren().add(closeBtn);
        alert.setAlignment(Pos.CENTER_RIGHT);
        
        return alert;
    }

    /**
     * Protected constructor for internal usage.
     */
    protected TwAlert() {
        super();
    }
}
