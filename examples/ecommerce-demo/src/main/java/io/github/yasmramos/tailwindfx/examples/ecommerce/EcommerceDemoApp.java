package io.github.yasmramos.tailwindfx.examples.ecommerce;

import io.github.yasmramos.tailwindfx.TwStyle;
import io.github.yasmramos.tailwindfx.components.TwButton;
import io.github.yasmramos.tailwindfx.responsive.ContainerQuery;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class EcommerceDemoApp extends Application {

    private List<Product> products = new ArrayList<>();
    private List<Product> cart = new ArrayList<>();
    private Label cartCountLabel;
    private Label cartTotalLabel;
    private VBox cartItemsContainer;

    @Override
    public void start(Stage primaryStage) {
        initializeProducts();

        BorderPane root = new BorderPane();
        TwStyle.apply(root, "bg-gray-50");

        root.setTop(createHeader());

        HBox content = new HBox(20);
        content.setPadding(new Insets(20));
        TwStyle.apply(content, "gap-5");

        ScrollPane productScroll = new ScrollPane(createProductGrid());
        productScroll.setFitToWidth(true);
        TwStyle.apply(productScroll, "flex-grow");
        HBox.setHgrow(productScroll, Priority.ALWAYS);

        VBox cartSidebar = createCartSidebar();
        cartSidebar.setPrefWidth(300);
        TwStyle.apply(cartSidebar, "w-[300px]", "border-l", "border-gray-200");

        content.getChildren().addAll(productScroll, cartSidebar);
        root.setCenter(content);

        Scene scene = new Scene(root, 1400, 900);
        TwStyle.apply(root, "font-sans");

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

        HBox cartIcon = new HBox(8);
        TwStyle.apply(cartIcon, "items-center", "cursor-pointer");
        
        Label cartEmoji = new Label("🛒");
        TwStyle.apply(cartEmoji, "text-xl");
        
        cartCountLabel = new Label("0");
        TwStyle.apply(cartCountLabel, "bg-blue-500", "text-white", "rounded-full", "min-w-[1.25rem]", "h-5", "flex", "items-center", "justify-center", "text-xs");

        cartIcon.getChildren().addAll(cartEmoji, cartCountLabel);
        cartIcon.setOnMouseClicked(e -> toggleCart());

        header.getChildren().addAll(logo, nav, cartIcon);
        
        VBox wrapper = new VBox(header);
        return wrapper;
    }

    private VBox createProductGrid() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(10));

        Label title = new Label("Featured Products");
        TwStyle.apply(title, "text-3xl", "font-bold", "text-gray-800", "mb-6");
        container.getChildren().add(title);

        HBox filters = new HBox(12);
        TwStyle.apply(filters, "gap-3");
        
        String[] categories = {"All", "Electronics", "Clothing", "Home"};
        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];
            Button btn;
            if (i == 0) {
                btn = TwButton.primary(category);
            } else {
                btn = TwButton.outline(category);
            }
            
            btn.setOnAction(e -> {
                System.out.println("Filter by: " + category);
                filterByCategory(category);
            });
            
            filters.getChildren().add(btn);
        }
        
        container.getChildren().add(filters);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        TwStyle.apply(grid, "gap-5");
        grid.setPadding(new Insets(20, 0, 0, 0));

        int col = 0, row = 0;
        for (Product product : products) {
            if (col >= 3) {
                col = 0;
                row++;
            }
            grid.add(createProductCard(product), col, row);
            col++;
        }

        container.getChildren().add(grid);
        return container;
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(12);
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
        addToCartBtn.setOnAction(e -> addToCart(product));

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
        TwStyle.apply(cartTotalLabel, "text-xl", "font-bold", "text-blue-600");

        totalRow.getChildren().addAll(totalLabel, cartTotalLabel);
        sidebar.getChildren().add(totalRow);

        Button checkoutBtn = TwButton.primary("Checkout");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setOnAction(e -> checkout());
        sidebar.getChildren().add(checkoutBtn);

        return sidebar;
    }

    private void addToCart(Product product) {
        cart.add(product);
        updateCartUI();
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

            itemRow.getChildren().addAll(emoji, itemInfo);
            cartItemsContainer.getChildren().add(itemRow);

            total += product.getPrice();
        }

        cartTotalLabel.setText("$" + String.format("%.2f", total));
    }

    private void filterByCategory(String category) {
        System.out.println("Filtering by: " + category);
    }

    private void toggleCart() {
        System.out.println("Toggle cart sidebar");
    }

    private void checkout() {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty!");
            return;
        }
        System.out.println("Processing checkout for " + cart.size() + " items");
        cart.clear();
        updateCartUI();
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
