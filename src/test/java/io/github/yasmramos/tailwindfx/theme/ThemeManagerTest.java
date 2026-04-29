package io.github.yasmramos.tailwindfx.theme;

import io.github.yasmramos.tailwindfx.TailwindFX;
import io.github.yasmramos.tailwindfx.theme.ThemeManager;
import io.github.yasmramos.tailwindfx.theme.ThemeScopeManager;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tests for {@link ThemeManager} and {@link ThemeScopeManager}.
 */
public final class ThemeManagerTest {

    private ThemeManagerTest() {
    }

    private static int passed = 0, failed = 0;

    static void ok(String l) {
        System.out.printf("  ✅ %s%n", l);
        passed++;
    }

    static void fail(String l, String m) {
        System.out.printf("  ❌ %s — %s%n", l, m);
        failed++;
    }

    static void check(String l, boolean v) {
        if (v) {
            ok(l);
        } else {
            fail(l, "false");
    
        }}

    static void eq(String l, Object e, Object a) {
        if (e == null ? a == null : e.equals(a)) {
            ok(l);
        } else {
            fail(l, "expected <" + e + "> got <" + a + ">");
        }
    }

    static void throws_(String l, Class<? extends Throwable> t, Runnable r) {
        try {
            r.run();
            fail(l, "no throw");
        } catch (Throwable ex) {
            if (t.isInstance(ex)) {
                ok(l);
            } else {
                fail(l, ex.getClass().getSimpleName());
        
            }}
    }

    static void runFx(Runnable w) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                w.run();
            } catch (Throwable t) {
                err.set(t);
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(3, TimeUnit.SECONDS)) {
            throw new RuntimeException("timeout");
        }
        if (err.get() != null) {
            throw new RuntimeException(err.get());
        }
    }

    public static boolean runAll() throws Exception {
        passed = 0;
        failed = 0;
        System.out.println("\n── ThemeManager ──");

        testAvailableThemes();
        testPresetThrows();
        testPresetDark();
        testPresetLight();
        testToggleDark();
        testIsDark();
        testReset();
        testSaveLoadTheme();
        testScopeApply();
        testScopeClear();
        testScopeCopy();
        testScopeHas();
        testScopePresetDark();
        testFindClosestScope();
        testInheritScope();

        System.out.printf("  %d passed, %d failed%n", passed, failed);
        return failed == 0;
    }

    // ── ThemeManager ────────────────────────────────────────────────────
    static void testAvailableThemes() throws Exception {
        runFx(() -> {
            List<String> themes = ThemeManager.availableThemes();
            check("themes not empty", themes != null && !themes.isEmpty());
            check("contains dark", themes.contains("dark"));
            check("contains light", themes.contains("light"));
            check("contains blue", themes.contains("blue"));
        });
    }

    static void testPresetThrows() throws Exception {
        runFx(() -> {
            StackPane root = new StackPane();
            Scene scene = new Scene(root, 400, 300);
            throws_("preset(null)", IllegalArgumentException.class,
                    () -> TailwindFX.theme(scene).preset(null));
            throws_("preset(blank)", IllegalArgumentException.class,
                    () -> TailwindFX.theme(scene).preset("   "));
            throws_("preset(unknown)", IllegalArgumentException.class,
                    () -> TailwindFX.theme(scene).preset("nope"));
        });
    }

    static void testPresetDark() throws Exception {
        runFx(() -> {
            StackPane root = new StackPane();
            Scene scene = new Scene(root, 400, 300);
            TailwindFX.theme(scene).dark().apply();
            check("dark class applied", root.getStyleClass().contains("dark"));
            check("dark has -fx-base", root.getStyle().contains("-fx-base"));
        });
    }

    static void testPresetLight() throws Exception {
        runFx(() -> {
            StackPane root = new StackPane();
            Scene scene = new Scene(root, 400, 300);
            TailwindFX.theme(scene).dark().apply();
            TailwindFX.theme(scene).light().apply();
            check("dark class removed", !root.getStyleClass().contains("dark"));
        });
    }

    static void testToggleDark() throws Exception {
        runFx(() -> {
            StackPane root = new StackPane();
            Scene scene = new Scene(root, 400, 300);
            boolean before = ThemeManager.isDark(scene);
            ThemeManager.toggle(scene);
            boolean after = ThemeManager.isDark(scene);
            check("toggle changes dark state", before != after);
            ThemeManager.toggle(scene); // restore
        });
    }

    static void testIsDark() throws Exception {
        runFx(() -> {
            StackPane root = new StackPane();
            Scene scene = new Scene(root, 400, 300);
            TailwindFX.theme(scene).light().apply();
            check("light → isDark=false", !ThemeManager.isDark(scene));
            TailwindFX.theme(scene).dark().apply();
            check("dark → isDark=true", ThemeManager.isDark(scene));
        });
    }

    static void testReset() throws Exception {
        runFx(() -> {
            StackPane root = new StackPane();
            Scene scene = new Scene(root, 400, 300);
            TailwindFX.theme(scene).dark().apply();
            ThemeManager.forScene(scene).reset();
            // After reset: no more scoped style, no dark class
            check("reset: no dark class", !root.getStyleClass().contains("dark"));
        });
    }

    static void testSaveLoadTheme() throws Exception {
        runFx(() -> {
            StackPane root = new StackPane();
            Scene scene = new Scene(root, 400, 300);
            TailwindFX.theme(scene).dark().apply();
            TailwindFX.saveTheme(scene, "test.theme");

            // Load into fresh scene
            StackPane root2 = new StackPane();
            Scene scene2 = new Scene(root2, 400, 300);
            boolean loaded = TailwindFX.loadTheme(scene2, "test.theme");
            check("loadTheme returns true", loaded);
            // Clean up
            TailwindFX.deleteTheme("test.theme");
        });
    }

    // ── ThemeScopeManager ───────────────────────────────────────────────
    static void testScopeApply() throws Exception {
        runFx(() -> {
            StackPane pane = new StackPane();
            ThemeScopeManager.scope(pane).dark().apply();
            check("scope: dark class", pane.getStyleClass().contains("dark"));
            check("scope: has -fx-base", pane.getStyle().contains("-fx-base"));
            check("scope: is scoped", ThemeScopeManager.hasScope(pane));
        });
    }

    static void testScopeClear() throws Exception {
        runFx(() -> {
            StackPane pane = new StackPane();
            ThemeScopeManager.scope(pane).dark().apply();
            ThemeScopeManager.clearScope(pane);
            check("clear: not scoped", !ThemeScopeManager.hasScope(pane));
            check("clear: no dark class", !pane.getStyleClass().contains("dark"));
            // Style should not contain -fx-base after clear (theme vars removed)
            check("clear: no -fx-base", !pane.getStyle().contains("-fx-base"));
        });
    }

    static void testScopeCopy() throws Exception {
        runFx(() -> {
            StackPane source = new StackPane();
            StackPane target = new StackPane();
            ThemeScopeManager.scope(source).preset("blue").apply();
            ThemeScopeManager.copyScope(source, target);
            check("copy: target is scoped", ThemeScopeManager.hasScope(target));
            check("copy: target has accent",
                    target.getStyle().contains("-fx-accent"));
        });
    }

    static void testScopeHas() throws Exception {
        runFx(() -> {
            StackPane pane = new StackPane();
            check("before apply: not scoped", !ThemeScopeManager.hasScope(pane));
            ThemeScopeManager.scope(pane).light().apply();
            check("after apply: is scoped", ThemeScopeManager.hasScope(pane));
        });
    }

    static void testScopePresetDark() throws Exception {
        runFx(() -> {
            StackPane pane = new StackPane();
            TailwindFX.scope(pane).preset("dark").apply();
            check("scope dark: has -fx-base",
                    pane.getStyle().contains("-fx-base"));
            check("scope dark: dark class",
                    pane.getStyleClass().contains("dark"));
        });
    }

    // ── Nested scope / ancestor ──────────────────────────────────────────
    static void testFindClosestScope() throws Exception {
        runFx(() -> {
            StackPane outer = new StackPane();
            StackPane inner = new StackPane();
            Region child = new Region();
            outer.getChildren().add(inner);
            inner.getChildren().add(child);

            // No scope yet
            check("no scope: findClosest=null",
                    TailwindFX.findClosestScope(child) == null);

            // Apply scope to outer
            ThemeScopeManager.scope(outer).dark().apply();
            Pane closest = TailwindFX.findClosestScope(child);
            check("child finds outer scope", closest == outer);

            // Apply scope to inner (closer)
            ThemeScopeManager.scope(inner).light().apply();
            Pane closestNow = TailwindFX.findClosestScope(child);
            check("child finds inner scope (closer)", closestNow == inner);
        });
    }

    static void testInheritScope() throws Exception {
        runFx(() -> {
            StackPane panel = new StackPane();
            Region trigger = new Region();
            panel.getChildren().add(trigger);
            ThemeScopeManager.scope(panel).preset("rose").apply();

            StackPane modal = new StackPane();
            TailwindFX.inheritScope(trigger, modal);
            check("modal inherits panel scope", ThemeScopeManager.hasScope(modal));
        });
    }
}
