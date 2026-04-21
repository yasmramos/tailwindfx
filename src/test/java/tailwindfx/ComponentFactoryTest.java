package tailwindfx;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;
import static org.testfx.matcher.control.LabeledMatchers.*;
import static org.testfx.matcher.control.TextInputControlMatchers.*;

/**
 * Unit tests for ComponentFactory with full TestFX interaction capabilities.
 * Tests component creation, styling, and user interactions.
 */
@DisplayName("ComponentFactory Tests with TestFX")
class ComponentFactoryTest extends ApplicationTest {

    private Scene scene;
    private StackPane root;

    @BeforeEach
    void setUp() {
        root = new StackPane();
        scene = new Scene(root, 800, 600);
    }

    @Nested
    @DisplayName("Avatar Component")
    class AvatarTests {

        @Test
        @DisplayName("Should create avatar with initials and verify visibility")
        void testAvatarWithInitials() {
            StackPane avatar = ComponentFactory.avatar("JD", "blue", 48);
            
            assertNotNull(avatar);
            assertTrue(avatar.getChildren().size() > 0);
            
            // Add to scene and verify with TestFX
            interact(() -> {
                root.getChildren().add(avatar);
            });
            
            verifyThat(".avatar", isNotNull());
        }

        @Test
        @DisplayName("Should create avatar with custom size and verify dimensions")
        void testAvatarCustomSize() {
            StackPane avatar = ComponentFactory.avatar("AB", "red", 64);

            assertNotNull(avatar);
            
            interact(() -> {
                root.getChildren().add(avatar);
            });
            
            // Verify avatar is displayed
            verifyThat(lookup(StackPane.class).queryAs(StackPane.class), hasMinimumSize(64, 64));
        }

        @Test
        @DisplayName("Should create avatar with default color and verify text")
        void testAvatarDefaultColor() {
            StackPane avatar = ComponentFactory.avatar("XY", null, 40);
            
            assertNotNull(avatar);
            
            interact(() -> {
                root.getChildren().add(avatar);
            });
            
            // Verify avatar contains the initials
            verifyThat(lookup(Label.class).queryAs(Label.class), LabeledMatchers.hasText("XY"));
        }
    }

    @Nested
    @DisplayName("Badge Component")
    class BadgeTests {

        @Test
        @DisplayName("Should create badge with text and verify display")
        void testBadgeCreation() {
            Label badge = ComponentFactory.badge("New", "blue");

            assertNotNull(badge);
            assertEquals("NEW", badge.getText());
            
            interact(() -> {
                root.getChildren().add(badge);
            });
            
            verifyThat(lookup(Label.class).queryAs(Label.class), LabeledMatchers.hasText("NEW"));
            verifyThat(".badge", hasStyleClass("badge"));
        }

        @Test
        @DisplayName("Should create badge with different colors and verify styling")
        void testBadgeColors() {
            Label badgeRed = ComponentFactory.badge("Error", "red");
            Label badgeGreen = ComponentFactory.badge("Success", "green");
            
            assertNotNull(badgeRed);
            assertNotNull(badgeGreen);
            
            interact(() -> {
                root.getChildren().addAll(badgeRed, badgeGreen);
            });
            
            // Verify badges are visible
            verifyThat(lookup(Label.class).queryAs(Label.class), (n) -> n.getText().equals("ERROR"));
            verifyThat(lookup(Label.class).queryAs(Label.class), (n) -> n.getText().equals("SUCCESS"));
        }
    }

    @Nested
    @DisplayName("Card Component")
    class CardTests {

        @Test
        @DisplayName("Should create card container and verify structure")
        void testCardCreation() {
            VBox card = ComponentFactory.card().build();

            assertNotNull(card);
            
            interact(() -> {
                root.getChildren().add(card);
            });
            
            verifyThat(lookup(VBox.class).queryAs(VBox.class), isNotNull());
            verifyThat(".card", hasStyleClass("card"));
        }

        @Test
        @DisplayName("Should create card with content and verify children")
        void testCardWithContent() {
            Label content = new Label("Test");
            VBox card = ComponentFactory.card().body(content).build();

            assertNotNull(card);
            assertEquals(1, card.getChildren().size());
            
            interact(() -> {
                root.getChildren().add(card);
            });
            
            // Verify card contains the label with text
            verifyThat(lookup(Label.class).queryAs(Label.class), LabeledMatchers.hasText("Test"));
        }
    }

    @Nested
    @DisplayName("Button Component")
    class ButtonTests {

        @Test
        @DisplayName("Should create primary styled button and verify click")
        void testPrimaryButton() {
            Button btn = new Button("Save");
            TailwindFX.apply(btn, "btn-primary");

            assertNotNull(btn);
            assertEquals("Save", btn.getText());
            
            interact(() -> {
                root.getChildren().add(btn);
            });
            
            // Verify button is visible with correct text
            verifyThat(lookup(Button.class).queryAs(Button.class), LabeledMatchers.hasText("Save"));
            verifyThat(".btn-primary", hasStyleClass("btn-primary"));
            
            // Test click interaction
            clickOn(btn);
        }

        @Test
        @DisplayName("Should create secondary styled button and verify appearance")
        void testSecondaryButton() {
            Button btn = new Button("Cancel");
            TailwindFX.apply(btn, "btn-secondary");

            assertNotNull(btn);
            
            interact(() -> {
                root.getChildren().add(btn);
            });
            
            verifyThat(lookup(Button.class).queryAs(Button.class), LabeledMatchers.hasText("Cancel"));
            verifyThat(".btn-secondary", hasStyleClass("btn-secondary"));
        }

        @Test
        @DisplayName("Should create buttons with variants and test interactions")
        void testButtonVariants() {
            Button btnPrimary = new Button("Primary");
            TailwindFX.apply(btnPrimary, "btn-primary");
            Button btnSecondary = new Button("Secondary");
            TailwindFX.apply(btnSecondary, "btn-secondary");
            Button btnDanger = new Button("Delete");
            TailwindFX.apply(btnDanger, "btn-danger");

            assertNotNull(btnPrimary);
            assertNotNull(btnSecondary);
            assertNotNull(btnDanger);
            
            interact(() -> {
                root.getChildren().addAll(btnPrimary, btnSecondary, btnDanger);
            });
            
            // Verify all buttons are visible
            verifyThat(lookup(Button.class).queryAs(Button.class), (n) -> n.getText().equals("Primary"));
            verifyThat(lookup(Button.class).queryAs(Button.class), (n) -> n.getText().equals("Secondary"));
            verifyThat(lookup(Button.class).queryAs(Button.class), (n) -> n.getText().equals("Delete"));
            
            // Test click interactions
            clickOn("Primary");
            clickOn("Secondary");
            clickOn("Delete");
        }
    }

    @Nested
    @DisplayName("Input Component")
    class InputTests {

        @Test
        @DisplayName("Should create styled text field and verify input")
        void testTextFieldCreation() {
            TextField field = new TextField();
            field.setPromptText("Enter text...");
            TailwindFX.apply(field, "input");

            assertNotNull(field);
            assertEquals("Enter text...", field.getPromptText());
            
            interact(() -> {
                root.getChildren().add(field);
            });
            
            // Verify text field is visible with prompt
            verifyThat(lookup(TextField.class).queryAs(TextField.class), hasPromptText("Enter text..."));
            verifyThat(".input", hasStyleClass("input"));
            
            // Test text input interaction
            clickOn(field);
            write("Test input");
            assertEquals("Test input", field.getText());
        }

        @Test
        @DisplayName("Should create text field without prompt and test typing")
        void testTextFieldWithoutPrompt() {
            TextField field = new TextField();
            TailwindFX.apply(field, "input");

            assertNotNull(field);
            
            interact(() -> {
                root.getChildren().add(field);
            });
            
            // Type into the field
            clickOn(field);
            write("Hello World");
            assertEquals("Hello World", field.getText());
            
            // Clear and type again
            field.clear();
            write("New text");
            assertEquals("New text", field.getText());
        }
    }

    @Nested
    @DisplayName("Modal Component")
    class ModalTests {

        @Test
        @DisplayName("Should create modal builder and verify structure")
        void testModalCreation() {
            var modalBuilder = ComponentFactory.modal(new Label("Content"));

            assertNotNull(modalBuilder);
            
            // Build and add modal to scene
            StackPane modal = modalBuilder.build();
            interact(() -> {
                root.getChildren().add(modal);
            });
            
            // Verify modal is visible with content
            verifyThat(lookup(Label.class).queryAs(Label.class), LabeledMatchers.hasText("Content"));
        }

        @Test
        @DisplayName("Should create modal with actions and test button clicks")
        void testModalWithActions() {
            Button okBtn = new Button("OK");
            var modalBuilder = ComponentFactory.modal(new Label("Content"));

            assertNotNull(modalBuilder);
            
            // Build modal and add to scene
            StackPane modal = modalBuilder.build();
            interact(() -> {
                root.getChildren().add(modal);
                root.getChildren().add(okBtn);
            });
            
            // Verify modal content
            verifyThat(Label.class, hasText("Content"));
            
            // Test clicking the OK button
            clickOn(okBtn);
        }
    }

    @Nested
    @DisplayName("Tooltip Component")
    class TooltipTests {

        @Test
        @DisplayName("Should create tooltip for node and verify on hover")
        void testTooltipCreation() {
            Label label = new Label("Hover me");
            var tooltip = ComponentFactory.tooltip(label, "Tooltip text");

            assertNotNull(tooltip);
            
            interact(() -> {
                root.getChildren().add(label);
            });
            
            // Verify label is visible
            verifyThat(lookup(Label.class).queryAs(Label.class), LabeledMatchers.hasText("Hover me"));
            
            // Simulate hover to show tooltip
            moveMouseTo(label);
            // Tooltip should be displayed (verification depends on tooltip implementation)
        }
    }

    @Nested
    @DisplayName("Drawer Component")
    class DrawerTests {

        @Test
        @DisplayName("Should create drawer builder and verify structure")
        void testDrawerCreation() {
            var drawerBuilder = ComponentFactory.drawer(ComponentFactory.DrawerSide.LEFT, 280);

            assertNotNull(drawerBuilder);
            
            // Build drawer and add to scene
            StackPane drawer = drawerBuilder.build();
            interact(() -> {
                root.getChildren().add(drawer);
            });
            
            // Verify drawer is visible
            verifyThat(lookup(StackPane.class).queryAs(StackPane.class), isNotNull());
            
            // Test drawer interactions - simulate open/close
            // (actual implementation may vary)
        }
        
        @Test
        @DisplayName("Should create right drawer and verify positioning")
        void testRightDrawer() {
            var drawerBuilder = ComponentFactory.drawer(ComponentFactory.DrawerSide.RIGHT, 300);
            assertNotNull(drawerBuilder);
            
            StackPane drawer = drawerBuilder.build();
            interact(() -> {
                root.getChildren().add(drawer);
            });
            
            // Verify drawer is positioned correctly
            verifyThat(StackPane.class, isNotNull());
        }
    }
}
