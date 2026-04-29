package io.github.yasmramos.tailwindfx.examples;

import io.github.yasmramos.tailwindfx.TailwindFX;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BasicApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        
        // Left panel with examples list
        VBox examplesList = new VBox(10);
        examplesList.getStyleClass().addAll("bg-gray-100", "p-5");
        examplesList.setPrefWidth(250);
        
        Label title = new Label("TailwindFX Examples");
        title.getStyleClass().addAll("text-xl", "font-bold", "mb-4", "text-gray-800");
        
        // Buttons for each example
        Button btnBasicDashboard = new Button("Basic Dashboard");
        btnBasicDashboard.getStyleClass().addAll("w-full", "text-left", "px-4", "py-2", "rounded-lg", "hover:bg-gray-200", "transition", "duration-200");
        btnBasicDashboard.setMaxWidth(Double.MAX_VALUE);
        
        Button btnBasicLogin = new Button("Basic Login");
        btnBasicLogin.getStyleClass().addAll("w-full", "text-left", "px-4", "py-2", "rounded-lg", "hover:bg-gray-200", "transition", "duration-200");
        btnBasicLogin.setMaxWidth(Double.MAX_VALUE);
        
        
        // Configure actions
        btnBasicDashboard.setOnAction(e -> launchExample(BasicDashboardExample.class));
        btnBasicLogin.setOnAction(e -> launchExample(BasicLoginExample.class));
        
        examplesList.getChildren().addAll(
            title, 
            btnBasicDashboard, 
            btnBasicLogin
        );
        
        // Center panel with instructions
        VBox centerContent = new VBox(20);
        centerContent.getStyleClass().addAll("p-8", "bg-white");
        Label instruction = new Label("Click any example on the left to run it");
        instruction.getStyleClass().addAll("text-gray-600", "text-lg");
        centerContent.getChildren().add(instruction);
        
        // Main layout
        BorderPane root = new BorderPane();
        root.setLeft(examplesList);
        root.setCenter(centerContent);
        root.getStyleClass().addAll("bg-gray-50");
        
        Scene scene = new Scene(root, 1100, 700);
        TailwindFX.install(scene);
        
        primaryStage.setTitle("TailwindFX - Examples Gallery");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void launchExample(Class<? extends Application> exampleClass) {
        try {
            Application example = exampleClass.getDeclaredConstructor().newInstance();
            Stage newStage = new Stage();
            example.start(newStage);
            newStage.setTitle(exampleClass.getSimpleName());
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}