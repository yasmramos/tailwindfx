package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.components.FxFlexPane;
import io.github.yasmramos.tailwindfx.components.FxGridPane;
import io.github.yasmramos.tailwindfx.layout.FxLayout;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * TwLayout — Layout management facade.
 * 
 * <p>Provides access to layout builders and utilities including
 * flexbox, grid, and aspect ratio helpers.</p>
 * 
 * <pre>
 * TwLayout.of(container).row().gap(16).build();
 * TwLayout.flexCol();
 * TwLayout.aspectRatio(node, 16, 9);
 * </pre>
 */
public final class TwLayout {
    
    private static final TwLayout INSTANCE = new TwLayout();
    
    private TwLayout() {}
    
    /**
     * Get layout builder for a container.
     * @param container the pane container
     * @return FxLayout builder
     */
    public static FxLayout of(Pane container) {
        return FxLayout.of(container);
    }
    
    /**
     * Create a horizontal flex pane (row direction).
     * @return new FxFlexPane with ROW direction
     */
    public static FxFlexPane flexRow() {
        return FxFlexPane.row();
    }
    
    /**
     * Create a vertical flex pane (column direction).
     * @return new FxFlexPane with COL direction
     */
    public static FxFlexPane flexCol() {
        return FxFlexPane.col();
    }
    
    /**
     * Create a grid pane builder.
     * @return FxGridPane.Builder to configure and build
     */
    public static FxGridPane.Builder grid() {
        return FxGridPane.create();
    }
    
    /**
     * Create a grid pane with default gap.
     * @return new FxGridPane instance
     */
    public static FxGridPane gridBuild() {
        return FxGridPane.create().build();
    }
    
    /**
     * Apply aspect ratio constraint to a node.
     * @param node the node
     * @param widthRatio width ratio
     * @param heightRatio height ratio
     * @throws IllegalArgumentException if ratios are not positive
     */
    public static void aspectRatio(Node node, int widthRatio, int heightRatio) {
        if (widthRatio <= 0 || heightRatio <= 0) {
            throw new IllegalArgumentException("Aspect ratio values must be positive");
        }
        // Implementation: add listener to maintain aspect ratio
        node.boundsInParentProperty().addListener((obs, old, bounds) -> {
            double targetWidth = bounds.getHeight() * widthRatio / heightRatio;
            if (node instanceof javafx.scene.layout.Region) {
                ((javafx.scene.layout.Region) node).setPrefWidth(targetWidth);
            }
        });
    }
}
