package tailwindfx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * BreakpointManager v2 — Responsive engine for TailwindFX.
 * 
 * JavaFX does not have @media queries. This engine simulates them:
 *   1. Listens to Stage.widthProperty() (and optionally heightProperty)
 *   2. Calculates the active breakpoint
 *   3. Injects cumulative CSS classes into the Scene root (.bp-sm, .bp-md...)
 *   4. Executes registered callbacks when crossing each breakpoint
 * 
 * Default breakpoints (same as Tailwind):
 *   XS  < 640px   → no class
 *   SM  ≥ 640px   → .bp-sm
 *   MD  ≥ 768px   → .bp-sm .bp-md
 *   LG  ≥ 1024px  → .bp-sm .bp-md .bp-lg
 *   XL  ≥ 1280px  → .bp-sm .bp-md .bp-lg .bp-xl
 *   2XL ≥ 1536px  → .bp-sm .bp-md .bp-lg .bp-xl .bp-2xl
 * 
 * Classes are CUMULATIVE like in Tailwind.
 * .bp-lg implies that .bp-sm and .bp-md are also active.
 * 
 * Basic usage:
 *   TailwindFX.responsive(stage)
 *       .onBreakpoint(Breakpoint.MD, () -> adaptLayout());
 * 
 * With custom breakpoints:
 *   BreakpointManager.custom()
 *       .add("phone",  0)
 *       .add("tablet", 600)
 *       .add("desktop", 960)
 *       .attach(stage);
 * 
 * CSS in tailwindfx.css:
 *   .bp-md .sidebar  { -fx-pref-width: 240px; }
 *   .bp-sm .sidebar  { -fx-pref-width: 60px;  }
 *   .bp-lg .card-grid { -fx-vgap: 20px; }
 * 
 * Architecture: Publisher pattern
 * - Single listener per Stage with throttling (~120ms)
 * - Exposes activeBreakpointProperty() for subscribers (e.g., VariantManager)
 * - Prevents O(N) listeners by centralizing breakpoint detection
 */
public final class BreakpointManager {

    // Cache of BreakpointManager instances per Stage
    private static final ConcurrentHashMap<Stage, BreakpointManager> instances = new ConcurrentHashMap<>();

    // =========================================================================
    // Breakpoints
    // =========================================================================

    public enum Breakpoint {
        XS(0,    null),
        SM(640,  "bp-sm"),
        MD(768,  "bp-md"),
        LG(1024, "bp-lg"),
        XL(1280, "bp-xl"),
        XXL(1536,"bp-2xl");

        public final double  minWidth;
        public final String  cssClass;

        Breakpoint(double minWidth, String cssClass) {
            this.minWidth = minWidth;
            this.cssClass = cssClass;
        }

        public static Breakpoint forWidth(double width) {
            Breakpoint current = XS;
            for (Breakpoint bp : values())
                if (width >= bp.minWidth) current = bp;
            return current;
        }
    }

    // =========================================================================
    // BP — shorthand alias for Breakpoint (allows BreakpointManager.BP.MD)
    // =========================================================================

    /**
     * Shorthand alias for {@link Breakpoint} — allows the concise form
     * {@code BreakpointManager.BP.MD} instead of {@code BreakpointManager.Breakpoint.MD}.
     *
     * <pre>
     * bpm.onBreakpoint(BreakpointManager.BP.MD, () -> flex.setDirection(Direction.ROW));
     * </pre>
     */
    public static final class BP {
        public static final Breakpoint XS  = Breakpoint.XS;
        public static final Breakpoint SM  = Breakpoint.SM;
        public static final Breakpoint MD  = Breakpoint.MD;
        public static final Breakpoint LG  = Breakpoint.LG;
        public static final Breakpoint XL  = Breakpoint.XL;
        public static final Breakpoint XXL = Breakpoint.XXL;
        private BP() {}
    }

    // =========================================================================
    // Builder for custom breakpoints
    // =========================================================================

    public static final class CustomBuilder {
        private final List<CustomBreakpoint> bps = new ArrayList<>();

        public CustomBuilder add(String cssClass, double minWidth) {
            bps.add(new CustomBreakpoint(cssClass, minWidth));
            bps.sort(Comparator.comparingDouble(b -> b.minWidth));
            return this;
        }

        public BreakpointManager attach(Stage stage) {
            return new BreakpointManager(stage, bps);
        }
    }

    private record CustomBreakpoint(String cssClass, double minWidth) {}

    /** Creates a builder for custom breakpoints */
    public static CustomBuilder custom() { return new CustomBuilder(); }

    /**
     * Gets or creates a BreakpointManager instance for the given Stage.
     * Uses a cache to ensure only one instance per Stage.
     * 
     * @param stage The stage to get the BreakpointManager for
     * @return The BreakpointManager instance
     */
    public static BreakpointManager from(Stage stage) {
        return instances.computeIfAbsent(stage, s -> new BreakpointManager(s, null));
    }

    // =========================================================================
    // State
    // =========================================================================

    private final Stage  stage;
    private final List<CustomBreakpoint>     customBps;
    private Breakpoint                       current;
    private String                           currentCustom;
    private final List<BpListener>           listeners = new ArrayList<>();
    private ChangeListener<Number>           widthListener;
    private ChangeListener<Number>           heightListener;
    private boolean                          orientationEnabled = false;
    
    // Throttle to avoid thrashing on resize
    private long lastUpdate = 0;
    private static final long THROTTLE_MS = 120;
    
    // Set to prevent duplicate callbacks
    private final java.util.Set<String> registeredCallbacks = new java.util.HashSet<>();
    
    // Property for reactive breakpoint changes (Publisher pattern)
    private final ObjectProperty<Breakpoint> activeBreakpoint;

    // =========================================================================
    // Construction
    // =========================================================================

    private BreakpointManager(Stage stage, List<CustomBreakpoint> custom) {
        this.stage     = Preconditions.requireNonNull(stage, "BreakpointManager", "stage");
        this.customBps = custom;
        this.current   = Breakpoint.forWidth(stage.getWidth());
        this.activeBreakpoint = new SimpleObjectProperty<>(current);
        attachWidthListener();
    }

    /** Creates with default breakpoints (Tailwind) */
    static BreakpointManager attach(Stage stage) {
        return new BreakpointManager(stage, null);
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Registers a callback that executes when crossing a breakpoint
     * (both up and down).
     * Prevents duplicates: if the same callback is already registered for this breakpoint, it won't be added again.
     */
    public BreakpointManager onBreakpoint(Breakpoint bp, Runnable callback) {
        Preconditions.requireNonNull(bp, "BreakpointManager.onBreakpoint", "breakpoint");
        Preconditions.requireNonNull(callback, "BreakpointManager.onBreakpoint", "callback");
        
        String key = bp.name() + ":" + System.identityHashCode(callback);
        if (!registeredCallbacks.add(key)) {
            return this; // Already registered, avoid duplicate
        }
        
        listeners.add(new BpListener(bp.minWidth, callback));
        return this;
    }

    /**
     * Registers a callback with information about whether it's activating or deactivating.
     * active=true → crossed upward (larger screen)
     * active=false → crossed downward (smaller screen)
     */
    public BreakpointManager onBreakpoint(Breakpoint bp, Consumer<Boolean> callback) {
        listeners.add(new BpListener(bp.minWidth, null, callback));
        return this;
    }

    /** Enables orientation detection (portrait/landscape) */
    public BreakpointManager withOrientation() {
        orientationEnabled = true;
        if (stage.getScene() != null)
            updateOrientation(stage.getScene(), stage.getWidth(), stage.getHeight());
        
        heightListener = (o, old, h) ->
            updateOrientation(stage.getScene(), stage.getWidth(), h.doubleValue());
        stage.heightProperty().addListener(heightListener);
        return this;
    }

    /** Currently active breakpoint */
    public Breakpoint current() { return current; }

    /**
     * Returns a read-only property of the active breakpoint.
     * Subscribers can listen to this property to react to breakpoint changes.
     * This is the main mechanism for the Publisher-Subscriber pattern.
     * 
     * @return Read-only property of the active breakpoint
     */
    public javafx.beans.property.ReadOnlyObjectProperty<Breakpoint> activeBreakpointProperty() {
        return activeBreakpoint;
    }

    /** If current width is >= the given breakpoint */
    public boolean is(Breakpoint bp) { return current.minWidth >= bp.minWidth; }

    /** If current width is < the given breakpoint (below) */
    public boolean below(Breakpoint bp) { return !is(bp); }

    /** Disconnects all listeners */
    public void detach() {
        if (widthListener != null) stage.widthProperty().removeListener(widthListener);
        if (heightListener != null) stage.heightProperty().removeListener(heightListener);
        listeners.clear();
        registeredCallbacks.clear();
        instances.remove(stage);
    }

    // =========================================================================
    // Internal
    // =========================================================================

    private void attachWidthListener() {
        widthListener = (obs, old, newVal) -> update(newVal.doubleValue());
        stage.widthProperty().addListener(widthListener);
        update(stage.getWidth()); // apply initial state
    }

    private void update(double width) {
        long now = System.nanoTime();
        if (now - lastUpdate < THROTTLE_MS * 1_000_000L) return;
        lastUpdate = now;
        
        javafx.application.Platform.runLater(() -> {
            if (customBps != null && !customBps.isEmpty()) {
                updateCustom(width);
            } else {
                updateDefault(width);
            }
        });
    }

    private void updateDefault(double width) {
        Breakpoint next = Breakpoint.forWidth(width);
        if (next == current) return;

        Breakpoint prev = current;
        current = next;

        // Update the reactive property (notifies subscribers)
        activeBreakpoint.set(next);

        Scene scene = stage.getScene();
        if (scene != null && scene.getRoot() != null)
            applyDefaultClasses(scene, next);

        double prevMin = prev.minWidth;
        double nextMin = next.minWidth;

        for (BpListener l : listeners) {
            boolean wasActive = prevMin >= l.minWidth;
            boolean isActive  = nextMin >= l.minWidth;
            if (wasActive != isActive) {
                if (l.runnable != null) l.runnable.run();
                if (l.consumer != null) l.consumer.accept(isActive);
            }
        }
    }

    private void updateCustom(double width) {
        String next = null;
        for (CustomBreakpoint bp : customBps)
            if (width >= bp.minWidth) next = bp.cssClass;

        if (Objects.equals(next, currentCustom)) return;
        currentCustom = next;

        Scene scene = stage.getScene();
        if (scene != null && scene.getRoot() != null) {
            var root = scene.getRoot();
            root.getStyleClass().removeIf(c ->
                customBps.stream().anyMatch(bp -> bp.cssClass.equals(c)));
            for (CustomBreakpoint bp : customBps)
                if (width >= bp.minWidth && bp.cssClass != null)
                    root.getStyleClass().add(bp.cssClass);
        }
    }

    private void applyDefaultClasses(Scene scene, Breakpoint active) {
        var root = scene.getRoot();
        root.getStyleClass().removeIf(c -> c.startsWith("bp-"));
        for (Breakpoint bp : Breakpoint.values()) {
            if (bp.cssClass != null && active.minWidth >= bp.minWidth)
                root.getStyleClass().add(bp.cssClass);
        }
    }

    private void updateOrientation(Scene scene, double w, double h) {
        if (scene == null || scene.getRoot() == null) return;
        var root = scene.getRoot();
        root.getStyleClass().removeIf(c -> c.equals("portrait") || c.equals("landscape"));
        root.getStyleClass().add((h > w ? "portrait" : "landscape").trim());
    }

    private record BpListener(
        double minWidth,
        Runnable runnable,
        Consumer<Boolean> consumer
    ) {
        BpListener(double minWidth, Runnable runnable) {
            this(minWidth, runnable, null);
        }
    }
}
