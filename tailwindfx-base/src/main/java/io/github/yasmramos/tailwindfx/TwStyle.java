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

import io.github.yasmramos.tailwindfx.animation.AnimationApplier;
import io.github.yasmramos.tailwindfx.core.JitCompiler;
import io.github.yasmramos.tailwindfx.core.Preconditions;
import io.github.yasmramos.tailwindfx.core.StyleCache;
import io.github.yasmramos.tailwindfx.core.TokenParser;
import io.github.yasmramos.tailwindfx.core.UtilityConflictResolver;
import io.github.yasmramos.tailwindfx.core.VariantManager;
import io.github.yasmramos.tailwindfx.effect.EffectApplier;
import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import io.github.yasmramos.tailwindfx.style.LayoutApplier;
import io.github.yasmramos.tailwindfx.style.ProgrammaticStyleApplier;
import io.github.yasmramos.tailwindfx.style.StyleMerger;
import io.github.yasmramos.tailwindfx.style.StylePerf;
import io.github.yasmramos.tailwindfx.style.StylesheetApplier;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Node;

/**
 * TwStyle — Style facade for utility classes and JIT tokens.
 *
 * <p>This class handles applying, removing, and toggling CSS classes and JIT-compiled styles on
 * JavaFX nodes.
 *
 * <p>Usage:
 *
 * <pre>
 * TwStyle.apply(node, "btn-primary", "rounded-lg");
 * TwStyle.apply(node, "bg-blue-500/80", "p-[13px]"); // arbitrary/JIT tokens
 * TwStyle.remove(node, "old-class");
 * TwStyle.toggle(node, "active");
 * </pre>
 *
 * <h2>Application Order</h2>
 *
 * <p>When {@code apply()} is called, styles are applied in the following sequence:
 *
 * <ol>
 *   <li><b>Effects</b>: Filter effects like blur, grayscale, invert via {@link EffectApplier}
 *   <li><b>CSS Classes</b>: Standard utility classes via {@link UtilityConflictResolver}
 *   <li><b>Layout Migration Warning</b>: Detects legacy layout tokens requiring container migration
 *   <li><b>Layout-dependent Styles</b>: Margins, gaps, flex properties requiring parent context
 *   <li><b>Variants</b>: Hover, focus, dark mode variants via {@link
 *       io.github.yasmramos.tailwindfx.core.VariantManager}
 *   <li><b>Unknown Token Warnings</b>: Debug logging for unrecognized tokens
 *   <li><b>JIT Compilation</b>: Arbitrary values and dynamic tokens compiled to inline styles
 * </ol>
 *
 * <p><b>Why order matters:</b> Variants must be applied before JIT because variant processing may
 * generate JIT-compiled styles. Applying variants first ensures that hover/focus states are
 * properly registered before any inline JIT styles override them.
 *
 * <h2>Token Parsing</h2>
 *
 * <p>The varargs {@code tokens} parameter accepts space-separated strings. Both of these calls are
 * equivalent:
 *
 * <pre>
 * TwStyle.apply(node, "p-4 bg-blue-500");
 * TwStyle.apply(node, "p-4", "bg-blue-500");
 * </pre>
 */
public final class TwStyle {

  private static final Logger LOGGER = Logger.getLogger(TwStyle.class.getName());

  private TwStyle() {}

  /**
   * Applies one or more TailwindFX utility classes to a JavaFX node.
   *
   * <p>Each argument may be a single token or a space-separated group of tokens. Tokens are
   * auto-detected: plain utilities become CSS classes, arbitrary/JIT values (for example {@code
   * w-[320px]} or {@code bg-blue-500/80}) are compiled to inline styles, {@code animate-*}
   * utilities are played as timelines, and variants such as {@code hover:} or {@code dark:} are
   * registered accordingly.
   *
   * <pre>
   * TwStyle.apply(button, "bg-blue-500", "text-white", "rounded-lg");
   * TwStyle.apply(button, "px-4 py-2");           // space-separated group
   * TwStyle.apply(button, "w-[320px]");           // JIT / arbitrary value
   * </pre>
   *
   * @param node the JavaFX node to style; must not be {@code null}
   * @param tokens one or more utility tokens (or space-separated groups of them); a {@code null} or
   *     empty array is ignored
   */
  public static void apply(Node node, String... tokens) {
    Preconditions.requireNode(node, "TwStyle.apply");
    if (tokens == null || tokens.length == 0) return;

    if (StylePerf.isBatchActive()) {
      StylePerf.enqueueDeferredApply(node, tokens);
    } else {
      applyInternal(node, tokens);
    }
  }

  private static void applyInternal(Node node, String... tokens) {
    // Delegate token parsing and classification to TokenParser
    TokenParser.ParseResult result = TokenParser.parse(tokens);

    // Apply effect tokens via EffectApplier
    if (!result.effectTokens().isEmpty()) {
      for (String effectToken : result.effectTokens()) {
        EffectApplier.applyEffectToken(node, effectToken);
      }
    }

    // Apply animate-* tokens via AnimationApplier (JavaFX has no CSS animation engine, so these
    // are played as TwAnimation timelines rather than compiled to inline styles)
    for (String animationToken : result.animationTokens()) {
      AnimationApplier.applyAnimationToken(node, animationToken);
    }

    // Migration tokens detected (e.g. legacy "flex"/"grid" usage on a plain node): we log a
    // warning and CONTINUE applying the remaining tokens. The node is therefore not left
    // "half-styled" by an exception; only the migration-requiring tokens themselves are skipped,
    // since turning a node into a container requires TwLayout.apply() rather than TwStyle.apply().
    if (!result.layoutMigrationTokens().isEmpty()) {
      LOGGER.log(
          Level.WARNING,
          "Layout classes requiring container migration ({0}) should be applied using TwLayout.apply() instead of TwStyle.apply().",
          String.join(", ", result.layoutMigrationTokens()));
      // These tokens are intentionally skipped here to avoid partial/incorrect styling;
      // every other token in the call is still applied below.
    }

    if (!result.cssClasses().isEmpty()) {
      UtilityConflictResolver.applyAll(node, result.cssClasses().toArray(new String[0]));
      // Some utilities (visibility, overflow, z-order, fill sizing) have no JavaFX CSS
      // equivalent; apply them through the JavaFX API so the class is not a silent no-op.
      ProgrammaticStyleApplier.apply(node, result.cssClasses());
      TailwindFXMetrics.instance().recordApply(result.cssClasses().size());
    }

    // Apply layout-dependent styles first (needs parent context)
    if (!result.layoutDependentTokens().isEmpty()) {
      LayoutApplier.applyLayoutDependentStyles(node, result.layoutDependentTokens());
    }

    // Apply variant tokens via VariantManager
    if (!result.variantTokens().isEmpty()) {
      for (String variantToken : result.variantTokens()) {
        VariantManager.processToken(node, variantToken, new JitCompiler());
      }
    }

    // Handle unknown tokens with debug warning (Smart fallback as documented in README)
    // Warnings collected during single pass to avoid redundant iteration
    if (!result.unknownTokens().isEmpty()) {
      for (String t : result.unknownTokens()) {
        LOGGER.log(Level.WARNING, "Unknown token ignored: {0}", t);
      }
    }

    if (!result.jitTokens().isEmpty()) {
      // Check preferStylesheet mode: apply classes from AOT stylesheet when available,
      // fallback to JIT inline for dynamic/arbitrary values
      if (io.github.yasmramos.tailwindfx.TwConfig.isPreferStylesheet()) {
        StylesheetApplier.applyWithStylesheetPreference(
            node, result.jitTokens().toArray(new String[0]));
      } else {
        StyleMerger.applyJit(node, result.jitTokens().toArray(new String[0]));
      }
    }
  }

  // NOTE: All layout-dependent style logic (margins, gaps, flex, grid) lives exclusively in
  // io.github.yasmramos.tailwindfx.style.LayoutApplier. It used to be duplicated here as a set of
  // private copies that could silently drift out of sync with LayoutApplier. Those duplicates were
  // removed; applyInternal() above delegates directly to LayoutApplier.applyLayoutDependentStyles.

  /**
   * Applies utility classes without conflict resolution.
   *
   * <p>Unlike {@link #apply(Node, String...)}, this method does not classify tokens, resolve
   * conflicts or run JIT compilation: each token is added verbatim to the node's style class list.
   * Use it when you want full control over the resulting class list (for example, when driving a
   * stylesheet directly).
   *
   * <pre>
   * TwStyle.applyRaw(button, "bg-blue-500", "rounded-lg");
   * TwStyle.applyRaw(button, "px-4 py-2"); // split on whitespace
   * </pre>
   *
   * @param node the JavaFX node to style; must not be {@code null}
   * @param classes the CSS classes to add; {@code null} is ignored and blank entries are skipped
   */
  public static void applyRaw(Node node, String... classes) {
    Preconditions.requireNode(node, "TwStyle.applyRaw");
    if (classes == null) return;
    for (String c : classes) {
      if (c == null || c.isBlank()) continue;
      for (String part : c.split("\\s+")) {
        if (!part.isBlank() && !node.getStyleClass().contains(part)) {
          node.getStyleClass().add(part);
        }
      }
    }
  }

  /**
   * Removes the given CSS classes from a node.
   *
   * <pre>
   * TwStyle.remove(button, "bg-blue-500", "rounded-lg");
   * </pre>
   *
   * @param node the JavaFX node to update; must not be {@code null}
   * @param classes the CSS classes to remove; {@code null} or an empty array is ignored
   */
  public static void remove(Node node, String... classes) {
    Preconditions.requireNode(node, "TwStyle.remove");
    if (classes == null || classes.length == 0) return;
    node.getStyleClass().removeAll(Arrays.asList(classes));
  }

  /**
   * Replaces the entire style class list of a node with the given classes.
   *
   * <pre>
   * TwStyle.replace(button, "bg-green-500", "rounded-lg");
   * </pre>
   *
   * @param node the JavaFX node to update
   * @param classes the classes that become the node's full style class list; {@code null} clears it
   */
  public static void replace(Node node, String... classes) {
    Preconditions.requireNode(node, "TwStyle.replace");
    node.getStyleClass().setAll(Arrays.asList(classes == null ? new String[0] : classes));
  }

  /**
   * Toggles a CSS class on a node: adds it when absent, removes it when present.
   *
   * <pre>
   * TwStyle.toggle(button, "active"); // add if missing, remove if present
   * </pre>
   *
   * @param node the JavaFX node to update
   * @param cssClass the class to toggle
   */
  public static void toggle(Node node, String cssClass) {
    if (node.getStyleClass().contains(cssClass)) {
      node.getStyleClass().remove(cssClass);
    } else {
      node.getStyleClass().add(cssClass);
    }
  }

  /**
   * Enables automatic cleanup of JIT styles when the node is removed from the scene.
   *
   * <p>Registers the node with {@link
   * io.github.yasmramos.tailwindfx.core.UtilityConflictResolver#autoCleanup(Node)} so that its
   * inline JIT styles and cache entries are released once it leaves the scene graph, preventing
   * stale state from leaking across scene rebuilds.
   *
   * <pre>
   * TwStyle.autoCleanup(card); // release JIT styles when the card leaves the scene
   * </pre>
   *
   * @param node the JavaFX node to track; must not be {@code null}
   */
  public static void autoCleanup(Node node) {
    Preconditions.requireNode(node, "TwStyle.autoCleanup");
    // Delegate to existing cleanup mechanism in UtilityConflictResolver
    io.github.yasmramos.tailwindfx.core.UtilityConflictResolver.autoCleanup(node);
  }

  /**
   * Invalidates the entire style cache for a node.
   *
   * <p>Forces the next {@code apply} on this node to re-resolve every category instead of reusing
   * cached results. Call it after changing state that the cache cannot observe (for example, a
   * theme switch performed outside {@code TwTheme}).
   *
   * <pre>
   * TwStyle.invalidateCache(node);
   * </pre>
   *
   * @param node the JavaFX node whose cache should be dropped
   */
  public static void invalidateCache(Node node) {
    StyleCache.invalidate(node);
  }

  /**
   * Removes all TailwindFX styles from a node.
   *
   * <p>Alias for {@link #invalidateCache(Node)}, kept for backward compatibility. Prefer {@code
   * cleanupNode} when you mean "detach TailwindFX from this node" and {@code invalidateCache} when
   * you only need to drop cached resolutions.
   *
   * <pre>
   * TwStyle.cleanupNode(node);
   * </pre>
   *
   * @param node the JavaFX node to clean up
   */
  public static void cleanupNode(Node node) {
    StyleCache.cleanup(node);
  }

  /**
   * Invalidates a single utility category from the style cache for a node.
   *
   * <p>More targeted than {@link #invalidateCache(Node)}: only the cache entries belonging to {@code
   * category} are dropped, so unrelated categories keep their cached resolutions.
   *
   * <pre>
   * TwStyle.invalidateCategoryCache(node, "colors");
   * </pre>
   *
   * @param node the JavaFX node whose category cache should be dropped
   * @param category the utility category to invalidate (for example {@code "colors"} or {@code
   *     "spacing"})
   */
  public static void invalidateCategoryCache(Node node, String category) {
    StyleCache.invalidateCategory(node, category);
  }

  // The following private helpers (isJitToken, stripVariantPrefix, isValidColorUtilityBase,
  // isEffectToken, applyEffectToken) were removed after confirming via grep that they had no
  // callers (direct or reflective). Their canonical replacements are:
  //   TokenRegistry.isJitPrefix / TokenRegistry.stripVariantPrefix /
  //   ColorUtilityValidator.isValidColorUtilityBase / TokenRegistry.isEffectToken /
  //   EffectApplier.applyEffectToken.

}
