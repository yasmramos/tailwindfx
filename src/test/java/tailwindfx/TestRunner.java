
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;

import tailwindfx.*;

/**
 * Master test runner for TailwindFX.
 *
 * <p>
 * Runs all test suites in dependency order. Suites that require the JavaFX
 * Application Thread run after the platform is started.
 *
 * <h3>Usage</h3>
 * <pre>
 * // From terminal (after compiling):
 * java -cp . tailwindfx.TestRunner
 *
 * // Or just run the main() in your IDE.
 * </pre>
 *
 * <h3>Suite inventory</h3>
 * <table>
 * <tr><th>Suite</th><th>Class under test</th><th>FX
 * thread?</th><th>Tests</th></tr>
 * <tr><td>TailwindFXTest</td><td>ColorPalette, StyleToken, JitCompiler,
 * StyleMerger, UtilityConflictResolver, StylePerf, TailwindFXMetrics,
 * Preconditions, FxLayout.justifyPositions</td>
 * <td>No</td><td>~80</td></tr>
 * <tr><td>FxFlexPaneTest</td><td>FxFlexPane layout engine</td>
 * <td>No</td><td>25</td></tr>
 * <tr><td>AnimationUtilTest</td><td>AnimationUtil, AnimationRegistry</td>
 * <td>Yes</td><td>27</td></tr>
 * <tr><td>StylesTest</td><td>Styles (GridPane, margin, z, filter, skew)</td>
 * <td>Yes</td><td>25</td></tr>
 * <tr><td>FxLayoutTest</td><td>FxLayout builder</td>
 * <td>Yes</td><td>15</td></tr>
 * <tr><td>ThemeManagerTest</td><td>ThemeManager + ThemeScopeManager</td>
 * <td>Yes</td><td>15</td></tr>
 * <tr><td>BreakpointManagerTest</td><td>BreakpointManager</td>
 * <td>Yes</td><td>15</td></tr>
 * <tr><td>ResponsiveNodeTest</td><td>ResponsiveNode</td>
 * <td>Yes</td><td>15</td></tr>
 * <tr><td>TailwindFXIntegrationTest</td><td>TailwindFX entry point</td>
 * <td>Yes</td><td>27</td></tr>
 * </table>
 */
public class TestRunner extends Application {

    private static int totalPassed = 0;
    private static int totalFailed = 0;
    private static final CountDownLatch DONE = new CountDownLatch(1);

    @Override
    public void start(Stage primaryStage) {
        // Run FX-thread suites on the FX thread (we're already on it)
        Platform.runLater(() -> {
            try {
                runFxSuites();
            } finally {
                DONE.countDown();
                Platform.exit();
            }
        });
    }

    // =========================================================================
    // Pure-Java suites (no FX thread needed)
    // =========================================================================
    static void runPureJavaSuites() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║   PURE JAVA SUITES (no FX thread)        ║");
        System.out.println("╚══════════════════════════════════════════╝");

        run("TailwindFXTest", () -> {
            boolean ok = TailwindFXTest.runAll();
            return ok;
        });

        run("FxFlexPaneTest", () -> FxFlexPaneTest.runAll());
    }

    // =========================================================================
    // FX-thread suites
    // =========================================================================
    static void runFxSuites() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║   FX THREAD SUITES                        ║");
        System.out.println("╚══════════════════════════════════════════╝");

        runAsync("AnimationUtilTest", () -> AnimationUtilTest.runAll());
        runAsync("StylesTest", () -> StylesTest.runAll());
        runAsync("FxLayoutTest", () -> FxLayoutTest.runAll());
        runAsync("ThemeManagerTest", () -> ThemeManagerTest.runAll());
        runAsync("BreakpointManagerTest", () -> BreakpointManagerTest.runAll());
        runAsync("ResponsiveNodeTest", () -> ResponsiveNodeTest.runAll());
        runAsync("FxDataTableTest", () -> FxDataTableTest.runAll());
        runAsync("TailwindFXIntegrationTest", () -> TailwindFXIntegrationTest.runAll());
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    @FunctionalInterface
    interface Suite {

        boolean run() throws Exception;
    }

    static void run(String name, Suite suite) {
        try {
            boolean ok = suite.run();
            if (!ok) {
                totalFailed++; 
            }else {
                totalPassed++;
            }
        } catch (Exception e) {
            System.out.println("  💥 " + name + " CRASHED: " + e.getMessage());
            totalFailed++;
        }
    }

    /**
     * Runs a suite synchronously on the current thread (must be FX thread).
     */
    static void runAsync(String name, Suite suite) {
        try {
            boolean ok = suite.run();
            if (!ok) {
                totalFailed++; 
            }else {
                totalPassed++;
            }
        } catch (Exception e) {
            System.out.println("  💥 " + name + " CRASHED: " + e.getMessage());
            e.printStackTrace();
            totalFailed++;
        }
    }

    // =========================================================================
    // Entry point
    // =========================================================================
    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║        TailwindFX — Full Test Run         ║");
        System.out.println("╚══════════════════════════════════════════╝");

        // 1. Pure-Java suites — run directly on the main thread
        runPureJavaSuites();

        // 2. FX-thread suites — launch JavaFX and run inside start()
        new Thread(() -> Application.launch(TestRunner.class, args)).start();
        DONE.await();

        // 3. Final summary
        System.out.println();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.printf("║  TOTAL SUITES: %2d passed, %2d failed       ║%n",
                totalPassed, totalFailed);
        System.out.println("╚══════════════════════════════════════════╝");

        System.exit(totalFailed == 0 ? 0 : 1);
    }
}
