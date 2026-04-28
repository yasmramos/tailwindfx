package io.github.yasmramos.tailwindfx.examples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class BasicDashboardExample extends Application {

    @Override
    public void start(Stage stage) {

        // Sidebar
        Label logo = new Label("TailwindFX");
        logo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Button dashboardBtn = new Button("Dashboard");
        Button profileBtn = new Button("Profile");
        Button settingsBtn = new Button("Settings");
        Button logoutBtn = new Button("Logout");

        dashboardBtn.setPrefWidth(150);
        profileBtn.setPrefWidth(150);
        settingsBtn.setPrefWidth(150);
        logoutBtn.setPrefWidth(150);

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

        // Navbar
        Label welcomeText = new Label("Welcome, User");
        welcomeText.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

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

        // Root Layout
        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(mainContent);

        Scene scene = new Scene(root, 1100, 700);

        stage.setTitle("TailwindFX Basic Dashboard Example");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createCard(String titleText, String valueText) {

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 14px;");

        Label value = new Label(valueText);
        value.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        VBox card = new VBox(10, title, value);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefSize(180, 120);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #e5e7eb;" +
                "-fx-border-radius: 8px;" +
                "-fx-background-radius: 8px;"
        );

        return card;
    }

    public static void main(String[] args) {
        launch();
    }
}
