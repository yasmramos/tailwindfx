package tailwindfx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * TailwindFX — Comprehensive demo application.
 *
 * <p>Demonstrates all major features introduced across v1–v4:
 * utility classes, JIT, themes, FxFlexPane, FxGridPane,
 * ResponsiveNode, FxDataTable, animations, glassmorphism, and more.
 *
 * <p>Run this class directly to see TailwindFX in action.
 */
public class TailwindFXExample extends Application {

    record Person(String name, String role, String dept, int age) {}

    @Override
    public void start(Stage stage) {
        // ── Install TailwindFX ──────────────────────────────────────────────
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 1100, 750);
        TailwindFX.install(scene, stage);

        // ── Outer scroll container ──────────────────────────────────────────
        VBox page = new VBox(24);
        page.setPadding(new Insets(24));
        TailwindFX.apply(page, "bg-gray-50");

        // ── Header ──────────────────────────────────────────────────────────
        page.getChildren().add(buildHeader());

        // ── Row 1: Cards + Badges ───────────────────────────────────────────
        page.getChildren().add(buildCardRow());

        // ── Row 2: FxFlexPane demo ──────────────────────────────────────────
        page.getChildren().add(buildFlexDemo(scene));

        // ── Row 3: FxGridPane demo ──────────────────────────────────────────
        page.getChildren().add(buildGridDemo());

        // ── Row 4: Glass + Neumorph ─────────────────────────────────────────
        page.getChildren().add(buildEffectsRow());

        // ── Row 5: FxDataTable ──────────────────────────────────────────────
        page.getChildren().add(buildDataTableDemo());

        // ── Row 6: Animations ───────────────────────────────────────────────
        page.getChildren().add(buildAnimationsRow());

        // ── Row 7: Theme toggle ─────────────────────────────────────────────
        page.getChildren().add(buildThemeRow(scene));

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #f9fafb; -fx-background-color: #f9fafb;");
        root.getChildren().add(scroll);

        // ── Responsive: sidebar collapses below MD ───────────────────────────
        ResponsiveNode.on(page)
            .base("p-6")
            .sm("p-4")
            .install(scene);

        stage.setTitle("TailwindFX Demo — v4.3");
        stage.setScene(scene);
        stage.show();

        // Metrics reporting
        TailwindFXMetrics metrics = TailwindFX.metrics();
        metrics.setEnabled(true);
        metrics
            .onAlert((m, v, t) -> System.out.printf("[Metrics] %s=%.2f (threshold=%.2f)%n", m, v, t))
            .alertOnLowCacheHitRatio(0.60);
    }

    // =========================================================================
    // Sections
    // =========================================================================

    private VBox buildHeader() {
        Label title = new Label("TailwindFX");
        TailwindFX.apply(title, "text-4xl", "font-bold", "text-blue-600");

        Label sub = new Label("Utility-first UI framework for JavaFX  •  v4.3");
        TailwindFX.apply(sub, "text-gray-500", "text-sm", "mt-1");
        TailwindFX.textShadowSm(title);

        VBox h = new VBox(4, title, sub);
        TailwindFX.apply(h, "card-elevated", "mb-2");
        return h;
    }

    private HBox buildCardRow() {
        HBox row = new HBox(16);
        row.setFillHeight(false);

        // Standard card
        VBox card1 = buildCard("Standard Card", "Uses .card with hover shadow.",
            "btn-primary", "Learn More");
        TailwindFX.apply(card1, "card");

        // Dark card
        VBox card2 = buildCard("Dark Card", "Uses .card-dark for dark theme.",
            "btn-secondary", "Details");
        TailwindFX.apply(card2, "card-dark");

        // Elevated card
        VBox card3 = buildCard("Elevated Card", "Uses .card-elevated for depth.",
            "btn-primary", "Open");
        TailwindFX.apply(card3, "card-elevated");

        // Badges
        VBox badgeDemo = new VBox(8);
        Label bTitle = new Label("Badges");
        TailwindFX.apply(bTitle, "font-bold", "text-gray-700");
        HBox badges = new HBox(6,
            badge("NEW",     "badge-primary"),
            badge("Active",  "badge-success"),
            badge("Pending", "badge-warning"),
            badge("Error",   "badge-danger"),
            badge("Info",    "badge-info")
        );
        badgeDemo.getChildren().addAll(bTitle, badges);
        TailwindFX.apply(badgeDemo, "card", "p-4");

        row.getChildren().addAll(card1, card2, card3, badgeDemo);
        return row;
    }

    private VBox buildFlexDemo(Scene scene) {
        Label title = sectionTitle("FxFlexPane — Flex Layout");

        // Row with justify-content: BETWEEN
        FxFlexPane flexRow = TailwindFX.flexRow()
            .justify(FxFlexPane.Justify.BETWEEN)
            .align(FxFlexPane.Align.CENTER)
            .gap(12).wrap(true);
        TailwindFX.apply(flexRow, "bg-white", "rounded-lg", "p-4");

        for (String color : List.of("blue", "green", "red", "purple", "yellow", "sky")) {
            Region box = new Region();
            box.setPrefSize(80, 60);
            TailwindFX.apply(box, "bg-" + color + "-400", "rounded-md");
            flexRow.getChildren().add(box);
        }

        // Growing child demo
        FxFlexPane growRow = TailwindFX.flexRow().gap(8);
        TailwindFX.apply(growRow, "bg-white", "rounded-lg", "p-4", "mt-3");

        Label fixedL = new Label("Fixed");
        TailwindFX.apply(fixedL, "bg-gray-200", "rounded", "p-2", "text-sm");

        Label growL = new Label("flex-grow: 1  (takes remaining space)");
        TailwindFX.apply(growL, "bg-blue-100", "rounded", "p-2", "text-sm");
        FxFlexPane.setGrow(growL, 1);

        Label fixedR = new Label("Fixed");
        TailwindFX.apply(fixedR, "bg-gray-200", "rounded", "p-2", "text-sm");

        growRow.getChildren().addAll(fixedL, growL, fixedR);

        // Responsive direction toggle
        Button toggleBtn = new Button("Toggle Direction");
        TailwindFX.apply(toggleBtn, "btn-secondary", "mt-2");
        toggleBtn.setOnAction(e -> {
            boolean isRow = flexRow.getDirection() == FxFlexPane.Direction.ROW;
            TailwindFX.flexDirection(flexRow,
                isRow ? FxFlexPane.Direction.COL : FxFlexPane.Direction.ROW, 200);
        });

        VBox section = new VBox(8, title, flexRow, growRow, toggleBtn);
        TailwindFX.apply(section, "card");
        return section;
    }

    private VBox buildGridDemo() {
        Label title = sectionTitle("FxGridPane — Grid Template Areas");

        FxGridPane grid = FxGridPane.create()
            .areas(
                "header header header",
                "sidebar main    main",
                "footer footer  footer"
            )
            .gap(8).build();

        Region header = colorPane("#3b82f6", "Header", 36);
        Region sidebar = colorPane("#8b5cf6", "Sidebar", 80);
        Region main    = colorPane("#10b981", "Main Content", 80);
        Region footer  = colorPane("#f59e0b", "Footer", 36);

        grid.placeIn(header,  "header");
        grid.placeIn(sidebar, "sidebar");
        grid.placeIn(main,    "main");
        grid.placeIn(footer,  "footer");

        VBox section = new VBox(8, title, grid);
        TailwindFX.apply(section, "card");
        return section;
    }

    private HBox buildEffectsRow() {
        // Glassmorphism
        StackPane glassDemo = new StackPane();
        glassDemo.setPrefSize(200, 120);
        // Colorful background
        Region bg = new Region();
        bg.setPrefSize(200, 120);
        TailwindFX.jit(bg, "bg-gradient-[to_right,blue-500,purple-600]");

        Region glass = new Region();
        glass.setPrefSize(160, 80);
        TailwindFX.glass(glass);
        TailwindFX.backdropBlurMd(glass);
        Label glassLabel = new Label("Glassmorphism");
        TailwindFX.apply(glassLabel, "text-white", "font-bold");

        StackPane glassCard = new StackPane(glass, glassLabel);
        glassCard.setPrefSize(160, 80);
        glassDemo.getChildren().addAll(bg, glassCard);

        Label glassTitle = new Label("Glass");
        TailwindFX.apply(glassTitle, "text-gray-600", "text-sm", "mt-2", "font-bold");

        // Neumorphism
        Region neuNode = new Region();
        neuNode.setPrefSize(160, 80);
        TailwindFX.neumorph(neuNode);
        Label neuLabel = new Label("Neumorphism");
        TailwindFX.apply(neuLabel, "text-gray-600", "text-sm");
        StackPane neuCard = new StackPane(neuNode, neuLabel);
        neuCard.setPrefSize(160, 80);

        Label neuTitle = new Label("Neumorph");
        TailwindFX.apply(neuTitle, "text-gray-600", "text-sm", "mt-2", "font-bold");

        // Text shadow
        Label shadowText = new Label("Text Shadow");
        TailwindFX.apply(shadowText, "text-2xl", "font-bold", "text-blue-700");
        TailwindFX.textShadowMd(shadowText);
        Label shadowXl = new Label("XL Shadow");
        TailwindFX.apply(shadowXl, "text-2xl", "font-bold", "text-purple-700");
        TailwindFX.textShadowXl(shadowXl);
        TailwindFX.dropShadowBlue(shadowXl);
        VBox shadowDemo = new VBox(8, shadowText, shadowXl);
        TailwindFX.apply(shadowDemo, "card", "p-4");

        HBox row = new HBox(16);
        VBox glassSection = new VBox(4, glassTitle, glassDemo);
        VBox neuSection = new VBox(4, neuTitle, neuCard);
        TailwindFX.apply(glassSection, "card", "p-4");
        TailwindFX.apply(neuSection, "card", "p-4");
        row.getChildren().addAll(glassSection, neuSection, shadowDemo);
        return row;
    }

    private VBox buildDataTableDemo() {
        Label title = sectionTitle("FxDataTable — Sortable + Filterable + Paginated");

        List<Person> people = List.of(
            new Person("Alice Chen",    "Engineer",   "Engineering",  30),
            new Person("Bob Martin",    "Designer",   "Design",       28),
            new Person("Carol White",   "Manager",    "Engineering",  35),
            new Person("Dan Brown",     "Analyst",    "Finance",      26),
            new Person("Eve Johnson",   "Engineer",   "Engineering",  32),
            new Person("Frank Lee",     "Director",   "Product",      42),
            new Person("Grace Kim",     "Designer",   "Design",       24),
            new Person("Henry Davis",   "Engineer",   "Engineering",  29),
            new Person("Iris Wang",     "Analyst",    "Finance",      31),
            new Person("Jack Wilson",   "Manager",    "HR",           38)
        );

        FxDataTable<Person> table = TailwindFX.dataTable(Person.class)
            .col("Name",       Person::name)
            .col("Role",       Person::role)
            .col("Department", Person::dept)
            .col("Age",        p -> String.valueOf(p.age()))
            .searchable(true)
            .pageSize(5)
            .style("table-striped", "table-hover")
            .build();

        table.setItems(people);
        table.container().setPrefHeight(280);

        VBox section = new VBox(8, title, table.container());
        TailwindFX.apply(section, "card");
        return section;
    }

    private HBox buildAnimationsRow() {
        Label title = sectionTitle("Animations");

        Button fadeBtn = new Button("Fade In");
        TailwindFX.apply(fadeBtn, "btn-primary");
        fadeBtn.setOnAction(e -> AnimationUtil.fadeIn(fadeBtn, 400).play());

        Button shakeBtn = new Button("Shake");
        TailwindFX.apply(shakeBtn, "btn-secondary");
        shakeBtn.setOnAction(e -> AnimationUtil.shake(shakeBtn).play());

        Button bounceBtn = new Button("Bounce");
        TailwindFX.apply(bounceBtn, "btn-primary");
        bounceBtn.setOnAction(e -> AnimationUtil.bounce(bounceBtn).play());

        Button pulseBtn = new Button("Pulse");
        TailwindFX.apply(pulseBtn, "btn-secondary");
        pulseBtn.setOnAction(e -> AnimationUtil.pulse(pulseBtn).play());

        Button scaleBtn = new Button("Scale In");
        TailwindFX.apply(scaleBtn, "btn-primary");
        scaleBtn.setOnAction(e -> AnimationUtil.scaleIn(scaleBtn, 300).play());

        Button hoverBtn = new Button("Hover Me ↑");
        TailwindFX.apply(hoverBtn, "btn-secondary");
        AnimationUtil.onHoverScale(hoverBtn, 1.08);
        AnimationUtil.onHoverLift(hoverBtn, -4);

        Button motionBtn = new Button("Motion Reduce");
        TailwindFX.apply(motionBtn, "btn-secondary");
        motionBtn.setOnAction(e -> {
            TailwindFX.setReducedMotion(!TailwindFX.shouldAnimate() ? false : true);
            motionBtn.setText(TailwindFX.shouldAnimate() ? "Motion Reduce" : "Motion ON");
        });

        HBox btns = new HBox(10, fadeBtn, shakeBtn, bounceBtn, pulseBtn,
                              scaleBtn, hoverBtn, motionBtn);
        VBox section = new VBox(8, title, btns);
        TailwindFX.apply(section, "card");

        HBox row = new HBox(section);
        return row;
    }

    private HBox buildThemeRow(Scene scene) {
        Label title = sectionTitle("Themes");

        Button lightBtn = new Button("Light");
        Button darkBtn  = new Button("Dark");
        Button blueBtn  = new Button("Blue");
        Button roseBtn  = new Button("Rose");

        TailwindFX.apply(lightBtn, "btn-secondary");
        TailwindFX.apply(darkBtn,  "btn-secondary");
        TailwindFX.apply(blueBtn,  "btn-primary");
        TailwindFX.apply(roseBtn,  "btn-secondary");

        lightBtn.setOnAction(e -> TailwindFX.theme(scene).light().apply());
        darkBtn.setOnAction(e  -> TailwindFX.theme(scene).dark().apply());
        blueBtn.setOnAction(e  -> TailwindFX.theme(scene).preset("blue").apply());
        roseBtn.setOnAction(e  -> TailwindFX.theme(scene).preset("rose").apply());

        // Scoped theme demo
        StackPane scopeDemo = new StackPane();
        scopeDemo.setPrefSize(180, 60);
        TailwindFX.scope(scopeDemo).preset("dark").apply();
        Label scopeLabel = new Label("Scoped Dark Theme");
        TailwindFX.apply(scopeLabel, "text-white", "font-bold");
        scopeDemo.getChildren().add(scopeLabel);
        TailwindFX.apply(scopeDemo, "rounded-lg", "p-3");

        HBox btns = new HBox(8, lightBtn, darkBtn, blueBtn, roseBtn);
        VBox section = new VBox(8, title, btns, scopeDemo);
        TailwindFX.apply(section, "card");
        return new HBox(section);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private VBox buildCard(String titleText, String bodyText, String btnClass, String btnLabel) {
        Label t = new Label(titleText);
        TailwindFX.apply(t, "font-bold", "text-lg", "mb-1");

        Label b = new Label(bodyText);
        TailwindFX.apply(b, "text-sm", "text-gray-500", "mb-3");

        Button btn = new Button(btnLabel);
        TailwindFX.apply(btn, btnClass);

        VBox card = new VBox(4, t, b, btn);
        card.setPrefWidth(200);
        return card;
    }

    private Label badge(String text, String cls) {
        Label l = new Label(text);
        TailwindFX.apply(l, cls);
        return l;
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        TailwindFX.apply(l, "font-bold", "text-lg", "text-gray-700", "mb-1");
        return l;
    }

    private Region colorPane(String hex, String labelText, double height) {
        StackPane pane = new StackPane();
        pane.setPrefHeight(height);
        TailwindFX.jit(pane, "bg-[" + hex + "]");
        Label label = new Label(labelText);
        TailwindFX.apply(label, "text-white", "font-bold", "text-sm");
        pane.getChildren().add(label);
        return pane;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
