package tailwindfx;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.function.Executable;

/**
 * Tests for the unified TailwindFX.apply() method with automatic JIT detection.
 */
class TailwindFXUnifiedApplyTest {

    private Label label;
    private Button button;

    @BeforeEach
    void setUp() {
        label = new Label("Test");
        button = new Button("Click");
    }

    // =========================================================================
    // Pure CSS Classes
    // =========================================================================

    @Test
    @DisplayName("Apply pure CSS classes")
    void testPureCssClasses() {
        TailwindFX.apply(label, "text-lg", "font-bold", "text-blue-600");
        
        assertTrue(label.getStyleClass().contains("text-lg"));
        assertTrue(label.getStyleClass().contains("font-bold"));
        assertTrue(label.getStyleClass().contains("text-blue-600"));
        
        // Should NOT have inline styles
        assertNull(label.getStyle());
    }

    @Test
    @DisplayName("Apply CSS classes with space-separated string")
    void testSpaceSeparatedCssClasses() {
        TailwindFX.apply(button, "btn-primary rounded-lg shadow-md");
        
        assertTrue(button.getStyleClass().contains("btn-primary"));
        assertTrue(button.getStyleClass().contains("rounded-lg"));
        assertTrue(button.getStyleClass().contains("shadow-md"));
    }

    // =========================================================================
    // Pure JIT Tokens
    // =========================================================================

    @Test
    @DisplayName("Apply JIT token with opacity")
    void testJitOpacity() {
        TailwindFX.apply(label, "bg-blue-500/80");
        
        // Should have inline style
        assertNotNull(label.getStyle());
        assertTrue(label.getStyle().contains("rgba"));
        
        // Should NOT be in styleClass
        assertFalse(label.getStyleClass().contains("bg-blue-500/80"));
    }

    @Test
    @DisplayName("Apply JIT token with arbitrary value")
    void testJitArbitraryValue() {
        TailwindFX.apply(label, "w-[320px]", "p-[13px]");
        
        assertNotNull(label.getStyle());
        assertTrue(label.getStyle().contains("-fx-pref-width") || 
                   label.getStyle().contains("width"));
        assertTrue(label.getStyle().contains("-fx-padding") || 
                   label.getStyle().contains("padding"));
    }

    @Test
    @DisplayName("Apply JIT token with negative value")
    void testJitNegativeValue() {
        TailwindFX.apply(label, "-translate-x-4", "-mt-2");
        
        assertNotNull(label.getStyle());
        assertTrue(label.getStyle().contains("-fx-translate-x") || 
                   label.getStyle().contains("translate"));
    }

    // =========================================================================
    // Mixed CSS + JIT
    // =========================================================================

    @Test
    @DisplayName("Apply mixed CSS classes and JIT tokens")
    void testMixedCssAndJit() {
        TailwindFX.apply(button, 
            "btn-primary",       // CSS class
            "rounded-lg",        // CSS class
            "bg-blue-500/80",    // JIT (opacity)
            "p-[13px]",          // JIT (arbitrary)
            "shadow-md"          // CSS class
        );
        
        // Verify CSS classes
        assertTrue(button.getStyleClass().contains("btn-primary"));
        assertTrue(button.getStyleClass().contains("rounded-lg"));
        assertTrue(button.getStyleClass().contains("shadow-md"));
        
        // Verify JIT tokens NOT in styleClass
        assertFalse(button.getStyleClass().contains("bg-blue-500/80"));
        assertFalse(button.getStyleClass().contains("p-[13px]"));
        
        // Verify inline styles applied
        assertNotNull(button.getStyle());
        assertTrue(button.getStyle().length() > 0);
    }

    @Test
    @DisplayName("Apply mixed tokens with space-separated string")
    void testMixedSpaceSeparated() {
        TailwindFX.apply(label, "card shadow-md bg-white/90 p-[16px] rounded-xl");
        
        // CSS classes
        assertTrue(label.getStyleClass().contains("card"));
        assertTrue(label.getStyleClass().contains("shadow-md"));
        assertTrue(label.getStyleClass().contains("rounded-xl"));
        
        // JIT tokens in inline style
        assertNotNull(label.getStyle());
    }

    // =========================================================================
    // Conflict Resolution
    // =========================================================================

    @Test
    @DisplayName("CSS class conflicts are resolved")
    void testCssConflictResolution() {
        TailwindFX.apply(label, "w-4");
        TailwindFX.apply(label, "w-8");
        
        // Only w-8 should remain
        assertFalse(label.getStyleClass().contains("w-4"));
        assertTrue(label.getStyleClass().contains("w-8"));
    }

    @Test
    @DisplayName("Mixed apply with conflict resolution")
    void testMixedConflictResolution() {
        TailwindFX.apply(button, "w-4", "bg-blue-500/80");
        TailwindFX.apply(button, "w-8", "bg-red-500/90");
        
        // CSS conflict resolved
        assertFalse(button.getStyleClass().contains("w-4"));
        assertTrue(button.getStyleClass().contains("w-8"));
        
        // JIT styles merged
        assertNotNull(button.getStyle());
    }

    // =========================================================================
    // Edge Cases
    // =========================================================================

    @Test
    @DisplayName("Handle null and empty tokens gracefully")
    void testNullAndEmpty() {
        assertDoesNotThrow(() -> {
            TailwindFX.apply(label, (String[]) null);
            TailwindFX.apply(label, "");
            TailwindFX.apply(label, "   ");
            TailwindFX.apply(label, "text-lg", null, "", "font-bold");
        });
    }

    @Test
    @DisplayName("Handle complex JIT patterns")
    void testComplexJitPatterns() {
        TailwindFX.apply(label,
            "bg-[#ff6600]",           // hex color
            "rotate-[45deg]",         // rotation
            "drop-shadow-[#3b82f6]",  // colored shadow
            "text-shadow-[rgba(0,0,0,0.5)]" // rgba shadow
        );
        
        assertNotNull(label.getStyle());
        assertTrue(label.getStyle().length() > 10);
    }

    @Test
    @DisplayName("Numeric utilities are handled correctly")
    void testNumericUtilities() {
        // These should be CSS classes, not JIT
        TailwindFX.apply(label, "z-10", "order-2", "col-span-3");
        
        assertTrue(label.getStyleClass().contains("z-10"));
        assertTrue(label.getStyleClass().contains("order-2"));
        assertTrue(label.getStyleClass().contains("col-span-3"));
    }

    // =========================================================================
    // Backward Compatibility
    // =========================================================================

    @Test
    @DisplayName("Deprecated jit() method still works")
    void testDeprecatedJitMethod() {
        @SuppressWarnings("deprecation")
        Runnable test = () -> TailwindFX.jit(label, "bg-blue-500/80");
        
        assertDoesNotThrow((Executable) test);
        assertNotNull(label.getStyle());
    }

    @Test
    @DisplayName("Deprecated jitApply() method still works")
    void testDeprecatedJitApplyMethod() {
        @SuppressWarnings("deprecation")
        Runnable test = () -> TailwindFX.jitApply(button, 
            "btn-primary", "bg-blue-500/80", "rounded-lg");
        
        assertDoesNotThrow((Executable) test);
        assertTrue(button.getStyleClass().contains("btn-primary"));
        assertNotNull(button.getStyle());
    }

    // =========================================================================
    // Performance
    // =========================================================================

    @Test
    @DisplayName("Apply handles large number of tokens efficiently")
    void testLargeNumberOfTokens() {
        String[] tokens = new String[100];
        for (int i = 0; i < 50; i++) {
            tokens[i] = "class-" + i;
        }
        for (int i = 50; i < 100; i++) {
            tokens[i] = "bg-blue-" + (i * 10) + "/80";
        }
        
        long start = System.nanoTime();
        TailwindFX.apply(label, tokens);
        long duration = System.nanoTime() - start;
        
        // Should complete in less than 10ms
        assertTrue(duration < 10_000_000, 
            "Apply should be fast, took: " + duration / 1_000_000 + "ms");
        
        // Verify both CSS and JIT were applied
        assertTrue(label.getStyleClass().size() >= 50);
        assertNotNull(label.getStyle());
    }
}
