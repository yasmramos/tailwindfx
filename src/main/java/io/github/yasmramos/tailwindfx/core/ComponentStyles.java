package io.github.yasmramos.tailwindfx.core;

import io.github.yasmramos.tailwindfx.color.ColorPalette;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * ComponentStyles — Programmatic styling for JavaFX components.
 *
 * <p>Applies TailwindCSS-like component styles directly via JavaFX API,
 * eliminating the need for static CSS files.
 */
public final class ComponentStyles {

  private ComponentStyles() {}

  // ============================================================================
  // BUTTON STYLES
  // ============================================================================

  public static void applyButtonBase(Button btn) {
    btn.setStyle("-fx-background-radius: 6px; -fx-font-weight: 500; -fx-padding: 8px 16px;");
    btn.setCursor(javafx.scene.Cursor.HAND);
    btn.setFocusTraversable(true);
  }

  public static void applyButtonPrimary(Button btn, String colorName) {
    applyButtonBase(btn);
    btn.getStyleClass().addAll("btn", "btn-primary", "btn-" + colorName, "btn-md");
    
    String hexColor = ColorPalette.hex(colorName, 500);
    String hexHover = ColorPalette.hex(colorName, 600);
    String hexPressed = ColorPalette.hex(colorName, 700);
    
    Color color = hexColor != null ? Color.web(hexColor) : Color.BLUE;
    Color hoverColor = hexHover != null ? Color.web(hexHover) : Color.DARKBLUE;
    Color pressedColor = hexPressed != null ? Color.web(hexPressed) : Color.NAVY;

    btn.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, null)));
    btn.setTextFill(Color.WHITE);

    btn.setOnMouseEntered(e -> btn.setBackground(new Background(new BackgroundFill(hoverColor, CornerRadii.EMPTY, null))));
    btn.setOnMouseExited(e -> btn.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, null))));
    btn.setOnMousePressed(e -> btn.setBackground(new Background(new BackgroundFill(pressedColor, CornerRadii.EMPTY, null))));
    btn.setOnMouseReleased(e -> btn.setBackground(new Background(new BackgroundFill(hoverColor, CornerRadii.EMPTY, null))));
  }

  public static void applyButtonSecondary(Button btn, String colorName) {
    applyButtonBase(btn);
    btn.getStyleClass().addAll("btn", "btn-secondary", "btn-" + colorName, "btn-md");
    
    String hexBg = ColorPalette.hex(colorName, 100);
    String hexText = ColorPalette.hex(colorName, 700);
    String hexHoverBg = ColorPalette.hex(colorName, 200);
    
    Color bg = hexBg != null ? Color.web(hexBg) : Color.LIGHTGRAY;
    Color text = hexText != null ? Color.web(hexText) : Color.DARKGRAY;
    Color hoverBg = hexHoverBg != null ? Color.web(hexHoverBg) : Color.GRAY;

    btn.setBackground(new Background(new BackgroundFill(bg, CornerRadii.EMPTY, null)));
    btn.setTextFill(text);

    btn.setOnMouseEntered(e -> btn.setBackground(new Background(new BackgroundFill(hoverBg, CornerRadii.EMPTY, null))));
    btn.setOnMouseExited(e -> btn.setBackground(new Background(new BackgroundFill(bg, CornerRadii.EMPTY, null))));
  }

  public static void applyButtonOutline(Button btn, String colorName) {
    applyButtonBase(btn);
    btn.getStyleClass().addAll("btn", "btn-outline", "btn-" + colorName, "btn-md");
    
    String hexBorder = ColorPalette.hex(colorName, 500);
    String hexHoverBg = ColorPalette.hex(colorName, 50);
    String hexText = ColorPalette.hex(colorName, 700);
    
    Color borderColor = hexBorder != null ? Color.web(hexBorder) : Color.GRAY;
    Color hoverBg = hexHoverBg != null ? Color.web(hexHoverBg) : Color.WHITE;
    Color text = hexText != null ? Color.web(hexText) : Color.DARKGRAY;

    btn.setBackground(Background.EMPTY);
    btn.setTextFill(text);
    btn.setBorder(new Border(new BorderStroke(borderColor, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

    btn.setOnMouseEntered(e -> {
      btn.setBackground(new Background(new BackgroundFill(hoverBg, CornerRadii.EMPTY, null)));
    });
    btn.setOnMouseExited(e -> {
      btn.setBackground(Background.EMPTY);
    });
  }

  public static void applyButtonGhost(Button btn, String colorName) {
    applyButtonBase(btn);
    btn.getStyleClass().addAll("btn", "btn-ghost", "btn-" + colorName, "btn-md");
    
    String hexText = ColorPalette.hex(colorName, 700);
    String hexHoverBg = ColorPalette.hex(colorName, 100);
    
    Color text = hexText != null ? Color.web(hexText) : Color.DARKGRAY;
    Color hoverBg = hexHoverBg != null ? Color.web(hexHoverBg) : Color.LIGHTGRAY;

    btn.setBackground(Background.EMPTY);
    btn.setTextFill(text);

    btn.setOnMouseEntered(e -> btn.setBackground(new Background(new BackgroundFill(hoverBg, CornerRadii.EMPTY, null))));
    btn.setOnMouseExited(e -> btn.setBackground(Background.EMPTY));
  }

  public static void applyButtonDisabled(Button btn) {
    applyButtonBase(btn);
    btn.getStyleClass().addAll("btn", "btn-disabled", "btn-gray", "btn-md");
    
    Color bg = Color.GRAY;
    Color text = Color.LIGHTGRAY;

    btn.setBackground(new Background(new BackgroundFill(bg, CornerRadii.EMPTY, null)));
    btn.setTextFill(text);
    btn.setCursor(javafx.scene.Cursor.DEFAULT);
    btn.setDisable(true);
  }

  // ============================================================================
  // CARD STYLES
  // ============================================================================

  public static void applyCardBase(Pane pane) {
    Color bg = Color.WHITE;
    String hexBorder = ColorPalette.hex("gray", 200);
    Color borderColor = hexBorder != null ? Color.web(hexBorder) : Color.LIGHTGRAY;
    
    DropShadow shadow = new DropShadow();
    shadow.setRadius(4);
    shadow.setOffsetY(2);
    shadow.setColor(Color.color(0, 0, 0, 0.1));

    pane.setBackground(new Background(new BackgroundFill(bg, new CornerRadii(8), null)));
    pane.setBorder(new Border(new BorderStroke(borderColor, BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(1))));
    pane.setEffect(shadow);
    pane.setPadding(new javafx.geometry.Insets(16));
  }

  // ============================================================================
  // INPUT STYLES
  // ============================================================================

  public static void applyInputBase(TextInputControl input) {
    Color bg = Color.WHITE;
    String hexBorder = ColorPalette.hex("gray", 300);
    String hexFocus = ColorPalette.hex("blue", 500);
    
    Color border = hexBorder != null ? Color.web(hexBorder) : Color.LIGHTGRAY;
    Color focusBorder = hexFocus != null ? Color.web(hexFocus) : Color.BLUE;

    input.setBackground(new Background(new BackgroundFill(bg, new CornerRadii(6), null)));
    input.setBorder(new Border(new BorderStroke(border, BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(1))));
    input.setPadding(new javafx.geometry.Insets(8, 12, 8, 12));

    input.focusedProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal) {
        input.setBorder(new Border(new BorderStroke(focusBorder, BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(2))));
      } else {
        input.setBorder(new Border(new BorderStroke(border, BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(1))));
      }
    });
  }

  // ============================================================================
  // BADGE STYLES
  // ============================================================================

  public static void applyBadgeBase(Label label, String colorName) {
    String hexBg = ColorPalette.hex(colorName, 100);
    String hexText = ColorPalette.hex(colorName, 800);
    
    Color bg = hexBg != null ? Color.web(hexBg) : Color.LIGHTGRAY;
    Color text = hexText != null ? Color.web(hexText) : Color.DARKGRAY;

    label.setBackground(new Background(new BackgroundFill(bg, new CornerRadii(9999), null)));
    label.setTextFill(text);
    label.setPadding(new javafx.geometry.Insets(2, 8, 2, 8));
    label.setStyle("-fx-font-size: 12px; -fx-font-weight: 500;");
  }

  // ============================================================================
  // AVATAR STYLES
  // ============================================================================

  public static void applyAvatarBase(Region avatar) {
    avatar.setStyle("-fx-background-radius: 9999px;");
    avatar.setMinSize(40, 40);
    avatar.setMaxSize(40, 40);
    avatar.setPrefSize(40, 40);
  }
}
