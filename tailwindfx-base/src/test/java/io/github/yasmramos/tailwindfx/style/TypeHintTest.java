package io.github.yasmramos.tailwindfx.style;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TypeHint enum.
 */
class TypeHintTest {

    @Test
    void testParseLengthHint() {
        TypeHint hint = TypeHint.parse("length:320px");
        assertNotNull(hint);
        assertEquals(TypeHint.LENGTH, hint);
        assertEquals("length", hint.getHint());
    }

    @Test
    void testParsePercentageHint() {
        TypeHint hint = TypeHint.parse("percentage:50%");
        assertNotNull(hint);
        assertEquals(TypeHint.PERCENTAGE, hint);
    }

    @Test
    void testParseNumberHint() {
        TypeHint hint = TypeHint.parse("number:0.5");
        assertNotNull(hint);
        assertEquals(TypeHint.NUMBER, hint);
    }

    @Test
    void testParseColorHint() {
        TypeHint hint = TypeHint.parse("color:#ff0000");
        assertNotNull(hint);
        assertEquals(TypeHint.COLOR, hint);
    }

    @Test
    void testParseAngleHint() {
        TypeHint hint = TypeHint.parse("angle:45deg");
        assertNotNull(hint);
        assertEquals(TypeHint.ANGLE, hint);
    }

    @Test
    void testParseUrlHint() {
        TypeHint hint = TypeHint.parse("url:https://example.com/image.png");
        assertNotNull(hint);
        assertEquals(TypeHint.URL, hint);
    }

    @Test
    void testParseImageHint() {
        TypeHint hint = TypeHint.parse("image:url(https://example.com/image.png)");
        assertNotNull(hint);
        assertEquals(TypeHint.IMAGE, hint);
    }

    @Test
    void testParseFamilyNameHint() {
        TypeHint hint = TypeHint.parse("family-name:Arial");
        assertNotNull(hint);
        assertEquals(TypeHint.FAMILY_NAME, hint);
    }

    @Test
    void testParseLineWidthHint() {
        TypeHint hint = TypeHint.parse("line-width:2px");
        assertNotNull(hint);
        assertEquals(TypeHint.LINE_WIDTH, hint);
    }

    @Test
    void testParseShapeHint() {
        TypeHint hint = TypeHint.parse("shape:rect(0, 100%, 100%, 0)");
        assertNotNull(hint);
        assertEquals(TypeHint.SHAPE, hint);
    }

    @Test
    void testParsePositionHint() {
        TypeHint hint = TypeHint.parse("position:top right");
        assertNotNull(hint);
        assertEquals(TypeHint.POSITION, hint);
    }

    @Test
    void testParseBgSizeHint() {
        TypeHint hint = TypeHint.parse("bg-size:cover");
        assertNotNull(hint);
        assertEquals(TypeHint.BG_SIZE, hint);
    }

    @Test
    void testParseNoHint() {
        TypeHint hint = TypeHint.parse("320px");
        assertNull(hint);
    }

    @Test
    void testParseNull() {
        TypeHint hint = TypeHint.parse(null);
        assertNull(hint);
    }

    @Test
    void testParseEmpty() {
        TypeHint hint = TypeHint.parse("");
        assertNull(hint);
    }

    @Test
    void testParseBlank() {
        TypeHint hint = TypeHint.parse("   ");
        assertNull(hint);
    }

    @Test
    void testExtractValueWithHint() {
        String value = TypeHint.extractValue("length:320px");
        assertEquals("320px", value);
    }

    @Test
    void testExtractValueWithPercentageHint() {
        String value = TypeHint.extractValue("percentage:50%");
        assertEquals("50%", value);
    }

    @Test
    void testExtractValueWithColorHint() {
        String value = TypeHint.extractValue("color:#ff0000");
        assertEquals("#ff0000", value);
    }

    @Test
    void testExtractValueNoHint() {
        String value = TypeHint.extractValue("320px");
        assertEquals("320px", value);
    }

    @Test
    void testExtractValueNull() {
        String value = TypeHint.extractValue(null);
        assertNull(value);
    }

    @Test
    void testExtractValueEmpty() {
        String value = TypeHint.extractValue("");
        assertEquals("", value);
    }

    @Test
    void testHasTypeHintTrue() {
        assertTrue(TypeHint.hasTypeHint("length:320px"));
        assertTrue(TypeHint.hasTypeHint("percentage:50%"));
        assertTrue(TypeHint.hasTypeHint("color:#ff0000"));
        assertTrue(TypeHint.hasTypeHint("angle:45deg"));
    }

    @Test
    void testHasTypeHintFalse() {
        assertFalse(TypeHint.hasTypeHint("320px"));
        assertFalse(TypeHint.hasTypeHint("50%"));
        assertFalse(TypeHint.hasTypeHint("#ff0000"));
        assertFalse(TypeHint.hasTypeHint(null));
        assertFalse(TypeHint.hasTypeHint(""));
    }

    @Test
    void testCaseInsensitive() {
        TypeHint hint1 = TypeHint.parse("LENGTH:320px");
        TypeHint hint2 = TypeHint.parse("Length:320px");
        TypeHint hint3 = TypeHint.parse("length:320px");
        
        assertEquals(TypeHint.LENGTH, hint1);
        assertEquals(TypeHint.LENGTH, hint2);
        assertEquals(TypeHint.LENGTH, hint3);
    }

    @Test
    void testAllTypeHints() {
        for (TypeHint typeHint : TypeHint.values()) {
            String testValue = typeHint.getHint() + ":test";
            TypeHint parsed = TypeHint.parse(testValue);
            assertNotNull(parsed, "Failed to parse hint: " + testValue);
            assertEquals(typeHint, parsed);
        }
    }
}
