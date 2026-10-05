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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Semantic color tokens that let component styles follow the active theme.
 *
 * <p>JavaFX only propagates theme changes to rules that reference <em>looked-up colors</em>. Modena
 * controls do this through {@code -fx-base} and friends, but a stylesheet that hard-codes values
 * such as {@code -fx-background-color: white} never reacts to a theme switch. The tokens defined
 * here are looked-up colors that {@link ThemeManager} and {@link ThemeScopeManager} write on the
 * themed root (or scoped pane) together with the Modena variables, so a rule like:
 *
 * <pre>
 * .tw-card { -fx-background-color: -tw-surface; -fx-border-color: -tw-border; }
 * </pre>
 *
 * <p>follows light/dark automatically, including inside scoped panes.
 *
 * <p>The light values are also declared as defaults in {@code tailwindfx-components.css} under
 * {@code .root}, so components render correctly when no theme has been applied. Keep the two in
 * sync; a test in the components module enforces it.
 */
public final class ThemeTokens {

  /** Page-like surface: cards, inputs, tables, popups. */
  public static final String SURFACE = "-tw-surface";

  /** Slightly recessed surface: table headers, zebra rows, accordion headers. */
  public static final String SURFACE_MUTED = "-tw-surface-muted";

  /** Hover, disabled and scroll-track backgrounds. */
  public static final String SURFACE_SUBTLE = "-tw-surface-subtle";

  /** Hairline borders and dividers. */
  public static final String BORDER = "-tw-border";

  /** Borders of interactive fields (inputs, selects, checkboxes). */
  public static final String BORDER_STRONG = "-tw-border-strong";

  /** Default body text on a surface. */
  public static final String TEXT = "-tw-text";

  /** High-emphasis text such as titles. */
  public static final String TEXT_STRONG = "-tw-text-strong";

  /** Secondary text such as helper messages. */
  public static final String TEXT_MUTED = "-tw-text-muted";

  /** Prefix shared by every token, used to recognise them inside inline styles. */
  public static final String PREFIX = "-tw-";

  private static final Map<String, String> LIGHT = new LinkedHashMap<>();
  private static final Map<String, String> DARK = new LinkedHashMap<>();

  static {
    LIGHT.put(SURFACE, "#ffffff");
    LIGHT.put(SURFACE_MUTED, "#f9fafb");
    LIGHT.put(SURFACE_SUBTLE, "#f3f4f6");
    LIGHT.put(BORDER, "#e5e7eb");
    LIGHT.put(BORDER_STRONG, "#d1d5db");
    LIGHT.put(TEXT, "#374151");
    LIGHT.put(TEXT_STRONG, "#111827");
    LIGHT.put(TEXT_MUTED, "#6b7280");

    DARK.put(SURFACE, "#1e1e1e");
    DARK.put(SURFACE_MUTED, "#262626");
    DARK.put(SURFACE_SUBTLE, "#303030");
    DARK.put(BORDER, "#3a3a3a");
    DARK.put(BORDER_STRONG, "#4a4a4a");
    DARK.put(TEXT, "#e5e5e5");
    DARK.put(TEXT_STRONG, "#fafafa");
    DARK.put(TEXT_MUTED, "#a3a3a3");
  }

  private ThemeTokens() {}

  /**
   * Returns the token values for a mode.
   *
   * @param dark {@code true} for the dark palette, {@code false} for the light one
   * @return an unmodifiable, insertion-ordered map of token name to color
   */
  public static Map<String, String> values(boolean dark) {
    return java.util.Collections.unmodifiableMap(dark ? DARK : LIGHT);
  }

  /**
   * Returns the tokens of a mode as an inline-style fragment, e.g. {@code "-tw-surface: #fff; ..."}.
   *
   * @param dark {@code true} for the dark palette, {@code false} for the light one
   * @return the declarations, each terminated by a semicolon and a space
   */
  public static String toStyle(boolean dark) {
    StringBuilder sb = new StringBuilder();
    values(dark).forEach((k, v) -> sb.append(k).append(": ").append(v).append("; "));
    return sb.toString();
  }

  /**
   * Tells whether a CSS property name is one of the semantic tokens.
   *
   * @param property the property name, e.g. {@code "-tw-surface"}
   * @return {@code true} if it starts with {@link #PREFIX}
   */
  public static boolean isToken(String property) {
    return property != null && property.startsWith(PREFIX);
  }
}
