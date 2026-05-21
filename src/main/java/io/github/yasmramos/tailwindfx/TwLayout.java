package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.layout.TwFlexPane;
import io.github.yasmramos.tailwindfx.layout.TwGridPane;
import io.github.yasmramos.tailwindfx.layout.TwLayoutHelper;
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
     * Apply layout classes (flex, grid, gap) with automatic container migration if needed.
     * @param node the node to apply layout to
     * @param tokens the layout tokens (flex, grid, gap-*, etc.)
     */
    public static void apply(Node node, String... tokens) {
        if (tokens == null || tokens.length == 0) return;
        
        Pane parent = getEffectiveParent(node);
        if (parent == null) return;
        
        // Use TwLayoutHelper for consistent layout management
        TwLayoutHelper helper = TwLayoutHelper.of(parent);
        
        for (String token : tokens) {
            if (token == null || token.isBlank()) continue;
            
            if (token.equals("flex")) {
                helper.flex().build();
            } else if (token.equals("grid")) {
                helper.flexGrid().build();
            } else if (token.startsWith("gap-")) {
                applyGap(helper, token);
            } else if (token.startsWith("flex-") || token.equals("grow") || token.equals("shrink")) {
                applyFlexItem(node, token);
            }
        }
    }
    
    /**
     * Applies gap style using TwLayoutHelper.
     */
    private static void applyGap(TwLayoutHelper helper, String token) {
        int value = parseTailwindValue(token);
        double px = value * 4.0;
        
        if (token.startsWith("gap-x-")) {
            helper.hgap(px).build();
        } else if (token.startsWith("gap-y-")) {
            helper.vgap(px).build();
        } else {
            helper.gap(px).build();
        }
    }
    
    /**
     * Applies flex item style to a node.
     */
    private static void applyFlexItem(Node node, String token) {
        Pane parent = getEffectiveParent(node);
        if (!(parent instanceof TwFlexPane)) return;
        
        if (token.equals("grow") || token.equals("flex-1")) {
            TwFlexPane.setGrow(node, 1);
        } else if (token.equals("shrink") || token.equals("flex-none")) {
            TwFlexPane.setShrink(node, 0);
        } else if (token.equals("flex-auto")) {
            TwFlexPane.setGrow(node, 1);
            TwFlexPane.setShrink(node, 1);
        } else if (token.startsWith("flex-")) {
            try {
                String value = token.substring(5);
                if (value.startsWith("[") && value.endsWith("]")) {
                    value = value.substring(1, value.length() - 1);
                }
                double flexValue = Double.parseDouble(value);
                TwFlexPane.setGrow(node, flexValue);
            } catch (NumberFormatException e) {
                // Ignore invalid values
            }
        }
    }
    
    /**
     * Gets the effective parent pane.
     */
    private static Pane getEffectiveParent(Node node) {
        javafx.scene.Parent parent = node.getParent();
        if (parent instanceof Pane) {
            return (Pane) parent;
        }
        return null;
    }
    
    /**
     * Parses numeric value from Tailwind token.
     */
    private static int parseTailwindValue(String token) {
        if (token.contains("[")) {
            int start = token.indexOf('[') + 1;
            int end = token.indexOf(']');
            String value = token.substring(start, end);
            if (value.contains("px")) {
                return (int) Double.parseDouble(value.replace("px", ""));
            }
            return Integer.parseInt(value);
        }
        
        String numPart = token.substring(token.lastIndexOf('-') + 1);
        try {
            return Integer.parseInt(numPart);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Get layout builder for a container.
     * @param container the pane container
     * @return TwLayoutHelper builder
     */
    public static TwLayoutHelper of(Pane container) {
        return TwLayoutHelper.of(container);
    }
    
    /**
     * Create a horizontal flex pane (row direction).
     * @return new TwFlexPane with ROW direction
     */
    public static TwFlexPane flexRow() {
        return TwFlexPane.row();
    }
    
    /**
     * Create a vertical flex pane (column direction).
     * @return new TwFlexPane with COL direction
     */
    public static TwFlexPane flexCol() {
        return TwFlexPane.col();
    }
    
    /**
     * Create a grid pane builder.
     * @return TwGridPane.Builder to configure and build
     */
    public static TwGridPane.Builder grid() {
        return TwGridPane.create();
    }
    
    /**
     * Create a grid pane with default gap.
     * @return new TwGridPane instance
     */
    public static TwGridPane gridBuild() {
        return TwGridPane.create().build();
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
