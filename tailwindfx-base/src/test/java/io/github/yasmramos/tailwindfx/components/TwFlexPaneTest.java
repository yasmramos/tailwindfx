package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.layout.TwFlexPane;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Region;

/**
 * Unit tests for {@link TwFlexPane} layout engine.
 *
 * <p>Pure Java — no JavaFX Application Thread needed. Tests invoke {@link
 * TwFlexPane#layoutChildren()} indirectly via {@link TwFlexPane#resize(double, double)} then read
 * child positions.
 *
 * <p>Run with any test framework. Each method is self-contained and prints PASS/FAIL to stdout.
 * Throws {@link AssertionError} on failure.
 *
 * <pre>
 * // Run all tests:
 * TwFlexPaneTest.runAll();
 * </pre>
 */
public final class TwFlexPaneTest {

  private TwFlexPaneTest() {}

  // Test runner
  /** Runs all tests and prints a summary. Returns true if all passed. */
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
      "testSingleChild",
    };

    for (String test : tests) {
      try {
        java.lang.reflect.Method m = TwFlexPaneTest.class.getMethod(test);
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

  // Helpers
  /** Creates a Region with fixed pref size (the simplest test child). */
  static Region box(double w, double h) {
    Region r = new Region();
    r.setPrefSize(w, h);
    r.resize(w, h); // prime the layout
    return r;
  }

  /**
   * Lays out a flex pane at the given size, then returns the children in their post-layout
   * positions.
   */
  static List<Node> layout(TwFlexPane pane, double w, double h) {
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

  // Row basic
  public static void testRowLayoutBasic() {
    TwFlexPane p = TwFlexPane.row().gap(0);
    Region a = box(50, 30), b = box(80, 30);
    p.getChildren().addAll(a, b);
    layout(p, 200, 50);
    assertEquals("a.x", 0, a.getLayoutX());
    assertEquals("b.x", 50, b.getLayoutX());
  }

  public static void testColumnLayoutBasic() {
    TwFlexPane p = TwFlexPane.col().gap(0);
    Region a = box(50, 30), b = box(50, 40);
    p.getChildren().addAll(a, b);
    layout(p, 100, 200);
    assertEquals("a.y", 0, a.getLayoutY());
    assertEquals("b.y", 30, b.getLayoutY());
  }

  public static void testSingleChild() {
    TwFlexPane p = TwFlexPane.row();
    Region a = box(60, 40);
    p.getChildren().add(a);
    layout(p, 200, 100);
    assertEquals("a.x", 0, a.getLayoutX());
    assertEquals("a.y", 0, a.getLayoutY());
  }

  public static void testEmptyContainer() {
    TwFlexPane p = TwFlexPane.row();
    layout(p, 200, 100); // should not throw
  }

  // Justify-content
  public static void testJustifyStart() {
    TwFlexPane p = TwFlexPane.row().justify(TwFlexPane.Justify.START).gap(0);
    Region a = box(50, 30), b = box(50, 30);
    p.getChildren().addAll(a, b);
    layout(p, 200, 50);
    assertEquals("a.x", 0, a.getLayoutX());
    assertEquals("b.x", 50, b.getLayoutX());
  }

  public static void testJustifyCenter() {
    TwFlexPane p = TwFlexPane.row().justify(TwFlexPane.Justify.CENTER).gap(0);
    Region a = box(50, 30), b = box(50, 30);
    p.getChildren().addAll(a, b);
    layout(p, 200, 50);
    // free = 200 - 100 = 100; start offset = 50
    assertEquals("a.x", 50, a.getLayoutX());
    assertEquals("b.x", 100, b.getLayoutX());
  }

  public static void testJustifyEnd() {
    TwFlexPane p = TwFlexPane.row().justify(TwFlexPane.Justify.END).gap(0);
    Region a = box(50, 30), b = box(50, 30);
    p.getChildren().addAll(a, b);
    layout(p, 200, 50);
    // free = 100; start at 100
    assertEquals("a.x", 100, a.getLayoutX());
    assertEquals("b.x", 150, b.getLayoutX());
  }

  public static void testJustifyBetween() {
    TwFlexPane p = TwFlexPane.row().justify(TwFlexPane.Justify.BETWEEN).gap(0);
    Region a = box(50, 30), b = box(50, 30);
    p.getChildren().addAll(a, b);
    layout(p, 200, 50);
    // space-between: a at 0, b at 150
    assertEquals("a.x", 0, a.getLayoutX());
    assertEquals("b.x", 150, b.getLayoutX());
  }

  public static void testJustifyAround() {
    TwFlexPane p = TwFlexPane.row().justify(TwFlexPane.Justify.AROUND).gap(0);
    Region a = box(50, 30), b = box(50, 30);
    p.getChildren().addAll(a, b);
    layout(p, 200, 50);
    // free = 100; unit = 50; a at 25, b at 125
    assertEquals("a.x", 25, a.getLayoutX());
    assertEquals("b.x", 125, b.getLayoutX());
  }

  public static void testJustifyEvenly() {
    TwFlexPane p = TwFlexPane.row().justify(TwFlexPane.Justify.EVENLY).gap(0);
    Region a = box(50, 30), b = box(50, 30);
    p.getChildren().addAll(a, b);
    layout(p, 200, 50);
    // free = 100; 3 gaps; unit = 33.3; a at 33.3, b at 116.7
    assertEquals("a.x", 33, a.getLayoutX());
    assertEquals("b.x", 117, b.getLayoutX());
  }

  // Align-items
  public static void testAlignStart() {
    TwFlexPane p = TwFlexPane.row().align(TwFlexPane.Align.START).gap(0);
    Region a = box(50, 20);
    p.getChildren().add(a);
    layout(p, 200, 100);
    assertEquals("a.y", 0, a.getLayoutY());
    assertEquals("a.height", 20, a.getHeight());
  }

  public static void testAlignCenter() {
    TwFlexPane p = TwFlexPane.row().align(TwFlexPane.Align.CENTER).gap(0);
    Region a = box(50, 20);
    p.getChildren().add(a);
    layout(p, 200, 100);
    // (100 - 20) / 2 = 40
    assertEquals("a.y", 40, a.getLayoutY());
  }

  public static void testAlignEnd() {
    TwFlexPane p = TwFlexPane.row().align(TwFlexPane.Align.END).gap(0);
    Region a = box(50, 20);
    p.getChildren().add(a);
    layout(p, 200, 100);
    assertEquals("a.y", 80, a.getLayoutY());
  }

  public static void testAlignStretch() {
    TwFlexPane p = TwFlexPane.row().align(TwFlexPane.Align.STRETCH).gap(0);
    Region a = box(50, 20);
    p.getChildren().add(a);
    layout(p, 200, 100);
    assertEquals("a.height", 100, a.getHeight());
  }

  // Flex-grow
  public static void testFlexGrow() {
    TwFlexPane p = TwFlexPane.row().gap(0);
    Region fixed = box(60, 30);
    Region grows = box(60, 30);
    TwFlexPane.setGrow(grows, 1);
    p.getChildren().addAll(fixed, grows);
    layout(p, 200, 50);
    // free = 200 - 120 = 80; grows takes all 80
    assertEquals("fixed.width", 60, fixed.getWidth());
    assertEquals("grows.width", 140, grows.getWidth());
    assertEquals("grows.x", 60, grows.getLayoutX());
  }

  // Flex-shrink
  public static void testFlexShrink() {
    TwFlexPane p = TwFlexPane.row().gap(0);
    Region a = box(150, 30); // shrink = 1 (default)
    Region b = box(150, 30); // shrink = 0 (fixed)
    TwFlexPane.setShrink(b, 0);
    p.getChildren().addAll(a, b);
    layout(p, 200, 50);
    // total pref = 300, overflow = 100
    // b does not shrink → b stays 150
    // a absorbs all overflow → a = 150 - 100 = 50
    assertEquals("b.width", 150, b.getWidth());
    assertEquals("a.width", 50, a.getWidth());
  }

  // Flex-basis
  public static void testFlexBasisZero() {
    TwFlexPane p = TwFlexPane.row().gap(0);
    Region a = box(100, 30), b = box(100, 30);
    TwFlexPane.setBasis(a, 0);
    TwFlexPane.setGrow(a, 1);
    TwFlexPane.setBasis(b, 0);
    TwFlexPane.setGrow(b, 1);
    p.getChildren().addAll(a, b);
    layout(p, 200, 50);
    // basis 0 + equal grow → each gets 100
    assertEquals("a.width", 100, a.getWidth());
    assertEquals("b.width", 100, b.getWidth());
  }

  public static void testFlexBasisFixed() {
    TwFlexPane p = TwFlexPane.row().gap(0);
    Region a = box(50, 30);
    TwFlexPane.setBasis(a, 120); // override pref 50 with 120
    p.getChildren().add(a);
    layout(p, 200, 50);
    assertEquals("a.width", 120, a.getWidth());
  }

  // Order
  public static void testOrder() {
    TwFlexPane p = TwFlexPane.row().justify(TwFlexPane.Justify.START).gap(0);
    Region first = box(50, 30);
    Region second = box(50, 30);
    TwFlexPane.setOrder(first, 2); // goes second visually
    TwFlexPane.setOrder(second, 1); // goes first visually
    p.getChildren().addAll(first, second);
    layout(p, 200, 50);
    // second (order=1) should be at x=0, first (order=2) at x=50
    assertEquals("second.x", 0, second.getLayoutX());
    assertEquals("first.x", 50, first.getLayoutX());
  }

  // Align-self
  public static void testAlignSelf() {
    TwFlexPane p = TwFlexPane.row().align(TwFlexPane.Align.START).gap(0);
    Region a = box(50, 20); // uses container align (START)
    Region b = box(50, 20);
    TwFlexPane.setAlignSelf(b, TwFlexPane.Align.CENTER); // override
    p.getChildren().addAll(a, b);
    layout(p, 200, 100);
    assertEquals("a.y", 0, a.getLayoutY()); // container START
    assertEquals("b.y", 40, b.getLayoutY()); // self CENTER: (100-20)/2=40
  }

  // Gap
  public static void testGapRow() {
    TwFlexPane p = TwFlexPane.row().gap(10);
    Region a = box(50, 30), b = box(50, 30);
    p.getChildren().addAll(a, b);
    layout(p, 200, 50);
    assertEquals("b.x", 60, b.getLayoutX()); // 50 + 10 gap
  }

  public static void testGapColumn() {
    TwFlexPane p = TwFlexPane.col().gap(8);
    Region a = box(50, 30), b = box(50, 30);
    p.getChildren().addAll(a, b);
    layout(p, 100, 200);
    assertEquals("b.y", 38, b.getLayoutY()); // 30 + 8 gap
  }

  public static void testGapXY() {
    TwFlexPane p = TwFlexPane.row().gapX(12).gapY(6); // main=12, cross=6
    Region a = box(50, 30), b = box(50, 30);
    p.getChildren().addAll(a, b);
    layout(p, 200, 50);
    assertEquals("b.x", 62, b.getLayoutX()); // 50 + 12
  }

  // Wrap
  public static void testWrapRow() {
    TwFlexPane p = TwFlexPane.row().wrap(true).gapX(0).gapY(0);
    Region a = box(80, 30), b = box(80, 30), c = box(80, 30);
    p.getChildren().addAll(a, b, c);
    layout(p, 150, 200); // only 2 fit per row (80+80=160 > 150 with gap)
    // a and b on first row, c wraps to second
    assertEquals("a.y", 0, a.getLayoutY());
    assertEquals("b.y", 0, b.getLayoutY());
    assertEquals("c.y", 30, c.getLayoutY()); // second row at y=30
  }

  // Padding
  public static void testPadding() {
    TwFlexPane p =
        TwFlexPane.row().padding(new Insets(10, 5, 10, 5)).justify(TwFlexPane.Justify.START).gap(0);
    Region a = box(50, 30);
    p.getChildren().add(a);
    layout(p, 200, 60);
    assertEquals("a.x", 5, a.getLayoutX()); // left padding
    assertEquals("a.y", 10, a.getLayoutY()); // top padding
  }
}
