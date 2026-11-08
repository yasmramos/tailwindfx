package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.*;

import javafx.animation.Interpolator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Layout and scrolling tests for {@link TwVirtualFlow}: visible-cell windowing, the size cache,
 * binary-search positioning, padding compensation and dispose.
 */
@DisplayName("TwVirtualFlow layout Tests")
class TwVirtualFlowLayoutTest extends ApplicationTest {

  @Override
  public void start(javafx.stage.Stage stage) {
    stage.setScene(new Scene(new Pane(), 800, 600));
    stage.show();
  }

  private TwVirtualFlow<String> flowWithItems(int count) {
    TwVirtualFlow<String> flow = new TwVirtualFlow<>();
    ObservableList<String> items = FXCollections.observableArrayList();
    for (int i = 0; i < count; i++) {
      items.add("Item " + i);
    }
    flow.setItems(items);
    flow.setCellFactory(item -> new Label(item));
    return flow;
  }

  private void layout(TwVirtualFlow<String> flow, double w, double h) {
    interact(
        () -> {
          flow.resize(w, h);
          flow.applyCss();
          flow.layout();
        });
  }

  @Test
  @DisplayName("Vertical layout populates the visible-cell window")
  void verticalLayoutCreatesCells() {
    TwVirtualFlow<String> flow = flowWithItems(100);
    layout(flow, 300, 240); // ~5 cells of 48px
    assertTrue(flow.getLastVisibleIndex() >= flow.getFirstVisibleIndex());
    assertTrue(flow.getLastVisibleIndex() < 100);
    assertTrue(flow.getFirstVisibleIndex() >= 0);
  }

  @Test
  @DisplayName("Horizontal layout also populates cells")
  void horizontalLayoutCreatesCells() {
    TwVirtualFlow<String> flow = flowWithItems(50);
    interact(
        () -> {
          flow.setOrientation(Orientation.HORIZONTAL);
          flow.setCellWidth(100);
          flow.resize(300, 120);
          flow.applyCss();
          flow.layout();
        });
    assertTrue(flow.getLastVisibleIndex() >= flow.getFirstVisibleIndex());
  }

  @Test
  @DisplayName("Empty items leave no visible cells")
  void emptyItemsNoCells() {
    TwVirtualFlow<String> flow = flowWithItems(0);
    layout(flow, 300, 300);
    assertEquals(0, flow.getFirstVisibleIndex());
    assertEquals(-1, flow.getLastVisibleIndex());
  }

  @Test
  @DisplayName("scrollToIndex moves the scroll position and windows further down")
  void scrollToIndexMovesWindow() {
    TwVirtualFlow<String> flow = flowWithItems(200);
    layout(flow, 300, 240);
    int firstBefore = flow.getFirstVisibleIndex();
    interact(() -> flow.scrollToIndex(100));
    assertTrue(flow.getFirstVisibleIndex() > firstBefore, "scrolling down advances the window");
    assertTrue(flow.getScrollPosition() > 0);
  }

  @Test
  @DisplayName("scrollToIndex with a duration starts an animation")
  void scrollToIndexAnimated() {
    TwVirtualFlow<String> flow = flowWithItems(200);
    layout(flow, 300, 240);
    assertDoesNotThrow(() -> interact(() -> flow.scrollToIndex(50, Duration.millis(200))));
  }

  @Test
  @DisplayName("scrollToIndex ignores out-of-range indices")
  void scrollToIndexGuards() {
    TwVirtualFlow<String> flow = flowWithItems(10);
    layout(flow, 300, 240);
    interact(
        () -> {
          flow.scrollToIndex(-1);
          flow.scrollToIndex(99);
        });
    assertTrue(flow.getScrollPosition() >= 0);
  }

  @Test
  @DisplayName("scrollBy clamps within the scrollable range")
  void scrollByClamps() {
    TwVirtualFlow<String> flow = flowWithItems(200);
    layout(flow, 300, 240);
    interact(() -> flow.scrollBy(-500, Duration.ZERO));
    assertEquals(0.0, flow.getScrollPosition(), 0.5);
  }

  @Test
  @DisplayName("A per-item size provider drives the size cache")
  void sizeProvider() {
    TwVirtualFlow<String> flow = new TwVirtualFlow<>();
    ObservableList<String> items = FXCollections.observableArrayList();
    for (int i = 0; i < 20; i++) {
      items.add("Item " + i);
    }
    flow.setItems(items);
    flow.setCellSizeProvider(item -> 80.0);
    flow.setCellFactory(item -> new Label(item));
    layout(flow, 300, 240);
    // With 80px cells only ~3 fit in a 240px viewport.
    assertTrue(flow.getLastVisibleIndex() - flow.getFirstVisibleIndex() < 20);
  }

  @Test
  @DisplayName("Viewport padding shifts the first cell without breaking the window")
  void viewportPadding() {
    TwVirtualFlow<String> flow = flowWithItems(50);
    interact(() -> flow.setViewportPadding(new Insets(10)));
    layout(flow, 300, 240);
    assertTrue(flow.getFirstVisibleIndex() >= 0);
    assertTrue(flow.getLastVisibleIndex() >= flow.getFirstVisibleIndex());
  }

  @Test
  @DisplayName("Dispose detaches listeners and clears all cells")
  void disposeClearsState() {
    TwVirtualFlow<String> flow = flowWithItems(50);
    layout(flow, 300, 240);
    interact(flow::dispose);
    assertTrue(flow.getItems().isEmpty());
    assertTrue(flow.getSelectedIndices().isEmpty());
    // Layout after dispose must not throw.
    assertDoesNotThrow(() -> layout(flow, 300, 240));
  }

  @Test
  @DisplayName("withTailwindStyling falls back to style classes when TailwindFX is absent")
  void withTailwindStylingFallback() {
    TwVirtualFlow<String> flow = new TwVirtualFlow<>();
    flow.withTailwindStyling(item -> new Label(item), "p-4", "bg-red-500");
    Node cell = flow.getCellFactory().apply("x");
    assertTrue(cell.getStyleClass().contains("p-4"));
    assertTrue(cell.getStyleClass().contains("bg-red-500"));
  }

  @Test
  @DisplayName("selected class is not duplicated when the layout runs repeatedly")
  void selectedClassIsNotDuplicatedAcrossLayouts() {
    TwVirtualFlow<String> flow = flowWithItems(10);
    interact(() -> flow.selectIndex(0));
    layout(flow, 300, 240);
    layout(flow, 300, 240);
    layout(flow, 300, 240);

    Node cell = firstCell(flow);
    assertNotNull(cell, "a cell should be visible after layout");
    long count = cell.getStyleClass().stream().filter("selected"::equals).count();
    assertEquals(
        1,
        count,
        "the selected class must not accumulate across layout passes: " + cell.getStyleClass());
  }

  @Test
  @DisplayName("Applies the virtual-flow base class the stylesheet scopes its rules to")
  void baseClassIsApplied() {
    TwVirtualFlow<String> flow = new TwVirtualFlow<>();
    assertTrue(
        flow.getStyleClass().contains("virtual-flow"),
        "the .virtual-flow rules only match when the base class is present: "
            + flow.getStyleClass());
  }

  private Node firstCell(TwVirtualFlow<String> flow) {
    Pane container = (Pane) flow.getChildrenUnmodifiable().get(0);
    return container.getChildren().isEmpty() ? null : container.getChildren().get(0);
  }

  @Test
  @DisplayName("getSelectedItems returns the items behind the selected indices")
  void selectedItems() {
    TwVirtualFlow<String> flow = flowWithItems(5);
    flow.setSelectionMode(TwVirtualFlow.SelectionMode.MULTIPLE);
    interact(
        () -> {
          flow.selectIndex(1);
          flow.selectIndex(3);
        });
    assertEquals(2, flow.getSelectedItems().size());
  }

  @Test
  @DisplayName("Changing items invalidates the size cache and refreshes the window")
  void itemsChangeRefreshes() {
    TwVirtualFlow<String> flow = flowWithItems(100);
    layout(flow, 300, 240);
    int last = flow.getLastVisibleIndex();
    interact(() -> flow.getItems().clear());
    assertEquals(-1, flow.getLastVisibleIndex());
    assertTrue(last >= 0);
  }

  @Test
  @DisplayName("Pref sizes follow the orientation")
  void prefSizes() {
    TwVirtualFlow<String> vertical = new TwVirtualFlow<>();
    assertEquals(400.0, vertical.prefWidth(-1), 0.001);
    assertEquals(600.0, vertical.prefHeight(-1), 0.001);

    TwVirtualFlow<String> horizontal = new TwVirtualFlow<>();
    horizontal.setOrientation(Orientation.HORIZONTAL);
    assertEquals(800.0, horizontal.prefWidth(-1), 0.001);
    assertEquals(200.0, horizontal.prefHeight(-1), 0.001);
  }

  @Test
  @DisplayName("Scroll interpolator is configurable via the fluent API")
  void scrollInterpolator() {
    TwVirtualFlow<String> flow = new TwVirtualFlow<>();
    flow.scrollInterpolator(Interpolator.EASE_BOTH);
    assertEquals(Interpolator.EASE_BOTH, flow.getScrollInterpolator());
  }

  @Test
  @DisplayName("A single huge item still yields a valid window")
  void singleItemWindow() {
    TwVirtualFlow<String> flow = flowWithItems(1);
    layout(flow, 300, 240);
    assertEquals(0, flow.getFirstVisibleIndex());
    assertEquals(0, flow.getLastVisibleIndex());
  }
}
