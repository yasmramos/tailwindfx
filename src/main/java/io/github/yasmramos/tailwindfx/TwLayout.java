package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.components.TwFlexPane;
import io.github.yasmramos.tailwindfx.components.TwGridPane;
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
        
        // Parse tokens to determine layout type
        for (String token : tokens) {
            if (token == null || token.isBlank()) continue;
            
            // Handle flex container
            if (token.equals("flex")) {
                migrateToFlexContainer(node);
            } else if (token.equals("grid")) {
                migrateToGridContainer(node);
            } else if (token.startsWith("gap-")) {
                // Gap is handled by the container
                Pane parent = getEffectiveParent(node);
                if (parent != null) {
                    applyGapStyle(parent, token);
                }
            } else if (token.startsWith("flex-") || token.equals("grow") || token.equals("shrink")) {
                // Flex item properties - applied to children
                Pane parent = getEffectiveParent(node);
                if (parent != null) {
                    applyFlexItemStyle(node, parent, token);
                }
            }
        }
    }
    
    /**
     * Migrates a node's parent to TwFlexPane if needed.
     */
    private static void migrateToFlexContainer(Node node) {
        javafx.scene.Parent currentParent = node.getParent();
        if (currentParent instanceof TwFlexPane) {
            // Already a flex container, no migration needed
            return;
        }
        
        if (currentParent instanceof Pane pane) {
            // Use TwLayoutHelper builder to migrate
            TwFlexPane flexPane = TwFlexPane.row();
            migrateContent(pane, flexPane);
        }
    }
    
    /**
     * Migrates a node's parent to TwGridPane if needed.
     */
    private static void migrateToGridContainer(Node node) {
        javafx.scene.Parent currentParent = node.getParent();
        if (currentParent instanceof TwGridPane) {
            // Already a grid container, no migration needed
            return;
        }
        
        if (currentParent instanceof Pane pane) {
            // Use TwLayoutHelper builder to migrate
            TwGridPane gridPane = TwGridPane.create().build();
            migrateContent(pane, gridPane);
        }
    }
    
    /**
     * Migrates content from source pane to target pane.
     */
    private static void migrateContent(Pane source, Pane target) {
        if (source == null || target == null) return;
        
        javafx.scene.Parent grandParent = source.getParent();
        int index = -1;
        
        if (grandParent instanceof javafx.scene.layout.Pane gp) {
            index = gp.getChildren().indexOf(source);
        }
        
        // Copy children
        java.util.List<javafx.scene.Node> children = new java.util.ArrayList<>(source.getChildren());
        target.getChildren().addAll(children);
        source.getChildren().clear();
        
        // Replace source with target in grandparent
        if (grandParent instanceof javafx.scene.layout.Pane gp && index >= 0) {
            gp.getChildren().remove(source);
            gp.getChildren().add(index, target);
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
     * Applies gap style to a container.
     */
    private static void applyGapStyle(Pane parent, String token) {
        int value = parseTailwindValue(token);
        double px = value * 4.0;
        
        if (parent instanceof TwFlexPane flexPane) {
            if (token.startsWith("gap-x-")) {
                flexPane.gapX(px);
            } else if (token.startsWith("gap-y-")) {
                flexPane.gapY(px);
            } else {
                flexPane.gap(px);
            }
        } else if (parent instanceof TwGridPane gridPane) {
            if (token.startsWith("gap-x-")) {
                gridPane.gapX(px);
            } else if (token.startsWith("gap-y-")) {
                gridPane.gapY(px);
            } else {
                gridPane.gap(px);
            }
        }
        // Add more container types as needed
    }
    
    /**
     * Applies flex item style to a node.
     */
    private static void applyFlexItemStyle(Node node, Pane parent, String token) {
        if (parent instanceof TwFlexPane flexPane) {
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
        // Add more container types as needed
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
