package io.github.yasmramos.tailwindfx.components;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * TwCard — Card container extending JavaFX {@link VBox} with TailwindCSS styling.
 *
 * <p>The card exposes optional header and footer slots that always stay in order (header, body
 * content, footer) regardless of the order in which they are assigned.
 */
public class TwCard extends VBox {

  private Node header;
  private Node footer;

  /** Creates an empty card. */
  public TwCard() {
    super();
    initialize();
  }

  /**
   * Creates a card with the given children as its body.
   *
   * @param children the body nodes
   */
  public TwCard(Node... children) {
    super(children);
    initialize();
  }

  private void initialize() {
    getStyleClass().add("tw-card");
    setSpacing(16);
    setPadding(new Insets(16));
  }

  /**
   * Sets the card header, replacing any previous one.
   *
   * <p>The header is always kept as the first child so header/body/footer keep their order no
   * matter the order in which they are set.
   *
   * @param header the header node
   */
  public void setHeader(Node header) {
    if (this.header != null) {
      getChildren().remove(this.header);
    }
    this.header = header;
    if (header != null) {
      getChildren().add(0, header);
    }
  }

  /**
   * Gets the card header.
   *
   * @return the header node, or null if none was set
   */
  public Node getHeader() {
    return header;
  }

  /**
   * Appends content to the card body, before the footer.
   *
   * <p>The body is inserted ahead of the footer so header/body/footer keep their documented order
   * no matter the order in which they are set.
   *
   * @param body the body node
   */
  public void setBody(Node body) {
    if (this.footer != null) {
      getChildren().add(getChildren().indexOf(this.footer), body);
    } else {
      getChildren().add(body);
    }
  }

  /**
   * Sets the card footer, replacing any previous one.
   *
   * @param footer the footer node, or null to clear the footer
   */
  public void setFooter(Node footer) {
    if (this.footer != null) {
      getChildren().remove(this.footer);
    }
    this.footer = footer;
    if (footer != null) {
      getChildren().add(footer);
    }
  }

  /**
   * Gets the card footer.
   *
   * @return the footer node, or null if none was set
   */
  public Node getFooter() {
    return footer;
  }

  /**
   * Creates a card with a bold title as its header.
   *
   * @param title the title text
   * @return a TwCard with the title applied
   */
  public static TwCard withTitle(String title) {
    TwCard card = new TwCard();
    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().addAll("text-xl", "font-bold");
    card.setHeader(titleLabel);
    return card;
  }

  /**
   * Creates a card with a single body node.
   *
   * @param content the body node
   * @return a TwCard containing the node
   */
  public static TwCard withContent(Node content) {
    TwCard card = new TwCard();
    card.setBody(content);
    return card;
  }
}
