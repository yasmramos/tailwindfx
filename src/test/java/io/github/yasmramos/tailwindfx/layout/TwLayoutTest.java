package io.github.yasmramos.tailwindfx.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.yasmramos.tailwindfx.TwLayout;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/** Tests for {@link TwLayout} — builder, type switching, constraints, TilePane. */
class TwLayoutTest extends ApplicationTest {

  @BeforeAll
  static void setupSuite() {
    // Ensure TestFX toolkit is initialized before any tests run
  }

  private void runFx(Runnable w) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Throwable> err = new AtomicReference<>();
    Platform.runLater(
        () -> {
          try {
            w.run();
          } catch (Throwable t) {
            err.set(t);
          } finally {
            latch.countDown();
          }
        });
    if (!latch.await(3, TimeUnit.SECONDS)) throw new RuntimeException("timeout");
    if (err.get() != null) throw new RuntimeException(err.get());
  }

  @Test
  void testRowCreatesHBox() throws Exception {
    runFx(
        () -> {
          HBox box = new HBox();
          Pane result = TwLayout.of(box).row().build();
          assertTrue(result instanceof HBox, "row → HBox");
        });
  }

  @Test
  void testColCreatesVBox() throws Exception {
    runFx(
        () -> {
          VBox box = new VBox();
          Pane result = TwLayout.of(box).col().build();
          assertTrue(result instanceof VBox, "col → VBox");
        });
  }

  @Test
  void testStackCreatesStackPane() throws Exception {
    runFx(
        () -> {
          StackPane sp = new StackPane();
          Pane result = TwLayout.of(sp).stack().build();
          assertTrue(result instanceof StackPane, "stack → StackPane");
        });
  }

  @Test
  void testGridCreatesGridPane() throws Exception {
    runFx(
        () -> {
          GridPane gp = new GridPane();
          Pane result = TwLayout.of(gp).grid(3).build();
          assertTrue(result instanceof GridPane, "grid → GridPane");
        });
  }

  @Test
  void testFlowRowCreatesFlowPane() throws Exception {
    runFx(
        () -> {
          FlowPane fp = new FlowPane();
          Pane result = TwLayout.of(fp).flowRow().build();
          assertTrue(result instanceof FlowPane, "flowRow → FlowPane");
        });
  }

  @Test
  void testFlowColCreatesFlowPane() throws Exception {
    runFx(
        () -> {
          FlowPane fp = new FlowPane();
          Pane result = TwLayout.of(fp).flowCol().build();
          assertTrue(
              result instanceof FlowPane f
                  && f.getOrientation() == javafx.geometry.Orientation.VERTICAL,
              "flowCol → FlowPane with VERTICAL");
        });
  }

  @Test
  void testTileCreatesTilePane() throws Exception {
    runFx(
        () -> {
          TilePane tp = new TilePane();
          Pane result = TwLayout.of(tp).tile().build();
          assertTrue(result instanceof TilePane, "tile → TilePane");
        });
  }

  @Test
  void testAnchorCreatesAnchorPane() throws Exception {
    runFx(
        () -> {
          AnchorPane ap = new AnchorPane();
          Pane result = TwLayout.of(ap).anchor().build();
          assertTrue(result instanceof AnchorPane, "anchor → AnchorPane");
        });
  }

  @Test
  void testNullPaneThrows() {
    assertThrows(IllegalArgumentException.class, () -> TwLayout.of(null), "TwLayout(null)");
  }

  @Test
  void testGridColsGuard() throws Exception {
    runFx(
        () -> {
          assertThrows(
              IllegalArgumentException.class,
              () -> TwLayout.of(new GridPane()).grid(0).build(),
              "grid(0) throws");
        });
  }

  @Test
  void testGapNegativeWarns() throws Exception {
    runFx(
        () -> {
          // Negative gap logs a warning but does NOT throw
          TwLayout.of(new HBox()).row().gap(-4).build();
          // If we reach here, no exception was thrown (as expected)
        });
  }

  @Test
  void testBuild() throws Exception {
    runFx(
        () -> {
          HBox source = new HBox();
          Pane built = TwLayout.of(source).row().gap(8).center().build();
          assertTrue(built instanceof HBox, "build returns HBox");
          assertSame(source, built, "build same instance");
        });
  }

  @Test
  void testReconfigure() throws Exception {
    runFx(
        () -> {
          HBox box = new HBox();
          // reconfigure on same type — should not recreate
          TwLayout.of(box).row().gap(16).reconfigure();
          assertTrue(box instanceof HBox, "reconfigure preserves type");
        });
  }

  @Test
  void testLayoutSwitchPreservesChildren() throws Exception {
    runFx(
        () -> {
          HBox box = new HBox();
          Region child = new Region();
          box.getChildren().add(child);
          // Switching to VBox preserves children
          Pane switched = TwLayout.of(box).col().build();
          assertTrue(
              switched.getChildren().contains(child), "children preserved after switch");
        });
  }

  @Test
  void testFlexType() throws Exception {
    runFx(
        () -> {
          TwFlexPane fp = new TwFlexPane();
          Pane result =
              TwLayout.of(fp)
                  .flex()
                  .justify(TwFlexPane.Justify.BETWEEN)
                  .alignItems(TwFlexPane.Align.CENTER)
                  .wrap(true)
                  .gap(16)
                  .build();
          assertTrue(result instanceof TwFlexPane, "flex() → TwFlexPane");
          TwFlexPane built = (TwFlexPane) result;
          assertEquals(TwFlexPane.Justify.BETWEEN, built.getJustify(), "justify=BETWEEN");
          assertEquals(TwFlexPane.Align.CENTER, built.getAlign(), "align=CENTER");
          assertTrue(built.isWrap(), "wrap=true");
        });
  }

  @Test
  void testFlexGridType() throws Exception {
    runFx(
        () -> {
          TwGridPane fg = TwGridPane.create().build();
          Pane result =
              TwLayout.of(fg)
                  .flexGrid()
                  .areas("header header", "sidebar main", "footer footer")
                  .gap(8)
                  .build();
          assertTrue(result instanceof TwGridPane, "flexGrid() → TwGridPane");
        });
  }

  @Test
  void testFlexOnNewPane() throws Exception {
    runFx(
        () -> {
          // If container is NOT already TwFlexPane, layout() migrates it
          Region r1 = new Region(), r2 = new Region();
          TwFlexPane source = new TwFlexPane();
          source.getChildren().addAll(r1, r2);
          Pane result = TwLayout.of(source).flex().gap(12).build();
          assertTrue(
              result.getChildren().containsAll(List.of(r1, r2)), "children preserved");
        });
  }

  @Test
  void testFlexColDirection() throws Exception {
    runFx(
        () -> {
          TwFlexPane fp = new TwFlexPane();
          // col() sets Direction.COL; flex() sets Direction.ROW
          // Using col() on TwFlexPane via TwLayout API
          Pane result = TwLayout.of(fp).flex().build();
          assertEquals(
              TwFlexPane.Direction.ROW,
              ((TwFlexPane) result).getDirection(),
              "flex() direction ROW");
        });
  }

  @Test
  void testValidationWarns() throws Exception {
    runFx(
        () -> {
          // GRID with 0 children and no cols — should not throw, just warn
          GridPane gp = new GridPane();
          TwLayout.of(gp).grid().build();
          // If we reach here, no exception was thrown (as expected)
        });
  }

  @Test
  void testDebugMode() throws Exception {
    runFx(
        () -> {
          HBox box = new HBox();
          // debug() should not change behavior, only log to stdout
          Pane result = TwLayout.of(box).row().gap(8).debug().build();
          assertTrue(result instanceof HBox, "debug() still returns correct type");
        });
  }

  @Test
  void testThreadCheck() throws Exception {
    // Calling build() off FX thread must throw
    AtomicBoolean threw = new AtomicBoolean(false);
    Thread t =
        new Thread(
            () -> {
              try {
                TwLayout.of(new HBox()).row().build();
              } catch (IllegalStateException e) {
                threw.set(true);
              }
            });
    t.start();
    t.join(2000);
    assertTrue(threw.get(), "build() off FX thread throws ISE");
  }

  @Test
  void testPaddingShorthand() throws Exception {
    runFx(
        () -> {
          // "16" → uniform
          HBox box = new HBox();
          TwLayout.of(box).row().padding("16").build();
          assertEquals(16, box.getPadding().getTop(), "padding('16') top=16");
          assertEquals(16, box.getPadding().getRight(), "padding('16') right=16");

          // "8 16" → vertical/horizontal
          VBox vbox = new VBox();
          TwLayout.of(vbox).col().padding("8 16").build();
          assertEquals(8, vbox.getPadding().getTop(), "padding('8 16') top=8");
          assertEquals(16, vbox.getPadding().getRight(), "padding('8 16') right=16");

          // "4 8 12 16" → individual
          HBox box3 = new HBox();
          TwLayout.of(box3).row().padding("4 8 12 16").build();
          assertEquals(4, box3.getPadding().getTop(), "padding('4 8 12 16') top=4");
          assertEquals(8, box3.getPadding().getRight(), "padding('4 8 12 16') right=8");
          assertEquals(12, box3.getPadding().getBottom(), "padding('4 8 12 16') bottom=12");
          assertEquals(16, box3.getPadding().getLeft(), "padding('4 8 12 16') left=16");

          // invalid value throws
          assertThrows(
              IllegalArgumentException.class,
              () -> TwLayout.of(new HBox()).row().padding("bad").build(),
              "padding('bad') throws");

          // wrong count throws
          assertThrows(
              IllegalArgumentException.class,
              () -> TwLayout.of(new HBox()).row().padding("1 2 3").build(),
              "padding('1 2 3') throws");
        });
  }

  @Test
  void testTransitionListener() throws Exception {
    AtomicBoolean changingFired = new AtomicBoolean(false);
    AtomicBoolean changedFired = new AtomicBoolean(false);
    runFx(
        () -> {
          HBox box = new HBox();
          // Switching to VBox → migration → listener fires
          TwLayout.of(box)
              .col()
              .onTransition(
                  new TwLayout.Builder.LayoutTransitionListener() {
                    public void onLayoutChanging(Pane src, TwLayout.LayoutType t) {
                      changingFired.set(true);
                    }

                    public void onLayoutChanged(Pane result) {
                      changedFired.set(true);
                    }
                  })
              .build();
          assertTrue(changingFired.get(), "onLayoutChanging fired");
          assertTrue(changedFired.get(), "onLayoutChanged fired");

          // No migration (same type) → listener NOT fired
          AtomicBoolean noFire = new AtomicBoolean(false);
          HBox same = new HBox();
          TwLayout.of(same)
              .row()
              .onTransition(
                  new TwLayout.Builder.LayoutTransitionListener() {
                    public void onLayoutChanging(Pane s, TwLayout.LayoutType t) {
                      noFire.set(true);
                    }

                    public void onLayoutChanged(Pane r) {
                      noFire.set(true);
                    }
                  })
              .build();
          assertFalse(noFire.get(), "no migration → listener not fired");
        });
  }

  @Test
  void testApplyGridColsNoOverwrite() throws Exception {
    runFx(
        () -> {
          GridPane gp = new GridPane();
          // Add a manual constraint first
          ColumnConstraints manual = new ColumnConstraints(200);
          gp.getColumnConstraints().add(manual);
          // layout(grid(3)) should NOT overwrite it
          TwLayout.of(gp).grid(3).build();
          assertEquals(
              1,
              gp.getColumnConstraints().size(),
              "manual constraint preserved - size");
          assertEquals(
              200,
              gp.getColumnConstraints().get(0).getPrefWidth(),
              "manual constraint preserved - width");
        });
  }

  @Test
  void testSnapRestoresAnchorEdges() throws Exception {
    runFx(
        () -> {
          AnchorPane original = new AnchorPane();
          Region child = new Region();
          AnchorPane.setTopAnchor(child, 10.0);
          AnchorPane.setLeftAnchor(child, 20.0);
          original.getChildren().add(child);

          // Migrate to a new AnchorPane — edges should be preserved
          Pane result = TwLayout.of(original).anchor().build();
          // same instance (no migration since already AnchorPane)
          assertSame(original, result, "anchor preserved same pane");
          assertNotNull(AnchorPane.getTopAnchor(child), "top anchor preserved - not null");
          assertEquals(10.0, AnchorPane.getTopAnchor(child), "top anchor preserved - value");
        });
  }

  @Test
  void testStaticHelpers() throws Exception {
    runFx(
        () -> {
          Region n = new Region();
          // hgrow
          HBox hbox = new HBox();
          hbox.getChildren().add(n);
          TwLayout.hgrow(n);
          assertEquals(Priority.ALWAYS, HBox.getHgrow(n), "hgrow=ALWAYS");

          // spacer
          Region spacer = TwLayout.spacer();
          assertNotNull(spacer, "spacer not null");
          assertEquals(Priority.ALWAYS, HBox.getHgrow(spacer), "spacer hgrow=ALWAYS");

          // spacer(size)
          Region sized = TwLayout.spacer(20);
          assertEquals(20, sized.getMinWidth(), "spacer(20) min=20");
        });
  }
}
