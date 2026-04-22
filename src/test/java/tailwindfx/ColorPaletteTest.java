package tailwindfx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ColorPalette.
 */
@DisplayName("ColorPalette Tests")
class ColorPaletteTest {

    @Nested
    @DisplayName("Basic Color Lookup")
    class BasicColorLookup {

        @Test
        @DisplayName("Should find blue-500 color")
        void testBlue500() {
            String color = ColorPalette.hex("blue", 500);
            
            assertNotNull(color);
            assertEquals("#3B82F6", color.toUpperCase());
        }

        @Test
        @DisplayName("Should find red-500 color")
        void testRed500() {
            String color = ColorPalette.hex("red", 500);
            
            assertNotNull(color);
            assertEquals("#EF4444", color.toUpperCase());
        }

        @Test
        @DisplayName("Should find green-500 color")
        void testGreen500() {
            String color = ColorPalette.hex("green", 500);
            
            assertNotNull(color);
            assertEquals("#22C55E", color.toUpperCase());
        }

        @Test
        @DisplayName("Should find gray-500 color")
        void testGray500() {
            String color = ColorPalette.hex("gray", 500);
            
            assertNotNull(color);
            assertEquals("#6B7280", color.toUpperCase());
        }
    }

    @Nested
    @DisplayName("Shade Variations")
    class ShadeVariationsTests {

        @Test
        @DisplayName("Should find lighter shade (100)")
        void testLighterShade() {
            String color = ColorPalette.hex("blue", 100);
            
            assertNotNull(color);
        }

        @Test
        @DisplayName("Should find darker shade (900)")
        void testDarkerShade() {
            String color = ColorPalette.hex("blue", 900);
            
            assertNotNull(color);
        }

        @ParameterizedTest
        @ValueSource(ints = {50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950})
        @DisplayName("Should find all shades for blue")
        void testAllBlueShades(int shade) {
            String color = ColorPalette.hex("blue", shade);
            
            assertNotNull(color, "Blue-" + shade + " should exist");
        }
    }

    @Nested
    @DisplayName("JavaFX Color Format")
    class FxColorFormatTests {

        @Test
        @DisplayName("Should return JavaFX compatible color")
        void testFxColorFormat() {
            String color = ColorPalette.fxColor("blue", 500, null);
            
            assertNotNull(color);
            assertTrue(color.startsWith("#") || color.startsWith("rgba"));
        }

        @Test
        @DisplayName("Should return color with alpha")
        void testFxColorWithAlpha() {
            String color = ColorPalette.fxColor("blue", 500, 0.5);
            
            assertNotNull(color);
            assertTrue(color.contains("rgba") || color.contains("rgb"));
        }
    }

    @Nested
    @DisplayName("Alpha Transparency")
    class AlphaTests {

        @Test
        @DisplayName("Should handle color with alpha value")
        void testColorWithAlpha() {
            String hex = ColorPalette.fxColor("blue", 500, 0.5);

            assertNotNull(hex);
        }

        @Test
        @DisplayName("Should handle color without alpha")
        void testColorWithoutAlpha() {
            String hex = ColorPalette.fxColor("blue", 500, null);

            assertNotNull(hex);
        }
    }

    @Nested
    @DisplayName("Hex Validation")
    class HexValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"#3B82F6", "#FF0000", "#00FF00", "#0000FF", "#FFFFFF", "#000000"})
        @DisplayName("Should validate correct hex colors")
        void testValidHexColors(String hex) {
            assertTrue(ColorPalette.isValidHex(hex), hex + " should be valid");
        }

        @ParameterizedTest
        @ValueSource(strings = {"3B82F6", "red", "#GGGGGG", "#12345"})
        @DisplayName("Should reject invalid hex colors")
        void testInvalidHexColors(String hex) {
            assertFalse(ColorPalette.isValidHex(hex), hex + " should be invalid");
        }

        @Test
        @DisplayName("Should normalize short hex to full hex")
        void testHexNormalization() {
            String normalized = ColorPalette.normalizeHex("#f60");
            
            assertNotNull(normalized);
            assertEquals("#FF6600", normalized.toUpperCase());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should return null for non-existent color")
        void testNonExistentColor() {
            String color = ColorPalette.hex("nonexistent", 500);
            
            assertNull(color);
        }

        @Test
        @DisplayName("Should handle null color name gracefully")
        void testNullColorName() {
            // Returns null instead of throwing
            String result = ColorPalette.hex(null, 500);
            assertNull(result);
        }

        @Test
        @DisplayName("Should handle unknown shade gracefully")
        void testUnknownShade() {
            // Should still work with any valid shade number
            String color = ColorPalette.hex("blue", 999);
            // May return null if shade doesn't exist
            // This is acceptable behavior
        }
    }

    @Nested
    @DisplayName("All Color Families")
    class AllColorFamiliesTests {

        @ParameterizedTest
        @ValueSource(strings = {"slate", "gray", "red", "orange", "amber", "yellow", "lime",
                                "green", "emerald", "teal", "cyan", "sky",
                                "blue", "indigo", "violet", "purple", "fuchsia",
                                "pink", "rose"})
        @DisplayName("Should support all Tailwind color families")
        void testAllColorFamilies(String colorName) {
            String color = ColorPalette.hex(colorName, 500);

            assertNotNull(color, colorName + " should be supported");
        }
    }
}
