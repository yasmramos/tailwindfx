package tailwindfx;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.scene.layout.Region;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tests for {@link AnimationUtil} — requires JavaFX Application Thread.
 *
 * <p>
 * Run via {@link #runAll()} after the JavaFX platform is initialized. Each test
 * runs on the FX thread via {@code runFx()} and blocks until done.
 */
public final class AnimationUtilTest {

    private AnimationUtilTest() {
    }

    private static int passed = 0, failed = 0;

    static void ok(String label) {
        System.out.printf("  ✅ %s%n", label);
        passed++;
    }

    static void fail(String label, String msg) {
        System.out.printf("  ❌ %s — %s%n", label, msg);
        failed++;
    }

    static void check(String label, boolean v) {
        if (v) {
            ok(label);
        } else {
            fail(label, "expected true");
    
        }}

    static void eq(String label, Object e, Object a) {
        if (e == null ? a == null : e.equals(a)) {
            ok(label); 
        }else {
            fail(label, "expected <" + e + "> got <" + a + ">");
        }
    }

    static void approx(String label, double e, double a) {
        if (Math.abs(e - a) <= 0.5) {
            ok(label);
        } else {
            fail(label, "expected ≈" + e + " got " + a);
        }
    }

    static void throws_(String label, Class<? extends Throwable> type, Runnable r) {
        try {
            r.run();
            fail(label, "no exception thrown");
        } catch (Throwable t) {
            if (type.isInstance(t)) {
                ok(label); 
            }else {
                fail(label, "expected " + type.getSimpleName() + " got " + t.getClass().getSimpleName());
            }
        }
    }

    /**
     * Runs work on FX thread and blocks until done (max 3s).
     */
    static void runFx(Runnable work) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                work.run();
            } catch (Throwable t) {
                err.set(t);
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(3, TimeUnit.SECONDS)) {
            throw new RuntimeException("FX test timed out");
        }
        if (err.get() != null) {
            throw new RuntimeException(err.get());
        }
    }

    public static boolean runAll() throws Exception {
        passed = 0;
        failed = 0;
        System.out.println("\n── AnimationUtil ──");

        testNullGuards();
        testFadeInCreatesTimeline();
        testFadeInWithInterpolator();
        testFadeInDurationGuard();
        testFadeOutCreatesTimeline();
        testSlideUpCreatesTimeline();
        testScaleInCreatesTimeline();
        testScaleInWithInterpolator();
        testShakeCreatesTimeline();
        testBounceCreatesTimeline();
        testPulseIsInfinite();
        testSpinIsInfinite();
        testBreatheIsInfinite();
        testChainSequential();
        testParallelTransition();
        testDelayCreatesTimeline();
        testFxAnimationSpeed();
        testFxAnimationCycleCount();
        testFxAnimationAutoReverse();
        testFxAnimationOnFinished();
        testFxAnimationEaseIn();
        testRegistrySlotIsolation();
        testRegistryReplacesCancels();
        testOnHoverScaleNullGuard();
        testOnHoverScaleZeroGuard();
        testRemoveHoverEffectsNoop();
        testResponsiveGuardResetNode();

        System.out.printf("  %d passed, %d failed%n", passed, failed);
        return failed == 0;
    }

    // ── Guard tests ────────────────────────────────────────────────────
    static void testNullGuards() {
        throws_("fadeIn(null)", IllegalArgumentException.class,
                () -> AnimationUtil.fadeIn(null));
        throws_("fadeOut(null)", IllegalArgumentException.class,
                () -> AnimationUtil.fadeOut(null));
        throws_("shake(null)", IllegalArgumentException.class,
                () -> AnimationUtil.shake(null));
        throws_("spin(null)", IllegalArgumentException.class,
                () -> AnimationUtil.spin(null));
        throws_("onHoverScale(null)", IllegalArgumentException.class,
                () -> AnimationUtil.onHoverScale(null, 1.05));
        throws_("fadeIn(node, 0ms)", IllegalArgumentException.class,
                () -> AnimationUtil.fadeIn(new Region(), 0));
        throws_("fadeIn(node, -1ms)", IllegalArgumentException.class,
                () -> AnimationUtil.fadeIn(new Region(), -1));
    }

    static void testFadeInCreatesTimeline() throws Exception {
        runFx(() -> {
            Region n = new Region();
            AnimationUtil.FxAnimation anim = AnimationUtil.fadeIn(n, 100);
            check("fadeIn returns FxAnimation", anim != null);
            check("fadeIn raw() is Animation", anim.raw() instanceof Animation);
            check("node opacity reset to 0", n.getOpacity() == 0.0);
        });
    }

    static void testFadeInWithInterpolator() throws Exception {
        runFx(() -> {
            Region n = new Region();
            AnimationUtil.FxAnimation anim = AnimationUtil.fadeIn(n, 200, Interpolator.LINEAR);
            check("fadeIn(interpolator) non-null", anim != null);
            throws_("fadeIn(null interpolator)", IllegalArgumentException.class,
                    () -> AnimationUtil.fadeIn(new Region(), 100, null));
        });
    }

    static void testFadeInDurationGuard() {
        throws_("fadeIn(-1ms)", IllegalArgumentException.class,
                () -> AnimationUtil.fadeIn(new Region(), -1));
    }

    static void testFadeOutCreatesTimeline() throws Exception {
        runFx(() -> {
            Region n = new Region();
            n.setOpacity(1.0);
            AnimationUtil.FxAnimation anim = AnimationUtil.fadeOut(n, 100);
            check("fadeOut non-null", anim != null);
        });
    }

    static void testSlideUpCreatesTimeline() throws Exception {
        runFx(() -> {
            Region n = new Region();
            AnimationUtil.FxAnimation anim = AnimationUtil.slideUp(n);
            check("slideUp non-null", anim != null);
            check("opacity reset to 0", n.getOpacity() == 0.0);
        });
    }

    static void testScaleInCreatesTimeline() throws Exception {
        runFx(() -> {
            Region n = new Region();
            AnimationUtil.FxAnimation anim = AnimationUtil.scaleIn(n);
            check("scaleIn non-null", anim != null);
            approx("scaleX reset", 0.85, n.getScaleX());
        });
    }

    static void testScaleInWithInterpolator() throws Exception {
        runFx(() -> {
            Region n = new Region();
            AnimationUtil.FxAnimation a = AnimationUtil.scaleIn(n, 150, Interpolator.EASE_IN);
            check("scaleIn(interpolator) non-null", a != null);
        });
    }

    static void testShakeCreatesTimeline() throws Exception {
        runFx(() -> {
            Region n = new Region();
            AnimationUtil.FxAnimation a = AnimationUtil.shake(n);
            check("shake non-null", a != null);
        });
    }

    static void testBounceCreatesTimeline() throws Exception {
        runFx(() -> {
            Region n = new Region();
            AnimationUtil.FxAnimation a = AnimationUtil.bounce(n);
            check("bounce non-null", a != null);
        });
    }

    static void testPulseIsInfinite() throws Exception {
        runFx(() -> {
            Region n = new Region();
            AnimationUtil.FxAnimation a = AnimationUtil.pulse(n);
            eq("pulse cycleCount INDEFINITE",
                    Animation.INDEFINITE, a.raw().getCycleCount());
        });
    }

    static void testSpinIsInfinite() throws Exception {
        runFx(() -> {
            Region n = new Region();
            AnimationUtil.FxAnimation a = AnimationUtil.spin(n);
            eq("spin cycleCount INDEFINITE",
                    Animation.INDEFINITE, a.raw().getCycleCount());
        });
    }

    static void testBreatheIsInfinite() throws Exception {
        runFx(() -> {
            Region n = new Region();
            AnimationUtil.FxAnimation a = AnimationUtil.breathe(n);
            eq("breathe cycleCount INDEFINITE",
                    Animation.INDEFINITE, a.raw().getCycleCount());
        });
    }

    // ── Composition ────────────────────────────────────────────────────
    static void testChainSequential() throws Exception {
        runFx(() -> {
            Region n = new Region();
            AnimationUtil.FxAnimation a1 = AnimationUtil.fadeIn(n, 50);
            AnimationUtil.FxAnimation a2 = AnimationUtil.fadeOut(n, 50);
            AnimationUtil.FxAnimation chain = AnimationUtil.chain(a1, a2);
            check("chain non-null", chain != null);
            check("chain raw non-null", chain.raw() != null);
        });
    }

    static void testParallelTransition() throws Exception {
        runFx(() -> {
            Region n = new Region();
            AnimationUtil.FxAnimation a1 = AnimationUtil.fadeIn(n, 50);
            AnimationUtil.FxAnimation a2 = AnimationUtil.scaleIn(n, 50);
            AnimationUtil.FxAnimation par = AnimationUtil.parallel(a1, a2);
            check("parallel non-null", par != null);
        });
    }

    static void testDelayCreatesTimeline() throws Exception {
        runFx(() -> {
            AnimationUtil.FxAnimation d = AnimationUtil.delay(200);
            check("delay non-null", d != null);
        });
    }

    // ── FxAnimation fluent API ──────────────────────────────────────────
    static void testFxAnimationSpeed() throws Exception {
        runFx(() -> {
            Region n = new Region();
            AnimationUtil.FxAnimation a = AnimationUtil.fadeIn(n, 200).speed(2.0);
            approx("speed=2.0", 2.0, a.raw().getRate());
        });
    }

    static void testFxAnimationCycleCount() throws Exception {
        runFx(() -> {
            Region n = new Region();
            AnimationUtil.FxAnimation a = AnimationUtil.fadeIn(n, 200).cycleCount(3);
            eq("cycleCount=3", 3, a.raw().getCycleCount());
        });
    }

    static void testFxAnimationAutoReverse() throws Exception {
        runFx(() -> {
            Region n = new Region();
            AnimationUtil.FxAnimation a = AnimationUtil.fadeIn(n, 200).autoReverse();
            check("autoReverse=true", a.raw().isAutoReverse());
        });
    }

    static void testFxAnimationOnFinished() throws Exception {
        runFx(() -> {
            Region n = new Region();
            AtomicBoolean fired = new AtomicBoolean(false);
            AnimationUtil.FxAnimation a = AnimationUtil.fadeIn(n, 1)
                    .onFinished(e -> fired.set(true));
            check("onFinished set", a.raw().getOnFinished() != null);
        });
    }

    static void testFxAnimationEaseIn() throws Exception {
        runFx(() -> {
            Region n = new Region();
            AnimationUtil.FxAnimation a = AnimationUtil.fadeIn(n, 200).easeIn();
            check("easeIn returns self", a != null);
        });
    }

    // ── AnimationRegistry ────────────────────────────────────────────────
    static void testRegistrySlotIsolation() throws Exception {
        runFx(() -> {
            Region n = new Region();
            AnimationUtil.FxAnimation enter = AnimationUtil.fadeIn(n, 50);
            AnimationUtil.FxAnimation loop = AnimationUtil.spin(n).loop();
            // Different slots — both should coexist
            AnimationUtil.AnimationRegistry.play(n, "enter", enter.raw());
            AnimationUtil.AnimationRegistry.play(n, "loop", loop.raw());
            check("enter active", AnimationUtil.AnimationRegistry.isActive(n, "enter"));
            check("loop active", AnimationUtil.AnimationRegistry.isActive(n, "loop"));
            AnimationUtil.AnimationRegistry.cancelAll(n);
        });
    }

    static void testRegistryReplacesCancels() throws Exception {
        runFx(() -> {
            Region n = new Region();
            javafx.animation.Timeline t1 = new javafx.animation.Timeline();
            javafx.animation.Timeline t2 = new javafx.animation.Timeline();
            AnimationUtil.AnimationRegistry.play(n, "enter", t1);
            check("t1 running", t1.getStatus() == Animation.Status.RUNNING);
            AnimationUtil.AnimationRegistry.play(n, "enter", t2);
            check("t1 stopped after replace", t1.getStatus() == Animation.Status.STOPPED);
            check("t2 running", t2.getStatus() == Animation.Status.RUNNING);
            AnimationUtil.AnimationRegistry.cancelAll(n);
        });
    }

    // ── Hover effects ─────────────────────────────────────────────────────
    static void testOnHoverScaleNullGuard() {
        throws_("onHoverScale(null)", IllegalArgumentException.class,
                () -> AnimationUtil.onHoverScale(null, 1.05));
    }

    static void testOnHoverScaleZeroGuard() {
        throws_("onHoverScale(node, 0)", IllegalArgumentException.class,
                () -> AnimationUtil.onHoverScale(new Region(), 0));
    }

    static void testRemoveHoverEffectsNoop() throws Exception {
        runFx(() -> {
            Region n = new Region();
            // removeHoverEffects on a node with no hover effects should not throw
            AnimationUtil.removeHoverEffects(n);
            ok("removeHoverEffects on clean node: no throw");
        });
    }

    // ── ResponsiveAnimationGuard ──────────────────────────────────────────
    static void testResponsiveGuardResetNode() throws Exception {
        runFx(() -> {
            Region n = new Region();
            n.setTranslateX(20);
            n.setTranslateY(-10);
            n.setScaleX(1.5);
            n.setOpacity(0.3);
            AnimationUtil.ResponsiveAnimationGuard.resetNode(n);
            approx("translateX=0", 0, n.getTranslateX());
            approx("translateY=0", 0, n.getTranslateY());
            approx("scaleX=1", 1, n.getScaleX());
            approx("opacity=1", 1, n.getOpacity());
        });
    }
}
