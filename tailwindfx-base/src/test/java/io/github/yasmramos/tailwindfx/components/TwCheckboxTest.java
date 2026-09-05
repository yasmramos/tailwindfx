package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/** Unit tests for TwCheckbox component. */
public class TwCheckboxTest extends ApplicationTest {

  @Override
  public void start(javafx.stage.Stage stage) {
    // Empty stage for TestFX
  }

  @Test
  public void testConstructor_NoArgs() {
    TwCheckbox checkbox = new TwCheckbox();

    assertNotNull(checkbox);
    assertTrue(checkbox.getStyleClass().contains("checkbox"));
    assertTrue(checkbox.getStyleClass().contains("checkbox-blue"));
    assertTrue(checkbox.getStyleClass().contains("checkbox-md"));
  }

  @Test
  public void testConstructor_WithText() {
    TwCheckbox checkbox = new TwCheckbox("Test Label");

    assertNotNull(checkbox);
    assertEquals("Test Label", checkbox.getText());
    assertTrue(checkbox.getStyleClass().contains("checkbox"));
  }

  @Test
  public void testSelectedProperty() {
    TwCheckbox checkbox = new TwCheckbox();

    checkbox.setSelected(true);
    assertTrue(checkbox.isSelected());

    checkbox.setSelected(false);
    assertFalse(checkbox.isSelected());
  }

  @Test
  public void testDisabledState() {
    TwCheckbox checkbox = new TwCheckbox();

    checkbox.setDisable(true);
    assertTrue(checkbox.isDisabled());

    checkbox.setDisable(false);
    assertFalse(checkbox.isDisabled());
  }

  @Test
  public void testOnActionHandler() {
    TwCheckbox checkbox = new TwCheckbox();
    final boolean[] triggered = {false};

    checkbox.setOnAction(e -> triggered[0] = true);
    assertNotNull(checkbox.getOnAction());
  }

  @Test
  public void testColorProperty() {
    TwCheckbox checkbox = new TwCheckbox();

    assertEquals("blue", checkbox.getColor());

    checkbox.setColor("red");
    assertEquals("red", checkbox.getColor());
    assertTrue(checkbox.getStyleClass().contains("checkbox-red"));
  }

  @Test
  public void testSizeProperty() {
    TwCheckbox checkbox = new TwCheckbox();

    assertEquals("md", checkbox.getSize());

    checkbox.setSize("lg");
    assertEquals("lg", checkbox.getSize());
    assertTrue(checkbox.getStyleClass().contains("checkbox-lg"));
  }

  @Test
  public void testErrorState() {
    TwCheckbox checkbox = new TwCheckbox();

    assertFalse(checkbox.isError());

    checkbox.setError(true);
    assertTrue(checkbox.isError());
    assertTrue(checkbox.getStyleClass().contains("checkbox-error"));

    checkbox.setError(false);
    assertFalse(checkbox.isError());
  }

  @Test
  public void testStaticFactory_Create() {
    TwCheckbox checkbox = TwCheckbox.create("Test");

    assertNotNull(checkbox);
    assertEquals("Test", checkbox.getText());
  }

  @Test
  public void testStaticFactory_Checked() {
    TwCheckbox checkbox = TwCheckbox.checked("Checked", true);

    assertTrue(checkbox.isSelected());
  }

  @Test
  public void testStaticFactory_Disabled() {
    TwCheckbox checkbox = TwCheckbox.disabled("Disabled");

    assertTrue(checkbox.isDisabled());
  }

  @Test
  public void testStaticFactory_Small() {
    TwCheckbox checkbox = TwCheckbox.small("Small");

    assertEquals("sm", checkbox.getSize());
  }

  @Test
  public void testStaticFactory_Large() {
    TwCheckbox checkbox = TwCheckbox.large("Large");

    assertEquals("lg", checkbox.getSize());
  }
}
