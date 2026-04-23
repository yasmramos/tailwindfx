package tailwindfx;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;
import static org.testfx.matcher.control.LabeledMatchers.*;
import static org.testfx.matcher.control.LabeledMatchers.hasText;
import static org.testfx.matcher.control.TextInputControlMatchers.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base test class for TestFX integration tests.
 * Provides common setup and teardown for all test classes.
 */
abstract class AbstractTestFXTest extends ApplicationTest {
    protected StackPane root;

    @Override
    public void start(Stage stage) {
        root = new StackPane();
        Scene scene = new Scene(root, 1024, 768);
        TailwindFX.install(scene);
        stage.setScene(scene);
        stage.show();
    }
}

/**
 * Button interaction tests using TestFX.
 */
@DisplayName("Button Interaction Tests")
class ButtonInteractionTest extends AbstractTestFXTest {

    @Test
    @DisplayName("Should handle single click on button")
    void testSingleClick() {
        Button btn = new Button("Click Me");
        TailwindFX.apply(btn, "btn-primary");

        AtomicBoolean clicked = new AtomicBoolean(false);
        btn.setOnAction(e -> clicked.set(true));

        interact(() -> root.getChildren().add(btn));

        clickOn("Click Me");
        assertTrue(clicked.get(), "Button should have been clicked");
    }

    @Test
    @DisplayName("Should handle double click on button")
    void testDoubleClick() {
        Button btn = new Button("Double Click");
        AtomicInteger clickCount = new AtomicInteger(0);

        btn.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                clickCount.incrementAndGet();
            }
        });

        interact(() -> root.getChildren().add(btn));

        doubleClickOn("Double Click");
        assertEquals(1, clickCount.get());
    }

    @Test
    @DisplayName("Should handle multiple button clicks in sequence")
    void testMultipleButtonClicks() {
        Button btn1 = new Button("First");
        Button btn2 = new Button("Second");
        Button btn3 = new Button("Third");

        AtomicInteger counter = new AtomicInteger(0);
        btn1.setOnAction(e -> counter.set(1));
        btn2.setOnAction(e -> counter.set(2));
        btn3.setOnAction(e -> counter.set(3));

        interact(() -> root.getChildren().addAll(
                new HBox(btn1, btn2, btn3)));

        clickOn("First");
        assertEquals(1, counter.get());

        clickOn("Second");
        assertEquals(2, counter.get());

        clickOn("Third");
        assertEquals(3, counter.get());
    }
}

/**
 * Text input tests using TestFX.
 */
@DisplayName("Text Input Tests")
class TextInputTest extends AbstractTestFXTest {

    @Test
    @DisplayName("Should type text into TextField")
    void testTyping() {
        TextField field = new TextField();
        TailwindFX.apply(field, "input");

        interact(() -> root.getChildren().add(field));

        clickOn(field);
        write("Hello World");

        assertEquals("Hello World", field.getText());
    }

    @Test
    @DisplayName("Should clear and replace text")
    void testClearAndReplace() {
        TextField field = new TextField();
        field.setText("Initial");

        interact(() -> root.getChildren().add(field));

        clickOn(field);
        field.clear();
        write("New Value");

        assertEquals("New Value", field.getText());
    }

    @Test
    @DisplayName("Should handle multiple text fields")
    void testMultipleFields() {
        TextField field1 = new TextField();
        field1.setId("field1");
        TextField field2 = new TextField();
        field2.setId("field2");

        interact(() -> root.getChildren().addAll(
                new VBox(field1, field2)));

        clickOn("#field1");
        write("First");

        clickOn("#field2");
        write("Second");

        assertEquals("First", field1.getText());
        assertEquals("Second", field2.getText());
    }

    @Test
    @DisplayName("Should verify prompt text")
    void testPromptText() {
        TextField field = new TextField();
        field.setPromptText("Enter your name");

        interact(() -> root.getChildren().add(field));

        assertEquals("Enter your name", field.getPromptText());
    }
}

/**
 * Node lookup and verification tests using TestFX.
 */
@DisplayName("Node Lookup Tests")
class NodeLookupTest extends AbstractTestFXTest {

    @Test
    @DisplayName("Should find node by CSS selector")
    void testFindByCssSelector() {
        Label label = new Label("Target");
        label.getStyleClass().add("target-class");

        interact(() -> root.getChildren().add(label));

        verifyThat(".target-class", isNotNull());
        verifyThat(".target-class", LabeledMatchers.hasText("Target"));
    }

    @Test
    @DisplayName("Should find node by ID")
    void testFindById() {
        Label label = new Label("ID Target");
        label.setId("my-label-id");

        interact(() -> root.getChildren().add(label));

        verifyThat("#my-label-id", isNotNull());
        verifyThat("#my-label-id", LabeledMatchers.hasText("ID Target"));
    }

    @Test
    @DisplayName("Should verify node properties")
    void testVerifyProperties() {
        Button btn = new Button("Test Button");
        btn.setDisable(false);

        interact(() -> root.getChildren().add(btn));

        verifyThat("Test Button", (Node n) -> !((Button) n).isDisabled());
        verifyThat("Test Button", LabeledMatchers.hasText("Test Button"));
    }

    @Test
    @DisplayName("Should count matching nodes")
    void testCountNodes() {
        for (int i = 0; i < 5; i++) {
            Label label = new Label("Item " + i);
            label.getStyleClass().add("item");
            interact(() -> root.getChildren().add(label));
        }

        long count = lookup(".item").queryAll().size();
        assertEquals(5, count);
    }
}

/**
 * Mouse movement and hover tests using TestFX.
 */
@DisplayName("Mouse Movement Tests")
class MouseMovementTest extends AbstractTestFXTest {

    @Test
    @DisplayName("Should trigger hover effect")
    void testHover() {
        Label label = new Label("Hover Me");
        AtomicBoolean hovered = new AtomicBoolean(false);

        label.setOnMouseEntered(e -> hovered.set(true));
        label.setOnMouseExited(e -> hovered.set(false));

        interact(() -> root.getChildren().add(label));

        moveTo(label);
        sleep(100);

        assertTrue(hovered.get(), "Hover should be triggered");
    }

    @Test
    @DisplayName("Should move between multiple nodes")
    void testMoveBetweenNodes() {
        Label label1 = new Label("First");
        Label label2 = new Label("Second");

        AtomicReference<Node> hoveredNode = new AtomicReference<>();

        label1.setOnMouseEntered(e -> hoveredNode.set(label1));
        label2.setOnMouseEntered(e -> hoveredNode.set(label2));

        interact(() -> root.getChildren().addAll(
                new VBox(label1, label2)));

        moveTo(label1);
        sleep(100);
        assertEquals(label1, hoveredNode.get());

        moveTo(label2);
        sleep(100);
        assertEquals(label2, hoveredNode.get());
    }
}

/**
 * Complex UI workflow tests using TestFX.
 */
@DisplayName("UI Workflow Tests")
class UIWorkflowTest extends AbstractTestFXTest {

    @Test
    @DisplayName("Should complete form submission workflow")
    void testFormWorkflow() {
        TextField nameField = new TextField();
        nameField.setId("name");
        nameField.setPromptText("Your Name");

        TextField emailField = new TextField();
        emailField.setId("email");
        emailField.setPromptText("Your Email");

        Button submitBtn = new Button("Submit");
        TailwindFX.apply(submitBtn, "btn-primary");

        AtomicBoolean submitted = new AtomicBoolean(false);
        submitBtn.setOnAction(e -> {
            if (!nameField.getText().isEmpty() && !emailField.getText().isEmpty()) {
                submitted.set(true);
            }
        });

        interact(() -> {
            VBox form = new VBox(10, nameField, emailField, submitBtn);
            TailwindFX.apply(form, "p-4", "gap-2");
            root.getChildren().add(form);
        });

        clickOn("#name");
        write("John Doe");

        clickOn("#email");
        write("john@example.com");

        clickOn("Submit");

        assertTrue(submitted.get(), "Form should be submitted");
    }

    @Test
    @DisplayName("Should handle tab navigation")
    void testTabNavigation() {
        TextField field1 = new TextField();
        field1.setId("field1");
        TextField field2 = new TextField();
        field2.setId("field2");
        Button btn = new Button("Go");

        interact(() -> root.getChildren().addAll(
                new VBox(field1, field2, btn)));

        clickOn("#field1");
        write("First");

        press(KeyCode.TAB);
        release(KeyCode.TAB);
        write("Second");

        assertEquals("First", field1.getText());
        assertEquals("Second", field2.getText());
    }
}

/**
 * Component factory integration tests using TestFX.
 */
@DisplayName("Component Factory Tests")
class ComponentFactoryTest extends AbstractTestFXTest {

    @Test
    @DisplayName("Should create and interact with avatar component")
    void testAvatarComponent() {
        StackPane avatar = ComponentFactory.avatar("JD", "blue", 48);

        interact(() -> root.getChildren().add(avatar));

        verifyThat(lookup(".stack-pane").queryAs(StackPane.class), isNotNull());
        clickOn(avatar);
    }

    @Test
    @DisplayName("Should create and verify badge component")
    void testBadgeComponent() {
        Label badge = ComponentFactory.badge("New", "green");

        interact(() -> root.getChildren().add(badge));

        verifyThat(lookup(".label").queryAs(Label.class), hasText("NEW"));
    }

    @Test
    @DisplayName("Should create card with interactive buttons")
    void testCardComponent() {
        Button actionBtn = new Button("Action");
        AtomicBoolean actionClicked = new AtomicBoolean(false);
        actionBtn.setOnAction(e -> actionClicked.set(true));

        VBox card = ComponentFactory.card()
                .body(new Label("Card Content"))
                .build();

        interact(() -> {
                card.getChildren().add(actionBtn);
                root.getChildren().add(card);
            });

        clickOn("Action");
        assertTrue(actionClicked.get());
    }
}
