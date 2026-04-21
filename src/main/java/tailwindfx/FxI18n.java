package tailwindfx;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

/**
 * FxI18n — Internationalization helper that dynamically updates UI text
 * when the locale changes without requiring application restart.
 *
 * <p>
 * Links JavaFX nodes with ResourceBundle keys and automatically rebinds
 * their text properties when the active locale changes. Supports:
 *
 * <ul>
 * <li>Automatic binding to Label, Button, TextField prompts, Tooltips, etc.</li>
 * <li>Parameterized messages with MessageFormat</li>
 * <li>Global locale switching with instant UI updates</li>
 * <li>Fallback to key name if translation is missing</li>
 * <li>Custom ResourceBundle base names</li>
 * </ul>
 *
 * <h3>Setup</h3>
 * <pre>
 * // 1. Create resource bundles in src/main/resources/i18n/
 * //    messages_en.properties:
 * //      app.title=My Application
 * //      button.save=Save
 * //      button.cancel=Cancel
 * //      
 * //    messages_es.properties:
 * //      app.title=Mi Aplicación
 * //      button.save=Guardar
 * //      button.cancel=Cancelar
 *
 * // 2. Initialize FxI18n with your bundle base name
 * FxI18n.setBaseName("i18n/messages");
 * FxI18n.setLocale(Locale.ENGLISH); // or Locale.forLanguageTag("es")
 * </pre>
 *
 * <h3>Usage</h3>
 * <pre>
 * // Bind a label to a translation key
 * Label titleLabel = new Label();
 * FxI18n.bind(titleLabel, "app.title");
 *
 * // Bind button text
 * Button saveBtn = new Button();
 * FxI18n.bind(saveBtn, "button.save");
 *
 * // Bind with parameters (uses MessageFormat)
 * Label statusLabel = new Label();
 * FxI18n.bind(statusLabel, "status.items", () -> new Object[]{itemCount});
 * // In properties: status.items=Found {0} items
 *
 * // Switch locale at runtime - all bound nodes update automatically
 * FxI18n.setLocale(Locale.forLanguageTag("es"));
 *
 * // Get translated string directly
 * String text = FxI18n.get("button.save");
 *
 * // Create a StringBinding for custom use
 * StringBinding binding = FxI18n.createBinding("welcome.message");
 * someProperty.bind(binding);
 * </pre>
 *
 * <h3>Supported Components</h3>
 * <ul>
 * <li>Label, Button, CheckBox, RadioButton (text)</li>
 * <li>TextField, TextArea (promptText)</li>
 * <li>Tooltip (text)</li>
 * <li>TableColumn (text)</li>
 * <li>Tab (text)</li>
 * <li>MenuItem (text)</li>
 * <li>TitledPane (text)</li>
 * </ul>
 */
public final class FxI18n {

    // =========================================================================
    // Global state
    // =========================================================================
    private static String baseName = "messages";
    private static final ObjectProperty<Locale> currentLocale = 
        new SimpleObjectProperty<>(Locale.getDefault());
    private static final ObjectProperty<ResourceBundle> currentBundle = 
        new SimpleObjectProperty<>();
    
    // Track all active bindings for cleanup
    private static final List<StringBinding> activeBindings = new ArrayList<>();
    
    // Cache for ResourceBundles per locale
    private static final Map<Locale, ResourceBundle> bundleCache = new HashMap<>();

    static {
        // Update bundle when locale changes
        currentLocale.addListener((obs, oldLocale, newLocale) -> {
            ResourceBundle bundle = getOrLoadBundle(newLocale);
            currentBundle.set(bundle);
        });
        
        // Initialize with default locale
        currentBundle.set(getOrLoadBundle(currentLocale.get()));
    }

    private FxI18n() {} // Utility class

    // =========================================================================
    // Configuration
    // =========================================================================

    /**
     * Sets the base name for ResourceBundle lookup.
     * E.g., "i18n/messages" will look for i18n/messages_en.properties, etc.
     *
     * @param baseName the resource bundle base name
     */
    public static void setBaseName(String baseName) {
        Preconditions.requireNonNull(baseName, "FxI18n.setBaseName", "baseName");
        FxI18n.baseName = baseName;
        bundleCache.clear();
        currentBundle.set(getOrLoadBundle(currentLocale.get()));
    }

    /**
     * Returns the current base name.
     */
    public static String getBaseName() {
        return baseName;
    }

    /**
     * Sets the active locale. All bound UI elements will update automatically.
     *
     * @param locale the new locale
     */
    public static void setLocale(Locale locale) {
        Preconditions.requireNonNull(locale, "FxI18n.setLocale", "locale");
        currentLocale.set(locale);
    }

    /**
     * Returns the current active locale.
     */
    public static Locale getLocale() {
        return currentLocale.get();
    }

    /**
     * Returns the locale property (for binding).
     */
    public static ObjectProperty<Locale> localeProperty() {
        return currentLocale;
    }

    // =========================================================================
    // Direct translation
    // =========================================================================

    /**
     * Gets a translated string for the given key in the current locale.
     * Returns the key itself if no translation is found.
     *
     * @param key the translation key
     * @return the translated string
     */
    public static String get(String key) {
        return get(key, key);
    }

    /**
     * Gets a translated string with a fallback if the key is not found.
     *
     * @param key the translation key
     * @param fallback the fallback string
     * @return the translated string or fallback
     */
    public static String get(String key, String fallback) {
        ResourceBundle bundle = currentBundle.get();
        if (bundle == null) return fallback;
        
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return fallback;
        }
    }

    /**
     * Gets a translated string with MessageFormat parameters.
     * E.g., get("welcome.user", "John") where properties has "welcome.user=Hello {0}!"
     *
     * @param key the translation key
     * @param params the parameters for MessageFormat
     * @return the formatted translated string
     */
    public static String get(String key, Object... params) {
        String pattern = get(key);
        if (params == null || params.length == 0) return pattern;
        
        try {
            return MessageFormat.format(pattern, params);
        } catch (IllegalArgumentException e) {
            return pattern; // Return unformatted if format fails
        }
    }

    // =========================================================================
    // Binding API
    // =========================================================================

    /**
     * Creates a StringBinding that updates when the locale changes.
     *
     * @param key the translation key
     * @return a StringBinding bound to the current locale
     */
    public static StringBinding createBinding(String key) {
        return createBinding(key, null);
    }

    /**
     * Creates a StringBinding with MessageFormat parameters.
     * The parameters supplier is called each time the binding updates.
     *
     * @param key the translation key
     * @param paramsSupplier supplier for MessageFormat parameters (can be null)
     * @return a StringBinding bound to the current locale
     */
    public static StringBinding createBinding(String key, Callable<Object[]> paramsSupplier) {
        StringBinding binding = Bindings.createStringBinding(() -> {
            if (paramsSupplier != null) {
                try {
                    Object[] params = paramsSupplier.call();
                    return get(key, params);
                } catch (Exception e) {
                    return get(key);
                }
            }
            return get(key);
        }, currentBundle);
        
        activeBindings.add(binding);
        return binding;
    }

    /**
     * Binds a node's text property to a translation key.
     * Automatically detects the node type and binds the appropriate property.
     *
     * @param node the node to bind
     * @param key the translation key
     */
    public static void bind(Node node, String key) {
        bind(node, key, null);
    }

    /**
     * Binds a node's text property to a translation key with parameters.
     *
     * @param node the node to bind
     * @param key the translation key
     * @param paramsSupplier supplier for MessageFormat parameters
     */
    public static void bind(Node node, String key, Callable<Object[]> paramsSupplier) {
        Preconditions.requireNonNull(node, "FxI18n.bind", "node");
        Preconditions.requireNonNull(key, "FxI18n.bind", "key");

        StringBinding binding = createBinding(key, paramsSupplier);

        // Bind based on node type
        if (node instanceof Labeled labeled) {
            labeled.textProperty().bind(binding);
        } else if (node instanceof TextField textField) {
            textField.promptTextProperty().bind(binding);
        } else if (node instanceof javafx.scene.control.TextArea textArea) {
            textArea.promptTextProperty().bind(binding);
        } else {
            throw new IllegalArgumentException(
                "FxI18n.bind: Unsupported node type: " + node.getClass().getName()
            );
        }
    }

    /**
     * Binds a Tooltip's text to a translation key.
     */
    public static void bind(Tooltip tooltip, String key) {
        bind(tooltip, key, null);
    }

    /**
     * Binds a Tooltip's text to a translation key with parameters.
     */
    public static void bind(Tooltip tooltip, String key, Callable<Object[]> paramsSupplier) {
        Preconditions.requireNonNull(tooltip, "FxI18n.bind", "tooltip");
        Preconditions.requireNonNull(key, "FxI18n.bind", "key");
        
        StringBinding binding = createBinding(key, paramsSupplier);
        tooltip.textProperty().bind(binding);
    }

    /**
     * Binds a TableColumn's text to a translation key.
     */
    public static void bind(TableColumn<?, ?> column, String key) {
        Preconditions.requireNonNull(column, "FxI18n.bind", "column");
        Preconditions.requireNonNull(key, "FxI18n.bind", "key");
        
        StringBinding binding = createBinding(key);
        column.textProperty().bind(binding);
    }

    /**
     * Binds a Tab's text to a translation key.
     */
    public static void bind(Tab tab, String key) {
        Preconditions.requireNonNull(tab, "FxI18n.bind", "tab");
        Preconditions.requireNonNull(key, "FxI18n.bind", "key");
        
        StringBinding binding = createBinding(key);
        tab.textProperty().bind(binding);
    }

    /**
     * Binds a MenuItem's text to a translation key.
     */
    public static void bind(MenuItem menuItem, String key) {
        Preconditions.requireNonNull(menuItem, "FxI18n.bind", "menuItem");
        Preconditions.requireNonNull(key, "FxI18n.bind", "key");
        
        StringBinding binding = createBinding(key);
        menuItem.textProperty().bind(binding);
    }

    /**
     * Binds a TitledPane's text to a translation key.
     */
    public static void bind(TitledPane pane, String key) {
        Preconditions.requireNonNull(pane, "FxI18n.bind", "pane");
        Preconditions.requireNonNull(key, "FxI18n.bind", "key");
        
        StringBinding binding = createBinding(key);
        pane.textProperty().bind(binding);
    }

    // =========================================================================
    // Cleanup
    // =========================================================================

    /**
     * Unbinds all active string bindings.
     * Useful when completely resetting the i18n system.
     */
    public static void clearBindings() {
        activeBindings.forEach(StringBinding::dispose);
        activeBindings.clear();
    }

    /**
     * Clears the ResourceBundle cache.
     * Forces reloading of bundles on next locale switch.
     */
    public static void clearCache() {
        bundleCache.clear();
    }

    // =========================================================================
    // Internal helpers
    // =========================================================================

    private static ResourceBundle getOrLoadBundle(Locale locale) {
        return bundleCache.computeIfAbsent(locale, loc -> {
            try {
                return ResourceBundle.getBundle(baseName, loc);
            } catch (MissingResourceException e) {
                System.err.println("FxI18n: Could not load bundle '" + baseName 
                    + "' for locale '" + loc + "': " + e.getMessage());
                // Return empty bundle as fallback
                return new ResourceBundle() {
                    @Override
                    protected Object handleGetObject(String key) {
                        return key; // Return key as value
                    }
                    @Override
                    public java.util.Enumeration<String> getKeys() {
                        return java.util.Collections.emptyEnumeration();
                    }
                };
            }
        });
    }
}
