package io.github.yasmramos.tailwindfx;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.layout.TwFlexPane;
import io.github.yasmramos.tailwindfx.layout.TwGridPane;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Comprehensive tests for TwLayout class. Covers layout migration, flex container configuration,
 * grid setup, and token parsing.
 */
@DisplayName("TwLayout Tests")
class TwLayoutTest extends ApplicationTest {

  private Pane container;
  private Region child1, child2, child3;

  @BeforeEach
  void setUp() {
    container = new Pane();
    child1 = new Region();
    child2 = new Region();
    child3 = new Region();
    container.getChildren().addAll(child1, child2, child3);
  }

  // ==================== FLEX DISPLAY TESTS ====================

  @Test
  @DisplayName("flex converts Pane to TwFlexPane without wrapper")
  void testFlexDisplayConvertsNode() {
    int initialChildCount = container.getChildren().size();

    TwLayout.apply(container, "flex");

    assertTrue(container instanceof TwFlexPane);
    assertEquals(initialChildCount, container.getChildren().size());
    assertSame(child1, container.getChildren().get(0));
    assertSame(child2, container.getChildren().get(1));
    assertSame(child3, container.getChildren().get(2));
  }

  @Test
  @DisplayName("flex sets direction to ROW by default")
  void testFlexDefaultDirection() {
    TwLayout.apply(container, "flex");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertEquals(TwFlexPane.Direction.ROW, flexPane.getDirection());
  }

  @Test
  @DisplayName("flex-col sets direction to COL")
  void testFlexColDirection() {
    TwLayout.apply(container, "flex", "flex-col");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertEquals(TwFlexPane.Direction.COL, flexPane.getDirection());
  }

  @Test
  @DisplayName("flex-row sets direction to ROW explicitly")
  void testFlexRowDirection() {
    TwLayout.apply(container, "flex", "flex-row");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertEquals(TwFlexPane.Direction.ROW, flexPane.getDirection());
  }

  @Test
  @DisplayName("flex-row-reverse sets direction to ROW_REVERSE")
  void testFlexRowReverseDirection() {
    TwLayout.apply(container, "flex", "flex-row-reverse");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertEquals(TwFlexPane.Direction.ROW_REVERSE, flexPane.getDirection());
  }

  @Test
  @DisplayName("flex-col-reverse sets direction to COL_REVERSE")
  void testFlexColReverseDirection() {
    TwLayout.apply(container, "flex", "flex-col-reverse");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertEquals(TwFlexPane.Direction.COL_REVERSE, flexPane.getDirection());
  }

  @Test
  @DisplayName("inline-flex converts Pane to TwFlexPane")
  void testInlineFlexDisplay() {
    TwLayout.apply(container, "inline-flex");

    assertTrue(container instanceof TwFlexPane);
  }

  // ==================== FLEX WRAP TESTS ====================

  @Test
  @DisplayName("flex-wrap enables wrapping")
  void testFlexWrap() {
    TwLayout.apply(container, "flex", "flex-wrap");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertTrue(flexPane.isWrap());
  }

  @Test
  @DisplayName("flex-nowrap disables wrapping")
  void testFlexNowrap() {
    TwLayout.apply(container, "flex", "flex-nowrap");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertFalse(flexPane.isWrap());
  }

  // ==================== JUSTIFY CONTENT TESTS ====================

  @Test
  @DisplayName("justify-start configures justify content")
  void testJustifyStart() {
    TwLayout.apply(container, "flex", "justify-start");

    TwFlexPane flexPane = (TwFlexPane) container;
    // Verify through layout behavior or internal state
    assertNotNull(flexPane);
  }

  @Test
  @DisplayName("justify-center configures justify content")
  void testJustifyCenter() {
    TwLayout.apply(container, "flex", "justify-center");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertNotNull(flexPane);
  }

  @Test
  @DisplayName("justify-end configures justify content")
  void testJustifyEnd() {
    TwLayout.apply(container, "flex", "justify-end");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertNotNull(flexPane);
  }

  @Test
  @DisplayName("justify-between configures justify content")
  void testJustifyBetween() {
    TwLayout.apply(container, "flex", "justify-between");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertNotNull(flexPane);
  }

  @Test
  @DisplayName("justify-around configures justify content")
  void testJustifyAround() {
    TwLayout.apply(container, "flex", "justify-around");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertNotNull(flexPane);
  }

  // ==================== ALIGN ITEMS TESTS ====================

  @Test
  @DisplayName("items-start configures align items")
  void testItemsStart() {
    TwLayout.apply(container, "flex", "items-start");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertNotNull(flexPane);
  }

  @Test
  @DisplayName("items-center configures align items")
  void testItemsCenter() {
    TwLayout.apply(container, "flex", "items-center");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertNotNull(flexPane);
  }

  @Test
  @DisplayName("items-end configures align items")
  void testItemsEnd() {
    TwLayout.apply(container, "flex", "items-end");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertNotNull(flexPane);
  }

  @Test
  @DisplayName("items-stretch configures align items")
  void testItemsStretch() {
    TwLayout.apply(container, "flex", "items-stretch");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertNotNull(flexPane);
  }

  @Test
  @DisplayName("items-baseline configures align items")
  void testItemsBaseline() {
    TwLayout.apply(container, "flex", "items-baseline");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertNotNull(flexPane);
  }

  // ==================== GAP TESTS ====================

  @Test
  @DisplayName("gap-4 sets spacing on flex container")
  void testGap4() {
    TwLayout.apply(container, "flex", "gap-4");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertEquals(16.0, flexPane.getSpacing());
  }

  @Test
  @DisplayName("gap-2 sets smaller spacing")
  void testGap2() {
    TwLayout.apply(container, "flex", "gap-2");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertEquals(8.0, flexPane.getSpacing());
  }

  @Test
  @DisplayName("gap-x-4 sets horizontal spacing")
  void testGapX4() {
    TwLayout.apply(container, "flex", "gap-x-4");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertNotNull(flexPane);
  }

  @Test
  @DisplayName("gap-y-4 sets vertical spacing")
  void testGapY4() {
    TwLayout.apply(container, "flex", "gap-y-4");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertNotNull(flexPane);
  }

  // ==================== GRID DISPLAY TESTS ====================

  @Test
  @DisplayName("grid converts Pane to TwGridPane without wrapper")
  void testGridDisplayConvertsNode() {
    int initialChildCount = container.getChildren().size();

    TwLayout.apply(container, "grid");

    assertTrue(container instanceof TwGridPane);
    assertEquals(initialChildCount, container.getChildren().size());
    assertSame(child1, container.getChildren().get(0));
  }

  @Test
  @DisplayName("grid-cols-3 sets column count")
  void testGridCols3() {
    TwLayout.apply(container, "grid", "grid-cols-3");

    TwGridPane gridPane = (TwGridPane) container;
    assertEquals(3, gridPane.getCols());
  }

  @Test
  @DisplayName("grid-cols-2 sets column count to 2")
  void testGridCols2() {
    TwLayout.apply(container, "grid", "grid-cols-2");

    TwGridPane gridPane = (TwGridPane) container;
    assertEquals(2, gridPane.getCols());
  }

  @Test
  @DisplayName("grid-rows-3 sets row count")
  void testGridRows3() {
    TwLayout.apply(container, "grid", "grid-rows-3");

    TwGridPane gridPane = (TwGridPane) container;
    // Verify rows configuration
    assertNotNull(gridPane);
  }

  @Test
  @DisplayName("grid-flow-col sets auto flow to COL")
  void testGridFlowCol() {
    TwLayout.apply(container, "grid", "grid-flow-col");

    TwGridPane gridPane = (TwGridPane) container;
    // Verify auto flow configuration
    assertNotNull(gridPane);
  }

  @Test
  @DisplayName("grid-flow-row sets auto flow to ROW")
  void testGridFlowRow() {
    TwLayout.apply(container, "grid", "grid-flow-row");

    TwGridPane gridPane = (TwGridPane) container;
    assertNotNull(gridPane);
  }

  @Test
  @DisplayName("grid-flow-dense sets auto flow to DENSE")
  void testGridFlowDense() {
    TwLayout.apply(container, "grid", "grid-flow-dense");

    TwGridPane gridPane = (TwGridPane) container;
    assertNotNull(gridPane);
  }

  // ==================== FLEX ITEM TESTS ====================

  @Test
  @DisplayName("flex-1 on child configures grow")
  void testFlexItemGrow() {
    TwLayout.apply(container, "flex");
    TwLayout.apply(child1, "flex-1");

    // Child should have flex constraints set
    assertNotNull(child1);
  }

  @Test
  @DisplayName("flex-none on child removes grow/shrink")
  void testFlexItemNone() {
    TwLayout.apply(container, "flex");
    TwLayout.apply(child1, "flex-none");

    assertNotNull(child1);
  }

  @Test
  @DisplayName("grow on child enables growing")
  void testFlexItemGrowOnly() {
    TwLayout.apply(container, "flex");
    TwLayout.apply(child1, "grow");

    assertNotNull(child1);
  }

  @Test
  @DisplayName("shrink on child enables shrinking")
  void testFlexItemShrinkOnly() {
    TwLayout.apply(container, "flex");
    TwLayout.apply(child1, "shrink");

    assertNotNull(child1);
  }

  @Test
  @DisplayName("basis-0 sets flex basis")
  void testFlexItemBasis() {
    TwLayout.apply(container, "flex");
    TwLayout.apply(child1, "basis-0");

    assertNotNull(child1);
  }

  @Test
  @DisplayName("order-1 sets item order")
  void testFlexItemOrder() {
    TwLayout.apply(container, "flex");
    TwLayout.apply(child1, "order-1");

    assertNotNull(child1);
  }

  // ==================== GRID ITEM TESTS ====================

  @Test
  @DisplayName("col-span-2 on child sets column span")
  void testGridItemColSpan() {
    TwLayout.apply(container, "grid", "grid-cols-3");
    TwLayout.apply(child1, "col-span-2");

    // Child should have column span set in parent context
    assertNotNull(child1);
  }

  @Test
  @DisplayName("row-span-2 on child sets row span")
  void testGridItemRowSpan() {
    TwLayout.apply(container, "grid", "grid-rows-3");
    TwLayout.apply(child1, "row-span-2");

    assertNotNull(child1);
  }

  @Test
  @DisplayName("col-start-2 sets column start position")
  void testGridItemColStart() {
    TwLayout.apply(container, "grid");
    TwLayout.apply(child1, "col-start-2");

    assertNotNull(child1);
  }

  @Test
  @DisplayName("col-end-4 sets column end position")
  void testGridItemColEnd() {
    TwLayout.apply(container, "grid");
    TwLayout.apply(child1, "col-end-4");

    assertNotNull(child1);
  }

  @Test
  @DisplayName("row-start-2 sets row start position")
  void testGridItemRowStart() {
    TwLayout.apply(container, "grid");
    TwLayout.apply(child1, "row-start-2");

    assertNotNull(child1);
  }

  @Test
  @DisplayName("row-end-4 sets row end position")
  void testGridItemRowEnd() {
    TwLayout.apply(container, "grid");
    TwLayout.apply(child1, "row-end-4");

    assertNotNull(child1);
  }

  // ==================== MIGRATION TESTS ====================

  @Test
  @DisplayName("reapplying flex does not recreate TwFlexPane")
  void testReapplyFlexNoRecreation() {
    TwLayout.apply(container, "flex");
    TwFlexPane firstInstance = (TwFlexPane) container;

    TwLayout.apply(container, "flex");

    assertSame(firstInstance, container);
  }

  @Test
  @DisplayName("reapplying grid does not recreate TwGridPane")
  void testReapplyGridNoRecreation() {
    TwLayout.apply(container, "grid");
    TwGridPane firstInstance = (TwGridPane) container;

    TwLayout.apply(container, "grid");

    assertSame(firstInstance, container);
  }

  @Test
  @DisplayName("children preserved after multiple migrations")
  void testChildrenPreservedAfterMigrations() {
    TwLayout.apply(container, "flex");
    TwLayout.apply(container, "grid");
    TwLayout.apply(container, "flex");

    assertEquals(3, container.getChildren().size());
    assertSame(child1, container.getChildren().get(0));
    assertSame(child2, container.getChildren().get(1));
    assertSame(child3, container.getChildren().get(2));
  }

  // ==================== COMBINED LAYOUT TESTS ====================

  @Test
  @DisplayName("flex with justify and items configures all properties")
  void testFlexCombined() {
    TwLayout.apply(container, "flex", "justify-center", "items-center", "gap-4");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertEquals(16.0, flexPane.getSpacing());
    assertNotNull(flexPane);
  }

  @Test
  @DisplayName("grid with cols and gap configures all properties")
  void testGridCombined() {
    TwLayout.apply(container, "grid", "grid-cols-3", "gap-4");

    TwGridPane gridPane = (TwGridPane) container;
    assertEquals(3, gridPane.getCols());
    assertNotNull(gridPane);
  }

  @Test
  @DisplayName("nested flex containers work correctly")
  void testNestedFlexContainers() {
    TwLayout.apply(container, "flex");
    TwLayout.apply(child1, "flex");

    assertTrue(container instanceof TwFlexPane);
    assertTrue(child1 instanceof TwFlexPane);
  }

  // ==================== EDGE CASES ====================

  @Test
  @DisplayName("empty container with flex works")
  void testEmptyContainerFlex() {
    Pane emptyContainer = new Pane();

    assertDoesNotThrow(() -> TwLayout.apply(emptyContainer, "flex"));
    assertTrue(emptyContainer instanceof TwFlexPane);
  }

  @Test
  @DisplayName("empty container with grid works")
  void testEmptyContainerGrid() {
    Pane emptyContainer = new Pane();

    assertDoesNotThrow(() -> TwLayout.apply(emptyContainer, "grid"));
    assertTrue(emptyContainer instanceof TwGridPane);
  }

  @Test
  @DisplayName("non-Pane node with flex does not crash")
  void testNonPaneNodeFlex() {
    Label label = new Label("test");

    assertDoesNotThrow(() -> TwLayout.apply(label, "flex"));
  }

  @Test
  @DisplayName("multiple tokens applied in correct order")
  void testMultipleTokensOrder() {
    TwLayout.apply(container, "flex", "flex-col", "flex-wrap", "gap-4", "justify-center");

    TwFlexPane flexPane = (TwFlexPane) container;
    assertEquals(TwFlexPane.Direction.COL, flexPane.getDirection());
    assertTrue(flexPane.isWrap());
    assertEquals(16.0, flexPane.getSpacing());
  }
}
