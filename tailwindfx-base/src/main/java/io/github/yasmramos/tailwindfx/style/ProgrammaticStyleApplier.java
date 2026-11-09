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
package io.github.yasmramos.tailwindfx.style;

import io.github.yasmramos.tailwindfx.core.Preconditions;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

/**
 * ProgrammaticStyleApplier - Applies utility classes whose effect has no JavaFX CSS equivalent.
 *
 * <p>A number of Tailwind utilities describe behaviour that JavaFX cannot express with an inline
 * {@code -fx-*} declaration or a stylesheet rule: visibility, overflow clipping, z-order and
 * "fill the parent" sizing. Historically these classes were added to the node's style class list
 * (and advertised by {@link io.github.yasmramos.tailwindfx.TwCatalog}) but nothing applied them,
 * so they were silent no-ops.
 *
 * <p>This class maps those tokens to the corresponding JavaFX API calls, delegating to the
 * existing {@link Styles} helpers where one already exists:
 *
 * <ul>
 *   <li>{@code visible} / {@code invisible} / {@code hidden} → {@link Styles#show},
 *       {@link Styles#invisible}, {@link Styles#hiddenNode}
 *   <li>{@code overflow-hidden} (and axis/clip variants) → {@code setClip};
 *       {@code overflow-*-visible} → {@code setClip(null)}
 *   <li>{@code z-{n}} / {@code z-auto} → {@link Styles#z}
 *   <li>{@code aspect-square}/{@code aspect-video}/... → {@link Styles#aspectRatio}
 *   <li>{@code w-full}/{@code w-screen}/{@code h-full}/{@code h-screen} and the {@code max-*}
 *       equivalents → grow to fill the available space
 * </ul>
 *
 * <p>Tokens outside this set are ignored, so the applier can be fed the whole token list without
 * interfering with margin/gap/flex handling performed by {@link LayoutApplier} or with plain CSS
 * classes resolved by the generated stylesheet.
 */
public final class ProgrammaticStyleApplier {

  /** Node property key holding the bounds listener installed by {@code overflow-hidden}. */
  private static final String OVERFLOW_CLIP_LISTENER_KEY = "tw-overflow-clip-listener";

  /** Width utilities that make a node fill the available horizontal space. */
  private static final Set<String> FILL_WIDTH =
      Set.of("w-full", "w-screen", "max-w-full", "max-w-screen");

  /** Height utilities that make a node fill the available vertical space. */
  private static final Set<String> FILL_HEIGHT =
      Set.of("h-full", "h-screen", "max-h-full", "max-h-screen");

  private ProgrammaticStyleApplier() {}

  /**
   * Applies all supported programmatic utilities found in {@code tokens} to {@code node}.
   *
   * @param node the target node
   * @param tokens the token list (may contain unrelated tokens, which are ignored)
   */
  public static void apply(Node node, Iterable<String> tokens) {
    Preconditions.requireNode(node, "ProgrammaticStyleApplier.apply");
    if (tokens == null) {
      return;
    }
    for (String token : tokens) {
      if (token == null || token.isBlank()) {
        continue;
      }
      applyToken(node, token.trim());
    }
  }

  /** Returns true when this applier knows how to handle the given token. */
  public static boolean handles(String token) {
    if (token == null || token.isBlank()) {
      return false;
    }
    String t = token.trim();
    return t.equals("visible")
        || t.equals("invisible")
        || t.equals("hidden")
        || FILL_WIDTH.contains(t)
        || FILL_HEIGHT.contains(t)
        || isOverflowToken(t)
        || isZToken(t)
        || isAspectToken(t);
  }

  private static void applyToken(Node node, String token) {
    switch (token) {
      case "visible" -> Styles.show(node);
      case "invisible" -> Styles.invisible(node);
      case "hidden" -> Styles.hiddenNode(node);
      case "z-auto" -> node.setViewOrder(0);
      default -> {
        if (isZToken(token)) {
          Styles.z(node, Integer.parseInt(token.substring(2)));
        } else if (isAspectToken(token)) {
          applyAspect(node, token);
        } else if (isOverflowToken(token)) {
          applyOverflow(node, token);
        } else if (node instanceof Region region) {
          if (FILL_WIDTH.contains(token)) {
            region.setMaxWidth(Double.MAX_VALUE);
          } else if (FILL_HEIGHT.contains(token)) {
            region.setMaxHeight(Double.MAX_VALUE);
          }
        }
      }
    }
  }

  private static boolean isAspectToken(String token) {
    return token.startsWith("aspect-") && !token.equals("aspect-auto");
  }

  private static void applyAspect(Node node, String token) {
    if (!(node instanceof Region region) || region.prefHeightProperty().isBound()) {
      return;
    }
    double ratio = aspectRatioOf(token.substring("aspect-".length()));
    if (ratio > 0) {
      Styles.aspectRatio(region, ratio);
    }
  }

  private static double aspectRatioOf(String value) {
    switch (value) {
      case "square":
        return 1.0;
      case "video":
        return 16.0 / 9.0;
      case "portrait":
        return 3.0 / 4.0;
      case "landscape":
        return 4.0 / 3.0;
      default:
        break;
    }
    if (value.startsWith("[") && value.endsWith("]")) {
      value = value.substring(1, value.length() - 1).trim();
    }
    int slash = value.indexOf('/');
    try {
      if (slash > 0) {
        double width = Double.parseDouble(value.substring(0, slash).trim());
        double height = Double.parseDouble(value.substring(slash + 1).trim());
        return height == 0 ? -1 : width / height;
      }
      return Double.parseDouble(value.trim());
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private static boolean isZToken(String token) {
    return token.equals("z-auto") || token.matches("z-\\d+");
  }

  private static boolean isOverflowToken(String token) {
    return token.matches("overflow(-[xy])?-(hidden|visible|clip)");
  }

  private static void applyOverflow(Node node, String token) {
    boolean clips = token.endsWith("-hidden") || token.endsWith("-clip");
    if (!clips) {
      // overflow-*-visible restores the default (no clipping).
      removeOverflowClip(node);
      return;
    }
    if (node.getClip() != null) {
      // A clip is already installed; do not stack a second one.
      return;
    }
    Rectangle clip = new Rectangle();
    Bounds bounds = node.getLayoutBounds();
    clip.setWidth(bounds.getWidth());
    clip.setHeight(bounds.getHeight());
    // Keep a reference to the bounds listener so removing the clip can also detach it; otherwise
    // each overflow-hidden/overflow-visible cycle would leave a listener bound to the node.
    ChangeListener<Bounds> boundsListener =
        (obs, oldBounds, newBounds) -> {
          clip.setWidth(newBounds.getWidth());
          clip.setHeight(newBounds.getHeight());
        };
    node.layoutBoundsProperty().addListener(boundsListener);
    node.getProperties().put(OVERFLOW_CLIP_LISTENER_KEY, boundsListener);
    node.setClip(clip);
  }

  /** Removes a clip previously installed by {@code overflow-hidden} and detaches its listener. */
  private static void removeOverflowClip(Node node) {
    Object recorded = node.getProperties().remove(OVERFLOW_CLIP_LISTENER_KEY);
    if (recorded == null) {
      return;
    }
    @SuppressWarnings("unchecked")
    ChangeListener<Bounds> boundsListener = (ChangeListener<Bounds>) recorded;
    node.layoutBoundsProperty().removeListener(boundsListener);
    node.setClip(null);
  }
}
