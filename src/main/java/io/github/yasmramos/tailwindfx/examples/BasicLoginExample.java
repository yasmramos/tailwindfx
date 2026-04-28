package io.github.yasmramos.tailwindfx.examples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Basic TailwindFX Login Example
 * Demonstrates a beginner-friendly login form layout
 */
public class BasicLoginExample extends Application {

    @Override
    public void start(Stage stage) {

        Label title = new Label("Welcome Back");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label subtitle = new Label("Login to continue");
        subtitle.setStyle("-fx-font-size: 14px;");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMaxWidth(250);

        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(250);
        loginButton.setStyle(
                "-fx-background-color: #111827;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 8px;" +
                "-fx-padding: 10px;"
        );

        Hyperlink forgotPassword = new Hyperlink("Forgot Password?");
        forgotPassword.setStyle("-fx-text-fill: #2563eb;");

        VBox root = new VBox(15,
                title,
                subtitle,
                emailField,
                passwordField,
                loginButton,
                forgotPassword
        );

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle(
                "-fx-background-color: #f9fafb;" +
                "-fx-border-color: #e5e7eb;" +
                "-fx-border-radius: 12px;" +
                "-fx-background-radius: 12px;"
        );

        Scene scene = new Scene(root, 450, 550);

        stage.setTitle("TailwindFX Basic Login Example");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
