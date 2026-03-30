package tailwindfx.examples;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import tailwindfx.TailwindFX;
import tailwindfx.ComponentFactory;

/**
 * Dashboard Example — Enhanced Design.
 *
 * Features demonstrated:
 * - Modern sidebar with gradient and icons
 * - Top bar with breadcrumbs and date
 * - Stats cards with colored icons
 * - Chart with simulated bars
 * - Data table with avatars
 * - Product cards with hover effects
 * - Chat/messages panel
 * - Dark mode support
 */
public class Dashboard {

    private static boolean darkModeEnabled = false;

    public static BorderPane create() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f3f4f6;");

        // Sidebar with modern design
        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        // Main content
        VBox mainContent = new VBox(0);
        mainContent.setStyle("-fx-background-color: #f9fafb;");

        // Top bar
        HBox topBar = createTopBar();
        mainContent.getChildren().add(topBar);

        // Scrollable content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox content = createContent();
        scrollPane.setContent(content);

        mainContent.getChildren().add(scrollPane);
        mainLayout.setCenter(mainContent);

        return mainLayout;
    }

    public static VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));

        // Welcome banner
        content.getChildren().add(createWelcomeBanner());

        // Stats row with gradient cards
        content.getChildren().add(createStatsRow());

        // Main content: Chart + Activity + Chat
        HBox middleRow = new HBox(20);
        middleRow.getChildren().addAll(
            createChartSection(),
            createActivityAndChat()
        );
        HBox.setHgrow(createChartSection(), Priority.ALWAYS);
        HBox.setHgrow(createActivityAndChat(), Priority.SOMETIMES);

        content.getChildren().add(middleRow);

        // Data table
        content.getChildren().add(createDataTableSection());

        // Products grid
        content.getChildren().add(createProductsSection());

        return content;
    }

    // =========================================================================
    // Sidebar Navigation - Enhanced
    // =========================================================================

    private static VBox createSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(280);
        sidebar.setStyle("-fx-background-color: linear-gradient(to bottom, #1e293b, #0f172a);");
        sidebar.setPadding(new Insets(0));

        // Header with gradient
        VBox header = new VBox(16);
        header.setPadding(new Insets(24, 20, 24, 20));
        header.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(59,130,246,0.1), transparent);");

        // Logo
        HBox brand = new HBox(14);
        brand.setAlignment(Pos.CENTER_LEFT);

        StackPane logo = new StackPane();
        logo.setPrefSize(44, 44);
        logo.setStyle("-fx-background-color: linear-gradient(to bottom right, #3b82f6, #8b5cf6);");
        TailwindFX.apply(logo, "rounded-xl", "shadow-lg");

        Label logoText = new Label("T");
        TailwindFX.apply(logoText, "text-2xl", "font-bold", "text-white");
        logo.getChildren().add(logoText);

        VBox brandText = new VBox(2);
        Label brandName = new Label("TailwindFX");
        TailwindFX.apply(brandName, "text-xl", "font-bold", "text-white");

        Label brandSubtitle = new Label("Dashboard");
        TailwindFX.apply(brandSubtitle, "text-xs", "text-gray-400");

        brandText.getChildren().addAll(brandName, brandSubtitle);
        brand.getChildren().addAll(logo, brandText);

        header.getChildren().add(brand);
        sidebar.getChildren().add(header);

        // Navigation menu
        VBox menu = new VBox(2);
        menu.setPadding(new Insets(0, 12, 0, 12));

        menu.getChildren().addAll(
            createMenuItem("📊", "Dashboard", true),
            createMenuItem("👥", "Users", false),
            createMenuItem("📦", "Products", false),
            createMenuItem("🛒", "Orders", false),
            createMenuItem("💬", "Messages", false, 3),
            createMenuItem("📈", "Analytics", false),
            createSeparator(),
            createMenuItem("⚙️", "Settings", false),
            createMenuItem("❓", "Help", false),
            createMenuItem("🚪", "Logout", false)
        );

        sidebar.getChildren().add(menu);

        // User profile at bottom with gradient
        VBox userProfile = createUserProfile();
        sidebar.getChildren().add(new Region());
        VBox.setVgrow(new Region(), Priority.ALWAYS);
        sidebar.getChildren().add(userProfile);

        return sidebar;
    }

    private static HBox createMenuItem(String icon, String text, boolean active) {
        return createMenuItem(icon, text, active, 0);
    }

    private static HBox createMenuItem(String icon, String text, boolean active, int badge) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(14, 16, 14, 16));
        item.setCursor(javafx.scene.Cursor.HAND);

        Label iconLabel = new Label(icon);
        TailwindFX.apply(iconLabel, "text-lg");

        Label label = new Label(text);
        TailwindFX.apply(label, "text-sm", "font-medium");

        if (active) {
            item.setStyle("-fx-background-color: rgba(59,130,246,0.2); -fx-background-radius: 8;");
            TailwindFX.apply(iconLabel, "text-white");
            TailwindFX.apply(label, "text-white");
        } else {
            TailwindFX.apply(iconLabel, "text-gray-400");
            TailwindFX.apply(label, "text-gray-400");
            
            item.setOnMouseEntered(e -> {
                item.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8;");
                iconLabel.setStyle("-fx-text-fill: white;");
                label.setStyle("-fx-text-fill: white;");
            });
            item.setOnMouseExited(e -> {
                item.setStyle("");
                iconLabel.setStyle("-fx-text-fill: -color-gray-400;");
                label.setStyle("-fx-text-fill: -color-gray-400;");
            });
        }

        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().addAll(iconLabel, label);

        item.getChildren().add(content);

        if (badge > 0) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            item.getChildren().add(spacer);

            StackPane badgePane = new StackPane();
            badgePane.setPrefSize(22, 22);
            badgePane.setStyle("-fx-background-color: #ef4444;");
            TailwindFX.apply(badgePane, "rounded-full");

            Label badgeLabel = new Label(String.valueOf(badge));
            TailwindFX.apply(badgeLabel, "text-xs", "font-bold", "text-white");
            badgePane.getChildren().add(badgeLabel);

            item.getChildren().add(badgePane);
        }

        return item;
    }

    private static Region createSeparator() {
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setStyle("-fx-background-color: rgba(255,255,255,0.1);");
        separator.setPadding(new Insets(12, 16, 12, 16));
        return separator;
    }

    private static VBox createUserProfile() {
        VBox profile = new VBox(12);
        profile.setAlignment(Pos.CENTER_LEFT);
        profile.setPadding(new Insets(20));
        profile.setStyle("-fx-background-color: rgba(255,255,255,0.05);");

        HBox info = new HBox(12);
        info.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = ComponentFactory.avatar("JD", "blue", 44);
        TailwindFX.apply(avatar, "rounded-full", "shadow-md");

        VBox text = new VBox(4);
        Label name = new Label("John Doe");
        TailwindFX.apply(name, "text-sm", "font-semibold", "text-white");

        Label email = new Label("john@example.com");
        TailwindFX.apply(email, "text-xs", "text-gray-400");

        text.getChildren().addAll(name, email);
        info.getChildren().addAll(avatar, text);

        profile.getChildren().add(info);

        // Settings icon
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button settingsBtn = new Button("⚙️");
        settingsBtn.setStyle("-fx-background-color: transparent;");
        TailwindFX.apply(settingsBtn, "text-sm");
        settingsBtn.setCursor(javafx.scene.Cursor.HAND);

        Button logoutBtn = new Button("🚪");
        logoutBtn.setStyle("-fx-background-color: transparent;");
        TailwindFX.apply(logoutBtn, "text-sm");
        logoutBtn.setCursor(javafx.scene.Cursor.HAND);

        actions.getChildren().addAll(settingsBtn, logoutBtn);
        profile.getChildren().add(actions);

        return profile;
    }

    // =========================================================================
    // Top Bar - Enhanced with breadcrumbs
    // =========================================================================

    private static HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(20, 32, 20, 32));
        topBar.setStyle("-fx-background-color: white;");
        topBar.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 15, 0, 0, 0);");

        // Breadcrumbs
        HBox breadcrumbs = new HBox(8);
        breadcrumbs.setAlignment(Pos.CENTER_LEFT);

        Label home = new Label("🏠 Home");
        TailwindFX.apply(home, "text-sm", "text-gray-500");

        Label separator = new Label("/");
        TailwindFX.apply(separator, "text-sm", "text-gray-400");

        Label dashboard = new Label("Dashboard");
        TailwindFX.apply(dashboard, "text-sm", "font-medium", "text-gray-900");

        breadcrumbs.getChildren().addAll(home, separator, dashboard);

        // Right side actions
        HBox actions = new HBox(16);
        actions.setAlignment(Pos.CENTER);

        // Date display
        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER_RIGHT);
        Label dateLabel = new Label("Monday, March 30, 2026");
        TailwindFX.apply(dateLabel, "text-xs", "text-gray-500");
        Label timeLabel = new Label("10:30 AM");
        TailwindFX.apply(timeLabel, "text-xs", "font-medium", "text-gray-700");
        dateBox.getChildren().addAll(dateLabel, timeLabel);

        // Separator line
        Region lineSeparator = new Region();
        lineSeparator.setPrefWidth(1);
        lineSeparator.setPrefHeight(30);
        lineSeparator.setStyle("-fx-background-color: #e5e7eb;");
        lineSeparator.setPadding(new Insets(0, 16, 0, 16));

        // Theme toggle
        Button themeToggle = createThemeToggle();

        // Notifications
        StackPane notificationBtn = createNotificationButton();

        // User menu
        HBox userMenu = new HBox(12);
        userMenu.setAlignment(Pos.CENTER);
        userMenu.setPadding(new Insets(8, 16, 8, 16));
        userMenu.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 12;");
        userMenu.setCursor(javafx.scene.Cursor.HAND);

        VBox userText = new VBox(2);
        userText.setAlignment(Pos.CENTER_RIGHT);
        Label userName = new Label("John Doe");
        TailwindFX.apply(userName, "text-sm", "font-semibold", "text-gray-700");
        Label userRole = new Label("Administrator");
        TailwindFX.apply(userRole, "text-xs", "text-gray-500");
        userText.getChildren().addAll(userName, userRole);

        StackPane userAvatar = ComponentFactory.avatar("JD", "blue", 40);
        TailwindFX.apply(userAvatar, "rounded-full", "shadow-sm");

        userMenu.getChildren().addAll(userText, userAvatar);

        actions.getChildren().addAll(dateBox, lineSeparator, themeToggle, notificationBtn, userMenu);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(breadcrumbs, spacer, actions);
        return topBar;
    }

    private static Button createThemeToggle() {
        Button toggle = new Button(darkModeEnabled ? "🌙" : "☀️");
        toggle.setPrefSize(44, 44);
        toggle.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 12;");
        toggle.setCursor(javafx.scene.Cursor.HAND);
        TailwindFX.apply(toggle, "text-xl");
        toggle.setOnMouseEntered(e -> toggle.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 12;"));
        toggle.setOnMouseExited(e -> toggle.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 12;"));
        toggle.setOnAction(e -> {
            darkModeEnabled = !darkModeEnabled;
            toggle.setText(darkModeEnabled ? "🌙" : "☀️");
            if (toggle.getScene() != null) {
                Node root = toggle.getScene().getRoot();
                if (darkModeEnabled) {
                    root.getStyleClass().add("dark");
                } else {
                    root.getStyleClass().remove("dark");
                }
            }
        });
        return toggle;
    }

    private static StackPane createNotificationButton() {
        StackPane btnWrapper = new StackPane();
        btnWrapper.setPrefSize(44, 44);
        btnWrapper.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 12;");
        btnWrapper.setCursor(javafx.scene.Cursor.HAND);

        Label bell = new Label("🔔");
        TailwindFX.apply(bell, "text-xl");
        btnWrapper.getChildren().add(bell);

        // Pulse animation effect (simulated with red dot)
        StackPane badge = new StackPane();
        badge.setPrefSize(12, 12);
        badge.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 6;");

        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        btnWrapper.getChildren().add(badge);

        btnWrapper.setOnMouseEntered(e -> btnWrapper.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 12;"));
        btnWrapper.setOnMouseExited(e -> btnWrapper.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 12;"));

        return btnWrapper;
    }

    // =========================================================================
    // Welcome Banner
    // =========================================================================

    private static HBox createWelcomeBanner() {
        HBox banner = new HBox(24);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(24, 32, 24, 32));
        banner.setStyle("-fx-background-color: linear-gradient(to right, #3b82f6, #8b5cf6); -fx-background-radius: 16;");
        banner.setStyle("-fx-background-color: linear-gradient(to right, #3b82f6, #8b5cf6);");

        VBox text = new VBox(8);
        Label title = new Label("Welcome back, John! 👋");
        TailwindFX.apply(title, "text-2xl", "font-bold", "text-white");

        Label subtitle = new Label("Here's what's happening with your business today.");
        TailwindFX.apply(subtitle, "text-sm", "text-blue-100");

        text.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button actionBtn = new Button("View Reports");
        actionBtn.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        actionBtn.setPadding(new Insets(12, 24, 12, 24));
        actionBtn.setCursor(javafx.scene.Cursor.HAND);
        Label btnLabel = new Label("📊 View Reports");
        TailwindFX.apply(btnLabel, "text-sm", "font-semibold");
        btnLabel.setStyle("-fx-text-fill: #3b82f6;");
        actionBtn.setGraphic(btnLabel);

        banner.getChildren().addAll(text, spacer, actionBtn);
        return banner;
    }

    // =========================================================================
    // Stats Row - Enhanced with gradient cards
    // =========================================================================

    private static HBox createStatsRow() {
        HBox stats = new HBox(20);

        stats.getChildren().addAll(
            createStatCard("💰", "Total Revenue", "$45,231", "+20.1%", "green", "linear-gradient(to bottom right, #10b981, #059669)"),
            createStatCard("👥", "Active Users", "2,350", "+15.2%", "blue", "linear-gradient(to bottom right, #3b82f6, #2563eb)"),
            createStatCard("📊", "Bounce Rate", "12.5%", "-3.2%", "purple", "linear-gradient(to bottom right, #8b5cf6, #7c3aed)"),
            createStatCard("⏱️", "Avg. Session", "4m 32s", "+8.4%", "amber", "linear-gradient(to bottom right, #f59e0b, #d97706)")
        );

        return stats;
    }

    private static StackPane createStatCard(String icon, String title, String value, String change, String color, String gradient) {
        VBox card = new VBox(16);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 4);");
        card.setPadding(new Insets(24));
        card.setCursor(javafx.scene.Cursor.HAND);

        // Header with icon and title
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(48, 48);
        iconBox.setStyle("-fx-background-color: " + gradient + ";");
        iconBox.setStyle("-fx-background-color: linear-gradient(to bottom right, rgba(59,130,246,0.1), rgba(139,92,246,0.1)); -fx-background-radius: 12;");

        Label iconLabel = new Label(icon);
        TailwindFX.apply(iconLabel, "text-2xl");
        iconBox.getChildren().add(iconLabel);

        Label titleLabel = new Label(title);
        TailwindFX.apply(titleLabel, "text-sm", "font-medium", "text-gray-500");

        header.getChildren().addAll(iconBox, titleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Change badge
        boolean isPositive = change.startsWith("+");
        StackPane changeBadge = new StackPane();
        changeBadge.setPadding(new Insets(6, 12, 6, 12));
        changeBadge.setStyle("-fx-background-color: " + (isPositive ? "#d1fae5" : "#fee2e2") + "; -fx-background-radius: 20;");
        Label changeLabel = new Label(change);
        TailwindFX.apply(changeLabel, "text-xs", "font-bold", isPositive ? "text-green-700" : "text-red-700");
        changeBadge.getChildren().add(changeLabel);

        card.getChildren().addAll(header, changeBadge);

        // Value
        Label valueLabel = new Label(value);
        TailwindFX.apply(valueLabel, "text-3xl", "font-bold", "text-gray-900");
        card.getChildren().add(valueLabel);

        StackPane wrapper = new StackPane(card);
        HBox.setHgrow(wrapper, Priority.ALWAYS);
        return wrapper;
    }

    // =========================================================================
    // Chart Section - Enhanced with simulated bars
    // =========================================================================

    private static VBox createChartSection() {
        VBox section = new VBox(20);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 4);");
        section.setPadding(new Insets(24));

        // Section header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Revenue Overview");
        TailwindFX.apply(title, "text-lg", "font-bold", "text-gray-900");
        Label subtitle = new Label("Monthly revenue performance");
        TailwindFX.apply(subtitle, "text-sm", "text-gray-500");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox periodSelector = new HBox(2);
        periodSelector.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 10;");
        String[] periods = {"7D", "30D", "90D"};
        for (int i = 0; i < periods.length; i++) {
            Button btn = new Button(periods[i]);
            btn.setStyle(i == 0 ? "-fx-background-color: #3b82f6; -fx-background-radius: 8;" : "-fx-background-color: transparent;");
            btn.setPadding(new Insets(8, 16, 8, 16));
            btn.setCursor(javafx.scene.Cursor.HAND);
            Label btnLabel = new Label(periods[i]);
            TailwindFX.apply(btnLabel, "text-xs", "font-medium", i == 0 ? "text-white" : "text-gray-600");
            btn.setGraphic(btnLabel);
            periodSelector.getChildren().add(btn);
        }

        header.getChildren().addAll(titleBox, periodSelector);

        // Simulated bar chart
        HBox chartContainer = createSimulatedBarChart();

        section.getChildren().addAll(header, chartContainer);
        return section;
    }

    private static HBox createSimulatedBarChart() {
        HBox chart = new HBox(12);
        chart.setAlignment(Pos.BOTTOM_CENTER);
        chart.setPadding(new Insets(20, 10, 10, 10));
        chart.setPrefHeight(280);

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul"};
        double[] values = {0.6, 0.8, 0.45, 0.9, 0.75, 0.95, 0.85};

        for (int i = 0; i < months.length; i++) {
            VBox barContainer = new VBox(8);
            barContainer.setAlignment(Pos.BOTTOM_CENTER);
            barContainer.setPrefWidth(50);

            // Bar
            StackPane bar = new StackPane();
            bar.setPrefWidth(36);
            bar.setPrefHeight(values[i] * 200);
            bar.setStyle("-fx-background-color: linear-gradient(to top, #3b82f6, #8b5cf6); -fx-background-radius: 8 8 0 0;");
            bar.setCursor(javafx.scene.Cursor.HAND);

            // Hover effect
            bar.setOnMouseEntered(e -> bar.setStyle("-fx-background-color: linear-gradient(to top, #2563eb, #7c3aed); -fx-background-radius: 8 8 0 0;"));
            bar.setOnMouseExited(e -> bar.setStyle("-fx-background-color: linear-gradient(to top, #3b82f6, #8b5cf6); -fx-background-radius: 8 8 0 0;"));

            // Month label
            Label monthLabel = new Label(months[i]);
            TailwindFX.apply(monthLabel, "text-xs", "font-medium", "text-gray-500");

            barContainer.getChildren().addAll(bar, monthLabel);
            chart.getChildren().add(barContainer);
        }

        return chart;
    }

    // =========================================================================
    // Activity and Chat Panel
    // =========================================================================

    private static VBox createActivityAndChat() {
        VBox panel = new VBox(20);
        panel.setPrefWidth(380);

        // Recent Activity
        panel.getChildren().add(createRecentActivity());

        // Quick Chat
        panel.getChildren().add(createQuickChat());

        return panel;
    }

    private static VBox createRecentActivity() {
        VBox section = new VBox(16);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 4);");
        section.setPadding(new Insets(20));

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Recent Activity");
        TailwindFX.apply(title, "text-base", "font-bold", "text-gray-900");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewAll = new Button("View All");
        viewAll.setStyle("-fx-background-color: transparent;");
        viewAll.setCursor(javafx.scene.Cursor.HAND);
        Label viewAllLabel = new Label("→");
        TailwindFX.apply(viewAllLabel, "text-sm", "font-medium", "text-blue-600");
        viewAll.setGraphic(viewAllLabel);

        header.getChildren().addAll(title, viewAll);

        // Activity list
        VBox activities = new VBox(16);

        activities.getChildren().addAll(
            createActivityItem("👤", "New user registered", "2 min ago", "#10b981"),
            createActivityItem("💳", "Payment received", "15 min ago", "#3b82f6"),
            createActivityItem("📄", "Report generated", "1 hour ago", "#8b5cf6"),
            createActivityItem("🔄", "System update", "3 hours ago", "#f59e0b"),
            createActivityItem("💾", "Backup completed", "5 hours ago", "#6b7280")
        );

        section.getChildren().addAll(header, activities);
        return section;
    }

    private static HBox createActivityItem(String icon, String activity, String time, String color) {
        HBox item = new HBox(14);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(8, 0, 8, 0));
        item.setCursor(javafx.scene.Cursor.HAND);

        // Icon with background
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(40, 40);
        iconBox.setStyle("-fx-background-color: " + color + "20; -fx-background-radius: 10;");
        Label iconLabel = new Label(icon);
        TailwindFX.apply(iconLabel, "text-sm");
        iconBox.getChildren().add(iconLabel);

        // Text
        VBox text = new VBox(2);
        Label activityLabel = new Label(activity);
        TailwindFX.apply(activityLabel, "text-sm", "font-medium", "text-gray-900");
        Label timeLabel = new Label(time);
        TailwindFX.apply(timeLabel, "text-xs", "text-gray-500");
        text.getChildren().addAll(activityLabel, timeLabel);

        item.getChildren().addAll(iconBox, text);

        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8;"));
        item.setOnMouseExited(e -> item.setStyle(""));

        return item;
    }

    // =========================================================================
    // Quick Chat Panel
    // =========================================================================

    private static VBox createQuickChat() {
        VBox section = new VBox(16);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 4);");
        section.setPadding(new Insets(20));

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Messages");
        TailwindFX.apply(title, "text-base", "font-bold", "text-gray-900");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newMsg = new Button("+ New");
        newMsg.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 8;");
        newMsg.setPadding(new Insets(8, 16, 8, 16));
        newMsg.setCursor(javafx.scene.Cursor.HAND);
        Label newMsgLabel = new Label("+ New");
        TailwindFX.apply(newMsgLabel, "text-xs", "font-medium", "text-white");
        newMsg.setGraphic(newMsgLabel);

        header.getChildren().addAll(title, newMsg);

        // Chat list
        VBox chats = new VBox(12);

        chats.getChildren().addAll(
            createChatItem("JD", "John Smith", "Hey, how's the project going?", "2m", true),
            createChatItem("AS", "Alice Johnson", "Thanks for the update!", "15m", false),
            createChatItem("BW", "Bob Wilson", "Can we schedule a meeting?", "1h", false),
            createChatItem("CM", "Carol Martinez", "The design looks great! 🎉", "3h", false)
        );

        section.getChildren().addAll(header, chats);
        return section;
    }

    private static HBox createChatItem(String initials, String name, String lastMessage, String time, boolean unread) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12));
        item.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 12;");
        item.setCursor(javafx.scene.Cursor.HAND);

        // Avatar
        StackPane avatar = ComponentFactory.avatar(initials, unread ? "blue" : "gray", 44);
        TailwindFX.apply(avatar, "rounded-full");

        // Message preview
        VBox preview = new VBox(4);
        preview.setPrefWidth(200);

        HBox nameRow = new HBox(8);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(name);
        TailwindFX.apply(nameLabel, "text-sm", "font-semibold", "text-gray-900");

        Label timeLabel = new Label(time);
        TailwindFX.apply(timeLabel, "text-xs", "text-gray-400");

        nameRow.getChildren().addAll(nameLabel, timeLabel);

        Label messageLabel = new Label(lastMessage);
        TailwindFX.apply(messageLabel, "text-sm", "text-gray-500");
        messageLabel.setWrapText(true);

        preview.getChildren().addAll(nameRow, messageLabel);

        item.getChildren().addAll(avatar, preview);

        // Unread indicator
        if (unread) {
            StackPane dot = new StackPane();
            dot.setPrefSize(10, 10);
            dot.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 5;");
            item.getChildren().add(dot);
        }

        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 12;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 12;"));

        return item;
    }

    // =========================================================================
    // Data Table Section - Enhanced
    // =========================================================================

    private static VBox createDataTableSection() {
        VBox section = new VBox(0);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 4);");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 20, 24));

        VBox titleBox = new VBox(4);
        Label title = new Label("Recent Orders");
        TailwindFX.apply(title, "text-lg", "font-bold", "text-gray-900");
        Label subtitle = new Label("Manage your latest orders");
        TailwindFX.apply(subtitle, "text-sm", "text-gray-500");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(12);

        TextField search = new TextField();
        search.setPromptText("Search orders...");
        search.setPrefWidth(200);
        search.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 8 12 8 12;");
        TailwindFX.apply(search, "text-sm");

        Button filterBtn = new Button("⚡ Filter");
        filterBtn.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8;");
        filterBtn.setPadding(new Insets(10, 16, 10, 16));
        filterBtn.setCursor(javafx.scene.Cursor.HAND);
        Label filterLabel = new Label("⚡ Filter");
        TailwindFX.apply(filterLabel, "text-sm", "font-medium", "text-gray-700");
        filterBtn.setGraphic(filterLabel);

        actions.getChildren().addAll(search, filterBtn);

        header.getChildren().addAll(titleBox, actions);

        // Table
        VBox tableContainer = createDataTable();

        section.getChildren().addAll(header, tableContainer);
        return section;
    }

    private static VBox createDataTable() {
        VBox table = new VBox(0);

        // Table header
        HBox headerRow = new HBox();
        headerRow.setPadding(new Insets(14, 24, 14, 24));
        headerRow.setStyle("-fx-background-color: #f9fafb;");

        String[] headers = {"Order ID", "Customer", "Product", "Amount", "Status", "Action"};
        int[] widths = {110, 180, 220, 100, 130, 80};

        for (int i = 0; i < headers.length; i++) {
            Label header = new Label(headers[i]);
            TailwindFX.apply(header, "text-xs", "font-semibold", "text-gray-500", "uppercase");
            header.setPrefWidth(widths[i]);
            headerRow.getChildren().add(header);
        }

        table.getChildren().add(headerRow);

        // Table rows
        OrderData[] orders = {
            new OrderData("#ORD-001", "Alice Johnson", "MacBook Pro 16\"", "$2,499", "completed", "2024-01-15", "AJ", "blue"),
            new OrderData("#ORD-002", "Bob Smith", "iPhone 15 Pro", "$1,199", "pending", "2024-01-15", "BS", "green"),
            new OrderData("#ORD-003", "Carol White", "AirPods Max", "$549", "shipped", "2024-01-14", "CW", "purple"),
            new OrderData("#ORD-004", "David Brown", "iPad Pro 12.9\"", "$1,099", "processing", "2024-01-14", "DB", "amber"),
            new OrderData("#ORD-005", "Eva Martinez", "Apple Watch Ultra", "$799", "completed", "2024-01-13", "EM", "blue")
        };

        for (OrderData order : orders) {
            HBox row = createTableRow(order);
            table.getChildren().add(row);
        }

        return table;
    }

    private static class OrderData {
        String orderId, customer, product, amount, status, date, initials, color;
        OrderData(String orderId, String customer, String product, String amount, String status, String date, String initials, String color) {
            this.orderId = orderId;
            this.customer = customer;
            this.product = product;
            this.amount = amount;
            this.status = status;
            this.date = date;
            this.initials = initials;
            this.color = color;
        }
    }

    private static HBox createTableRow(OrderData data) {
        HBox row = new HBox();
        row.setPadding(new Insets(18, 24, 18, 24));
        row.setStyle("-fx-background-color: white;");
        row.setCursor(javafx.scene.Cursor.HAND);

        int[] widths = {110, 180, 220, 100, 130, 80};

        // Order ID
        Label orderId = new Label(data.orderId);
        TailwindFX.apply(orderId, "text-sm", "font-semibold", "text-gray-900");
        orderId.setPrefWidth(widths[0]);
        row.getChildren().add(orderId);

        // Customer with avatar
        HBox customer = new HBox(10);
        customer.setAlignment(Pos.CENTER_LEFT);
        StackPane avatar = ComponentFactory.avatar(data.initials, data.color, 32);
        TailwindFX.apply(avatar, "rounded-full");
        Label customerName = new Label(data.customer);
        TailwindFX.apply(customerName, "text-sm", "text-gray-700");
        customer.getChildren().addAll(avatar, customerName);
        customer.setPrefWidth(widths[1]);
        row.getChildren().add(customer);

        // Product
        Label product = new Label(data.product);
        TailwindFX.apply(product, "text-sm", "text-gray-700");
        product.setPrefWidth(widths[2]);
        row.getChildren().add(product);

        // Amount
        Label amount = new Label(data.amount);
        TailwindFX.apply(amount, "text-sm", "font-semibold", "text-gray-900");
        amount.setPrefWidth(widths[3]);
        row.getChildren().add(amount);

        // Status badge
        StackPane badge = createStatusBadge(data.status);
        badge.setPrefWidth(widths[4]);
        row.getChildren().add(badge);

        // Action button
        Button moreBtn = new Button("⋮");
        moreBtn.setStyle("-fx-background-color: transparent;");
        moreBtn.setCursor(javafx.scene.Cursor.HAND);
        TailwindFX.apply(moreBtn, "text-lg", "text-gray-400");
        moreBtn.setPrefWidth(widths[5]);
        row.getChildren().add(moreBtn);

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f9fafb;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: white;"));

        return row;
    }

    private static StackPane createStatusBadge(String status) {
        StackPane badge = new StackPane();
        Label label = new Label(status.substring(0, 1).toUpperCase() + status.substring(1));

        switch (status) {
            case "completed":
                badge.setStyle("-fx-background-color: #d1fae5;");
                TailwindFX.apply(label, "text-xs", "font-bold", "text-green-700");
                break;
            case "pending":
                badge.setStyle("-fx-background-color: #fef3c7;");
                TailwindFX.apply(label, "text-xs", "font-bold", "text-amber-700");
                break;
            case "shipped":
                badge.setStyle("-fx-background-color: #dbeafe;");
                TailwindFX.apply(label, "text-xs", "font-bold", "text-blue-700");
                break;
            case "processing":
                badge.setStyle("-fx-background-color: #ede9fe;");
                TailwindFX.apply(label, "text-xs", "font-bold", "text-purple-700");
                break;
        }

        badge.setPadding(new Insets(6, 14, 6, 14));
        TailwindFX.apply(badge, "rounded-full");
        badge.getChildren().add(label);
        return badge;
    }

    // =========================================================================
    // Products Section - Enhanced
    // =========================================================================

    private static VBox createProductsSection() {
        VBox section = new VBox(20);

        // Section header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Featured Products");
        TailwindFX.apply(title, "text-lg", "font-bold", "text-gray-900");
        Label subtitle = new Label("Handpicked selection of top products");
        TailwindFX.apply(subtitle, "text-sm", "text-gray-500");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(12);
        Button prev = new Button("←");
        prev.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        prev.setPrefSize(40, 40);
        prev.setCursor(javafx.scene.Cursor.HAND);
        TailwindFX.apply(prev, "text-lg");

        Button next = new Button("→");
        next.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        next.setPrefSize(40, 40);
        next.setCursor(javafx.scene.Cursor.HAND);
        TailwindFX.apply(next, "text-lg");

        actions.getChildren().addAll(prev, next);

        header.getChildren().addAll(titleBox, spacer, actions);
        section.getChildren().add(header);

        // Products grid
        HBox productsGrid = new HBox(20);
        productsGrid.getChildren().addAll(
            createProductCard("MacBook Pro 16\"", "Apple M3 Max, 36GB RAM, 1TB SSD", "$3,499", "blue", "⭐⭐⭐⭐⭐", "(48 reviews)", true),
            createProductCard("iPhone 15 Pro", "Titanium, 256GB, Natural", "$1,199", "gray", "⭐⭐⭐⭐⭐", "(124 reviews)", true),
            createProductCard("AirPods Pro", "2nd Gen with USB-C Charging", "$249", "green", "⭐⭐⭐⭐☆", "(89 reviews)", false),
            createProductCard("iPad Pro 12.9\"", "M2 Chip, 512GB, Wi-Fi", "$1,099", "purple", "⭐⭐⭐⭐⭐", "(67 reviews)", true)
        );

        section.getChildren().add(productsGrid);
        return section;
    }

    private static VBox createProductCard(String title, String description, String price, String color, String rating, String reviews, boolean inStock) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 4);");
        card.setCursor(javafx.scene.Cursor.HAND);

        // Image area with gradient
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(180);
        imageContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #f3f4f6, #e5e7eb); -fx-background-radius: 16 16 0 0;");

        Label productIcon = new Label("📦");
        TailwindFX.apply(productIcon, "text-5xl");
        imageContainer.getChildren().add(productIcon);

        // Wishlist button
        Button wishlist = new Button("♡");
        wishlist.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 0);");
        wishlist.setPrefSize(36, 36);
        wishlist.setCursor(javafx.scene.Cursor.HAND);
        TailwindFX.apply(wishlist, "text-lg");
        StackPane.setAlignment(wishlist, Pos.TOP_RIGHT);
        ((StackPane) imageContainer).getChildren().add(wishlist);

        card.getChildren().add(imageContainer);

        // Content
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

        // Title and description
        Label titleLabel = new Label(title);
        TailwindFX.apply(titleLabel, "text-base", "font-bold", "text-gray-900");

        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        TailwindFX.apply(descLabel, "text-sm", "text-gray-500");

        // Rating
        HBox ratingRow = new HBox(6);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        Label ratingLabel = new Label(rating);
        TailwindFX.apply(ratingLabel, "text-sm", "text-amber-500");
        Label reviewsLabel = new Label(reviews);
        TailwindFX.apply(reviewsLabel, "text-xs", "text-gray-400");
        ratingRow.getChildren().addAll(ratingLabel, reviewsLabel);

        // Price and cart
        HBox priceRow = new HBox(12);
        priceRow.setAlignment(Pos.CENTER);

        Label priceLabel = new Label(price);
        TailwindFX.apply(priceLabel, "text-xl", "font-bold", "text-gray-900");

        Button addToCart = new Button("Add to Cart");
        addToCart.setStyle("-fx-background-color: linear-gradient(to right, #3b82f6, #8b5cf6); -fx-background-radius: 10;");
        addToCart.setPadding(new Insets(10, 20, 10, 20));
        addToCart.setCursor(javafx.scene.Cursor.HAND);
        Label cartLabel = new Label("Add to Cart");
        TailwindFX.apply(cartLabel, "text-sm", "font-semibold", "text-white");
        addToCart.setGraphic(cartLabel);

        priceRow.getChildren().addAll(priceLabel, addToCart);

        content.getChildren().addAll(titleLabel, descLabel, ratingRow, priceRow);
        card.getChildren().add(content);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 30, 0, 0, 8);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 4);"));

        return card;
    }

    // =========================================================================
    // Demo Controls (for reference)
    // =========================================================================

    public static VBox createDemoControls() {
        VBox controls = new VBox(16);
        controls.getStyleClass().add("card");
        controls.setPadding(new Insets(20));

        Label title = new Label("UI Components Demo");
        TailwindFX.apply(title, "text-lg", "font-semibold", "text-gray-900");

        // Buttons
        HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Button primary = new Button("Primary");
        TailwindFX.apply(primary, "btn", "btn-primary");

        Button secondary = new Button("Secondary");
        TailwindFX.apply(secondary, "btn", "btn-secondary");

        Button success = new Button("Success");
        TailwindFX.apply(success, "btn", "btn-success");

        Button danger = new Button("Danger");
        TailwindFX.apply(danger, "btn", "btn-danger");

        Button warning = new Button("Warning");
        TailwindFX.apply(warning, "btn", "btn-warning");

        Button outline = new Button("Outline");
        TailwindFX.apply(outline, "btn", "btn-outline");

        buttons.getChildren().addAll(primary, secondary, success, danger, warning, outline);

        // Badges
        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);

        badges.getChildren().addAll(
            createBadge("Default", "gray"),
            createBadge("Blue", "blue"),
            createBadge("Green", "green"),
            createBadge("Red", "red"),
            createBadge("Yellow", "yellow"),
            createBadge("Purple", "purple")
        );

        // Form elements
        VBox form = new VBox(8);

        TextField input = new TextField();
        input.setPromptText("Enter your email...");
        TailwindFX.apply(input, "input");

        ChoiceBox<String> choice = new ChoiceBox<>();
        choice.getItems().addAll("Option 1", "Option 2", "Option 3");
        choice.setValue("Option 1");
        TailwindFX.apply(choice, "input");

        CheckBox checkbox = new CheckBox("I agree to the terms");
        TailwindFX.apply(checkbox, "text-sm");

        form.getChildren().addAll(input, choice, checkbox);

        controls.getChildren().addAll(title, buttons, badges, form);
        return controls;
    }

    private static Label createBadge(String text, String color) {
        Label badge = new Label(text);
        TailwindFX.apply(badge, "badge", "badge-" + color);
        return badge;
    }
}
