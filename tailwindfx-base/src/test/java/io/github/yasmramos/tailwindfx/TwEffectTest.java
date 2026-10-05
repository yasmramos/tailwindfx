package io.github.yasmramos.tailwindfx;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.Node;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for TwEffect class. */
@DisplayName("TwEffect Tests")
class TwEffectTest {

  private Node testNode;

  @BeforeEach
  void setUp() {
    testNode = new Rectangle(100, 100);
  }

  @Test
  @DisplayName("should apply backdrop blur with given radius (legacy method)")
  void shouldApplyBackdropBlur() {
    // Given
    double radius = 8.0;

    // When
    TwEffect.backdropBlur(testNode, radius);

    // Then - backdropBlur is now deprecated and delegates to blur(), which uses GaussianBlur
    Effect effect = testNode.getEffect();
    assertNotNull(effect, "Effect should be applied");
    assertTrue(effect instanceof GaussianBlur, "Effect should be GaussianBlur");

    GaussianBlur gaussianBlur = (GaussianBlur) effect;
    assertEquals(radius, gaussianBlur.getRadius(), 0.01, "Radius should match");
  }

  @Test
  @DisplayName("should apply backdrop blur with zero radius (legacy method)")
  void shouldApplyBackdropBlurWithZeroRadius() {
    // Given
    double radius = 0.0;

    // When
    TwEffect.backdropBlur(testNode, radius);

    // Then - zero radius removes the effect
    Effect effect = testNode.getEffect();
    assertNull(effect, "Effect should be null for zero radius");
  }

  @Test
  @DisplayName("should apply backdrop blur with large radius (legacy method)")
  void shouldApplyBackdropBlurWithLargeRadius() {
    // Given
    double radius = 50.0;

    // When
    TwEffect.backdropBlur(testNode, radius);

    // Then
    Effect effect = testNode.getEffect();
    assertNotNull(effect, "Effect should be applied");
    assertTrue(effect instanceof GaussianBlur, "Effect should be GaussianBlur");

    GaussianBlur gaussianBlur = (GaussianBlur) effect;
    assertEquals(radius, gaussianBlur.getRadius(), 0.01, "Radius should match large radius");
  }

  @Test
  @DisplayName("should remove backdrop blur effect (legacy method)")
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
  @DisplayName("should remove effect even if none was applied (legacy method)")
  void shouldRemoveEffectWhenNoneApplied() {
    // Given
    assertNull(testNode.getEffect(), "No effect initially");

    // When
    TwEffect.backdropBlurNone(testNode);

    // Then
    assertNull(testNode.getEffect(), "Effect should still be null");
  }

  @Test
  @DisplayName("should overwrite previous effect with new backdrop blur (legacy method)")
  void shouldOverwritePreviousEffect() {
    // Given
    BoxBlur previousEffect = new BoxBlur(5, 5, 1);
    testNode.setEffect(previousEffect);

    // When
    TwEffect.backdropBlur(testNode, 15.0);

    // Then
    Effect effect = testNode.getEffect();
    assertNotSame(previousEffect, effect, "Should be a new effect instance");
    assertTrue(effect instanceof GaussianBlur, "Effect should be GaussianBlur");

    GaussianBlur gaussianBlur = (GaussianBlur) effect;
    assertEquals(15.0, gaussianBlur.getRadius(), 0.01, "Radius should match new radius");
  }

  @Test
  @DisplayName("should apply different blur radii sequentially (legacy method)")
  void shouldApplyDifferentBlurRadiiSequentially() {
    // Given
    double radius1 = 5.0;
    double radius2 = 20.0;

    // When
    TwEffect.backdropBlur(testNode, radius1);
    TwEffect.backdropBlur(testNode, radius2);

    // Then
    Effect effect = testNode.getEffect();
    assertTrue(effect instanceof GaussianBlur, "Effect should be GaussianBlur");

    GaussianBlur gaussianBlur = (GaussianBlur) effect;
    assertEquals(radius2, gaussianBlur.getRadius(), 0.01, "Radius should match last radius");
  }

  @Test
  @DisplayName("should handle fractional blur radius (legacy method)")
  void shouldHandleFractionalBlurRadius() {
    // Given
    double radius = 7.5;

    // When
    TwEffect.backdropBlur(testNode, radius);

    // Then
    Effect effect = testNode.getEffect();
    assertNotNull(effect, "Effect should be applied");
    assertTrue(effect instanceof GaussianBlur, "Effect should be GaussianBlur");

    GaussianBlur gaussianBlur = (GaussianBlur) effect;
    assertEquals(radius, gaussianBlur.getRadius(), 0.01, "Radius should match fractional radius");
  }

  @Test
  @DisplayName("backdropBlurNone should work after multiple blur applications (legacy method)")
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

  @Test
  @DisplayName("should apply blur with GaussianBlur")
  void shouldApplyBlur() {
    // Given
    double radius = 8.0;

    // When
    TwEffect.blur(testNode, radius);

    // Then
    Effect effect = testNode.getEffect();
    assertNotNull(effect, "Effect should be applied");
    assertTrue(effect instanceof GaussianBlur, "Effect should be GaussianBlur");

    GaussianBlur gaussianBlur = (GaussianBlur) effect;
    assertEquals(radius, gaussianBlur.getRadius(), 0.01, "Radius should match");
  }

  @Test
  @DisplayName("should remove blur effect")
  void shouldRemoveBlur() {
    // Given
    TwEffect.blur(testNode, 10.0);
    assertNotNull(testNode.getEffect(), "Effect should be applied initially");

    // When
    TwEffect.blurNone(testNode);

    // Then
    assertNull(testNode.getEffect(), "Effect should be removed");
  }

  @Test
  @DisplayName("should apply brightness effect")
  void shouldApplyBrightness() {
    // Given
    double value = 1.25;

    // When
    TwEffect.brightness(testNode, value);

    // Then
    Effect effect = testNode.getEffect();
    assertNotNull(effect, "Effect should be applied");
    assertTrue(effect instanceof javafx.scene.effect.ColorAdjust, "Effect should be ColorAdjust");

    javafx.scene.effect.ColorAdjust adjust = (javafx.scene.effect.ColorAdjust) effect;
    assertEquals(value - 1.0, adjust.getBrightness(), 0.01, "Brightness should match");
  }

  @Test
  @DisplayName("should apply grayscale effect")
  void shouldApplyGrayscale() {
    // When
    TwEffect.grayscale(testNode);

    // Then
    Effect effect = testNode.getEffect();
    assertNotNull(effect, "Effect should be applied");
    assertTrue(effect instanceof javafx.scene.effect.ColorAdjust, "Effect should be ColorAdjust");

    javafx.scene.effect.ColorAdjust adjust = (javafx.scene.effect.ColorAdjust) effect;
    assertEquals(-1.0, adjust.getSaturation(), 0.01, "Saturation should be -1.0 (grayscale)");
  }

  @Test
  @DisplayName("should apply sepia effect")
  void shouldApplySepia() {
    // When
    TwEffect.sepia(testNode);

    // Then
    Effect effect = testNode.getEffect();
    assertNotNull(effect, "Effect should be applied");
    assertTrue(effect instanceof javafx.scene.effect.ColorAdjust, "Effect should be ColorAdjust");

    javafx.scene.effect.ColorAdjust adjust = (javafx.scene.effect.ColorAdjust) effect;
    assertTrue(adjust.getSaturation() < 0, "Saturation should be reduced");
    assertTrue(adjust.getHue() > 0, "Hue should be shifted");
  }

  @Test
  @DisplayName("should apply invert effect")
  void shouldApplyInvert() {
    // When
    TwEffect.invert(testNode);

    // Then
    Effect effect = testNode.getEffect();
    assertNotNull(effect, "Effect should be applied");
    assertTrue(effect instanceof javafx.scene.effect.ColorAdjust, "Effect should be ColorAdjust");

    javafx.scene.effect.ColorAdjust adjust = (javafx.scene.effect.ColorAdjust) effect;
    assertEquals(Math.PI, adjust.getHue(), 0.01, "Hue should be PI (180 degrees)");
  }

  @Test
  @DisplayName("should apply contrast effect")
  void shouldApplyContrast() {
    // Given
    double value = 1.1;

    // When
    TwEffect.contrast(testNode, value);

    // Then
    Effect effect = testNode.getEffect();
    assertNotNull(effect, "Effect should be applied");
    assertTrue(effect instanceof javafx.scene.effect.ColorAdjust, "Effect should be ColorAdjust");

    javafx.scene.effect.ColorAdjust adjust = (javafx.scene.effect.ColorAdjust) effect;
    assertEquals(value - 1.0, adjust.getContrast(), 0.01, "Contrast should match");
  }

  @Test
  @DisplayName("should chain effects properly")
  void shouldChainEffects() {
    // Given - apply brightness first
    TwEffect.brightness(testNode, 1.2);

    // When - apply blur (should chain)
    TwEffect.blur(testNode, 4.0);

    // Then - should have GaussianBlur with ColorAdjust as input
    Effect effect = testNode.getEffect();
    assertNotNull(effect, "Effect should be applied");
    assertTrue(effect instanceof GaussianBlur, "Final effect should be GaussianBlur");

    GaussianBlur blur = (GaussianBlur) effect;
    assertNotNull(blur.getInput(), "Blur should have an input effect chained");
  }
}
