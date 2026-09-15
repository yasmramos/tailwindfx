package io.github.yasmramos.tailwindfx.style;

import io.github.yasmramos.tailwindfx.TwConfig;
import io.github.yasmramos.tailwindfx.layout.TwFlexPane;
import io.github.yasmramos.tailwindfx.layout.TwGridPane;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
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
 * <ul>
 *   <li>Margin application via Styles utility methods</li>
 *   <li>Gap application to parent containers (HBox, VBox, GridPane, TwFlexPane, TwGridPane)</li>
 *   <li>Flex grow/shrink factors for HBox, VBox, and TwFlexPane</li>
 *   <li>Grid column/row configuration for TwGridPane</li>
 *   <li>Automatic retry via parent property listener when node is not yet attached</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * LayoutApplier.applyLayoutDependentStyles(node, tokens);
 * </pre>
 */
public final class LayoutApplier {

  private LayoutApplier() {
    // Utility class - prevent instantiation
  }

  /**
   * Applies layout-dependent styles to a node.
   *
   * <p>Layout-dependent styles require knowledge of the parent container type to be applied
   * correctly. This method handles:
   * <ul>
   *   <li>Margin styles (m-*, mx-*, my-*, mt-*, mr-*, mb-*, ml-*)</li>
   *   <li>Gap styles (gap-*, gap-x-*, gap-y-*) - applied to the node if it's a Pane</li>
   *   <li>Flex styles (flex-*, grow, shrink) - applied based on parent type</li>
   *   <li>Grid styles (grid-cols-*, grid-rows-*, grid-flow-*) - applied to TwGridPane</li>
   *   <li>Grid item styles (col-span-*, row-span-*) - applied via TwGridPane</li>
   * </ul>
   *
   * @param node the node to apply styles to
   * @param tokens list of layout-dependent tokens to apply
   */
  public static void applyLayoutDependentStyles(Node node, List<String> tokens) {
    Pane parent = getEffectiveParent(node);

    for (String token : tokens) {
      // For gap styles, the node itself is the container
      if (token.startsWith("gap-") || token.startsWith("gap-x-") || token.startsWith("gap-y-")) {
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
      if (parent == null) {
        // Parent not available yet - register listener to apply when attached
        registerLayoutListener(node, tokens);
        return;
      }

      applySingleLayoutStyle(node, parent, token);
    }
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
   * <ul>
   *   <li>Margin tokens → {@link #applyMarginStyleViaStyles}</li>
   *   <li>Gap tokens → {@link #applyGapStyle}</li>
   *   <li>Flex tokens → {@link #applyFlexStyleViaStyles}</li>
   *   <li>Grid container tokens → {@link #applyGridContainerStyle}</li>
   *   <li>Grid item tokens → {@link #applyGridItemStyle}</li>
   * </ul>
   *
   * @param node the node to apply the style to
   * @param parent the parent pane for context
   * @param token the layout token to apply
   */
  private static void applySingleLayoutStyle(Node node, Pane parent, String token) {
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
   * Delegates margin application to Styles.java methods. Supports both numeric values (m-4) and
   * arbitrary values (m-[20px]).
   *
   * @param node the node to apply margin to
   * @param token the margin token (e.g., "m-4", "m-[20px]")
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

  /**
   * Delegates flex application to TwFlexPane or Styles.java methods.
   *
   * <p>For TwFlexPane containers, uses precise grow factors via {@link TwFlexPane#setGrow}.
   * For HBox/VBox, maps to Priority enum (NEVER/SOMETIMES/ALWAYS).
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

  /**
   * Applies flex styles specifically for TwFlexPane container.
   *
   * <p>TwFlexPane supports precise grow factors via {@link TwFlexPane#setGrow(Node, double)}.
   *
   * @param node the node to apply flex to
   * @param token the flex token
   */
  private static void applyFlexForTwFlexPane(Node node, String token) {
    if (token.equals("grow") || token.equals("flex-1")) {
      TwFlexPane.setGrow(node, 1);
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
   * Parses gap value from token and applies it to parent container.
   * Supports both numeric values (gap-4) and arbitrary values (gap-[20px]).
   *
   * @param parent the parent pane to apply gap to
   * @param token the gap token (e.g., "gap-4", "gap-[20px]")
   */
  private static void applyGapStyle(Pane parent, String token) {
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
    if (parent instanceof TwFlexPane flexPane) {
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
    if (parent instanceof TwGridPane gridPane) {
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
      int cols = (int) parseTailwindValue(token);
      gridPane.cols(cols);
    } else if (token.startsWith("grid-rows-")) {
      int rows = (int) parseTailwindValue(token);
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
      int span = (int) parseTailwindValue(token);
      TwGridPane.setColSpan(node, span);
    } else if (token.startsWith("row-span-")) {
      int span = (int) parseTailwindValue(token);
      TwGridPane.setRowSpan(node, span);
    }
  }

  /**
   * Parses CSS value string to pixels using configured unit size.
   *
   * <p>Supports:
   * <ul>
   *   <li>Pixels: "20px" → 20.0</li>
   *   <li>Rem: "1.5rem" → 1.5 * TwConfig.unit()</li>
   *   <li>Em: "2em" → 2.0 * TwConfig.unit()</li>
   *   <li>Plain numbers: "16" → 16.0</li>
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

  /**
   * Parses numeric value from Tailwind token.
   *
   * <p>Examples:
   * <ul>
   *   <li>"m-4" → 4</li>
   *   <li>"m-[16px]" → 16px / TwConfig.unit()</li>
   *   <li>"-m-4" → -4</li>
   * </ul>
   *
   * @param token the Tailwind token to parse
   * @return the numeric value respecting TwConfig.unit()
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
   * Registers a listener to apply layout styles when node is attached to parent.
   * Uses WeakReference to prevent memory leaks.
   *
   * <p>Bug #10 fix: Converted local ListenerWrapper class to use AtomicReference for
   * safe self-removal of the listener after application.
   *
   * @param node the node to register listener for
   * @param tokens the tokens to apply when parent becomes available
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

    // Use AtomicReference for safe self-removal of listener (Bug #10 fix)
    AtomicReference<ChangeListener<Parent>> listenerRef = new AtomicReference<>();

    ChangeListener<Parent> listener =
        (obs, oldParent, newParent) -> {
          Node actualNode = weakNode.get();
          if (actualNode == null) {
            // Node was garbage collected, remove listener
            ChangeListener<Parent> listenerToRemove = listenerRef.getAndSet(null);
            if (listenerToRemove != null) {
              obs.removeListener(listenerToRemove);
            }
            return;
          }

          if (newParent instanceof Pane pane) {
            // Remove this listener after applying
            ChangeListener<Parent> listenerToRemove = listenerRef.getAndSet(null);
            if (listenerToRemove != null) {
              obs.removeListener(listenerToRemove);
            }
            // Apply layout styles now that we have a parent
            for (String token : tokens) {
              applySingleLayoutStyle(actualNode, pane, token);
            }
          }
        };

    listenerRef.set(listener);
    node.parentProperty().addListener(listener);
  }

  /**
   * Checks if a token requires layout context (parent container) to be applied.
   *
   * @param token the token to check
   * @return true if the token is layout-dependent
   */
  public static boolean isLayoutDependent(String token) {
    // Exact matches for flex utilities
    if (token.equals("grow") || token.equals("shrink")) {
      return true;
    }

    // Prefix-based matching for layout token families
    return token.startsWith("m-")
        || token.startsWith("mx-")
        || token.startsWith("my-")
        || token.startsWith("mt-")
        || token.startsWith("mr-")
        || token.startsWith("mb-")
        || token.startsWith("ml-")
        || token.startsWith("gap-")
        || token.startsWith("gap-x-")
        || token.startsWith("gap-y-")
        || token.startsWith("flex-")
        || token.startsWith("grid-cols-")
        || token.startsWith("grid-rows-")
        || token.startsWith("grid-flow-")
        || token.startsWith("col-span-")
        || token.startsWith("row-span-");
  }

  /**
   * Checks if a token is an effect token (blur, brightness, grayscale, invert, etc.).
   *
   * <p>Effect tokens are distinct from layout tokens and are applied via TwEffect.
   *
   * @param token the token to check
   * @return true if the token is an effect token
   */
  public static boolean isEffectToken(String token) {
    // Exact matches for effect utilities
    if (token.equals("grayscale") || token.equals("invert")) {
      return true;
    }

    // Prefix-based matching for effect token families
    return token.startsWith("blur-")
        || token.startsWith("brightness-")
        || token.startsWith("contrast-")
        || token.startsWith("drop-shadow-")
        || token.startsWith("hue-rotate-")
        || token.startsWith("saturate-")
        || token.startsWith("sepia-")
        || token.startsWith("grayscale-")
        || token.startsWith("invert-")
        || token.startsWith("opacity-");
  }
}
