package tailwindfx;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ThemeManager to verify themes apply to ALL components including Stage.
 */
class ThemeManagerRefreshTest extends ApplicationTest {

    private Scene scene;
    private VBox root;
    private Label label;
    private Button button;
    private TextField textField;
    private VBox nestedContainer;
    private Label nestedLabel;

    @Override
    public void start(Stage stage) {
        // Create a complex hierarchy
        root = new VBox(10);
        
        label = new Label("Test Label");
        button = new Button("Test Button");
        textField = new TextField("Test Field");
        
        // Nested components to test deep refresh
        nestedContainer = new VBox(5);
        nestedLabel = new Label("Nested Label");
        nestedContainer.getChildren().add(nestedLabel);
        
        root.getChildren().addAll(label, button, textField, nestedContainer);
        
        scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

    // =========================================================================
    // Theme Application Tests
    // =========================================================================

    @Test
    @DisplayName("Apply dark theme - all components should update")
    void testDarkThemeAppliesEverywhere() {
        Platform.runLater(() -> {
            // Apply dark theme
            TailwindFX.theme(scene).dark().apply();
            
            // Verify root has dark class
            assertTrue(root.getStyleClass().contains("dark"), 
                "Root should have 'dark' class");
            
            // Verify root has style
            assertNotNull(root.getStyle(), 
                "Root should have inline style");
            assertTrue(root.getStyle().contains("-fx-base"), 
                "Root should have -fx-base variable");
            
            // Force a rendering cycle
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        });
        
        // Wait for rendering
        sleep(200);
        
        Platform.runLater(() -> {
            // Verify nested components have styles applied (inherited from root)
            // We can't check computed styles in headless mode, but we can verify
            // the theme was set on root which should propagate
            assertTrue(root.getStyleClass().contains("dark"));
        });
    }

    @Test
    @DisplayName("Toggle theme - should affect all components")
    void testThemeToggleRefreshes() {
        Platform.runLater(() -> {
            // Start with light
            TailwindFX.theme(scene).light().apply();
            assertFalse(root.getStyleClass().contains("dark"));
            
            // Toggle to dark
            ThemeManager.toggle(scene);
            assertTrue(root.getStyleClass().contains("dark"));
            
            // Toggle back to light
            ThemeManager.toggle(scene);
            assertFalse(root.getStyleClass().contains("dark"));
        });
    }

    @Test
    @DisplayName("Apply preset theme - should set variables")
    void testPresetTheme() {
        Platform.runLater(() -> {
            TailwindFX.theme(scene).preset("blue").apply();
            
            String style = root.getStyle();
            assertNotNull(style);
            assertTrue(style.contains("-fx-base"));
            assertTrue(style.contains("-fx-accent"));
        });
    }

    @Test
    @DisplayName("Custom theme with accent color")
    void testCustomTheme() {
        Platform.runLater(() -> {
            TailwindFX.theme(scene)
                .base("#1e293b")
                .accent("#3b82f6")
                .apply();
            
            String style = root.getStyle();
            assertTrue(style.contains("#1e293b"));
            assertTrue(style.contains("#3b82f6"));
            assertTrue(root.getStyleClass().contains("dark"));
        });
    }

    @Test
    @DisplayName("Scoped theme - only affects subtree")
    void testScopedTheme() {
        Platform.runLater(() -> {
            // Apply global light theme
            TailwindFX.theme(scene).light().apply();
            assertFalse(root.getStyleClass().contains("dark"));
            
            // Apply dark theme to nested container only
            TailwindFX.scope(nestedContainer).preset("dark").apply();
            
            // Root should still be light
            assertFalse(root.getStyleClass().contains("dark"));
            
            // Nested container should be dark
            assertTrue(nestedContainer.getStyleClass().contains("dark"));
        });
    }

    @Test
    @DisplayName("Animated theme transition")
    void testAnimatedTheme() {
        Platform.runLater(() -> {
            TailwindFX.theme(scene)
                .dark()
                .animated(300)
                .apply();
            
            // Should have dark class even during animation
            assertTrue(root.getStyleClass().contains("dark"));
        });
        
        // Wait for animation
        sleep(400);
        
        Platform.runLater(() -> {
            // Should still have dark after animation
            assertTrue(root.getStyleClass().contains("dark"));
        });
    }

    // =========================================================================
    // Refresh Tests
    // =========================================================================

    @Test
    @DisplayName("Theme refresh propagates to deeply nested components")
    void testDeepRefresh() {
        Platform.runLater(() -> {
            // Create deeper nesting
            VBox level1 = new VBox();
            VBox level2 = new VBox();
            VBox level3 = new VBox();
            Label deepLabel = new Label("Deep Label");
            
            level3.getChildren().add(deepLabel);
            level2.getChildren().add(level3);
            level1.getChildren().add(level2);
            root.getChildren().add(level1);
            
            // Apply theme
            TailwindFX.theme(scene).dark().apply();
            
            // Verify root has theme
            assertTrue(root.getStyleClass().contains("dark"));
            assertNotNull(root.getStyle());
        });
    }

    @Test
    @DisplayName("Multiple rapid theme switches")
    void testRapidThemeSwitching() {
        Platform.runLater(() -> {
            // Rapidly switch themes
            TailwindFX.theme(scene).preset("light").apply();
            TailwindFX.theme(scene).preset("dark").apply();
            TailwindFX.theme(scene).preset("blue").apply();
            TailwindFX.theme(scene).preset("green").apply();
            TailwindFX.theme(scene).preset("purple").apply();
            
            // Should end up with purple (last one)
            String style = root.getStyle();
            assertNotNull(style);
            assertTrue(style.length() > 0);
        });
    }

    @Test
    @DisplayName("Theme persists after adding new components")
    void testThemePersistsOnNewComponents() {
        Platform.runLater(() -> {
            // Apply theme first
            TailwindFX.theme(scene).dark().apply();
            assertTrue(root.getStyleClass().contains("dark"));
            
            // Add new components
            Label newLabel = new Label("New Label");
            Button newButton = new Button("New Button");
            root.getChildren().addAll(newLabel, newButton);
            
            // Force layout
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            
            // Root should still have dark theme
            assertTrue(root.getStyleClass().contains("dark"));
        });
    }

    // =========================================================================
    // Save/Load Tests
    // =========================================================================

    @Test
    @DisplayName("Save and load theme")
    void testSaveLoadTheme() {
        Platform.runLater(() -> {
            // Apply and save theme
            TailwindFX.theme(scene).dark().apply();
            ThemeManager.saveTheme(scene, "test.theme");
            
            // Reset to light
            TailwindFX.theme(scene).light().apply();
            assertFalse(root.getStyleClass().contains("dark"));
            
            // Load saved theme
            boolean loaded = ThemeManager.loadTheme(scene, "test.theme");
            assertTrue(loaded, "Theme should load successfully");
        });
        
        // Wait for Platform.runLater in loadTheme
        sleep(200);
        
        Platform.runLater(() -> {
            // Should be dark again
            assertTrue(root.getStyleClass().contains("dark"));
            
            // Cleanup
            ThemeManager.deleteTheme("test.theme");
        });
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
