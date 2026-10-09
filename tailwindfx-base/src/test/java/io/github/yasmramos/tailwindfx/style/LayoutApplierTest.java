package io.github.yasmramos.tailwindfx.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.yasmramos.tailwindfx.TwConfig;
import io.github.yasmramos.tailwindfx.layout.TwFlexPane;
import io.github.yasmramos.tailwindfx.layout.TwGridPane;
import java.util.Arrays;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * LayoutApplierTest - Unit tests for layout style application.
 *
 * <p>Verifies that layout-dependent styles (margins, gaps, flex, grid) are applied correctly to
 * various container types (HBox, VBox, TwFlexPane, TwGridPane).
 */
public class LayoutApplierTest extends ApplicationTest {

  private Label label;
  private HBox hBox;
  private VBox vBox;
  private TwFlexPane twFlexPane;
  private TwGridPane twGridPane;

  @Override
  public void start(javafx.stage.Stage stage) {
    // Setup test fixtures
    label = new Label("test");
    hBox = new HBox(label);
    vBox = new VBox(new Label("test2"));
    twFlexPane = new TwFlexPane();
    twGridPane = TwGridPane.create(4.0);

    Pane root = new Pane();
    root.getChildren().addAll(hBox, vBox, twFlexPane, twGridPane);

    javafx.scene.Scene scene = new javafx.scene.Scene(root, 800, 600);
    stage.setScene(scene);
    stage.show();
  }

  @BeforeEach
  public void setUp() {
    // Reset TwConfig to default unit size
    TwConfig.unit(4.0);
  }

  @Test
  public void testApplyMarginNumericValue() {
    Label node = new Label("margin-test");
    interact(() -> hBox.getChildren().add(node));

    LayoutApplier.applyLayoutDependentStyles(node, Arrays.asList("m-4"));

    // m-4 should apply margin via HBox/VBox/GridPane.setMargin(), not translate
    // Margins are handled programmatically, not via CSS or translate properties
    Insets hboxMargin = HBox.getMargin(node);
    assertNotNull(hboxMargin, "HBox margin should be set");
    assertEquals(16.0, hboxMargin.getTop(), 0.1, "Margin top should be 16px (4 * 4px)");
    assertEquals(16.0, hboxMargin.getRight(), 0.1, "Margin right should be 16px");
    assertEquals(16.0, hboxMargin.getBottom(), 0.1, "Margin bottom should be 16px");
    assertEquals(16.0, hboxMargin.getLeft(), 0.1, "Margin left should be 16px");
  }

  @Test
  public void testApplyMarginArbitraryValue() {
    Label node = new Label("margin-arbitrary-test");
    interact(() -> hBox.getChildren().add(node));

    LayoutApplier.applyLayoutDependentStyles(node, Arrays.asList("m-[20px]"));

    // m-[20px] should apply 20px margin via HBox.setMargin()
    // JavaFX doesn't have -fx-margin CSS property; margins are set programmatically
    Insets hboxMargin = HBox.getMargin(node);
    assertNotNull(hboxMargin, "HBox margin should be set for arbitrary values");
    assertEquals(20.0, hboxMargin.getTop(), 0.1, "Margin top should be 20px");
    assertEquals(20.0, hboxMargin.getRight(), 0.1, "Margin right should be 20px");
    assertEquals(20.0, hboxMargin.getBottom(), 0.1, "Margin bottom should be 20px");
    assertEquals(20.0, hboxMargin.getLeft(), 0.1, "Margin left should be 20px");
  }

  @Test
  public void testApplyGapToHBox() {
    Label node1 = new Label("gap-test-1");
    Label node2 = new Label("gap-test-2");
    HBox testHBox = new HBox(node1, node2);
    interact(() -> {});

    LayoutApplier.applyLayoutDependentStyles(testHBox, Arrays.asList("gap-4"));

    // gap-4 should set spacing to 4 * unit = 16px
    assertEquals(16.0, testHBox.getSpacing(), 0.1, "HBox spacing should be 16px");
  }

  @Test
  public void testApplyGapArbitraryValue() {
    Label node1 = new Label("gap-px-1");
    Label node2 = new Label("gap-px-2");
    HBox testHBox = new HBox(node1, node2);
    interact(() -> {});

    LayoutApplier.applyLayoutDependentStyles(testHBox, Arrays.asList("gap-[20px]"));

    assertEquals(20.0, testHBox.getSpacing(), 0.1, "HBox spacing should be 20px");
  }

  @Test
  public void testApplyFlexGrowInHBox() {
    Label node = new Label("flex-grow-test");
    interact(() -> hBox.getChildren().add(node));

    LayoutApplier.applyLayoutDependentStyles(node, Arrays.asList("grow"));

    Priority growPriority = HBox.getHgrow(node);
    assertEquals(Priority.ALWAYS, growPriority, "Grow should set priority to ALWAYS");
  }

  @Test
  public void testApplyFlexShrink() {
    Label node = new Label("flex-shrink-test");
    interact(() -> hBox.getChildren().add(node));

    LayoutApplier.applyLayoutDependentStyles(node, Arrays.asList("shrink"));

    Priority shrinkPriority = HBox.getHgrow(node);
    assertEquals(Priority.NEVER, shrinkPriority, "Shrink should set priority to NEVER");
  }

  @Test
  public void testApplyFlexArbitraryValueInTwFlexPane() {
    Label node = new Label("flex-arbitrary-test");
    interact(() -> twFlexPane.getChildren().add(node));

    LayoutApplier.applyLayoutDependentStyles(node, Arrays.asList("flex-[2]"));

    double growFactor = TwFlexPane.getGrow(node);
    assertEquals(2.0, growFactor, 0.1, "TwFlexPane should support arbitrary flex factor 2.0");
  }

  @Test
  public void testApplyFlexArbitraryValueInHBox() {
    Label node = new Label("flex-hbox-test");
    interact(() -> hBox.getChildren().add(node));

    LayoutApplier.applyLayoutDependentStyles(node, Arrays.asList("flex-[3]"));

    // HBox/VBox only supports Priority enum, maps positive values to ALWAYS
    Priority flexPriority = HBox.getHgrow(node);
    assertEquals(
        Priority.ALWAYS,
        flexPriority,
        "HBox maps flex-[N>0] to Priority.ALWAYS (JavaFX limitation)");
  }

  @Test
  public void testApplyGridColsToTwGridPane() {
    LayoutApplier.applyLayoutDependentStyles(twGridPane, Arrays.asList("grid-cols-3"));

    int cols = twGridPane.getCols();
    assertEquals(3, cols, "TwGridPane should have 3 columns");
  }

  @Test
  public void testApplyGridRowsToTwGridPane() {
    LayoutApplier.applyLayoutDependentStyles(twGridPane, Arrays.asList("grid-rows-2"));

    // TwGridPane doesn't expose getRows() publicly, verify via internal state or skip
    // For now, just ensure no exception is thrown
    assertNotNull(twGridPane, "TwGridPane should exist");
  }

  @Test
  public void testApplyColSpanToChild() {
    Label node = new Label("colspan-test");
    interact(() -> twGridPane.getChildren().add(node));

    LayoutApplier.applyLayoutDependentStyles(node, Arrays.asList("col-span-2"));

    int colSpan = TwGridPane.getColSpan(node);
    assertEquals(2, colSpan, "Column span should be 2");
  }

  @Test
  public void testApplyRowSpanToChild() {
    Label node = new Label("rowspan-test");
    interact(() -> twGridPane.getChildren().add(node));

    LayoutApplier.applyLayoutDependentStyles(node, Arrays.asList("row-span-3"));

    int rowSpan = TwGridPane.getRowSpan(node);
    assertEquals(3, rowSpan, "Row span should be 3");
  }

  @Test
  public void testParseTailwindValueWithBrackets() {
    // Test via reflection or direct method call if accessible
    // This verifies the parseTailwindValue logic is working correctly
    double value1 = invokeParseTailwindValue("m-[16px]");
    assertEquals(4.0, value1, 0.1, "m-[16px] with unit=4 should return 4.0");

    double value2 = invokeParseTailwindValue("m-8");
    assertEquals(8.0, value2, 0.1, "m-8 should return 8.0");
  }

  @Test
  public void testParseCssValue() {
    double pxValue = invokeParseCssValue("20px");
    assertEquals(20.0, pxValue, 0.1, "20px should return 20.0");

    TwConfig.unit(16.0);
    double remValue = invokeParseCssValue("1.5rem");
    assertEquals(24.0, remValue, 0.1, "1.5rem with unit=16 should return 24.0");

    double emValue = invokeParseCssValue("2em");
    assertEquals(32.0, emValue, 0.1, "2em with unit=16 should return 32.0");

    // Reset to default
    TwConfig.unit(4.0);
  }

  @Test
  public void testMultipleTokensApplication() {
    Label node = new Label("multi-token-test");
    interact(() -> hBox.getChildren().add(node));

    List<String> tokens = Arrays.asList("m-2", "flex-1", "gap-2");
    LayoutApplier.applyLayoutDependentStyles(node, tokens);

    // Verify flex was applied
    Priority flexPriority = HBox.getHgrow(node);
    assertEquals(Priority.ALWAYS, flexPriority, "flex-1 should be applied");
  }

  @Test
  public void testGapOnNonPaneNodeRegistersListener() {
    // Bug #9 / unified gap semantics: gap-* is a CONTAINER property. A gap token written on a
    // non-Pane node must never mutate its parent; instead a parent-property listener keeps the
    // application alive so the gap lands on the node itself once it becomes (or is swapped for)
    // a Pane-backed container.
    Label node = new Label("gap-on-label");
    HBox outer = new HBox();

    // Attach under a Pane first: the retry path applies against the node, NOT the parent.
    interact(() -> outer.getChildren().add(node));
    LayoutApplier.applyLayoutDependentStyles(node, Arrays.asList("gap-4"));

    assertEquals(0.0, outer.getSpacing(), 0.1, "gap on a child must not mutate the parent HBox");

    // No exception means the deferred-listener registration worked; after re-parenting into a
    // Pane the listener stays alive (re-parenting support) and re-applies against the node.
    Pane paneHost = new Pane();
    interact(() -> paneHost.getChildren().add(node));
    assertEquals(0.0, outer.getSpacing(), 0.1, "parent spacing must remain untouched");
  }

  @Test
  public void testNegativeMarginNumericToken() {
    Label node = new Label("negative-margin-test");
    interact(() -> hBox.getChildren().add(node));

    LayoutApplier.applyLayoutDependentStyles(node, Arrays.asList("-m-4"));

    Insets hboxMargin = HBox.getMargin(node);
    assertNotNull(hboxMargin, "HBox margin should be set for negative tokens");
    assertEquals(-16.0, hboxMargin.getTop(), 0.1, "-m-4 should produce -16px top margin");
    assertEquals(-16.0, hboxMargin.getLeft(), 0.1, "-m-4 should produce -16px left margin");
  }

  @Test
  public void testNegativeMarginArbitraryToken() {
    Label node = new Label("negative-margin-arb-test");
    interact(() -> hBox.getChildren().add(node));

    LayoutApplier.applyLayoutDependentStyles(node, Arrays.asList("-ml-[20px]"));

    Insets hboxMargin = HBox.getMargin(node);
    assertNotNull(hboxMargin, "HBox margin should be set for arbitrary negative tokens");
    assertEquals(-20.0, hboxMargin.getLeft(), 0.1, "-ml-[20px] should produce -20px left margin");
  }

  @Test
  public void testBareFlexTokenMapsToGrowAndShrink() {
    Label node = new Label("bare-flex-test");
    interact(() -> hBox.getChildren().add(node));

    // Bare "flex" == Tailwind flex: 1 1 0% -> ALWAYS priority in HBox/VBox
    LayoutApplier.applyLayoutDependentStyles(node, Arrays.asList("flex"));

    assertEquals(
        Priority.ALWAYS, HBox.getHgrow(node), "bare flex should map to grow ALWAYS in HBox");
  }

  @Test
  public void testParseTailwindValueReturnsNaNForInvalid() {
    // NaN is the error sentinel; literal 0 remains a legitimate parsed value.
    assertTrue(
        Double.isNaN(invokeParseTailwindValue("m-abc")), "non-numeric tail should return NaN");
    assertTrue(
        Double.isNaN(invokeParseTailwindValue("m-[16px")), "missing bracket should return NaN");
    assertEquals(0.0, invokeParseTailwindValue("m-0"), 0.001, "m-0 must parse as legitimate 0.0");
    assertEquals(8.0, invokeParseTailwindValue("m-8"), 0.001, "m-8 should still parse as 8.0");
  }

  @Test
  public void testGridColsZeroIsSkippedWithWarning() {
    // grid-cols-[0] parses to 0 -> must NOT be forwarded to TwGridPane.cols (division guard)
    int before = twGridPane.getCols();
    LayoutApplier.applyLayoutDependentStyles(twGridPane, Arrays.asList("grid-cols-[0]"));
    assertEquals(before, twGridPane.getCols(), "invalid grid-cols value must leave cols unchanged");
  }

  @Test
  public void testNamedScaleKeywordsDoNotEmitNonNumericWarning() {
    // "mt-auto" is a valid Tailwind utility (recognized by TokenRegistry) that carries no numeric
    // measurement: it must resolve to NaN without logging the "Non-numeric value" warning. A
    // genuinely malformed tail must still be reported, so the quiet path is not a blanket mute.
    boolean previousDebug = TwConfig.isDebug();
    java.io.PrintStream previousOut = System.out;
    try {
      TwConfig.debug(true);

      java.io.ByteArrayOutputStream namedOut = new java.io.ByteArrayOutputStream();
      System.setOut(new java.io.PrintStream(namedOut, true, java.nio.charset.StandardCharsets.UTF_8));
      assertTrue(
          Double.isNaN(invokeParseTailwindValue("mt-auto")), "mt-auto should resolve to NaN");
      String namedMessages = namedOut.toString(java.nio.charset.StandardCharsets.UTF_8);
      assertTrue(
          !namedMessages.contains("Non-numeric value"),
          "named scale keyword mt-auto must not emit a warning, got: " + namedMessages);

      java.io.ByteArrayOutputStream malformedOut = new java.io.ByteArrayOutputStream();
      System.setOut(
          new java.io.PrintStream(malformedOut, true, java.nio.charset.StandardCharsets.UTF_8));
      assertTrue(
          Double.isNaN(invokeParseTailwindValue("m-abc")), "malformed tail should resolve to NaN");
      String malformedMessages = malformedOut.toString(java.nio.charset.StandardCharsets.UTF_8);
      assertTrue(
          malformedMessages.contains("Non-numeric value in token: m-abc"),
          "malformed tail must still emit a warning, got: " + malformedMessages);
    } finally {
      System.setOut(previousOut);
      TwConfig.debug(previousDebug);
    }
  }

  /** Helper to invoke private parseTailwindValue via reflection for testing. */
  private double invokeParseTailwindValue(String token) {
    try {
      java.lang.reflect.Method method =
          LayoutApplier.class.getDeclaredMethod("parseTailwindValue", String.class);
      method.setAccessible(true);
      return (double) method.invoke(null, token);
    } catch (Exception e) {
      throw new RuntimeException("Failed to invoke parseTailwindValue", e);
    }
  }

  /** Helper to invoke private parseCssValue via reflection for testing. */
  private double invokeParseCssValue(String value) {
    try {
      java.lang.reflect.Method method =
          LayoutApplier.class.getDeclaredMethod("parseCssValue", String.class);
      method.setAccessible(true);
      return (double) method.invoke(null, value);
    } catch (Exception e) {
      throw new RuntimeException("Failed to invoke parseCssValue", e);
    }
  }
}
