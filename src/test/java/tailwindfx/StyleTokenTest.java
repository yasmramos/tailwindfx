package tailwindfx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StyleToken parser.
 */
@DisplayName("StyleToken Tests")
class StyleTokenTest {

    @Nested
    @DisplayName("Token Parsing")
    class TokenParsingTests {

        @Test
        @DisplayName("Should parse simple scale token")
        void testSimpleScaleToken() {
            StyleToken token = StyleToken.parse("p-4");
            
            assertEquals("p", token.prefix);
            assertEquals(4, token.scale);
            assertNull(token.subPrefix);
        }

        @Test
        @DisplayName("Should parse color shade token")
        void testColorShadeToken() {
            StyleToken token = StyleToken.parse("bg-blue-500");
            
            assertEquals("bg", token.prefix);
            assertEquals("blue", token.colorName);
            assertEquals(500, token.shade);
        }

        @Test
        @DisplayName("Should parse arbitrary value token")
        void testArbitraryValueToken() {
            StyleToken token = StyleToken.parse("bg-[#ff6600]");
            
            assertEquals(StyleToken.Kind.ARBITRARY, token.kind);
            assertEquals("#ff6600", token.arbitraryVal);
        }

        @Test
        @DisplayName("Should parse named token")
        void testNamedToken() {
            StyleToken token = StyleToken.parse("text-sm");
            
            assertEquals(StyleToken.Kind.NAMED, token.kind);
            assertEquals("sm", token.namedValue);
        }

        @Test
        @DisplayName("Should parse negative token")
        void testNegativeToken() {
            StyleToken token = StyleToken.parse("-translate-x-4");
            
            assertTrue(token.negative);
            assertEquals(4, token.scale);
        }
    }

    @Nested
    @DisplayName("Token Kind Detection")
    class TokenKindDetectionTests {

        @ParameterizedTest
        @ValueSource(strings = {"p-4", "m-8", "w-12", "gap-4", "rounded-2", "opacity-75"})
        @DisplayName("Should detect SCALE kind")
        void testScaleKind(String token) {
            StyleToken t = StyleToken.parse(token);
            assertEquals(StyleToken.Kind.SCALE, t.kind);
        }

        @ParameterizedTest
        @ValueSource(strings = {"bg-blue-500", "text-red-600", "border-green-300", "shadow-blue-400"})
        @DisplayName("Should detect COLOR_SHADE kind")
        void testColorShadeKind(String token) {
            StyleToken t = StyleToken.parse(token);
            assertEquals(StyleToken.Kind.COLOR_SHADE, t.kind);
        }

        @ParameterizedTest
        @ValueSource(strings = {"p-[13px]", "bg-[#ff6600]", "w-[320px]", "rounded-[8px]"})
        @DisplayName("Should detect ARBITRARY kind")
        void testArbitraryKind(String token) {
            StyleToken t = StyleToken.parse(token);
            assertEquals(StyleToken.Kind.ARBITRARY, t.kind);
        }

        @ParameterizedTest
        @ValueSource(strings = {"text-sm", "rounded-lg", "font-bold", "shadow-md"})
        @DisplayName("Should detect NAMED kind")
        void testNamedKind(String token) {
            StyleToken t = StyleToken.parse(token);
            assertEquals(StyleToken.Kind.NAMED, t.kind);
        }
    }

    @Nested
    @DisplayName("Alpha Transparency")
    class AlphaTests {

        @Test
        @DisplayName("Should parse token with alpha")
        void testTokenWithAlpha() {
            StyleToken token = StyleToken.parse("bg-blue-500/50");
            
            assertTrue(token.hasAlpha());
            assertEquals(50, token.alpha);
            assertEquals(0.5, token.alphaFraction());
        }

        @Test
        @DisplayName("Should handle token without alpha")
        void testTokenWithoutAlpha() {
            StyleToken token = StyleToken.parse("bg-blue-500");
            
            assertFalse(token.hasAlpha());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null input gracefully")
        void testNullInput() {
            // StyleToken.parse returns a token with empty prefix for null
            StyleToken token = StyleToken.parse(null);
            assertNotNull(token);
        }

        @Test
        @DisplayName("Should handle blank input gracefully")
        void testBlankInput() {
            // StyleToken.parse returns a token with empty prefix for blank
            StyleToken token = StyleToken.parse("");
            assertNotNull(token);
        }

        @Test
        @DisplayName("Should parse complex arbitrary token")
        void testComplexArbitrary() {
            StyleToken token = StyleToken.parse("bg-gradient-[to_right,blue-500,purple-600]");

            assertEquals(StyleToken.Kind.ARBITRARY, token.kind);
            assertNotNull(token.arbitraryVal);
        }
    }
}
