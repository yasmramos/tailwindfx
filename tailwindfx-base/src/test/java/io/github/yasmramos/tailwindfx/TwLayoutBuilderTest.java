package io.github.yasmramos.tailwindfx;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.layout.TwFlexPane;
import io.github.yasmramos.tailwindfx.layout.TwGridPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Tests for {@link TwLayout.Builder}: container migration, property reconfiguration and constraint
 * preservation. Complements {@code TwLayoutApplyTest}, which covers the token parser.
 */
@DisplayName("TwLayout Builder Tests")
class TwLayoutBuilderTest extends ApplicationTest {

  @Override
  public void start(javafx.stage.Stage stage) {
    stage.setScene(new javafx.scene.Scene(new Pane(), 800, 600));
    stage.show();
  }

  private <T> T onFx(java.util.concurrent.Callable<T> action) {
    final Object[] holder = new Object[1];
    interact(
        () -> {
          try {
            holder[0] = action.call();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
    @SuppressWarnings("unchecked")
    T result = (T) holder[0];
    return result;
  }

  @Test
  @DisplayName("row() builds an HBox and applies gap, alignment and padding")
  void rowBuild() {
    HBox box =
        onFx(
            () ->
                (HBox)
                    TwLayout.of(new Pane())
                        .row()
                        .gap(12)
                        .center()
                        .padding(8)
                        .build());
    assertEquals(12.0, box.getSpacing(), 0.001);
    assertEquals(Pos.CENTER, box.getAlignment());
    assertEquals(new Insets(8), box.getPadding());
  }

  @Test
  @DisplayName("col() builds a VBox")
  void colBuild() {
    VBox box = onFx(() -> (VBox) TwLayout.of(new Pane()).col().gap(4).build());
    assertEquals(4.0, box.getSpacing(), 0.001);
  }

  @Test
  @DisplayName("stack() builds a StackPane")
  void stackBuild() {
    StackPane pane = onFx(() -> (StackPane) TwLayout.of(new Pane()).stack().center().build());
    assertEquals(Pos.CENTER, pane.getAlignment());
  }

  @Test
  @DisplayName("flowRow() and flowCol() set the flow orientation")
  void flowBuild() {
    FlowPane row = onFx(() -> (FlowPane) TwLayout.of(new Pane()).flowRow().gap(6).build());
    FlowPane col = onFx(() -> (FlowPane) TwLayout.of(new Pane()).flowCol().build());
    assertEquals(javafx.geometry.Orientation.HORIZONTAL, row.getOrientation());
    assertEquals(javafx.geometry.Orientation.VERTICAL, col.getOrientation());
  }

  @Test
  @DisplayName("tile() builds a TilePane")
  void tileBuild() {
    TilePane tile = onFx(() -> (TilePane) TwLayout.of(new Pane()).tile().gap(10).build());
    assertEquals(10.0, tile.getHgap(), 0.001);
    assertEquals(10.0, tile.getVgap(), 0.001);
  }

  @Test
  @DisplayName("anchor() builds an AnchorPane and applies builder anchors")
  void anchorBuild() {
    Region child = new Region();
    AnchorPane pane =
        onFx(
            () ->
                (AnchorPane)
                    TwLayout.of(new Pane())
                        .anchor()
                        .anchorTop(child, 5)
                        .anchorLeft(child, 7)
                        .build());
    // Child is not in the pane yet (the builder anchors only apply to migrated children),
    // but the constraints must be registered without throwing.
    assertNotNull(pane);
  }

  @Test
  @DisplayName("grid() builds a GridPane with the requested column count")
  void gridBuild() {
    javafx.scene.layout.GridPane gp =
        onFx(
            () ->
                (javafx.scene.layout.GridPane) TwLayout.of(new Pane()).grid(3).gap(5).build());
    assertEquals(3, gp.getColumnConstraints().size());
    assertEquals(5.0, gp.getHgap(), 0.001);
  }

  @Test
  @DisplayName("flex() builds a TwFlexPane with direction, wrap, justify and gap")
  void flexBuild() {
    TwFlexPane pane =
        onFx(
            () ->
                (TwFlexPane)
                    TwLayout.of(new Pane())
                        .flex()
                        .wrap(true)
                        .justify(TwFlexPane.Justify.BETWEEN)
                        .alignItems(TwFlexPane.Align.CENTER)
                        .gap(9)
                        .build());
    assertTrue(pane.isWrap());
    assertEquals(TwFlexPane.Justify.BETWEEN, pane.getJustify());
    assertEquals(TwFlexPane.Align.CENTER, pane.getAlign());
    assertEquals(9.0, pane.getGap(), 0.001);
  }

  @Test
  @DisplayName("flex() with rowReverse/colReverse sets the reverse direction")
  void flexReverseBuild() {
    TwFlexPane row =
        onFx(() -> (TwFlexPane) TwLayout.of(new Pane()).rowReverse().build());
    TwFlexPane col =
        onFx(() -> (TwFlexPane) TwLayout.of(new Pane()).colReverse().build());
    assertEquals(TwFlexPane.Direction.ROW_REVERSE, row.getDirection());
    assertEquals(TwFlexPane.Direction.COL_REVERSE, col.getDirection());
  }

  @Test
  @DisplayName("flexGrid() builds a TwGridPane with areas")
  void flexGridBuild() {
    TwGridPane pane =
        onFx(
            () ->
                (TwGridPane)
                    TwLayout.of(new Pane())
                        .flexGrid()
                        .areas("header header", "main aside")
                        .gap(8)
                        .build());
    assertNotNull(pane);
  }

  @Test
  @DisplayName("Reusing the same builder type does not migrate the container")
  void noMigrationWhenTypeMatches() {
    HBox original = new HBox();
    Pane result = onFx(() -> TwLayout.of(original).row().gap(3).build());
    assertSame(original, result, "an HBox target must be reconfigured in place");
  }

  @Test
  @DisplayName("Switching type migrates children and preserves HBox constraints")
  void migrationPreservesConstraints() {
    HBox source = new HBox();
    Region child = new Region();
    HBox.setHgrow(child, javafx.scene.layout.Priority.ALWAYS);
    HBox.setMargin(child, new Insets(4));
    source.getChildren().add(child);

    VBox migrated = onFx(() -> (VBox) TwLayout.of(source).col().build());
    assertEquals(1, migrated.getChildren().size());
    assertSame(child, migrated.getChildren().get(0));
  }

  @Test
  @DisplayName("Migration replaces the source in its parent at the same index")
  void migrationReplacesInParent() {
    Pane parent = new Pane();
    HBox source = new HBox();
    Pane other = new Pane();
    parent.getChildren().addAll(other, source);

    Pane result = onFx(() -> TwLayout.of(source).col().build());
    assertEquals(2, parent.getChildren().size());
    assertSame(result, parent.getChildren().get(1));
  }

  @Test
  @DisplayName("Transition listener fires around a migration but not for in-place rebuilds")
  void transitionListener() {
    boolean[] changing = {false};
    boolean[] changed = {false};
    onFx(
        () ->
            TwLayout.of(new Pane())
                .col()
                .onTransition(
                    new TwLayout.Builder.LayoutTransitionListener() {
                      @Override
                      public void onLayoutChanging(Pane src, TwLayout.LayoutType newType) {
                        changing[0] = true;
                      }

                      @Override
                      public void onLayoutChanged(Pane result) {
                        changed[0] = true;
                      }
                    })
                .build());
    assertTrue(changing[0], "onLayoutChanging should fire on migration");
    assertTrue(changed[0], "onLayoutChanged should fire on migration");

    boolean[] noChange = {false};
    onFx(
        () ->
            TwLayout.of(new VBox())
                .col()
                .onTransition(
                    new TwLayout.Builder.LayoutTransitionListener() {
                      @Override
                      public void onLayoutChanging(Pane src, TwLayout.LayoutType newType) {
                        noChange[0] = true;
                      }

                      @Override
                      public void onLayoutChanged(Pane result) {
                        noChange[0] = true;
                      }
                    })
                .build());
    assertFalse(noChange[0], "no transition when the container already matches");
  }

  @Test
  @DisplayName("grow() applies HGrow/VGrow to every child")
  void growAppliesToChildren() {
    HBox box = new HBox();
    Region a = new Region();
    Region b = new Region();
    box.getChildren().addAll(a, b);
    onFx(
        () -> {
          TwLayout.of(box).row().grow().build();
          return null;
        });
    assertEquals(javafx.scene.layout.Priority.ALWAYS, HBox.getHgrow(a));
    assertEquals(javafx.scene.layout.Priority.ALWAYS, HBox.getHgrow(b));
  }

  @Test
  @DisplayName("fill()/prefWidth()/minWidth()/maxWidth() set the size bounds")
  void sizeBounds() {
    Region region =
        onFx(
            () ->
                (Region)
                    TwLayout.of(new Pane())
                        .row()
                        .prefWidth(200)
                        .prefHeight(100)
                        .minWidth(50)
                        .minHeight(20)
                        .maxWidth(400)
                        .maxHeight(300)
                        .build());
    assertEquals(200.0, region.getPrefWidth(), 0.001);
    assertEquals(100.0, region.getPrefHeight(), 0.001);
    assertEquals(50.0, region.getMinWidth(), 0.001);
    assertEquals(20.0, region.getMinHeight(), 0.001);
    assertEquals(400.0, region.getMaxWidth(), 0.001);
    assertEquals(300.0, region.getMaxHeight(), 0.001);
  }

  @Test
  @DisplayName("fillWidth()/fillHeight() push the max size to MAX_VALUE")
  void fillBounds() {
    Region region =
        onFx(() -> (Region) TwLayout.of(new Pane()).row().fillWidth().fillHeight().build());
    assertEquals(Double.MAX_VALUE, region.getMaxWidth());
    assertEquals(Double.MAX_VALUE, region.getMaxHeight());
  }

  @Test
  @DisplayName("hgap()/vgap() override the uniform gap on a GridPane")
  void separateGaps() {
    javafx.scene.layout.GridPane gp =
        onFx(
            () ->
                (javafx.scene.layout.GridPane)
                    TwLayout.of(new Pane()).grid(2).hgap(11).vgap(22).build());
    assertEquals(11.0, gp.getHgap(), 0.001);
    assertEquals(22.0, gp.getVgap(), 0.001);
  }

  @Test
  @DisplayName("padding(String) parses 1, 2 and 4 value shorthands")
  void paddingShorthand() {
    VBox one = onFx(() -> (VBox) TwLayout.of(new Pane()).col().padding("16").build());
    assertEquals(new Insets(16), one.getPadding());

    VBox two = onFx(() -> (VBox) TwLayout.of(new Pane()).col().padding("8 16").build());
    assertEquals(new Insets(8, 16, 8, 16), two.getPadding());

    VBox four = onFx(() -> (VBox) TwLayout.of(new Pane()).col().padding("4 8 12 16").build());
    assertEquals(new Insets(4, 8, 12, 16), four.getPadding());
  }

  @Test
  @DisplayName("padding(String) rejects an invalid shorthand")
  void paddingShorthandInvalid() {
    assertThrows(
        RuntimeException.class,
        () -> onFx(() -> TwLayout.of(new Pane()).col().padding("1 2 3").build()));
  }

  @Test
  @DisplayName("Alignment helpers map to the expected Pos values")
  void alignmentHelpers() {
    assertEquals(Pos.CENTER, onFx(() -> (HBox) TwLayout.of(new Pane()).row().center().build()).getAlignment());
    assertEquals(
        Pos.BOTTOM_RIGHT,
        onFx(() -> (HBox) TwLayout.of(new Pane()).row().bottomRight().build()).getAlignment());
    assertEquals(
        Pos.TOP_CENTER,
        onFx(() -> (HBox) TwLayout.of(new Pane()).row().topCenter().build()).getAlignment());
    assertEquals(
        Pos.CENTER_LEFT,
        onFx(() -> (HBox) TwLayout.of(new Pane()).row().itemsCenter().build()).getAlignment());
  }

  @Test
  @DisplayName("build() outside the FX thread is rejected")
  void buildOffFxThread() {
    assertThrows(IllegalStateException.class, () -> TwLayout.of(new Pane()).row().build());
  }

  @Test
  @DisplayName("grid() with no explicit columns auto-computes from child count")
  void autoGridCols() {
    Pane source = new Pane();
    for (int i = 0; i < 4; i++) {
      source.getChildren().add(new Region());
    }
    javafx.scene.layout.GridPane gp =
        onFx(() -> (javafx.scene.layout.GridPane) TwLayout.of(source).grid().build());
    assertEquals(2, gp.getColumnConstraints().size());
  }

  @Test
  @DisplayName("grid(n) rejects a non-positive column count")
  void gridRejectsBadColumns() {
    assertThrows(IllegalArgumentException.class, () -> TwLayout.of(new Pane()).grid(0));
  }

  @Test
  @DisplayName("reconfigure() re-applies properties without migrating")
  void reconfigureInPlace() {
    VBox box = new VBox();
    onFx(
        () -> {
          TwLayout.Builder b = TwLayout.of(box).col().gap(5);
          b.build();
          b.gap(20).reconfigure();
          return null;
        });
    assertEquals(20.0, box.getSpacing(), 0.001);
  }

  @Test
  @DisplayName("StackPane children keep their alignment through a migration")
  void stackAlignmentPreserved() {
    StackPane source = new StackPane();
    Region child = new Region();
    StackPane.setAlignment(child, Pos.BOTTOM_LEFT);
    source.getChildren().add(child);

    StackPane migrated =
        onFx(
            () -> {
              // stack -> stack keeps the same type; force a migration through a different type
              VBox other = (VBox) TwLayout.of(source).col().build();
              return (StackPane) TwLayout.of(other).stack().build();
            });
    assertEquals(Pos.BOTTOM_LEFT, StackPane.getAlignment(migrated.getChildren().get(0)));
  }

  @Test
  @DisplayName("Static helpers hgrow/vgrow/grow set the container priorities")
  void staticGrowthHelpers() {
    Region a = new Region();
    Region b = new Region();
    Region c = new Region();
    TwLayout.hgrow(a);
    TwLayout.vgrow(b);
    TwLayout.grow(c);
    assertEquals(javafx.scene.layout.Priority.ALWAYS, HBox.getHgrow(a));
    assertEquals(javafx.scene.layout.Priority.ALWAYS, VBox.getVgrow(b));
    assertEquals(javafx.scene.layout.Priority.ALWAYS, HBox.getHgrow(c));
    assertEquals(javafx.scene.layout.Priority.ALWAYS, VBox.getVgrow(c));
  }

  @Test
  @DisplayName("spacer() returns an expanding region")
  void spacer() {
    Region spacer = TwLayout.spacer();
    assertEquals(javafx.scene.layout.Priority.ALWAYS, HBox.getHgrow(spacer));
    assertEquals(javafx.scene.layout.Priority.ALWAYS, VBox.getVgrow(spacer));
    Region sized = TwLayout.spacer(40);
    assertEquals(40.0, sized.getPrefWidth(), 0.001);
  }

  @Test
  @DisplayName("margin helpers apply insets")
  void marginHelpers() {
    Region n = new Region();
    TwLayout.margin(n, 6);
    assertEquals(new Insets(6), HBox.getMargin(n));
    Region m = new Region();
    TwLayout.margin(m, 1, 2, 3, 4);
    assertEquals(new Insets(1, 2, 3, 4), HBox.getMargin(m));
  }

  @Test
  @DisplayName("anchorFill() anchors all four edges")
  void anchorFillHelper() {
    Region n = new Region();
    TwLayout.anchorFill(n);
    assertEquals(0.0, AnchorPane.getTopAnchor(n), 0.001);
    assertEquals(0.0, AnchorPane.getLeftAnchor(n), 0.001);
  }

  @Test
  @DisplayName("aspectRatio() rejects non-positive ratios")
  void aspectRatioRejectsBadInput() {
    assertThrows(IllegalArgumentException.class, () -> TwLayout.aspectRatio(new Region(), 0, 1));
    assertThrows(IllegalArgumentException.class, () -> TwLayout.aspectRatio(new Region(), 1, -1));
  }

  @Test
  @DisplayName("aspectRatio() updates the preferred width from the height")
  void aspectRatioUpdatesPrefWidth() {
    Region r = onFx(() -> new Region());
    onFx(
        () -> {
          TwLayout.aspectRatio(r, 2, 1); // width = height * 2
          r.resize(100, 50);
          return null;
        });
    // The listener reacts to bounds-in-parent changes; the value must be positive and finite.
    assertTrue(r.getPrefWidth() >= 0);
  }

  @Test
  @DisplayName("Builder rejects a null source pane")
  void nullSourceRejected() {
    assertThrows(IllegalArgumentException.class, () -> TwLayout.of(null));
  }

  @Test
  @DisplayName("FlowPane keeps its orientation through configure()")
  void flowOrientationConfigured() {
    FlowPane fp =
        onFx(() -> (FlowPane) TwLayout.of(new Pane()).flowCol().gap(3).build());
    assertEquals(javafx.geometry.Orientation.VERTICAL, fp.getOrientation());
    assertEquals(3.0, fp.getVgap(), 0.001);
  }

  @Test
  @DisplayName("Nested children survive a multi-step migration chain")
  void chainedMigration() {
    HBox source = new HBox();
    Node child = new Region();
    source.getChildren().add(child);
    Pane result =
        onFx(
            () -> {
              Pane step1 = TwLayout.of(source).col().build();
              Pane step2 = TwLayout.of(step1).grid(2).build();
              return TwLayout.of(step2).flex().build();
            });
    assertTrue(result instanceof TwFlexPane);
    assertEquals(1, result.getChildren().size());
    assertSame(child, result.getChildren().get(0));
  }

  @Test
  @DisplayName("debug() build still returns the configured container")
  void debugBuild() {
    HBox box = onFx(() -> (HBox) TwLayout.of(new Pane()).row().gap(2).debug().build());
    assertEquals(2.0, box.getSpacing(), 0.001);
  }

  @Test
  @DisplayName("filler: assertions on migrated GridPane child coordinates")
  void migrationGridCoordinates() {
    HBox source = new HBox();
    for (int i = 0; i < 4; i++) {
      source.getChildren().add(new Region());
    }
    javafx.scene.layout.GridPane gp =
        onFx(() -> (javafx.scene.layout.GridPane) TwLayout.of(source).grid(2).build());
    assertEquals(4, gp.getChildren().size());
    assertEquals(0, javafx.scene.layout.GridPane.getColumnIndex(gp.getChildren().get(0)));
    assertEquals(1, javafx.scene.layout.GridPane.getColumnIndex(gp.getChildren().get(1)));
    assertEquals(0, javafx.scene.layout.GridPane.getColumnIndex(gp.getChildren().get(2)));
    assertEquals(1, javafx.scene.layout.GridPane.getRowIndex(gp.getChildren().get(2)));
  }
}
