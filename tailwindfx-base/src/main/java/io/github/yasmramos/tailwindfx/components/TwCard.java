package io.github.yasmramos.tailwindfx.components;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/** TwCard — Custom card component extending JavaFX VBox with TailwindCSS styling. */
public class TwCard extends VBox {

  public TwCard() {
    super();
    initialize();
  }

  public TwCard(Node... children) {
    super(children);
    initialize();
  }

  private void initialize() {
    getStyleClass().add("tw-card");
    setSpacing(16);
    setPadding(new Insets(16));
  }

  public void setHeader(Node header) {
    if (!getChildren().isEmpty()) {
      getChildren().add(0, header);
    } else {
      getChildren().add(header);
    }
  }

  public void setBody(Node body) {
    getChildren().add(body);
  }

  public void setFooter(Node footer) {
    getChildren().add(footer);
  }

  public static TwCard withTitle(String title) {
    TwCard card = new TwCard();
    javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(title);
    titleLabel.getStyleClass().addAll("text-xl", "font-bold");
    card.setHeader(titleLabel);
    return card;
  }

  public static TwCard withContent(Node content) {
    TwCard card = new TwCard();
    card.setBody(content);
    return card;
  }
}
