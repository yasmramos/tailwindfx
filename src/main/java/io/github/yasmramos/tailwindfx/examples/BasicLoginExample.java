package io.github.yasmramos.tailwindfx.examples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tailwindfx.TailwindFX;

/**
 * Basic TailwindFX Login Example
 * Demonstrates a beginner-friendly login form layout
 */
public class BasicLoginExample extends Application {

    @Override
    public void start(Stage stage) {

        Label title = new Label("Welcome Back");
        TailwindFX.apply(title, "text-2xl", "font-bold");

        Label subtitle = new Label("Login to continue");
        TailwindFX.apply(subtitle, "text-sm", "text-gray-600");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setMaxWidth(250);
        TailwindFX.apply(emailField, "rounded-lg", "border");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMaxWidth(250);
        TailwindFX.apply(passwordField, "rounded-lg", "border");

        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(250);
        TailwindFX.apply(loginButton, "btn-primary", "rounded-lg");

        Hyperlink forgotPassword = new Hyperlink("Forgot Password?");
        TailwindFX.apply(forgotPassword, "text-blue-600");

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
        TailwindFX.apply(root, "bg-gray-50", "border", "rounded-xl");

        Scene scene = new Scene(root, 450, 550);

        // Install TailwindFX framework styles
        TailwindFX.install(scene);

        stage.setTitle("TailwindFX Basic Login Example");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
