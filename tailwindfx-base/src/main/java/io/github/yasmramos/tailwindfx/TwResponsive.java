/*
 * Copyright 2026 Yasmany Ramos García (yasmramos).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * TwResponsive.on(region);
 * TwResponsive.forStage(stage);
 * </pre>
 */
public final class TwResponsive {

  private TwResponsive() {}

  /**
   * Installs responsive support on the scene root of a stage.
   *
   * @param stage the stage whose scene root should become responsive; the stage must have a scene
   *     attached
   * @return the {@link ResponsiveNode} bound to the scene root
   * @throws IllegalArgumentException if the stage has no scene, or its root is not a {@link
   *     javafx.scene.Node}
   */
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

  /**
   * Makes a region responsive to breakpoint changes.
   *
   * @param region the region to bind to the current scene
   * @return the {@link ResponsiveNode} bound to the region
   */
  public static ResponsiveNode on(Region region) {
    return ResponsiveNode.on(region).install(region.getScene());
  }

  /**
   * Returns the breakpoint manager associated with a stage.
   *
   * @param stage the stage whose breakpoints should be managed
   * @return the {@link BreakpointManager} for the stage
   */
  public static BreakpointManager forStage(Stage stage) {
    return BreakpointManager.from(stage);
  }
}
