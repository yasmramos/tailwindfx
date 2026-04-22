package tailwindfx.examples;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tailwindfx.TailwindFX;

/**
 * Dashboard Demo Application.
 *
 * Run this to see TailwindFX capabilities in action.
 *
 * Usage:
 *   mvn exec:java -Dexec.mainClass=tailwindfx.examples.DashboardApp
 */
public class DashboardApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create complete dashboard with sidebar
        BorderPane dashboard = Dashboard.create();

        // Scene with TailwindFX
        Scene scene = new Scene(dashboard, 1500, 1000);
        TailwindFX.installAll(scene, primaryStage);

        // Configure stage
        primaryStage.setTitle("TailwindFX — Advanced Dashboard");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Show welcome toast
        javafx.application.Platform.runLater(() -> {
            DashboardComponents.showToast(
                "Welcome to the Dashboard! 🎉",
                DashboardComponents.ToastType.SUCCESS);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
