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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ThemeTokens")
class ThemeTokensTest {

  @Test
  @DisplayName("light and dark palettes define exactly the same tokens")
  void palettesShareKeys() {
    assertEquals(
        ThemeTokens.values(false).keySet(),
        ThemeTokens.values(true).keySet(),
        "a token missing in one mode would stay unresolved after a theme switch");
  }

  @Test
  @DisplayName("every token uses the -tw- prefix and a color value")
  void tokensAreWellFormed() {
    for (boolean dark : new boolean[] {false, true}) {
      ThemeTokens.values(dark)
          .forEach(
              (name, value) -> {
                assertTrue(ThemeTokens.isToken(name), name);
                assertTrue(value.matches("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?"), name + "=" + value);
              });
    }
  }

  @Test
  @DisplayName("dark surface differs from light surface")
  void darkDiffersFromLight() {
    assertNotEquals(
        ThemeTokens.values(false).get(ThemeTokens.SURFACE),
        ThemeTokens.values(true).get(ThemeTokens.SURFACE));
  }

  @Test
  @DisplayName("toStyle renders every token as a declaration")
  void toStyleRendersAllTokens() {
    String style = ThemeTokens.toStyle(true);
    ThemeTokens.values(true)
        .forEach((k, v) -> assertTrue(style.contains(k + ": " + v + ";"), "missing " + k));
  }

  @Test
  @DisplayName("isToken only accepts -tw- properties")
  void isTokenRecognisesPrefix() {
    assertTrue(ThemeTokens.isToken("-tw-surface"));
    assertFalse(ThemeTokens.isToken("-fx-base"));
    assertFalse(ThemeTokens.isToken(null));
  }
}
