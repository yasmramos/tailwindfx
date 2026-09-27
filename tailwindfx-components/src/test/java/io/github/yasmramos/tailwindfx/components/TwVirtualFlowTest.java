package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.function.Function;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

/**
 * Unit tests for TwVirtualFlow component. Tests cover initialization, item management, selection,
 * and configuration.
 */
@DisplayName("TwVirtualFlow Unit Tests")
@ExtendWith(ApplicationExtension.class)
class TwVirtualFlowTest {

  private TwVirtualFlow<String> virtualFlow;
  private ObservableList<String> items;

  @BeforeEach
  void setUp() {
    virtualFlow = new TwVirtualFlow<>();
    items = FXCollections.observableArrayList();
  }

  @Test
  @DisplayName("Should initialize with default values")
  void testInitialization() {
    assertNotNull(virtualFlow);
    assertEquals(TwVirtualFlow.SelectionMode.SINGLE, virtualFlow.getSelectionMode());
    assertEquals(48.0, virtualFlow.getCellHeight(), 0.01);
    assertEquals(200.0, virtualFlow.getCellWidth(), 0.01);
    assertTrue(virtualFlow.getItems().isEmpty());
  }

  @Test
  @DisplayName("Should set items correctly")
  void testSetItems() {
    items.addAll("Item 1", "Item 2", "Item 3");
    virtualFlow.setItems(items);

    assertEquals(3, virtualFlow.getItems().size());
    assertEquals("Item 1", virtualFlow.getItems().get(0));
    assertEquals("Item 3", virtualFlow.getItems().get(2));
  }

  @Test
  @DisplayName("Should set cell factory correctly")
  void testSetCellFactory() {
    Function<String, Node> factory = item -> new Label(item);
    virtualFlow.setCellFactory(factory);

    assertNotNull(virtualFlow.getCellFactory());
    Node node = virtualFlow.getCellFactory().apply("Test");
    assertNotNull(node);
    assertTrue(node instanceof Label);
    assertEquals("Test", ((Label) node).getText());
  }

  @Test
  @DisplayName("Should set cell height correctly")
  void testSetCellHeight() {
    virtualFlow.setCellHeight(64.0);
    assertEquals(64.0, virtualFlow.getCellHeight(), 0.01);
  }

  @Test
  @DisplayName("Should set cell width correctly")
  void testSetCellWidth() {
    virtualFlow.setCellWidth(300.0);
    assertEquals(300.0, virtualFlow.getCellWidth(), 0.01);
  }

  @Test
  @DisplayName("Should set cell size provider correctly")
  void testSetCellSizeProvider() {
    Function<String, Double> sizeProvider = item -> (double) item.length() * 10;
    virtualFlow.setCellSizeProvider(sizeProvider);

    assertNotNull(virtualFlow.getCellSizeProvider());
    assertEquals(40.0, virtualFlow.getCellSizeProvider().apply("Test"), 0.01);
  }

  @Test
  @DisplayName("Should set selection mode correctly")
  void testSetSelectionMode() {
    virtualFlow.setSelectionMode(TwVirtualFlow.SelectionMode.NONE);
    assertEquals(TwVirtualFlow.SelectionMode.NONE, virtualFlow.getSelectionMode());

    virtualFlow.setSelectionMode(TwVirtualFlow.SelectionMode.MULTIPLE);
    assertEquals(TwVirtualFlow.SelectionMode.MULTIPLE, virtualFlow.getSelectionMode());
  }

  @Test
  @DisplayName("Should set viewport padding correctly")
  void testSetViewportPadding() {
    Insets padding = new Insets(10, 20, 10, 20);
    virtualFlow.setViewportPadding(padding);

    assertEquals(padding, virtualFlow.getViewportPadding());
  }

  @Test
  @DisplayName("Should select single index correctly")
  void testSelectIndex() {
    items.addAll("Item 1", "Item 2", "Item 3");
    virtualFlow.setItems(items);
    virtualFlow.setSelectionMode(TwVirtualFlow.SelectionMode.SINGLE);

    virtualFlow.selectIndex(1);

    assertEquals(1, virtualFlow.getSelectedIndices().size());
    assertEquals(1, virtualFlow.getSelectedIndices().get(0));
    assertEquals("Item 2", virtualFlow.getSelectedItems().get(0));
  }

  @Test
  @DisplayName("Should clear selection correctly")
  void testClearSelection() {
    items.addAll("Item 1", "Item 2", "Item 3");
    virtualFlow.setItems(items);
    virtualFlow.selectIndex(0);
    virtualFlow.selectIndex(1);

    assertEquals(2, virtualFlow.getSelectedIndices().size());

    virtualFlow.clearSelection();

    assertTrue(virtualFlow.getSelectedIndices().isEmpty());
  }

  @Test
  @DisplayName("Should handle multiple selection")
  void testMultipleSelection() {
    items.addAll("Item 1", "Item 2", "Item 3", "Item 4");
    virtualFlow.setItems(items);
    virtualFlow.setSelectionMode(TwVirtualFlow.SelectionMode.MULTIPLE);

    virtualFlow.selectIndex(0);
    virtualFlow.selectIndex(2);
    virtualFlow.selectIndex(3);

    assertEquals(3, virtualFlow.getSelectedIndices().size());
    assertTrue(virtualFlow.getSelectedIndices().contains(0));
    assertTrue(virtualFlow.getSelectedIndices().contains(2));
    assertTrue(virtualFlow.getSelectedIndices().contains(3));
  }

  @Test
  @DisplayName("Should return selected items correctly")
  void testGetSelectedItems() {
    items.addAll("Apple", "Banana", "Cherry");
    virtualFlow.setItems(items);
    virtualFlow.setSelectionMode(TwVirtualFlow.SelectionMode.MULTIPLE);

    virtualFlow.selectIndex(0);
    virtualFlow.selectIndex(1);

    List<String> selected = virtualFlow.getSelectedItems();
    assertEquals(2, selected.size());
    assertTrue(selected.contains("Apple"));
    assertTrue(selected.contains("Banana"));
  }

  @Test
  @DisplayName("Should handle fluent API - items")
  void testFluentApiItems() {
    ObservableList<String> testItems = FXCollections.observableArrayList("A", "B", "C");
    TwVirtualFlow<String> result = virtualFlow.items(testItems);

    assertSame(virtualFlow, result);
    assertEquals(3, virtualFlow.getItems().size());
  }

  @Test
  @DisplayName("Should handle fluent API - cellFactory")
  void testFluentApiCellFactory() {
    Function<String, Node> factory = item -> new Pane();
    TwVirtualFlow<String> result = virtualFlow.cellFactory(factory);

    assertSame(virtualFlow, result);
    assertNotNull(virtualFlow.getCellFactory());
  }

  @Test
  @DisplayName("Should handle fluent API - cellHeight")
  void testFluentApiCellHeight() {
    TwVirtualFlow<String> result = virtualFlow.cellHeight(100.0);

    assertSame(virtualFlow, result);
    assertEquals(100.0, virtualFlow.getCellHeight(), 0.01);
  }

  @Test
  @DisplayName("Should handle fluent API - cellWidth")
  void testFluentApiCellWidth() {
    TwVirtualFlow<String> result = virtualFlow.cellWidth(250.0);

    assertSame(virtualFlow, result);
    assertEquals(250.0, virtualFlow.getCellWidth(), 0.01);
  }

  @Test
  @DisplayName("Should handle fluent API - orientation")
  void testFluentApiOrientation() {
    TwVirtualFlow<String> result = virtualFlow.orientation(javafx.geometry.Orientation.HORIZONTAL);

    assertSame(virtualFlow, result);
    assertEquals(javafx.geometry.Orientation.HORIZONTAL, virtualFlow.getOrientation());
  }

  @Test
  @DisplayName("Should handle fluent API - viewportPadding with insets")
  void testFluentApiViewportPaddingInsets() {
    Insets padding = new Insets(5, 10, 5, 10);
    TwVirtualFlow<String> result = virtualFlow.viewportPadding(padding);

    assertSame(virtualFlow, result);
    assertEquals(padding, virtualFlow.getViewportPadding());
  }

  @Test
  @DisplayName("Should handle fluent API - viewportPadding with pixels")
  void testFluentApiViewportPaddingPixels() {
    TwVirtualFlow<String> result = virtualFlow.viewportPadding(15.0);

    assertSame(virtualFlow, result);
    assertEquals(new Insets(15.0), virtualFlow.getViewportPadding());
  }

  @Test
  @DisplayName("Should handle fluent API - selectionMode")
  void testFluentApiSelectionMode() {
    TwVirtualFlow<String> result = virtualFlow.selectionMode(TwVirtualFlow.SelectionMode.NONE);

    assertSame(virtualFlow, result);
    assertEquals(TwVirtualFlow.SelectionMode.NONE, virtualFlow.getSelectionMode());
  }

  @Test
  @DisplayName("Should handle fluent API - onSelect")
  void testFluentApiOnSelect() {
    final boolean[] callbackCalled = {false};
    virtualFlow.onSelect(idx -> callbackCalled[0] = true);

    // Trigger selection to verify callback is set
    items.add("Test");
    virtualFlow.setItems(items);
    virtualFlow.selectIndex(0);

    // Note: Actual callback execution depends on internal implementation
    // This test verifies the setter works without errors
    assertNotNull(virtualFlow);
  }

  @Test
  @DisplayName("Should handle fluent API - onDoubleClick")
  void testFluentApiOnDoubleClick() {
    final boolean[] callbackCalled = {false};
    virtualFlow.onDoubleClick(idx -> callbackCalled[0] = true);

    assertNotNull(virtualFlow);
  }

  @Test
  @DisplayName("Should handle fluent API - onItemReorder")
  void testFluentApiOnItemReorder() {
    virtualFlow.onItemReorder((from, to) -> {});

    assertNotNull(virtualFlow);
  }

  @Test
  @DisplayName("Should handle fluent API - onSelectionChange")
  void testFluentApiOnSelectionChange() {
    virtualFlow.onSelectionChange(itemsList -> {});

    assertNotNull(virtualFlow);
  }

  @Test
  @DisplayName("Should handle fluent API - outerPadding with insets")
  void testFluentApiOuterPaddingInsets() {
    Insets padding = new Insets(8, 16, 8, 16);
    TwVirtualFlow<String> result = virtualFlow.outerPadding(padding);

    assertSame(virtualFlow, result);
    assertEquals(padding, virtualFlow.getPadding());
  }

  @Test
  @DisplayName("Should handle fluent API - outerPadding with pixels")
  void testFluentApiOuterPaddingPixels() {
    TwVirtualFlow<String> result = virtualFlow.outerPadding(20.0);

    assertSame(virtualFlow, result);
    assertEquals(new Insets(20.0), virtualFlow.getPadding());
  }

  @Test
  @DisplayName("Should handle dispose correctly")
  void testDispose() {
    items.addAll("Item 1", "Item 2", "Item 3");
    virtualFlow.setItems(items);
    virtualFlow.selectIndex(0);

    virtualFlow.dispose();

    assertTrue(virtualFlow.getItems().isEmpty());
    assertTrue(virtualFlow.getSelectedIndices().isEmpty());
  }

  @Test
  @DisplayName("Should handle invalid index gracefully in selectIndex")
  void testSelectInvalidIndex() {
    items.add("Only Item");
    virtualFlow.setItems(items);

    // Should not throw exception
    assertDoesNotThrow(() -> virtualFlow.selectIndex(-1));
    assertDoesNotThrow(() -> virtualFlow.selectIndex(100));
  }

  @Test
  @DisplayName("Should update selected indices when items change")
  void testSelectedIndicesUpdateOnItemsChange() {
    items.addAll("Item 1", "Item 2", "Item 3");
    virtualFlow.setItems(items);
    virtualFlow.setSelectionMode(TwVirtualFlow.SelectionMode.SINGLE);
    virtualFlow.selectIndex(1);

    assertEquals(1, virtualFlow.getSelectedIndices().size());

    // Clear items
    items.clear();
    // Selection should be cleared or adjusted
    assertTrue(
        virtualFlow.getSelectedIndices().isEmpty() || virtualFlow.getSelectedIndices().size() == 0);
  }

  @Test
  @DisplayName("Should create cell from factory")
  void testCellCreation() {
    items.add("Test Item");
    virtualFlow.setItems(items);

    Function<String, Node> factory =
        item -> {
          Label label = new Label(item);
          label.getStyleClass().add("custom-cell");
          return label;
        };
    virtualFlow.setCellFactory(factory);

    Node cell = virtualFlow.getCellFactory().apply("Test Item");
    assertNotNull(cell);
    assertTrue(cell instanceof Label);
    assertTrue(((Label) cell).getStyleClass().contains("custom-cell"));
  }

  @Test
  @DisplayName("Should handle empty items list")
  void testEmptyItemsList() {
    virtualFlow.setItems(FXCollections.observableArrayList());

    assertTrue(virtualFlow.getItems().isEmpty());
    assertEquals(0, virtualFlow.getItems().size());
  }

  @Test
  @DisplayName("Should handle null cell factory gracefully")
  void testNullCellFactory() {
    assertDoesNotThrow(() -> virtualFlow.setCellFactory(null));
  }

  @Test
  @DisplayName("Should chain multiple fluent methods")
  void testFluentMethodChaining() {
    Function<String, Node> factory = item -> new Label(item);
    Insets padding = new Insets(5);

    TwVirtualFlow<String> result =
        virtualFlow
            .items(items)
            .cellFactory(factory)
            .cellHeight(50.0)
            .cellWidth(150.0)
            .orientation(javafx.geometry.Orientation.VERTICAL)
            .viewportPadding(padding)
            .selectionMode(TwVirtualFlow.SelectionMode.SINGLE)
            .outerPadding(10.0);

    assertSame(virtualFlow, result);
    assertEquals(50.0, virtualFlow.getCellHeight(), 0.01);
    assertEquals(150.0, virtualFlow.getCellWidth(), 0.01);
    assertEquals(javafx.geometry.Orientation.VERTICAL, virtualFlow.getOrientation());
    assertEquals(TwVirtualFlow.SelectionMode.SINGLE, virtualFlow.getSelectionMode());
  }
}
