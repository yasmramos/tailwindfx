package io.github.yasmramos.tailwindfx.responsive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.yasmramos.tailwindfx.testing.JavaFxToolkitExtension;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Unit tests for {@link ResponsiveNode}. */
@ExtendWith(JavaFxToolkitExtension.class)
@DisplayName("ResponsiveNode")
class ResponsiveNodeTest {

  @Test
  @DisplayName("applies the breakpoint matching the current scene width")
  void appliesActiveBreakpointOnInstall() {
    StackPane root = new StackPane();
    Scene scene = new Scene(root, 800, 600);
    StackPane node = new StackPane();
    root.getChildren().add(node);

    ResponsiveNode rn =
        ResponsiveNode.on(node).base("p-4").md("text-center").lg("font-bold").install(scene);

    assertEquals(768, rn.activeBreakpoint(), "800px falls in the md range");
    assertTrue(node.getStyleClass().contains("p-4"), "base classes are always present");
    assertTrue(node.getStyleClass().contains("text-center"), "md classes are applied at 800px");
    assertFalse(
        node.getStyleClass().contains("font-bold"), "lg classes must not be applied at 800px");
  }

  @Test
  @DisplayName("refresh() re-applies the active classes after an external mutation")
  void refreshReappliesAfterExternalRemoval() {
    // Regression: refresh() used to delegate to the width-driven path, whose "breakpoint unchanged"
    // guard made it a silent no-op — exactly the case it exists to fix.
    StackPane root = new StackPane();
    Scene scene = new Scene(root, 800, 600);
    StackPane node = new StackPane();
    root.getChildren().add(node);

    ResponsiveNode rn = ResponsiveNode.on(node).base("p-4").md("text-center").install(scene);
    assertTrue(node.getStyleClass().contains("text-center"), "precondition: md class applied");

    node.getStyleClass().remove("text-center"); // external mutation
    rn.refresh();

    assertTrue(
        node.getStyleClass().contains("text-center"), "refresh() must re-apply active classes");
    assertEquals(768, rn.activeBreakpoint());
  }

  @Test
  @DisplayName("refresh() fires the breakpoint callback again")
  void refreshFiresCallback() {
    StackPane root = new StackPane();
    Scene scene = new Scene(root, 800, 600);
    StackPane node = new StackPane();
    root.getChildren().add(node);

    AtomicInteger calls = new AtomicInteger();
    ResponsiveNode rn =
        ResponsiveNode.on(node)
            .md("w-1-2")
            .onBreakpoint(bp -> calls.incrementAndGet())
            .install(scene);

    assertEquals(1, calls.get(), "install fires the callback once");
    rn.refresh();
    assertEquals(2, calls.get(), "refresh() re-runs the callback");
  }
}
