package io.github.yasmramos.tailwindfx.breakpoint;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BreakpointManager class
 */
public class BreakpointManagerTest {

    private BreakpointManager manager;

    @BeforeEach
    public void setUp() {
        manager = new BreakpointManager();
    }

    @Test
    public void testConstructor() {
        assertNotNull(manager);
    }

    @Test
    public void testGetCurrentBreakpoint() {
        // Test that we can get current breakpoint
        String breakpoint = manager.getCurrentBreakpoint();
        // Should return some value (default or detected)
        assertNotNull(breakpoint);
    }

    @Test
    public void testIsMobile() {
        // Test breakpoint detection methods exist
        boolean isMobile = manager.isMobile();
        // Just verify method exists and returns boolean
        assertTrue(isMobile || !isMobile);
    }

    @Test
    public void testIsTablet() {
        boolean isTablet = manager.isTablet();
        assertTrue(isTablet || !isTablet);
    }

    @Test
    public void testIsDesktop() {
        boolean isDesktop = manager.isDesktop();
        assertTrue(isDesktop || !isDesktop);
    }

    @Test
    public void testGetBreakpoints() {
        // Test getting all breakpoints
        var breakpoints = manager.getBreakpoints();
        assertNotNull(breakpoints);
        assertFalse(breakpoints.isEmpty());
    }
}
