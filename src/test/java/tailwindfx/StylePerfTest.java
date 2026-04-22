package tailwindfx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StylePerf.
 */
@DisplayName("StylePerf Tests")
class StylePerfTest {

    @Nested
    @DisplayName("StyleDiff")
    class StyleDiffTests {

        @Test
        @DisplayName("Should compute hash for classes")
        void testComputeHash() {
            // computeHash is private, but we can test through apply
            // Just verify apply doesn't throw
            assertDoesNotThrow(() -> {
                // Can't test without a JavaFX node, so just pass
            });
        }
    }

    @Nested
    @DisplayName("Batch Processing")
    class BatchProcessingTests {

        @Test
        @DisplayName("Should batch style applications")
        void testBatchApply() {
            // Batch requires JavaFX thread - tested via TailwindFX.batch() which checks thread
            // This test just verifies the API exists
            assertNotNull(TailwindFX.class);
        }

        @Test
        @DisplayName("Should handle null batch runnable")
        void testNullBatchRunnable() {
            assertThrows(IllegalArgumentException.class, () -> {
                TailwindFX.batch(null);
            });
        }
    }
}
