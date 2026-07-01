package io.github.yasmramos.tailwindfx.components;

import javafx.scene.control.Button;
import javafx.beans.property.BooleanProperty;

/**
 * TwButton — Custom button component extending JavaFX Button with TailwindCSS variants.
 */
public class TwButton extends Button {

    private TwButtonVariant variant = TwButtonVariant.PRIMARY;
    private String color = "blue";
    private boolean loading = false;

    public TwButton(String text) {
        super(text);
        initialize();
    }

    private void initialize() {
        getStyleClass().add("tw-button");
        applyVariant();
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        hoverProperty().addListener((obs, oldVal, newVal) -> {
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
        getStyleClass().removeAll("btn-primary", "btn-secondary", "btn-outline", "btn-ghost", "btn-danger");
        
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
        return btn;
    }

    public static TwButton secondary(String text) {
        TwButton btn = new TwButton(text);
        btn.setVariant(TwButtonVariant.SECONDARY);
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
        return btn;
    }

    public static TwButton danger(String text) {
        TwButton btn = new TwButton(text);
        btn.setVariant(TwButtonVariant.DANGER);
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
