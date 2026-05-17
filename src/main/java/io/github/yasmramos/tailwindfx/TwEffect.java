package io.github.yasmramos.tailwindfx;

import javafx.scene.Node;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.BlurType;

/**
 * TwEffect — Visual effects facade.
 * 
 * <p>Provides access to visual effects including blur,
 * shadows, and clipping utilities.</p>
 * 
 * <pre>
 * TwEffect.backdropBlur(node, 8);
 * TwEffect.backdropBlurNone(node);
 * </pre>
 */
public final class TwEffect {
    
    private static final TwEffect INSTANCE = new TwEffect();
    
    private TwEffect() {}
    
    /**
     * Apply backdrop blur effect to a node.
     * @param node the node
     * @param radius blur radius
     */
    public static void backdropBlur(Node node, double radius) {
        BoxBlur blur = new BoxBlur(radius, radius, 1);
        node.setEffect(blur);
    }
    
    /**
     * Remove backdrop blur effect from a node.
     * @param node the node
     */
    public static void backdropBlurNone(Node node) {
        node.setEffect(null);
    }
}
