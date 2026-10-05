package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.style.TypeHint;
import org.junit.jupiter.api.Test;

/** Unit tests for TypeHintProcessor. */
class TypeHintProcessorTest {

  @Test
  void testHasArbitraryValue() {
    assertTrue(TypeHintProcessor.hasArbitraryValue("w-[320px]"));
    assertTrue(TypeHintProcessor.hasArbitraryValue("bg-[color:#ff0000]"));
    assertTrue(TypeHintProcessor.hasArbitraryValue("rotate-[angle:45deg]"));
    assertFalse(TypeHintProcessor.hasArbitraryValue("w-4"));
    assertFalse(TypeHintProcessor.hasArbitraryValue(null));
    assertFalse(TypeHintProcessor.hasArbitraryValue(""));
  }

  @Test
  void testExtractArbitraryValue() {
    assertEquals("320px", TypeHintProcessor.extractArbitraryValue("w-[320px]"));
    assertEquals("color:#ff0000", TypeHintProcessor.extractArbitraryValue("bg-[color:#ff0000]"));
    assertEquals("angle:45deg", TypeHintProcessor.extractArbitraryValue("rotate-[angle:45deg]"));
    assertNull(TypeHintProcessor.extractArbitraryValue("w-4"));
    assertNull(TypeHintProcessor.extractArbitraryValue(null));
  }

  @Test
  void testProcessTypeHint_WithExplicitHint() {
    TypeHintProcessor.TypeHintResult result = TypeHintProcessor.processTypeHint("w-[length:320px]");
    assertNotNull(result);
    assertEquals(TypeHint.LENGTH, result.typeHint());
    assertEquals("320px", result.value());
    assertEquals("length:320px", result.originalValue());
  }

  @Test
  void testProcessTypeHint_WithoutHint() {
    TypeHintProcessor.TypeHintResult result = TypeHintProcessor.processTypeHint("w-[320px]");
    assertNotNull(result);
    assertNull(result.typeHint());
    assertEquals("320px", result.value());
    assertEquals("320px", result.originalValue());
  }

  @Test
  void testAutoDetectType_Color() {
    assertEquals(TypeHint.COLOR, TypeHintProcessor.autoDetectType("#ff0000"));
    assertEquals(TypeHint.COLOR, TypeHintProcessor.autoDetectType("#f00"));
    assertEquals(TypeHint.COLOR, TypeHintProcessor.autoDetectType("rgb(255,0,0)"));
    assertEquals(TypeHint.COLOR, TypeHintProcessor.autoDetectType("rgba(255,0,0,0.5)"));
    assertEquals(TypeHint.COLOR, TypeHintProcessor.autoDetectType("hsl(0,100%,50%)"));
    assertEquals(TypeHint.COLOR, TypeHintProcessor.autoDetectType("transparent"));
  }

  @Test
  void testAutoDetectType_Angle() {
    assertEquals(TypeHint.ANGLE, TypeHintProcessor.autoDetectType("45deg"));
    assertEquals(TypeHint.ANGLE, TypeHintProcessor.autoDetectType("90deg"));
    assertEquals(TypeHint.ANGLE, TypeHintProcessor.autoDetectType("1.5rad"));
    assertEquals(TypeHint.ANGLE, TypeHintProcessor.autoDetectType("0.5turn"));
  }

  @Test
  void testAutoDetectType_Percentage() {
    assertEquals(TypeHint.PERCENTAGE, TypeHintProcessor.autoDetectType("50%"));
    assertEquals(TypeHint.PERCENTAGE, TypeHintProcessor.autoDetectType("100%"));
    assertEquals(TypeHint.PERCENTAGE, TypeHintProcessor.autoDetectType("0.5%"));
  }

  @Test
  void testAutoDetectType_Length() {
    assertEquals(TypeHint.LENGTH, TypeHintProcessor.autoDetectType("320px"));
    assertEquals(TypeHint.LENGTH, TypeHintProcessor.autoDetectType("16rem"));
    assertEquals(TypeHint.LENGTH, TypeHintProcessor.autoDetectType("1.5em"));
    assertEquals(TypeHint.LENGTH, TypeHintProcessor.autoDetectType("100vh"));
  }

  @Test
  void testAutoDetectType_Number() {
    assertEquals(TypeHint.NUMBER, TypeHintProcessor.autoDetectType("0.5"));
    assertEquals(TypeHint.NUMBER, TypeHintProcessor.autoDetectType("100"));
    assertEquals(TypeHint.NUMBER, TypeHintProcessor.autoDetectType("-1"));
  }

  @Test
  void testAutoDetectType_Url() {
    assertEquals(TypeHint.URL, TypeHintProcessor.autoDetectType("url(image.png)"));
    assertEquals(
        TypeHint.URL, TypeHintProcessor.autoDetectType("url(https://example.com/image.png)"));
  }

  @Test
  void testAutoDetectType_Image() {
    assertEquals(
        TypeHint.IMAGE, TypeHintProcessor.autoDetectType("linear-gradient(to right, red, blue)"));
    assertEquals(
        TypeHint.IMAGE, TypeHintProcessor.autoDetectType("radial-gradient(circle, red, blue)"));
  }

  @Test
  void testToJavaFxValue_Length() {
    assertEquals("320px", TypeHintProcessor.toJavaFxValue(TypeHint.LENGTH, "320px"));
    // Note: rem is kept as-is or converted to em depending on implementation
    String remResult = TypeHintProcessor.toJavaFxValue(TypeHint.LENGTH, "16rem");
    assertTrue(
        remResult.equals("16rem") || remResult.equals("16em"),
        "Expected rem to be kept or converted to em");
    assertEquals("1.5em", TypeHintProcessor.toJavaFxValue(TypeHint.LENGTH, "1.5em"));
  }

  @Test
  void testToJavaFxValue_Color() {
    assertEquals("#ff0000", TypeHintProcessor.toJavaFxValue(TypeHint.COLOR, "#ff0000"));
    assertEquals("rgb(255,0,0)", TypeHintProcessor.toJavaFxValue(TypeHint.COLOR, "rgb(255,0,0)"));
    assertEquals("transparent", TypeHintProcessor.toJavaFxValue(TypeHint.COLOR, "transparent"));
  }

  @Test
  void testToJavaFxValue_Angle() {
    assertEquals("45deg", TypeHintProcessor.toJavaFxValue(TypeHint.ANGLE, "45deg"));
    assertEquals("85.94366926962348deg", TypeHintProcessor.toJavaFxValue(TypeHint.ANGLE, "1.5rad"));
    assertEquals("180.0deg", TypeHintProcessor.toJavaFxValue(TypeHint.ANGLE, "0.5turn"));
  }

  @Test
  void testToJavaFxValue_Percentage() {
    assertEquals("50%", TypeHintProcessor.toJavaFxValue(TypeHint.PERCENTAGE, "50%"));
  }

  @Test
  void testToJavaFxValue_Number() {
    assertEquals("0.5", TypeHintProcessor.toJavaFxValue(TypeHint.NUMBER, "0.5"));
  }

  @Test
  void testToJavaFxValue_FamilyName() {
    assertEquals(
        "Custom Font", TypeHintProcessor.toJavaFxValue(TypeHint.FAMILY_NAME, "'Custom Font'"));
    assertEquals(
        "Custom Font", TypeHintProcessor.toJavaFxValue(TypeHint.FAMILY_NAME, "\"Custom Font\""));
  }

  @Test
  void testTypeHintResult_GetEffectiveType_WithHint() {
    TypeHintProcessor.TypeHintResult result = TypeHintProcessor.processTypeHint("w-[length:320px]");
    assertEquals(TypeHint.LENGTH, result.getEffectiveType());
  }

  @Test
  void testTypeHintResult_GetEffectiveType_WithoutHint() {
    TypeHintProcessor.TypeHintResult result = TypeHintProcessor.processTypeHint("w-[320px]");
    assertEquals(TypeHint.LENGTH, result.getEffectiveType());
  }

  @Test
  void testTypeHintResult_ToJavaFxValue() {
    TypeHintProcessor.TypeHintResult result = TypeHintProcessor.processTypeHint("w-[length:320px]");
    assertEquals("320px", result.toJavaFxValue());

    result = TypeHintProcessor.processTypeHint("rotate-[angle:45deg]");
    assertEquals("45deg", result.toJavaFxValue());
  }

  @Test
  void testEdgeCases() {
    // Empty brackets
    // Empty brackets still have the bracket structure
    assertTrue(
        TypeHintProcessor.hasArbitraryValue("w-[]"),
        "Empty brackets should still be detected as arbitrary value");

    // Unclosed bracket
    assertFalse(TypeHintProcessor.hasArbitraryValue("w-[320px"));

    // Null safety
    assertNull(TypeHintProcessor.processTypeHint(null));
    assertNull(TypeHintProcessor.extractArbitraryValue(null));
  }

  @Test
  void testComplexArbitraryValues() {
    // Font family with spaces
    TypeHintProcessor.TypeHintResult result =
        TypeHintProcessor.processTypeHint("font-['Custom Font']");
    assertNotNull(result);
    assertEquals("'Custom Font'", result.value());

    // Multiple colons in value (should only split on first)
    result = TypeHintProcessor.processTypeHint("bg-[color:rgb(255,0,0)]");
    assertNotNull(result);
    assertEquals(TypeHint.COLOR, result.typeHint());
    assertEquals("rgb(255,0,0)", result.value());
  }
}
