package io.github.yasmramos.tailwindfx.components;

import javafx.scene.control.TitledPane;
import javafx.scene.layout.Region;

/**
 * TWTitledPane — TitledPane carrying the {@code collapse-item} component class.
 *
 * <p>JavaFX does not expose the header and content nodes of a {@link TitledPane}, so the internal
 * parts are styled through descendant selectors declared in {@code tailwindfx-components.css}
 * rather than by injecting classes into the skin.
 *
 * <p>Use with {@link TwAccordion}, which applies the {@code collapse-open} / {@code
 * collapse-close} state classes as the pane is expanded and collapsed.
 */
public class TwTitledPane extends TitledPane {

  /** Identifies the pane as an item inside an accordion. */
  private static final String ITEM_CLASS = "collapse-item";

  /** Creates an empty titled pane. */
  public TwTitledPane() {
    super();
    initialize();
  }

  /**
   * Creates a titled pane with a title.
   *
   * @param title the pane title
   */
  public TwTitledPane(String title) {
    super(title);
    initialize();
  }

  /**
   * Creates a titled pane with a title and content.
   *
   * @param title the pane title
   * @param content the pane content
   */
  public TwTitledPane(String title, Region content) {
    super(title, content);
    initialize();
  }

  private void initialize() {
    getStyleClass().add(ITEM_CLASS);
  }
}