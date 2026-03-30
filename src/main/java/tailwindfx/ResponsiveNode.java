package tailwindfx;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;

import java.util.Arrays;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * ResponsiveNode — per-node responsive utility rules driven by Scene width.
 *
 * <p>Attaches CSS utility classes to a node based on the scene width,
 * replacing them automatically as the window resizes. Uses
 * {@link UtilityConflictResolver} internally — transitions are deterministic.
 *
 * <h3>Usage</h3>
 * <pre>
 * // Declare responsive rules, then attach:
 * ResponsiveNode.on(sidebar)
 *     .base("w-64", "flex-col")        // always (default)
 *     .sm("w-full", "flex-row")        // scene width >= 640px
 *     .md("w-48", "flex-col")          // scene width >= 768px
 *     .lg("w-64", "flex-col")          // scene width >= 1024px
 *     .install(scene);
 *
 * // Custom breakpoints (px):
 * ResponsiveNode.on(card)
 *     .at(0,   "w-full")
 *     .at(600, "w-1/2")
 *     .at(900, "w-1/3")
 *     .install(scene);
 *
 * // React to breakpoint changes:
 * ResponsiveNode.on(content)
 *     .md("p-8")
 *     .onBreakpoint(bp -> System.out.println("Now: " + bp))
 *     .install(scene);
 *
 * // Detach when done (e.g., node removed from scene):
 * rn.detach();
 * </pre>
 *
 * <h3>How it works</h3>
 * <ol>
 *   <li>Listens to {@code scene.widthProperty()} (not Stage — works in all contexts).</li>
 *   <li>On resize, finds the highest breakpoint whose minWidth ≤ scene width.</li>
 *   <li>Removes the previous breakpoint's classes, applies the new ones via
 *       {@link UtilityConflictResolver#applyAll}.</li>
 *   <li>Base classes (registered with {@link #base}) are always present and
 *       are applied before breakpoint-specific classes.</li>
 * </ol>
 */
public final class ResponsiveNode {

    // =========================================================================
    // Factory
    // =========================================================================

    /**
     * Creates a responsive rule builder for the given node.
     *
     * @param node the node to apply responsive utilities to
     * @return a new {@link Builder} for this node
     */
    public static Builder on(Node node) {
        Preconditions.requireNode(node, "ResponsiveNode.on");
        return new Builder(node);
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static final class Builder {

        private final Node node;
        // Sorted map: minWidth → classes to apply at that breakpoint
        private final TreeMap<Integer, String[]> rules = new TreeMap<>();
        // Classes always present regardless of breakpoint
        private String[] baseClasses = new String[0];
        // Optional callback when breakpoint changes
        private java.util.function.Consumer<Integer> breakpointCallback;

        private Builder(Node node) { this.node = node; }

        // --- Tailwind standard breakpoints ---

        /**
         * Classes to apply always (base state, width 0+).
         * Applied first; breakpoint classes are layered on top.
         */
        public Builder base(String... classes)  { baseClasses = classes; return this; }

        /** Classes to apply at scene width >= 640px (Tailwind {@code sm:}). */
        public Builder sm(String... classes)    { return at(640, classes); }

        /** Classes to apply at scene width >= 768px (Tailwind {@code md:}). */
        public Builder md(String... classes)    { return at(768, classes); }

        /** Classes to apply at scene width >= 1024px (Tailwind {@code lg:}). */
        public Builder lg(String... classes)    { return at(1024, classes); }

        /** Classes to apply at scene width >= 1280px (Tailwind {@code xl:}). */
        public Builder xl(String... classes)    { return at(1280, classes); }

        /** Classes to apply at scene width >= 1536px (Tailwind {@code 2xl:}). */
        public Builder xxl(String... classes)   { return at(1536, classes); }

        /**
         * Classes to apply at a custom minimum width (in px).
         * Multiple calls with the same width replace each other.
         *
         * @param minWidthPx minimum scene width to activate this rule
         * @param classes    utility classes to apply
         */
        public Builder at(int minWidthPx, String... classes) {
            if (minWidthPx < 0) throw new IllegalArgumentException(
                "ResponsiveNode.at: minWidthPx must be >= 0, got: " + minWidthPx);
            rules.put(minWidthPx, classes);
            return this;
        }

        /**
         * Registers a callback invoked whenever the active breakpoint changes.
         * Receives the new minimum width (in px) of the active rule.
         *
         * @param callback consumer of the active minWidth, or {@code null} to clear
         */
        public Builder onBreakpoint(java.util.function.Consumer<Integer> callback) {
            this.breakpointCallback = callback;
            return this;
        }

        /**
         * Attaches responsive listeners to the given Scene and returns a
         * {@link ResponsiveNode} handle for later detachment.
         *
         * <p>Applies the correct rule immediately for the current scene width.
         *
         * @param scene the Scene to observe
         * @return a live {@link ResponsiveNode} handle
         * @throws IllegalArgumentException if scene is null
         */
        public ResponsiveNode install(Scene scene) {
            Preconditions.requireNonNull(scene, "ResponsiveNode.install", "scene");
            return new ResponsiveNode(node, rules, baseClasses, breakpointCallback, scene);
        }
    }

    // =========================================================================
    // Live instance
    // =========================================================================

    private final Node          node;
    private final TreeMap<Integer, String[]> rules;
    private final String[]      baseClasses;
    private final java.util.function.Consumer<Integer> breakpointCallback;
    private final Scene         scene;
    private final ChangeListener<Number> widthListener;
    private int                 activeBp = -1;

    private ResponsiveNode(Node node, TreeMap<Integer, String[]> rules,
                           String[] baseClasses,
                           java.util.function.Consumer<Integer> callback,
                           Scene scene) {
        this.node              = node;
        this.rules             = new TreeMap<>(rules);
        this.baseClasses       = baseClasses;
        this.breakpointCallback = callback;
        this.scene             = scene;

        // Apply immediately at current width
        applyForWidth(scene.getWidth());

        // Listen for subsequent changes
        this.widthListener = (obs, ov, nv) -> applyForWidth(nv.doubleValue());
        scene.widthProperty().addListener(widthListener);

        // Auto-detach when the node is removed from any scene (prevents listener leak)
        installAutoDetach(node);
    }

    /**
     * Detaches the responsive width listener from the scene.
     * The listener is also removed automatically when the node leaves the scene graph,
     * so explicit calls to {@code detach()} are only needed for early cleanup.
     */
    public void detach() {
        scene.widthProperty().removeListener(widthListener);
    }

    /**
     * Installs a scene listener that auto-detaches the width listener when
     * the node is removed from the scene graph.
     *
     * <p>This prevents the responsive node from keeping the scene alive through
     * the width listener after the node has been removed from the UI.
     */
    private void installAutoDetach(Node node) {
        final String KEY = "tailwindfx.responsive.installed";
        if (node.getProperties().containsKey(KEY)) return;
        node.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                // Node removed from scene — detach our width listener
                detach();
                Preconditions.LOG.fine(
                    "ResponsiveNode: auto-detached on scene removal — "
                    + node.getClass().getSimpleName());
            }
        });
        node.getProperties().put(KEY, Boolean.TRUE);
    }

    /**
     * Returns the currently active minimum width breakpoint, or {@code -1} if only
     * base classes are active.
     */
    public int activeBreakpoint() { return activeBp; }

    /**
     * Forces re-evaluation of responsive rules at the current scene width.
     * Useful after programmatic changes to the node's class list.
     */
    public void refresh() { applyForWidth(scene.getWidth()); }

    // =========================================================================
    // Core logic
    // =========================================================================

    private void applyForWidth(double width) {
        // Find the highest breakpoint whose minWidth <= current width
        Integer bp = null;
        for (Integer minW : rules.descendingKeySet()) {
            if (width >= minW) { bp = minW; break; }
        }

        int newBp = bp != null ? bp : -1;
        if (newBp == activeBp) return; // no change — skip
        activeBp = newBp;

        // Remove ALL breakpoint-specific classes from all rules
        for (String[] classes : rules.values()) {
            node.getStyleClass().removeAll(Arrays.asList(classes));
        }

        // Re-apply base classes
        if (baseClasses.length > 0) {
            UtilityConflictResolver.applyAll(node, baseClasses);
        }

        // Apply active breakpoint classes on top
        if (bp != null && rules.get(bp).length > 0) {
            UtilityConflictResolver.applyAll(node, rules.get(bp));
        }

        // Fire callback
        if (breakpointCallback != null) {
            breakpointCallback.accept(activeBp);
        }

        TailwindFXMetrics.instance().recordApply(1);
    }
}
