package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.control.Button;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

/** Unit tests for TwButton component. */
@ExtendWith(ApplicationExtension.class)
public class TwButtonTest {

  @Test
  public void testPrimaryButtonCreation() {
    Button btn = TwButton.primary("Save");

    assertNotNull(btn);
    assertEquals("Save", btn.getText());
    assertTrue(btn.getStyleClass().contains("btn"));
    assertTrue(btn.getStyleClass().contains("btn-primary"));
    assertTrue(btn.getStyleClass().contains("btn-blue"));
    assertTrue(btn.getStyleClass().contains("btn-md"));
  }

  @Test
  public void testPrimaryButtonWithCustomColor() {
    Button btn = TwButton.primary("Submit", "green");

    assertNotNull(btn);
    assertEquals("Submit", btn.getText());
    assertTrue(btn.getStyleClass().contains("btn-green"));
    assertFalse(btn.getStyleClass().contains("btn-blue"));
  }

  @Test
  public void testSecondaryButtonCreation() {
    Button btn = TwButton.secondary("Cancel");

    assertNotNull(btn);
    assertEquals("Cancel", btn.getText());
    assertTrue(btn.getStyleClass().contains("btn"));
    assertTrue(btn.getStyleClass().contains("btn-secondary"));
    assertTrue(btn.getStyleClass().contains("btn-gray"));
    assertTrue(btn.getStyleClass().contains("btn-md"));
  }

  @Test
  public void testSecondaryButtonWithCustomColor() {
    Button btn = TwButton.secondary("Maybe", "yellow");

    assertNotNull(btn);
    assertTrue(btn.getStyleClass().contains("btn-yellow"));
  }

  @Test
  public void testOutlineButtonCreation() {
    Button btn = TwButton.outline("Delete");

    assertNotNull(btn);
    assertEquals("Delete", btn.getText());
    assertTrue(btn.getStyleClass().contains("btn"));
    assertTrue(btn.getStyleClass().contains("btn-outline"));
    assertTrue(btn.getStyleClass().contains("btn-gray"));
    assertTrue(btn.getStyleClass().contains("btn-md"));
  }

  @Test
  public void testOutlineButtonWithCustomColor() {
    Button btn = TwButton.outline("Remove", "red");

    assertNotNull(btn);
    assertTrue(btn.getStyleClass().contains("btn-red"));
  }

  @Test
  public void testGhostButtonCreation() {
    Button btn = TwButton.ghost("More");

    assertNotNull(btn);
    assertEquals("More", btn.getText());
    assertTrue(btn.getStyleClass().contains("btn"));
    assertTrue(btn.getStyleClass().contains("btn-ghost"));
    assertTrue(btn.getStyleClass().contains("btn-gray"));
    assertTrue(btn.getStyleClass().contains("btn-md"));
  }

  @Test
  public void testGhostButtonWithCustomColor() {
    Button btn = TwButton.ghost("Info", "blue");

    assertNotNull(btn);
    assertTrue(btn.getStyleClass().contains("btn-blue"));
  }

  @Test
  public void testIconButtonCreation() {
    Button btn = TwButton.icon("🔍", "Search");

    assertNotNull(btn);
    assertEquals("🔍", btn.getText());
    assertEquals("Search", btn.getAccessibleText());
    assertTrue(btn.getStyleClass().contains("btn"));
    assertTrue(btn.getStyleClass().contains("btn-icon"));
    assertTrue(btn.getStyleClass().contains("btn-gray"));
    assertTrue(btn.getStyleClass().contains("btn-circle"));
  }

  @Test
  public void testIconButtonWithCustomColor() {
    Button btn = TwButton.icon("❤️", "Like", "red");

    assertNotNull(btn);
    assertTrue(btn.getStyleClass().contains("btn-red"));
  }

  @Test
  public void testDangerButtonCreation() {
    Button btn = TwButton.danger("Delete All");

    assertNotNull(btn);
    assertEquals("Delete All", btn.getText());
    assertTrue(btn.getStyleClass().contains("btn"));
    assertTrue(btn.getStyleClass().contains("btn-danger"));
    assertTrue(btn.getStyleClass().contains("btn-red"));
  }

  @Test
  public void testSuccessButtonCreation() {
    Button btn = TwButton.success("Confirm");

    assertNotNull(btn);
    assertEquals("Confirm", btn.getText());
    assertTrue(btn.getStyleClass().contains("btn"));
    assertTrue(btn.getStyleClass().contains("btn-primary"));
    assertTrue(btn.getStyleClass().contains("btn-green"));
  }

  @Test
  public void testDisabledButtonCreation() {
    Button btn = TwButton.disabled("Unavailable");

    assertNotNull(btn);
    assertEquals("Unavailable", btn.getText());
    assertTrue(btn.getStyleClass().contains("btn"));
    assertTrue(btn.getStyleClass().contains("btn-disabled"));
    assertTrue(btn.isDisabled());
  }

  @Test
  public void testPrimaryButtonWithNullText() {
    Button btn = TwButton.primary(null);

    assertNotNull(btn);
    assertNull(btn.getText());
  }

  @Test
  public void testSecondaryButtonWithNullText() {
    Button btn = TwButton.secondary(null);

    assertNotNull(btn);
    assertNull(btn.getText());
  }

  @Test
  public void testOutlineButtonWithEmptyText() {
    Button btn = TwButton.outline("");

    assertNotNull(btn);
    assertEquals("", btn.getText());
  }

  @Test
  public void testGhostButtonWithSpecialCharacters() {
    Button btn = TwButton.ghost("Test & < > \" '");

    assertNotNull(btn);
    assertEquals("Test & < > \" '", btn.getText());
  }

  @Test
  public void testIconButtonWithEmptyLabel() {
    Button btn = TwButton.icon("★", "");

    assertNotNull(btn);
    assertEquals("★", btn.getText());
    assertEquals("", btn.getAccessibleText());
  }

  @Test
  public void testAllButtonTypesHaveBtnBaseClass() {
    Button[] buttons = {
      TwButton.primary("1"),
      TwButton.secondary("2"),
      TwButton.outline("3"),
      TwButton.ghost("4"),
      TwButton.icon("★", "icon"),
      TwButton.danger("5"),
      TwButton.success("6"),
      TwButton.disabled("7")
    };

    for (Button btn : buttons) {
      assertTrue(
          btn.getStyleClass().contains("btn"),
          "Button should have 'btn' base class: " + btn.getText());
    }
  }

  @Test
  public void testButtonSizes() {
    Button btn = TwButton.primary("Test");

    assertTrue(btn.getStyleClass().contains("btn-md"));
  }
}
