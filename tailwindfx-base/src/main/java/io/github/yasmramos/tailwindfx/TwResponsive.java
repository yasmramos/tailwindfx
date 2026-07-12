package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.breakpoint.BreakpointManager;
import io.github.yasmramos.tailwindfx.responsive.ResponsiveNode;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * TwResponsive — Responsive facade for breakpoint management.
 *
 * <pre>
 * TwResponsive.on(stage);
 * TwResponsive.forRegion(region);
 * </pre>
 */
public final class TwResponsive {

  private static final TwResponsive INSTANCE = new TwResponsive();

  private TwResponsive() {}

  /** Installs responsive support on a Stage. */
  public static ResponsiveNode on(Stage stage) {
    Scene scene = stage.getScene();
    if (scene == null) {
      throw new IllegalArgumentException("Stage must have a scene attached");
    }
    // Install on the root node of the scene
    javafx.scene.Parent root = scene.getRoot();
    if (root instanceof Region) {
      return ResponsiveNode.on((Region) root).install(scene);
    } else if (root instanceof javafx.scene.Node) {
      return ResponsiveNode.on(root).install(scene);
    }
    throw new IllegalArgumentException("Scene root must be a Node");
  }

  /** Makes a Region responsive to breakpoint changes. */
  public static ResponsiveNode on(Region region) {
    return ResponsiveNode.on(region).install(region.getScene());
  }

  /** Gets the BreakpointManager for a Stage. */
  public static BreakpointManager forStage(Stage stage) {
    return BreakpointManager.from(stage);
  }
}
