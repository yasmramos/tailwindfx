package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.TwStyle;
import io.github.yasmramos.tailwindfx.animation.FxAnimation;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * TwAvatar — Pre-styled avatar component.
 * 
 * <pre>
 * StackPane avatar = TwAvatar.create("JD", "blue");
 * StackPane imgAvatar = TwAvatar.fromImage(imageView);
 * StackPane group = TwAvatar.group(avatar1, avatar2, avatar3);
 * </pre>
 */
public final class TwAvatar {

    private TwAvatar() {}

    /**
     * Creates an avatar with initials.
     * @param initials the initials to display (e.g. "JD", "A")
     * @return styled StackPane with default blue color and 40px size
     */
    public static StackPane create(String initials) {
        return create(initials, "blue", 40);
    }

    /**
     * Creates an avatar with initials and custom color.
     * @param initials the initials to display
     * @param color Tailwind color name
     * @return styled StackPane with 40px size
     */
    public static StackPane create(String initials, String color) {
        return create(initials, color, 40);
    }

    /**
     * Creates an avatar with initials, color, and custom size.
     * @param initials the initials to display
     * @param color Tailwind color name
     * @param size diameter in pixels
     * @return styled StackPane
     */
    public static StackPane create(String initials, String color, double size) {
        StackPane avatar = new StackPane();
        
        String bg = getLightColor(color);
        String fg = getDarkColor(color);
        
        avatar.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: %s;"
            + " -fx-background-radius: 999px; -fx-min-width: %.0fpx;"
            + " -fx-min-height: %.0fpx; -fx-max-width: %.0fpx; -fx-max-height: %.0fpx;",
            bg, fg, size, size, size, size));
        avatar.setPadding(new Insets(0));
        
        Label lbl = new Label(initials.toUpperCase());
        lbl.setStyle(String.format(
            "-fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: %.0fpx;",
            fg, size * 0.4));
        
        avatar.getChildren().add(lbl);
        StackPane.setAlignment(lbl, Pos.CENTER);
        
        return avatar;
    }

    /**
     * Creates an avatar from an image node.
     * @param image the image node (ImageView)
     * @return styled StackPane with 40px size
     */
    public static StackPane fromImage(Node image) {
        return fromImage(image, 40);
    }

    /**
     * Creates an avatar from an image node with custom size.
     * @param image the image node (ImageView)
     * @param size diameter in pixels
     * @return styled StackPane
     */
    public static StackPane fromImage(Node image, double size) {
        StackPane avatar = new StackPane();
        
        avatar.setStyle(String.format(
            "-fx-background-radius: 999px; -fx-min-width: %.0fpx;"
            + " -fx-min-height: %.0fpx; -fx-max-width: %.0fpx; -fx-max-height: %.0fpx;"
            + " -fx-border-color: #e5e7eb; -fx-border-width: 2px;",
            size, size, size, size));
        
        // Clip to circle
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(size / 2, size / 2, size / 2);
        avatar.setClip(clip);
        
        if (image instanceof javafx.scene.image.ImageView) {
            javafx.scene.image.ImageView imgView = (javafx.scene.image.ImageView) image;
            imgView.setFitWidth(size);
            imgView.setFitHeight(size);
            imgView.setPreserveRatio(true);
        }
        
        avatar.getChildren().add(image);
        
        return avatar;
    }

    /**
     * Creates an avatar group (overlapping avatars).
     * @param avatars array of avatar nodes
     * @return HBox with overlapping avatars
     */
    public static javafx.scene.layout.HBox group(StackPane... avatars) {
        javafx.scene.layout.HBox group = new javafx.scene.layout.HBox();
        group.setSpacing(-12); // Overlap
        
        for (StackPane avatar : avatars) {
            // Add border to each avatar in group
            String existingStyle = avatar.getStyle();
            if (!existingStyle.contains("-fx-border-color")) {
                avatar.setStyle(existingStyle + " -fx-border-color: #ffffff; -fx-border-width: 2px;");
            }
            group.getChildren().add(avatar);
        }
        
        return group;
    }

    /**
     * Creates an online status indicator for an avatar.
     * @param avatar the avatar to wrap
     * @param isOnline true for online (green), false for offline (gray)
     * @return StackPane containing avatar with status dot
     */
    public static StackPane withStatus(StackPane avatar, boolean isOnline) {
        StackPane container = new StackPane();
        container.getChildren().add(avatar);
        
        double size = avatar.getMinWidth();
        double dotSize = size * 0.3;
        
        javafx.scene.shape.Circle statusDot = new javafx.scene.shape.Circle(dotSize / 2, dotSize / 2, dotSize / 2);
        statusDot.setFill(isOnline ? 
            javafx.scene.paint.Color.web("#22c55e") : // green-500
            javafx.scene.paint.Color.web("#9ca3af"));  // gray-400
        
        statusDot.setStroke(javafx.scene.paint.Color.WHITE);
        statusDot.setStrokeWidth(2);
        
        StackPane.setAlignment(statusDot, Pos.BOTTOM_RIGHT);
        container.getChildren().add(statusDot);
        
        // Adjust position
        statusDot.setTranslateX(size * 0.15);
        statusDot.setTranslateY(size * 0.15);
        
        return container;
    }

    private static String getLightColor(String color) {
        try {
            Class<?> palette = Class.forName("io.github.yasmramos.tailwindfx.color.ColorPalette");
            java.lang.reflect.Method method = palette.getMethod("hex", String.class, int.class);
            return (String) method.invoke(null, color, 100);
        } catch (Exception e) {
            return "#dbeafe";
        }
    }

    private static String getDarkColor(String color) {
        try {
            Class<?> palette = Class.forName("io.github.yasmramos.tailwindfx.color.ColorPalette");
            java.lang.reflect.Method method = palette.getMethod("hex", String.class, int.class);
            return (String) method.invoke(null, color, 700);
        } catch (Exception e) {
            return "#1e40af";
        }
    }
}
