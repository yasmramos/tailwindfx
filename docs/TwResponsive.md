# TwResponsive Documentation

The `TwResponsive` class is a responsive facade for breakpoint management in TailwindFX. It enables responsive design patterns by monitoring scene/region size changes and applying breakpoint-based styles.

## Overview

`TwResponsive` provides responsive design capabilities by installing breakpoint monitoring on stages or regions. It delegates to `BreakpointManager` for breakpoint detection and `ResponsiveNode` for reactive behavior.

## Key Features

- **Stage-based Monitoring**: Install responsive support on entire stages
- **Region-based Monitoring**: Monitor specific regions for size changes
- **Breakpoint Detection**: Automatic detection of sm, md, lg, xl, 2xl breakpoints
- **Reactive Updates**: Property-based notifications for breakpoint changes
- **Scene Integration**: Works with JavaFX scenes and stages

## Basic Usage

### Stage-based Responsive Support

```java
// Install responsive support on a stage
TwResponsive.on(stage);

// Get breakpoint manager
BreakpointManager bpm = BreakpointManager.forStage(stage);

// Listen for breakpoint changes
bpm.currentBreakpointProperty().addListener((obs, oldVal, newVal) -> {
    System.out.println("Current breakpoint: " + newVal);
    
    // Apply different layouts based on breakpoint
    switch (newVal) {
        case "sm": applyMobileLayout(); break;
        case "md": applyTabletLayout(); break;
        case "lg": applyDesktopLayout(); break;
    }
});
```

### Region-based Responsive Support

```java
// Install responsive support on a region
ResponsiveNode responsive = TwResponsive.on(region);

// Monitor the region
responsive.breakpointProperty().addListener((obs, oldVal, newVal) -> {
    updateLayoutForBreakpoint(newVal);
});
```

### Access Breakpoint Manager

```java
// Get manager for stage
BreakpointManager manager = BreakpointManager.forStage(stage);

// Get current breakpoint
String currentBp = manager.getCurrentBreakpoint();

// Get all available breakpoints
Set<String> breakpoints = manager.getAvailableBreakpoints();
```

## Advanced Usage

### Conditional Styling

```java
stage.widthProperty().addListener((obs, oldVal, newVal) -> {
    String bp = BreakpointManager.getBreakpoint(newVal.doubleValue());
    
    if ("sm".equals(bp)) {
        // Mobile: stack vertically
        container.setOrientation(Orientation.VERTICAL);
        sidebar.setVisible(false);
    } else if ("lg".equals(bp)) {
        // Desktop: horizontal layout with sidebar
        container.setOrientation(Orientation.HORIZONTAL);
        sidebar.setVisible(true);
    }
});
```

### Responsive Grid

```java
public void setupResponsiveGrid(GridPane grid, Stage stage) {
    BreakpointManager bpm = BreakpointManager.forStage(stage);
    
    bpm.currentBreakpointProperty().addListener((obs, oldVal, newVal) -> {
        int cols = getColumnsForBreakpoint(newVal);
        
        // Update grid constraints
        grid.getColumnConstraints().clear();
        for (int i = 0; i < cols; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / cols);
            grid.getColumnConstraints().add(col);
        }
        
        // Reorganize children
        reorganizeGridChildren(grid, cols);
    });
}

private int getColumnsForBreakpoint(String breakpoint) {
    switch (breakpoint) {
        case "sm": return 1;
        case "md": return 2;
        case "lg": return 3;
        case "xl": return 4;
        default: return 1;
    }
}
```

### Responsive Navigation

```java
public class ResponsiveNavigation extends BorderPane {
    
    private final HBox navLinks;
    private final MenuButton mobileMenu;
    
    public ResponsiveNavigation() {
        navLinks = new HBox(10);
        mobileMenu = new MenuButton("Menu");
        
        // Setup links
        navLinks.getChildren().addAll(
            createLink("Home"),
            createLink("About"),
            createLink("Contact")
        );
        
        // Install responsive monitoring
        TwResponsive.on(this);
        
        // React to breakpoint changes
        widthProperty().addListener((obs, oldVal, newVal) -> {
            updateNavigation(newVal.doubleValue());
        });
    }
    
    private void updateNavigation(double width) {
        String bp = BreakpointManager.getBreakpoint(width);
        
        if ("sm".equals(bp)) {
            // Mobile: show hamburger menu
            getLeft().setAll(mobileMenu);
            navLinks.setVisible(false);
        } else {
            // Desktop: show full navigation
            getLeft().setAll(navLinks);
            mobileMenu.setVisible(false);
        }
    }
}
```

## Breakpoint Values

Default TailwindFX breakpoints:

| Breakpoint | Min Width | Typical Device |
|------------|-----------|----------------|
| `sm` | 640px | Large phones |
| `md` | 768px | Tablets |
| `lg` | 1024px | Laptops |
| `xl` | 1280px | Desktops |
| `2xl` | 1536px | Large screens |

## Common Patterns

### Responsive Card Layout

```java
public class ResponsiveCardGrid extends FlowPane {
    
    public ResponsiveCardGrid() {
        setHgap(16);
        setVgap(16);
        
        widthProperty().addListener((obs, oldVal, newVal) -> {
            updateCardWidth(newVal.doubleValue());
        });
    }
    
    private void updateCardWidth(double containerWidth) {
        String bp = BreakpointManager.getBreakpoint(containerWidth);
        double cardWidth;
        
        switch (bp) {
            case "sm":
                cardWidth = containerWidth - 32; // Full width minus padding
                break;
            case "md":
                cardWidth = (containerWidth - 48) / 2; // 2 columns
                break;
            default:
                cardWidth = (containerWidth - 64) / 3; // 3 columns
        }
        
        for (Node child : getChildren()) {
            if (child instanceof Region) {
                ((Region) child).setPrefWidth(cardWidth);
            }
        }
    }
}
```

### Responsive Sidebar

```java
public class SidebarLayout extends BorderPane {
    
    private final VBox sidebar;
    private String currentBreakpoint = "lg";
    
    public SidebarLayout() {
        sidebar = new VBox(8);
        sidebar.setPrefWidth(256);
        sidebar.getStyleClass().add("bg-gray-800");
        
        setLeft(sidebar);
        
        // Monitor width changes
        widthProperty().addListener((obs, oldVal, newVal) -> {
            String bp = BreakpointManager.getBreakpoint(newVal.doubleValue());
            if (!bp.equals(currentBreakpoint)) {
                currentBreakpoint = bp;
                updateSidebar(bp);
            }
        });
    }
    
    private void updateSidebar(String breakpoint) {
        if ("sm".equals(breakpoint) || "md".equals(breakpoint)) {
            // Hide sidebar on mobile/tablet
            sidebar.setVisible(false);
            sidebar.setManaged(false);
        } else {
            // Show sidebar on desktop
            sidebar.setVisible(true);
            sidebar.setManaged(true);
        }
    }
}
```

### Responsive Typography

```java
public void applyResponsiveText(Text text, Stage stage) {
    BreakpointManager bpm = BreakpointManager.forStage(stage);
    
    bpm.currentBreakpointProperty().addListener((obs, oldVal, newVal) -> {
        switch (newVal) {
            case "sm":
                text.setFont(Font.font(12));
                break;
            case "md":
                text.setFont(Font.font(14));
                break;
            case "lg":
            case "xl":
                text.setFont(Font.font(16));
                break;
        }
    });
}
```

## Best Practices

1. **Install Early**: Set up responsive monitoring before showing the stage
2. **Debounce Updates**: Avoid rapid layout changes during resizing
3. **Test All Breakpoints**: Verify UI at each breakpoint size
4. **Graceful Degradation**: Ensure usability at smallest breakpoint
5. **Performance**: Minimize work in breakpoint change listeners

## Performance Considerations

- Breakpoint detection runs on every resize event
- Keep listener logic minimal
- Use property binding where possible instead of listeners
- Consider debouncing for expensive operations

## See Also

- `BreakpointManager` - Core breakpoint detection
- `ResponsiveNode` - Responsive node wrapper
- `TwStyle` - Apply breakpoint-specific utilities
- `TwLayout` - Responsive layout utilities
