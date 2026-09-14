package io.github.yasmramos.tailwindfx.style;

import io.github.yasmramos.tailwindfx.layout.TwFlexPane;
import io.github.yasmramos.tailwindfx.layout.TwGridPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the Styles utility class.
 * Covers all style methods including grid, flex, spacing, effects, and transforms.
 */
@DisplayName("Styles Utility Tests")
class StylesTest extends ApplicationTest {

    private Pane container;
    private Region child;
    private ImageView imageView;

    @BeforeEach
    void setUp() {
        container = new Pane();
        child = new Region();
        container.getChildren().add(child);
        imageView = new ImageView();
    }

    // ==================== GRID TESTS ====================

    @Test
    @DisplayName("colSpan sets GridPane column span correctly")
    void testColSpan() {
        GridPane grid = new GridPane();
        Region cell = new Region();
        grid.add(cell, 0, 0);

        Node result = Styles.colSpan(cell, 3);

        assertSame(cell, result);
        assertEquals(3, GridPane.getColumnSpan(cell));
    }

    @Test
    @DisplayName("rowSpan sets GridPane row span correctly")
    void testRowSpan() {
        GridPane grid = new GridPane();
        Region cell = new Region();
        grid.add(cell, 0, 0);

        Node result = Styles.rowSpan(cell, 2);

        assertSame(cell, result);
        assertEquals(2, GridPane.getRowSpan(cell));
    }

    @Test
    @DisplayName("colSpanFull sets maximum column span")
    void testColSpanFull() {
        GridPane grid = new GridPane();
        Region cell = new Region();
        grid.add(cell, 0, 0);

        Styles.colSpanFull(cell);

        assertEquals(Integer.MAX_VALUE, GridPane.getColumnSpan(cell));
    }

    @Test
    @DisplayName("rowSpanFull sets maximum row span")
    void testRowSpanFull() {
        GridPane grid = new GridPane();
        Region cell = new Region();
        grid.add(cell, 0, 0);

        Styles.rowSpanFull(cell);

        assertEquals(Integer.MAX_VALUE, GridPane.getRowSpan(cell));
    }

    // ==================== FLEX TESTS ====================

    @Test
    @DisplayName("flex1 sets HBox grow priority to ALWAYS")
    void testFlex1() {
        HBox hbox = new HBox();
        Region item = new Region();
        hbox.getChildren().add(item);

        Node result = Styles.flex1(item);

        assertSame(item, result);
        assertEquals(Priority.ALWAYS, HBox.getHgrow(item));
    }

    @Test
    @DisplayName("flexNone removes HBox grow priority")
    void testFlexNone() {
        HBox hbox = new HBox();
        Region item = new Region();
        hbox.getChildren().add(item);
        HBox.setHgrow(item, Priority.ALWAYS);

        Styles.flexNone(item);

        assertNull(HBox.getHgrow(item));
    }

    @Test
    @DisplayName("flexAuto sets VBox grow priority to ALWAYS")
    void testFlexAuto() {
        VBox vbox = new VBox();
        Region item = new Region();
        vbox.getChildren().add(item);

        Node result = Styles.flexAuto(item);

        assertSame(item, result);
        assertEquals(Priority.ALWAYS, VBox.getVgrow(item));
    }

    @Test
    @DisplayName("order sets HBox margin index for ordering")
    void testOrder() {
        HBox hbox = new HBox();
        Region item = new Region();
        hbox.getChildren().add(item);

        Styles.order(item, 5);

        Insets margins = HBox.getMargin(item);
        assertNotNull(margins);
    }

    @Test
    @DisplayName("orderFirst sets negative margin index")
    void testOrderFirst() {
        HBox hbox = new HBox();
        Region item = new Region();
        hbox.getChildren().add(item);

        Styles.orderFirst(item);

        Insets margins = HBox.getMargin(item);
        assertNotNull(margins);
    }

    @Test
    @DisplayName("orderLast sets large positive margin index")
    void testOrderLast() {
        HBox hbox = new HBox();
        Region item = new Region();
        hbox.getChildren().add(item);

        Styles.orderLast(item);

        Insets margins = HBox.getMargin(item);
        assertNotNull(margins);
    }

    // ==================== ALIGNMENT TESTS ====================

    @Test
    @DisplayName("selfCenter sets VBox alignment to CENTER")
    void testSelfCenter() {
        VBox vbox = new VBox();
        Region item = new Region();
        vbox.getChildren().add(item);

        Styles.selfCenter(item);

        assertEquals(javafx.geometry.Pos.CENTER, VBox.getAlignment(item));
    }

    @Test
    @DisplayName("selfStart sets VBox alignment to TOP_LEFT")
    void testSelfStart() {
        VBox vbox = new VBox();
        Region item = new Region();
        vbox.getChildren().add(item);

        Styles.selfStart(item);

        assertEquals(javafx.geometry.Pos.TOP_LEFT, VBox.getAlignment(item));
    }

    @Test
    @DisplayName("selfEnd sets VBox alignment to BOTTOM_RIGHT")
    void testSelfEnd() {
        VBox vbox = new VBox();
        Region item = new Region();
        vbox.getChildren().add(item);

        Styles.selfEnd(item);

        assertEquals(javafx.geometry.Pos.BOTTOM_RIGHT, VBox.getAlignment(item));
    }

    @Test
    @DisplayName("justifySelfCenter sets GridPane halignment to CENTER")
    void testJustifySelfCenter() {
        GridPane grid = new GridPane();
        Region item = new Region();
        grid.add(item, 0, 0);

        Styles.justifySelfCenter(item);

        assertEquals(HPos.CENTER, GridPane.getHalignment(item));
    }

    @Test
    @DisplayName("justifySelfStart sets GridPane halignment to LEFT")
    void testJustifySelfStart() {
        GridPane grid = new GridPane();
        Region item = new Region();
        grid.add(item, 0, 0);

        Styles.justifySelfStart(item);

        assertEquals(HPos.LEFT, GridPane.getHalignment(item));
    }

    @Test
    @DisplayName("justifySelfEnd sets GridPane halignment to RIGHT")
    void testJustifySelfEnd() {
        GridPane grid = new GridPane();
        Region item = new Region();
        grid.add(item, 0, 0);

        Styles.justifySelfEnd(item);

        assertEquals(HPos.RIGHT, GridPane.getHalignment(item));
    }

    // ==================== SPACING TESTS ====================

    @Test
    @DisplayName("mx sets left and right padding")
    void testMx() {
        Region region = new Region();

        Styles.mx(region, 4);

        assertEquals(16.0, region.getPadding().getLeft());
        assertEquals(16.0, region.getPadding().getRight());
    }

    @Test
    @DisplayName("my sets top and bottom padding")
    void testMy() {
        Region region = new Region();

        Styles.my(region, 3);

        assertEquals(12.0, region.getPadding().getTop());
        assertEquals(12.0, region.getPadding().getBottom());
    }

    @Test
    @DisplayName("m sets all sides padding")
    void testM() {
        Region region = new Region();

        Styles.m(region, 2);

        Insets padding = region.getPadding();
        assertEquals(8.0, padding.getTop());
        assertEquals(8.0, padding.getRight());
        assertEquals(8.0, padding.getBottom());
        assertEquals(8.0, padding.getLeft());
    }

    @Test
    @DisplayName("px sets left and right padding only")
    void testPx() {
        Region region = new Region();

        Styles.px(region, 6);

        assertEquals(24.0, region.getPadding().getLeft());
        assertEquals(24.0, region.getPadding().getRight());
        assertEquals(0.0, region.getPadding().getTop());
        assertEquals(0.0, region.getPadding().getBottom());
    }

    @Test
    @DisplayName("py sets top and bottom padding only")
    void testPy() {
        Region region = new Region();

        Styles.py(region, 5);

        assertEquals(20.0, region.getPadding().getTop());
        assertEquals(20.0, region.getPadding().getBottom());
        assertEquals(0.0, region.getPadding().getLeft());
        assertEquals(0.0, region.getPadding().getRight());
    }

    // ==================== Z-INDEX TESTS ====================

    @Test
    @DisplayName("z sets translateZ for layering")
    void testZ() {
        Region region = new Region();

        Styles.z(region, 50);

        assertEquals(50.0, region.getTranslateZ());
    }

    @Test
    @DisplayName("zAuto resets translateZ to 0")
    void testZAuto() {
        Region region = new Region();
        region.setTranslateZ(100.0);

        Styles.zAuto(region);

        assertEquals(0.0, region.getTranslateZ());
    }

    // ==================== EFFECTS TESTS ====================

    @Test
    @DisplayName("grayscale applies full color adjustment")
    void testGrayscale() {
        Rectangle rect = new Rectangle(100, 100);

        Styles.grayscale(rect);

        assertNotNull(rect.getEffect());
        assertTrue(rect.getEffect() instanceof javafx.scene.effect.ColorAdjust);
        ColorAdjust adjust = (ColorAdjust) rect.getEffect();
        assertEquals(1.0, adjust.getSaturation());
    }

    @Test
    @DisplayName("brightness applies brightness adjustment")
    void testBrightness() {
        Rectangle rect = new Rectangle(100, 100);

        Styles.brightness(rect, 0.75);

        assertNotNull(rect.getEffect());
        assertTrue(rect.getEffect() instanceof javafx.scene.effect.ColorAdjust);
        ColorAdjust adjust = (ColorAdjust) rect.getEffect();
        assertEquals(-0.25, adjust.getBrightness(), 0.01);
    }

    @Test
    @DisplayName("contrast applies contrast adjustment")
    void testContrast() {
        Rectangle rect = new Rectangle(100, 100);

        Styles.contrast(rect, 1.5);

        assertNotNull(rect.getEffect());
        assertTrue(rect.getEffect() instanceof javafx.scene.effect.ColorAdjust);
        ColorAdjust adjust = (ColorAdjust) rect.getEffect();
        assertEquals(0.5, adjust.getContrast(), 0.01);
    }

    @Test
    @DisplayName("hueRotate applies hue rotation")
    void testHueRotate() {
        Rectangle rect = new Rectangle(100, 100);

        Styles.hueRotate(rect, 90);

        assertNotNull(rect.getEffect());
        assertTrue(rect.getEffect() instanceof javafx.scene.effect.ColorAdjust);
        ColorAdjust adjust = (ColorAdjust) rect.getEffect();
        assertEquals(0.25, adjust.getHue(), 0.01);
    }

    @Test
    @DisplayName("invert applies invert adjustment")
    void testInvert() {
        Rectangle rect = new Rectangle(100, 100);

        Styles.invert(rect, 1.0);

        assertNotNull(rect.getEffect());
        assertTrue(rect.getEffect() instanceof javafx.scene.effect.ColorAdjust);
        ColorAdjust adjust = (ColorAdjust) rect.getEffect();
        assertEquals(1.0, adjust.getInvert(), 0.01);
    }

    @Test
    @DisplayName("saturate applies saturation adjustment")
    void testSaturate() {
        Rectangle rect = new Rectangle(100, 100);

        Styles.saturate(rect, 2.0);

        assertNotNull(rect.getEffect());
        assertTrue(rect.getEffect() instanceof javafx.scene.effect.ColorAdjust);
        ColorAdjust adjust = (ColorAdjust) rect.getEffect();
        assertEquals(1.0, adjust.getSaturation(), 0.01);
    }

    @Test
    @DisplayName("sepia applies sepia effect")
    void testSepia() {
        Rectangle rect = new Rectangle(100, 100);

        Styles.sepia(rect);

        assertNotNull(rect.getEffect());
    }

    @Test
    @DisplayName("blur applies blur effect")
    void testBlur() {
        Rectangle rect = new Rectangle(100, 100);

        Styles.blur(rect, 4);

        assertNotNull(rect.getEffect());
        assertTrue(rect.getEffect() instanceof javafx.scene.effect.Blur);
    }

    @Test
    @DisplayName("dropShadow applies drop shadow effect")
    void testDropShadow() {
        Rectangle rect = new Rectangle(100, 100);

        Styles.dropShadow(rect, 4, "rgba(0,0,0,0.25)");

        assertNotNull(rect.getEffect());
        assertTrue(rect.getEffect() instanceof javafx.scene.effect.DropShadow);
    }

    // ==================== TRANSFORM TESTS ====================

    @Test
    @DisplayName("skewX applies shear transform")
    void testSkewX() {
        Rectangle rect = new Rectangle(100, 100);

        Styles.skewX(rect, 6);

        assertNotNull(rect.getTransforms());
        assertFalse(rect.getTransforms().isEmpty());
        assertTrue(rect.getTransforms().get(0) instanceof Shear);
    }

    @Test
    @DisplayName("skewY applies shear transform")
    void testSkewY() {
        Rectangle rect = new Rectangle(100, 100);

        Styles.skewY(rect, 6);

        assertNotNull(rect.getTransforms());
        assertFalse(rect.getTransforms().isEmpty());
    }

    @Test
    @DisplayName("rotate applies rotate transform")
    void testRotate() {
        Rectangle rect = new Rectangle(100, 100);

        Styles.rotate(rect, 45);

        assertNotNull(rect.getTransforms());
        assertFalse(rect.getTransforms().isEmpty());
        assertTrue(rect.getTransforms().get(0) instanceof javafx.scene.transform.Rotate);
    }

    @Test
    @DisplayName("scale applies scale transform")
    void testScale() {
        Rectangle rect = new Rectangle(100, 100);

        Styles.scale(rect, 1.5);

        assertNotNull(rect.getTransforms());
        assertFalse(rect.getTransforms().isEmpty());
        assertTrue(rect.getTransforms().get(0) instanceof javafx.scene.transform.Scale);
    }

    @Test
    @DisplayName("translate applies translate transform")
    void testTranslate() {
        Rectangle rect = new Rectangle(100, 100);

        Styles.translate(rect, 10, 20);

        assertEquals(10.0, rect.getTranslateX());
        assertEquals(20.0, rect.getTranslateY());
    }

    // ==================== OBJECT FIT TESTS ====================

    @Test
    @DisplayName("objectCover sets preserveRatio and fit dimensions")
    void testObjectCover() {
        imageView.setImage(new javafx.scene.image.Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="));

        Styles.objectCover(imageView);

        assertFalse(imageView.isPreserveRatio());
    }

    @Test
    @DisplayName("objectContain sets preserveRatio to true")
    void testObjectContain() {
        imageView.setImage(new javafx.scene.image.Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="));

        Styles.objectContain(imageView);

        assertTrue(imageView.isPreserveRatio());
    }

    @Test
    @DisplayName("objectFill disables ratio preservation")
    void testObjectFill() {
        imageView.setImage(new javafx.scene.image.Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="));

        Styles.objectFill(imageView);

        assertFalse(imageView.isPreserveRatio());
    }

    @Test
    @DisplayName("objectNone disables ratio preservation")
    void testObjectNone() {
        imageView.setImage(new javafx.scene.image.Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="));

        Styles.objectNone(imageView);

        assertFalse(imageView.isPreserveRatio());
    }

    @Test
    @DisplayName("objectScaleDown enables ratio preservation")
    void testObjectScaleDown() {
        imageView.setImage(new javafx.scene.image.Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="));

        Styles.objectScaleDown(imageView);

        assertTrue(imageView.isPreserveRatio());
    }

    // ==================== CHAINING TESTS ====================

    @Test
    @DisplayName("Methods support chaining by returning the same node")
    void testChaining() {
        Region region = new Region();

        Node result = Styles.mx(Styles.my(Styles.p(region, 4), 2), 3);

        assertSame(region, result);
        assertNotNull(region.getPadding());
    }

    @Test
    @DisplayName("Multiple styles can be applied in sequence")
    void testMultipleStyles() {
        Region region = new Region();

        Styles.mx(region, 4);
        Styles.my(region, 2);
        Styles.z(region, 10);

        assertEquals(16.0, region.getPadding().getLeft());
        assertEquals(8.0, region.getPadding().getTop());
        assertEquals(10.0, region.getTranslateZ());
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("colSpan with value 1 works correctly")
    void testColSpanMinimum() {
        GridPane grid = new GridPane();
        Region cell = new Region();
        grid.add(cell, 0, 0);

        Styles.colSpan(cell, 1);

        assertEquals(1, GridPane.getColumnSpan(cell));
    }

    @Test
    @DisplayName("flex operations work on non-flex containers gracefully")
    void testFlexOnNonFlexContainer() {
        Pane pane = new Pane();
        Region item = new Region();
        pane.getChildren().add(item);

        // Should not throw exception
        assertDoesNotThrow(() -> Styles.flex1(item));
    }

    @Test
    @DisplayName("spacing with zero value sets zero padding")
    void testSpacingZero() {
        Region region = new Region();

        Styles.m(region, 0);

        Insets padding = region.getPadding();
        assertEquals(0.0, padding.getTop());
        assertEquals(0.0, padding.getRight());
        assertEquals(0.0, padding.getBottom());
        assertEquals(0.0, padding.getLeft());
    }
}
