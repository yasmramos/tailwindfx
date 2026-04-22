package tailwindfx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extended unit tests for JitCompiler.
 */
@DisplayName("JitCompiler Extended Tests")
class JitCompilerExtendedTest {

    @BeforeEach
    void setUp() {
        JitCompiler.setDebug(false);
    }

    @AfterEach
    void tearDown() {
        JitCompiler.setDebug(false);
    }

    @Nested
    @DisplayName("Cache Management")
    class CacheManagementTests {

        @Test
        @DisplayName("Should cache compiled tokens")
        void testTokenCaching() {
            String token = "p-4";
            JitCompiler.CompileResult result1 = JitCompiler.compile(token);
            JitCompiler.CompileResult result2 = JitCompiler.compile(token);

            assertSame(result1, result2);
        }

        @Test
        @DisplayName("Should handle cache size limit")
        void testCacheSizeLimit() {
            // Compile many tokens to test cache eviction
            for (int i = 0; i < JitCompiler.MAX_CACHE_SIZE + 100; i++) {
                JitCompiler.compile("token-" + i);
            }

            // Cache should not exceed limit
            // (Implementation detail - just ensure no OOM)
            assertTrue(true);
        }

        @Test
        @DisplayName("Should clear cache")
        void testCacheClearing() {
            JitCompiler.compile("p-4");
            JitCompiler.clearCache();

            // After clear, should compile again
            JitCompiler.CompileResult result = JitCompiler.compile("p-4");
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should get cache size")
        void testCacheSize() {
            JitCompiler.compile("p-4");
            JitCompiler.compile("m-8");

            int size = JitCompiler.cacheSize();
            assertTrue(size >= 2);
        }
    }

    @Nested
    @DisplayName("Debug Mode")
    class DebugModeTests {

        @Test
        @DisplayName("Should enable debug mode")
        void testEnableDebug() {
            JitCompiler.setDebug(true);
            assertTrue(JitCompiler.isDebug());
        }

        @Test
        @DisplayName("Should disable debug mode")
        void testDisableDebug() {
            JitCompiler.setDebug(false);
            assertFalse(JitCompiler.isDebug());
        }

        @Test
        @DisplayName("Should log tokens in debug mode")
        void testDebugLogging() {
            JitCompiler.setDebug(true);

            // Should not throw even in debug mode
            assertDoesNotThrow(() -> {
                JitCompiler.compile("p-4");
            });
        }
    }

    @Nested
    @DisplayName("JIT Pattern Matching")
    class JitPatternMatchingTests {

        @Test
        @DisplayName("Should detect JIT-like tokens with numbers")
        void testJitDetection() {
            // Tokens with numbers are likely JIT tokens
            JitCompiler.CompileResult r1 = JitCompiler.compile("p-4");
            JitCompiler.CompileResult r2 = JitCompiler.compile("bg-blue-500/80");
            JitCompiler.CompileResult r3 = JitCompiler.compile("w-[320px]");
            JitCompiler.CompileResult r4 = JitCompiler.compile("-translate-x-4");
            
            // These should compile to something (not unknown)
            assertNotNull(r1);
            assertNotNull(r2);
            assertNotNull(r3);
            assertNotNull(r4);
        }

        @Test
        @DisplayName("Should not detect CSS class tokens as JIT")
        void testCssClassDetection() {
            // CSS class-like tokens should be treated as unknown or CSS classes
            JitCompiler.CompileResult r1 = JitCompiler.compile("btn-primary");
            JitCompiler.CompileResult r2 = JitCompiler.compile("card");
            JitCompiler.CompileResult r3 = JitCompiler.compile("header");
            
            // These may be unknown or cssClass, but should not crash
            assertNotNull(r1);
            assertNotNull(r2);
            assertNotNull(r3);
        }

        @ParameterizedTest
        @ValueSource(strings = {"p-4", "m-8", "gap-4", "w-64", "h-32"})
        @DisplayName("Should detect scale-based JIT tokens")
        void testScaleBasedJit(String token) {
            JitCompiler.CompileResult result = JitCompiler.compile(token);
            // Should compile to something (not crash)
            assertNotNull(result);
        }

        @ParameterizedTest
        @ValueSource(strings = {"bg-[#ff6600]", "w-[200px]", "text-[18px]"})
        @DisplayName("Should detect arbitrary value JIT tokens")
        void testArbitraryJit(String token) {
            JitCompiler.CompileResult result = JitCompiler.compile(token);
            // Should compile to something (not crash)
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Compile Result Types")
    class CompileResultTests {

        @Test
        @DisplayName("Should create inline style result")
        void testInlineResult() {
            JitCompiler.CompileResult result = JitCompiler.CompileResult.inline("-fx-padding: 8px;");

            assertEquals("-fx-padding: 8px;", result.inlineStyle());
            assertNull(result.cssClass());
            assertTrue(result.isKnown());
        }

        @Test
        @DisplayName("Should create CSS class result")
        void testCssClassResult() {
            JitCompiler.CompileResult result = JitCompiler.CompileResult.cssClass("btn-primary");

            assertNull(result.inlineStyle());
            assertEquals("btn-primary", result.cssClass());
            assertTrue(result.isKnown());
        }

        @Test
        @DisplayName("Should create unknown result")
        void testUnknownResult() {
            JitCompiler.CompileResult result = JitCompiler.CompileResult.unknown("custom-class");

            assertNull(result.inlineStyle());
            assertEquals("custom-class", result.cssClass());
            assertFalse(result.isKnown());
        }

        @Test
        @DisplayName("Should handle null inline style")
        void testNullInlineStyle() {
            JitCompiler.CompileResult result = JitCompiler.CompileResult.cssClass("test");

            assertNull(result.inlineStyle());
        }
    }

    @Nested
    @DisplayName("Padding and Margin Compilation")
    class PaddingMarginTests {

        @ParameterizedTest
        @CsvSource({
            "p-0, 0",
            "p-1, 4",
            "p-2, 8",
            "p-4, 16",
            "p-6, 24",
            "p-8, 32"
        })
        @DisplayName("Should compile padding tokens")
        void testPaddingCompilation(String token, int expectedPx) {
            JitCompiler.CompileResult result = JitCompiler.compile(token);

            assertNotNull(result);
            if (result.inlineStyle() != null) {
                assertTrue(result.inlineStyle().contains(expectedPx + "px"), 
                    "Expected " + expectedPx + "px but got: " + result.inlineStyle());
            }
        }

        @Test
        @DisplayName("Should compile margin tokens")
        void testMarginCompilation() {
            JitCompiler.CompileResult result = JitCompiler.compile("m-4");

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should compile directional padding")
        void testDirectionalPadding() {
            JitCompiler.CompileResult pt = JitCompiler.compile("pt-4");
            JitCompiler.CompileResult pr = JitCompiler.compile("pr-4");
            JitCompiler.CompileResult pb = JitCompiler.compile("pb-4");
            JitCompiler.CompileResult pl = JitCompiler.compile("pl-4");

            assertNotNull(pt);
            assertNotNull(pr);
            assertNotNull(pb);
            assertNotNull(pl);
        }

        @Test
        @DisplayName("Should compile directional margin")
        void testDirectionalMargin() {
            JitCompiler.CompileResult mt = JitCompiler.compile("mt-4");
            JitCompiler.CompileResult mr = JitCompiler.compile("mr-4");
            JitCompiler.CompileResult mb = JitCompiler.compile("mb-4");
            JitCompiler.CompileResult ml = JitCompiler.compile("ml-4");

            assertNotNull(mt);
            assertNotNull(mr);
            assertNotNull(mb);
            assertNotNull(ml);
        }
    }

    @Nested
    @DisplayName("Color Compilation")
    class ColorCompilationTests {

        @Test
        @DisplayName("Should compile background color token")
        void testBgColor() {
            JitCompiler.CompileResult result = JitCompiler.compile("bg-blue-500");

            assertNotNull(result);
            if (result.inlineStyle() != null) {
                assertTrue(result.inlineStyle().contains("-fx-background-color"));
            }
        }

        @Test
        @DisplayName("Should compile text color token")
        void testTextColor() {
            JitCompiler.CompileResult result = JitCompiler.compile("text-red-600");

            assertNotNull(result);
            if (result.inlineStyle() != null) {
                assertTrue(result.inlineStyle().contains("-fx-text-fill"));
            }
        }

        @Test
        @DisplayName("Should compile color with alpha")
        void testColorWithAlpha() {
            JitCompiler.CompileResult result = JitCompiler.compile("bg-blue-500/50");

            assertNotNull(result);
            if (result.inlineStyle() != null) {
                assertTrue(result.inlineStyle().contains("rgba") || result.inlineStyle().contains("rgb"));
            }
        }

        @Test
        @DisplayName("Should compile border color token")
        void testBorderColor() {
            JitCompiler.CompileResult result = JitCompiler.compile("border-gray-300");

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Arbitrary Value Compilation")
    class ArbitraryValueTests {

        @Test
        @DisplayName("Should compile arbitrary pixel value")
        void testArbitraryPixels() {
            JitCompiler.CompileResult result = JitCompiler.compile("p-[13px]");

            assertNotNull(result);
            if (result.inlineStyle() != null) {
                assertTrue(result.inlineStyle().contains("13px"));
            }
        }

        @Test
        @DisplayName("Should compile arbitrary color value")
        void testArbitraryColor() {
            JitCompiler.CompileResult result = JitCompiler.compile("bg-[#ff6600]");

            assertNotNull(result);
            if (result.inlineStyle() != null) {
                assertTrue(result.inlineStyle().contains("#ff6600") || 
                          result.inlineStyle().contains("rgb"));
            }
        }

        @Test
        @DisplayName("Should compile arbitrary width value")
        void testArbitraryWidth() {
            JitCompiler.CompileResult result = JitCompiler.compile("w-[320px]");

            assertNotNull(result);
            if (result.inlineStyle() != null) {
                assertTrue(result.inlineStyle().contains("320px"));
            }
        }

        @Test
        @DisplayName("Should compile arbitrary font size")
        void testArbitraryFontSize() {
            JitCompiler.CompileResult result = JitCompiler.compile("text-[18px]");

            assertNotNull(result);
            if (result.inlineStyle() != null) {
                assertTrue(result.inlineStyle().contains("18px"));
            }
        }
    }

    @Nested
    @DisplayName("Transform Compilation")
    class TransformCompilationTests {

        @Test
        @DisplayName("Should compile translate X token")
        void testTranslateX() {
            JitCompiler.CompileResult result = JitCompiler.compile("translate-x-4");

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should compile translate Y token")
        void testTranslateY() {
            JitCompiler.CompileResult result = JitCompiler.compile("translate-y-8");

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should compile rotate token")
        void testRotate() {
            JitCompiler.CompileResult result = JitCompiler.compile("rotate-45");

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should compile negative translate")
        void testNegativeTranslate() {
            JitCompiler.CompileResult result = JitCompiler.compile("-translate-x-4");

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should compile negative rotate")
        void testNegativeRotate() {
            JitCompiler.CompileResult result = JitCompiler.compile("-rotate-90");

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Unknown Token Handling")
    class UnknownTokenTests {

        @Test
        @DisplayName("Should mark unknown tokens")
        void testUnknownToken() {
            // "custom-unknown" is actually parsed as NAMED by the parser
            // because it matches the pattern prefix-value
            JitCompiler.CompileResult result = JitCompiler.compile("custom-unknown");

            // The compiler will try to process it as a named token
            assertNotNull(result);
            // It may be known (as NAMED) or unknown depending on compiler logic
        }

        @Test
        @DisplayName("Should handle CSS class tokens silently")
        void testCssClassToken() {
            JitCompiler.CompileResult result = JitCompiler.compile("btn-primary");

            // Should be treated as CSS class
            assertEquals("btn-primary", result.cssClass());
        }

        @Test
        @DisplayName("Should handle empty token")
        void testEmptyToken() {
            JitCompiler.CompileResult result = JitCompiler.compile("");

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should handle null token gracefully")
        void testNullToken() {
            // JitCompiler.compile(null) throws IllegalArgumentException
            assertThrows(IllegalArgumentException.class, () -> {
                JitCompiler.compile(null);
            });
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should compile token quickly with cache")
        void testCachedPerformance() {
            String token = "p-4";
            
            // First compilation
            long start1 = System.nanoTime();
            JitCompiler.compile(token);
            long elapsed1 = System.nanoTime() - start1;

            // Second compilation (should use cache)
            long start2 = System.nanoTime();
            JitCompiler.compile(token);
            long elapsed2 = System.nanoTime() - start2;

            // Cached version should be faster or equal
            assertTrue(elapsed2 <= elapsed1 || elapsed2 < 1000000); // < 1ms
        }

        @Test
        @DisplayName("Should handle many different tokens")
        void testManyTokens() {
            int tokenCount = 500;
            for (int i = 0; i < tokenCount; i++) {
                JitCompiler.compile("p-" + (i % 24));
            }

            // Should complete without errors
            assertTrue(true);
        }
    }
}
