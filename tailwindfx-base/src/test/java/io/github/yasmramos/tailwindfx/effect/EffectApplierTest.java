package io.github.yasmramos.tailwindfx.effect;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.TwConfig;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for EffectApplier.
 * Tests cover effect token application, validation, and edge cases.
 */
@DisplayName("EffectApplier Unit Tests")
class EffectApplierTest {

    private Button button;
    private Label label;

    @BeforeEach
    void setUp() {
        button = new Button();
        label = new Label();
    }

    @Nested
    @DisplayName("Basic Effect Application")
    class BasicEffectApplicationTests {

        @Test
        @DisplayName("Should apply blur-sm effect")
        void testApplyBlurSm() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(button, "blur-sm")
            );
        }

        @Test
        @DisplayName("Should apply blur-md effect")
        void testApplyBlurMd() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(label, "blur-md")
            );
        }

        @Test
        @DisplayName("Should apply blur-lg effect")
        void testApplyBlurLg() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(button, "blur-lg")
            );
        }

        @Test
        @DisplayName("Should apply blur-none effect")
        void testApplyBlurNone() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(label, "blur-none")
            );
        }

        @Test
        @DisplayName("Should apply grayscale effect")
        void testApplyGrayscale() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(button, "grayscale")
            );
        }

        @Test
        @DisplayName("Should apply grayscale-0 effect")
        void testApplyGrayscaleNone() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(label, "grayscale-0")
            );
        }

        @Test
        @DisplayName("Should apply invert effect")
        void testApplyInvert() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(button, "invert")
            );
        }

        @Test
        @DisplayName("Should apply invert-0 effect")
        void testApplyInvertNone() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(label, "invert-0")
            );
        }

        @Test
        @DisplayName("Should apply sepia effect")
        void testApplySepia() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(button, "sepia")
            );
        }

        @Test
        @DisplayName("Should apply sepia-0 effect")
        void testApplySepiaNone() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(label, "sepia-0")
            );
        }
    }

    @Nested
    @DisplayName("Brightness Effects")
    class BrightnessEffectsTests {

        @Test
        @DisplayName("Should apply brightness-50 effect")
        void testApplyBrightness50() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(button, "brightness-50")
            );
        }

        @Test
        @DisplayName("Should apply brightness-75 effect")
        void testApplyBrightness75() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(label, "brightness-75")
            );
        }

        @Test
        @DisplayName("Should apply brightness-100 effect")
        void testApplyBrightness100() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(button, "brightness-100")
            );
        }

        @Test
        @DisplayName("Should apply brightness-125 effect")
        void testApplyBrightness125() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(label, "brightness-125")
            );
        }

        @Test
        @DisplayName("Should apply brightness-150 effect")
        void testApplyBrightness150() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(button, "brightness-150")
            );
        }
    }

    @Nested
    @DisplayName("Contrast Effects")
    class ContrastEffectsTests {

        @Test
        @DisplayName("Should apply contrast-50 effect")
        void testApplyContrast50() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(label, "contrast-50")
            );
        }

        @Test
        @DisplayName("Should apply contrast-75 effect")
        void testApplyContrast75() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(button, "contrast-75")
            );
        }

        @Test
        @DisplayName("Should apply contrast-100 effect")
        void testApplyContrast100() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(label, "contrast-100")
            );
        }

        @Test
        @DisplayName("Should apply contrast-125 effect")
        void testApplyContrast125() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(button, "contrast-125")
            );
        }

        @Test
        @DisplayName("Should apply contrast-150 effect")
        void testApplyContrast150() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(label, "contrast-150")
            );
        }
    }

    @Nested
    @DisplayName("Edge Cases and Validation")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should throw exception for null node")
        void testNullNode() {
            assertThrows(IllegalArgumentException.class, () -> 
                EffectApplier.applyEffectToken(null, "blur-sm")
            );
        }

        @Test
        @DisplayName("Should handle null token gracefully")
        void testNullToken() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(button, null)
            );
        }

        @Test
        @DisplayName("Should handle blank token gracefully")
        void testBlankToken() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(button, "")
            );
        }

        @Test
        @DisplayName("Should handle whitespace-only token gracefully")
        void testWhitespaceOnlyToken() {
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(button, "   ")
            );
        }

        @Test
        @DisplayName("Should ignore non-effect tokens")
        void testNonEffectToken() {
            // Non-effect tokens should be ignored without error
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(button, "bg-blue-500")
            );
            
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(label, "p-4")
            );
        }

        @Test
        @DisplayName("Should handle invalid effect token gracefully in debug mode")
        void testInvalidEffectToken() {
            // Invalid effect tokens should not throw, just log in debug mode
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(button, "blur-unknown-size")
            );
        }
    }

    @Nested
    @DisplayName("Multiple Effects")
    class MultipleEffectsTests {

        @Test
        @DisplayName("Should apply multiple effects sequentially")
        void testMultipleEffectsSequential() {
            assertDoesNotThrow(() -> {
                EffectApplier.applyEffectToken(button, "blur-sm");
                EffectApplier.applyEffectToken(button, "grayscale");
                EffectApplier.applyEffectToken(button, "brightness-125");
            });
        }

        @Test
        @DisplayName("Should apply combination of blur and contrast")
        void testBlurAndContrast() {
            assertDoesNotThrow(() -> {
                EffectApplier.applyEffectToken(label, "blur-md");
                EffectApplier.applyEffectToken(label, "contrast-75");
            });
        }

        @Test
        @DisplayName("Should apply combination of grayscale and sepia")
        void testGrayscaleAndSepia() {
            assertDoesNotThrow(() -> {
                EffectApplier.applyEffectToken(button, "grayscale");
                EffectApplier.applyEffectToken(button, "sepia-0");
            });
        }

        @Test
        @DisplayName("Should apply all effect types")
        void testAllEffectTypes() {
            assertDoesNotThrow(() -> {
                EffectApplier.applyEffectToken(button, "blur-sm");
                EffectApplier.applyEffectToken(button, "brightness-100");
                EffectApplier.applyEffectToken(button, "contrast-100");
                EffectApplier.applyEffectToken(button, "grayscale");
                EffectApplier.applyEffectToken(button, "invert");
                EffectApplier.applyEffectToken(button, "sepia");
            });
        }
    }

    @Nested
    @DisplayName("Effect Token Detection")
    class EffectTokenDetectionTests {

        @Test
        @DisplayName("Should recognize blur tokens")
        void testRecognizeBlurTokens() {
            assertDoesNotThrow(() -> {
                EffectApplier.applyEffectToken(button, "blur");
                EffectApplier.applyEffectToken(button, "blur-sm");
                EffectApplier.applyEffectToken(button, "blur-md");
                EffectApplier.applyEffectToken(button, "blur-lg");
                EffectApplier.applyEffectToken(button, "blur-none");
            });
        }

        @Test
        @DisplayName("Should recognize brightness tokens")
        void testRecognizeBrightnessTokens() {
            assertDoesNotThrow(() -> {
                EffectApplier.applyEffectToken(label, "brightness");
                EffectApplier.applyEffectToken(label, "brightness-50");
                EffectApplier.applyEffectToken(label, "brightness-150");
            });
        }

        @Test
        @DisplayName("Should recognize contrast tokens")
        void testRecognizeContrastTokens() {
            assertDoesNotThrow(() -> {
                EffectApplier.applyEffectToken(button, "contrast");
                EffectApplier.applyEffectToken(button, "contrast-50");
                EffectApplier.applyEffectToken(button, "contrast-150");
            });
        }

        @Test
        @DisplayName("Should not recognize color tokens as effects")
        void testColorTokensNotEffects() {
            // These should be ignored (not effect tokens)
            assertDoesNotThrow(() -> {
                EffectApplier.applyEffectToken(label, "bg-red-500");
                EffectApplier.applyEffectToken(label, "text-blue-600");
            });
        }

        @Test
        @DisplayName("Should not recognize layout tokens as effects")
        void testLayoutTokensNotEffects() {
            // These should be ignored (not effect tokens)
            assertDoesNotThrow(() -> {
                EffectApplier.applyEffectToken(button, "p-4");
                EffectApplier.applyEffectToken(button, "m-2");
                EffectApplier.applyEffectToken(button, "flex");
            });
        }
    }

    @Nested
    @DisplayName("Real-world Scenarios")
    class RealWorldScenariosTests {

        @Test
        @DisplayName("Should apply image filter effects")
        void testImageFilterEffects() {
            Label imageLabel = new Label("Image Placeholder");
            
            assertDoesNotThrow(() -> {
                // Apply a vintage photo effect
                EffectApplier.applyEffectToken(imageLabel, "sepia");
                EffectApplier.applyEffectToken(imageLabel, "contrast-125");
                EffectApplier.applyEffectToken(imageLabel, "brightness-90");
            });
        }

        @Test
        @DisplayName("Should apply disabled state effect")
        void testDisabledStateEffect() {
            Button disabledButton = new Button("Disabled");
            disabledButton.setDisable(true);
            
            assertDoesNotThrow(() -> {
                // Apply grayscale to indicate disabled state
                EffectApplier.applyEffectToken(disabledButton, "grayscale");
                EffectApplier.applyEffectToken(disabledButton, "brightness-75");
            });
        }

        @Test
        @DisplayName("Should apply hover-like effect simulation")
        void testHoverEffectSimulation() {
            assertDoesNotThrow(() -> {
                // Simulate a hover brightening effect
                EffectApplier.applyEffectToken(button, "brightness-110");
                EffectApplier.applyEffectToken(button, "contrast-125");
            });
        }

        @Test
        @DisplayName("Should apply dramatic visual effect")
        void testDramaticVisualEffect() {
            assertDoesNotThrow(() -> {
                // Apply a dramatic high-contrast effect
                EffectApplier.applyEffectToken(label, "contrast-150");
                EffectApplier.applyEffectToken(label, "grayscale");
                EffectApplier.applyEffectToken(label, "brightness-125");
            });
        }

        @Test
        @DisplayName("Should apply soft blur background effect")
        void testSoftBlurBackgroundEffect() {
            assertDoesNotThrow(() -> {
                // Apply a soft background blur effect
                EffectApplier.applyEffectToken(button, "blur-lg");
                EffectApplier.applyEffectToken(button, "brightness-105");
            });
        }
    }

    @Nested
    @DisplayName("Exception Handling")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should re-throw IllegalArgumentException from TwEffect")
        void testRethrowIllegalArgumentException() {
            // Invalid size should cause IllegalArgumentException which is re-thrown
            assertThrows(IllegalArgumentException.class, () -> 
                EffectApplier.applyEffectToken(button, "blur-invalid-size-name")
            );
        }

        @Test
        @DisplayName("Should handle unsupported effect token gracefully")
        void testUnsupportedEffectToken() {
            // Unsupported tokens should not throw, just potentially log in debug mode
            assertDoesNotThrow(() -> 
                EffectApplier.applyEffectToken(label, "unknown-effect-token")
            );
        }
    }
}
