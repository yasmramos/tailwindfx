package tailwindfx;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * FxAnimation — Wrapper fluido para animaciones de JavaFX.
 *
 * Proporciona una API declarativa sobre Timeline + KeyFrame + Interpolator.
 * Cero imports de javafx.animation en el código del usuario.
 *
 * Uso:
 *   FxAnimation.fadeIn(node).play();
 *   FxAnimation.fadeIn(node, 300).play();
 *   FxAnimation.slideUp(node).onFinished(e -> ...).play();
 *   FxAnimation.pulse(node).cycleCount(3).play();
 *   FxAnimation.onHoverScale(button, 1.05);
 *   FxAnimation.chain(
 *       FxAnimation.fadeIn(node),
 *       FxAnimation.slideUp(node, 200)
 *   ).play();
 */
public final class FxAnimation {

    // Duraciones por defecto (ms)
    public static final int FAST    = 150;
    public static final int NORMAL  = 250;
    public static final int SLOW    = 400;
    public static final int SLOWER  = 600;

    private final Animation timeline;
    private Node registeredNode;
    private String registeredSlot;
    private Interpolator ease = null;

    private FxAnimation(Animation animation) {
        this.timeline = animation;
    }

    // =========================================================================
    // Animaciones de ENTRADA
    // =========================================================================

    /** Aparece desde opacidad 0 → 1 */
    public static FxAnimation fadeIn(Node node) {
        return fadeIn(node, NORMAL);
    }

    /**
     * Fades a node in from opacity 0 to 1 using {@code EASE_OUT} interpolation.
     *
     * @param node       the node to animate (must not be null)
     * @param durationMs animation duration in milliseconds (must be > 0)
     * @return a ready-to-play {@link FxAnimation} registered in slot {@code "enter"}
     */
    public static FxAnimation fadeIn(Node node, int durationMs) {
        return fadeIn(node, durationMs, Interpolator.EASE_OUT);
    }

    /**
     * fadeIn con easing personalizable.
     *
     * <pre>
     * FxAnimation.fadeIn(node, 300, Interpolator.EASE_IN).play();
     * FxAnimation.fadeIn(node, 200, Interpolator.LINEAR).play();
     * </pre>
     */
    public static FxAnimation fadeIn(Node node, int durationMs, Interpolator ease) {
        Preconditions.requireNode(node, "FxAnimation.fadeIn");
        Preconditions.requirePositiveDuration(durationMs, "FxAnimation.fadeIn");
        Preconditions.requireNonNull(ease, "FxAnimation.fadeIn", "ease");
        node.setOpacity(0);
        FxAnimation fxAnim = new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.opacityProperty(), 0, ease)),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(node.opacityProperty(), 1, ease))
        ));
        fxAnim.ease = ease;
        return fxAnim.register(node, "enter");
    }

    /** Se desliza hacia arriba mientras aparece */
    public static FxAnimation slideUp(Node node) {
        return slideUp(node, NORMAL);
    }

    public static FxAnimation slideUp(Node node, int durationMs) {
        double startY = node.getTranslateY() + 20;
        node.setOpacity(0);
        node.setTranslateY(startY);
        return new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.opacityProperty(),    0,       Interpolator.EASE_OUT),
                new KeyValue(node.translateYProperty(), startY,  Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(node.opacityProperty(),    1,       Interpolator.EASE_OUT),
                new KeyValue(node.translateYProperty(), node.getTranslateY() - 20 + 20, Interpolator.EASE_OUT))
        ));
    }

    /** Se desliza hacia abajo mientras aparece */
    public static FxAnimation slideDown(Node node) {
        return slideDown(node, NORMAL);
    }

    public static FxAnimation slideDown(Node node, int durationMs) {
        double startY = node.getTranslateY() - 20;
        node.setOpacity(0);
        node.setTranslateY(startY);
        return new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.opacityProperty(),    0,      Interpolator.EASE_OUT),
                new KeyValue(node.translateYProperty(), startY, Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(node.opacityProperty(),    1,      Interpolator.EASE_OUT),
                new KeyValue(node.translateYProperty(), node.getTranslateY() + 20 - 20, Interpolator.EASE_OUT))
        ));
    }

    /** Se desliza desde la izquierda */
    public static FxAnimation slideRight(Node node) { return slideRight(node, NORMAL); }
    public static FxAnimation slideRight(Node node, int durationMs) {
        double startX = node.getTranslateX() - 24;
        node.setOpacity(0); node.setTranslateX(startX);
        return new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.opacityProperty(),    0,      Interpolator.EASE_OUT),
                new KeyValue(node.translateXProperty(), startX, Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(node.opacityProperty(),    1,      Interpolator.EASE_OUT),
                new KeyValue(node.translateXProperty(), node.getTranslateX() + 24 - 24, Interpolator.EASE_OUT))
        ));
    }

    /** Se desliza desde la derecha */
    public static FxAnimation slideLeft(Node node) { return slideLeft(node, NORMAL); }
    public static FxAnimation slideLeft(Node node, int durationMs) {
        double startX = node.getTranslateX() + 24;
        node.setOpacity(0); node.setTranslateX(startX);
        return new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.opacityProperty(),    0,      Interpolator.EASE_OUT),
                new KeyValue(node.translateXProperty(), startX, Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(node.opacityProperty(),    1,      Interpolator.EASE_OUT),
                new KeyValue(node.translateXProperty(), node.getTranslateX() - 24 + 24, Interpolator.EASE_OUT))
        ));
    }

    /** Aparece escalando desde 0.85 → 1 */
    public static FxAnimation scaleIn(Node node) { return scaleIn(node, NORMAL); }
    public static FxAnimation scaleIn(Node node, int durationMs) {
        return scaleIn(node, durationMs, Interpolator.EASE_OUT);
    }
    /** scaleIn con easing personalizable. */
    public static FxAnimation scaleIn(Node node, int durationMs, Interpolator ease) {
        Preconditions.requireNode(node, "FxAnimation.scaleIn");
        Preconditions.requireNonNull(ease, "FxAnimation.scaleIn", "ease");
        node.setOpacity(0); node.setScaleX(0.85); node.setScaleY(0.85);
        FxAnimation fxAnim = new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.opacityProperty(), 0,    ease),
                new KeyValue(node.scaleXProperty(),  0.85, ease),
                new KeyValue(node.scaleYProperty(),  0.85, ease)),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(node.opacityProperty(), 1,    ease),
                new KeyValue(node.scaleXProperty(),  1.0,  ease),
                new KeyValue(node.scaleYProperty(),  1.0,  ease))
        ));
        fxAnim.ease = ease;
        return fxAnim;
    }

    // =========================================================================
    // Animaciones de SALIDA
    // =========================================================================

    public static FxAnimation fadeOut(Node node) { return fadeOut(node, NORMAL); }
    public static FxAnimation fadeOut(Node node, int durationMs) {
        Preconditions.requireNode(node, "FxAnimation.fadeOut");
        Preconditions.requirePositiveDuration(durationMs, "FxAnimation.fadeOut");
        return new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,        new KeyValue(node.opacityProperty(), node.getOpacity())),
            new KeyFrame(Duration.millis(durationMs), new KeyValue(node.opacityProperty(), 0, Interpolator.EASE_IN))
        )).register(node, "exit");
    }

    public static FxAnimation scaleOut(Node node) { return scaleOut(node, NORMAL); }
    public static FxAnimation scaleOut(Node node, int durationMs) {
        return new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.opacityProperty(), 1,   Interpolator.EASE_IN),
                new KeyValue(node.scaleXProperty(),  1.0, Interpolator.EASE_IN),
                new KeyValue(node.scaleYProperty(),  1.0, Interpolator.EASE_IN)),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(node.opacityProperty(), 0,    Interpolator.EASE_IN),
                new KeyValue(node.scaleXProperty(),  0.85, Interpolator.EASE_IN),
                new KeyValue(node.scaleYProperty(),  0.85, Interpolator.EASE_IN))
        ));
    }

    // =========================================================================
    // Animaciones de ATENCIÓN
    // =========================================================================

    /** Pulsa la opacidad: útil para badges, notificaciones */
    public static FxAnimation pulse(Node node) {
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,         new KeyValue(node.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(500),  new KeyValue(node.opacityProperty(), 0.6, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(1000), new KeyValue(node.opacityProperty(), 1.0))
        );
        tl.setCycleCount(Animation.INDEFINITE);
        return new FxAnimation(tl);
    }

    /**
     * Shakes the node horizontally — useful for validation errors.
     *
     * @param node the node to shake (must not be null)
     * @return a ready-to-play {@link FxAnimation} registered in slot {@code "attention"}
     */
    public static FxAnimation shake(Node node) {
        Preconditions.requireNode(node, "FxAnimation.shake");
        double x = node.getTranslateX();
        return new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,        new KeyValue(node.translateXProperty(), x)),
            new KeyFrame(Duration.millis(60),  new KeyValue(node.translateXProperty(), x - 8, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(120), new KeyValue(node.translateXProperty(), x + 8, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(180), new KeyValue(node.translateXProperty(), x - 6, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(240), new KeyValue(node.translateXProperty(), x + 6, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(300), new KeyValue(node.translateXProperty(), x - 4, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(360), new KeyValue(node.translateXProperty(), x,     Interpolator.EASE_OUT))
        ));
    }

    /** Rebota verticalmente */
    public static FxAnimation bounce(Node node) {
        double y = node.getTranslateY();
        return new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,        new KeyValue(node.translateYProperty(), y)),
            new KeyFrame(Duration.millis(100), new KeyValue(node.translateYProperty(), y - 12, Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(200), new KeyValue(node.translateYProperty(), y,      Interpolator.EASE_IN)),
            new KeyFrame(Duration.millis(280), new KeyValue(node.translateYProperty(), y - 6,  Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(360), new KeyValue(node.translateYProperty(), y,      Interpolator.EASE_IN)),
            new KeyFrame(Duration.millis(420), new KeyValue(node.translateYProperty(), y - 3,  Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(480), new KeyValue(node.translateYProperty(), y,      Interpolator.EASE_IN))
        ));
    }

    /** Flash de opacidad: para notificaciones tipo "toast" */
    public static FxAnimation flash(Node node) {
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,        new KeyValue(node.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(200), new KeyValue(node.opacityProperty(), 0.0)),
            new KeyFrame(Duration.millis(400), new KeyValue(node.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(600), new KeyValue(node.opacityProperty(), 0.0)),
            new KeyFrame(Duration.millis(800), new KeyValue(node.opacityProperty(), 1.0))
        );
        return new FxAnimation(tl);
    }

    // =========================================================================
    // Animaciones LOOP
    // =========================================================================

    /** Gira continuamente: útil para spinners/loading */
    public static FxAnimation spin(Node node) {
        return spin(node, 1000);
    }
    public static FxAnimation spin(Node node, int durationMs) {
        Preconditions.requireNode(node, "FxAnimation.spin");
        Preconditions.requirePositiveDuration(durationMs, "FxAnimation.spin");
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,            new KeyValue(node.rotateProperty(), 0)),
            new KeyFrame(Duration.millis(durationMs), new KeyValue(node.rotateProperty(), 360, Interpolator.LINEAR))
        );
        tl.setCycleCount(Animation.INDEFINITE);
        return new FxAnimation(tl);
    }

    /** Respira (escala suavemente): para indicadores de estado */
    public static FxAnimation breathe(Node node) {
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,            new KeyValue(node.scaleXProperty(), 1.0), new KeyValue(node.scaleYProperty(), 1.0)),
            new KeyFrame(Duration.millis(1000),    new KeyValue(node.scaleXProperty(), 1.06), new KeyValue(node.scaleYProperty(), 1.06), new KeyValue(node.opacityProperty(), 0.85)),
            new KeyFrame(Duration.millis(2000),    new KeyValue(node.scaleXProperty(), 1.0), new KeyValue(node.scaleYProperty(), 1.0), new KeyValue(node.opacityProperty(), 1.0))
        );
        tl.setCycleCount(Animation.INDEFINITE);
        return new FxAnimation(tl);
    }

    // =========================================================================
    // HOVER effects — se instalan como listeners permanentes
    // =========================================================================

    /**
     * Scales the node on hover — installs permanent mouse enter/exit listeners.
     *
     * <pre>
     * FxAnimation.onHoverScale(button, 1.05); // 5% larger on hover
     * </pre>
     *
     * @param node        the node to add hover effect to (must not be null)
     * @param scaleFactor scale factor on hover (e.g. {@code 1.05} = 5% larger, must be > 0)
     * @throws IllegalArgumentException if node is null or scaleFactor <= 0
     */
    public static void onHoverScale(Node node, double scaleFactor) {
        Preconditions.requireNode(node, "FxAnimation.onHoverScale");
        if (scaleFactor <= 0) {
            throw new IllegalArgumentException("FxAnimation.onHoverScale: scaleFactor must be > 0");
        }
        final double baseScaleX = node.getScaleX();
        final double baseScaleY = node.getScaleY();
        node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, e ->
            animateScale(node, baseScaleX * scaleFactor, baseScaleY * scaleFactor, 150)
        );
        node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, e ->
            animateScale(node, baseScaleX, baseScaleY, 150)
        );
    }

    private static void animateScale(Node node, double targetX, double targetY, int durationMs) {
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.scaleXProperty(), node.getScaleX()),
                new KeyValue(node.scaleYProperty(), node.getScaleY())),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(node.scaleXProperty(), targetX),
                new KeyValue(node.scaleYProperty(), targetY))
        );
        tl.play();
    }

    // =========================================================================
    // Utilidades de composición
    // =========================================================================

    /** Encadena múltiples animaciones en secuencia */
    public static FxAnimation chain(FxAnimation... animations) {
        if (animations == null || animations.length == 0) {
            throw new IllegalArgumentException("FxAnimation.chain: al menos una animación requerida");
        }
        javafx.animation.SequentialTransition seq = new javafx.animation.SequentialTransition();
        for (FxAnimation anim : animations) {
            seq.getChildren().add(anim.timeline);
        }
        return new FxAnimation(seq);
    }

    /** Ejecuta múltiples animaciones en paralelo */
    public static FxAnimation parallel(FxAnimation... animations) {
        if (animations == null || animations.length == 0) {
            throw new IllegalArgumentException("FxAnimation.parallel: al menos una animación requerida");
        }
        javafx.animation.ParallelTransition par = new javafx.animation.ParallelTransition();
        for (FxAnimation anim : animations) {
            par.getChildren().add(anim.timeline);
        }
        return new FxAnimation(par);
    }

    /** Pausa de duración específica */
    public static FxAnimation pause(int durationMs) {
        PauseTransition p = new PauseTransition(Duration.millis(durationMs));
        return new FxAnimation(p);
    }

    // =========================================================================
    // Métodos de instancia (fluent API)
    // =========================================================================

    /** Número de ciclos. Animation.INDEFINITE para loop. */
    public FxAnimation cycleCount(int count) {
        timeline.setCycleCount(count);
        return this;
    }

    /** Loop infinito */
    public FxAnimation loop() {
        timeline.setCycleCount(Animation.INDEFINITE);
        return this;
    }

    /** Velocidad: 0.5 = mitad de velocidad, 2.0 = doble */
    public FxAnimation speed(double rate) {
        timeline.setRate(rate);
        return this;
    }

    /** Callback al terminar */
    public FxAnimation onFinished(javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        timeline.setOnFinished(handler);
        return this;
    }

    /** Invierte la dirección de reproducción */
    public FxAnimation autoReverse() {
        timeline.setAutoReverse(true);
        return this;
    }

    /**
     * Registra esta animación en el AnimationRegistry del nodo.
     * Al llamar play(), cancela automáticamente la animación previa del mismo slot.
     * Slots estándar: "enter", "exit", "attention", "loop"
     */
    public FxAnimation register(javafx.scene.Node node, String slot) {
        this.registeredNode = node;
        this.registeredSlot = slot;
        return this;
    }

    /** Starts the animation. If registered, cancels the previous animation in the same slot. */
    public void play() {
        TailwindFXMetrics.instance().recordAnimationPlay();
        if (registeredNode != null && registeredSlot != null) {
            AnimationRegistry.play(registeredNode, registeredSlot, timeline);
        } else {
            timeline.play();
        }
    }

    /** Para la animación */
    public void stop() {
        timeline.stop();
        if (registeredNode != null && registeredSlot != null) {
            AnimationRegistry.cancel(registeredNode, registeredSlot);
        }
    }

    /** Pausa la animación */
    public void pause() { timeline.pause(); }

    /** Reanuda la animación */
    public void resume() { timeline.play(); }

    /**
     * Sets the easing interpolator for this animation.
     *
     * <p>Use the convenience shortcuts {@link #easeIn()}, {@link #easeOut()},
     * {@link #easeBoth()}, {@link #linear()} for common cases.
     *
     * @param interpolator the {@link Interpolator} to apply (must not be null)
     * @return this animation for chaining
     */
    public FxAnimation withEase(Interpolator interpolator) {
        Preconditions.requireNonNull(interpolator, "FxAnimation.withEase", "interpolator");
        this.ease = interpolator;
        return this;
    }

    /** .ease-in — desacelera al inicio */
    public FxAnimation easeIn()    { return withEase(Interpolator.EASE_IN); }
    /** .ease-out — desacelera al final */
    public FxAnimation easeOut()   { return withEase(Interpolator.EASE_OUT); }
    /** .ease-in-out — desacelera en ambos extremos */
    public FxAnimation easeBoth()  { return withEase(Interpolator.EASE_BOTH); }
    /** .linear — velocidad constante */
    public FxAnimation linear()    { return withEase(Interpolator.LINEAR); }

    /** Acceso al Animation subyacente para configuración avanzada */
    public Animation raw() { return timeline; }
}
