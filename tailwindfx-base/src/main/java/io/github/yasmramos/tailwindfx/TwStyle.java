package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.core.ColorUtilityValidator;
import io.github.yasmramos.tailwindfx.core.Preconditions;
import io.github.yasmramos.tailwindfx.core.TokenParser;
import io.github.yasmramos.tailwindfx.core.TokenRegistry;
import io.github.yasmramos.tailwindfx.core.UtilityConflictResolver;
import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import io.github.yasmramos.tailwindfx.style.StyleMerger;
import io.github.yasmramos.tailwindfx.style.StylePerf;
import io.github.yasmramos.tailwindfx.style.Styles;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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
 * TwStyle.jit(node, "bg-blue-500/80", "p-[13px]");
 * TwStyle.remove(node, "old-class");
 * TwStyle.toggle(node, "active");
 * </pre>
 */
public final class TwStyle {

  private static final TwStyle INSTANCE = new TwStyle();

  private static final Set<String> RESPONSIVE_PREFIXES =
      new HashSet<>(Arrays.asList("sm:", "md:", "lg:", "xl:", "2xl:"));

  private static final Set<String> STATE_PREFIXES =
      new HashSet<>(
          Arrays.asList("hover:", "focus:", "active:", "disabled:", "visited:", "checked:"));

  private TwStyle() {}

  /** Applies utility classes and JIT tokens to a node with intelligent auto-detection. */
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

    // Apply effect tokens via TwEffect
    if (!result.effectTokens().isEmpty()) {
      for (String effectToken : result.effectTokens()) {
        applyEffectToken(node, effectToken);
      }
    }

    // If migration is needed, delegate to TwLayout and skip JIT for those tokens
    // Moved to BEGINNING before any mutations to avoid leaving node half-styled
    if (!result.layoutMigrationTokens().isEmpty()) {
      // Degrade gracefully with warning instead of throwing exception
      // This prevents runtime crashes and allows consumer to continue
      if (TwConfig.isDebug()) {
        System.out.println(
            "[TailwindFX Warning] Layout classes requiring container migration ("
                + String.join(", ", result.layoutMigrationTokens())
                + ") should be applied using TailwindFX.layout() instead of TailwindFX.apply().");
      }
      // Delegate these tokens to TwLayout for proper handling
      // For now, we skip them to avoid partial styling
    }

    if (!result.cssClasses().isEmpty()) {
      UtilityConflictResolver.applyAll(node, result.cssClasses().toArray(new String[0]));
      TailwindFXMetrics.instance().recordApply(result.cssClasses().size());
    }

    // Apply layout-dependent styles first (needs parent context)
    if (!result.layoutDependentTokens().isEmpty()) {
      applyLayoutDependentStyles(node, result.layoutDependentTokens());
    }

    // Apply variant tokens via VariantManager
    if (!result.variantTokens().isEmpty()) {
      for (String variantToken : result.variantTokens()) {
        io.github.yasmramos.tailwindfx.core.VariantManager.processToken(
            node, variantToken, new io.github.yasmramos.tailwindfx.core.JitCompiler());
      }
    }

    // Handle unknown tokens with debug warning (Smart fallback as documented in README)
    // Warnings collected during single pass to avoid redundant iteration
    if (!result.unknownTokens().isEmpty() && io.github.yasmramos.tailwindfx.TwConfig.isDebug()) {
      for (String t : result.unknownTokens()) {
        System.out.println("[TailwindFX Warning] Unknown token ignored: " + t);
      }
    }

    if (!result.jitTokens().isEmpty()) {
      // Check preferStylesheet mode: apply classes from AOT stylesheet when available,
      // fallback to JIT inline for dynamic/arbitrary values
      if (io.github.yasmramos.tailwindfx.TwConfig.isPreferStylesheet()) {
        applyWithStylesheetPreference(node, result.jitTokens().toArray(new String[0]));
      } else {
        StyleMerger.applyJit(node, result.jitTokens().toArray(new String[0]));
      }
    }
  }

  /**
   * Applies tokens with stylesheet preference mode. When preferStylesheet is enabled, this method
   * adds CSS classes for tokens that exist in the AOT-generated stylesheet, and only uses JIT
   * inline compilation as fallback for dynamic/arbitrary values not resolved at build-time.
   *
   * @param node the target node
   * @param tokens the tokens to apply
   */
  private static void applyWithStylesheetPreference(Node node, String... tokens) {
    // Separate tokens into:
    // 1. Static tokens (no arbitrary values) → apply as CSS class
    // 2. Dynamic/arbitrary tokens ([...], /opacity) → fallback to JIT inline
    java.util.List<String> staticTokens = new java.util.ArrayList<>();
    java.util.List<String> dynamicTokens = new java.util.ArrayList<>();

    for (String token : tokens) {
      if (token == null || token.isBlank()) continue;

      // Check if token contains arbitrary value syntax [...] or opacity modifier /
      // Opacity modifier: only valid for color utilities (use same validation as isJitToken)
      boolean hasArbitraryValue = token.contains("[") && token.contains("]");
      boolean hasOpacityModifier = false;
      
      // Use the same validation logic as isJitToken to avoid false positives like "icon/large"
      if (token.contains("/")) {
        int slashIndex = token.indexOf('/');
        if (slashIndex > 0) {
          String base = token.substring(0, slashIndex);
          hasOpacityModifier = isValidColorUtilityBase(base);
        }
      }

      if (hasArbitraryValue || hasOpacityModifier) {
        dynamicTokens.add(token);
      } else {
        staticTokens.add(token);
      }
    }

    // Apply static tokens as CSS classes (AOT stylesheet will handle them)
    if (!staticTokens.isEmpty()) {
      for (String cls : staticTokens) {
        if (!node.getStyleClass().contains(cls)) {
          node.getStyleClass().add(cls);
        }
      }
      // Use UtilityConflictResolver for conflict resolution between classes of same category
      UtilityConflictResolver.applyAll(node, staticTokens.toArray(new String[0]));
    }

    // Fallback to JIT inline for dynamic/arbitrary values
    if (!dynamicTokens.isEmpty()) {
      StyleMerger.applyJit(node, dynamicTokens.toArray(new String[0]));
    }
  }

  /**
   * Applies layout-dependent styles that require parent context (margins, gaps, flex). These cannot
   * be handled by CSS alone in JavaFX.
   */
  private static void applyLayoutDependentStyles(Node node, java.util.List<String> tokens) {
    javafx.scene.layout.Pane parent = getEffectiveParent(node);

    for (String token : tokens) {
      // For gap styles, the node itself is the container
      if (token.startsWith("gap-") || token.startsWith("gap-x-") || token.startsWith("gap-y-")) {
        if (node instanceof javafx.scene.layout.Pane pane) {
          applyGapStyle(pane, token);
        }
        continue;
      }

      // For margin and flex styles, we need the parent
      if (parent == null) {
        // Parent not available yet - register listener to apply when attached
        registerLayoutListener(node, tokens);
        return;
      }

      applySingleLayoutStyle(node, parent, token);
    }
  }

  /** Gets the effective parent pane, handling special cases like TwFlexPane. */
  private static javafx.scene.layout.Pane getEffectiveParent(Node node) {
    javafx.scene.Parent parent = node.getParent();
    if (parent instanceof javafx.scene.layout.Pane) {
      return (javafx.scene.layout.Pane) parent;
    }
    return null;
  }

  /** Applies a single layout-dependent style token to a node. */
  private static void applySingleLayoutStyle(
      Node node, javafx.scene.layout.Pane parent, String token) {
    if (token.startsWith("m-")
        || token.startsWith("mx-")
        || token.startsWith("my-")
        || token.startsWith("mt-")
        || token.startsWith("mr-")
        || token.startsWith("mb-")
        || token.startsWith("ml-")) {
      // Delegate to Styles.java for margin handling
      applyMarginStyleViaStyles(node, token);
    } else if (token.startsWith("gap-")
        || token.startsWith("gap-x-")
        || token.startsWith("gap-y-")) {
      applyGapStyle(parent, token);
    } else if (token.startsWith("flex-") || token.equals("grow") || token.equals("shrink")) {
      // Delegate to Styles.java for flex handling
      applyFlexStyleViaStyles(node, parent, token);
    } else if (token.startsWith("grid-cols-")
        || token.startsWith("grid-rows-")
        || token.startsWith("grid-flow-")) {
      // Grid container styles: apply to the node itself if it's a Pane
      if (node instanceof javafx.scene.layout.Pane pane) {
        applyGridContainerStyle(pane, token);
      }
    } else if (token.startsWith("col-span-") || token.startsWith("row-span-")) {
      // Grid item styles: apply via parent TwGridPane
      if (parent instanceof io.github.yasmramos.tailwindfx.layout.TwGridPane gridPane) {
        applyGridItemStyle(node, gridPane, token);
      }
    }
  }

  /**
   * Delegates margin application to Styles.java methods. Supports both numeric values (m-4) and
   * arbitrary values (m-[20px]).
   */
  private static void applyMarginStyleViaStyles(Node node, String token) {
    // Check for arbitrary value syntax: m-[20px], m-[2.5rem], etc.
    if (token.contains("[")) {
      int start = token.indexOf('[') + 1;
      int end = token.indexOf(']');
      if (start > 0 && end > start) {
        String valueStr = token.substring(start, end);
        double px = parseCssValue(valueStr);

        if (token.startsWith("m-[")) {
          Styles.margin(node, px, px, px, px);
        } else if (token.startsWith("mx-[")) {
          Styles.margin(node, 0, px, 0, px);
        } else if (token.startsWith("my-[")) {
          Styles.margin(node, px, 0, px, 0);
        } else if (token.startsWith("mt-[")) {
          Styles.margin(node, px, 0, 0, 0);
        } else if (token.startsWith("mr-[")) {
          Styles.margin(node, 0, px, 0, 0);
        } else if (token.startsWith("mb-[")) {
          Styles.margin(node, 0, 0, px, 0);
        } else if (token.startsWith("ml-[")) {
          Styles.margin(node, 0, 0, 0, px);
        }
        return;
      }
    }

    // Fallback to numeric parsing for standard values
    double value = parseTailwindValue(token);

    if (token.startsWith("m-")) {
      Styles.m(node, (int) value);
    } else if (token.startsWith("mx-")) {
      Styles.mx(node, (int) value);
    } else if (token.startsWith("my-")) {
      Styles.my(node, (int) value);
    } else if (token.startsWith("mt-")) {
      Styles.mt(node, (int) value);
    } else if (token.startsWith("mr-")) {
      Styles.mr(node, (int) value);
    } else if (token.startsWith("mb-")) {
      Styles.mb(node, (int) value);
    } else if (token.startsWith("ml-")) {
      Styles.ml(node, (int) value);
    }
  }

  /** Delegates flex application to TwFlexPane or Styles.java methods. */
  private static void applyFlexStyleViaStyles(
      Node node, javafx.scene.layout.Pane parent, String token) {
    // Prioritize TwFlexPane if parent is TwFlexPane
    if (parent instanceof io.github.yasmramos.tailwindfx.layout.TwFlexPane flexPane) {
      applyFlexForTwFlexPane(node, token);
      return;
    }

    // Fallback to HBox/VBox with Styles.java
    if (token.equals("grow") || token.equals("flex-1")) {
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

  /** Applies flex styles specifically for TwFlexPane container. */
  private static void applyFlexForTwFlexPane(Node node, String token) {
    if (token.equals("grow") || token.equals("flex-1")) {
      io.github.yasmramos.tailwindfx.layout.TwFlexPane.setGrow(node, 1);
    } else if (token.equals("shrink") || token.equals("flex-none")) {
      io.github.yasmramos.tailwindfx.layout.TwFlexPane.setShrink(node, 0);
    } else if (token.equals("flex-auto")) {
      io.github.yasmramos.tailwindfx.layout.TwFlexPane.setGrow(node, 1);
      io.github.yasmramos.tailwindfx.layout.TwFlexPane.setShrink(node, 1);
    } else if (token.equals("flex-initial")) {
      io.github.yasmramos.tailwindfx.layout.TwFlexPane.setGrow(node, 0);
      io.github.yasmramos.tailwindfx.layout.TwFlexPane.setShrink(node, 1);
    } else if (token.startsWith("flex-")) {
      // Handle arbitrary flex values like flex-[2]
      try {
        String value = token.substring(5);
        if (value.startsWith("[") && value.endsWith("]")) {
          value = value.substring(1, value.length() - 1);
        }
        double flexValue = Double.parseDouble(value);
        io.github.yasmramos.tailwindfx.layout.TwFlexPane.setGrow(node, flexValue);
      } catch (NumberFormatException e) {
        // Ignore invalid flex values
      }
    }
  }

  /**
   * Parses gap value from token and applies it to parent container. Supports both numeric values
   * (gap-4) and arbitrary values (gap-[20px]).
   */
  private static void applyGapStyle(javafx.scene.layout.Pane parent, String token) {
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
        px = value * TwConfig.unit();
      }
    } else {
      double value = parseTailwindValue(token);
      px = value * TwConfig.unit();
    }

    // Prioritize TwFlexPane if parent is TwFlexPane
    if (parent instanceof io.github.yasmramos.tailwindfx.layout.TwFlexPane flexPane) {
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
    if (parent instanceof io.github.yasmramos.tailwindfx.layout.TwGridPane gridPane) {
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
    if (parent instanceof HBox hbox) {
      if (token.startsWith("gap-x-")) {
        hbox.setSpacing(px);
      } else if (token.startsWith("gap-y-")) {
        // HBox doesn't support vertical gap directly
      } else {
        hbox.setSpacing(px);
      }
    } else if (parent instanceof VBox vbox) {
      if (token.startsWith("gap-y-")) {
        vbox.setSpacing(px);
      } else if (token.startsWith("gap-x-")) {
        // VBox doesn't support horizontal gap directly
      } else {
        vbox.setSpacing(px);
      }
    } else if (parent instanceof GridPane grid) {
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

  /** Applies grid container styles (grid-cols-*, grid-rows-*, grid-flow-*) to a Pane node. */
  private static void applyGridContainerStyle(javafx.scene.layout.Pane pane, String token) {
    // Only applies if the pane is a TwGridPane
    if (!(pane instanceof io.github.yasmramos.tailwindfx.layout.TwGridPane gridPane)) {
      return;
    }

    if (token.startsWith("grid-cols-")) {
      int cols = (int) parseTailwindValue(token);
      gridPane.cols(cols);
    } else if (token.startsWith("grid-rows-")) {
      int rows = (int) parseTailwindValue(token);
      gridPane.rows(rows);
    } else if (token.equals("grid-flow-row")) {
      gridPane.autoFlow(io.github.yasmramos.tailwindfx.layout.TwGridPane.AutoFlow.ROW);
    } else if (token.equals("grid-flow-col")) {
      gridPane.autoFlow(io.github.yasmramos.tailwindfx.layout.TwGridPane.AutoFlow.COL);
    } else if (token.equals("grid-flow-dense") || token.equals("grid-flow-row-dense")) {
      gridPane.autoFlow(io.github.yasmramos.tailwindfx.layout.TwGridPane.AutoFlow.ROW_DENSE);
    } else if (token.equals("grid-flow-col-dense")) {
      gridPane.autoFlow(io.github.yasmramos.tailwindfx.layout.TwGridPane.AutoFlow.COL_DENSE);
    }
  }

  /** Applies grid item styles (col-span-*, row-span-*) to a node via its parent TwGridPane. */
  private static void applyGridItemStyle(
      Node node, io.github.yasmramos.tailwindfx.layout.TwGridPane gridPane, String token) {
    if (token.startsWith("col-span-")) {
      int span = (int) parseTailwindValue(token);
      io.github.yasmramos.tailwindfx.layout.TwGridPane.setColSpan(node, span);
    } else if (token.startsWith("row-span-")) {
      int span = (int) parseTailwindValue(token);
      io.github.yasmramos.tailwindfx.layout.TwGridPane.setRowSpan(node, span);
    }
  }

  /** Parses CSS value string to pixels using configured unit size. */
  private static double parseCssValue(String value) {
    if (value.endsWith("px")) {
      try {
        return Double.parseDouble(value.substring(0, value.length() - 2));
      } catch (NumberFormatException e) {
        if (TwConfig.isDebug()) {
          System.out.println("[TailwindFX Warning] Invalid px value: " + value);
        }
        return 0;
      }
    } else if (value.endsWith("rem")) {
      try {
        double rem = Double.parseDouble(value.substring(0, value.length() - 3));
        return rem * TwConfig.unit();
      } catch (NumberFormatException e) {
        if (TwConfig.isDebug()) {
          System.out.println("[TailwindFX Warning] Invalid rem value: " + value);
        }
        return 0;
      }
    } else if (value.endsWith("em")) {
      try {
        double em = Double.parseDouble(value.substring(0, value.length() - 2));
        return em * TwConfig.unit();
      } catch (NumberFormatException e) {
        if (TwConfig.isDebug()) {
          System.out.println("[TailwindFX Warning] Invalid em value: " + value);
        }
        return 0;
      }
    } else {
      try {
        return Double.parseDouble(value);
      } catch (NumberFormatException e) {
        if (TwConfig.isDebug()) {
          System.out.println("[TailwindFX Warning] Invalid numeric value: " + value);
        }
        return 0;
      }
    }
  }

  /** Parses numeric value from Tailwind token (e.g., "m-4" -> 4, "p-[16px]" -> 4). */
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
        return 0;
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
          return 0;
        }
      }
      try {
        return Double.parseDouble(value);
      } catch (NumberFormatException e) {
        if (TwConfig.isDebug()) {
          System.out.println("[TailwindFX Warning] Invalid numeric value in token: " + token);
        }
        return 0;
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
        // Handle non-numeric values like "auto", "full"
        if (TwConfig.isDebug()) {
          System.out.println("[TailwindFX Warning] Non-numeric value in token: " + token);
        }
        return 0;
      }
    }
    return 0;
  }

  /**
   * Registers a listener to apply layout styles when node is attached to parent. Uses WeakReference
   * to prevent memory leaks.
   */
  private static void registerLayoutListener(Node node, java.util.List<String> tokens) {
    // Check if node is already attached (race condition)
    if (node.getParent() instanceof javafx.scene.layout.Pane pane) {
      for (String token : tokens) {
        applySingleLayoutStyle(node, pane, token);
      }
      return;
    }

    // Use WeakReference to prevent memory leaks if node is garbage collected
    java.lang.ref.WeakReference<Node> weakNode = new java.lang.ref.WeakReference<>(node);

    // Create wrapper to hold listener reference
    class ListenerWrapper {
      javafx.beans.value.ChangeListener<javafx.scene.Parent> listener;
    }
    final ListenerWrapper wrapper = new ListenerWrapper();

    wrapper.listener =
        (obs, oldParent, newParent) -> {
          Node actualNode = weakNode.get();
          if (actualNode == null) {
            // Node was garbage collected, remove listener
            if (wrapper.listener != null) {
              obs.removeListener(wrapper.listener);
            }
            return;
          }

          if (newParent instanceof javafx.scene.layout.Pane pane) {
            // Remove this listener after applying
            if (wrapper.listener != null) {
              obs.removeListener(wrapper.listener);
              wrapper.listener = null;
            }
            // Apply layout styles now that we have a parent
            for (String token : tokens) {
              applySingleLayoutStyle(actualNode, pane, token);
            }
          }
        };

    node.parentProperty().addListener(wrapper.listener);
  }

  /**
   * Checks if a token requires layout context (parent container) to be applied.
   *
   * @deprecated Use {@link TokenRegistry#isLayoutDependent(String)} instead. This method is kept
   *     for backward compatibility but delegates to TokenRegistry.
   */
  @Deprecated
  private static boolean isLayoutDependent(String token) {
    return TokenRegistry.isLayoutDependent(token);
  }

  /**
   * Checks if a token requires container migration (flex, grid).
   *
   * @deprecated Use {@link TokenRegistry#requiresMigration(String)} instead. This method is kept
   *     for backward compatibility but delegates to TokenRegistry.
   */
  @Deprecated
  private static boolean requiresMigration(String token) {
    return TokenRegistry.requiresMigration(token);
  }

  /** Applies utility classes WITHOUT conflict resolution. */
  public static void applyRaw(Node node, String... classes) {
    for (String c : classes) {
      if (c == null || c.isBlank()) continue;
      for (String part : c.split("\\s+")) {
        if (!part.isBlank() && !node.getStyleClass().contains(part)) {
          node.getStyleClass().add(part);
        }
      }
    }
  }

  /** Removes CSS classes from a node. */
  public static void remove(Node node, String... classes) {
    node.getStyleClass().removeAll(Arrays.asList(classes));
  }

  /** Replaces all CSS classes on a node. */
  public static void replace(Node node, String... classes) {
    node.getStyleClass().setAll(Arrays.asList(classes));
  }

  /** Toggles a CSS class on a node. */
  public static void toggle(Node node, String cssClass) {
    if (node.getStyleClass().contains(cssClass)) {
      node.getStyleClass().remove(cssClass);
    } else {
      node.getStyleClass().add(cssClass);
    }
  }

  /** Enables automatic cleanup of JIT styles when a node is removed from the scene. */
  public static void autoCleanup(Node node) {
    Preconditions.requireNode(node, "TwStyle.autoCleanup");
    // Delegate to existing cleanup mechanism in UtilityConflictResolver
    io.github.yasmramos.tailwindfx.core.UtilityConflictResolver.autoCleanup(node);
  }

  /** Invalidates the entire style cache for a node. */
  public static void invalidateCache(Node node) {
    Preconditions.requireNode(node, "TwStyle.invalidateCache");
    node.getProperties().remove("tailwindfx.category.cache");
    node.getProperties().remove("tailwindfx.cleanup-listener");
  }

  /**
   * Removes all TailwindFX styles from a node (cleanup). Alias for invalidateCache for backward
   * compatibility.
   */
  public static void cleanupNode(Node node) {
    invalidateCache(node);
  }

  /** Invalidates a specific category from the style cache for a node. */
  public static void invalidateCategoryCache(Node node, String category) {
    Preconditions.requireNode(node, "TwStyle.invalidateCategoryCache");
    Preconditions.requireNonBlank(category, "TwStyle.invalidateCategoryCache", "category");
    @SuppressWarnings("unchecked")
    java.util.Map<String, String> cache =
        (java.util.Map<String, String>) node.getProperties().get("tailwindfx.category.cache");
    if (cache != null) {
      cache.remove(category);
    }
  }

  /**
   * Detects if a token should be compiled as JIT. Uses strict prefix matching +
   * numeric/arbitrary/negative pattern validation. Eliminates false positives like "card-2" or
   * "panel-v2".
   *
   * <p>This method strips variant prefixes (hover:, focus:, dark:, sm:, etc.) before checking, so
   * that "hover:bg-blue-500" is correctly identified as a JIT token.
   *
   * @param token the token to check
   * @return true if this token should be compiled as JIT
   */
  private static boolean isJitToken(String token) {
    // Delegate to TokenRegistry for centralized JIT detection
    return TokenRegistry.isJitPrefix(token);
  }

  /**
   * Strips variant prefixes from a token. Examples: "hover:bg-blue-500" -> "bg-blue-500",
   * "dark:hover:text-white" -> "text-white", "md:w-full" -> "w-full"
   */
  private static String stripVariantPrefix(String token) {
    if (token == null || !token.contains(":")) {
      return token;
    }
    // Find the last colon to handle chained variants like "dark:hover:bg-blue-500"
    int lastColon = token.lastIndexOf(':');
    if (lastColon >= 0 && lastColon < token.length() - 1) {
      return token.substring(lastColon + 1);
    }
    return token;
  }

  /**
   * Validates if a base token (before /) is a valid color utility that can have opacity. Prevents
   * false positives like "icon/large" being treated as JIT.
   *
   * @param base the token before the '/' modifier
   * @return true if this is a valid color utility base
   * @see ColorUtilityValidator#isValidColorUtilityBase(String)
   */
  private static boolean isValidColorUtilityBase(String base) {
    return ColorUtilityValidator.isValidColorUtilityBase(base);
  }

  /**
   * Checks if a token is a filter/effect token that should be handled via TwEffect. Effect tokens
   * include: blur, brightness, contrast, grayscale, invert, sepia, hue-rotate, saturate,
   * drop-shadow, backdrop-blur.
   *
   * @param token the base token (without variant prefix)
   * @return true if this token should be applied via TwEffect instead of CSS
   * @deprecated Use {@link TokenRegistry#isEffectToken(String)} instead. This method is kept for
   *     backward compatibility but delegates to TokenRegistry.
   */
  @Deprecated
  private static boolean isEffectToken(String token) {
    return TokenRegistry.isEffectToken(token);
  }

  /**
   * Applies an effect token to a node via TwEffect. Parses the token and calls the appropriate
   * TwEffect method.
   *
   * @param node the node to apply the effect to
   * @param token the effect token (e.g., "blur-sm", "brightness-125", "grayscale")
   */
  private static void applyEffectToken(javafx.scene.Node node, String token) {
    try {
      if (token.startsWith("blur-")) {
        String size = token.substring(5);
        if ("none".equals(size)) {
          TwEffect.blurNone(node);
        } else {
          TwEffect.blurWithSize(node, size);
        }
      } else if (token.equals("blur")) {
        TwEffect.blur(node, 0); // default blur
      } else if (token.startsWith("brightness-")) {
        String percentage = token.substring(11);
        TwEffect.brightnessWithPercentage(node, percentage);
      } else if (token.equals("brightness")) {
        TwEffect.brightness(node, 1.0); // default no change
      } else if (token.startsWith("contrast-")) {
        String percentage = token.substring(9);
        TwEffect.contrastWithPercentage(node, percentage);
      } else if (token.equals("contrast")) {
        TwEffect.contrast(node, 1.0); // default no change
      } else if (token.equals("grayscale")) {
        TwEffect.grayscale(node);
      } else if (token.equals("grayscale-0")) {
        TwEffect.grayscaleNone(node);
      } else if (token.equals("invert")) {
        TwEffect.invert(node);
      } else if (token.equals("invert-0")) {
        TwEffect.invertNone(node);
      } else if (token.equals("sepia")) {
        TwEffect.sepia(node);
      } else if (token.equals("sepia-0")) {
        TwEffect.sepiaNone(node);
      } else if (TwConfig.isDebug()) {
        System.out.println("[TailwindFX Warning] Unsupported effect token: " + token);
      }
    } catch (Exception e) {
      if (TwConfig.isDebug()) {
        System.out.println(
            "[TailwindFX Warning] Failed to apply effect \"" + token + "\": " + e.getMessage());
      }
    }
  }
}
