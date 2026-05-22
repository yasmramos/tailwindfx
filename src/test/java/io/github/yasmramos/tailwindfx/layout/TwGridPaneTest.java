package io.github.yasmramos.tailwindfx.layout;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.testfx.framework.junit5.ApplicationTest;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TwGridPane layout container.
 */
@DisplayName("TwGridPane Layout Tests")
class TwGridPaneTest extends ApplicationTest {

    @Override
    public void start(javafx.stage.Stage stage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    @DisplayName("Should create grid with default builder")
    void testCreateDefault() {
        TwGridPane grid = TwGridPane.create().build();
        
        assertNotNull(grid);
        assertEquals(1, grid.getCols());
        assertEquals(TwGridPane.AutoFlow.ROW, grid.getAutoFlow());
    }

    @Test
    @DisplayName("Should create grid with gap via convenience method")
    void testCreateWithGap() {
        TwGridPane grid = TwGridPane.create(16.0);
        
        assertNotNull(grid);
        assertEquals(16.0, grid.getGapX(), 0.001);
        assertEquals(16.0, grid.getGapY(), 0.001);
    }

    @Test
    @DisplayName("Should create grid with separate gaps via convenience method")
    void testCreateWithSeparateGaps() {
        TwGridPane grid = TwGridPane.create(10.0, 20.0);
        
        assertNotNull(grid);
        assertEquals(10.0, grid.getGapX(), 0.001);
        assertEquals(20.0, grid.getGapY(), 0.001);
    }

    @Test
    @DisplayName("Should build grid with columns via builder")
    void testBuilderCols() {
        TwGridPane grid = TwGridPane.create().cols(3).build();
        
        assertEquals(3, grid.getCols());
    }

    @Test
    @DisplayName("Should throw exception when setting cols < 1")
    void testBuilderInvalidCols() {
        assertThrows(IllegalArgumentException.class, () -> 
            TwGridPane.create().cols(0).build()
        );
        
        assertThrows(IllegalArgumentException.class, () -> 
            TwGridPane.create().cols(-1).build()
        );
    }

    @Test
    @DisplayName("Should build grid with rows via builder")
    void testBuilderRows() {
        TwGridPane grid = TwGridPane.create().rows(4).build();
        
        // rows se usa internamente pero no hay getter publico
        assertNotNull(grid);
    }

    @Test
    @DisplayName("Should throw exception when setting rows < 0")
    void testBuilderInvalidRows() {
        assertThrows(IllegalArgumentException.class, () -> 
            TwGridPane.create().rows(-1).build()
        );
    }

    @Test
    @DisplayName("Should set uniform gap via builder")
    void testBuilderGap() {
        TwGridPane grid = TwGridPane.create().gap(12.0).build();
        
        assertEquals(12.0, grid.getGapX(), 0.001);
        assertEquals(12.0, grid.getGapY(), 0.001);
    }

    @Test
    @DisplayName("Should set separate X and Y gaps via builder")
    void testBuilderSeparateGaps() {
        TwGridPane grid = TwGridPane.create()
            .gapX(8.0)
            .gapY(16.0)
            .build();
        
        assertEquals(8.0, grid.getGapX(), 0.001);
        assertEquals(16.0, grid.getGapY(), 0.001);
    }

    @Test
    @DisplayName("Should set autoFlow mode via builder")
    void testBuilderAutoFlow() {
        TwGridPane grid = TwGridPane.create()
            .autoFlow(TwGridPane.AutoFlow.COL)
            .build();
        
        assertEquals(TwGridPane.AutoFlow.COL, grid.getAutoFlow());
        
        grid = TwGridPane.create()
            .autoFlow(TwGridPane.AutoFlow.ROW_DENSE)
            .build();
        
        assertEquals(TwGridPane.AutoFlow.ROW_DENSE, grid.getAutoFlow());
    }

    @Test
    @DisplayName("Should set masonry mode via builder")
    void testBuilderMasonry() {
        TwGridPane grid = TwGridPane.create().masonry(3).build();
        
        assertEquals(3, grid.getCols());
    }

    @Test
    @DisplayName("Should set padding via builder with double")
    void testBuilderPaddingDouble() {
        TwGridPane grid = TwGridPane.create().padding(10.0).build();
        
        Insets padding = grid.getInternalPadding();
        assertEquals(10.0, padding.getTop(), 0.001);
        assertEquals(10.0, padding.getRight(), 0.001);
        assertEquals(10.0, padding.getBottom(), 0.001);
        assertEquals(10.0, padding.getLeft(), 0.001);
    }

    @Test
    @DisplayName("Should set padding via builder with Insets")
    void testBuilderPaddingInsets() {
        Insets customPadding = new Insets(5, 10, 15, 20);
        TwGridPane grid = TwGridPane.create().padding(customPadding).build();
        
        assertEquals(customPadding, grid.getInternalPadding());
    }

    @Test
    @DisplayName("Should support fluent chaining in builder")
    void testBuilderFluentChaining() {
        TwGridPane grid = TwGridPane.create()
            .cols(4)
            .rows(3)
            .gap(16.0)
            .autoFlow(TwGridPane.AutoFlow.COL)
            .padding(8.0)
            .build();
        
        assertNotNull(grid);
        assertEquals(4, grid.getCols());
        assertEquals(16.0, grid.getGapX(), 0.001);
        assertEquals(16.0, grid.getGapY(), 0.001);
        assertEquals(TwGridPane.AutoFlow.COL, grid.getAutoFlow());
    }

    @Test
    @DisplayName("Should set col span for child node")
    void testSetColSpan() {
        Label child = new Label("Test");
        
        assertEquals(1, TwGridPane.getColSpan(child));
        
        TwGridPane.setColSpan(child, 2);
        assertEquals(2, TwGridPane.getColSpan(child));
        
        TwGridPane.setColSpan(child, 3);
        assertEquals(3, TwGridPane.getColSpan(child));
    }

    @Test
    @DisplayName("Should throw exception when setting invalid col span")
    void testSetInvalidColSpan() {
        Label child = new Label("Test");
        
        assertThrows(IllegalArgumentException.class, () -> 
            TwGridPane.setColSpan(child, 0)
        );
        
        assertThrows(IllegalArgumentException.class, () -> 
            TwGridPane.setColSpan(child, -1)
        );
    }

    @Test
    @DisplayName("Should throw exception when setting col span on null node")
    void testSetColSpanOnNullNode() {
        assertThrows(NullPointerException.class, () -> 
            TwGridPane.setColSpan(null, 2)
        );
    }

    @Test
    @DisplayName("Should set row span for child node")
    void testSetRowSpan() {
        Label child = new Label("Test");
        
        assertEquals(1, TwGridPane.getRowSpan(child));
        
        TwGridPane.setRowSpan(child, 2);
        assertEquals(2, TwGridPane.getRowSpan(child));
    }

    @Test
    @DisplayName("Should throw exception when setting invalid row span")
    void testSetInvalidRowSpan() {
        Label child = new Label("Test");
        
        assertThrows(IllegalArgumentException.class, () -> 
            TwGridPane.setRowSpan(child, 0)
        );
    }

    @Test
    @DisplayName("Should throw exception when setting row span on null node")
    void testSetRowSpanOnNullNode() {
        assertThrows(NullPointerException.class, () -> 
            TwGridPane.setRowSpan(null, 2)
        );
    }

    @Test
    @DisplayName("Should place child in template area")
    void testPlaceInArea() {
        TwGridPane grid = TwGridPane.create()
            .areas("header header", "sidebar main")
            .build();
        
        Label header = new Label("Header");
        Label sidebar = new Label("Sidebar");
        Label main = new Label("Main");
        
        grid.placeIn(header, "header");
        grid.placeIn(sidebar, "sidebar");
        grid.placeIn(main, "main");
        
        assertTrue(grid.getChildren().contains(header));
        assertTrue(grid.getChildren().contains(sidebar));
        assertTrue(grid.getChildren().contains(main));
    }

    @Test
    @DisplayName("Should throw exception when placing in non-existent area")
    void testPlaceInNonExistentArea() {
        TwGridPane grid = TwGridPane.create()
            .areas("header main")
            .build();
        
        Label child = new Label("Test");
        
        assertThrows(IllegalArgumentException.class, () -> 
            grid.placeIn(child, "nonexistent")
        );
    }

    @Test
    @DisplayName("Should throw exception when placing null node")
    void testPlaceInNullNode() {
        TwGridPane grid = TwGridPane.create()
            .areas("header main")
            .build();
        
        assertThrows(IllegalArgumentException.class, () -> 
            grid.placeIn(null, "header")
        );
    }

    @Test
    @DisplayName("Should have correct default values")
    void testDefaultValues() {
        TwGridPane grid = TwGridPane.create().build();
        
        assertEquals(1, grid.getCols());
        assertEquals(0.0, grid.getGapX(), 0.001);
        assertEquals(0.0, grid.getGapY(), 0.001);
        assertEquals(TwGridPane.AutoFlow.ROW, grid.getAutoFlow());
        assertEquals(Insets.EMPTY, grid.getInternalPadding());
    }

    @Test
    @DisplayName("Should handle multiple children with different spans")
    void testMultipleChildrenSpans() {
        Label child1 = new Label("First");
        Label child2 = new Label("Second");
        Button child3 = new Button("Third");
        
        TwGridPane.setColSpan(child1, 2);
        TwGridPane.setColSpan(child2, 1);
        TwGridPane.setColSpan(child3, 3);
        
        TwGridPane.setRowSpan(child1, 1);
        TwGridPane.setRowSpan(child2, 2);
        TwGridPane.setRowSpan(child3, 1);
        
        assertEquals(2, TwGridPane.getColSpan(child1));
        assertEquals(1, TwGridPane.getColSpan(child2));
        assertEquals(3, TwGridPane.getColSpan(child3));
        
        assertEquals(1, TwGridPane.getRowSpan(child1));
        assertEquals(2, TwGridPane.getRowSpan(child2));
        assertEquals(1, TwGridPane.getRowSpan(child3));
    }
}
