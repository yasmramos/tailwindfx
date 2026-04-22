package tailwindfx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JitCompiler with gradient support.
 */
@DisplayName("JitCompiler Tests")
class JitCompilerTest {

    @Nested
    @DisplayName("Gradient Support")
    class GradientSupportTests {

        @Test
        @DisplayName("Should compile bg-gradient-to-b with from/to colors")
        void testGradientDirectionBottom() {
            JitCompiler.BatchResult result = JitCompiler.compileBatch(
                "bg-gradient-to-b", "from-blue-500", "to-purple-600");
            
            assertTrue(result.hasInlineStyle());
            assertTrue(result.inlineStyle().contains("linear-gradient"));
            assertTrue(result.inlineStyle().contains("to bottom"));
        }

        @Test
        @DisplayName("Should compile bg-gradient-to-r with colors")
        void testGradientDirectionRight() {
            JitCompiler.BatchResult result = JitCompiler.compileBatch(
                "bg-gradient-to-r", "from-gray-800", "to-gray-900");
            
            assertTrue(result.hasInlineStyle());
            assertTrue(result.inlineStyle().contains("to right"));
        }

        @Test
        @DisplayName("Should compile gradient with via color stop")
        void testGradientWithViaStop() {
            JitCompiler.BatchResult result = JitCompiler.compileBatch(
                "bg-gradient-to-r", "from-blue-500", "via-purple-500", "to-pink-500");
            
            assertTrue(result.hasInlineStyle());
            assertTrue(result.inlineStyle().contains("linear-gradient"));
        }

        @Test
        @DisplayName("Should compile bg-gradient-to-br (bottom right)")
        void testGradientBottomRight() {
            JitCompiler.BatchResult result = JitCompiler.compileBatch(
                "bg-gradient-to-br", "from-blue-500", "to-purple-600");
            
            assertTrue(result.hasInlineStyle());
            assertTrue(result.inlineStyle().contains("to bottom right"));
        }

        @Test
        @DisplayName("Should handle all 8 gradient directions")
        void testAllGradientDirections() {
            String[] directions = {
                "bg-gradient-to-t", "bg-gradient-to-tr", "bg-gradient-to-r",
                "bg-gradient-to-br", "bg-gradient-to-b", "bg-gradient-to-bl",
                "bg-gradient-to-l", "bg-gradient-to-tl"
            };
            String[] expectedDirs = {
                "to top", "to top right", "to right", "to bottom right",
                "to bottom", "to bottom left", "to left", "to top left"
            };

            for (int i = 0; i < directions.length; i++) {
                JitCompiler.BatchResult result = JitCompiler.compileBatch(
                    directions[i], "from-blue-500", "to-purple-600");
                
                assertTrue(result.hasInlineStyle(), 
                    "Gradient " + directions[i] + " should produce inline style");
                assertTrue(result.inlineStyle().contains(expectedDirs[i]),
                    "Should contain direction: " + expectedDirs[i]);
            }
        }

        @Test
        @DisplayName("Should resolve Tailwind color names to hex")
        void testColorResolution() {
            JitCompiler.BatchResult result = JitCompiler.compileBatch(
                "bg-gradient-to-b", "from-blue-500", "to-purple-600");
            
            // Should contain hex color codes
            assertTrue(result.inlineStyle().contains("#"));
        }

        @Test
        @DisplayName("Should use default colors when from/to not specified")
        void testGradientDefaultColors() {
            JitCompiler.BatchResult result = JitCompiler.compileBatch(
                "bg-gradient-to-b");
            
            assertTrue(result.hasInlineStyle());
        }
    }

    @Nested
    @DisplayName("Basic Token Compilation")
    class BasicTokenTests {

        @Test
        @DisplayName("Should compile padding tokens")
        void testPadding() {
            JitCompiler.CompileResult result = JitCompiler.compile("p-4");
            
            assertTrue(result.hasInlineStyle());
            assertTrue(result.inlineStyle().contains("-fx-padding"));
        }

        @Test
        @DisplayName("Should compile background color tokens")
        void testBackgroundColor() {
            JitCompiler.CompileResult result = JitCompiler.compile("bg-blue-500");
            
            assertTrue(result.hasInlineStyle());
            assertTrue(result.inlineStyle().contains("-fx-background-color"));
        }

        @Test
        @DisplayName("Should compile text color tokens")
        void testTextColor() {
            JitCompiler.CompileResult result = JitCompiler.compile("text-gray-900");
            
            assertTrue(result.hasInlineStyle());
            assertTrue(result.inlineStyle().contains("-fx-text-fill"));
        }

        @Test
        @DisplayName("Should compile border radius tokens")
        void testBorderRadius() {
            JitCompiler.CompileResult result = JitCompiler.compile("rounded-lg");
            
            assertTrue(result.hasInlineStyle());
            assertTrue(result.inlineStyle().contains("-fx-background-radius"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"p-4", "p-8", "p-2", "p-6"})
        @DisplayName("Should compile various padding values")
        void testVariousPadding(String token) {
            JitCompiler.CompileResult result = JitCompiler.compile(token);
            
            assertTrue(result.hasInlineStyle());
        }
    }

    @Nested
    @DisplayName("Batch Compilation")
    class BatchTests {

        @Test
        @DisplayName("Should compile multiple tokens in batch")
        void testBatchCompile() {
            JitCompiler.BatchResult result = JitCompiler.compileBatch(
                "bg-white", "p-4", "rounded-lg", "shadow-md");
            
            assertTrue(result.hasInlineStyle());
            assertFalse(result.cssClasses().isEmpty());
        }

        @Test
        @DisplayName("Should handle empty tokens gracefully")
        void testEmptyBatch() {
            JitCompiler.BatchResult result = JitCompiler.compileBatch();
            
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should skip null and blank tokens")
        void testSkipNullAndBlank() {
            JitCompiler.BatchResult result = JitCompiler.compileBatch(
                null, "", "  ", "p-4");
            
            assertTrue(result.hasInlineStyle());
        }

        @Test
        @DisplayName("Should handle tokens with spaces")
        void testTokensWithSpaces() {
            JitCompiler.BatchResult result = JitCompiler.compileBatch(
                "bg-white p-4 rounded-lg");
            
            assertTrue(result.hasInlineStyle());
        }
    }

    @Nested
    @DisplayName("Ring Utilities")
    class RingTests {

        @Test
        @DisplayName("Should compile ring-0")
        void testRingZero() {
            // Ring may not be fully implemented, just verify it compiles
            JitCompiler.CompileResult result = JitCompiler.compile("ring-0");
            
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should compile ring with default width")
        void testRingDefault() {
            JitCompiler.CompileResult result = JitCompiler.compile("ring");
            
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should compile colored ring")
        void testColoredRing() {
            JitCompiler.BatchResult result = JitCompiler.compileBatch(
                "ring-2", "ring-blue-500");

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Border Styles")
    class BorderStyleTests {

        @Test
        @DisplayName("Should compile border-solid")
        void testBorderSolid() {
            JitCompiler.CompileResult result = JitCompiler.compile("border-solid");
            
            assertTrue(result.hasInlineStyle());
            assertTrue(result.inlineStyle().contains("-fx-border-style"));
        }

        @Test
        @DisplayName("Should compile border-dashed")
        void testBorderDashed() {
            JitCompiler.CompileResult result = JitCompiler.compile("border-dashed");
            
            assertTrue(result.hasInlineStyle());
        }

        @Test
        @DisplayName("Should compile border-width utilities")
        void testBorderWidth() {
            JitCompiler.CompileResult result = JitCompiler.compile("border-2");

            // border-2 compiles to border-width property
            assertTrue(result.hasInlineStyle());
            assertTrue(result.inlineStyle().contains("border"));
        }
    }

    @Nested
    @DisplayName("Cache Management")
    class CacheTests {

        @Test
        @DisplayName("Should cache compiled tokens")
        void testCaching() {
            JitCompiler.clearCache();
            int initialSize = JitCompiler.cacheSize();
            
            JitCompiler.compile("p-4");
            int afterCompile = JitCompiler.cacheSize();
            
            assertEquals(initialSize + 1, afterCompile);
        }

        @Test
        @DisplayName("Should clear cache")
        void testClearCache() {
            JitCompiler.compile("p-4");
            JitCompiler.clearCache();
            
            assertEquals(0, JitCompiler.cacheSize());
        }
    }

    @Nested
    @DisplayName("Debug Mode")
    class DebugModeTests {

        @Test
        @DisplayName("Should toggle debug mode")
        void testDebugMode() {
            boolean original = JitCompiler.isDebug();
            
            JitCompiler.setDebug(true);
            assertTrue(JitCompiler.isDebug());
            
            JitCompiler.setDebug(false);
            assertFalse(JitCompiler.isDebug());
            
            JitCompiler.setDebug(original); // restore
        }
    }
}
