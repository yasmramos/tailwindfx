package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.core.Preconditions;
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

  private static final Set<String> JIT_PREFIXES =
      new HashSet<>(
          Arrays.asList(
              "bg",
              "text",
              "border",
              "ring",
              "shadow",
              "w",
              "h",
              "min-w",
              "min-h",
              "max-w",
              "max-h",
              "p",
              "px",
              "py",
              "pt",
              "pr",
              "pb",
              "pl",
              "m",
              "mx",
              "my",
              "mt",
              "mr",
              "mb",
              "ml",
              "space",
              "translate",
              "rotate",
              "scale",
              "skew",
              "opacity",
              "z",
              "order",
              "col",
              "row",
              "gap",
              "inset",
              "top",
              "right",
              "bottom",
              "left",
              "blur",
              "brightness",
              "contrast",
              "grayscale",
              "hue-rotate",
              "invert",
              "saturate",
              "sepia",
              "drop-shadow",
              "backdrop"));

  private static final Set<String> LAYOUT_DEPENDENT_PREFIXES =
      new HashSet<>(
          Arrays.asList(
              "m-", "mx-", "my-", "mt-", "mr-", "mb-", "ml-", "gap-", "gap-x-", "gap-y-", "flex-",
              "grow", "shrink"));

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
    java.util.List<String> cssClasses = new java.util.ArrayList<>();
    java.util.List<String> jitTokens = new java.util.ArrayList<>();
    java.util.List<String> layoutDependentTokens = new java.util.ArrayList<>();
    java.util.List<String> layoutMigrationTokens = new java.util.ArrayList<>();
    java.util.List<String> variantTokens = new java.util.ArrayList<>();

    for (String token : tokens) {
      if (token == null || token.isBlank()) continue;
      for (String t : token.split("\\s+")) {
        if (t.isBlank()) continue;

        // Check if token has variants (hover:, focus:, dark:, sm:, etc.)
        boolean hasVariant = t.contains(":");
        
        // Check for unsupported variants (responsive/state) on layout-dependent properties
        if (hasVariant && isLayoutDependent(t)) {
          throw new UnsupportedOperationException(
              "Layout-dependent properties do not support responsive or state variants: "
                  + t
                  + ". Use programmatic logic instead.");
        }

        if (hasVariant) {
          // Tokens with variants need special handling via VariantManager
          variantTokens.add(t);
        } else if (isJitToken(t)) {
          jitTokens.add(t);
          if (isLayoutDependent(t)) {
            layoutDependentTokens.add(t);
          }
          // Check if token requires container migration (flex, grid)
          if (requiresMigration(t)) {
            layoutMigrationTokens.add(t);
          }
        } else {
          cssClasses.add(t);
        }
      }
    }

    if (!cssClasses.isEmpty()) {
      UtilityConflictResolver.applyAll(node, cssClasses.toArray(new String[0]));
      TailwindFXMetrics.instance().recordApply(cssClasses.size());
    }

    // If migration is needed, delegate to TwLayout and skip JIT for those tokens
    if (!layoutMigrationTokens.isEmpty()) {
      throw new UnsupportedOperationException(
          "Layout classes requiring container migration ("
              + String.join(", ", layoutMigrationTokens)
              + ") must be applied using TailwindFX.layout() instead of TailwindFX.apply().");
    }

    // Apply layout-dependent styles first (needs parent context)
    if (!layoutDependentTokens.isEmpty()) {
      applyLayoutDependentStyles(node, layoutDependentTokens);
    }

    // Apply variant tokens via VariantManager
    if (!variantTokens.isEmpty()) {
      for (String variantToken : variantTokens) {
        io.github.yasmramos.tailwindfx.core.VariantManager.processToken(node, variantToken, 
            new io.github.yasmramos.tailwindfx.core.JitCompiler());
      }
    }

    // Handle unknown tokens with debug warning (Smart fallback as documented in README)
    for (String token : tokens) {
      if (token == null || token.isBlank()) continue;
      for (String t : token.split("\\s+")) {
        if (t.isBlank()) continue;
        boolean hasVariant = t.contains(":");
        if (!hasVariant && !isJitToken(t)) {
          // Check if it's a known CSS class or an unknown token
          if (!io.github.yasmramos.tailwindfx.style.Styles.isKnownUtilityClass(t)) {
            if (io.github.yasmramos.tailwindfx.TwConfig.isDebug()) {
              System.out.println("[TailwindFX Warning] Unknown token ignored: " + t);
            }
          }
        }
      }
    }

    if (!jitTokens.isEmpty()) {
      StyleMerger.applyJit(node, jitTokens.toArray(new String[0]));
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
    int value = parseTailwindValue(token);

    if (token.startsWith("m-")) {
      Styles.m(node, value);
    } else if (token.startsWith("mx-")) {
      Styles.mx(node, value);
    } else if (token.startsWith("my-")) {
      Styles.my(node, value);
    } else if (token.startsWith("mt-")) {
      Styles.mt(node, value);
    } else if (token.startsWith("mr-")) {
      Styles.mr(node, value);
    } else if (token.startsWith("mb-")) {
      Styles.mb(node, value);
    } else if (token.startsWith("ml-")) {
      Styles.ml(node, value);
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
        // For arbitrary flex values in HBox/VBox, use ALWAYS priority
        if (parent instanceof HBox) {
          Styles.flex1(node);
        } else if (parent instanceof VBox) {
          Styles.vgrow(node);
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
        int value = parseTailwindValue(token);
        px = value * 4.0;
      }
    } else {
      int value = parseTailwindValue(token);
      px = value * 4.0;
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

  /** Parses a CSS value string (e.g., "20px", "2.5rem", "10") to pixels. */
  private static double parseCssValue(String value) {
    if (value.endsWith("px")) {
      try {
        return Double.parseDouble(value.substring(0, value.length() - 2));
      } catch (NumberFormatException e) {
        return 0;
      }
    } else if (value.endsWith("rem")) {
      try {
        double rem = Double.parseDouble(value.substring(0, value.length() - 3));
        return rem * 16.0; // Assuming 1rem = 16px
      } catch (NumberFormatException e) {
        return 0;
      }
    } else if (value.endsWith("em")) {
      try {
        double em = Double.parseDouble(value.substring(0, value.length() - 2));
        return em * 16.0; // Assuming 1em = 16px
      } catch (NumberFormatException e) {
        return 0;
      }
    } else {
      try {
        return Double.parseDouble(value);
      } catch (NumberFormatException e) {
        return 0;
      }
    }
  }

  /** Applies flex-related styles (flex-*, grow, shrink). */
  private static void applyFlexStyle(Node node, javafx.scene.layout.Pane parent, String token) {
    if (token.equals("grow") || token.equals("flex-1")) {
      if (parent instanceof HBox) {
        HBox.setHgrow(node, Priority.ALWAYS);
      } else if (parent instanceof VBox) {
        VBox.setVgrow(node, Priority.ALWAYS);
      }
    } else if (token.equals("shrink") || token.equals("flex-none")) {
      if (parent instanceof HBox) {
        HBox.setHgrow(node, Priority.NEVER);
      } else if (parent instanceof VBox) {
        VBox.setVgrow(node, Priority.NEVER);
      }
    } else if (token.equals("flex-auto")) {
      if (parent instanceof HBox) {
        HBox.setHgrow(node, Priority.ALWAYS);
      } else if (parent instanceof VBox) {
        VBox.setVgrow(node, Priority.ALWAYS);
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
        if (parent instanceof HBox) {
          HBox.setHgrow(node, Priority.ALWAYS);
        } else if (parent instanceof VBox) {
          VBox.setVgrow(node, Priority.ALWAYS);
        }
      } catch (NumberFormatException e) {
        // Ignore invalid flex values
      }
    }
  }

  /** Parses numeric value from Tailwind token (e.g., "m-4" -> 4, "p-[16px]" -> 4). */
  private static int parseTailwindValue(String token) {
    // Handle arbitrary values like m-[16px]
    if (token.contains("[")) {
      int start = token.indexOf('[') + 1;
      int end = token.indexOf(']');
      String value = token.substring(start, end);
      if (value.endsWith("px")) {
        return Integer.parseInt(value.substring(0, value.length() - 2)) / 4;
      }
      return Integer.parseInt(value);
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

  /** Checks if a token has unsupported variants (responsive or state prefixes). */
  private static boolean hasUnsupportedVariant(String token) {
    return RESPONSIVE_PREFIXES.stream().anyMatch(token::startsWith)
        || STATE_PREFIXES.stream().anyMatch(token::startsWith);
  }

  /** Checks if a token requires layout context (parent container) to be applied. */
  private static boolean isLayoutDependent(String token) {
    return LAYOUT_DEPENDENT_PREFIXES.stream().anyMatch(token::startsWith);
  }

  /** Checks if a token requires container migration (flex, grid). */
  private static boolean requiresMigration(String token) {
    // Remove variants like hover:, md:, etc.
    String baseToken = token.contains(":") ? token.substring(token.indexOf(':') + 1) : token;
    return baseToken.equals("flex") || baseToken.equals("grid");
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
   * <p>This method strips variant prefixes (hover:, focus:, dark:, sm:, etc.) before checking,
   * so that "hover:bg-blue-500" is correctly identified as a JIT token.
   */
  private static boolean isJitToken(String token) {
    // Strip variant prefixes before checking (e.g., "hover:bg-blue-500" -> "bg-blue-500")
    String baseToken = stripVariantPrefix(token);
    
    // Opacity modifier: bg-blue-500/80 - but only for valid color utilities
    if (baseToken.contains("/")) {
      String base = baseToken.substring(0, baseToken.indexOf('/'));
      return isValidColorUtilityBase(base);
    }
    if (baseToken.contains("[")) return true; // arbitrary: w-[320px]

    // Special layout keywords without numeric values
    if (baseToken.equals("grow")
        || baseToken.equals("shrink")
        || baseToken.equals("flex-none")
        || baseToken.equals("flex-auto")
        || baseToken.equals("flex-1")) {
      return true;
    }

    // Strict negative prefix: only JIT if followed by a known property prefix
    if (baseToken.startsWith("-") && baseToken.length() > 1) {
      String withoutNeg = baseToken.substring(1);
      return JIT_PREFIXES.stream().anyMatch(withoutNeg::startsWith);
    }

    // Must start with a known Tailwind prefix AND contain a numeric modifier
    boolean hasPrefix =
        JIT_PREFIXES.stream()
            .anyMatch(
                p ->
                    baseToken.startsWith(p)
                        && (baseToken.length() == p.length() || baseToken.charAt(p.length()) == '-'));

    if (hasPrefix) {
      return baseToken.matches(".*\\d+.*");
    }

    return false;
  }

  /**
   * Strips variant prefixes from a token.
   * Examples: "hover:bg-blue-500" -> "bg-blue-500", "dark:hover:text-white" -> "text-white",
   * "md:w-full" -> "w-full"
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
   */
  private static boolean isValidColorUtilityBase(String base) {
    // Color utilities that support opacity: bg-*, text-*, border-*, ring-*, shadow-*
    String[] colorPrefixes = {"bg-", "text-", "border-", "ring-", "shadow-"};

    for (String prefix : colorPrefixes) {
      if (base.startsWith(prefix) && base.length() > prefix.length()) {
        String colorPart = base.substring(prefix.length());
        // Valid color patterns: blue-500, red-900, gray-50, custom-color
        // Must contain at least one hyphen or be a simple color name
        return colorPart.contains("-") || colorPart.matches("[a-zA-Z]+");
      }
    }
    return false;
  }
}
