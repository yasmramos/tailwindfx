package io.github.yasmramos.tailwindfx;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.geometry.Insets;

import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.ApplicationTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for layout-dependent styles (margins, gaps, flex).
 */
@DisplayName("Layout-Dependent Style Tests")
public class TwStyleLayoutTest extends ApplicationTest {

    private HBox hbox;
    private VBox vbox;
    private GridPane grid;
    private Button button;

    @Override
    public void start(javafx.stage.Stage stage) {
        hbox = new HBox();
        vbox = new VBox();
        grid = new GridPane();
        button = new Button("Test");
        
        javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane();
        root.getChildren().addAll(hbox, vbox, grid);
        
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 800, 600);
        TailwindFX.install(scene);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    public void setUp() {
        interact(() -> {
            hbox.getChildren().clear();
            vbox.getChildren().clear();
            grid.getChildren().clear();
            button = new Button("Test");
        });
    }

    @Test
    public void testMarginAppliedInHBox() {
        interact(() -> hbox.getChildren().add(button));
        TwStyle.apply(button, "m-4");
        
        Insets margin = HBox.getMargin(button);
        assertNotNull(margin);
        assertEquals(16.0, margin.getTop(), 0.01);
        assertEquals(16.0, margin.getRight(), 0.01);
        assertEquals(16.0, margin.getBottom(), 0.01);
        assertEquals(16.0, margin.getLeft(), 0.01);
    }

    @Test
    public void testMarginHorizontalApplied() {
        interact(() -> hbox.getChildren().add(button));
        TwStyle.apply(button, "mx-2");
        
        Insets margin = HBox.getMargin(button);
        assertNotNull(margin);
        assertEquals(0.0, margin.getTop(), 0.01);
        assertEquals(8.0, margin.getRight(), 0.01);
        assertEquals(0.0, margin.getBottom(), 0.01);
        assertEquals(8.0, margin.getLeft(), 0.01);
    }

    @Test
    public void testMarginVerticalApplied() {
        interact(() -> vbox.getChildren().add(button));
        TwStyle.apply(button, "my-3");
        
        Insets margin = VBox.getMargin(button);
        assertNotNull(margin);
        assertEquals(12.0, margin.getTop(), 0.01);
        assertEquals(0.0, margin.getRight(), 0.01);
        assertEquals(12.0, margin.getBottom(), 0.01);
        assertEquals(0.0, margin.getLeft(), 0.01);
    }

    @Test
    public void testGapAppliedToHBox() {
        TwStyle.apply(hbox, "gap-4");
        
        assertEquals(16.0, hbox.getSpacing(), 0.01);
    }

    @Test
    public void testGapAppliedToVBox() {
        TwStyle.apply(vbox, "gap-2");
        
        assertEquals(8.0, vbox.getSpacing(), 0.01);
    }

    @Test
    public void testGapHorizontalAppliedToGridPane() {
        TwStyle.apply(grid, "gap-x-5");
        
        assertEquals(20.0, grid.getHgap(), 0.01);
        assertEquals(0.0, grid.getVgap(), 0.01);
    }

    @Test
    public void testGapVerticalAppliedToGridPane() {
        TwStyle.apply(grid, "gap-y-6");
        
        assertEquals(0.0, grid.getHgap(), 0.01);
        assertEquals(24.0, grid.getVgap(), 0.01);
    }

    @Test
    public void testGrowAppliedInHBox() {
        interact(() -> hbox.getChildren().add(button));
        TwStyle.apply(button, "grow");
        
        assertEquals(javafx.scene.layout.Priority.ALWAYS, HBox.getHgrow(button));
    }

    @Test
    public void testGrowAppliedInVBox() {
        interact(() -> vbox.getChildren().add(button));
        TwStyle.apply(button, "flex-1");
        
        assertEquals(javafx.scene.layout.Priority.ALWAYS, VBox.getVgrow(button));
    }

    @Test
    public void testFlexNoneApplied() {
        interact(() -> hbox.getChildren().add(button));
        TwStyle.apply(button, "flex-none");
        
        assertEquals(javafx.scene.layout.Priority.NEVER, HBox.getHgrow(button));
    }

    @Test
    public void testCombinedLayoutStyles() {
        interact(() -> hbox.getChildren().add(button));
        TwStyle.apply(button, "m-2", "grow");
        
        Insets margin = HBox.getMargin(button);
        assertNotNull(margin);
        assertEquals(8.0, margin.getTop(), 0.01);
        assertEquals(javafx.scene.layout.Priority.ALWAYS, HBox.getHgrow(button));
    }

    @Test
    public void testArbitraryMarginValue() {
        interact(() -> hbox.getChildren().add(button));
        TwStyle.apply(button, "m-[20px]");
        
        Insets margin = HBox.getMargin(button);
        assertNotNull(margin);
        assertEquals(20.0, margin.getTop(), 0.01);
        assertEquals(20.0, margin.getRight(), 0.01);
        assertEquals(20.0, margin.getBottom(), 0.01);
        assertEquals(20.0, margin.getLeft(), 0.01);
    }

    @Test
    public void testMarginBeforeAddingToParent_UsesListener() {
        // Apply margin before adding to parent
        TwStyle.apply(button, "m-3");
        
        // Initially no parent, so margin should not be set yet
        Insets marginBefore = HBox.getMargin(button);
        assertNull(marginBefore); // Or default empty insets
        
        // Now add to parent - listener should trigger
        interact(() -> hbox.getChildren().add(button));
        
        // Give JavaFX time to process the listener
        sleep(200);
        
        // Margin should now be applied
        Insets marginAfter = HBox.getMargin(button);
        assertNotNull(marginAfter);
        assertEquals(12.0, marginAfter.getTop(), 0.01);
    }
}
