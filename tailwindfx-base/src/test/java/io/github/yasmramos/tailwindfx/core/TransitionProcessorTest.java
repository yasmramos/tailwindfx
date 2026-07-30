package io.github.yasmramos.tailwindfx.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransitionProcessor.
 */
class TransitionProcessorTest {

    @Test
    void testIsTransitionToken() {
        // Transition properties
        assertTrue(TransitionProcessor.isTransitionToken("transition-none"));
        assertTrue(TransitionProcessor.isTransitionToken("transition-all"));
        assertTrue(TransitionProcessor.isTransitionToken("transition-colors"));
        assertTrue(TransitionProcessor.isTransitionToken("transition-opacity"));
        assertTrue(TransitionProcessor.isTransitionToken("transition-transform"));
        
        // Duration
        assertTrue(TransitionProcessor.isTransitionToken("duration-75"));
        assertTrue(TransitionProcessor.isTransitionToken("duration-300"));
        assertTrue(TransitionProcessor.isTransitionToken("duration-[500]"));
        
        // Easing
        assertTrue(TransitionProcessor.isTransitionToken("ease-linear"));
        assertTrue(TransitionProcessor.isTransitionToken("ease-in"));
        assertTrue(TransitionProcessor.isTransitionToken("ease-out"));
        assertTrue(TransitionProcessor.isTransitionToken("ease-in-out"));
        
        // Animations
        assertTrue(TransitionProcessor.isTransitionToken("animate-spin"));
        assertTrue(TransitionProcessor.isTransitionToken("animate-pulse"));
        assertTrue(TransitionProcessor.isTransitionToken("animate-bounce"));
        
        // Non-transition tokens
        assertFalse(TransitionProcessor.isTransitionToken("p-4"));
        assertFalse(TransitionProcessor.isTransitionToken("bg-blue-500"));
        assertFalse(TransitionProcessor.isTransitionToken(null));
        assertFalse(TransitionProcessor.isTransitionToken(""));
    }

    @Test
    void testProcessTransitionNone() {
        TransitionProcessor.TransitionResult result = TransitionProcessor.processTransition("transition-none");
        assertNotNull(result);
        assertEquals("transition", result.type());
        assertEquals("-fx-transition: none;", result.inlineStyle());
    }

    @Test
    void testProcessTransitionAll() {
        TransitionProcessor.TransitionResult result = TransitionProcessor.processTransition("transition-all");
        assertNotNull(result);
        assertEquals("transition", result.type());
        assertTrue(result.inlineStyle().contains("-fx-background-color"));
        assertTrue(result.inlineStyle().contains("-fx-text-fill"));
        assertTrue(result.inlineStyle().contains("-fx-opacity"));
    }

    @Test
    void testProcessTransitionColors() {
        TransitionProcessor.TransitionResult result = TransitionProcessor.processTransition("transition-colors");
        assertNotNull(result);
        assertEquals("transition", result.type());
        assertTrue(result.inlineStyle().contains("-fx-background-color"));
        assertTrue(result.inlineStyle().contains("-fx-text-fill"));
        assertTrue(result.inlineStyle().contains("-fx-border-color"));
    }

    @Test
    void testProcessTransitionOpacity() {
        TransitionProcessor.TransitionResult result = TransitionProcessor.processTransition("transition-opacity");
        assertNotNull(result);
        assertEquals("transition", result.type());
        assertEquals("-fx-transition: -fx-opacity;", result.inlineStyle());
    }

    @Test
    void testProcessTransitionTransform() {
        TransitionProcessor.TransitionResult result = TransitionProcessor.processTransition("transition-transform");
        assertNotNull(result);
        assertEquals("transition", result.type());
        assertTrue(result.inlineStyle().contains("-fx-scale-x"));
        assertTrue(result.inlineStyle().contains("-fx-rotate"));
        assertTrue(result.inlineStyle().contains("-fx-translate-x"));
    }

    @Test
    void testProcessDuration() {
        String[] durations = {"75", "100", "150", "200", "300", "500", "700", "1000"};
        int[] expectedMs = {75, 100, 150, 200, 300, 500, 700, 1000};
        
        for (int i = 0; i < durations.length; i++) {
            TransitionProcessor.TransitionResult result = 
                TransitionProcessor.processTransition("duration-" + durations[i]);
            assertNotNull(result, "Failed for duration-" + durations[i]);
            assertEquals("duration", result.type());
            assertEquals(expectedMs[i], result.durationMs());
            assertTrue(result.inlineStyle().contains(expectedMs[i] + "ms"));
        }
    }

    @Test
    void testProcessDurationArbitrary() {
        TransitionProcessor.TransitionResult result = 
            TransitionProcessor.processTransition("duration-[450]");
        assertNotNull(result);
        assertEquals("duration", result.type());
        assertEquals(450, result.durationMs());
        assertTrue(result.inlineStyle().contains("450ms"));
    }

    @Test
    void testProcessEasing() {
        String[] easings = {"linear", "in", "out", "in-out"};
        String[] expectedBezier = {
            "cubic-bezier(0, 0, 1, 1)",
            "cubic-bezier(0.4, 0, 1, 1)",
            "cubic-bezier(0, 0, 0.2, 1)",
            "cubic-bezier(0.4, 0, 0.2, 1)"
        };
        
        for (int i = 0; i < easings.length; i++) {
            TransitionProcessor.TransitionResult result = 
                TransitionProcessor.processTransition("ease-" + easings[i]);
            assertNotNull(result, "Failed for ease-" + easings[i]);
            assertEquals("ease", result.type());
            assertEquals(easings[i], result.easingOrAnimation());
            assertTrue(result.inlineStyle().contains(expectedBezier[i]));
        }
    }

    @Test
    void testProcessAnimation() {
        String[] animations = {"spin", "pulse", "bounce", "ping"};
        
        for (String animation : animations) {
            TransitionProcessor.TransitionResult result = 
                TransitionProcessor.processTransition("animate-" + animation);
            assertNotNull(result, "Failed for animate-" + animation);
            assertEquals("animate", result.type());
            assertEquals(animation, result.easingOrAnimation());
            assertTrue(result.inlineStyle().contains("animate-" + animation));
            assertTrue(result.inlineStyle().contains("TwAnimation"));
        }
    }

    @Test
    void testProcessInvalidToken() {
        TransitionProcessor.TransitionResult result = 
            TransitionProcessor.processTransition("invalid-token");
        assertNull(result);
    }

    @Test
    void testProcessNullToken() {
        TransitionProcessor.TransitionResult result = 
            TransitionProcessor.processTransition(null);
        assertNull(result);
    }

    @Test
    void testProcessEmptyToken() {
        TransitionProcessor.TransitionResult result = 
            TransitionProcessor.processTransition("");
        assertNull(result);
    }

    @Test
    void testTransitionWithImportantModifier() {
        TransitionProcessor.TransitionResult result = 
            TransitionProcessor.processTransition("transition-all!");
        assertNotNull(result);
        assertEquals("transition", result.type());
        assertTrue(result.inlineStyle().contains("-fx-transition"));
    }

    @Test
    void testTransitionWithHoverVariant() {
        TransitionProcessor.TransitionResult result = 
            TransitionProcessor.processTransition("hover:transition-colors");
        assertNotNull(result);
        assertEquals("transition", result.type());
    }

    @Test
    void testTransitionWithResponsivePrefix() {
        TransitionProcessor.TransitionResult result = 
            TransitionProcessor.processTransition("md:duration-300");
        assertNotNull(result);
        assertEquals("duration", result.type());
        assertEquals(300, result.durationMs());
    }

    @Test
    void testRecordEquality() {
        TransitionProcessor.TransitionResult result1 = 
            new TransitionProcessor.TransitionResult("-fx-opacity;", "transition", null, null);
        TransitionProcessor.TransitionResult result2 = 
            new TransitionProcessor.TransitionResult("-fx-opacity;", "transition", null, null);
        
        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    void testRecordToString() {
        TransitionProcessor.TransitionResult result = 
            new TransitionProcessor.TransitionResult("-fx-opacity;", "transition", 300, "ease-out");
        
        String str = result.toString();
        assertTrue(str.contains("TransitionResult"));
        assertTrue(str.contains("-fx-opacity;"));
        assertTrue(str.contains("transition"));
    }
}
