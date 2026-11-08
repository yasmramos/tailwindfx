package io.github.yasmramos.tailwindfx.theme;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import java.util.prefs.Preferences;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ThemeCustomizationPanel}.
 *
 * <p>The panel renders CSS and JSON by formatting doubles, so the tests pin a comma-decimal locale
 * to guard against the locale bug fixed in this class (the same class of defect previously found in
 * {@code ThemeManager} and {@code GradientProcessor}).
 */
@DisplayName("ThemeCustomizationPanel Unit Tests")
class ThemeCustomizationPanelTest {

  @BeforeEach
  void clearStoredPreferences() throws Exception {
    // Keep construction deterministic: loadSavedConfiguration() only reacts when values exist.
    Preferences.userRoot().node("tailwindfx/theme/custom").clear();
  }

  @Test
  @DisplayName("builds the color and property controls")
  void buildsControls() {
    ThemeCustomizationPanel panel = new ThemeCustomizationPanel();

    assertFalse(panel.getChildren().isEmpty());
    assertNotNull(findFirst(panel, ColorPicker.class), "expected at least one ColorPicker");
    assertNotNull(findFirst(panel, Slider.class), "expected at least one Slider");
    assertNotNull(findButton(panel, "Preview Button"), "expected the preview button");
  }

  @Test
  @DisplayName("getConfigAsJson emits locale-independent decimals")
  void jsonUsesRootLocale() {
    withLocale(
        Locale.GERMANY,
        () -> {
          ThemeCustomizationPanel panel = new ThemeCustomizationPanel();
          String json = panel.getConfigAsJson();

          assertTrue(json.contains("\"borderRadius\": 8.0"), json);
          assertTrue(json.contains("\"spacingScale\": 1.00"), json);
          assertTrue(json.contains("\"shadowIntensity\": 50.0"), json);
          assertFalse(
              json.matches("(?s).*\\d,\\d.*"), "comma decimal leaked into JSON: " + json);
        });
  }

  @Test
  @DisplayName("getConfigAsJson serialises the current colors as hex")
  void jsonContainsColorHex() {
    ThemeCustomizationPanel panel = new ThemeCustomizationPanel();
    String json = panel.getConfigAsJson();

    assertTrue(json.contains("\"primary\": \"#3b82f6\""), json);
    assertTrue(json.contains("\"secondary\": \"#10b981\""), json);
    assertTrue(json.contains("\"background\": \"#ffffff\""), json);
    assertTrue(json.contains("\"text\": \"#1e293b\""), json);
  }

  @Test
  @DisplayName("live preview CSS stays locale-independent under a comma-decimal locale")
  void livePreviewCssUsesRootLocale() {
    withLocale(
        Locale.GERMANY,
        () -> {
          ThemeCustomizationPanel panel = new ThemeCustomizationPanel();

          // Fires the valueProperty listener, which triggers applyLiveChanges().
          findFirst(panel, ColorPicker.class).setValue(Color.web("#ff0000"));

          Button preview = findButton(panel, "Preview Button");
          assertNotNull(preview);
          String style = preview.getStyle();
          assertFalse(style.matches("(?s).*\\d,\\d.*"), "comma decimal leaked into CSS: " + style);
          assertTrue(style.contains("14.0"), style);
        });
  }

  private static void withLocale(Locale locale, Runnable body) {
    Locale previous = Locale.getDefault();
    try {
      Locale.setDefault(locale);
      body.run();
    } finally {
      Locale.setDefault(previous);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> T findFirst(Parent parent, Class<T> type) {
    for (Node child : parent.getChildrenUnmodifiable()) {
      if (type.isInstance(child)) {
        return (T) child;
      }
      if (child instanceof Parent nested) {
        T found = findFirst(nested, type);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  private static Button findButton(Parent parent, String text) {
    for (Node child : parent.getChildrenUnmodifiable()) {
      if (child instanceof Button button && text.equals(button.getText())) {
        return button;
      }
      if (child instanceof Parent nested) {
        Button found = findButton(nested, text);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }
}
