package io.github.yasmramos.tailwindfx;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Tests for {@link TwLayout#apply} spacing tokens.
 *
 * <p>Regression coverage for arbitrary values: {@code gap-[16px]} used to throw a
 * {@link NumberFormatException} (uncaught) and, when it did not, the value was still multiplied by
 * the spacing unit, yielding 64px instead of 16px.
 */
@DisplayName("TwLayout apply Tests")
class TwLayoutApplyTest extends ApplicationTest {

  @Override
  public void start(javafx.stage.Stage stage) {
    Pane root = new Pane();
    stage.setScene(new Scene(root, 400, 300));
    stage.show();
  }

  private HBox containerWithGap(String token) {
    HBox box = new HBox();
    interact(() -> TwLayout.apply(box, token));
    return box;
  }

  @Test
  @DisplayName("gap-4 applies the scale value (4 x unit = 16px)")
  void testScaleGap() {
    assertEquals(16.0, containerWithGap("gap-4").getSpacing());
  }

  @Test
  @DisplayName("gap-[16px] applies 16px, not 16 x unit")
  void testArbitraryPxGap() {
    assertEquals(16.0, containerWithGap("gap-[16px]").getSpacing());
  }

  @Test
  @DisplayName("gap-[1.5rem] resolves through the spacing unit (1.5 x 4 = 6px)")
  void testArbitraryRemGap() {
    assertEquals(6.0, containerWithGap("gap-[1.5rem]").getSpacing());
  }

  @Test
  @DisplayName("Non-numeric arbitrary gap is ignored instead of throwing")
  void testNonNumericArbitraryGapIsIgnored() {
    assertEquals(0.0, containerWithGap("gap-[calc(1rem+2px)]").getSpacing());
  }

  @Test
  @DisplayName("Arbitrary and non-numeric grid counts do not throw")
  void testGridCountsAreGuarded() {
    io.github.yasmramos.tailwindfx.layout.TwGridPane grid =
        io.github.yasmramos.tailwindfx.layout.TwGridPane.create().build();

    // Before the fix, a non-numeric arbitrary count ("repeat(3,1fr)") reached
    // Integer.parseInt and threw an uncaught NumberFormatException.
    interact(() -> TwLayout.apply(grid, "grid-cols-[repeat(3,1fr)]"));
    interact(() -> TwLayout.apply(grid, "grid-cols-3"));
    interact(() -> TwLayout.apply(grid, "col-span-abc"));
  }
}
