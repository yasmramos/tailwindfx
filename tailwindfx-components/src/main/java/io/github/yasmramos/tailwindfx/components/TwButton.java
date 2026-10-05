package io.github.yasmramos.tailwindfx.components;

import javafx.scene.control.Button;

/** TwButton — Custom button component extending JavaFX Button with TailwindCSS variants. */
public class TwButton extends Button {

  /** Scale applied while the pointer is over an enabled button. */
  private static final double HOVER_SCALE = 1.05;

  /** Prefix shared by every modifier class applied by this component. */
  private static final String MODIFIER_PREFIX = "btn-";

  /** Shape modifier for circular icon-only buttons. */
  private static final String ICON_CLASS = "btn-icon";

  /** Shape modifier forcing a circular background. */
  private static final String CIRCLE_CLASS = "btn-circle";

  /** Applied while the button is disabled by the caller. */
  private static final String DISABLED_CLASS = "btn-disabled";

  /** Applied while the button is in the loading state. */
  private static final String LOADING_CLASS = "btn-loading";

  private TwButtonVariant variant = TwButtonVariant.PRIMARY;
  private String color = "blue";
  private String size = "md";
  private boolean loading = false;

  /**
   * Disabled state requested by the caller, kept separate from {@link #loading} so a loading
   * toggle does not permanently override it.
   */
  private boolean disabledByUser = false;

  public TwButton(String text) {
    super(text);
    initialize();
  }

  private void initialize() {
    getStyleClass().add("btn");
    applyVariant();
    setupEventHandlers();
  }

  private void setupEventHandlers() {
    hoverProperty()
        .addListener(
            (obs, oldVal, hovering) -> {
              if (isDisabled()) {
                resetScale();
              } else if (hovering) {
                setScaleX(HOVER_SCALE);
                setScaleY(HOVER_SCALE);
              } else {
                resetScale();
              }
            });

    // A button that becomes disabled while hovered would otherwise stay scaled up, because the
    // hover listener only fires on hover transitions.
    disabledProperty().addListener((obs, oldVal, disabled) -> resetScale());
  }

  private void resetScale() {
    setScaleX(1.0);
    setScaleY(1.0);
  }

  private void applyVariant() {
    // Remove every btn-* modifier this component owns (variant, color, size) and rebuild it, but
    // keep classes managed elsewhere: the icon/circle shape modifiers and the state classes
    // driven by setLoading/setDisabled.
    getStyleClass()
        .removeIf(
            cls ->
                cls.startsWith(MODIFIER_PREFIX)
                    && !cls.equals(ICON_CLASS)
                    && !cls.equals(CIRCLE_CLASS)
                    && !cls.equals(DISABLED_CLASS)
                    && !cls.equals(LOADING_CLASS));

    switch (variant) {
      case PRIMARY:
        getStyleClass().add("btn-primary");
        break;
      case SECONDARY:
        getStyleClass().add("btn-secondary");
        break;
      case OUTLINE:
        getStyleClass().add("btn-outline");
        break;
      case GHOST:
        getStyleClass().add("btn-ghost");
        break;
      case DANGER:
        getStyleClass().add("btn-danger");
        break;
    }

    // Add color class
    if (color != null && !color.isEmpty()) {
      getStyleClass().add("btn-" + color);
    }

    // Add size class (only if not an icon button)
    if (size != null && !size.isEmpty()) {
      getStyleClass().add("btn-" + size);
    }
  }

  public void setVariant(TwButtonVariant variant) {
    this.variant = variant;
    applyVariant();
  }

  public TwButtonVariant getVariant() {
    return variant;
  }

  public void setColor(String color) {
    this.color = color;
    applyVariant();
  }

  public String getColor() {
    return color;
  }

  /**
   * Sets the loading state, disabling the button while work is in progress.
   *
   * <p>The disabled state is derived from the loading flag and the state previously set through
   * {@link #setDisabled(boolean)}, so clearing the loading flag restores the caller's intent
   * instead of always re-enabling the button.
   *
   * @param loading true while the associated action is running
   */
  public void setLoading(boolean loading) {
    this.loading = loading;
    getStyleClass().remove(LOADING_CLASS);
    getStyleClass().remove(DISABLED_CLASS);
    setDisabled(loading || disabledByUser);
    if (loading) {
      getStyleClass().add(LOADING_CLASS);
    }
  }

  /**
   * Reports whether the button is in the loading state.
   *
   * @return true while loading
   */
  public boolean isLoading() {
    return loading;
  }

  /**
   * Sets the disabled state, keeping it independent from the loading flag.
   *
   * @param disabled true to disable the button
   */
  @Override
  public void setDisabled(boolean disabled) {
    this.disabledByUser = disabled;
    super.setDisabled(disabled || loading);
    getStyleClass().remove(DISABLED_CLASS);
    if (disabled) {
      getStyleClass().add(DISABLED_CLASS);
    }
  }

  public static TwButton primary(String text) {
    TwButton btn = new TwButton(text);
    btn.setVariant(TwButtonVariant.PRIMARY);
    btn.setColor("blue");
    return btn;
  }

  public static TwButton primary(String text, String color) {
    TwButton btn = new TwButton(text);
    btn.setVariant(TwButtonVariant.PRIMARY);
    btn.setColor(color);
    return btn;
  }

  public static TwButton secondary(String text) {
    TwButton btn = new TwButton(text);
    btn.setVariant(TwButtonVariant.SECONDARY);
    btn.setColor("gray");
    return btn;
  }

  public static TwButton secondary(String text, String color) {
    TwButton btn = new TwButton(text);
    btn.setVariant(TwButtonVariant.SECONDARY);
    btn.setColor(color);
    return btn;
  }

  public static TwButton outline(String text) {
    TwButton btn = new TwButton(text);
    btn.setVariant(TwButtonVariant.OUTLINE);
    btn.setColor("gray");
    return btn;
  }

  public static TwButton outline(String text, String color) {
    TwButton btn = new TwButton(text);
    btn.setVariant(TwButtonVariant.OUTLINE);
    btn.setColor(color);
    return btn;
  }

  public static TwButton ghost(String text) {
    TwButton btn = new TwButton(text);
    btn.setVariant(TwButtonVariant.GHOST);
    btn.setColor("gray");
    return btn;
  }

  public static TwButton ghost(String text, String color) {
    TwButton btn = new TwButton(text);
    btn.setVariant(TwButtonVariant.GHOST);
    btn.setColor(color);
    return btn;
  }

  public static TwButton danger(String text) {
    TwButton btn = new TwButton(text);
    btn.setVariant(TwButtonVariant.DANGER);
    btn.setColor("red");
    return btn;
  }

  public static TwButton success(String text) {
    TwButton btn = new TwButton(text);
    btn.setVariant(TwButtonVariant.PRIMARY);
    btn.setColor("green");
    return btn;
  }

  /**
   * Creates a disabled button.
   *
   * @param text the button label
   * @return a disabled TwButton
   */
  public static TwButton disabled(String text) {
    TwButton btn = new TwButton(text);
    btn.setDisabled(true);
    return btn;
  }

  /**
   * Creates a circular icon-only button with an accessible label.
   *
   * @param icon the glyph or short text shown on the button
   * @param label the accessible text announced by screen readers
   * @return a styled icon button
   */
  public static TwButton icon(String icon, String label) {
    return icon(icon, label, "gray");
  }

  /**
   * Creates a circular icon-only button with an accessible label and color.
   *
   * <p>No size class is applied: {@code .btn-icon} and {@code .btn-circle} already fix the
   * dimensions, and a size modifier would override the padding they rely on.
   *
   * @param icon the glyph or short text shown on the button
   * @param label the accessible text announced by screen readers
   * @param color the Tailwind color name
   * @return a styled icon button
   */
  public static TwButton icon(String icon, String label, String color) {
    TwButton btn = new TwButton(icon);
    btn.setAccessibleText(label);
    btn.setColor(color);
    btn.size = null;
    btn.applyVariant();
    btn.getStyleClass().addAll(ICON_CLASS, CIRCLE_CLASS);
    return btn;
  }

  public enum TwButtonVariant {
    PRIMARY,
    SECONDARY,
    OUTLINE,
    GHOST,
    DANGER
  }
}
