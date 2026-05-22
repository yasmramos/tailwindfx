package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.core.Preconditions;
import io.github.yasmramos.tailwindfx.layout.TwFlexPane;
import io.github.yasmramos.tailwindfx.layout.TwGridPane;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

/**
 * TwLayout — Unified Layout Engine for TailwindFX.
 *
 * <p>Principles:
 * <ol>
 *   <li><strong>INTELLIGENT MUTATION:</strong> if the source Pane is already the correct type, it is
 *       never recreated. Only properties are reconfigured. Critical for calling from breakpoint
 *       listeners without rebuilding the tree.</li>
 *   <li><strong>CONSTRAINTS MIGRATION:</strong> when switching Panes, child constraints (hgrow,
 *       vgrow, margin, gridCol/Row) are preserved.</li>
 *   <li><strong>RESPONSIVE SWITCH:</strong> reusing the same builder with .row()/.col() and calling
 *       build() again is safe and efficient.</li>
 *   <li><strong>ANCHORPANE FLUENT:</strong> anchorAll / anchorFill / anchorTop/Right/Bottom/Left</li>
 *   <li><strong>AUTO GRID COLS:</strong> if no columns are specified, they are calculated
 *       automatically based on the number of children.</li>
 * </ol>
 *
 * <p>Basic usage:
 * <pre>
 *   TwLayout.of(pane).row().gap(12).center().build();
 *   TwLayout.of(pane).grid(3).hgap(16).vgap(16).build();
 *   TwLayout.of(pane).flowRow().gap(8).build();
 * </pre>
 *
 * <p>Responsive switch (safe — does not recreate the Pane):
 * <pre>
 *   var lyt = TwLayout.of(container).gap(12);
 *   stage.widthProperty().addListener((o, old, w) -> {
 *     if (w.doubleValue() &lt; 768) lyt.col().build();
 *     else lyt.row().build();
 *   });
 * </pre>
 *
 * <p>AnchorPane:
 * <pre>
 *   TwLayout.of(root).anchor()
 *     .anchorFill(content, 0, 0, 0, 0)
 *     .build();
 * </pre>
 */
public final class TwLayout {

  private static final TwLayout INSTANCE = new TwLayout();

  private TwLayout() {}

  /**
   * Apply layout classes (flex, grid, gap) with automatic container migration if needed.
   *
   * @param node the node to apply layout to
   * @param tokens the layout tokens (flex, grid, gap-*, etc.)
   */
  public static void apply(Node node, String... tokens) {
    if (tokens == null || tokens.length == 0) return;

    Pane parent = getEffectiveParent(node);
    if (parent == null) return;

    Builder helper = of(parent);

    for (String token : tokens) {
      if (token == null || token.isBlank()) continue;

      if (token.equals("flex")) {
        helper.flex().build();
      } else if (token.equals("grid")) {
        helper.flexGrid().build();
      } else if (token.startsWith("gap-")) {
        applyGap(helper, token);
      } else if (token.startsWith("flex-") || token.equals("grow") || token.equals("shrink")) {
        applyFlexItem(node, token);
      }
    }
  }

  /** Applies gap style using Builder. */
  private static void applyGap(Builder helper, String token) {
    int value = parseTailwindValue(token);
    double px = value * 4.0;

    if (token.startsWith("gap-x-")) {
      helper.hgap(px).build();
    } else if (token.startsWith("gap-y-")) {
      helper.vgap(px).build();
    } else {
      helper.gap(px).build();
    }
  }

  /** Applies flex item style to a node. */
  private static void applyFlexItem(Node node, String token) {
    Pane parent = getEffectiveParent(node);
    if (!(parent instanceof TwFlexPane)) return;

    if (token.equals("grow") || token.equals("flex-1")) {
      TwFlexPane.setGrow(node, 1);
    } else if (token.equals("shrink") || token.equals("flex-none")) {
      TwFlexPane.setShrink(node, 0);
    } else if (token.equals("flex-auto")) {
      TwFlexPane.setGrow(node, 1);
      TwFlexPane.setShrink(node, 1);
    } else if (token.startsWith("flex-")) {
      try {
        String value = token.substring(5);
        if (value.startsWith("[") && value.endsWith("]")) {
          value = value.substring(1, value.length() - 1);
        }
        double flexValue = Double.parseDouble(value);
        TwFlexPane.setGrow(node, flexValue);
      } catch (NumberFormatException e) {
        // Ignore invalid values
      }
    }
  }

  /** Gets the effective parent pane. */
  private static Pane getEffectiveParent(Node node) {
    javafx.scene.Parent parent = node.getParent();
    if (parent instanceof Pane) {
      return (Pane) parent;
    }
    return null;
  }

  /** Parses numeric value from Tailwind token. */
  private static int parseTailwindValue(String token) {
    if (token.contains("[")) {
      int start = token.indexOf('[') + 1;
      int end = token.indexOf(']');
      String value = token.substring(start, end);
      if (value.contains("px")) {
        return (int) Double.parseDouble(value.replace("px", ""));
      }
      return Integer.parseInt(value);
    }

    String numPart = token.substring(token.lastIndexOf('-') + 1);
    try {
      return Integer.parseInt(numPart);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  /**
   * Get layout builder for a container.
   *
   * @param container the pane container
   * @return Builder instance
   */
  public static Builder of(Pane container) {
    return new Builder(container);
  }

  /**
   * Create a horizontal flex pane (row direction).
   *
   * @return new TwFlexPane with ROW direction
   */
  public static TwFlexPane flexRow() {
    return TwFlexPane.row();
  }

  /**
   * Create a vertical flex pane (column direction).
   *
   * @return new TwFlexPane with COL direction
   */
  public static TwFlexPane flexCol() {
    return TwFlexPane.col();
  }

  /**
   * Create a grid pane builder.
   *
   * @return TwGridPane.Builder to configure and build
   */
  public static TwGridPane.Builder grid() {
    return TwGridPane.create();
  }

  /**
   * Create a grid pane with default gap.
   *
   * @return new TwGridPane instance
   */
  public static TwGridPane gridBuild() {
    return TwGridPane.create().build();
  }

  /**
   * Apply aspect ratio constraint to a node.
   *
   * @param node the node
   * @param widthRatio width ratio
   * @param heightRatio height ratio
   * @throws IllegalArgumentException if ratios are not positive
   */
  public static void aspectRatio(Node node, int widthRatio, int heightRatio) {
    if (widthRatio <= 0 || heightRatio <= 0) {
      throw new IllegalArgumentException("Aspect ratio values must be positive");
    }
    node.boundsInParentProperty()
        .addListener(
            (obs, old, bounds) -> {
              double targetWidth = bounds.getHeight() * widthRatio / heightRatio;
              if (node instanceof javafx.scene.layout.Region) {
                ((javafx.scene.layout.Region) node).setPrefWidth(targetWidth);
              }
            });
  }

  // =========================================================================
  // Inner Builder class (formerly TwLayoutHelper)
  // =========================================================================

  public enum LayoutType {
    ROW,
    COL,
    STACK,
    GRID,
    FLOW_ROW,
    FLOW_COL,
    ANCHOR,
    TILE,
    FLEX,
    FLEX_GRID
  }

  /** Layout builder with fluent API. */
  public static final class Builder {

    private final Pane source;
    private LayoutType type = LayoutType.ROW;
    private double gap = 0;

    private TwFlexPane.Justify flexJustify = TwFlexPane.Justify.START;
    private TwFlexPane.Align flexAlign = TwFlexPane.Align.START;
    private boolean flexWrap = false;
    private String[] gridAreas = null;
    private int gridCols2 = 3;
    private double hgapVal = -1;
    private double vgapVal = -1;
    private Insets padding = Insets.EMPTY;
    private Pos alignment = Pos.TOP_LEFT;
    private int gridCols = -1;
    private boolean growAll = false;
    private boolean fillW = false;
    private boolean fillH = false;
    private boolean debugMode = false;
    private LayoutTransitionListener transitionListener = null;
    private double minW = -1, minH = -1, maxW = -1, maxH = -1;
    private double prefW = -1, prefH = -1;
    private final Map<Node, double[]> anchors = new LinkedHashMap<>();

    public Builder(Pane source) {
      this.source = Preconditions.requireNonNull(source, "TwLayout", "pane");
    }

    // Type of layout

    public Builder row() {
      type = LayoutType.ROW;
      return this;
    }

    public Builder col() {
      type = LayoutType.COL;
      return this;
    }

    public Builder stack() {
      type = LayoutType.STACK;
      return this;
    }

    public Builder grid(int cols) {
      type = LayoutType.GRID;
      gridCols = Preconditions.requireSpan(cols, "TwLayout.grid");
      return this;
    }

    public Builder grid() {
      type = LayoutType.GRID;
      gridCols = -1;
      return this;
    }

    public Builder flowRow() {
      type = LayoutType.FLOW_ROW;
      return this;
    }

    public Builder flowCol() {
      type = LayoutType.FLOW_COL;
      return this;
    }

    /** TilePane — grid of uniform-sized tiles. */
    public Builder tile() {
      type = LayoutType.TILE;
      return this;
    }

    public Builder anchor() {
      type = LayoutType.ANCHOR;
      return this;
    }

    /**
     * Switches to {@link TwFlexPane} mode. The container must be an {@link TwFlexPane} or will be
     * converted on {@link #build()}.
     *
     * <pre>
     * TwFlexPane cards = (TwFlexPane) TwLayout.of(flexPane)
     *     .flex().wrap(true).justify(TwFlexPane.Justify.BETWEEN).gap(16).build();
     * </pre>
     */
    public Builder flex() {
      type = LayoutType.FLEX;
      return this;
    }

    /**
     * Switches to {@link TwGridPane} (grid-template-areas) mode. Use {@link #areas(String...)} to
     * define the grid.
     *
     * <pre>
     * TwGridPane page = (TwGridPane) TwLayout.of(new TwGridPane())
     *     .flexGrid()
     *     .areas("header header", "sidebar main", "footer footer")
     *     .gap(12).build();
     * </pre>
     */
    public Builder flexGrid() {
      type = LayoutType.FLEX_GRID;
      return this;
    }

    /**
     * Sets the justify-content for a flex container. Only applies when type is {@link
     * LayoutType#FLEX}.
     *
     * @param justify the justify-content value
     */
    public Builder justify(TwFlexPane.Justify justify) {
      this.flexJustify = justify;
      return this;
    }

    /**
     * Sets the align-items for a flex container. Only applies when type is {@link LayoutType#FLEX}.
     *
     * @param align the align-items value
     */
    public Builder alignItems(TwFlexPane.Align align) {
      this.flexAlign = align;
      return this;
    }

    /**
     * Enables or disables flex-wrap. Only applies when type is {@link LayoutType#FLEX}.
     *
     * @param wrap {@code true} to enable wrapping
     */
    public Builder wrap(boolean wrap) {
      this.flexWrap = wrap;
      return this;
    }

    /**
     * Defines grid-template-areas for a {@link TwGridPane}. Only applies when type is {@link
     * LayoutType#FLEX_GRID}.
     *
     * <pre>
     * .areas("header header",
     *        "sidebar main",
     *        "footer footer")
     * </pre>
     *
     * @param rows each string defines one row of named areas
     */
    public Builder areas(String... rows) {
      this.gridAreas = rows;
      return this;
    }

    /**
     * Sets the number of columns for {@link TwGridPane} auto-flow or masonry mode. Only applies
     * when type is {@link LayoutType#FLEX_GRID}.
     *
     * @param cols number of columns
     */
    public Builder cols(int cols) {
      this.gridCols2 = cols;
      return this;
    }

    // Spacing

    public Builder gap(double v) {
      gap = v;
      return this;
    }

    public Builder hgap(double v) {
      hgapVal = v;
      return this;
    }

    public Builder vgap(double v) {
      vgapVal = v;
      return this;
    }

    // Padding

    public Builder padding(double all) {
      padding = new Insets(all);
      return this;
    }

    public Builder padding(double tb, double lr) {
      padding = new Insets(tb, lr, tb, lr);
      return this;
    }

    public Builder padding(double t, double r, double b, double l) {
      padding = new Insets(t, r, b, l);
      return this;
    }

    /**
     * Sets padding from a CSS-like shorthand string (values in px, no unit suffix needed).
     *
     * <ul>
     *   <li>{@code "16"} → uniform 16px
     *   <li>{@code "8 16"} → 8px top/bottom, 16px left/right
     *   <li>{@code "4 8 4 8"} → top right bottom left
     * </ul>
     *
     * <pre>
     * TwLayout.of(card).row().padding("16").build();
     * TwLayout.of(form).col().padding("8 16").build();
     * </pre>
     *
     * @param shorthand space-separated px values (1, 2, or 4 values)
     */
    public Builder padding(String shorthand) {
      Preconditions.requireNonBlank(shorthand, "TwLayout.padding", "shorthand");
      String[] parts = shorthand.trim().split("\\s+");
      padding =
          switch (parts.length) {
            case 1 -> new Insets(parsePx(parts[0]));
            case 2 -> new Insets(
                parsePx(parts[0]), parsePx(parts[1]), parsePx(parts[0]), parsePx(parts[1]));
            case 4 -> new Insets(
                parsePx(parts[0]), parsePx(parts[1]), parsePx(parts[2]), parsePx(parts[3]));
            default -> throw new IllegalArgumentException(
                "TwLayout.padding: expected 1, 2, or 4 values, got: " + parts.length);
          };
      return this;
    }

    private static double parsePx(String s) {
      try {
        return Double.parseDouble(s.replace("px", "").trim());
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("TwLayout.padding: invalid value '" + s + "'");
      }
    }

    // Alignment

    public Builder center() {
      alignment = Pos.CENTER;
      return this;
    }

    public Builder centerLeft() {
      alignment = Pos.CENTER_LEFT;
      return this;
    }

    public Builder centerRight() {
      alignment = Pos.CENTER_RIGHT;
      return this;
    }

    public Builder topLeft() {
      alignment = Pos.TOP_LEFT;
      return this;
    }

    public Builder topCenter() {
      alignment = Pos.TOP_CENTER;
      return this;
    }

    public Builder topRight() {
      alignment = Pos.TOP_RIGHT;
      return this;
    }

    public Builder bottomLeft() {
      alignment = Pos.BOTTOM_LEFT;
      return this;
    }

    public Builder bottomCenter() {
      alignment = Pos.BOTTOM_CENTER;
      return this;
    }

    public Builder bottomRight() {
      alignment = Pos.BOTTOM_RIGHT;
      return this;
    }

    // semantic aliases
    public Builder justifyCenter() {
      return center();
    }

    public Builder justifyStart() {
      return topLeft();
    }

    public Builder itemsCenter() {
      return centerLeft();
    }

    public Builder placeCenter() {
      return center();
    }

    // Growth and sizing

    public Builder grow() {
      growAll = true;
      return this;
    }

    public Builder fillWidth() {
      fillW = true;
      return this;
    }

    public Builder fillHeight() {
      fillH = true;
      return this;
    }

    public Builder fill() {
      fillW = true;
      fillH = true;
      return this;
    }

    public Builder prefWidth(double w) {
      prefW = w;
      return this;
    }

    public Builder prefHeight(double h) {
      prefH = h;
      return this;
    }

    public Builder minWidth(double w) {
      minW = w;
      return this;
    }

    public Builder minHeight(double h) {
      minH = h;
      return this;
    }

    public Builder maxWidth(double w) {
      maxW = w;
      return this;
    }

    public Builder maxHeight(double h) {
      maxH = h;
      return this;
    }

    // AnchorPane constraints (fluent, for use with .anchor())

    /** Anchors a node to all sides with the same value */
    public Builder anchorAll(Node n, double v) {
      anchors.put(n, new double[] {v, v, v, v});
      return this;
    }

    /** Anchors to 4 sides: top, right, bottom, left */
    public Builder anchorFill(Node n, double t, double r, double b, double l) {
      anchors.put(n, new double[] {t, r, b, l});
      return this;
    }

    public Builder anchorTop(Node n, double v) {
      return setEdge(n, 0, v);
    }

    public Builder anchorRight(Node n, double v) {
      return setEdge(n, 1, v);
    }

    public Builder anchorBottom(Node n, double v) {
      return setEdge(n, 2, v);
    }

    public Builder anchorLeft(Node n, double v) {
      return setEdge(n, 3, v);
    }

    /** Horizontal anchor (left + right) → node fills available width */
    public Builder anchorH(Node n, double left, double right) {
      return setEdge(n, 3, left).setEdge(n, 1, right);
    }

    /** Vertical anchor (top + bottom) → node fills available height */
    public Builder anchorV(Node n, double top, double bottom) {
      return setEdge(n, 0, top).setEdge(n, 2, bottom);
    }

    private Builder setEdge(Node n, int i, double v) {
      anchors.computeIfAbsent(n, k -> new double[] {-1, -1, -1, -1})[i] = v;
      return this;
    }

    // Debug mode

    /**
     * Enables debug logging for this layout operation. Prints layout type, container class, child
     * count, and migration decision to stdout.
     *
     * <pre>
     * TwLayout.of(pane).row().gap(12).debug().build();
     * // Output: [TwLayout] ROW  source=HBox  children=3  migrate=false
     * </pre>
     */
    public Builder debug() {
      this.debugMode = true;
      return this;
    }

    // Transition listener

    /**
     * Callback fired before and after a layout type change. Only invoked when the container is
     * actually migrated (type changes).
     */
    public interface LayoutTransitionListener {
      /**
       * Called just before the container is replaced.
       *
       * @param source the original container
       * @param newType the layout type being switched to
       */
      void onLayoutChanging(Pane source, LayoutType newType);

      /**
       * Called after the new container is fully configured and in place.
       *
       * @param result the new container
       */
      void onLayoutChanged(Pane result);
    }

    /**
     * Registers a transition listener for this layout operation. Useful for animating between
     * layout types or updating bindings.
     *
     * <pre>
     * TwLayout.of(pane).col()
     *     .onTransition(new Builder.LayoutTransitionListener() {
     *         public void onLayoutChanging(Pane src, LayoutType t) {
     *             TwAnimation.fadeOut(src, 150).play();
     *         }
     *         public void onLayoutChanged(Pane result) {
     *             TwAnimation.fadeIn(result, 150).play();
     *         }
     *     })
     *     .build();
     * </pre>
     *
     * @param listener the transition listener, or {@code null} to clear
     */
    public Builder onTransition(LayoutTransitionListener listener) {
      this.transitionListener = listener;
      return this;
    }

    /**
     * Applies the configuration. If the source is already the correct type, it only reconfigures
     * properties (does not recreate anything). If the type changed, migrates children + constraints
     * to a new Pane.
     */
    public Pane build() {
      checkFxThread();
      validate();

      if (type == LayoutType.GRID && gridCols == -1)
        gridCols = autoGridCols(source.getChildren().size());

      boolean migrate = mustMigrate();

      if (debugMode) {
        System.out.printf(
            "[TwLayout] %-12s source=%-16s children=%-4d migrate=%s%n",
            type, source.getClass().getSimpleName(), source.getChildren().size(), migrate);
      }

      if (migrate && transitionListener != null) transitionListener.onLayoutChanging(source, type);

      Pane result = migrate ? migrateToNew() : source;

      configure(result);
      applyGrowth(result);
      applySize(result);

      if (migrate && source.getParent() instanceof Pane parent) {
        int idx = parent.getChildren().indexOf(source);
        if (idx >= 0) parent.getChildren().set(idx, result);
      }

      if (migrate && transitionListener != null) transitionListener.onLayoutChanged(result);

      return result;
    }

    /**
     * Reconfigures only properties (spacing, alignment, padding) without touching the Pane type or
     * its children. Safe to call from any listener.
     */
    public void reconfigure() {
      checkFxThread();
      configure(source);
      applyGrowth(source);
      applySize(source);
    }

    private static void checkFxThread() {
      if (!javafx.application.Platform.isFxApplicationThread()) {
        throw new IllegalStateException(
            "TwLayout must be used on the JavaFX Application Thread. "
                + "Use Platform.runLater() to schedule layout changes from background threads.");
      }
    }

    // Validation

    private void validate() {
      if (type == LayoutType.GRID && gridCols == -1 && source.getChildren().isEmpty()) {
        Preconditions.LOG.warning(
            "TwLayout.build: GRID type with 0 children and no column count — "
                + "grid will be empty. Call grid(n) to set column count.");
      }
      if (type == LayoutType.FLEX_GRID && gridAreas == null && gridCols2 <= 0) {
        Preconditions.LOG.warning(
            "TwLayout.build: FLEX_GRID with no areas() and cols <= 0 — "
                + "use .areas(...) or .cols(n) to define the grid layout.");
      }
      if (gap < 0) {
        Preconditions.LOG.warning(
            () -> "TwLayout.build: gap=" + gap + " is negative — did you mean margin?");
      }
      if (type == LayoutType.ANCHOR
          && anchors.isEmpty()
          && !(source instanceof javafx.scene.layout.AnchorPane)) {
        Preconditions.LOG.fine(
            "TwLayout.build: ANCHOR type but no anchor constraints defined — "
                + "use anchorTop/Right/Bottom/Left() to position children.");
      }
    }

    private boolean mustMigrate() {
      return switch (type) {
        case ROW -> !(source instanceof HBox);
        case COL -> !(source instanceof VBox);
        case STACK -> !(source instanceof StackPane);
        case GRID -> !(source instanceof GridPane);
        case FLOW_ROW, FLOW_COL -> !(source instanceof FlowPane);
        case TILE -> !(source instanceof TilePane);
        case ANCHOR -> !(source instanceof AnchorPane);
        case FLEX -> !(source instanceof TwFlexPane);
        case FLEX_GRID -> !(source instanceof TwGridPane);
      };
    }

    private Pane migrateToNew() {
      List<Snap> snaps = snap(source);
      Pane target =
          switch (type) {
            case ROW -> new HBox();
            case COL -> new VBox();
            case STACK -> new StackPane();
            case GRID -> new GridPane();
            case FLOW_ROW -> new FlowPane(Orientation.HORIZONTAL);
            case FLOW_COL -> new FlowPane(Orientation.VERTICAL);
            case ANCHOR -> new AnchorPane();
            case TILE -> new TilePane();
            case FLEX -> new TwFlexPane();
            case FLEX_GRID -> TwGridPane.create().build();
          };
      restore(target, snaps);
      return target;
    }

    private record Snap(
        Node n,
        Priority hgrow,
        Priority vgrow,
        Insets hm,
        Insets vm,
        Integer col,
        Integer row,
        Integer cs,
        Integer rs,
        HPos ha,
        VPos va,
        Pos stackAlign,
        Insets gm,
        double[] anchorEdges) {}

    private List<Snap> snap(Pane p) {
      List<Snap> list = new ArrayList<>();
      for (Node n : p.getChildren()) {
        double[] aEdges = null;
        if (p instanceof AnchorPane) {
          Double t = AnchorPane.getTopAnchor(n),
              r = AnchorPane.getRightAnchor(n),
              b = AnchorPane.getBottomAnchor(n),
              l = AnchorPane.getLeftAnchor(n);
          if (t != null || r != null || b != null || l != null) {
            aEdges =
                new double[] {
                  t != null ? t : -1, r != null ? r : -1, b != null ? b : -1, l != null ? l : -1
                };
          }
        }
        list.add(
            new Snap(
                n,
                HBox.getHgrow(n),
                VBox.getVgrow(n),
                HBox.getMargin(n),
                VBox.getMargin(n),
                GridPane.getColumnIndex(n),
                GridPane.getRowIndex(n),
                GridPane.getColumnSpan(n),
                GridPane.getRowSpan(n),
                GridPane.getHalignment(n),
                GridPane.getValignment(n),
                StackPane.getAlignment(n),
                GridPane.getMargin(n),
                aEdges));
      }
      return list;
    }

    private void restore(Pane target, List<Snap> snaps) {
      for (Snap s : snaps) {
        target.getChildren().add(s.n());
        if (target instanceof HBox) {
          HBox hb = (HBox) target;
          if (s.hgrow() != null) HBox.setHgrow(s.n(), s.hgrow());
          if (s.hm() != null) HBox.setMargin(s.n(), s.hm());
        } else if (target instanceof VBox) {
          VBox vb = (VBox) target;
          if (s.vgrow() != null) VBox.setVgrow(s.n(), s.vgrow());
          if (s.vm() != null) VBox.setMargin(s.n(), s.vm());
        } else if (target instanceof GridPane) {
          GridPane gp = (GridPane) target;
          if (s.col() != null) GridPane.setColumnIndex(s.n(), s.col());
          if (s.row() != null) GridPane.setRowIndex(s.n(), s.row());
          if (s.cs() != null) GridPane.setColumnSpan(s.n(), s.cs());
          if (s.rs() != null) GridPane.setRowSpan(s.n(), s.rs());
          if (s.ha() != null) GridPane.setHalignment(s.n(), s.ha());
          if (s.va() != null) GridPane.setValignment(s.n(), s.va());
          if (s.gm() != null) GridPane.setMargin(s.n(), s.gm());
        } else if (target instanceof AnchorPane) {
          if (s.anchorEdges() != null) applyAnchor(s.n(), s.anchorEdges());
          double[] builderC = anchors.get(s.n());
          if (builderC != null) applyAnchor(s.n(), builderC);
        } else if (target instanceof StackPane) {
          StackPane sp = (StackPane) target;
          if (s.stackAlign() != null) StackPane.setAlignment(s.n(), s.stackAlign());
        }
      }
    }

    private void configure(Pane p) {
      double h = hgapVal >= 0 ? hgapVal : gap;
      double v = vgapVal >= 0 ? vgapVal : gap;
      boolean hasPad = !padding.equals(Insets.EMPTY);

      if (p instanceof HBox hb) {
        hb.setSpacing(gap);
        hb.setAlignment(alignment);
        if (hasPad) hb.setPadding(padding);
      } else if (p instanceof VBox vb) {
        vb.setSpacing(gap);
        vb.setAlignment(alignment);
        if (hasPad) vb.setPadding(padding);
      } else if (p instanceof StackPane sp) {
        sp.setAlignment(alignment);
        if (hasPad) sp.setPadding(padding);
      } else if (p instanceof GridPane gp) {
        gp.setHgap(h);
        gp.setVgap(v);
        gp.setAlignment(alignment);
        if (hasPad) gp.setPadding(padding);
        applyGridCols(gp);
      } else if (p instanceof FlowPane fp) {
        fp.setHgap(h);
        fp.setVgap(v);
        fp.setAlignment(alignment);
        if (hasPad) fp.setPadding(padding);
        fp.setOrientation(
            type == LayoutType.FLOW_COL ? Orientation.VERTICAL : Orientation.HORIZONTAL);
      } else if (p instanceof TilePane tp) {
        double th = hgapVal >= 0 ? hgapVal : (gap >= 0 ? gap : 0);
        double tv = vgapVal >= 0 ? vgapVal : (gap >= 0 ? gap : 0);
        tp.setHgap(th);
        tp.setVgap(tv);
        tp.setAlignment(alignment);
        if (hasPad) tp.setPadding(padding);
      } else if (p instanceof AnchorPane ap) {
        if (hasPad) ap.setPadding(padding);
        anchors.forEach((n, cc) -> applyAnchor(n, cc));
      } else if (p instanceof TwFlexPane fp) {
        fp.setDirection(TwFlexPane.Direction.ROW);
        fp.setJustify(flexJustify);
        fp.setAlign(flexAlign);
        fp.setWrap(flexWrap);
        fp.gap(gap);
        if (hasPad) fp.padding(padding);
      } else if (p instanceof TwGridPane fg) {
        if (gridAreas != null) {
          fg.areas(gridAreas);
        } else {
          fg.cols(gridCols2);
        }
        if (gap > 0) fg.gap(gap);
        if (hasPad) fg.padding(padding);
      } else {
        if (hasPad) p.setPadding(padding);
      }
    }

    private void applyAnchor(Node n, double[] c) {
      if (c[0] >= 0) AnchorPane.setTopAnchor(n, c[0]);
      if (c[1] >= 0) AnchorPane.setRightAnchor(n, c[1]);
      if (c[2] >= 0) AnchorPane.setBottomAnchor(n, c[2]);
      if (c[3] >= 0) AnchorPane.setLeftAnchor(n, c[3]);
    }

    private void applyGridCols(GridPane gp) {
      if (gridCols <= 0) return;
      if (!gp.getColumnConstraints().isEmpty()) return;
      gp.getColumnConstraints().clear();
      for (int i = 0; i < gridCols; i++) {
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        cc.setPercentWidth(100.0 / gridCols);
        gp.getColumnConstraints().add(cc);
      }
      List<Node> kids = new ArrayList<>(gp.getChildren());
      for (int i = 0; i < kids.size(); i++) {
        Node child = kids.get(i);
        if (GridPane.getColumnIndex(child) == null) {
          GridPane.setColumnIndex(child, i % gridCols);
          GridPane.setRowIndex(child, i / gridCols);
        }
      }
    }

    private void applyGrowth(Pane p) {
      if (!growAll) return;
      for (Node child : p.getChildren()) {
        if (p instanceof HBox) HBox.setHgrow(child, Priority.ALWAYS);
        if (p instanceof VBox) VBox.setVgrow(child, Priority.ALWAYS);
      }
    }

    private void applySize(Pane p) {
      if (prefW >= 0) p.setPrefWidth(prefW);
      if (prefH >= 0) p.setPrefHeight(prefH);
      if (minW >= 0) p.setMinWidth(minW);
      if (minH >= 0) p.setMinHeight(minH);
      if (maxW >= 0) p.setMaxWidth(maxW);
      if (maxH >= 0) p.setMaxHeight(maxH);
      if (fillW) p.setMaxWidth(Double.MAX_VALUE);
      if (fillH) p.setMaxHeight(Double.MAX_VALUE);
    }

    private static int autoGridCols(int n) {
      if (n <= 1) return 1;
      if (n <= 4) return 2;
      if (n <= 9) return 3;
      return 4;
    }
  }

  // =========================================================================
  // Static helpers
  // =========================================================================

  public static void hgrow(Node n) {
    HBox.setHgrow(n, Priority.ALWAYS);
  }

  public static void vgrow(Node n) {
    VBox.setVgrow(n, Priority.ALWAYS);
  }

  public static void grow(Node n) {
    hgrow(n);
    vgrow(n);
  }

  public static void margin(Node n, double all) {
    Insets i = new Insets(all);
    HBox.setMargin(n, i);
    VBox.setMargin(n, i);
  }

  public static void margin(Node n, double t, double r, double b, double l) {
    Insets i = new Insets(t, r, b, l);
    HBox.setMargin(n, i);
    VBox.setMargin(n, i);
  }

  /** Flexible spacer — pushes elements to the opposite end */
  public static Region spacer() {
    Region r = new Region();
    HBox.setHgrow(r, Priority.ALWAYS);
    VBox.setVgrow(r, Priority.ALWAYS);
    return r;
  }

  /** Fixed-size spacer */
  public static Region spacer(double size) {
    Region r = new Region();
    r.setMinWidth(size);
    r.setPrefWidth(size);
    r.setMinHeight(size);
    r.setPrefHeight(size);
    return r;
  }

  /** Anchors a node to all sides of its AnchorPane parent */
  public static void anchorFill(Node n) {
    AnchorPane.setTopAnchor(n, 0.0);
    AnchorPane.setRightAnchor(n, 0.0);
    AnchorPane.setBottomAnchor(n, 0.0);
    AnchorPane.setLeftAnchor(n, 0.0);
  }
}
