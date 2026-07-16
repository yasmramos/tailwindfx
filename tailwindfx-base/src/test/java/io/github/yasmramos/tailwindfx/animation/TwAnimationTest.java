package io.github.yasmramos.tailwindfx.animation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Tests for {@link TwAnimation} — requires JavaFX Application Thread.
 *
 * <p>Each test runs on the FX thread via {@code runFx()} and blocks until done.
 */
@DisplayName("TwAnimation Tests")
public class TwAnimationTest extends ApplicationTest {

  private static final double DELTA = 0.5;

  @BeforeAll
  static void setupSpec() {
    // Ensure JavaFX is initialized
  }

  /** Runs work on FX thread and blocks until done (max 3s). */
  static void runFx(Runnable work) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Throwable> err = new AtomicReference<>();
    Platform.runLater(
        () -> {
          try {
            work.run();
          } catch (Throwable t) {
            err.set(t);
          } finally {
            latch.countDown();
          }
        });
    if (!latch.await(3, TimeUnit.SECONDS)) {
      throw new RuntimeException("FX test timed out");
    }
    if (err.get() != null) {
      throw new RuntimeException(err.get());
    }
  }

  @Nested
  @DisplayName("Null Guards and Validation")
  class NullGuardsTests {

    @Test
    @DisplayName("fadeIn should throw IllegalArgumentException for null node")
    void testFadeInNullNode() {
      assertThrows(IllegalArgumentException.class, () -> TwAnimation.fadeIn(null));
    }

    @Test
    @DisplayName("fadeOut should throw IllegalArgumentException for null node")
    void testFadeOutNullNode() {
      assertThrows(IllegalArgumentException.class, () -> TwAnimation.fadeOut(null));
    }

    @Test
    @DisplayName("shake should throw IllegalArgumentException for null node")
    void testShakeNullNode() {
      assertThrows(IllegalArgumentException.class, () -> TwAnimation.shake(null));
    }

    @Test
    @DisplayName("spin should throw IllegalArgumentException for null node")
    void testSpinNullNode() {
      assertThrows(IllegalArgumentException.class, () -> TwAnimation.spin(null));
    }

    @Test
    @DisplayName("onHoverScale should throw IllegalArgumentException for null node")
    void testOnHoverScaleNullNode() {
      assertThrows(IllegalArgumentException.class, () -> TwAnimation.onHoverScale(null, 1.05));
    }

    @Test
    @DisplayName("fadeIn should throw IllegalArgumentException for zero duration")
    void testFadeInZeroDuration() {
      assertThrows(IllegalArgumentException.class, () -> TwAnimation.fadeIn(new Region(), 0));
    }

    @Test
    @DisplayName("fadeIn should throw IllegalArgumentException for negative duration")
    void testFadeInNegativeDuration() {
      assertThrows(IllegalArgumentException.class, () -> TwAnimation.fadeIn(new Region(), -1));
    }

    @Test
    @DisplayName("fadeIn with interpolator should throw IllegalArgumentException for null interpolator")
    void testFadeInNullInterpolator() {
      assertThrows(
          IllegalArgumentException.class, () -> TwAnimation.fadeIn(new Region(), 100, null));
    }

    @Test
    @DisplayName("onHoverScale should throw IllegalArgumentException for zero scale factor")
    void testOnHoverScaleZeroFactor() {
      assertThrows(IllegalArgumentException.class, () -> TwAnimation.onHoverScale(new Region(), 0));
    }
  }

  @Nested
  @DisplayName("Entry Animations")
  class EntryAnimationsTests {

    @Test
    @DisplayName("fadeIn should create Timeline and reset opacity to 0")
    void testFadeInCreatesTimeline() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            TwAnimation anim = TwAnimation.fadeIn(n, 100);
            assertNotNull(anim, "fadeIn should return non-null TwAnimation");
            assertNotNull(anim.raw(), "fadeIn raw() should be non-null Animation");
            assertEquals(0.0, n.getOpacity(), DELTA, "node opacity should be reset to 0");
          });
    }

    @Test
    @DisplayName("fadeIn with custom interpolator should work correctly")
    void testFadeInWithInterpolator() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            TwAnimation anim = TwAnimation.fadeIn(n, 200, Interpolator.LINEAR);
            assertNotNull(anim, "fadeIn(interpolator) should return non-null");
          });
    }

    @Test
    @DisplayName("fadeOut should create Timeline")
    void testFadeOutCreatesTimeline() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            n.setOpacity(1.0);
            TwAnimation anim = TwAnimation.fadeOut(n, 100);
            assertNotNull(anim, "fadeOut should return non-null");
          });
    }

    @Test
    @DisplayName("slideUp should create Timeline and reset opacity")
    void testSlideUpCreatesTimeline() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            TwAnimation anim = TwAnimation.slideUp(n);
            assertNotNull(anim, "slideUp should return non-null");
            assertEquals(0.0, n.getOpacity(), DELTA, "opacity should be reset to 0");
          });
    }

    @Test
    @DisplayName("scaleIn should create Timeline and reset scale")
    void testScaleInCreatesTimeline() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            TwAnimation anim = TwAnimation.scaleIn(n);
            assertNotNull(anim, "scaleIn should return non-null");
            assertEquals(0.85, n.getScaleX(), DELTA, "scaleX should be reset to 0.85");
          });
    }

    @Test
    @DisplayName("scaleIn with custom interpolator should work correctly")
    void testScaleInWithInterpolator() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            TwAnimation a = TwAnimation.scaleIn(n, 150, Interpolator.EASE_IN);
            assertNotNull(a, "scaleIn(interpolator) should return non-null");
          });
    }
  }

  @Nested
  @DisplayName("Attention Animations")
  class AttentionAnimationsTests {

    @Test
    @DisplayName("shake should create Timeline")
    void testShakeCreatesTimeline() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            TwAnimation a = TwAnimation.shake(n);
            assertNotNull(a, "shake should return non-null");
          });
    }

    @Test
    @DisplayName("bounce should create Timeline")
    void testBounceCreatesTimeline() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            TwAnimation a = TwAnimation.bounce(n);
            assertNotNull(a, "bounce should return non-null");
          });
    }

    @Test
    @DisplayName("pulse should have INDEFINITE cycle count")
    void testPulseIsInfinite() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            TwAnimation a = TwAnimation.pulse(n);
            assertEquals(
                Animation.INDEFINITE,
                a.raw().getCycleCount(),
                "pulse cycleCount should be INDEFINITE");
          });
    }

    @Test
    @DisplayName("spin should have INDEFINITE cycle count")
    void testSpinIsInfinite() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            TwAnimation a = TwAnimation.spin(n);
            assertEquals(
                Animation.INDEFINITE,
                a.raw().getCycleCount(),
                "spin cycleCount should be INDEFINITE");
          });
    }

    @Test
    @DisplayName("breathe should have INDEFINITE cycle count")
    void testBreatheIsInfinite() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            TwAnimation a = TwAnimation.breathe(n);
            assertEquals(
                Animation.INDEFINITE,
                a.raw().getCycleCount(),
                "breathe cycleCount should be INDEFINITE");
          });
    }
  }

  @Nested
  @DisplayName("Animation Composition")
  class CompositionTests {

    @Test
    @DisplayName("chain should create SequentialTransition")
    void testChainSequential() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            TwAnimation a1 = TwAnimation.fadeIn(n, 50);
            TwAnimation a2 = TwAnimation.fadeOut(n, 50);
            TwAnimation chain = TwAnimation.chain(a1, a2);
            assertNotNull(chain, "chain should return non-null");
            assertNotNull(chain.raw(), "chain raw should return non-null");
          });
    }

    @Test
    @DisplayName("parallel should create ParallelTransition")
    void testParallelTransition() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            TwAnimation a1 = TwAnimation.fadeIn(n, 50);
            TwAnimation a2 = TwAnimation.scaleIn(n, 50);
            TwAnimation par = TwAnimation.parallel(a1, a2);
            assertNotNull(par, "parallel should return non-null");
          });
    }

    @Test
    @DisplayName("pause should create delay Timeline")
    void testDelayCreatesTimeline() throws Exception {
      runFx(
          () -> {
            TwAnimation d = TwAnimation.pause(200);
            assertNotNull(d, "delay should return non-null");
          });
    }
  }

  @Nested
  @DisplayName("Fluent API")
  class FluentApiTests {

    @Test
    @DisplayName("speed should set animation rate")
    void testTwAnimationSpeed() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            TwAnimation a = TwAnimation.fadeIn(n, 200).speed(2.0);
            assertEquals(2.0, a.raw().getRate(), DELTA, "speed should be 2.0");
          });
    }

    @Test
    @DisplayName("cycleCount should set animation cycle count")
    void testTwAnimationCycleCount() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            TwAnimation a = TwAnimation.fadeIn(n, 200).cycleCount(3);
            assertEquals(3, a.raw().getCycleCount(), "cycleCount should be 3");
          });
    }

    @Test
    @DisplayName("autoReverse should enable auto reverse")
    void testTwAnimationAutoReverse() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            TwAnimation a = TwAnimation.fadeIn(n, 200).autoReverse();
            assertTrue(a.raw().isAutoReverse(), "autoReverse should be true");
          });
    }

    @Test
    @DisplayName("onFinished should set handler")
    void testTwAnimationOnFinished() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            AtomicBoolean fired = new AtomicBoolean(false);
            TwAnimation a = TwAnimation.fadeIn(n, 1).onFinished(e -> fired.set(true));
            assertNotNull(a.raw().getOnFinished(), "onFinished handler should be set");
          });
    }

    @Test
    @DisplayName("easeIn should return self")
    void testTwAnimationEaseIn() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            TwAnimation a = TwAnimation.fadeIn(n, 200).easeIn();
            assertNotNull(a, "easeIn should return self");
          });
    }
  }

  @Nested
  @DisplayName("Animation Registry")
  class AnimationRegistryTests {

    @Test
    @DisplayName("registry should support slot isolation")
    void testRegistrySlotIsolation() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            TwAnimation enter = TwAnimation.fadeIn(n, 50);
            TwAnimation loop = TwAnimation.spin(n).loop();
            // Different slots — both should coexist
            TwAnimation.AnimationRegistry.play(n, "enter", enter.raw());
            TwAnimation.AnimationRegistry.play(n, "loop", loop.raw());
            assertTrue(TwAnimation.AnimationRegistry.isActive(n, "enter"), "enter should be active");
            assertTrue(TwAnimation.AnimationRegistry.isActive(n, "loop"), "loop should be active");
            TwAnimation.AnimationRegistry.cancelAll(n);
          });
    }

    @Test
    @DisplayName("registry should cancel previous animation when replacing")
    void testRegistryReplacesCancels() throws Exception {
      runFx(
          () -> {
            try {
              Region n = new Region();
              javafx.animation.Timeline t1 = new javafx.animation.Timeline(
                  new javafx.animation.KeyFrame(
                      javafx.util.Duration.millis(1000),
                      e -> {}
                  )
              );
              javafx.animation.Timeline t2 = new javafx.animation.Timeline(
                  new javafx.animation.KeyFrame(
                      javafx.util.Duration.millis(1000),
                      e -> {}
                  )
              );
              TwAnimation.AnimationRegistry.play(n, "enter", t1);
              // Give it a moment to start
              Thread.sleep(50);
              assertEquals(Animation.Status.RUNNING, t1.getStatus(), "t1 should be running after play()");
              TwAnimation.AnimationRegistry.play(n, "enter", t2);
              // Give it a moment to stop
              Thread.sleep(50);
              assertEquals(Animation.Status.STOPPED, t1.getStatus(), "t1 should be stopped after replace");
              assertEquals(Animation.Status.RUNNING, t2.getStatus(), "t2 should be running after play()");
              TwAnimation.AnimationRegistry.cancelAll(n);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              throw new RuntimeException(e);
            }
          });
    }
  }

  @Nested
  @DisplayName("Hover Effects")
  class HoverEffectsTests {

    @Test
    @DisplayName("removeHoverEffects should not throw on clean node")
    void testRemoveHoverEffectsNoop() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            // removeHoverEffects on a node with no hover effects should not throw
            TwAnimation.removeHoverEffects(n);
          });
    }
  }

  @Nested
  @DisplayName("Responsive Animation Guard")
  class ResponsiveGuardTests {

    @Test
    @DisplayName("resetNode should reset all transforms to defaults")
    void testResponsiveGuardResetNode() throws Exception {
      runFx(
          () -> {
            Region n = new Region();
            n.setTranslateX(20);
            n.setTranslateY(-10);
            n.setScaleX(1.5);
            n.setOpacity(0.3);
            TwAnimation.ResponsiveAnimationGuard.resetNode(n);
            assertEquals(0, n.getTranslateX(), DELTA, "translateX should be 0");
            assertEquals(0, n.getTranslateY(), DELTA, "translateY should be 0");
            assertEquals(1, n.getScaleX(), DELTA, "scaleX should be 1");
            assertEquals(1, n.getOpacity(), DELTA, "opacity should be 1");
          });
    }
  }
}
