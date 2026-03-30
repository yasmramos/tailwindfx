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
        expandedPaneProperty().addListener((obs, oldPane, newPane) -> {
            updateStateClasses();
        });
    }

    public TWAccordion(TitledPane... titledPanes) {
        super(titledPanes);
        getStyleClass().add("collapse");
        // Asegurar que los panes iniciales tengan el estilo correcto
        for (TitledPane pane : titledPanes) {
            ensureTailwindStyle(pane);
        }
        updateStateClasses();
    }


    private void ensureTailwindStyle(TitledPane pane) {
        // Si no es un TWTitledPane, forzamos las clases necesarias
        if (!(pane instanceof TWTitledPane)) {
            pane.getStyleClass().add("collapse-item"); 
            // Nota: Para un control total, se recomienda usar TWTitledPane
        }
    }

    private void updateStateClasses() {
        // Limpiar estados anteriores en todos los panes
        for (TitledPane pane : getPanes()) {
            if (pane.isExpanded()) {
                pane.getStyleClass().add("collapse-open");
                pane.getStyleClass().remove("collapse-close");
            } else {
                pane.getStyleClass().add("collapse-close");
                pane.getStyleClass().remove("collapse-open");
            }
        }
    }
}