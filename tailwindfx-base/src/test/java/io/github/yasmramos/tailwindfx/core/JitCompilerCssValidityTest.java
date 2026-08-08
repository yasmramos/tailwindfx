package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Verifica que el JIT emita únicamente propiedades y valores que JavaFX entiende.
 *
 * <p>Cada caso corresponde a un token que antes compilaba a CSS inválido (propiedad inexistente o
 * valor con la unidad equivocada) y que JavaFX descartaba silenciosamente.
 */
@DisplayName("JIT — validez del CSS generado")
class JitCompilerCssValidityTest {

  @BeforeEach
  void setUp() {
    JitCompiler.clearCache();
  }

  @Nested
  @DisplayName("Padding")
  class PaddingTests {

    @ParameterizedTest
    @CsvSource({
      "px-4, -fx-padding: 0px 16px 0px 16px;",
      "py-2, -fx-padding: 8px 0px 8px 0px;",
      "pt-3, -fx-padding: 12px 0px 0px 0px;",
      "pl-1, -fx-padding: 0px 0px 0px 4px;"
    })
    @DisplayName("El padding direccional usa la propiedad -fx-padding")
    void directionalPaddingUsesFxPadding(String token, String expected) {
      assertEquals(expected, JitCompiler.compile(token).inlineStyle());
    }

    @Test
    @DisplayName("px + py se combinan en una sola declaración")
    void mergesHorizontalAndVerticalPadding() {
      assertEquals(
          "-fx-padding: 8px 16px 8px 16px;",
          JitCompiler.compileBatch("px-4", "py-2").inlineStyle());
    }

    @Test
    @DisplayName("Un lado específico sobrescribe el shorthand anterior")
    void sideOverridesShorthand() {
      assertEquals(
          "-fx-padding: 16px 8px 16px 8px;", JitCompiler.compileBatch("p-4", "px-2").inlineStyle());
    }
  }

  @Nested
  @DisplayName("Tipografía")
  class TypographyTests {

    @ParameterizedTest
    @CsvSource({"text-sm, 14px", "text-xl, 20px", "text-[13px], 13px"})
    @DisplayName("Los tamaños de texto compilan a -fx-font-size")
    void textSizesUseFontSize(String token, String size) {
      assertEquals("-fx-font-size: " + size + ";", JitCompiler.compile(token).inlineStyle());
    }

    @ParameterizedTest
    @ValueSource(strings = {"text-red-500", "text-white", "text-[#ff6600]"})
    @DisplayName("Los colores de texto siguen compilando a -fx-text-fill")
    void textColorsUseTextFill(String token) {
      assertTrue(JitCompiler.compile(token).inlineStyle().startsWith("-fx-text-fill:"));
    }

    @Test
    @DisplayName("text-center compila a -fx-text-alignment")
    void textAlignment() {
      assertEquals("-fx-text-alignment: center;", JitCompiler.compile("text-center").inlineStyle());
    }

    @Test
    @DisplayName("font-bold compila a -fx-font-weight, no a -fx-font-family")
    void fontWeight() {
      assertEquals("-fx-font-weight: 700;", JitCompiler.compile("font-bold").inlineStyle());
    }

    @Test
    @DisplayName("font-mono compila a una familia tipográfica de JavaFX")
    void fontFamily() {
      assertEquals("-fx-font-family: Monospaced;", JitCompiler.compile("font-mono").inlineStyle());
    }
  }

  @Nested
  @DisplayName("Valores sin unidad")
  class UnitlessTests {

    @ParameterizedTest
    @CsvSource({
      "opacity-50, -fx-opacity: 0.5;",
      "opacity-100, -fx-opacity: 1;",
      "rotate-45, -fx-rotate: 45;",
      "-rotate-90, -fx-rotate: -90;",
      "scale-95, -fx-scale-x: 0.95; -fx-scale-y: 0.95;"
    })
    @DisplayName("Las utilidades sin unidad no reciben px")
    void unitlessUtilities(String token, String expected) {
      assertEquals(expected, JitCompiler.compile(token).inlineStyle());
    }

    @Test
    @DisplayName("Las escalas negativas conservan el signo")
    void negativeSpacingKeepsSign() {
      assertEquals("-fx-translate-x: -16px;", JitCompiler.compile("-translate-x-4").inlineStyle());
    }
  }

  @Nested
  @DisplayName("Bordes y colores")
  class BorderAndColorTests {

    @Test
    @DisplayName("border-2 es un ancho de borde, no un color")
    void borderWidth() {
      assertEquals("-fx-border-width: 2px;", JitCompiler.compile("border-2").inlineStyle());
    }

    @Test
    @DisplayName("rounded-* fija el radio del fondo y del borde")
    void roundedSetsBothRadii() {
      String style = JitCompiler.compile("rounded-lg").inlineStyle();
      assertTrue(style.contains("-fx-background-radius: 8px;"));
      assertTrue(style.contains("-fx-border-radius: 8px;"));
    }

    @Test
    @DisplayName("Un color arbitrario admite modificador de opacidad")
    void arbitraryColorWithOpacity() {
      assertEquals(
          "-fx-background-color: rgba(255,0,0,0.80);",
          JitCompiler.compile("bg-[#ff0000]/80").inlineStyle());
    }

    @Test
    @DisplayName("cursor-pointer usa un valor válido de javafx.scene.Cursor")
    void cursorValue() {
      assertEquals("-fx-cursor: hand;", JitCompiler.compile("cursor-pointer").inlineStyle());
    }
  }

  @Nested
  @DisplayName("Utilidades sin equivalente en JavaFX")
  class UnsupportedTests {

    @ParameterizedTest
    @ValueSource(strings = {"z-10", "skew-x-6", "blur-sm", "grayscale-100", "overflow-hidden"})
    @DisplayName("No se emiten propiedades -fx-* inventadas")
    void noInventedProperties(String token) {
      assertFalse(JitCompiler.compile(token).hasInlineStyle());
    }
  }
}
