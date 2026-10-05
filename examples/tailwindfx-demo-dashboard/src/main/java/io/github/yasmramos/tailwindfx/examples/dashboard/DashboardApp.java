/*
 * Copyright 2026 Yasmany Ramos García (yasmramos).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.yasmramos.tailwindfx.examples.dashboard;

import io.github.yasmramos.tailwindfx.TwBatch;
import io.github.yasmramos.tailwindfx.TwConfig;
import io.github.yasmramos.tailwindfx.TwEffect;
import io.github.yasmramos.tailwindfx.TwInstall;
import io.github.yasmramos.tailwindfx.TwMetrics;
import io.github.yasmramos.tailwindfx.TwStyle;
import io.github.yasmramos.tailwindfx.TwTheme;
import io.github.yasmramos.tailwindfx.animation.TwAnimation;
import io.github.yasmramos.tailwindfx.breakpoint.BreakpointManager;
import io.github.yasmramos.tailwindfx.components.TWAccordion;
import io.github.yasmramos.tailwindfx.components.TWTitledPane;
import io.github.yasmramos.tailwindfx.components.TwAlert;
import io.github.yasmramos.tailwindfx.components.TwAvatar;
import io.github.yasmramos.tailwindfx.components.TwBadge;
import io.github.yasmramos.tailwindfx.components.TwButton;
import io.github.yasmramos.tailwindfx.components.TwCard;
import io.github.yasmramos.tailwindfx.components.TwCheckbox;
import io.github.yasmramos.tailwindfx.components.TwDataTable;
import io.github.yasmramos.tailwindfx.components.TwInput;
import io.github.yasmramos.tailwindfx.components.TwProgressBar;
import io.github.yasmramos.tailwindfx.components.TwSelect;
import io.github.yasmramos.tailwindfx.components.TwSpinner;
import io.github.yasmramos.tailwindfx.layout.TwFlexPane;
import io.github.yasmramos.tailwindfx.layout.TwGridPane;
import io.github.yasmramos.tailwindfx.metrics.TailwindFXMetrics;
import io.github.yasmramos.tailwindfx.responsive.ResponsiveNode;
import io.github.yasmramos.tailwindfx.theme.ThemeManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * TailwindFX Demo Dashboard.
 *
 * <p>A JavaFX admin-panel style example that exercises the public API of the framework: JIT
 * utility styling with arbitrary values, AOT generated stylesheet installation, dark mode toggling,
 * responsive breakpoints, pre-built components (buttons, badges, alerts, avatars, forms, data
 * tables, accordion), effects, animations, batched style application and runtime metrics.
 *
 * <p>Run with: {@code mvn -pl examples/tailwindfx-demo-dashboard javafx:run}
 */
public class DashboardApp extends Application {

  /** Simple row record used by the data table demo. */
  private record MetricRow(String name, String value, String trend, String status) {}

  @Override
  public void start(Stage stage) {
    // Global configuration: 4px spacing unit and debug logging for JIT compilation.
    TwConfig.unit(4.0);
    TwConfig.debug(true);
    TwMetrics.setEnabled(true);
    TailwindFXMetrics.instance().setEnabled(true);

    BorderPane root = new BorderPane();
    TwStyle.apply(root, "bg-gray-100", "dark:bg-gray-800");

    Scene scene = new Scene(root, 1280, 800);

    // Install base runtime styles plus the AOT generated stylesheet (build-time CSS).
    TwInstall.install(scene);
    TwInstall.installGenerated(scene, "css/tailwindfx-generated.css");

    root.setLeft(buildSidebar());
    root.setTop(buildHeader(scene));
    root.setCenter(buildMainArea(stage, scene));

    stage.setTitle("TailwindFX Demo Dashboard");
    stage.setScene(scene);
    stage.show();

    // Entrance animation for the whole dashboard.
    TwAnimation.fadeIn(root, TwAnimation.SLOW).play();
  }

  // ---------------------------------------------------------------------------------------------
  // Sidebar
  // ---------------------------------------------------------------------------------------------

  /** Builds the left sidebar using an arbitrary JIT value ({@code w-[240px]}) for the width. */
  private VBox buildSidebar() {
    Label brand = new Label("⚡ TailwindFX");
    TwStyle.apply(brand, "text-xl", "font-bold", "text-white", "mb-4");

    String[] items = {"Dashboard", "Analytics", "Customers", "Orders", "Settings"};
    VBox nav = new VBox(4);
    for (int i = 0; i < items.length; i++) {
      Label item = new Label(items[i]);
      // First item is highlighted as "active"; hover variant demonstrates state prefixes.
      TwStyle.apply(
          item,
          "p-2",
          "px-3",
          "rounded-md",
          "text-sm",
          "text-gray-300",
          "hover:bg-gray-800",
          "hover:text-white",
          i == 0 ? "bg-gray-800" : "bg-transparent");
      nav.getChildren().add(item);
    }

    Label footer = new Label("v0.1.2 · JIT + AOT");
    TwStyle.apply(footer, "text-xs", "text-gray-500", "mt-auto");

    VBox sidebar = new VBox(8, brand, nav, footer);
    // Arbitrary value w-[240px] demonstrates JIT bracket syntax alongside standard tokens.
    TwStyle.apply(sidebar, "w-[240px]", "bg-gray-900", "text-white", "p-4");
    sidebar.setPadding(new Insets(16));
    return sidebar;
  }

  // ---------------------------------------------------------------------------------------------
  // Header with dark mode toggle
  // ---------------------------------------------------------------------------------------------

  /** Builds the top header bar including the dark mode toggle button. */
  private HBox buildHeader(Scene scene) {
    Label title = new Label("Admin Dashboard");
    TwStyle.apply(title, "text-2xl", "font-bold", "text-gray-900", "dark:text-white");

    Label subtitle = new Label("JIT utilities · AOT stylesheet · responsive · theming");
    TwStyle.apply(subtitle, "text-sm", "text-gray-500", "dark:text-gray-400");

    TwButton darkToggle = TwButton.outline("🌙 Toggle Dark Mode");
    darkToggle.setOnAction(
        e -> {
          // Toggles the "dark" class on the scene root and applies dark tokens (dark:* variants).
          ThemeManager.toggle(scene);
        });

    TwButton refresh = TwButton.primary("Refresh");
    refresh.setOnAction(e -> refreshMetricsCard());

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox header = new HBox(12, title, subtitle, spacer, darkToggle, refresh);
    header.setPadding(new Insets(12, 16, 12, 16));
    TwStyle.apply(
        header,
        "bg-white",
        "dark:bg-gray-800",
        "border-b",
        "border-gray-200",
        "shadow-sm",
        "items-center");
    return header;
  }

  // ---------------------------------------------------------------------------------------------
  // Main content area
  // ---------------------------------------------------------------------------------------------

  private ScrollPane mainScroll;
  private TwGridPane cardsGrid;
  private TwCard metricsCard;
  private Label metricsLabel;

  /** Builds the scrollable main area with a responsive grid of demo cards. */
  private VBox buildMainArea(Stage stage, Scene scene) {
    cardsGrid = TwGridPane.create().cols(3).gap(16).build();

    cardsGrid.getChildren().addAll(buildButtonsCard(), buildBadgesCard(), buildAlertsCard());
    cardsGrid.getChildren().addAll(buildAvatarsCard(), buildFormCard(), buildDataCard());
    cardsGrid.getChildren().addAll(buildAccordionCard(), buildUtilitiesCard());
    cardsGrid.getChildren().addAll(buildEffectsCard(), buildMetricsCard());

    // Give the metrics card a double column span to show per-child grid spans.
    TwGridPane.setColSpan(metricsCard, 2);

    // Animate cards in with a staggered fade effect.
    int delay = 0;
    for (javafx.scene.Node child : cardsGrid.getChildren()) {
      TwAnimation anim = TwAnimation.fadeIn(child, TwAnimation.NORMAL);
      anim.play();
      delay += 40;
    }

    VBox content = new VBox(16, cardsGrid);
    content.setPadding(new Insets(16));

    mainScroll = new ScrollPane(content);
    mainScroll.setFitToWidth(true);
    mainScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    TwStyle.apply(mainScroll, "bg-transparent");
    VBox.setVgrow(mainScroll, Priority.ALWAYS);

    // Responsive: switch grid columns depending on window width via BreakpointManager.
    BreakpointManager bp = BreakpointManager.attach(stage);
    applyResponsiveColumns(bp.current());
    bp.activeBreakpointProperty()
        .addListener(
            (obs, oldBp, newBp) -> {
              if (newBp != null) {
                applyResponsiveColumns(newBp);
              }
            });

    // Declarative responsive styling of the grid container itself.
    // ResponsiveNode.Builder can be configured before the node is attached to a Scene,
    // and Breakpoint#minWidth is a public field holding the breakpoint width in px.
    ResponsiveNode.on(cardsGrid)
        .at((int) BreakpointManager.BP.SM.minWidth, "gap-2")
        .at((int) BreakpointManager.BP.LG.minWidth, "gap-4")
        .install(scene);

    return contentWrapper(mainScroll);
  }

  private VBox contentWrapper(Region center) {
    VBox box = new VBox(center);
    VBox.setVgrow(center, Priority.ALWAYS);
    return box;
  }

  /** Adjusts the number of grid columns based on the active breakpoint. */
  private void applyResponsiveColumns(BreakpointManager.Breakpoint bp) {
    switch (bp) {
      case XS, SM -> cardsGrid.cols(1);
      case MD -> cardsGrid.cols(2);
      case LG -> cardsGrid.cols(3);
      default -> cardsGrid.cols(4);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Section: Buttons
  // ---------------------------------------------------------------------------------------------

  private TwCard buildButtonsCard() {
    TwFlexPane row = TwFlexPane.row().wrap(true).gap(8);

    row.getChildren()
        .addAll(
            TwButton.primary("Primary"),
            TwButton.secondary("Secondary"),
            TwButton.outline("Outline"),
            TwButton.ghost("Ghost"),
            TwButton.danger("Danger"));

    TwFlexPane colored = TwFlexPane.row().wrap(true).gap(8);
    colored
        .getChildren()
        .addAll(
            TwButton.primary("Indigo", "indigo"),
            TwButton.primary("Green", "green"),
            TwButton.outline("Blue", "blue"),
            TwButton.icon("★", "Icon"),
            TwButton.success("Success"));

    VBox box = new VBox(8, row, colored);
    return wrapCard("Buttons", box);
  }

  // ---------------------------------------------------------------------------------------------
  // Section: Badges
  // ---------------------------------------------------------------------------------------------

  private TwCard buildBadgesCard() {
    TwFlexPane row = TwFlexPane.row().wrap(true).gap(8);
    row.getChildren()
        .addAll(
            TwBadge.create("New"),
            TwBadge.create("Info", "blue"),
            TwBadge.pill("Pill"),
            TwBadge.pill("Pill green", "green"),
            TwBadge.outline("Outline"),
            TwBadge.dot("Online", "green"),
            TwBadge.dot("Busy", "red"));
    return wrapCard("Badges", row);
  }

  // ---------------------------------------------------------------------------------------------
  // Section: Alerts
  // ---------------------------------------------------------------------------------------------

  private TwCard buildAlertsCard() {
    VBox box =
        new VBox(
            8,
            TwAlert.info("Heads up! This is an informational alert."),
            TwAlert.success("Your profile has been saved."),
            TwAlert.warning("Your trial expires in 3 days."),
            TwAlert.error("Something went wrong while loading data."));
    return wrapCard("Alerts", box);
  }

  // ---------------------------------------------------------------------------------------------
  // Section: Avatars
  // ---------------------------------------------------------------------------------------------

  private TwCard buildAvatarsCard() {
    TwFlexPane sizes = TwFlexPane.row().wrap(true).gap(8);
    sizes
        .getChildren()
        .addAll(
            TwAvatar.create("xs", "blue", "xs"),
            TwAvatar.create("sm", "green", "sm"),
            TwAvatar.create("md", "purple", "md"),
            TwAvatar.create("lg", "red", "lg"),
            TwAvatar.create("xl", "indigo", "xl"));

    TwAvatar a1 = TwAvatar.create("jr", "blue", "md");
    TwAvatar a2 = TwAvatar.create("ak", "green", "md");
    TwAvatar a3 = TwAvatar.create("ms", "purple", "md");
    TwAvatar.TwAvatarGroup group = TwAvatar.group(a1, a2, a3);

    TwAvatar single = TwAvatar.create("yd", "red", "lg");
    TwAvatar.TwAvatarWithStatus online = TwAvatar.withStatus(single, true);

    HBox row2 = new HBox(16, group, online);
    row2.setStyle("-fx-alignment: center-left;");

    VBox box = new VBox(12, sizes, row2);
    return wrapCard("Avatars", box);
  }

  // ---------------------------------------------------------------------------------------------
  // Section: Form
  // ---------------------------------------------------------------------------------------------

  private TwCard buildFormCard() {
    TwInput name = new TwInput("Full name");
    TwInput email = TwInput.withPlaceholder("you@example.com");
    TwInput secret = TwInput.password();
    secret.setPromptText("Password");

    var role = TwSelect.comboBox("Admin", "Editor", "Viewer");
    role.setPromptText("Role");

    TwCheckbox terms = TwCheckbox.create("Accept terms");
    TwCheckbox newsletter = TwCheckbox.checked("Subscribe to newsletter", true);

    VBox box = new VBox(10, name, email, secret, role, terms, newsletter);
    return wrapCard("Form", box);
  }

  // ---------------------------------------------------------------------------------------------
  // Section: Data (progress, spinner, table)
  // ---------------------------------------------------------------------------------------------

  private TwCard buildDataCard() {
    TwProgressBar cpu = new TwProgressBar(0.72);
    TwProgressBar mem = TwProgressBar.success(0.45);
    TwProgressBar disk = TwProgressBar.warning(0.88);
    TwProgressBar net = TwProgressBar.error(0.95);

    var spinnerSmall = TwSpinner.small();
    var spinnerLarge = TwSpinner.largeColored("blue");
    HBox spinners = new HBox(12, spinnerSmall, spinnerLarge);
    spinners.setStyle("-fx-alignment: center-left;");

    TwDataTable<MetricRow> table =
        TwDataTable.<MetricRow>of(MetricRow.class)
            .column("Metric", MetricRow::name)
            .column("Value", MetricRow::value)
            .column("Trend", MetricRow::trend)
            .column("Status", MetricRow::status)
            .searchable(false)
            .style("text-sm")
            .build();
    table.setPrefHeight(180);
    table.setItems(
        java.util.List.of(
            new MetricRow("Revenue", "$48,210", "+12.4%", "up"),
            new MetricRow("Active users", "9,342", "+3.1%", "up"),
            new MetricRow("Bounce rate", "38.2%", "-1.8%", "down"),
            new MetricRow("Avg. session", "4m 12s", "+0.6%", "flat")));

    VBox box = new VBox(10, cpu, mem, disk, net, spinners, table);
    return wrapCard("Data", box);
  }

  // ---------------------------------------------------------------------------------------------
  // Section: Accordion
  // ---------------------------------------------------------------------------------------------

  private TwCard buildAccordionCard() {
    Label c1 = new Label("All widgets follow Tailwind utility semantics.");
    Label c2 = new Label("Colors, spacing and typography map to design tokens.");
    Label c3 = new Label("Use dark:* variants for theme-aware styling.");
    TWAccordion accordion =
        new TWAccordion(
            new TWTitledPane("Design system", new VBox(c1)),
            new TWTitledPane("Tokens", new VBox(c2)),
            new TWTitledPane("Theming", new VBox(c3)));
    return wrapCard("Accordion", accordion);
  }

  // ---------------------------------------------------------------------------------------------
  // Section: Style utilities
  // ---------------------------------------------------------------------------------------------

  private TwCard buildUtilitiesCard() {
    Label heading = new Label("text-xl font-bold");
    TwStyle.apply(heading, "text-xl", "font-bold", "text-indigo-600", "dark:text-indigo-400");

    Label colors = new Label("bg-blue-500 text-white p-2 rounded-md");
    TwStyle.apply(colors, "bg-blue-500", "text-white", "p-2", "rounded-md");

    Label shadow = new Label("shadow-lg rounded-lg bg-white p-2");
    TwStyle.apply(shadow, "shadow-lg", "rounded-lg", "bg-white", "p-2", "text-gray-700");

    Label hover = new Label("hover:bg-red-500 focus:bg-green-500 p-2 rounded");
    TwStyle.apply(hover, "p-2", "rounded", "bg-gray-200", "hover:bg-red-500");

    Label jit = new Label("w-[180px] bg-[#7c3aed] text-white p-2 rounded");
    TwStyle.apply(jit, "w-[180px]", "bg-[#7c3aed]", "text-white", "p-2", "rounded");

    Label margin = new Label("m-4 p-2 bg-yellow-200 rounded");
    TwStyle.apply(margin, "m-4", "p-2", "bg-yellow-200", "rounded", "text-gray-800");

    VBox box = new VBox(10, heading, colors, shadow, hover, jit, margin);
    return wrapCard("Style Utilities", box);
  }

  // ---------------------------------------------------------------------------------------------
  // Section: Effects & animation
  // ---------------------------------------------------------------------------------------------

  private TwCard buildEffectsCard() {
    Label blurred = new Label("backdropBlur / grayscale");
    TwStyle.apply(blurred, "p-3", "rounded-lg", "bg-purple-500", "text-white", "font-bold");
    TwEffect.backdropBlur(blurred, 8);

    Label gray = new Label("grayscale");
    TwStyle.apply(gray, "p-3", "rounded-lg", "bg-pink-500", "text-white", "font-bold");
    TwEffect.grayscale(gray);

    TwButton pulseBtn = TwButton.primary("Pulse me");
    pulseBtn.setOnAction(e -> TwAnimation.pulse(pulseBtn).cycleCount(3).play());

    TwButton scaleBtn = TwButton.secondary("Scale in");
    scaleBtn.setOnAction(e -> TwAnimation.scaleIn(scaleBtn).play());

    TwButton slideBtn = TwButton.outline("Slide up");
    slideBtn.setOnAction(e -> TwAnimation.slideUp(slideBtn).play());

    TwFlexPane row = TwFlexPane.row().wrap(true).gap(8);
    row.getChildren().addAll(blurred, gray);
    TwFlexPane btns = TwFlexPane.row().wrap(true).gap(8);
    btns.getChildren().addAll(pulseBtn, scaleBtn, slideBtn);

    VBox box = new VBox(12, row, btns);
    return wrapCard("Effects & Animation", box);
  }

  // ---------------------------------------------------------------------------------------------
  // Section: JIT metrics
  // ---------------------------------------------------------------------------------------------

  private TwCard buildMetricsCard() {
    metricsLabel = new Label(collectMetricsText());
    TwStyle.apply(metricsLabel, "font-mono", "text-sm", "text-gray-700", "dark:text-gray-300");

    TwButton batchBtn =
        TwButton.outline("Apply 50 styles in one batch (TwBatch)");
    batchBtn.setOnAction(
        e -> {
          // Batched style application: coalesces invalidations for better performance.
          TwBatch.run(
              () -> {
                for (int i = 0; i < 50; i++) {
                  TwStyle.apply(
                      metricsLabel, "text-sm", "font-mono", "text-gray-700", "dark:text-gray-300");
                }
              });
          refreshMetricsCard();
        });

    TwButton reportBtn = TwButton.ghost("Print metrics report");
    reportBtn.setOnAction(
        e -> {
          TailwindFXMetrics.instance().report();
          refreshMetricsCard();
        });

    VBox box = new VBox(10, metricsLabel, batchBtn, reportBtn);
    metricsCard = wrapCard("JIT Metrics", box);
    return metricsCard;
  }

  /** Refreshes the metrics card label with current counters. */
  private void refreshMetricsCard() {
    if (metricsLabel != null) {
      metricsLabel.setText(collectMetricsText());
    }
  }

  private String collectMetricsText() {
    TailwindFXMetrics m = TailwindFXMetrics.instance();
    return String.format(
        "compilations=%d | cache hits=%d | misses=%d | apply calls=%d | avg compile=%dns | uptime=%dms",
        m.compilations(), m.cacheHits(), m.cacheMisses(), m.applyCalls(), m.avgCompileNs(),
        m.uptimeMs());
  }

  // ---------------------------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------------------------

  /** Wraps content into a titled card with common surface styling. */
  private TwCard wrapCard(String title, javafx.scene.Node body) {
    TwCard card = TwCard.withTitle(title);
    card.setBody(body);
    TwStyle.apply(
        card, "bg-white", "dark:bg-gray-800", "rounded-xl", "shadow-md", "p-4", "gap-2");
    return card;
  }

  public static void main(String[] args) {
    launch(args);
  }
}
