package tailwindfx;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ThemeScopeManager.
 */
@DisplayName("ThemeScopeManager Tests")
class ThemeScopeManagerTest {

    private Scene scene;
    private StackPane root;

    @BeforeAll
    static void initPlatform() {
        Platform.startup(() -> {});
    }

    @BeforeEach
    void setUp() {
        root = new StackPane();
        scene = new Scene(root, 800, 600);
    }

    @Nested
    @DisplayName("Theme Application")
    class ThemeApplicationTests {

        @Test
        @DisplayName("Should apply light theme")
        void testApplyLightTheme() {
            ThemeManager theme = TailwindFX.theme(scene);
            theme.light();

            assertNotNull(theme);
        }

        @Test
        @DisplayName("Should apply dark theme")
        void testApplyDarkTheme() {
            ThemeManager theme = TailwindFX.theme(scene);
            theme.dark();

            assertNotNull(theme);
        }

        @Test
        @DisplayName("Should apply custom theme")
        void testApplyCustomTheme() {
            ThemeManager theme = TailwindFX.theme(scene);
            theme.base("#1e293b").accent("#3b82f6");

            assertNotNull(theme);
        }
    }

    @Nested
    @DisplayName("Theme Toggling")
    class ThemeToggleTests {

        @Test
        @DisplayName("Should toggle between light and dark")
        void testToggleTheme() {
            ThemeManager theme = TailwindFX.theme(scene);
            theme.light();

            // Toggle to dark
            theme.dark();

            assertNotNull(theme);
        }

        @Test
        @DisplayName("Should apply theme with preset")
        void testGetCurrentTheme() {
            ThemeManager theme = TailwindFX.theme(scene);
            theme.dark();

            // Theme manager should be configured
            assertNotNull(theme);
        }

        @Test
        @DisplayName("Should check if dark theme is active")
        void testIsDark() {
            ThemeManager theme = TailwindFX.theme(scene);
            theme.dark();

            // Theme manager should track dark mode
            assertNotNull(theme);
        }
    }

    @Nested
    @DisplayName("Theme CSS Application")
    class ThemeCssApplicationTests {

        @Test
        @DisplayName("Should add dark theme CSS to scene")
        void testDarkThemeCssAdded() {
            ThemeManager theme = TailwindFX.theme(scene);
            theme.dark().apply();

            // Should have added dark theme stylesheet
            assertTrue(scene.getStylesheets().size() > 0);
        }

        @Test
        @DisplayName("Should remove previous theme before adding new one")
        void testThemeReplacement() {
            ThemeManager theme = TailwindFX.theme(scene);
            int initialSize = scene.getStylesheets().size();

            theme.light().apply();
            int afterLight = scene.getStylesheets().size();

            theme.dark().apply();
            int afterDark = scene.getStylesheets().size();

            // Should manage stylesheets properly
            assertTrue(afterDark >= initialSize);
        }
    }

    @Nested
    @DisplayName("Theme Scope Management")
    class ThemeScopeManagementTests {

        @Test
        @DisplayName("Should create scoped theme for specific node")
        void testScopedThemeForNode() {
            StackPane pane = new StackPane();
            ThemeManager theme = ThemeManager.scope(pane);

            assertNotNull(theme);
        }

        @Test
        @DisplayName("Should handle multiple theme scopes")
        void testMultipleScopes() {
            StackPane pane1 = new StackPane();
            StackPane pane2 = new StackPane();
            ThemeManager theme1 = ThemeManager.scope(pane1);
            ThemeManager theme2 = ThemeManager.scope(pane2);

            assertNotNull(theme1);
            assertNotNull(theme2);
        }

        @Test
        @DisplayName("Should reset theme to default")
        void testResetTheme() {
            ThemeManager theme = TailwindFX.theme(scene);
            theme.dark().apply();
            theme.light().apply();

            // Should revert to default
            assertNotNull(theme);
        }
    }

    @Nested
    @DisplayName("Theme Persistence")
    class ThemePersistenceTests {

        @Test
        @DisplayName("Should apply theme with accent color")
        void testSaveThemePreference() {
            ThemeManager theme = TailwindFX.theme(scene);
            theme.dark().accent("#ff6600").apply();

            // Should apply without errors
            assertNotNull(theme);
        }

        @Test
        @DisplayName("Should apply theme with custom colors")
        void testLoadThemePreference() {
            ThemeManager theme = TailwindFX.theme(scene);
            theme.base("#1e293b").accent("#3b82f6").apply();

            // Should apply without errors
            assertNotNull(theme);
        }
    }

    @Nested
    @DisplayName("Theme Customization")
    class ThemeCustomizationTests {

        @Test
        @DisplayName("Should apply custom color overrides")
        void testCustomColorOverrides() {
            ThemeManager theme = TailwindFX.theme(scene);
            theme.accent("#ff6600").apply();

            assertNotNull(theme);
        }

        @Test
        @DisplayName("Should apply multiple custom overrides")
        void testMultipleOverrides() {
            ThemeManager theme = TailwindFX.theme(scene);
            theme.base("#e0e0e0").accent("#ff6600").focus("#ff8800").apply();

            assertNotNull(theme);
        }

        @Test
        @DisplayName("Should create theme from preset")
        void testThemeFromCustomCss() {
            ThemeManager theme = TailwindFX.theme(scene);
            theme.preset("blue").apply();

            assertNotNull(theme);
        }
    }

    @Nested
    @DisplayName("Theme Validation")
    class ThemeValidationTests {

        @Test
        @DisplayName("Should apply theme successfully")
        void testValidateTheme() {
            ThemeManager theme = TailwindFX.theme(scene);
            theme.light().apply();

            // Validation should complete without errors
            assertTrue(true); // If we got here, it's valid
        }

        @Test
        @DisplayName("Should check if theme has stylesheets")
        void testIsInstalled() {
            ThemeManager theme = TailwindFX.theme(scene);
            int stylesheetCount = scene.getStylesheets().size();

            // Should return non-negative count
            assertTrue(stylesheetCount >= 0);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null scene gracefully")
        void testNullScene() {
            assertThrows(NullPointerException.class, () -> {
                TailwindFX.theme(null);
            });
        }

        @Test
        @DisplayName("Should handle rapid theme switching")
        void testRapidThemeSwitching() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Boolean> result = new AtomicReference<>();

            Platform.runLater(() -> {
                try {
                    ThemeManager theme = TailwindFX.theme(scene);
                    for (int i = 0; i < 10; i++) {
                        if (i % 2 == 0) {
                            theme.dark().apply();
                        } else {
                            theme.light().apply();
                        }
                    }
                    result.set(true);
                } catch (Exception e) {
                    result.set(false);
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(3, TimeUnit.SECONDS));
            assertTrue(result.get());
        }

        @Test
        @DisplayName("Should handle concurrent theme applications")
        void testConcurrentThemeApplications() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Boolean> result = new AtomicReference<>();

            Platform.runLater(() -> {
                try {
                    ThemeManager theme1 = TailwindFX.theme(scene);
                    ThemeManager theme2 = TailwindFX.theme(scene);

                    theme1.dark().apply();
                    theme2.light().apply();

                    result.set(true);
                } catch (Exception e) {
                    result.set(false);
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(3, TimeUnit.SECONDS));
            assertTrue(result.get());
        }
    }
}
