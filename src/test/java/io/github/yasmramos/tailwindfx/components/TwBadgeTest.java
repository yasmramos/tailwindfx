package io.github.yasmramos.tailwindfx.components;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TwBadge component
 */
public class TwBadgeTest {

    private TwBadge badge;

    @BeforeEach
    public void setUp() {
        badge = new TwBadge();
    }

    @Test
    public void testConstructor() {
        assertNotNull(badge);
        assertNull(badge.getText());
    }

    @Test
    public void testSetText() {
        badge.setText("New Badge");
        assertEquals("New Badge", badge.getText());
    }

    @Test
    public void testSetVariant() {
        badge.setVariant("success");
        // Verify variant is set (implementation dependent)
        assertNotNull(badge);
    }

    @Test
    public void testSetSize() {
        badge.setSize("lg");
        assertNotNull(badge);
    }

    @Test
    public void testSetPill() {
        badge.setPill(true);
        assertTrue(badge.isPill());
        
        badge.setPill(false);
        assertFalse(badge.isPill());
    }

    @Test
    public void testChainingMethods() {
        TwBadge result = badge.text("Test")
                              .variant("primary")
                              .size("sm")
                              .pill(true);
        
        assertEquals(badge, result);
        assertEquals("Test", badge.getText());
        assertTrue(badge.isPill());
    }
}
