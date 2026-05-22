# TailwindFX - CSS Modules

TailwindFX provides a modular styling system for JavaFX inspired by Tailwind CSS.

## Installation

### Option 1: Combined File (Recommended)

Load all styles with a single method:

```java
import tailwindfx.TailwindFX;

public class MyApp extends Application {
    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(root, 800, 600);

        // Install all TailwindFX styles
        TailwindFX.install(scene);

        stage.setScene(scene);
        stage.show();
    }
}
```

### Option 2: Essential Modules

Load only what's necessary for most applications:

```java
// Required: Variables and reset
TailwindFX.installBase(scene);

// Recommended: Controls + preset components
TailwindFX.installComponents(scene);
TailwindFX.installComponentsPreset(scene);
```

### Option 3: All Modules

```java
// Install everything (equivalent to TailwindFX.install(scene))
TailwindFX.installAll(scene);
```

### Option 4: Selective

Load only the modules you need:

```java
TailwindFX.installBase(scene);              // Required
TailwindFX.installComponents(scene);        // JavaFX controls
TailwindFX.installUtilities(scene);         // Layout, spacing
TailwindFX.installColors(scene);            // Colors and typography
TailwindFX.installEffects(scene);           // Shadows, transforms
TailwindFX.installComponentsPreset(scene);  // Cards, badges, buttons
TailwindFX.installDark(scene);              // Dark mode (optional)
```

### Available Installation Methods

| Method | Description |
|--------|-------------|
| `TailwindFX.install(scene)` | All modules (combined file) |
| `TailwindFX.installBase(scene)` | Variables and reset (**required**) |
| `TailwindFX.installComponents(scene)` | JavaFX controls |
| `TailwindFX.installUtilities(scene)` | Layout, spacing, sizing |
| `TailwindFX.installColors(scene)` | Colors and typography |
| `TailwindFX.installEffects(scene)` | Shadows, transforms, filters |
| `TailwindFX.installComponentsPreset(scene)` | Predefined components |
| `TailwindFX.installDark(scene)` | Dark mode |
| `TailwindFX.installEssentials(scene)` | Base + Components + ComponentsPreset |
| `TailwindFX.installAll(scene)` | All individual modules |

## Available Modules

| Module | Size | Description |
|--------|------|-------------|
| `tailwindfx.css` | ~300KB | **Combined file** - all modules |
| `tailwindfx-base.css` | ~40KB | CSS variables and reset (**required**) |
| `tailwindfx-components.css` | ~35KB | Styled JavaFX controls |
| `tailwindfx-utilities.css` | ~25KB | Layout, spacing, sizing, visibility |
| `tailwindfx-colors.css` | ~30KB | Colors (bg, text, border) and typography |
| `tailwindfx-effects.css` | ~20KB | Shadows, transforms, filters |
| `tailwindfx-components-preset.css` | ~25KB | Components: cards, badges, buttons |
| `tailwindfx-dark.css` | ~30KB | Dark mode |

### tailwindfx-base.css
**Size:** ~40KB  
**Description:** CSS variables and base reset. **Required by all other modules.**

Contains:
- Complete color palette (Slate, Gray, Red, Orange, Amber, Yellow, Lime, Green, Emerald, Teal, Cyan, Sky, Blue, Indigo, Violet, Purple, Fuchsia, Pink, Rose)
- Spacing variables (-sp-0 to -sp-32)
- Border radius variables (-radius-*)
- Font size variables (-font-size-*)
- Font weight variables (-font-weight-*)
- Shadow variables (-shadow-*)
- Opacity variables (-opacity-*)
- Cursor variables (-cursor-*)
- Modena theming system (-fx-base, -fx-accent, etc.)

### tailwindfx-components.css
**Size:** ~35KB  
**Description:** Automatic styles for JavaFX controls.

Contains styles for:
- Button (primary, hover, pressed, disabled)
- TextField, PasswordField, TextArea
- ComboBox, ChoiceBox, DatePicker, ColorPicker
- CheckBox, RadioButton, ToggleButton
- Slider, ProgressBar, ProgressIndicator
- ListView, TableView, TreeView
- TabPane, MenuBar, ContextMenu, Tooltip
- Separator, ToolBar, SplitPane
- TitledPane, Accordion, Pagination
- ScrollBar, ScrollPane

### tailwindfx-utilities.css
**Size:** ~25KB  
**Description:** Utility classes for layout and spacing.

Contains:
- Visibility: `.hidden`, `.visible`, `.opacity-*`
- Padding: `.p-4`, `.px-4`, `.py-4`, `.pt-4`, etc.
- Gap: `.gap-4`, `.gap-x-4`, `.gap-y-4`
- Sizing: `.w-full`, `.h-screen`, `.min-w-0`, `.max-w-md`
- Z-index: `.z-0`, `.z-10`, `.z-50`
- Overflow: `.overflow-hidden`, `.overflow-scroll`
- Cursor: `.cursor-pointer`, `.cursor-text`

### tailwindfx-colors.css
**Size:** ~30KB  
**Description:** Color and typography classes.

Contains:
- Background: `.bg-gray-*`, `.bg-blue-*`, `.bg-red-*`, etc.
- Text: `.text-gray-*`, `.text-blue-*`, `.text-white`
- Border: `.border-*`, `.border-color-*`
- Border radius: `.rounded`, `.rounded-lg`, `.rounded-full`
- Font size: `.text-xs` to `.text-5xl`
- Font weight: `.font-bold`, `.font-medium`, etc.
- Text alignment: `.text-center`, `.text-left`, `.text-right`
- Text decoration: `.underline`, `.line-through`

### tailwindfx-effects.css
**Size:** ~20KB  
**Description:** Visual effects and transformations.

Contains:
- Shadows: `.shadow`, `.shadow-lg`, `.shadow-xl`
- Rotate: `.rotate-45`, `.rotate-90`, `.rotate-180`
- Scale: `.scale-95`, `.scale-105`, `.scale-110`
- Translate: `.translate-x-4`, `.translate-y-4`
- Blur: `.blur-sm`, `.blur`, `.blur-lg`
- Filters: `.brightness-*`, `.contrast-*`, `.grayscale`, `.invert`, `.sepia`

### tailwindfx-components-preset.css
**Size:** ~25KB  
**Description:** Pre-built UI components.

Contains:
- Buttons: `.btn`, `.btn-primary`, `.btn-secondary`, `.btn-success`, `.btn-danger`
- Cards: `.card`, `.card-header`, `.card-body`, `.card-footer`
- Badges: `.badge`, `.badge-blue`, `.badge-green`, `.badge-red`
- Alerts: `.alert`, `.alert-success`, `.alert-error`, `.alert-warning`
- Inputs: `.input`, `.input-error`, `.input-success`
- Avatars: `.avatar`, `.avatar-sm`, `.avatar-lg`, `.avatar-group`
- Modals: `.modal`, `.modal-overlay`
- Toasts: `.toast`, `.toast-success`, `.toast-error`
- Glassmorphism: `.glass`, `.glass-dark`
- Neumorphism: `.neumorph`, `.neumorph-pressed`

### tailwindfx-dark.css
**Size:** ~30KB  
**Description:** Dark mode variants.

Contains:
- `.dark` selector to activate dark mode
- Color overrides for all components
- Dark mode-specific variables
- Support for preset components

## Using CSS Classes

Once styles are installed, you can use CSS classes in your components:

```java
// Primary button
Button btn = new Button("Save");
btn.getStyleClass().add("btn-primary");

// Card
VBox card = new VBox();
card.getStyleClass().add("card");

// Badge
Label badge = new Label("New");
badge.getStyleClass().addAll("badge", "badge-blue");

// Input with error
TextField input = new TextField();
input.getStyleClass().add("input-error");

// Dark mode
root.getStyleClass().add("dark");
```

## Customization

### Changing Theme Colors

```java
// Change global accent color
scene.getRoot().setStyle("-fx-accent: #3b82f6;");

// Change base color (affects all controls)
scene.getRoot().setStyle("-fx-base: #1e293b;");
```

### Using Preset Themes

```java
import tailwindfx.TailwindFX;

// Dark theme
TailwindFX.theme(scene).dark().apply();

// Blue theme
TailwindFX.theme(scene).preset("blue").apply();

// Custom theme
TailwindFX.theme(scene)
    .base("#1e293b")
    .accent("#3b82f6")
    .apply();
```

## File Structure

```
tailwindfx/
├── tailwindfx.css                    # Combined file (all modules)
├── tailwindfx-base.css               # Variables and reset (required)
├── tailwindfx-components.css         # JavaFX controls
├── tailwindfx-utilities.css          # Layout, spacing, sizing
├── tailwindfx-colors.css             # Colors and typography
├── tailwindfx-effects.css            # Shadows, transforms, filters
├── tailwindfx-components-preset.css  # Pre-built components
├── tailwindfx-dark.css               # Dark mode
└── MODULES.md                        # This documentation
```

## Performance

| Configuration | Total Size | Load Time |
|---------------|------------|-----------|
| Base only     | ~40KB      | ~50ms     |
| Base + Components | ~75KB  | ~80ms     |
| All modules   | ~200KB     | ~150ms    |

*Measured on Intel i7, SSD, JavaFX 21*

## Migrating from Single tailwindfx.css

If you already use `tailwindfx.css`, you don't need to change anything. The modular files are optional.

**Recommended configuration:**

```java
// Option A: All-inclusive (simplest)
TailwindFX.install(scene);

// Option B: Essential modules
TailwindFX.installEssentials(scene);

// Option C: Selective
TailwindFX.installBase(scene);
TailwindFX.installComponents(scene);
TailwindFX.installComponentsPreset(scene);
TailwindFX.installDark(scene); // optional
```

## Support

- Full documentation: [README.md](../README.md)
- Examples: [TailwindFXExample.java](../TailwindFXExample.java)
- Issues: https://github.com/yasmramos/tailwindfx/issues
