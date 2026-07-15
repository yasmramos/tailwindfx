package io.github.yasmramos.tailwindfx.components;

import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TwCard component.
 */
public class TwCardTest extends ApplicationTest {

    @Override
    public void start(javafx.stage.Stage stage) {
        // Empty stage for TestFX
    }

    @Test
    public void testConstructor_NoArgs() {
        TwCard card = new TwCard();
        
        assertNotNull(card);
        assertTrue(card.getStyleClass().contains("tw-card"));
        assertEquals(16, card.getSpacing(), 0.01);
        assertEquals(16, card.getPadding().getTop(), 0.01);
        assertEquals(16, card.getPadding().getRight(), 0.01);
        assertEquals(16, card.getPadding().getBottom(), 0.01);
        assertEquals(16, card.getPadding().getLeft(), 0.01);
    }

    @Test
    public void testConstructor_WithChildren() {
        Label child1 = new Label("Child 1");
        Label child2 = new Label("Child 2");
        
        TwCard card = new TwCard(child1, child2);
        
        assertNotNull(card);
        assertTrue(card.getStyleClass().contains("tw-card"));
        assertEquals(2, card.getChildren().size());
        assertSame(child1, card.getChildren().get(0));
        assertSame(child2, card.getChildren().get(1));
    }

    @Test
    public void testSetHeader_EmptyCard() {
        TwCard card = new TwCard();
        Label header = new Label("Header");
        
        card.setHeader(header);
        
        assertEquals(1, card.getChildren().size());
        assertSame(header, card.getChildren().get(0));
    }

    @Test
    public void testSetHeader_NonEmptyCard() {
        TwCard card = new TwCard();
        Label existing = new Label("Existing");
        card.getChildren().add(existing);
        
        Label header = new Label("Header");
        card.setHeader(header);
        
        assertEquals(2, card.getChildren().size());
        assertSame(header, card.getChildren().get(0));
        assertSame(existing, card.getChildren().get(1));
    }

    @Test
    public void testSetBody() {
        TwCard card = new TwCard();
        Label body = new Label("Body Content");
        
        card.setBody(body);
        
        assertEquals(1, card.getChildren().size());
        assertSame(body, card.getChildren().get(0));
    }

    @Test
    public void testSetFooter() {
        TwCard card = new TwCard();
        Label footer = new Label("Footer");
        
        card.setFooter(footer);
        
        assertEquals(1, card.getChildren().size());
        assertSame(footer, card.getChildren().get(0));
    }

    @Test
    public void testWithTitle() {
        TwCard card = TwCard.withTitle("My Title");
        
        assertNotNull(card);
        assertTrue(card.getStyleClass().contains("tw-card"));
        assertEquals(1, card.getChildren().size());
        
        Node header = card.getChildren().get(0);
        assertTrue(header instanceof Label);
        Label titleLabel = (Label) header;
        assertEquals("My Title", titleLabel.getText());
        assertTrue(titleLabel.getStyleClass().contains("text-xl"));
        assertTrue(titleLabel.getStyleClass().contains("font-bold"));
    }

    @Test
    public void testWithContent() {
        Label content = new Label("Content");
        
        TwCard card = TwCard.withContent(content);
        
        assertNotNull(card);
        assertEquals(1, card.getChildren().size());
        assertSame(content, card.getChildren().get(0));
    }

    @Test
    public void testFullCardStructure() {
        TwCard card = new TwCard();
        Label header = new Label("Header");
        Label body = new Label("Body");
        Label footer = new Label("Footer");
        
        card.setHeader(header);
        card.setBody(body);
        card.setFooter(footer);
        
        assertEquals(3, card.getChildren().size());
        assertSame(header, card.getChildren().get(0));
        assertSame(body, card.getChildren().get(1));
        assertSame(footer, card.getChildren().get(2));
    }

    @Test
    public void testMultipleSetBodyCalls() {
        TwCard card = new TwCard();
        
        card.setBody(new Label("Body 1"));
        card.setBody(new Label("Body 2"));
        
        assertEquals(2, card.getChildren().size());
    }

    @Test
    public void testCardSpacingAndPadding() {
        TwCard card = new TwCard();
        
        assertEquals(16, card.getSpacing(), 0.01);
        
        Insets padding = card.getPadding();
        assertEquals(16, padding.getTop(), 0.01);
        assertEquals(16, padding.getRight(), 0.01);
        assertEquals(16, padding.getBottom(), 0.01);
        assertEquals(16, padding.getLeft(), 0.01);
    }
}
