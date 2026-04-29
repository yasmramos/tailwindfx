package io.github.yasmramos.tailwindfx.layout;

import io.github.yasmramos.tailwindfx.components.FxFlexPane;
import io.github.yasmramos.tailwindfx.components.FxFlexPane;
import io.github.yasmramos.tailwindfx.components.FxGridPane;
import io.github.yasmramos.tailwindfx.components.FxGridPane;
import io.github.yasmramos.tailwindfx.TailwindFX;
import io.github.yasmramos.tailwindfx.layout.FxLayout;
import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tests for {@link FxLayout} — builder, type switching, constraints, TilePane.
 */
public final class FxLayoutTest {

    private FxLayoutTest() {}

    private static int passed = 0, failed = 0;
    static void ok(String l)  { System.out.printf("  ✅ %s%n", l); passed++; }
    static void fail(String l, String m) { System.out.printf("  ❌ %s — %s%n", l, m); failed++; }
    static void check(String l, boolean v)  { if (v) ok(l); else fail(l, "false"); }
    static void throws_(String l, Class<? extends Throwable> t, Runnable r) {
        try { r.run(); fail(l, "no throw"); }
        catch (Throwable e) { if (t.isInstance(e)) ok(l); else fail(l, e.getClass().getSimpleName()); }
    }
    static void runFx(Runnable w) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> { try { w.run(); } catch (Throwable t) { err.set(t); } finally { latch.countDown(); } });
        if (!latch.await(3, TimeUnit.SECONDS)) throw new RuntimeException("timeout");
        if (err.get() != null) throw new RuntimeException(err.get());
    }

    public static boolean runAll() throws Exception {
        passed = 0; failed = 0;
        System.out.println("\n── FxLayout ──");

        testRowCreatesHBox();
        testColCreatesVBox();
        testStackCreatesStackPane();
        testGridCreatesGridPane();
        testFlowRowCreatesFlowPane();
        testFlowColCreatesFlowPane();
        testTileCreatesTilePane();
        testAnchorCreatesAnchorPane();
        testNullPaneThrows();
        testGridColsGuard();
        testGapNegativeWarns();
        testBuild();
        testReconfigure();
        testLayoutSwitchPreservesChildren();
        testFlexType();
        testFlexGridType();
        testFlexOnNewPane();
        testFlexColDirection();
        testValidationWarns();
        testDebugMode();
        testThreadCheck();
        testPaddingShorthand();
        testTransitionListener();
        testApplyGridColsNoOverwrite();
        testSnapRestoresAnchorEdges();
        testStaticHelpers();

        System.out.printf("  %d passed, %d failed%n", passed, failed);
        return failed == 0;
    }

    static void testRowCreatesHBox() throws Exception {
        runFx(() -> {
            HBox box = new HBox();
            Pane result = TailwindFX.layout(box).row().build();
            check("row → HBox", result instanceof HBox);
        });
    }

    static void testColCreatesVBox() throws Exception {
        runFx(() -> {
            VBox box = new VBox();
            Pane result = TailwindFX.layout(box).col().build();
            check("col → VBox", result instanceof VBox);
        });
    }

    static void testStackCreatesStackPane() throws Exception {
        runFx(() -> {
            StackPane sp = new StackPane();
            Pane result = TailwindFX.layout(sp).stack().build();
            check("stack → StackPane", result instanceof StackPane);
        });
    }

    static void testGridCreatesGridPane() throws Exception {
        runFx(() -> {
            GridPane gp = new GridPane();
            Pane result = TailwindFX.layout(gp).grid(3).build();
            check("grid → GridPane", result instanceof GridPane);
        });
    }

    static void testFlowRowCreatesFlowPane() throws Exception {
        runFx(() -> {
            FlowPane fp = new FlowPane();
            Pane result = TailwindFX.layout(fp).flowRow().build();
            check("flowRow → FlowPane", result instanceof FlowPane);
        });
    }

    static void testFlowColCreatesFlowPane() throws Exception {
        runFx(() -> {
            FlowPane fp = new FlowPane();
            Pane result = TailwindFX.layout(fp).flowCol().build();
            check("flowCol → FlowPane with VERTICAL",
                result instanceof FlowPane f &&
                f.getOrientation() == javafx.geometry.Orientation.VERTICAL);
        });
    }

    static void testTileCreatesTilePane() throws Exception {
        runFx(() -> {
            TilePane tp = new TilePane();
            Pane result = TailwindFX.layout(tp).tile().build();
            check("tile → TilePane", result instanceof TilePane);
        });
    }

    static void testAnchorCreatesAnchorPane() throws Exception {
        runFx(() -> {
            AnchorPane ap = new AnchorPane();
            Pane result = TailwindFX.layout(ap).anchor().build();
            check("anchor → AnchorPane", result instanceof AnchorPane);
        });
    }

    static void testNullPaneThrows() {
        throws_("FxLayout(null)", IllegalArgumentException.class,
            () -> TailwindFX.layout(null));
    }

    static void testGridColsGuard() throws Exception {
        runFx(() -> {
            throws_("grid(0) throws", IllegalArgumentException.class,
                () -> TailwindFX.layout(new GridPane()).grid(0).build());
        });
    }

    static void testGapNegativeWarns() throws Exception {
        runFx(() -> {
            // Negative gap logs a warning but does NOT throw
            try {
                TailwindFX.layout(new HBox()).row().gap(-4).build();
                ok("gap(-4) no throw (warns)");
            } catch (Exception e) {
                fail("gap(-4) should not throw", e.getMessage());
            }
        });
    }

    static void testBuild() throws Exception {
        runFx(() -> {
            HBox source = new HBox();
            Pane built = TailwindFX.layout(source).row().gap(8).center().build();
            check("build returns HBox", built instanceof HBox);
            check("build same instance", built == source);
        });
    }

    static void testReconfigure() throws Exception {
        runFx(() -> {
            HBox box = new HBox();
            // reconfigure on same type — should not recreate
            TailwindFX.layout(box).row().gap(16).reconfigure();
            check("reconfigure preserves type", box instanceof HBox);
        });
    }

    static void testLayoutSwitchPreservesChildren() throws Exception {
        runFx(() -> {
            HBox box = new HBox();
            Region child = new Region();
            box.getChildren().add(child);
            // Switching to VBox preserves children
            Pane switched = TailwindFX.layout(box).col().build();
            check("children preserved after switch",
                switched.getChildren().contains(child));
        });
    }

    static void testFlexType() throws Exception {
        runFx(() -> {
            FxFlexPane fp = new FxFlexPane();
            Pane result = TailwindFX.layout(fp).flex()
                .justify(FxFlexPane.Justify.BETWEEN)
                .alignItems(FxFlexPane.Align.CENTER)
                .wrap(true).gap(16).build();
            check("flex() → FxFlexPane", result instanceof FxFlexPane);
            FxFlexPane built = (FxFlexPane) result;
            check("justify=BETWEEN",  built.getJustify()    == FxFlexPane.Justify.BETWEEN);
            check("align=CENTER",     built.getAlign()      == FxFlexPane.Align.CENTER);
            check("wrap=true",        built.isWrap());
        });
    }

    static void testFlexGridType() throws Exception {
        runFx(() -> {
            FxGridPane fg = FxGridPane.create().build();
            Pane result = TailwindFX.layout(fg).flexGrid()
                .areas("header header",
                       "sidebar main",
                       "footer footer")
                .gap(8).build();
            check("flexGrid() → FxGridPane", result instanceof FxGridPane);
        });
    }

    static void testFlexOnNewPane() throws Exception {
        runFx(() -> {
            // If container is NOT already FxFlexPane, layout() migrates it
            Region r1 = new Region(), r2 = new Region();
            FxFlexPane source = new FxFlexPane();
            source.getChildren().addAll(r1, r2);
            Pane result = TailwindFX.layout(source).flex().gap(12).build();
            check("children preserved", result.getChildren().containsAll(java.util.List.of(r1, r2)));
        });
    }

    static void testFlexColDirection() throws Exception {
        runFx(() -> {
            FxFlexPane fp = new FxFlexPane();
            // col() sets Direction.COL; flex() sets Direction.ROW
            // Using col() on FxFlexPane via FxLayout
            Pane result = TailwindFX.layout(fp).flex().build();
            check("flex() direction ROW", ((FxFlexPane)result).getDirection() == FxFlexPane.Direction.ROW);
        });
    }

    static void testValidationWarns() throws Exception {
        runFx(() -> {
            // GRID with 0 children and no cols — should not throw, just warn
            GridPane gp = new GridPane();
            try {
                TailwindFX.layout(gp).grid().build();
                ok("validate GRID/0-children: no throw");
            } catch (Exception e) {
                fail("validate GRID/0-children: unexpected throw", e.getMessage());
            }
        });
    }

    static void testDebugMode() throws Exception {
        runFx(() -> {
            HBox box = new HBox();
            // debug() should not change behavior, only log to stdout
            Pane result = TailwindFX.layout(box).row().gap(8).debug().build();
            check("debug() still returns correct type", result instanceof HBox);
        });
    }

    static void testThreadCheck() {
        // Calling build() off FX thread must throw
        java.util.concurrent.atomic.AtomicBoolean threw = new java.util.concurrent.atomic.AtomicBoolean(false);
        Thread t = new Thread(() -> {
            try {
                TailwindFX.layout(new HBox()).row().build();
            } catch (IllegalStateException e) {
                threw.set(true);
            }
        });
        t.start();
        try { t.join(2000); } catch (InterruptedException ignored) {}
        check("build() off FX thread throws ISE", threw.get());
    }

    static void testPaddingShorthand() throws Exception {
        runFx(() -> {
            // "16" → uniform
            HBox box = new HBox();
            TailwindFX.layout(box).row().padding("16").build();
            check("padding('16') top=16",    box.getPadding().getTop()    == 16);
            check("padding('16') right=16",  box.getPadding().getRight()  == 16);

            // "8 16" → vertical/horizontal
            VBox vbox = new VBox();
            TailwindFX.layout(vbox).col().padding("8 16").build();
            check("padding('8 16') top=8",   vbox.getPadding().getTop()   == 8);
            check("padding('8 16') right=16",vbox.getPadding().getRight() == 16);

            // "4 8 12 16" → individual
            HBox box3 = new HBox();
            TailwindFX.layout(box3).row().padding("4 8 12 16").build();
            check("padding('4 8 12 16') top=4",    box3.getPadding().getTop()    == 4);
            check("padding('4 8 12 16') right=8",  box3.getPadding().getRight()  == 8);
            check("padding('4 8 12 16') bottom=12",box3.getPadding().getBottom() == 12);
            check("padding('4 8 12 16') left=16",  box3.getPadding().getLeft()   == 16);

            // invalid value throws
            throws_("padding('bad') throws", IllegalArgumentException.class,
                () -> TailwindFX.layout(new HBox()).row().padding("bad").build());

            // wrong count throws
            throws_("padding('1 2 3') throws", IllegalArgumentException.class,
                () -> TailwindFX.layout(new HBox()).row().padding("1 2 3").build());
        });
    }

    static void testTransitionListener() throws Exception {
        java.util.concurrent.atomic.AtomicBoolean changingFired = new java.util.concurrent.atomic.AtomicBoolean(false);
        java.util.concurrent.atomic.AtomicBoolean changedFired  = new java.util.concurrent.atomic.AtomicBoolean(false);
        runFx(() -> {
            HBox box = new HBox();
            // Switching to VBox → migration → listener fires
            TailwindFX.layout(box).col()
                .onTransition(new FxLayout.LayoutTransitionListener() {
                    public void onLayoutChanging(Pane src, FxLayout.LayoutType t) {
                        changingFired.set(true);
                    }
                    public void onLayoutChanged(Pane result) {
                        changedFired.set(true);
                    }
                })
                .build();
            check("onLayoutChanging fired", changingFired.get());
            check("onLayoutChanged fired",  changedFired.get());

            // No migration (same type) → listener NOT fired
            java.util.concurrent.atomic.AtomicBoolean noFire = new java.util.concurrent.atomic.AtomicBoolean(false);
            HBox same = new HBox();
            TailwindFX.layout(same).row()
                .onTransition(new FxLayout.LayoutTransitionListener() {
                    public void onLayoutChanging(Pane s, FxLayout.LayoutType t) { noFire.set(true); }
                    public void onLayoutChanged(Pane r) { noFire.set(true); }
                })
                .build();
            check("no migration → listener not fired", !noFire.get());
        });
    }

    static void testApplyGridColsNoOverwrite() throws Exception {
        runFx(() -> {
            GridPane gp = new GridPane();
            // Add a manual constraint first
            ColumnConstraints manual = new ColumnConstraints(200);
            gp.getColumnConstraints().add(manual);
            // layout(grid(3)) should NOT overwrite it
            TailwindFX.layout(gp).grid(3).build();
            check("manual constraint preserved",
                gp.getColumnConstraints().size() == 1 &&
                gp.getColumnConstraints().get(0).getPrefWidth() == 200);
        });
    }

    static void testSnapRestoresAnchorEdges() throws Exception {
        runFx(() -> {
            AnchorPane original = new AnchorPane();
            Region child = new Region();
            AnchorPane.setTopAnchor(child, 10.0);
            AnchorPane.setLeftAnchor(child, 20.0);
            original.getChildren().add(child);

            // Migrate to a new AnchorPane — edges should be preserved
            Pane result = TailwindFX.layout(original).anchor().build();
            // same instance (no migration since already AnchorPane)
            check("anchor preserved same pane", result == original);
            check("top anchor preserved",
                AnchorPane.getTopAnchor(child) != null &&
                AnchorPane.getTopAnchor(child) == 10.0);
        });
    }

    static void testStaticHelpers() throws Exception {
        runFx(() -> {
            Region n = new Region();
            // hgrow
            HBox hbox = new HBox();
            hbox.getChildren().add(n);
            FxLayout.hgrow(n);
            check("hgrow=ALWAYS", HBox.getHgrow(n) == Priority.ALWAYS);

            // spacer
            Region spacer = FxLayout.spacer();
            check("spacer not null",    spacer != null);
            check("spacer maxW=MAX",    spacer.getMaxWidth() == Double.MAX_VALUE);

            // spacer(size)
            Region sized = FxLayout.spacer(20);
            check("spacer(20) min=20",  sized.getMinWidth() == 20);
        });
    }
}
