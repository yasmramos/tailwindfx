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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Guards the contract between the components and {@code tailwindfx-components.css}: every class a
 * component applies must have a matching rule, otherwise the styling is silently dead.
 *
 * <p>Each entry below is a class the components module emits. When a component starts or stops
 * applying one of them, this list must change with it.
 */
@DisplayName("tailwindfx-components.css class contract")
class ComponentCssContractTest {

  /** Classes the components apply; each must be declared by at least one stylesheet rule. */
  private static final Set<String> REQUIRED_CLASSES =
      new LinkedHashSet<>(
          Set.of(
              // TwCard slots
              "tw-card",
              "tw-card-header",
              "tw-card-title",
              "tw-card-content",
              "tw-card-footer",
              // TwAlert
              "tw-alert",
              "alert-icon",
              "alert-message",
              // TwButton
              "btn",
              "btn-disabled",
              "btn-loading",
              "btn-icon",
              "btn-circle",
              // TwDataTable
              "data-table",
              "table-striped",
              "table-hover",
              "table-compact",
              "table-bordered",
              "search-bar",
              "search-field",
              // TwSelect popup
              "combo-box-popup",
              // TwCheckbox / TwBadge / TwSpinner / TwProgressBar / TwVirtualFlow / TwAccordion
              "checkbox",
              "badge",
              "dot",
              "spinner",
              "progress-bar",
              "virtual-flow",
              "collapse-item"));

  private static Set<String> declaredClasses;

  @BeforeAll
  static void parseStylesheet() throws IOException {
    String css;
    try (InputStream in =
        ComponentCssContractTest.class.getResourceAsStream("/tailwindfx-components.css")) {
      assertNotNull(in, "tailwindfx-components.css must be on the classpath");
      css = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
    // Drop comments first so class names mentioned only in prose are not counted as rules.
    String withoutComments = css.replaceAll("/\\*.*?\\*/", " ");

    declaredClasses = new LinkedHashSet<>();
    Matcher rule = Pattern.compile("(?s)[^{}]*\\{").matcher(withoutComments);
    while (rule.find()) {
      Matcher cls = Pattern.compile("\\.([a-zA-Z][a-zA-Z0-9_-]*)").matcher(rule.group());
      while (cls.find()) {
        declaredClasses.add(cls.group(1));
      }
    }
  }

  @Test
  @DisplayName("every class a component applies has a matching stylesheet rule")
  void everyComponentClassHasARule() {
    Set<String> missing = new LinkedHashSet<>();
    for (String cls : REQUIRED_CLASSES) {
      if (!declaredClasses.contains(cls)) {
        missing.add(cls);
      }
    }
    assertTrue(
        missing.isEmpty(),
        "these classes are applied by components but have no rule in tailwindfx-components.css, "
            + "so their styling is dead: "
            + missing);
  }

  @Test
  @DisplayName("no rule targets a class the components never apply")
  void noRuleTargetsAnUnappliedClass() {
    // Legacy rules that matched nothing because no node carries these classes: the ComboBox popup
    // is tagged "combo-box-popup", and TitledPane's skin tags its header/content "title" /
    // "content". A rule that can never match is dead weight and misleads callers.
    Set<String> dead = new LinkedHashSet<>();
    for (String cls : Set.of("select-popup", "collapse-header", "collapse-content")) {
      if (declaredClasses.contains(cls)) {
        dead.add(cls);
      }
    }
    assertTrue(
        dead.isEmpty(),
        "these rules target a class no node carries, so they can never match: " + dead);
  }
}
