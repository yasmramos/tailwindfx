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
package io.github.yasmramos.tailwindfx.theme;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.ToolkitBootstrap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies that a stylesheet rule using {@code -tw-*} lookups follows the theme applied by {@link
 * ThemeManager} and {@link ThemeScopeManager}, which is what makes component styles react to dark
 * mode.
 */
@DisplayName("Theme tokens follow the active theme")
class ThemeTokenSwitchingTest {

  private static String stylesheet;

  @BeforeAll
  static void setUp() throws Exception {
    ToolkitBootstrap.ensureStarted();
    Path css = Files.createTempFile("tw-token-probe", ".css");
    Files.writeString(
        css,
        ".root { -tw-surface: #ffffff; }\n.probe { -fx-background-color: -tw-surface; }\n");
    css.toFile().deleteOnExit();
    stylesheet = css.toUri().toString();
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

  private static Region probe() {
    Pane p = new Pane();
    p.getStyleClass().add("probe");
    return p;
  }

  private static Scene sceneWith(Pane root) {
    Scene scene = new Scene(root, 200, 200);
    scene.getStylesheets().add(stylesheet);
    return scene;
  }

  private static Color background(Region r) {
    r.applyCss();
    return (Color) r.getBackground().getFills().get(0).getFill();
  }

  private static Color surface(boolean dark) {
    return Color.web(ThemeTokens.values(dark).get(ThemeTokens.SURFACE));
  }

  @Test
  @DisplayName("ThemeManager switches the token between light and dark")
  void themeManagerSwitchesTokens() throws Exception {
    onFx(
        () -> {
          Region probe = probe();
          Pane root = new Pane(probe);
          Scene scene = sceneWith(root);

          assertEquals(surface(false), background(probe), "stylesheet default");

          ThemeManager.forScene(scene).dark().apply();
          assertEquals(surface(true), background(probe));
          assertTrue(root.getStyleClass().contains("dark"));

          ThemeManager.toggle(scene);
          assertEquals(surface(false), background(probe));
          assertFalse(root.getStyleClass().contains("dark"));
          return null;
        });
  }

  @Test
  @DisplayName("a dark scope inside a light root themes only its subtree")
  void scopeKeepsTokens() throws Exception {
    onFx(
        () -> {
          Region inner = probe();
          Region outer = probe();
          Pane scoped = new Pane(inner);
          Scene scene = sceneWith(new Pane(outer, scoped));

          ThemeScopeManager.scope(scoped).dark().apply();
          assertEquals(surface(true), background(inner), "tokens must survive the style merge");
          assertEquals(surface(false), background(outer));

          ThemeScopeManager.clearScope(scoped);
          assertEquals(surface(false), background(inner), "clearScope must remove the tokens");
          assertNotNull(scene);
          return null;
        });
  }

  @Test
  @DisplayName("a scope that only changes the accent keeps the inherited tokens")
  void accentOnlyScopeDoesNotResetTokens() throws Exception {
    onFx(
        () -> {
          Region inner = probe();
          Pane scoped = new Pane(inner);
          Scene scene = sceneWith(new Pane(scoped));

          ThemeManager.forScene(scene).dark().apply();
          ThemeScopeManager.scope(scoped).accent("#f97316").apply();

          assertEquals(surface(true), background(inner));
          return null;
        });
  }
}
