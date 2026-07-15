package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.*;

import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for TwSelect component (ChoiceBox and ComboBox).
 */
public class TwSelectTest {

  @Test
  public void testChoiceBox_WithSingleOption() {
    ChoiceBox<String> cb = TwSelect.choiceBox("Option 1");
    
    assertNotNull(cb);
    assertEquals(1, cb.getItems().size());
    assertEquals("Option 1", cb.getValue());
    assertTrue(cb.getStyleClass().contains("select"));
    assertTrue(cb.getStyleClass().contains("select-md"));
  }

  @Test
  public void testChoiceBox_WithMultipleOptions() {
    ChoiceBox<String> cb = TwSelect.choiceBox("A", "B", "C");
    
    assertNotNull(cb);
    assertEquals(3, cb.getItems().size());
    assertEquals("A", cb.getValue());
  }

  @Test
  public void testChoiceBox_EmptyOptions() {
    ChoiceBox<String> cb = TwSelect.choiceBox();
    
    assertNotNull(cb);
    assertTrue(cb.getItems().isEmpty());
    assertNull(cb.getValue());
  }

  @Test
  public void testChoiceBoxWithPlaceholder_PreservesOptions() {
    ChoiceBox<String> cb = TwSelect.choiceBoxWithPlaceholder("Choose...", "X", "Y", "Z");
    
    assertNotNull(cb);
    assertEquals(3, cb.getItems().size());
    assertTrue(cb.getItems().containsAll(java.util.Arrays.asList("X", "Y", "Z")));
  }

  @Test
  public void testChoiceBoxLarge_LargeStyle() {
    ChoiceBox<String> cb = TwSelect.choiceBoxLarge("Item 1", "Item 2");
    
    assertNotNull(cb);
    assertTrue(cb.getStyleClass().contains("select-lg"));
  }

  @Test
  public void testChoiceBoxSmall_SmallStyle() {
    ChoiceBox<String> cb = TwSelect.choiceBoxSmall("Small 1");
    
    assertNotNull(cb);
    assertTrue(cb.getStyleClass().contains("select-sm"));
  }

  @Test
  public void testComboBox_WithOptions() {
    ComboBox<String> combo = TwSelect.comboBox("Select...", "A", "B");
    
    assertNotNull(combo);
    assertEquals(2, combo.getItems().size());
    assertTrue(combo.getStyleClass().contains("select"));
  }

  @Test
  public void testComboBoxEditable_EditableFlag() {
    ComboBox<String> combo = TwSelect.editable("Type here...");
    
    assertNotNull(combo);
    assertTrue(combo.isEditable());
  }
}
