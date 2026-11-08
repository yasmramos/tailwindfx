package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.*;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/** Unit tests for TwCard component. */
public class TwCardIT extends ApplicationTest {

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
    assertTrue(
        titleLabel.getStyleClass().contains("tw-card-title"),
        "the title must carry tw-card-title so the .tw-card-title rule styles it: "
            + titleLabel.getStyleClass());
    assertTrue(
        titleLabel.getStyleClass().contains("tw-card-header"),
        "the title is the header slot, so it must also carry tw-card-header: "
            + titleLabel.getStyleClass());
  }

  @Test
  public void testHeaderGetsHeaderClass() {
    TwCard card = new TwCard();
    Label header = new Label("Header");

    card.setHeader(header);

    assertTrue(
        header.getStyleClass().contains("tw-card-header"),
        "the header slot must carry tw-card-header so the stylesheet styles it: "
            + header.getStyleClass());
  }

  @Test
  public void testBodyGetsContentClass() {
    TwCard card = new TwCard();
    Label body = new Label("Body");

    card.setBody(body);

    assertTrue(
        body.getStyleClass().contains("tw-card-content"),
        "the body slot must carry tw-card-content so the stylesheet styles it: "
            + body.getStyleClass());
  }

  @Test
  public void testFooterGetsFooterClass() {
    TwCard card = new TwCard();
    Label footer = new Label("Footer");

    card.setFooter(footer);

    assertTrue(
        footer.getStyleClass().contains("tw-card-footer"),
        "the footer slot must carry tw-card-footer so the stylesheet styles it: "
            + footer.getStyleClass());
  }

  @Test
  public void testSlotClassesAreNotDuplicated() {
    TwCard card = new TwCard();
    Label header = new Label("Header");
    Label body = new Label("Body");
    Label footer = new Label("Footer");

    card.setHeader(header);
    card.setHeader(header);
    card.setBody(body);
    card.setFooter(footer);

    assertEquals(1, header.getStyleClass().stream().filter("tw-card-header"::equals).count());
    assertEquals(1, body.getStyleClass().stream().filter("tw-card-content"::equals).count());
    assertEquals(1, footer.getStyleClass().stream().filter("tw-card-footer"::equals).count());
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
  public void testSetHeaderTwiceReplacesPrevious() {
    TwCard card = new TwCard();
    Label first = new Label("First");
    Label second = new Label("Second");

    card.setHeader(first);
    card.setHeader(second);

    assertEquals(1, card.getChildren().size());
    assertSame(second, card.getHeader());
    assertFalse(card.getChildren().contains(first));
  }

  @Test
  public void testSetFooterTwiceReplacesPrevious() {
    TwCard card = new TwCard();
    Label first = new Label("First");
    Label second = new Label("Second");

    card.setFooter(first);
    card.setFooter(second);

    assertEquals(1, card.getChildren().size());
    assertSame(second, card.getFooter());
    assertFalse(card.getChildren().contains(first));
  }

  @Test
  public void testHeaderStaysFirstWhenSetLast() {
    TwCard card = new TwCard();
    Label footer = new Label("Footer");
    Label header = new Label("Header");

    // Footer assigned before the header must not push the header out of first position.
    card.setFooter(footer);
    card.setBody(new Label("Body"));
    card.setHeader(header);

    assertEquals(3, card.getChildren().size());
    assertSame(header, card.getChildren().get(0));
    assertSame(footer, card.getChildren().get(2));
  }

  @Test
  public void testGetHeaderAndFooterDefaultToNull() {
    TwCard card = new TwCard();

    assertNull(card.getHeader());
    assertNull(card.getFooter());
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
