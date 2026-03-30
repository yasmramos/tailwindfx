package tailwindfx;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FxGridPane — a declarative, Tailwind-style grid container for JavaFX.
 *
 * <p>
 * Wraps {@link GridPane} with a fluent API covering features that neither plain
 * {@code GridPane} nor CSS utility classes can express directly:
 *
 * <ul>
 * <li><b>grid-auto-flow</b> — {@link #autoFlow(AutoFlow)}: children placed
 * automatically in row or column order, with optional dense packing.</li>
 * <li><b>grid-template-areas</b> — {@link #areas(String...)}: named regions
 * ({@code "header header"}, {@code "sidebar main"}) mapped to children by name
 * via {@link #placeIn(Node, String)}.</li>
 * <li><b>grid-cols / grid-rows utilities</b> — {@link #cols(int)},
 *       {@link #rows(int)}: uniform column/row count with equal sizing.</li>
 * <li><b>Masonry-like layout</b> — {@link #masonry(int)}: auto-flow column mode
 * with items placed in the shortest column.</li>
 * <li><b>gap semantics</b> — {@link #gap(double)}, {@link #gapX(double)},
 *       {@link #gapY(double)}: consistent with FxFlexPane.</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>
 * // 3-column auto-flow grid:
 * FxGridPane grid = FxGridPane.create()
 *     .cols(3).gap(16).autoFlow(AutoFlow.ROW)
 *     .build();
 * grid.getChildren().addAll(card1, card2, card3, card4);
 *
 * // Template areas:
 * FxGridPane page = FxGridPane.create()
 *     .areas("header header",
 *            "sidebar main",
 *            "footer footer")
 *     .gap(8).build();
 * page.placeIn(header, "header");
 * page.placeIn(sidebar, "sidebar");
 * page.placeIn(main,    "main");
 * page.placeIn(footer,  "footer");
 *
 * // Masonry (shortest-column placement):
 * FxGridPane pins = FxGridPane.create().masonry(3).gap(12).build();
 * pins.getChildren().addAll(pin1, pin2, pin3, pin4, pin5);
 *
 * // Per-child span:
 * FxGridPane.setColSpan(wideCard, 2);
 * FxGridPane.setRowSpan(tallCard, 2);
 * </pre>
 */
public final class FxGridPane extends Pane {

    // =========================================================================
    // Enums
    // =========================================================================
    /**
     * Auto-flow direction for automatically placed children (analogous to CSS
     * {@code grid-auto-flow}).
     */
    public enum AutoFlow {
        ROW, // fill each row, then move to next row (default)
        COL, // fill each column, then move to next column
        ROW_DENSE, // row flow with dense packing (fills gaps)
        COL_DENSE     // column flow with dense packing
    }

    // =========================================================================
    // Builder
    // =========================================================================
    /**
     * Returns a new builder for FxGridPane.
     */
    public static Builder create() {
        return new Builder();
    }

    public static final class Builder {

        int cols = 1;
        int rows = 0;   // 0 = inferred from children
        double gapX = 0;
        double gapY = 0;
        AutoFlow autoFlow = AutoFlow.ROW;
        String[] areaTemplates = null;
        boolean masonryMode = false;
        Insets padding = Insets.EMPTY;

        public Builder cols(int c) {
            if (c < 1) {
                throw new IllegalArgumentException("FxGridPane.cols: must be >= 1");
            }
            this.cols = c;
            return this;
        }

        public Builder rows(int r) {
            if (r < 0) {
                throw new IllegalArgumentException("FxGridPane.rows: must be >= 0");
            }
            this.rows = r;
            return this;
        }

        public Builder gap(double px) {
            gapX = px;
            gapY = px;
            return this;
        }

        public Builder gapX(double px) {
            gapX = px;
            return this;
        }

        public Builder gapY(double px) {
            gapY = px;
            return this;
        }

        public Builder autoFlow(AutoFlow af) {
            this.autoFlow = af;
            return this;
        }

        public Builder areas(String... rows) {
            this.areaTemplates = rows;
            return this;
        }

        public Builder masonry(int columns) {
            this.cols = columns;
            this.masonryMode = true;
            return this;
        }

        public Builder padding(double px) {
            this.padding = new Insets(px);
            return this;
        }

        public Builder padding(Insets p) {
            this.padding = p;
            return this;
        }

        public FxGridPane build() {
            return new FxGridPane(this);
        }
    }

    // =========================================================================
    // State
    // =========================================================================
    private int cols;
    private int rows;
    private double gapX;
    private double gapY;
    private AutoFlow autoFlow;
    private boolean masonryMode;
    private Insets padding;

    // Template-area map: area-name → [col, row, colSpan, rowSpan]
    private final Map<String, int[]> areaMap = new LinkedHashMap<>();

    // Per-child property keys
    private static final String COL_SPAN_KEY = "tailwindfx.grid.col-span";
    private static final String ROW_SPAN_KEY = "tailwindfx.grid.row-span";
    private static final String AREA_KEY = "tailwindfx.grid.area";

    // =========================================================================
    // Construction
    // =========================================================================
    private FxGridPane(Builder b) {
        this.cols = b.cols;
        this.rows = b.rows;
        this.gapX = b.gapX;
        this.gapY = b.gapY;
        this.autoFlow = b.autoFlow;
        this.masonryMode = b.masonryMode;
        this.padding = b.padding;
        if (b.areaTemplates != null) {
            parseAreaTemplates(b.areaTemplates);
        }
        getChildren().addListener((javafx.collections.ListChangeListener<Node>) c -> requestLayout());
    }

    // =========================================================================
    // Template areas
    // =========================================================================
    /**
     * Parses area template strings and builds the area map. Each string is a
     * row; each word is a cell name. Consecutive identical names in the same
     * row = column span. Identical names in consecutive rows at same column =
     * row span.
     *
     * <pre>
     * grid.areas(
     *   "header header",   // header spans 2 columns
     *   "sidebar main",    // sidebar col=0, main col=1
     *   "footer footer"    // footer spans 2 columns
     * );
     * </pre>
     */
    private void parseAreaTemplates(String[] templates) {
        areaMap.clear();
        // Build a 2D name grid
        String[][] grid = new String[templates.length][];
        for (int r = 0; r < templates.length; r++) {
            grid[r] = templates[r].trim().split("\\s+");
        }
        // Update cols from area template if wider
        if (grid.length > 0) {
            this.cols = Math.max(cols, grid[0].length);
        }
        // Find each unique area name: its top-left cell, col-span, row-span
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                String name = grid[r][c];
                if (areaMap.containsKey(name) || name.equals(".")) {
                    continue;
                }
                // Measure colSpan
                int cs = 1;
                while (c + cs < grid[r].length && grid[r][c + cs].equals(name)) {
                    cs++;
                }
                // Measure rowSpan
                int rs = 1;
                while (r + rs < grid.length && grid[r + rs].length > c
                        && grid[r + rs][c].equals(name)) {
                    rs++;
                }
                areaMap.put(name, new int[]{c, r, cs, rs});
            }
        }
    }

    /**
     * Sets the area template strings after construction.
     *
     * @param templates row strings (e.g.
     * {@code "header header"}, {@code "sidebar main"})
     */
    public FxGridPane areas(String... templates) {
        parseAreaTemplates(templates);
        requestLayout();
        return this;
    }

    /**
     * Places a node in a named template area.
     *
     * @param node the child node (must be in getChildren())
     * @param area the area name as defined in {@link #areas(String...)}
     * @throws IllegalArgumentException if the area name is unknown
     */
    public FxGridPane placeIn(Node node, String area) {
        Preconditions.requireNonNull(node, "FxGridPane.placeIn", "node");
        Preconditions.requireNonBlank(area, "FxGridPane.placeIn", "area");
        if (!areaMap.containsKey(area)) {
            throw new IllegalArgumentException(
                    "FxGridPane.placeIn: unknown area '" + area + "'. Defined areas: " + areaMap.keySet());
        }
        node.getProperties().put(AREA_KEY, area);
        if (!getChildren().contains(node)) {
            getChildren().add(node);
        }
        requestLayout();
        return this;
    }

    // =========================================================================
    // Per-child span
    // =========================================================================
    /**
     * Sets the column span for a child (analogous to {@code .col-span-N}).
     *
     * @param node the child node
     * @param span column span (>= 1)
     */
    public static void setColSpan(Node node, int span) {
        if (span < 1) {
            throw new IllegalArgumentException("setColSpan: span must be >= 1");
        }
        node.getProperties().put(COL_SPAN_KEY, span);
        if (node.getParent() instanceof FxGridPane gp) {
            gp.requestLayout();
        }
    }

    /**
     * Sets the row span for a child (analogous to {@code .row-span-N}).
     *
     * @param node the child node
     * @param span row span (>= 1)
     */
    public static void setRowSpan(Node node, int span) {
        if (span < 1) {
            throw new IllegalArgumentException("setRowSpan: span must be >= 1");
        }
        node.getProperties().put(ROW_SPAN_KEY, span);
        if (node.getParent() instanceof FxGridPane gp) {
            gp.requestLayout();
        }
    }

    public static int getColSpan(Node node) {
        Object v = node.getProperties().get(COL_SPAN_KEY);
        return v instanceof Integer i ? i : 1;
    }

    public static int getRowSpan(Node node) {
        Object v = node.getProperties().get(ROW_SPAN_KEY);
        return v instanceof Integer i ? i : 1;
    }

    // =========================================================================
    // Runtime mutators
    // =========================================================================
    public FxGridPane cols(int c) {
        this.cols = c;
        requestLayout();
        return this;
    }

    public FxGridPane gap(double px) {
        gapX = px;
        gapY = px;
        requestLayout();
        return this;
    }

    public FxGridPane gapX(double px) {
        this.gapX = px;
        requestLayout();
        return this;
    }

    public FxGridPane gapY(double px) {
        this.gapY = px;
        requestLayout();
        return this;
    }

    public FxGridPane autoFlow(AutoFlow af) {
        this.autoFlow = af;
        requestLayout();
        return this;
    }

    /**
     * Sets padding after construction (analogous to {@code .p-N}). Triggers
     * layout pass.
     */
    public FxGridPane padding(double px) {
        this.padding = new Insets(px);
        requestLayout();
        return this;
    }

    /**
     * Sets padding with Insets after construction. Triggers layout pass.
     */
    public FxGridPane padding(Insets p) {
        this.padding = Preconditions.requireNonNull(p, "FxGridPane.padding", "padding");
        requestLayout();
        return this;
    }

    /**
     * Sets horizontal padding (left/right).
     */
    public FxGridPane paddingX(double px) {
        this.padding = new Insets(padding.getTop(), px, padding.getBottom(), px);
        requestLayout();
        return this;
    }

    /**
     * Sets vertical padding (top/bottom).
     */
    public FxGridPane paddingY(double px) {
        this.padding = new Insets(px, padding.getRight(), px, padding.getLeft());
        requestLayout();
        return this;
    }

// Getter opcional (útil para debugging o bindings)
    public Insets getInternalPadding() {
        return padding;
    }

    public int getCols() {
        return cols;
    }

    public double getGapX() {
        return gapX;
    }

    public double getGapY() {
        return gapY;
    }

    public AutoFlow getAutoFlow() {
        return autoFlow;
    }

    // =========================================================================
    // Layout engine
    // =========================================================================
    @Override
    protected void layoutChildren() {
        long t0 = System.nanoTime();
        List<Node> children = getManagedChildren();
        if (children.isEmpty()) {
            return;
        }

        double w = getWidth() - padding.getLeft() - padding.getRight();
        double h = getHeight() - padding.getTop() - padding.getBottom();
        double ox = padding.getLeft(), oy = padding.getTop();

        if (!areaMap.isEmpty()) {
            layoutByAreas(children, w, h, ox, oy);
        } else if (masonryMode) {
            layoutMasonry(children, w, h, ox, oy);
        } else {
            layoutAutoFlow(children, w, h, ox, oy);
        }
        TailwindFXMetrics.instance().recordLayoutPass(System.nanoTime() - t0);
    }

    // ── Auto-flow layout ─────────────────────────────────────────────────────
    private void layoutAutoFlow(List<Node> children, double w, double h, double ox, double oy) {
        boolean isCol = autoFlow == AutoFlow.COL || autoFlow == AutoFlow.COL_DENSE;
        boolean dense = autoFlow == AutoFlow.ROW_DENSE || autoFlow == AutoFlow.COL_DENSE;

        int gridCols = cols;
        int gridRows = rows > 0 ? rows : (int) Math.ceil((double) children.size() / cols);

        double cellW = (w - gapX * (gridCols - 1)) / gridCols;
        double cellH = gridRows > 1 ? (h - gapY * (gridRows - 1)) / gridRows
                : computeMaxCellHeight(children);

        // Simple occupancy grid for dense packing
        boolean[][] occupied = dense ? new boolean[gridRows + 10][gridCols] : null;

        int curCol = 0, curRow = 0;

        for (Node child : children) {
            int cs = getColSpan(child);
            int rs = getRowSpan(child);

            // Find placement
            int[] pos;
            if (dense && occupied != null) {
                pos = findDensePos(occupied, cs, rs, gridCols, gridRows + 10);
                markOccupied(occupied, pos[0], pos[1], cs, rs);
                curCol = isCol ? pos[0] : pos[0];
                curRow = isCol ? pos[1] : pos[1];
            } else {
                if (isCol) {
                    pos = new int[]{curCol, curRow};
                    curRow += rs;
                    if (curRow >= (rows > 0 ? rows : Integer.MAX_VALUE)) {
                        curRow = 0;
                        curCol++;
                    }
                } else {
                    pos = new int[]{curCol, curRow};
                    curCol += cs;
                    if (curCol >= gridCols) {
                        curCol = 0;
                        curRow++;
                    }
                }
            }

            double cx = ox + pos[0] * (cellW + gapX);
            double cy = oy + pos[1] * (cellH + gapY);
            double cw = cellW * cs + gapX * (cs - 1);
            double ch = cellH * rs + gapY * (rs - 1);
            child.resizeRelocate(cx, cy, cw, ch);
        }
    }

    private int[] findDensePos(boolean[][] occupied, int cs, int rs, int maxCols, int maxRows) {
        for (int r = 0; r < maxRows; r++) {
            for (int c = 0; c <= maxCols - cs; c++) {
                if (canPlace(occupied, c, r, cs, rs, maxCols, maxRows)) {
                    return new int[]{c, r};
                }
            }
        }
        return new int[]{0, 0};
    }

    private boolean canPlace(boolean[][] occupied, int c, int r, int cs, int rs, int maxCols, int maxRows) {
        if (c + cs > maxCols || r + rs > maxRows) {
            return false;
        }
        for (int dr = 0; dr < rs; dr++) {
            for (int dc = 0; dc < cs; dc++) {
                if (occupied[r + dr][c + dc]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void markOccupied(boolean[][] occupied, int c, int r, int cs, int rs) {
        for (int dr = 0; dr < rs; dr++) {
            for (int dc = 0; dc < cs; dc++) {
                occupied[r + dr][c + dc] = true;
            }
        }
    }

    // ── Area layout ──────────────────────────────────────────────────────────
    private void layoutByAreas(List<Node> children, double w, double h, double ox, double oy) {
        // Count unique cols/rows in the area map
        int gridCols = areaMap.values().stream().mapToInt(a -> a[0] + a[2]).max().orElse(cols);
        int gridRows = areaMap.values().stream().mapToInt(a -> a[1] + a[3]).max().orElse(1);

        double cellW = (w - gapX * (gridCols - 1)) / gridCols;
        double cellH = (h - gapY * (gridRows - 1)) / gridRows;

        for (Node child : children) {
            Object areaName = child.getProperties().get(AREA_KEY);
            if (areaName instanceof String area && areaMap.containsKey(area)) {
                int[] def = areaMap.get(area); // [col, row, colSpan, rowSpan]
                double cx = ox + def[0] * (cellW + gapX);
                double cy = oy + def[1] * (cellH + gapY);
                double cw = cellW * def[2] + gapX * (def[2] - 1);
                double ch = cellH * def[3] + gapY * (def[3] - 1);
                child.resizeRelocate(cx, cy, cw, ch);
            } else {
                // Unmapped child — hide or place at origin
                child.resizeRelocate(ox, oy, 0, 0);
            }
        }
    }

    // ── Masonry layout ───────────────────────────────────────────────────────
    private void layoutMasonry(List<Node> children, double w, double h, double ox, double oy) {
        double cellW = (w - gapX * (cols - 1)) / cols;
        double[] colHeights = new double[cols];

        for (Node child : children) {
            // Place in shortest column
            int shortestCol = 0;
            for (int c = 1; c < cols; c++) {
                if (colHeights[c] < colHeights[shortestCol]) {
                    shortestCol = c;
                }
            }
            double childPrefH = child instanceof Region r ? r.prefHeight(cellW) : child.prefHeight(cellW);
            double cx = ox + shortestCol * (cellW + gapX);
            double cy = oy + colHeights[shortestCol];
            child.resizeRelocate(cx, cy, cellW, childPrefH);
            colHeights[shortestCol] += childPrefH + gapY;
        }
    }

    // =========================================================================
    // Size computation
    // =========================================================================
    private double computeMaxCellHeight(List<Node> children) {
        return children.stream().mapToDouble(n -> n instanceof Region r ? r.prefHeight(-1) : n.prefHeight(-1))
                .max().orElse(48);
    }

    @Override
    protected double computePrefWidth(double height) {
        return cols * 100 + gapX * (cols - 1) + padding.getLeft() + padding.getRight();
    }

    @Override
    protected double computePrefHeight(double width) {
        List<Node> children = getManagedChildren();
        if (children.isEmpty()) {
            return 0;
        }
        int rowCount = rows > 0 ? rows : (int) Math.ceil((double) children.size() / cols);
        double cellH = computeMaxCellHeight(children);
        return rowCount * cellH + gapY * (rowCount - 1) + padding.getTop() + padding.getBottom();
    }
}
