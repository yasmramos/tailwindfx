package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

/** Unit tests for TwSelect component. */
@ExtendWith(ApplicationExtension.class)
public class TwSelectTest {

  @Test
  public void testChoiceBoxCreation() {
    ChoiceBox<String> cb = TwSelect.choiceBox("Option 1", "Option 2", "Option 3");

    assertNotNull(cb);
    assertTrue(cb.getStyleClass().contains("select"));
    assertTrue(cb.getStyleClass().contains("select-base"));
    assertEquals(3, cb.getItems().size());
    assertEquals("Option 1", cb.getValue());
  }

  @Test
  public void testChoiceBoxWithSingleOption() {
    ChoiceBox<String> cb = TwSelect.choiceBox("Only Option");

    assertNotNull(cb);
    assertEquals(1, cb.getItems().size());
    assertEquals("Only Option", cb.getValue());
  }

  @Test
  public void testChoiceBoxWithNoOptions() {
    ChoiceBox<String> cb = TwSelect.choiceBox();

    assertNotNull(cb);
    assertTrue(cb.getItems().isEmpty());
    assertNull(cb.getValue());
    assertTrue(cb.getStyleClass().contains("select"));
    assertTrue(cb.getStyleClass().contains("select-base"));
  }

  @Test
  public void testChoiceBoxWithIntegerOptions() {
    ChoiceBox<Integer> cb = TwSelect.choiceBox(1, 2, 3);

    assertNotNull(cb);
    assertEquals(3, cb.getItems().size());
    assertEquals(1, cb.getValue());
  }

  @Test
  public void testComboBoxCreation() {
    ComboBox<String> combo = TwSelect.comboBox("A", "B", "C");

    assertNotNull(combo);
    assertTrue(combo.getStyleClass().contains("select"));
    assertTrue(combo.getStyleClass().contains("select-base"));
    assertEquals(3, combo.getItems().size());
    assertFalse(combo.isEditable());
  }

  @Test
  public void testComboBoxWithSingleOption() {
    ComboBox<String> combo = TwSelect.comboBox("Only");

    assertNotNull(combo);
    assertEquals(1, combo.getItems().size());
  }

  @Test
  public void testComboBoxWithNoOptions() {
    ComboBox<String> combo = TwSelect.comboBox();

    assertNotNull(combo);
    assertTrue(combo.getItems().isEmpty());
    assertTrue(combo.getStyleClass().contains("select"));
    assertTrue(combo.getStyleClass().contains("select-base"));
  }

  @Test
  public void testEditableComboBox() {
    ComboBox<String> combo = TwSelect.editable("Type or select...");

    assertNotNull(combo);
    assertTrue(combo.isEditable());
    assertEquals("Type or select...", combo.getPromptText());
    assertTrue(combo.getStyleClass().contains("select"));
    assertTrue(combo.getStyleClass().contains("select-base"));
    assertTrue(combo.getItems().isEmpty());
  }

  @Test
  public void testEditableComboBoxWithEmptyPlaceholder() {
    ComboBox<String> combo = TwSelect.editable("");

    assertNotNull(combo);
    assertTrue(combo.isEditable());
    assertEquals("", combo.getPromptText());
  }

  @Test
  public void testDisabledChoiceBox() {
    ChoiceBox<String> cb = TwSelect.disabledChoiceBox("Opt1", "Opt2");

    assertNotNull(cb);
    assertTrue(cb.isDisabled());
    assertTrue(cb.getStyleClass().contains("select"));
    assertTrue(cb.getStyleClass().contains("select-disabled"));
    assertEquals(2, cb.getItems().size());
    assertEquals("Opt1", cb.getValue());
  }

  @Test
  public void testDisabledChoiceBoxWithNoOptions() {
    ChoiceBox<String> cb = TwSelect.disabledChoiceBox();

    assertNotNull(cb);
    assertTrue(cb.isDisabled());
    assertTrue(cb.getStyleClass().contains("select"));
    assertTrue(cb.getStyleClass().contains("select-disabled"));
    assertTrue(cb.getItems().isEmpty());
    assertNull(cb.getValue());
  }

  @Test
  public void testDisabledComboBox() {
    ComboBox<String> combo = TwSelect.disabledComboBox("A", "B");

    assertNotNull(combo);
    assertTrue(combo.isDisabled());
    assertTrue(combo.getStyleClass().contains("select"));
    assertTrue(combo.getStyleClass().contains("select-disabled"));
    assertEquals(2, combo.getItems().size());
    assertEquals("A", combo.getValue());
  }

  @Test
  public void testDisabledComboBoxWithNoOptions() {
    ComboBox<String> combo = TwSelect.disabledComboBox();

    assertNotNull(combo);
    assertTrue(combo.isDisabled());
    assertTrue(combo.getStyleClass().contains("select"));
    assertTrue(combo.getStyleClass().contains("select-disabled"));
    assertTrue(combo.getItems().isEmpty());
    assertNull(combo.getValue());
  }

  @Test
  public void testErrorComboBox() {
    ComboBox<String> combo = TwSelect.error("Select an option", "X", "Y");

    assertNotNull(combo);
    assertEquals("Select an option", combo.getPromptText());
    assertTrue(combo.getStyleClass().contains("select"));
    assertTrue(combo.getStyleClass().contains("select-error"));
    assertEquals(2, combo.getItems().size());
    assertFalse(combo.isDisabled());
  }

  @Test
  public void testErrorComboBoxWithNoOptions() {
    ComboBox<String> combo = TwSelect.error("No options available");

    assertNotNull(combo);
    assertEquals("No options available", combo.getPromptText());
    assertTrue(combo.getStyleClass().contains("select"));
    assertTrue(combo.getStyleClass().contains("select-error"));
    assertTrue(combo.getItems().isEmpty());
  }

  @Test
  public void testChoiceBoxHasCorrectStyleClasses() {
    ChoiceBox<String> cb = TwSelect.choiceBox("Test");

    assertTrue(cb.getStyleClass().contains("select"));
    assertTrue(cb.getStyleClass().contains("select-base"));
  }

  @Test
  public void testComboBoxHasCorrectStyleClasses() {
    ComboBox<String> combo = TwSelect.comboBox("Test");

    assertTrue(combo.getStyleClass().contains("select"));
    assertTrue(combo.getStyleClass().contains("select-base"));
  }

  @Test
  public void testDisabledChoiceBoxDoesNotHaveBaseClass() {
    ChoiceBox<String> cb = TwSelect.disabledChoiceBox("Test");

    assertTrue(cb.getStyleClass().contains("select"));
    assertTrue(cb.getStyleClass().contains("select-disabled"));
    assertFalse(cb.getStyleClass().contains("select-base"));
  }

  @Test
  public void testDisabledComboBoxDoesNotHaveBaseClass() {
    ComboBox<String> combo = TwSelect.disabledComboBox("Test");

    assertTrue(combo.getStyleClass().contains("select"));
    assertTrue(combo.getStyleClass().contains("select-disabled"));
    assertFalse(combo.getStyleClass().contains("select-base"));
  }

  @Test
  public void testErrorComboBoxDoesNotHaveBaseClass() {
    ComboBox<String> combo = TwSelect.error("Prompt", "Test");

    assertTrue(combo.getStyleClass().contains("select"));
    assertTrue(combo.getStyleClass().contains("select-error"));
    assertFalse(combo.getStyleClass().contains("select-base"));
  }

  @Test
  public void testAllSelectComponentsHaveSelectBaseClass() {
    ChoiceBox<String> cb = TwSelect.choiceBox("Test");
    ComboBox<String> combo = TwSelect.comboBox("Test");

    assertTrue(cb.getStyleClass().contains("select"));
    assertTrue(combo.getStyleClass().contains("select"));
  }
}
