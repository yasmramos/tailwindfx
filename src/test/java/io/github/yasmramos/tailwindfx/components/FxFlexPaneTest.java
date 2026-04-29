package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.components.FxFlexPane;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Region;

import java.util.List;

/**
 * Unit tests for {@link FxFlexPane} layout engine.
 *
 * <p>
 * Pure Java — no JavaFX Application Thread needed. Tests invoke
 * {@link FxFlexPane#layoutChildren()} indirectly via
 * {@link FxFlexPane#resize(double, double)} then read child positions.
 *
 * <p>
 * Run with any test framework. Each method is self-contained and prints
 * PASS/FAIL to stdout. Throws {@link AssertionError} on failure.
 *
 * <pre>
 * // Run all tests:
 * FxFlexPaneTest.runAll();
 * </pre>
 */
public final class FxFlexPaneTest {

    private FxFlexPaneTest() {
    }

    // =========================================================================
    // Test runner
    // =========================================================================
    /**
     * Runs all tests and prints a summary. Returns true if all passed.
     */
    public static boolean runAll() {
        int passed = 0, failed = 0;
        String[] tests = {
            "testRowLayoutBasic",
            "testColumnLayoutBasic",
            "testJustifyStart",
            "testJustifyCenter",
            "testJustifyEnd",
            "testJustifyBetween",
            "testJustifyAround",
            "testJustifyEvenly",
            "testAlignStart",
            "testAlignCenter",
            "testAlignEnd",
            "testAlignStretch",
            "testFlexGrow",
            "testFlexShrink",
            "testFlexBasisZero",
            "testFlexBasisFixed",
            "testOrder",
            "testAlignSelf",
            "testGapRow",
            "testGapColumn",
            "testGapXY",
            "testWrapRow",
            "testPadding",
            "testEmptyContainer",
            "testSingleChild",};

        for (String test : tests) {
            try {
                java.lang.reflect.Method m = FxFlexPaneTest.class.getMethod(test);
                m.invoke(null);
                System.out.printf("  ✅ %-35s PASS%n", test);
                passed++;
            } catch (java.lang.reflect.InvocationTargetException ite) {
                System.out.printf("  ❌ %-35s FAIL — %s%n", test, ite.getCause().getMessage());
                failed++;
            } catch (Exception e) {
                System.out.printf("  ❌ %-35s ERROR — %s%n", test, e.getMessage());
                failed++;
            }
        }
        System.out.printf("%n  %d passed, %d failed%n", passed, failed);
        return failed == 0;
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    /**
     * Creates a Region with fixed pref size (the simplest test child).
     */
    static Region box(double w, double h) {
        Region r = new Region();
        r.setPrefSize(w, h);
        r.resize(w, h); // prime the layout
        return r;
    }

    /**
     * Lays out a flex pane at the given size, then returns the children in
     * their post-layout positions.
     */
    static List<Node> layout(FxFlexPane pane, double w, double h) {
        pane.resize(w, h);
        pane.layout(); // triggers layoutChildren()
        return pane.getChildren();
    }

    static void assertEquals(String label, double expected, double actual) {
        if (Math.abs(expected - actual) > 0.5) {
            throw new AssertionError(label + ": expected " + expected + " but got " + actual);
        }
    }

    static void assertTrue(String label, boolean condition) {
        if (!condition) {
            throw new AssertionError(label + ": was false");
        }
    }

    // =========================================================================
    // Row basic
    // =========================================================================
    public static void testRowLayoutBasic() {
        FxFlexPane p = FxFlexPane.row().gap(0);
        Region a = box(50, 30), b = box(80, 30);
        p.getChildren().addAll(a, b);
        layout(p, 200, 50);
        assertEquals("a.x", 0, a.getLayoutX());
        assertEquals("b.x", 50, b.getLayoutX());
    }

    public static void testColumnLayoutBasic() {
        FxFlexPane p = FxFlexPane.col().gap(0);
        Region a = box(50, 30), b = box(50, 40);
        p.getChildren().addAll(a, b);
        layout(p, 100, 200);
        assertEquals("a.y", 0, a.getLayoutY());
        assertEquals("b.y", 30, b.getLayoutY());
    }

    public static void testSingleChild() {
        FxFlexPane p = FxFlexPane.row();
        Region a = box(60, 40);
        p.getChildren().add(a);
        layout(p, 200, 100);
        assertEquals("a.x", 0, a.getLayoutX());
        assertEquals("a.y", 0, a.getLayoutY());
    }

    public static void testEmptyContainer() {
        FxFlexPane p = FxFlexPane.row();
        layout(p, 200, 100); // should not throw
    }

    // =========================================================================
    // Justify-content
    // =========================================================================
    public static void testJustifyStart() {
        FxFlexPane p = FxFlexPane.row().justify(FxFlexPane.Justify.START).gap(0);
        Region a = box(50, 30), b = box(50, 30);
        p.getChildren().addAll(a, b);
        layout(p, 200, 50);
        assertEquals("a.x", 0, a.getLayoutX());
        assertEquals("b.x", 50, b.getLayoutX());
    }

    public static void testJustifyCenter() {
        FxFlexPane p = FxFlexPane.row().justify(FxFlexPane.Justify.CENTER).gap(0);
        Region a = box(50, 30), b = box(50, 30);
        p.getChildren().addAll(a, b);
        layout(p, 200, 50);
        // free = 200 - 100 = 100; start offset = 50
        assertEquals("a.x", 50, a.getLayoutX());
        assertEquals("b.x", 100, b.getLayoutX());
    }

    public static void testJustifyEnd() {
        FxFlexPane p = FxFlexPane.row().justify(FxFlexPane.Justify.END).gap(0);
        Region a = box(50, 30), b = box(50, 30);
        p.getChildren().addAll(a, b);
        layout(p, 200, 50);
        // free = 100; start at 100
        assertEquals("a.x", 100, a.getLayoutX());
        assertEquals("b.x", 150, b.getLayoutX());
    }

    public static void testJustifyBetween() {
        FxFlexPane p = FxFlexPane.row().justify(FxFlexPane.Justify.BETWEEN).gap(0);
        Region a = box(50, 30), b = box(50, 30);
        p.getChildren().addAll(a, b);
        layout(p, 200, 50);
        // space-between: a at 0, b at 150
        assertEquals("a.x", 0, a.getLayoutX());
        assertEquals("b.x", 150, b.getLayoutX());
    }

    public static void testJustifyAround() {
        FxFlexPane p = FxFlexPane.row().justify(FxFlexPane.Justify.AROUND).gap(0);
        Region a = box(50, 30), b = box(50, 30);
        p.getChildren().addAll(a, b);
        layout(p, 200, 50);
        // free = 100; unit = 50; a at 25, b at 125
        assertEquals("a.x", 25, a.getLayoutX());
        assertEquals("b.x", 125, b.getLayoutX());
    }

    public static void testJustifyEvenly() {
        FxFlexPane p = FxFlexPane.row().justify(FxFlexPane.Justify.EVENLY).gap(0);
        Region a = box(50, 30), b = box(50, 30);
        p.getChildren().addAll(a, b);
        layout(p, 200, 50);
        // free = 100; 3 gaps; unit = 33.3; a at 33.3, b at 116.7
        assertEquals("a.x", 33, a.getLayoutX());
        assertEquals("b.x", 117, b.getLayoutX());
    }

    // =========================================================================
    // Align-items
    // =========================================================================
    public static void testAlignStart() {
        FxFlexPane p = FxFlexPane.row().align(FxFlexPane.Align.START).gap(0);
        Region a = box(50, 20);
        p.getChildren().add(a);
        layout(p, 200, 100);
        assertEquals("a.y", 0, a.getLayoutY());
        assertEquals("a.height", 20, a.getHeight());
    }

    public static void testAlignCenter() {
        FxFlexPane p = FxFlexPane.row().align(FxFlexPane.Align.CENTER).gap(0);
        Region a = box(50, 20);
        p.getChildren().add(a);
        layout(p, 200, 100);
        // (100 - 20) / 2 = 40
        assertEquals("a.y", 40, a.getLayoutY());
    }

    public static void testAlignEnd() {
        FxFlexPane p = FxFlexPane.row().align(FxFlexPane.Align.END).gap(0);
        Region a = box(50, 20);
        p.getChildren().add(a);
        layout(p, 200, 100);
        assertEquals("a.y", 80, a.getLayoutY());
    }

    public static void testAlignStretch() {
        FxFlexPane p = FxFlexPane.row().align(FxFlexPane.Align.STRETCH).gap(0);
        Region a = box(50, 20);
        p.getChildren().add(a);
        layout(p, 200, 100);
        assertEquals("a.height", 100, a.getHeight());
    }

    // =========================================================================
    // Flex-grow
    // =========================================================================
    public static void testFlexGrow() {
        FxFlexPane p = FxFlexPane.row().gap(0);
        Region fixed = box(60, 30);
        Region grows = box(60, 30);
        FxFlexPane.setGrow(grows, 1);
        p.getChildren().addAll(fixed, grows);
        layout(p, 200, 50);
        // free = 200 - 120 = 80; grows takes all 80
        assertEquals("fixed.width", 60, fixed.getWidth());
        assertEquals("grows.width", 140, grows.getWidth());
        assertEquals("grows.x", 60, grows.getLayoutX());
    }

    // =========================================================================
    // Flex-shrink
    // =========================================================================
    public static void testFlexShrink() {
        FxFlexPane p = FxFlexPane.row().gap(0);
        Region a = box(150, 30); // shrink = 1 (default)
        Region b = box(150, 30); // shrink = 0 (fixed)
        FxFlexPane.setShrink(b, 0);
        p.getChildren().addAll(a, b);
        layout(p, 200, 50);
        // total pref = 300, overflow = 100
        // b does not shrink → b stays 150
        // a absorbs all overflow → a = 150 - 100 = 50
        assertEquals("b.width", 150, b.getWidth());
        assertEquals("a.width", 50, a.getWidth());
    }

    // =========================================================================
    // Flex-basis
    // =========================================================================
    public static void testFlexBasisZero() {
        FxFlexPane p = FxFlexPane.row().gap(0);
        Region a = box(100, 30), b = box(100, 30);
        FxFlexPane.setBasis(a, 0);
        FxFlexPane.setGrow(a, 1);
        FxFlexPane.setBasis(b, 0);
        FxFlexPane.setGrow(b, 1);
        p.getChildren().addAll(a, b);
        layout(p, 200, 50);
        // basis 0 + equal grow → each gets 100
        assertEquals("a.width", 100, a.getWidth());
        assertEquals("b.width", 100, b.getWidth());
    }

    public static void testFlexBasisFixed() {
        FxFlexPane p = FxFlexPane.row().gap(0);
        Region a = box(50, 30);
        FxFlexPane.setBasis(a, 120); // override pref 50 with 120
        p.getChildren().add(a);
        layout(p, 200, 50);
        assertEquals("a.width", 120, a.getWidth());
    }

    // =========================================================================
    // Order
    // =========================================================================
    public static void testOrder() {
        FxFlexPane p = FxFlexPane.row().justify(FxFlexPane.Justify.START).gap(0);
        Region first = box(50, 30);
        Region second = box(50, 30);
        FxFlexPane.setOrder(first, 2);   // goes second visually
        FxFlexPane.setOrder(second, 1);  // goes first visually
        p.getChildren().addAll(first, second);
        layout(p, 200, 50);
        // second (order=1) should be at x=0, first (order=2) at x=50
        assertEquals("second.x", 0, second.getLayoutX());
        assertEquals("first.x", 50, first.getLayoutX());
    }

    // =========================================================================
    // Align-self
    // =========================================================================
    public static void testAlignSelf() {
        FxFlexPane p = FxFlexPane.row().align(FxFlexPane.Align.START).gap(0);
        Region a = box(50, 20); // uses container align (START)
        Region b = box(50, 20);
        FxFlexPane.setAlignSelf(b, FxFlexPane.Align.CENTER); // override
        p.getChildren().addAll(a, b);
        layout(p, 200, 100);
        assertEquals("a.y", 0, a.getLayoutY());          // container START
        assertEquals("b.y", 40, b.getLayoutY());          // self CENTER: (100-20)/2=40
    }

    // =========================================================================
    // Gap
    // =========================================================================
    public static void testGapRow() {
        FxFlexPane p = FxFlexPane.row().gap(10);
        Region a = box(50, 30), b = box(50, 30);
        p.getChildren().addAll(a, b);
        layout(p, 200, 50);
        assertEquals("b.x", 60, b.getLayoutX()); // 50 + 10 gap
    }

    public static void testGapColumn() {
        FxFlexPane p = FxFlexPane.col().gap(8);
        Region a = box(50, 30), b = box(50, 30);
        p.getChildren().addAll(a, b);
        layout(p, 100, 200);
        assertEquals("b.y", 38, b.getLayoutY()); // 30 + 8 gap
    }

    public static void testGapXY() {
        FxFlexPane p = FxFlexPane.row().gapX(12).gapY(6); // main=12, cross=6
        Region a = box(50, 30), b = box(50, 30);
        p.getChildren().addAll(a, b);
        layout(p, 200, 50);
        assertEquals("b.x", 62, b.getLayoutX()); // 50 + 12
    }

    // =========================================================================
    // Wrap
    // =========================================================================
    public static void testWrapRow() {
        FxFlexPane p = FxFlexPane.row().wrap(true).gapX(0).gapY(0);
        Region a = box(80, 30), b = box(80, 30), c = box(80, 30);
        p.getChildren().addAll(a, b, c);
        layout(p, 150, 200); // only 2 fit per row (80+80=160 > 150 with gap)
        // a and b on first row, c wraps to second
        assertEquals("a.y", 0, a.getLayoutY());
        assertEquals("b.y", 0, b.getLayoutY());
        assertEquals("c.y", 30, c.getLayoutY()); // second row at y=30
    }

    // =========================================================================
    // Padding
    // =========================================================================
    public static void testPadding() {
        FxFlexPane p = FxFlexPane.row()
                .padding(new Insets(10, 5, 10, 5))
                .justify(FxFlexPane.Justify.START)
                .gap(0);
        Region a = box(50, 30);
        p.getChildren().add(a);
        layout(p, 200, 60);
        assertEquals("a.x", 5, a.getLayoutX()); // left padding
        assertEquals("a.y", 10, a.getLayoutY()); // top padding
    }
}
