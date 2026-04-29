package io.github.yasmramos.tailwindfx.breakpoint;

import io.github.yasmramos.tailwindfx.TailwindFX;
import io.github.yasmramos.tailwindfx.breakpoint.BreakpointManager;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tests for {@link BreakpointManager} — breakpoint detection, callbacks, custom
 * breakpoints, orientation, and detach.
 */
public final class BreakpointManagerTest {

    private BreakpointManagerTest() {
    }

    private static int passed = 0, failed = 0;

    static void ok(String l) {
        System.out.printf("  ✅ %s%n", l);
        passed++;
    }

    static void fail(String l, String m) {
        System.out.printf("  ❌ %s — %s%n", l, m);
        failed++;
    }

    static void check(String l, boolean v) {
        if (v) {
            ok(l);
        } else {
            fail(l, "false");
    
        }}

    static void eq(String l, Object e, Object a) {
        if (e == null ? a == null : e.equals(a)) {
            ok(l); 
        }else {
            fail(l, "expected <" + e + "> got <" + a + ">");
        }
    }

    static void throws_(String l, Class<? extends Throwable> t, Runnable r) {
        try {
            r.run();
            fail(l, "no throw");
        } catch (Throwable e) {
            if (t.isInstance(e)) {
                ok(l);
            } else {
                fail(l, e.getClass().getSimpleName());
        
            }}
    }

    static void runFx(Runnable w) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                w.run();
            } catch (Throwable t) {
                err.set(t);
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(3, TimeUnit.SECONDS)) {
            throw new RuntimeException("timeout");
        }
        if (err.get() != null) {
            throw new RuntimeException(err.get());
        }
    }

    public static boolean runAll() throws Exception {
        passed = 0;
        failed = 0;
        System.out.println("\n── BreakpointManager ──");

        testForWidth();
        testForWidthExact();
        testForWidthZero();
        testBreakpointIs();
        testBreakpointBelow();
        testBreakpointEnum();
        testCustomBuilderGuards();
        testCustomBreakpointDetection();
        testOnBreakpointRunnable();
        testOnBreakpointConsumer();
        testOnBreakpointBothDirections();
        testWithOrientationNoThrow();
        testCurrentInitialBreakpoint();
        testDetachStopsListening();
        testCssClassInjected();

        System.out.printf("  %d passed, %d failed%n", passed, failed);
        return failed == 0;
    }

    // ── Breakpoint.forWidth() ─────────────────────────────────────────────
    static void testForWidth() {
        // Standard Tailwind breakpoints:
        // XS < 640, SM >= 640, MD >= 768, LG >= 1024, XL >= 1280, XXL >= 1536
        eq("320 → XS", BreakpointManager.Breakpoint.XS,
                BreakpointManager.Breakpoint.forWidth(320));
        eq("640 → SM", BreakpointManager.Breakpoint.SM,
                BreakpointManager.Breakpoint.forWidth(640));
        eq("768 → MD", BreakpointManager.Breakpoint.MD,
                BreakpointManager.Breakpoint.forWidth(768));
        eq("1024 → LG", BreakpointManager.Breakpoint.LG,
                BreakpointManager.Breakpoint.forWidth(1024));
        eq("1280 → XL", BreakpointManager.Breakpoint.XL,
                BreakpointManager.Breakpoint.forWidth(1280));
        eq("1536 → XXL", BreakpointManager.Breakpoint.XXL,
                BreakpointManager.Breakpoint.forWidth(1536));
    }

    static void testForWidthExact() {
        // Exact boundary: 639 = XS, 640 = SM
        eq("639 → XS", BreakpointManager.Breakpoint.XS,
                BreakpointManager.Breakpoint.forWidth(639));
        eq("640 → SM", BreakpointManager.Breakpoint.SM,
                BreakpointManager.Breakpoint.forWidth(640));
        eq("767 → XS or SM", true,
                BreakpointManager.Breakpoint.forWidth(767).minWidth < 768);
        eq("768 → MD exactly", BreakpointManager.Breakpoint.MD,
                BreakpointManager.Breakpoint.forWidth(768));
    }

    static void testForWidthZero() {
        eq("0 → XS", BreakpointManager.Breakpoint.XS,
                BreakpointManager.Breakpoint.forWidth(0));
        eq("-1 → XS", BreakpointManager.Breakpoint.XS,
                BreakpointManager.Breakpoint.forWidth(-1));
    }

    // ── Breakpoint.is() / below() ─────────────────────────────────────────
    static void testBreakpointIs() throws Exception {
        runFx(() -> {
            Stage stage = new Stage();
            stage.setWidth(900); // LG range
            BreakpointManager bpm = TailwindFX.responsive(stage);
            check("is(LG) at 900", bpm.is(BreakpointManager.Breakpoint.LG));
            check("is(SM) at 900", bpm.is(BreakpointManager.Breakpoint.SM));
            check("is(MD) at 900", bpm.is(BreakpointManager.Breakpoint.MD));
            bpm.detach();
        });
    }

    static void testBreakpointBelow() throws Exception {
        runFx(() -> {
            Stage stage = new Stage();
            stage.setWidth(700); // between SM and MD
            BreakpointManager bpm = TailwindFX.responsive(stage);
            check("below(MD) at 700", bpm.below(BreakpointManager.Breakpoint.MD));
            check("not below(SM) at 700", !bpm.below(BreakpointManager.Breakpoint.SM));
            bpm.detach();
        });
    }

    // ── Breakpoint enum ────────────────────────────────────────────────────
    static void testBreakpointEnum() {
        // Verify ordering: XS < SM < MD < LG < XL < XXL
        BreakpointManager.Breakpoint[] bps = BreakpointManager.Breakpoint.values();
        for (int i = 1; i < bps.length; i++) {
            check("ordered: " + bps[i - 1] + " < " + bps[i],
                    bps[i - 1].minWidth < bps[i].minWidth);
        }
        // Each has a non-blank CSS class
        for (BreakpointManager.Breakpoint bp : bps) {
            check(bp + ".cssClass not blank",
                    bp.cssClass != null && !bp.cssClass.isBlank());
        }
    }

    // ── CustomBuilder ──────────────────────────────────────────────────────
    static void testCustomBuilderGuards() throws Exception {
        runFx(() -> {
            throws_("add(null, px)", IllegalArgumentException.class,
                    () -> BreakpointManager.custom().add(null, 600));
            throws_("add(blank, px)", IllegalArgumentException.class,
                    () -> BreakpointManager.custom().add("  ", 600));
            throws_("add(cls, -1)", IllegalArgumentException.class,
                    () -> BreakpointManager.custom().add("tablet", -1));
            throws_("attach(null)", IllegalArgumentException.class,
                    () -> BreakpointManager.custom().add("tablet", 700).attach(null));
        });
    }

    static void testCustomBreakpointDetection() throws Exception {
        runFx(() -> {
            Stage stage = new Stage();
            stage.setWidth(750);
            BreakpointManager bpm = BreakpointManager.custom()
                    .add("mobile", 0)
                    .add("tablet", 600)
                    .add("desktop", 1000)
                    .attach(stage);

            check("tablet active at 750", bpm.current().cssClass.equals("tablet"));
            check("not desktop at 750", !bpm.current().cssClass.equals("desktop"));
            bpm.detach();
        });
    }

    // ── Callbacks ───────────────────────────────────────────────────────────
    static void testOnBreakpointRunnable() throws Exception {
        CountDownLatch callbackLatch = new CountDownLatch(1);
        runFx(() -> {
            Stage stage = new Stage();
            stage.setWidth(400); // starts XS
            BreakpointManager bpm = TailwindFX.responsive(stage);

            bpm.onBreakpoint(BreakpointManager.Breakpoint.SM, () -> callbackLatch.countDown());

            // Simulate resize to SM
            stage.setWidth(700);
        });
        boolean fired = callbackLatch.await(2, TimeUnit.SECONDS);
        check("SM callback fires on resize", fired);
    }

    static void testOnBreakpointConsumer() throws Exception {
        AtomicBoolean lastActive = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        runFx(() -> {
            Stage stage = new Stage();
            stage.setWidth(400);
            BreakpointManager bpm = TailwindFX.responsive(stage);
            bpm.onBreakpoint(BreakpointManager.Breakpoint.MD, active -> {
                lastActive.set(active);
                latch.countDown();
            });
            stage.setWidth(900); // crosses MD
        });
        latch.await(2, TimeUnit.SECONDS);
        check("consumer receives active=true", lastActive.get());
    }

    static void testOnBreakpointBothDirections() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(2);
        runFx(() -> {
            Stage stage = new Stage();
            stage.setWidth(400);
            BreakpointManager bpm = TailwindFX.responsive(stage);
            bpm.onBreakpoint(BreakpointManager.Breakpoint.MD, active -> {
                callCount.incrementAndGet();
                latch.countDown();
            });
            stage.setWidth(900); // enter MD
            Platform.runLater(() -> stage.setWidth(400)); // exit MD
        });
        latch.await(3, TimeUnit.SECONDS);
        check("callback fires both enter+exit", callCount.get() >= 2);
    }

    // ── withOrientation ────────────────────────────────────────────────────
    static void testWithOrientationNoThrow() throws Exception {
        runFx(() -> {
            Stage stage = new Stage();
            try {
                BreakpointManager bpm = TailwindFX.responsive(stage).withOrientation();
                ok("withOrientation() no throw");
                bpm.detach();
            } catch (Exception e) {
                fail("withOrientation()", e.getMessage());
            }
        });
    }

    // ── current() at install ───────────────────────────────────────────────
    static void testCurrentInitialBreakpoint() throws Exception {
        runFx(() -> {
            Stage stage = new Stage();
            stage.setWidth(1100);
            BreakpointManager bpm = TailwindFX.responsive(stage);
            check("initial current not null", bpm.current() != null);
            check("1100px → LG range",
                    bpm.current().minWidth >= BreakpointManager.Breakpoint.LG.minWidth);
            bpm.detach();
        });
    }

    // ── detach ─────────────────────────────────────────────────────────────
    static void testDetachStopsListening() throws Exception {
        AtomicInteger count = new AtomicInteger(0);
        runFx(() -> {
            Stage stage = new Stage();
            stage.setWidth(400);
            BreakpointManager bpm = TailwindFX.responsive(stage);
            bpm.onBreakpoint(BreakpointManager.Breakpoint.SM, () -> count.incrementAndGet());
            bpm.detach();
            // After detach, resizing should NOT fire callback
            stage.setWidth(700);
        });
        Thread.sleep(200); // give FX thread time to process
        eq("no callback after detach", 0, count.get());
    }

    // ── CSS class injection ─────────────────────────────────────────────────
    static void testCssClassInjected() throws Exception {
        runFx(() -> {
            Stage stage = new Stage();
            StackPane root = new StackPane();
            Scene scene = new Scene(root, 900, 600);
            stage.setScene(scene);
            stage.setWidth(900);

            TailwindFX.install(scene, stage);
            // After install, root should have the current breakpoint CSS class
            // bp-lg (or similar) depending on width
            boolean hasAnyBpClass = root.getStyleClass().stream()
                    .anyMatch(c -> c.startsWith("bp-"));
            check("root has bp-* class after install", hasAnyBpClass);
        });
    }
}
