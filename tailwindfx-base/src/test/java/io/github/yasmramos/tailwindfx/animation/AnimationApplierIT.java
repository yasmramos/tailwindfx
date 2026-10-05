package io.github.yasmramos.tailwindfx.animation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.yasmramos.tailwindfx.TwStyle;
import io.github.yasmramos.tailwindfx.core.TokenRegistry;
import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Tests for {@link AnimationApplier} — requires JavaFX Application Thread.
 *
 * <p>Covers token recognition in {@link TokenRegistry} and the timeline that each {@code animate-*}
 * utility maps to.
 */
@DisplayName("AnimationApplier Tests")
public class AnimationApplierIT extends ApplicationTest {

  /** Registry slot used by AnimationApplier for token-driven animations. */
  private static final String SLOT = "loop";

  @Nested
  @DisplayName("Token Recognition")
  class TokenRecognitionTests {

    @ParameterizedTest
    @ValueSource(
        strings = {
          "animate-spin",
          "animate-pulse",
          "animate-bounce",
          "animate-ping",
          "animate-flash",
          "animate-shake",
          "animate-spin-slow"
        })
    @DisplayName("Should recognize supported animation tokens")
    void testRecognizesSupportedTokens(String token) {
      assertTrue(TokenRegistry.isAnimationToken(token), () -> "expected recognized: " + token);
    }

    @ParameterizedTest
    @ValueSource(strings = {"animate-nonexistent", "p-4", "bg-blue-500", "spin", ""})
    @DisplayName("Should reject non-animation tokens")
    void testRejectsNonAnimationTokens(String token) {
      assertFalse(TokenRegistry.isAnimationToken(token), () -> "expected rejected: " + token);
    }

    @Test
    @DisplayName("Should reject null tokens")
    void testRejectsNull() {
      assertFalse(TokenRegistry.isAnimationToken(null));
    }

    @Test
    @DisplayName("Should expose animation names without the prefix")
    void testExposesAnimationNames() {
      assertTrue(TokenRegistry.getAnimationNames().contains("spin"));
      assertFalse(
          TokenRegistry.getAnimationNames().contains("animate-spin"),
          "names should be exposed without the animate- prefix");
    }
  }

  @Nested
  @DisplayName("Application")
  class ApplicationTests {

    @ParameterizedTest
    @ValueSource(
        strings = {
          "animate-spin",
          "animate-pulse",
          "animate-bounce",
          "animate-ping",
          "animate-flash",
          "animate-shake",
          "animate-spin-slow",
          "animate-pulse-slow",
          "animate-bounce-slow"
        })
    @DisplayName("Should start a timeline in the loop slot for supported tokens")
    void testStartsTimeline(String token) {
      interact(
          () -> {
            Region node = new Region();
            AnimationApplier.applyAnimationToken(node, token);

            assertTrue(
                TwAnimation.AnimationRegistry.isActive(node, SLOT),
                () -> "expected active animation for: " + token);

            // Re-applying must cancel the previous timeline instead of stacking a second one on
            // the same node properties.
            AnimationApplier.applyAnimationToken(node, token);
            assertTrue(TwAnimation.AnimationRegistry.isActive(node, SLOT));
          });
    }

    @Test
    @DisplayName("Should record the animation play in metrics")
    void testRecordsMetrics() {
      interact(
          () -> {
            long before = TailwindFXMetrics.instance().animationPlays();
            AnimationApplier.applyAnimationToken(new Region(), "animate-spin");
            long after = TailwindFXMetrics.instance().animationPlays();

            assertTrue(after > before, "expected animation play to be recorded in metrics");
          });
    }

    @Test
    @DisplayName("Should ignore unknown animation tokens without throwing")
    void testIgnoresUnknownToken() {
      interact(
          () -> {
            Region node = new Region();
            assertDoesNotThrow(() -> AnimationApplier.applyAnimationToken(node, "animate-nonexistent"));
            assertFalse(TwAnimation.AnimationRegistry.isActive(node, SLOT));
          });
    }

    @Test
    @DisplayName("Should ignore null and blank tokens")
    void testIgnoresNullAndBlank() {
      interact(
          () -> {
            Region node = new Region();
            assertDoesNotThrow(() -> AnimationApplier.applyAnimationToken(node, null));
            assertDoesNotThrow(() -> AnimationApplier.applyAnimationToken(node, "   "));
            assertFalse(TwAnimation.AnimationRegistry.isActive(node, SLOT));
          });
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null node")
    void testNullNodeThrows() {
      assertThrows(
          IllegalArgumentException.class, () -> AnimationApplier.applyAnimationToken(null, "animate-spin"));
    }
  }

  @Nested
  @DisplayName("TwStyle Integration")
  class TwStyleIntegrationTests {

    @Test
    @DisplayName("TwStyle.apply should play animate-* tokens")
    void testTwStylePlaysAnimations() {
      interact(
          () -> {
            Region node = new Region();
            TwStyle.apply(node, "animate-spin");

            assertTrue(
                TwAnimation.AnimationRegistry.isActive(node, SLOT),
                "TwStyle.apply should route animate-* to AnimationApplier");
          });
    }

    @Test
    @DisplayName("TwStyle.apply should not add animate-* as a CSS class")
    void testTwStyleDoesNotAddCssClass() {
      interact(
          () -> {
            Region node = new Region();
            TwStyle.apply(node, "animate-spin");

            assertFalse(
                node.getStyleClass().contains("animate-spin"),
                "animate-* must not be applied as a CSS class: JavaFX cannot animate it that way");
          });
    }

    @Test
    @DisplayName("TwStyle.apply should keep applying sibling tokens alongside animate-*")
    void testTwStyleAppliesSiblings() {
      interact(
          () -> {
            Region node = new Region();
            TwStyle.apply(node, "animate-pulse", "p-4");

            assertTrue(TwAnimation.AnimationRegistry.isActive(node, SLOT));
            assertTrue(
                node.getStyleClass().contains("p-4"),
                "non-animation tokens must still be classified normally");
          });
    }
  }

  @Nested
  @DisplayName("Cancellation")
  class CancellationTests {

    @Test
    @DisplayName("Should cancel the animation when the node is detached from the scene")
    void testCancelsOnSceneRemoval() {
      interact(
          () -> {
            Region node = new Region();
            StackPane root = new StackPane(node);
            Scene scene = new Scene(root, 200, 200);

            AnimationApplier.applyAnimationToken(node, "animate-spin");
            assertTrue(
                TwAnimation.AnimationRegistry.isActive(node, SLOT), "animation should be running");

            // Removing the node from the scene graph clears its scene, which the registry listens
            // for: a detached node must not keep a running Timeline alive.
            root.getChildren().remove(node);

            assertFalse(
                TwAnimation.AnimationRegistry.isActive(node, SLOT),
                "animation should be cancelled once the node leaves the scene");
          });
    }
  }
}