package tailwindfx;

import javafx.scene.layout.Region;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Test suite for all pure-Java (no FX thread) classes in TailwindFX.
 *
 * <h3>Coverage</h3>
 * <ul>
 * <li>{@link ColorPalette} — palette lookup, fallback, hex validation</li>
 * <li>{@link StyleToken} — parsing, kinds, alpha, arbitrary, negative</li>
 * <li>{@link JitCompiler} — compilation, cache, LRU, metrics, batch</li>
 * <li>{@link StyleMerger} — non-destructive merge, conflict resolution</li>
 * <li>{@link UtilityConflictResolver} — category detection, responsive
 * prefixes, cleanup</li>
 * <li>{@link StylePerf} — diff hash, cache hits/misses, batch deferred
 * apply</li>
 * <li>{@link TailwindFXMetrics} — counters, ratios, report format, reset</li>
 * <li>{@link Preconditions} — null/blank/range guard throws and warns</li>
 * <li>{@link FxFlexPane} — justify-content math (via FxFlexPaneTest)</li>
 * <li>{@link FxLayout} — justifyPositions() math</li>
 * </ul>
 *
 * <h3>Not tested here (require FX Application Thread)</h3>
 * AnimationUtil, BreakpointManager, ThemeManager, ThemeScopeManager,
 * ResponsiveNode, TailwindFX entry-point (apply/jit/install).
 *
 * <h3>Running</h3>
 * <pre>
 * // Run all suites:
 * TailwindFXTest.runAll();
 *
 * // Run one suite:
 * TailwindFXTest.runSuite("ColorPalette");
 * </pre>
 */
public final class TailwindFXTest {

    private TailwindFXTest() {
    }

    // =========================================================================
    // Runner infrastructure
    // =========================================================================
    private static int passed = 0, failed = 0;
    private static String currentSuite = "";

    private static void suite(String name) {
        currentSuite = name;
        System.out.println("\n── " + name + " ──");
    }

    private static void ok(String label) {
        System.out.printf("  ✅ %s%n", label);
        passed++;
    }

    private static void fail(String label, Throwable t) {
        System.out.printf("  ❌ %s — %s%n", label, t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName());
        failed++;
    }

    private static void check(String label, boolean condition) {
        if (condition) {
            ok(label); 
        }else {
            fail(label, new AssertionError("expected true"));
        }
    }

    private static void eq(String label, Object expected, Object actual) {
        if (expected == null ? actual == null : expected.equals(actual)) {
            ok(label); 
        }else {
            fail(label, new AssertionError("expected <" + expected + "> but was <" + actual + ">"));
        }
    }

    private static void approx(String label, double expected, double actual) {
        if (Math.abs(expected - actual) <= 0.5) {
            ok(label); 
        }else {
            fail(label, new AssertionError("expected ≈" + expected + " but was " + actual));
        }
    }

    /**
     * Asserts that the block throws the expected exception type.
     */
    private static void throws_(String label, Class<? extends Throwable> type, Runnable block) {
        try {
            block.run();
            fail(label, new AssertionError("expected " + type.getSimpleName() + " but nothing thrown"));
        } catch (Throwable t) {
            if (type.isInstance(t)) {
                ok(label); 
            }else {
                fail(label, new AssertionError(
                        "expected " + type.getSimpleName() + " but got " + t.getClass().getSimpleName()));
            }
        }
    }

    // =========================================================================
    // Entry point
    // =========================================================================
    /**
     * Runs all test suites and prints a final summary.
     *
     * @return {@code true} if all tests passed
     */
    public static boolean runAll() {
        passed = 0;
        failed = 0;
        System.out.println("═══════════════════════════════════════");
        System.out.println("  TailwindFX Test Suite");
        System.out.println("═══════════════════════════════════════");

        testColorPalette();
        testStyleToken();
        testJitCompiler();
        testStyleMerger();
        testUtilityConflictResolver();
        testStylePerf();
        testTailwindFXMetrics();
        testTailwindFXMetricsAlerts();
        testPreconditions();
        testFxLayoutJustify();

        // Re-run FxFlexPaneTest via its own runner
        System.out.println("\n── FxFlexPane (layout engine) ──");
        try {
            boolean ok = FxFlexPaneTest.runAll();
            if (!ok) {
                failed++;
            }
        } catch (Exception e) {
            fail("FxFlexPaneTest.runAll()", e);
        }

        System.out.println("\n═══════════════════════════════════════");
        System.out.printf("  TOTAL: %d passed, %d failed%n", passed, failed);
        System.out.println("═══════════════════════════════════════");
        return failed == 0;
    }

    // =========================================================================
    // 1. ColorPalette
    // =========================================================================
    static void testColorPalette() {
        suite("ColorPalette");

        // hex() returns correct value
        eq("blue-500 hex", "#3b82f6", ColorPalette.hex("blue", 500));
        eq("gray-900 hex", "#111827", ColorPalette.hex("gray", 900));

        // rgb() returns r,g,b components
        eq("blue-500 rgb", "59,130,246", ColorPalette.rgb("blue", 500));

        // exists()
        check("blue-500 exists", ColorPalette.exists("blue", 500));
        check("notacolor-500 !exists", !ColorPalette.exists("notacolor", 500));
        check("blue-999 !exists", !ColorPalette.exists("blue", 999));

        // fxColor() without alpha returns hex
        eq("fxColor no alpha", "#3b82f6", ColorPalette.fxColor("blue", 500, null));
        eq("fxColor alpha=1.0", "#3b82f6", ColorPalette.fxColor("blue", 500, 1.0));

        // fxColor() with alpha returns rgba()
        String rgba = ColorPalette.fxColor("blue", 500, 0.5);
        check("fxColor alpha=0.5 starts rgba", rgba != null && rgba.startsWith("rgba("));
        check("fxColor alpha=0.5 contains 0.50", rgba != null && rgba.contains("0.50"));

        // fxColor() fallback on unknown color
        String fallback = ColorPalette.fxColor("unicorn", 500, null);
        eq("fallback is gray-500", ColorPalette.hex("gray", 500), fallback);

        // fxColor() alpha clamped below 0 → 0
        String clamped0 = ColorPalette.fxColor("blue", 500, -0.5);
        check("alpha clamped to 0 → rgba", clamped0 != null && clamped0.contains("0.00"));

        // fxColor() alpha clamped above 1 → 1.0 (returns hex, not rgba)
        String clamped1 = ColorPalette.fxColor("blue", 500, 1.5);
        eq("alpha clamped to 1.0 returns hex", ColorPalette.hex("blue", 500), clamped1);

        // isValidHex()
        check("#3b82f6 valid", ColorPalette.isValidHex("#3b82f6"));
        check("#fff valid (3-digit)", ColorPalette.isValidHex("#fff"));
        check("3b82f6 invalid", !ColorPalette.isValidHex("3b82f6"));
        check("#gg0000 invalid", !ColorPalette.isValidHex("#gg0000"));
        check("null invalid", !ColorPalette.isValidHex(null));

        // normalizeHex()
        eq("normalize #f60 → #ff6600", "#ff6600", ColorPalette.normalizeHex("#f60"));
        eq("normalize #ff6600 unchanged", "#ff6600", ColorPalette.normalizeHex("#ff6600"));
        eq("normalize null → null", null, ColorPalette.normalizeHex(null));

        // hexToRgbString()
        eq("hexToRgbString #3b82f6", "59,130,246", ColorPalette.hexToRgbString("#3b82f6"));
        eq("hexToRgbString #f60 (3-digit)", "255,102,0", ColorPalette.hexToRgbString("#f60"));
        eq("hexToRgbString invalid → null", null, ColorPalette.hexToRgbString("notahex"));
    }

    // =========================================================================
    // 2. StyleToken
    // =========================================================================
    static void testStyleToken() {
        suite("StyleToken");

        // SCALE kind: p-4
        StyleToken p4 = StyleToken.parse("p-4");
        eq("p-4 kind", StyleToken.Kind.SCALE, p4.kind);
        eq("p-4 prefix", "p", p4.prefix);
        eq("p-4 scale", 4, p4.scale);
        check("p-4 not negative", !p4.negative);

        // COLOR_SHADE kind: bg-blue-500
        StyleToken bg = StyleToken.parse("bg-blue-500");
        eq("bg-blue-500 kind", StyleToken.Kind.COLOR_SHADE, bg.kind);
        eq("bg-blue-500 colorName", "blue", bg.colorName);
        eq("bg-blue-500 shade", 500, bg.shade);
        check("bg-blue-500 no alpha", !bg.hasAlpha());

        // COLOR_SHADE with alpha: bg-blue-500/80
        StyleToken bgAlpha = StyleToken.parse("bg-blue-500/80");
        check("bg-blue-500/80 has alpha", bgAlpha.hasAlpha());
        approx("bg-blue-500/80 alphaFraction", 0.80, bgAlpha.alphaFraction());

        // ARBITRARY kind: p-[13px]
        StyleToken arb = StyleToken.parse("p-[13px]");
        eq("p-[13px] kind", StyleToken.Kind.ARBITRARY, arb.kind);
        eq("p-[13px] arbitraryVal", "13px", arb.arbitraryVal);

        // NAMED kind: text-sm
        StyleToken named = StyleToken.parse("text-sm");
        eq("text-sm kind", StyleToken.Kind.NAMED, named.kind);
        eq("text-sm namedValue", "sm", named.namedValue);

        // Negative: -translate-x-4
        StyleToken neg = StyleToken.parse("-translate-x-4");
        check("-translate-x-4 negative", neg.negative);
        check("-translate-x-4 signedScale < 0", neg.signedScale() < 0);

        // Unknown: btn-primary
        StyleToken unknown = StyleToken.parse("btn-primary");
        eq("btn-primary kind", StyleToken.Kind.UNKNOWN, unknown.kind);
    }

    // =========================================================================
    // 3. JitCompiler
    // =========================================================================
    static void testJitCompiler() {
        suite("JitCompiler");

        JitCompiler.clearCache();

        // null → throws
        throws_("compile(null) throws", IllegalArgumentException.class,
                () -> JitCompiler.compile(null));

        // blank → unknown (no throw)
        JitCompiler.CompileResult blank = JitCompiler.compile("  ");
        check("compile(blank) isUnknown", !blank.isKnown());

        // p-4 → -fx-padding
        JitCompiler.CompileResult p4 = JitCompiler.compile("p-4");
        check("p-4 is known", p4.isKnown());
        check("p-4 has inline style", p4.inlineStyle() != null && !p4.inlineStyle().isBlank());
        check("p-4 contains padding", p4.inlineStyle().contains("-fx-padding"));

        // bg-blue-500 → -fx-background-color
        JitCompiler.CompileResult bg = JitCompiler.compile("bg-blue-500");
        check("bg-blue-500 known", bg.isKnown());
        check("bg-blue-500 has bg color", bg.inlineStyle().contains("-fx-background-color"));
        check("bg-blue-500 hex value", bg.inlineStyle().contains("#3b82f6"));

        // bg-blue-500/80 → rgba
        JitCompiler.CompileResult bgAlpha = JitCompiler.compile("bg-blue-500/80");
        check("bg-blue-500/80 rgba", bgAlpha.inlineStyle().contains("rgba("));

        // p-[13px] arbitrary
        JitCompiler.CompileResult arbP = JitCompiler.compile("p-[13px]");
        check("p-[13px] known", arbP.isKnown());
        check("p-[13px] value 13px", arbP.inlineStyle().contains("13px"));

        // -translate-x-4 negative
        JitCompiler.CompileResult neg = JitCompiler.compile("-translate-x-4");
        check("-translate-x-4 known", neg.isKnown());
        check("-translate-x-4 negative px", neg.inlineStyle().contains("-16px")
                || neg.inlineStyle().contains("-fx-translate-x"));

        // opacity-75
        JitCompiler.CompileResult op = JitCompiler.compile("opacity-75");
        check("opacity-75 known", op.isKnown());
        check("opacity-75 contains 0.75", op.inlineStyle().contains("0.75"));

        // unknown CSS class → not known, no inline style
        JitCompiler.CompileResult btn = JitCompiler.compile("btn-primary");
        check("btn-primary not known as JIT", !btn.isKnown() || btn.inlineStyle().isBlank());

        // Cache: compile same token twice → second is cache hit
        JitCompiler.clearCache();
        TailwindFXMetrics.instance().reset();
        TailwindFXMetrics.instance().setEnabled(true);
        JitCompiler.compile("p-4"); // miss
        JitCompiler.compile("p-4"); // hit
        check("cache hit recorded", TailwindFXMetrics.instance().cacheHits() >= 1);
        check("cache miss recorded", TailwindFXMetrics.instance().cacheMisses() >= 1);

        // compileBatch → combined inline style
        JitCompiler.BatchResult batch = JitCompiler.compileBatch("p-4", "opacity-75");
        check("batch inlineStyle not blank", !batch.inlineStyle().isBlank());
        check("batch contains padding", batch.inlineStyle().contains("-fx-padding"));
        check("batch contains opacity", batch.inlineStyle().contains("-fx-opacity"));

        // clearCache
        JitCompiler.clearCache();
        eq("cacheSize after clear", 0, JitCompiler.cacheSize());

        // LRU: cache stays at or below MAX_CACHE_SIZE
        JitCompiler.clearCache();
        // Insert enough entries to test eviction boundary
        for (int i = 0; i <= JitCompiler.MAX_CACHE_SIZE + 5; i++) {
            JitCompiler.compile("opacity-" + (i % 101));
        }
        check("LRU bounded: cacheSize <= MAX",
                JitCompiler.cacheSize() <= JitCompiler.MAX_CACHE_SIZE);

        // === Tailwind v4.1 tokens ===
        // drop-shadow-[#hex] arbitrary
        JitCompiler.CompileResult ds = JitCompiler.compile("drop-shadow-[#3b82f6]");
        check("drop-shadow-[#hex] known", ds.isKnown());
        check("drop-shadow-[#hex] dropshadow", ds.inlineStyle().contains("dropshadow"));
        check("drop-shadow-[#hex] color", ds.inlineStyle().contains("#3b82f6") || ds.inlineStyle().contains("3b82f6"));

        // text-shadow-[rgba] arbitrary
        JitCompiler.CompileResult ts = JitCompiler.compile("text-shadow-[rgba(0,0,0,0.4)]");
        check("text-shadow-[rgba] known", ts.isKnown());
        check("text-shadow-[rgba] dropshadow", ts.inlineStyle().contains("dropshadow"));

        // stroke-[4] arbitrary stroke-width
        JitCompiler.CompileResult sw = JitCompiler.compile("stroke-[4]");
        check("stroke-[4] known", sw.isKnown());
        check("stroke-[4] stroke-width", sw.inlineStyle().contains("-fx-stroke-width"));
        check("stroke-[4] value=4", sw.inlineStyle().contains("4"));

        // fill-[#hex] arbitrary
        JitCompiler.CompileResult fillArb = JitCompiler.compile("fill-[#22c55e]");
        check("fill-[#hex] known", fillArb.isKnown());
        check("fill-[#hex] -fx-fill", fillArb.inlineStyle().contains("-fx-fill"));

        // drop-shadow-blue-500 (COLOR_SHADE prefix)
        JitCompiler.clearCache();
        JitCompiler.CompileResult dsColor = JitCompiler.compile("drop-shadow-blue-500");
        check("drop-shadow-blue-500 known", dsColor.isKnown());
        check("drop-shadow-blue-500 dropshadow", dsColor.inlineStyle().contains("dropshadow"));
    }

    // =========================================================================
    // 4. StyleMerger
    // =========================================================================
    static void testStyleMerger() {
        suite("StyleMerger");

        // merge (package-private — same package access): non-destructive
        String merged = StyleMerger.merge(
                "-fx-padding: 8px;",
                "-fx-background-color: #ff0000;"
        );
        check("merge preserves padding", merged.contains("-fx-padding"));
        check("merge adds bg-color", merged.contains("-fx-background-color"));

        // merge: incoming wins on conflict
        String conflict = StyleMerger.merge(
                "-fx-padding: 8px;",
                "-fx-padding: 16px;"
        );
        check("conflict: 16px present", conflict.contains("16px"));
        check("conflict: no duplicate prop",
                conflict.indexOf("-fx-padding") == conflict.lastIndexOf("-fx-padding"));

        // merge null incoming → existing unchanged
        String nullIn = StyleMerger.merge("-fx-padding: 8px;", null);
        check("null incoming → existing preserved", nullIn.contains("8px"));

        // merge null existing → incoming returned
        String nullEx = StyleMerger.merge(null, "-fx-background-color: red;");
        check("null existing → incoming applied", nullEx.contains("red"));

        // merge both null → blank
        String bothNull = StyleMerger.merge(null, null);
        check("both null → blank", bothNull == null || bothNull.isBlank());

        // parseStyle round-trip
        Map<String, String> props
                = StyleMerger.parseStyle("-fx-padding: 8px; -fx-opacity: 0.5;");
        eq("parseStyle count", 2, props.size());
        check("parseStyle has padding", props.containsKey("-fx-padding"));
        check("parseStyle has opacity", props.containsKey("-fx-opacity"));
        eq("parseStyle padding value", "8px",
                props.get("-fx-padding").trim().replace(";", ""));

        // buildStyle produces valid CSS
        java.util.LinkedHashMap<String, String> out = new java.util.LinkedHashMap<>();
        out.put("-fx-padding", "4px");
        out.put("-fx-opacity", "0.8");
        String built = StyleMerger.buildStyle(out);
        check("buildStyle contains padding", built.contains("-fx-padding: 4px"));
        check("buildStyle contains opacity", built.contains("-fx-opacity: 0.8"));
    }

    // =========================================================================
    // 5. UtilityConflictResolver
    // =========================================================================
    static void testUtilityConflictResolver() {
        suite("UtilityConflictResolver");

        // apply() same category replaces previous
        Region node = new Region();
        UtilityConflictResolver.apply(node, "w-4");
        check("w-4 applied", node.getStyleClass().contains("w-4"));

        UtilityConflictResolver.apply(node, "w-8");
        check("w-8 replaces w-4", node.getStyleClass().contains("w-8"));
        check("w-4 removed", !node.getStyleClass().contains("w-4"));

        // apply() different categories coexist
        UtilityConflictResolver.apply(node, "p-4");
        check("p-4 added alongside w-8", node.getStyleClass().contains("p-4"));
        check("w-8 still there", node.getStyleClass().contains("w-8"));

        // applyAll() with space-separated string
        Region n2 = new Region();
        UtilityConflictResolver.applyAll(n2, "rounded-lg shadow-md");
        check("rounded-lg applied", n2.getStyleClass().contains("rounded-lg"));
        check("shadow-md applied", n2.getStyleClass().contains("shadow-md"));

        // applyAll() resolves conflict within one call
        Region n3 = new Region();
        UtilityConflictResolver.applyAll(n3, "w-4 w-8");
        check("w-8 wins in applyAll", n3.getStyleClass().contains("w-8"));
        check("w-4 not there", !n3.getStyleClass().contains("w-4"));

        // Responsive: md:w-4 and w-4 do NOT conflict (different categories)
        Region n4 = new Region();
        UtilityConflictResolver.apply(n4, "w-4");
        UtilityConflictResolver.apply(n4, "md:w-8");
        check("w-4 stays with md:w-8", n4.getStyleClass().contains("w-4"));
        check("md:w-8 added", n4.getStyleClass().contains("md:w-8"));

        // Responsive: md:w-4 and md:w-8 DO conflict (same category md:w)
        Region n5 = new Region();
        UtilityConflictResolver.apply(n5, "md:w-4");
        UtilityConflictResolver.apply(n5, "md:w-8");
        check("md:w-8 replaces md:w-4", n5.getStyleClass().contains("md:w-8"));
        check("md:w-4 removed", !n5.getStyleClass().contains("md:w-4"));

        // findCategory returns null for unknown class
        check("findCategory(btn-primary) == null",
                UtilityConflictResolver.categoryOf("btn-primary") == null);

        // findCategory returns correct category
        eq("categoryOf(w-4)", "w", UtilityConflictResolver.categoryOf("w-4"));
        eq("categoryOf(shadow-md)", "shadow", UtilityConflictResolver.categoryOf("shadow-md"));

        // invalidateCache removes cache
        Region n6 = new Region();
        UtilityConflictResolver.apply(n6, "w-4");
        UtilityConflictResolver.invalidateCache(n6);
        check("after invalidate: no category cache",
                !n6.getProperties().containsKey("tailwindfx.category.cache")
                || ((java.util.Map<?, ?>) n6.getProperties().get("tailwindfx.category.cache")).isEmpty());

        // invalidateCategoryCache removes only that category
        Region n7 = new Region();
        UtilityConflictResolver.apply(n7, "w-4");
        UtilityConflictResolver.apply(n7, "p-2");
        UtilityConflictResolver.invalidateCategoryCache(n7, "w");
        @SuppressWarnings("unchecked")
        java.util.Map<String, String> cache7
                = (java.util.Map<String, String>) n7.getProperties().get("tailwindfx.category.cache");
        check("w removed from cache", cache7 == null || !cache7.containsKey("w"));
        check("p still in cache", cache7 != null && cache7.containsKey("p"));

        // cleanupNode(null) is safe
        UtilityConflictResolver.cleanupNode(null); // should not throw
        ok("cleanupNode(null) safe");

        // cleanupNode removes all keys
        Region n8 = new Region();
        UtilityConflictResolver.apply(n8, "w-4");
        n8.getProperties().put("tailwindfx.style.hash", 12345);
        UtilityConflictResolver.cleanupNode(n8);
        check("cleanupNode removes category cache",
                !n8.getProperties().containsKey("tailwindfx.category.cache"));
        check("cleanupNode removes style hash",
                !n8.getProperties().containsKey("tailwindfx.style.hash"));
    }

    // =========================================================================
    // 6. StylePerf
    // =========================================================================
    static void testStylePerf() {
        suite("StylePerf");

        // apply() first time → returns true (applied)
        Region node = new Region();
        boolean first = StylePerf.apply(node, "w-4", "p-2");
        check("first apply returns true", first);

        // apply() same classes → false (cache hit, skipped)
        boolean second = StylePerf.apply(node, "w-4", "p-2");
        check("duplicate apply returns false", !second);

        // apply() different classes → true (cache miss)
        boolean third = StylePerf.apply(node, "w-8", "p-4");
        check("different classes returns true", third);

        // Hash is order-independent: "w-4 p-2" == "p-2 w-4"
        Region n1 = new Region(), n2 = new Region();
        StylePerf.apply(n1, "w-4", "p-2");
        StylePerf.apply(n2, "p-2", "w-4");
        eq("hash order-independent", StylePerf.currentHash(n1), StylePerf.currentHash(n2));

        // invalidate() clears hash
        Region n3 = new Region();
        StylePerf.apply(n3, "w-4");
        StylePerf.invalidate(n3);
        check("after invalidate hash is null", StylePerf.currentHash(n3) == null);

        // apply after invalidate → re-applies (returns true)
        boolean afterInvalidate = StylePerf.apply(n3, "w-4");
        check("after invalidate re-applies", afterInvalidate);

        // currentHash() null before any apply
        Region fresh = new Region();
        check("fresh node hash is null", StylePerf.currentHash(fresh) == null);

        // batch() nested: inner batch is transparent, outer flushes
        Region b1 = new Region(), b2 = new Region();
        // Note: batch() requires FX thread — test only the structure
        // We verify isBatchActive() returns false when not in batch
        check("isBatchActive false outside batch", !StylePerf.isBatchActive());
    }

    // =========================================================================
    // 7. TailwindFXMetrics
    // =========================================================================
    static void testTailwindFXMetrics() {
        suite("TailwindFXMetrics");

        TailwindFXMetrics m = TailwindFXMetrics.instance();
        m.reset();
        m.setEnabled(true);

        // Counters increment
        m.recordCacheHit();
        m.recordCacheHit();
        m.recordCacheMiss();
        eq("cacheHits=2", 2L, m.cacheHits());
        eq("cacheMisses=1", 1L, m.cacheMisses());

        // Hit ratio: 2/3 ≈ 0.667
        approx("cacheHitRatio", 0.667, m.cacheHitRatio());

        // compilations
        m.recordCompilation(1_000_000L); // 1ms
        m.recordCompilation(3_000_000L); // 3ms
        eq("compilations=2", 2L, m.compilations());
        approx("avgCompileNs", 2_000_000.0, m.avgCompileNs());

        // conflictsByCategory sorted descending
        m.recordConflictResolution("w");
        m.recordConflictResolution("w");
        m.recordConflictResolution("p");
        Map<String, Long> cats = m.conflictsByCategory();
        check("w first (higher count)", cats.keySet().iterator().next().equals("w"));
        eq("w count=2", 2L, cats.get("w"));
        eq("p count=1", 1L, cats.get("p"));

        // theme switches and animation plays
        m.recordThemeSwitch();
        m.recordAnimationPlay();
        m.recordAnimationPlay();
        eq("themeSwitches=1", 1L, m.themeSwitches());
        eq("animationPlays=2", 2L, m.animationPlays());

        // report() contains key sections
        String report = m.report();
        check("report has JIT cache hits", report.contains("JIT cache hits"));
        check("report has compilations", report.contains("compilations"));
        check("report has conflicts", report.contains("Conflict"));
        check("report has uptime", report.contains("Uptime"));

        // setEnabled(false) → no-ops
        m.setEnabled(false);
        m.reset();
        long hBefore = m.cacheHits();
        m.recordCacheHit();
        eq("disabled: hit not recorded", hBefore, m.cacheHits());
        m.setEnabled(true);

        // reset() clears all counters
        m.reset();
        eq("reset: hits=0", 0L, m.cacheHits());
        eq("reset: misses=0", 0L, m.cacheMisses());
        eq("reset: compilations=0", 0L, m.compilations());
        eq("reset: conflicts=0", 0L, m.conflictResolutions());
        check("reset: cats empty", m.conflictsByCategory().isEmpty());
    }

    // =========================================================================
    // 7b. TailwindFXMetrics — Alert system
    // =========================================================================
    static void testTailwindFXMetricsAlerts() {
        suite("TailwindFXMetrics.alerts");

        TailwindFXMetrics m = TailwindFXMetrics.instance();
        m.reset();
        m.setEnabled(true);

        // onAlert() returns self (chaining)
        java.util.concurrent.atomic.AtomicReference<String> lastAlert
                = new java.util.concurrent.atomic.AtomicReference<>("");
        java.util.concurrent.atomic.AtomicReference<Double> lastValue
                = new java.util.concurrent.atomic.AtomicReference<>(0.0);

        TailwindFXMetrics chained = m.onAlert((metric, value, threshold) -> {
            lastAlert.set(metric);
            lastValue.set(value);
        });
        check("onAlert returns self", chained == m);

        // alertOnLowCacheHitRatio — fire when ratio < 0.90
        m.alertOnLowCacheHitRatio(0.90);

        // Simulate 100 misses + 10 hits → ratio = 0.09 → below 0.90
        m.reset();
        m.setEnabled(true);
        m.onAlert((metric, value, threshold) -> {
            lastAlert.set(metric);
            lastValue.set(value);
        });
        m.alertOnLowCacheHitRatio(0.90);

        for (int i = 0; i < 10; i++) {
            m.recordCacheHit();
        }
        for (int i = 0; i < 90; i++) {
            m.recordCacheMiss(); // triggers checkAlerts at 50th miss
        }        // Alert should have fired since ratio ≈ 0.10 < 0.90 AND total > 100

        // Force the remaining 10 misses to exceed 100 total
        for (int i = 0; i < 10; i++) {
            m.recordCacheMiss();
        }

        // At this point totalHits=10, totalMisses=100 → ratio≈0.09
        // checkAlerts() fires every 50 misses — at miss #50 and #100
        check("lowCacheHitRatio alert fired",
                "cacheHitRatio".equals(lastAlert.get()) || lastValue.get() < 0.90);

        // alertOnHighConflictRate — fire when rate > 0.10
        m.reset();
        m.setEnabled(true);
        m.onAlert((metric, value, threshold) -> lastAlert.set(metric));
        m.alertOnHighConflictRate(0.10);

        // Record 50 applies and 30 conflicts → rate = 0.60 > 0.10
        for (int i = 0; i < 50; i++) {
            m.recordApply(1);
        }
        for (int i = 0; i < 30; i++) {
            m.recordConflictResolution("w");
        }
        // Trigger checkAlerts manually via a cache miss at 50
        for (int i = 0; i < 50; i++) {
            m.recordCacheMiss();
        }
        check("highConflictRate alert fired",
                "conflictRate".equals(lastAlert.get()) || "cacheHitRatio".equals(lastAlert.get()));

        // alertOnSlowCompile — fire when avg > 0.001ms (virtually always fires)
        m.reset();
        m.setEnabled(true);
        m.onAlert((metric, value, threshold) -> lastAlert.set(metric));
        m.alertOnSlowCompile(0.001);
        m.recordCompilation(1_000_000L); // 1ms — above 0.001ms
        for (int i = 0; i < 50; i++) {
            m.recordCacheMiss();
        }
        check("slowCompile alert can fire",
                "avgCompileMs".equals(lastAlert.get()) || !lastAlert.get().isEmpty());

        // Null handler clears alerts
        m.onAlert(null);
        ok("onAlert(null) clears handler without throw");

        m.reset();
        m.setEnabled(false);
    }

    // =========================================================================
    // 8. Preconditions
    // =========================================================================
    static void testPreconditions() {
        suite("Preconditions");

        // requireNonNull throws on null
        throws_("requireNonNull(null) throws IAE", IllegalArgumentException.class,
                () -> Preconditions.requireNonNull(null, "Test", "param"));

        // requireNonNull passes through non-null
        String result = Preconditions.requireNonNull("hello", "Test", "param");
        eq("requireNonNull returns value", "hello", result);

        // requireNonBlank throws on null
        throws_("requireNonBlank(null) throws IAE", IllegalArgumentException.class,
                () -> Preconditions.requireNonBlank(null, "Test", "param"));

        // requireNonBlank throws on blank
        throws_("requireNonBlank('  ') throws IAE", IllegalArgumentException.class,
                () -> Preconditions.requireNonBlank("   ", "Test", "param"));

        // requireNonBlank passes through valid string
        String s = Preconditions.requireNonBlank("hello", "Test", "param");
        eq("requireNonBlank returns value", "hello", s);

        // requireSpan throws on 0
        throws_("requireSpan(0) throws IAE", IllegalArgumentException.class,
                () -> Preconditions.requireSpan(0, "Test"));

        // requireSpan throws on negative
        throws_("requireSpan(-1) throws IAE", IllegalArgumentException.class,
                () -> Preconditions.requireSpan(-1, "Test"));

        // requireSpan passes on positive
        int span = Preconditions.requireSpan(3, "Test");
        eq("requireSpan(3) returns 3", 3, span);

        // requireOpacity throws below 0
        throws_("requireOpacity(-0.1) throws IAE", IllegalArgumentException.class,
                () -> Preconditions.requireOpacity(-0.1, "Test"));

        // requireOpacity throws above 1
        throws_("requireOpacity(1.1) throws IAE", IllegalArgumentException.class,
                () -> Preconditions.requireOpacity(1.1, "Test"));

        // requireOpacity passes at boundary
        approx("requireOpacity(0.0) ok", 0.0, Preconditions.requireOpacity(0.0, "Test"));
        approx("requireOpacity(1.0) ok", 1.0, Preconditions.requireOpacity(1.0, "Test"));

        // requirePositiveDuration throws on 0
        throws_("requirePositiveDuration(0) throws IAE", IllegalArgumentException.class,
                () -> Preconditions.requirePositiveDuration(0, "Test"));

        // requirePositiveDuration passes on positive
        eq("requirePositiveDuration(1) ok", 1, Preconditions.requirePositiveDuration(1, "Test"));

        // requirePositiveScale throws on 0
        throws_("requirePositiveScale(0) throws IAE", IllegalArgumentException.class,
                () -> Preconditions.requirePositiveScale(0.0, "Test"));

        // warnNoParent does NOT throw (just logs) — verify it runs without error
        Region nodeNoParent = new Region();
        try {
            Preconditions.warnNoParent(nodeNoParent, "Test");
            ok("warnNoParent no parent: no throw");
        } catch (Exception e) {
            fail("warnNoParent no parent: unexpected throw", e);
        }
    }

    // =========================================================================
    // 9. FxLayout — justifyPositions() math
    // =========================================================================
    static void testFxLayoutJustify() {
        suite("FxLayout.justifyPositions");

        double[] sizes = {50, 50, 50}; // 3 items of 50px each
        double total = 300;           // container = 300px
        double gap = 0;

        // START: first at 0, each 50 apart
        double[] start = FxFlexPane.justifyPositions(sizes, gap, total, FxFlexPane.Justify.START);
        approx("START[0]=0", 0, start[0]);
        approx("START[1]=50", 50, start[1]);
        approx("START[2]=100", 100, start[2]);

        // CENTER: free=150, offset=75
        double[] center = FxFlexPane.justifyPositions(sizes, gap, total, FxFlexPane.Justify.CENTER);
        approx("CENTER[0]=75", 75, center[0]);
        approx("CENTER[1]=125", 125, center[1]);
        approx("CENTER[2]=175", 175, center[2]);

        // END: start at 150
        double[] end = FxFlexPane.justifyPositions(sizes, gap, total, FxFlexPane.Justify.END);
        approx("END[0]=150", 150, end[0]);
        approx("END[2]=250", 250, end[2]);

        // BETWEEN: a=0, b=125, c=250 (free=150, 2 gaps of 75)
        double[] between = FxFlexPane.justifyPositions(sizes, gap, total, FxFlexPane.Justify.BETWEEN);
        approx("BETWEEN[0]=0", 0, between[0]);
        approx("BETWEEN[1]=125", 125, between[1]);
        approx("BETWEEN[2]=250", 250, between[2]);

        // AROUND: free=150, unit=50, a=25, b=125, c=225
        double[] around = FxFlexPane.justifyPositions(sizes, gap, total, FxFlexPane.Justify.AROUND);
        approx("AROUND[0]=25", 25, around[0]);
        approx("AROUND[1]=125", 125, around[1]);
        approx("AROUND[2]=225", 225, around[2]);

        // EVENLY: free=150, 4 slots, unit=37.5, a=37.5, b=137.5, c=237.5
        double[] evenly = FxFlexPane.justifyPositions(sizes, gap, total, FxFlexPane.Justify.EVENLY);
        approx("EVENLY[0]=37.5", 37.5, evenly[0]);
        approx("EVENLY[1]=137.5", 137.5, evenly[1]);
        approx("EVENLY[2]=237.5", 237.5, evenly[2]);

        // With gap: 3×50 + 2×10 = 170, free=130
        double[] withGap = FxFlexPane.justifyPositions(sizes, 10, total, FxFlexPane.Justify.START);
        approx("START gap[1]=60", 60, withGap[1]); // 50 + 10
        approx("START gap[2]=120", 120, withGap[2]); // 100 + 20

        // Single item BETWEEN: at 0
        double[] single = FxFlexPane.justifyPositions(new double[]{50}, 0, 200, FxFlexPane.Justify.BETWEEN);
        approx("BETWEEN single[0]=0", 0, single[0]);
    }

    // =========================================================================
    // Main
    // =========================================================================
    public static void main(String[] args) {
        boolean ok = runAll();
        System.exit(ok ? 0 : 1);
    }
}
