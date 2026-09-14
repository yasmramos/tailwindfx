package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/** Unit tests for ComponentStyles class. */
class ComponentStylesTest extends ApplicationTest {

  private Button button;
  private Pane pane;
  private TextField textField;
  private Label label;
  private Region region;

  @Override
  public void start(javafx.stage.Stage stage) {
    button = new Button("Test Button");
    pane = new Pane();
    textField = new TextField();
    label = new Label("Test Label");
    region = new Region();

    pane.getChildren().addAll(button, textField, label, region);
    stage.setScene(new javafx.scene.Scene(pane, 400, 300));
    stage.show();
  }

  @BeforeEach
  void setUp() {
    // Reset button state
    button = new Button("Test Button");
    pane = new Pane();
    textField = new TextField();
    label = new Label("Test Label");
    region = new Region();
  }

  @Test
  void testApplyButtonBase() {
    interact(() -> ComponentStyles.applyButtonBase(button));

    assertTrue(button.getStyle().contains("-fx-background-radius: 6px"));
    assertTrue(button.getStyle().contains("-fx-font-weight: 500"));
    assertTrue(button.getStyle().contains("-fx-padding: 8px 16px"));
    assertEquals(javafx.scene.Cursor.HAND, button.getCursor());
    assertTrue(button.isFocusTraversable());
  }

  @Test
  void testApplyButtonPrimary() {
    interact(() -> ComponentStyles.applyButtonPrimary(button, "blue"));

    assertTrue(button.getStyleClass().contains("btn"));
    assertTrue(button.getStyleClass().contains("btn-primary"));
    assertTrue(button.getStyleClass().contains("btn-blue"));
    assertTrue(button.getStyleClass().contains("btn-md"));
    assertNotNull(button.getBackground());
    assertEquals(javafx.scene.paint.Color.WHITE, button.getTextFill());
  }

  @Test
  void testApplyButtonSecondary() {
    interact(() -> ComponentStyles.applyButtonSecondary(button, "green"));

    assertTrue(button.getStyleClass().contains("btn"));
    assertTrue(button.getStyleClass().contains("btn-secondary"));
    assertTrue(button.getStyleClass().contains("btn-green"));
    assertTrue(button.getStyleClass().contains("btn-md"));
    assertNotNull(button.getBackground());
  }

  @Test
  void testApplyButtonOutline() {
    interact(() -> ComponentStyles.applyButtonOutline(button, "red"));

    assertTrue(button.getStyleClass().contains("btn"));
    assertTrue(button.getStyleClass().contains("btn-outline"));
    assertTrue(button.getStyleClass().contains("btn-red"));
    assertTrue(button.getStyleClass().contains("btn-md"));
    assertNotNull(button.getBorder());
  }

  @Test
  void testApplyButtonGhost() {
    interact(() -> ComponentStyles.applyButtonGhost(button, "purple"));

    assertTrue(button.getStyleClass().contains("btn"));
    assertTrue(button.getStyleClass().contains("btn-ghost"));
    assertTrue(button.getStyleClass().contains("btn-purple"));
    assertTrue(button.getStyleClass().contains("btn-md"));
    assertEquals(javafx.scene.layout.Background.EMPTY, button.getBackground());
  }

  @Test
  void testApplyButtonDisabled() {
    interact(() -> ComponentStyles.applyButtonDisabled(button));

    assertTrue(button.getStyleClass().contains("btn"));
    assertTrue(button.getStyleClass().contains("btn-disabled"));
    assertTrue(button.getStyleClass().contains("btn-gray"));
    assertTrue(button.getStyleClass().contains("btn-md"));
    assertTrue(button.isDisabled());
    assertEquals(javafx.scene.Cursor.DEFAULT, button.getCursor());
  }

  @Test
  void testApplyCardBase() {
    interact(() -> ComponentStyles.applyCardBase(pane));

    assertNotNull(pane.getBackground());
    assertNotNull(pane.getBorder());
    assertNotNull(pane.getEffect());
    assertEquals(new javafx.geometry.Insets(16), pane.getPadding());
  }

  @Test
  void testApplyInputBase() {
    interact(() -> ComponentStyles.applyInputBase(textField));

    assertNotNull(textField.getBackground());
    assertNotNull(textField.getBorder());
    assertEquals(new javafx.geometry.Insets(8, 12, 8, 12), textField.getPadding());
  }

  @Test
  void testApplyBadgeBase() {
    interact(() -> ComponentStyles.applyBadgeBase(label, "yellow"));

    assertNotNull(label.getBackground());
    assertTrue(label.getStyle().contains("-fx-font-size: 12px"));
    assertTrue(label.getStyle().contains("-fx-font-weight: 500"));
    assertEquals(new javafx.geometry.Insets(2, 8, 2, 8), label.getPadding());
  }

  @Test
  void testApplyAvatarBase() {
    interact(() -> ComponentStyles.applyAvatarBase(region));

    assertTrue(region.getStyle().contains("-fx-background-radius: 9999px"));
    assertEquals(40, region.getMinWidth(), 0.01);
    assertEquals(40, region.getMinHeight(), 0.01);
    assertEquals(40, region.getPrefWidth(), 0.01);
    assertEquals(40, region.getPrefHeight(), 0.01);
  }

  @Test
  void testApplyButtonPrimaryWithInvalidColor() {
    interact(() -> ComponentStyles.applyButtonPrimary(button, "invalid-color"));

    // Should use default blue color
    assertNotNull(button.getBackground());
    assertTrue(button.getStyleClass().contains("btn-invalid-color"));
  }

  @Test
  void testApplyButtonSecondaryWithInvalidColor() {
    interact(() -> ComponentStyles.applyButtonSecondary(button, "nonexistent"));

    assertNotNull(button.getBackground());
  }

  @Test
  void testApplyButtonOutlineWithInvalidColor() {
    interact(() -> ComponentStyles.applyButtonOutline(button, "badcolor"));

    assertNotNull(button.getBorder());
  }

  @Test
  void testApplyButtonGhostWithInvalidColor() {
    interact(() -> ComponentStyles.applyButtonGhost(button, "unknown"));

    assertEquals(javafx.scene.layout.Background.EMPTY, button.getBackground());
  }

  @Test
  void testApplyBadgeBaseWithInvalidColor() {
    interact(() -> ComponentStyles.applyBadgeBase(label, "badcolor"));

    assertNotNull(label.getBackground());
  }
}
