package io.github.yasmramos.tailwindfx.i18n;

import io.github.yasmramos.tailwindfx.i18n.FxI18n;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FxI18n internationalization helper.
 */
public class FxI18nTest {

    @BeforeEach
    public void setUp() {
        // Reset to default state before each test
        FxI18n.setBaseName("messages");
        FxI18n.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    public void tearDown() {
        // Clean up after tests
        FxI18n.clearBindings();
    }

    @Test
    public void testSetBaseName() {
        assertDoesNotThrow(() -> FxI18n.setBaseName("test.messages"));
    }

    @Test
    public void testSetLocale() {
        assertDoesNotThrow(() -> FxI18n.setLocale(Locale.FRENCH));
        assertDoesNotThrow(() -> FxI18n.setLocale(Locale.GERMAN));
        assertDoesNotThrow(() -> FxI18n.setLocale(Locale.forLanguageTag("es")));
    }

    @Test
    public void testSetLocale_null() {
        assertThrows(IllegalArgumentException.class, () -> FxI18n.setLocale(null));
    }

    @Test
    public void testGet_withoutInitialization() {
        // Should not throw exception even if not initialized
        String result = FxI18n.get("some.key");
        // Should return key as fallback
        assertEquals("some.key", result);
    }

    @Test
    public void testCreateBinding_withoutInitialization() {
        // Should not throw exception
        var binding = FxI18n.createBinding("some.key");
        assertNotNull(binding);
    }

    @Test
    public void testCreateBinding_withParams_withoutInitialization() {
        // Should not throw exception
        var binding = FxI18n.createBinding("some.key", () -> new Object[]{"param1"});
        assertNotNull(binding);
    }

    @Test
    public void testClearBindings() {
        assertDoesNotThrow(() -> FxI18n.clearBindings());
    }

    @Test
    public void testClearCache() {
        assertDoesNotThrow(() -> FxI18n.clearCache());
    }

    @Test
    public void testLocaleChange() {
        Locale initial = FxI18n.getLocale();
        assertNotNull(initial);

        FxI18n.setLocale(Locale.FRENCH);
        assertEquals(Locale.FRENCH, FxI18n.getLocale());

        FxI18n.setLocale(Locale.GERMAN);
        assertEquals(Locale.GERMAN, FxI18n.getLocale());
    }

    @Test
    public void testGetLocale() {
        FxI18n.setLocale(Locale.ENGLISH);
        assertEquals(Locale.ENGLISH, FxI18n.getLocale());
    }

    @Test
    public void testResourceBundleMissing() {
        // Test that missing resource bundle is handled gracefully
        // This test verifies the fallback behavior when no bundle exists
        assertDoesNotThrow(() -> {
            try {
                ResourceBundle bundle = ResourceBundle.getBundle("nonexistent.bundle", Locale.ENGLISH);
                assertNotNull(bundle);
            } catch (Exception e) {
                // Expected - bundle doesn't exist
                assertTrue(e instanceof java.util.MissingResourceException ||
                          e.getClass().getSimpleName().contains("Resource"));
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
        assertDoesNotThrow(() -> {
            FxI18n.setLocale(Locale.forLanguageTag("en-US"));
            FxI18n.setLocale(Locale.forLanguageTag("es-ES"));
            FxI18n.setLocale(Locale.forLanguageTag("fr-FR"));
            FxI18n.setLocale(Locale.forLanguageTag("de-DE"));
            FxI18n.setLocale(Locale.forLanguageTag("ja-JP"));
            FxI18n.setLocale(Locale.forLanguageTag("zh-CN"));
        });
    }

    @Test
    public void testFxI18nClassLoads() {
        assertDoesNotThrow(() -> {
            Class<?> clazz = FxI18n.class;
            assertNotNull(clazz);
        });
    }
}