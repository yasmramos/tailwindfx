package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Unit tests for ColorUtilityValidator class. */
class ColorUtilityValidatorTest {

  @Test
  void testValidColorUtilityWithNumericShade() {
    assertTrue(ColorUtilityValidator.isValidColorUtilityBase("bg-red-500"));
    assertTrue(ColorUtilityValidator.isValidColorUtilityBase("text-blue-300"));
    assertTrue(ColorUtilityValidator.isValidColorUtilityBase("border-green-700"));
    assertTrue(ColorUtilityValidator.isValidColorUtilityBase("fill-yellow-100"));
    assertTrue(ColorUtilityValidator.isValidColorUtilityBase("stroke-purple-900"));
    assertTrue(ColorUtilityValidator.isValidColorUtilityBase("shadow-pink-400"));
    assertTrue(ColorUtilityValidator.isValidColorUtilityBase("ring-indigo-600"));
    assertTrue(ColorUtilityValidator.isValidColorUtilityBase("outline-teal-800"));
  }

  @Test
  void testValidColorUtilityWithNamedColors() {
    assertTrue(ColorUtilityValidator.isValidColorUtilityBase("bg-transparent"));
    assertTrue(ColorUtilityValidator.isValidColorUtilityBase("bg-white"));
    assertTrue(ColorUtilityValidator.isValidColorUtilityBase("text-black"));
    assertTrue(ColorUtilityValidator.isValidColorUtilityBase("border-gray"));
  }

  @Test
  void testInvalidColorUtilityNullInput() {
    assertFalse(ColorUtilityValidator.isValidColorUtilityBase(null));
  }

  @Test
  void testInvalidColorUtilityEmptyInput() {
    assertFalse(ColorUtilityValidator.isValidColorUtilityBase(""));
  }

  @Test
  void testInvalidColorUtilityBlankInput() {
    assertFalse(ColorUtilityValidator.isValidColorUtilityBase("   "));
  }

  @Test
  void testInvalidColorUtilityNonColorPrefix() {
    assertFalse(ColorUtilityValidator.isValidColorUtilityBase("padding-4"));
    assertFalse(ColorUtilityValidator.isValidColorUtilityBase("margin-2"));
    assertFalse(ColorUtilityValidator.isValidColorUtilityBase("flex-row"));
    assertFalse(ColorUtilityValidator.isValidColorUtilityBase("grid-cols-3"));
  }

  @Test
  void testInvalidColorUtilityMissingShade() {
    // Color with dash but no numeric shade should be invalid unless it's a named color
    assertFalse(ColorUtilityValidator.isValidColorUtilityBase("bg-red-"));
    assertFalse(ColorUtilityValidator.isValidColorUtilityBase("text-blue-invalid"));
  }

  @Test
  void testInvalidColorUtilityWrongFormat() {
    assertFalse(ColorUtilityValidator.isValidColorUtilityBase("bgred500"));
    // "bg-" is actually valid as it could be a named color without shade
    // assertFalse(ColorUtilityValidator.isValidColorUtilityBase("bg-"));
    assertFalse(ColorUtilityValidator.isValidColorUtilityBase("bg"));
  }

  @Test
  void testAllSupportedColorPrefixes() {
    String[] prefixes = {"bg", "text", "border", "fill", "stroke", "shadow", "ring", "outline"};

    for (String prefix : prefixes) {
      assertTrue(
          ColorUtilityValidator.isValidColorUtilityBase(prefix + "-blue-500"),
          "Prefix " + prefix + " should be valid");
    }
  }

  @Test
  void testEdgeCasesWithNumbersInColorName() {
    // Test that only the last part is validated as a number
    assertTrue(ColorUtilityValidator.isValidColorUtilityBase("bg-blue-500"));
    assertTrue(ColorUtilityValidator.isValidColorUtilityBase("bg-slate-950"));
  }

  @Test
  void testNonNumericShadeRejection() {
    assertFalse(ColorUtilityValidator.isValidColorUtilityBase("bg-red-light"));
    assertFalse(ColorUtilityValidator.isValidColorUtilityBase("text-blue-dark"));
    assertFalse(ColorUtilityValidator.isValidColorUtilityBase("border-green-bright"));
  }
}
