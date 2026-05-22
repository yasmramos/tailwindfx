package io.github.yasmramos.tailwindfx.layout;

import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import io.github.yasmramos.tailwindfx.core.Preconditions;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.util.List;

import javafx.scene.Parent;

/**
 * TwFlexPane — a Flexbox-model layout container for JavaFX.
 *
 * <p>
 * Fills the gap between JavaFX's existing layout panes and the CSS flexbox
 * model. No existing JavaFX pane supports wrapping + alignment + gap
 * simultaneously. This pane implements all three.
 *
 * <h3>What this adds over existing panes</h3>
 * <ul>
 * <li>{@code HBox} — row only, no wrap, no align-items control.</li>
 * <li>{@code VBox} — column only, no wrap.</li>
 * <li>{@code FlowPane} — wraps but has no justify-content or align-items.</li>
 * <li>{@code TilePane} — forces equal size on all children.</li>
 * <li><b>TwFlexPane</b> — direction, wrap, justify, align, gap — all at
 * once.</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>
 * TwFlexPane flex = new TwFlexPane();
 * flex.setDirection(Direction.ROW);
 * flex.setWrap(true);
 * flex.setJustify(Justify.BETWEEN);
 * flex.setAlign(Align.CENTER);
 * flex.setGap(16);
 *
 * // Or fluent:
 * TwFlexPane flex = TwFlexPane.row()
 *     .wrap(true)
 *     .justify(Justify.BETWEEN)
 *     .align(Align.CENTER)
 *     .gap(16);
 *
 * // Responsive direction switch:
 * flex.setDirection(Direction.COL);   // on narrow screen
 * flex.setDirection(Direction.ROW);   // on wide screen
 * // TwFlexPane re-layouts immediately — no rebuild needed.
 *
 * // Per-child flex-grow:
 * TwFlexPane.setGrow(child, 1);  // like flex-1
 * TwFlexPane.setGrow(child, 0);  // like flex-none (default)
 * </pre>
 *
 * <h3>Integration with TailwindFX</h3>
 * <pre>
 * TwFlexPane flex = TwFlexPane.row().gap(16).justify(Justify.BETWEEN);
 * TailwindFX.apply(flex, "p-4", "bg-white", "rounded-lg");
 *
 * // Responsive with ResponsiveNode:
 * ResponsiveNode.on(flex)
 *     .base(/* direction set via Java )
 *     .install(scene);
 * // Then toggle direction:
 * bpm.onBreakpoint(BreakpointManager.BP.MD, () -> flex.setDirection(Direction.ROW));
 * </pre>
 *
 */
public class TwFlexPane extends Pane {

    // =========================================================================
    // Enums
    // =========================================================================
    /**
     * Main axis direction (analogous to {@code flex-direction}).
     */
    public enum Direction {
        ROW, COL
    }

    /**
     * Main axis distribution (analogous to {@code justify-content}). Controls
     * how children are spaced along the main axis.
     */
    public enum Justify {
        START, CENTER, END,
        BETWEEN, // equal space between items
        AROUND, // equal space around items
        EVENLY    // equal space between AND before first/after last
    }

    /**
     * Cross axis alignment (analogous to {@code align-items}). Controls how
     * children are aligned perpendicular to the main axis.
     */
    public enum Align {
        START, CENTER, END, STRETCH
    }

    /**
     * Multi-line alignment (analogous to {@code align-content}). Controls how
     * multiple lines are distributed in the cross-axis when there's extra space.
     * Only applies when wrap=true and there are multiple lines.
     */
    public enum AlignContent {
        START, CENTER, END,
        BETWEEN,  // equal space between lines
        AROUND,   // equal space around lines
        EVENLY,   // equal space between AND before first/after last line
        STRETCH   // lines stretch to fill available space
    }

    // =========================================================================
    // Properties
    // =========================================================================
    private Direction direction = Direction.ROW;
    private boolean wrap = false;
    private Justify justify = Justify.START;
    private Align align = Align.START;
    private AlignContent alignContent = AlignContent.START;
    private double gapMain = 0; // gap along main axis
    private double gapCross = 0; // gap along cross axis
    private Insets padding = Insets.EMPTY;

    /**
     * Node property key for grow factor (analogous to {@code flex-grow})
     */
    private static final String GROW_KEY = "tailwindfx.flex.grow";
    /**
     * Node property key for shrink factor (analogous to {@code flex-shrink})
     */
    private static final String SHRINK_KEY = "tailwindfx.flex.shrink";
    /**
     * Node property key for visual order (analogous to {@code order})
     */
    private static final String ORDER_KEY = "tailwindfx.flex.order";
    /**
     * Node property key for per-child align override (analogous to
     * {@code align-self})
     */
    private static final String SELF_KEY = "tailwindfx.flex.align-self";
    
    /**
     * Internal cache to avoid repeated map lookups and autoboxing during layout.
     * Uses WeakHashMap to prevent memory leaks.
     */
    private static final java.util.WeakHashMap<Node, FlexData> flexDataCache = new java.util.WeakHashMap<>();
    
    /**
     * Helper class to hold flex properties without autoboxing overhead.
     */
    private static class FlexData {
        double grow = 0.0;
        double shrink = 1.0;
        int order = 0;
        Align selfAlign = null;
        
        void update(Node node) {
            Object v = node.getProperties().get(GROW_KEY);
            grow = v instanceof Number n ? n.doubleValue() : 0.0;
            
            v = node.getProperties().get(SHRINK_KEY);
            shrink = v instanceof Number n ? n.doubleValue() : 1.0;
            
            v = node.getProperties().get(ORDER_KEY);
            order = v instanceof Number n ? n.intValue() : 0;
            
            v = node.getProperties().get(SELF_KEY);
            selfAlign = v instanceof Align a ? a : null;
        }
    }
    
    /**
     * Gets cached flex data for a node, updating from properties if needed.
     * This reduces autoboxing overhead in hot layout loops.
     */
    private static FlexData getFlexData(Node node) {
        return flexDataCache.computeIfAbsent(node, k -> new FlexData());
    }
    
    /**
     * Invalidates cached flex data for a node when its properties change.
     */
    private static void invalidateFlexData(Node node) {
        FlexData data = flexDataCache.get(node);
        if (data != null) {
            data.update(node);
        }
    }

    // =========================================================================
    // Static factory methods
    // =========================================================================
    /**
     * Creates a row-direction TwFlexPane.
     */
    public static TwFlexPane row() {
        return new TwFlexPane();
    }

    /**
     * Creates a column-direction TwFlexPane.
     */
    public static TwFlexPane col() {
        TwFlexPane f = new TwFlexPane();
        f.setDirection(Direction.COL);
        return f;
    }

    // =========================================================================
    // Fluent setters (return this for chaining)
    // =========================================================================
    /**
     * Sets the main axis direction. Triggers layout.
     */
    public TwFlexPane direction(Direction d) {
        setDirection(d);
        return this;
    }

    /**
     * Enables or disables wrapping. Triggers layout.
     */
    public TwFlexPane wrap(boolean w) {
        setWrap(w);
        return this;
    }

    /**
     * Sets justify-content distribution. Triggers layout.
     */
    public TwFlexPane justify(Justify j) {
        setJustify(j);
        return this;
    }

    /**
     * Sets align-items. Triggers layout.
     */
    public TwFlexPane align(Align a) {
        setAlign(a);
        return this;
    }

    /**
     * Sets align-content (multi-line alignment). Triggers layout.
     * Only applies when wrap=true and there are multiple lines.
     */
    public TwFlexPane alignContent(AlignContent ac) {
        setAlignContent(ac);
        return this;
    }

    /**
     * Sets uniform gap (both main and cross axes). Triggers layout.
     */
    public TwFlexPane gap(double px) {
        if (px < 0) {
            throw new IllegalArgumentException("TwFlexPane.gap: must be >= 0, got: " + px);
        }
        gapMain = px;
        gapCross = px;
        requestLayout();
        return this;
    }

    /**
     * Sets main-axis gap (row gap in row mode, column gap in column mode).
     */
    public TwFlexPane gapX(double px) {
        gapMain = px;
        requestLayout();
        return this;
    }

    /**
     * Sets cross-axis gap.
     */
    public TwFlexPane gapY(double px) {
        gapCross = px;
        requestLayout();
        return this;
    }

    /**
     * Sets padding around the flex container.
     */
    public TwFlexPane padding(Insets p) {
        Preconditions.requireNonNull(p, "TwFlexPane.padding", "insets");
        padding = p;
        setPadding(p);
        requestLayout();
        return this;
    }

    /**
     * Sets uniform padding.
     */
    public TwFlexPane padding(double px) {
        return padding(new Insets(px));
    }

    // =========================================================================
    // Standard setters
    // =========================================================================
    public void setDirection(Direction d) {
        Preconditions.requireNonNull(d, "TwFlexPane.setDirection", "direction");
        direction = d;
        requestLayout();
    }

    /**
     * Changes the flex direction with an animated fade transition.
     *
     * <p>
     * Fades all children out, switches direction, then fades back in. Useful
     * for responsive breakpoint switches where an abrupt re-layout looks
     * jarring.
     *
     * <pre>
     * // Responsive with smooth direction change:
     * bpm.onBreakpoint(BreakpointManager.BP.MD, () ->
     *     container.setDirectionAnimated(Direction.COL, 150));
     * </pre>
     *
     * @param d the new direction
     * @param durationMs fade duration in milliseconds (each way); use 0 for
     * instant
     */
    public void setDirectionAnimated(Direction d, int durationMs) {
        Preconditions.requireNonNull(d, "TwFlexPane.setDirectionAnimated", "direction");
        if (durationMs <= 0 || d == direction) {
            setDirection(d);
            return;
        }
        javafx.animation.Timeline fadeOut = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(durationMs),
                        new javafx.animation.KeyValue(opacityProperty(), 0.0,
                                javafx.animation.Interpolator.EASE_IN)));
        fadeOut.setOnFinished(e -> {
            direction = d;
            requestLayout();
            javafx.animation.Timeline fadeIn = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(durationMs),
                            new javafx.animation.KeyValue(opacityProperty(), 1.0,
                                    javafx.animation.Interpolator.EASE_OUT)));
            fadeIn.play();
        });
        fadeOut.play();
    }

    public void setWrap(boolean w) {
        wrap = w;
        requestLayout();
    }

    public void setJustify(Justify j) {
        Preconditions.requireNonNull(j, "TwFlexPane.setJustify", "justify");
        justify = j;
        requestLayout();
    }

    public void setAlign(Align a) {
        Preconditions.requireNonNull(a, "TwFlexPane.setAlign", "align");
        align = a;
        requestLayout();
    }

    public void setAlignContent(AlignContent ac) {
        Preconditions.requireNonNull(ac, "TwFlexPane.setAlignContent", "alignContent");
        alignContent = ac;
        requestLayout();
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isWrap() {
        return wrap;
    }

    public Justify getJustify() {
        return justify;
    }

    public Align getAlign() {
        return align;
    }

    public AlignContent getAlignContent() {
        return alignContent;
    }

    public double getGap() {
        return gapMain;
    }

    public double getGapX() {
        return gapMain;
    }

    public double getGapY() {
        return gapCross;
    }

    // =========================================================================
    // Per-child flex-grow
    // =========================================================================
    /**
     * Sets the grow factor for a child node (analogous to {@code flex-grow}). A
     * grow factor of 1 means the child takes available space; 0 (default) means
     * it does not grow.
     *
     * @param node the child node
     * @param factor grow factor (>= 0)
     */
    /**
     * Sets the grow factor for a child node (analogous to {@code flex-grow}).
     *
     * <p>
     * May be called before or after adding the node to a {@link TwFlexPane}. If
     * called before, a listener is installed to trigger layout as soon as the
     * node is added to its parent.
     *
     * @param node the child node
     * @param factor grow factor (>= 0; default 0)
     */
    public static void setGrow(Node node, double factor) {
        Preconditions.requireNonNull(node, "TwFlexPane.setGrow", "node");
        if (factor < 0) {
            throw new IllegalArgumentException(
                    "TwFlexPane.setGrow: factor must be >= 0, got: " + factor);
        }
        node.getProperties().put(GROW_KEY, factor);
        invalidateFlexData(node); // Update cache
        if (node.getParent() instanceof TwFlexPane fp) {
            fp.requestLayout();
        } else {
            ensureLayoutOnParent(node);
        }
    }

    /**
     * Returns the grow factor of a child node (default 0).
     */
    public static double getGrow(Node node) {
        Object v = node.getProperties().get(GROW_KEY);
        return v instanceof Number n ? n.doubleValue() : 0.0;
    }

    /**
     * Installs a one-shot {@code parentProperty} listener that calls
     * {@code requestLayout()} on the parent when the node is first added to a
     * {@link TwFlexPane}. This ensures flex properties applied before a node is
     * added to its parent take effect immediately.
     *
     * <p>
     * The listener removes itself after the first TwFlexPane parent is
     * detected, so it does not accumulate.
     */
    private static void ensureLayoutOnParent(Node node) {
        final Object KEY = "tailwindfx.flex.parent-listener";
        if (node.getProperties().containsKey(KEY)) {
            return;
        }

        javafx.beans.value.ChangeListener<Parent> listener = new javafx.beans.value.ChangeListener<Parent>() {
            @Override
            public void changed(javafx.beans.value.ObservableValue<? extends Parent> obs, Parent oldP, Parent newP) {
                if (newP instanceof TwFlexPane fp) {
                    fp.requestLayout();
                    // One-shot cleanup
                    node.parentProperty().removeListener(this);
                    node.getProperties().remove(KEY);
                }
            }
        };

        node.parentProperty().addListener(listener);
        node.getProperties().put(KEY, Boolean.TRUE);
    }

    /**
     * Sets the shrink factor for a child node (analogous to
     * {@code flex-shrink}).
     *
     * <p>
     * A shrink factor > 0 means the child will shrink proportionally when there
     * is not enough space. The default is 1 (shrinks if needed). Set to 0 to
     * prevent shrinking (analogous to {@code flex-shrink: 0}).
     *
     * @param node the child node
     * @param factor shrink factor (>= 0; default 1)
     */
    public static void setShrink(Node node, double factor) {
        Preconditions.requireNonNull(node, "TwFlexPane.setShrink", "node");
        if (factor < 0) {
            throw new IllegalArgumentException(
                    "TwFlexPane.setShrink: factor must be >= 0, got: " + factor);
        }
        node.getProperties().put(SHRINK_KEY, factor);
        invalidateFlexData(node); // Update cache
        if (node.getParent() instanceof TwFlexPane fp) {
            fp.requestLayout();
        } else {
            ensureLayoutOnParent(node);
        }
    }

    /**
     * Returns the shrink factor of a child node (default 1).
     */
    public static double getShrink(Node node) {
        Object v = node.getProperties().get(SHRINK_KEY);
        return v instanceof Number n ? n.doubleValue() : 1.0;
    }

    /**
     * Sets the visual display order for a child node (analogous to CSS
     * {@code order}).
     *
     * <p>
     * Children are sorted by order value before layout. Lower values appear
     * first. The default order is 0. Negative values are allowed and place the
     * child before all order-0 items.
     *
     * <pre>
     * TwFlexPane.setOrder(footer, 99);   // always last
     * TwFlexPane.setOrder(header, -1);   // always first
     * </pre>
     *
     * @param node the child node
     * @param order display order (default 0)
     */
    public static void setOrder(Node node, int order) {
        Preconditions.requireNonNull(node, "TwFlexPane.setOrder", "node");
        node.getProperties().put(ORDER_KEY, order);
        invalidateFlexData(node); // Update cache
        if (node.getParent() instanceof TwFlexPane fp) {
            fp.requestLayout();
        } else {
            ensureLayoutOnParent(node);
        }
    }

    /**
     * Returns the display order of a child node (default 0).
     */
    public static int getOrder(Node node) {
        Object v = node.getProperties().get(ORDER_KEY);
        return v instanceof Integer i ? i : 0;
    }

    /**
     * Sets a per-child alignment override on the cross axis (analogous to
     * {@code align-self}).
     *
     * <p>
     * Overrides the container's {@link #setAlign(Align)} for this specific
     * child. Set to {@code null} to use the container's default.
     *
     * <pre>
     * flex.setAlign(Align.CENTER);            // default for all children
     * TwFlexPane.setAlignSelf(specialBtn, Align.END);  // this one goes to end
     * </pre>
     *
     * @param node the child node
     * @param align cross-axis alignment for this child, or {@code null} to
     * inherit
     */
    public static void setAlignSelf(Node node, Align align) {
        Preconditions.requireNonNull(node, "TwFlexPane.setAlignSelf", "node");
        if (align == null) {
            node.getProperties().remove(SELF_KEY);
        } else {
            node.getProperties().put(SELF_KEY, align);
        }
        invalidateFlexData(node); // Update cache
        if (node.getParent() instanceof TwFlexPane fp) {
            fp.requestLayout();
        } else {
            ensureLayoutOnParent(node);
        }
    }

    /**
     * Returns the per-child align-self, or {@code null} if inheriting from the
     * container.
     */
    public static Align getAlignSelf(Node node) {
        Object v = node.getProperties().get(SELF_KEY);
        return v instanceof Align a ? a : null;
    }

    /**
     * Node property key for flex-basis (overrides pref size before grow/shrink)
     */
    private static final String BASIS_KEY = "tailwindfx.flex.basis";

    /**
     * Sets the initial main-axis size for a child before flex-grow and
     * flex-shrink are applied (analogous to CSS {@code flex-basis}).
     *
     * <p>
     * In row direction, this overrides the child's preferred <em>width</em>. In
     * column direction, this overrides the child's preferred <em>height</em>.
     *
     * <p>
     * Special values:
     * <ul>
     * <li>{@code -1} — use the child's natural preferred size (default, same as
     * {@code flex-basis: auto})</li>
     * <li>{@code 0} — start from zero before distribution
     * ({@code flex-basis: 0})</li>
     * <li>{@code > 0} — fixed base size in px before grow/shrink</li>
     * </ul>
     *
     * <pre>
     * // flex: 1 1 0 (equal distribution from zero base)
     * TwFlexPane.setBasis(child, 0);
     * TwFlexPane.setGrow(child, 1);
     *
     * // flex: 0 0 200px (fixed 200px, no grow, no shrink)
     * TwFlexPane.setBasis(child, 200);
     * TwFlexPane.setShrink(child, 0);
     * </pre>
     *
     * @param node the child node
     * @param basis base size in px, or {@code -1} for auto
     */
    public static void setBasis(Node node, double basis) {
        Preconditions.requireNonNull(node, "TwFlexPane.setBasis", "node");
        if (basis < -1) {
            throw new IllegalArgumentException(
                    "TwFlexPane.setBasis: basis must be >= -1 (use -1 for auto), got: " + basis);
        }
        node.getProperties().put(BASIS_KEY, basis);
        if (node.getParent() instanceof TwFlexPane fp) {
            fp.requestLayout();
        } else {
            ensureLayoutOnParent(node);
        }
    }

    /**
     * Returns the flex-basis for a child, or {@code -1} if using natural pref
     * size.
     *
     * @param node the child node
     * @return the flex-basis in px, or {@code -1} for auto
     */
    public static double getBasis(Node node) {
        Object v = node.getProperties().get(BASIS_KEY);
        return v instanceof Number n ? n.doubleValue() : -1.0;
    }

    // =========================================================================
    // Layout engine
    // =========================================================================
    @Override
    protected void layoutChildren() {
        long t0 = System.nanoTime();
        // Sort by order property (analogous to CSS order) before layout
        List<Node> children = getManagedChildren().stream()
                .sorted((a, b) -> Integer.compare(getOrder(a), getOrder(b)))
                .toList();
        if (children.isEmpty()) {
            return;
        }

        double containerW = getWidth() - padding.getLeft() - padding.getRight();
        double containerH = getHeight() - padding.getTop() - padding.getBottom();
        double ox = padding.getLeft();
        double oy = padding.getTop();

        if (direction == Direction.ROW) {
            layoutRow(children, containerW, containerH, ox, oy);
        } else {
            layoutCol(children, containerW, containerH, ox, oy);
        }
        TailwindFXMetrics.instance().recordLayoutPass(System.nanoTime() - t0);
    }

    private void layoutRow(List<Node> children, double w, double h, double ox, double oy) {
        if (wrap) {
            layoutRowWrap(children, w, h, ox, oy);
        } else {
            layoutRowNoWrap(children, w, h, ox, oy);
        }
    }

    private void layoutRowNoWrap(List<Node> children, double w, double h, double ox, double oy) {
        int count = children.size();
        if (count == 0) return;

        // CRITICAL OPTIMIZATION: Pre-calculate ALL sums outside the loop for real O(N)
        double[] prefs = new double[count];
        double totalPrefW = 0;
        double totalGrowWeighted = 0;
        double totalShrinkWeighted = 0;

        for (int i = 0; i < count; i++) {
            Node n = children.get(i);
            double pref = prefW(n);
            prefs[i] = pref;
            totalPrefW += pref;
            
            double grow = getGrow(n);
            double shrink = getShrink(n);
            
            if (grow > 0) totalGrowWeighted += grow * pref;
            if (shrink > 0) totalShrinkWeighted += shrink * pref;
        }
        
        totalPrefW += gapMain * (count - 1);
        double overflow = totalPrefW - w;  // positive = too wide, need to shrink
        double extra = Math.max(0, w - (totalPrefW - gapMain * (count - 1)));  // free space without gaps

        double[] widths = new double[count];
        if (extra > 0 && totalGrowWeighted > 0) {
            // Grow: distribute free space proportionally to (grow × pref)
            for (int i = 0; i < count; i++) {
                double grow = getGrow(children.get(i));
                if (grow > 0) {
                    double factor = (grow * prefs[i]) / totalGrowWeighted;
                    widths[i] = prefs[i] + extra * factor;
                } else {
                    widths[i] = prefs[i];
                }
            }
        } else if (overflow > 0 && totalShrinkWeighted > 0) {
            // Shrink: reduce proportionally to (shrink × pref) - CORREGIDO O(N)
            for (int i = 0; i < count; i++) {
                double shrink = getShrink(children.get(i));
                if (shrink > 0) {
                    double factor = (shrink * prefs[i]) / totalShrinkWeighted;
                    widths[i] = Math.max(0, prefs[i] - overflow * factor);
                } else {
                    widths[i] = prefs[i];
                }
            }
        } else {
            // No grow/shrink needed or possible
            for (int i = 0; i < count; i++) {
                widths[i] = prefs[i];
            }
        }

        double[] xs = justifyPositions(widths, gapMain, w, justify);

        for (int i = 0; i < count; i++) {
            Node c = children.get(i);
            double ch = alignedH(c, h, align);
            double cy = alignedY(c, h, ch, align, oy);
            c.resizeRelocate(ox + xs[i], cy, widths[i], ch);
        }
    }

    private void layoutRowWrap(List<Node> children, double w, double h, double ox, double oy) {
        // CRITICAL FIX: Implement grow/shrink WITHIN each row (missing in original version)
        // Group children into rows
        java.util.List<java.util.List<Node>> rows = new java.util.ArrayList<>();
        java.util.List<Node> currentRow = new java.util.ArrayList<>();
        double currentWidth = 0;

        for (Node c : children) {
            double cw = prefW(c);
            if (!currentRow.isEmpty() && currentWidth + gapMain + cw > w) {
                rows.add(currentRow);
                currentRow = new java.util.ArrayList<>();
                currentWidth = 0;
            }
            currentRow.add(c);
            currentWidth += (currentRow.size() > 1 ? gapMain : 0) + cw;
        }
        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
        }

        // Calculate row heights
        double[] rowHeights = new double[rows.size()];
        double totalRowsHeight = 0;
        for (int r = 0; r < rows.size(); r++) {
            java.util.List<Node> row = rows.get(r);
            rowHeights[r] = row.stream().mapToDouble(n -> prefH(n)).max().orElse(0);
            totalRowsHeight += rowHeights[r];
        }

        // Apply align-content to distribute extra vertical space between rows
        double[] rowYPositions = alignContentPositions(rowHeights, gapCross, h, alignContent);

        // Lay out each row WITH grow/shrink support
        for (int r = 0; r < rows.size(); r++) {
            java.util.List<Node> row = rows.get(r);
            int count = row.size();
            double rowH = rowHeights[r];
            double curY = oy + rowYPositions[r];
            
            // OPTIMIZATION: Pre-calculate sums for real O(N) within each row
            double[] prefs = new double[count];
            double totalPref = 0;
            double totalGrowWeighted = 0;
            double totalShrinkWeighted = 0;
            
            for(int i = 0; i < count; i++) {
                Node n = row.get(i);
                double p = prefW(n);
                prefs[i] = p;
                totalPref += p;
                
                double g = getGrow(n);
                double s = getShrink(n);
                if(g > 0) totalGrowWeighted += g * p;
                if(s > 0) totalShrinkWeighted += s * p;
            }
            
            double lineAvailable = w - (gapMain * (count - 1));
            double overflow = totalPref - lineAvailable;
            double[] finalWidths = new double[count];
            
            if (overflow <= 0) {
                // Grow within the row
                double extra = -overflow;
                if (totalGrowWeighted > 0) {
                    for(int i = 0; i < count; i++) {
                        double g = getGrow(row.get(i));
                        if(g > 0) {
                            double factor = (g * prefs[i]) / totalGrowWeighted;
                            finalWidths[i] = prefs[i] + extra * factor;
                        } else {
                            finalWidths[i] = prefs[i];
                        }
                    }
                } else {
                    System.arraycopy(prefs, 0, finalWidths, 0, count);
                }
            } else {
                // Shrink within the row - FIX ADDED
                if (totalShrinkWeighted > 0) {
                    for(int i = 0; i < count; i++) {
                        double s = getShrink(row.get(i));
                        if(s > 0) {
                            double factor = (s * prefs[i]) / totalShrinkWeighted;
                            finalWidths[i] = Math.max(0, prefs[i] - overflow * factor);
                        } else {
                            finalWidths[i] = prefs[i];
                        }
                    }
                } else {
                    System.arraycopy(prefs, 0, finalWidths, 0, count);
                }
            }
            
            double[] xs = justifyPositions(finalWidths, gapMain, w, justify);
            for (int i = 0; i < count; i++) {
                Node c = row.get(i);
                double ch = alignedH(c, rowH, align);
                double cy = alignedY(c, rowH, ch, align, curY);
                c.resizeRelocate(ox + xs[i], cy, finalWidths[i], ch);
            }
        }
    }

    private void layoutCol(List<Node> children, double w, double h, double ox, double oy) {
        int count = children.size();
        if (count == 0) return;

        // CRITICAL FIX: Implement shrink in columns (missing in original version)
        double[] prefs = new double[count];
        double totalPrefH = 0;
        double totalGrowWeighted = 0;
        double totalShrinkWeighted = 0;

        for (int i = 0; i < count; i++) {
            Node n = children.get(i);
            double pref = prefH(n);
            prefs[i] = pref;
            totalPrefH += pref;
            
            double grow = getGrow(n);
            double shrink = getShrink(n);
            
            if (grow > 0) totalGrowWeighted += grow * pref;
            if (shrink > 0) totalShrinkWeighted += shrink * pref;
        }
        
        totalPrefH += gapCross * (count - 1);
        double overflow = totalPrefH - h;  // positive = too tall, need to shrink
        double extra = Math.max(0, h - (totalPrefH - gapCross * (count - 1)));  // free space

        double[] heights = new double[count];
        if (extra > 0 && totalGrowWeighted > 0) {
            // Grow: distribute free space proportionally to (grow × pref)
            for (int i = 0; i < count; i++) {
                double grow = getGrow(children.get(i));
                if (grow > 0) {
                    double factor = (grow * prefs[i]) / totalGrowWeighted;
                    heights[i] = prefs[i] + extra * factor;
                } else {
                    heights[i] = prefs[i];
                }
            }
        } else if (overflow > 0 && totalShrinkWeighted > 0) {
            // Shrink: reduce proportionally to (shrink × pref) - FIX ADDED
            for (int i = 0; i < count; i++) {
                double shrink = getShrink(children.get(i));
                if (shrink > 0) {
                    double factor = (shrink * prefs[i]) / totalShrinkWeighted;
                    heights[i] = Math.max(0, prefs[i] - overflow * factor);
                } else {
                    heights[i] = prefs[i];
                }
            }
        } else {
            // No grow/shrink needed or possible
            for (int i = 0; i < count; i++) {
                heights[i] = prefs[i];
            }
        }

        double[] ys = justifyPositions(heights, gapCross, h, justify);

        for (int i = 0; i < count; i++) {
            Node c = children.get(i);
            double cw = alignedW(c, w, align);
            double cx = alignedX(c, w, cw, align, ox);
            c.resizeRelocate(cx, oy + ys[i], cw, heights[i]);
        }
    }

    // =========================================================================
    // Layout math helpers
    // =========================================================================
    /**
     * Computes starting positions for items along one axis given
     * justify-content.
     *
     * @param sizes sizes of each item along the main axis
     * @param gap gap between items
     * @param total total available space
     * @param j justify-content value
     * @return array of starting positions (same length as sizes)
     */
    static double[] justifyPositions(double[] sizes, double gap, double total, Justify j) {
        double usedSpace = 0;
        for (double s : sizes) {
            usedSpace += s;
        }
        double gapTotal = gap * (sizes.length - 1);
        double free = total - usedSpace - gapTotal;
        double[] pos = new double[sizes.length];

        switch (j) {
            case START -> {
                double x = 0;
                for (int i = 0; i < sizes.length; i++) {
                    pos[i] = x;
                    x += sizes[i] + gap;
                }
            }
            case CENTER -> {
                double x = Math.max(0, free / 2);
                for (int i = 0; i < sizes.length; i++) {
                    pos[i] = x;
                    x += sizes[i] + gap;
                }
            }
            case END -> {
                double x = Math.max(0, free);
                for (int i = 0; i < sizes.length; i++) {
                    pos[i] = x;
                    x += sizes[i] + gap;
                }
            }
            case BETWEEN -> {
                if (sizes.length == 1) {
                    pos[0] = 0;
                    break;
                }
                double spaceBetween = sizes.length > 1
                        ? Math.max(gap, (total - usedSpace) / (sizes.length - 1)) : gap;
                double x = 0;
                for (int i = 0; i < sizes.length; i++) {
                    pos[i] = x;
                    x += sizes[i] + spaceBetween;
                }
            }
            case AROUND -> {
                double unit = free / sizes.length;
                double x = unit / 2;
                for (int i = 0; i < sizes.length; i++) {
                    pos[i] = x;
                    x += sizes[i] + unit;
                }
            }
            case EVENLY -> {
                double unit = free / (sizes.length + 1);
                double x = unit;
                for (int i = 0; i < sizes.length; i++) {
                    pos[i] = x;
                    x += sizes[i] + unit;
                }
            }
        }
        return pos;
    }

    /**
     * Computes starting Y positions for rows/lines when wrap=true based on align-content.
     * Similar to justifyPositions but for the cross-axis distribution of multiple lines.
     *
     * @param rowHeights heights of each row/line
     * @param gap gap between rows (gapCross)
     * @param total total available height
     * @param ac align-content value
     * @return array of starting Y positions (same length as rowHeights)
     */
    static double[] alignContentPositions(double[] rowHeights, double gap, double total, AlignContent ac) {
        double usedSpace = 0;
        for (double h : rowHeights) {
            usedSpace += h;
        }
        double gapTotal = gap * (rowHeights.length - 1);
        double free = total - usedSpace - gapTotal;
        double[] pos = new double[rowHeights.length];

        switch (ac) {
            case START -> {
                double y = 0;
                for (int i = 0; i < rowHeights.length; i++) {
                    pos[i] = y;
                    y += rowHeights[i] + gap;
                }
            }
            case CENTER -> {
                double y = Math.max(0, free / 2);
                for (int i = 0; i < rowHeights.length; i++) {
                    pos[i] = y;
                    y += rowHeights[i] + gap;
                }
            }
            case END -> {
                double y = Math.max(0, free);
                for (int i = 0; i < rowHeights.length; i++) {
                    pos[i] = y;
                    y += rowHeights[i] + gap;
                }
            }
            case BETWEEN -> {
                if (rowHeights.length == 1) {
                    pos[0] = 0;
                    break;
                }
                double spaceBetween = rowHeights.length > 1
                        ? Math.max(gap, (total - usedSpace) / (rowHeights.length - 1)) : gap;
                double y = 0;
                for (int i = 0; i < rowHeights.length; i++) {
                    pos[i] = y;
                    y += rowHeights[i] + spaceBetween;
                }
            }
            case AROUND -> {
                double unit = free / rowHeights.length;
                double y = unit / 2;
                for (int i = 0; i < rowHeights.length; i++) {
                    pos[i] = y;
                    y += rowHeights[i] + unit;
                }
            }
            case EVENLY -> {
                double unit = free / (rowHeights.length + 1);
                double y = unit;
                for (int i = 0; i < rowHeights.length; i++) {
                    pos[i] = y;
                    y += rowHeights[i] + unit;
                }
            }
            case STRETCH -> {
                // Distribute extra space equally to each row
                double extraPerRow = rowHeights.length > 0 ? free / rowHeights.length : 0;
                double y = 0;
                for (int i = 0; i < rowHeights.length; i++) {
                    pos[i] = y;
                    y += rowHeights[i] + extraPerRow + gap;
                }
            }
        }
        return pos;
    }

    /**
     * Returns the main-axis base size for a child, respecting flex-basis. Used
     * in row-direction layouts.
     */
    private double prefW(Node n) {
        double basis = getBasis(n);
        if (basis >= 0) {
            return basis;
        }
        return n instanceof Region r ? r.prefWidth(-1) : n.prefWidth(-1);
    }

    /**
     * Returns the cross-axis base size for a child, respecting flex-basis. Used
     * in column-direction layouts.
     */
    private double prefH(Node n) {
        double basis = getBasis(n);
        if (basis >= 0 && direction == Direction.COL) {
            return basis;
        }
        return n instanceof Region r ? r.prefHeight(-1) : n.prefHeight(-1);
    }

    /**
     * Returns the natural preferred width (ignoring flex-basis). Used for
     * cross-axis size in column layouts.
     */
    private double naturalPrefW(Node n) {
        return n instanceof Region r ? r.prefWidth(-1) : n.prefWidth(-1);
    }

    /**
     * Returns the natural preferred height (ignoring flex-basis). Used for
     * cross-axis size in row layouts.
     */
    private double naturalPrefH(Node n) {
        return n instanceof Region r ? r.prefHeight(-1) : n.prefHeight(-1);
    }

    private double alignedW(Node n, double axisW, Align a) {
        Align eff = getAlignSelf(n);
        if (eff != null) {
            a = eff;
        }
        return a == Align.STRETCH ? axisW : naturalPrefW(n);
    }

    /**
     * Returns the effective align for a child, respecting align-self override.
     */
    private Align effectiveAlign(Node n) {
        Align self = getAlignSelf(n);
        return self != null ? self : align;
    }

    private double alignedH(Node n, double axisH, Align a) {
        Align eff = getAlignSelf(n);
        if (eff != null) {
            a = eff;
        }
        return a == Align.STRETCH ? axisH : naturalPrefH(n);
    }

    private double alignedX(Node n, double axisW, double childW, Align a, double origin) {
        Align eff = getAlignSelf(n);
        if (eff != null) {
            a = eff;
        }
        return switch (a) {
            case START, STRETCH ->
                origin;
            case CENTER ->
                origin + (axisW - childW) / 2;
            case END ->
                origin + (axisW - childW);
        };
    }

    private double alignedY(Node n, double axisH, double childH, Align a, double origin) {
        Align eff = getAlignSelf(n);
        if (eff != null) {
            a = eff;
        }
        return switch (a) {
            case START, STRETCH ->
                origin;
            case CENTER ->
                origin + (axisH - childH) / 2;
            case END ->
                origin + (axisH - childH);
        };
    }

    @Override
    protected double computePrefWidth(double height) {
        if (direction == Direction.ROW && !wrap) {
            return getManagedChildren().stream().mapToDouble(this::prefW).sum()
                    + gapMain * Math.max(0, getManagedChildren().size() - 1)
                    + padding.getLeft() + padding.getRight();
        }
        return getManagedChildren().stream().mapToDouble(this::prefW).max().orElse(0)
                + padding.getLeft() + padding.getRight();
    }

    @Override
    protected double computePrefHeight(double width) {
        if (direction == Direction.COL) {
            return getManagedChildren().stream().mapToDouble(this::prefH).sum()
                    + gapCross * Math.max(0, getManagedChildren().size() - 1)
                    + padding.getTop() + padding.getBottom();
        }
        // ROW + wrap: sum the heights of all rows
        if (wrap && width > 0) {
            double containerW = width - padding.getLeft() - padding.getRight();
            double totalH = 0, rowH = 0, rowW = 0;
            for (Node child : getManagedChildren()) {
                double cw = prefW(child);
                if (rowW > 0 && rowW + gapMain + cw > containerW) {
                    totalH += rowH + gapCross;
                    rowH = 0;
                    rowW = 0;
                }
                rowW += (rowW > 0 ? gapMain : 0) + cw;
                rowH = Math.max(rowH, naturalPrefH(child));
            }
            totalH += rowH; // last row
            return totalH + padding.getTop() + padding.getBottom();
        }
        // ROW, no wrap: height = tallest child
        return getManagedChildren().stream().mapToDouble(this::naturalPrefH).max().orElse(0)
                + padding.getTop() + padding.getBottom();
    }
}
