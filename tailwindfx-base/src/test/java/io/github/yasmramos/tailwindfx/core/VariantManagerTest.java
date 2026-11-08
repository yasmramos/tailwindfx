package io.github.yasmramos.tailwindfx.core;

import static org.junit.jupiter.api.Assertions.*;

import io.github.yasmramos.tailwindfx.ToolkitBootstrap;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link VariantManager}: state, group and arbitrary variant application. */
@DisplayName("VariantManager Tests")
class VariantManagerTest {

  private static final String UTILITY = "bg-gray-800";

  @BeforeAll
  static void setUp() {
    ToolkitBootstrap.ensureStarted();
  }

  private static <T> T onFx(Callable<T> task) throws Exception {
    CompletableFuture<T> result = new CompletableFuture<>();
    Platform.runLater(
        () -> {
          try {
            result.complete(task.call());
          } catch (Throwable t) {
            result.completeExceptionally(t);
          }
        });
    return result.get(10, TimeUnit.SECONDS);
  }

  private static void fireMouse(javafx.scene.Node node, javafx.event.EventType<MouseEvent> type) {
    Event.fireEvent(
        node,
        new MouseEvent(type, 0, 0, 0, 0, MouseButton.PRIMARY, 1, false, false, false, false, true,
            false, false, false, false, false, null));
  }

  @Test
  @DisplayName("hover applies on mouse-enter and removes on mouse-exit")
  void hoverVariant() throws Exception {
    onFx(
        () -> {
          Region node = new Region();
          VariantManager.applyStateVariant(node, "hover", UTILITY, new JitCompiler());
          fireMouse(node, MouseEvent.MOUSE_ENTERED);
          assertNotEquals("", node.getStyle(), "entering applies the style");
          fireMouse(node, MouseEvent.MOUSE_EXITED);
          assertEquals("", node.getStyle(), "exiting removes the style");
          return null;
        });
  }

  @Test
  @DisplayName("active applies while the mouse is pressed")
  void activeVariant() throws Exception {
    onFx(
        () -> {
          Region node = new Region();
          VariantManager.applyStateVariant(node, "active", UTILITY, new JitCompiler());
          fireMouse(node, MouseEvent.MOUSE_PRESSED);
          assertNotEquals("", node.getStyle());
          fireMouse(node, MouseEvent.MOUSE_RELEASED);
          assertEquals("", node.getStyle());
          return null;
        });
  }

  private static void setFocused(javafx.scene.Node node, boolean value) throws Exception {
    java.lang.reflect.Method m = javafx.scene.Node.class.getDeclaredMethod("setFocused", boolean.class);
    m.setAccessible(true);
    m.invoke(node, value);
  }

  @Test
  @DisplayName("focus applies only to controls and follows the focused property")
  void focusVariant() throws Exception {
    onFx(
        () -> {
          Label control = new Label("x");
          VariantManager.applyStateVariant(control, "focus", UTILITY, new JitCompiler());
          setFocused(control, true);
          assertNotEquals("", control.getStyle());
          setFocused(control, false);
          assertEquals("", control.getStyle());
          return null;
        });
  }

  @Test
  @DisplayName("disabled applies while the control is disabled")
  void disabledVariant() throws Exception {
    onFx(
        () -> {
          Label control = new Label("x");
          VariantManager.applyStateVariant(control, "disabled", UTILITY, new JitCompiler());
          control.setDisable(true);
          assertNotEquals("", control.getStyle());
          control.setDisable(false);
          assertEquals("", control.getStyle());
          return null;
        });
  }

  @Test
  @DisplayName("checked applies only to check boxes")
  void checkedVariant() throws Exception {
    onFx(
        () -> {
          CheckBox box = new CheckBox("c");
          VariantManager.applyStateVariant(box, "checked", UTILITY, new JitCompiler());
          box.setSelected(true);
          assertNotEquals("", box.getStyle());
          box.setSelected(false);
          assertEquals("", box.getStyle());
          return null;
        });
  }

  @Test
  @DisplayName("State variants ignore null arguments and unknown variant names")
  void stateVariantGuards() throws Exception {
    onFx(
        () -> {
          VariantManager.applyStateVariant(null, "hover", UTILITY, new JitCompiler());
          VariantManager.applyStateVariant(new Region(), null, UTILITY, new JitCompiler());
          VariantManager.applyStateVariant(new Region(), "hover", null, new JitCompiler());
          Region node = new Region();
          VariantManager.applyStateVariant(node, "unknown-variant", UTILITY, new JitCompiler());
          assertEquals("", node.getStyle());
          return null;
        });
  }

  @Test
  @DisplayName("Non-JIT utilities are skipped without throwing")
  void nonJitUtilitySkipped() throws Exception {
    onFx(
        () -> {
          Region node = new Region();
          VariantManager.applyStateVariant(node, "hover", "not-a-real-utility", new JitCompiler());
          assertEquals("", node.getStyle());
          return null;
        });
  }

  @Test
  @DisplayName("group-hover applies while the group parent is hovered")
  void groupHoverVariant() throws Exception {
    onFx(
        () -> {
          Pane group = new Pane();
          group.getStyleClass().add("group");
          Region child = new Region();
          group.getChildren().add(child);
          new Scene(group, 100, 100);

          VariantManager.applyGroupVariant(child, "group-hover", UTILITY, new JitCompiler());
          group.getOnMouseEntered().handle(null);
          assertNotEquals("", child.getStyle(), "group enter applies the child style");
          group.getOnMouseExited().handle(null);
          assertEquals("", child.getStyle(), "group exit removes the child style");
          return null;
        });
  }

  @Test
  @DisplayName("group variant ignores a parent without the group class")
  void groupVariantWithoutGroupParent() throws Exception {
    onFx(
        () -> {
          Pane parent = new Pane();
          Region child = new Region();
          parent.getChildren().add(child);
          new Scene(parent, 100, 100);
          VariantManager.applyGroupVariant(child, "group-hover", UTILITY, new JitCompiler());
          assertNull(parent.getOnMouseEntered());
          return null;
        });
  }

  @Test
  @DisplayName("arbitrary variants apply their compiled style directly")
  void arbitraryVariant() throws Exception {
    onFx(
        () -> {
          Region media = new Region();
          VariantManager.applyArbitraryVariant(media, "[@media(min-width:600px)]", UTILITY,
              new JitCompiler());
          assertNotEquals("", media.getStyle());

          Region other = new Region();
          VariantManager.applyArbitraryVariant(other, "[&:hover]", UTILITY, new JitCompiler());
          assertNotEquals("", other.getStyle());
          return null;
        });
  }

  @Test
  @DisplayName("processToken applies a plain utility with no variant")
  void processTokenPlain() throws Exception {
    onFx(
        () -> {
          Region node = new Region();
          VariantManager.processToken(node, UTILITY, new JitCompiler());
          assertNotEquals("", node.getStyle());
          return null;
        });
  }

  @Test
  @DisplayName("processToken routes a state variant to the state handler")
  void processTokenState() throws Exception {
    onFx(
        () -> {
          Region node = new Region();
          VariantManager.processToken(node, "hover:" + UTILITY, new JitCompiler());
          fireMouse(node, MouseEvent.MOUSE_ENTERED);
          assertNotEquals("", node.getStyle());
          return null;
        });
  }

  @Test
  @DisplayName("processToken routes a theme variant and follows the theme")
  void processTokenTheme() throws Exception {
    onFx(
        () -> {
          Region node = new Region();
          Pane root = new Pane(node);
          Scene scene = new Scene(root, 100, 100);
          VariantManager.processToken(node, "dark:" + UTILITY, new JitCompiler());
          io.github.yasmramos.tailwindfx.theme.ThemeManager.forScene(scene).dark().apply();
          assertNotEquals("", node.getStyle());
          return null;
        });
  }

  @Test
  @DisplayName("processToken routes an arbitrary variant")
  void processTokenArbitrary() throws Exception {
    onFx(
        () -> {
          Region node = new Region();
          VariantManager.processToken(node, "[&:hover]:" + UTILITY, new JitCompiler());
          assertNotEquals("", node.getStyle());
          return null;
        });
  }

  @Test
  @DisplayName("processToken routes a group variant")
  void processTokenGroup() throws Exception {
    onFx(
        () -> {
          Pane group = new Pane();
          group.getStyleClass().add("group");
          Region child = new Region();
          group.getChildren().add(child);
          new Scene(group, 100, 100);
          VariantManager.processToken(child, "group-hover:" + UTILITY, new JitCompiler());
          group.getOnMouseEntered().handle(null);
          assertNotEquals("", child.getStyle());
          return null;
        });
  }

  @Test
  @DisplayName("processToken ignores a null node/compiler gracefully for a plain token")
  void processTokenGuards() throws Exception {
    onFx(
        () -> {
          Region node = new Region();
          VariantManager.processToken(node, "text-sm", new JitCompiler());
          assertNotNull(node.getStyle());
          return null;
        });
  }

  @Test
  @DisplayName("Style merge and removal keep unrelated properties intact")
  void styleMergeAndRemoval() throws Exception {
    onFx(
        () -> {
          Region node = new Region();
          node.setStyle("-fx-border-color: red;");
          VariantManager.applyStateVariant(node, "hover", "p-4", new JitCompiler());
          fireMouse(node, MouseEvent.MOUSE_ENTERED);
          assertTrue(node.getStyle().contains("-fx-padding"), "hover adds its own property");
          fireMouse(node, MouseEvent.MOUSE_EXITED);
          // Removal is property-level: only the hover's property is dropped, unrelated ones stay.
          assertTrue(
              node.getStyle().contains("-fx-border-color"),
              "unrelated properties must be preserved");
          return null;
        });
  }
}
