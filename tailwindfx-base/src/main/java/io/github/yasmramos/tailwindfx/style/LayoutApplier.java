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

import io.github.yasmramos.tailwindfx.TwConfig;
import io.github.yasmramos.tailwindfx.layout.TwFlexPane;
import io.github.yasmramos.tailwindfx.layout.TwGridPane;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * LayoutApplier - Handles application of layout-dependent styles.
 *
 * <p>This class centralizes all layout-related style application logic including margins, gaps,
 * flex properties, and grid configurations. It handles both standard JavaFX containers and
 * TailwindFX custom containers like TwFlexPane and TwGridPane.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Margin application via Styles utility methods (including negative margins such as -m-4)
 *   <li>Gap application to the container node itself (HBox, VBox, GridPane, TwFlexPane, TwGridPane)
 *       — gap-* is a container property and never mutates a parent from a child token
 *   <li>Flex grow/shrink factors for HBox, VBox, and TwFlexPane
 *   <li>Grid column/row configuration for TwGridPane
 *   <li>Automatic retry via parent property listener when node is not yet attached
 * </ul>
 *
 * <p>Usage:
 *
 * <pre>
 * LayoutApplier.applyLayoutDependentStyles(node, tokens);
 * </pre>
 */
public final class LayoutApplier {

  private static final Logger LOGGER = Logger.getLogger(LayoutApplier.class.getName());

  /**
   * Named Tailwind scale keywords that legitimately carry no numeric measurement.
   *
   * <p>They are valid tokens (recognized by {@code TokenRegistry}), so parsing them into {@link
   * Double#NaN} is expected behavior instead of a malformed-token warning.
   */
  private static final Set<String> NAMED_SCALE_KEYWORDS =
      Set.of(
          "auto",
          "full",
          "min",
          "max",
          "fit",
          "screen",
          "px",
          "none",
          "normal",
          "inherit",
          "initial",
          "unset");

  private LayoutApplier() {
    // Utility class - prevent instantiation
  }

  /**
   * Applies layout-dependent styles to a node.
   *
   * <p>Layout-dependent styles require knowledge of the parent container type to be applied
   * correctly. This method handles:
   *
   * <ul>
   *   <li>Margin styles (m-*, mx-*, my-*, mt-*, mr-*, mb-*, ml-* and negative variants -m-*)
   *   <li>Gap styles (gap-*, gap-x-*, gap-y-*) - applied to the node if it's a Pane
   *   <li>Flex styles (flex, flex-*, grow, shrink) - applied based on parent type
   *   <li>Grid styles (grid-cols-*, grid-rows-*, grid-flow-*) - applied to TwGridPane
   *   <li>Grid item styles (col-span-*, row-span-*) - applied via TwGridPane
   * </ul>
   *
   * <p><b>Gap semantics:</b> {@code gap-*} is a <i>container</i> property. It is only ever applied
   * to the node that carries the token (when that node is a Pane). A gap token written on a child
   * never mutates its parent — apply the gap class to the container itself instead.
   *
   * @param node the node to apply styles to
   * @param tokens list of layout-dependent tokens to apply
   */
  public static void applyLayoutDependentStyles(Node node, List<String> tokens) {
    for (String token : tokens) {
      // Gap is a container property: apply it to THIS node when it is a Pane. If the node is not
      // a Pane yet, keep the retry listener alive so the gap can still be applied after the node
      // is re-parented or swapped for a Pane-backed implementation (virtualization scenarios).
      if (isGapToken(token)) {
        if (node instanceof Pane pane) {
          applyGapStyle(pane, token);
        } else {
          // Bug #9 fix: Don't silently discard gap-* on non-Pane nodes
          // Register listener to retry when node becomes a Pane or gets a proper parent
          registerLayoutListener(node, tokens);
          return;
        }
        continue;
      }

      // For margin and flex styles, we need the parent
      Pane parent = getEffectiveParent(node);
      if (parent == null) {
        // Parent not available yet - register listener to apply when attached
        registerLayoutListener(node, tokens);
        return;
      }

      applySingleLayoutStyle(node, parent, token);
    }
  }

  /** Returns true when the token belongs to the gap family (gap-, gap-x-, gap-y-). */
  private static boolean isGapToken(String token) {
    return token.startsWith("gap-") || token.startsWith("gap-x-") || token.startsWith("gap-y-");
  }

  /**
   * Gets the effective parent pane, handling special cases.
   *
   * @param node the node whose parent to retrieve
   * @return the parent Pane if available, null otherwise
   */
  private static Pane getEffectiveParent(Node node) {
    Parent parent = node.getParent();
    if (parent instanceof Pane) {
      return (Pane) parent;
    }
    return null;
  }

  /**
   * Applies a single layout-dependent style token to a node.
   *
   * <p>This method dispatches to specific handlers based on the token type:
   *
   * <ul>
   *   <li>Margin tokens → {@link #applyMarginStyleViaStyles}
   *   <li>Gap tokens → {@link #applyGapStyle}
   *   <li>Flex tokens → {@link #applyFlexStyleViaStyles}
   *   <li>Grid container tokens → {@link #applyGridContainerStyle}
   *   <li>Grid item tokens → {@link #applyGridItemStyle}
   * </ul>
   *
   * @param node the node to apply the style to
   * @param parent the parent pane for context
   * @param token the layout token to apply
   */
  private static void applySingleLayoutStyle(Node node, Pane parent, String token) {
    if (isMarginToken(token)) {
      // Delegate to Styles.java for margin handling (supports negative -m-* tokens)
      applyMarginStyleViaStyles(node, token);
    } else if (isGapToken(token)) {
      // Gap is a container property. A gap token written on a CHILD must never mutate the parent:
      // this branch is intentionally a no-op fallback for deferred/retry application paths where
      // the original token was already handled against the node itself in
      // applyLayoutDependentStyles(). Apply gap classes to the container node instead.
      if (!(node instanceof Pane pane) || pane != parent) {
        return;
      }
      applyGapStyle(pane, token);
    } else if (token.equals("flex")
        || token.startsWith("flex-")
        || token.equals("grow")
        || token.equals("shrink")) {
      // Delegate to Styles.java for flex handling
      applyFlexStyleViaStyles(node, parent, token);
    } else if (token.startsWith("grid-cols-")
        || token.startsWith("grid-rows-")
        || token.startsWith("grid-flow-")) {
      // Grid container styles: apply to the node itself if it's a Pane
      if (node instanceof Pane pane) {
        applyGridContainerStyle(pane, token);
      }
    } else if (token.startsWith("col-span-") || token.startsWith("row-span-")) {
      // Grid item styles: apply via parent TwGridPane
      if (parent instanceof TwGridPane gridPane) {
        applyGridItemStyle(node, gridPane, token);
      }
    }
  }

  /**
   * Returns true when the token belongs to the margin family, including Tailwind's negative
   * variants (-m-*, -mx-*, -my-*, -mt-*, -mr-*, -mb-*, -ml-*).
   */
  private static boolean isMarginToken(String token) {
    // Negative margins: the leading "-" is followed by the margin prefix (e.g. "-m-4", "-ml-[8px]")
    if (token.startsWith("-m-")
        || token.startsWith("-mx-")
        || token.startsWith("-my-")
        || token.startsWith("-mt-")
        || token.startsWith("-mr-")
        || token.startsWith("-mb-")
        || token.startsWith("-ml-")) {
      return true;
    }
    return token.startsWith("m-")
        || token.startsWith("mx-")
        || token.startsWith("my-")
        || token.startsWith("mt-")
        || token.startsWith("mr-")
        || token.startsWith("mb-")
        || token.startsWith("ml-");
  }

  /**
   * Delegates margin application to Styles.java methods. Supports numeric values (m-4), arbitrary
   * values (m-[20px]) and Tailwind negative margins (-m-4, -ml-[8px]).
   *
   * <p>Negative insets are valid for {@code HBox.setMargin} / {@code VBox.setMargin} / {@code
   * GridPane.setMargin} in JavaFX: the layout managers add the margin value to the node position,
   * so a negative margin simply shifts/overlaps the child toward its neighbours.
   */
  private static void applyMarginStyleViaStyles(Node node, String token) {
    // Normalize negative margin tokens: strip the leading "-", parse the remainder, negate result.
    boolean negative = token.startsWith("-");
    if (negative) {
      token = token.substring(1);
    }

    // Compute the margin in PIXELS up front. Every branch below routes through
    // Styles.margin(node, topPx, rightPx, bottomPx, leftPx), which takes raw pixel values.
    // The Styles.m/mx/my/... helpers must NOT be used here: they multiply by their own
    // hardcoded UNIT (4.0), while LayoutApplier already honors TwConfig.unit(), so mixing
    // both paths would double-scale margins under non-default unit configurations.
    double px;
    if (token.contains("[")) {
      // Arbitrary value syntax: m-[20px], m-[2.5rem], etc. — parseCssValue returns pixels.
      int start = token.indexOf('[') + 1;
      int end = token.indexOf(']');
      if (start <= 0 || end <= start) {
        return; // Malformed bracket syntax - skip silently (parseTailwindValue logs in debug)
      }
      px = parseCssValue(token.substring(start, end));
    } else {
      // Numeric Tailwind scale value (m-4): parseTailwindValue returns scale units, convert once.
      double value = parseTailwindValue(token);
      if (Double.isNaN(value)) {
        return; // Invalid / non-numeric tail (e.g. "mx-auto") - skip application
      }
      px = value * TwConfig.unit();
    }

    if (Double.isNaN(px)) {
      return; // Invalid value - skip application instead of silently using 0
    }
    if (negative) {
      px = -px;
    }

    if (token.startsWith("m-")) {
      Styles.margin(node, px, px, px, px);
    } else if (token.startsWith("mx-")) {
      Styles.margin(node, 0, px, 0, px);
    } else if (token.startsWith("my-")) {
      Styles.margin(node, px, 0, px, 0);
    } else if (token.startsWith("mt-")) {
      Styles.margin(node, px, 0, 0, 0);
    } else if (token.startsWith("mr-")) {
      Styles.margin(node, 0, px, 0, 0);
    } else if (token.startsWith("mb-")) {
      Styles.margin(node, 0, 0, px, 0);
    } else if (token.startsWith("ml-")) {
      Styles.margin(node, 0, 0, 0, px);
    }
  }

  /**
   * Delegates flex application to TwFlexPane or Styles.java methods.
   *
   * <p>For TwFlexPane containers, uses precise grow factors via {@link TwFlexPane#setGrow}. For
   * HBox/VBox, maps to Priority enum (NEVER/SOMETIMES/ALWAYS).
   *
   * @param node the node to apply flex to
   * @param parent the parent pane
   * @param token the flex token (e.g., "flex-1", "flex-[2]", "grow", "shrink")
   */
  private static void applyFlexStyleViaStyles(Node node, Pane parent, String token) {
    // Prioritize TwFlexPane if parent is TwFlexPane
    if (parent instanceof TwFlexPane flexPane) {
      applyFlexForTwFlexPane(node, token);
      return;
    }

    // Fallback to HBox/VBox with Styles.java
    // Bare "flex" mirrors Tailwind's `display:flex` shorthand behavior on children:
    // equivalent to flex: 1 1 0% (grow=1, shrink=1), same treatment as flex-1/flex-auto.
    if (token.equals("grow") || token.equals("flex-1") || token.equals("flex")) {
      if (parent instanceof HBox) {
        Styles.flex1(node);
      } else if (parent instanceof VBox) {
        Styles.vgrow(node);
      }
    } else if (token.equals("shrink") || token.equals("flex-none")) {
      Styles.growNone(node);
    } else if (token.equals("flex-auto")) {
      if (parent instanceof HBox) {
        Styles.flexAuto(node);
      } else if (parent instanceof VBox) {
        VBox.setVgrow(node, Priority.SOMETIMES);
      }
    } else if (token.equals("flex-initial")) {
      if (parent instanceof HBox) {
        HBox.setHgrow(node, Priority.SOMETIMES);
      } else if (parent instanceof VBox) {
        VBox.setVgrow(node, Priority.SOMETIMES);
      }
    } else if (token.startsWith("flex-")) {
      // Handle arbitrary flex values like flex-[2]
      try {
        String value = token.substring(5);
        if (value.startsWith("[") && value.endsWith("]")) {
          value = value.substring(1, value.length() - 1);
        }
        double flexValue = Double.parseDouble(value);
        // For arbitrary flex values in HBox/VBox, use ALWAYS priority with the actual factor
        // Note: JavaFX HBox/VBox only supports Priority enum (NEVER/SOMETIMES/ALWAYS)
        // and does not expose a public API to set custom grow factors.
        // For precise flex factor control, use TwFlexPane instead of HBox/VBox.
        // This maps flex-[N] where N > 0 to ALWAYS priority (equivalent to flex-1 behavior)
        // while documenting the limitation for HBox/VBox containers.
        if (parent instanceof HBox) {
          HBox.setHgrow(node, flexValue > 0 ? Priority.ALWAYS : Priority.NEVER);
        } else if (parent instanceof VBox) {
          VBox.setVgrow(node, flexValue > 0 ? Priority.ALWAYS : Priority.NEVER);
        }
      } catch (NumberFormatException e) {
        // Ignore invalid flex values
      }
    }
  }

  /**
   * Applies flex styles specifically for TwFlexPane container.
   *
   * <p>TwFlexPane supports precise grow factors via {@link TwFlexPane#setGrow(Node, double)}.
   *
   * @param node the node to apply flex to
   * @param token the flex token
   */
  private static void applyFlexForTwFlexPane(Node node, String token) {
    // Bare "flex" == flex: 1 1 0% (grow=1, shrink=1), matching Tailwind's default flex item sizing.
    if (token.equals("grow") || token.equals("flex-1")) {
      TwFlexPane.setGrow(node, 1);
    } else if (token.equals("flex")) {
      TwFlexPane.setGrow(node, 1);
      TwFlexPane.setShrink(node, 1);
    } else if (token.equals("shrink") || token.equals("flex-none")) {
      TwFlexPane.setShrink(node, 0);
    } else if (token.equals("flex-auto")) {
      TwFlexPane.setGrow(node, 1);
      TwFlexPane.setShrink(node, 1);
    } else if (token.equals("flex-initial")) {
      TwFlexPane.setGrow(node, 0);
      TwFlexPane.setShrink(node, 1);
    } else if (token.startsWith("flex-")) {
      // Handle arbitrary flex values like flex-[2]
      try {
        String value = token.substring(5);
        if (value.startsWith("[") && value.endsWith("]")) {
          value = value.substring(1, value.length() - 1);
        }
        double flexValue = Double.parseDouble(value);
        TwFlexPane.setGrow(node, flexValue);
      } catch (NumberFormatException e) {
        // Ignore invalid flex values
      }
    }
  }

  /**
   * Parses gap value from token and applies it to the container pane that carries the token.
   * Supports both numeric values (gap-4) and arbitrary values (gap-[20px]).
   *
   * @param container the pane to apply gap to (the node annotated with the gap-* class)
   * @param token the gap token (e.g., "gap-4", "gap-[20px]")
   */
  private static void applyGapStyle(Pane container, String token) {
    double px;

    // Check for arbitrary value syntax: gap-[20px], gap-[2.5rem], etc.
    if (token.contains("[")) {
      int start = token.indexOf('[') + 1;
      int end = token.indexOf(']');
      if (start > 0 && end > start) {
        String value = token.substring(start, end);
        px = parseCssValue(value);
      } else {
        double value = parseTailwindValue(token);
        if (Double.isNaN(value)) {
          return;
        }
        px = value * TwConfig.unit();
      }
    } else {
      double value = parseTailwindValue(token);
      if (Double.isNaN(value)) {
        return;
      }
      px = value * TwConfig.unit();
    }
    if (Double.isNaN(px)) {
      return;
    }

    // Prioritize TwFlexPane if parent is TwFlexPane
    if (container instanceof TwFlexPane flexPane) {
      if (token.startsWith("gap-x-")) {
        flexPane.gapX(px);
      } else if (token.startsWith("gap-y-")) {
        flexPane.gapY(px);
      } else {
        flexPane.gap(px);
      }
      return;
    }

    // Prioritize TwGridPane if parent is TwGridPane
    if (container instanceof TwGridPane gridPane) {
      if (token.startsWith("gap-x-")) {
        gridPane.gapX(px);
      } else if (token.startsWith("gap-y-")) {
        gridPane.gapY(px);
      } else {
        gridPane.gap(px);
      }
      return;
    }

    // Fallback to standard JavaFX panes
    if (container instanceof HBox hbox) {
      if (token.startsWith("gap-x-")) {
        hbox.setSpacing(px);
      } else if (token.startsWith("gap-y-")) {
        // HBox doesn't support vertical gap directly
      } else {
        hbox.setSpacing(px);
      }
    } else if (container instanceof VBox vbox) {
      if (token.startsWith("gap-y-")) {
        vbox.setSpacing(px);
      } else if (token.startsWith("gap-x-")) {
        // VBox doesn't support horizontal gap directly
      } else {
        vbox.setSpacing(px);
      }
    } else if (container instanceof GridPane grid) {
      if (token.startsWith("gap-x-")) {
        grid.setHgap(px);
      } else if (token.startsWith("gap-y-")) {
        grid.setVgap(px);
      } else {
        grid.setHgap(px);
        grid.setVgap(px);
      }
    }
  }

  /**
   * Applies grid container styles (grid-cols-*, grid-rows-*, grid-flow-*) to a Pane node.
   *
   * @param pane the pane to apply grid styles to (must be TwGridPane)
   * @param token the grid container token
   */
  private static void applyGridContainerStyle(Pane pane, String token) {
    // Only applies if the pane is a TwGridPane
    if (!(pane instanceof TwGridPane gridPane)) {
      return;
    }

    if (token.startsWith("grid-cols-")) {
      double parsed = parseTailwindValue(token);
      if (Double.isNaN(parsed)) {
        LOGGER.warning(
            "Invalid grid-cols value in token '" + token + "': could not be parsed; skipped.");
        return;
      }
      int cols = (int) parsed;
      if (cols <= 0) {
        // Guard against division-by-zero in the layout engine (cellW = w / gridCols).
        LOGGER.warning(
            "Invalid grid-cols value in token '"
                + token
                + "': must be >= 1; grid configuration skipped.");
        return;
      }
      gridPane.cols(cols);
    } else if (token.startsWith("grid-rows-")) {
      double parsed = parseTailwindValue(token);
      if (Double.isNaN(parsed)) {
        LOGGER.warning(
            "Invalid grid-rows value in token '" + token + "': could not be parsed; skipped.");
        return;
      }
      int rows = (int) parsed;
      if (rows < 0) {
        LOGGER.warning(
            "Invalid grid-rows value in token '"
                + token
                + "': must be >= 0 (0 = inferred); grid configuration skipped.");
        return;
      }
      gridPane.rows(rows);
    } else if (token.equals("grid-flow-row")) {
      gridPane.autoFlow(TwGridPane.AutoFlow.ROW);
    } else if (token.equals("grid-flow-col")) {
      gridPane.autoFlow(TwGridPane.AutoFlow.COL);
    } else if (token.equals("grid-flow-dense") || token.equals("grid-flow-row-dense")) {
      gridPane.autoFlow(TwGridPane.AutoFlow.ROW_DENSE);
    } else if (token.equals("grid-flow-col-dense")) {
      gridPane.autoFlow(TwGridPane.AutoFlow.COL_DENSE);
    }
  }

  /**
   * Applies grid item styles (col-span-*, row-span-*) to a node via its parent TwGridPane.
   *
   * @param node the node to apply grid item styles to
   * @param gridPane the parent TwGridPane
   * @param token the grid item token
   */
  private static void applyGridItemStyle(Node node, TwGridPane gridPane, String token) {
    if (token.startsWith("col-span-")) {
      double parsed = parseTailwindValue(token);
      if (Double.isNaN(parsed) || parsed < 1) {
        LOGGER.warning("Invalid span value in token '" + token + "': must be >= 1; skipped.");
        return;
      }
      TwGridPane.setColSpan(node, (int) parsed);
    } else if (token.startsWith("row-span-")) {
      double parsed = parseTailwindValue(token);
      if (Double.isNaN(parsed) || parsed < 1) {
        LOGGER.warning("Invalid span value in token '" + token + "': must be >= 1; skipped.");
        return;
      }
      TwGridPane.setRowSpan(node, (int) parsed);
    }
  }

  /**
   * Parses CSS value string to pixels using configured unit size.
   *
   * <p>Supports:
   *
   * <ul>
   *   <li>Pixels: "20px" → 20.0
   *   <li>Rem: "1.5rem" → 1.5 * TwConfig.unit()
   *   <li>Em: "2em" → 2.0 * TwConfig.unit()
   *   <li>Plain numbers: "16" → 16.0
   * </ul>
   *
   * @param value the CSS value string to parse
   * @return the value in pixels
   */
  private static double parseCssValue(String value) {
    if (value.endsWith("px")) {
      try {
        return Double.parseDouble(value.substring(0, value.length() - 2));
      } catch (NumberFormatException e) {
        if (TwConfig.isDebug()) {
          System.out.println("[TailwindFX Warning] Invalid px value: " + value);
        }
        return Double.NaN;
      }
    } else if (value.endsWith("rem")) {
      try {
        double rem = Double.parseDouble(value.substring(0, value.length() - 3));
        return rem * TwConfig.unit();
      } catch (NumberFormatException e) {
        if (TwConfig.isDebug()) {
          System.out.println("[TailwindFX Warning] Invalid rem value: " + value);
        }
        return Double.NaN;
      }
    } else if (value.endsWith("em")) {
      try {
        double em = Double.parseDouble(value.substring(0, value.length() - 2));
        return em * TwConfig.unit();
      } catch (NumberFormatException e) {
        if (TwConfig.isDebug()) {
          System.out.println("[TailwindFX Warning] Invalid em value: " + value);
        }
        return Double.NaN;
      }
    } else {
      try {
        return Double.parseDouble(value);
      } catch (NumberFormatException e) {
        if (TwConfig.isDebug()) {
          System.out.println("[TailwindFX Warning] Invalid numeric value: " + value);
        }
        return Double.NaN;
      }
    }
  }

  /**
   * Parses numeric value from Tailwind token.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>"m-4" → 4
   *   <li>"m-[16px]" → 16px / TwConfig.unit()
   *   <li>"-m-4" → -4
   * </ul>
   *
   * @param token the Tailwind token to parse
   * @return the numeric value respecting TwConfig.unit(), or {@link Double#NaN} when the value
   *     cannot be parsed. NaN is an error sentinel only — a literal {@code 0} (e.g. "m-0") is a
   *     legitimate parsed result and must never be confused with a parse failure.
   */
  private static double parseTailwindValue(String token) {
    // Handle arbitrary values like m-[16px]
    if (token.contains("[")) {
      int start = token.indexOf('[') + 1;
      int end = token.indexOf(']');

      // Verify closing bracket exists to avoid StringIndexOutOfBoundsException
      if (end == -1) {
        if (TwConfig.isDebug()) {
          System.out.println("[TailwindFX Warning] Missing closing bracket in token: " + token);
        }
        return Double.NaN;
      }

      String value = token.substring(start, end);
      if (value.endsWith("px")) {
        try {
          // Use double arithmetic respecting TwConfig.unit() like parseCssValue does for rem/em
          double pxValue = Double.parseDouble(value.substring(0, value.length() - 2));
          return pxValue / TwConfig.unit();
        } catch (NumberFormatException e) {
          if (TwConfig.isDebug()) {
            System.out.println("[TailwindFX Warning] Invalid px value in token: " + token);
          }
          return Double.NaN;
        }
      }
      try {
        return Double.parseDouble(value);
      } catch (NumberFormatException e) {
        if (TwConfig.isDebug()) {
          System.out.println("[TailwindFX Warning] Invalid numeric value in token: " + token);
        }
        return Double.NaN;
      }
    }

    // Handle negative values
    boolean negative = token.startsWith("-");
    String cleanToken = negative ? token.substring(1) : token;

    // Extract numeric part after last hyphen
    int lastHyphen = cleanToken.lastIndexOf('-');
    if (lastHyphen >= 0 && lastHyphen < cleanToken.length() - 1) {
      String numPart = cleanToken.substring(lastHyphen + 1);
      try {
        int value = Integer.parseInt(numPart);
        return negative ? -value : value;
      } catch (NumberFormatException e) {
        // Named scale keywords ("mt-auto", "w-full", "h-screen", "p-px", ...) are valid Tailwind
        // utilities that simply carry no numeric measurement. They are expected here, so they must
        // not be reported as warnings; only genuinely malformed tails are worth flagging.
        if (!isNamedScaleKeyword(numPart) && TwConfig.isDebug()) {
          System.out.println("[TailwindFX Warning] Non-numeric value in token: " + token);
        }
        return Double.NaN;
      }
    }
    return Double.NaN;
  }

  /**
   * Checks whether a value tail is a named Tailwind keyword instead of a malformed token.
   *
   * <p>These tokens are recognized by {@code TokenRegistry} and handled (or intentionally ignored)
   * elsewhere, so returning {@code NaN} for them is the expected outcome rather than an error.
   *
   * @param valuePart the token tail after the last hyphen (e.g. {@code auto} in {@code mt-auto})
   * @return true if the tail is a recognized non-numeric Tailwind keyword
   */
  private static boolean isNamedScaleKeyword(String valuePart) {
    return NAMED_SCALE_KEYWORDS.contains(valuePart);
  }

  /**
   * Registers a listener to apply layout styles when the node is attached to (or re-attached under)
   * a Pane parent. Uses WeakReference to prevent memory leaks.
   *
   * <p><b>Re-parenting design decision:</b> the listener intentionally stays alive after the first
   * successful application so that layout styles are re-applied whenever the node moves between
   * parents (virtualization / cell recycling scenarios). It only removes itself once the node has
   * been garbage collected (weak reference cleared), which bounds its lifetime without leaking.
   *
   * <p>Bug #10 fix: Converted local ListenerWrapper class to use AtomicReference for safe
   * self-removal of the listener when the node becomes unreachable.
   *
   * @param node the node to register listener for
   * @param tokens the tokens to apply when a Pane parent becomes available
   */
  private static void registerLayoutListener(Node node, List<String> tokens) {
    // Check if node is already attached (race condition)
    if (node.getParent() instanceof Pane pane) {
      for (String token : tokens) {
        applySingleLayoutStyle(node, pane, token);
      }
      return;
    }

    // Use WeakReference to prevent memory leaks if node is garbage collected
    WeakReference<Node> weakNode = new WeakReference<>(node);

    // Use AtomicReference for safe self-removal of the listener (Bug #10 fix)
    AtomicReference<ChangeListener<Parent>> listenerRef = new AtomicReference<>();

    ChangeListener<Parent> listener =
        (obs, oldParent, newParent) -> {
          Node actualNode = weakNode.get();
          if (actualNode == null) {
            // Node was garbage collected, remove listener — this is the ONLY self-removal path.
            ChangeListener<Parent> listenerToRemove = listenerRef.getAndSet(null);
            if (listenerToRemove != null) {
              obs.removeListener(listenerToRemove);
            }
            return;
          }

          if (newParent instanceof Pane pane) {
            // Keep the listener registered so future re-parenting events re-apply the layout
            // styles against the new parent (supports virtualized/recycled cell containers).
            for (String token : tokens) {
              applySingleLayoutStyle(actualNode, pane, token);
            }
          }
        };

    listenerRef.set(listener);
    node.parentProperty().addListener(listener);
  }
}
