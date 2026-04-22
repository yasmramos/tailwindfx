# TailwindFX Examples

This directory contains example applications demonstrating TailwindFX capabilities.

## Examples

### DashboardApp

A complete advanced dashboard showing various TailwindFX features with real data visualization.

**Run:**
```bash
mvn exec:java -Dexec.mainClass=tailwindfx.examples.DashboardApp
```

**Features demonstrated:**
- ✓ **Real Line Charts** - Canvas-based charts with multiple datasets, area fills, and data points
- ✓ **Donut/Pie Charts** - Interactive pie charts with legend and percentages
- ✓ **Sparklines** - Mini inline charts in stat cards showing trends
- ✓ **Toast Notifications** - Animated notification system with different types (success, error, warning, info)
- ✓ **Enhanced Data Table** - Sortable, searchable, paginated table with FxDataTable
- ✓ **Calendar Widget** - Interactive calendar with upcoming events
- ✓ **Activity Feed** - Real-time activity tracking
- ✓ **Modal Dialogs** - Popup dialogs for details and confirmations
- ✓ **Skeleton Loading States** - Animated loading placeholders
- ✓ **Enhanced Dropdowns** - Rich dropdown menus for notifications and user profile
- ✓ **Keyboard Shortcuts** - Ctrl+K for search
- ✓ **Interactive Charts** - Hover effects and period selectors
- ✓ Layout builders (HBox, VBox, GridPane)
- ✓ Color utilities (`bg-*`, `text-*`)
- ✓ Spacing utilities (`p-*`, `m-*`, `gap-*`)
- ✓ Shadows and effects
- ✓ Components (cards, badges, buttons, avatars)
- ✓ Form elements (input, checkbox, choice)
- ✓ Dark mode support

**Screenshot:**
```
┌─────────────────────────────────────────────────────────┐
│  [Avatar] Welcome back, John!      [New Report] [Export]│
│           Here's what's happening                       │
├─────────────────────────────────────────────────────────┤
│ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│ │ Revenue  │ │  Users   │ │ Bounce   │ │ Session  │   │
│ │ $45,231  │ │  2,350   │ │  12.5%   │ │  4m 32s  │   │
│ │ +20.1% 📈│ │ +15.2% 📈│ │ -3.2% 📉 │ │ +8.4% 📈 │   │
│ └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
├───────────────────────────┬───────────────────────────┤
│  Revenue Overview         │   Recent Activity         │
│  ┌─────────────────────┐  │  ● New user registered    │
│  │                     │  │  ● Payment received       │
│  │    [Chart Area]     │  │  ● Report generated       │
│  │                     │  │  ● System update          │
│  └─────────────────────┘  │  ● Backup completed       │
└───────────────────────────┴───────────────────────────┘
```

## Usage in Your Project

### Basic Setup

```java
import tailwindfx.TailwindFX;
import tailwindfx.examples.Dashboard;

public class MyApp extends Application {
    @Override
    public void start(Stage stage) {
        // Use pre-built dashboard
        VBox dashboard = Dashboard.create();
        
        Scene scene = new Scene(dashboard, 1200, 800);
        TailwindFX.install(scene); // Install all styles
        
        stage.setScene(scene);
        stage.show();
    }
}
```

### Modular Setup

```java
// Load only essential modules
TailwindFX.installBase(scene);
TailwindFX.installComponents(scene);
TailwindFX.installComponentsPreset(scene);

// Optional: dark mode
// TailwindFX.installDark(scene);
```

### Enable Dark Mode

```java
// Add dark class to root
root.getStyleClass().add("dark");

// Or use ThemeManager
TailwindFX.theme(scene).dark().apply();
```

## Components Used

### Buttons
```java
Button primary = new Button("Save");
TailwindFX.apply(primary, "btn", "btn-primary");

Button secondary = new Button("Cancel");
TailwindFX.apply(secondary, "btn", "btn-secondary");
```

### Cards
```java
VBox card = new VBox();
TailwindFX.apply(card, "card", "shadow-lg");
```

### Badges
```java
Label badge = new Label("New");
TailwindFX.apply(badge, "badge", "badge-blue");
```

### Avatars
```java
StackPane avatar = ComponentFactory.avatar("JD", "blue", 48);
```

### Inputs
```java
TextField input = new TextField();
TailwindFX.apply(input, "input");
```

## Customization

### Custom Colors
```java
// Override accent color
scene.getRoot().setStyle("-fx-accent: #3b82f6;");
```

### Custom Spacing
```java
// Use arbitrary values
TailwindFX.jit(node, "p-[13px]", "m-[5px]");
```

### Custom Shadows
```java
TailwindFX.jit(node, "shadow-[0_10px_40px_rgba(0,0,0,0.1)]");
```

## More Examples

Coming soon:
- Login Page
- Settings Page
- Data Table with Pagination
- Form Validation
- Modal Dialogs
- Toast Notifications

## Support

- Documentation: [MODULOS.md](../tailwindfx/MODULOS.md)
- Main README: [../../README.md](../../README.md)
- Issues: https://github.com/yasmramos/tailwindfx/issues
