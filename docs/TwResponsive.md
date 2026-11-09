# TwResponsive Documentation

The `TwResponsive` class is a responsive facade for breakpoint management in TailwindFX. It enables responsive design patterns by monitoring scene/region size changes and applying breakpoint-based styles.

## Overview

`TwResponsive` provides responsive design capabilities by installing breakpoint monitoring on stages or regions. It delegates to `BreakpointManager` for breakpoint detection and `ResponsiveNode` for reactive behavior.

## Key Features

- **Stage-based Monitoring**: Install responsive support on entire stages
- **Region-based Monitoring**: Monitor specific regions for size changes
- **Breakpoint Detection**: Automatic detection of `SM`, `MD`, `LG`, `XL`, `XXL` breakpoints
- **Reactive Updates**: Property-based notifications for breakpoint changes
- **Scene Integration**: Works with JavaFX scenes and stages

## Basic Usage

### Stage-based Responsive Support

```java
// Install responsive support on the scene root of a stage
TwResponsive.on(stage);

// Get the breakpoint manager for the stage
BreakpointManager bpm = TwResponsive.forStage(stage);

// Listen for breakpoint changes (activeBreakpointProperty is a
// ReadOnlyObjectProperty<BreakpointManager.Breakpoint>)
bpm.activeBreakpointProperty().addListener((obs, oldVal, newVal) -> {
    System.out.println("Current breakpoint: " + newVal);

    switch (newVal) {
        case SM: applyMobileLayout(); break;
        case MD: applyTabletLayout(); break;
        case LG: applyDesktopLayout(); break;
        default: break;
    }
});
```

### Region-based Responsive Support

```java
// Install responsive support on a region and declare breakpoint classes
ResponsiveNode.on(region)
    .base("p-4")            // always applied
    .md("p-8")              // applied at >= 768px
    .lg("p-12")             // applied at >= 1024px
    .onBreakpoint(bp -> System.out.println("active width: " + bp))
    .install(region.getScene());
```

### Access Breakpoint Manager

```java
// Get manager for a stage
BreakpointManager manager = TwResponsive.forStage(stage);

// Get the current breakpoint (enum value)
BreakpointManager.Breakpoint currentBp = manager.current();

// Query relative positions
boolean desktop = manager.is(BreakpointManager.Breakpoint.LG);
boolean belowTablet = manager.below(BreakpointManager.Breakpoint.MD);
```

## Advanced Usage

### Conditional Styling

```java
stage.widthProperty().addListener((obs, oldVal, newVal) -> {
    BreakpointManager.Breakpoint bp =
        BreakpointManager.Breakpoint.forWidth(newVal.doubleValue());

    if (bp == BreakpointManager.Breakpoint.SM) {
        // Mobile: stack vertically
        container.setOrientation(Orientation.VERTICAL);
        sidebar.setVisible(false);
    } else if (bp == BreakpointManager.Breakpoint.LG) {
        // Desktop: horizontal layout with sidebar
        container.setOrientation(Orientation.HORIZONTAL);
        sidebar.setVisible(true);
    }
});
```

### Responsive Grid

```java
public void setupResponsiveGrid(GridPane grid, Stage stage) {
    BreakpointManager bpm = TwResponsive.forStage(stage);

    bpm.activeBreakpointProperty().addListener((obs, oldVal, newVal) -> {
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

private int getColumnsForBreakpoint(BreakpointManager.Breakpoint breakpoint) {
    switch (breakpoint) {
        case SM: return 1;
        case MD: return 2;
        case LG: return 3;
        case XL:
        case XXL: return 4;
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
        BreakpointManager.Breakpoint bp =
            BreakpointManager.Breakpoint.forWidth(width);

        if (bp == BreakpointManager.Breakpoint.SM) {
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

Default TailwindFX breakpoints (enum constants on `BreakpointManager.Breakpoint`):

| Breakpoint | Min Width | CSS class | Typical Device |
|------------|-----------|-----------|----------------|
| `XS` | 0px | — | any |
| `SM` | 640px | `bp-sm` | Large phones |
| `MD` | 768px | `bp-md` | Tablets |
| `LG` | 1024px | `bp-lg` | Laptops |
| `XL` | 1280px | `bp-xl` | Desktops |
| `XXL` | 1536px | `bp-2xl` | Large screens |

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
        BreakpointManager.Breakpoint bp =
            BreakpointManager.Breakpoint.forWidth(containerWidth);
        double cardWidth;

        switch (bp) {
            case XS:
            case SM:
                cardWidth = containerWidth - 32; // Full width minus padding
                break;
            case MD:
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
    private BreakpointManager.Breakpoint currentBreakpoint = BreakpointManager.Breakpoint.LG;

    public SidebarLayout() {
        sidebar = new VBox(8);
        sidebar.setPrefWidth(256);
        sidebar.getStyleClass().add("bg-gray-800");

        setLeft(sidebar);

        // Monitor width changes
        widthProperty().addListener((obs, oldVal, newVal) -> {
            BreakpointManager.Breakpoint bp =
                BreakpointManager.Breakpoint.forWidth(newVal.doubleValue());
            if (bp != currentBreakpoint) {
                currentBreakpoint = bp;
                updateSidebar(bp);
            }
        });
    }

    private void updateSidebar(BreakpointManager.Breakpoint breakpoint) {
        if (breakpoint == BreakpointManager.Breakpoint.SM
            || breakpoint == BreakpointManager.Breakpoint.MD) {
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
    BreakpointManager bpm = TwResponsive.forStage(stage);

    bpm.activeBreakpointProperty().addListener((obs, oldVal, newVal) -> {
        switch (newVal) {
            case XS:
            case SM:
                text.setFont(Font.font(12));
                break;
            case MD:
                text.setFont(Font.font(14));
                break;
            case LG:
            case XL:
            case XXL:
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

- `BreakpointManager` - Core breakpoint detection (`current()`, `is()`, `below()`, `activeBreakpointProperty()`)
- `ResponsiveNode` - Responsive node wrapper (`on(Node)`, `base()`, `md()`, `install(Scene)`)
- `TwStyle` - Apply breakpoint-specific utilities
- `TwLayout` - Responsive layout utilities
