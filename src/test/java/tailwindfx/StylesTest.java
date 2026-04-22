package tailwindfx;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tests for {@link Styles} — GridPane constraints, margins, z-order,
 * ColorAdjust filters, skew, ImageView, and visibility helpers.
 */
public final class StylesTest {

    private StylesTest() {
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

        }
    }

    static void approx(String l, double e, double a) {
        if (Math.abs(e - a) <= 0.5) {
            ok(l);
        } else {
            fail(l, "expected≈" + e + " got " + a);
        }
    }

    static void throws_(String l, Class<? extends Throwable> t, Runnable r) {
        try {
            r.run();
            fail(l, "no throw");
        } catch (Throwable ex) {
            if (t.isInstance(ex)) {
                ok(l);
            } else {
                fail(l, ex.getClass().getSimpleName());

            }
        }
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
        System.out.println("\n── Styles ──");

        testGridPaneSpans();
        testGridPaneSpanGuards();
        testGridCell();
        testGrow();
        testGrowNone();
        testMarginWithParent();
        testMarginGuards();
        testZOrder();
        testOrderFirstLast();
        testSelfAlignment();
        testJustifySelf();
        testGrayscale();
        testBrightness();
        testContrast();
        testSaturate();
        testHueRotate();
        testInvert();
        testSepia();
        testFilterNone();
        testSkewX();
        testSkewY();
        testVisibility();
        testHiddenNode();
        testShow();
        testWithParentMethods();
        testTextShadow();
        testDropShadowColored();
        testClipCircle();
        testClipRounded();
        testClipMask();
        testRotateX();
        testRotateY();
        testTranslateZ();
        testReset3D();
        testReducedMotion();
        testGlass();
        testNeumorph();
        testSvgHelpers();

        System.out.printf("  %d passed, %d failed%n", passed, failed);
        return failed == 0;
    }

    // ── GridPane constraints ────────────────────────────────────────────
    static void testGridPaneSpans() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.colSpan(n, 3);
            check("colSpan=3", GridPane.getColumnSpan(n) == 3);
            Styles.rowSpan(n, 2);
            check("rowSpan=2", GridPane.getRowSpan(n) == 2);
            Styles.colSpanFull(n);
            check("colSpanFull=REMAINING", GridPane.getColumnSpan(n).equals(GridPane.REMAINING));
            Styles.rowSpanFull(n);
            check("rowSpanFull=REMAINING", GridPane.getRowSpan(n).equals(GridPane.REMAINING));
        });
    }

    static void testGridPaneSpanGuards() {
        throws_("colSpan(null)", IllegalArgumentException.class,
                () -> Styles.colSpan(null, 3));
        throws_("colSpan(0)", IllegalArgumentException.class,
                () -> Styles.colSpan(new Region(), 0));
        throws_("rowSpan(-1)", IllegalArgumentException.class,
                () -> Styles.rowSpan(new Region(), -1));
    }

    static void testGridCell() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.gridCell(n, 2, 3);
            check("col=2", GridPane.getColumnIndex(n) == 2);
            check("row=3", GridPane.getRowIndex(n) == 3);
            throws_("gridCell(null)", IllegalArgumentException.class,
                    () -> Styles.gridCell(null, 0, 0));
            throws_("gridCell(node,-1,0)", IllegalArgumentException.class,
                    () -> Styles.gridCell(new Region(), -1, 0));
        });
    }

    // ── Grow / flex ─────────────────────────────────────────────────────
    static void testGrow() throws Exception {
        runFx(() -> {
            HBox hbox = new HBox();
            Region n = new Region();
            hbox.getChildren().add(n);
            Styles.flex1(n);
            check("flex1 hgrow=ALWAYS", HBox.getHgrow(n) == javafx.scene.layout.Priority.ALWAYS);
            Styles.vgrow(n);
            check("vgrow=ALWAYS", VBox.getVgrow(n) == javafx.scene.layout.Priority.ALWAYS);
        });
    }

    static void testGrowNone() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.grow(n);
            Styles.growNone(n);
            check("growNone hgrow=NEVER", HBox.getHgrow(n) == javafx.scene.layout.Priority.NEVER);
            check("growNone vgrow=NEVER", VBox.getVgrow(n) == javafx.scene.layout.Priority.NEVER);
        });
    }

    // ── Margin ────────────────────────────────────────────────────────────
    static void testMarginWithParent() throws Exception {
        runFx(() -> {
            HBox parent = new HBox();
            Region n = new Region();
            Styles.mxWithParent(n, parent, 4);
            check("mxWithParent: node added", parent.getChildren().contains(n));
            Insets m = HBox.getMargin(n);
            check("mx=16px left", m != null && m.getLeft() == 16.0);
            check("mx=16px right", m != null && m.getRight() == 16.0);
            check("mx=0 top", m != null && m.getTop() == 0.0);

            VBox vbox = new VBox();
            Region n2 = new Region();
            Styles.myWithParent(n2, vbox, 2);
            Insets m2 = VBox.getMargin(n2);
            check("my=8px top", m2 != null && m2.getTop() == 8.0);
            check("my=8px bottom", m2 != null && m2.getBottom() == 8.0);
        });
    }

    static void testMarginGuards() {
        throws_("margin(null,...)", IllegalArgumentException.class,
                () -> Styles.margin(null, 4, 4, 4, 4));
        throws_("mx(null,...)", IllegalArgumentException.class,
                () -> Styles.mx(null, 4));
        throws_("mxWithParent(null,parent)", IllegalArgumentException.class,
                () -> Styles.mxWithParent(null, new HBox(), 4));
        throws_("mxWithParent(node,null)", IllegalArgumentException.class,
                () -> Styles.mxWithParent(new Region(), null, 4));
    }

    // ── Z-order ────────────────────────────────────────────────────────
    static void testZOrder() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.z(n, 50);
            approx("z50 viewOrder=-50", -50, n.getViewOrder());
            Styles.z0(n);
            approx("z0 viewOrder=0", 0, n.getViewOrder());
        });
    }

    static void testOrderFirstLast() throws Exception {
        runFx(() -> {
            Pane parent = new Pane();
            Region a = new Region(), b = new Region();
            parent.getChildren().addAll(a, b);
            Styles.orderFirst(b);
            // toFront moves b last in children list (rendered on top)
            check("b is last after toFront",
                    parent.getChildren().indexOf(b) > parent.getChildren().indexOf(a));
            Styles.orderLast(b);
            check("b is first after toBack",
                    parent.getChildren().indexOf(b) < parent.getChildren().indexOf(a));
        });
    }

    // ── Self/justify-self ─────────────────────────────────────────────
    static void testSelfAlignment() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.selfCenter(n);
            check("selfCenter=CENTER", GridPane.getValignment(n) == javafx.geometry.VPos.CENTER);
            Styles.selfStart(n);
            check("selfStart=TOP", GridPane.getValignment(n) == javafx.geometry.VPos.TOP);
            Styles.selfEnd(n);
            check("selfEnd=BOTTOM", GridPane.getValignment(n) == javafx.geometry.VPos.BOTTOM);
        });
    }

    static void testJustifySelf() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.justifySelfEnd(n);
            check("justifyEnd=RIGHT", GridPane.getHalignment(n) == javafx.geometry.HPos.RIGHT);
            Styles.justifySelfStart(n);
            check("justifyStart=LEFT", GridPane.getHalignment(n) == javafx.geometry.HPos.LEFT);
            Styles.justifySelfCenter(n);
            check("justifyCenter=CENTER", GridPane.getHalignment(n) == javafx.geometry.HPos.CENTER);
        });
    }

    // ── ColorAdjust filters ────────────────────────────────────────────
    static void testGrayscale() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.grayscale(n);
            check("grayscale sets effect",
                    n.getEffect() instanceof javafx.scene.effect.ColorAdjust);
            javafx.scene.effect.ColorAdjust ca
                    = (javafx.scene.effect.ColorAdjust) n.getEffect();
            approx("grayscale saturation=-1", -1.0, ca.getSaturation());
        });
    }

    static void testBrightness() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.brightness(n, 0.5);
            javafx.scene.effect.ColorAdjust ca
                    = (javafx.scene.effect.ColorAdjust) n.getEffect();
            approx("brightness 0.5 → adjusted=-0.5", -0.5, ca.getBrightness());
        });
    }

    static void testContrast() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.contrast(n, 1.5);
            javafx.scene.effect.ColorAdjust ca
                    = (javafx.scene.effect.ColorAdjust) n.getEffect();
            approx("contrast 1.5 → adjusted=0.5", 0.5, ca.getContrast());
        });
    }

    static void testSaturate() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.saturate(n, 2.0);
            javafx.scene.effect.ColorAdjust ca
                    = (javafx.scene.effect.ColorAdjust) n.getEffect();
            approx("saturate 2.0 → adjusted=1.0", 1.0, ca.getSaturation());
        });
    }

    static void testHueRotate() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.hueRotate(n, 180);
            javafx.scene.effect.ColorAdjust ca
                    = (javafx.scene.effect.ColorAdjust) n.getEffect();
            approx("hueRotate 180° → hue=1.0", 1.0, ca.getHue());
        });
    }

    static void testInvert() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.invert(n);
            check("invert sets effect", n.getEffect() != null);
        });
    }

    static void testSepia() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.sepia(n);
            check("sepia sets effect", n.getEffect() != null);
        });
    }

    static void testFilterNone() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.grayscale(n);
            check("effect set", n.getEffect() != null);
            Styles.filterNone(n);
            check("filterNone removes effect", n.getEffect() == null);
        });
    }

    // ── Transforms ────────────────────────────────────────────────────────
    static void testSkewX() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.skewX(n, 6);
            check("skewX adds Shear",
                    n.getTransforms().stream().anyMatch(t -> t instanceof javafx.scene.transform.Shear));
        });
    }

    static void testSkewY() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.skewY(n, 12);
            check("skewY adds Shear",
                    n.getTransforms().stream().anyMatch(t -> t instanceof javafx.scene.transform.Shear));
        });
    }

    // ── Visibility ────────────────────────────────────────────────────────
    static void testVisibility() throws Exception {
        runFx(() -> {
            Region n = new Region();
            n.setOpacity(1.0);
            Styles.invisible(n);
            approx("invisible: opacity=0", 0.0, n.getOpacity());
            check("invisible: still managed", n.isManaged());
            check("invisible: still visible", n.isVisible());
        });
    }

    static void testHiddenNode() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.hiddenNode(n);
            check("hiddenNode: visible=false", !n.isVisible());
            check("hiddenNode: managed=false", !n.isManaged());
        });
    }

    static void testShow() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.hiddenNode(n);
            Styles.show(n);
            check("show: visible=true", n.isVisible());
            check("show: managed=true", n.isManaged());
            approx("show: opacity=1", 1.0, n.getOpacity());
        });
    }

    // ── withParent ────────────────────────────────────────────────────────
    // ── Tailwind v4.1 — Text Shadow ────────────────────────────────────────
    static void testTextShadow() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.textShadowSm(n);
            check("textShadowSm sets DropShadow",
                    n.getEffect() instanceof javafx.scene.effect.DropShadow);

            Styles.textShadowMd(n);
            check("textShadowMd sets DropShadow",
                    n.getEffect() instanceof javafx.scene.effect.DropShadow);

            Styles.textShadowNone(n);
            check("textShadowNone removes effect", n.getEffect() == null);

            Styles.textShadow(n, "#3b82f6", 4, 0, 2);
            check("textShadow color sets effect",
                    n.getEffect() instanceof javafx.scene.effect.DropShadow);
            javafx.scene.effect.DropShadow ds
                    = (javafx.scene.effect.DropShadow) n.getEffect();
            approx("textShadow offsetY=2", 2.0, ds.getOffsetY());

            throws_("textShadow(null node)", IllegalArgumentException.class,
                    () -> Styles.textShadowSm(null));
            throws_("textShadow(blank color)", IllegalArgumentException.class,
                    () -> Styles.textShadow(new Region(), "  ", 4, 0, 2));
        });
    }

    // ── Tailwind v4.1 — Colored Drop Shadow ─────────────────────────────────
    static void testDropShadowColored() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.dropShadowBlue(n);
            check("dropShadowBlue sets DropShadow",
                    n.getEffect() instanceof javafx.scene.effect.DropShadow);

            Styles.dropShadow(n, "#22c55e", 0.5, 10, 0, 3);
            javafx.scene.effect.DropShadow ds
                    = (javafx.scene.effect.DropShadow) n.getEffect();
            approx("green alpha≈0.5", 0.5, ds.getColor().getOpacity());

            Styles.dropShadowNone(n);
            check("dropShadowNone removes effect", n.getEffect() == null);

            throws_("dropShadow(null)", IllegalArgumentException.class,
                    () -> Styles.dropShadowBlue(null));
        });
    }

    // ── Clip / mask ──────────────────────────────────────────────────────────
    static void testClipCircle() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.clipCircle(n);
            check("clipCircle sets clip",
                    n.getClip() instanceof javafx.scene.shape.Circle);
            Styles.clipNone(n);
            check("clipNone removes clip", n.getClip() == null);
        });
    }

    static void testClipRounded() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.clipRounded(n, 8);
            check("clipRounded sets Rectangle clip",
                    n.getClip() instanceof javafx.scene.shape.Rectangle);
            javafx.scene.shape.Rectangle r
                    = (javafx.scene.shape.Rectangle) n.getClip();
            approx("radius arcWidth=16", 16.0, r.getArcWidth());
            Styles.clipNone(n);
        });
    }

    static void testClipMask() throws Exception {
        runFx(() -> {
            Region n = new Region();
            javafx.scene.shape.Rectangle mask = new javafx.scene.shape.Rectangle(50, 50);
            Styles.clipMask(n, mask);
            check("clipMask sets clip", n.getClip() == mask);
            throws_("clipMask(null node)", IllegalArgumentException.class,
                    () -> Styles.clipMask(null, mask));
            throws_("clipMask(null shape)", IllegalArgumentException.class,
                    () -> Styles.clipMask(n, null));
        });
    }

    // ── 3D transforms ────────────────────────────────────────────────────────
    static void testRotateX() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.rotateX(n, 30);
            check("rotateX adds Rotate transform",
                    n.getTransforms().stream().anyMatch(t
                            -> t instanceof javafx.scene.transform.Rotate r
                    && r.getAxis().equals(javafx.scene.transform.Rotate.X_AXIS)));
            Styles.rotateX(n, 0);
            check("rotateX(0) removes transform",
                    n.getTransforms().stream().noneMatch(t
                            -> t instanceof javafx.scene.transform.Rotate r
                    && r.getAxis().equals(javafx.scene.transform.Rotate.X_AXIS)));
        });
    }

    static void testRotateY() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.rotateY(n, 45);
            check("rotateY adds Rotate transform",
                    n.getTransforms().stream().anyMatch(t
                            -> t instanceof javafx.scene.transform.Rotate r
                    && r.getAxis().equals(javafx.scene.transform.Rotate.Y_AXIS)));
        });
    }

    static void testTranslateZ() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.translateZ(n, 50);
            approx("translateZ=50", 50.0, n.getTranslateZ());
            Styles.translateZ(n, 0);
            approx("translateZ=0", 0.0, n.getTranslateZ());
        });
    }

    static void testReset3D() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.rotateX(n, 30);
            Styles.rotateY(n, 45);
            Styles.translateZ(n, 100);
            Styles.reset3D(n);
            check("reset3D clears rotations",
                    n.getTransforms().stream().noneMatch(t
                            -> t instanceof javafx.scene.transform.Rotate r
                    && !r.getAxis().equals(javafx.scene.transform.Rotate.Z_AXIS)));
            approx("reset3D: translateZ=0", 0, n.getTranslateZ());
        });
    }

    // ── Motion reduce ─────────────────────────────────────────────────────────
    static void testReducedMotion() {
        // default is false (motion enabled)
        Styles.setReducedMotion(false);
        check("motion enabled by default", Styles.shouldAnimate());

        Styles.setReducedMotion(true);
        check("reduced motion: shouldAnimate=false", !Styles.shouldAnimate());

        Styles.setReducedMotion(false);
        check("motion restored", Styles.shouldAnimate());
    }

    // ── Glass / neumorph ─────────────────────────────────────────────────────
    static void testGlass() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.glass(n);
            check("glass sets BoxBlur",
                    n.getEffect() instanceof javafx.scene.effect.BoxBlur);
            check("glass sets bg-color",
                    n.getStyle().contains("-fx-background-color"));

            Region n2 = new Region();
            Styles.glassDark(n2);
            check("glassDark sets BoxBlur",
                    n2.getEffect() instanceof javafx.scene.effect.BoxBlur);
        });
    }

    static void testNeumorph() throws Exception {
        runFx(() -> {
            Region n = new Region();
            Styles.neumorph(n);
            check("neumorph sets DropShadow",
                    n.getEffect() instanceof javafx.scene.effect.DropShadow);
            check("neumorph sets bg #e0e5ec",
                    n.getStyle().contains("#e0e5ec") || n.getStyle().contains("e0e5ec"));

            Region n2 = new Region();
            Styles.neumorphInset(n2);
            check("neumorphInset sets InnerShadow",
                    n2.getEffect() instanceof javafx.scene.effect.InnerShadow);
        });
    }

    // ── SVG helpers ────────────────────────────────────────────────────────────
    static void testSvgHelpers() throws Exception {
        runFx(() -> {
            javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(50, 50);
            Styles.fill(rect, "#3b82f6");
            check("fill sets color",
                    rect.getFill() instanceof javafx.scene.paint.Color c
                    && Math.abs(c.getRed() - 59.0 / 255) < 0.01);

            Styles.stroke(rect, "#000000");
            check("stroke sets black",
                    rect.getStroke() instanceof javafx.scene.paint.Color c
                    && c.getRed() < 0.01);
            check("stroke width >= 1", rect.getStrokeWidth() >= 1);

            Styles.strokeWidth(rect, 3);
            approx("strokeWidth=3", 3.0, rect.getStrokeWidth());

            throws_("fill(null shape)", IllegalArgumentException.class,
                    () -> Styles.fill(null, "#000"));
            throws_("stroke(null shape)", IllegalArgumentException.class,
                    () -> Styles.stroke(null, "#000"));
        });
    }

    static void testWithParentMethods() throws Exception {
        runFx(() -> {
            // addWithMargin
            HBox hbox = new HBox();
            Region a = new Region(), b = new Region();
            Styles.addWithMargin(hbox, 3, a, b);
            check("addWithMargin: a added", hbox.getChildren().contains(a));
            check("addWithMargin: b added", hbox.getChildren().contains(b));
            Insets ia = HBox.getMargin(a);
            approx("addWithMargin: margin=12px", 12.0, ia.getTop());

            // mWithParent
            VBox vbox = new VBox();
            Region c = new Region();
            Styles.mWithParent(c, vbox, 2);
            check("mWithParent: added", vbox.getChildren().contains(c));
            Insets ic = VBox.getMargin(c);
            approx("mWithParent: margin=8px", 8.0, ic.getTop());
        });
    }
}
