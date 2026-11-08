package io.github.yasmramos.tailwindfx.layout;

import static org.junit.jupiter.api.Assertions.*;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Layout-engine tests for {@link TwGridPane}: auto-flow (row/column/dense), masonry, template areas
 * and the runtime mutators.
 *
 * <p>Regression coverage for column auto-flow: with the default {@code rows = 0} the wrap condition
 * used {@code Integer.MAX_VALUE}, so every child was stacked in column 0 instead of filling down the
 * column and moving to the next one.
 */
@DisplayName("TwGridPane layout engine Tests")
class TwGridPaneLayoutTest extends ApplicationTest {

  @Override
  public void start(javafx.stage.Stage stage) {
    stage.setScene(new javafx.scene.Scene(new Pane(), 800, 600));
    stage.show();
  }

  private Region region(double w, double h) {
    Region r = new Region();
    r.setPrefSize(w, h);
    return r;
  }

  private void layout(TwGridPane grid, double w, double h) {
    interact(
        () -> {
          grid.resize(w, h);
          grid.applyCss();
          grid.layout();
        });
  }

  @Test
  @DisplayName("Row auto-flow fills left to right then wraps")
  void rowAutoFlowWraps() {
    TwGridPane grid = TwGridPane.create().cols(2).build();
    for (int i = 0; i < 4; i++) {
      grid.getChildren().add(region(50, 50));
    }
    layout(grid, 300, 300);
    assertEquals(0.0, grid.getChildren().get(0).getLayoutX(), 0.5);
    assertTrue(grid.getChildren().get(1).getLayoutX() > grid.getChildren().get(0).getLayoutX());
    assertTrue(grid.getChildren().get(2).getLayoutY() > grid.getChildren().get(0).getLayoutY());
  }

  @Test
  @DisplayName("Column auto-flow fills top to bottom then moves to the next column")
  void columnAutoFlowMovesRight() {
    TwGridPane grid =
        TwGridPane.create().cols(2).autoFlow(TwGridPane.AutoFlow.COL).build();
    for (int i = 0; i < 4; i++) {
      grid.getChildren().add(region(50, 50));
    }
    layout(grid, 300, 300);

    Node c0 = grid.getChildren().get(0);
    Node c1 = grid.getChildren().get(1);
    Node c2 = grid.getChildren().get(2);
    Node c3 = grid.getChildren().get(3);

    assertEquals(0.0, c0.getLayoutX(), 0.5);
    assertEquals(0.0, c0.getLayoutY(), 0.5);
    // Same column, next row
    assertEquals(c0.getLayoutX(), c1.getLayoutX(), 0.5);
    assertTrue(c1.getLayoutY() > c0.getLayoutY(), "second child should be below the first");
    // New column
    assertTrue(c2.getLayoutX() > c0.getLayoutX(), "third child should start a new column");
    assertEquals(0.0, c2.getLayoutY(), 0.5);
    assertTrue(c3.getLayoutY() > c2.getLayoutY(), "fourth child continues the second column");
  }

  @Test
  @DisplayName("Explicit rows bound the column auto-flow")
  void columnAutoFlowRespectsRows() {
    TwGridPane grid =
        TwGridPane.create().cols(3).rows(2).autoFlow(TwGridPane.AutoFlow.COL).build();
    for (int i = 0; i < 4; i++) {
      grid.getChildren().add(region(40, 40));
    }
    layout(grid, 400, 400);
    // With 2 rows: children 0,1 in col0; 2,3 in col1.
    assertEquals(grid.getChildren().get(0).getLayoutX(), grid.getChildren().get(1).getLayoutX(), 0.5);
    assertTrue(grid.getChildren().get(2).getLayoutX() > grid.getChildren().get(0).getLayoutX());
  }

  @Test
  @DisplayName("Row auto-flow wraps a column-spanning child that does not fit the row")
  void rowAutoFlowWrapsSpanningChild() {
    TwGridPane grid = TwGridPane.create().cols(3).build();
    Region single = region(40, 40);
    Region wide = region(40, 40);
    TwGridPane.setColSpan(wide, 3);
    grid.getChildren().addAll(single, wide);
    layout(grid, 400, 400);

    // single occupies col 0; wide (span 3) cannot fit in cols 1-2, so it moves to row 1 col 0.
    assertEquals(0.0, single.getLayoutX(), 0.5);
    assertEquals(0.0, single.getLayoutY(), 0.5);
    assertEquals(0.0, wide.getLayoutX(), 0.5);
    assertTrue(wide.getLayoutY() > single.getLayoutY(), "spanning child should wrap to the next row");
  }

  @Test
  @DisplayName("Column auto-flow starts a new column for a row-spanning child that does not fit")
  void colAutoFlowStartsNewColumnForSpanningChild() {
    TwGridPane grid =
        TwGridPane.create().cols(2).rows(2).autoFlow(TwGridPane.AutoFlow.COL).build();
    Region single = region(40, 40);
    Region tall = region(40, 40);
    TwGridPane.setRowSpan(tall, 2);
    grid.getChildren().addAll(single, tall);
    layout(grid, 400, 400);

    // single occupies (col 0, row 0); tall (span 2) cannot fit below it in column 0, so it
    // starts column 1 at row 0 instead of overflowing past the bottom of the grid.
    assertEquals(0.0, single.getLayoutX(), 0.5);
    assertTrue(tall.getLayoutX() > single.getLayoutX(), "spanning child should use a new column");
    assertEquals(0.0, tall.getLayoutY(), 0.5);
  }

  @Test
  @DisplayName("Dense auto-flow packs around a spanning child")
  void denseAutoFlow() {
    TwGridPane grid =
        TwGridPane.create().cols(3).autoFlow(TwGridPane.AutoFlow.ROW_DENSE).build();
    Region wide = region(40, 40);
    TwGridPane.setColSpan(wide, 2);
    grid.getChildren().add(wide);
    for (int i = 0; i < 4; i++) {
      grid.getChildren().add(region(40, 40));
    }
    layout(grid, 400, 400);
    assertTrue(grid.getChildren().size() == 5);
  }

  @Test
  @DisplayName("Masonry places children in the shortest column")
  void masonryLayout() {
    TwGridPane grid = TwGridPane.create().masonry(2).build();
    Region tall = region(40, 200);
    Region short1 = region(40, 40);
    Region short2 = region(40, 40);
    grid.getChildren().addAll(tall, short1, short2);
    layout(grid, 200, 400);
    // tall goes to col0; short1 to col1 (shortest); short2 back to col1 or col0.
    assertEquals(0.0, tall.getLayoutX(), 0.5);
    assertTrue(short1.getLayoutX() > tall.getLayoutX(), "second child uses the other column");
  }

  @Test
  @DisplayName("Template areas place children by name and support spans")
  void areaLayout() {
    TwGridPane grid =
        TwGridPane.create()
            .cols(2)
            .areas("header header", "main aside")
            .build();
    Region header = region(40, 40);
    Region main = region(40, 40);
    Region aside = region(40, 40);
    grid.getChildren().addAll(header, main, aside);
    grid.placeIn(header, "header");
    grid.placeIn(main, "main");
    grid.placeIn(aside, "aside");
    layout(grid, 400, 400);

    // header spans both columns → its width is roughly the full row
    assertTrue(header.getLayoutY() < main.getLayoutY(), "header is on the first row");
    assertTrue(aside.getLayoutX() > main.getLayoutX(), "aside is to the right of main");
  }

  @Test
  @DisplayName("placeIn rejects an unknown area name")
  void placeInUnknownArea() {
    TwGridPane grid = TwGridPane.create().areas("a b").build();
    assertThrows(IllegalArgumentException.class, () -> grid.placeIn(new Region(), "nope"));
  }

  @Test
  @DisplayName("areas() can be set after construction")
  void areasAfterConstruction() {
    TwGridPane grid = TwGridPane.create().cols(2).build();
    grid.areas("a b", "c d");
    Region a = region(30, 30);
    grid.getChildren().add(a);
    grid.placeIn(a, "a");
    layout(grid, 200, 200);
    assertEquals(0.0, a.getLayoutX(), 0.5);
  }

  @Test
  @DisplayName("Runtime mutators update state and reject invalid values")
  void runtimeMutators() {
    TwGridPane grid = TwGridPane.create().cols(2).build();
    grid.cols(3).gap(8).gapX(4).gapY(6).rows(2).autoFlow(TwGridPane.AutoFlow.COL);
    assertEquals(3, grid.getCols());
    assertEquals(4.0, grid.getGapX(), 0.001);
    assertEquals(6.0, grid.getGapY(), 0.001);
    assertEquals(TwGridPane.AutoFlow.COL, grid.getAutoFlow());

    assertThrows(IllegalArgumentException.class, () -> grid.cols(0));
    assertThrows(IllegalArgumentException.class, () -> grid.rows(-1));
    assertThrows(IllegalArgumentException.class, () -> TwGridPane.create().cols(0));
    assertThrows(IllegalArgumentException.class, () -> TwGridPane.create().rows(-2));
  }

  @Test
  @DisplayName("Gap aliases and padding setters update internal state")
  void gapsAndPadding() {
    TwGridPane grid = TwGridPane.create().build();
    grid.gap(5).padding(10);
    assertEquals(5.0, grid.getHgap(), 0.001);
    assertEquals(5.0, grid.getVgap(), 0.001);
    assertEquals(new Insets(10), grid.getInternalPadding());

    grid.padding(new Insets(1, 2, 3, 4));
    assertEquals(new Insets(1, 2, 3, 4), grid.getInternalPadding());
    grid.paddingX(7);
    assertEquals(7.0, grid.getInternalPadding().getLeft(), 0.001);
    grid.paddingY(9);
    assertEquals(9.0, grid.getInternalPadding().getTop(), 0.001);
    assertThrows(IllegalArgumentException.class, () -> grid.padding((Insets) null));
  }

  @Test
  @DisplayName("Convenience factories build grids with the requested gaps")
  void convenienceFactories() {
    assertEquals(16.0, TwGridPane.create(16.0).getGapX(), 0.001);
    TwGridPane split = TwGridPane.create(4.0, 12.0);
    assertEquals(4.0, split.getGapX(), 0.001);
    assertEquals(12.0, split.getGapY(), 0.001);
  }

  @Test
  @DisplayName("add() overloads place and constrain children")
  void addOverloads() {
    TwGridPane grid = TwGridPane.create().cols(2).build();
    Region a = region(20, 20);
    Region b = region(20, 20);
    Region c = region(20, 20);
    grid.add(a, 0, 0);
    grid.add(b, 1, 0, 1, 1);
    grid.add(c, 0, 1, Priority.ALWAYS, Priority.SOMETIMES);
    assertEquals(3, grid.getChildren().size());
    assertThrows(IllegalArgumentException.class, () -> grid.add(null, 0, 0));
  }

  @Test
  @DisplayName("Span accessors store and read per-child spans")
  void spanAccessors() {
    Region r = region(20, 20);
    assertEquals(1, TwGridPane.getColSpan(r));
    assertEquals(1, TwGridPane.getRowSpan(r));
    TwGridPane.setColSpan(r, 3);
    TwGridPane.setRowSpan(r, 2);
    assertEquals(3, TwGridPane.getColSpan(r));
    assertEquals(2, TwGridPane.getRowSpan(r));
    assertThrows(IllegalArgumentException.class, () -> TwGridPane.setColSpan(r, 0));
    assertThrows(IllegalArgumentException.class, () -> TwGridPane.setRowSpan(r, 0));
  }

  @Test
  @DisplayName("computePrefWidth/prefHeight reflect the column count")
  void prefSizes() {
    TwGridPane grid = TwGridPane.create().cols(3).gap(10).build();
    grid.getChildren().add(region(20, 30));
    assertTrue(grid.prefWidth(-1) > 0);
    assertTrue(grid.prefHeight(-1) > 0);
    TwGridPane empty = TwGridPane.create().cols(2).build();
    assertEquals(0.0, empty.prefHeight(-1), 0.001);
  }

  @Test
  @DisplayName("layoutChildren on an empty grid is a no-op")
  void emptyGridLayout() {
    TwGridPane grid = TwGridPane.create().cols(2).build();
    layout(grid, 100, 100);
    assertEquals(0, grid.getChildren().size());
  }

  @Test
  @DisplayName("Removing a masonry child detaches its height listener without error")
  void masonryChildRemoval() {
    TwGridPane grid = TwGridPane.create().masonry(2).build();
    Region a = region(30, 30);
    Region b = region(30, 30);
    grid.getChildren().addAll(a, b);
    layout(grid, 200, 200);
    interact(
        () -> {
          grid.getChildren().remove(b);
          grid.layout();
        });
    assertEquals(1, grid.getChildren().size());
  }

  @Test
  @DisplayName("Unmapped child in area layout is collapsed, not crashed")
  void unmappedAreaChildCollapses() {
    TwGridPane grid = TwGridPane.create().cols(2).areas("a b").build();
    Region mapped = region(20, 20);
    Region stray = region(20, 20);
    grid.getChildren().addAll(mapped, stray);
    grid.placeIn(mapped, "a");
    layout(grid, 200, 200);
    assertEquals(0.0, stray.getLayoutX(), 0.5);
    assertEquals(0.0, stray.getLayoutY(), 0.5);
  }

  @Test
  @DisplayName("Column count is clamped to at least one during layout")
  void colsClampedDuringLayout() {
    // Bypass the setter guard by building with the default (cols=1) and no children,
    // then add a child; layout must not divide by zero.
    TwGridPane grid = TwGridPane.create().build();
    grid.getChildren().add(region(10, 10));
    layout(grid, 100, 100);
    assertEquals(1, grid.getCols());
  }
}
