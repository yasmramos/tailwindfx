package tailwindfx;

import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * BreakpointManager v2 — Responsive engine de TailwindFX.
 *
 * En JavaFX no existen @media queries. Este engine los simula:
 *   1. Escucha Stage.widthProperty() (y opcionalmente heightProperty)
 *   2. Calcula el breakpoint activo
 *   3. Inyecta clases CSS acumulativas en el root de la Scene (.bp-sm, .bp-md…)
 *   4. Ejecuta callbacks registrados al cruzar cada breakpoint
 *
 * Breakpoints por defecto (iguales a Tailwind):
 *   XS  < 640px   → sin clase
 *   SM  ≥ 640px   → .bp-sm
 *   MD  ≥ 768px   → .bp-sm .bp-md
 *   LG  ≥ 1024px  → .bp-sm .bp-md .bp-lg
 *   XL  ≥ 1280px  → .bp-sm .bp-md .bp-lg .bp-xl
 *   2XL ≥ 1536px  → .bp-sm .bp-md .bp-lg .bp-xl .bp-2xl
 *
 * Las clases son ACUMULATIVAS como en Tailwind.
 * .bp-lg implica que también .bp-sm y .bp-md están activas.
 *
 * Uso básico:
 *   TailwindFX.responsive(stage)
 *       .onBreakpoint(Breakpoint.MD, () -> adaptLayout());
 *
 * Con breakpoints custom:
 *   BreakpointManager.custom()
 *       .add("phone",  0)
 *       .add("tablet", 600)
 *       .add("desktop", 960)
 *       .attach(stage);
 *
 * CSS en tailwindfx.css:
 *   .bp-md .sidebar  { -fx-pref-width: 240px; }
 *   .bp-sm .sidebar  { -fx-pref-width: 60px;  }
 *   .bp-lg .card-grid { -fx-vgap: 20px; }
 */
public final class BreakpointManager {

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
    // Builder para breakpoints custom
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

    /** Crea un builder para breakpoints personalizados */
    public static CustomBuilder custom() { return new CustomBuilder(); }

    // =========================================================================
    // Estado
    // =========================================================================

    private final Stage  stage;
    private final List<CustomBreakpoint>     customBps;
    private Breakpoint                       current;
    private String                           currentCustom;
    private final List<BpListener>           listeners = new ArrayList<>();
    private ChangeListener<Number>           widthListener;
    private ChangeListener<Number>           heightListener;
    private boolean                          orientationEnabled = false;
    
    // Throttle para evitar thrashing en resize
    private long lastUpdate = 0;
    private static final long THROTTLE_MS = 120;
    
    // Set para evitar callbacks duplicados
    private final java.util.Set<String> registeredCallbacks = new java.util.HashSet<>();

    // =========================================================================
    // Construcción
    // =========================================================================

    private BreakpointManager(Stage stage, List<CustomBreakpoint> custom) {
        this.stage     = Preconditions.requireNonNull(stage, "BreakpointManager", "stage");
        this.customBps = custom;
        this.current   = Breakpoint.forWidth(stage.getWidth());
        attachWidthListener();
    }

    /** Crea con breakpoints por defecto (Tailwind) */
    static BreakpointManager attach(Stage stage) {
        return new BreakpointManager(stage, null);
    }

    // =========================================================================
    // API pública
    // =========================================================================

    /**
     * Registra un callback que se ejecuta al cruzar un breakpoint
     * (tanto al subir como al bajar).
     * Evita duplicados: si ya está registrado el mismo callback para este breakpoint, no lo añade.
     */
    public BreakpointManager onBreakpoint(Breakpoint bp, Runnable callback) {
        Preconditions.requireNonNull(bp, "BreakpointManager.onBreakpoint", "breakpoint");
        Preconditions.requireNonNull(callback, "BreakpointManager.onBreakpoint", "callback");
        
        String key = bp.name() + ":" + System.identityHashCode(callback);
        if (!registeredCallbacks.add(key)) {
            return this; // Ya está registrado, evitar duplicado
        }
        
        listeners.add(new BpListener(bp.minWidth, callback));
        return this;
    }

    /**
     * Registra un callback con información de si se está activando o desactivando.
     * active=true → se cruzó hacia arriba (pantalla más grande)
     * active=false → se cruzó hacia abajo (pantalla más pequeña)
     */
    public BreakpointManager onBreakpoint(Breakpoint bp, Consumer<Boolean> callback) {
        listeners.add(new BpListener(bp.minWidth, null, callback));
        return this;
    }

    /** Activa detección de orientación (portrait/landscape) */
    public BreakpointManager withOrientation() {
        orientationEnabled = true;
        if (stage.getScene() != null)
            updateOrientation(stage.getScene(), stage.getWidth(), stage.getHeight());
        
        heightListener = (o, old, h) ->
            updateOrientation(stage.getScene(), stage.getWidth(), h.doubleValue());
        stage.heightProperty().addListener(heightListener);
        return this;
    }

    /** Breakpoint activo en este momento */
    public Breakpoint current() { return current; }

    /** Si el ancho actual es >= al breakpoint dado */
    public boolean is(Breakpoint bp) { return current.minWidth >= bp.minWidth; }

    /** Si el ancho actual es < al breakpoint dado (below) */
    public boolean below(Breakpoint bp) { return !is(bp); }

    /** Desconecta todos los listeners */
    public void detach() {
        if (widthListener != null) stage.widthProperty().removeListener(widthListener);
        if (heightListener != null) stage.heightProperty().removeListener(heightListener);
        listeners.clear();
        registeredCallbacks.clear();
    }

    // =========================================================================
    // Internos
    // =========================================================================

    private void attachWidthListener() {
        widthListener = (obs, old, newVal) -> update(newVal.doubleValue());
        stage.widthProperty().addListener(widthListener);
        update(stage.getWidth()); // aplicar estado inicial
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
