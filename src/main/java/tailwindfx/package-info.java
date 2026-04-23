/**
 * TailwindFX — Utility-first UI framework for JavaFX, v4.3.
 *
 * <h2>Quick start</h2>
 * <pre>
 * // 1. Install in your Application.start():
 * TailwindFX.install(scene);          // loads tailwindfx.css
 * TailwindFX.install(scene, stage);   // also enables responsive breakpoints
 *
 * // 2. Apply utility classes:
 * TailwindFX.apply(button, "btn-primary", "rounded-lg", "shadow-md");
 * TailwindFX.apply(card,   "card", "w-80");
 *
 * // 3. JIT — compile arbitrary values at runtime:
 * TailwindFX.jit(pane, "bg-blue-500/80", "p-[13px]", "-translate-x-4");
 *
 * // 4. Responsive per-node rules:
 * TailwindFX.responsive(sidebar)
 *     .base("w-64").sm("w-full").md("w-48").install(scene);
 *
 * // 5. Themes:
 * TailwindFX.theme(scene).dark().apply();
 * TailwindFX.scope(pane).preset("blue").apply();
 *
 * // 6. Flex layout:
 * FxFlexPane row = TailwindFX.flexRow()
 *     .wrap(true).justify(FxFlexPane.Justify.BETWEEN).gap(16);
 *
 * // 7. Data table:
 * FxDataTable&lt;User&gt; table = TailwindFX.dataTable(User.class)
 *     .column("Name", User::name).searchable(true).pageSize(20).build();
 *
 * // 8. Animations:
 * FxAnimation.fadeIn(node).play();
 * FxAnimation.onHoverScale(button, 1.05);
 * </pre>
 *
 * <h2>Class overview</h2>
 * <table>
 *   <tr><th>Class</th><th>Purpose</th></tr>
 *   <tr><td>{@link tailwindfx.TailwindFX}</td><td>Main entry point — all public APIs</td></tr>
 *   <tr><td>{@link tailwindfx.Styles}</td><td>Java APIs for CSS-unsupported features</td></tr>
 *   <tr><td>{@link tailwindfx.FxFlexPane}</td><td>Flexbox layout container</td></tr>
 *   <tr><td>{@link tailwindfx.FxGridPane}</td><td>Grid-template-areas layout</td></tr>
 *   <tr><td>{@link tailwindfx.FxLayout}</td><td>Builder for HBox/VBox/GridPane/etc.</td></tr>
 *   <tr><td>{@link tailwindfx.FxDataTable}</td><td>Sortable/filterable/paginated table</td></tr>
 *   <tr><td>{@link tailwindfx.ComponentFactory}</td><td>High-level component builders</td></tr>
 *   <tr><td>{@link tailwindfx.FxAnimation}</td><td>Animations + hover effects</td></tr>
 *   <tr><td>{@link tailwindfx.ResponsiveNode}</td><td>Per-node responsive rules</td></tr>
 *   <tr><td>{@link tailwindfx.BreakpointManager}</td><td>Scene-level breakpoint detection</td></tr>
 *   <tr><td>{@link tailwindfx.ThemeManager}</td><td>Theme presets + persistence</td></tr>
 *   <tr><td>{@link tailwindfx.ThemeScopeManager}</td><td>Scoped themes per subtree</td></tr>
 *   <tr><td>{@link tailwindfx.JitCompiler}</td><td>JIT CSS token compiler</td></tr>
 *   <tr><td>{@link tailwindfx.StylePerf}</td><td>Batch apply + style diffing</td></tr>
 *   <tr><td>{@link tailwindfx.TailwindFXMetrics}</td><td>Runtime metrics + alerts</td></tr>
 *   <tr><td>{@link tailwindfx.UtilityConflictResolver}</td><td>Deterministic class conflict resolution</td></tr>
 *   <tr><td>{@link tailwindfx.ColorPalette}</td><td>209 Tailwind colors</td></tr>
 * </table>
 */
package tailwindfx;
