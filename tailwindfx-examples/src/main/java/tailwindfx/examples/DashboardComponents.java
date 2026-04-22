package tailwindfx.examples;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import tailwindfx.AnimationUtil;
import tailwindfx.ComponentFactory;
import tailwindfx.TailwindFX;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Advanced dashboard components with real data visualization.
 * Line charts, pie charts, sparklines, toasts, modals, and more.
 */
public class DashboardComponents {

    // =========================================================================
    // Interactive Line Chart with Canvas
    // =========================================================================

    public static VBox createLineChart(String title, List<ChartData> datasets, List<String> labels) {
        VBox container = new VBox(16);
        TailwindFX.jit(container, "bg-white", "p-6", "rounded-xl", "shadow-md");

        // Header with period selector
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(header, Priority.ALWAYS);

        Label titleLabel = new Label(title);
        TailwindFX.apply(titleLabel, "text-lg", "font-bold", "text-gray-800");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox periodSelector = createPeriodSelector();

        header.getChildren().addAll(titleLabel, spacer, periodSelector);

        // Canvas for chart
        Canvas canvas = new Canvas(600, 300);
        TailwindFX.jit(canvas, "w-full", "rounded-lg");

        drawLineChart(canvas, datasets, labels);

        container.getChildren().addAll(header, canvas);
        return container;
    }

    private static void drawLineChart(Canvas canvas, List<ChartData> datasets, List<String> labels) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // Clear
        gc.clearRect(0, 0, width, height);

        double padding = 60;
        double chartWidth = width - padding * 2;
        double chartHeight = height - padding * 2;

        // Find min/max for scaling
        double minVal = datasets.stream().flatMapToDouble(d -> d.values.stream().mapToDouble(Double::doubleValue)).min().orElse(0);
        double maxVal = datasets.stream().flatMapToDouble(d -> d.values.stream().mapToDouble(Double::doubleValue)).max().orElse(100);
        double range = maxVal - minVal;
        if (range == 0) range = 100;

        // Draw grid lines
        gc.setStroke(Color.rgb(229, 231, 235));
        gc.setLineWidth(1);
        for (int i = 0; i <= 5; i++) {
            double y = padding + (chartHeight / 5) * i;
            gc.strokeLine(padding, y, width - padding, y);

            // Y-axis labels
            double value = maxVal - (range / 5) * i;
            gc.setFill(Color.rgb(107, 114, 128));
            gc.setFont(javafx.scene.text.Font.font(11));
            gc.fillText(String.format("%.0f", value), 10, y + 4);
        }

        // Draw X-axis labels
        gc.setFill(Color.rgb(107, 114, 128));
        gc.setFont(javafx.scene.text.Font.font(11));
        double stepX = chartWidth / Math.max(1, labels.size() - 1);
        for (int i = 0; i < labels.size(); i++) {
            double x = padding + stepX * i;
            String label = labels.get(i);
            // Approximate text width for centering
            javafx.scene.text.Text text = new javafx.scene.text.Text(label);
            text.setFont(gc.getFont());
            double textWidth = text.getBoundsInLocal().getWidth();
            gc.fillText(label, x - textWidth / 2, height - padding + 20);
        }

        // Draw datasets
        for (ChartData dataset : datasets) {
            if (dataset.values.isEmpty()) continue;

            // Draw area fill
            gc.setFill(dataset.color.deriveColor(0, 1, 1, 0.1));
            gc.beginPath();
            for (int i = 0; i < dataset.values.size(); i++) {
                double x = padding + stepX * i;
                double y = padding + chartHeight - ((dataset.values.get(i) - minVal) / range * chartHeight);
                if (i == 0) {
                    gc.moveTo(x, y);
                } else {
                    gc.lineTo(x, y);
                }
            }
            gc.lineTo(padding + stepX * (dataset.values.size() - 1), padding + chartHeight);
            gc.lineTo(padding, padding + chartHeight);
            gc.closePath();
            gc.fill();

            // Draw line
            gc.setStroke(dataset.color);
            gc.setLineWidth(2.5);
            gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
            gc.beginPath();
            for (int i = 0; i < dataset.values.size(); i++) {
                double x = padding + stepX * i;
                double y = padding + chartHeight - ((dataset.values.get(i) - minVal) / range * chartHeight);
                if (i == 0) {
                    gc.moveTo(x, y);
                } else {
                    gc.lineTo(x, y);
                }
            }
            gc.stroke();

            // Draw data points
            for (int i = 0; i < dataset.values.size(); i++) {
                double x = padding + stepX * i;
                double y = padding + chartHeight - ((dataset.values.get(i) - minVal) / range * chartHeight);

                gc.setFill(Color.WHITE);
                gc.fillOval(x - 4, y - 4, 8, 8);

                gc.setStroke(dataset.color);
                gc.setLineWidth(2);
                gc.strokeOval(x - 4, y - 4, 8, 8);
            }
        }

        // Draw legend
        double legendY = 15;
        double legendX = padding;
        gc.setFont(javafx.scene.text.Font.font(null, javafx.scene.text.FontWeight.BOLD, 12));
        for (ChartData dataset : datasets) {
            gc.setFill(dataset.color);
            gc.fillRoundRect(legendX, legendY - 8, 12, 12, 6, 6);

            gc.setFill(Color.rgb(55, 65, 81));
            javafx.scene.text.Text text = new javafx.scene.text.Text(dataset.label);
            text.setFont(gc.getFont());
            gc.fillText(dataset.label, legendX + 18, legendY + 2);
            legendX += text.getBoundsInLocal().getWidth() + 40;
        }
    }

    private static HBox createPeriodSelector() {
        HBox selector = new HBox(8);
        selector.setAlignment(Pos.CENTER_RIGHT);

        String[] periods = {"7D", "30D", "90D", "1Y"};
        for (String period : periods) {
            Button btn = new Button(period);
            boolean isActive = period.equals("30D");
            if (isActive) {
                TailwindFX.jit(btn, "bg-blue-600", "text-white", "rounded-lg", "px-3", "py-1", "text-sm");
            } else {
                TailwindFX.jit(btn, "bg-gray-100", "text-gray-700", "rounded-lg", "px-3", "py-1", "text-sm");
                btn.setOnMouseEntered(e -> TailwindFX.jit(btn, "bg-gray-200"));
                btn.setOnMouseExited(e -> TailwindFX.jit(btn, "bg-gray-100"));
            }
            btn.setCursor(javafx.scene.Cursor.HAND);
            selector.getChildren().add(btn);
        }

        return selector;
    }

    // =========================================================================
    // Pie/Donut Chart
    // =========================================================================

    public static VBox createPieChart(String title, List<PieSlice> slices) {
        VBox container = new VBox(16);
        TailwindFX.jit(container, "bg-white", "p-6", "rounded-xl", "shadow-md");

        Label titleLabel = new Label(title);
        TailwindFX.apply(titleLabel, "text-lg", "font-bold", "text-gray-800");

        HBox content = new HBox(32);
        content.setAlignment(Pos.CENTER_LEFT);

        // Canvas for pie chart
        Canvas canvas = new Canvas(250, 250);
        drawPieChart(canvas, slices);

        // Legend
        VBox legend = new VBox(12);
        double total = slices.stream().mapToDouble(s -> s.value).sum();

        for (PieSlice slice : slices) {
            HBox item = new HBox(12);
            item.setAlignment(Pos.CENTER_LEFT);

            Region colorBox = new Region();
            colorBox.setPrefSize(16, 16);
            TailwindFX.jit(colorBox, "rounded");
            colorBox.setStyle(String.format("-fx-background-color: #%02X%02X%02X;",
                    (int)(slice.color.getRed() * 255),
                    (int)(slice.color.getGreen() * 255),
                    (int)(slice.color.getBlue() * 255)));

            VBox text = new VBox(2);
            Label label = new Label(slice.label);
            TailwindFX.apply(label, "text-sm", "font-medium", "text-gray-700");

            Label percentage = new Label(String.format("%.1f%%", (slice.value / total) * 100));
            TailwindFX.apply(percentage, "text-xs", "text-gray-500");

            text.getChildren().addAll(label, percentage);
            item.getChildren().addAll(colorBox, text);
            legend.getChildren().add(item);
        }

        content.getChildren().addAll(canvas, legend);
        container.getChildren().addAll(titleLabel, content);

        return container;
    }

    private static void drawPieChart(Canvas canvas, List<PieSlice> slices) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;
        double radius = Math.min(centerX, centerY) - 10;
        double donutRadius = radius * 0.6; // For donut chart

        double total = slices.stream().mapToDouble(s -> s.value).sum();
        double currentAngle = -90; // Start from top

        gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);

        for (PieSlice slice : slices) {
            double angle = (slice.value / total) * 360;

            // Draw slice
            gc.setFill(slice.color);
            gc.beginPath();
            gc.moveTo(centerX, centerY);
            gc.arc(centerX, centerY, radius, radius, currentAngle, angle);
            gc.closePath();
            gc.fill();

            // White border
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.stroke();

            currentAngle += angle;
        }

        // Draw donut hole
        gc.setFill(Color.WHITE);
        gc.fillOval(centerX - donutRadius, centerY - donutRadius, donutRadius * 2, donutRadius * 2);

        // Center text
        gc.setFill(Color.rgb(55, 65, 81));
        gc.setFont(javafx.scene.text.Font.font(null, javafx.scene.text.FontWeight.BOLD, 18));
        String totalText = String.format("%.0f", total);
        javafx.scene.text.Text textNode = new javafx.scene.text.Text(totalText);
        textNode.setFont(gc.getFont());
        double textWidth = textNode.getBoundsInLocal().getWidth();
        gc.fillText(totalText, centerX - textWidth / 2, centerY + 6);
    }

    // =========================================================================
    // Sparkline (Mini inline chart for stats)
    // =========================================================================

    public static HBox createStatCardWithSparkline(String title, String value, String change,
                                                    boolean isPositive, List<Double> data, Color color) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        TailwindFX.jit(card, "bg-white", "p-5", "rounded-xl", "shadow-md");
        card.setPrefWidth(280);

        // Text content
        VBox text = new VBox(8);

        Label titleLabel = new Label(title);
        TailwindFX.apply(titleLabel, "text-sm", "text-gray-600");

        Label valueLabel = new Label(value);
        TailwindFX.apply(valueLabel, "text-2xl", "font-bold", "text-gray-900");

        HBox changeBox = new HBox(4);
        changeBox.setAlignment(Pos.CENTER_LEFT);
        Label changeIcon = new Label(isPositive ? "📈" : "📉");
        Label changeLabel = new Label(change);
        TailwindFX.apply(changeLabel, "text-sm", "font-medium");
        if (isPositive) {
            TailwindFX.apply(changeLabel, "text-green-600");
        } else {
            TailwindFX.apply(changeLabel, "text-red-600");
        }
        changeBox.getChildren().addAll(changeIcon, changeLabel);

        text.getChildren().addAll(titleLabel, valueLabel, changeBox);

        // Sparkline canvas
        Canvas canvas = new Canvas(100, 40);
        drawSparkline(canvas, data, color);

        card.getChildren().addAll(text, canvas);

        // Hover effect
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseEntered(e -> {
            card.setEffect(new DropShadow(15, Color.rgb(0, 0, 0, 0.15)));
            AnimationUtil.onHoverScale(card, 1.02);
        });
        card.setOnMouseExited(e -> {
            card.setEffect(new DropShadow(4, Color.rgb(0, 0, 0, 0.1)));
        });

        return card;
    }

    private static void drawSparkline(Canvas canvas, List<Double> data, Color color) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        if (data.isEmpty()) return;

        double min = data.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = data.stream().mapToDouble(Double::doubleValue).max().orElse(100);
        double range = max - min;
        if (range == 0) range = 1;

        double stepX = width / Math.max(1, data.size() - 1);

        // Draw area
        gc.setFill(color.deriveColor(0, 1, 1, 0.2));
        gc.beginPath();
        for (int i = 0; i < data.size(); i++) {
            double x = stepX * i;
            double y = height - ((data.get(i) - min) / range * (height - 4)) - 2;
            if (i == 0) gc.moveTo(x, y);
            else gc.lineTo(x, y);
        }
        gc.lineTo(width, height);
        gc.lineTo(0, height);
        gc.closePath();
        gc.fill();

        // Draw line
        gc.setStroke(color);
        gc.setLineWidth(2);
        gc.beginPath();
        for (int i = 0; i < data.size(); i++) {
            double x = stepX * i;
            double y = height - ((data.get(i) - min) / range * (height - 4)) - 2;
            if (i == 0) gc.moveTo(x, y);
            else gc.lineTo(x, y);
        }
        gc.stroke();
    }

    // =========================================================================
    // Toast Notification System
    // =========================================================================

    private static VBox toastContainer;
    private static final List<Timeline> activeToasts = new ArrayList<>();

    public static void initToastContainer(Pane parent) {
        toastContainer = new VBox(12);
        toastContainer.setAlignment(Pos.TOP_RIGHT);
        TailwindFX.jit(toastContainer, "p-4");
        toastContainer.setTranslateX(-20);
        toastContainer.setTranslateY(20);

        parent.getChildren().add(toastContainer);
        StackPane.setMargin(toastContainer, new Insets(0, 0, 0, 0));
    }

    public static void showToast(String message, ToastType type) {
        if (toastContainer == null) return;

        HBox toast = new HBox(12);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setMaxWidth(380);
        TailwindFX.jit(toast, "bg-white", "rounded-lg", "shadow-lg", "p-4");

        // Border color based on type
        Color borderColor = switch (type) {
            case SUCCESS -> Color.rgb(34, 197, 94);
            case ERROR -> Color.rgb(239, 68, 68);
            case WARNING -> Color.rgb(245, 158, 11);
            case INFO -> Color.rgb(59, 130, 246);
        };

        toast.setStyle(String.format("-fx-border-color: #%02X%02X%02X; -fx-border-width: 0 0 0 4; -fx-border-radius: 8;",
                (int)(borderColor.getRed() * 255),
                (int)(borderColor.getGreen() * 255),
                (int)(borderColor.getBlue() * 255)));

        // Icon
        Label icon = new Label(switch (type) {
            case SUCCESS -> "✓";
            case ERROR -> "✕";
            case WARNING -> "⚠";
            case INFO -> "ℹ";
        });
        TailwindFX.jit(icon, "text-xl");
        icon.setTextFill(borderColor);

        // Message
        Label messageLabel = new Label(message);
        TailwindFX.apply(messageLabel, "text-sm", "text-gray-700");
        HBox.setHgrow(messageLabel, Priority.ALWAYS);

        // Close button
        Button closeBtn = new Button("✕");
        TailwindFX.jit(closeBtn, "text-gray-400", "bg-transparent");
        closeBtn.setCursor(javafx.scene.Cursor.HAND);
        closeBtn.setOnAction(e -> dismissToast(toast));

        toast.getChildren().addAll(icon, messageLabel, closeBtn);
        toastContainer.getChildren().add(toast);

        // Fade in animation
        toast.setOpacity(0);
        toast.setTranslateX(20);
        Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(toast.opacityProperty(), 0),
                        new KeyValue(toast.translateXProperty(), 20)),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(toast.opacityProperty(), 1),
                        new KeyValue(toast.translateXProperty(), 0))
        );
        fadeIn.play();

        // Auto dismiss after 4 seconds
        Timeline autoDismiss = new Timeline(
                new KeyFrame(Duration.seconds(4),
                        new KeyValue(toast.opacityProperty(), 0),
                        new KeyValue(toast.translateXProperty(), 20))
        );
        autoDismiss.setOnFinished(e -> toastContainer.getChildren().remove(toast));
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> autoDismiss.play());
        delay.play();

        activeToasts.add(autoDismiss);
    }

    private static void dismissToast(HBox toast) {
        Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.millis(200),
                        new KeyValue(toast.opacityProperty(), 0),
                        new KeyValue(toast.translateXProperty(), 20))
        );
        fadeOut.setOnFinished(e -> toastContainer.getChildren().remove(toast));
        fadeOut.play();
    }

    public enum ToastType {
        SUCCESS, ERROR, WARNING, INFO
    }

    // =========================================================================
    // Modal Dialog
    // =========================================================================

    public static StackPane createModalOverlay(Node content) {
        StackPane modal = new StackPane();
        TailwindFX.jit(modal, "bg-black");
        modal.setOpacity(0.5);

        modal.getChildren().add(content);
        StackPane.setAlignment(content, Pos.CENTER);

        return modal;
    }

    public static VBox createModalDialog(String title, Node body, List<Button> actions) {
        VBox dialog = new VBox(20);
        dialog.setMaxWidth(600);
        TailwindFX.jit(dialog, "bg-white", "rounded-2xl", "shadow-2xl", "p-6");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        TailwindFX.apply(titleLabel, "text-xl", "font-bold", "text-gray-900");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        TailwindFX.jit(closeBtn, "text-gray-400", "bg-gray-100", "rounded-full", "w-8", "h-8");
        closeBtn.setCursor(javafx.scene.Cursor.HAND);
        closeBtn.setOnAction(e -> {
            StackPane parent = (StackPane) dialog.getParent();
            parent.setVisible(false);
        });

        header.getChildren().addAll(titleLabel, spacer, closeBtn);

        // Body
        VBox bodyContainer = new VBox(16);
        bodyContainer.getChildren().add(body);

        // Footer with actions
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        footer.getChildren().add(footerSpacer);
        footer.getChildren().addAll(actions);

        dialog.getChildren().addAll(header, bodyContainer, footer);

        // Animate in
        dialog.setScaleX(0.9);
        dialog.setScaleY(0.9);
        dialog.setOpacity(0);

        Timeline animateIn = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(dialog.scaleXProperty(), 0.9),
                        new KeyValue(dialog.scaleYProperty(), 0.9),
                        new KeyValue(dialog.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(dialog.scaleXProperty(), 1.0),
                        new KeyValue(dialog.scaleYProperty(), 1.0),
                        new KeyValue(dialog.opacityProperty(), 1.0))
        );
        animateIn.play();

        return dialog;
    }

    // =========================================================================
    // Calendar Widget
    // =========================================================================

    public static VBox createCalendarWidget() {
        VBox container = new VBox(16);
        TailwindFX.jit(container, "bg-white", "p-5", "rounded-xl", "shadow-md");

        // Month header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label monthLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        TailwindFX.apply(monthLabel, "text-lg", "font-bold", "text-gray-800");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button prevBtn = new Button("◀");
        Button nextBtn = new Button("▶");
        TailwindFX.jit(prevBtn, "bg-gray-100", "rounded-lg", "w-7", "h-7");
        TailwindFX.jit(nextBtn, "bg-gray-100", "rounded-lg", "w-7", "h-7");
        prevBtn.setCursor(javafx.scene.Cursor.HAND);
        nextBtn.setCursor(javafx.scene.Cursor.HAND);

        header.getChildren().addAll(monthLabel, spacer, prevBtn, nextBtn);

        // Days of week
        GridPane calendar = new GridPane();
        calendar.setHgap(4);
        calendar.setVgap(4);

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(days[i]);
            TailwindFX.apply(dayLabel, "text-xs", "font-semibold", "text-gray-500");
            calendar.add(dayLabel, i, 0);
        }

        // Calendar days (simplified for current month)
        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1=Monday
        int daysInMonth = today.lengthOfMonth();

        int row = 1;
        int col = dayOfWeek - 1;

        for (int day = 1; day <= daysInMonth; day++) {
            StackPane dayCell = new StackPane();
            dayCell.setPrefSize(36, 36);

            Label dayNum = new Label(String.valueOf(day));
            TailwindFX.apply(dayNum, "text-sm", "text-gray-700");

            dayCell.getChildren().add(dayNum);

            if (day == today.getDayOfMonth()) {
                TailwindFX.jit(dayCell, "bg-blue-600", "rounded-full");
                TailwindFX.apply(dayNum, "text-white", "font-bold");
            } else {
                dayCell.setCursor(javafx.scene.Cursor.HAND);
                final int currentDay = day; // Capture in final variable
                dayCell.setOnMouseEntered(e -> TailwindFX.jit(dayCell, "bg-gray-100", "rounded-full"));
                dayCell.setOnMouseExited(e -> {
                    if (currentDay != today.getDayOfMonth()) {
                        TailwindFX.jit(dayCell, "bg-transparent");
                    }
                });
            }

            calendar.add(dayCell, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }

        // Upcoming events
        VBox events = new VBox(8);
        TailwindFX.jit(events, "mt-4", "pt-4", "border-t", "border-gray-200");

        Label eventsTitle = new Label("Upcoming Events");
        TailwindFX.apply(eventsTitle, "text-sm", "font-semibold", "text-gray-700");

        VBox eventList = new VBox(6);
        eventList.getChildren().addAll(
                createEventItem("Team Meeting", "10:00 AM", "blue"),
                createEventItem("Project Review", "2:00 PM", "green"),
                createEventItem("Client Call", "4:30 PM", "purple")
        );

        events.getChildren().addAll(eventsTitle, eventList);
        container.getChildren().addAll(header, calendar, events);

        return container;
    }

    private static HBox createEventItem(String title, String time, String color) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);

        Region colorIndicator = new Region();
        colorIndicator.setPrefSize(4, 32);
        TailwindFX.jit(colorIndicator, "bg-" + color + "-500", "rounded");

        VBox text = new VBox(2);
        Label titleLabel = new Label(title);
        TailwindFX.apply(titleLabel, "text-sm", "font-medium", "text-gray-700");

        Label timeLabel = new Label(time);
        TailwindFX.apply(timeLabel, "text-xs", "text-gray-500");

        text.getChildren().addAll(titleLabel, timeLabel);
        item.getChildren().addAll(colorIndicator, text);

        item.setCursor(javafx.scene.Cursor.HAND);
        item.setOnMouseEntered(e -> TailwindFX.jit(item, "bg-gray-50", "rounded-lg", "p-2"));
        item.setOnMouseExited(e -> TailwindFX.jit(item, "bg-transparent", "p-0"));

        return item;
    }

    // =========================================================================
    // Skeleton Loading States
    // =========================================================================

    public static VBox createSkeletonCard() {
        VBox card = new VBox(16);
        TailwindFX.jit(card, "bg-white", "p-6", "rounded-xl", "shadow-md");

        // Animated skeleton lines
        VBox skeleton = new VBox(12);
        
        Region titleSkeleton = new Region();
        titleSkeleton.setPrefSize(200, 24);
        TailwindFX.jit(titleSkeleton, "bg-gray-200", "rounded");
        animateSkeleton(titleSkeleton);

        Region line1 = new Region();
        line1.setPrefSize(280, 16);
        TailwindFX.jit(line1, "bg-gray-200", "rounded");
        animateSkeleton(line1);

        Region line2 = new Region();
        line2.setPrefSize(240, 16);
        TailwindFX.jit(line2, "bg-gray-200", "rounded");
        animateSkeleton(line2);

        Region line3 = new Region();
        line3.setPrefSize(180, 16);
        TailwindFX.jit(line3, "bg-gray-200", "rounded");
        animateSkeleton(line3);

        skeleton.getChildren().addAll(titleSkeleton, line1, line2, line3);
        card.getChildren().add(skeleton);

        return card;
    }

    public static HBox createSkeletonStatCard() {
        HBox card = new HBox(16);
        TailwindFX.jit(card, "bg-white", "p-5", "rounded-xl", "shadow-md");
        card.setPrefWidth(280);

        VBox text = new VBox(12);
        
        Region iconSkeleton = new Region();
        iconSkeleton.setPrefSize(48, 48);
        TailwindFX.jit(iconSkeleton, "bg-gray-200", "rounded-xl");
        animateSkeleton(iconSkeleton);

        Region titleSkeleton = new Region();
        titleSkeleton.setPrefSize(120, 16);
        TailwindFX.jit(titleSkeleton, "bg-gray-200", "rounded");
        animateSkeleton(titleSkeleton);

        Region valueSkeleton = new Region();
        valueSkeleton.setPrefSize(100, 32);
        TailwindFX.jit(valueSkeleton, "bg-gray-200", "rounded");
        animateSkeleton(valueSkeleton);

        Region changeSkeleton = new Region();
        changeSkeleton.setPrefSize(80, 20);
        TailwindFX.jit(changeSkeleton, "bg-gray-200", "rounded-full");
        animateSkeleton(changeSkeleton);

        text.getChildren().addAll(titleSkeleton, valueSkeleton, changeSkeleton);

        Region chartSkeleton = new Region();
        chartSkeleton.setPrefSize(100, 40);
        TailwindFX.jit(chartSkeleton, "bg-gray-200", "rounded");
        animateSkeleton(chartSkeleton);

        card.getChildren().addAll(text, chartSkeleton);
        return card;
    }

    private static void animateSkeleton(Node node) {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.ZERO,
                new javafx.animation.KeyValue(node.opacityProperty(), 0.5)),
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1),
                new javafx.animation.KeyValue(node.opacityProperty(), 1.0)),
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2),
                new javafx.animation.KeyValue(node.opacityProperty(), 0.5))
        );
        timeline.setAutoReverse(true);
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }

    // =========================================================================
    // Data Models
    // =========================================================================

    public static class ChartData {
        public String label;
        public List<Double> values;
        public Color color;

        public ChartData(String label, List<Double> values, Color color) {
            this.label = label;
            this.values = values;
            this.color = color;
        }
    }

    public static class PieSlice {
        public String label;
        public double value;
        public Color color;

        public PieSlice(String label, double value, Color color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }
}
