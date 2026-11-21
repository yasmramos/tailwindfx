package io.github.yasmramos.tailwindfx.style;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.animation.TwAnimation;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Shear;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Styles} programmatic utility facade. Each nested class covers one group
 * of helpers (grid, flex, spacing, filters, transforms, ...).
 */
@DisplayName("Styles Utility Tests")
class StylesTest {

  private Region region;
  private Pane parent;

  @BeforeEach
  void setUp() {
    region = new Region();
    parent = new Pane();
  }

  @Nested
  @DisplayName("Grid placement")
  class GridTests {

    @Test
    @DisplayName("colSpan / rowSpan set the constraints and return the same node")
    void spans() {
      assertSame(region, Styles.colSpan(region, 3));
      assertEquals(3, GridPane.getColumnSpan(region));
      assertSame(region, Styles.rowSpan(region, 2));
      assertEquals(2, GridPane.getRowSpan(region));
    }

    @Test
    @DisplayName("colSpanFull / rowSpanFull use REMAINING")
    void fullSpans() {
      Styles.colSpanFull(region);
      assertEquals(GridPane.REMAINING, GridPane.getColumnSpan(region));
      Styles.rowSpanFull(region);
      assertEquals(GridPane.REMAINING, GridPane.getRowSpan(region));
    }

    @Test
    @DisplayName("colSpan rejects a span below 1")
    void spanValidation() {
      assertThrows(IllegalArgumentException.class, () -> Styles.colSpan(region, 0));
    }

    @Test
    @DisplayName("shorthand col-span-N / row-span-N helpers")
    void spanShorthands() {
      assertEquals(2, GridPane.getColumnSpan(Styles.colSpan2(new Region())));
      assertEquals(12, GridPane.getColumnSpan(Styles.colSpan12(new Region())));
      assertEquals(3, GridPane.getRowSpan(Styles.rowSpan3(new Region())));
      assertEquals(6, GridPane.getRowSpan(Styles.rowSpan6(new Region())));
    }

    @Test
    @DisplayName("gridCell positions the node and rejects negative indices")
    void gridCell() {
      Styles.gridCell(region, 2, 1);
      assertEquals(2, GridPane.getColumnIndex(region));
      assertEquals(1, GridPane.getRowIndex(region));
      assertThrows(IllegalArgumentException.class, () -> Styles.gridCell(region, -1, 0));
      assertThrows(IllegalArgumentException.class, () -> Styles.gridCell(region, 0, -1));
    }

    @Test
    @DisplayName("colStart / rowStart set the single index")
    void starts() {
      Styles.colStart(region, 4);
      assertEquals(4, GridPane.getColumnIndex(region));
      Styles.rowStart(region, 5);
      assertEquals(5, GridPane.getRowIndex(region));
    }
  }

  @Nested
  @DisplayName("Flex / grow")
  class FlexTests {

    @Test
    @DisplayName("flex1 and grow set HBox grow to ALWAYS")
    void grow() {
      Styles.flex1(region);
      assertEquals(Priority.ALWAYS, HBox.getHgrow(region));
      Styles.grow(region);
      assertEquals(Priority.ALWAYS, HBox.getHgrow(region));
    }

    @Test
    @DisplayName("growNone clears both grow constraints")
    void growNone() {
      Styles.growNone(region);
      assertEquals(Priority.NEVER, HBox.getHgrow(region));
      assertEquals(Priority.NEVER, VBox.getVgrow(region));
    }

    @Test
    @DisplayName("flexAuto uses SOMETIMES")
    void flexAuto() {
      Styles.flexAuto(region);
      assertEquals(Priority.SOMETIMES, HBox.getHgrow(region));
    }

    @Test
    @DisplayName("vgrow sets VBox grow")
    void vgrow() {
      Styles.vgrow(region);
      assertEquals(Priority.ALWAYS, VBox.getVgrow(region));
    }

    @Test
    @DisplayName("fill width/height expands the max size")
    void fill() {
      Styles.hboxFillWidth(region);
      assertEquals(Double.MAX_VALUE, region.getMaxWidth());
      Styles.vboxFillHeight(region);
      assertEquals(Double.MAX_VALUE, region.getMaxHeight());
    }
  }

  @Nested
  @DisplayName("Spacing")
  class SpacingTests {

    @Test
    @DisplayName("m applies a uniform margin of n*4px on every side")
    void uniformMargin() {
      parent.getChildren().add(region);
      Styles.m(region, 2);
      Insets insets = HBox.getMargin(region);
      assertEquals(8, insets.getTop());
      assertEquals(8, insets.getLeft());
      assertEquals(8, insets.getBottom());
      assertEquals(8, insets.getRight());
    }

    @Test
    @DisplayName("mx / my set only the relevant sides")
    void axisMargins() {
      parent.getChildren().add(region);
      Styles.mx(region, 1);
      assertEquals(4, HBox.getMargin(region).getLeft());
      assertEquals(0, HBox.getMargin(region).getTop());
      Styles.my(region, 3);
      assertEquals(12, HBox.getMargin(region).getTop());
    }

    @Test
    @DisplayName("mt / mr / mb / ml set a single side")
    void singleSideMargins() {
      parent.getChildren().add(region);
      Styles.mt(region, 1);
      assertEquals(4, HBox.getMargin(region).getTop());
      Styles.ml(region, 2);
      assertEquals(8, HBox.getMargin(region).getLeft());
      Styles.mr(region, 3);
      assertEquals(12, HBox.getMargin(region).getRight());
      Styles.mb(region, 4);
      assertEquals(16, HBox.getMargin(region).getBottom());
    }

    @Test
    @DisplayName("margin accepts exact pixel values")
    void pixelMargin() {
      parent.getChildren().add(region);
      Styles.margin(region, 1, 2, 3, 4);
      Insets insets = HBox.getMargin(region);
      assertEquals(1, insets.getTop());
      assertEquals(2, insets.getRight());
      assertEquals(3, insets.getBottom());
      assertEquals(4, insets.getLeft());
    }

    @Test
    @DisplayName("mxAuto grows the node to centre it")
    void mxAuto() {
      Styles.mxAuto(region);
      assertEquals(Priority.ALWAYS, HBox.getHgrow(region));
    }

    @Test
    @DisplayName("WithParent helpers add the node and apply the margin")
    void withParent() {
      HBox hbox = new HBox();
      Styles.mWithParent(region, hbox, 1);
      assertTrue(hbox.getChildren().contains(region));
      assertEquals(4, HBox.getMargin(region).getTop());

      Region other = new Region();
      Styles.mxWithParent(other, hbox, 2);
      assertTrue(hbox.getChildren().contains(other));

      Region third = new Region();
      Styles.myWithParent(third, hbox, 1);
      assertTrue(hbox.getChildren().contains(third));

      Region fourth = new Region();
      Styles.marginWithParent(fourth, hbox, 1, 1, 1, 1);
      assertTrue(hbox.getChildren().contains(fourth));
    }

    @Test
    @DisplayName("addWithMargin adds several nodes with the same margin")
    void addWithMargin() {
      HBox hbox = new HBox();
      Region a = new Region();
      Region b = new Region();
      Styles.addWithMargin(hbox, 1, a, b);
      assertEquals(2, hbox.getChildren().size());
      assertEquals(4, HBox.getMargin(a).getTop());
      assertEquals(4, HBox.getMargin(b).getTop());
    }
  }

  @Nested
  @DisplayName("Z-order and alignment")
  class ZAndAlignmentTests {

    @Test
    @DisplayName("z inverts the value into viewOrder")
    void zOrder() {
      Styles.z(region, 30);
      assertEquals(-30, region.getViewOrder());
      Styles.z0(new Region());
      assertEquals(0, new Region().getViewOrder());
      Styles.z50(region);
      assertEquals(-50, region.getViewOrder());
    }

    @Test
    @DisplayName("self-* set the grid vertical alignment")
    void selfAlignment() {
      Styles.selfStart(region);
      assertEquals(VPos.TOP, GridPane.getValignment(region));
      Styles.selfCenter(region);
      assertEquals(VPos.CENTER, GridPane.getValignment(region));
      Styles.selfEnd(region);
      assertEquals(VPos.BOTTOM, GridPane.getValignment(region));
      Styles.selfBaseline(region);
      assertEquals(VPos.BASELINE, GridPane.getValignment(region));
    }

    @Test
    @DisplayName("justify-self-* set the grid horizontal alignment")
    void justifySelf() {
      Styles.justifySelfStart(region);
      assertEquals(HPos.LEFT, GridPane.getHalignment(region));
      Styles.justifySelfCenter(region);
      assertEquals(HPos.CENTER, GridPane.getHalignment(region));
      Styles.justifySelfEnd(region);
      assertEquals(HPos.RIGHT, GridPane.getHalignment(region));
    }
  }

  @Nested
  @DisplayName("Colour filters")
  class FilterTests {

    @Test
    @DisplayName("grayscale applies and grayscale0 removes the ColorAdjust")
    void grayscale() {
      Styles.grayscale(region);
      assertInstanceOf(ColorAdjust.class, region.getEffect());
      assertEquals(-1, ((ColorAdjust) region.getEffect()).getSaturation());
      Styles.grayscale0(region);
      assertNull(region.getEffect());
    }

    @Test
    @DisplayName("brightness is normalised around 1.0")
    void brightness() {
      Styles.brightness(region, 1.5);
      ColorAdjust effect = (ColorAdjust) region.getEffect();
      assertEquals(0.5, effect.getBrightness(), 1e-9);
    }

    @Test
    @DisplayName("brightness rejects negative values")
    void brightnessNegative() {
      assertThrows(IllegalArgumentException.class, () -> Styles.brightness(region, -1));
    }

    @Test
    @DisplayName("contrast and saturate are merged onto one ColorAdjust")
    void mergeFilters() {
      Styles.contrast(region, 2.0);
      Styles.saturate(region, 1.5);
      ColorAdjust effect = (ColorAdjust) region.getEffect();
      assertEquals(1.0, effect.getContrast(), 1e-9);
      assertEquals(0.5, effect.getSaturation(), 1e-9);
    }

    @Test
    @DisplayName("hueRotate converts degrees to the -1..1 hue range")
    void hueRotate() {
      Styles.hueRotate(region, 90);
      assertEquals(0.5, ((ColorAdjust) region.getEffect()).getHue(), 1e-9);
    }

    @Test
    @DisplayName("invert and sepia replace the effect")
    void invertAndSepia() {
      Styles.invert(region);
      assertInstanceOf(ColorAdjust.class, region.getEffect());
      Styles.sepia(region);
      assertInstanceOf(ColorAdjust.class, region.getEffect());
    }

    @Test
    @DisplayName("filterNone clears a ColorAdjust")
    void filterNone() {
      Styles.grayscale(region);
      Styles.filterNone(region);
      assertNull(region.getEffect());
    }
  }

  @Nested
  @DisplayName("Transforms and aspect ratio")
  class TransformTests {

    @Test
    @DisplayName("skewX and skewY compose into a single Shear transform")
    void skew() {
      Styles.skewX(region, 45);
      assertEquals(1, region.getTransforms().size());
      assertInstanceOf(Shear.class, region.getTransforms().get(0));

      Styles.skewY(region, 45);
      assertEquals(1, region.getTransforms().size());
      Shear shear = (Shear) region.getTransforms().get(0);
      assertEquals(1.0, shear.getX(), 1e-6);
      assertEquals(1.0, shear.getY(), 1e-6);

      Styles.skewY(region, 0);
      assertEquals(1, region.getTransforms().size());
      assertEquals(1.0, ((Shear) region.getTransforms().get(0)).getX(), 1e-6);
    }

    @Test
    @DisplayName("skew shorthands apply fixed angles")
    void skewShorthands() {
      Styles.skewX6(new Region());
      Styles.skewY12(new Region());
      Styles.skewX1(new Region());
      Styles.skewY3(new Region());
    }

    @Test
    @DisplayName("aspectRatio binds the preferred height to the width")
    void aspectRatio() {
      Styles.aspectRatio(region, 2.0);
      assertTrue(region.prefHeightProperty().isBound());
    }

    @Test
    @DisplayName("aspectRatio rejects non-positive ratios")
    void aspectRatioValidation() {
      assertThrows(IllegalArgumentException.class, () -> Styles.aspectRatio(region, 0));
      assertThrows(IllegalArgumentException.class, () -> Styles.aspectRatio(region, -1));
    }

    @Test
    @DisplayName("aspect presets bind the height")
    void aspectPresets() {
      assertTrue(Styles.aspectSquare(new Region()).prefHeightProperty().isBound());
      assertTrue(Styles.aspectVideo(new Region()).prefHeightProperty().isBound());
      assertTrue(Styles.aspectPortrait(new Region()).prefHeightProperty().isBound());
      assertTrue(Styles.aspectLandscape(new Region()).prefHeightProperty().isBound());
    }

    @Test
    @DisplayName("rotateX / rotateY add an axis Rotate; zero removes it")
    void rotations() {
      Styles.rotateX(region, 30);
      assertEquals(1, region.getTransforms().size());
      assertEquals(Rotate.X_AXIS, ((Rotate) region.getTransforms().get(0)).getAxis());
      Styles.rotateY(region, 30);
      assertEquals(2, region.getTransforms().size());
      Styles.rotateX(region, 0);
      assertEquals(1, region.getTransforms().size());
    }

    @Test
    @DisplayName("translateZ sets the Z translation and reset3D clears it")
    void translateZ() {
      Styles.translateZ(region, 25);
      assertEquals(25, region.getTranslateZ());
      Styles.reset3D(region);
      assertEquals(0, region.getTranslateZ());
    }

    @Test
    @DisplayName("perspective is a no-op without a Scene and sets a camera with one")
    void perspective() {
      Styles.perspective(region, 1000);
      assertNull(region.getScene());

      StackPane root = new StackPane(region);
      Scene scene = new Scene(root, 800, 600);
      Styles.perspective(region, 1000);
      assertNotNull(scene.getCamera());
    }
  }

  @Nested
  @DisplayName("ImageView helpers")
  class ImageViewTests {

    @Test
    @DisplayName("objectCover / objectFill clear preserveRatio, objectContain sets it")
    void objectFit() {
      ImageView iv = new ImageView();
      Styles.objectCover(iv);
      assertFalse(iv.isPreserveRatio());
      Styles.objectContain(iv);
      assertTrue(iv.isPreserveRatio());
      Styles.objectFill(iv);
      assertFalse(iv.isPreserveRatio());
    }

    @Test
    @DisplayName("objectTop / objectCenter tolerate a null image")
    void objectPosition() {
      ImageView iv = new ImageView();
      assertSame(iv, Styles.objectTop(iv));
      assertSame(iv, Styles.objectCenter(iv));
      assertNull(iv.getViewport());
    }

    @Test
    @DisplayName("imgSize sets the fit dimensions")
    void imgSize() {
      ImageView iv = new ImageView();
      Styles.imgSize(iv, 120, 80, true);
      assertEquals(120, iv.getFitWidth());
      assertEquals(80, iv.getFitHeight());
      assertTrue(iv.isPreserveRatio());
    }
  }

  @Nested
  @DisplayName("Visibility")
  class VisibilityTests {

    @Test
    @DisplayName("invisible keeps layout space, hiddenNode removes it, show restores")
    void visibility() {
      Styles.invisible(region);
      assertEquals(0, region.getOpacity());
      assertTrue(region.isVisible());

      Styles.hiddenNode(region);
      assertFalse(region.isVisible());
      assertFalse(region.isManaged());

      Styles.show(region);
      assertTrue(region.isVisible());
      assertTrue(region.isManaged());
      assertEquals(1, region.getOpacity());
    }
  }

  @Nested
  @DisplayName("Shadows")
  class ShadowTests {

    @Test
    @DisplayName("textShadow applies a DropShadow effect")
    void textShadow() {
      Label label = new Label("hi");
      Styles.textShadow(label, "#000000", 4, 0, 2);
      assertInstanceOf(DropShadow.class, label.getEffect());
    }

    @Test
    @DisplayName("textShadow presets and textShadowNone")
    void textShadowPresets() {
      Label label = new Label("hi");
      Styles.textShadowSm(label);
      assertInstanceOf(DropShadow.class, label.getEffect());
      Styles.textShadowMd(label);
      Styles.textShadowLg(label);
      Styles.textShadowXl(label);
      Styles.textShadowColor(label, "#3b82f6", 6, 0, 2);
      Styles.textShadowNone(label);
      assertNull(label.getEffect());
    }

    @Test
    @DisplayName("dropShadow clamps alpha and applies a coloured shadow")
    void dropShadow() {
      Styles.dropShadow(region, "#3b82f6", 2.0, 12, 0, 4);
      DropShadow ds = (DropShadow) region.getEffect();
      assertEquals(1.0, ds.getColor().getOpacity(), 1e-9);
      assertEquals(12, ds.getRadius());
    }

    @Test
    @DisplayName("dropShadow colour presets and dropShadowNone")
    void dropShadowPresets() {
      Styles.dropShadowBlue(new Region());
      Styles.dropShadowGreen(new Region());
      Styles.dropShadowRed(new Region());
      Styles.dropShadowPurple(new Region());
      Styles.dropShadowYellow(new Region());
      Styles.dropShadowNone(region);
      assertNull(region.getEffect());
    }
  }

  @Nested
  @DisplayName("Clipping")
  class ClipTests {

    @Test
    @DisplayName("clipRect sets a Rectangle clip with optional rounding")
    void clipRect() {
      Styles.clipRect(region, 100, 50, 8);
      Rectangle clip = (Rectangle) region.getClip();
      assertEquals(100, clip.getWidth());
      assertEquals(50, clip.getHeight());
      assertEquals(16, clip.getArcWidth());
      Styles.clipRect(region, 10, 10);
      assertEquals(0, ((Rectangle) region.getClip()).getArcWidth());
    }

    @Test
    @DisplayName("clipRounded and clipCircle set a clip")
    void clipShapes() {
      Styles.clipRounded(region, 12);
      assertInstanceOf(Rectangle.class, region.getClip());
      Styles.clipCircle(region);
      assertNotNull(region.getClip());
    }

    @Test
    @DisplayName("clipMask applies a shape and clipNone removes it")
    void clipMaskAndNone() {
      Styles.clipMask(region, new Rectangle(10, 10));
      assertNotNull(region.getClip());
      Styles.clipNone(region);
      assertNull(region.getClip());
    }

    @Test
    @DisplayName("clipNone detaches the bounds listener installed by clipRounded")
    void clipNoneDetachesBoundsListener() {
      region.resize(100, 100);
      Styles.clipRounded(region, 8);
      Rectangle stale = (Rectangle) region.getClip();
      double widthWhileClipped = stale.getWidth();

      Styles.clipNone(region);
      // A leaked listener would keep resizing the detached clip on the next layout pass.
      region.resize(400, 400);

      assertEquals(
          widthWhileClipped,
          stale.getWidth(),
          "a removed clip must no longer track the node's layout bounds");
    }
  }

  @Nested
  @DisplayName("SVG helpers")
  class SvgTests {

    @Test
    @DisplayName("fill and stroke apply the given colour")
    void fillAndStroke() {
      Rectangle shape = new Rectangle();
      Styles.fill(shape, "#3b82f6");
      assertEquals(Color.web("#3b82f6"), shape.getFill());
      Styles.stroke(shape, "#ef4444");
      assertEquals(Color.web("#ef4444"), shape.getStroke());
      assertEquals(1, shape.getStrokeWidth());
    }

    @Test
    @DisplayName("strokeWidth sets the stroke width")
    void strokeWidth() {
      Rectangle shape = new Rectangle();
      Styles.strokeWidth(shape, 3);
      assertEquals(3, shape.getStrokeWidth());
    }

    @Test
    @DisplayName("fillCurrent copies the reference text colour")
    void fillCurrent() {
      Rectangle shape = new Rectangle();
      Node reference = new Region();
      reference.getProperties().put("tailwindfx.text.color", Color.web("#22c55e"));
      Styles.fillCurrent(shape, reference);
      assertEquals(Color.web("#22c55e"), shape.getFill());
    }

    @Test
    @DisplayName("fillCurrent reads a Label's text fill")
    void fillCurrentFromLabel() {
      Label label = new Label("hi");
      label.setTextFill(Color.web("#ef4444"));
      Rectangle shape = new Rectangle();
      Styles.fillCurrent(shape, label);
      assertEquals(Color.web("#ef4444"), shape.getFill());
    }

    @Test
    @DisplayName("fillCurrent reads a Text node's fill")
    void fillCurrentFromText() {
      javafx.scene.text.Text text = new javafx.scene.text.Text("hi");
      text.setFill(Color.web("#3b82f6"));
      Rectangle shape = new Rectangle();
      Styles.fillCurrent(shape, text);
      assertEquals(Color.web("#3b82f6"), shape.getFill());
    }

    @Test
    @DisplayName("fillCurrent falls back to black for a plain node")
    void fillCurrentFallback() {
      Rectangle shape = new Rectangle();
      Styles.fillCurrent(shape, new Region());
      assertEquals(Color.BLACK, shape.getFill());
    }
  }

  @Nested
  @DisplayName("Reduced motion")
  class MotionTests {

    @Test
    @DisplayName("setReducedMotion toggles shouldAnimate")
    void reducedMotion() {
      String previous = System.getProperty("tailwindfx.motion.reduced");
      try {
        Styles.setReducedMotion(true);
        assertFalse(Styles.shouldAnimate());
        Styles.setReducedMotion(false);
        assertTrue(Styles.shouldAnimate());
      } finally {
        if (previous == null) {
          System.clearProperty("tailwindfx.motion.reduced");
        } else {
          System.setProperty("tailwindfx.motion.reduced", previous);
        }
      }
    }

    @Test
    @DisplayName("playIfMotionOk plays the animation without throwing")
    void playIfMotionOk() {
      Styles.setReducedMotion(false);
      assertDoesNotThrow(() -> Styles.playIfMotionOk(TwAnimation.fadeIn(region)));
      Styles.setReducedMotion(true);
      assertDoesNotThrow(() -> Styles.playIfMotionOk(TwAnimation.fadeIn(region)));
      System.clearProperty("tailwindfx.motion.reduced");
    }
  }

  @Nested
  @DisplayName("Presets, interaction and layout")
  class MiscTests {

    @Test
    @DisplayName("glass / glassDark / neumorph / neumorphInset set style and effect")
    void presets() {
      Region node = new Region();
      Styles.glass(node);
      assertTrue(node.getStyle().contains("rgba(255,255,255,0.15)"));
      assertNotNull(node.getEffect());
      Styles.glassDark(node);
      assertTrue(node.getStyle().contains("rgba(0,0,0,0.20)"));
      Styles.neumorph(node);
      assertInstanceOf(DropShadow.class, node.getEffect());
      Styles.neumorphInset(node);
      assertInstanceOf(InnerShadow.class, node.getEffect());
    }

    @Test
    @DisplayName("user-select helpers toggle mouse transparency")
    void userSelect() {
      Styles.selectNone(region);
      assertTrue(region.isMouseTransparent());
      Styles.selectText(region);
      assertFalse(region.isMouseTransparent());
      Styles.selectAuto(region);
      assertFalse(region.isMouseTransparent());
      Styles.selectAll(new Label("x"));
    }

    @Test
    @DisplayName("touch-action helpers store the property")
    void touchActions() {
      Region node = new Region();
      Styles.touchNone(node);
      assertEquals("none", node.getProperties().get("touch-action"));
      Styles.touchPanX(node);
      assertEquals("pan-x", node.getProperties().get("touch-action"));
      Styles.touchPanY(node);
      assertEquals("pan-y", node.getProperties().get("touch-action"));
      Styles.touchPinchZoom(node);
      assertEquals("pinch-zoom", node.getProperties().get("touch-action"));
      Styles.touchManipulation(node);
      assertEquals("manipulation", node.getProperties().get("touch-action"));
      Styles.touchAuto(node);
      assertNull(node.getProperties().get("touch-action"));
    }

    @Test
    @DisplayName("scroll-snap helpers store the properties")
    void snapHelpers() {
      Region node = new Region();
      Styles.snapStart(node);
      assertEquals("start", node.getProperties().get("scroll-snap-align"));
      Styles.snapEnd(node);
      Styles.snapCenter(node);
      Styles.snapAlignNone(node);
      Styles.snapX(node);
      assertEquals("x", node.getProperties().get("scroll-snap-type"));
      Styles.snapY(node);
      Styles.snapBoth(node);
      Styles.snapMandatory(node);
      assertEquals("mandatory", node.getProperties().get("scroll-snap-stop"));
      Styles.snapProximity(node);
    }

    @Test
    @DisplayName("columns helpers store the count and size the pane")
    void columns() {
      Pane pane = new Pane();
      Styles.columns1(pane);
      assertEquals("1", pane.getProperties().get("columns"));
      assertEquals(100, pane.getPrefWidth());
      Styles.columns12(new Pane());
      Styles.columns3(new Pane());
      Styles.columns6(new Pane());
      Pane auto = new Pane();
      Styles.columnsAuto(auto);
      assertEquals("auto", auto.getProperties().get("columns"));
    }

    @Test
    @DisplayName("isKnownUtilityClass accepts valid tokens and rejects typos")
    void knownUtilityClass() {
      assertTrue(Styles.isKnownUtilityClass("rounded-lg"));
      assertTrue(Styles.isKnownUtilityClass("bg-red-500"));
      assertTrue(Styles.isKnownUtilityClass("p-4"));
      assertTrue(Styles.isKnownUtilityClass("gap-4"));
      assertTrue(Styles.isKnownUtilityClass("flex-col"));
      assertTrue(Styles.isKnownUtilityClass("-mt-2"));
      assertTrue(Styles.isKnownUtilityClass("btn"));
      assertTrue(Styles.isKnownUtilityClass("dark:text-white"));
      assertFalse(Styles.isKnownUtilityClass("pd-4"));
      assertFalse(Styles.isKnownUtilityClass("foo-bar"));
      assertFalse(Styles.isKnownUtilityClass(""));
      assertFalse(Styles.isKnownUtilityClass(null));
    }
  }

  @Nested
  @DisplayName("Chainability")
  class ChainTests {

    @Test
    @DisplayName("every helper returns the same node for fluent use")
    void fluent() {
      GridPane grid = new GridPane();
      Region cell = new Region();
      grid.add(cell, 0, 0);
      Node result =
          Styles.colSpan(
              Styles.rowSpan(Styles.gridCell(Styles.z(Styles.grayscale(cell), 10), 0, 0), 2), 3);
      assertSame(cell, result);
    }
  }
}
