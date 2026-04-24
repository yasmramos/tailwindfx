package tailwindfx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CSS utility classes and stylesheets.
 */
@DisplayName("CSS Utilities Tests")
class CssUtilitiesTest {

    @Nested
    @DisplayName("CSS File Validation")
    class CssFileValidationTests {

        @Test
        @DisplayName("Should have valid tailwindfx.css")
        void testMainCssExists() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx.css");
            assertNotNull(resource, "tailwindfx.css should exist");
        }

        @Test
        @DisplayName("Should have valid tailwindfx-base.css")
        void testBaseCssExists() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-base.css");
            assertNotNull(resource, "tailwindfx-base.css should exist");
        }

        @Test
        @DisplayName("Should have valid tailwindfx-utilities.css")
        void testUtilitiesCssExists() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-utilities.css");
            assertNotNull(resource, "tailwindfx-utilities.css should exist");
        }

        @Test
        @DisplayName("Should have valid tailwindfx-colors.css")
        void testColorsCssExists() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
            assertNotNull(resource, "tailwindfx-colors.css should exist");
        }

        @Test
        @DisplayName("Should have valid tailwindfx-effects.css")
        void testEffectsCssExists() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-effects.css");
            assertNotNull(resource, "tailwindfx-effects.css should exist");
        }

        @Test
        @DisplayName("Should have valid tailwindfx-components.css")
        void testComponentsCssExists() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-components.css");
            assertNotNull(resource, "tailwindfx-components.css should exist");
        }

        @Test
        @DisplayName("Should have valid tailwindfx-components-preset.css")
        void testComponentsPresetCssExists() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-components-preset.css");
            assertNotNull(resource, "tailwindfx-components-preset.css should exist");
        }
    }

    @Nested
    @DisplayName("CSS Variable Definitions")
    class CssVariableTests {

        @Test
        @DisplayName("Should define color variables in base CSS")
        void testColorVariablesDefined() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-base.css");
            assertNotNull(resource);

            // Read and check for variable definitions
            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains("-color-blue-500"));
                assertTrue(content.contains("-color-red-500"));
                assertTrue(content.contains("-color-green-500"));
                assertTrue(content.contains("-color-gray-500"));
            } catch (Exception e) {
                fail("Should read base CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define font size variables")
        void testFontSizeVariablesDefined() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-base.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains("-font-size-xs"));
                assertTrue(content.contains("-font-size-sm"));
                assertTrue(content.contains("-font-size-base"));
                assertTrue(content.contains("-font-size-lg"));
                assertTrue(content.contains("-font-size-xl"));
            } catch (Exception e) {
                fail("Should read base CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define font weight variables")
        void testFontWeightVariablesDefined() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-base.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains("-font-weight-thin"));
                assertTrue(content.contains("-font-weight-normal"));
                assertTrue(content.contains("-font-weight-bold"));
            } catch (Exception e) {
                fail("Should read base CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define spacing variables")
        void testSpacingVariablesDefined() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-base.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains("-sp-0"));
                assertTrue(content.contains("-sp-1"));
                assertTrue(content.contains("-sp-2"));
                assertTrue(content.contains("-sp-4"));
            } catch (Exception e) {
                fail("Should read base CSS file", e);
            }
        }
    }

    @Nested
    @DisplayName("Utility Class Names")
    class UtilityClassTests {

        @Test
        @DisplayName("Should define padding utility classes")
        void testPaddingClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-utilities.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".p-0"));
                assertTrue(content.contains(".p-4"));
                assertTrue(content.contains(".p-8"));
                assertTrue(content.contains(".px-4"));
                assertTrue(content.contains(".py-4"));
            } catch (Exception e) {
                fail("Should read utilities CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define margin utility classes")
        void testMarginClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-utilities.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                // m-* classes map to padding in JavaFX CSS
                assertTrue(content.contains(".m-0"));
                assertTrue(content.contains(".m-4"));
                // Note: mx-* and my-* are not in CSS, they're handled by Java API
            } catch (Exception e) {
                fail("Should read utilities CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define gap utility classes")
        void testGapClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-utilities.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".gap-0"));
                assertTrue(content.contains(".gap-4"));
                assertTrue(content.contains(".gap-x-4"));
                assertTrue(content.contains(".gap-y-4"));
            } catch (Exception e) {
                fail("Should read utilities CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define width utility classes")
        void testWidthClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-utilities.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".w-full"));
                assertTrue(content.contains(".w-auto"));
                assertTrue(content.contains(".w-0"));
                assertTrue(content.contains(".w-64"));
            } catch (Exception e) {
                fail("Should read utilities CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define height utility classes")
        void testHeightClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-utilities.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".h-full"));
                assertTrue(content.contains(".h-auto"));
                assertTrue(content.contains(".h-0"));
                assertTrue(content.contains(".max-h-screen"));
            } catch (Exception e) {
                fail("Should read utilities CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define visibility utility classes")
        void testVisibilityClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-utilities.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".visible"));
                assertTrue(content.contains(".hidden"));
                assertTrue(content.contains(".invisible"));
            } catch (Exception e) {
                fail("Should read utilities CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define opacity utility classes")
        void testOpacityClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-utilities.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".opacity-0"));
                assertTrue(content.contains(".opacity-50"));
                assertTrue(content.contains(".opacity-100"));
            } catch (Exception e) {
                fail("Should read utilities CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define z-index utility classes")
        void testZIndexClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-utilities.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".z-0"));
                assertTrue(content.contains(".z-10"));
                assertTrue(content.contains(".z-50"));
            } catch (Exception e) {
                fail("Should read utilities CSS file", e);
            }
        }
    }

    @Nested
    @DisplayName("Color Class Names")
    class ColorClassTests {

        @Test
        @DisplayName("Should define background color classes")
        void testBgColorClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".bg-blue-500"));
                assertTrue(content.contains(".bg-red-500"));
                assertTrue(content.contains(".bg-green-500"));
                assertTrue(content.contains(".bg-gray-500"));
                assertTrue(content.contains(".bg-white"));
            } catch (Exception e) {
                fail("Should read colors CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define text color classes")
        void testTextColorClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".text-blue-500"));
                assertTrue(content.contains(".text-red-500"));
                assertTrue(content.contains(".text-green-500"));
                assertTrue(content.contains(".text-white"));
            } catch (Exception e) {
                fail("Should read colors CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define border color classes")
        void testBorderColorClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".border-gray-200"));
                assertTrue(content.contains(".border-blue-500"));
                assertTrue(content.contains(".border-transparent"));
            } catch (Exception e) {
                fail("Should read colors CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define font size classes")
        void testFontSizeClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".text-xs"));
                assertTrue(content.contains(".text-sm"));
                assertTrue(content.contains(".text-base"));
                assertTrue(content.contains(".text-lg"));
                assertTrue(content.contains(".text-xl"));
            } catch (Exception e) {
                fail("Should read colors CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define font weight classes")
        void testFontWeightClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".font-thin"));
                assertTrue(content.contains(".font-normal"));
                assertTrue(content.contains(".font-medium"));
                assertTrue(content.contains(".font-bold"));
                assertTrue(content.contains(".font-black"));
            } catch (Exception e) {
                fail("Should read colors CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define border radius classes")
        void testBorderRadiusClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".rounded-none"));
                assertTrue(content.contains(".rounded-sm"));
                assertTrue(content.contains(".rounded"));
                assertTrue(content.contains(".rounded-md"));
                assertTrue(content.contains(".rounded-lg"));
                assertTrue(content.contains(".rounded-full"));
            } catch (Exception e) {
                fail("Should read colors CSS file", e);
            }
        }
    }

    @Nested
    @DisplayName("CSS Syntax Validation")
    class CssSyntaxValidationTests {

        @Test
        @DisplayName("Should have no 100vh values (not supported in JavaFX)")
        void testNoViewportUnits() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-utilities.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertFalse(content.contains("100vh"), 
                    "CSS should not use viewport units (vh) - JavaFX doesn't support them");
            } catch (Exception e) {
                fail("Should read utilities CSS file", e);
            }
        }

        @Test
        @DisplayName("Should use actual values not CSS variables for font-size")
        void testFontSizeUsesActualValues() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                // Font size classes should use actual em values, not CSS variables
                assertFalse(content.contains("-fx-font-size: -font-size-"),
                    "Font size should use actual values, not CSS variables");
            } catch (Exception e) {
                fail("Should read colors CSS file", e);
            }
        }

        @Test
        @DisplayName("Should use actual values not CSS variables for font-weight")
        void testFontWeightUsesActualValues() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                // Font weight classes should use actual numeric values, not CSS variables
                assertFalse(content.contains("-fx-font-weight: -font-weight-"),
                    "Font weight should use actual values, not CSS variables");
            } catch (Exception e) {
                fail("Should read colors CSS file", e);
            }
        }

        @Test
        @DisplayName("Should have valid CSS syntax (basic check)")
        void testValidCssSyntax() {
            String[] cssFiles = {
                "/tailwindfx/tailwindfx.css",
                "/tailwindfx/tailwindfx-base.css",
                "/tailwindfx/tailwindfx-utilities.css",
                "/tailwindfx/tailwindfx-colors.css",
                "/tailwindfx/tailwindfx-effects.css"
            };

            for (String cssFile : cssFiles) {
                var resource = TailwindFX.class.getResource(cssFile);
                assertNotNull(resource, cssFile + " should exist");

                try {
                    String content = new String(resource.openStream().readAllBytes());
                    // Basic syntax checks
                    assertTrue(content.startsWith("/*") || content.startsWith("."), 
                        cssFile + " should start with comment or selector");
                    // Should have balanced braces
                    long openBraces = content.chars().filter(ch -> ch == '{').count();
                    long closeBraces = content.chars().filter(ch -> ch == '}').count();
                    assertEquals(openBraces, closeBraces, 
                        cssFile + " should have balanced braces");
                } catch (Exception e) {
                    fail("Should read CSS file: " + cssFile, e);
                }
            }
        }
    }

    @Nested
    @DisplayName("Typography Classes")
    class TypographyClassTests {

        @Test
        @DisplayName("Should define text alignment classes")
        void testTextAlignmentClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".text-left"));
                assertTrue(content.contains(".text-center"));
                assertTrue(content.contains(".text-right"));
            } catch (Exception e) {
                fail("Should read colors CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define text decoration classes")
        void testTextDecorationClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".underline"));
                assertTrue(content.contains(".no-underline"));
                assertTrue(content.contains(".line-through"));
            } catch (Exception e) {
                fail("Should read colors CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define text transform classes")
        void testTextTransformClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".uppercase"));
                assertTrue(content.contains(".lowercase"));
                assertTrue(content.contains(".capitalize"));
            } catch (Exception e) {
                fail("Should read colors CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define font family classes")
        void testFontFamilyClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-colors.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".font-sans"));
                assertTrue(content.contains(".font-serif"));
                assertTrue(content.contains(".font-mono"));
            } catch (Exception e) {
                fail("Should read colors CSS file", e);
            }
        }
    }

    @Nested
    @DisplayName("Effect Classes")
    class EffectClassTests {

        @Test
        @DisplayName("Should define shadow utility classes")
        void testShadowClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-effects.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".shadow-xs"));
                assertTrue(content.contains(".shadow-sm"));
                assertTrue(content.contains(".shadow"));
                assertTrue(content.contains(".shadow-md"));
                assertTrue(content.contains(".shadow-lg"));
                assertTrue(content.contains(".shadow-xl"));
            } catch (Exception e) {
                fail("Should read effects CSS file", e);
            }
        }

        @Test
        @DisplayName("Should define transform utility classes")
        void testTransformClasses() {
            var resource = TailwindFX.class.getResource("/tailwindfx/tailwindfx-effects.css");
            assertNotNull(resource);

            try {
                String content = new String(resource.openStream().readAllBytes());
                assertTrue(content.contains(".scale-"));
                assertTrue(content.contains(".rotate-"));
                assertTrue(content.contains(".translate-"));
            } catch (Exception e) {
                fail("Should read effects CSS file", e);
            }
        }
    }
}
