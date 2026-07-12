package io.github.yasmramos.tailwindfx.components;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

/**
 * TwAlert — Custom alert component extending JavaFX HBox with TailwindCSS styling.
 *
 * <p>Extends HBox for proper layout support, CSS styling, and native behavior.
 */
public class TwAlert extends HBox {

  private AlertType type = AlertType.INFO;

  /** Creates a default alert. */
  public TwAlert() {
    super();
    initialize();
  }

  /**
   * Creates an alert with content.
   *
   * @param content the alert content
   */
  public TwAlert(String content) {
    super();
    initialize();
    setContent(content);
  }

  /**
   * Creates an alert with type and content.
   *
   * @param type the alert type
   * @param content the alert content
   */
  public TwAlert(AlertType type, String content) {
    super();
    this.type = type;
    initialize();
    setContent(content);
  }

  private void initialize() {
    getStyleClass().add("tw-alert");
    setSpacing(12);
    setPadding(new Insets(16));
    applyType();
  }

  private void applyType() {
    getStyleClass().removeAll("alert-info", "alert-success", "alert-warning", "alert-error");

    switch (type) {
      case INFO:
        getStyleClass().add("alert-info");
        break;
      case SUCCESS:
        getStyleClass().add("alert-success");
        break;
      case WARNING:
        getStyleClass().add("alert-warning");
        break;
      case ERROR:
        getStyleClass().add("alert-error");
        break;
    }
  }

  /**
   * Sets the alert type.
   *
   * @param type the alert type
   */
  public void setType(AlertType type) {
    this.type = type;
    applyType();
  }

  /**
   * Gets the alert type.
   *
   * @return the alert type
   */
  public AlertType getType() {
    return type;
  }

  /**
   * Sets the alert content.
   *
   * @param content the content text
   */
  public void setContent(String content) {
    getChildren().clear();
    javafx.scene.control.Label label = new javafx.scene.control.Label(content);
    label.setWrapText(true);
    getChildren().add(label);
  }

  /**
   * Sets the alert content with icon.
   *
   * @param icon the icon node
   * @param content the content text
   */
  public void setContent(Node icon, String content) {
    getChildren().clear();
    javafx.scene.control.Label label = new javafx.scene.control.Label(content);
    label.setWrapText(true);
    getChildren().addAll(icon, label);
  }

  /**
   * Creates an info alert.
   *
   * @param content the alert content
   * @return TwAlert instance
   */
  public static TwAlert info(String content) {
    return new TwAlert(AlertType.INFO, content);
  }

  /**
   * Creates a success alert.
   *
   * @param content the alert content
   * @return TwAlert instance
   */
  public static TwAlert success(String content) {
    return new TwAlert(AlertType.SUCCESS, content);
  }

  /**
   * Creates a warning alert.
   *
   * @param content the alert content
   * @return TwAlert instance
   */
  public static TwAlert warning(String content) {
    return new TwAlert(AlertType.WARNING, content);
  }

  /**
   * Creates an error alert.
   *
   * @param content the alert content
   * @return TwAlert instance
   */
  public static TwAlert error(String content) {
    return new TwAlert(AlertType.ERROR, content);
  }

  /** Alert type enum. */
  public enum AlertType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
  }
}
