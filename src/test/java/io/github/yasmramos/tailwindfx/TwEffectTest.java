package io.github.yasmramos.tailwindfx;

import javafx.scene.Node;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TwEffect class.
 */
@DisplayName("TwEffect Tests")
class TwEffectTest {

    private Node testNode;

    @BeforeEach
    void setUp() {
        testNode = new Rectangle(100, 100);
    }

    @Test
    @DisplayName("should apply backdrop blur with given radius")
    void shouldApplyBackdropBlur() {
        // Given
        double radius = 8.0;

        // When
        TwEffect.backdropBlur(testNode, radius);

        // Then
        Effect effect = testNode.getEffect();
        assertNotNull(effect, "Effect should be applied");
        assertTrue(effect instanceof BoxBlur, "Effect should be BoxBlur");
        
        BoxBlur boxBlur = (BoxBlur) effect;
        assertEquals(radius, boxBlur.getWidth(), 0.01, "Width should match radius");
        assertEquals(radius, boxBlur.getHeight(), 0.01, "Height should match radius");
        assertEquals(1, boxBlur.getIterations(), "Iterations should be 1");
    }

    @Test
    @DisplayName("should apply backdrop blur with zero radius")
    void shouldApplyBackdropBlurWithZeroRadius() {
        // Given
        double radius = 0.0;

        // When
        TwEffect.backdropBlur(testNode, radius);

        // Then
        Effect effect = testNode.getEffect();
        assertNotNull(effect, "Effect should be applied even with zero radius");
        assertTrue(effect instanceof BoxBlur, "Effect should be BoxBlur");
        
        BoxBlur boxBlur = (BoxBlur) effect;
        assertEquals(0.0, boxBlur.getWidth(), 0.01, "Width should be zero");
        assertEquals(0.0, boxBlur.getHeight(), 0.01, "Height should be zero");
    }

    @Test
    @DisplayName("should apply backdrop blur with large radius")
    void shouldApplyBackdropBlurWithLargeRadius() {
        // Given
        double radius = 50.0;

        // When
        TwEffect.backdropBlur(testNode, radius);

        // Then
        Effect effect = testNode.getEffect();
        assertNotNull(effect, "Effect should be applied");
        assertTrue(effect instanceof BoxBlur, "Effect should be BoxBlur");
        
        BoxBlur boxBlur = (BoxBlur) effect;
        assertEquals(radius, boxBlur.getWidth(), 0.01, "Width should match large radius");
        assertEquals(radius, boxBlur.getHeight(), 0.01, "Height should match large radius");
    }

    @Test
    @DisplayName("should remove backdrop blur effect")
    void shouldRemoveBackdropBlur() {
        // Given
        TwEffect.backdropBlur(testNode, 10.0);
        assertNotNull(testNode.getEffect(), "Effect should be applied initially");

        // When
        TwEffect.backdropBlurNone(testNode);

        // Then
        assertNull(testNode.getEffect(), "Effect should be removed");
    }

    @Test
    @DisplayName("should remove effect even if none was applied")
    void shouldRemoveEffectWhenNoneApplied() {
        // Given
        assertNull(testNode.getEffect(), "No effect initially");

        // When
        TwEffect.backdropBlurNone(testNode);

        // Then
        assertNull(testNode.getEffect(), "Effect should still be null");
    }

    @Test
    @DisplayName("should overwrite previous effect with new backdrop blur")
    void shouldOverwritePreviousEffect() {
        // Given
        BoxBlur previousEffect = new BoxBlur(5, 5, 1);
        testNode.setEffect(previousEffect);

        // When
        TwEffect.backdropBlur(testNode, 15.0);

        // Then
        Effect effect = testNode.getEffect();
        assertNotSame(previousEffect, effect, "Should be a new effect instance");
        assertTrue(effect instanceof BoxBlur, "Effect should be BoxBlur");
        
        BoxBlur boxBlur = (BoxBlur) effect;
        assertEquals(15.0, boxBlur.getWidth(), 0.01, "Width should match new radius");
    }

    @Test
    @DisplayName("should apply different blur radii sequentially")
    void shouldApplyDifferentBlurRadiiSequentially() {
        // Given
        double radius1 = 5.0;
        double radius2 = 20.0;

        // When
        TwEffect.backdropBlur(testNode, radius1);
        TwEffect.backdropBlur(testNode, radius2);

        // Then
        Effect effect = testNode.getEffect();
        assertTrue(effect instanceof BoxBlur, "Effect should be BoxBlur");
        
        BoxBlur boxBlur = (BoxBlur) effect;
        assertEquals(radius2, boxBlur.getWidth(), 0.01, "Width should match last radius");
        assertEquals(radius2, boxBlur.getHeight(), 0.01, "Height should match last radius");
    }

    @Test
    @DisplayName("should handle fractional blur radius")
    void shouldHandleFractionalBlurRadius() {
        // Given
        double radius = 7.5;

        // When
        TwEffect.backdropBlur(testNode, radius);

        // Then
        Effect effect = testNode.getEffect();
        assertNotNull(effect, "Effect should be applied");
        assertTrue(effect instanceof BoxBlur, "Effect should be BoxBlur");
        
        BoxBlur boxBlur = (BoxBlur) effect;
        assertEquals(radius, boxBlur.getWidth(), 0.01, "Width should match fractional radius");
        assertEquals(radius, boxBlur.getHeight(), 0.01, "Height should match fractional radius");
    }

    @Test
    @DisplayName("backdropBlurNone should work after multiple blur applications")
    void backdropBlurNoneAfterMultipleApplications() {
        // Given
        TwEffect.backdropBlur(testNode, 5.0);
        TwEffect.backdropBlur(testNode, 10.0);
        TwEffect.backdropBlur(testNode, 15.0);
        assertNotNull(testNode.getEffect(), "Effect should be applied");

        // When
        TwEffect.backdropBlurNone(testNode);

        // Then
        assertNull(testNode.getEffect(), "Effect should be removed after multiple applications");
    }
}
