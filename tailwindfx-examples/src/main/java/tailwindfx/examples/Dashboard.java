package tailwindfx.examples;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tailwindfx.TailwindFX;
import tailwindfx.ComponentFactory;

/**
 * Dashboard Example — Using TailwindFX Utility Classes.
 * 
 * All styling uses TailwindFX CSS classes via:
 * - node.getStyleClass().add("class-name")
 * - TailwindFX.apply(node, "class1", "class2", ...)
 */
public class Dashboard {

    private static boolean darkModeEnabled = false;

    public static BorderPane create() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("bg-gray-100");

        // Sidebar
        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        // Main content
        VBox mainContent = new VBox(0);
        mainContent.getStyleClass().addAll("bg-gray-50");

        // Top bar
        HBox topBar = createTopBar();
        mainContent.getChildren().add(topBar);

        // Scrollable content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().addAll("bg-transparent");
        scrollPane.setStyle("-fx-background: transparent;");
        VBox content = createContent();
        scrollPane.setContent(content);

        mainContent.getChildren().add(scrollPane);
        mainLayout.setCenter(mainContent);

        return mainLayout;
    }

    public static VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.getStyleClass().add("bg-gray-50");

        // Welcome banner
        content.getChildren().add(createWelcomeBanner());

        // Stats row
        content.getChildren().add(createStatsRow());

        // Main content: Chart + Activity
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
    // Sidebar Navigation
    // =========================================================================

    private static VBox createSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(280);
        sidebar.getStyleClass().addAll("bg-gray-900");
        sidebar.setStyle("-fx-background-color: -color-gray-900;");

        // Header
        VBox header = new VBox(16);
        header.setPadding(new Insets(24, 20, 24, 20));
        header.setStyle("-fx-background-color: -color-gray-800;");

        // Logo
        HBox brand = new HBox(14);
        brand.setAlignment(Pos.CENTER_LEFT);

        StackPane logo = new StackPane();
        logo.setPrefSize(44, 44);
        logo.getStyleClass().addAll("bg-blue-500", "rounded-xl", "shadow-md");

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

        // User profile at bottom
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
            item.setStyle("-fx-background-color: -color-blue-600;");
            item.getStyleClass().addAll("rounded-lg");
            TailwindFX.apply(iconLabel, "text-white");
            TailwindFX.apply(label, "text-white");
        } else {
            TailwindFX.apply(iconLabel, "text-gray-400");
            TailwindFX.apply(label, "text-gray-400");
            
            item.setOnMouseEntered(e -> {
                item.setStyle("-fx-background-color: -color-gray-800;");
                item.getStyleClass().addAll("rounded-lg");
                iconLabel.setStyle("-fx-text-fill: -color-white;");
                label.setStyle("-fx-text-fill: -color-white;");
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
            badgePane.getStyleClass().addAll("bg-red-500", "rounded-full");

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
        separator.setStyle("-fx-background-color: -color-gray-800;");
        separator.setPadding(new Insets(12, 16, 12, 16));
        return separator;
    }

    private static VBox createUserProfile() {
        VBox profile = new VBox(12);
        profile.setAlignment(Pos.CENTER_LEFT);
        profile.setPadding(new Insets(20));
        profile.setStyle("-fx-background-color: -color-gray-800;");

        HBox info = new HBox(12);
        info.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = ComponentFactory.avatar("JD", "blue", 44);
        TailwindFX.apply(avatar, "rounded-full");

        VBox text = new VBox(4);
        Label name = new Label("John Doe");
        TailwindFX.apply(name, "text-sm", "font-semibold", "text-white");

        Label email = new Label("john@example.com");
        TailwindFX.apply(email, "text-xs", "text-gray-400");

        text.getChildren().addAll(name, email);
        info.getChildren().addAll(avatar, text);
        profile.getChildren().add(info);

        return profile;
    }

    // =========================================================================
    // Top Bar
    // =========================================================================

    private static HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(20, 32, 20, 32));
        topBar.getStyleClass().addAll("bg-white", "shadow-md");

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
        lineSeparator.getStyleClass().addAll("bg-gray-200");
        lineSeparator.setPadding(new Insets(0, 16, 0, 16));

        // Theme toggle
        Button themeToggle = createThemeToggle();

        // Notifications
        StackPane notificationBtn = createNotificationButton();

        // User menu
        HBox userMenu = new HBox(12);
        userMenu.setAlignment(Pos.CENTER);
        userMenu.setPadding(new Insets(8, 16, 8, 16));
        userMenu.getStyleClass().addAll("bg-gray-100", "rounded-xl");
        userMenu.setCursor(javafx.scene.Cursor.HAND);

        VBox userText = new VBox(2);
        userText.setAlignment(Pos.CENTER_RIGHT);
        Label userName = new Label("John Doe");
        TailwindFX.apply(userName, "text-sm", "font-semibold", "text-gray-700");
        Label userRole = new Label("Administrator");
        TailwindFX.apply(userRole, "text-xs", "text-gray-500");
        userText.getChildren().addAll(userName, userRole);

        StackPane userAvatar = ComponentFactory.avatar("JD", "blue", 40);
        TailwindFX.apply(userAvatar, "rounded-full");

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
        toggle.getStyleClass().addAll("bg-gray-100", "rounded-xl");
        toggle.setCursor(javafx.scene.Cursor.HAND);
        TailwindFX.apply(toggle, "text-xl");
        toggle.setOnMouseEntered(e -> toggle.setStyle("-fx-background-color: -color-gray-200; -fx-background-radius: 12;"));
        toggle.setOnMouseExited(e -> toggle.setStyle("-fx-background-color: -color-gray-100; -fx-background-radius: 12;"));
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
        btnWrapper.getStyleClass().addAll("bg-gray-100", "rounded-xl");
        btnWrapper.setCursor(javafx.scene.Cursor.HAND);

        Label bell = new Label("🔔");
        TailwindFX.apply(bell, "text-xl");
        btnWrapper.getChildren().add(bell);

        // Notification dot
        StackPane badge = new StackPane();
        badge.setPrefSize(12, 12);
        badge.getStyleClass().addAll("bg-red-500", "rounded-full");

        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        btnWrapper.getChildren().add(badge);

        btnWrapper.setOnMouseEntered(e -> btnWrapper.setStyle("-fx-background-color: -color-gray-200; -fx-background-radius: 12;"));
        btnWrapper.setOnMouseExited(e -> btnWrapper.setStyle("-fx-background-color: -color-gray-100; -fx-background-radius: 12;"));

        return btnWrapper;
    }

    // =========================================================================
    // Welcome Banner
    // =========================================================================

    private static HBox createWelcomeBanner() {
        HBox banner = new HBox(24);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(24, 32, 24, 32));
        banner.setStyle("-fx-background-color: linear-gradient(to right, -color-blue-500, -color-purple-500);");
        banner.getStyleClass().addAll("rounded-xl", "shadow-lg");

        VBox text = new VBox(8);
        Label title = new Label("Welcome back, John! 👋");
        TailwindFX.apply(title, "text-2xl", "font-bold", "text-white");

        Label subtitle = new Label("Here's what's happening with your business today.");
        TailwindFX.apply(subtitle, "text-sm", "text-blue-100");

        text.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button actionBtn = new Button("📊 View Reports");
        actionBtn.getStyleClass().addAll("bg-white", "rounded-lg");
        actionBtn.setPadding(new Insets(12, 24, 12, 24));
        actionBtn.setCursor(javafx.scene.Cursor.HAND);
        TailwindFX.apply(actionBtn, "text-sm", "font-semibold", "text-blue-600");

        banner.getChildren().addAll(text, spacer, actionBtn);
        return banner;
    }

    // =========================================================================
    // Stats Row
    // =========================================================================

    private static HBox createStatsRow() {
        HBox stats = new HBox(20);

        stats.getChildren().addAll(
            createStatCard("💰", "Total Revenue", "$45,231", "+20.1%", "green"),
            createStatCard("👥", "Active Users", "2,350", "+15.2%", "blue"),
            createStatCard("📊", "Bounce Rate", "12.5%", "-3.2%", "purple"),
            createStatCard("⏱️", "Avg. Session", "4m 32s", "+8.4%", "amber")
        );

        return stats;
    }

    private static StackPane createStatCard(String icon, String title, String value, String change, String color) {
        VBox card = new VBox(16);
        card.getStyleClass().addAll("card", "shadow-md");
        card.setPadding(new Insets(24));
        card.setCursor(javafx.scene.Cursor.HAND);

        // Header with icon and title
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(48, 48);
        iconBox.getStyleClass().addAll("bg-blue-50", "rounded-xl");

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
        changeBadge.getStyleClass().addAll(isPositive ? "bg-green-100" : "bg-red-100", "rounded-full");
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
    // Chart Section
    // =========================================================================

    private static VBox createChartSection() {
        VBox section = new VBox(20);
        section.getStyleClass().addAll("card", "shadow-md");
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
        periodSelector.getStyleClass().addAll("bg-gray-100", "rounded-lg");
        String[] periods = {"7D", "30D", "90D"};
        for (int i = 0; i < periods.length; i++) {
            Button btn = new Button(periods[i]);
            btn.getStyleClass().addAll(i == 0 ? "bg-blue-500" : "bg-transparent", "rounded-md");
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
            bar.getStyleClass().addAll("bg-blue-500", "rounded-t-lg");
            bar.setStyle("-fx-background-color: -color-blue-500;");
            bar.setCursor(javafx.scene.Cursor.HAND);

            bar.setOnMouseEntered(e -> bar.setStyle("-fx-background-color: -color-blue-600;"));
            bar.setOnMouseExited(e -> bar.setStyle("-fx-background-color: -color-blue-500;"));

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

        panel.getChildren().add(createRecentActivity());
        panel.getChildren().add(createQuickChat());

        return panel;
    }

    private static VBox createRecentActivity() {
        VBox section = new VBox(16);
        section.getStyleClass().addAll("card", "shadow-md");
        section.setPadding(new Insets(20));

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Recent Activity");
        TailwindFX.apply(title, "text-base", "font-bold", "text-gray-900");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewAll = new Button("View All →");
        viewAll.getStyleClass().addAll("bg-transparent");
        viewAll.setCursor(javafx.scene.Cursor.HAND);
        TailwindFX.apply(viewAll, "text-sm", "font-medium", "text-blue-600");

        header.getChildren().addAll(title, viewAll);

        // Activity list
        VBox activities = new VBox(16);

        activities.getChildren().addAll(
            createActivityItem("👤", "New user registered", "2 min ago", "bg-green-100", "text-green-600"),
            createActivityItem("💳", "Payment received", "15 min ago", "bg-blue-100", "text-blue-600"),
            createActivityItem("📄", "Report generated", "1 hour ago", "bg-purple-100", "text-purple-600"),
            createActivityItem("🔄", "System update", "3 hours ago", "bg-amber-100", "text-amber-600"),
            createActivityItem("💾", "Backup completed", "5 hours ago", "bg-gray-100", "text-gray-600")
        );

        section.getChildren().addAll(header, activities);
        return section;
    }

    private static HBox createActivityItem(String icon, String activity, String time, String bgClass, String textClass) {
        HBox item = new HBox(14);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(8, 0, 8, 0));
        item.setCursor(javafx.scene.Cursor.HAND);

        // Icon with background
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(40, 40);
        iconBox.getStyleClass().addAll(bgClass, "rounded-lg");
        Label iconLabel = new Label(icon);
        TailwindFX.apply(iconLabel, "text-sm", textClass);
        iconBox.getChildren().add(iconLabel);

        // Text
        VBox text = new VBox(2);
        Label activityLabel = new Label(activity);
        TailwindFX.apply(activityLabel, "text-sm", "font-medium", "text-gray-900");
        Label timeLabel = new Label(time);
        TailwindFX.apply(timeLabel, "text-xs", "text-gray-500");
        text.getChildren().addAll(activityLabel, timeLabel);

        item.getChildren().addAll(iconBox, text);

        item.setOnMouseEntered(e -> item.getStyleClass().addAll("bg-gray-50", "rounded-lg"));
        item.setOnMouseExited(e -> item.getStyleClass().removeAll("bg-gray-50", "rounded-lg"));

        return item;
    }

    // =========================================================================
    // Quick Chat Panel
    // =========================================================================

    private static VBox createQuickChat() {
        VBox section = new VBox(16);
        section.getStyleClass().addAll("card", "shadow-md");
        section.setPadding(new Insets(20));

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Messages");
        TailwindFX.apply(title, "text-base", "font-bold", "text-gray-900");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newMsg = new Button("+ New");
        newMsg.getStyleClass().addAll("btn-primary", "btn-sm");
        newMsg.setCursor(javafx.scene.Cursor.HAND);

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
        item.getStyleClass().addAll("bg-gray-50", "rounded-xl");
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
            dot.getStyleClass().addAll("bg-blue-500", "rounded-full");
            item.getChildren().add(dot);
        }

        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: -color-gray-100;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-color: -color-gray-50;"));

        return item;
    }

    // =========================================================================
    // Data Table Section
    // =========================================================================

    private static VBox createDataTableSection() {
        VBox section = new VBox(0);
        section.getStyleClass().addAll("card", "shadow-md");

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
        search.getStyleClass().addAll("input", "input-sm");
        search.setStyle("-fx-background-color: -color-gray-100;");

        Button filterBtn = new Button("⚡ Filter");
        filterBtn.getStyleClass().addAll("btn-outline", "btn-sm");
        filterBtn.setCursor(javafx.scene.Cursor.HAND);

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
        headerRow.getStyleClass().addAll("bg-gray-50");

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
        row.getStyleClass().addAll("bg-white");
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
        moreBtn.getStyleClass().addAll("bg-transparent");
        moreBtn.setCursor(javafx.scene.Cursor.HAND);
        TailwindFX.apply(moreBtn, "text-lg", "text-gray-400");
        moreBtn.setPrefWidth(widths[5]);
        row.getChildren().add(moreBtn);

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: -color-gray-50;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: -color-white;"));

        return row;
    }

    private static StackPane createStatusBadge(String status) {
        StackPane badge = new StackPane();
        Label label = new Label(status.substring(0, 1).toUpperCase() + status.substring(1));

        switch (status) {
            case "completed":
                badge.getStyleClass().addAll("bg-green-100", "rounded-full");
                TailwindFX.apply(label, "text-xs", "font-bold", "text-green-700");
                break;
            case "pending":
                badge.getStyleClass().addAll("bg-amber-100", "rounded-full");
                TailwindFX.apply(label, "text-xs", "font-bold", "text-amber-700");
                break;
            case "shipped":
                badge.getStyleClass().addAll("bg-blue-100", "rounded-full");
                TailwindFX.apply(label, "text-xs", "font-bold", "text-blue-700");
                break;
            case "processing":
                badge.getStyleClass().addAll("bg-purple-100", "rounded-full");
                TailwindFX.apply(label, "text-xs", "font-bold", "text-purple-700");
                break;
        }

        badge.setPadding(new Insets(6, 14, 6, 14));
        badge.getChildren().add(label);
        return badge;
    }

    // =========================================================================
    // Products Section
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
        prev.getStyleClass().addAll("btn-outline", "rounded-lg");
        prev.setPrefSize(40, 40);
        prev.setCursor(javafx.scene.Cursor.HAND);

        Button next = new Button("→");
        next.getStyleClass().addAll("btn-outline", "rounded-lg");
        next.setPrefSize(40, 40);
        next.setCursor(javafx.scene.Cursor.HAND);

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
        card.getStyleClass().addAll("card", "shadow-md");
        card.setCursor(javafx.scene.Cursor.HAND);

        // Image area
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(180);
        imageContainer.getStyleClass().addAll("bg-gray-100", "rounded-t-xl");

        Label productIcon = new Label("📦");
        TailwindFX.apply(productIcon, "text-5xl");
        imageContainer.getChildren().add(productIcon);

        // Wishlist button
        Button wishlist = new Button("♡");
        wishlist.getStyleClass().addAll("bg-white", "rounded-full", "shadow-sm");
        wishlist.setPrefSize(36, 36);
        wishlist.setCursor(javafx.scene.Cursor.HAND);
        StackPane.setAlignment(wishlist, Pos.TOP_RIGHT);
        ((StackPane) imageContainer).getChildren().add(wishlist);

        card.getChildren().add(imageContainer);

        // Content
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

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
        addToCart.getStyleClass().addAll("btn-primary", "btn-sm");
        addToCart.setCursor(javafx.scene.Cursor.HAND);

        priceRow.getChildren().addAll(priceLabel, addToCart);

        content.getChildren().addAll(titleLabel, descLabel, ratingRow, priceRow);
        card.getChildren().add(content);

        card.setOnMouseEntered(e -> card.getStyleClass().addAll("shadow-lg"));
        card.setOnMouseExited(e -> card.getStyleClass().removeAll("shadow-lg"));

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
