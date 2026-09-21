package io.github.yasmramos.tailwindfx.components;

import javafx.scene.control.Button;

/** TwButton — Custom button component extending JavaFX Button with TailwindCSS variants. */
public class TwButton extends Button {

  private TwButtonVariant variant = TwButtonVariant.PRIMARY;
  private String color = "blue";
  private String size = "md";
  private boolean loading = false;

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
            (obs, oldVal, newVal) -> {
              if (newVal && !isDisabled()) {
                setScaleX(1.05);
                setScaleY(1.05);
              } else if (!isDisabled()) {
                setScaleX(1.0);
                setScaleY(1.0);
              }
            });
  }

  private void applyVariant() {
    // Remove all variant, color and size classes but preserve special classes like btn-icon,
    // btn-circle
    getStyleClass()
        .removeAll("btn-primary", "btn-secondary", "btn-outline", "btn-ghost", "btn-danger");
    getStyleClass()
        .removeIf(
            cls ->
                cls.startsWith("btn-")
                    && !cls.equals("btn")
                    && !cls.equals("btn-icon")
                    && !cls.equals("btn-circle"));

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

  public void setLoading(boolean loading) {
    this.loading = loading;
    setDisabled(loading);
  }

  public boolean isLoading() {
    return loading;
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

  public static TwButton disabled(String text) {
    TwButton btn = new TwButton(text);
    btn.setDisable(true);
    btn.getStyleClass().add("btn-disabled");
    return btn;
  }

  public static TwButton icon(String icon, String label) {
    TwButton btn = new TwButton(icon);
    btn.setAccessibleText(label);
    btn.setColor("gray");
    btn.size = null; // Remove size class for icon buttons
    // Add icon-specific classes after setColor to avoid removal
    getStyleClassStatic(btn).addAll("btn-icon", "btn-circle");
    return btn;
  }

  public static TwButton icon(String icon, String label, String color) {
    TwButton btn = new TwButton(icon);
    btn.setAccessibleText(label);
    btn.setColor(color);
    btn.size = null; // Remove size class for icon buttons
    // Add icon-specific classes after setColor to avoid removal
    getStyleClassStatic(btn).addAll("btn-icon", "btn-circle");
    return btn;
  }

  private static javafx.collections.ObservableList<String> getStyleClassStatic(
      javafx.scene.control.Button btn) {
    return btn.getStyleClass();
  }

  public enum TwButtonVariant {
    PRIMARY,
    SECONDARY,
    OUTLINE,
    GHOST,
    DANGER
  }
}
