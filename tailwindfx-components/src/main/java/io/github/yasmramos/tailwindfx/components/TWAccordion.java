package io.github.yasmramos.tailwindfx.components;

import javafx.collections.ListChangeListener;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;

/**
 * TWAccordion — Accordion styled with the {@code collapse} component classes.
 *
 * <p>Every pane receives the {@code collapse-item} class plus a {@code collapse-open} or {@code
 * collapse-close} state class that follows the currently expanded pane. Panes added after
 * construction are styled the same way as the ones passed to the constructor.
 *
 * <pre>
 * TWAccordion accordion = new TWAccordion(
 *     new TWTitledPane("Design system", content),
 *     new TWTitledPane("Theming", theming));
 * </pre>
 */
public class TWAccordion extends Accordion {

  /** Base stylesheet class applied to the accordion container. */
  private static final String BASE_CLASS = "collapse";

  /** State class applied to the currently expanded pane. */
  private static final String STATE_OPEN = "collapse-open";

  /** State class applied to every collapsed pane. */
  private static final String STATE_CLOSED = "collapse-close";

  /** Creates an empty accordion. */
  public TWAccordion() {
    super();
    initialize();
  }

  /**
   * Creates an accordion with the given panes.
   *
   * @param titledPanes the panes to add
   */
  public TWAccordion(TitledPane... titledPanes) {
    super(titledPanes);
    initialize();
  }

  private void initialize() {
    getStyleClass().add(BASE_CLASS);

    // Style the constructor panes, then keep styling later additions so dynamically added panes
    // behave the same as the initial ones.
    getPanes().forEach(this::ensureTailwindStyle);
    getPanes()
        .addListener(
            (ListChangeListener<TitledPane>)
                change -> {
                  change.getAddedSubList().forEach(this::ensureTailwindStyle);
                  updateStateClasses();
                });

    updateStateClasses();

    // Reflect the expanded pane as collapse-open / collapse-close on each pane.
    expandedPaneProperty().addListener((obs, oldPane, newPane) -> updateStateClasses());
  }

  /**
   * Ensures a pane carries the classes required by the component stylesheet.
   *
   * <p>{@link TWTitledPane} adds them itself, so plain panes are the only ones that need the
   * class injected here.
   *
   * @param pane the pane to style, ignored when null
   */
  private void ensureTailwindStyle(TitledPane pane) {
    if (pane != null && !(pane instanceof TWTitledPane)) {
      pane.getStyleClass().add("collapse-item");
    }
  }

  private void updateStateClasses() {
    TitledPane expanded = getExpandedPane();
    for (TitledPane pane : getPanes()) {
      if (pane == null) {
        continue;
      }
      // Replace the state class in a single pass so a pane never keeps both, and so panes that
      // were expanded keep no stale class after collapsing.
      if (pane.equals(expanded)) {
        pane.getStyleClass().remove(STATE_CLOSED);
        pane.getStyleClass().add(STATE_OPEN);
      } else {
        pane.getStyleClass().remove(STATE_OPEN);
        pane.getStyleClass().add(STATE_CLOSED);
      }
    }
  }
}
