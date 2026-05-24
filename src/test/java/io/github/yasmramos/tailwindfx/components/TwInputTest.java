package io.github.yasmramos.tailwindfx.components;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TwInput component
 */
public class TwInputTest {

    private TwInput input;

    @BeforeEach
    public void setUp() {
        input = new TwInput();
    }

    @Test
    public void testConstructor() {
        assertNotNull(input);
    }

    @Test
    public void testSetPromptText() {
        input.setPromptText("Enter your name");
        assertEquals("Enter your name", input.getPromptText());
    }

    @Test
    public void testSetText() {
        input.setText("Hello World");
        assertEquals("Hello World", input.getText());
    }

    @Test
    public void testSetVariant() {
        input.setVariant("error");
        assertNotNull(input);
    }

    @Test
    public void testSetSize() {
        input.setSize("lg");
        assertNotNull(input);
    }

    @Test
    public void testSetDisabled() {
        input.setDisabled(true);
        assertTrue(input.isDisabled());
        
        input.setDisabled(false);
        assertFalse(input.isDisabled());
    }

    @Test
    public void testChainingMethods() {
        TwInput result = input.promptText("Email")
                              .variant("primary")
                              .size("md")
                              .disabled(false);
        
        assertEquals(input, result);
        assertEquals("Email", input.getPromptText());
        assertFalse(input.isDisabled());
    }
}
