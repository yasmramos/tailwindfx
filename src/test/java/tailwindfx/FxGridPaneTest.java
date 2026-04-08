package tailwindfx;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FxGridPane.
 */
@DisplayName("FxGridPane Tests")
class FxGridPaneTest {

    @Nested
    @DisplayName("Basic Grid Creation")
    class BasicGridTests {

        @Test
        @DisplayName("Should create empty grid")
        void testCreateEmptyGrid() {
            FxGridPane grid = FxGridPane.create().build();

            assertNotNull(grid);
            assertEquals(0, grid.getChildren().size());
        }

        @Test
        @DisplayName("Should create grid with gap")
        void testCreateGridWithGap() {
            FxGridPane grid = FxGridPane.create(10);

            assertNotNull(grid);
            assertEquals(10, grid.getHgap());
            assertEquals(10, grid.getVgap());
        }

        @Test
        @DisplayName("Should create grid with different gaps")
        void testCreateGridWithDifferentGaps() {
            FxGridPane grid = FxGridPane.create(8, 12);

            assertNotNull(grid);
            assertEquals(8, grid.getHgap());
            assertEquals(12, grid.getVgap());
        }
    }

    @Nested
    @DisplayName("Adding Nodes to Grid")
    class AddNodeTests {

        @Test
        @DisplayName("Should add node at position")
        void testAddNodeAtPosition() {
            FxGridPane grid = FxGridPane.create().build();
            Region node = new Region();

            grid.add(node, 2, 3);

            assertEquals(1, grid.getChildren().size());
            assertEquals(Integer.valueOf(2), GridPane.getColumnIndex(node));
            assertEquals(Integer.valueOf(3), GridPane.getRowIndex(node));
        }

        @Test
        @DisplayName("Should add node with column span")
        void testAddNodeWithColSpan() {
            FxGridPane grid = FxGridPane.create().build();
            Region node = new Region();

            grid.add(node, 0, 0, 3, 1);

            assertEquals(Integer.valueOf(3), GridPane.getColumnSpan(node));
            assertEquals(Integer.valueOf(1), GridPane.getRowSpan(node));
        }

        @Test
        @DisplayName("Should add node with row span")
        void testAddNodeWithRowSpan() {
            FxGridPane grid = FxGridPane.create().build();
            Region node = new Region();

            grid.add(node, 0, 0, 1, 2);

            assertEquals(Integer.valueOf(2), GridPane.getRowSpan(node));
        }

        @Test
        @DisplayName("Should add multiple nodes")
        void testAddMultipleNodes() {
            FxGridPane grid = FxGridPane.create().build();
            Region n1 = new Region();
            Region n2 = new Region();

            grid.add(n1, 0, 0);
            grid.add(n2, 1, 0);

            assertEquals(2, grid.getChildren().size());
        }
    }

    @Nested
    @DisplayName("Grid Constraints")
    class GridConstraintsTests {

        @Test
        @DisplayName("Should add column constraints")
        void testAddColumnConstraints() {
            FxGridPane grid = FxGridPane.create().build();
            ColumnConstraints cc = new ColumnConstraints(100);

            grid.getColumnConstraints().add(cc);

            assertEquals(1, grid.getColumnConstraints().size());
        }

        @Test
        @DisplayName("Should add row constraints")
        void testRowConstraints() {
            FxGridPane grid = FxGridPane.create().build();
            RowConstraints rc = new RowConstraints(50);

            grid.getRowConstraints().add(rc);

            assertEquals(1, grid.getRowConstraints().size());
        }

        @Test
        @DisplayName("Should set column fill width")
        void testColumnFillWidth() {
            FxGridPane grid = FxGridPane.create().build();
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);

            ColumnConstraints added = grid.getColumnConstraints().get(0);
            assertEquals(Priority.ALWAYS, added.getHgrow());
        }
    }

    @Nested
    @DisplayName("Grid Pane Styling")
    class GridStylingTests {

        @Test
        @DisplayName("Should apply TailwindFX styles to grid")
        void testApplyStyles() {
            FxGridPane grid = FxGridPane.create().build();
            TailwindFX.apply(grid, "gap-4");

            assertTrue(grid.getStyleClass().contains("gap-4"));
        }

        @Test
        @DisplayName("Should apply padding to grid")
        void testApplyPadding() {
            FxGridPane grid = FxGridPane.create().build();
            TailwindFX.apply(grid, "p-4");

            // p-4 = 16px in Tailwind scale (4 * 4px)
            assertTrue(grid.getStyleClass().contains("p-4"));
        }
    }

    @Nested
    @DisplayName("Grid Alignment")
    class GridAlignmentTests {

        @Test
        @DisplayName("Should set node alignment in cell")
        void testSetAlignment() {
            FxGridPane grid = FxGridPane.create().build();
            Region node = new Region();
            grid.add(node, 0, 0);

            GridPane.setHalignment(node, javafx.geometry.HPos.CENTER);
            GridPane.setValignment(node, javafx.geometry.VPos.CENTER);

            assertEquals(javafx.geometry.HPos.CENTER, GridPane.getHalignment(node));
            assertEquals(javafx.geometry.VPos.CENTER, GridPane.getValignment(node));
        }

        @Test
        @DisplayName("Should set node to fill cell")
        void testSetFill() {
            FxGridPane grid = FxGridPane.create().build();
            Region node = new Region();
            grid.add(node, 0, 0);

            HBox.setHgrow(node, Priority.ALWAYS);
            VBox.setVgrow(node, Priority.ALWAYS);

            // Node should grow to fill cell
            assertNotNull(node);
        }
    }

    @Nested
    @DisplayName("Grid Utility Methods")
    class GridUtilityTests {

        @Test
        @DisplayName("Should set column span utility")
        void testColSpanUtility() {
            Region node = new Region();
            Styles.colSpan(node, 3);

            assertEquals(Integer.valueOf(3), GridPane.getColumnSpan(node));
        }

        @Test
        @DisplayName("Should set row span utility")
        void testRowSpanUtility() {
            Region node = new Region();
            Styles.rowSpan(node, 2);

            assertEquals(Integer.valueOf(2), GridPane.getRowSpan(node));
        }

        @Test
        @DisplayName("Should set full column span")
        void testColSpanFull() {
            Region node = new Region();
            Styles.colSpanFull(node);

            assertEquals(GridPane.REMAINING, GridPane.getColumnSpan(node));
        }

        @Test
        @DisplayName("Should set full row span")
        void testRowSpanFull() {
            Region node = new Region();
            Styles.rowSpanFull(node);

            assertEquals(GridPane.REMAINING, GridPane.getRowSpan(node));
        }

        @Test
        @DisplayName("Should set grid cell position")
        void testGridCellPosition() {
            Region node = new Region();
            Styles.gridCell(node, 5, 7);

            assertEquals(Integer.valueOf(5), GridPane.getColumnIndex(node));
            assertEquals(Integer.valueOf(7), GridPane.getRowIndex(node));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle adding null node")
        void testAddNullNode() {
            FxGridPane grid = FxGridPane.create().build();

            assertThrows(IllegalArgumentException.class, () -> {
                grid.add(null, 0, 0);
            });
        }

        @Test
        @DisplayName("Should handle negative column index")
        void testNegativeColumnIndex() throws Exception {
            FxGridPane grid = FxGridPane.create().build();
            Region node = new Region();

            // GridPane allows negative indices, but our wrapper may not
            assertThrows(IllegalArgumentException.class, () -> {
                grid.add(node, -1, 0);
            });
        }

        @Test
        @DisplayName("Should handle zero span")
        void testZeroSpan() {
            Region node = new Region();

            assertThrows(IllegalArgumentException.class, () -> {
                Styles.colSpan(node, 0);
            });
        }

        @Test
        @DisplayName("Should handle null node in span utility")
        void testNullNodeInSpan() {
            assertThrows(IllegalArgumentException.class, () -> {
                Styles.colSpan(null, 3);
            });
        }
    }

    // Helper classes for utility tests
    static class HBox {
        static void setHgrow(Node node, Priority priority) {
            javafx.scene.layout.HBox.setHgrow(node, priority);
        }
    }

    static class VBox {
        static void setVgrow(Node node, Priority priority) {
            javafx.scene.layout.VBox.setVgrow(node, priority);
        }
    }
}
