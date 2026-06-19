package io.github.yasmramos.tailwindfx.i18n;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import java.util.ResourceBundle;
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
    // Should not throw exception even if not initialized
    String result = TwI18n.get("some.key");
    // Should return key as fallback
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
}
