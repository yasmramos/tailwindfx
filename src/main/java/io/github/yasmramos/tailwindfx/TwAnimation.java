package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.animation.FxAnimation;
import javafx.scene.Node;

/**
 * TwAnimation — Animation facade for creating and playing animations.
 * 
 * <pre>
 * TwAnimation.fadeIn(node).play();
 * TwAnimation.slideUp(node, 300).play();
 * </pre>
 */
public final class TwAnimation {
    
    private static final TwAnimation INSTANCE = new TwAnimation();
    
    private TwAnimation() {}
    
    public static FxAnimation fadeIn(Node node) {
        return FxAnimation.fadeIn(node);
    }
    
    public static FxAnimation fadeIn(Node node, int durationMs) {
        return FxAnimation.fadeIn(node, durationMs);
    }
    
    public static FxAnimation fadeOut(Node node) {
        return FxAnimation.fadeOut(node);
    }
    
    public static FxAnimation slideUp(Node node) {
        return FxAnimation.slideUp(node);
    }
    
    public static FxAnimation slideUp(Node node, int durationMs) {
        return FxAnimation.slideUp(node, durationMs);
    }
    
    public static FxAnimation slideDown(Node node) {
        return FxAnimation.slideDown(node);
    }
    
    public static FxAnimation slideLeft(Node node) {
        return FxAnimation.slideLeft(node);
    }
    
    public static FxAnimation slideRight(Node node) {
        return FxAnimation.slideRight(node);
    }
    
    public static FxAnimation shake(Node node) {
        return FxAnimation.shake(node);
    }
    
    public static FxAnimation pulse(Node node) {
        return FxAnimation.pulse(node);
    }
    
    public static FxAnimation bounce(Node node) {
        return FxAnimation.bounce(node);
    }
}
