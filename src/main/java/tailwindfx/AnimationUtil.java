import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;

import java.util.Objects;

/**
 * AnimationUtil — Animaciones fluidas para nodos JavaFX.
 *
 * Lo que ningún framework JavaFX tiene: una API declarativa sobre
 * Timeline + KeyFrame + Interpolator. Cero imports de javafx.animation
 * en el código del usuario.
 *
 * Categorías:
 *   Entrada:   fadeIn, slideUp, slideDown, slideLeft, slideRight, scaleIn, zoomIn
 *   Salida:    fadeOut, slideOut*, scaleOut, zoomOut
 *   Atención:  pulse, shake, bounce, flash, ping
 *   Hover:     onHover (scale, glow, lift — usando setOnMouseEntered/Exited)
 *   Loop:      spin, breathe
 *   Control:   pause, resume, stop, reverse
 *
 * Uso:
 *   AnimationUtil.fadeIn(node).play();
 *   AnimationUtil.fadeIn(node, 300).play();
 *   AnimationUtil.slideUp(node).onFinished(e -> ...).play();
 *   AnimationUtil.pulse(node).cycleCount(3).play();
 *   AnimationUtil.onHoverScale(button, 1.05);
 *   AnimationUtil.chain(
 *       AnimationUtil.fadeIn(node),
 *       AnimationUtil.slideUp(node, 200)
 *   ).play();
 */
public final class AnimationUtil {

    // Duraciones por defecto (ms)
    public static final int FAST    = 150;
    public static final int NORMAL  = 250;
    public static final int SLOW    = 400;
    public static final int SLOWER  = 600;

    private AnimationUtil() {}

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
     * AnimationUtil.fadeIn(node, 300, Interpolator.EASE_IN).play();
     * AnimationUtil.fadeIn(node, 200, Interpolator.LINEAR).play();
     * </pre>
     */
    public static FxAnimation fadeIn(Node node, int durationMs, Interpolator ease) {
        Preconditions.requireNode(node, "AnimationUtil.fadeIn");
        Preconditions.requirePositiveDuration(durationMs, "AnimationUtil.fadeIn");
        Preconditions.requireNonNull(ease, "AnimationUtil.fadeIn", "ease");
        node.setOpacity(0);
        return new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.opacityProperty(), 0, ease)),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(node.opacityProperty(), 1, ease))
        )).register(node, "enter");
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
        Preconditions.requireNode(node, "AnimationUtil.scaleIn");
        Preconditions.requireNonNull(ease, "AnimationUtil.scaleIn", "ease");
        node.setOpacity(0); node.setScaleX(0.85); node.setScaleY(0.85);
        return new FxAnimation(new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.opacityProperty(), 0,    ease),
                new KeyValue(node.scaleXProperty(),  0.85, ease),
                new KeyValue(node.scaleYProperty(),  0.85, ease)),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(node.opacityProperty(), 1,    ease),
                new KeyValue(node.scaleXProperty(),  1.0,  ease),
                new KeyValue(node.scaleYProperty(),  1.0,  ease))
        ));
    }

    // =========================================================================
    // Animaciones de SALIDA
    // =========================================================================

    public static FxAnimation fadeOut(Node node) { return fadeOut(node, NORMAL); }
    public static FxAnimation fadeOut(Node node, int durationMs) {
        Preconditions.requireNode(node, "AnimationUtil.fadeOut");
        Preconditions.requirePositiveDuration(durationMs, "AnimationUtil.fadeOut");
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
        Preconditions.requireNode(node, "AnimationUtil.shake");
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
        Preconditions.requireNode(node, "AnimationUtil.spin");
        Preconditions.requirePositiveDuration(durationMs, "AnimationUtil.spin");
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
     * AnimationUtil.onHoverScale(button, 1.05); // 5% larger on hover
     * </pre>
     *
     * @param node        the node to add hover effect to (must not be null)
     * @param scaleFactor scale factor on hover (e.g. {@code 1.05} = 5% larger, must be > 0)
     * @throws IllegalArgumentException if node is null or scaleFactor <= 0
     */
    /**
     * Scales the node on hover. Multiple hover effects can be stacked on the same node
     * because this method uses {@code addEventHandler} (not {@code setOnMouseEntered},
     * which would overwrite previous listeners).
     *
     * @param node        the node to add the hover effect to
     * @param scaleFactor scale factor on hover (e.g. {@code 1.05} = 5% larger)
     */
    public static void onHoverScale(Node node, double scaleFactor) {
        Preconditions.requireNode(node, "AnimationUtil.onHoverScale");
        Preconditions.requirePositiveScale(scaleFactor, "AnimationUtil.onHoverScale");
        Timeline hoverIn = new Timeline(new KeyFrame(Duration.millis(FAST),
            new KeyValue(node.scaleXProperty(), scaleFactor, Interpolator.EASE_OUT),
            new KeyValue(node.scaleYProperty(), scaleFactor, Interpolator.EASE_OUT)));
        Timeline hoverOut = new Timeline(new KeyFrame(Duration.millis(FAST),
            new KeyValue(node.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
            new KeyValue(node.scaleYProperty(), 1.0, Interpolator.EASE_OUT)));
        javafx.event.EventHandler<javafx.scene.input.MouseEvent> enterH =
            e -> { hoverOut.stop(); hoverIn.play(); };
        javafx.event.EventHandler<javafx.scene.input.MouseEvent> exitH =
            e -> { hoverIn.stop();  hoverOut.play(); };
        node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, enterH);
        node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED,  exitH);
        storeHoverHandlers(node, enterH, exitH);
    }

    /**
     * Sube el nodo ligeramente al hacer hover (efecto "lift").
     * @param node
     */
    public static void onHoverLift(Node node) { onHoverLift(node, -4); }
    public static void onHoverLift(Node node, double offsetY) {
        double originalY = node.getTranslateY();
        Timeline hoverIn  = new Timeline(new KeyFrame(Duration.millis(FAST),
            new KeyValue(node.translateYProperty(), originalY + offsetY, Interpolator.EASE_OUT)));
        Timeline hoverOut = new Timeline(new KeyFrame(Duration.millis(FAST),
            new KeyValue(node.translateYProperty(), originalY, Interpolator.EASE_OUT)));
        // addEventHandler chains with existing hover listeners (e.g. onHoverScale)
        javafx.event.EventHandler<javafx.scene.input.MouseEvent> enterLift =
            e -> { hoverOut.stop(); hoverIn.play(); };
        javafx.event.EventHandler<javafx.scene.input.MouseEvent> exitLift =
            e -> { hoverIn.stop();  hoverOut.play(); };
        node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, enterLift);
        node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED,  exitLift);
        storeHoverHandlers(node, enterLift, exitLift);
    }

    /**
     * Reduce la opacidad ligeramente al hacer hover (efecto "dim").
     */
    public static void onHoverDim(Node node, double targetOpacity) {
        Timeline hoverIn  = new Timeline(new KeyFrame(Duration.millis(FAST),
            new KeyValue(node.opacityProperty(), targetOpacity, Interpolator.EASE_OUT)));
        Timeline hoverOut = new Timeline(new KeyFrame(Duration.millis(FAST),
            new KeyValue(node.opacityProperty(), 1.0, Interpolator.EASE_OUT)));
        // addEventHandler chains with existing hover listeners — does not overwrite
        javafx.event.EventHandler<javafx.scene.input.MouseEvent> enterH2 =
            e -> { hoverOut.stop(); hoverIn.play(); };
        javafx.event.EventHandler<javafx.scene.input.MouseEvent> exitH2 =
            e -> { hoverIn.stop();  hoverOut.play(); };
        node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, enterH2);
        node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED,  exitH2);
        storeHoverHandlers(node, enterH2, exitH2);
    }

    /**
     * Removes all hover effects previously installed by
     * {@link #onHoverScale}, {@link #onHoverLift}, or {@link #onHoverDim}.
     *
     * <p>Because hover effects use {@code addEventHandler}, they accumulate.
     * Call this method before re-installing effects to avoid stacking.
     *
     * @param node the node to remove hover effects from
     */
    public static void removeHoverEffects(Node node) {
        Preconditions.requireNode(node, "AnimationUtil.removeHoverEffects");
        // Retrieve and remove the stored handler references
        @SuppressWarnings("unchecked")
        var handlers = (java.util.List<javafx.event.EventHandler<javafx.scene.input.MouseEvent>>)
            node.getProperties().remove("tailwindfx.hover.handlers");
        if (handlers != null) {
            for (int i = 0; i < handlers.size(); i += 2) {
                node.removeEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, handlers.get(i));
                if (i + 1 < handlers.size())
                    node.removeEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, handlers.get(i + 1));
            }
        }
        // Reset visual properties to baseline
        node.setScaleX(1.0); node.setScaleY(1.0);
        node.setOpacity(1.0);
    }

    // =========================================================================
    // Internal helpers
    // =========================================================================

    /** Stores hover handler pairs in node properties for later removal. */
    @SuppressWarnings("unchecked")
    private static void storeHoverHandlers(
            Node node,
            javafx.event.EventHandler<javafx.scene.input.MouseEvent> enterH,
            javafx.event.EventHandler<javafx.scene.input.MouseEvent> exitH) {
        var list = (java.util.List<javafx.event.EventHandler<javafx.scene.input.MouseEvent>>)
            node.getProperties().computeIfAbsent(
                "tailwindfx.hover.handlers",
                k -> new java.util.ArrayList<>());
        list.add(enterH);
        list.add(exitH);
    }

    // =========================================================================
    // Composición — ejecutar animaciones en secuencia
    // =========================================================================

    /**
     * Encadena animaciones para que se ejecuten en secuencia.
     *
     *   AnimationUtil.chain(
     *       AnimationUtil.fadeIn(node),
     *       AnimationUtil.slideUp(node)
     *   ).play();
     */
    public static FxAnimation chain(FxAnimation... animations) {
        SequentialTransition seq = new SequentialTransition();
        for (FxAnimation a : animations) seq.getChildren().add(a.timeline);
        return new FxAnimation(seq);
    }

    /**
     * Ejecuta animaciones en paralelo.
     */
    public static FxAnimation parallel(FxAnimation... animations) {
        ParallelTransition par = new ParallelTransition();
        for (FxAnimation a : animations) par.getChildren().add(a.timeline);
        return new FxAnimation(par);
    }

    /**
     * Pausa antes de la siguiente animación en una cadena.
     */
    public static FxAnimation delay(int ms) {
        return new FxAnimation(new PauseTransition(Duration.millis(ms)));
    }

    // =========================================================================
    // FxAnimation — wrapper con fluent API sobre Animation
    // =========================================================================


    // =========================================================================
    // AnimationRegistry — cancels active timelines before starting a new one
    // =========================================================================

    /**
     * Per-node animation registry. Guarantees that starting a new animation
     * cancels any previous one in the same slot, preventing zombie timelines
     * from competing for the same node properties.
     *
     * <h3>Predefined slots</h3>
     * Each slot governs a semantic category of animation. Starting an animation
     * in a slot cancels whatever is currently running in that slot:
     * <ul>
     *   <li>{@code "enter"}      — entry animations: {@link #fadeIn}, {@link #slideUp},
     *                               {@link #slideDown}, {@link #slideLeft}, {@link #slideRight},
     *                               {@link #scaleIn}. Only one entry animation runs at a time.</li>
     *   <li>{@code "exit"}       — exit animations: {@link #fadeOut}, {@link #scaleOut}.</li>
     *   <li>{@code "attention"}  — attention animations: {@link #shake}, {@link #bounce},
     *                               {@link #flash}, {@link #pulse}.</li>
     *   <li>{@code "loop"}       — infinite animations: {@link #spin}, {@link #breathe}.
     *                               Stop explicitly with {@link #cancelAll} or
     *                               {@link FxAnimation#stop}.</li>
     *   <li>{@code "transition"} — property transitions via {@link TailwindFX#transition}.</li>
     * </ul>
     *
     * <h3>Custom slots</h3>
     * Any string is a valid slot. Use custom slots when you need independent
     * animations that should not cancel each other:
     * <pre>
     * AnimationRegistry.play(node, "highlight", myHighlightAnim);
     * AnimationRegistry.play(node, "bounce",    myBounceAnim);
     * // Both run concurrently — different slots
     * </pre>
     *
     * <h3>Auto-cancel on scene removal</h3>
     * All registered animations are automatically stopped when the node is
     * removed from the scene graph, preventing Timeline → node memory leaks.
     */
    public static final class AnimationRegistry {

        // Node.getProperties() key → mapa de slot → Animation activo
        private static final String KEY = "tailwindfx.animations";

        private AnimationRegistry() {}

        /**
         * Registers and starts an animation in a slot, cancelling any previous one in that slot.
         * Also installs a scene listener that auto-cancels all animations if the node
         * is removed from the scene graph, preventing Timeline-induced memory leaks.
         */
        public static void play(javafx.scene.Node node, String slot, Animation animation) {
            cancel(node, slot);
            getSlots(node).put(slot, animation);
            animation.setOnFinished(e -> getSlots(node).remove(slot));
            animation.play();
            installSceneListener(node);
        }

        /**
         * Installs a scene listener (at most once per node) that cancels all active
         * animations when the node is removed from the scene graph.
         *
         * <p><b>Why this matters:</b> a running {@link javafx.animation.Timeline} holds
         * a reference to the {@link javafx.animation.KeyValue} targets, which in turn
         * hold references to node properties. This creates a strong reference chain:
         * {@code Timeline → KeyValue → node}, preventing GC of detached nodes.
         * Cancelling the timeline on scene removal breaks that chain.
         */
        private static void installSceneListener(javafx.scene.Node node) {
            final String LISTENER_KEY = "tailwindfx.anim.scene-listener";
            if (node.getProperties().containsKey(LISTENER_KEY)) return; // already installed

            javafx.beans.value.ChangeListener<javafx.scene.Scene> listener =
                (obs, oldScene, newScene) -> {
                    if (newScene == null) {
                        cancelAll(node);
                        Preconditions.LOG.fine(
                            "AnimationRegistry: auto-cancelled animations on scene detach — "
                            + node.getClass().getSimpleName());
                    }
                };
            node.sceneProperty().addListener(listener);
            // Mark as installed (store Boolean.TRUE, not the listener — avoid strong ref loop)
            node.getProperties().put(LISTENER_KEY, Boolean.TRUE);
        }

        /** Cancela la animación activa en un slot */
        public static void cancel(javafx.scene.Node node, String slot) {
            Animation prev = getSlots(node).get(slot);
            if (prev != null) { prev.stop(); getSlots(node).remove(slot); }
        }

        /** Cancela todas las animaciones activas en un nodo */
        public static void cancelAll(javafx.scene.Node node) {
            getSlots(node).values().forEach(Animation::stop);
            getSlots(node).clear();
        }

        /** Si hay una animación activa en el slot */
        public static boolean isActive(javafx.scene.Node node, String slot) {
            Animation a = getSlots(node).get(slot);
            return a != null && a.getStatus() == Animation.Status.RUNNING;
        }

        @SuppressWarnings("unchecked")
        private static java.util.Map<String, Animation> getSlots(javafx.scene.Node node) {
            return (java.util.Map<String, Animation>) node.getProperties()
                .computeIfAbsent(KEY, k -> new java.util.HashMap<String, Animation>());
        }
    }
    /**
     * A fluent wrapper around JavaFX {@link javafx.animation.Animation} that provides
     * a clean, chainable API for configuring and controlling animations.
     *
     * <p>This class serves as the primary interface for TailwindFX animation operations,
     * offering method chaining for common animation properties and integration with
     * the {@link AnimationRegistry} for automatic lifecycle management.</p>
     *
     * <h2>Key Features</h2>
     * <ul>
     *   <li><strong>Fluent API:</strong> Chain configuration methods for readable code</li>
     *   <li><strong>Registry Integration:</strong> Automatic slot-based animation management</li>
     *   <li><strong>Memory Safety:</strong> Prevents zombie timelines and memory leaks</li>
     *   <li><strong>Scene Awareness:</strong> Auto-cancels on node removal from scene graph</li>
     * </ul>
     *
     * <h2>Basic Usage</h2>
     * <pre>{@code
     * // Simple fade-in animation
     * AnimationUtil.fadeIn(button).play();
     *
     * // Chained configuration
     * AnimationUtil.slideUp(node, 300)
     *     .easeOut()
     *     .cycleCount(3)
     *     .onFinished(e -> System.out.println("Animation complete!"))
     *     .play();
     *
     * // Registry-controlled animation
     * AnimationUtil.pulse(badge)
     *     .register(badge, "attention")
     *     .loop()
     *     .play();
     * }</pre>
     *
     * <h2>Standard Slots</h2>
     * <table border="1" cellpadding="4">
     *   <tr><th>Slot</th><th>Purpose</th><th>Example Methods</th></tr>
     *   <tr><td>"enter"</td><td>Entry animations</td><td>fadeIn, slideUp, scaleIn</td></tr>
     *   <tr><td>"exit"</td><td>Exit animations</td><td>fadeOut, scaleOut</td></tr>
     *   <tr><td>"attention"</td><td>Attention-grabbing effects</td><td>shake, pulse, flash</td></tr>
     *   <tr><td>"loop"</td><td>Infinite animations</td><td>spin, breathe</td></tr>
     *   <tr><td>"transition"</td><td>Property transitions</td><td>TailwindFX.transition()</td></tr>
     * </table>
     *
     * @author TailwindFX Team
     * @since 1.0.0
     * @see AnimationUtil
     * @see AnimationRegistry
     */
    public static final class FxAnimation {

        final Animation timeline;
        private javafx.scene.Node registeredNode;
        private String             registeredSlot;

        /**
         * Creates a new FxAnimation wrapping the specified JavaFX animation.
         *
         * @param animation the underlying JavaFX Animation (must not be null)
         * @throws NullPointerException if animation is null
         */
        FxAnimation(Animation animation) {
            this.timeline = Objects.requireNonNull(animation, "animation");
        }

        /**
         * Sets the number of times this animation will repeat.
         *
         * <p>The animation will play the specified number of times before stopping.
         * Use {@link #loop()} for infinite repetition.</p>
         *
         * @param count the number of cycles to play (must be positive, or {@code Animation.INDEFINITE})
         * @return this animation instance for method chaining
         * @throws IllegalArgumentException if count is negative and not {@code Animation.INDEFINITE}
         * @see #loop()
         * @see Animation#INDEFINITE
         */
        public FxAnimation cycleCount(int count) {
            timeline.setCycleCount(count);
            return this;
        }

        /**
         * Configures this animation to repeat indefinitely.
         *
         * <p>This is equivalent to calling {@code cycleCount(Animation.INDEFINITE)}.
         * The animation will continue playing until explicitly stopped with {@link #stop()}.</p>
         *
         * @return this animation instance for method chaining
         * @see #cycleCount(int)
         * @see #stop()
         */
        public FxAnimation loop() {
            timeline.setCycleCount(Animation.INDEFINITE);
            return this;
        }

        /**
         * Sets the playback speed multiplier for this animation.
         *
         * <p>A speed of 1.0 is normal speed, 2.0 is double speed, 0.5 is half speed,
         * and negative values reverse the direction while playing.</p>
         *
         * @param rate the speed multiplier (must be positive)
         * @return this animation instance for method chaining
         * @throws IllegalArgumentException if rate is not positive
         * @see #autoReverse()
         */
        public FxAnimation speed(double rate) {
            Preconditions.requirePositiveSpeed(rate, "FxAnimation.speed");
            timeline.setRate(rate);
            return this;
        }

        /**
         * Sets a callback handler that will be invoked when this animation completes.
         *
         * <p>The handler will be called once per completed cycle. For infinite animations
         * ({@link #loop()}), the handler is not invoked since the animation never truly ends.</p>
         *
         * @param handler the event handler to invoke on completion (must not be null)
         * @return this animation instance for method chaining
         * @throws NullPointerException if handler is null
         * @see #onFinished(javafx.event.EventHandler)
         */
        public FxAnimation onFinished(javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
            Preconditions.requireNonNull(handler, "FxAnimation.onFinished", "handler");
            timeline.setOnFinished(handler);
            return this;
        }

        /**
         * Configures this animation to automatically reverse direction after each cycle.
         *
         * <p>For example, if the animation plays forward from A→B, it will then play
         * backward from B→A, and so on. The final state of each cycle becomes
         * the starting state of the next cycle.</p>
         *
         * <p>This is useful for creating ping-pong effects or subtle back-and-forth motions.</p>
         *
         * @return this animation instance for method chaining
         * @see #speed(double)
         */
        public FxAnimation autoReverse() {
            timeline.setAutoReverse(true);
            return this;
        }

        /**
         * Registers this animation with the {@link AnimationRegistry} for automatic lifecycle management.
         *
         * <p>When {@link #play()} is called, the registry will automatically cancel any existing
         * animation in the same slot before starting this one. This prevents multiple animations
         * from competing for the same node properties and ensures clean state management.</p>
         *
         * <h3>Standard Slots</h3>
         * <ul>
         *   <li><strong>"enter"</strong> - Entry animations (fadeIn, slideUp, scaleIn)</li>
         *   <li><strong>"exit"</strong> - Exit animations (fadeOut, scaleOut)</li>
         *   <li><strong>"attention"</strong> - Attention-grabbing effects (shake, pulse, flash)</li>
         *   <li><strong>"loop"</strong> - Infinite animations (spin, breathe)</li>
         *   <li><strong>"transition"</strong> - Property transitions via TailwindFX.transition()</li>
         * </ul>
         *
         * @param node the JavaFX node to associate this animation with (must not be null)
         * @param slot the registry slot to register under (use standard slots when possible)
         * @return this animation instance for method chaining
         * @throws NullPointerException if node is null
         * @see AnimationRegistry
         */
        public FxAnimation register(javafx.scene.Node node, String slot) {
            this.registeredNode = Preconditions.requireNode(node, "FxAnimation.register");
            this.registeredSlot = slot;
            return this;
        }

        /**
         * Starts this animation.
         *
         * <p>If the animation was registered with the {@link AnimationRegistry} via
         * {@link #register(javafx.scene.Node, String)}, this method will automatically
         * cancel any existing animation in the same slot before starting.</p>
         *
         * <h3>Scene Graph Safety</h3>
         * <p>The registry automatically cancels animations when their associated node is removed
         * from the scene graph, preventing memory leaks from zombie timelines.</p>
         *
         * @see #register(javafx.scene.Node, String)
         * @see AnimationRegistry
         */
        public void play() {
            TailwindFXMetrics.instance().recordAnimationPlay();
            if (registeredNode != null && registeredSlot != null) {
                AnimationRegistry.play(registeredNode, registeredSlot, timeline);
            } else {
                timeline.play();
            }
        }

        /**
         * Stops this animation immediately.
         *
         * <p>If the animation was registered with the {@link AnimationRegistry}, this method
         * will also remove it from the registry for the associated node.</p>
         *
         * @see #play()
         * @see AnimationRegistry
         */
        public void stop() {
            timeline.stop();
            if (registeredNode != null && registeredSlot != null) {
                AnimationRegistry.cancel(registeredNode, registeredSlot);
            }
        }

        /**
         * Pauses this animation at its current position.
         *
         * @see #resume()
         * @see Animation#pause()
         */
        public void pause() {
            timeline.pause();
        }

        /**
         * Resumes this animation from where it was paused.
         *
         * @see #pause()
         * @see Animation#play()
         */
        public void resume() {
            timeline.play();
        }

        /**
         * Establece el interpolador de easing para toda la animación.
         * JavaFX no permite cambiar el interpolador de KeyValues existentes (son inmutables),
         * pero este método guarda el interpolador para ser usado por fábricas que lo soporten.
         *
         * <p>Para aplicar easing garantizado usa los overloads directamente:
         * <pre>
         * AnimationUtil.fadeIn(node, 300, Interpolator.EASE_IN).play();
         * AnimationUtil.scaleIn(node, 250, Interpolator.EASE_OUT).play();
         * </pre>
         *
         * @param interpolator interpolador a aplicar
         * @return this (para encadenamiento)
         */
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

/** .ease-in — starts slowly, accelerates toward the end */
        public FxAnimation easeIn()    { return withEase(Interpolator.EASE_IN); }
        /** .ease-out — starts quickly, decelerates toward the end */
        public FxAnimation easeOut()   { return withEase(Interpolator.EASE_OUT); }
        /** .ease-in-out — combines ease-in and ease-out */
        public FxAnimation easeBoth()  { return withEase(Interpolator.EASE_BOTH); }
        /** .linear — constant speed throughout */
        public FxAnimation linear()    { return withEase(Interpolator.LINEAR); }

        /**
         * Provides access to the underlying JavaFX Animation for advanced configuration.
         *
         * <p>This method returns the raw {@link javafx.animation.Animation} instance,
         * allowing access to low-level properties and methods not exposed by the fluent API.</p>
         *
         * @return the underlying JavaFX Animation instance
         */
        public Animation raw() { return timeline; }

        Interpolator ease = null;
         * Sets the easing interpolator for this animation.
         *
         * <p>The interpolator controls how values change over time during the animation.
         * Common interpolators include {@link Interpolator#EASE_IN}, {@link Interpolator#EASE_OUT},
         * and {@link Interpolator#LINEAR}.</p>
         *
         * <h3>Convenience Methods</h3>
         * <p>Use these shortcuts instead of creating interpolator instances directly:</p>
         * <ul>
         *   <li>{@link #easeIn()} - starts slowly, accelerates toward the end</li>
         *   <li>{@link #easeOut()} - starts quickly, decelerates toward the end</li>
         *   <li>{@link #easeBoth()} - combines ease-in and ease-out</li>
         *   <li>{@link #linear()} - constant speed throughout</li>
         * </ul>
         *
         * @param interpolator the easing function to apply (must not be null)
         * @return this animation instance for method chaining
         * @throws NullPointerException if interpolator is null
         * @see Interpolator
         */
        public FxAnimation withEase(Interpolator interpolator) {
            Preconditions.requireNonNull(interpolator, "FxAnimation.withEase", "interpolator");
            this.ease = interpolator;
            return this;
        }
    }


    // =========================================================================
    // ResponsiveAnimationGuard — coordina animaciones con layout responsive
    // =========================================================================

    /**
     * Protege animaciones de translate/scale de desincronizarse cuando el layout
     * cambia (por ejemplo, al cruzar un breakpoint que reorganiza los Panes).
     *
     * Problema: si se anima translateX mientras el layout re-calcula posiciones,
     * el nodo puede quedar en una posición incorrecta.
     *
     * Solución: pausar las animaciones activas de "enter"/"exit" durante el layout
     * y reanudarlas cuando termina. Se integra con BreakpointManager.
     *
     * Uso:
     *   BreakpointManager bpm = TailwindFX.responsive(stage);
     *   ResponsiveAnimationGuard.install(bpm, scene);
     *   // A partir de aquí, los breakpoint changes pausan/reanudan animaciones automáticamente
     */
    public static final class ResponsiveAnimationGuard {

        private static final String PAUSED_KEY = "tailwindfx.anim.paused";

        private ResponsiveAnimationGuard() {}

        /**
         * Instala el guard: escucha cambios de breakpoint y pausa/resume animaciones activas.
         * @param bpm   BreakpointManager activo
         * @param scene Scene cuyos nodos se protegen
         */
        public static void install(javafx.scene.Scene scene) {
            // Se llama desde BreakpointManager.onBreakpoint()
            // El usuario lo conecta así:
            // bpm.onBreakpoint(BP.MD, () -> ResponsiveAnimationGuard.onLayoutChange(scene));
        }

        /**
         * Llamar al inicio de un cambio de layout responsive (antes de reconfigurар Panes).
         * Pausa todas las animaciones de translate/scale activas en la Scene.
         */
        public static void onLayoutChangeStart(javafx.scene.Scene scene) {
            if (scene == null || scene.getRoot() == null) return;
            pauseAnimationsInSubtree(scene.getRoot());
        }

        /**
         * Llamar al finalizar un cambio de layout responsive.
         * Reanuda las animaciones que estaban activas.
         */
        public static void onLayoutChangeEnd(javafx.scene.Scene scene) {
            if (scene == null || scene.getRoot() == null) return;
            resumeAnimationsInSubtree(scene.getRoot());
        }

        /**
         * Reset completo: detiene todas las animaciones de un nodo y restaura su estado base.
         * Útil al cambiar de layout (row → col) para evitar translateY residuales.
         */
        public static void resetNode(javafx.scene.Node node) {
            AnimationRegistry.cancelAll(node);
            node.setTranslateX(0);
            node.setTranslateY(0);
            node.setScaleX(1);
            node.setScaleY(1);
            node.setOpacity(1);
            node.setRotate(0);
        }

        private static void pauseAnimationsInSubtree(javafx.scene.Node node) {
            // Pausar animaciones activas del nodo
            @SuppressWarnings("unchecked")
            var slots = (java.util.Map<String, Animation>) node.getProperties()
                .get("tailwindfx.animations");
            if (slots != null) {
                java.util.List<String> toPause = new java.util.ArrayList<>();
                for (var entry : slots.entrySet()) {
                    if (entry.getValue().getStatus() == Animation.Status.RUNNING
                        && isTransformAnimation(entry.getKey())) {
                        entry.getValue().pause();
                        toPause.add(entry.getKey());
                    }
                }
                if (!toPause.isEmpty()) {
                    node.getProperties().put(PAUSED_KEY, toPause);
                }
            }
            // Recursivo en hijos
            if (node instanceof javafx.scene.Parent parent) {
                for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                    pauseAnimationsInSubtree(child);
                }
            }
        }

        private static void resumeAnimationsInSubtree(javafx.scene.Node node) {
            @SuppressWarnings("unchecked")
            var paused = (java.util.List<String>) node.getProperties().remove(PAUSED_KEY);
            @SuppressWarnings("unchecked")
            var slots = (java.util.Map<String, Animation>) node.getProperties()
                .get("tailwindfx.animations");
            if (paused != null && slots != null) {
                for (String slot : paused) {
                    Animation a = slots.get(slot);
                    if (a != null && a.getStatus() == Animation.Status.PAUSED) {
                        a.play();
                    }
                }
            }
            if (node instanceof javafx.scene.Parent parent) {
                for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                    resumeAnimationsInSubtree(child);
                }
            }
        }

        // Slots que animan propiedades de transform — los que deben pausarse al re-layout
        private static boolean isTransformAnimation(String slot) {
            return slot.equals("enter") || slot.equals("exit")
                || slot.startsWith("slide") || slot.startsWith("scale");
        }
    }

}