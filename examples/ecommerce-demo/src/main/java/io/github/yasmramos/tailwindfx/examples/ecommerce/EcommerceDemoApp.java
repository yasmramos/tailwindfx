package io.github.yasmramos.tailwindfx.examples.ecommerce;

import io.github.yasmramos.tailwindfx.TwInstall;
import io.github.yasmramos.tailwindfx.TwStyle;
import io.github.yasmramos.tailwindfx.TwTheme;
import io.github.yasmramos.tailwindfx.animation.TwAnimation;
import io.github.yasmramos.tailwindfx.components.TwAlert;
import io.github.yasmramos.tailwindfx.components.TwButton;
import io.github.yasmramos.tailwindfx.components.TwInput;
import io.github.yasmramos.tailwindfx.responsive.ContainerQuery;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * E-commerce demo showcasing TailwindFX capabilities.
 *
 * <p>Features demonstrated: a GridPane product layout, flex-style cart and navigation, light/dark
 * theme switching, animations for add-to-cart and validation feedback, category filtering, a
 * validated checkout form, and responsive breakpoints.
 */
public class EcommerceDemoApp extends Application {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern CARD_PATTERN = Pattern.compile("\\d{16}");
    private static final int CARD_NUMBER_LENGTH = 16;
    private static final int NAME_MIN_LENGTH = 2;

    private final List<Product> products = new ArrayList<>();
    private final List<Product> cart = new ArrayList<>();

    private Scene scene;
    private Stage stage;

    private Label cartCountLabel;
    private Label cartTotalLabel;
    private VBox cartItemsContainer;
    private HBox cartIcon;
    private VBox cartSidebar;
    private VBox productsRoot;
    private Label resultCountLabel;
    private Button themeToggle;

    private String activeCategory = "All";
    private boolean darkMode = false;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        initializeProducts();

        BorderPane root = new BorderPane();
        TwStyle.apply(root, "bg-gray-50", "font-sans");

        root.setTop(createHeader());

        HBox content = new HBox(20);
        content.setPadding(new Insets(20));
        TwStyle.apply(content, "gap-5");

        productsRoot = new VBox(20);
        productsRoot.setPadding(new Insets(10));
        productsRoot.setId("productsRoot");

        ScrollPane productScroll = new ScrollPane(productsRoot);
        productScroll.setFitToWidth(true);
        productScroll.setId("productScroll");
        TwStyle.apply(productScroll, "flex-grow");
        HBox.setHgrow(productScroll, Priority.ALWAYS);

        cartSidebar = createCartSidebar();
        cartSidebar.setPrefWidth(300);
        cartSidebar.setId("cartSidebar");
        TwStyle.apply(cartSidebar, "w-[300px]", "border-l", "border-gray-200");

        content.getChildren().addAll(productScroll, cartSidebar);
        root.setCenter(content);

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double width = bounds.getWidth() > 0 ? Math.min(1400, bounds.getWidth()) : 1400;
        double height = bounds.getHeight() > 0 ? Math.min(900, bounds.getHeight()) : 900;
        scene = new Scene(root, width, height);

        TwInstall.install(scene);

        renderProducts();
        updateCartUI();

        primaryStage.setTitle("TailwindFX E-commerce Demo");
        primaryStage.setScene(scene);
        primaryStage.show();

        setupResponsiveQueries(root);
    }

    private VBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(16, 24, 16, 24));
        TwStyle.apply(header, "bg-white", "shadow-md", "items-center", "justify-between");

        Label logo = new Label("🛍️ TailwindFX Store");
        TwStyle.apply(logo, "text-2xl", "font-bold", "text-blue-600");

        HBox nav = new HBox(30);
        TwStyle.apply(nav, "items-center");

        String[] navItems = {"Home", "Products", "Categories", "About"};
        for (String item : navItems) {
            Label navLabel = new Label(item);
            TwStyle.apply(navLabel, "text-gray-600", "cursor-pointer");
            navLabel.setOnMouseClicked(e -> System.out.println("Navigate to: " + item));
            nav.getChildren().add(navLabel);
        }

        themeToggle = TwButton.outline("🌙 Dark");
        themeToggle.setId("themeToggle");
        themeToggle.setOnAction(e -> toggleTheme());

        cartIcon = new HBox(8);
        cartIcon.setId("cartIcon");
        TwStyle.apply(cartIcon, "items-center", "cursor-pointer");

        Label cartEmoji = new Label("🛒");
        TwStyle.apply(cartEmoji, "text-xl");

        cartCountLabel = new Label("0");
        cartCountLabel.setId("cartCount");
        TwStyle.apply(cartCountLabel, "bg-blue-500", "text-white", "rounded-full", "min-w-[1.25rem]", "h-5", "flex", "items-center", "justify-center", "text-xs");

        cartIcon.getChildren().addAll(cartEmoji, cartCountLabel);
        cartIcon.setOnMouseClicked(e -> toggleCart());

        HBox actions = new HBox(16, themeToggle, cartIcon);
        TwStyle.apply(actions, "items-center");

        header.getChildren().addAll(logo, nav, actions);

        return new VBox(header);
    }

    /** Rebuilds the product section (filters + grid) honoring the active category filter. */
    private void renderProducts() {
        productsRoot.getChildren().clear();

        Label title = new Label("Featured Products");
        TwStyle.apply(title, "text-3xl", "font-bold", "text-gray-800", "mb-6");

        HBox filters = new HBox(12);
        TwStyle.apply(filters, "gap-3");

        String[] categories = {"All", "Electronics", "Clothing", "Home"};
        for (String category : categories) {
            Button btn = category.equals(activeCategory)
                    ? TwButton.primary(category)
                    : TwButton.outline(category);
            btn.setId("filter-" + category);
            btn.setOnAction(e -> filterByCategory(category));
            filters.getChildren().add(btn);
        }

        List<Product> visible = products.stream()
                .filter(p -> "All".equals(activeCategory) || p.getCategory().equals(activeCategory))
                .toList();

        resultCountLabel = new Label(visible.size() + " product(s)");
        resultCountLabel.setId("resultCount");
        TwStyle.apply(resultCountLabel, "text-sm", "text-gray-500");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setId("productGrid");
        TwStyle.apply(grid, "gap-5");
        grid.setPadding(new Insets(20, 0, 0, 0));

        int col = 0, row = 0;
        for (Product product : visible) {
            if (col >= 3) {
                col = 0;
                row++;
            }
            grid.add(createProductCard(product), col, row);
            col++;
        }

        productsRoot.getChildren().addAll(title, filters, resultCountLabel, grid);
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(12);
        card.setId("card-" + slug(product.getName()));
        TwStyle.apply(card, "bg-white", "rounded-lg", "shadow-md", "overflow-hidden");

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(200);
        TwStyle.apply(imageContainer, "bg-gray-100");

        Label imagePlaceholder = new Label(product.getEmoji());
        TwStyle.apply(imagePlaceholder, "text-6xl");

        imageContainer.getChildren().add(imagePlaceholder);
        card.getChildren().add(imageContainer);

        VBox info = new VBox(8);
        info.setPadding(new Insets(12));
        TwStyle.apply(info, "flex-grow");

        Label name = new Label(product.getName());
        TwStyle.apply(name, "text-lg", "font-semibold", "text-gray-800");

        Label category = new Label(product.getCategory());
        TwStyle.apply(category, "text-sm", "text-gray-500");

        HBox priceRow = new HBox(8);
        TwStyle.apply(priceRow, "items-center", "justify-between");

        Label price = new Label("$" + String.format("%.2f", product.getPrice()));
        TwStyle.apply(price, "text-xl", "font-bold", "text-blue-600");

        Label rating = new Label("⭐ " + product.getRating());
        TwStyle.apply(rating, "text-sm", "text-yellow-500");

        priceRow.getChildren().addAll(price, rating);

        Button addToCartBtn = TwButton.primary("Add to Cart");
        addToCartBtn.setMaxWidth(Double.MAX_VALUE);
        addToCartBtn.setOnAction(e -> addToCart(product, addToCartBtn));

        info.getChildren().addAll(name, category, priceRow, addToCartBtn);
        card.getChildren().add(info);

        return card;
    }

    private VBox createCartSidebar() {
        VBox sidebar = new VBox(16);
        sidebar.setPadding(new Insets(20));
        TwStyle.apply(sidebar, "bg-white");

        Label title = new Label("Shopping Cart");
        TwStyle.apply(title, "text-xl", "font-bold", "text-gray-800", "mb-4");
        sidebar.getChildren().add(title);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        TwStyle.apply(scroll, "flex-grow");

        cartItemsContainer = new VBox(12);
        TwStyle.apply(cartItemsContainer, "gap-3");
        scroll.setContent(cartItemsContainer);

        VBox.setVgrow(scroll, Priority.ALWAYS);
        sidebar.getChildren().add(scroll);

        Separator separator = new Separator();
        sidebar.getChildren().add(separator);

        HBox totalRow = new HBox();
        TwStyle.apply(totalRow, "items-center", "justify-between");

        Label totalLabel = new Label("Total:");
        TwStyle.apply(totalLabel, "text-lg", "font-semibold");

        cartTotalLabel = new Label("$0.00");
        cartTotalLabel.setId("cartTotal");
        TwStyle.apply(cartTotalLabel, "text-xl", "font-bold", "text-blue-600");

        totalRow.getChildren().addAll(totalLabel, cartTotalLabel);
        sidebar.getChildren().add(totalRow);

        Button checkoutBtn = TwButton.primary("Checkout");
        checkoutBtn.setId("checkoutButton");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setOnAction(e -> checkout());
        sidebar.getChildren().add(checkoutBtn);

        return sidebar;
    }

    private void addToCart(Product product, Node source) {
        cart.add(product);
        updateCartUI();
        // Subtle feedback: pulse the cart icon and pop the clicked button.
        TwAnimation.pulse(cartIcon).cycleCount(2).play();
        TwAnimation.scaleIn(source, TwAnimation.FAST).play();
    }

    private void updateCartUI() {
        cartCountLabel.setText(String.valueOf(cart.size()));
        cartItemsContainer.getChildren().clear();
        double total = 0;

        for (Product product : cart) {
            HBox itemRow = new HBox(12);
            TwStyle.apply(itemRow, "items-center", "gap-3", "p-2", "rounded", "hover:bg-gray-50");

            Label emoji = new Label(product.getEmoji());
            TwStyle.apply(emoji, "text-lg");

            VBox itemInfo = new VBox(4);
            Label name = new Label(product.getName());
            TwStyle.apply(name, "font-medium");

            Label price = new Label("$" + String.format("%.2f", product.getPrice()));
            TwStyle.apply(price, "text-gray-600", "text-sm");

            itemInfo.getChildren().addAll(name, price);
            HBox.setHgrow(itemInfo, Priority.ALWAYS);

            Button removeBtn = TwButton.ghost("✕");
            removeBtn.setId("remove-" + slug(product.getName()));
            removeBtn.setOnAction(e -> removeFromCart(product));

            itemRow.getChildren().addAll(emoji, itemInfo, removeBtn);
            cartItemsContainer.getChildren().add(itemRow);

            total += product.getPrice();
        }

        cartTotalLabel.setText("$" + String.format("%.2f", total));
    }

    private void removeFromCart(Product product) {
        cart.remove(product);
        updateCartUI();
    }

    private void filterByCategory(String category) {
        activeCategory = category;
        renderProducts();
    }

    /** Collapses or expands the cart sidebar. */
    private void toggleCart() {
        boolean visible = cartSidebar.isVisible();
        cartSidebar.setVisible(!visible);
        cartSidebar.setManaged(!visible);
    }

    private void toggleTheme() {
        darkMode = !darkMode;
        if (darkMode) {
            TwTheme.of(scene).dark().animated(TwAnimation.NORMAL).apply();
            themeToggle.setText("☀️ Light");
        } else {
            TwTheme.of(scene).light().animated(TwAnimation.NORMAL).apply();
            themeToggle.setText("🌙 Dark");
        }
    }

    private void checkout() {
        if (cart.isEmpty()) {
            TwAlert warning = new TwAlert(TwAlert.AlertType.WARNING, "Your cart is empty.");
            showTransientAlert(warning);
            return;
        }
        openCheckoutDialog();
    }

    /** Opens the checkout form and validates its fields before completing the order. */
    private void openCheckoutDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Checkout");
        dialog.setHeaderText("Enter your shipping and payment details");

        TwInput nameInput = new TwInput("Full name");
        nameInput.setId("checkoutName");
        TwInput emailInput = new TwInput("Email");
        emailInput.setId("checkoutEmail");
        TwInput addressInput = new TwInput("Shipping address");
        addressInput.setId("checkoutAddress");
        TwInput cardInput = new TwInput("Card number (16 digits)");
        cardInput.setId("checkoutCard");
        cardInput.setNumericOnly();

        TwAlert errorAlert = new TwAlert(TwAlert.AlertType.ERROR, "");
        errorAlert.setId("checkoutError");
        errorAlert.setVisible(false);
        errorAlert.setManaged(false);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(16));
        form.addRow(0, new Label("Name"), nameInput);
        form.addRow(1, new Label("Email"), emailInput);
        form.addRow(2, new Label("Address"), addressInput);
        form.addRow(3, new Label("Card"), cardInput);
        form.add(errorAlert, 0, 4, 2, 1);
        GridPane.setHgrow(nameInput, Priority.ALWAYS);
        GridPane.setHgrow(emailInput, Priority.ALWAYS);
        GridPane.setHgrow(addressInput, Priority.ALWAYS);
        GridPane.setHgrow(cardInput, Priority.ALWAYS);
        TwStyle.apply(form, "bg-white");

        dialog.getDialogPane().setContent(form);
        ButtonType placeOrder = new ButtonType("Place Order", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(placeOrder, ButtonType.CANCEL);
        dialog.initOwner(stage);

        Button placeOrderButton = (Button) dialog.getDialogPane().lookupButton(placeOrder);
        placeOrderButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            List<String> errors = validateCheckout(
                    nameInput.getText(), emailInput.getText(),
                    addressInput.getText(), cardInput.getText());

            nameInput.setError(errors.contains("name"));
            emailInput.setError(errors.contains("email"));
            addressInput.setError(errors.contains("address"));
            cardInput.setError(errors.contains("card"));

            if (!errors.isEmpty()) {
                errorAlert.setContent(describeErrors(errors));
                errorAlert.setVisible(true);
                errorAlert.setManaged(true);
                TwAnimation.shake(form).play();
                // Keep the dialog open so the user can correct the highlighted fields.
                event.consume();
            }
        });

        dialog.showAndWait().ifPresent(button -> {
            if (button == placeOrder) {
                completeOrder();
            }
        });
    }

    private void completeOrder() {
        int items = cart.size();
        cart.clear();
        updateCartUI();
        TwAlert success = new TwAlert(TwAlert.AlertType.SUCCESS,
                "Order placed for " + items + " item(s). Thank you!");
        showTransientAlert(success);
    }

    /** Shows a non-blocking alert at the bottom of the window for a few seconds. */
    private void showTransientAlert(TwAlert alert) {
        VBox holder = new VBox(alert);
        holder.setPadding(new Insets(12, 24, 0, 24));
        BorderPane root = (BorderPane) scene.getRoot();
        root.setBottom(holder);
        TwAnimation.fadeIn(holder, TwAnimation.FAST).onFinished(e -> root.setBottom(null)).play();
    }

    /**
     * Validates the checkout form and returns the list of invalid field keys.
     *
     * @return a list containing any of {@code "name"}, {@code "email"}, {@code "address"},
     *     {@code "card"} for the fields that failed validation; empty when the form is valid
     */
    static List<String> validateCheckout(String name, String email, String address, String card) {
        List<String> errors = new ArrayList<>();
        if (name == null || name.trim().length() < NAME_MIN_LENGTH) {
            errors.add("name");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            errors.add("email");
        }
        if (address == null || address.trim().isEmpty()) {
            errors.add("address");
        }
        if (card == null || !CARD_PATTERN.matcher(card.trim()).matches()) {
            errors.add("card");
        }
        return errors;
    }

    private static String describeErrors(List<String> errors) {
        StringBuilder sb = new StringBuilder("Please fix: ");
        for (int i = 0; i < errors.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(switch (errors.get(i)) {
                case "name" -> "a valid name";
                case "email" -> "a valid email";
                case "address" -> "a shipping address";
                case "card" -> "a " + CARD_NUMBER_LENGTH + "-digit card number";
                default -> errors.get(i);
            });
        }
        return sb.toString();
    }

    /** Converts a product name to a CSS-id-safe slug (ids may not contain spaces). */
    private static String slug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }

    private void initializeProducts() {
        products.add(new Product("Wireless Headphones", "Electronics", 99.99, "4.5", "🎧"));
        products.add(new Product("Smart Watch", "Electronics", 249.99, "4.8", "⌚"));
        products.add(new Product("Laptop Stand", "Electronics", 49.99, "4.3", "💻"));
        products.add(new Product("Bluetooth Speaker", "Electronics", 79.99, "4.6", "🔊"));
        products.add(new Product("Cotton T-Shirt", "Clothing", 29.99, "4.2", "👕"));
        products.add(new Product("Running Shoes", "Clothing", 89.99, "4.7", "👟"));
        products.add(new Product("Coffee Maker", "Home", 119.99, "4.5", "☕"));
        products.add(new Product("Desk Lamp", "Home", 39.99, "4.4", "💡"));
    }

    private void setupResponsiveQueries(BorderPane root) {
        ContainerQuery.on(root)
            .base("p-4")
            .md("p-6")
            .lg("p-8")
            .install(root);
    }

    public static class Product {
        private final String name;
        private final String category;
        private final double price;
        private final String rating;
        private final String emoji;

        public Product(String name, String category, double price, String rating, String emoji) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.rating = rating;
            this.emoji = emoji;
        }

        public String getName() { return name; }
        public String getCategory() { return category; }
        public double getPrice() { return price; }
        public String getRating() { return rating; }
        public String getEmoji() { return emoji; }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
