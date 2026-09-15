package io.github.yasmramos.tailwindfx.components;

import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;

/**
 * Componente Accordion estilizado con clases utilitarias tipo Tailwind.
 *
 * @author CONTADOR
 */
public class TWAccordion extends Accordion {

  public TWAccordion() {
    super();
    // La clase 'collapse' define el contenedor principal
    getStyleClass().add("collapse");

    // Escuchar cambios en el pane expandido para actualizar estados (open/close)
    expandedPaneProperty()
        .addListener(
            (obs, oldPane, newPane) -> {
              updateStateClasses();
            });
  }

  public TWAccordion(TitledPane... titledPanes) {
    super(titledPanes);
    getStyleClass().add("collapse");
    // Ensure that all panes have the correct style
    for (TitledPane pane : titledPanes) {
      ensureTailwindStyle(pane);
    }
    // Update state classes after panes are added
    updateStateClasses();
    
    // Listen for changes in expanded pane to update states (open/close)
    expandedPaneProperty()
        .addListener(
            (obs, oldPane, newPane) -> {
              updateStateClasses();
            });
  }

  private void ensureTailwindStyle(TitledPane pane) {
    // Si no es un TWTitledPane, forzamos las clases necesarias
    if (!(pane instanceof TWTitledPane)) {
      pane.getStyleClass().add("collapse-item");
      // Nota: Para un control total, se recomienda usar TWTitledPane
    }
  }

  private void updateStateClasses() {
    // Clear previous state classes on all panes
    for (TitledPane pane : getPanes()) {
      pane.getStyleClass().remove("collapse-open");
      pane.getStyleClass().remove("collapse-close");
    }
    
    // Apply correct state class based on expanded state
    TitledPane expanded = getExpandedPane();
    for (TitledPane pane : getPanes()) {
      if (pane.equals(expanded)) {
        pane.getStyleClass().add("collapse-open");
      } else {
        pane.getStyleClass().add("collapse-close");
      }
    }
  }
}
