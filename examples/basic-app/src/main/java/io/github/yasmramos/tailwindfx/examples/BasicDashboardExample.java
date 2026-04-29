package io.github.yasmramos.tailwindfx.examples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import io.github.yasmramos.tailwindfx.TailwindFX;

public class BasicDashboardExample extends Application {

    @Override
    public void start(Stage stage) {

        // Sidebar
        Label logo = new Label("TailwindFX");
        TailwindFX.apply(logo, "text-2xl", "font-bold");

        Button dashboardBtn = new Button("Dashboard");
        Button profileBtn = new Button("Profile");
        Button settingsBtn = new Button("Settings");
        Button logoutBtn = new Button("Logout");

        dashboardBtn.setPrefWidth(150);
        profileBtn.setPrefWidth(150);
        settingsBtn.setPrefWidth(150);
        logoutBtn.setPrefWidth(150);

        TailwindFX.apply(dashboardBtn, "btn-primary", "rounded-lg");
        TailwindFX.apply(profileBtn, "rounded-lg", "border");
        TailwindFX.apply(settingsBtn, "rounded-lg", "border");
        TailwindFX.apply(logoutBtn, "rounded-lg", "border");

        VBox sidebar = new VBox(15,
                logo,
                new Separator(),
                dashboardBtn,
                profileBtn,
                settingsBtn,
                logoutBtn
        );

        sidebar.setPadding(new Insets(20));
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPrefWidth(200);
        TailwindFX.apply(sidebar, "bg-gray-100", "border-r");

        // Navbar
        Label welcomeText = new Label("Welcome, User");
        TailwindFX.apply(welcomeText, "text-2xl", "font-bold");

        HBox navbar = new HBox(welcomeText);
        navbar.setPadding(new Insets(20));
        navbar.setAlignment(Pos.CENTER_LEFT);

        // Stats Cards
        VBox salesCard = createCard("Total Sales", "$12,450");
        VBox usersCard = createCard("Users", "1,248");
        VBox ordersCard = createCard("Orders", "320");

        HBox cardsSection = new HBox(20, salesCard, usersCard, ordersCard);
        cardsSection.setAlignment(Pos.CENTER);

        // Collapse Section
        TitledPane analyticsPane = new TitledPane(
                "Analytics Overview",
                new Label("Sales are up by 18% this month.")
        );

        TitledPane reportsPane = new TitledPane(
                "Reports",
                new Label("Monthly reports are available for review.")
        );

        analyticsPane.getStyleClass().add("collapse-item");
        reportsPane.getStyleClass().add("collapse-item");

        VBox collapseBox = new VBox(10, analyticsPane, reportsPane);
        collapseBox.getStyleClass().add("collapse");

        // Main Content
        VBox mainContent = new VBox(25,
                navbar,
                cardsSection,
                collapseBox
        );

        mainContent.setPadding(new Insets(20));
        TailwindFX.apply(mainContent, "bg-gray-50");

        // Root Layout
        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(mainContent);

        Scene scene = new Scene(root, 1100, 700);

        // Install TailwindFX framework styles
        TailwindFX.install(scene);

        stage.setTitle("TailwindFX Basic Dashboard Example");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private VBox createCard(String titleText, String valueText) {

        Label title = new Label(titleText);
        TailwindFX.apply(title, "text-sm", "text-gray-600");

        Label value = new Label(valueText);
        TailwindFX.apply(value, "text-2xl", "font-bold");

        VBox card = new VBox(10, title, value);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefSize(180, 120);
        TailwindFX.apply(card, "bg-white", "border", "rounded-lg", "shadow-md");

        return card;
    }

    public static void main(String[] args) {
        launch();
    }
}
