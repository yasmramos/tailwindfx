package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.TwStyle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * TwBadge — Pre-styled badge and pill components.
 *
 * <p>Uses base .badge class from tailwindfx-components.css with utility modifiers.
 *
 * <pre>
 * TwBadge badge = TwBadge.create("NEW", "blue");
 * TwBadge pill = TwBadge.pill("Active", "green");
 * TwBadge dot = TwBadge.dot("Online", "green");
 * </pre>
 */
public class TwBadge extends Label {

  /** Base stylesheet class applied to every badge. */
  private static final String BASE_CLASS = "badge";

  /** Modifier applied by {@link #pill(String, String)}. */
  private static final String PILL_CLASS = "badge-pill";

  /** Modifier applied by {@link #outline(String, String)}. */
  private static final String OUTLINE_CLASS = "badge-outline";

  /** Modifier applied by the dot variant, which renders bare text next to a dot. */
  private static final String COMPACT_CLASS = "badge-compact";

  /** Modifier applied to the status dot of the dot variant. */
  private static final String DOT_CLASS = "dot";

  /**
   * Creates a small badge label (e.g. NEW, BETA, PRO).
   *
   * @param text badge text
   * @return styled TwBadge with default blue color
   */
  public static TwBadge create(String text) {
    return create(text, "blue");
  }

  /**
   * Creates a small badge label with custom color.
   *
   * @param text badge text
   * @param color Tailwind color name (e.g. "blue", "green", "red")
   * @return styled TwBadge
   */
  public static TwBadge create(String text, String color) {
    TwBadge lbl = new TwBadge();
    lbl.setText(text == null ? "" : text.toUpperCase());
    TwStyle.apply(lbl, BASE_CLASS, "badge-" + color, "badge-md");
    return lbl;
  }

  /**
   * Creates a rounded pill / chip label.
   *
   * @param text pill text
   * @return styled TwBadge with default blue color
   */
  public static TwBadge pill(String text) {
    return pill(text, "blue");
  }

  /**
   * Creates a rounded pill / chip label with custom color.
   *
   * @param text pill text
   * @param color Tailwind color name
   * @return styled TwBadge
   */
  public static TwBadge pill(String text, String color) {
    TwBadge lbl = create(text, color);
    TwStyle.apply(lbl, PILL_CLASS);
    return lbl;
  }

  /**
   * Creates a badge with a status dot indicator.
   *
   * @param text badge text
   * @param color Tailwind color name for the dot
   * @return TwBadgeDot container with dot and text
   */
  public static TwBadgeDot dot(String text, String color) {
    return dot(text, color, "gray");
  }

  /**
   * Creates a badge with a status dot indicator.
   *
   * @param text badge text
   * @param dotColor Tailwind color name for the dot
   * @param textColor Tailwind color name for the text
   * @return TwBadgeDot container with dot and text
   */
  public static TwBadgeDot dot(String text, String dotColor, String textColor) {
    return new TwBadgeDot(text, dotColor, textColor);
  }

  /**
   * Creates an outline badge (border only).
   *
   * @param text badge text
   * @return styled TwBadge with default gray color
   */
  public static TwBadge outline(String text) {
    return outline(text, "gray");
  }

  /**
   * Creates an outline badge with custom color.
   *
   * @param text badge text
   * @param color Tailwind color name
   * @return styled TwBadge
   */
  public static TwBadge outline(String text, String color) {
    TwBadge lbl = new TwBadge();
    lbl.setText(text == null ? "" : text.toUpperCase());
    TwStyle.apply(lbl, BASE_CLASS, OUTLINE_CLASS, "badge-" + color, "badge-md");
    return lbl;
  }

  /** Protected constructor for internal usage. */
  protected TwBadge() {
    super();
  }

  /** Container for badge with dot indicator. */
  public static class TwBadgeDot extends HBox {

    private final Label dot;
    private final TwBadge label;

    /**
     * Creates a badge with dot indicator.
     *
     * @param text badge text
     * @param dotColor Tailwind color name for the dot
     * @param textColor Tailwind color name for the text
     */
    public TwBadgeDot(String text, String dotColor, String textColor) {
      super();

      dot = new Label();
      TwStyle.apply(dot, DOT_CLASS, "dot-" + dotColor, "dot-sm");

      label = new TwBadge();
      label.setText(text == null ? "" : text);
      TwStyle.apply(label, BASE_CLASS, "badge-" + textColor, COMPACT_CLASS);
      label.setPadding(Insets.EMPTY);

      getChildren().addAll(dot, label);
      setSpacing(4);
      setAlignment(Pos.CENTER_LEFT);
    }

    /**
     * Gets the dot node.
     *
     * @return the dot Label
     */
    public Label getDot() {
      return dot;
    }

    /**
     * Gets the text label.
     *
     * @return the TwBadge label
     */
    public TwBadge getLabel() {
      return label;
    }
  }
}
