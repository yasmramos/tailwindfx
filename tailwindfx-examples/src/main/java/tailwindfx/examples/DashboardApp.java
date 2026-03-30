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
        Scene scene = new Scene(dashboard, 1400, 900);
        TailwindFX.install(scene);

        // Configure stage
        primaryStage.setTitle("TailwindFX — Complete Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Show features info
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║     TailwindFX Dashboard Demo          ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Features demonstrated:");
        System.out.println("  ✓ Navigation sidebar with menu");
        System.out.println("  ✓ Top bar with search & notifications");
        System.out.println("  ✓ Theme toggle (dark/light mode)");
        System.out.println("  ✓ Stats cards with trends");
        System.out.println("  ✓ Chart section placeholder");
        System.out.println("  ✓ Recent activity feed");
        System.out.println("  ✓ Data table with status badges");
        System.out.println("  ✓ Product cards grid");
        System.out.println("  ✓ Layout builders (HBox, VBox, GridPane)");
        System.out.println("  ✓ Color utilities (bg-*, text-*)");
        System.out.println("  ✓ Spacing utilities (p-*, m-*, gap-*)");
        System.out.println("  ✓ Shadows and effects");
        System.out.println("  ✓ Components (cards, badges, buttons, avatars)");
        System.out.println();
        System.out.println("Click the ☀️/🌙 button to toggle dark mode!");
        System.out.println();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
