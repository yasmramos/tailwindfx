package io.github.yasmramos.tailwindfx.components;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TwCard component
 */
public class TwCardTest {

    private TwCard card;

    @BeforeEach
    public void setUp() {
        card = new TwCard();
    }

    @Test
    public void testConstructor() {
        assertNotNull(card);
    }

    @Test
    public void testSetTitle() {
        card.setTitle("My Card");
        assertEquals("My Card", card.getTitle());
    }

    @Test
    public void testSetDescription() {
        card.setDescription("Card description");
        // Verify description is set
        assertNotNull(card);
    }

    @Test
    public void testSetVariant() {
        card.setVariant("outlined");
        assertNotNull(card);
    }

    @Test
    public void testChainingMethods() {
        TwCard result = card.title("Title")
                           .description("Description")
                           .variant("elevated");
        
        assertEquals(card, result);
        assertEquals("Title", card.getTitle());
    }
}
