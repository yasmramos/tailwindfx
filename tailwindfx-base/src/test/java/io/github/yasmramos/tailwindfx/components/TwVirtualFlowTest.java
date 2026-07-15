package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.*;

import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for TwVirtualFlow component.
 */
public class TwVirtualFlowTest {

  @Test
  public void testCreate_EmptyVirtualFlow() {
    TwVirtualFlow<String> flow = TwVirtualFlow.create();
    
    assertNotNull(flow);
    assertTrue(flow.getItems().isEmpty());
  }

  @Test
  public void testCreate_WithItems() {
    TwVirtualFlow<String> flow = TwVirtualFlow.create("A", "B", "C");
    
    assertNotNull(flow);
    assertEquals(3, flow.getItems().size());
    assertEquals("A", flow.getItems().get(0));
  }

  @Test
  public void testSetCellFactory_CustomFactory() {
    TwVirtualFlow<String> flow = TwVirtualFlow.create();
    
    flow.setCellFactory(item -> new Label("Custom: " + item));
    
    assertNotNull(flow.getCellFactory());
    Label label = (Label) flow.getCellFactory().apply("test");
    assertEquals("Custom: test", label.getText());
  }

  @Test
  public void testAddItem_ToEmptyList() {
    TwVirtualFlow<String> flow = TwVirtualFlow.create();
    
    flow.addItem("First");
    
    assertEquals(1, flow.getItems().size());
    assertEquals("First", flow.getItems().get(0));
  }

  @Test
  public void testRemoveItem_FromList() {
    TwVirtualFlow<String> flow = TwVirtualFlow.create("X", "Y", "Z");
    
    flow.removeItem("Y");
    
    assertEquals(2, flow.getItems().size());
    assertFalse(flow.getItems().contains("Y"));
  }

  @Test
  public void testClear_AllItemsRemoved() {
    TwVirtualFlow<String> flow = TwVirtualFlow.create("1", "2", "3");
    
    flow.clear();
    
    assertTrue(flow.getItems().isEmpty());
  }

  @Test
  public void testSetSelectionMode_Single() {
    TwVirtualFlow<String> flow = TwVirtualFlow.create();
    
    flow.setSelectionMode(TwVirtualFlow.SelectionMode.SINGLE);
    
    assertEquals(TwVirtualFlow.SelectionMode.SINGLE, flow.getSelectionMode());
  }

  @Test
  public void testSetOrientation_Horizontal() {
    TwVirtualFlow<String> flow = TwVirtualFlow.create();
    
    flow.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
    
    assertEquals(javafx.geometry.Orientation.HORIZONTAL, flow.getOrientation());
  }

  @Test
  public void testSetItems_ReplaceAllItems() {
    TwVirtualFlow<String> flow = TwVirtualFlow.create("Old");
    
    flow.setItems(FXCollections.observableArrayList("New1", "New2"));
    
    assertEquals(2, flow.getItems().size());
    assertEquals("New1", flow.getItems().get(0));
  }
}
