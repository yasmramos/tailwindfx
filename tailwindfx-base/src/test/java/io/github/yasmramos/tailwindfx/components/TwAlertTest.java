package io.github.yasmramos.tailwindfx.components;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TwAlert component.
 */
@ExtendWith(ApplicationExtension.class)
public class TwAlertTest {

    @Test
    public void testDefaultConstructor() {
        TwAlert alert = new TwAlert();
        
        assertNotNull(alert);
        assertEquals(TwAlert.AlertType.INFO, alert.getType());
        assertTrue(alert.getStyleClass().contains("tw-alert"));
        assertTrue(alert.getStyleClass().contains("alert-info"));
    }

    @Test
    public void testConstructorWithContent() {
        TwAlert alert = new TwAlert("Test message");
        
        assertNotNull(alert);
        assertEquals(1, alert.getChildren().size());
        assertTrue(alert.getChildren().get(0) instanceof javafx.scene.control.Label);
    }

    @Test
    public void testConstructorWithTypeAndContent() {
        TwAlert alert = new TwAlert(TwAlert.AlertType.SUCCESS, "Success message");
        
        assertNotNull(alert);
        assertEquals(TwAlert.AlertType.SUCCESS, alert.getType());
        assertTrue(alert.getStyleClass().contains("alert-success"));
    }

    @Test
    public void testSetType() {
        TwAlert alert = new TwAlert();
        
        alert.setType(TwAlert.AlertType.WARNING);
        assertEquals(TwAlert.AlertType.WARNING, alert.getType());
        assertTrue(alert.getStyleClass().contains("alert-warning"));
        assertFalse(alert.getStyleClass().contains("alert-info"));
        
        alert.setType(TwAlert.AlertType.ERROR);
        assertEquals(TwAlert.AlertType.ERROR, alert.getType());
        assertTrue(alert.getStyleClass().contains("alert-error"));
    }

    @Test
    public void testSetContent() {
        TwAlert alert = new TwAlert();
        alert.setContent("New content");
        
        assertEquals(1, alert.getChildren().size());
        javafx.scene.control.Label label = (javafx.scene.control.Label) alert.getChildren().get(0);
        assertEquals("New content", label.getText());
    }

    @Test
    public void testStaticInfoMethod() {
        TwAlert alert = TwAlert.info("Info message");
        
        assertNotNull(alert);
        assertEquals(TwAlert.AlertType.INFO, alert.getType());
        assertTrue(alert.getStyleClass().contains("alert-info"));
    }

    @Test
    public void testStaticSuccessMethod() {
        TwAlert alert = TwAlert.success("Success message");
        
        assertNotNull(alert);
        assertEquals(TwAlert.AlertType.SUCCESS, alert.getType());
        assertTrue(alert.getStyleClass().contains("alert-success"));
    }

    @Test
    public void testStaticWarningMethod() {
        TwAlert alert = TwAlert.warning("Warning message");
        
        assertNotNull(alert);
        assertEquals(TwAlert.AlertType.WARNING, alert.getType());
        assertTrue(alert.getStyleClass().contains("alert-warning"));
    }

    @Test
    public void testStaticErrorMethod() {
        TwAlert alert = TwAlert.error("Error message");
        
        assertNotNull(alert);
        assertEquals(TwAlert.AlertType.ERROR, alert.getType());
        assertTrue(alert.getStyleClass().contains("alert-error"));
    }

    @Test
    public void testSpacingAndPadding() {
        TwAlert alert = new TwAlert();
        
        assertEquals(12, alert.getSpacing(), 0.001);
        assertNotNull(alert.getPadding());
        assertEquals(16, alert.getPadding().getTop(), 0.001);
        assertEquals(16, alert.getPadding().getRight(), 0.001);
        assertEquals(16, alert.getPadding().getBottom(), 0.001);
        assertEquals(16, alert.getPadding().getLeft(), 0.001);
    }
}
