package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/** Unit tests for TwBadge component. */
public class TwBadgeTest extends ApplicationTest {

  @Override
  public void start(javafx.stage.Stage stage) {
    // Empty stage for TestFX
  }

  @Test
  public void testCreate_WithTextOnly() {
    TwBadge badge = TwBadge.create("NEW");

    assertNotNull(badge);
    assertEquals("NEW", badge.getText());
    assertTrue(badge.getStyleClass().contains("badge"));
    assertTrue(badge.getStyleClass().contains("badge-blue"));
    assertTrue(badge.getStyleClass().contains("badge-md"));
  }

  @Test
  public void testCreate_WithTextAndColor() {
    TwBadge badge = TwBadge.create("BETA", "red");

    assertNotNull(badge);
    assertEquals("BETA", badge.getText());
    assertTrue(badge.getStyleClass().contains("badge-red"));
    assertFalse(badge.getStyleClass().contains("badge-blue"));
  }

  @Test
  public void testCreate_TextIsUppercase() {
    TwBadge badge = TwBadge.create("new");

    assertEquals("NEW", badge.getText());
  }

  @Test
  public void testPill_WithTextOnly() {
    TwBadge pill = TwBadge.pill("Active");

    assertNotNull(pill);
    assertTrue(pill.getStyleClass().contains("badge-pill"));
    assertTrue(pill.getStyleClass().contains("badge-blue"));
  }

  @Test
  public void testPill_WithTextAndColor() {
    TwBadge pill = TwBadge.pill("Inactive", "gray");

    assertNotNull(pill);
    assertTrue(pill.getStyleClass().contains("badge-pill"));
    assertTrue(pill.getStyleClass().contains("badge-gray"));
  }

  @Test
  public void testDot_CreatesBadgeWithDot() {
    TwBadge.TwBadgeDot badgeDot = TwBadge.dot("Online", "green");

    assertNotNull(badgeDot);
    assertNotNull(badgeDot.getDot());
    assertNotNull(badgeDot.getLabel());
    assertEquals(2, badgeDot.getChildren().size());
    assertTrue(badgeDot.getDot().getStyleClass().contains("dot-green"));
    assertTrue(badgeDot.getDot().getStyleClass().contains("dot-sm"));
  }

  @Test
  public void testDot_WithCustomColors() {
    TwBadge.TwBadgeDot badgeDot = TwBadge.dot("Status", "red", "blue");

    assertNotNull(badgeDot);
    assertTrue(badgeDot.getDot().getStyleClass().contains("dot-red"));
    assertTrue(badgeDot.getLabel().getStyleClass().contains("badge-blue"));
  }

  @Test
  public void testOutline_WithTextOnly() {
    TwBadge outline = TwBadge.outline("PRO");

    assertNotNull(outline);
    assertTrue(outline.getStyleClass().contains("badge-outline"));
    assertTrue(outline.getStyleClass().contains("badge-gray"));
    assertTrue(outline.getStyleClass().contains("badge-md"));
  }

  @Test
  public void testOutline_WithCustomColor() {
    TwBadge outline = TwBadge.outline("VIP", "purple");

    assertNotNull(outline);
    assertTrue(outline.getStyleClass().contains("badge-purple"));
    assertFalse(outline.getStyleClass().contains("badge-gray"));
  }

  @Test
  public void testBadgeDot_HasSpacing() {
    TwBadge.TwBadgeDot badgeDot = TwBadge.dot("Test", "green");

    assertEquals(4, badgeDot.getSpacing(), 0.01);
  }

  @Test
  public void testBadgeDot_Alignment() {
    TwBadge.TwBadgeDot badgeDot = TwBadge.dot("Test", "green");

    assertEquals(javafx.geometry.Pos.CENTER_LEFT, badgeDot.getAlignment());
  }

  @Test
  public void testBadgeDot_LabelPadding() {
    TwBadge.TwBadgeDot badgeDot = TwBadge.dot("Test", "green");

    javafx.geometry.Insets padding = badgeDot.getLabel().getPadding();
    assertEquals(0, padding.getTop(), 0.01);
    assertEquals(0, padding.getRight(), 0.01);
    assertEquals(0, padding.getBottom(), 0.01);
    assertEquals(0, padding.getLeft(), 0.01);
  }

  @Test
  public void testDifferentColors() {
    String[] colors = {"blue", "green", "red", "yellow", "purple", "gray"};

    for (String color : colors) {
      TwBadge badge = TwBadge.create("T", color);
      assertTrue(badge.getStyleClass().contains("badge-" + color), "Should contain badge-" + color);
    }
  }

  @Test
  public void testBadgeDot_Getters() {
    TwBadge.TwBadgeDot badgeDot = TwBadge.dot("Status", "green");

    assertNotNull(badgeDot.getDot());
    assertNotNull(badgeDot.getLabel());
    assertSame(badgeDot.getDot(), badgeDot.getChildren().get(0));
    assertSame(badgeDot.getLabel(), badgeDot.getChildren().get(1));
  }
}
