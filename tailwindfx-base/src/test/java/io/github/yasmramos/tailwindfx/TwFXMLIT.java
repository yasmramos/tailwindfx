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
package io.github.yasmramos.tailwindfx;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Integration tests for TwFXML utility class.
 *
 * @author yasmramos
 * @since 1.0.0
 */
class TwFXMLTest extends ApplicationTest {

  private VBox root;

  @BeforeEach
  void setup() {
    root = new VBox();
    interact(
        () -> {
          root.setSpacing(10);
        });
  }

  @Test
  @DisplayName("process() compiles arbitrary JIT tokens to inline styles")
  void testProcessCompilesArbitraryTokens() {
    VBox box = new VBox();
    interact(
        () -> {
          box.getStyleClass().addAll("bg-[#ff0000]/80", "w-[320px]");
          root.getChildren().add(box);
        });

    // Process the scene graph
    TwFXML.process(root);

    // Verify that inline styles were applied
    String style = box.getStyle();
    assertNotNull(style);
    assertTrue(
        style.contains("-fx-background-color") || style.contains("background-color"),
        "Expected inline background color style, got: " + style);
    assertTrue(
        style.contains("rgba(255,0,0,0.80)") || style.contains("255, 0, 0"),
        "Expected rgba(255,0,0,0.80) in style: " + style);
  }

  @Test
  @DisplayName("process() keeps static utility classes as styleClass")
  void testProcessKeepsStaticClasses() {
    VBox box = new VBox();
    interact(
        () -> {
          box.getStyleClass().addAll("text-xl", "font-bold");
          root.getChildren().add(box);
        });

    // Process the scene graph
    TwFXML.process(root);

    // Verify that static classes remain in styleClass
    assertNotNull(lookup(".text-xl").tryQuery());
    assertTrue(box.getStyleClass().contains("text-xl"));
    assertTrue(box.getStyleClass().contains("font-bold"));
  }

  @Test
  @DisplayName("process() handles mixed JIT and static classes")
  void testProcessHandlesMixedClasses() {
    VBox box = new VBox();
    interact(
        () -> {
          box.getStyleClass().addAll("text-xl", "bg-[#00ff00]", "font-bold", "h-[100px]");
          root.getChildren().add(box);
        });

    // Process the scene graph
    TwFXML.process(root);

    // Verify both types are handled correctly
    assertNotNull(lookup(".text-xl").tryQuery());
    assertTrue(box.getStyleClass().contains("text-xl"));
    assertTrue(box.getStyleClass().contains("font-bold"));

    String style = box.getStyle();
    assertNotNull(style);
    assertTrue(
        style.contains("background-color") || style.contains("-fx-background-color"),
        "Expected inline background color for bg-[#00ff00], got: " + style);
    assertTrue(
        style.contains("height") || style.contains("-fx-pref-height"),
        "Expected inline height for h-[100px], got: " + style);
  }

  @Test
  @DisplayName("isJitToken() correctly identifies arbitrary tokens")
  void testIsJitTokenDetection() {
    // Arbitrary values with brackets
    assertTrue(TwFXML.isJitToken("bg-[#ff0000]"));
    assertTrue(TwFXML.isJitToken("w-[320px]"));
    assertTrue(TwFXML.isJitToken("h-[50%]"));

    // Arbitrary values with opacity modifier
    assertTrue(TwFXML.isJitToken("bg-[#ff0000]/80"));
    assertTrue(TwFXML.isJitToken("text-[#123456]/50"));

    // Regular utility classes should not be detected as JIT
    assertFalse(TwFXML.isJitToken("text-xl"));
    assertFalse(TwFXML.isJitToken("font-bold"));
    assertFalse(TwFXML.isJitToken("bg-red-500"));
    assertFalse(TwFXML.isJitToken("m-4"));

    // Edge cases
    assertFalse(TwFXML.isJitToken(null));
    assertFalse(TwFXML.isJitToken(""));
    assertFalse(TwFXML.isJitToken("invalid-class"));
  }

  @Test
  @DisplayName("enableAutoJit() compiles dynamically added JIT tokens")
  void testEnableAutoJitDynamicCompilation() {
    VBox box = new VBox();
    interact(
        () -> {
          root.getChildren().add(box);
        });

    // Enable auto-JIT
    TwFXML.enableAutoJit(root);

    // Add a JIT token dynamically
    interact(
        () -> {
          box.getStyleClass().add("bg-[#0000ff]/60");
        });

    // Wait a bit for the listener to process
    sleep(100);

    // Verify that inline style was applied
    String style = box.getStyle();
    assertNotNull(style);
    assertTrue(
        style.contains("background-color") || style.contains("-fx-background-color"),
        "Expected inline background color style after dynamic addition, got: " + style);
  }

  @Test
  @DisplayName("enableAutoJit() does not cause infinite recursion")
  void testEnableAutoJitNoRecursion() {
    VBox box = new VBox();
    interact(
        () -> {
          root.getChildren().add(box);
        });

    // Enable auto-JIT
    TwFXML.enableAutoJit(root);

    // Add multiple JIT tokens - should not cause stack overflow
    assertDoesNotThrow(
        () -> {
          interact(
              () -> {
                box.getStyleClass().addAll("w-[100px]", "h-[200px]");
              });
          sleep(200);
        },
        "Auto-JIT listener should not cause infinite recursion");
  }

  @Test
  @DisplayName("disableAutoJit() removes auto-JIT functionality")
  void testDisableAutoJit() {
    VBox box = new VBox();
    interact(
        () -> {
          root.getChildren().add(box);
        });

    // Enable then disable auto-JIT
    TwFXML.enableAutoJit(root);
    TwFXML.disableAutoJit(root);

    // Adding a JIT token should NOT compile it automatically
    interact(
        () -> {
          box.getStyleClass().add("bg-[#112233]/70");
        });

    sleep(100);

    // The class should still be in styleClass (not removed and compiled)
    assertTrue(
        box.getStyleClass().contains("bg-[#112233]/70"),
        "JIT token should remain in styleClass after disabling auto-JIT");
  }

  @Test
  @DisplayName("process() throws IllegalArgumentException for null root")
  void testProcessNullRoot() {
    assertThrows(IllegalArgumentException.class, () -> TwFXML.process(null));
  }

  @Test
  @DisplayName("enableAutoJit() throws IllegalArgumentException for null root")
  void testEnableAutoJitNullRoot() {
    assertThrows(IllegalArgumentException.class, () -> TwFXML.enableAutoJit(null));
  }

  @Test
  @DisplayName("disableAutoJit() throws IllegalArgumentException for null root")
  void testDisableAutoJitNullRoot() {
    assertThrows(IllegalArgumentException.class, () -> TwFXML.disableAutoJit(null));
  }
}
