package io.github.yasmramos.tailwindfx.style;

import io.github.yasmramos.tailwindfx.style.StyleToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extended unit tests for StyleToken parsing.
 */
@DisplayName("StyleToken Extended Tests")
class StyleTokenExtendedTest {

    @Nested
    @DisplayName("Arbitrary Value Parsing")
    class ArbitraryValueTests {

        @Test
        @DisplayName("Should parse arbitrary pixel value")
        void testArbitraryPixels() {
            StyleToken token = StyleToken.parse("p-[16px]");

            assertEquals(StyleToken.Kind.ARBITRARY, token.kind);
            assertEquals("p", token.prefix);
            assertEquals("16px", token.arbitraryVal);
            assertFalse(token.negative);
        }

        @Test
        @DisplayName("Should parse arbitrary color value")
        void testArbitraryColor() {
            StyleToken token = StyleToken.parse("bg-[#ff6600]");

            assertEquals(StyleToken.Kind.ARBITRARY, token.kind);
            assertEquals("bg", token.prefix);
            assertEquals("#ff6600", token.arbitraryVal);
        }

        @Test
        @DisplayName("Should parse arbitrary width value")
        void testArbitraryWidth() {
            StyleToken token = StyleToken.parse("w-[320px]");

            assertEquals(StyleToken.Kind.ARBITRARY, token.kind);
            assertEquals("w", token.prefix);
            assertEquals("320px", token.arbitraryVal);
        }

        @Test
        @DisplayName("Should parse arbitrary font size")
        void testArbitraryFontSize() {
            StyleToken token = StyleToken.parse("text-[18px]");

            assertEquals(StyleToken.Kind.ARBITRARY, token.kind);
            assertEquals("text", token.prefix);
            assertEquals("18px", token.arbitraryVal);
        }

        @Test
        @DisplayName("Should parse negative arbitrary value")
        void testNegativeArbitrary() {
            StyleToken token = StyleToken.parse("-translate-x-[20px]");

            assertEquals(StyleToken.Kind.ARBITRARY, token.kind);
            assertTrue(token.negative);
            assertEquals("translate", token.prefix);
            assertEquals("x", token.subPrefix);
        }
    }

    @Nested
    @DisplayName("Color with Alpha Parsing")
    class ColorAlphaTests {

        @Test
        @DisplayName("Should parse color with 50% alpha")
        void testColorAlpha50() {
            StyleToken token = StyleToken.parse("bg-blue-500/50");

            assertEquals(StyleToken.Kind.COLOR_SHADE, token.kind);
            assertEquals("blue", token.colorName);
            assertEquals(500, token.shade);
            assertEquals(50, token.alpha);
        }

        @Test
        @DisplayName("Should parse color with 100% alpha")
        void testColorAlpha100() {
            StyleToken token = StyleToken.parse("bg-red-600/100");

            assertEquals(100, token.alpha);
        }

        @Test
        @DisplayName("Should parse color with 0% alpha")
        void testColorAlpha0() {
            StyleToken token = StyleToken.parse("bg-green-400/0");

            assertEquals(0, token.alpha);
        }

        @Test
        @DisplayName("Should parse text color with alpha")
        void testTextColorAlpha() {
            StyleToken token = StyleToken.parse("text-gray-900/75");

            assertEquals(StyleToken.Kind.COLOR_SHADE, token.kind);
            assertEquals("gray", token.colorName);
            assertEquals(900, token.shade);
            assertEquals(75, token.alpha);
        }

        @Test
        @DisplayName("Should parse border color with alpha")
        void testBorderColorAlpha() {
            StyleToken token = StyleToken.parse("border-blue-300/25");

            assertEquals("border", token.prefix);
            assertEquals(25, token.alpha);
        }
    }

    @Nested
    @DisplayName("Negative Values")
    class NegativeValueTests {

        @Test
        @DisplayName("Should parse negative margin top")
        void testNegativeMarginTop() {
            StyleToken token = StyleToken.parse("-mt-4");

            assertTrue(token.negative);
            assertEquals("m", token.prefix);
            assertEquals("t", token.subPrefix);
            assertEquals(4, token.scale);
        }

        @Test
        @DisplayName("Should parse negative translate X")
        void testNegativeTranslateX() {
            StyleToken token = StyleToken.parse("-translate-x-8");

            assertTrue(token.negative);
            assertEquals("translate", token.prefix);
            assertEquals("x", token.subPrefix);
        }

        @Test
        @DisplayName("Should parse negative rotate")
        void testNegativeRotate() {
            StyleToken token = StyleToken.parse("-rotate-45");

            assertTrue(token.negative);
            assertEquals("rotate", token.prefix);
        }

        @Test
        @DisplayName("Should parse negative skew X")
        void testNegativeSkewX() {
            StyleToken token = StyleToken.parse("-skew-x-6");

            assertTrue(token.negative);
            assertEquals("skew", token.prefix);
            assertEquals("x", token.subPrefix);
        }
    }

    @Nested
    @DisplayName("Named Values")
    class NamedValueTests {

        @Test
        @DisplayName("Should parse text size sm")
        void testTextSizeSm() {
            StyleToken token = StyleToken.parse("text-sm");

            assertEquals(StyleToken.Kind.NAMED, token.kind);
            assertEquals("text", token.prefix);
            assertEquals("sm", token.namedValue);
        }

        @Test
        @DisplayName("Should parse rounded lg")
        void testRoundedLg() {
            StyleToken token = StyleToken.parse("rounded-lg");

            assertEquals(StyleToken.Kind.NAMED, token.kind);
            assertEquals("lg", token.namedValue);
        }

        @Test
        @DisplayName("Should parse font weight bold")
        void testFontBold() {
            StyleToken token = StyleToken.parse("font-bold");

            assertEquals(StyleToken.Kind.NAMED, token.kind);
            assertEquals("font", token.prefix);
            assertEquals("bold", token.namedValue);
        }

        @Test
        @DisplayName("Should parse shadow xl")
        void testShadowXl() {
            StyleToken token = StyleToken.parse("shadow-xl");

            assertEquals("shadow", token.prefix);
            assertEquals("xl", token.namedValue);
        }
    }

    @Nested
    @DisplayName("Scale Values")
    class ScaleValueTests {

        @ParameterizedTest
        @ValueSource(strings = {"p-0", "p-1", "p-2", "p-4", "p-8", "p-12", "p-16", "p-24"})
        @DisplayName("Should parse padding scale values")
        void testPaddingScale(String tokenStr) {
            StyleToken token = StyleToken.parse(tokenStr);

            assertEquals(StyleToken.Kind.SCALE, token.kind);
            assertEquals("p", token.prefix);
        }

        @Test
        @DisplayName("Should parse gap scale value")
        void testGapScale() {
            StyleToken token = StyleToken.parse("gap-4");

            assertEquals(StyleToken.Kind.SCALE, token.kind);
            assertEquals("gap", token.prefix);
            assertEquals(4, token.scale);
        }

        @Test
        @DisplayName("Should parse width scale value")
        void testWidthScale() {
            StyleToken token = StyleToken.parse("w-64");

            assertEquals(StyleToken.Kind.SCALE, token.kind);
            assertEquals("w", token.prefix);
            assertEquals(64, token.scale);
        }

        @Test
        @DisplayName("Should parse height scale value")
        void testHeightScale() {
            StyleToken token = StyleToken.parse("h-32");

            assertEquals(StyleToken.Kind.SCALE, token.kind);
            assertEquals("h", token.prefix);
            assertEquals(32, token.scale);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null input")
        void testNullInput() {
            StyleToken token = StyleToken.parse(null);

            assertEquals(StyleToken.Kind.UNKNOWN, token.kind);
        }

        @Test
        @DisplayName("Should handle blank input")
        void testBlankInput() {
            StyleToken token = StyleToken.parse("");

            assertEquals(StyleToken.Kind.UNKNOWN, token.kind);
        }

        @Test
        @DisplayName("Should handle whitespace input")
        void testWhitespaceInput() {
            StyleToken token = StyleToken.parse("   ");

            assertEquals(StyleToken.Kind.UNKNOWN, token.kind);
        }

        @Test
        @DisplayName("Should handle unknown token")
        void testUnknownToken() {
            // "unknown-xyz" matches the NAMED pattern (prefix-value)
            // So it gets parsed as NAMED, not UNKNOWN
            StyleToken token = StyleToken.parse("unknown-xyz");

            // Unknown tokens in the CSS sense are those that the compiler can't process
            // But the parser will classify them as NAMED if they match the pattern
            assertEquals(StyleToken.Kind.NAMED, token.kind);
            assertEquals("unknown", token.prefix);
            assertEquals("xyz", token.namedValue);
        }

        @Test
        @DisplayName("Should preserve original raw value")
        void testRawValuePreserved() {
            String original = "  bg-blue-500/80  ";
            StyleToken token = StyleToken.parse(original);

            assertEquals(original, token.raw);
        }
    }

    @Nested
    @DisplayName("Complex Color Families")
    class ComplexColorFamilyTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "bg-slate-500", "bg-gray-500", "bg-red-500", "bg-orange-500",
            "bg-amber-500", "bg-yellow-500", "bg-lime-500", "bg-green-500",
            "bg-emerald-500", "bg-teal-500", "bg-cyan-500", "bg-sky-500",
            "bg-blue-500", "bg-indigo-500", "bg-violet-500", "bg-purple-500",
            "bg-fuchsia-500", "bg-pink-500", "bg-rose-500"
        })
        @DisplayName("Should parse all color family backgrounds")
        void testAllColorFamiliesBg(String tokenStr) {
            StyleToken token = StyleToken.parse(tokenStr);

            assertEquals(StyleToken.Kind.COLOR_SHADE, token.kind);
            assertNotNull(token.colorName);
            assertEquals(500, token.shade);
        }

        @Test
        @DisplayName("Should parse shadow with color")
        void testShadowWithColor() {
            StyleToken token = StyleToken.parse("shadow-blue-500");

            assertEquals("shadow", token.prefix);
            assertEquals("blue", token.colorName);
            assertEquals(500, token.shade);
        }

        @Test
        @DisplayName("Should parse ring with color")
        void testRingWithColor() {
            StyleToken token = StyleToken.parse("ring-blue-500");

            assertEquals("ring", token.prefix);
            assertEquals("blue", token.colorName);
        }
    }

    @Nested
    @DisplayName("Sub-prefix Parsing")
    class SubPrefixTests {

        @ParameterizedTest
        @CsvSource({
            "px-4, p, x, 4",
            "py-8, p, y, 8",
            "pt-2, p, t, 2",
            "pr-6, p, r, 6",
            "pb-4, p, b, 4",
            "pl-3, p, l, 3",
            "mx-4, m, x, 4",
            "my-8, m, y, 8",
            "mt-2, m, t, 2",
            "mr-6, m, r, 6",
            "mb-4, m, b, 4",
            "ml-3, m, l, 3"
        })
        @DisplayName("Should parse sub-prefix correctly")
        void testSubPrefix(String tokenStr, String expectedPrefix, String expectedSub, int expectedScale) {
            StyleToken token = StyleToken.parse(tokenStr);

            assertEquals(expectedPrefix, token.prefix);
            assertEquals(expectedSub, token.subPrefix);
            assertEquals(expectedScale, token.scale);
        }
    }

    @Nested
    @DisplayName("Transform Values")
    class TransformValueTests {

        @Test
        @DisplayName("Should parse rotate value")
        void testRotate() {
            StyleToken token = StyleToken.parse("rotate-90");

            assertEquals("rotate", token.prefix);
        }

        @Test
        @DisplayName("Should parse scale value")
        void testScale() {
            StyleToken token = StyleToken.parse("scale-150");

            assertEquals("scale", token.prefix);
            assertEquals(150, token.scale);
        }

        @Test
        @DisplayName("Should parse skew X value")
        void testSkewX() {
            StyleToken token = StyleToken.parse("skew-x-12");

            assertEquals("skew", token.prefix);
            assertEquals("x", token.subPrefix);
            assertEquals(12, token.scale);
        }

        @Test
        @DisplayName("Should parse skew Y value")
        void testSkewY() {
            StyleToken token = StyleToken.parse("skew-y-6");

            assertEquals("skew", token.prefix);
            assertEquals("y", token.subPrefix);
            assertEquals(6, token.scale);
        }
    }
}
