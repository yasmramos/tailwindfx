package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Unit tests for TransitionProcessor. */
class TransitionProcessorTest {

  @Test
  void testIsTransitionToken() {
    // Transition properties
    assertTrue(TransitionProcessor.isTransitionToken("transition-none"));
    assertTrue(TransitionProcessor.isTransitionToken("transition-all"));
    assertTrue(TransitionProcessor.isTransitionToken("transition-colors"));
    assertTrue(TransitionProcessor.isTransitionToken("transition-opacity"));
    assertTrue(TransitionProcessor.isTransitionToken("transition-transform"));

    // Duration
    assertTrue(TransitionProcessor.isTransitionToken("duration-75"));
    assertTrue(TransitionProcessor.isTransitionToken("duration-300"));
    assertTrue(TransitionProcessor.isTransitionToken("duration-[500]"));

    // Easing
    assertTrue(TransitionProcessor.isTransitionToken("ease-linear"));
    assertTrue(TransitionProcessor.isTransitionToken("ease-in"));
    assertTrue(TransitionProcessor.isTransitionToken("ease-out"));
    assertTrue(TransitionProcessor.isTransitionToken("ease-in-out"));

    // Animations
    assertTrue(TransitionProcessor.isTransitionToken("animate-spin"));
    assertTrue(TransitionProcessor.isTransitionToken("animate-pulse"));
    assertTrue(TransitionProcessor.isTransitionToken("animate-bounce"));

    // Non-transition tokens
    assertFalse(TransitionProcessor.isTransitionToken("p-4"));
    assertFalse(TransitionProcessor.isTransitionToken("bg-blue-500"));
    assertFalse(TransitionProcessor.isTransitionToken(null));
    assertFalse(TransitionProcessor.isTransitionToken(""));
  }

  @Test
  void testProcessTransitionNone() {
    TransitionProcessor.TransitionResult result =
        TransitionProcessor.processTransition("transition-none");
    assertNull(result);
  }

  @Test
  void testProcessTransitionAll() {
    TransitionProcessor.TransitionResult result =
        TransitionProcessor.processTransition("transition-all");
    assertNull(result);
  }

  @Test
  void testProcessTransitionColors() {
    TransitionProcessor.TransitionResult result =
        TransitionProcessor.processTransition("transition-colors");
    assertNull(result);
  }

  @Test
  void testProcessTransitionOpacity() {
    TransitionProcessor.TransitionResult result =
        TransitionProcessor.processTransition("transition-opacity");
    assertNull(result);
  }

  @Test
  void testProcessTransitionTransform() {
    TransitionProcessor.TransitionResult result =
        TransitionProcessor.processTransition("transition-transform");
    assertNull(result);
  }

  @Test
  void testProcessDuration() {
    String[] durations = {"75", "100", "150", "200", "300", "500", "700", "1000"};

    for (int i = 0; i < durations.length; i++) {
      TransitionProcessor.TransitionResult result =
          TransitionProcessor.processTransition("duration-" + durations[i]);
      assertNull(result, "Failed for duration-" + durations[i]);
    }
  }

  @Test
  void testProcessDurationArbitrary() {
    TransitionProcessor.TransitionResult result =
        TransitionProcessor.processTransition("duration-[450]");
    assertNull(result);
  }

  @Test
  void testProcessEasing() {
    String[] easings = {"linear", "in", "out", "in-out"};

    for (int i = 0; i < easings.length; i++) {
      TransitionProcessor.TransitionResult result =
          TransitionProcessor.processTransition("ease-" + easings[i]);
      assertNull(result, "Failed for ease-" + easings[i]);
    }
  }

  @Test
  void testProcessAnimation() {
    String[] animations = {"spin", "pulse", "bounce", "ping"};

    for (String animation : animations) {
      TransitionProcessor.TransitionResult result =
          TransitionProcessor.processTransition("animate-" + animation);
      assertNotNull(result, "Failed for animate-" + animation);
      assertEquals("animate", result.type());
      assertEquals(animation, result.easingOrAnimation());
      assertTrue(result.inlineStyle().contains("animate-" + animation));
      assertTrue(result.inlineStyle().contains("TwAnimation"));
    }
  }

  @Test
  void testProcessInvalidToken() {
    TransitionProcessor.TransitionResult result =
        TransitionProcessor.processTransition("invalid-token");
    assertNull(result);
  }

  @Test
  void testProcessNullToken() {
    TransitionProcessor.TransitionResult result = TransitionProcessor.processTransition(null);
    assertNull(result);
  }

  @Test
  void testProcessEmptyToken() {
    TransitionProcessor.TransitionResult result = TransitionProcessor.processTransition("");
    assertNull(result);
  }

  @Test
  void testTransitionWithImportantModifier() {
    TransitionProcessor.TransitionResult result =
        TransitionProcessor.processTransition("transition-all!");
    assertNull(result);
  }

  @Test
  void testTransitionWithHoverVariant() {
    TransitionProcessor.TransitionResult result =
        TransitionProcessor.processTransition("hover:transition-colors");
    assertNull(result);
  }

  @Test
  void testTransitionWithResponsivePrefix() {
    TransitionProcessor.TransitionResult result =
        TransitionProcessor.processTransition("md:duration-300");
    assertNull(result);
  }

  @Test
  void testRecordEquality() {
    TransitionProcessor.TransitionResult result1 =
        new TransitionProcessor.TransitionResult("-fx-opacity;", "transition", null, null);
    TransitionProcessor.TransitionResult result2 =
        new TransitionProcessor.TransitionResult("-fx-opacity;", "transition", null, null);

    assertEquals(result1, result2);
    assertEquals(result1.hashCode(), result2.hashCode());
  }

  @Test
  void testRecordToString() {
    TransitionProcessor.TransitionResult result =
        new TransitionProcessor.TransitionResult("-fx-opacity;", "transition", 300, "ease-out");

    String str = result.toString();
    assertTrue(str.contains("TransitionResult"));
    assertTrue(str.contains("-fx-opacity;"));
    assertTrue(str.contains("transition"));
  }
}
