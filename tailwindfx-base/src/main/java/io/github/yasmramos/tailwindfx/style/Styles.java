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

import io.github.yasmramos.tailwindfx.animation.TwAnimation;
import io.github.yasmramos.tailwindfx.core.Preconditions;
import io.github.yasmramos.tailwindfx.core.TokenRegistry;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Shear;

/**
 * Styles — Java API for everything CSS cannot do in JavaFX.
 *
 * <p>Each method maps to a Tailwind class documented in tailwindfx.css with "use X in Java".
 * The naming is identical to Tailwind so the mapping is immediate:
 *
 * <p>Commented CSS class → Java method .col-span-3 → Styles.colSpan(node, 3) .row-span-full →
 * Styles.rowSpanFull(node) .mx-4 → Styles.mx(node, 4) .z-50 → Styles.z(node, 50) .self-center →
 * Styles.selfCenter(node) .justify-self-end → Styles.justifySelfEnd(node) .flex-1 →
 * Styles.flex1(node) .grayscale → Styles.grayscale(node) .brightness-75 → Styles.brightness(node,
 * 0.75) .skew-x-6 → Styles.skewX(node, 6) .object-cover → Styles.objectCover(imageView)
 * .order-first → Styles.orderFirst(node)
 *
 * <p>All methods return the node they receive to allow chaining:
 * Styles.colSpan(Styles.mx(card, 4), 3);
 */
public final class Styles {

  // Tailwind scale: 1 unit = 4px
  private static final double UNIT = 4.0;

  private Styles() {}

  // GRIDPANE — col-span-*, row-span-*
  // Maps to: grid-column: span N / grid-row: span N in web CSS

  /**
   * Sets the column span of a node in a {@link GridPane} (equivalent to {@code .col-span-N}).
   *
   * @param node the node to span
   * @param span number of columns to span (must be >= 1)
   * @param <T> node type, returned unchanged for chaining
   * @return the same node
   * @throws IllegalArgumentException if node is null or span < 1
   */
  public static <T extends Node> T colSpan(T node, int span) {
    Preconditions.requireNode(node, "Styles.colSpan");
    Preconditions.requireSpan(span, "Styles.colSpan");
    GridPane.setColumnSpan(node, span);
    return node;
  }

  /** .col-span-full — spans all columns of the GridPane */
  public static <T extends Node> T colSpanFull(T node) {
    Preconditions.requireNode(node, "Styles.colSpanFull");
    GridPane.setColumnSpan(node, GridPane.REMAINING);
    return node;
  }

  /**
   * Sets the row span of a node in a {@link GridPane} (equivalent to {@code .row-span-N}).
   *
   * @param node the node to span
   * @param span number of rows to span (must be >= 1)
   * @param <T> node type, returned unchanged for chaining
   * @return the same node
   * @throws IllegalArgumentException if node is null or span < 1
   */
  public static <T extends Node> T rowSpan(T node, int span) {
    Preconditions.requireNode(node, "Styles.rowSpan");
    Preconditions.requireSpan(span, "Styles.rowSpan");
    GridPane.setRowSpan(node, span);
    return node;
  }

  /** .row-span-full — spans all rows of the GridPane */
  public static <T extends Node> T rowSpanFull(T node) {
    Preconditions.requireNode(node, "Styles.rowSpanFull");
    GridPane.setRowSpan(node, GridPane.REMAINING);
    return node;
  }

  /** Shorthand for {@code colSpan(node, 2)}. */
  public static <T extends Node> T colSpan2(T node) {
    return colSpan(node, 2);
  }

  public static <T extends Node> T colSpan3(T node) {
    return colSpan(node, 3);
  }

  public static <T extends Node> T colSpan4(T node) {
    return colSpan(node, 4);
  }

  public static <T extends Node> T colSpan6(T node) {
    return colSpan(node, 6);
  }

  public static <T extends Node> T colSpan8(T node) {
    return colSpan(node, 8);
  }

  public static <T extends Node> T colSpan12(T node) {
    return colSpan(node, 12);
  }

  public static <T extends Node> T rowSpan2(T node) {
    return rowSpan(node, 2);
  }

  public static <T extends Node> T rowSpan3(T node) {
    return rowSpan(node, 3);
  }

  public static <T extends Node> T rowSpan4(T node) {
    return rowSpan(node, 4);
  }

  public static <T extends Node> T rowSpan6(T node) {
    return rowSpan(node, 6);
  }

  // GRIDPANE - explicit position col-start-*, row-start-*

  /**
   * Positions a node at a specific cell in a {@link GridPane}.
   *
   * @param node the node to position
   * @param col zero-based column index
   * @param row zero-based row index
   * @param <T> node type, returned for chaining
   * @return the same node
   * @throws IllegalArgumentException if node is null or col/row are negative
   */
  public static <T extends Node> T gridCell(T node, int col, int row) {
    Preconditions.requireNode(node, "Styles.gridCell");
    if (col < 0)
      throw new IllegalArgumentException("Styles.gridCell: col must be >= 0, got: " + col);
    if (row < 0)
      throw new IllegalArgumentException("Styles.gridCell: row must be >= 0, got: " + row);
    GridPane.setColumnIndex(node, col);
    GridPane.setRowIndex(node, row);
    return node;
  }

  public static <T extends Node> T colStart(T node, int col) {
    GridPane.setColumnIndex(node, col);
    return node;
  }

  public static <T extends Node> T rowStart(T node, int row) {
    GridPane.setRowIndex(node, row);
    return node;
  }

  // GROW/SHRINK — flex-1, flex-grow, flex-shrink, flex-none
  // Maps to: flex-grow, flex-shrink in web CSS

  /** .flex-1 / .grow — grows to fill the horizontal space (HBox) */
  public static <T extends Node> T flex1(T node) {
    Preconditions.requireNode(node, "Styles.flex1");
    Preconditions.warnNoParent(node, "Styles.flex1");
    HBox.setHgrow(node, Priority.ALWAYS);
    return node;
  }

  /** .grow — grows to fill the horizontal space */
  public static <T extends Node> T grow(T node) {
    Preconditions.requireNode(node, "Styles.grow");
    Preconditions.warnNoParent(node, "Styles.grow");
    HBox.setHgrow(node, Priority.ALWAYS);
    return node;
  }

  /** .grow-0 / .flex-none — does not grow */
  public static <T extends Node> T growNone(T node) {
    HBox.setHgrow(node, Priority.NEVER);
    VBox.setVgrow(node, Priority.NEVER);
    return node;
  }

  /** .flex-auto — grows and shrinks as needed */
  public static <T extends Node> T flexAuto(T node) {
    HBox.setHgrow(node, Priority.SOMETIMES);
    return node;
  }

  /** Grows vertically inside a VBox */
  public static <T extends Node> T vgrow(T node) {
    VBox.setVgrow(node, Priority.ALWAYS);
    return node;
  }

  /** .hbox-fill-width — the node fills the available width in an HBox */
  public static <T extends Node> T hboxFillWidth(T node) {
    HBox.setHgrow(node, Priority.ALWAYS);
    if (node instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
    return node;
  }

  /** .vbox-fill-height — the node fills the available height in a VBox */
  public static <T extends Node> T vboxFillHeight(T node) {
    VBox.setVgrow(node, Priority.ALWAYS);
    if (node instanceof Region r) r.setMaxHeight(Double.MAX_VALUE);
    return node;
  }

  // MARGIN — mx-*, my-*, mt-*, mr-*, mb-*, ml-*
  // Maps to: margin in web CSS
  // NOTE: JavaFX has no CSS margin. setMargin() on the container is used instead.

  /**
   * IMPORTANT — call order with the margin methods.
   *
   * <p>JavaFX applies the constraints ({@code HBox.setMargin}, etc.) at call time. The node must
   * already be added to the parent for the margin to take effect immediately.
   *
   * <pre>
   * // Correct - add first, margin after
   * parent.getChildren().add(node);
   * Styles.mx(node, 4);
   *
   * // Wrong — the constraint is applied without a parent, with no visible effect
   * Styles.mx(node, 4);
   * parent.getChildren().add(node);
   * </pre>
   *
   * <p>To avoid the problem, use the {@code WithParent} methods that add the node and apply the
   * margin in a single step:
   *
   * <pre>
   * Styles.mxWithParent(node, hbox, 4);
   * Styles.addWithMargin(hbox, 2, nodeA, nodeB, nodeC);
   * </pre>
   */
  /**
   * Applies uniform margin on all sides (equivalent to {@code .m-N}).
   *
   * <p>Sets {@code HBox.setMargin}, {@code VBox.setMargin} and {@code GridPane.setMargin}. The node
   * must already be added to its parent for the margin to take effect immediately.
   *
   * @param node the node to margin (must be in a parent container)
   * @param n margin in Tailwind units (n × 4px)
   * @param <T> node type, returned for chaining
   * @return the same node
   */
  public static <T extends Node> T m(T node, int n) {
    Preconditions.requireNode(node, "Styles.m");
    Preconditions.warnNoParent(node, "Styles.m");
    Insets insets = new Insets(n * UNIT);
    HBox.setMargin(node, insets);
    VBox.setMargin(node, insets);
    GridPane.setMargin(node, insets);
    return node;
  }

  /**
   * Applies horizontal margin (left + right) equivalent to {@code .mx-N}.
   *
   * @param node the node to margin
   * @param n margin in Tailwind units (n × 4px per side)
   * @param <T> node type, returned for chaining
   * @return the same node
   */
  public static <T extends Node> T mx(T node, int n) {
    Preconditions.requireNode(node, "Styles.mx");
    Preconditions.warnNoParent(node, "Styles.mx");
    double px = n * UNIT;
    Insets insets = new Insets(0, px, 0, px);
    HBox.setMargin(node, insets);
    VBox.setMargin(node, insets);
    GridPane.setMargin(node, insets);
    return node;
  }

  /**
   * Applies vertical margin (top + bottom) equivalent to {@code .my-N}.
   *
   * @param node the node to margin
   * @param n margin in Tailwind units (n × 4px per side)
   * @param <T> node type, returned for chaining
   * @return the same node
   */
  public static <T extends Node> T my(T node, int n) {
    Preconditions.requireNode(node, "Styles.my");
    Preconditions.warnNoParent(node, "Styles.my");
    double px = n * UNIT;
    Insets insets = new Insets(px, 0, px, 0);
    HBox.setMargin(node, insets);
    VBox.setMargin(node, insets);
    GridPane.setMargin(node, insets);
    return node;
  }

  /** .mt-{n} — top margin */
  public static <T extends Node> T mt(T node, int n) {
    return margin(node, n * UNIT, 0, 0, 0);
  }

  /** .mr-{n} — right margin */
  public static <T extends Node> T mr(T node, int n) {
    return margin(node, 0, n * UNIT, 0, 0);
  }

  /** .mb-{n} — bottom margin */
  public static <T extends Node> T mb(T node, int n) {
    return margin(node, 0, 0, n * UNIT, 0);
  }

  /** .ml-{n} — left margin */
  public static <T extends Node> T ml(T node, int n) {
    return margin(node, 0, 0, 0, n * UNIT);
  }

  /** .mx-auto — centers horizontally in an HBox using spacers */
  public static <T extends Node> T mxAuto(T node) {
    HBox.setHgrow(node, Priority.ALWAYS);
    return node;
  }

  /** Margin with exact pixel values */
  public static <T extends Node> T margin(
      T node, double top, double right, double bottom, double left) {
    Preconditions.requireNode(node, "Styles.margin");
    Preconditions.warnNoParent(node, "Styles.margin");

    // Get existing margins to preserve values not being explicitly set
    Insets existingHBox = HBox.getMargin(node);
    Insets existingVBox = VBox.getMargin(node);
    Insets existingGridPane = GridPane.getMargin(node);

    // Use existing values as fallback for zero values (preserves previously set margins)
    double finalTop = (top == 0 && existingHBox != null) ? existingHBox.getTop() : top;
    double finalRight = (right == 0 && existingHBox != null) ? existingHBox.getRight() : right;
    double finalBottom = (bottom == 0 && existingHBox != null) ? existingHBox.getBottom() : bottom;
    double finalLeft = (left == 0 && existingHBox != null) ? existingHBox.getLeft() : left;

    // If all values are zero and we have existing margins, preserve them
    if (top == 0 && right == 0 && bottom == 0 && left == 0 && existingHBox != null) {
      finalTop = existingHBox.getTop();
      finalRight = existingHBox.getRight();
      finalBottom = existingHBox.getBottom();
      finalLeft = existingHBox.getLeft();
    }

    Insets insets = new Insets(finalTop, finalRight, finalBottom, finalLeft);
    HBox.setMargin(node, insets);
    VBox.setMargin(node, insets);
    GridPane.setMargin(node, insets);
    return node;
  }

  // MARGIN WithParent - add node AND apply margin in a single step
  // They remove the call-order problem described in the margin Javadoc.

  /**
   * Adds {@code node} to {@code parent} and applies a margin on all sides.
   *
   * <p>Equivalent to:
   *
   * <pre>
   * parent.getChildren().add(node);
   * Styles.m(node, n);
   * </pre>
   */
  public static <T extends Node> T mWithParent(T node, Pane parent, int n) {
    Preconditions.requireNode(node, "Styles.mWithParent");
    Preconditions.requireNonNull(parent, "Styles.mWithParent", "parent");
    if (!parent.getChildren().contains(node)) parent.getChildren().add(node);
    return m(node, n);
  }

  /**
   * Adds {@code node} to {@code parent} and applies a horizontal margin (n × 4px).
   *
   * <p>Equivalent to:
   *
   * <pre>
   * parent.getChildren().add(node);
   * Styles.mx(node, n);
   * </pre>
   */
  public static <T extends Node> T mxWithParent(T node, Pane parent, int n) {
    Preconditions.requireNode(node, "Styles.mxWithParent");
    Preconditions.requireNonNull(parent, "Styles.mxWithParent", "parent");
    if (!parent.getChildren().contains(node)) parent.getChildren().add(node);
    return mx(node, n);
  }

  /** Adds {@code node} to {@code parent} and applies a vertical margin (n × 4px). */
  public static <T extends Node> T myWithParent(T node, Pane parent, int n) {
    Preconditions.requireNode(node, "Styles.myWithParent");
    Preconditions.requireNonNull(parent, "Styles.myWithParent", "parent");
    if (!parent.getChildren().contains(node)) parent.getChildren().add(node);
    return my(node, n);
  }

  /**
   * Adds {@code node} to {@code parent} and applies individual margins in px.
   *
   * @param top top margin in px
   * @param right right margin in px
   * @param bottom bottom margin in px
   * @param left left margin in px
   */
  public static <T extends Node> T marginWithParent(
      T node, Pane parent, double top, double right, double bottom, double left) {
    Preconditions.requireNode(node, "Styles.marginWithParent");
    Preconditions.requireNonNull(parent, "Styles.marginWithParent", "parent");
    if (!parent.getChildren().contains(node)) parent.getChildren().add(node);
    return margin(node, top, right, bottom, left);
  }

  /**
   * Adds several nodes to a parent with the same margin (n × 4px) applied to each one.
   *
   * <pre>
   * // Adds three cards to an HBox with 8px margin each
   * Styles.addWithMargin(hbox, 2, card1, card2, card3);
   * </pre>
   *
   * @param parent destination container
   * @param n Tailwind units (n × 4px)
   * @param nodes nodes to add
   */
  @SafeVarargs
  public static <T extends Node> void addWithMargin(Pane parent, int n, T... nodes) {
    Preconditions.requireNonNull(parent, "Styles.addWithMargin", "parent");
    for (T node : nodes) {
      if (node == null) continue;
      if (!parent.getChildren().contains(node)) parent.getChildren().add(node);
      m(node, n);
    }
  }

  // Z-ORDER — z-0, z-10, z-20, z-30, z-40, z-50, z-auto
  // Maps to: z-index in web CSS
  // NOTE: In JavaFX, lower viewOrder = more forward (opposite of z-index)

  /**
   * .z-{value} — controls the node's paint order. JavaFX viewOrder: 0 = front, positive values
   * = further back. It is inverted with respect to web z-index (where higher = more forward).
   */
  public static <T extends Node> T z(T node, int zIndex) {
    Preconditions.requireNode(node, "Styles.z");
    // Invert: z-50 in Tailwind = more forward = negative viewOrder
    node.setViewOrder(-zIndex);
    return node;
  }

  public static <T extends Node> T z0(T node) {
    return z(node, 0);
  }

  public static <T extends Node> T z10(T node) {
    return z(node, 10);
  }

  public static <T extends Node> T z20(T node) {
    return z(node, 20);
  }

  public static <T extends Node> T z30(T node) {
    return z(node, 30);
  }

  public static <T extends Node> T z40(T node) {
    return z(node, 40);
  }

  public static <T extends Node> T z50(T node) {
    return z(node, 50);
  }

  /** .order-first — brings the node to the front */
  public static <T extends Node> T orderFirst(T node) {
    node.toFront();
    return node;
  }

  /** .order-last — sends the node to the back */
  public static <T extends Node> T orderLast(T node) {
    node.toBack();
    return node;
  }

  // SELF ALIGNMENT — self-start, self-center, self-end, self-stretch
  // Maps to: align-self in web CSS (GridPane)

  public static <T extends Node> T selfStart(T node) {
    GridPane.setValignment(node, VPos.TOP);
    return node;
  }

  public static <T extends Node> T selfCenter(T node) {
    GridPane.setValignment(node, VPos.CENTER);
    return node;
  }

  public static <T extends Node> T selfEnd(T node) {
    GridPane.setValignment(node, VPos.BOTTOM);
    return node;
  }

  public static <T extends Node> T selfBaseline(T node) {
    GridPane.setValignment(node, VPos.BASELINE);
    return node;
  }

  // JUSTIFY SELF — justify-self-start, justify-self-center, justify-self-end
  // Maps to: justify-self in web CSS (GridPane)

  public static <T extends Node> T justifySelfStart(T node) {
    GridPane.setHalignment(node, HPos.LEFT);
    return node;
  }

  public static <T extends Node> T justifySelfCenter(T node) {
    GridPane.setHalignment(node, HPos.CENTER);
    return node;
  }

  public static <T extends Node> T justifySelfEnd(T node) {
    GridPane.setHalignment(node, HPos.RIGHT);
    return node;
  }

  // COLORADJUST FILTERS — grayscale, brightness, contrast, saturate,
  //                        hueRotate, invert, sepia
  // Maps to: filter: grayscale(), brightness(), etc. in web CSS

  /** .grayscale — removes color from the node */
  public static <T extends Node> T grayscale(T node) {
    Preconditions.requireNode(node, "Styles.grayscale");
    node.setEffect(new ColorAdjust(0, -1, 0, 0));
    return node;
  }

  /** .grayscale-0 — removes the grayscale filter */
  public static <T extends Node> T grayscale0(T node) {
    if (node.getEffect() instanceof ColorAdjust) node.setEffect(null);
    return node;
  }

  /**
   * .brightness-{n} — adjusts brightness (0=black, 1=normal, 2=double). Tailwind uses 0-200; here
   * it takes the fraction directly. brightness(0.5) = darken by half, brightness(1.5) = brighten 50%
   */
  public static <T extends Node> T brightness(T node, double value) {
    Preconditions.requireNode(node, "Styles.brightness");
    Preconditions.warnBrightnessRange(value, "Styles.brightness");
    // ColorAdjust brightness: -1 (black) to 1 (white), 0 = normal
    double adjusted = value - 1.0; // 1.0 → 0, 0.5 → -0.5, 1.5 → 0.5
    applyOrMergeColorAdjust(node, adjusted, Double.NaN, Double.NaN, Double.NaN);
    return node;
  }

  /** .contrast-{n} — adjusts contrast (1 = normal) */
  public static <T extends Node> T contrast(T node, double value) {
    Preconditions.requireNode(node, "Styles.contrast");
    double adjusted = value - 1.0;
    applyOrMergeColorAdjust(node, Double.NaN, Double.NaN, adjusted, Double.NaN);
    return node;
  }

  /** .saturate-{n} — adjusts saturation (0 = gray, 1 = normal, >1 = more vivid) */
  public static <T extends Node> T saturate(T node, double value) {
    Preconditions.requireNode(node, "Styles.saturate");
    double adjusted = value - 1.0;
    applyOrMergeColorAdjust(node, Double.NaN, adjusted, Double.NaN, Double.NaN);
    return node;
  }

  /** .hue-rotate-{deg} — rotates the hue in degrees (0-360) */
  public static <T extends Node> T hueRotate(T node, double degrees) {
    Preconditions.requireNode(node, "Styles.hueRotate");
    // ColorAdjust hue: -1 to 1 (represents -180° to +180°)
    double adjusted = degrees / 180.0;
    applyOrMergeColorAdjust(node, Double.NaN, Double.NaN, Double.NaN, adjusted);
    return node;
  }

  /** .invert — inverts the node's colors */
  public static <T extends Node> T invert(T node) {
    Preconditions.requireNode(node, "Styles.invert");
    // ColorAdjust has no direct property for a full invert
    // It is approximated with hue=1 + brightness=-1
    node.setEffect(new ColorAdjust(1.0, 0, -0.5, 0));
    return node;
  }

  /** .sepia — applies a sepia tone to the node */
  public static <T extends Node> T sepia(T node) {
    Preconditions.requireNode(node, "Styles.sepia");
    // Sepia: desaturate + warm tone shift
    node.setEffect(new ColorAdjust(0.1, -0.5, 0.1, 0));
    return node;
  }

  /** Removes every ColorAdjust filter from the node */
  public static <T extends Node> T filterNone(T node) {
    if (node.getEffect() instanceof ColorAdjust) node.setEffect(null);
    return node;
  }

  // ASPECT RATIO — aspect-square, aspect-video, aspect-[w/h]
  // JavaFX CSS has no native aspect-ratio property; the real mechanism is this width/height
  // binding (the generated stylesheet intentionally omits aspect-* rules except aspect-auto).

  /**
   * Binds a {@link Region}'s preferred height to its computed width so the node keeps the given
   * aspect ratio (width / height), mirroring the CSS {@code aspect-ratio} property.
   *
   * <p>The binding uses the region's actual width when available and falls back to the current
   * preferred width otherwise, so it behaves correctly inside flex/grid containers.
   *
   * @param node the region to constrain
   * @param ratio width divided by height (e.g. 16.0 / 9.0 for {@code aspect-video})
   * @param <T> the region type
   * @return the same node, for chaining
   */
  public static <T extends Region> T aspectRatio(T node, double ratio) {
    Preconditions.requireNode(node, "Styles.aspectRatio");
    if (ratio <= 0 || Double.isNaN(ratio) || Double.isInfinite(ratio)) {
      throw new IllegalArgumentException("aspectRatio requires a positive, finite ratio");
    }
    node.prefHeightProperty()
        .bind(
            Bindings.createDoubleBinding(
                () -> {
                  double width = node.getWidth();
                  if (width <= 0) {
                    width = node.getPrefWidth();
                  }
                  return width > 0 ? width / ratio : Region.USE_COMPUTED_SIZE;
                },
                node.widthProperty(),
                node.prefWidthProperty()));
    return node;
  }

  /** Shortcut for {@code aspect-square} (1:1). */
  public static <T extends Region> T aspectSquare(T node) {
    return aspectRatio(node, 1.0);
  }

  /** Shortcut for {@code aspect-video} (16:9). */
  public static <T extends Region> T aspectVideo(T node) {
    return aspectRatio(node, 16.0 / 9.0);
  }

  /** Shortcut for {@code aspect-portrait} (3:4). */
  public static <T extends Region> T aspectPortrait(T node) {
    return aspectRatio(node, 3.0 / 4.0);
  }

  /** Shortcut for {@code aspect-landscape} (4:3). */
  public static <T extends Region> T aspectLandscape(T node) {
    return aspectRatio(node, 4.0 / 3.0);
  }

  // SKEW — skew-x-*, skew-y-*
  // Maps to: transform: skewX(), skewY() in web CSS

  /** .skew-x-{degrees} — skews the node along the X axis */
  public static <T extends Node> T skewX(T node, double degrees) {
    Preconditions.requireNode(node, "Styles.skewX");
    double kx = Math.tan(Math.toRadians(degrees));
    applyOrMergeShear(node, kx, 0);
    return node;
  }

  /** .skew-y-{degrees} — skews the node along the Y axis */
  public static <T extends Node> T skewY(T node, double degrees) {
    Preconditions.requireNode(node, "Styles.skewY");
    double ky = Math.tan(Math.toRadians(degrees));
    applyOrMergeShear(node, 0, ky);
    return node;
  }

  /** Shorthands for standard Tailwind values */
  public static <T extends Node> T skewX1(T node) {
    return skewX(node, 1);
  }

  public static <T extends Node> T skewX2(T node) {
    return skewX(node, 2);
  }

  public static <T extends Node> T skewX3(T node) {
    return skewX(node, 3);
  }

  public static <T extends Node> T skewX6(T node) {
    return skewX(node, 6);
  }

  public static <T extends Node> T skewX12(T node) {
    return skewX(node, 12);
  }

  public static <T extends Node> T skewY1(T node) {
    return skewY(node, 1);
  }

  public static <T extends Node> T skewY2(T node) {
    return skewY(node, 2);
  }

  public static <T extends Node> T skewY3(T node) {
    return skewY(node, 3);
  }

  public static <T extends Node> T skewY6(T node) {
    return skewY(node, 6);
  }

  public static <T extends Node> T skewY12(T node) {
    return skewY(node, 12);
  }

  // IMAGEVIEW — object-fit, object-position, object-cover, object-contain
  // Maps to: object-fit, object-position in web CSS

  /** .object-cover — fills the area by cropping if necessary */
  public static ImageView objectCover(ImageView iv) {
    Preconditions.requireNonNull(iv, "Styles.objectCover", "imageView");
    iv.setPreserveRatio(false);
    return iv;
  }

  /** .object-contain — fits the whole image while preserving its ratio */
  public static ImageView objectContain(ImageView iv) {
    Preconditions.requireNonNull(iv, "Styles.objectContain", "imageView");
    iv.setPreserveRatio(true);
    return iv;
  }

  /** .object-fill — stretches to fill exactly (distorts) */
  public static ImageView objectFill(ImageView iv) {
    iv.setPreserveRatio(false);
    iv.setSmooth(true);
    return iv;
  }

  /**
   * .object-top — shows the top part of the image. Requires fitWidth and fitHeight to be set.
   */
  public static ImageView objectTop(ImageView iv) {
    if (iv.getImage() != null) {
      double w = iv.getFitWidth() > 0 ? iv.getFitWidth() : iv.getImage().getWidth();
      double h = iv.getFitHeight() > 0 ? iv.getFitHeight() : iv.getImage().getHeight();
      iv.setViewport(new javafx.geometry.Rectangle2D(0, 0, iv.getImage().getWidth(), h));
    }
    return iv;
  }

  /** .object-center — shows the center of the image */
  public static ImageView objectCenter(ImageView iv) {
    Preconditions.requireNonNull(iv, "Styles.objectCenter", "imageView");
    Preconditions.warnNoImage(iv, "Styles.objectCenter");
    if (iv.getImage() != null) {
      double iw = iv.getImage().getWidth();
      double ih = iv.getImage().getHeight();
      double fw = iv.getFitWidth() > 0 ? iv.getFitWidth() : iw;
      double fh = iv.getFitHeight() > 0 ? iv.getFitHeight() : ih;
      double x = Math.max(0, (iw - fw) / 2);
      double y = Math.max(0, (ih - fh) / 2);
      iv.setViewport(new javafx.geometry.Rectangle2D(x, y, Math.min(fw, iw), Math.min(fh, ih)));
    }
    return iv;
  }

  /** Sets the ImageView size and fit mode in a single call */
  public static ImageView imgSize(
      ImageView iv, double width, double height, boolean preserveRatio) {
    iv.setFitWidth(width);
    iv.setFitHeight(height);
    iv.setPreserveRatio(preserveRatio);
    return iv;
  }

  // NODE VISIBILITY — hidden, visible, managed

  /** .hidden — hides the node but keeps its space in the layout */
  public static <T extends Node> T invisible(T node) {
    Preconditions.requireNode(node, "Styles.invisible");
    node.setOpacity(0);
    return node;
  }

  /** .hidden-node — hides the node AND removes its space from the layout */
  public static <T extends Node> T hiddenNode(T node) {
    Preconditions.requireNode(node, "Styles.hiddenNode");
    node.setVisible(false);
    node.setManaged(false);
    return node;
  }

  /** Restores visibility and layout space */
  public static <T extends Node> T show(T node) {
    Preconditions.requireNode(node, "Styles.show");
    node.setVisible(true);
    node.setManaged(true);
    node.setOpacity(1);
    return node;
  }

  // INTERNAL

  private static void applyOrMergeColorAdjust(
      Node node, double brightness, double saturation, double contrast, double hue) {
    ColorAdjust ca =
        (node.getEffect() instanceof ColorAdjust existing) ? existing : new ColorAdjust();
    if (!Double.isNaN(brightness)) ca.setBrightness(brightness);
    if (!Double.isNaN(saturation)) ca.setSaturation(saturation);
    if (!Double.isNaN(contrast)) ca.setContrast(contrast);
    if (!Double.isNaN(hue)) ca.setHue(hue);
    node.setEffect(ca);
  }

  private static void applyOrMergeShear(Node node, double kx, double ky) {
    // Merge with any existing Shear so skew-x and skew-y compose instead of the second call
    // discarding the first. A zero component on one axis keeps the value already set there.
    Shear existing = null;
    for (var t : node.getTransforms()) {
      if (t instanceof Shear s) {
        existing = s;
        break;
      }
    }
    double newKx = kx != 0 ? kx : (existing != null ? existing.getX() : 0);
    double newKy = ky != 0 ? ky : (existing != null ? existing.getY() : 0);

    node.getTransforms().removeIf(t -> t instanceof Shear);
    if (newKx != 0 || newKy != 0) {
      node.getTransforms().add(new Shear(newKx, newKy));
    }
  }

  // TEXT-SHADOW — Java API (DropShadow on Text child of Label/Text node)

  /**
   * Applies a text shadow to a {@link javafx.scene.text.Text} or {@link javafx.scene.control.Label}
   * node (analogous to CSS {@code text-shadow}).
   *
   * <p>Unlike the CSS {@code .text-shadow-*} classes (which apply DropShadow to the whole Label
   * box), this method targets the {@code Text} child inside a {@code Label} for a true text-only
   * shadow.
   *
   * <pre>
   * // text-shadow: 0 2px 4px rgba(0,0,0,0.30)
   * Styles.textShadow(heading, "rgba(0,0,0,0.30)", 4, 0, 2);
   *
   * // Named presets:
   * Styles.textShadowSm(label);
   * Styles.textShadowLg(label);
   * Styles.textShadowColor(label, "#3b82f6", 6, 0, 2); // colored
   * </pre>
   *
   * @param node the Text or Label node
   * @param color CSS color string (hex, rgba, color name)
   * @param radius blur radius in px
   * @param offsetX horizontal offset in px
   * @param offsetY vertical offset in px
   */
  public static void textShadow(
      Node node, String color, double radius, double offsetX, double offsetY) {
    Preconditions.requireNonNull(node, "Styles.textShadow", "node");
    Preconditions.requireNonBlank(color, "Styles.textShadow", "color");
    javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
    ds.setColor(javafx.scene.paint.Color.web(color));
    ds.setRadius(radius);
    ds.setOffsetX(offsetX);
    ds.setOffsetY(offsetY);
    ds.setSpread(0);
    node.setEffect(ds);
  }

  /** {@code text-shadow-sm} — subtle shadow. */
  public static void textShadowSm(Node node) {
    textShadow(node, "rgba(0,0,0,0.20)", 2, 0, 1);
  }

  /** {@code text-shadow} — standard shadow. */
  public static void textShadowMd(Node node) {
    textShadow(node, "rgba(0,0,0,0.30)", 4, 0, 2);
  }

  /** {@code text-shadow-lg} — large shadow. */
  public static void textShadowLg(Node node) {
    textShadow(node, "rgba(0,0,0,0.40)", 10, 0, 4);
  }

  /** {@code text-shadow-xl} — extra large shadow. */
  public static void textShadowXl(Node node) {
    textShadow(node, "rgba(0,0,0,0.50)", 16, 0, 6);
  }

  /**
   * Colored text shadow — analogous to Tailwind v4.1's arbitrary text-shadow with a named color.
   *
   * @param node the Text or Label node
   * @param color color string (hex, rgba)
   * @param radius blur radius
   * @param offsetX horizontal offset
   * @param offsetY vertical offset
   */
  public static void textShadowColor(
      Node node, String color, double radius, double offsetX, double offsetY) {
    textShadow(node, color, radius, offsetX, offsetY);
  }

  /** Removes any text shadow (or other effect) from the node. */
  public static void textShadowNone(Node node) {
    Preconditions.requireNonNull(node, "Styles.textShadowNone", "node");
    if (node.getEffect() instanceof javafx.scene.effect.DropShadow) node.setEffect(null);
  }

  // COLORED DROP-SHADOW — arbitrary color dropshadow

  /**
   * Applies a colored drop shadow (analogous to Tailwind v4.1's {@code drop-shadow-[color]}
   * arbitrary utility).
   *
   * <pre>
   * // drop-shadow-blue/50 approximation:
   * Styles.dropShadow(card, "#3b82f6", 0.5, 12, 0, 4);
   *
   * // drop-shadow-none:
   * Styles.dropShadowNone(card);
   * </pre>
   *
   * @param node the node to shadow
   * @param hexColor hex color (e.g. {@code "#3b82f6"})
   * @param alpha alpha multiplier [0.0, 1.0]
   * @param radius blur radius
   * @param offsetX horizontal offset
   * @param offsetY vertical offset
   */
  public static void dropShadow(
      Node node, String hexColor, double alpha, double radius, double offsetX, double offsetY) {
    Preconditions.requireNonNull(node, "Styles.dropShadow", "node");
    Preconditions.requireNonBlank(hexColor, "Styles.dropShadow", "hexColor");
    alpha = Math.max(0, Math.min(1, alpha));
    javafx.scene.paint.Color base = javafx.scene.paint.Color.web(hexColor);
    javafx.scene.paint.Color colored =
        new javafx.scene.paint.Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    javafx.scene.effect.DropShadow ds =
        new javafx.scene.effect.DropShadow(
            javafx.scene.effect.BlurType.GAUSSIAN, colored, radius, 0, offsetX, offsetY);
    node.setEffect(ds);
  }

  /** Colored drop shadow — presets by semantic color. */
  public static void dropShadowBlue(Node n) {
    dropShadow(n, "#3b82f6", 0.4, 12, 0, 4);
  }

  public static void dropShadowGreen(Node n) {
    dropShadow(n, "#22c55e", 0.4, 12, 0, 4);
  }

  public static void dropShadowRed(Node n) {
    dropShadow(n, "#ef4444", 0.4, 12, 0, 4);
  }

  public static void dropShadowPurple(Node n) {
    dropShadow(n, "#a855f7", 0.4, 12, 0, 4);
  }

  public static void dropShadowYellow(Node n) {
    dropShadow(n, "#eab308", 0.4, 12, 0, 4);
  }

  /** Removes drop shadow effect from node. */
  public static void dropShadowNone(Node node) {
    filterNone(node);
  }

  // MASK / CLIP UTILITIES

  /**
   * Clips a node to a rectangle with optional corner rounding (analogous to CSS {@code clip-path:
   * inset(...)}).
   *
   * @param node the node to clip
   * @param width clip width in px
   * @param height clip height in px
   * @param radius corner radius in px (0 = sharp corners)
   * @return the same node, for chaining
   */
  public static <T extends Node> T clipRect(T node, double width, double height, double radius) {
    Preconditions.requireNonNull(node, "Styles.clipRect", "node");
    detachClipListener(node);
    javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(width, height);
    clip.setArcWidth(radius * 2);
    clip.setArcHeight(radius * 2);
    node.setClip(clip);
    return node;
  }

  /**
   * Clips a node to a rectangle without corner rounding.
   *
   * @param node the node to clip
   * @param width clip width in px
   * @param height clip height in px
   * @return the same node, for chaining
   */
  public static <T extends Node> T clipRect(T node, double width, double height) {
    return clipRect(node, width, height, 0);
  }

  /**
   * Node property key under which the bounds listener installed by {@link #clipCircle} / {@link
   * #clipRounded} is stored, so a later {@link #clipNone} can detach it.
   */
  private static final String CLIP_LISTENER_KEY = "tailwindfx.clip.listener";

  /**
   * Clips a node to a circle centered in its bounds (analogous to CSS {@code mask-image:
   * radial-gradient(circle, ...)}).
   *
   * <p>Useful for avatar images and circular buttons.
   *
   * <pre>
   * Styles.clipCircle(avatarImageView);
   * // The node is now circular and follows its bounds. Styles.clipNone removes it.
   * </pre>
   *
   * @param node the node to clip
   */
  public static void clipCircle(Node node) {
    Preconditions.requireNonNull(node, "Styles.clipCircle", "node");
    javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle();
    // Keep the radius in sync with the node bounds and remember the listener so clipNone can
    // detach it; otherwise every re-clip would leave a stale listener on the node.
    applyBoundsClip(
        node,
        clip,
        (nv, shape) -> {
          javafx.scene.shape.Circle circle = (javafx.scene.shape.Circle) shape;
          double r = Math.min(nv.getWidth(), nv.getHeight()) / 2.0;
          circle.setCenterX(nv.getWidth() / 2.0);
          circle.setCenterY(nv.getHeight() / 2.0);
          circle.setRadius(r);
        });
  }

  /**
   * Clips a node to a rounded rectangle (analogous to CSS {@code overflow: hidden} + {@code
   * border-radius}).
   *
   * <pre>
   * Styles.clipRounded(imageView, 12); // 12px radius
   * </pre>
   *
   * @param node the node to clip
   * @param radius corner radius in px
   */
  public static void clipRounded(Node node, double radius) {
    Preconditions.requireNonNull(node, "Styles.clipRounded", "node");
    javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
    clip.setArcWidth(radius * 2);
    clip.setArcHeight(radius * 2);
    applyBoundsClip(
        node,
        clip,
        (nv, shape) -> {
          javafx.scene.shape.Rectangle rect = (javafx.scene.shape.Rectangle) shape;
          rect.setWidth(nv.getWidth());
          rect.setHeight(nv.getHeight());
        });
  }

  /** Installs a bounds-tracking clip and records its listener so {@link #clipNone} can remove it. */
  private static void applyBoundsClip(
      Node node,
      javafx.scene.shape.Shape clip,
      java.util.function.BiConsumer<javafx.geometry.Bounds, javafx.scene.shape.Shape> updater) {
    detachClipListener(node);
    updater.accept(node.getLayoutBounds(), clip);
    javafx.beans.value.ChangeListener<javafx.geometry.Bounds> listener =
        (obs, ov, nv) -> updater.accept(nv, clip);
    node.layoutBoundsProperty().addListener(listener);
    node.getProperties().put(CLIP_LISTENER_KEY, listener);
    node.setClip(clip);
  }

  /** Detaches the bounds listener installed by a previous bounds-tracking clip, if any. */
  @SuppressWarnings("unchecked")
  private static void detachClipListener(Node node) {
    Object recorded = node.getProperties().remove(CLIP_LISTENER_KEY);
    if (recorded instanceof javafx.beans.value.ChangeListener<?> listener) {
      node.layoutBoundsProperty()
          .removeListener((javafx.beans.value.ChangeListener<javafx.geometry.Bounds>) listener);
    }
  }

  /**
   * Clips a node to an arbitrary {@link javafx.scene.shape.Shape} (analogous to CSS {@code
   * mask-image} / {@code clip-path}).
   *
   * @param node the node to clip
   * @param shape the clipping shape (coordinates should match node bounds)
   */
  public static void clipMask(Node node, javafx.scene.shape.Shape shape) {
    Preconditions.requireNonNull(node, "Styles.clipMask", "node");
    Preconditions.requireNonNull(shape, "Styles.clipMask", "shape");
    detachClipListener(node);
    node.setClip(shape);
  }

  /**
   * Removes any clip/mask from the node, detaching the bounds listener installed by {@link
   * #clipCircle} or {@link #clipRounded} if one is present.
   *
   * @param node the node to clear
   */
  public static void clipNone(Node node) {
    Preconditions.requireNonNull(node, "Styles.clipNone", "node");
    detachClipListener(node);
    node.setClip(null);
  }

  // SVG HELPERS — fill-current, stroke-current

  /**
   * Sets the fill color of a {@link javafx.scene.shape.Shape} node to match the text color of a
   * reference node (analogous to CSS {@code fill: currentColor}).
   *
   * <pre>
   * Label label = new Label("Click me");
   * TwStyle.apply(label, "text-blue-500");
   * SVGPath icon = loadIcon();
   * Styles.fillCurrent(icon, label); // icon fill = label text color
   * </pre>
   *
   * @param shape the SVG shape whose fill to set
   * @param reference the node whose text color to read
   */
  public static void fillCurrent(javafx.scene.shape.Shape shape, Node reference) {
    Preconditions.requireNonNull(shape, "Styles.fillCurrent", "shape");
    Preconditions.requireNonNull(reference, "Styles.fillCurrent", "reference");
    shape.setFill(resolveTextFill(reference));
  }

  private static javafx.scene.paint.Paint resolveTextFill(Node reference) {
    // An explicit override wins, then the node's real text fill. Without this the method always
    // fell back to black because nothing in the codebase ever wrote the property.
    Object explicit = reference.getProperties().get("tailwindfx.text.color");
    if (explicit instanceof javafx.scene.paint.Paint paint) {
      return paint;
    }
    javafx.scene.paint.Paint textFill = null;
    if (reference instanceof javafx.scene.control.Labeled labeled) {
      textFill = labeled.getTextFill();
    } else if (reference instanceof javafx.scene.text.Text text) {
      textFill = text.getFill();
    }
    return textFill != null ? textFill : javafx.scene.paint.Color.BLACK;
  }

  /**
   * Sets an SVG shape's fill to an explicit color.
   *
   * @param shape the shape to fill
   * @param hex hex color string (e.g. {@code "#3b82f6"})
   */
  public static void fill(javafx.scene.shape.Shape shape, String hex) {
    Preconditions.requireNonNull(shape, "Styles.fill", "shape");
    Preconditions.requireNonBlank(hex, "Styles.fill", "hex");
    shape.setFill(javafx.scene.paint.Color.web(hex));
  }

  /** Sets an SVG shape's stroke to an explicit color. */
  public static void stroke(javafx.scene.shape.Shape shape, String hex) {
    Preconditions.requireNonNull(shape, "Styles.stroke", "shape");
    Preconditions.requireNonBlank(hex, "Styles.stroke", "hex");
    shape.setStroke(javafx.scene.paint.Color.web(hex));
    if (shape.getStrokeWidth() == 0) shape.setStrokeWidth(1);
  }

  /** Sets stroke width. Shorthand for {@code shape.setStrokeWidth(width)}. */
  public static void strokeWidth(javafx.scene.shape.Shape shape, double width) {
    Preconditions.requireNonNull(shape, "Styles.strokeWidth", "shape");
    shape.setStrokeWidth(width);
  }

  // 3D TRANSFORMS (JavaFX Rotate/Translate on 3 axes)

  /**
   * Rotates a node around the X axis (analogous to CSS {@code rotateX(deg)}).
   *
   * @param node the node to rotate
   * @param degrees angle in degrees
   */
  public static void rotateX(Node node, double degrees) {
    Preconditions.requireNonNull(node, "Styles.rotateX", "node");
    javafx.scene.transform.Rotate r =
        new javafx.scene.transform.Rotate(degrees, javafx.scene.transform.Rotate.X_AXIS);
    node.getTransforms()
        .removeIf(
            t ->
                t instanceof javafx.scene.transform.Rotate
                    && ((javafx.scene.transform.Rotate) t)
                        .getAxis()
                        .equals(javafx.scene.transform.Rotate.X_AXIS));
    if (degrees != 0) node.getTransforms().add(r);
  }

  /**
   * Rotates a node around the Y axis (analogous to CSS {@code rotateY(deg)}).
   *
   * @param node the node to rotate
   * @param degrees angle in degrees
   */
  public static void rotateY(Node node, double degrees) {
    Preconditions.requireNonNull(node, "Styles.rotateY", "node");
    javafx.scene.transform.Rotate r =
        new javafx.scene.transform.Rotate(degrees, javafx.scene.transform.Rotate.Y_AXIS);
    node.getTransforms()
        .removeIf(
            t ->
                t instanceof javafx.scene.transform.Rotate
                    && ((javafx.scene.transform.Rotate) t)
                        .getAxis()
                        .equals(javafx.scene.transform.Rotate.Y_AXIS));
    if (degrees != 0) node.getTransforms().add(r);
  }

  /**
   * Translates a node along the Z axis (analogous to CSS {@code translateZ(px)}). In JavaFX this
   * changes {@link Node#setTranslateZ(double)}.
   *
   * @param node the node to translate
   * @param px Z offset in px (positive = closer to viewer)
   */
  public static void translateZ(Node node, double px) {
    Preconditions.requireNonNull(node, "Styles.translateZ", "node");
    node.setTranslateZ(px);
  }

  /**
   * Applies a perspective camera effect (depth perception).
   *
   * <p>Enables 3D depth on the node's parent scene by ensuring the scene uses a {@link
   * javafx.scene.PerspectiveCamera}. The {@code focalLength} controls the perspective strength —
   * lower values create more dramatic perspective.
   *
   * @param node any node in a Scene
   * @param focalLength camera focal length (default ≈ 1000)
   */
  public static void perspective(Node node, double focalLength) {
    Preconditions.requireNonNull(node, "Styles.perspective", "node");
    if (node.getScene() == null) return;
    javafx.scene.PerspectiveCamera cam = new javafx.scene.PerspectiveCamera(false);
    cam.setFieldOfView(
        Math.toDegrees(2 * Math.atan(node.getScene().getHeight() / 2 / focalLength)));
    node.getScene().setCamera(cam);
  }

  /** Resets all 3D transforms (rotateX, rotateY, translateZ). */
  public static void reset3D(Node node) {
    Preconditions.requireNonNull(node, "Styles.reset3D", "node");
    node.getTransforms()
        .removeIf(
            t ->
                t instanceof javafx.scene.transform.Rotate
                    && !((javafx.scene.transform.Rotate) t)
                        .getAxis()
                        .equals(javafx.scene.transform.Rotate.Z_AXIS));
    node.setTranslateZ(0);
  }

  // MOTION-REDUCE — respect prefers-reduced-motion

  /** System property key for reduced motion preference. */
  private static final String REDUCED_MOTION_KEY = "tailwindfx.motion.reduced";

  /**
   * Sets the reduced-motion preference globally. When {@code true}, {@link #shouldAnimate()}
   * returns {@code false}, allowing callers to skip or shorten animations (analogous to CSS {@code
   * prefers-reduced-motion}).
   *
   * <pre>
   * // Respect OS reduced-motion setting:
   * Styles.setReducedMotion(Toolkit.getDefaultToolkit()
   *     .getDesktopProperty("gnome.reduceAnimations") != null);
   *
   * // Or force it in tests:
   * Styles.setReducedMotion(true);
   * if (Styles.shouldAnimate()) TwAnimation.fadeIn(node).play();
   * </pre>
   *
   * @param reduced {@code true} to suppress animations
   */
  public static void setReducedMotion(boolean reduced) {
    System.setProperty(REDUCED_MOTION_KEY, String.valueOf(reduced));
  }

  /**
   * Returns {@code true} if animations should run (motion is NOT reduced). Check this before
   * starting non-essential animations.
   *
   * @return {@code false} when reduced-motion is enabled
   */
  public static boolean shouldAnimate() {
    return !"true".equalsIgnoreCase(System.getProperty(REDUCED_MOTION_KEY, "false"));
  }

  /**
   * Plays an animation only if motion is not reduced. If reduced, the final state is applied
   * instantly.
   *
   * <pre>
   * Styles.playIfMotionOk(TwAnimation.fadeIn(node, 300));
   * </pre>
   *
   * @param animation the animation to conditionally play
   */
  public static void playIfMotionOk(TwAnimation animation) {
    Preconditions.requireNonNull(animation, "Styles.playIfMotionOk", "animation");
    if (shouldAnimate()) {
      animation.play();
    } else {
      // Jump to end state: play at very high speed for 1 frame then stop
      animation.speed(100.0).play();
    }
  }

  // GLASSMORPHISM / NEUMORPHISM PRESETS

  /**
   * Applies a glassmorphism effect to a node.
   *
   * <p>Sets semi-transparent background, border, and blur effect. For best results, place the node
   * over a colorful background.
   *
   * <pre>
   * Styles.glass(panel);
   * TwEffect.backdropBlur(panel, 12); // blur the content behind
   * </pre>
   *
   * @param node the node to style as glass
   */
  public static void glass(Region node) {
    Preconditions.requireNonNull(node, "Styles.glass", "node");
    node.setStyle(
        node.getStyle()
            + " -fx-background-color: rgba(255,255,255,0.15);"
            + " -fx-background-radius: 12;"
            + " -fx-border-color: rgba(255,255,255,0.30);"
            + " -fx-border-radius: 12;"
            + " -fx-border-width: 1;");
    node.setEffect(new javafx.scene.effect.BoxBlur(12, 12, 3));
  }

  /** Dark-tinted glass variant (for dark backgrounds). */
  public static void glassDark(Region node) {
    Preconditions.requireNonNull(node, "Styles.glassDark", "node");
    node.setStyle(
        node.getStyle()
            + " -fx-background-color: rgba(0,0,0,0.20);"
            + " -fx-background-radius: 12;"
            + " -fx-border-color: rgba(255,255,255,0.10);"
            + " -fx-border-radius: 12;"
            + " -fx-border-width: 1;");
    node.setEffect(new javafx.scene.effect.BoxBlur(12, 12, 3));
  }

  /**
   * Applies a neumorphism (soft-UI) effect to a node.
   *
   * <p>Creates the characteristic dual-shadow look. Background color of the node should be {@code
   * #e0e5ec} for best results.
   *
   * @param node the Region to style
   */
  public static void neumorph(Region node) {
    Preconditions.requireNonNull(node, "Styles.neumorph", "node");
    node.setStyle(
        node.getStyle() + " -fx-background-color: #e0e5ec;" + " -fx-background-radius: 12;");
    node.setEffect(
        new javafx.scene.effect.DropShadow(
            javafx.scene.effect.BlurType.GAUSSIAN,
            javafx.scene.paint.Color.web("#a3b1c6"),
            10,
            0,
            -4,
            -4));
  }

  /** Inset neumorphism (pressed state). */
  public static void neumorphInset(Region node) {
    Preconditions.requireNonNull(node, "Styles.neumorphInset", "node");
    node.setStyle(
        node.getStyle() + " -fx-background-color: #e0e5ec;" + " -fx-background-radius: 12;");
    node.setEffect(
        new javafx.scene.effect.InnerShadow(
            javafx.scene.effect.BlurType.GAUSSIAN,
            javafx.scene.paint.Color.web("#a3b1c6"),
            10,
            0,
            4,
            4));
  }

  // USER-SELECT - Text selection control
  // Maps to: user-select: none/text/all/auto in web CSS

  /** .select-none — Disables text selection */
  public static <T extends Node> T selectNone(T node) {
    node.setMouseTransparent(true);
    return node;
  }

  /** .select-text — Enables text selection (default behavior) */
  public static <T extends Node> T selectText(T node) {
    node.setMouseTransparent(false);
    return node;
  }

  /** .select-all — Selects all text on click */
  public static <T extends Node> T selectAll(T node) {
    if (node instanceof javafx.scene.control.TextInputControl) {
      node.setOnMouseClicked(
          e -> {
            ((javafx.scene.control.TextInputControl) node).selectAll();
          });
    }
    return node;
  }

  /** .select-auto — Automatic selection behavior based on context */
  public static <T extends Node> T selectAuto(T node) {
    node.setMouseTransparent(false);
    return node;
  }

  // TOUCH-ACTION - Touch gesture control
  // Maps to: touch-action: auto/none/pan-x/pan-y/pinch-zoom/manipulation in web CSS

  /** .touch-auto — Default touch gesture handling */
  public static <T extends Node> T touchAuto(T node) {
    node.getProperties().remove("touch-action");
    return node;
  }

  /** .touch-none — Disables all touch gestures */
  @SuppressWarnings("unchecked")
  public static <T extends Node> T touchNone(T node) {
    node.getProperties().put("touch-action", "none");
    if (node instanceof Region) {
      ((Region) node).addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> e.consume());
      ((Region) node).addEventFilter(javafx.scene.input.ZoomEvent.ZOOM, e -> e.consume());
      ((Region) node).addEventFilter(javafx.scene.input.RotateEvent.ROTATE, e -> e.consume());
    }
    return node;
  }

  /** .touch-pan-x — Allows only horizontal scrolling */
  @SuppressWarnings("unchecked")
  public static <T extends Node> T touchPanX(T node) {
    node.getProperties().put("touch-action", "pan-x");
    if (node instanceof Region) {
      ((Region) node)
          .addEventFilter(
              javafx.scene.input.ScrollEvent.SCROLL,
              e -> {
                if (Math.abs(e.getDeltaY()) > Math.abs(e.getDeltaX())) {
                  e.consume();
                }
              });
    }
    return node;
  }

  /** .touch-pan-y — Allows only vertical scrolling */
  @SuppressWarnings("unchecked")
  public static <T extends Node> T touchPanY(T node) {
    node.getProperties().put("touch-action", "pan-y");
    if (node instanceof Region) {
      ((Region) node)
          .addEventFilter(
              javafx.scene.input.ScrollEvent.SCROLL,
              e -> {
                if (Math.abs(e.getDeltaX()) > Math.abs(e.getDeltaY())) {
                  e.consume();
                }
              });
    }
    return node;
  }

  /** .touch-pinch-zoom — Allows pinch-to-zoom gestures */
  public static <T extends Node> T touchPinchZoom(T node) {
    node.getProperties().put("touch-action", "pinch-zoom");
    return node;
  }

  /** .touch-manipulation — Allows panning without zooming */
  @SuppressWarnings("unchecked")
  public static <T extends Node> T touchManipulation(T node) {
    node.getProperties().put("touch-action", "manipulation");
    if (node instanceof Region) {
      ((Region) node).addEventFilter(javafx.scene.input.ZoomEvent.ZOOM, e -> e.consume());
    }
    return node;
  }

  // SCROLL-SNAP — Snap scrolling behavior
  // Maps to: scroll-snap-align/type in web CSS

  /** .snap-start — Snap to start of container */
  public static <T extends Region> T snapStart(T node) {
    node.getProperties().put("scroll-snap-align", "start");
    return node;
  }

  /** .snap-end — Snap to end of container */
  public static <T extends Region> T snapEnd(T node) {
    node.getProperties().put("scroll-snap-align", "end");
    return node;
  }

  /** .snap-center — Snap to center of container */
  public static <T extends Region> T snapCenter(T node) {
    node.getProperties().put("scroll-snap-align", "center");
    return node;
  }

  /** .snap-none — No snapping alignment */
  public static <T extends Region> T snapAlignNone(T node) {
    node.getProperties().put("scroll-snap-align", "none");
    return node;
  }

  /** .snap-x — Enable snapping on X axis */
  public static <T extends Region> T snapX(T node) {
    node.getProperties().put("scroll-snap-type", "x");
    return node;
  }

  /** .snap-y — Enable snapping on Y axis */
  public static <T extends Region> T snapY(T node) {
    node.getProperties().put("scroll-snap-type", "y");
    return node;
  }

  /** .snap-both — Enable snapping on both axes */
  public static <T extends Region> T snapBoth(T node) {
    node.getProperties().put("scroll-snap-type", "both");
    return node;
  }

  /** .snap-mandatory — Always snap to target */
  public static <T extends Region> T snapMandatory(T node) {
    node.getProperties().put("scroll-snap-stop", "mandatory");
    return node;
  }

  /** .snap-proximity — Snap only when close to target */
  public static <T extends Region> T snapProximity(T node) {
    node.getProperties().put("scroll-snap-stop", "normal");
    return node;
  }

  // COLUMNS — Multi-column layout
  // Maps to: columns-N/auto in web CSS

  /** .columns-1 — Single column layout */
  public static <T extends Pane> T columns1(T node) {
    return applyColumns(node, 1);
  }

  /** .columns-2 — Two column layout */
  public static <T extends Pane> T columns2(T node) {
    return applyColumns(node, 2);
  }

  /** .columns-3 — Three column layout */
  public static <T extends Pane> T columns3(T node) {
    return applyColumns(node, 3);
  }

  /** .columns-4 — Four column layout */
  public static <T extends Pane> T columns4(T node) {
    return applyColumns(node, 4);
  }

  /** .columns-5 — Five column layout */
  public static <T extends Pane> T columns5(T node) {
    return applyColumns(node, 5);
  }

  /** .columns-6 — Six column layout */
  public static <T extends Pane> T columns6(T node) {
    return applyColumns(node, 6);
  }

  /** .columns-7 — Seven column layout */
  public static <T extends Pane> T columns7(T node) {
    return applyColumns(node, 7);
  }

  /** .columns-8 — Eight column layout */
  public static <T extends Pane> T columns8(T node) {
    return applyColumns(node, 8);
  }

  /** .columns-9 — Nine column layout */
  public static <T extends Pane> T columns9(T node) {
    return applyColumns(node, 9);
  }

  /** .columns-10 — Ten column layout */
  public static <T extends Pane> T columns10(T node) {
    return applyColumns(node, 10);
  }

  /** .columns-11 — Eleven column layout */
  public static <T extends Pane> T columns11(T node) {
    return applyColumns(node, 11);
  }

  /** .columns-12 — Twelve column layout */
  public static <T extends Pane> T columns12(T node) {
    return applyColumns(node, 12);
  }

  /** .columns-auto — Automatic column layout based on content */
  public static <T extends Pane> T columnsAuto(T node) {
    return applyColumns(node, -1);
  }

  private static <T extends Pane> T applyColumns(T node, int count) {
    if (count <= 0) {
      node.getProperties().put("columns", "auto");
    } else {
      node.getProperties().put("columns", String.valueOf(count));
      node.setPrefWidth(count * 100);
    }
    return node;
  }

  /**
   * Checks whether {@code token} is a known utility class that can be applied via CSS.
   *
   * <p>Delegates to {@link TokenRegistry#isKnownUtility(String)}, which is the single source of
   * truth for token knowledge; the previous local regex copy had drifted and rejected valid tokens
   * such as {@code bg-red-500}, {@code gap-4}, {@code flex-col} or negative margins.
   *
   * @param token the token to validate
   * @return {@code true} if the token is a recognized utility class
   */
  public static boolean isKnownUtilityClass(String token) {
    return TokenRegistry.isKnownUtility(token);
  }
}
