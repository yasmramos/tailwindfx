package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.TailwindFX;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleButton;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for TwCheckbox and TwSwitch components.
 */
public class TwCheckboxTest {

  @Test
  public void testCreate_CheckboxWithText() {
    CheckBox checkbox = TwCheckbox.create("Accept terms");
    
    assertNotNull(checkbox);
    assertEquals("Accept terms", checkbox.getText());
    assertFalse(checkbox.isSelected());
    assertTrue(checkbox.getStyleClass().contains("checkbox"));
  }

  @Test
  public void testChecked_CheckboxInitialStateTrue() {
    CheckBox checkbox = TwCheckbox.checked("Remember me", true);
    
    assertNotNull(checkbox);
    assertEquals("Remember me", checkbox.getText());
    assertTrue(checkbox.isSelected());
    assertTrue(checkbox.getStyleClass().contains("checkbox"));
  }

  @Test
  public void testChecked_CheckboxInitialStateFalse() {
    CheckBox checkbox = TwCheckbox.checked("Enable feature", false);
    
    assertNotNull(checkbox);
    assertEquals("Enable feature", checkbox.getText());
    assertFalse(checkbox.isSelected());
    assertTrue(checkbox.getStyleClass().contains("checkbox"));
  }

  @Test
  public void testCreate_SwitchWithText() {
    ToggleButton switchBtn = TwSwitch.create("Dark mode");
    
    assertNotNull(switchBtn);
    assertEquals("Dark mode", switchBtn.getText());
    assertFalse(switchBtn.isSelected());
    assertTrue(switchBtn.getStyleClass().contains("switch"));
  }

  @Test
  public void testChecked_SwitchInitialStateTrue() {
    ToggleButton switchBtn = TwSwitch.checked("Notifications", true);
    
    assertNotNull(switchBtn);
    assertEquals("Notifications", switchBtn.getText());
    assertTrue(switchBtn.isSelected());
    assertTrue(switchBtn.getStyleClass().contains("switch"));
  }

  @Test
  public void testChecked_SwitchInitialStateFalse() {
    ToggleButton switchBtn = TwSwitch.checked("Airplane mode", false);
    
    assertNotNull(switchBtn);
    assertEquals("Airplane mode", switchBtn.getText());
    assertFalse(switchBtn.isSelected());
    assertTrue(switchBtn.getStyleClass().contains("switch"));
  }
}
