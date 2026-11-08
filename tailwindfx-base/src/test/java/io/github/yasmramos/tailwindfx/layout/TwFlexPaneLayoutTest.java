package io.github.yasmramos.tailwindfx.layout;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Unit tests for the pure layout math in {@link TwFlexPane} plus the child-layout pass.
 *
 * <p>Regression coverage for the overflow clamp: {@code justifyPositions} clamps the free space to
 * zero for AROUND/EVENLY, but {@code alignContentPositions} did not, so wrapped rows were placed
 * above the container (negative Y) when their total height exceeded the available space.
 */
@DisplayName("TwFlexPane layout math Tests")
class TwFlexPaneLayoutTest extends ApplicationTest {

  @Override
  public void start(javafx.stage.Stage stage) {
    stage.setScene(new javafx.scene.Scene(new Pane(), 400, 300));
    stage.show();
  }

  @Test
  @DisplayName("justifyPositions clamps AROUND to non-negative positions on overflow")
  void justifyAroundOverflowClamps() {
    double[] sizes = {100, 100};
    double[] pos = TwFlexPane.justifyPositions(sizes, 0, 50, TwFlexPane.Justify.AROUND);
    assertTrue(pos[0] >= 0, "first position must not be negative, was " + pos[0]);
  }

  @Test
  @DisplayName("justifyPositions clamps EVENLY to non-negative positions on overflow")
  void justifyEvenlyOverflowClamps() {
    double[] sizes = {100, 100};
    double[] pos = TwFlexPane.justifyPositions(sizes, 0, 50, TwFlexPane.Justify.EVENLY);
    assertTrue(pos[0] >= 0, "first position must not be negative, was " + pos[0]);
  }

  @Test
  @DisplayName("alignContentPositions clamps AROUND to non-negative positions on overflow")
  void alignContentAroundOverflowClamps() {
    double[] rows = {100, 100};
    double[] pos =
        TwFlexPane.alignContentPositions(rows, 0, 50, TwFlexPane.AlignContent.AROUND);
    assertTrue(pos[0] >= 0, "first row must not be placed above the container, was " + pos[0]);
  }

  @Test
  @DisplayName("alignContentPositions clamps EVENLY to non-negative positions on overflow")
  void alignContentEvenlyOverflowClamps() {
    double[] rows = {100, 100};
    double[] pos =
        TwFlexPane.alignContentPositions(rows, 0, 50, TwFlexPane.AlignContent.EVENLY);
    assertTrue(pos[0] >= 0, "first row must not be placed above the container, was " + pos[0]);
  }

  @Test
  @DisplayName("alignContentPositions distributes STRETCH rows in order")
  void alignContentStretchIsMonotonic() {
    double[] rows = {40, 40};
    double[] pos =
        TwFlexPane.alignContentPositions(rows, 10, 300, TwFlexPane.AlignContent.STRETCH);
    assertEquals(0.0, pos[0], 0.001);
    assertTrue(pos[1] > pos[0]);
  }

  @Test
  @DisplayName("justifyPositions BETWEEN pins the first item at zero")
  void justifyBetweenStartsAtZero() {
    double[] sizes = {50, 50, 50};
    double[] pos = TwFlexPane.justifyPositions(sizes, 10, 300, TwFlexPane.Justify.BETWEEN);
    assertEquals(0.0, pos[0], 0.001);
    assertEquals(0.0, TwFlexPane.justifyPositions(sizes, 10, 300, TwFlexPane.Justify.START)[0], 0.001);
  }

  @Test
  @DisplayName("Row layout honours flex-basis for the main axis size")
  void rowLayoutRespectsBasis() {
    TwFlexPane pane = new TwFlexPane();
    Region a = new Region();
    Region b = new Region();
    pane.getChildren().addAll(a, b);
    TwFlexPane.setBasis(a, 120);
    TwFlexPane.setBasis(b, 80);
    pane.resize(400, 100);

    interact(
        () -> {
          pane.applyCss();
          pane.layout();
        });

    assertEquals(120.0, a.getWidth(), 1.0);
    assertEquals(80.0, b.getWidth(), 1.0);
  }

  @Test
  @DisplayName("Row layout grows children with a grow factor")
  void rowLayoutGrows() {
    TwFlexPane pane = new TwFlexPane();
    Region fixed = new Region();
    fixed.setPrefWidth(100);
    Region grower = new Region();
    grower.setPrefWidth(100);
    pane.getChildren().addAll(fixed, grower);
    TwFlexPane.setGrow(grower, 1);
    pane.resize(500, 100);

    interact(
        () -> {
          pane.applyCss();
          pane.layout();
        });

    assertTrue(grower.getWidth() > fixed.getWidth(), "growing child should be wider");
    assertEquals(100.0, fixed.getWidth(), 1.0);
  }

  @Test
  @DisplayName("Row grow with a gap must not overflow the container")
  void rowGrowWithGapStaysWithinContainer() {
    TwFlexPane pane = new TwFlexPane();
    pane.gap(40);
    Region a = new Region();
    a.setPrefWidth(100);
    Region b = new Region();
    b.setPrefWidth(100);
    pane.getChildren().addAll(a, b);
    TwFlexPane.setGrow(a, 1);
    TwFlexPane.setGrow(b, 1);
    pane.resize(400, 100);

    interact(
        () -> {
          pane.applyCss();
          pane.layout();
        });

    double used = a.getWidth() + b.getWidth() + 40;
    assertTrue(
        used <= 400.0 + 0.5,
        "children plus gap must fit the container, used " + used + " of 400");
    assertEquals(180.0, a.getWidth(), 1.0);
  }

  @Test
  @DisplayName("Column layout stacks children and honours gap")
  void columnLayoutStacks() {
    TwFlexPane pane = new TwFlexPane();
    pane.setDirection(TwFlexPane.Direction.COL);
    pane.gap(10);
    Label l1 = new Label("a");
    Label l2 = new Label("b");
    pane.getChildren().addAll(l1, l2);
    pane.resize(200, 400);

    interact(
        () -> {
          pane.applyCss();
          pane.layout();
        });

    assertEquals(0.0, l1.getLayoutY(), 1.0);
    assertTrue(l2.getLayoutY() >= l1.getLayoutY() + l1.getHeight(), "second item follows the first");
  }

  @Test
  @DisplayName("Order property reorders children during layout")
  void orderReordersChildren() {
    TwFlexPane pane = new TwFlexPane();
    Region first = new Region();
    first.setPrefWidth(50);
    Region second = new Region();
    second.setPrefWidth(50);
    pane.getChildren().addAll(first, second);
    TwFlexPane.setOrder(first, 5); // pushed after `second`
    pane.resize(200, 50);

    interact(
        () -> {
          pane.applyCss();
          pane.layout();
        });

    assertTrue(
        second.getLayoutX() < first.getLayoutX(),
        "the item with the lower order value should be placed first");
  }

  @Test
  @DisplayName("Wrapped rows are all placed at non-negative Y on overflow")
  void wrappedRowsStayInsideContainer() {
    TwFlexPane pane = new TwFlexPane();
    pane.setWrap(true);
    pane.setAlignContent(TwFlexPane.AlignContent.AROUND);
    for (int i = 0; i < 4; i++) {
      Region r = new Region();
      r.setPrefSize(300, 60);
      pane.getChildren().add(r);
    }
    pane.resize(320, 80); // rows overflow the height on purpose

    interact(
        () -> {
          pane.applyCss();
          pane.layout();
        });

    for (javafx.scene.Node child : pane.getChildren()) {
      assertTrue(
          child.getLayoutY() >= -0.001, "child placed above container at Y=" + child.getLayoutY());
    }
  }

  @Test
  @DisplayName("Empty pane layout is a no-op")
  void emptyPaneLayout() {
    TwFlexPane pane = new TwFlexPane();
    pane.resize(100, 100);
    interact(
        () -> {
          pane.applyCss();
          pane.layout();
        });
    assertEquals(0, pane.getChildren().size());
  }

  @Test
  @DisplayName("computePrefWidth sums children and gaps in a non-wrapping row")
  void prefWidthRow() {
    TwFlexPane pane = new TwFlexPane();
    Region a = new Region();
    a.setPrefWidth(30);
    Region b = new Region();
    b.setPrefWidth(20);
    pane.getChildren().addAll(a, b);
    pane.gap(5);
    assertEquals(55.0, pane.prefWidth(-1), 1.0);
  }

  @Test
  @DisplayName("computePrefHeight stacks children in a column")
  void prefHeightColumn() {
    TwFlexPane pane = new TwFlexPane();
    pane.setDirection(TwFlexPane.Direction.COL);
    Region a = new Region();
    a.setPrefHeight(30);
    Region b = new Region();
    b.setPrefHeight(20);
    pane.getChildren().addAll(a, b);
    pane.gap(5);
    assertEquals(55.0, pane.prefHeight(-1), 1.0);
  }

  @Test
  @DisplayName("StackPane-like nesting does not break child layout")
  void nestedPaneLayout() {
    TwFlexPane pane = new TwFlexPane();
    StackPane holder = new StackPane(new Region());
    pane.getChildren().add(holder);
    pane.resize(200, 100);
    interact(
        () -> {
          pane.applyCss();
          pane.layout();
        });
    assertEquals(1, pane.getChildren().size());
  }
}
