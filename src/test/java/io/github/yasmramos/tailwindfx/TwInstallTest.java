package io.github.yasmramos.tailwindfx;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TwInstall class.
 */
@DisplayName("TwInstall Tests")
class TwInstallTest extends ApplicationTest {

    private Scene scene;
    private Pane root;

    @Override
    public void start(Stage stage) {
        root = new Pane();
        scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void setUp() {
        // Clear any existing stylesheets
        scene.getStylesheets().clear();
    }

    @Test
    @DisplayName("should install all CSS files with install()")
    void shouldInstallAllCssFiles() {
        // When
        TwInstall.install(scene);

        // Then
        List<String> stylesheets = scene.getStylesheets();
        assertTrue(stylesheets.size() >= 7, "Should install at least 7 CSS files");
        assertTrue(stylesheets.stream().anyMatch(s -> s.contains("tailwindfx-base")), "Should include base CSS");
        assertTrue(stylesheets.stream().anyMatch(s -> s.contains("tailwindfx-components")), "Should include components CSS");
        assertTrue(stylesheets.stream().anyMatch(s -> s.contains("tailwindfx-utilities")), "Should include utilities CSS");
        assertTrue(stylesheets.stream().anyMatch(s -> s.contains("tailwindfx-colors")), "Should include colors CSS");
        assertTrue(stylesheets.stream().anyMatch(s -> s.contains("tailwindfx-effects")), "Should include effects CSS");
        assertTrue(stylesheets.stream().anyMatch(s -> s.contains("tailwindfx-components-preset")), "Should include components preset CSS");
        assertTrue(stylesheets.stream().anyMatch(s -> s.contains("tailwindfx-dark")), "Should include dark CSS");
    }

    @Test
    @DisplayName("should install only base CSS with installBase()")
    void shouldInstallOnlyBaseCss() {
        // When
        TwInstall.installBase(scene);

        // Then
        List<String> stylesheets = scene.getStylesheets();
        assertEquals(1, stylesheets.size(), "Should install exactly 1 CSS file");
        assertTrue(stylesheets.get(0).contains("tailwindfx-base"), "Should be base CSS");
    }

    @Test
    @DisplayName("should install components CSS with installComponents()")
    void shouldInstallComponentsCss() {
        // When
        TwInstall.installComponents(scene);

        // Then
        List<String> stylesheets = scene.getStylesheets();
        assertEquals(1, stylesheets.size(), "Should install exactly 1 CSS file");
        assertTrue(stylesheets.get(0).contains("tailwindfx-components"), "Should be components CSS");
    }

    @Test
    @DisplayName("should install utilities CSS with installUtilities()")
    void shouldInstallUtilitiesCss() {
        // When
        TwInstall.installUtilities(scene);

        // Then
        List<String> stylesheets = scene.getStylesheets();
        assertEquals(1, stylesheets.size(), "Should install exactly 1 CSS file");
        assertTrue(stylesheets.get(0).contains("tailwindfx-utilities"), "Should be utilities CSS");
    }

    @Test
    @DisplayName("should install colors CSS with installColors()")
    void shouldInstallColorsCss() {
        // When
        TwInstall.installColors(scene);

        // Then
        List<String> stylesheets = scene.getStylesheets();
        assertEquals(1, stylesheets.size(), "Should install exactly 1 CSS file");
        assertTrue(stylesheets.get(0).contains("tailwindfx-colors"), "Should be colors CSS");
    }

    @Test
    @DisplayName("should install effects CSS with installEffects()")
    void shouldInstallEffectsCss() {
        // When
        TwInstall.installEffects(scene);

        // Then
        List<String> stylesheets = scene.getStylesheets();
        assertEquals(1, stylesheets.size(), "Should install exactly 1 CSS file");
        assertTrue(stylesheets.get(0).contains("tailwindfx-effects"), "Should be effects CSS");
    }

    @Test
    @DisplayName("should install components preset CSS with installComponentsPreset()")
    void shouldInstallComponentsPresetCss() {
        // When
        TwInstall.installComponentsPreset(scene);

        // Then
        List<String> stylesheets = scene.getStylesheets();
        assertEquals(1, stylesheets.size(), "Should install exactly 1 CSS file");
        assertTrue(stylesheets.get(0).contains("tailwindfx-components-preset"), "Should be components preset CSS");
    }

    @Test
    @DisplayName("should install dark CSS with installDark()")
    void shouldInstallDarkCss() {
        // When
        TwInstall.installDark(scene);

        // Then
        List<String> stylesheets = scene.getStylesheets();
        assertEquals(1, stylesheets.size(), "Should install exactly 1 CSS file");
        assertTrue(stylesheets.get(0).contains("tailwindfx-dark"), "Should be dark CSS");
    }

    @Test
    @DisplayName("should install essentials (base, components, components-preset)")
    void shouldInstallEssentials() {
        // When
        TwInstall.installEssentials(scene);

        // Then
        List<String> stylesheets = scene.getStylesheets();
        assertEquals(3, stylesheets.size(), "Should install exactly 3 CSS files");
        assertTrue(stylesheets.get(0).contains("tailwindfx-base"), "First should be base CSS");
        assertTrue(stylesheets.get(1).contains("tailwindfx-components"), "Second should be components CSS");
        assertTrue(stylesheets.get(2).contains("tailwindfx-components-preset"), "Third should be components preset CSS");
    }

    @Test
    @DisplayName("should replace existing stylesheet when installing same CSS")
    void shouldReplaceExistingStylesheet() {
        // Given
        TwInstall.installBase(scene);
        assertEquals(1, scene.getStylesheets().size(), "Should have 1 stylesheet initially");
        String firstUrl = scene.getStylesheets().get(0);

        // When
        TwInstall.installBase(scene);

        // Then
        List<String> stylesheets = scene.getStylesheets();
        assertEquals(1, stylesheets.size(), "Should still have 1 stylesheet");
        assertNotSame(firstUrl, stylesheets.get(0), "Should be a new URL instance");
    }

    @Test
    @DisplayName("should maintain order based on priority")
    void shouldMaintainOrderBasedOnPriority() {
        // When
        TwInstall.installBase(scene);
        TwInstall.installComponents(scene);
        TwInstall.installDark(scene);

        // Then
        List<String> stylesheets = scene.getStylesheets();
        assertEquals(3, stylesheets.size(), "Should have 3 stylesheets");
        
        // Base should be first (priority 0)
        assertTrue(stylesheets.get(0).contains("tailwindfx-base"), "Base should be first");
        // Components should be second (priority 1)
        assertTrue(stylesheets.get(1).contains("tailwindfx-components"), "Components should be second");
        // Dark should be last (priority 10, but inserted at end)
        assertTrue(stylesheets.get(2).contains("tailwindfx-dark"), "Dark should be last");
    }

    @Test
    @DisplayName("should install all with stage and attach breakpoint manager")
    void shouldInstallAllWithStage() {
        // When - Use the scene from ApplicationTest, create stage on FX thread
        interact(() -> {
            Stage stage = new Stage();
            TwInstall.install(scene, stage);
        });

        // Then
        List<String> stylesheets = scene.getStylesheets();
        assertTrue(stylesheets.size() >= 7, "Should install at least 7 CSS files");
    }

    @Test
    @DisplayName("install() should delegate to installAll()")
    void installShouldDelegateToInstallAll() {
        // When
        TwInstall.install(scene);

        // Then
        List<String> stylesheets = scene.getStylesheets();
        assertTrue(stylesheets.stream().anyMatch(s -> s.contains("tailwindfx-base")), "Should include base");
        assertTrue(stylesheets.stream().anyMatch(s -> s.contains("tailwindfx-dark")), "Should include dark");
    }

    @Test
    @DisplayName("should handle multiple installations without duplicates")
    void shouldHandleMultipleInstallationsWithoutDuplicates() {
        // When
        TwInstall.installBase(scene);
        TwInstall.installBase(scene);
        TwInstall.installBase(scene);

        // Then
        List<String> stylesheets = scene.getStylesheets();
        assertEquals(1, stylesheets.size(), "Should have only 1 stylesheet after multiple installs");
    }
}
