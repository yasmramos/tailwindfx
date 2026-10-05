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
package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.ToolkitBootstrap;
import io.github.yasmramos.tailwindfx.theme.ThemeManager;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code dark:} and {@code light:} utilities must follow theme switches in both directions. */
@DisplayName("Theme variants (dark: / light:)")
class ThemeVariantSwitchingTest {

  private static final String DARK_UTILITY = "bg-gray-800";
  private static final String LIGHT_UTILITY = "bg-gray-100";

  @BeforeAll
  static void setUp() {
    ToolkitBootstrap.ensureStarted();
  }

  private static <T> T onFx(Callable<T> task) throws Exception {
    CompletableFuture<T> result = new CompletableFuture<>();
    Platform.runLater(
        () -> {
          try {
            result.complete(task.call());
          } catch (Throwable t) {
            result.completeExceptionally(t);
          }
        });
    return result.get(10, TimeUnit.SECONDS);
  }

  private static String inlineStyleOf(String utility) {
    return new JitCompiler().compile(utility).inlineStyle();
  }

  @Test
  @DisplayName("dark: applies after the theme becomes dark and is undone when it becomes light")
  void darkVariantFollowsToggle() throws Exception {
    onFx(
        () -> {
          String darkStyle = inlineStyleOf(DARK_UTILITY);
          Region node = new Region();
          node.setStyle("-fx-background-color: white;");
          Pane root = new Pane(node);
          Scene scene = new Scene(root, 100, 100);

          // The node is already inside the scene when the variant is registered.
          VariantManager.applyThemeVariant(node, "dark", DARK_UTILITY, new JitCompiler());
          assertEquals("-fx-background-color: white;", node.getStyle());

          ThemeManager.forScene(scene).dark().apply();
          assertEquals(darkStyle.trim(), node.getStyle().trim());

          ThemeManager.toggle(scene);
          assertEquals(
              "-fx-background-color: white;",
              node.getStyle().trim(),
              "the value the node had before the variant must be restored");
          return null;
        });
  }

  @Test
  @DisplayName("light: is the mirror image of dark:")
  void lightVariantFollowsToggle() throws Exception {
    onFx(
        () -> {
          String lightStyle = inlineStyleOf(LIGHT_UTILITY);
          Region node = new Region();
          node.setStyle("-fx-background-color: black;");
          Scene scene = new Scene(new Pane(node), 100, 100);

          VariantManager.applyThemeVariant(node, "light", LIGHT_UTILITY, new JitCompiler());
          assertEquals(lightStyle.trim(), node.getStyle().trim(), "scene starts light");

          ThemeManager.forScene(scene).dark().apply();
          assertEquals("-fx-background-color: black;", node.getStyle().trim());

          ThemeManager.toggle(scene);
          assertEquals(lightStyle.trim(), node.getStyle().trim());
          return null;
        });
  }

  @Test
  @DisplayName("a node attached to a scene after registration picks up the current theme")
  void variantAppliesWhenNodeJoinsLaterScene() throws Exception {
    onFx(
        () -> {
          String darkStyle = inlineStyleOf(DARK_UTILITY);
          Region node = new Region();
          VariantManager.applyThemeVariant(node, "dark", DARK_UTILITY, new JitCompiler());

          Pane root = new Pane();
          Scene scene = new Scene(root, 100, 100);
          ThemeManager.forScene(scene).dark().apply();

          root.getChildren().add(node);
          assertEquals(darkStyle.trim(), node.getStyle().trim());

          root.getChildren().remove(node);
          assertEquals("", node.getStyle().trim(), "detached nodes drop the dark: style");
          return null;
        });
  }
}
