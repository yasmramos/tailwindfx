package io.github.yasmramos.tailwindfx.components;

import javafx.scene.control.TitledPane;
import javafx.scene.layout.Region;

/**
 * TitledPane personalizado para inyectar clases de Tailwind en sus partes internas.
 */
public class TWTitledPane extends TitledPane {

    public TWTitledPane() {
        super();
        initTailwindClasses();
    }

    public TWTitledPane(String title) {
        this.setText(title);
        initTailwindClasses();
    }

    public TWTitledPane(String title, Region content) {
        super(title, content);
        initTailwindClasses();
    }

    private void initTailwindClasses() {
        // Esta clase identifica al item dentro del accordion
        getStyleClass().add("collapse-item");
        
        // Nota: En JavaFX no podemos añadir clases directamente al nodo interno 
        // del header o content sin manipular la Skin. 
        // La estrategia recomendada es usar selectores CSS descendentes.
        // Ver archivo CSS abajo.
    }
}