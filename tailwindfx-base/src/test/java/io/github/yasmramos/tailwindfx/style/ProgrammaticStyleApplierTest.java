/*
 * Copyright 2026 Yasmany Ramos García (yasmramos).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.yasmramos.tailwindfx.style;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.TwStyle;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for the runtime application of class-only utilities that have no JavaFX CSS rule. */
@DisplayName("ProgrammaticStyleApplier Tests")
class ProgrammaticStyleApplierTest {

  @Test
  @DisplayName("handles() recognises only the supported utility families")
  void testHandles() {
    assertTrue(ProgrammaticStyleApplier.handles("hidden"));
    assertTrue(ProgrammaticStyleApplier.handles("overflow-hidden"));
    assertTrue(ProgrammaticStyleApplier.handles("overflow-x-clip"));
    assertTrue(ProgrammaticStyleApplier.handles("z-50"));
    assertTrue(ProgrammaticStyleApplier.handles("w-full"));
    assertTrue(ProgrammaticStyleApplier.handles("h-full"));
    assertTrue(ProgrammaticStyleApplier.handles("aspect-video"));
    assertTrue(ProgrammaticStyleApplier.handles("aspect-[4/3]"));

    assertFalse(ProgrammaticStyleApplier.handles("p-4"));
    assertFalse(ProgrammaticStyleApplier.handles("bg-blue-500"));
    assertFalse(ProgrammaticStyleApplier.handles("aspect-auto"));
    assertFalse(ProgrammaticStyleApplier.handles(null));
    assertFalse(ProgrammaticStyleApplier.handles(""));
  }

  @Test
  @DisplayName("hidden clears visibility and layout space")
  void testHidden() {
    Pane pane = new Pane();
    ProgrammaticStyleApplier.apply(pane, java.util.List.of("hidden"));
    assertFalse(pane.isVisible());
    assertFalse(pane.isManaged());
  }

  @Test
  @DisplayName("visible and invisible toggle visibility without touching layout")
  void testVisibleAndInvisible() {
    Pane pane = new Pane();
    ProgrammaticStyleApplier.apply(pane, java.util.List.of("hidden", "visible"));
    assertTrue(pane.isVisible());
    assertTrue(pane.isManaged());

    ProgrammaticStyleApplier.apply(pane, java.util.List.of("invisible"));
    assertEquals(0.0, pane.getOpacity(), 1e-9);
    assertTrue(pane.isVisible());
  }

  @Test
  @DisplayName("overflow-hidden installs a clip and overflow-visible removes it")
  void testOverflow() {
    Pane pane = new Pane();
    ProgrammaticStyleApplier.apply(pane, java.util.List.of("overflow-hidden"));
    assertNotNull(pane.getClip());

    ProgrammaticStyleApplier.apply(pane, java.util.List.of("overflow-visible"));
    assertNull(pane.getClip());
  }

  @Test
  @DisplayName("z utilities map to an inverted view order")
  void testZOrder() {
    Pane pane = new Pane();
    ProgrammaticStyleApplier.apply(pane, java.util.List.of("z-50"));
    assertEquals(-50.0, pane.getViewOrder(), 1e-9);

    ProgrammaticStyleApplier.apply(pane, java.util.List.of("z-auto"));
    assertEquals(0.0, pane.getViewOrder(), 1e-9);
  }

  @Test
  @DisplayName("fill sizing utilities grow the region to the available space")
  void testFillSizing() {
    Pane pane = new Pane();
    ProgrammaticStyleApplier.apply(pane, java.util.List.of("w-full"));
    assertEquals(Double.MAX_VALUE, pane.getMaxWidth());

    ProgrammaticStyleApplier.apply(pane, java.util.List.of("h-full", "max-h-screen"));
    assertEquals(Double.MAX_VALUE, pane.getMaxHeight());
  }

  @Test
  @DisplayName("aspect utilities bind the region preferred height to its width")
  void testAspectRatio() {
    Pane pane = new Pane();
    ProgrammaticStyleApplier.apply(pane, java.util.List.of("aspect-video"));
    assertTrue(pane.prefHeightProperty().isBound(), "aspect-video should bind pref height");

    Pane arbitrary = new Pane();
    ProgrammaticStyleApplier.apply(arbitrary, java.util.List.of("aspect-[4/3]"));
    assertTrue(arbitrary.prefHeightProperty().isBound());

    // aspect-auto must leave the region untouched.
    Pane auto = new Pane();
    ProgrammaticStyleApplier.apply(auto, java.util.List.of("aspect-auto"));
    assertFalse(auto.prefHeightProperty().isBound());
  }

  @Test
  @DisplayName("TwStyle.apply wires the class-only utilities at runtime")
  void testTwStyleIntegration() {
    Pane pane = new Pane();
    TwStyle.apply(pane, "hidden");
    assertFalse(pane.isVisible());
    assertFalse(pane.isManaged());

    Pane overlay = new Pane();
    TwStyle.apply(overlay, "overflow-hidden z-50 w-full");
    assertNotNull(overlay.getClip());
    assertEquals(-50.0, overlay.getViewOrder(), 1e-9);
    assertEquals(Double.MAX_VALUE, overlay.getMaxWidth());
  }
}
