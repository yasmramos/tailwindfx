package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.TailwindFX;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleButton;

/**
 * TwCheckbox — Pre-styled checkbox and switch components with TailwindCSS variants.
 * 
 * Uses base CheckBox and ToggleButton controls styled via
 * tailwindfx-components.css utility classes.
 * 
 * <pre>
 * CheckBox chk = TwCheckbox.create("Accept terms");
 * CheckBox chk = TwCheckbox.checked("Remember me", true);
 * ToggleButton swt = TwSwitch.create("Enable notifications");
 * ToggleButton swt = TwSwitch.checked("Dark mode", true);
 * </pre>
 */
public final class TwCheckbox {

    private TwCheckbox() {}

    /**
     * Creates a standard checkbox.
     * @param text label text
     * @return styled CheckBox
     */
    public static CheckBox create(String text) {
        CheckBox chk = new CheckBox(text);
        TailwindFX.apply(chk, "checkbox");
        return chk;
    }

    /**
     * Creates a checkbox with initial state.
     * @param text label text
     * @param checked initial checked state
     * @return styled CheckBox
     */
    public static CheckBox checked(String text, boolean checked) {
        CheckBox chk = new CheckBox(text);
        chk.setSelected(checked);
        TailwindFX.apply(chk, "checkbox");
        return chk;
    }

    /**
     * Creates a disabled checkbox.
     * @param text label text
     * @return styled CheckBox (disabled)
     */
    public static CheckBox disabled(String text) {
        CheckBox chk = new CheckBox(text);
        chk.setDisable(true);
        TailwindFX.apply(chk, "checkbox", "checkbox-disabled");
        return chk;
    }

    /**
     * Creates a checkbox with error styling.
     * @param text label text
     * @return styled CheckBox (error state)
     */
    public static CheckBox error(String text) {
        CheckBox chk = new CheckBox(text);
        TailwindFX.apply(chk, "checkbox", "checkbox-error");
        return chk;
    }

    /**
     * Creates a switch/toggle button (alternative to checkbox).
     * @param text label text
     * @return styled ToggleButton (switch style)
     */
    public static ToggleButton createSwitch(String text) {
        ToggleButton toggle = new ToggleButton(text);
        TailwindFX.apply(toggle, "switch");
        return toggle;
    }

    /**
     * Creates a switch with initial state.
     * @param text label text
     * @param toggled initial toggled state
     * @return styled ToggleButton (switch style)
     */
    public static ToggleButton checkedSwitch(String text, boolean toggled) {
        ToggleButton toggle = new ToggleButton(text);
        toggle.setSelected(toggled);
        TailwindFX.apply(toggle, "switch");
        return toggle;
    }

    /**
     * Creates a disabled switch.
     * @param text label text
     * @return styled ToggleButton (switch style, disabled)
     */
    public static ToggleButton disabledSwitch(String text) {
        ToggleButton toggle = new ToggleButton(text);
        toggle.setDisable(true);
        TailwindFX.apply(toggle, "switch", "switch-disabled");
        return toggle;
    }

    /**
     * Creates a small checkbox (compact layout).
     * @param text label text
     * @return styled CheckBox (small)
     */
    public static CheckBox small(String text) {
        CheckBox chk = new CheckBox(text);
        TailwindFX.apply(chk, "checkbox", "checkbox-sm");
        return chk;
    }

    /**
     * Creates a large checkbox.
     * @param text label text
     * @return styled CheckBox (large)
     */
    public static CheckBox large(String text) {
        CheckBox chk = new CheckBox(text);
        TailwindFX.apply(chk, "checkbox", "checkbox-lg");
        return chk;
    }
}
