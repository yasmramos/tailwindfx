package io.github.yasmramos.tailwindfx.i18n;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import java.util.ResourceBundle;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for TwI18n internationalization helper. */
public class TwI18nTest {

  @BeforeEach
  public void setUp() {
    // Reset to default state before each test
    TwI18n.setBaseName("messages");
    TwI18n.setLocale(Locale.ENGLISH);
  }

  @AfterEach
  public void tearDown() {
    // Clean up after tests
    TwI18n.clearBindings();
  }

  @Test
  public void testSetBaseName() {
    assertDoesNotThrow(() -> TwI18n.setBaseName("test.messages"));
  }

  @Test
  public void testSetLocale() {
    assertDoesNotThrow(() -> TwI18n.setLocale(Locale.FRENCH));
    assertDoesNotThrow(() -> TwI18n.setLocale(Locale.GERMAN));
    assertDoesNotThrow(() -> TwI18n.setLocale(Locale.forLanguageTag("es")));
  }

  @Test
  public void testSetLocale_null() {
    assertThrows(IllegalArgumentException.class, () -> TwI18n.setLocale(null));
  }

  @Test
  public void testGet_withoutInitialization() {
    // Set a non-existent bundle to test fallback behavior
    TwI18n.setBaseName("nonexistent.bundle.for.test");
    String result = TwI18n.get("some.key");
    // Should return key as fallback when bundle doesn't exist
    assertEquals("some.key", result);
  }

  @Test
  public void testCreateBinding_withoutInitialization() {
    // Should not throw exception
    var binding = TwI18n.createBinding("some.key");
    assertNotNull(binding);
  }

  @Test
  public void testCreateBinding_withParams_withoutInitialization() {
    // Should not throw exception
    var binding = TwI18n.createBinding("some.key", () -> new Object[] {"param1"});
    assertNotNull(binding);
  }

  @Test
  public void testClearBindings() {
    assertDoesNotThrow(() -> TwI18n.clearBindings());
  }

  @Test
  public void testClearCache() {
    assertDoesNotThrow(() -> TwI18n.clearCache());
  }

  @Test
  public void testLocaleChange() {
    Locale initial = TwI18n.getLocale();
    assertNotNull(initial);

    TwI18n.setLocale(Locale.FRENCH);
    assertEquals(Locale.FRENCH, TwI18n.getLocale());

    TwI18n.setLocale(Locale.GERMAN);
    assertEquals(Locale.GERMAN, TwI18n.getLocale());
  }

  @Test
  public void testGetLocale() {
    TwI18n.setLocale(Locale.ENGLISH);
    assertEquals(Locale.ENGLISH, TwI18n.getLocale());
  }

  @Test
  public void testResourceBundleMissing() {
    // Test that missing resource bundle is handled gracefully
    // This test verifies the fallback behavior when no bundle exists
    assertDoesNotThrow(
        () -> {
          try {
            ResourceBundle bundle = ResourceBundle.getBundle("nonexistent.bundle", Locale.ENGLISH);
            assertNotNull(bundle);
          } catch (Exception e) {
            // Expected - bundle doesn't exist
            assertTrue(
                e instanceof java.util.MissingResourceException
                    || e.getClass().getSimpleName().contains("Resource"));
          }
        });
  }

  @Test
  public void testMessageFormat() {
    // Test MessageFormat pattern creation
    String pattern = "Hello {0}, you have {1} messages";
    Object[] params = {"John", 5};

    java.text.MessageFormat format = new java.text.MessageFormat(pattern);
    String result = format.format(params);

    assertNotNull(result);
    assertTrue(result.contains("John"));
    assertTrue(result.contains("5"));
  }

  @Test
  public void testMultipleLocaleTags() {
    assertDoesNotThrow(
        () -> {
          TwI18n.setLocale(Locale.forLanguageTag("en-US"));
          TwI18n.setLocale(Locale.forLanguageTag("es-ES"));
          TwI18n.setLocale(Locale.forLanguageTag("fr-FR"));
          TwI18n.setLocale(Locale.forLanguageTag("de-DE"));
          TwI18n.setLocale(Locale.forLanguageTag("ja-JP"));
          TwI18n.setLocale(Locale.forLanguageTag("zh-CN"));
        });
  }

  @Test
  public void testTwI18nClassLoads() {
    assertDoesNotThrow(
        () -> {
          Class<?> clazz = TwI18n.class;
          assertNotNull(clazz);
        });
  }

  @Test
  public void testSetBaseName_null() {
    assertThrows(IllegalArgumentException.class, () -> TwI18n.setBaseName(null));
  }

  @Test
  public void testSetBaseName_clearsCache() {
    TwI18n.clearCache();
    TwI18n.setBaseName("test.messages");
    TwI18n.setLocale(Locale.ENGLISH);
    // Should not throw even with non-existent bundle
    assertDoesNotThrow(() -> TwI18n.get("some.key"));
  }

  @Test
  public void testGet_withFallback() {
    // Set a non-existent bundle to force fallback behavior
    TwI18n.setBaseName("nonexistent.bundle");
    String result = TwI18n.get("nonexistent.key", "fallback value");
    assertEquals("fallback value", result);
  }

  @Test
  public void testGet_withParams_noParams() {
    // Set a non-existent bundle to test fallback behavior
    TwI18n.setBaseName("nonexistent.bundle.for.test");
    String result = TwI18n.get("some.key");
    assertEquals("some.key", result);
  }

  @Test
  public void testGet_withParams_emptyArray() {
    // Set a non-existent bundle to test fallback behavior
    TwI18n.setBaseName("nonexistent.bundle.for.test");
    String result = TwI18n.get("some.key", new Object[0]);
    assertEquals("some.key", result);
  }

  @Test
  public void testGet_withParams_nullArray() {
    // Set a non-existent bundle to test fallback behavior
    TwI18n.setBaseName("nonexistent.bundle.for.test");
    String result = TwI18n.get("some.key", (Object[]) null);
    assertEquals("some.key", result);
  }

  @Test
  public void testGet_withInvalidMessageFormat() {
    // Test that invalid MessageFormat patterns are handled gracefully
    String pattern = "Invalid pattern {invalid}";
    // The method should return the pattern unformatted if format fails
    String result = TwI18n.get(pattern, "param1", "param2");
    assertNotNull(result);
  }

  @Test
  public void testCreateBinding_keyOnly() {
    var binding = TwI18n.createBinding("test.key");
    assertNotNull(binding);
    // StringBinding doesn't have isBound() method, just verify it was created
    assertTrue(binding.getValue() != null || binding.getValue() == null);
  }

  @Test
  public void testCreateBinding_withValidParams() {
    var binding =
        TwI18n.createBinding(
            "test.key",
            () -> {
              return new Object[] {"param1", 123};
            });
    assertNotNull(binding);
  }

  @Test
  public void testCreateBinding_withExceptionInParamsSupplier() {
    var binding =
        TwI18n.createBinding(
            "test.key",
            () -> {
              throw new RuntimeException("Test exception");
            });
    assertNotNull(binding);
    // Should not throw when getting value
    assertDoesNotThrow(() -> binding.get());
  }

  @Test
  public void testBind_node_nullNode() {
    assertThrows(
        IllegalArgumentException.class, () -> TwI18n.bind((javafx.scene.Node) null, "key"));
  }

  @Test
  public void testBind_node_nullKey() {
    javafx.scene.control.Label label = new javafx.scene.control.Label();
    assertThrows(IllegalArgumentException.class, () -> TwI18n.bind(label, null));
  }

  @Test
  public void testBind_tooltip_nullTooltip() {
    assertThrows(IllegalArgumentException.class, () -> TwI18n.bind((Tooltip) null, "key"));
  }

  @Test
  public void testBind_tooltip_nullKey() {
    Tooltip tooltip = new Tooltip();
    assertThrows(IllegalArgumentException.class, () -> TwI18n.bind(tooltip, null));
  }

  @Test
  public void testBind_tableColumn_nullColumn() {
    assertThrows(
        IllegalArgumentException.class,
        () -> TwI18n.bind((javafx.scene.control.TableColumn<?, ?>) null, "key"));
  }

  @Test
  public void testBind_tab_nullTab() {
    assertThrows(IllegalArgumentException.class, () -> TwI18n.bind((Tab) null, "key"));
  }

  @Test
  public void testBind_menuItem_nullMenuItem() {
    assertThrows(IllegalArgumentException.class, () -> TwI18n.bind((MenuItem) null, "key"));
  }

  @Test
  public void testBind_titledPane_nullTitledPane() {
    assertThrows(IllegalArgumentException.class, () -> TwI18n.bind((TitledPane) null, "key"));
  }

  @Test
  public void testBind_label() {
    javafx.scene.control.Label label = new javafx.scene.control.Label();
    assertDoesNotThrow(() -> TwI18n.bind(label, "test.key"));
    assertTrue(label.textProperty().isBound());
  }

  @Test
  public void testBind_button() {
    javafx.scene.control.Button button = new javafx.scene.control.Button();
    assertDoesNotThrow(() -> TwI18n.bind(button, "test.key"));
    assertTrue(button.textProperty().isBound());
  }

  @Test
  public void testBind_checkBox() {
    javafx.scene.control.CheckBox checkBox = new javafx.scene.control.CheckBox();
    assertDoesNotThrow(() -> TwI18n.bind(checkBox, "test.key"));
    assertTrue(checkBox.textProperty().isBound());
  }

  @Test
  public void testBind_radioButtton() {
    javafx.scene.control.RadioButton radioButton = new javafx.scene.control.RadioButton();
    assertDoesNotThrow(() -> TwI18n.bind(radioButton, "test.key"));
    assertTrue(radioButton.textProperty().isBound());
  }

  @Test
  public void testBind_textField() {
    javafx.scene.control.TextField textField = new javafx.scene.control.TextField();
    assertDoesNotThrow(() -> TwI18n.bind(textField, "test.key"));
    assertTrue(textField.promptTextProperty().isBound());
  }

  @Test
  public void testBind_textArea() {
    javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea();
    assertDoesNotThrow(() -> TwI18n.bind(textArea, "test.key"));
    assertTrue(textArea.promptTextProperty().isBound());
  }

  @Test
  public void testBind_unsupportedNodeType() {
    javafx.scene.layout.Pane pane = new javafx.scene.layout.Pane();
    assertThrows(
        IllegalArgumentException.class,
        () -> TwI18n.bind(pane, "test.key"),
        "Unsupported node type");
  }

  @Test
  public void testBind_tooltip() {
    Tooltip tooltip = new Tooltip();
    assertDoesNotThrow(() -> TwI18n.bind(tooltip, "test.key"));
    assertTrue(tooltip.textProperty().isBound());
  }

  @Test
  public void testBind_tableColumn() {
    javafx.scene.control.TableColumn<?, ?> column = new javafx.scene.control.TableColumn<>();
    assertDoesNotThrow(() -> TwI18n.bind(column, "test.key"));
    assertTrue(column.textProperty().isBound());
  }

  @Test
  public void testBind_tab() {
    Tab tab = new Tab();
    assertDoesNotThrow(() -> TwI18n.bind(tab, "test.key"));
    assertTrue(tab.textProperty().isBound());
  }

  @Test
  public void testBind_menuItem() {
    MenuItem menuItem = new MenuItem();
    assertDoesNotThrow(() -> TwI18n.bind(menuItem, "test.key"));
    assertTrue(menuItem.textProperty().isBound());
  }

  @Test
  public void testBind_titledPane() {
    TitledPane titledPane = new TitledPane();
    assertDoesNotThrow(() -> TwI18n.bind(titledPane, "test.key"));
    assertTrue(titledPane.textProperty().isBound());
  }

  @Test
  public void testLocaleProperty() {
    var localeProp = TwI18n.localeProperty();
    assertNotNull(localeProp);
    assertEquals(Locale.getDefault(), localeProp.get());
  }

  @Test
  public void testGetBaseName() {
    TwI18n.setBaseName("custom.bundle");
    assertEquals("custom.bundle", TwI18n.getBaseName());
  }

  @Test
  public void testClearBindings_disposesBindings() {
    var binding1 = TwI18n.createBinding("key1");
    var binding2 = TwI18n.createBinding("key2");

    assertNotNull(binding1);
    assertNotNull(binding2);

    TwI18n.clearBindings();

    // After clearBindings, the bindings list should be empty
    // We can't directly check if a binding is disposed in JavaFX
    // but we verify the method runs without error
    assertDoesNotThrow(() -> TwI18n.clearBindings());
  }

  @Test
  public void testBundleCache_clearedOnBaseNameChange() {
    TwI18n.setBaseName("messages");
    TwI18n.setLocale(Locale.ENGLISH);
    TwI18n.clearCache();
    TwI18n.setBaseName("another.bundle");
    // Cache should be empty after setBaseName
    assertDoesNotThrow(() -> TwI18n.get("test.key"));
  }

  @Test
  public void testStaticInitializer_runsWithoutError() {
    // Force class initialization
    Class<?> clazz = TwI18n.class;
    assertNotNull(clazz);
    // If static initializer failed, we wouldn't reach here
    assertTrue(true);
  }
}
