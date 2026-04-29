package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.color.ColorPalette;
import io.github.yasmramos.tailwindfx.animation.FxAnimation;
import io.github.yasmramos.tailwindfx.TailwindFX;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * ComponentFactory — high-level UI components built from TailwindFX utilities.
 *
 * <p>Provides ready-made components that would otherwise require significant
 * boilerplate in JavaFX: cards, badges, modals, drawers, tooltips, and
 * data tables. All components apply TailwindFX utility classes and remain
 * fully customizable after creation.
 *
 * <h3>Glassmorphism + Neumorphism presets</h3>
 * <pre>
 * // Glass card (backdrop blur + semi-transparent):
 * VBox card = ComponentFactory.glass()
 *     .blur(12).opacity(0.25).border(true).build();
 *
 * // Neumorphic button:
 * Button btn = ComponentFactory.neumorphic(new Button("Click"))
 *     .light("#e0e5ec").shadow(true).build();
 * </pre>
 *
 * <h3>Cards and badges</h3>
 * <pre>
 * VBox card     = ComponentFactory.card().title("Revenue").body(chart).build();
 * Label badge   = ComponentFactory.badge("NEW", "blue");
 * Label pill    = ComponentFactory.pill("Active", "green");
 * </pre>
 *
 * <h3>Modals and drawers</h3>
 * <pre>
 * StackPane modal  = ComponentFactory.modal(content).show(sceneRoot);
 * VBox drawer      = ComponentFactory.drawer(Direction.LEFT, 280).show(sceneRoot);
 * </pre>
 *
 * <h3>Data table</h3>
 * <pre>
 * TableView&lt;Person&gt; table = ComponentFactory.dataTable(Person.class)
 *     .col("Name",  p -> p.getName())
 *     .col("Email", p -> p.getEmail())
 *     .striped(true).hoverable(true)
 *     .build();
 * </pre>
 */
public final class ComponentFactory {

    public ComponentFactory() {}

    // =========================================================================
    // Card
    // =========================================================================

    /** Builder for styled card containers. */
    public static CardBuilder card() { return new CardBuilder(); }

    public static final class CardBuilder {
        private String  title       = null;
        private Node    body        = null;
        private Node    footer      = null;
        private boolean shadow      = true;
        private boolean border      = false;
        private boolean hoverable   = false;
        private double  padding     = 4;   // Tailwind units
        private double  radius      = 12;  // px

        public CardBuilder title(String t)     { this.title = t;     return this; }
        public CardBuilder body(Node n)        { this.body = n;       return this; }
        public CardBuilder footer(Node n)      { this.footer = n;     return this; }
        public CardBuilder shadow(boolean s)   { this.shadow = s;     return this; }
        public CardBuilder border(boolean b)   { this.border = b;     return this; }
        public CardBuilder hoverable(boolean h){ this.hoverable = h;  return this; }
        public CardBuilder padding(double p)   { this.padding = p;    return this; }
        public CardBuilder radius(double r)    { this.radius = r;     return this; }

        public VBox build() {
            VBox card = new VBox();
            TailwindFX.apply(card, "bg-white", shadow ? "shadow-md" : "", "rounded-lg");
            card.setStyle("-fx-background-radius: " + radius + "px;"
                + (border ? " -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: " + radius + "px;" : ""));
            double pad = padding * 4;
            card.setPadding(new Insets(pad));
            card.setSpacing(12);

            if (title != null) {
                Label lbl = new Label(title);
                TailwindFX.apply(lbl, "text-lg", "font-semibold", "text-gray-900");
                card.getChildren().add(lbl);
            }
            if (body != null) {
                VBox.setVgrow(body, Priority.ALWAYS);
                card.getChildren().add(body);
            }
            if (footer != null) {
                Region spacer = new Region();
                VBox.setVgrow(spacer, Priority.ALWAYS);
                if (body != null) card.getChildren().add(spacer);
                TailwindFX.apply(footer, "pt-3", "border-t", "border-gray-100");
                card.getChildren().add(footer);
            }

            if (hoverable) {
                FxAnimation.onHoverLift(card, -3);
            }
            return card;
        }
    }

    // =========================================================================
    // Badge / Pill
    // =========================================================================

    /**
     * Creates a small badge label (e.g. NEW, BETA, PRO).
     *
     * @param text  badge text
     * @param color Tailwind color name (e.g. {@code "blue"}, {@code "green"})
     * @return styled Label
     */
    public static Label badge(String text, String color) {
        Label lbl = new Label(text.toUpperCase());
        String bg   = ColorPalette.hex(color, 100);
        String fg   = ColorPalette.hex(color, 700);
        lbl.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: %s; -fx-font-size: 10px;"
            + " -fx-font-weight: bold; -fx-padding: 2 8 2 8;"
            + " -fx-background-radius: 4px;", bg, fg));
        return lbl;
    }

    /**
     * Creates a rounded pill / chip label.
     *
     * @param text  pill text
     * @param color Tailwind color name
     * @return styled Label
     */
    public static Label pill(String text, String color) {
        Label lbl = badge(text, color);
        lbl.setStyle(lbl.getStyle().replace("border-radius: 4px", "")
            + " -fx-background-radius: 999px;");
        return lbl;
    }

    // =========================================================================
    // Tooltip (styled)
    // =========================================================================

    /**
     * Installs a styled tooltip on a node.
     *
     * <pre>
     * ComponentFactory.tooltip(button, "Click to save");
     * ComponentFactory.tooltip(button, "Click to save", "dark"); // dark style
     * </pre>
     *
     * @param node  the node to attach the tooltip to
     * @param text  tooltip text
     * @return the installed {@link Tooltip}
     */
    public static Tooltip tooltip(Node node, String text) {
        return tooltip(node, text, "dark");
    }

    /**
     * Installs a styled tooltip with a color variant.
     *
     * @param node   the node to attach the tooltip to
     * @param text   tooltip text
     * @param style  {@code "dark"} (default) or {@code "light"}
     * @return the installed {@link Tooltip}
     */
    public static Tooltip tooltip(Node node, String text, String style) {
        Tooltip tip = new Tooltip(text);
        if ("dark".equals(style)) {
            tip.setStyle("-fx-background-color: #111827; -fx-text-fill: #f9fafb;"
                + " -fx-font-size: 12px; -fx-padding: 6 10 6 10;"
                + " -fx-background-radius: 6px;");
        } else {
            tip.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #374151;"
                + " -fx-border-color: #e5e7eb; -fx-border-width: 1px;"
                + " -fx-font-size: 12px; -fx-padding: 6 10 6 10;"
                + " -fx-background-radius: 6px;");
        }
        Tooltip.install(node, tip);
        return tip;
    }

    // =========================================================================
    // Modal
    // =========================================================================

    /** Builder for modal overlay dialogs. */
    public static ModalBuilder modal(Node content) {
        return new ModalBuilder(content);
    }

    public static final class ModalBuilder {
        private final Node content;
        private boolean dismissOnOverlay = true;
        private boolean animated         = true;
        private double  maxWidth         = 480;
        private Runnable onDismiss       = null;

        private ModalBuilder(Node content) { this.content = content; }

        public ModalBuilder dismissOnOverlay(boolean d) { this.dismissOnOverlay = d; return this; }
        public ModalBuilder animated(boolean a)         { this.animated = a;         return this; }
        public ModalBuilder maxWidth(double w)          { this.maxWidth = w;         return this; }
        public ModalBuilder onDismiss(Runnable r)       { this.onDismiss = r;        return this; }

        /**
         * Adds the modal overlay to the given root pane and returns the overlay.
         * Remove the overlay from root to close.
         *
         * @param root the scene root (must be a Pane)
         * @return the overlay StackPane (remove from root to close)
         */
        public StackPane show(Pane root) {
            // Dim overlay
            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
            overlay.setPrefSize(root.getWidth(), root.getHeight());
            overlay.maxWidthProperty().bind(root.widthProperty());
            overlay.maxHeightProperty().bind(root.heightProperty());

            // Modal panel
            VBox panel = new VBox();
            panel.setMaxWidth(maxWidth);
            TailwindFX.apply(panel, "bg-white", "rounded-xl", "shadow-xl");
            panel.setStyle("-fx-background-radius: 16px; -fx-padding: 24;");
            panel.getChildren().add(content);

            overlay.getChildren().add(panel);
            StackPane.setAlignment(panel, Pos.CENTER);

            if (dismissOnOverlay) {
                overlay.setOnMouseClicked(e -> {
                    if (e.getTarget() == overlay) {
                        if (onDismiss != null) onDismiss.run();
                        root.getChildren().remove(overlay);
                    }
                });
            }

            root.getChildren().add(overlay);

            if (animated) {
                FxAnimation.fadeIn(overlay, 180).play();
                FxAnimation.scaleIn(panel, 180).play();
            }
            return overlay;
        }
    }

    // =========================================================================
    // Drawer
    // =========================================================================

    /** Slide-in drawer direction. */
    public enum DrawerSide { LEFT, RIGHT, TOP, BOTTOM }

    /** Builder for slide-in drawer panels. */
    public static DrawerBuilder drawer(DrawerSide side, double size) {
        return new DrawerBuilder(side, size);
    }

    public static final class DrawerBuilder {
        private final DrawerSide side;
        private final double     size;
        private boolean  animated   = true;
        private double   durationMs = 220;

        private DrawerBuilder(DrawerSide side, double size) {
            this.side = side; this.size = size;
        }

        public DrawerBuilder animated(boolean a)    { this.animated = a;    return this; }
        public DrawerBuilder duration(double ms)    { this.durationMs = ms; return this; }

        /**
         * Adds the drawer to the given root pane and returns it.
         *
         * @param root    scene root pane
         * @param content drawer content
         * @return the drawer panel (remove from root to close)
         */
        public VBox show(Pane root, Node content) {
            VBox drawer = new VBox();
            TailwindFX.apply(drawer, "bg-white", "shadow-xl");

            switch (side) {
                case LEFT  -> {
                    drawer.setStyle("-fx-padding: 16; -fx-background-radius: 0 8px 8px 0;");
                    drawer.setPrefWidth(size);
                    drawer.setPrefHeight(root.getHeight());
                    root.getChildren().add(drawer);
                    if (animated) {
                        drawer.setTranslateX(-size);
                        TailwindFX.transition(drawer, (int) durationMs,
                            new javafx.animation.KeyValue(drawer.translateXProperty(), 0,
                                javafx.animation.Interpolator.EASE_OUT)).play();
                    }
                }
                case RIGHT -> {
                    drawer.setStyle("-fx-padding: 16; -fx-background-radius: 8px 0 0 8px;");
                    drawer.setPrefWidth(size);
                    drawer.setPrefHeight(root.getHeight());
                    drawer.setLayoutX(root.getWidth() - size);
                    root.getChildren().add(drawer);
                    if (animated) {
                        drawer.setTranslateX(size);
                        TailwindFX.transition(drawer, (int) durationMs,
                            new javafx.animation.KeyValue(drawer.translateXProperty(), 0,
                                javafx.animation.Interpolator.EASE_OUT)).play();
                    }
                }
                case BOTTOM -> {
                    drawer.setStyle("-fx-padding: 16; -fx-background-radius: 8px 8px 0 0;");
                    drawer.setPrefHeight(size);
                    drawer.setPrefWidth(root.getWidth());
                    drawer.setLayoutY(root.getHeight() - size);
                    root.getChildren().add(drawer);
                    if (animated) {
                        drawer.setTranslateY(size);
                        TailwindFX.transition(drawer, (int) durationMs,
                            new javafx.animation.KeyValue(drawer.translateYProperty(), 0,
                                javafx.animation.Interpolator.EASE_OUT)).play();
                    }
                }
                case TOP -> {
                    drawer.setStyle("-fx-padding: 16; -fx-background-radius: 0 0 8px 8px;");
                    drawer.setPrefHeight(size);
                    drawer.setPrefWidth(root.getWidth());
                    root.getChildren().add(drawer);
                    if (animated) {
                        drawer.setTranslateY(-size);
                        TailwindFX.transition(drawer, (int) durationMs,
                            new javafx.animation.KeyValue(drawer.translateYProperty(), 0,
                                javafx.animation.Interpolator.EASE_OUT)).play();
                    }
                }
            }
            drawer.getChildren().add(content);
            return drawer;
        }
    }

    // =========================================================================
    // Glassmorphism
    // =========================================================================

    /** Builder for glass-morphism containers. */
    public static GlassBuilder glass() { return new GlassBuilder(); }

    public static final class GlassBuilder {
        private double  blur        = 10;
        private double  opacity     = 0.2;
        private boolean border      = true;
        private String  borderColor = "rgba(255,255,255,0.3)";
        private double  padding     = 20;
        private double  radius      = 16;

        public GlassBuilder blur(double b)          { this.blur = b;          return this; }
        public GlassBuilder opacity(double o)        { this.opacity = o;        return this; }
        public GlassBuilder border(boolean b)        { this.border = b;         return this; }
        public GlassBuilder borderColor(String c)    { this.borderColor = c;    return this; }
        public GlassBuilder padding(double p)        { this.padding = p;        return this; }
        public GlassBuilder radius(double r)         { this.radius = r;         return this; }

        public VBox build() {
            VBox pane = new VBox();
            pane.setSpacing(12);
            pane.setPadding(new Insets(padding));

            // Semi-transparent white background
            String bg = "rgba(255,255,255," + opacity + ")";
            String borderStyle = border
                ? " -fx-border-color: " + borderColor + "; -fx-border-width: 1px;"
                  + " -fx-border-radius: " + radius + "px;" : "";
            pane.setStyle(
                "-fx-background-color: " + bg + ";"
                + " -fx-background-radius: " + radius + "px;"
                + borderStyle
            );
            // BoxBlur for the glass effect
            TailwindFX.backdropBlur(pane, blur);
            return pane;
        }
    }

    // =========================================================================
    // Neumorphism
    // =========================================================================

    /**
     * Applies neumorphic styling to any node.
     *
     * <pre>
     * Button btn = new Button("Save");
     * ComponentFactory.neumorphic(btn).apply();
     * </pre>
     */
    public static NeumorphicBuilder neumorphic(Region node) {
        return new NeumorphicBuilder(node);
    }

    public static final class NeumorphicBuilder {
        private final Region node;
        private String background = "#e0e5ec";
        private double radius     = 12;
        private boolean pressed   = false;

        private NeumorphicBuilder(Region node) { this.node = node; }

        public NeumorphicBuilder background(String hex) { this.background = hex; return this; }
        public NeumorphicBuilder radius(double r)       { this.radius = r;       return this; }
        public NeumorphicBuilder pressed(boolean p)     { this.pressed = p;      return this; }

        public Region apply() {
            // Neumorphism: two shadows — one light (top-left), one dark (bottom-right)
            // JavaFX can only do one DropShadow effect natively — we approximate with style
            String shadowLight = pressed ? "inset 3px 3px 6px #b8bec7" : "-6px -6px 12px #ffffff";
            String shadowDark  = pressed ? "inset -3px -3px 6px #ffffff" : "6px 6px 12px #b8bec7";

            // Apply via -fx-effect (single shadow, approximate)
            node.setStyle(
                "-fx-background-color: " + background + ";"
                + " -fx-background-radius: " + radius + "px;"
                + " -fx-effect: dropshadow(gaussian, #b8bec7, 12, 0, 6, 6);"
            );

            // Pressed state toggle on click
            node.setOnMousePressed(e -> {
                node.setStyle(node.getStyle()
                    .replace("dropshadow(gaussian, #b8bec7, 12, 0, 6, 6)",
                             "dropshadow(gaussian, #b8bec7, 4, 0, 2, 2)"));
            });
            node.setOnMouseReleased(e -> {
                node.setStyle(node.getStyle()
                    .replace("dropshadow(gaussian, #b8bec7, 4, 0, 2, 2)",
                             "dropshadow(gaussian, #b8bec7, 12, 0, 6, 6)"));
            });
            return node;
        }
    }

    // =========================================================================
    // DataTable
    // =========================================================================

    /**
     * Builder for a styled, sortable data table.
     *
     * <pre>
     * ObservableList&lt;Person&gt; data = FXCollections.observableArrayList(people);
     *
     * FxDataTable&lt;Person&gt; table = ComponentFactory.dataTable(Person.class)
     *     .col("Name",   Person::name)
     *     .col("Email",  Person::email)
     *     .col("Role",   Person::role)
     *     .striped(true)
     *     .hoverable(true)
     *     .items(data)
     *     .build();
     * </pre>
     */
    public static <T> DataTableBuilder<T> dataTable(Class<T> type) {
        return new DataTableBuilder<>();
    }

    public static final class DataTableBuilder<T> {
        private final FxDataTable.Builder<T> delegate = FxDataTable.of(null);
        private final java.util.List<String> styleList = new java.util.ArrayList<>();
        private boolean hoverable = true;
        private javafx.collections.ObservableList<T> pendingItems;

        /**
         * Adds a column.
         *
         * @param header    column header text
         * @param extractor function to extract the display value from a row object
         */
        public DataTableBuilder<T> col(String header, Function<T, Object> extractor) {
            delegate.column(header, t -> extractor.apply(t).toString());
            return this;
        }

        public DataTableBuilder<T> striped(boolean s) {
            if (s) styleList.add("table-striped");
            else styleList.remove("table-striped");
            return this;
        }

        public DataTableBuilder<T> hoverable(boolean h) {
            this.hoverable = h;
            return this;
        }

        public DataTableBuilder<T> bordered(boolean b) {
            if (b) styleList.add("table-bordered");
            else styleList.remove("table-bordered");
            return this;
        }

        public DataTableBuilder<T> rowHeight(double h) {
            // FxDataTable doesn't support rowHeight directly
            return this;
        }

        public DataTableBuilder<T> items(javafx.collections.ObservableList<T> data) {
            this.pendingItems = data;
            return this;
        }

        public DataTableBuilder<T> searchable(boolean s) {
            delegate.searchable(s);
            return this;
        }

        public DataTableBuilder<T> pageSize(int size) {
            delegate.pageSize(size);
            return this;
        }

        public DataTableBuilder<T> style(String... styles) {
            for (String s : styles) {
                if (s != null && !s.isEmpty() && !styleList.contains(s)) {
                    styleList.add(s);
                }
            }
            return this;
        }

        @SuppressWarnings("unchecked")
        public FxDataTable<T> build() {
            delegate.style(styleList.toArray(new String[0]));
            FxDataTable<T> table = delegate.build();
            if (pendingItems != null) {
                table.setItems(pendingItems);
            }
            return table;
        }
    }

    // =========================================================================
    // Divider
    // =========================================================================

    /** Creates a horizontal divider line. */
    public static Region divider() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setMaxHeight(1);
        r.setStyle("-fx-background-color: #e5e7eb;");
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    /** Creates a vertical divider line. */
    public static Region dividerV() {
        Region r = new Region();
        r.setPrefWidth(1);
        r.setMaxWidth(1);
        r.setStyle("-fx-background-color: #e5e7eb;");
        VBox.setVgrow(r, Priority.ALWAYS);
        return r;
    }

    // =========================================================================
    // Avatar / Initials
    // =========================================================================

    /**
     * Creates a circular avatar showing initials.
     *
     * @param initials 1-2 character initials (e.g. "JD")
     * @param color    Tailwind color name for background
     * @param size     diameter in px
     */
    public static StackPane avatar(String initials, String color, double size) {
        StackPane pane = new StackPane();
        String bg = ColorPalette.hex(color, 500);
        String fg = "#ffffff";
        pane.setStyle(
            "-fx-background-color: " + bg + ";"
            + " -fx-background-radius: 999px;"
            + " -fx-min-width: " + size + "px; -fx-min-height: " + size + "px;"
            + " -fx-max-width: " + size + "px; -fx-max-height: " + size + "px;"
        );
        Label lbl = new Label(initials.substring(0, Math.min(2, initials.length())).toUpperCase());
        lbl.setStyle("-fx-text-fill: " + fg + "; -fx-font-size: " + (size * 0.35) + "px; -fx-font-weight: bold;");
        pane.getChildren().add(lbl);
        return pane;
    }

    // =========================================================================
    // Alert / Notification bar
    // =========================================================================

    /**
     * Creates a styled alert bar.
     *
     * @param message  alert text
     * @param type     {@code "info"}, {@code "success"}, {@code "warn"}, or {@code "error"}
     */
    public static HBox alert(String message, String type) {
        record AlertStyle(String bg, String fg, String icon) {}
        AlertStyle style = switch (type) {
            case "success" -> new AlertStyle("#dcfce7", "#166534", "✓");
            case "warn"    -> new AlertStyle("#fef9c3", "#854d0e", "⚠");
            case "error"   -> new AlertStyle("#fee2e2", "#991b1b", "✕");
            default        -> new AlertStyle("#dbeafe", "#1e40af", "ℹ");
        };

        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setStyle("-fx-background-color: " + style.bg() + ";"
            + " -fx-background-radius: 8px;");

        Label icon = new Label(style.icon());
        icon.setStyle("-fx-text-fill: " + style.fg() + "; -fx-font-weight: bold;");

        Label msg = new Label(message);
        msg.setStyle("-fx-text-fill: " + style.fg() + "; -fx-font-size: 13px;");

        bar.getChildren().addAll(icon, msg);
        return bar;
    }
}
