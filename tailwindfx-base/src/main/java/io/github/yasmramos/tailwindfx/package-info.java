/**
 * TailwindFX — Utility-first UI framework for JavaFX, v4.3.
 *
 * <h2>Quick start</h2>
 *
 * <pre>
 * // 1. Install in your Application.start():
 * TwInstall.install(scene);          // loads tailwindfx.css
 * TwInstall.install(scene, stage);   // also enables responsive breakpoints
 *
 * // 2. Apply utility classes:
 * TwStyle.apply(button, "btn-primary", "rounded-lg", "shadow-md");
 * TwStyle.apply(card,   "card", "w-80");
 *
 * // 3. JIT — arbitrary values are auto-detected by apply():
 * TwStyle.apply(pane, "bg-blue-500/80", "p-[13px]", "-translate-x-4");
 *
 * // 4. Responsive per-node rules:
 * ResponsiveNode.on(sidebar)
 *     .base("w-64").sm("w-full").md("w-48").install(scene);
 *
 * // 5. Themes:
 * TwTheme.of(scene).dark().apply();
 * TwTheme.scope(pane).preset("blue").apply();
 *
 * // 6. Flex layout:
 * TwFlexPane row = TwLayout.flexRow();
 * row.wrap(true).justify(TwFlexPane.Justify.BETWEEN).gap(16);
 *
 * // 7. Data table (from the tailwindfx-components module):
 * TwDataTable&lt;User&gt; table = TwDataTable.of(User.class)
 *     .column("Name", User::name).searchable(true).pageSize(20).build();
 *
 * // 8. Animations:
 * TwAnimation.fadeIn(node).play();
 * TwAnimation.onHoverScale(button, 1.05);
 *
 * // 9. Tailwind animation tokens (played as TwAnimation timelines):
 * TwStyle.apply(spinner, "animate-spin");
 * </pre>
 *
 * <h2>Class overview</h2>
 *
 * <table>
 *   <tr><th>Class</th><th>Purpose</th></tr>
 *   <tr><td>{@link TwStyle}</td><td>Style application facade</td></tr>
 *   <tr><td>{@link TwInstall}</td><td>Stylesheet installation facade</td></tr>
 *   <tr><td>{@link TwTheme}</td><td>Theme facade</td></tr>
 *   <tr><td>{@link TwLayout}</td><td>Unified layout engine with fluent builder</td></tr>
 *   <tr><td>{@link TwConfig}</td><td>Global configuration facade</td></tr>
 *   <tr><td>{@link TwEffect}</td><td>Java APIs for CSS-unsupported effects</td></tr>
 *   <tr><td>{@link TwBatch}</td><td>Batch style operations</td></tr>
 *   <tr><td>{@link io.github.yasmramos.tailwindfx.animation.TwAnimation}</td><td>Animations + hover effects</td></tr>
 *   <tr><td>{@link io.github.yasmramos.tailwindfx.layout.TwFlexPane}</td><td>Flexbox layout container</td></tr>
 *   <tr><td>{@link io.github.yasmramos.tailwindfx.layout.TwGridPane}</td><td>Grid-template-areas layout</td></tr>
 *   <tr><td>{@link io.github.yasmramos.tailwindfx.responsive.ResponsiveNode}</td><td>Per-node responsive rules</td></tr>
 *   <tr><td>{@link io.github.yasmramos.tailwindfx.breakpoint.BreakpointManager}</td><td>Scene-level breakpoint detection</td></tr>
 *   <tr><td>{@link io.github.yasmramos.tailwindfx.theme.ThemeManager}</td><td>Theme presets + persistence</td></tr>
 *   <tr><td>{@link io.github.yasmramos.tailwindfx.theme.ThemeScopeManager}</td><td>Scoped themes per subtree</td></tr>
 *   <tr><td>{@link io.github.yasmramos.tailwindfx.core.JitCompiler}</td><td>JIT CSS token compiler</td></tr>
 *   <tr><td>{@link io.github.yasmramos.tailwindfx.style.StylePerf}</td><td>Batch apply + style diffing</td></tr>
 *   <tr><td>{@link io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics}</td><td>Runtime metrics + alerts</td></tr>
 *   <tr><td>{@link io.github.yasmramos.tailwindfx.core.UtilityConflictResolver}</td><td>Deterministic class conflict resolution</td></tr>
 *   <tr><td>{@link io.github.yasmramos.tailwindfx.color.ColorPalette}</td><td>209 Tailwind colors</td></tr>
 * </table>
 */
package io.github.yasmramos.tailwindfx;
