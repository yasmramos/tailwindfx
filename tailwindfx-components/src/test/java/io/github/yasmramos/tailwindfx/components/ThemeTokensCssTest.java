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
package io.github.yasmramos.tailwindfx.components;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.theme.ThemeTokens;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Keeps {@code tailwindfx-components.css} and {@link ThemeTokens} consistent: the stylesheet
 * declares the light defaults of every token, and never references a token that does not exist.
 */
@DisplayName("tailwindfx-components.css theme tokens")
class ThemeTokensCssTest {

  private static String css() throws IOException {
    try (InputStream in = ThemeTokensCssTest.class.getResourceAsStream("/tailwindfx-components.css")) {
      assertNotNull(in, "tailwindfx-components.css must be on the classpath");
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  @Test
  @DisplayName(".root declares the light default of every token")
  void rootDefaultsMatchLightPalette() throws IOException {
    Matcher root = Pattern.compile("(?m)^\\.root\\s*\\{(.*?)^\\}", Pattern.DOTALL).matcher(css());
    assertTrue(root.find(), ".root block with the token defaults is missing");

    Map<String, String> declared = new LinkedHashMap<>();
    Matcher decl = Pattern.compile("(-tw-[a-z-]+)\\s*:\\s*([^;]+);").matcher(root.group(1));
    while (decl.find()) {
      declared.put(decl.group(1), decl.group(2).trim());
    }
    assertEquals(ThemeTokens.values(false), declared);
  }

  @Test
  @DisplayName("every -tw-* lookup used by a rule is a defined token")
  void noUndefinedTokenReferences() throws IOException {
    Set<String> used = new TreeSet<>();
    Matcher m = Pattern.compile(":\\s*(-tw-[a-z-]+)\\s*;").matcher(css());
    while (m.find()) {
      used.add(m.group(1));
    }
    assertFalse(used.isEmpty(), "components should reference the theme tokens");
    used.removeAll(ThemeTokens.values(false).keySet());
    assertTrue(used.isEmpty(), "undefined tokens referenced: " + used);
  }

  @Test
  @DisplayName("stylesheet ships dark overrides")
  void hasDarkOverrides() throws IOException {
    assertTrue(css().contains(".dark .badge-blue"), "dark overrides for status colors missing");
  }
}
