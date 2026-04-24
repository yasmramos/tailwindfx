package tailwindfx.examples;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import tailwindfx.ComponentFactory;
import tailwindfx.FxDataTable;
import tailwindfx.TailwindFX;

import java.util.Arrays;
import java.util.List;

/**
 * Dashboard with Advanced Data Visualization.
 * Features: Real charts, sparklines, interactive data table, calendar widget,
 * toast notifications, modal dialogs, and improved UX.
 */
public class Dashboard {

    private static boolean darkModeEnabled = false;
    private static boolean sidebarCollapsed = false;
    private static Popup notificationDropdown = null;
    private static Popup userDropdown = null;
    private static StackPane toastOverlay;

    public static BorderPane create() {
        BorderPane mainLayout = new BorderPane();
        TailwindFX.jit(mainLayout, "bg-gray-100");

        // Sidebar
        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        // Main content
        VBox mainContent = new VBox(0);
        TailwindFX.jit(mainContent, "bg-gray-50");

        // Top bar
        HBox topBar = createTopBar();
        mainContent.getChildren().add(topBar);

        // Scrollable content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        
        VBox content = createContent();
        scrollPane.setContent(content);

        mainContent.getChildren().add(scrollPane);
        mainLayout.setCenter(mainContent);

        return mainLayout;
    }

    public static VBox createContent() {
        VBox content = new VBox(28);
        TailwindFX.jit(content, "p-8", "bg-gray-50");

        // Welcome banner with gradients
        content.getChildren().add(createWelcomeBanner());

        // Stats row with sparklines
        content.getChildren().add(createEnhancedStatsRow());

        // Charts row: Line chart + Pie chart
        HBox chartsRow = new HBox(24);
        
        VBox lineChart = createLineChartSection();
        HBox.setHgrow(lineChart, Priority.ALWAYS);
        
        VBox pieChart = DashboardComponents.createPieChart(
            "Traffic Sources",
            Arrays.asList(
                new DashboardComponents.PieSlice("Direct", 4500, Color.rgb(59, 130, 246)),
                new DashboardComponents.PieSlice("Organic", 3200, Color.rgb(16, 185, 129)),
                new DashboardComponents.PieSlice("Referral", 2100, Color.rgb(245, 158, 11)),
                new DashboardComponents.PieSlice("Social", 1800, Color.rgb(139, 92, 246))
            )
        );
        
        chartsRow.getChildren().addAll(lineChart, pieChart);
        content.getChildren().add(chartsRow);

        // Calendar + Activity row
        HBox widgetsRow = new HBox(24);
        VBox calendar = DashboardComponents.createCalendarWidget();
        HBox.setHgrow(calendar, Priority.ALWAYS);
        
        VBox activity = createActivityFeed();
        HBox.setHgrow(activity, Priority.ALWAYS);
        
        widgetsRow.getChildren().addAll(calendar, activity);
        content.getChildren().add(widgetsRow);

        // Enhanced data table
        content.getChildren().add(createEnhancedDataTable());

        // Demo controls section
        content.getChildren().add(createDemoControls());

        return content;
    }

    // =========================================================================
    // Sidebar (Enhanced with better UX)
    // =========================================================================

    private static VBox createSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(280);
        TailwindFX.jit(sidebar, "bg-gray-900");

        // Header with gradient
        VBox header = new VBox(16);
        TailwindFX.jit(header, "p-5", "bg-gradient-to-b", "from-gray-800", "to-gray-900");

        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        // Enhanced logo with gradient
        StackPane logo = new StackPane();
        logo.setPrefSize(48, 48);
        TailwindFX.jit(logo, "bg-gradient-to-br", "from-blue-500", "to-purple-600", "rounded-xl");
        
        Label logoText = new Label("T");
        TailwindFX.apply(logoText, "text-2xl", "font-bold", "text-white");
        logo.getChildren().add(logoText);

        VBox brandText = new VBox(2);
        Label brandName = new Label("TailwindFX");
        TailwindFX.apply(brandName, "text-lg", "font-bold", "text-white");

        Label brandSubtitle = new Label("Admin Panel v2.0");
        TailwindFX.apply(brandSubtitle, "text-xs", "text-gray-400");

        brandText.getChildren().addAll(brandName, brandSubtitle);

        // Collapse button
        Button collapseBtn = new Button("◀");
        collapseBtn.setPrefSize(32, 32);
        TailwindFX.jit(collapseBtn, "bg-gray-700", "rounded-lg", "text-sm", "text-gray-400");
        collapseBtn.setCursor(javafx.scene.Cursor.HAND);
        collapseBtn.setOnMouseEntered(e -> 
            TailwindFX.jit(collapseBtn, "bg-blue-600", "rounded-lg", "text-sm", "text-white"));
        collapseBtn.setOnMouseExited(e -> 
            TailwindFX.jit(collapseBtn, "bg-gray-700", "rounded-lg", "text-sm", "text-gray-400"));
        collapseBtn.setOnAction(e -> toggleSidebar(sidebar, collapseBtn));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerRow.getChildren().addAll(logo, brandText, spacer, collapseBtn);
        header.getChildren().add(headerRow);
        sidebar.getChildren().add(header);

        // Navigation menu
        VBox menu = new VBox(4);
        TailwindFX.jit(menu, "p-3");

        menu.getChildren().addAll(
            createMenuItem("📊", "Dashboard", true),
            createMenuItem("👥", "Users", false),
            createMenuItem("📦", "Products", false),
            createMenuItem("🛒", "Orders", false, 12),
            createMenuItem("📈", "Analytics", false),
            createMenuItem("💬", "Messages", false, 5),
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
        TailwindFX.jit(item, "p-3", "px-4", "rounded-lg");
        item.setCursor(javafx.scene.Cursor.HAND);

        Label iconLabel = new Label(icon);
        TailwindFX.apply(iconLabel, "text-lg");

        Label label = new Label(text);
        TailwindFX.apply(label, "text-sm", "font-medium");

        if (active) {
            TailwindFX.jit(item, "bg-blue-600", "rounded-lg");
            TailwindFX.apply(iconLabel, "text-white");
            TailwindFX.apply(label, "text-white");
        } else {
            TailwindFX.apply(iconLabel, "text-gray-400");
            TailwindFX.apply(label, "text-gray-400");

            item.setOnMouseEntered(e -> {
                TailwindFX.jit(item, "bg-gray-800", "rounded-lg");
                TailwindFX.apply(iconLabel, "text-white");
                TailwindFX.apply(label, "text-white");
            });
            item.setOnMouseExited(e -> {
                TailwindFX.jit(item, "rounded-lg");
                TailwindFX.apply(iconLabel, "text-gray-400");
                TailwindFX.apply(label, "text-gray-400");
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
            badgePane.setPrefSize(20, 20);
            TailwindFX.jit(badgePane, "bg-red-500", "rounded-full");

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
        TailwindFX.jit(separator, "bg-gray-800", "py-3", "px-4");
        return separator;
    }

    private static VBox createUserProfile() {
        VBox profile = new VBox(12);
        profile.setAlignment(Pos.CENTER_LEFT);
        TailwindFX.jit(profile, "p-5", "bg-gray-800");

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

    private static void toggleSidebar(VBox sidebar, Button collapseBtn) {
        sidebarCollapsed = !sidebarCollapsed;

        if (sidebarCollapsed) {
            sidebar.setPrefWidth(80);
            collapseBtn.setText("▶");
        } else {
            sidebar.setPrefWidth(280);
            collapseBtn.setText("◀");
        }
    }

    // =========================================================================
    // Top Bar (Enhanced)
    // =========================================================================

    private static HBox createTopBar() {
        HBox topBar = new HBox(24);
        topBar.setAlignment(Pos.CENTER);
        TailwindFX.jit(topBar, "p-3", "px-6", "bg-gradient-to-b", "from-white", "to-gray-50", "rounded-b-2xl");

        // Breadcrumb
        HBox breadcrumb = createBreadcrumb();

        // Search box
        HBox searchBox = createSearchBox();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Right actions
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER);

        Button quickActionsBtn = createQuickActionsButton();
        StackPane notificationBtn = createNotificationButtonWithDropdown();
        Button themeToggle = createThemeToggle();

        Region vSeparator = new Region();
        vSeparator.setPrefWidth(1);
        vSeparator.setPrefHeight(32);
        TailwindFX.jit(vSeparator, "bg-gray-200");

        HBox userMenu = createUserMenuWithDropdown();

        actions.getChildren().addAll(quickActionsBtn, notificationBtn, themeToggle, vSeparator, userMenu);

        topBar.getChildren().addAll(breadcrumb, searchBox, spacer, actions);
        return topBar;
    }

    private static HBox createBreadcrumb() {
        HBox breadcrumb = new HBox(8);
        breadcrumb.setAlignment(Pos.CENTER_LEFT);

        String[] items = {"Home", "Dashboard", "Overview"};
        for (int i = 0; i < items.length; i++) {
            Label item = new Label(items[i]);
            if (i == items.length - 1) {
                TailwindFX.apply(item, "text-sm", "font-semibold", "text-blue-600");
            } else {
                TailwindFX.apply(item, "text-sm", "text-gray-500");
                item.setCursor(javafx.scene.Cursor.HAND);
                final boolean isLast = i == items.length - 1;
                item.setOnMouseEntered(e -> {
                    if (!isLast) {
                        TailwindFX.apply(item, "text-blue-600");
                        item.setUnderline(true);
                    }
                });
                item.setOnMouseExited(e -> {
                    if (!isLast) {
                        TailwindFX.apply(item, "text-gray-500");
                        item.setUnderline(false);
                    }
                });
            }
            breadcrumb.getChildren().add(item);

            if (i < items.length - 1) {
                Label separator = new Label("/");
                TailwindFX.apply(separator, "text-sm", "text-gray-400");
                breadcrumb.getChildren().add(separator);
            }
        }

        return breadcrumb;
    }

    private static HBox createSearchBox() {
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        TailwindFX.jit(searchBox, "p-2", "px-3", "bg-gray-100", "rounded-xl", "border-2", "border-transparent");
        searchBox.setPrefWidth(320);
        searchBox.setCursor(javafx.scene.Cursor.TEXT);

        Label searchIcon = new Label("🔍");
        TailwindFX.apply(searchIcon, "text-sm");

        TextField searchField = new TextField();
        searchField.setPromptText("Search anything... (Ctrl+K)");
        TailwindFX.jit(searchField, "bg-transparent", "text-sm");
        searchField.setStyle("-fx-prompt-text-fill: -color-gray-400; -fx-text-fill: -color-gray-900;");
        searchField.setPrefWidth(270);
        
        // Add keyboard shortcut
        searchField.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == javafx.scene.input.KeyCode.K) {
                searchField.requestFocus();
                searchField.selectAll();
            }
        });

        Button quickSearchBtn = new Button("⚡");
        quickSearchBtn.setPrefSize(28, 28);
        TailwindFX.jit(quickSearchBtn, "bg-blue-500", "rounded-lg", "text-xs");
        quickSearchBtn.setCursor(javafx.scene.Cursor.HAND);
        quickSearchBtn.setOnMouseEntered(e -> TailwindFX.jit(quickSearchBtn, "bg-blue-600", "rounded-lg"));
        quickSearchBtn.setOnMouseExited(e -> TailwindFX.jit(quickSearchBtn, "bg-blue-500", "rounded-lg"));
        quickSearchBtn.setOnAction(e -> 
            DashboardComponents.showToast("Searching: " + searchField.getText(), 
                DashboardComponents.ToastType.INFO));

        searchBox.getChildren().addAll(searchIcon, searchField, quickSearchBtn);

        searchBox.setOnMouseEntered(e -> {
            TailwindFX.jit(searchBox, "bg-white", "rounded-xl", "border-blue-200");
        });
        searchBox.setOnMouseExited(e -> {
            TailwindFX.jit(searchBox, "bg-gray-100", "rounded-xl", "border-transparent");
        });

        return searchBox;
    }

    private static StackPane createNotificationButtonWithDropdown() {
        StackPane btnWrapper = new StackPane();
        btnWrapper.setPrefSize(44, 44);
        TailwindFX.jit(btnWrapper, "bg-gray-100", "rounded-xl");
        btnWrapper.setCursor(javafx.scene.Cursor.HAND);

        Label bell = new Label("🔔");
        TailwindFX.apply(bell, "text-lg");
        btnWrapper.getChildren().add(bell);

        StackPane badge = new StackPane();
        badge.setPrefSize(20, 20);
        TailwindFX.jit(badge, "bg-gradient-to-br", "from-red-500", "to-red-600", "rounded-full");

        Label badgeCount = new Label("3");
        TailwindFX.apply(badgeCount, "text-xs", "font-bold", "text-white");
        badge.getChildren().add(badgeCount);

        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        btnWrapper.getChildren().add(badge);

        btnWrapper.setOnMouseEntered(e -> 
            TailwindFX.jit(btnWrapper, "bg-blue-50", "rounded-xl", "border-blue-200"));
        btnWrapper.setOnMouseExited(e -> 
            TailwindFX.jit(btnWrapper, "bg-gray-100", "rounded-xl"));

        btnWrapper.setOnMouseClicked(e -> {
            DashboardComponents.showToast("You have 3 new notifications", 
                DashboardComponents.ToastType.INFO);
            showNotificationDropdown(btnWrapper);
        });

        return btnWrapper;
    }

    private static void showNotificationDropdown(StackPane anchor) {
        if (notificationDropdown != null && notificationDropdown.isShowing()) {
            notificationDropdown.hide();
            return;
        }

        VBox dropdown = new VBox(0);
        dropdown.setPrefWidth(340);
        TailwindFX.jit(dropdown, "bg-white", "rounded-xl", "shadow-2xl");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        TailwindFX.jit(header, "p-4", "bg-gray-50", "rounded-t-xl");

        Label title = new Label("Notifications");
        TailwindFX.apply(title, "text-sm", "font-bold", "text-gray-900");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label markAllRead = new Label("Mark all read");
        TailwindFX.apply(markAllRead, "text-xs", "text-blue-600");
        markAllRead.setCursor(javafx.scene.Cursor.HAND);
        markAllRead.setOnMouseClicked(e -> {
            DashboardComponents.showToast("All notifications marked as read", 
                DashboardComponents.ToastType.SUCCESS);
            notificationDropdown.hide();
        });

        header.getChildren().addAll(title, spacer, markAllRead);

        Separator sep1 = new Separator();
        TailwindFX.jit(sep1, "bg-gray-200");

        VBox notifications = new VBox(0);
        notifications.getChildren().addAll(
            createNotificationItem("👤", "New user registered", "2 minutes ago", false),
            createNotificationItem("💳", "Payment received - $1,250", "15 minutes ago", false),
            createNotificationItem("📄", "Report generated successfully", "1 hour ago", false),
            createNotificationItem("⚙️", "System update completed", "3 hours ago", true)
        );

        Separator sep2 = new Separator();
        TailwindFX.jit(sep2, "bg-gray-200");

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        TailwindFX.jit(footer, "p-3");

        Label viewAll = new Label("View all notifications");
        TailwindFX.apply(viewAll, "text-sm", "font-medium", "text-blue-600");
        viewAll.setCursor(javafx.scene.Cursor.HAND);

        footer.getChildren().add(viewAll);
        dropdown.getChildren().addAll(header, sep1, notifications, sep2, footer);

        notificationDropdown = new Popup();
        notificationDropdown.getContent().add(dropdown);
        notificationDropdown.setHideOnEscape(true);

        javafx.geometry.Point2D anchorScreen = anchor.localToScreen(
            anchor.getBoundsInLocal().getMaxX(), 
            anchor.getBoundsInLocal().getMaxY());
        notificationDropdown.show(anchor, anchorScreen.getX() - 340, anchorScreen.getY() + 5);
    }

    private static VBox createNotificationItem(String icon, String message, String time, boolean read) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        TailwindFX.jit(item, "p-3", "px-4");
        item.setCursor(javafx.scene.Cursor.HAND);

        if (!read) {
            TailwindFX.jit(item, "bg-blue-50");
        }

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(36, 36);
        TailwindFX.jit(iconBox, "bg-white", "rounded-lg");
        Label iconLabel = new Label(icon);
        iconBox.getChildren().add(iconLabel);

        VBox content = new VBox(2);
        Label msgLabel = new Label(message);
        TailwindFX.apply(msgLabel, "text-sm", "text-gray-900");
        Label timeLabel = new Label(time);
        TailwindFX.apply(timeLabel, "text-xs", "text-gray-500");
        content.getChildren().addAll(msgLabel, timeLabel);

        if (!read) {
            StackPane unreadDot = new StackPane();
            unreadDot.setPrefSize(8, 8);
            TailwindFX.jit(unreadDot, "bg-blue-500", "rounded-full");
            item.getChildren().add(unreadDot);
        }

        item.getChildren().addAll(iconBox, content);

        item.setOnMouseEntered(e -> TailwindFX.jit(item, "bg-gray-50"));
        item.setOnMouseExited(e -> TailwindFX.jit(item, read ? "bg-transparent" : "bg-blue-50"));
        item.setOnMouseClicked(e -> notificationDropdown.hide());

        VBox wrapper = new VBox(item);
        return wrapper;
    }

    private static Button createQuickActionsButton() {
        Button btn = new Button("➕");
        btn.setPrefSize(44, 44);
        TailwindFX.jit(btn, "bg-gray-100", "rounded-xl");
        btn.setCursor(javafx.scene.Cursor.HAND);

        btn.setOnMouseEntered(e -> TailwindFX.jit(btn, "bg-blue-500", "rounded-xl"));
        btn.setOnMouseExited(e -> TailwindFX.jit(btn, "bg-gray-100", "rounded-xl"));
        
        btn.setOnAction(e -> 
            DashboardComponents.showToast("Quick actions menu opened", 
                DashboardComponents.ToastType.INFO));

        return btn;
    }

    private static Button createThemeToggle() {
        Button toggle = new Button(darkModeEnabled ? "🌙" : "☀️");
        toggle.setPrefSize(44, 44);
        TailwindFX.jit(toggle, "bg-gray-100", "rounded-xl");
        toggle.setCursor(javafx.scene.Cursor.HAND);

        toggle.setOnMouseEntered(e -> TailwindFX.jit(toggle, "bg-blue-50", "rounded-xl"));
        toggle.setOnMouseExited(e -> TailwindFX.jit(toggle, "bg-gray-100", "rounded-xl"));

        toggle.setOnAction(e -> {
            darkModeEnabled = !darkModeEnabled;
            toggle.setText(darkModeEnabled ? "🌙" : "☀️");
            if (toggle.getScene() != null) {
                Node root = toggle.getScene().getRoot();
                if (darkModeEnabled) {
                    if (!root.getStyleClass().contains("dark")) {
                        root.getStyleClass().add("dark");
                    }
                } else {
                    root.getStyleClass().remove("dark");
                }
            }
            DashboardComponents.showToast(
                darkModeEnabled ? "Dark mode enabled" : "Light mode enabled",
                DashboardComponents.ToastType.INFO);
        });
        return toggle;
    }

    private static HBox createUserMenuWithDropdown() {
        HBox userMenu = new HBox(10);
        userMenu.setAlignment(Pos.CENTER);
        TailwindFX.jit(userMenu, "p-1", "px-2", "bg-gray-100", "rounded-xl");
        userMenu.setCursor(javafx.scene.Cursor.HAND);

        VBox userText = new VBox(2);
        userText.setAlignment(Pos.CENTER_LEFT);
        Label userName = new Label("John Doe");
        TailwindFX.apply(userName, "text-sm", "font-semibold", "text-gray-900");
        Label userRole = new Label("Admin");
        TailwindFX.apply(userRole, "text-xs", "text-gray-500");
        userText.getChildren().addAll(userName, userRole);

        StackPane userAvatar = ComponentFactory.avatar("JD", "blue", 38);
        TailwindFX.apply(userAvatar, "rounded-full");

        Label dropdownArrow = new Label("▼");
        TailwindFX.apply(dropdownArrow, "text-xs", "text-gray-400");

        userMenu.getChildren().addAll(userText, userAvatar, dropdownArrow);

        userMenu.setOnMouseEntered(e -> 
            TailwindFX.jit(userMenu, "bg-white", "rounded-xl", "border-blue-200"));
        userMenu.setOnMouseExited(e -> 
            TailwindFX.jit(userMenu, "bg-gray-100", "rounded-xl"));

        userMenu.setOnMouseClicked(e -> showUserDropdown(userMenu));

        return userMenu;
    }

    private static void showUserDropdown(HBox anchor) {
        if (userDropdown != null && userDropdown.isShowing()) {
            userDropdown.hide();
            return;
        }

        VBox dropdown = new VBox(0);
        dropdown.setPrefWidth(280);
        TailwindFX.jit(dropdown, "bg-white", "rounded-xl", "shadow-2xl");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        TailwindFX.jit(header, "p-4", "bg-gradient-to-r", "from-blue-50", "to-purple-50", "rounded-t-xl");

        StackPane avatar = ComponentFactory.avatar("JD", "blue", 50);
        TailwindFX.apply(avatar, "rounded-full");

        VBox userInfo = new VBox(4);
        Label name = new Label("John Doe");
        TailwindFX.apply(name, "text-base", "font-bold", "text-gray-900");
        Label email = new Label("john@example.com");
        TailwindFX.apply(email, "text-xs", "text-gray-600");
        userInfo.getChildren().addAll(name, email);

        header.getChildren().addAll(avatar, userInfo);

        Separator sep1 = new Separator();
        TailwindFX.jit(sep1, "bg-gray-200");

        VBox menuItems = new VBox(0);
        menuItems.getChildren().addAll(
            createDropdownMenuItem("👤", "My Profile", "Manage your account", 
                () -> DashboardComponents.showToast("Opening profile...", DashboardComponents.ToastType.INFO)),
            createDropdownMenuItem("⚙️", "Settings", "Preferences and security",
                () -> DashboardComponents.showToast("Opening settings...", DashboardComponents.ToastType.INFO)),
            createDropdownMenuItem("💳", "Billing", "Payment and subscription",
                () -> DashboardComponents.showToast("Opening billing...", DashboardComponents.ToastType.INFO)),
            createDropdownMenuItem("❓", "Help Center", "FAQs and support",
                () -> DashboardComponents.showToast("Opening help...", DashboardComponents.ToastType.INFO))
        );

        Separator sep2 = new Separator();
        TailwindFX.jit(sep2, "bg-gray-200");

        HBox logoutItem = new HBox(12);
        logoutItem.setAlignment(Pos.CENTER_LEFT);
        TailwindFX.jit(logoutItem, "p-3", "px-4");
        logoutItem.setCursor(javafx.scene.Cursor.HAND);

        Label logoutIcon = new Label("🚪");
        TailwindFX.apply(logoutIcon, "text-base");

        VBox logoutText = new VBox(2);
        Label logoutTitle = new Label("Sign out");
        TailwindFX.apply(logoutTitle, "text-sm", "font-medium", "text-red-600");
        Label logoutDesc = new Label("Log out of your account");
        TailwindFX.apply(logoutDesc, "text-xs", "text-gray-500");
        logoutText.getChildren().addAll(logoutTitle, logoutDesc);

        logoutItem.getChildren().addAll(logoutIcon, logoutText);
        logoutItem.setOnMouseEntered(e -> TailwindFX.jit(logoutItem, "bg-red-50"));
        logoutItem.setOnMouseExited(e -> TailwindFX.jit(logoutItem, "bg-transparent"));
        logoutItem.setOnMouseClicked(e -> {
            DashboardComponents.showToast("Signing out...", DashboardComponents.ToastType.SUCCESS);
            userDropdown.hide();
        });

        dropdown.getChildren().addAll(header, sep1, menuItems, sep2, logoutItem);

        userDropdown = new Popup();
        userDropdown.getContent().add(dropdown);
        userDropdown.setHideOnEscape(true);

        javafx.geometry.Point2D anchorScreen = anchor.localToScreen(
            anchor.getBoundsInLocal().getMaxX(), 
            anchor.getBoundsInLocal().getMaxY());
        userDropdown.show(anchor, anchorScreen.getX() - 280, anchorScreen.getY() + 5);
    }

    private static HBox createDropdownMenuItem(String icon, String title, String description, Runnable action) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        TailwindFX.jit(item, "p-3", "px-4", "bg-transparent");
        item.setCursor(javafx.scene.Cursor.HAND);

        Label iconLabel = new Label(icon);
        TailwindFX.apply(iconLabel, "text-base");

        VBox text = new VBox(2);
        Label titleLabel = new Label(title);
        TailwindFX.apply(titleLabel, "text-sm", "font-medium", "text-gray-900");
        Label descLabel = new Label(description);
        TailwindFX.apply(descLabel, "text-xs", "text-gray-500");
        text.getChildren().addAll(titleLabel, descLabel);

        item.getChildren().addAll(iconLabel, text);

        item.setOnMouseEntered(e -> TailwindFX.jit(item, "bg-gray-50"));
        item.setOnMouseExited(e -> TailwindFX.jit(item, "bg-transparent"));
        item.setOnMouseClicked(e -> {
            action.run();
            userDropdown.hide();
        });

        return item;
    }

    // =========================================================================
    // Welcome Banner
    // =========================================================================

    private static HBox createWelcomeBanner() {
        HBox banner = new HBox(24);
        banner.setAlignment(Pos.CENTER_LEFT);
        TailwindFX.jit(banner, "p-8", "px-10", "bg-gradient-to-r", "from-blue-600", "to-purple-700", "rounded-2xl");

        VBox text = new VBox(8);
        Label title = new Label("Welcome back, John! 👋");
        TailwindFX.apply(title, "text-3xl", "font-bold", "text-white");

        Label subtitle = new Label("Here's what's happening with your business today.");
        TailwindFX.apply(subtitle, "text-base", "text-blue-100");

        text.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(12);
        Button reportsBtn = new Button("📊 View Reports");
        TailwindFX.jit(reportsBtn, "bg-white", "rounded-xl", "text-sm", "font-semibold", "text-blue-600", "p-3", "px-6");
        reportsBtn.setCursor(javafx.scene.Cursor.HAND);
        reportsBtn.setOnMouseEntered(e -> TailwindFX.jit(reportsBtn, "bg-blue-50", "rounded-xl"));
        reportsBtn.setOnMouseExited(e -> TailwindFX.jit(reportsBtn, "bg-white", "rounded-xl"));
        reportsBtn.setOnAction(e -> 
            DashboardComponents.showToast("Opening reports...", DashboardComponents.ToastType.INFO));

        Button exportBtn = new Button("📥 Export Data");
        TailwindFX.jit(exportBtn, "bg-blue-700", "rounded-xl", "text-sm", "font-semibold", "text-white", "p-3", "px-6");
        exportBtn.setCursor(javafx.scene.Cursor.HAND);
        exportBtn.setOnMouseEntered(e -> TailwindFX.jit(exportBtn, "bg-blue-800", "rounded-xl"));
        exportBtn.setOnMouseExited(e -> TailwindFX.jit(exportBtn, "bg-blue-700", "rounded-xl"));
        exportBtn.setOnAction(e -> 
            DashboardComponents.showToast("Exporting data...", DashboardComponents.ToastType.SUCCESS));

        actions.getChildren().addAll(reportsBtn, exportBtn);

        banner.getChildren().addAll(text, spacer, actions);
        return banner;
    }

    // =========================================================================
    // Enhanced Stats Row with Sparklines
    // =========================================================================

    private static HBox createEnhancedStatsRow() {
        HBox stats = new HBox(20);

        stats.getChildren().addAll(
            DashboardComponents.createStatCardWithSparkline(
                "Total Revenue", "$45,231", "+20.1%", true,
                Arrays.asList(30.0, 35.0, 32.0, 40.0, 38.0, 42.0, 45.0),
                Color.rgb(34, 197, 94)),
            DashboardComponents.createStatCardWithSparkline(
                "Active Users", "2,350", "+15.2%", true,
                Arrays.asList(20.0, 22.0, 21.0, 23.0, 24.0, 23.5, 25.0),
                Color.rgb(59, 130, 246)),
            DashboardComponents.createStatCardWithSparkline(
                "Bounce Rate", "12.5%", "-3.2%", false,
                Arrays.asList(15.0, 14.5, 14.0, 13.5, 13.0, 12.8, 12.5),
                Color.rgb(139, 92, 246)),
            DashboardComponents.createStatCardWithSparkline(
                "Avg. Session", "4m 32s", "+8.4%", true,
                Arrays.asList(3.8, 4.0, 4.1, 4.2, 4.15, 4.25, 4.32),
                Color.rgb(245, 158, 11))
        );

        return stats;
    }

    // =========================================================================
    // Line Chart Section
    // =========================================================================

    private static VBox createLineChartSection() {
        List<String> labels = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul");
        
        List<DashboardComponents.ChartData> datasets = Arrays.asList(
            new DashboardComponents.ChartData(
                "Revenue",
                Arrays.asList(35.0, 42.0, 38.0, 50.0, 45.0, 55.0, 48.0),
                Color.rgb(59, 130, 246)),
            new DashboardComponents.ChartData(
                "Expenses",
                Arrays.asList(20.0, 25.0, 22.0, 28.0, 26.0, 30.0, 27.0),
                Color.rgb(239, 68, 68))
        );

        VBox chart = DashboardComponents.createLineChart("Revenue vs Expenses", datasets, labels);
        HBox.setHgrow(chart, Priority.ALWAYS);
        return chart;
    }

    // =========================================================================
    // Activity Feed
    // =========================================================================

    private static VBox createActivityFeed() {
        VBox section = new VBox(16);
        TailwindFX.jit(section, "bg-white", "p-6", "rounded-xl", "shadow-md");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Recent Activity");
        TailwindFX.apply(title, "text-lg", "font-bold", "text-gray-800");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label viewAll = new Label("View all");
        TailwindFX.apply(viewAll, "text-sm", "text-blue-600");
        viewAll.setCursor(javafx.scene.Cursor.HAND);

        header.getChildren().addAll(title, spacer, viewAll);

        VBox activities = new VBox(12);
        activities.getChildren().addAll(
            createActivityItem("👤", "New user registered", "2 minutes ago"),
            createActivityItem("💳", "Payment received - $1,250", "15 minutes ago"),
            createActivityItem("📄", "Report generated", "1 hour ago"),
            createActivityItem("⚙️", "System update completed", "3 hours ago"),
            createActivityItem("💾", "Backup completed successfully", "5 hours ago"),
            createActivityItem("🔔", "New order #1234", "6 hours ago")
        );

        section.getChildren().addAll(header, activities);
        return section;
    }

    private static HBox createActivityItem(String icon, String message, String time) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setCursor(javafx.scene.Cursor.HAND);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(40, 40);
        TailwindFX.jit(iconBox, "bg-blue-50", "rounded-lg");
        Label iconLabel = new Label(icon);
        TailwindFX.apply(iconLabel, "text-lg");
        iconBox.getChildren().add(iconLabel);

        VBox content = new VBox(4);
        Label msgLabel = new Label(message);
        TailwindFX.apply(msgLabel, "text-sm", "font-medium", "text-gray-900");

        Label timeLabel = new Label(time);
        TailwindFX.apply(timeLabel, "text-xs", "text-gray-500");

        content.getChildren().addAll(msgLabel, timeLabel);
        item.getChildren().addAll(iconBox, content);

        item.setOnMouseEntered(e -> TailwindFX.jit(item, "bg-gray-50", "rounded-lg", "p-2"));
        item.setOnMouseExited(e -> TailwindFX.jit(item, "bg-transparent", "p-0"));

        return item;
    }

    // =========================================================================
    // Enhanced Data Table
    // =========================================================================

    private static VBox createEnhancedDataTable() {
        VBox section = new VBox(20);
        TailwindFX.jit(section, "bg-white", "p-6", "rounded-xl", "shadow-md");

        // Header with search and filters
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Recent Orders");
        TailwindFX.apply(title, "text-lg", "font-bold", "text-gray-800");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Search orders...");
        TailwindFX.jit(searchField, "input", "w-64");
        
        Button filterBtn = new Button("🔍 Filter");
        TailwindFX.jit(filterBtn, "bg-gray-100", "rounded-lg", "px-4", "py-2");
        filterBtn.setCursor(javafx.scene.Cursor.HAND);
        filterBtn.setOnAction(e -> 
            DashboardComponents.showToast("Filter options coming soon!", DashboardComponents.ToastType.INFO));

        Button exportBtn = new Button("📥 Export");
        TailwindFX.jit(exportBtn, "bg-blue-600", "text-white", "rounded-lg", "px-4", "py-2");
        exportBtn.setCursor(javafx.scene.Cursor.HAND);
        exportBtn.setOnAction(e -> 
            DashboardComponents.showToast("Exporting orders...", DashboardComponents.ToastType.SUCCESS));

        header.getChildren().addAll(title, spacer, searchField, filterBtn, exportBtn);

        // Sample data table using FxDataTable
        FxDataTable<Order> table = FxDataTable.of(Order.class)
            .column("Order ID", Order::getOrderId)
            .column("Customer", Order::getCustomer)
            .column("Product", Order::getProduct)
            .column("Amount", Order::getAmount)
            .column("Status", Order::getStatus)
            .column("Date", Order::getDate)
            .searchable(true)
            .pageSize(10)
            .style("table-striped", "table-hover")
            .build();

        // Sample data
        java.util.List<Order> orders = java.util.Arrays.asList(
            new Order("#1234", "Alice Smith", "MacBook Pro", "$2,499", "completed", "2024-01-15"),
            new Order("#1235", "Bob Johnson", "iPhone 15", "$999", "pending", "2024-01-15"),
            new Order("#1236", "Carol Williams", "AirPods Pro", "$249", "shipped", "2024-01-14"),
            new Order("#1237", "David Brown", "iPad Air", "$799", "processing", "2024-01-14"),
            new Order("#1238", "Eve Davis", "Apple Watch", "$399", "completed", "2024-01-13"),
            new Order("#1239", "Frank Miller", "Mac Mini", "$699", "pending", "2024-01-13"),
            new Order("#1240", "Grace Wilson", "Magic Keyboard", "$299", "shipped", "2024-01-12"),
            new Order("#1241", "Henry Moore", "Studio Display", "$1,599", "completed", "2024-01-12")
        );

        table.setItems(orders);

        section.getChildren().addAll(header, table.container());
        return section;
    }

    public static class Order {
        private String orderId, customer, product, amount, status, date;

        public Order(String orderId, String customer, String product, String amount, String status, String date) {
            this.orderId = orderId;
            this.customer = customer;
            this.product = product;
            this.amount = amount;
            this.status = status;
            this.date = date;
        }

        public String getOrderId() { return orderId; }
        public String getCustomer() { return customer; }
        public String getProduct() { return product; }
        public String getAmount() { return amount; }
        public String getStatus() { return status; }
        public String getDate() { return date; }
    }

    // =========================================================================
    // Demo Controls
    // =========================================================================

    private static VBox createDemoControls() {
        VBox section = new VBox(20);
        TailwindFX.jit(section, "bg-white", "p-6", "rounded-xl", "shadow-md");

        Label title = new Label("UI Components Showcase");
        TailwindFX.apply(title, "text-xl", "font-bold", "text-gray-900");

        // Buttons row
        HBox buttonsRow = new HBox(12);
        buttonsRow.setAlignment(Pos.CENTER_LEFT);

        Button primary = new Button("Primary Button");
        TailwindFX.jit(primary, "bg-blue-600", "text-white", "rounded-lg", "px-4", "py-2");
        primary.setCursor(javafx.scene.Cursor.HAND);
        primary.setOnAction(e -> 
            DashboardComponents.showToast("Primary button clicked!", DashboardComponents.ToastType.SUCCESS));

        Button secondary = new Button("Secondary");
        TailwindFX.jit(secondary, "bg-gray-200", "text-gray-800", "rounded-lg", "px-4", "py-2");
        secondary.setCursor(javafx.scene.Cursor.HAND);

        Button success = new Button("✓ Success");
        TailwindFX.jit(success, "bg-green-600", "text-white", "rounded-lg", "px-4", "py-2");
        success.setCursor(javafx.scene.Cursor.HAND);
        success.setOnAction(e -> 
            DashboardComponents.showToast("Operation successful!", DashboardComponents.ToastType.SUCCESS));

        Button danger = new Button("✕ Delete");
        TailwindFX.jit(danger, "bg-red-600", "text-white", "rounded-lg", "px-4", "py-2");
        danger.setCursor(javafx.scene.Cursor.HAND);
        danger.setOnAction(e -> 
            DashboardComponents.showToast("Item deleted", DashboardComponents.ToastType.ERROR));

        Button warning = new Button("⚠ Warning");
        TailwindFX.jit(warning, "bg-yellow-500", "text-white", "rounded-lg", "px-4", "py-2");
        warning.setCursor(javafx.scene.Cursor.HAND);
        warning.setOnAction(e -> 
            DashboardComponents.showToast("This is a warning!", DashboardComponents.ToastType.WARNING));

        buttonsRow.getChildren().addAll(primary, secondary, success, danger, warning);

        // Badges row
        HBox badgesRow = new HBox(12);
        badgesRow.setAlignment(Pos.CENTER_LEFT);

        Label badge1 = new Label("New");
        TailwindFX.jit(badge1, "bg-blue-100", "text-blue-800", "px-3", "py-1", "rounded-full", "text-sm");

        Label badge2 = new Label("Active");
        TailwindFX.jit(badge2, "bg-green-100", "text-green-800", "px-3", "py-1", "rounded-full", "text-sm");

        Label badge3 = new Label("Pending");
        TailwindFX.jit(badge3, "bg-yellow-100", "text-yellow-800", "px-3", "py-1", "rounded-full", "text-sm");

        Label badge4 = new Label("Error");
        TailwindFX.jit(badge4, "bg-red-100", "text-red-800", "px-3", "py-1", "rounded-full", "text-sm");

        badgesRow.getChildren().addAll(new Label("Badges: "), badge1, badge2, badge3, badge4);

        section.getChildren().addAll(title, buttonsRow, badgesRow);
        return section;
    }
}
