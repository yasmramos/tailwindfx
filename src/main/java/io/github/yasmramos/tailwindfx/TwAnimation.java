package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.animation.FxAnimation;
import javafx.scene.Node;

/**
 * TwAnimation — Animation facade for creating and playing animations.
 * 
 * <pre>
 * TwAnimation.INSTANCE.fadeIn(node).play();
 * TwAnimation.INSTANCE.slideUp(node, 300).play();
 * </pre>
 */
public final class TwAnimation {
    
    public static final TwAnimation INSTANCE = new TwAnimation();
    
    private TwAnimation() {}
    
    public FxAnimation fadeIn(Node node) {
        return FxAnimation.fadeIn(node);
    }
    
    public FxAnimation fadeIn(Node node, int durationMs) {
        return FxAnimation.fadeIn(node, durationMs);
    }
    
    public FxAnimation fadeOut(Node node) {
        return FxAnimation.fadeOut(node);
    }
    
    public FxAnimation slideUp(Node node) {
        return FxAnimation.slideUp(node);
    }
    
    public FxAnimation slideUp(Node node, int durationMs) {
        return FxAnimation.slideUp(node, durationMs);
    }
    
    public FxAnimation slideDown(Node node) {
        return FxAnimation.slideDown(node);
    }
    
    public FxAnimation slideLeft(Node node) {
        return FxAnimation.slideLeft(node);
    }
    
    public FxAnimation slideRight(Node node) {
        return FxAnimation.slideRight(node);
    }
    
    public FxAnimation shake(Node node) {
        return FxAnimation.shake(node);
    }
    
    public FxAnimation pulse(Node node) {
        return FxAnimation.pulse(node);
    }
    
    public FxAnimation bounce(Node node) {
        return FxAnimation.bounce(node);
    }
}
