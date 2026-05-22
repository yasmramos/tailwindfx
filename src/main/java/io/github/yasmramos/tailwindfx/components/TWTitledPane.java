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
        // This class identifies the item within the accordion
        getStyleClass().add("collapse-item");
        
        // Note: In JavaFX we cannot add classes directly to the internal node 
        // of the header or content without manipulating the Skin. 
        // The recommended strategy is to use descendant CSS selectors.
        // See CSS file below.
    }
}