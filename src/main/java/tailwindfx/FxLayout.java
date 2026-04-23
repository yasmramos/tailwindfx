package tailwindfx;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FxLayout v2 — Layout Engine inteligente de TailwindFX.
 *
 * Principios:
 *   1. MUTACIÓN INTELIGENTE: si el Pane source ya es el tipo correcto,
 *      nunca se recrea. Solo se reconfiguran propiedades. Crítico para
 *      llamar desde listeners de breakpoint sin rehacer el árbol.
 *
 *   2. MIGRACIÓN DE CONSTRAINTS: al cambiar de Pane, las constraints
 *      (hgrow, vgrow, margin, gridCol/Row) de los hijos se preservan.
 *
 *   3. RESPONSIVE SWITCH: reutilizar el mismo builder con .row()/.col()
 *      y llamar build() de nuevo es seguro y eficiente.
 *
 *   4. ANCHORPANE FLUENT: anchorAll / anchorFill / anchorTop/Right/Bottom/Left
 *
 *   5. AUTO GRID COLS: si no se especifican columnas, se calculan
 *      automáticamente según el número de hijos.
 *
 * Uso básico:
 *   TailwindFX.layout(pane).row().gap(12).center().build();
 *   TailwindFX.layout(pane).grid(3).hgap(16).vgap(16).build();
 *   TailwindFX.layout(pane).flowRow().gap(8).build();
 *
 * Responsive switch (seguro — no recrea el Pane):
 *   FxLayout lyt = TailwindFX.layout(container).gap(12);
 *   stage.widthProperty().addListener((o, old, w) -> {
 *       if (w.doubleValue() < 768) lyt.col().build();
 *       else                       lyt.row().build();
 *   });
 *
 * AnchorPane:
 *   TailwindFX.layout(root).anchor()
 *       .anchorFill(content, 0, 0, 0, 0)
 *       .build();
 */
public final class FxLayout {

    public enum LayoutType { ROW, COL, STACK, GRID, FLOW_ROW, FLOW_COL, ANCHOR, TILE, FLEX, FLEX_GRID }

    // --- estado ---
    private final Pane   source;
    private LayoutType   type      = LayoutType.ROW;
    private double       gap       = 0;

    // ── Thread safety ──────────────────────────────────────────────────────────
    /**
     * Asserts that the current thread is the JavaFX Application Thread.
     * All FxLayout operations must run on the FX thread because they modify
     * the live scene graph.
     */
    private static void checkFxThread() {
        if (!javafx.application.Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                "FxLayout must be used on the JavaFX Application Thread. "
                + "Use Platform.runLater() to schedule layout changes from background threads.");
        }
    }

    // ── FxFlexPane / FxGridPane properties ─────────────────────────────────────
    private FxFlexPane.Justify   flexJustify  = FxFlexPane.Justify.START;
    private FxFlexPane.Align     flexAlign    = FxFlexPane.Align.START;
    private boolean              flexWrap     = false;
    private String[]             gridAreas    = null;
    private int                  gridCols2    = 3;
    private double       hgapVal   = -1;
    private double       vgapVal   = -1;
    private Insets       padding   = Insets.EMPTY;
    private Pos          alignment = Pos.TOP_LEFT;
    private int          gridCols  = -1;
    private boolean      growAll   = false;
    private boolean      fillW     = false;
    private boolean      fillH     = false;
    private boolean      debugMode = false;
    private LayoutTransitionListener transitionListener = null;
    private double       minW = -1, minH = -1, maxW = -1, maxH = -1;
    private double       prefW = -1, prefH = -1;
    private final Map<Node, double[]> anchors = new LinkedHashMap<>();

    FxLayout(Pane source) { this.source = Preconditions.requireNonNull(source, "FxLayout", "pane"); }

    // =========================================================================
    // Tipo de layout
    // =========================================================================

    public FxLayout row()          { type = LayoutType.ROW;      return this; }
    public FxLayout col()          { type = LayoutType.COL;      return this; }
    public FxLayout stack()        { type = LayoutType.STACK;    return this; }
    public FxLayout grid(int cols) { type = LayoutType.GRID; gridCols = Preconditions.requireSpan(cols, "FxLayout.grid"); return this; }
    public FxLayout grid()         { type = LayoutType.GRID; gridCols = -1;   return this; }
    public FxLayout flowRow()      { type = LayoutType.FLOW_ROW; return this; }
    public FxLayout flowCol()      { type = LayoutType.FLOW_COL; return this; }
    /** TilePane — cuadrícula de tiles de tamaño uniforme. */
    public FxLayout tile()         { type = LayoutType.TILE;     return this; }
    public FxLayout anchor()       { type = LayoutType.ANCHOR;   return this; }

    // ── FxFlexPane builder ────────────────────────────────────────────────────

    /**
     * Switches to {@link FxFlexPane} mode.
     * The container must be an {@link FxFlexPane} or will be converted on {@link #build()}.
     *
     * <pre>
     * FxFlexPane cards = (FxFlexPane) TailwindFX.layout(flexPane)
     *     .flex().wrap(true).justify(FxFlexPane.Justify.BETWEEN).gap(16).build();
     * </pre>
     */
    public FxLayout flex()         { type = LayoutType.FLEX;      return this; }

    /**
     * Switches to {@link FxGridPane} (grid-template-areas) mode.
     * Use {@link #areas(String...)} to define the grid.
     *
     * <pre>
     * FxGridPane page = (FxGridPane) TailwindFX.layout(new FxGridPane())
     *     .flexGrid()
     *     .areas("header header", "sidebar main", "footer footer")
     *     .gap(12).build();
     * </pre>
     */
    public FxLayout flexGrid()     { type = LayoutType.FLEX_GRID; return this; }

    /**
     * Sets the justify-content for a flex container.
     * Only applies when type is {@link LayoutType#FLEX}.
     *
     * @param justify the justify-content value
     */
    public FxLayout justify(FxFlexPane.Justify justify) {
        this.flexJustify = justify; return this;
    }

    /**
     * Sets the align-items for a flex container.
     * Only applies when type is {@link LayoutType#FLEX}.
     *
     * @param align the align-items value
     */
    public FxLayout alignItems(FxFlexPane.Align align) {
        this.flexAlign = align; return this;
    }

    /**
     * Enables or disables flex-wrap.
     * Only applies when type is {@link LayoutType#FLEX}.
     *
     * @param wrap {@code true} to enable wrapping
     */
    public FxLayout wrap(boolean wrap) { this.flexWrap = wrap; return this; }

    /**
     * Defines grid-template-areas for a {@link FxGridPane}.
     * Only applies when type is {@link LayoutType#FLEX_GRID}.
     *
     * <pre>
     * .areas("header header",
     *        "sidebar main",
     *        "footer footer")
     * </pre>
     *
     * @param rows each string defines one row of named areas
     */
    public FxLayout areas(String... rows) {
        this.gridAreas = rows;
        return this;
    }

    /**
     * Sets the number of columns for {@link FxGridPane} auto-flow or masonry mode.
     * Only applies when type is {@link LayoutType#FLEX_GRID}.
     *
     * @param cols number of columns
     */
    public FxLayout cols(int cols) { this.gridCols2 = cols; return this; }

    // =========================================================================
    // Spacing
    // =========================================================================

    public FxLayout gap(double v)  { gap     = v; return this; }
    public FxLayout hgap(double v) { hgapVal = v; return this; }
    public FxLayout vgap(double v) { vgapVal = v; return this; }

    // =========================================================================
    // Padding
    // =========================================================================

    public FxLayout padding(double all)                             { padding = new Insets(all);             return this; }
    public FxLayout padding(double tb, double lr)                   { padding = new Insets(tb, lr, tb, lr);  return this; }
    public FxLayout padding(double t, double r, double b, double l) { padding = new Insets(t, r, b, l); return this; }

    /**
     * Sets padding from a CSS-like shorthand string (values in px, no unit suffix needed).
     *
     * <ul>
     *   <li>{@code "16"}       → uniform 16px</li>
     *   <li>{@code "8 16"}     → 8px top/bottom, 16px left/right</li>
     *   <li>{@code "4 8 4 8"}  → top right bottom left</li>
     * </ul>
     *
     * <pre>
     * TailwindFX.layout(card).row().padding("16").build();
     * TailwindFX.layout(form).col().padding("8 16").build();
     * </pre>
     *
     * @param shorthand space-separated px values (1, 2, or 4 values)
     */
    public FxLayout padding(String shorthand) {
        Preconditions.requireNonBlank(shorthand, "FxLayout.padding", "shorthand");
        String[] parts = shorthand.trim().split("\s+");
        padding = switch (parts.length) {
            case 1 -> new Insets(parsePx(parts[0]));
            case 2 -> new Insets(parsePx(parts[0]), parsePx(parts[1]),
                                  parsePx(parts[0]), parsePx(parts[1]));
            case 4 -> new Insets(parsePx(parts[0]), parsePx(parts[1]),
                                  parsePx(parts[2]), parsePx(parts[3]));
            default -> throw new IllegalArgumentException(
                "FxLayout.padding: expected 1, 2, or 4 values, got: " + parts.length);
        };
        return this;
    }

    private static double parsePx(String s) {
        try { return Double.parseDouble(s.replace("px", "").trim()); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("FxLayout.padding: invalid value '" + s + "'");
        }
    }

    // =========================================================================
    // Alineación
    // =========================================================================

    public FxLayout center()       { alignment = Pos.CENTER;        return this; }
    public FxLayout centerLeft()   { alignment = Pos.CENTER_LEFT;   return this; }
    public FxLayout centerRight()  { alignment = Pos.CENTER_RIGHT;  return this; }
    public FxLayout topLeft()      { alignment = Pos.TOP_LEFT;      return this; }
    public FxLayout topCenter()    { alignment = Pos.TOP_CENTER;    return this; }
    public FxLayout topRight()     { alignment = Pos.TOP_RIGHT;     return this; }
    public FxLayout bottomLeft()   { alignment = Pos.BOTTOM_LEFT;   return this; }
    public FxLayout bottomCenter() { alignment = Pos.BOTTOM_CENTER; return this; }
    public FxLayout bottomRight()  { alignment = Pos.BOTTOM_RIGHT;  return this; }

    // aliases semánticos
    public FxLayout justifyCenter()  { return center(); }
    public FxLayout justifyStart()   { return topLeft(); }
    public FxLayout itemsCenter()    { return centerLeft(); }
    public FxLayout placeCenter()    { return center(); }

    // =========================================================================
    // Crecimiento y tamaño
    // =========================================================================

    public FxLayout grow()               { growAll = true; return this; }
    public FxLayout fillWidth()          { fillW   = true; return this; }
    public FxLayout fillHeight()         { fillH   = true; return this; }
    public FxLayout fill()               { fillW   = true; fillH = true; return this; }
    public FxLayout prefWidth(double w)  { prefW   = w;    return this; }
    public FxLayout prefHeight(double h) { prefH   = h;    return this; }
    public FxLayout minWidth(double w)   { minW    = w;    return this; }
    public FxLayout minHeight(double h)  { minH    = h;    return this; }
    public FxLayout maxWidth(double w)   { maxW    = w;    return this; }
    public FxLayout maxHeight(double h)  { maxH    = h;    return this; }

    // =========================================================================
    // AnchorPane constraints (fluent, para uso con .anchor())
    // =========================================================================

    /** Ancla un nodo a todos los lados con el mismo valor */
    public FxLayout anchorAll(Node n, double v)                              { anchors.put(n, new double[]{v, v, v, v}); return this; }
    /** Ancla a los 4 lados: top, right, bottom, left */
    public FxLayout anchorFill(Node n, double t, double r, double b, double l) { anchors.put(n, new double[]{t, r, b, l}); return this; }

    public FxLayout anchorTop(Node n, double v)    { return setEdge(n, 0, v); }
    public FxLayout anchorRight(Node n, double v)  { return setEdge(n, 1, v); }
    public FxLayout anchorBottom(Node n, double v) { return setEdge(n, 2, v); }
    public FxLayout anchorLeft(Node n, double v)   { return setEdge(n, 3, v); }

    /** Ancla horizontal (left + right) → nodo llena el ancho disponible */
    public FxLayout anchorH(Node n, double left, double right) { return setEdge(n, 3, left).setEdge(n, 1, right); }
    /** Ancla vertical  (top + bottom) → nodo llena el alto disponible  */
    public FxLayout anchorV(Node n, double top, double bottom)  { return setEdge(n, 0, top).setEdge(n, 2, bottom); }

    private FxLayout setEdge(Node n, int i, double v) {
        anchors.computeIfAbsent(n, k -> new double[]{-1,-1,-1,-1})[i] = v;
        return this;
    }

    // =========================================================================
    // build() / reconfigure()
    // =========================================================================

    // =========================================================================
    // Debug mode
    // =========================================================================

    /**
     * Enables debug logging for this layout operation.
     * Prints layout type, container class, child count, and migration decision to stdout.
     *
     * <pre>
     * TailwindFX.layout(pane).row().gap(12).debug().build();
     * // Output: [FxLayout] ROW  source=HBox  children=3  migrate=false
     * </pre>
     */
    public FxLayout debug() { this.debugMode = true; return this; }

    // =========================================================================
    // Transition listener
    // =========================================================================

    /**
     * Callback fired before and after a layout type change.
     * Only invoked when the container is actually migrated (type changes).
     */
    public interface LayoutTransitionListener {
        /**
         * Called just before the container is replaced.
         *
         * @param source  the original container
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
     * Registers a transition listener for this layout operation.
     * Useful for animating between layout types or updating bindings.
     *
     * <pre>
     * TailwindFX.layout(pane).col()
     *     .onTransition(new FxLayout.LayoutTransitionListener() {
     *         public void onLayoutChanging(Pane src, FxLayout.LayoutType t) {
     *             FxAnimation.fadeOut(src, 150).play();
     *         }
     *         public void onLayoutChanged(Pane result) {
     *             FxAnimation.fadeIn(result, 150).play();
     *         }
     *     })
     *     .build();
     * </pre>
     *
     * @param listener the transition listener, or {@code null} to clear
     */
    public FxLayout onTransition(LayoutTransitionListener listener) {
        this.transitionListener = listener;
        return this;
    }

        /**
     * Aplica la configuración. Si el source ya es el tipo correcto,
     * solo reconfigura propiedades (no recrea nada).
     * Si el tipo cambió, migra hijos + constraints a un Pane nuevo.
     */
    public Pane build() {
        checkFxThread();
        validate();

        if (type == LayoutType.GRID && gridCols == -1)
            gridCols = autoGridCols(source.getChildren().size());

        boolean migrate = mustMigrate();

        if (debugMode) {
            System.out.printf("[FxLayout] %-12s source=%-16s children=%-4d migrate=%s%n",
                type, source.getClass().getSimpleName(),
                source.getChildren().size(), migrate);
        }

        if (migrate && transitionListener != null)
            transitionListener.onLayoutChanging(source, type);

        Pane result = migrate ? migrateToNew() : source;

        configure(result);
        applyGrowth(result);
        applySize(result);

        if (migrate && source.getParent() instanceof Pane parent) {
            int idx = parent.getChildren().indexOf(source);
            if (idx >= 0) parent.getChildren().set(idx, result);
        }

        if (migrate && transitionListener != null)
            transitionListener.onLayoutChanged(result);

        return result;
    }

    /**
     * Reconfigura solo propiedades (spacing, alignment, padding)
     * sin tocar el tipo de Pane ni sus hijos.
     * Llamada segura desde cualquier listener.
     */
    public void reconfigure() {
        checkFxThread();
        configure(source);
        applyGrowth(source);
        applySize(source);
    }

    // =========================================================================
    // Internos
    // =========================================================================

    // =========================================================================
    // Validation
    // =========================================================================

    /**
     * Validates the current configuration for logical consistency.
     * Logs warnings (rather than throwing) for recoverable cases.
     */
    private void validate() {
        if (type == LayoutType.GRID && gridCols == -1 && source.getChildren().isEmpty()) {
            Preconditions.LOG.warning(
                "FxLayout.build: GRID type with 0 children and no column count — "
                + "grid will be empty. Call grid(n) to set column count.");
        }
        if (type == LayoutType.FLEX_GRID && gridAreas == null && gridCols2 <= 0) {
            Preconditions.LOG.warning(
                "FxLayout.build: FLEX_GRID with no areas() and cols <= 0 — "
                + "use .areas(...) or .cols(n) to define the grid layout.");
        }
        if (gap < 0) {
            Preconditions.LOG.warning(() -> "FxLayout.build: gap=" + gap + " is negative — did you mean margin?");
        }
        if (type == LayoutType.ANCHOR && anchors.isEmpty()
                && !(source instanceof javafx.scene.layout.AnchorPane)) {
            Preconditions.LOG.fine(
                "FxLayout.build: ANCHOR type but no anchor constraints defined — "
                + "use anchorTop/Right/Bottom/Left() to position children.");
        }
    }

    private boolean mustMigrate() {
        return switch (type) {
            case ROW                -> !(source instanceof HBox);
            case COL                -> !(source instanceof VBox);
            case STACK              -> !(source instanceof StackPane);
            case GRID               -> !(source instanceof GridPane);
            case FLOW_ROW, FLOW_COL -> !(source instanceof FlowPane);
            case TILE               -> !(source instanceof TilePane);
            case ANCHOR             -> !(source instanceof AnchorPane);
            case FLEX               -> !(source instanceof FxFlexPane);
            case FLEX_GRID          -> !(source instanceof FxGridPane);
        };
    }

    private Pane migrateToNew() {
        List<Snap> snaps = snap(source);
        Pane target = switch (type) {
            case ROW      -> new HBox();
            case COL      -> new VBox();
            case STACK    -> new StackPane();
            case GRID     -> new GridPane();
            case FLOW_ROW -> new FlowPane(Orientation.HORIZONTAL);
            case FLOW_COL -> new FlowPane(Orientation.VERTICAL);
            case ANCHOR   -> new AnchorPane();
            case TILE      -> new TilePane();
            case FLEX      -> new FxFlexPane();
            case FLEX_GRID -> FxGridPane.create().build();
        };
        restore(target, snaps);
        return target;
    }

    private record Snap(
        Node n,
        Priority hgrow, Priority vgrow,
        Insets hm, Insets vm,
        Integer col, Integer row, Integer cs, Integer rs,
        HPos ha, VPos va,
        // StackPane per-child alignment
        Pos stackAlign,
        // GridPane per-child margin
        Insets gm,
        // AnchorPane edges [top, right, bottom, left]
        double[] anchorEdges
    ) {}

    private List<Snap> snap(Pane p) {
        List<Snap> list = new ArrayList<>();
        for (Node n : p.getChildren()) {
            // Capture AnchorPane edges from original source if it was an AnchorPane
            double[] aEdges = null;
            if (p instanceof AnchorPane) {
                Double t = AnchorPane.getTopAnchor(n),    r = AnchorPane.getRightAnchor(n),
                       b = AnchorPane.getBottomAnchor(n), l = AnchorPane.getLeftAnchor(n);
                if (t != null || r != null || b != null || l != null) {
                    aEdges = new double[]{
                        t != null ? t : -1, r != null ? r : -1,
                        b != null ? b : -1, l != null ? l : -1
                    };
                }
            }
            list.add(new Snap(n,
                HBox.getHgrow(n),       VBox.getVgrow(n),
                HBox.getMargin(n),      VBox.getMargin(n),
                GridPane.getColumnIndex(n), GridPane.getRowIndex(n),
                GridPane.getColumnSpan(n),  GridPane.getRowSpan(n),
                GridPane.getHalignment(n),  GridPane.getValignment(n),
                StackPane.getAlignment(n),  GridPane.getMargin(n),
                aEdges
            ));
        }
        return list;
    }

    private void restore(Pane target, List<Snap> snaps) {
        for (Snap s : snaps) {
            target.getChildren().add(s.n());
            if (target instanceof HBox) {
                HBox hb = (HBox) target;
                if (s.hgrow() != null) HBox.setHgrow(s.n(),  s.hgrow());
                if (s.hm()    != null) HBox.setMargin(s.n(), s.hm());
            } else if (target instanceof VBox) {
                VBox vb = (VBox) target;
                if (s.vgrow() != null) VBox.setVgrow(s.n(),  s.vgrow());
                if (s.vm()    != null) VBox.setMargin(s.n(), s.vm());
            } else if (target instanceof GridPane) {
                GridPane gp = (GridPane) target;
                if (s.col() != null) GridPane.setColumnIndex(s.n(), s.col());
                if (s.row() != null) GridPane.setRowIndex(s.n(),    s.row());
                if (s.cs()  != null) GridPane.setColumnSpan(s.n(), s.cs());
                if (s.rs()  != null) GridPane.setRowSpan(s.n(),    s.rs());
                if (s.ha()  != null) GridPane.setHalignment(s.n(), s.ha());
                if (s.va()  != null) GridPane.setValignment(s.n(), s.va());
                if (s.gm()  != null) GridPane.setMargin(s.n(),     s.gm());
            } else if (target instanceof AnchorPane) {
                // First restore edges captured from original source
                if (s.anchorEdges() != null) applyAnchor(s.n(), s.anchorEdges());
                // Then apply any builder-specified anchors (override)
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
            gp.setHgap(h); gp.setVgap(v);
            gp.setAlignment(alignment);
            if (hasPad) gp.setPadding(padding);
            applyGridCols(gp);
        } else if (p instanceof FlowPane fp) {
            fp.setHgap(h); fp.setVgap(v);
            fp.setAlignment(alignment);
            if (hasPad) fp.setPadding(padding);
            fp.setOrientation(type == LayoutType.FLOW_COL
                ? Orientation.VERTICAL : Orientation.HORIZONTAL);
        } else if (p instanceof TilePane tp) {
            double th = hgapVal >= 0 ? hgapVal : (gap >= 0 ? gap : 0);
            double tv = vgapVal >= 0 ? vgapVal : (gap >= 0 ? gap : 0);
            tp.setHgap(th); tp.setVgap(tv);
            tp.setAlignment(alignment);
            if (hasPad) tp.setPadding(padding);
        } else if (p instanceof AnchorPane ap) {
            if (hasPad) ap.setPadding(padding);
            anchors.forEach((n, cc) -> applyAnchor(n, cc));
        } else if (p instanceof FxFlexPane fp) {
            fp.setDirection(FxFlexPane.Direction.ROW);
            fp.setJustify(flexJustify);
            fp.setAlign(flexAlign);
            fp.setWrap(flexWrap);
            fp.gap(gap);
            if (hasPad) fp.padding(padding);
        } else if (p instanceof FxGridPane fg) {
            if (gridAreas != null) {
                fg.areas(gridAreas);
            } else {
                fg.cols(gridCols2);
            }
            if (gap > 0) fg.gap(gap);
            if (hasPad)  fg.padding(padding);
        } else {
            if (hasPad) p.setPadding(padding);
        }
    }

    private void applyAnchor(Node n, double[] c) {
        if (c[0] >= 0) AnchorPane.setTopAnchor(n,    c[0]);
        if (c[1] >= 0) AnchorPane.setRightAnchor(n,  c[1]);
        if (c[2] >= 0) AnchorPane.setBottomAnchor(n, c[2]);
        if (c[3] >= 0) AnchorPane.setLeftAnchor(n,   c[3]);
    }

    /**
     * Applies equal-width column constraints to a GridPane.
     * Skips if the GridPane already has manually defined constraints,
     * so user-configured layouts are not overwritten.
     */
    private void applyGridCols(GridPane gp) {
        if (gridCols <= 0) return;
        // Only auto-configure if no manual constraints exist
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
                GridPane.setRowIndex(child,    i / gridCols);
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
        if (minW  >= 0) p.setMinWidth(minW);
        if (minH  >= 0) p.setMinHeight(minH);
        if (maxW  >= 0) p.setMaxWidth(maxW);
        if (maxH  >= 0) p.setMaxHeight(maxH);
        if (fillW) p.setMaxWidth(Double.MAX_VALUE);
        if (fillH) p.setMaxHeight(Double.MAX_VALUE);
    }

    private static int autoGridCols(int n) {
        if (n <= 1) return 1;
        if (n <= 4) return 2;
        if (n <= 9) return 3;
        return 4;
    }

    // =========================================================================
    // Helpers estáticos
    // =========================================================================

    public static void hgrow(Node n) { HBox.setHgrow(n, Priority.ALWAYS); }
    public static void vgrow(Node n) { VBox.setVgrow(n, Priority.ALWAYS); }
    public static void grow(Node n)  { hgrow(n); vgrow(n); }

    public static void margin(Node n, double all) {
        Insets i = new Insets(all);
        HBox.setMargin(n, i); VBox.setMargin(n, i);
    }

    public static void margin(Node n, double t, double r, double b, double l) {
        Insets i = new Insets(t, r, b, l);
        HBox.setMargin(n, i); VBox.setMargin(n, i);
    }

    /** Spacer flexible — empuja elementos al otro extremo */
    public static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        VBox.setVgrow(r, Priority.ALWAYS);
        return r;
    }

    /** Spacer de tamaño fijo */
    public static Region spacer(double size) {
        Region r = new Region();
        r.setMinWidth(size);  r.setPrefWidth(size);
        r.setMinHeight(size); r.setPrefHeight(size);
        return r;
    }

    /** Ancla un nodo a todos los lados de su AnchorPane padre */
    public static void anchorFill(Node n) {
        AnchorPane.setTopAnchor(n, 0.0);    AnchorPane.setRightAnchor(n, 0.0);
        AnchorPane.setBottomAnchor(n, 0.0); AnchorPane.setLeftAnchor(n, 0.0);
    }

}
