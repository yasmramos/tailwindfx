package io.github.yasmramos.tailwindfx.layout;

import javafx.geometry.Insets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.testfx.framework.junit5.ApplicationTest;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TwFlexPane layout container.
 */
@DisplayName("TwFlexPane Layout Tests")
class TwFlexPaneTest extends ApplicationTest {

    private TwFlexPane flexPane;

    @Override
    public void start(javafx.stage.Stage stage) {
        flexPane = new TwFlexPane();
        Pane root = new Pane(flexPane);
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void setUp() {
        // flexPane ya está inicializado en start()
    }

    @Test
    @DisplayName("Should create row-direction pane with row() factory")
    void testRowFactory() {
        TwFlexPane row = TwFlexPane.row();
        
        assertNotNull(row);
        assertEquals(TwFlexPane.Direction.ROW, row.getDirection());
        assertFalse(row.isWrap());
        assertEquals(TwFlexPane.Justify.START, row.getJustify());
        assertEquals(TwFlexPane.Align.START, row.getAlign());
    }

    @Test
    @DisplayName("Should create column-direction pane with col() factory")
    void testColFactory() {
        TwFlexPane col = TwFlexPane.col();
        
        assertNotNull(col);
        assertEquals(TwFlexPane.Direction.COL, col.getDirection());
    }

    @Test
    @DisplayName("Should set direction via setter and fluent method")
    void testSetDirection() {
        flexPane.setDirection(TwFlexPane.Direction.COL);
        assertEquals(TwFlexPane.Direction.COL, flexPane.getDirection());
        
        flexPane.direction(TwFlexPane.Direction.ROW);
        assertEquals(TwFlexPane.Direction.ROW, flexPane.getDirection());
    }

    @Test
    @DisplayName("Should throw exception when setting null direction")
    void testSetNullDirection() {
        assertThrows(IllegalArgumentException.class, () -> 
            flexPane.setDirection(null)
        );
        
        assertThrows(IllegalArgumentException.class, () -> 
            flexPane.direction(null)
        );
    }

    @Test
    @DisplayName("Should enable and disable wrap")
    void testSetWrap() {
        assertFalse(flexPane.isWrap());
        
        flexPane.setWrap(true);
        assertTrue(flexPane.isWrap());
        
        flexPane.wrap(false);
        assertFalse(flexPane.isWrap());
    }

    @Test
    @DisplayName("Should set justify property")
    void testSetJustify() {
        flexPane.setJustify(TwFlexPane.Justify.BETWEEN);
        assertEquals(TwFlexPane.Justify.BETWEEN, flexPane.getJustify());
        
        flexPane.justify(TwFlexPane.Justify.CENTER);
        assertEquals(TwFlexPane.Justify.CENTER, flexPane.getJustify());
    }

    @Test
    @DisplayName("Should throw exception when setting null justify")
    void testSetNullJustify() {
        assertThrows(IllegalArgumentException.class, () -> 
            flexPane.setJustify(null)
        );
    }

    @Test
    @DisplayName("Should set align property")
    void testSetAlign() {
        flexPane.setAlign(TwFlexPane.Align.CENTER);
        assertEquals(TwFlexPane.Align.CENTER, flexPane.getAlign());
        
        flexPane.align(TwFlexPane.Align.END);
        assertEquals(TwFlexPane.Align.END, flexPane.getAlign());
    }

    @Test
    @DisplayName("Should throw exception when setting null align")
    void testSetNullAlign() {
        assertThrows(IllegalArgumentException.class, () -> 
            flexPane.setAlign(null)
        );
    }

    @Test
    @DisplayName("Should set alignContent property")
    void testSetAlignContent() {
        flexPane.setAlignContent(TwFlexPane.AlignContent.CENTER);
        assertEquals(TwFlexPane.AlignContent.CENTER, flexPane.getAlignContent());
        
        flexPane.alignContent(TwFlexPane.AlignContent.BETWEEN);
        assertEquals(TwFlexPane.AlignContent.BETWEEN, flexPane.getAlignContent());
    }

    @Test
    @DisplayName("Should throw exception when setting null alignContent")
    void testSetNullAlignContent() {
        assertThrows(IllegalArgumentException.class, () -> 
            flexPane.setAlignContent(null)
        );
    }

    @Test
    @DisplayName("Should set uniform gap")
    void testSetGap() {
        flexPane.gap(16.0);
        assertEquals(16.0, flexPane.getGap(), 0.001);
        assertEquals(16.0, flexPane.getGapX(), 0.001);
        assertEquals(16.0, flexPane.getGapY(), 0.001);
    }

    @Test
    @DisplayName("Should throw exception when setting negative gap")
    void testSetNegativeGap() {
        assertThrows(IllegalArgumentException.class, () -> 
            flexPane.gap(-5.0)
        );
    }

    @Test
    @DisplayName("Should set separate X and Y gaps")
    void testSetSeparateGaps() {
        flexPane.gapX(10.0);
        flexPane.gapY(20.0);
        
        assertEquals(10.0, flexPane.getGapX(), 0.001);
        assertEquals(20.0, flexPane.getGapY(), 0.001);
    }

    @Test
    @DisplayName("Should set padding with Insets")
    void testSetPaddingInsets() {
        Insets padding = new Insets(10, 20, 30, 40);
        flexPane.padding(padding);
        
        assertEquals(padding, flexPane.getPadding());
    }

    @Test
    @DisplayName("Should set uniform padding")
    void testSetUniformPadding() {
        flexPane.padding(15.0);
        
        Insets padding = flexPane.getPadding();
        assertEquals(15.0, padding.getTop(), 0.001);
        assertEquals(15.0, padding.getRight(), 0.001);
        assertEquals(15.0, padding.getBottom(), 0.001);
        assertEquals(15.0, padding.getLeft(), 0.001);
    }

    @Test
    @DisplayName("Should throw exception when setting null padding")
    void testSetNullPadding() {
        assertThrows(IllegalArgumentException.class, () -> 
            flexPane.padding(null)
        );
    }

    @Test
    @DisplayName("Should set and get grow factor for child node")
    void testSetGrow() {
        Label child = new Label("Test");
        
        assertEquals(0.0, TwFlexPane.getGrow(child), 0.001);
        
        TwFlexPane.setGrow(child, 1.0);
        assertEquals(1.0, TwFlexPane.getGrow(child), 0.001);
        
        TwFlexPane.setGrow(child, 2.5);
        assertEquals(2.5, TwFlexPane.getGrow(child), 0.001);
    }

    @Test
    @DisplayName("Should throw exception when setting negative grow")
    void testSetNegativeGrow() {
        Label child = new Label("Test");
        
        assertThrows(IllegalArgumentException.class, () -> 
            TwFlexPane.setGrow(child, -1.0)
        );
    }

    @Test
    @DisplayName("Should throw exception when setting grow on null node")
    void testSetGrowOnNullNode() {
        assertThrows(IllegalArgumentException.class, () -> 
            TwFlexPane.setGrow(null, 1.0)
        );
    }

    @Test
    @DisplayName("Should set and get shrink factor for child node")
    void testSetShrink() {
        Label child = new Label("Test");
        
        assertEquals(1.0, TwFlexPane.getShrink(child), 0.001);
        
        TwFlexPane.setShrink(child, 0.0);
        assertEquals(0.0, TwFlexPane.getShrink(child), 0.001);
        
        TwFlexPane.setShrink(child, 2.0);
        assertEquals(2.0, TwFlexPane.getShrink(child), 0.001);
    }

    @Test
    @DisplayName("Should throw exception when setting negative shrink")
    void testSetNegativeShrink() {
        Label child = new Label("Test");
        
        assertThrows(IllegalArgumentException.class, () -> 
            TwFlexPane.setShrink(child, -0.5)
        );
    }

    @Test
    @DisplayName("Should set and get order for child node")
    void testSetOrder() {
        Label child = new Label("Test");
        
        assertEquals(0, TwFlexPane.getOrder(child));
        
        TwFlexPane.setOrder(child, 5);
        assertEquals(5, TwFlexPane.getOrder(child));
        
        TwFlexPane.setOrder(child, -1);
        assertEquals(-1, TwFlexPane.getOrder(child));
    }

    @Test
    @DisplayName("Should throw exception when setting order on null node")
    void testSetOrderOnNullNode() {
        assertThrows(IllegalArgumentException.class, () -> 
            TwFlexPane.setOrder(null, 1)
        );
    }

    @Test
    @DisplayName("Should set and get alignSelf for child node")
    void testSetAlignSelf() {
        Label child = new Label("Test");
        
        assertNull(TwFlexPane.getAlignSelf(child));
        
        TwFlexPane.setAlignSelf(child, TwFlexPane.Align.END);
        assertEquals(TwFlexPane.Align.END, TwFlexPane.getAlignSelf(child));
        
        TwFlexPane.setAlignSelf(child, null);
        assertNull(TwFlexPane.getAlignSelf(child));
    }

    @Test
    @DisplayName("Should throw exception when setting alignSelf on null node")
    void testSetAlignSelfOnNullNode() {
        assertThrows(IllegalArgumentException.class, () -> 
            TwFlexPane.setAlignSelf(null, TwFlexPane.Align.CENTER)
        );
    }

    @Test
    @DisplayName("Should set and get basis for child node")
    void testSetBasis() {
        Label child = new Label("Test");
        
        assertEquals(-1.0, TwFlexPane.getBasis(child), 0.001);
        
        TwFlexPane.setBasis(child, 0.0);
        assertEquals(0.0, TwFlexPane.getBasis(child), 0.001);
        
        TwFlexPane.setBasis(child, 200.0);
        assertEquals(200.0, TwFlexPane.getBasis(child), 0.001);
    }

    @Test
    @DisplayName("Should throw exception when setting invalid basis")
    void testSetInvalidBasis() {
        Label child = new Label("Test");
        
        assertThrows(IllegalArgumentException.class, () -> 
            TwFlexPane.setBasis(child, -5.0)
        );
    }

    @Test
    @DisplayName("Should support fluent chaining")
    void testFluentChaining() {
        TwFlexPane result = TwFlexPane.row()
            .direction(TwFlexPane.Direction.ROW)
            .wrap(true)
            .justify(TwFlexPane.Justify.BETWEEN)
            .align(TwFlexPane.Align.CENTER)
            .alignContent(TwFlexPane.AlignContent.STRETCH)
            .gap(16.0)
            .gapX(10.0)
            .gapY(20.0)
            .padding(8.0);
        
        assertNotNull(result);
        assertEquals(TwFlexPane.Direction.ROW, result.getDirection());
        assertTrue(result.isWrap());
        assertEquals(TwFlexPane.Justify.BETWEEN, result.getJustify());
        assertEquals(TwFlexPane.Align.CENTER, result.getAlign());
        assertEquals(TwFlexPane.AlignContent.STRETCH, result.getAlignContent());
    }

    @Test
    @DisplayName("Should handle multiple children with different properties")
    void testMultipleChildrenProperties() {
        Label child1 = new Label("First");
        Label child2 = new Label("Second");
        Button child3 = new Button("Third");
        
        TwFlexPane.setGrow(child1, 1.0);
        TwFlexPane.setGrow(child2, 2.0);
        TwFlexPane.setGrow(child3, 0.0);
        
        TwFlexPane.setOrder(child3, -1);
        TwFlexPane.setOrder(child1, 1);
        TwFlexPane.setOrder(child2, 0);
        
        assertEquals(1.0, TwFlexPane.getGrow(child1), 0.001);
        assertEquals(2.0, TwFlexPane.getGrow(child2), 0.001);
        assertEquals(0.0, TwFlexPane.getGrow(child3), 0.001);
        
        assertEquals(1, TwFlexPane.getOrder(child1));
        assertEquals(0, TwFlexPane.getOrder(child2));
        assertEquals(-1, TwFlexPane.getOrder(child3));
    }

    @Test
    @DisplayName("Should have default values correctly initialized")
    void testDefaultValues() {
        TwFlexPane pane = new TwFlexPane();
        
        assertEquals(TwFlexPane.Direction.ROW, pane.getDirection());
        assertFalse(pane.isWrap());
        assertEquals(TwFlexPane.Justify.START, pane.getJustify());
        assertEquals(TwFlexPane.Align.START, pane.getAlign());
        assertEquals(TwFlexPane.AlignContent.START, pane.getAlignContent());
        assertEquals(0.0, pane.getGap(), 0.001);
        assertEquals(0.0, pane.getGapX(), 0.001);
        assertEquals(0.0, pane.getGapY(), 0.001);
        assertEquals(Insets.EMPTY, pane.getPadding());
    }
}
