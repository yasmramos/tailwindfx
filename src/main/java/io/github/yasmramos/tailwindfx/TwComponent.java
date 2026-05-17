package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.components.ComponentFactory;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * TwComponent — Component facade for pre-built UI components.
 * 
 * <pre>
 * Node card = TwComponent.card().title("Hello").build();
 * Node badge = TwComponent.badge("New");
 * </pre>
 */
public final class TwComponent {
    
    private static final TwComponent INSTANCE = new TwComponent();
    
    private TwComponent() {}
    
    /**
     * Creates a card builder.
     */
    public static ComponentFactory.CardBuilder card() {
        return ComponentFactory.card();
    }
    
    /**
     * Creates a badge node.
     */
    public static Node badge(String text) {
        return ComponentFactory.badge(text, "blue");
    }
    
    /**
     * Creates a modal dialog.
     */
    public static ComponentFactory.ModalBuilder modal(Node content) {
        return ComponentFactory.modal(content);
    }
    
    /**
     * Creates a drawer panel.
     */
    public static ComponentFactory.DrawerBuilder drawer(ComponentFactory.DrawerSide side, double size) {
        return ComponentFactory.drawer(side, size);
    }
}
