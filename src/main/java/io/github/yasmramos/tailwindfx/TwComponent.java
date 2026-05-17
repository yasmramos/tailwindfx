package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.components.ComponentFactory;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * TwComponent — Component facade for pre-built UI components.
 * 
 * <pre>
 * Node card = TwComponent.INSTANCE.card().title("Hello").build();
 * Node badge = TwComponent.INSTANCE.badge("New");
 * </pre>
 */
public final class TwComponent {
    
    public static final TwComponent INSTANCE = new TwComponent();
    
    private TwComponent() {}
    
    /**
     * Creates a card builder.
     */
    public ComponentFactory.CardBuilder card() {
        return ComponentFactory.card();
    }
    
    /**
     * Creates a badge node.
     */
    public Node badge(String text) {
        return ComponentFactory.badge(text, "blue");
    }
    
    /**
     * Creates a modal dialog.
     */
    public ComponentFactory.ModalBuilder modal(Node content) {
        return ComponentFactory.modal(content);
    }
    
    /**
     * Creates a drawer panel.
     */
    public ComponentFactory.DrawerBuilder drawer(ComponentFactory.DrawerSide side, double size) {
        return ComponentFactory.drawer(side, size);
    }
}
