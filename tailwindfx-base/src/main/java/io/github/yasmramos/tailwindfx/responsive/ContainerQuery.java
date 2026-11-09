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
package io.github.yasmramos.tailwindfx.responsive;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;

public final class ContainerQuery {

  private final Node node;
  private final List<String> baseClasses = new ArrayList<>();
  private final List<String> smClasses = new ArrayList<>();
  private final List<String> mdClasses = new ArrayList<>();
  private final List<String> lgClasses = new ArrayList<>();
  private final List<String> xlClasses = new ArrayList<>();
  private final List<String> xxlClasses = new ArrayList<>();
  private final List<CustomBreakpoint> customBreakpoints = new ArrayList<>();
  private Consumer<String> breakpointCallback;

  private ChangeListener<Number> listener;
  private ChangeListener<Scene> nodeSceneListener;
  private ChangeListener<Scene> containerSceneListener;
  private Region currentContainer;

  public static final class CustomBreakpoint {
    public final double minWidth;
    public final List<String> classes = new ArrayList<>();

    public CustomBreakpoint(double minWidth) {
      this.minWidth = minWidth;
    }
  }

  private ContainerQuery(Node node) {
    this.node = Objects.requireNonNull(node, "node");
  }

  public static ContainerQuery on(Node node) {
    return new ContainerQuery(node);
  }

  public ContainerQuery base(String... classes) {
    for (String c : classes) {
      if (c != null && !c.trim().isEmpty()) {
        baseClasses.add(c.trim());
      }
    }
    return this;
  }

  public ContainerQuery sm(String... classes) {
    for (String c : classes) {
      if (c != null && !c.trim().isEmpty()) {
        smClasses.add(c.trim());
      }
    }
    return this;
  }

  public ContainerQuery md(String... classes) {
    for (String c : classes) {
      if (c != null && !c.trim().isEmpty()) {
        mdClasses.add(c.trim());
      }
    }
    return this;
  }

  public ContainerQuery lg(String... classes) {
    for (String c : classes) {
      if (c != null && !c.trim().isEmpty()) {
        lgClasses.add(c.trim());
      }
    }
    return this;
  }

  public ContainerQuery xl(String... classes) {
    for (String c : classes) {
      if (c != null && !c.trim().isEmpty()) {
        xlClasses.add(c.trim());
      }
    }
    return this;
  }

  public ContainerQuery xxl(String... classes) {
    for (String c : classes) {
      if (c != null && !c.trim().isEmpty()) {
        xxlClasses.add(c.trim());
      }
    }
    return this;
  }

  public ContainerQuery at(double minWidth, String... classes) {
    CustomBreakpoint bp = new CustomBreakpoint(minWidth);
    for (String c : classes) {
      if (c != null && !c.trim().isEmpty()) {
        bp.classes.add(c.trim());
      }
    }
    customBreakpoints.add(bp);
    return this;
  }

  public ContainerQuery onBreakpoint(Consumer<String> callback) {
    this.breakpointCallback = callback;
    return this;
  }

  public void install(Region container) {
    Objects.requireNonNull(container, "container");
    // Re-installing on a different container must not leave the previous listeners attached.
    detach();
    this.currentContainer = container;

    applyClasses(baseClasses);

    listener =
        (obs, oldVal, newVal) -> {
          double width = newVal.doubleValue();
          updateClasses(width);
        };

    container.widthProperty().addListener(listener);
    updateClasses(container.getWidth());

    nodeSceneListener =
        (obs, oldScene, newScene) -> {
          if (newScene == null) {
            detach();
          }
        };
    node.sceneProperty().addListener(nodeSceneListener);

    containerSceneListener =
        (obs, oldScene, newScene) -> {
          if (newScene == null) {
            detach();
          }
        };
    container.sceneProperty().addListener(containerSceneListener);
  }

  private void updateClasses(double width) {
    removeClasses(smClasses);
    removeClasses(mdClasses);
    removeClasses(lgClasses);
    removeClasses(xlClasses);
    removeClasses(xxlClasses);
    for (CustomBreakpoint bp : customBreakpoints) {
      removeClasses(bp.classes);
    }

    String currentBp = "base";

    if (width >= 1536) {
      applyClasses(xxlClasses);
      currentBp = "xxl";
    } else if (width >= 1280) {
      applyClasses(xlClasses);
      currentBp = "xl";
    } else if (width >= 1024) {
      applyClasses(lgClasses);
      currentBp = "lg";
    } else if (width >= 768) {
      applyClasses(mdClasses);
      currentBp = "md";
    } else if (width >= 640) {
      applyClasses(smClasses);
      currentBp = "sm";
    }

    for (CustomBreakpoint bp : customBreakpoints) {
      if (width >= bp.minWidth) {
        applyClasses(bp.classes);
        currentBp = "custom-" + bp.minWidth;
      }
    }

    if (breakpointCallback != null) {
      breakpointCallback.accept(currentBp);
    }
  }

  private void applyClasses(List<String> classes) {
    if (classes.isEmpty()) return;
    for (String c : classes) {
      if (!node.getStyleClass().contains(c)) {
        node.getStyleClass().add(c);
      }
    }
  }

  private void removeClasses(List<String> classes) {
    for (String c : classes) {
      node.getStyleClass().remove(c);
    }
  }

  public void detach() {
    if (listener != null && currentContainer != null) {
      currentContainer.widthProperty().removeListener(listener);
      listener = null;
    }
    if (nodeSceneListener != null) {
      node.sceneProperty().removeListener(nodeSceneListener);
      nodeSceneListener = null;
    }
    if (containerSceneListener != null && currentContainer != null) {
      currentContainer.sceneProperty().removeListener(containerSceneListener);
      containerSceneListener = null;
    }
  }

  public void refresh() {
    if (currentContainer != null) {
      updateClasses(currentContainer.getWidth());
    }
  }
}
