package io.github.yasmramos.tailwindfx.examples.ecommerce;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

/**
 * Integration tests for {@link EcommerceDemoApp}, driving the real JavaFX scene graph through
 * TestFX (headless Monocle).
 */
@ExtendWith(ApplicationExtension.class)
public class EcommerceDemoIT {

    private EcommerceDemoApp app;
    private Scene scene;
    private final org.testfx.api.FxRobot robot = new org.testfx.api.FxRobot();

    @Start
    public void start(Stage stage) {
        app = new EcommerceDemoApp();
        app.start(stage);
        scene = stage.getScene();
        stage.show();
    }

    @Test
    public void applicationLaunchesWithHeader() {
        verifyThat("🛍️ TailwindFX Store", hasText("🛍️ TailwindFX Store"));
        verifyThat("Featured Products", isVisible());
        verifyThat("Shopping Cart", isVisible());
    }

    @Test
    public void allProductCardsAreRendered() {
        for (String id : new String[] {
                "wireless-headphones", "smart-watch", "laptop-stand", "bluetooth-speaker",
                "cotton-t-shirt", "running-shoes", "coffee-maker", "desk-lamp"}) {
            assertTrue(scene.lookup("#card-" + id) != null, "missing product card: " + id);
        }
    }

    @Test
    public void addToCartUpdatesCountAndTotal() {
        clickFirstAddToCart();

        assertEquals("1", labelText("#cartCount"));
        assertEquals("$99.99", labelText("#cartTotal"));
    }

    @Test
    public void filterByCategoryShowsOnlyMatchingProducts() {
        click("#filter-Home");

        assertEquals("2 product(s)", labelText("#resultCount"));
        assertTrue(scene.lookup("#card-coffee-maker") != null);
        assertTrue(scene.lookup("#card-desk-lamp") != null);
        assertTrue(scene.lookup("#card-smart-watch") == null);
    }

    @Test
    public void removeFromCartResetsTotal() {
        clickFirstAddToCart();
        click("#remove-wireless-headphones");

        assertEquals("0", labelText("#cartCount"));
        assertEquals("$0.00", labelText("#cartTotal"));
    }

    @Test
    public void toggleCartHidesAndShowsSidebar() {
        Node sidebar = scene.lookup("#cartSidebar");
        assertTrue(sidebar.isVisible());

        click("#cartIcon");
        assertFalse(sidebar.isVisible());

        click("#cartIcon");
        assertTrue(sidebar.isVisible());
    }

    @Test
    public void checkoutValidationRejectsInvalidInput() {
        List<String> errors = EcommerceDemoApp.validateCheckout("", "not-an-email", "", "123");
        assertEquals(List.of("name", "email", "address", "card"), errors);
    }

    @Test
    public void checkoutValidationAcceptsValidInput() {
        List<String> errors = EcommerceDemoApp.validateCheckout(
                "Jane Doe", "jane@example.com", "123 Main St", "4242424242424242");
        assertTrue(errors.isEmpty(), "expected no errors but got " + errors);
    }

    private void clickFirstAddToCart() {
        click("#card-wireless-headphones .button");
    }

    private void click(String selector) {
        Node node = scene.lookup(selector);
        assertTrue(node != null, "selector matched no node: " + selector);
        robot.clickOn(node);
    }

    private String labelText(String selector) {
        Node node = scene.lookup(selector);
        assertTrue(node instanceof Label, "selector did not match a Label: " + selector);
        return ((Label) node).getText();
    }
}
