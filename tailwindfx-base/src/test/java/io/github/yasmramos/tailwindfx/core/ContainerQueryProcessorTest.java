package io.github.yasmramos.tailwindfx.core;

import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ContainerQueryProcessor.
 */
class ContainerQueryProcessorTest {

    @Test
    void testIsContainerQuery_WithValidTokens() {
        assertTrue(ContainerQueryProcessor.isContainerQuery("@min-320"));
        assertTrue(ContainerQueryProcessor.isContainerQuery("@max-768"));
        assertTrue(ContainerQueryProcessor.isContainerQuery("@[sm]"));
        assertTrue(ContainerQueryProcessor.isContainerQuery("@[md]"));
        assertTrue(ContainerQueryProcessor.isContainerQuery("@[lg]"));
        assertTrue(ContainerQueryProcessor.isContainerQuery("@[xl]"));
        assertTrue(ContainerQueryProcessor.isContainerQuery("@[2xl]"));
        assertTrue(ContainerQueryProcessor.isContainerQuery("@[1024px]"));
    }

    @Test
    void testIsContainerQuery_WithInvalidTokens() {
        assertFalse(ContainerQueryProcessor.isContainerQuery(null));
        assertFalse(ContainerQueryProcessor.isContainerQuery(""));
        assertFalse(ContainerQueryProcessor.isContainerQuery("p-4"));
        assertFalse(ContainerQueryProcessor.isContainerQuery("bg-blue-500"));
        assertFalse(ContainerQueryProcessor.isContainerQuery("container"));
    }

    @Test
    void testProcessContainerQuery_MinQuery() {
        var result = ContainerQueryProcessor.processContainerQuery("@min-320");
        assertTrue(result.isContainerQuery());
        assertEquals(ContainerQueryProcessor.QueryType.MIN, result.queryType());
        assertEquals("320", result.value());
        assertEquals("@container (min-width: 320px)", result.cssEquivalent());
    }

    @Test
    void testProcessContainerQuery_MaxQuery() {
        var result = ContainerQueryProcessor.processContainerQuery("@max-768");
        assertTrue(result.isContainerQuery());
        assertEquals(ContainerQueryProcessor.QueryType.MAX, result.queryType());
        assertEquals("768", result.value());
        assertEquals("@container (max-width: 768px)", result.cssEquivalent());
    }

    @Test
    void testProcessContainerQuery_BreakpointSm() {
        var result = ContainerQueryProcessor.processContainerQuery("@[sm]");
        assertTrue(result.isContainerQuery());
        assertEquals(ContainerQueryProcessor.QueryType.BREAKPOINT, result.queryType());
        assertEquals("sm", result.value());
        assertEquals("@container (min-width: 640px)", result.cssEquivalent());
    }

    @Test
    void testProcessContainerQuery_BreakpointMd() {
        var result = ContainerQueryProcessor.processContainerQuery("@[md]");
        assertTrue(result.isContainerQuery());
        assertEquals(ContainerQueryProcessor.QueryType.BREAKPOINT, result.queryType());
        assertEquals("md", result.value());
        assertEquals("@container (min-width: 768px)", result.cssEquivalent());
    }

    @Test
    void testProcessContainerQuery_BreakpointLg() {
        var result = ContainerQueryProcessor.processContainerQuery("@[lg]");
        assertTrue(result.isContainerQuery());
        assertEquals(ContainerQueryProcessor.QueryType.BREAKPOINT, result.queryType());
        assertEquals("lg", result.value());
        assertEquals("@container (min-width: 1024px)", result.cssEquivalent());
    }

    @Test
    void testProcessContainerQuery_BreakpointXl() {
        var result = ContainerQueryProcessor.processContainerQuery("@[xl]");
        assertTrue(result.isContainerQuery());
        assertEquals(ContainerQueryProcessor.QueryType.BREAKPOINT, result.queryType());
        assertEquals("xl", result.value());
        assertEquals("@container (min-width: 1280px)", result.cssEquivalent());
    }

    @Test
    void testProcessContainerQuery_Breakpoint2xl() {
        var result = ContainerQueryProcessor.processContainerQuery("@[2xl]");
        assertTrue(result.isContainerQuery());
        assertEquals(ContainerQueryProcessor.QueryType.BREAKPOINT, result.queryType());
        assertEquals("2xl", result.value());
        assertEquals("@container (min-width: 1536px)", result.cssEquivalent());
    }

    @Test
    void testProcessContainerQuery_CustomBreakpointPixels() {
        var result = ContainerQueryProcessor.processContainerQuery("@[1024px]");
        assertTrue(result.isContainerQuery());
        assertEquals(ContainerQueryProcessor.QueryType.BREAKPOINT, result.queryType());
        assertEquals("1024px", result.value());
        assertEquals("@container (min-width: 1024px)", result.cssEquivalent());
    }

    @Test
    void testProcessContainerQuery_CustomBreakpointRem() {
        var result = ContainerQueryProcessor.processContainerQuery("@[20rem]");
        assertTrue(result.isContainerQuery());
        assertEquals(ContainerQueryProcessor.QueryType.BREAKPOINT, result.queryType());
        assertEquals("20rem", result.value());
        assertEquals("@container (min-width: 320px)", result.cssEquivalent());
    }

    @Test
    void testProcessContainerQuery_NonContainerQuery() {
        var result = ContainerQueryProcessor.processContainerQuery("p-4");
        assertFalse(result.isContainerQuery());
        assertEquals(ContainerQueryProcessor.QueryType.UNKNOWN, result.queryType());
        assertNull(result.value());
        assertNull(result.cssEquivalent());
    }

    @Test
    void testParseSizeValue_Pixels() {
        // Direct test via processing
        var result = ContainerQueryProcessor.processContainerQuery("@min-500px");
        assertEquals("@container (min-width: 500px)", result.cssEquivalent());
    }

    @Test
    void testParseSizeValue_Rem() {
        var result = ContainerQueryProcessor.processContainerQuery("@min-10rem");
        assertEquals("@container (min-width: 160px)", result.cssEquivalent());
    }

    @Test
    void testParseSizeValue_Em() {
        var result = ContainerQueryProcessor.processContainerQuery("@min-5em");
        assertEquals("@container (min-width: 80px)", result.cssEquivalent());
    }

    @Test
    void testParseSizeValue_PlainNumber() {
        var result = ContainerQueryProcessor.processContainerQuery("@min-400");
        assertEquals("@container (min-width: 400px)", result.cssEquivalent());
    }

    @Test
    void testGetBreakpoints_ReturnsStandardBreakpoints() {
        Map<String, Integer> breakpoints = ContainerQueryProcessor.getBreakpoints();
        
        assertEquals(5, breakpoints.size());
        assertEquals(640, breakpoints.get("sm"));
        assertEquals(768, breakpoints.get("md"));
        assertEquals(1024, breakpoints.get("lg"));
        assertEquals(1280, breakpoints.get("xl"));
        assertEquals(1536, breakpoints.get("2xl"));
    }

    @Test
    void testGetBreakpoints_ReturnsUnmodifiableMap() {
        assertThrows(UnsupportedOperationException.class, () -> {
            ContainerQueryProcessor.getBreakpoints().put("custom", 999);
        });
    }

    @Test
    void testAddBreakpoint() {
        ContainerQueryProcessor.addBreakpoint("3xl", 1920);
        Map<String, Integer> breakpoints = ContainerQueryProcessor.getBreakpoints();
        assertEquals(1920, breakpoints.get("3xl"));
        
        // Clean up
        ContainerQueryProcessor.removeBreakpoint("3xl");
    }

    @Test
    void testRemoveBreakpoint() {
        ContainerQueryProcessor.addBreakpoint("test", 1000);
        Integer removed = ContainerQueryProcessor.removeBreakpoint("test");
        
        assertEquals(1000, removed);
        assertNull(ContainerQueryProcessor.getBreakpoints().get("test"));
    }

    @Test
    void testRemoveNonExistentBreakpoint() {
        Integer removed = ContainerQueryProcessor.removeBreakpoint("nonexistent");
        assertNull(removed);
    }

    @Test
    void testProcessContainerQuery_UnknownFormat() {
        var result = ContainerQueryProcessor.processContainerQuery("@unknown-format");
        assertTrue(result.isContainerQuery());
        assertEquals(ContainerQueryProcessor.QueryType.UNKNOWN, result.queryType());
        assertEquals("@unknown-format", result.value());
        assertNull(result.cssEquivalent());
    }

    @Test
    void testProcessContainerQuery_EmptyBreakpoint() {
        var result = ContainerQueryProcessor.processContainerQuery("@[]");
        assertTrue(result.isContainerQuery());
        assertEquals(ContainerQueryProcessor.QueryType.BREAKPOINT, result.queryType());
        assertEquals("", result.value());
        assertEquals("@container (min-width: 0px)", result.cssEquivalent());
    }

    @Test
    void testProcessContainerQuery_InvalidRemValue() {
        var result = ContainerQueryProcessor.processContainerQuery("@[invalidrem]");
        assertTrue(result.isContainerQuery());
        assertEquals(ContainerQueryProcessor.QueryType.BREAKPOINT, result.queryType());
        assertEquals("@container (min-width: 0px)", result.cssEquivalent());
    }
}
