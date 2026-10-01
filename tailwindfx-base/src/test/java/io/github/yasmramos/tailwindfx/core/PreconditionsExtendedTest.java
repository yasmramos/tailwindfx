package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Extended coverage tests for {@link Preconditions}. */
@DisplayName("Preconditions - Extended Coverage")
class PreconditionsExtendedTest {

  private final java.util.List<LogRecord> captured = new java.util.ArrayList<>();
  private final java.util.logging.Handler handler =
      new java.util.logging.Handler() {
        @Override
        public void publish(LogRecord record) {
          captured.add(record);
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}
      };

  @BeforeEach
  void attachHandler() {
    captured.clear();
    Logger logger = Preconditions.LOG;
    logger.setLevel(Level.ALL);
    logger.addHandler(handler);
  }

  @AfterEach
  void detachHandler() {
    Preconditions.LOG.removeHandler(handler);
  }

  @Nested
  @DisplayName("Node and Pane requirements")
  class NodeChecks {

    @Test
    @DisplayName("requireNode returns the same instance when non-null")
    void requireNodeReturnsSame() {
      javafx.scene.layout.StackPane node = new javafx.scene.layout.StackPane();
      assertEquals(node, Preconditions.requireNode(node, "test"));
    }

    @Test
    @DisplayName("requireNode throws on null with method context in message")
    void requireNodeNullThrows() {
      IllegalArgumentException ex =
          assertThrows(
              IllegalArgumentException.class, () -> Preconditions.requireNode(null, "Tw.apply"));
      assertTrue(ex.getMessage().contains("Tw.apply"), "message should include method name");
      assertTrue(ex.getMessage().contains("node"), "message should include parameter name");
    }

    @Test
    @DisplayName("requirePane returns the same instance when non-null")
    void requirePaneReturnsSame() {
      javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox();
      assertEquals(pane, Preconditions.requirePane(pane, "test"));
    }

    @Test
    @DisplayName("requirePane throws on null")
    void requirePaneNullThrows() {
      assertThrows(
          IllegalArgumentException.class, () -> Preconditions.requirePane(null, "Layout.grow"));
    }
  }

  @Nested
  @DisplayName("Span upper bound")
  class SpanUpperBound {

    @Test
    @DisplayName("span at maximum boundary (1000) is accepted")
    void spanAtMaxAccepted() {
      assertEquals(1000, Preconditions.requireSpan(1000, "Grid.span"));
    }

    @Test
    @DisplayName("span above maximum throws")
    void spanAboveMaxThrows() {
      IllegalArgumentException ex =
          assertThrows(
              IllegalArgumentException.class, () -> Preconditions.requireSpan(1001, "Grid.span"));
      assertTrue(
          ex.getMessage().contains("exceeds maximum"),
          "message should mention the maximum: " + ex.getMessage());
    }

    @Test
    @DisplayName("negative span throws")
    void negativeSpanThrows() {
      assertThrows(
          IllegalArgumentException.class, () -> Preconditions.requireSpan(-3, "Grid.span"));
    }
  }

  @Nested
  @DisplayName("Opacity boundaries")
  class OpacityBoundaries {

    @Test
    @DisplayName("opacity 0.0 is valid")
    void opacityZeroValid() {
      assertEquals(0.0, Preconditions.requireOpacity(0.0, "test"));
    }

    @Test
    @DisplayName("opacity 1.0 is valid")
    void opacityOneValid() {
      assertEquals(1.0, Preconditions.requireOpacity(1.0, "test"));
    }

    @Test
    @DisplayName("negative opacity throws")
    void negativeOpacityThrows() {
      assertThrows(
          IllegalArgumentException.class, () -> Preconditions.requireOpacity(-0.1, "test"));
    }

    @Test
    @DisplayName("opacity above 1.0 throws")
    void opacityAboveOneThrows() {
      assertThrows(
          IllegalArgumentException.class, () -> Preconditions.requireOpacity(1.01, "test"));
    }
  }

  @Nested
  @DisplayName("Duration boundaries")
  class DurationBoundaries {

    @Test
    @DisplayName("duration of 1 ms is valid")
    void minDurationValid() {
      assertEquals(1, Preconditions.requirePositiveDuration(1, "Anim.duration"));
    }

    @Test
    @DisplayName("zero duration throws")
    void zeroDurationThrows() {
      assertThrows(
          IllegalArgumentException.class, () -> Preconditions.requirePositiveDuration(0, "test"));
    }

    @Test
    @DisplayName("negative duration throws")
    void negativeDurationThrows() {
      assertThrows(
          IllegalArgumentException.class,
          () -> Preconditions.requirePositiveDuration(-500, "test"));
    }

    @Test
    @DisplayName("duration above maximum (600000 ms) throws")
    void durationAboveMaxThrows() {
      IllegalArgumentException ex =
          assertThrows(
              IllegalArgumentException.class,
              () -> Preconditions.requirePositiveDuration(600_001, "test"));
      assertTrue(ex.getMessage().contains("exceeds maximum"));
    }

    @Test
    @DisplayName("duration exactly at maximum is valid")
    void durationAtMaxValid() {
      assertEquals(600_000, Preconditions.requirePositiveDuration(600_000, "test"));
    }
  }

  @Nested
  @DisplayName("Scale boundaries")
  class ScaleBoundaries {

    @Test
    @DisplayName("scale at maximum (100.0) is valid")
    void scaleAtMaxValid() {
      assertEquals(100.0, Preconditions.requirePositiveScale(100.0, "test"));
    }

    @Test
    @DisplayName("scale above maximum throws")
    void scaleAboveMaxThrows() {
      assertThrows(
          IllegalArgumentException.class, () -> Preconditions.requirePositiveScale(100.1, "test"));
    }

    @Test
    @DisplayName("zero scale throws")
    void zeroScaleThrows() {
      assertThrows(
          IllegalArgumentException.class, () -> Preconditions.requirePositiveScale(0.0, "test"));
    }

    @Test
    @DisplayName("negative scale throws")
    void negativeScaleThrows() {
      assertThrows(
          IllegalArgumentException.class, () -> Preconditions.requirePositiveScale(-1.0, "test"));
    }
  }

  @Nested
  @DisplayName("Speed boundaries")
  class SpeedBoundaries {

    @Test
    @DisplayName("speed at maximum (100.0) is valid")
    void speedAtMaxValid() {
      assertEquals(100.0, Preconditions.requirePositiveSpeed(100.0, "test"));
    }

    @Test
    @DisplayName("speed above maximum throws")
    void speedAboveMaxThrows() {
      assertThrows(
          IllegalArgumentException.class, () -> Preconditions.requirePositiveSpeed(101.0, "test"));
    }

    @Test
    @DisplayName("non-positive speed throws")
    void nonPositiveSpeedThrows() {
      assertThrows(
          IllegalArgumentException.class, () -> Preconditions.requirePositiveSpeed(0.0, "test"));
      assertThrows(
          IllegalArgumentException.class, () -> Preconditions.requirePositiveSpeed(-2.5, "test"));
    }
  }

  @Nested
  @DisplayName("Alpha boundaries")
  class AlphaBoundaries {

    @Test
    @DisplayName("alpha 0 is valid")
    void alphaZeroValid() {
      assertEquals(0, Preconditions.requireAlpha(0, "Color.opacity"));
    }

    @Test
    @DisplayName("alpha 100 is valid")
    void alphaHundredValid() {
      assertEquals(100, Preconditions.requireAlpha(100, "Color.opacity"));
    }

    @Test
    @DisplayName("negative alpha throws")
    void negativeAlphaThrows() {
      assertThrows(IllegalArgumentException.class, () -> Preconditions.requireAlpha(-1, "test"));
    }

    @Test
    @DisplayName("alpha above 100 throws")
    void alphaAboveHundredThrows() {
      assertThrows(IllegalArgumentException.class, () -> Preconditions.requireAlpha(101, "test"));
    }
  }

  @Nested
  @DisplayName("Brightness warnings")
  class BrightnessWarnings {

    @Test
    @DisplayName("brightness within range logs nothing")
    void brightnessInRangeNoWarning() {
      assertDoesNotThrow(() -> Preconditions.warnBrightnessRange(1.5, "Filter.brightness"));
      assertTrue(captured.isEmpty(), "no warning expected for valid brightness");
    }

    @Test
    @DisplayName("negative brightness throws")
    void negativeBrightnessThrows() {
      assertThrows(
          IllegalArgumentException.class, () -> Preconditions.warnBrightnessRange(-0.5, "test"));
    }

    @Test
    @DisplayName("brightness above 2.0 logs a warning but does not throw")
    void highBrightnessWarns() {
      assertDoesNotThrow(() -> Preconditions.warnBrightnessRange(3.0, "Filter.brightness"));
      assertEquals(1, captured.size(), "exactly one warning should be logged");
      assertEquals(Level.WARNING, captured.get(0).getLevel());
      assertTrue(captured.get(0).getMessage().contains("brightness"));
    }
  }

  @Nested
  @DisplayName("Parent / image warnings")
  class ContextWarnings {

    @Test
    @DisplayName("warnNoParent logs when node has no parent")
    void warnNoParentLogs() {
      javafx.scene.layout.StackPane orphan = new javafx.scene.layout.StackPane();
      Preconditions.warnNoParent(orphan, "Margin.apply");
      assertEquals(1, captured.size());
      assertTrue(captured.get(0).getMessage().contains("no parent"));
    }

    @Test
    @DisplayName("warnNoParent stays silent when node has a parent")
    void warnNoParentSilentWithParent() {
      javafx.scene.layout.StackPane child = new javafx.scene.layout.StackPane();
      new javafx.scene.layout.HBox(child); // reparents child
      Preconditions.warnNoParent(child, "Margin.apply");
      assertTrue(captured.isEmpty(), "no warning expected when a parent exists");
    }

    @Test
    @DisplayName("warnNoImage logs for ImageView without an image")
    void warnNoImageLogs() {
      javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView();
      Preconditions.warnNoImage(iv, "Viewport.apply");
      assertEquals(1, captured.size());
      assertTrue(captured.get(0).getMessage().contains("no image"));
    }

    @Test
    @DisplayName("warnLikelyTypo always logs a warning naming the token")
    void warnLikelyTypoLogs() {
      Preconditions.warnLikelyTypo("fllx", "Tw.style");
      assertEquals(1, captured.size());
      assertEquals(Level.WARNING, captured.get(0).getLevel());
      assertTrue(captured.get(0).getMessage().contains("fllx"));
    }
  }

  @Nested
  @DisplayName("Return-value contract")
  class ReturnContract {

    @Test
    @DisplayName("validators return the validated value unchanged")
    void validatorsArePassThrough() {
      String s = Preconditions.requireNonBlank("p-4", "test", "token");
      assertEquals("p-4", s);
      assertEquals(7, Preconditions.requireSpan(7, "test"));
      assertEquals(0.42, Preconditions.requireOpacity(0.42, "test"));
      assertEquals(250, Preconditions.requirePositiveDuration(250, "test"));
      assertEquals(2.0, Preconditions.requirePositiveScale(2.0, "test"));
      assertEquals(5.0, Preconditions.requirePositiveSpeed(5.0, "test"));
      assertEquals(80, Preconditions.requireAlpha(80, "test"));
    }

    @Test
    @DisplayName("requireNonNull error message format is 'method: 'param' cannot be null'")
    void nullMessageFormat() {
      IllegalArgumentException ex =
          assertThrows(
              IllegalArgumentException.class,
              () -> Preconditions.requireNonNull(null, "Foo.bar", "baz"));
      assertEquals("Foo.bar: 'baz' cannot be null", ex.getMessage());
    }

    @Test
    @DisplayName("requireNonBlank error message format is 'method: 'param' cannot be blank'")
    void blankMessageFormat() {
      IllegalArgumentException ex =
          assertThrows(
              IllegalArgumentException.class,
              () -> Preconditions.requireNonBlank("", "Foo.bar", "baz"));
      assertEquals("Foo.bar: 'baz' cannot be blank", ex.getMessage());
    }
  }
}
